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
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MinCrossRankdirTest {

  static Stream<Arguments> directions() {
    return Stream.of(Layout.DOT, Layout.DOTQ).flatMap(layout ->
        Arrays.stream(Rankdir.values()).map(rankdir -> Arguments.of(layout, rankdir)));
  }

  static Stream<Arguments> chains() {
    return directions().flatMap(direction -> IntStream.range(0, 6).boxed().flatMap(shape ->
        IntStream.rangeClosed(0, 1).mapToObj(minlen ->
            Arguments.of(direction.get()[0], direction.get()[1], shape, minlen))));
  }

  @ParameterizedTest(name = "{0}/{1} shape={2} minlen={3}")
  @MethodSource("chains")
  void chainsKeepVisualDirectionAcrossContainers(Layout layout, Rankdir rankdir, int shape,
                                                int minlen) throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Graphviz.GraphvizBuilder builder = Graphviz.digraph().layout(layout).rankdir(rankdir);
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
    Node[] nodes = {a, b, c, d};
    Line[] lines = new Line[3];
    for (int i = 0; i < lines.length; i++) {
      lines[i] = Line.builder(nodes[i], nodes[i + 1]).minlen(minlen).build();
      builder.addLine(lines[i]);
    }
    DrawGraph draw = layout.getLayoutEngine().layout(builder.build());
    for (Line line : lines) {
      assertDirection(draw, line.tail(), line.head(), minlen == 0);
      LineDrawProp prop = draw.getLineDrawProp(line);
      Assertions.assertFalse(prop.isEmpty(), "edge must still be routed");
      // Geometry may start at either endpoint; the semantic arrow must still point to the head.
      Assertions.assertNotNull(prop.getArrowHead());
      NodeDrawProp head = draw.getNodeDrawProp(line.head());
      NodeDrawProp tail = draw.getNodeDrawProp(line.tail());
      FlatPoint end = prop.isHeadStart() ? prop.get(0) : prop.get(prop.size() - 1);
      Assertions.assertTrue(Math.hypot(end.getX() - head.getX(), end.getY() - head.getY())
          < Math.hypot(end.getX() - tail.getX(), end.getY() - tail.getY()),
          "head endpoint must not be exchanged with the tail");
    }
  }

  @ParameterizedTest(name = "{0}/{1}")
  @MethodSource("directions")
  void intermediateFlatEdgeOrdersWholeMultiRankSiblingBlocks(Layout layout, Rankdir rankdir)
      throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Node e = node("e");
    Node f = node("f");
    Graphviz graph = Graphviz.digraph().layout(layout).rankdir(rankdir)
        .cluster(Cluster.builder().addNode(d, e, f).build())
        .cluster(Cluster.builder().addNode(a, b, c).build())
        .addLine(a, b).addLine(b, c).addLine(d, e).addLine(e, f)
        .addLine(Line.builder(b, e).minlen(0).build()).build();
    DrawGraph draw = layout.getLayoutEngine().layout(graph);
    assertDirection(draw, a, d, true);
    assertDirection(draw, b, e, true);
    assertDirection(draw, c, f, true);
    assertDirection(draw, a, b, false);
    assertDirection(draw, b, c, false);
    assertDirection(draw, d, e, false);
    assertDirection(draw, e, f, false);
  }

  @ParameterizedTest(name = "{0}/{1}")
  @MethodSource("directions")
  void graphWithoutFlatEdgesKeepsExistingWithinRankOrientation(Layout layout, Rankdir rankdir)
      throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Node d = node("d");
    Graphviz.GraphvizBuilder builder = Graphviz.digraph().layout(layout)
        .addLine(a, b).addLine(a, c).addLine(b, d).addLine(c, d);
    DrawGraph baseline = layout.getLayoutEngine().layout(builder.rankdir(Rankdir.TB).build());
    DrawGraph draw = layout.getLayoutEngine().layout(builder.rankdir(rankdir).build());
    double baselineDelta = baseline.getNodeDrawProp(c).getX() - baseline.getNodeDrawProp(b).getX();
    double delta = withinRank(draw, c) - withinRank(draw, b);
    boolean horizontal = rankdir == Rankdir.LR || rankdir == Rankdir.RL;
    Assertions.assertEquals(Math.signum(baselineDelta), Math.signum(horizontal ? -delta : delta),
        "fixing flat precedence must not globally mirror unrelated layouts");
    assertDirection(draw, a, b, false);
    assertDirection(draw, a, c, false);
    assertDirection(draw, b, d, false);
    assertDirection(draw, c, d, false);
  }

  static Stream<Arguments> horizontalDirections() {
    return Stream.of(Layout.DOT, Layout.DOTQ).flatMap(layout ->
        Stream.of(Rankdir.LR, Rankdir.RL).map(rankdir -> Arguments.of(layout, rankdir)));
  }

  @ParameterizedTest(name = "{0}/{1}")
  @MethodSource("horizontalDirections")
  void expandedClustersMayViolateFlatPreferenceToRemoveCrossing(Layout layout, Rankdir rankdir) {
    Node x = node("x");
    Node y = node("y");
    Node a = node("a");
    Node b = node("b");
    Cluster first = Cluster.builder().addNode(x, y).build();
    Cluster second = Cluster.builder().addNode(a, b).build();
    Graphviz graph = Graphviz.digraph().layout(layout).rankdir(rankdir)
        .cluster(first).cluster(second).addLine(x, a).addLine(y, b)
        .addLine(Line.builder(x, y).minlen(0).build())
        .addLine(Line.builder(b, a).minlen(0).build()).build();
    DrawGraph draw = new DrawGraph(graph);
    Map<Node, DNode> nodes = new HashMap<>();
    DotDigraph digraph = new DotDigraph(4, graph, nodes);
    DotAttachment attachment = new DotAttachment(digraph, draw, nodes);
    attachment.markHaveCluster();
    for (Node node : new Node[]{x, y, a, b}) {
      DNode dn = new DNode(new NodeDrawProp(node, node.nodeAttrs()), 20, 20, 20);
      boolean upstream = node == x || node == y;
      dn.setRank(upstream ? 1 : 2);
      digraph.add(dn, upstream ? first : second);
    }
    for (Line line : graph.lines()) {
      int minlen = line.lineAttrs().getMinlen() == null ? 1 : line.lineAttrs().getMinlen();
      digraph.addEdge(new DLine(nodes.get(line.tail()), nodes.get(line.head()),
          new LineDrawProp(line, line.lineAttrs(), draw), 1, minlen));
    }
    RankContent ranks = new RankContent(digraph, 50, false, null);
    new MinCross(ranks, attachment, layout == Layout.DOTQ);
    int upstreamOrder = nodes.get(x).getRankIndex() - nodes.get(y).getRankIndex();
    int downstreamOrder = nodes.get(a).getRankIndex() - nodes.get(b).getRankIndex();
    Assertions.assertTrue(upstreamOrder * downstreamOrder > 0,
        "x -> a and y -> b must not cross to satisfy opposing flat preferences");
    Assertions.assertTrue(upstreamOrder < 0 || downstreamOrder > 0,
        "a strict crossing improvement must be allowed to violate a horizontal flat preference");
    for (Node node : new Node[]{x, y, a, b}) {
      Assertions.assertEquals(node == x || node == y ? 1 : 2, nodes.get(node).getRank());
    }
  }

  private static void assertDirection(DrawGraph draw, Node tail, Node head, boolean flat) {
    Rankdir rankdir = draw.rankdir();
    boolean horizontal = rankdir == Rankdir.LR || rankdir == Rankdir.RL;
    NodeDrawProp from = draw.getNodeDrawProp(tail);
    NodeDrawProp to = draw.getNodeDrawProp(head);
    double rankDelta = horizontal ? to.getX() - from.getX() : to.getY() - from.getY();
    if (flat) {
      Assertions.assertEquals(0, rankDelta, 0.01, "flat endpoints must share a rank");
      Assertions.assertTrue(withinRank(draw, tail) < withinRank(draw, head),
          "flat tail must be visually before head: " + tail.nodeAttrs().getId()
              + " -> " + head.nodeAttrs().getId());
    } else {
      boolean reverse = rankdir == Rankdir.BT || rankdir == Rankdir.RL;
      Assertions.assertTrue((reverse ? -rankDelta : rankDelta) > 0,
          "normal edge must follow rankdir");
    }
  }

  private static double withinRank(DrawGraph draw, Node node) {
    NodeDrawProp prop = draw.getNodeDrawProp(node);
    return draw.rankdir() == Rankdir.LR || draw.rankdir() == Rankdir.RL
        ? prop.getY() : prop.getX();
  }

  private static Node node(String id) {
    return Node.builder().id(id).label(id).width(1).height(1).fixedSize(true).build();
  }
}
