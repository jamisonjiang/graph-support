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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MinCrossFlatOrderTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5})
  void flatChainOrdersBlocksAtTheirCommonAncestor(int shape) throws ExecuteException {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Graphviz.GraphvizBuilder builder = Graphviz.digraph();
    if (shape == 0) {
      builder.addNode(d, c, b, a);
    } else if (shape == 1) {
      builder.addNode(a).cluster(Cluster.builder().addNode(d, c, b).build());
    } else if (shape == 2) {
      builder.addNode(a).cluster(Cluster.builder().addNode(b).build())
          .cluster(Cluster.builder().addNode(d, c).build());
    } else if (shape == 3) {
      builder.cluster(Cluster.builder().addNode(b, a).build())
          .cluster(Cluster.builder().addNode(d, c).build());
    } else if (shape == 4) {
      builder.cluster(Cluster.builder().addNode(a)
          .cluster(Cluster.builder().addNode(b).build())
          .cluster(Cluster.builder().cluster(Cluster.builder().addNode(d, c).build()).build())
          .build());
    } else {
      builder.addNode(a).cluster(Cluster.builder().addNode(b)
          .cluster(Cluster.builder().addNode(d, c).build()).build());
    }
    builder.addLine(flat(a, b)).addLine(flat(b, c)).addLine(flat(c, d));
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(builder.build());
    Node[] chain = {a, b, c, d};
    for (int i = 1; i < chain.length; i++) {
      Assertions.assertEquals(draw.getNodeDrawProp(a).getY(),
          draw.getNodeDrawProp(chain[i]).getY(), 0.01, "minlen=0 ranks must stay equal");
      Assertions.assertTrue(draw.getNodeDrawProp(chain[i - 1]).getX()
          < draw.getNodeDrawProp(chain[i]).getX(),
          "shape " + shape + ": chain node " + (i - 1) + " must precede " + i);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void intermediateRankFlatEdgeOrdersWholeMultiRankBlocks(boolean quick) {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Node e = node("e");
    Node f = node("f");
    Cluster left = Cluster.builder().addNode(a, b, c).build();
    Cluster right = Cluster.builder().addNode(d, e, f).build();
    Graphviz graph = Graphviz.digraph().cluster(right).cluster(left)
        .addLine(Line.builder(a, b).build()).addLine(Line.builder(b, c).build())
        .addLine(Line.builder(d, e).build()).addLine(Line.builder(e, f).build())
        .addLine(flat(b, e)).build();
    Map<Node, DNode> result = mincross(graph, new Node[]{d, e, f, a, b, c},
        new int[]{1, 2, 3, 1, 2, 3},
        new GraphContainer[]{right, right, right, left, left, left}, quick);
    before(result, a, d);
    before(result, b, e);
    before(result, c, f);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void cyclesAndIncompatibleBlockConstraintsTerminateWithoutSplittingClusters(boolean quick) {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Node e = node("e");
    Cluster inner = Cluster.builder().addNode(c, a).build();
    Cluster outer = Cluster.builder().cluster(inner).addNode(d).build();
    Graphviz graph = Graphviz.digraph().cluster(outer).addNode(b, e)
        .addLine(flat(a, b)).addLine(flat(b, c)).addLine(flat(c, a))
        .addLine(flat(c, d)).addLine(flat(d, e)).build();
    Map<Node, DNode> first = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5),
        () -> mincross(graph, new Node[]{e, d, c, b, a}, new int[]{1, 1, 1, 1, 1},
            new GraphContainer[]{graph, outer, inner, graph, inner}, quick));
    Map<Node, DNode> second = mincross(graph, new Node[]{e, d, c, b, a},
        new int[]{1, 1, 1, 1, 1}, new GraphContainer[]{graph, outer, inner, graph, inner}, quick);
    for (Node node : new Node[]{a, b, c, d, e}) {
      Assertions.assertEquals(first.get(node).getRankIndex(), second.get(node).getRankIndex(),
          "cycle breaking must be deterministic");
    }
    List<Node> actual = new ArrayList<>(Arrays.asList(a, b, c, d, e));
    actual.sort(Comparator.comparingInt(n -> first.get(n).getRankIndex()));
    before(first, c, a); // Acyclic preference inside the inner cluster must survive.
    Assertions.assertEquals(3, flatCrossings(graph, Arrays.asList(e, c, a, d, b)),
        "the old c < d arrangement was not crossing-optimal");
    int legalOrders = 0;
    int minimum = Integer.MAX_VALUE;
    int forwardMinimum = Integer.MAX_VALUE;
    int retainedForwardMinimum = Integer.MAX_VALUE;
    // Exhaust the 5! orders only in this tiny regression, keeping both cluster blocks contiguous.
    for (int code = 0; code < 3125; code++) {
      List<Node> order = new ArrayList<>();
      int digits = code;
      for (int i = 0; i < 5; i++, digits /= 5) {
        Node node = Arrays.asList(a, b, c, d, e).get(digits % 5);
        if (!order.contains(node)) {
          order.add(node);
        }
      }
      if (order.size() != 5 || Math.abs(order.indexOf(a) - order.indexOf(c)) != 1) {
        continue;
      }
      int low = Math.min(order.indexOf(a), Math.min(order.indexOf(c), order.indexOf(d)));
      int high = Math.max(order.indexOf(a), Math.max(order.indexOf(c), order.indexOf(d)));
      if (high - low != 2) {
        continue;
      }
      legalOrders++;
      int crossings = flatCrossings(graph, order);
      minimum = Math.min(minimum, crossings);
      if (order.indexOf(c) < order.indexOf(d)) {
        forwardMinimum = Math.min(forwardMinimum, crossings);
        if (order.indexOf(c) < order.indexOf(a)) {
          retainedForwardMinimum = Math.min(retainedForwardMinimum, crossings);
        }
      }
    }
    Assertions.assertEquals(24, legalOrders);
    Assertions.assertEquals(0, minimum);
    Assertions.assertEquals(0, forwardMinimum, "c < d alone can tie, but reverses c < a");
    Assertions.assertEquals(1, retainedForwardMinimum);
    Assertions.assertEquals(minimum, flatCrossings(graph, actual),
        "cycle breaking must attain the legal crossing optimum, not just terminate");
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void mixedMinlenAndNonConstraintEdgesLeaveAssignedRanksUntouched(boolean quick) {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Cluster cluster = Cluster.builder().addNode(c, b).build();
    Graphviz graph = Graphviz.digraph().addNode(a, d).cluster(cluster)
        .addLine(flat(a, b)).addLine(Line.builder(b, c).minlen(1).build())
        .addLine(flat(c, d))
        .addLine(Line.builder(a, c).minlen(0).constraint(false).build()).build();
    Map<Node, DNode> result = mincross(graph, new Node[]{b, a, d, c}, new int[]{1, 1, 2, 2},
        new GraphContainer[]{cluster, graph, graph, cluster}, quick);
    before(result, a, b);
    before(result, c, d);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void crossContainerFlatPreferenceDoesNotOverrideLowerCrossingCost(boolean quick) {
    Node x = node("x");
    Node y = node("y");
    Node a = node("a");
    Node b = node("b");
    Cluster cluster = Cluster.builder().addNode(b).build();
    Graphviz graph = Graphviz.digraph().addNode(x, y, a).cluster(cluster)
        .addLine(flat(x, y)).addLine(Line.builder(x, b).build())
        .addLine(Line.builder(y, a).build()).addLine(flat(a, b)).build();
    Map<Node, DNode> result = mincross(graph, new Node[]{x, y, b, a},
        new int[]{1, 1, 2, 2}, new GraphContainer[]{graph, graph, cluster, graph}, quick);
    before(result, x, y);
    before(result, b, a);
    // x -> b and y -> a must not cross just to satisfy the flat preference a -> b.
    Assertions.assertTrue((result.get(x).getRankIndex() - result.get(y).getRankIndex())
        * (result.get(b).getRankIndex() - result.get(a).getRankIndex()) > 0);
  }

  private static Map<Node, DNode> mincross(Graphviz graph, Node[] nodes, int[] ranks,
                                         GraphContainer[] containers, boolean quick) {
    DrawGraph draw = new DrawGraph(graph);
    Map<Node, DNode> result = new HashMap<>();
    DotDigraph digraph = new DotDigraph(nodes.length, graph, result);
    DotAttachment attachment = new DotAttachment(digraph, draw, result);
    for (int i = 0; i < nodes.length; i++) {
      DNode node = new DNode(new NodeDrawProp(nodes[i], nodes[i].nodeAttrs()), 20, 20, 20);
      node.setRank(ranks[i]);
      digraph.add(node, containers[i]);
      if (containers[i].isCluster()) {
        attachment.markHaveCluster();
      }
    }
    for (Line line : graph.lines()) {
      int minlen = line.lineAttrs().getMinlen() == null ? 1 : line.lineAttrs().getMinlen();
      digraph.addEdge(new DLine(result.get(line.tail()), result.get(line.head()),
          new LineDrawProp(line, line.lineAttrs(), draw), 1, minlen));
    }
    RankContent content = new RankContent(digraph, 50, false, null);
    new MinCross(content, attachment, quick);
    BasicCrossRank ordered = new BasicCrossRank(graph);
    for (RankContent.RankNode rank : content) {
      for (int i = 0; i < rank.size(); i++) {
        ordered.addNode(rank.get(i));
        Assertions.assertEquals(i, rank.get(i).getRankIndex());
      }
    }
    Assertions.assertTrue(ClusterOrderConsistency.violations(ordered, attachment).isEmpty());
    Assertions.assertTrue(ClusterOrderConsistency.contiguityViolations(ordered, attachment).isEmpty());
    for (int i = 0; i < nodes.length; i++) {
      Assertions.assertEquals(ranks[i], result.get(nodes[i]).getRank(),
          "crossing minimization must not change assigned ranks");
    }
    return result;
  }

  private static void before(Map<Node, DNode> result, Node left, Node right) {
    Assertions.assertTrue(result.get(left).getRankIndex() < result.get(right).getRankIndex(),
        left.nodeAttrs().getLabel() + " must precede " + right.nodeAttrs().getLabel());
  }

  private static int flatCrossings(Graphviz graph, List<Node> order) {
    List<Line> lines = new ArrayList<>();
    graph.lines().forEach(lines::add);
    int count = 0;
    for (int i = 0; i < lines.size(); i++) {
      for (int j = i + 1; j < lines.size(); j++) {
        Line a = lines.get(i);
        Line b = lines.get(j);
        if (a.tail() == b.tail() || a.head() == b.head()
            || a.tail() == b.head() || a.head() == b.tail()) {
          continue;
        }
        int low = Math.min(order.indexOf(a.tail()), order.indexOf(a.head()));
        int high = Math.max(order.indexOf(a.tail()), order.indexOf(a.head()));
        int tail = order.indexOf(b.tail());
        int head = order.indexOf(b.head());
        if ((low < tail && tail < high) != (low < head && head < high)) {
          count++;
        }
      }
    }
    return count;
  }

  private static Node node(String id) {
    return Node.builder().id(id).label(id).build();
  }

  private static Line flat(Node from, Node to) {
    return Line.builder(from, to).minlen(0).build();
  }
}
