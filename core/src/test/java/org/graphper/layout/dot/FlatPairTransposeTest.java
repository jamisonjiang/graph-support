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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FlatPairTransposeTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5})
  void pairMovesInBothDirectionsWithoutCrossingContainerOrPrecedenceBoundaries(int scenario) {
    boolean reverse = scenario % 2 == 1;
    boolean clusterBoundary = scenario / 2 == 1;
    boolean precedenceBoundary = scenario / 2 == 2;
    Node a = Node.builder().id("a").build();
    Node b = Node.builder().id("b").build();
    Node c = Node.builder().id("c").build();
    Node x = Node.builder().id("x").build();
    Node y = Node.builder().id("y").build();
    Cluster pair = Cluster.builder().addNode(a, b).build();
    Cluster other = Cluster.builder().addNode(c).build();
    Line flat = Line.builder(reverse ? b : a, reverse ? a : b).minlen(0).build();
    Graphviz graph = Graphviz.digraph().cluster(Cluster.builder().cluster(pair).cluster(other).build())
        .addNode(x, y).addLine(flat).addLine(a, y).addLine(c, x).build();
    DrawGraph draw = new DrawGraph(graph);
    Map<Node, DNode> nodes = new HashMap<>();
    DotDigraph digraph = new DotDigraph(5, graph, nodes);
    DotAttachment attachment = new DotAttachment(digraph, draw, nodes);
    RootCrossRank root = new RootCrossRank(draw, new MinCross.ClusterMerge());
    for (Node node : reverse ? new Node[]{c, b, a, y, x} : new Node[]{a, b, c, x, y}) {
      DNode dn = new DNode(new NodeDrawProp(node, node.nodeAttrs()), 20, 20, 20);
      dn.setRank(node == x || node == y ? 2 : 1);
      digraph.add(dn, dn.getRank() == 2 ? graph : node == c && clusterBoundary ? other : pair);
      root.addNode(dn);
      dn.setRankIndex(root.getRankIndex(dn));
    }
    SameRankAdjacentRecord preferences = new SameRankAdjacentRecord();
    for (Line line : graph.lines()) {
      DLine edge = new DLine(nodes.get(line.tail()), nodes.get(line.head()),
          new LineDrawProp(line, line.lineAttrs(), draw), 1, line == flat ? 0 : 1);
      root.addEdge(edge);
      if (line == flat) {
        preferences.addOutAdjacent(edge.from(), edge);
      }
    }
    if (precedenceBoundary) {
      DNode from = nodes.get(reverse ? c : a);
      DNode to = nodes.get(reverse ? a : c);
      preferences.addOutAdjacent(from, new DLine(from, to, null, 1, 0));
    }
    root.resetToRoot();
    root.setSameRankAdjacentRecord(preferences);
    Assertions.assertEquals(1, root.crossSnapshot().getCrossNum());
    root.transposeFlatPairs();
    int expected = clusterBoundary || precedenceBoundary ? 1 : 0;
    Assertions.assertEquals(expected, root.crossSnapshot().getCrossNum());
    root.setCacheExpired();
    Assertions.assertEquals(expected, root.crossSnapshot().getCrossNum(), "cache equals fresh count");
    Assertions.assertTrue(ClusterOrderConsistency.violations(root.getBasicCrossRank(), attachment).isEmpty());
    Assertions.assertTrue(ClusterOrderConsistency.contiguityViolations(root.getBasicCrossRank(), attachment).isEmpty());
    for (Node node : Arrays.asList(a, b, c, x, y)) {
      Assertions.assertEquals(node == x || node == y ? 2 : 1, nodes.get(node).getRank());
      Assertions.assertEquals(root.getRankIndex(nodes.get(node)), nodes.get(node).getRankIndex());
    }
    Assertions.assertTrue(reverse ? root.getRankIndex(nodes.get(b)) < root.getRankIndex(nodes.get(a))
        : root.getRankIndex(nodes.get(a)) < root.getRankIndex(nodes.get(b)));
  }
}
