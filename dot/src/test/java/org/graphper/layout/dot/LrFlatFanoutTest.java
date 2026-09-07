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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.attributes.Rankdir;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.LayoutAttach;
import org.graphper.parser.DotParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LrFlatFanoutTest {

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void fullLayoutRemovesAlternatingPairsWithoutChangingRanksOrSplittingCluster(boolean quick)
      throws Exception {
    Graphviz graph;
    try (InputStream in = getClass().getResourceAsStream("/lr-flat-fanout.dot")) {
      Assertions.assertNotNull(in);
      graph = DotParser.parse(in, StandardCharsets.UTF_8);
    }
    Assertions.assertEquals(Rankdir.LR, graph.graphAttrs().getRankdir());
    List<String> oldOrder = Arrays.asList("sh0015", "sh0014", "sh0013", "sh0012", "sh0011", "sh0010");
    Assertions.assertEquals(2, alternatingPairs(graph, oldOrder));
    List<String> internalOrder = new ArrayList<>();
    DrawGraph draw = new DotLayoutEngine(quick) {
      @Override
      protected void layout(DrawGraph draw, LayoutAttach attach) throws ExecuteException {
        super.layout(draw, attach);
        DotAttachment attachment = (DotAttachment) attach;
        List<DNode> nodes = new ArrayList<>();
        attachment.getDotDigraph().forEach(nodes::add);
        nodes.sort(Comparator.comparingInt(DNode::getRankIndex));
        Assertions.assertEquals(6, nodes.size());
        BasicCrossRank order = new BasicCrossRank(graph);
        RootCrossRank root = new RootCrossRank(draw, new MinCross.ClusterMerge());
        for (DNode node : nodes) {
          Assertions.assertEquals(1, node.getRank(), "all minlen=0 nodes must retain rank 1");
          Assertions.assertEquals(nodes.get(0).getY(), node.getY(), 0.01,
              "rank centers stay aligned before LR rendering transforms");
          order.addNode(node);
          root.addNode(node);
          internalOrder.add(node.getNode().nodeAttrs().getId());
        }
        attachment.getDotDigraph().edges().forEach(root::addEdge);
        Assertions.assertEquals(0, alternatingPairs(graph, internalOrder), internalOrder.toString());
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        root.setCacheExpired();
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        Assertions.assertTrue(ClusterOrderConsistency.violations(order, attachment).isEmpty());
        Assertions.assertTrue(ClusterOrderConsistency.contiguityViolations(order, attachment).isEmpty());
      }
    }.layout(graph);
    Map<String, NodeDrawProp> nodes = new HashMap<>();
    draw.nodes(true).forEach(n -> nodes.put(n.getNode().nodeAttrs().getId(), n));
    Assertions.assertEquals(6, nodes.size(), "exclude the cluster label's HTML cell nodes");
    List<String> positioned = new ArrayList<>(nodes.keySet());
    positioned.sort(Comparator.comparingDouble(id -> nodes.get(id).getY()));
    List<String> flippedOrder = new ArrayList<>(internalOrder);
    Collections.reverse(flippedOrder);
    Assertions.assertEquals(flippedOrder, positioned, "LR positioning preserves the internal order");
    Assertions.assertEquals(0, alternatingPairs(graph, positioned), positioned.toString());
    for (int i = 1; i < positioned.size(); i++) {
      NodeDrawProp before = nodes.get(positioned.get(i - 1));
      NodeDrawProp after = nodes.get(positioned.get(i));
      Assertions.assertEquals(before.getX(), after.getX(), 0.01, "LR flat rank stays aligned");
      Assertions.assertTrue(before.getDownBorder() < after.getUpBorder(), "nodes must not overlap");
    }
    List<String> targets = Arrays.asList("sh0010", "sh0011", "sh0012", "sh0013");
    int first = positioned.size();
    int last = -1;
    for (String target : targets) {
      first = Math.min(first, positioned.indexOf(target));
      last = Math.max(last, positioned.indexOf(target));
    }
    Assertions.assertEquals(3, last - first, "positioning keeps the four cluster members contiguous");
    Assertions.assertEquals(4, draw.lines().size());
    draw.lines().forEach(line -> Assertions.assertFalse(line.isEmpty(), "edges must be routed"));
  }

  private static int alternatingPairs(Graphviz graph, List<String> order) {
    List<Line> lines = new ArrayList<>();
    graph.lines().forEach(lines::add);
    Assertions.assertEquals(4, lines.size());
    int count = 0;
    for (int i = 0; i < lines.size(); i++) {
      for (int j = i + 1; j < lines.size(); j++) {
        Line a = lines.get(i);
        Line b = lines.get(j);
        if (a.tail() == b.tail()) {
          continue;
        }
        int low = Math.min(order.indexOf(a.tail().nodeAttrs().getId()),
            order.indexOf(a.head().nodeAttrs().getId()));
        int high = Math.max(order.indexOf(a.tail().nodeAttrs().getId()),
            order.indexOf(a.head().nodeAttrs().getId()));
        int tail = order.indexOf(b.tail().nodeAttrs().getId());
        int head = order.indexOf(b.head().nodeAttrs().getId());
        if ((low < tail && tail < high) != (low < head && head < high)) {
          count++;
        }
      }
    }
    return count;
  }
}
