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
import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.LayoutAttach;
import org.graphper.parser.DotParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LabeledFlatClusterOrderTest {

  private Graphviz fixture() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("/labeled-flat-cluster-order.dot")) {
      Assertions.assertNotNull(in);
      return DotParser.parse(in, StandardCharsets.UTF_8);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void positioningPreservesNodeAndNestedClusterOrder(boolean quick) throws Exception {
    DrawGraph draw = new DotLayoutEngine(quick).layout(fixture());
    Map<String, NodeDrawProp> nodes = new HashMap<>();
    draw.nodes().forEach(n -> nodes.put(n.getNode().nodeAttrs().getId(), n));
    String[] order = {"sh0022", "sh0017", "sh0010", "sh0011", "sh0012"};
    for (int i = 1; i < order.length; i++) {
      NodeDrawProp left = nodes.get(order[i - 1]);
      NodeDrawProp right = nodes.get(order[i]);
      Assertions.assertEquals(left.getY(), right.getY(), 0.01);
      Assertions.assertTrue(left.getRightBorder() < right.getLeftBorder());
    }
    Assertions.assertTrue(nodes.get("sh0024").getY() < nodes.get("sh0023").getY());
    Assertions.assertTrue(nodes.get("sh0023").getY() < nodes.get("sh0022").getY());

    Map<String, ClusterDrawProp> clusters = new HashMap<>();
    draw.clusters().forEach(c -> clusters.put(c.getCluster().id(), c));
    // Node bounds alone do not prove that the wide labels and nested cluster boxes stay separate.
    for (String suffix : new String[]{"p0", "", "p1"}) {
      ClusterDrawProp first = clusters.get("cluster18" + suffix);
      ClusterDrawProp middle = clusters.get("cluster13" + suffix);
      ClusterDrawProp last = clusters.get("cluster6" + suffix);
      Assertions.assertTrue(first.getRightBorder() < middle.getLeftBorder());
      Assertions.assertTrue(middle.getRightBorder() < last.getLeftBorder());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void labelCanMoveWithoutSacrificingCrossingsOrChangingRanks(boolean quick) throws Exception {
    Graphviz graph = fixture();
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
        Map<DNode, Integer> assigned = new HashMap<>();
        digraph.forEach(n -> assigned.put(n, n.getRank()));
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
            nodes.put(name(node), node);
            Assertions.assertEquals(i, node.getRankIndex());
          }
        }
        root.setBasicCrossRank(ordered);
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        Assertions.assertEquals(1, nodes.get("sh0024").getRank());
        Assertions.assertEquals(2, nodes.get("sh0023").getRank());
        DNode label = nodes.get("label(sh0024->sh0017)");
        Assertions.assertNotNull(label);
        Assertions.assertEquals(2, label.getRank());
        Assertions.assertEquals(0, ordered.getRankIndex(nodes.get("sh0023")));
        Assertions.assertEquals(1, ordered.getRankIndex(label));
        String[] order = {"sh0022", "sh0017", "sh0010", "sh0011", "sh0012"};
        for (int i = 0; i < order.length; i++) {
          Assertions.assertEquals(3, nodes.get(order[i]).getRank());
          Assertions.assertEquals(i, ordered.getRankIndex(nodes.get(order[i])));
        }
        assigned.forEach((n, r) -> Assertions.assertEquals(r.intValue(), n.getRank()));
        Assertions.assertTrue(ClusterOrderConsistency.violations(ordered, attachment).isEmpty());
        Assertions.assertTrue(ClusterOrderConsistency.contiguityViolations(ordered, attachment).isEmpty());

        // Reconstruct the rejected candidate: only the rank-2 label is on its original side.
        BasicCrossRank unrelaxed = ordered.clone();
        unrelaxed.exchange(label, nodes.get("sh0023"), false);
        Assertions.assertEquals(1, root.tryCacheCrossNum(unrelaxed).getCrossNum(),
            "preferred cluster order alone really increases the internal crossing count");

        // Reconstruct the measured pre-fix 13 -> 6 -> 18 order, including its label position.
        BasicCrossRank before = unrelaxed.clone();
        for (int i = 1; i < order.length; i++) {
          before.exchange(nodes.get("sh0022"), nodes.get(order[i]), false);
        }
        Assertions.assertEquals(0, root.tryCacheCrossNum(before).getCrossNum());
      }

      @Override
      protected void afterLayoutShifter(LayoutAttach attach) { }

      @Override
      protected void afterRenderShifter(LayoutAttach attach) { }
    }.layout(graph);
  }

  private static String name(DNode n) {
    return !n.isVirtual() ? n.getNode().nodeAttrs().getId() : n.isLabelNode()
        ? "label(" + n.getLabelLine().tail().nodeAttrs().getId() + "->"
            + n.getLabelLine().head().nodeAttrs().getId() + ")" : "virtual";
  }
}
