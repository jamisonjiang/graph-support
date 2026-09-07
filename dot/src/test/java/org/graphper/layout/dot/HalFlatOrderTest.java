/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphper.layout.dot;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.LayoutAttach;
import org.graphper.layout.PortHelper;
import org.graphper.parser.DotParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HalFlatOrderTest {

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void positioningKeepsTheImprovedOrder(boolean quick) throws Exception {
    Graphviz graph;
    try (InputStream in = getClass().getResourceAsStream("/hal-flat-order.dot")) {
      graph = DotParser.parse(in, StandardCharsets.UTF_8);
    }
    DrawGraph draw = new DotLayoutEngine(quick).layout(graph);
    Map<String, NodeDrawProp> nodes = new HashMap<>();
    draw.nodes().forEach(n -> nodes.put(n.getNode().nodeAttrs().getId(), n));
    NodeDrawProp read = nodes.get("sh0011");
    NodeDrawProp gpio = nodes.get("sh0028");
    NodeDrawProp leaf = nodes.get("sh0036");
    Assertions.assertEquals(read.getY(), gpio.getY(), 0.01);
    Assertions.assertEquals(read.getY(), leaf.getY(), 0.01);
    Assertions.assertTrue(read.getX() < gpio.getX());
    Assertions.assertTrue(gpio.getX() < leaf.getX());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void flatLeafDoesNotTrapReadKeyBehindGpio(boolean quick) throws Exception {
    Graphviz graph;
    try (InputStream in = getClass().getResourceAsStream("/hal-flat-order.dot")) {
      graph = DotParser.parse(in, StandardCharsets.UTF_8);
    }
    new DotLayoutEngine(quick) {
      @Override
      protected void layout(DrawGraph draw, LayoutAttach attach) throws ExecuteException {
        DotAttachment attachment = (DotAttachment) attach;
        DotDigraph digraph = attachment.getDotDigraph();
        new ContainerCollapse(attachment, graph);
        try {
          Method legal = DotLayoutEngine.class.getDeclaredMethod("handleLegalLine",
              DotDigraph.class, DrawGraph.class);
          legal.setAccessible(true);
          legal.invoke(this, digraph, draw);
        } catch (ReflectiveOperationException e) {
          throw new AssertionError(e);
        }
        RankContent content = new RankContent(digraph, graph.graphAttrs().getRankSep(), true, null);
        Map<DNode, Integer> assignedRanks = new HashMap<>();
        digraph.forEach(n -> assignedRanks.put(n, n.getRank()));
        MinCross minCross = new MinCross(content, attachment, quick);
        RootCrossRank root = new RootCrossRank(draw, new MinCross.ClusterMerge());
        minCross.getDigraphProxy().forEach(root::addNode);
        minCross.getDigraphProxy().edges().forEach(root::addEdge);
        root.resetToRoot();
        BasicCrossRank ordered = new BasicCrossRank(graph);
        Map<String, DNode> nodes = new HashMap<>();
        for (RankContent.RankNode rank : content) {
          for (int i = 0; i < rank.size(); i++) {
            DNode node = rank.get(i);
            ordered.addNode(node);
            Assertions.assertEquals(i, node.getRankIndex());
            if (!node.isVirtual()) {
              nodes.put(node.getNode().nodeAttrs().getId(), node);
            }
          }
        }
        root.setBasicCrossRank(ordered);
        root.setSameRankAdjacentRecord(attachment.getSameRankAdjacentRecord());
        DNode read = nodes.get("sh0011");
        DNode gpio = nodes.get("sh0028");
        DNode leaf = nodes.get("sh0036");
        Assertions.assertEquals(1, read.getRank());
        Assertions.assertEquals(read.getRank(), gpio.getRank());
        Assertions.assertEquals(0, read.getRankIndex());
        Assertions.assertEquals(1, gpio.getRankIndex());
        Assertions.assertEquals(2, leaf.getRankIndex());
        Assertions.assertTrue(crossingPairs(root, ordered, draw).isEmpty());
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        root.setCacheExpired();
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        // Measured with the saved pre-fix JAR. Reconstruct both affected ranks, not just rank 1.
        BasicCrossRank oldLowerRanks = ordered.clone();
        List<String> oldOrder = Arrays.asList("sh0019", "sh0018", "sh0012", "sh0017",
            "sh0016", "sh0013", "sh0010", "sh0015", "sh0014", "sh0023", "sh0024");
        oldLowerRanks.sort(5, Comparator.comparingInt(n -> oldOrder.indexOf(name(n))), false);
        List<String> oldTargets = Arrays.asList("sh0050", "sh0049", "sh0057", "sh0045", "sh0062",
            "sh0055", "sh0052", "sh0046", "sh0063", "sh0047", "sh0051", "sh0048", "sh0061",
            "sh0064", "sh0065", "sh0059", "sh0066", "sh0060", "sh0054", "sh0058", "sh0053", "sh0056");
        oldLowerRanks.sort(6, Comparator.comparingInt(n -> oldTargets.indexOf(name(n))), false);
        List<String> oldPairs = crossingPairs(root, oldLowerRanks, draw);
        Assertions.assertEquals(2, oldPairs.size());
        Assertions.assertTrue(oldPairs.contains("sh0057->sh0047 / sh0052->sh0065"));
        Assertions.assertTrue(oldPairs.contains("sh0057->sh0047 / sh0055->sh0059"));
        Assertions.assertEquals(2, root.tryCacheCrossNum(oldLowerRanks).getCrossNum());
        // The improved rank-6 order makes the offending flat edge adjacent, removing both pairs.
        Assertions.assertEquals(1, Math.abs(nodes.get("sh0057").getRankIndex()
            - nodes.get("sh0047").getRankIndex()));
        Assertions.assertTrue(ClusterOrderConsistency.violations(ordered, attachment).isEmpty());
        Assertions.assertTrue(ClusterOrderConsistency.contiguityViolations(ordered, attachment).isEmpty());
        assignedRanks.forEach((node, rank) -> Assertions.assertEquals(rank.intValue(), node.getRank()));
        if (quick) {
          return;
        }

        // Preserve the original rank-1 cost barrier, now without the two lower-rank crossings.
        BasicCrossRank trapped = ordered.clone();
        trapped.exchange(read, gpio, false);
        trapped.exchange(read, leaf, false);
        Assertions.assertEquals(1, crossingPairs(root, trapped, draw).size());
        Assertions.assertEquals(1, root.tryCacheCrossNum(trapped).getCrossNum());
        BasicCrossRank swap = trapped.clone();
        swap.exchange(read, gpio, false);
        Assertions.assertEquals(0, crossingPairs(root, swap, draw).size());
        Assertions.assertEquals(0, root.tryCacheCrossNum(swap).getCrossNum(),
            "the user-proposed nonadjacent swap really lowers the internal crossing count");
        BasicCrossRank adjacent = trapped.clone();
        adjacent.exchange(leaf, read, false);
        Assertions.assertEquals(2, crossingPairs(root, adjacent, draw).size());
        Assertions.assertEquals(2, root.tryCacheCrossNum(adjacent).getCrossNum(),
            "ordinary adjacent transpose cannot cross the leaf's cost barrier");
        root.updateCross(root.tryCacheCrossNum(trapped));
        root.setSameRankAdjacentRecord(null);
        for (int i = 0; i < 8; i++) {
          root.vmedian(i);
          root.transpose(i % 4 >= 2);
        }
        Assertions.assertEquals(1, crossingPairs(root, root.getBasicCrossRank(), draw).size());
        Assertions.assertEquals(1, root.crossSnapshot().getCrossNum(),
            "relaxing flat precedence alone does not escape this minimum");
        root.setSameRankAdjacentRecord(attachment.getSameRankAdjacentRecord());
        root.transposeFlatPairs();
        Assertions.assertEquals(0, crossingPairs(root, root.getBasicCrossRank(), draw).size());
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        root.setCacheExpired();
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        Assertions.assertTrue(root.getRankIndex(read) < root.getRankIndex(gpio));
        Assertions.assertTrue(root.getRankIndex(gpio) < root.getRankIndex(leaf),
            "moving the intact pair also preserves flat precedence");
      }

      // Stop before positioning/routing: these assertions concern the internal crossing order.
      @Override
      protected void afterLayoutShifter(LayoutAttach attach) { }

      @Override
      protected void afterRenderShifter(LayoutAttach attach) { }
    }.layout(graph);
  }

  private static List<String> crossingPairs(RootCrossRank root, CrossRank order, DrawGraph draw) {
    List<DLine> edges = new ArrayList<>();
    root.getDigraphProxy().edges().forEach(edges::add);
    List<String> pairs = new ArrayList<>();
    for (int i = 0; i < edges.size(); i++) {
      for (int j = i + 1; j < edges.size(); j++) {
        DLine a = edges.get(i);
        DLine b = edges.get(j);
        if (a.from() == b.from() || a.from().getRank() != b.from().getRank()) {
          continue;
        }
        if (order.getRankIndex(a.from()) > order.getRankIndex(b.from())) {
          DLine swap = a;
          a = b;
          b = swap;
        }
        boolean cross;
        if (a.from() == b.to() || a.to() == b.from()) {
          cross = false;
        } else if (a.to() == b.to()) {
          Assertions.assertFalse(a.isSameRank() || b.isSameRank(),
              "this fixture's shared heads are nonflat; flat shared ports need a separate oracle");
          cross = PortHelper.portCompareNo(a.getLineDrawProp(), a.to(), draw)
              > PortHelper.portCompareNo(b.getLineDrawProp(), b.to(), draw);
        } else if (a.isSameRank() || b.isSameRank()) {
          DLine flat = a.isSameRank() ? a : b;
          DLine other = flat == a ? b : a;
          if (flat.getLineDrawProp() == null
              || !Integer.valueOf(0).equals(flat.lineAttrs().getMinlen())
              || other.isSameRank() && (other.getLineDrawProp() == null
              || !Integer.valueOf(0).equals(other.lineAttrs().getMinlen()))) {
            continue;
          }
          int low = Math.min(order.getRankIndex(flat.from()), order.getRankIndex(flat.to()));
          int high = Math.max(order.getRankIndex(flat.from()), order.getRankIndex(flat.to()));
          int tail = order.getRankIndex(other.from());
          int head = order.getRankIndex(other.to());
          cross = (low < tail && tail < high)
              != (other.isSameRank() && low < head && head < high);
        } else {
          cross = order.getRankIndex(a.to()) > order.getRankIndex(b.to());
        }
        if (cross) {
          pairs.add(name(a.from()) + "->" + name(a.to()) + " / "
              + name(b.from()) + "->" + name(b.to()));
        }
      }
    }
    return pairs;
  }

  private static String name(DNode node) {
    return node.isVirtual() ? "virtual" : node.getNode().nodeAttrs().getId();
  }
}
