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

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Line.LineBuilder;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Exhaustive deterministic combinations where every edge is non-constraining. */
public class ConstraintFalseCombinationTest {

  private static final Layout[] LAYOUTS = {Layout.DOT, Layout.DOTQ};

  private static final Splines[] SPLINES = {
      Splines.SPLINE, Splines.ROUNDED, Splines.POLYLINE, Splines.LINE, Splines.ORTHO
  };

  private static final int[] MINLENS = {0, 1, 2, 10};

  private static final double[] WEIGHTS = {0, 0.5, 1, 100};

  @Test
  public void allFalseEdgesAcrossLayoutRoutingAndAttributes() throws Exception {
    forEachConfiguration((layout, rankdir, splines) -> {
      List<Node> nodes = nodes(9, NodeShapeEnum.RECT);
      List<Line> lines = new ArrayList<>();
      for (int i = 0; i < nodes.size(); i++) {
        Node tail = nodes.get(i);
        Node head = nodes.get((i + 1) % nodes.size());
        lines.add(configuredLine(tail, head, i).build());
      }
      lines.add(configuredLine(nodes.get(0), nodes.get(0), nodes.size()).build());

      GraphvizBuilder builder = graph(layout, rankdir, splines);
      nodes.forEach(builder::addNode);
      lines.forEach(builder::addLine);
      assertEveryLineRouted(builder.build(), layout, lines, context(layout, rankdir, splines));
    });
  }

  @Test
  public void allFalseRecordCellAndPortCombinations() throws Exception {
    forEachConfiguration((layout, rankdir, splines) -> {
      Node a = record("a");
      Node b = record("b");
      Node c = record("c");
      List<Line> lines = new ArrayList<>();
      Port[] ports = Port.values();
      for (int i = 0; i < ports.length; i++) {
        Node tail = i % 2 == 0 ? a : b;
        Node head = i % 3 == 0 ? c : b;
        lines.add(configuredLine(tail, head, i)
                      .tailCell(i % 2 == 0 ? "out" : "in")
                      .headCell(i % 2 == 0 ? "in" : "out")
                      .tailPort(ports[i])
                      .headPort(ports[ports.length - 1 - i])
                      .build());
      }

      GraphvizBuilder builder = graph(layout, rankdir, splines).addNode(a, b, c);
      lines.forEach(builder::addLine);
      assertEveryLineRouted(builder.build(), layout, lines,
                            "record " + context(layout, rankdir, splines));
    });
  }

  @Test
  public void allFalseClusterAndRankCombinations() throws Exception {
    forEachConfiguration((layout, rankdir, splines) -> {
      Node a = node("a", NodeShapeEnum.RECT);
      Node b = node("b", NodeShapeEnum.RECT);
      Node c = node("c", NodeShapeEnum.ELLIPSE);
      Node d = node("d", NodeShapeEnum.ELLIPSE);
      Node e = node("e", NodeShapeEnum.CIRCLE);

      Cluster inner = Cluster.builder().id("cluster_inner").addNode(b).build();
      Cluster left = Cluster.builder().id("cluster_left").addNode(a).cluster(inner).build();
      Cluster right = Cluster.builder().id("cluster_right").addNode(c, d).build();

      List<Line> lines = new ArrayList<>();
      lines.add(configuredLine(a, b, 0).build());
      lines.add(configuredLine(c, d, 1).build());
      lines.add(configuredLine(b, c, 2)
                    .ltail("cluster_left").lhead("cluster_right").build());
      lines.add(configuredLine(d, a, 3)
                    .ltail("cluster_right").lhead("cluster_left").build());
      lines.add(configuredLine(e, e, 4).build());

      GraphvizBuilder builder = graph(layout, rankdir, splines)
          .cluster(left).cluster(right).addNode(e)
          .subgraph(Subgraph.builder().rank(Rank.SAME).addNode(a, c).build());
      lines.forEach(builder::addLine);
      assertEveryLineRouted(builder.build(), layout, lines,
                            "cluster " + context(layout, rankdir, splines));
    });
  }

  @Test
  public void sameRankPortRoutingMatchesConstrainedControl() throws Exception {
    for (boolean constraint : new boolean[]{false, true}) {
      Node a = node("control_a_" + constraint, NodeShapeEnum.RECT);
      Node b = node("control_b_" + constraint, NodeShapeEnum.RECT);
      Line line = Line.builder(a, b)
          .constraint(constraint)
          .minlen(2)
          .weight(1)
          .label("control")
          .floatLabels(FloatLabel.builder().label("float").lengthRatio(0.7).build())
          .tailPort(Port.SOUTH_EAST)
          .headPort(Port.WEST)
          .tailclip(true)
          .headclip(false)
          .dir(Dir.BOTH)
          .arrowHead(ArrowShape.VEE)
          .arrowTail(ArrowShape.BOX)
          .arrowSize(2)
          .radian(18)
          .style(LineStyle.DASHED)
          .build();
      Graphviz graph = Graphviz.digraph()
          .layout(Layout.DOTQ).rankdir(Rankdir.BT).splines(Splines.SPLINE)
          .subgraph(Subgraph.builder().rank(Rank.SAME).addNode(a, b).build())
          .addLine(line)
          .build();
      LineDrawProp prop = Layout.DOTQ.getLayoutEngine().layout(graph).getLineDrawProp(line);
      Assertions.assertNotNull(prop);
      Assertions.assertTrue(prop.size() >= 2, "same-rank constrained-control=" + constraint);
    }
  }

  @Test
  public void allFalseRingMatchesExplicitSameRankControl() throws Exception {
    assertRingRouted(true);
    assertRingRouted(false);
  }

  private void assertRingRouted(boolean explicitSameRank) throws Exception {
    List<Node> nodes = nodes(9, NodeShapeEnum.RECT);
    List<Line> lines = new ArrayList<>();
    for (int i = 0; i < nodes.size(); i++) {
      lines.add(configuredLine(nodes.get(i), nodes.get((i + 1) % nodes.size()), i)
                    .constraint(explicitSameRank)
                    .build());
    }
    GraphvizBuilder builder = graph(Layout.DOTQ, Rankdir.BT, Splines.SPLINE);
    if (explicitSameRank) {
      builder.subgraph(Subgraph.builder().rank(Rank.SAME)
                           .addNode(nodes.toArray(new Node[0])).build());
    } else {
      builder.addNode(nodes.toArray(new Node[0]));
    }
    lines.forEach(builder::addLine);
    DrawGraph drawGraph = Layout.DOTQ.getLayoutEngine().layout(builder.build());
    for (int i = 0; i < lines.size(); i++) {
      LineDrawProp prop = drawGraph.getLineDrawProp(lines.get(i));
      Assertions.assertTrue(prop.size() >= 2,
                            "explicitSameRank=" + explicitSameRank + " line=" + i
                                + " prop=" + prop);
    }
  }

  private LineBuilder configuredLine(Node tail, Node head, int index) {
    ArrowShape[] arrows = ArrowShape.values();
    Dir[] directions = Dir.values();
    Port[] ports = Port.values();
    return Line.builder(tail, head)
        .constraint(false)
        .minlen(MINLENS[index % MINLENS.length])
        .weight(WEIGHTS[index % WEIGHTS.length])
        .label("edge-" + index)
        .floatLabels(FloatLabel.builder()
                         .label("float-" + index)
                         .lengthRatio((index % 9 + 1) / 10D)
                         .offset((index % 3 - 1) / 2D, (index % 5 - 2) / 3D)
                         .build())
        .tailPort(ports[index % ports.length])
        .headPort(ports[(index * 3 + 1) % ports.length])
        .tailclip(index % 2 == 0)
        .headclip(index % 3 != 0)
        .dir(directions[index % directions.length])
        .arrowHead(arrows[index % arrows.length])
        .arrowTail(arrows[(index + 2) % arrows.length])
        .arrowSize(0.5 + index % 4)
        .radian(index * 3D)
        .style(index % 2 == 0 ? LineStyle.DASHED : LineStyle.DOTTED);
  }

  private GraphvizBuilder graph(Layout layout, Rankdir rankdir, Splines splines) {
    return Graphviz.digraph().layout(layout).rankdir(rankdir).splines(splines);
  }

  private List<Node> nodes(int count, NodeShapeEnum shape) {
    List<Node> nodes = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      nodes.add(node("n" + i, shape));
    }
    return nodes;
  }

  private Node node(String id, NodeShapeEnum shape) {
    return Node.builder().id(id).label(id).shape(shape).build();
  }

  private Node record(String id) {
    return Node.builder().id(id).shape(NodeShapeEnum.RECORD)
        .label("{<in>in|middle|<out>out}").build();
  }

  private void assertEveryLineRouted(Graphviz graph, Layout layout, List<Line> lines,
                                     String context) throws Exception {
    DrawGraph drawGraph = layout.getLayoutEngine().layout(graph);
    Assertions.assertEquals(lines.size(), drawGraph.lines().size(), context);
    for (int i = 0; i < lines.size(); i++) {
      Line line = lines.get(i);
      String lineContext = context + " line=" + i + " tail=" + line.tail().nodeAttrs().getId()
          + " head=" + line.head().nodeAttrs().getId() + " attrs=" + line.lineAttrs();
      LineDrawProp prop = drawGraph.getLineDrawProp(line);
      Assertions.assertNotNull(prop, lineContext);
      if (line.tail() == line.head()) {
        Assertions.assertFalse(prop.isEmpty(), lineContext + " path=" + prop);
      } else {
        Assertions.assertTrue(prop.size() >= 2, lineContext + " path=" + prop);
      }
      Assertions.assertNotNull(prop.getStart(), lineContext);
      Assertions.assertNotNull(prop.getEnd(), lineContext);
      Assertions.assertEquals(Boolean.FALSE, prop.lineAttrs().getConstraint(), context);
      if (line.lineAttrs().getLabel() != null) {
        Assertions.assertNotNull(prop.getLabelCenter(), lineContext);
      }
    }
  }

  private void forEachConfiguration(ConfigurationConsumer consumer) throws Exception {
    for (Layout layout : LAYOUTS) {
      for (Rankdir rankdir : Rankdir.values()) {
        for (Splines splines : SPLINES) {
          consumer.accept(layout, rankdir, splines);
        }
      }
    }
  }

  private String context(Layout layout, Rankdir rankdir, Splines splines) {
    return "layout=" + layout + " rankdir=" + rankdir + " splines=" + splines;
  }

  private interface ConfigurationConsumer {
    void accept(Layout layout, Rankdir rankdir, Splines splines) throws Exception;
  }
}
