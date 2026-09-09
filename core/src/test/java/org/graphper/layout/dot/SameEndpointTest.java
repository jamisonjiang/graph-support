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
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import static org.graphper.layout.StraightPathClip.straightLineClipShape;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SameEndpointTest {

  @Test
  public void sameHeadGroupsShareDifferentShapePoints() throws Exception {
    Node queue = node("queue", NodeShapeEnum.HEXAGON);
    Node upper1 = node("upper1", NodeShapeEnum.RECT);
    Node upper2 = node("upper2", NodeShapeEnum.RECT);
    Node lower1 = node("lower1", NodeShapeEnum.RECT);
    Node lower2 = node("lower2", NodeShapeEnum.RECT);
    Line u1 = Line.builder(upper1, queue).sameHead("upper").headPort(Port.NORTH).build();
    Line u2 = Line.builder(upper2, queue).sameHead("upper").headPort(Port.SOUTH).build();
    Line l1 = Line.builder(lower1, queue).sameHead("lower").headclip(false).build();
    Line l2 = Line.builder(lower2, queue).sameHead("lower").headclip(true).build();

    Graphviz graph = Graphviz.digraph().rankdir(Rankdir.LR)
        .subgraph(org.graphper.api.Subgraph.builder().rank(org.graphper.api.attributes.Rank.SAME)
                      .addNode(upper1, upper2).build())
        .subgraph(org.graphper.api.Subgraph.builder().rank(org.graphper.api.attributes.Rank.SAME)
                      .addNode(lower1, lower2).build())
        .addNode(queue).addLine(u1).addLine(u2).addLine(l1).addLine(l2).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    FlatPoint upper = sameHead(draw, u1, u2);
    FlatPoint lower = sameHead(draw, l1, l2);
    Assertions.assertNotSame(upper, lower);
    assertOnShape(draw.getNodeDrawProp(queue), upper);
    assertOnShape(draw.getNodeDrawProp(queue), lower);
  }

  @Test
  public void sameTailSharesPointAcrossPortsClipAndShapes() throws Exception {
    for (NodeShapeEnum shape : new NodeShapeEnum[]{NodeShapeEnum.RECT, NodeShapeEnum.ELLIPSE,
        NodeShapeEnum.HEXAGON}) {
      Node source = node("source_" + shape, shape);
      Node a = node("a_" + shape, NodeShapeEnum.RECT);
      Node b = node("b_" + shape, NodeShapeEnum.RECT);
      Node c = node("c_" + shape, NodeShapeEnum.RECT);
      Line la = Line.builder(source, a).sameTail("x").tailPort(Port.NORTH).build();
      Line lb = Line.builder(source, b).sameTail("x").tailPort(Port.SOUTH)
          .tailclip(false).build();
      Line lc = Line.builder(source, c).sameTail("x").tailPort(Port.WEST)
          .tailclip(true).build();
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
          Graphviz.digraph().rankdir(Rankdir.LR).addLine(la).addLine(lb).addLine(lc).build());
      FlatPoint point = sameTail(draw, la, lb, lc);
      assertOnShape(draw.getNodeDrawProp(source), point);
    }
  }

  @Test
  public void sameEndpointsWorkAcrossDotLayoutsAndRankDirections() throws Exception {
    for (Layout layout : new Layout[]{Layout.DOT, Layout.DOTQ}) {
      for (Rankdir rankdir : Rankdir.values()) {
        Node source = node("s" + layout + rankdir, NodeShapeEnum.OCTAGON);
        Node a = node("a" + layout + rankdir, NodeShapeEnum.RECT);
        Node b = node("b" + layout + rankdir, NodeShapeEnum.RECT);
        Line first = Line.builder(source, a).sameTail("x").build();
        Line second = Line.builder(source, b).sameTail("x").build();
        DrawGraph draw = layout.getLayoutEngine().layout(
            Graphviz.digraph().layout(layout).rankdir(rankdir).addLine(first).addLine(second).build());
        sameTail(draw, first, second);
      }
    }
  }

  @Test
  public void loopsAndSingleMemberGroupsAreIgnored() throws Exception {
    Node a = node("a", NodeShapeEnum.RECT);
    Node b = node("b", NodeShapeEnum.RECT);
    Line loop = Line.builder(a, a).sameTail("x").sameHead("x").build();
    Line single = Line.builder(a, b).sameTail("single").build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(loop).addLine(single).build());
    Assertions.assertNull(draw.getLineDrawProp(loop).getSameTailPoint());
    Assertions.assertNull(draw.getLineDrawProp(loop).getSameHeadPoint());
    Assertions.assertNull(draw.getLineDrawProp(single).getSameTailPoint());
  }

  @Test
  public void orthoKeepsNativePortsWithoutSamePointOverride() throws Exception {
    Node source = node("source", NodeShapeEnum.RECT);
    Line a = Line.builder(source, node("a", NodeShapeEnum.RECT)).sameTail("x").build();
    Line b = Line.builder(source, node("b", NodeShapeEnum.RECT)).sameTail("x").build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().splines(Splines.ORTHO).addLine(a).addLine(b).build());
    Assertions.assertNull(draw.getLineDrawProp(a).getSameTailPoint());
    Assertions.assertNull(draw.getLineDrawProp(b).getSameTailPoint());
  }

  @Test
  public void forceLayoutsRetainAttributeWithoutApplyingDotSemantics() throws Exception {
    for (Layout layout : new Layout[]{Layout.FDP, Layout.JFDP, Layout.GFDP}) {
      Node source = node("source" + layout, NodeShapeEnum.RECT);
      Line a = Line.builder(source, node("a" + layout, NodeShapeEnum.RECT)).sameTail("x").build();
      Line b = Line.builder(source, node("b" + layout, NodeShapeEnum.RECT)).sameTail("x").build();
      DrawGraph draw = layout.getLayoutEngine().layout(
          Graphviz.digraph().layout(layout).addLine(a).addLine(b).build());
      Assertions.assertEquals("x", draw.getLineDrawProp(a).lineAttrs().getSameTail());
      Assertions.assertNull(draw.getLineDrawProp(a).getSameTailPoint());
    }
  }

  @Test
  public void cancellingDirectionsUseDeterministicFallback() throws Exception {
    Node center = node("center", NodeShapeEnum.CIRCLE);
    Node left = node("left", NodeShapeEnum.RECT);
    Node right = node("right", NodeShapeEnum.RECT);
    Line toLeft = Line.builder(center, left).sameTail("x").build();
    Line toRight = Line.builder(center, right).sameTail("x").build();
    Graphviz graph = Graphviz.digraph().rankdir(Rankdir.LR)
        .subgraph(org.graphper.api.Subgraph.builder().rank(org.graphper.api.attributes.Rank.SAME)
                      .addNode(left, center, right).build())
        .addLine(toLeft).addLine(toRight).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    FlatPoint point = sameTail(draw, toLeft, toRight);
    Assertions.assertTrue(Double.isFinite(point.getX()));
    Assertions.assertTrue(Double.isFinite(point.getY()));
  }

  @Test
  public void sharedPointUsesAverageNormalizedDirection() throws Exception {
    Node source = node("source_average", NodeShapeEnum.ELLIPSE);
    Node a = node("average_a", NodeShapeEnum.RECT);
    Node b = node("average_b", NodeShapeEnum.RECT);
    Node c = node("average_c", NodeShapeEnum.RECT);
    Line la = Line.builder(source, a).sameTail("average").build();
    Line lb = Line.builder(source, b).sameTail("average").build();
    Line lc = Line.builder(source, c).sameTail("average").build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().rankdir(Rankdir.LR).addLine(la).addLine(lb).addLine(lc).build());
    NodeDrawProp owner = draw.getNodeDrawProp(source);
    double x = 0;
    double y = 0;
    for (Node target : new Node[]{a, b, c}) {
      NodeDrawProp targetProp = draw.getNodeDrawProp(target);
      double dx = targetProp.getX() - owner.getX();
      double dy = targetProp.getY() - owner.getY();
      double length = Math.hypot(dx, dy);
      x += dx / length;
      y += dy / length;
    }
    double length = Math.hypot(x, y);
    FlatPoint center = new FlatPoint(owner.getX(), owner.getY());
    double radius = Math.max(owner.getWidth(), owner.getHeight()) + 1;
    FlatPoint expected = straightLineClipShape(owner, owner.shapeProp(), center,
        new FlatPoint(center.getX() + x / length * radius,
                      center.getY() + y / length * radius));
    FlatPoint actual = sameTail(draw, la, lb, lc);
    Assertions.assertEquals(expected.getX(), actual.getX(), 0.1);
    Assertions.assertEquals(expected.getY(), actual.getY(), 0.1);
  }

  @Test
  public void compoundClusterClippingStillAppliesAfterSameTail() throws Exception {
    Node source = node("cluster_source", NodeShapeEnum.RECT);
    Node a = node("cluster_a", NodeShapeEnum.RECT);
    Node b = node("cluster_b", NodeShapeEnum.RECT);
    Cluster cluster = Cluster.builder().id("source_cluster").addNode(source).build();
    Line la = Line.builder(source, a).sameTail("x").ltail("source_cluster").build();
    Line lb = Line.builder(source, b).sameTail("x").ltail("source_cluster").build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().cluster(cluster).addNode(a, b).addLine(la).addLine(lb).build());
    LineDrawProp laProp = draw.getLineDrawProp(la);
    Assertions.assertNotNull(laProp.getSameTailPoint());
    Assertions.assertTrue(laProp.size() >= 2);
    Assertions.assertTrue(draw.getLineDrawProp(lb).size() >= 2);
    FlatPoint finalTail = laProp.isHeadStart() ? laProp.getEnd() : laProp.getStart();
    Assertions.assertNotEquals(laProp.getSameTailPoint(), finalTail,
                               "cluster clipping must continue past the shared node point");
  }

  @Test
  public void parallelGroupsCombineConstraintLabelsArrowsAndClipFlags() throws Exception {
    Node a = node("parallel_a", NodeShapeEnum.OCTAGON);
    Node b = node("parallel_b", NodeShapeEnum.HEXAGON);
    Line first = Line.builder(a, b).sameTail("tail").sameHead("head")
        .constraint(false).dir(Dir.NONE).tailclip(false).headclip(false)
        .label("visual").floatLabels(FloatLabel.builder().label("float").build()).build();
    Line second = Line.builder(a, b).sameTail("tail").sameHead("head")
        .constraint(true).dir(Dir.BOTH).tailclip(true).headclip(true).label("rank").build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(first).addLine(second).build());
    sameTail(draw, first, second);
    sameHead(draw, first, second);
    Assertions.assertNotNull(draw.getLineDrawProp(first).getLabelCenter());
    Assertions.assertFalse(draw.getLineDrawProp(first).getFloatLabelFlatCenters().isEmpty());
  }

  @Test
  public void shortUnlabeledParallelBranchSharesBothEndpoints() throws Exception {
    for (Splines splines : new Splines[]{Splines.SPLINE, Splines.ROUNDED,
        Splines.POLYLINE, Splines.LINE}) {
      Node a = node("short_a_" + splines, NodeShapeEnum.OCTAGON);
      Node b = node("short_b_" + splines, NodeShapeEnum.HEXAGON);
      Line first = Line.builder(a, b).sameTail("tail").sameHead("head").build();
      Line second = Line.builder(a, b).sameTail("tail").sameHead("head").build();
      Line third = Line.builder(a, b).sameTail("tail").sameHead("head").build();
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
          Graphviz.digraph().splines(splines).addLine(first).addLine(second).addLine(third).build());
      sameTail(draw, first, second, third);
      sameHead(draw, first, second, third);
      Assertions.assertNull(draw.getLineDrawProp(first).getLabelCenter());
    }
  }

  @Test
  public void labeledParallelBoxRoutingSharesBothEndpoints() throws Exception {
    for (Splines splines : new Splines[]{Splines.SPLINE, Splines.ROUNDED,
        Splines.POLYLINE, Splines.LINE}) {
      Node a = node("label_a_" + splines, NodeShapeEnum.OCTAGON);
      Node b = node("label_b_" + splines, NodeShapeEnum.HEXAGON);
      Line first = Line.builder(a, b).sameTail("tail").sameHead("head")
          .label("first label").build();
      Line second = Line.builder(a, b).sameTail("tail").sameHead("head")
          .label("second label").build();
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
          Graphviz.digraph().splines(splines).addLine(first).addLine(second).build());
      sameTail(draw, first, second);
      sameHead(draw, first, second);
      Assertions.assertNotNull(draw.getLineDrawProp(first).getLabelCenter());
      Assertions.assertNotNull(draw.getLineDrawProp(second).getLabelCenter());
    }
  }

  @Test
  public void longParallelVirtualChainSharesBothEndpoints() throws Exception {
    for (Splines splines : new Splines[]{Splines.SPLINE, Splines.ROUNDED,
        Splines.POLYLINE, Splines.LINE}) {
      Node a = node("long_a_" + splines, NodeShapeEnum.OCTAGON);
      Node middle1 = node("long_middle1_" + splines, NodeShapeEnum.RECT);
      Node middle2 = node("long_middle2_" + splines, NodeShapeEnum.RECT);
      Node b = node("long_b_" + splines, NodeShapeEnum.HEXAGON);
      Line first = Line.builder(a, b).constraint(false).sameTail("tail").sameHead("head").build();
      Line second = Line.builder(a, b).constraint(false).sameTail("tail").sameHead("head").build();
      Graphviz graph = Graphviz.digraph().splines(splines)
          .addLine(a, middle1).addLine(middle1, middle2).addLine(middle2, b)
          .addLine(first).addLine(second).build();
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
      sameTail(draw, first, second);
      sameHead(draw, first, second);
      Assertions.assertTrue(draw.getLineDrawProp(first).size() >= 2);
      Assertions.assertTrue(draw.getLineDrawProp(second).size() >= 2);
    }
  }

  @Test
  public void sameTailOverridesCellsPortsAndClipFlags() throws Exception {
    Node record = Node.builder().id("record_owner").shape(NodeShapeEnum.RECORD)
        .label("{<left>left|middle|<right>right}").build();
    Node a = node("cell_target_a", NodeShapeEnum.RECT);
    Node b = node("cell_target_b", NodeShapeEnum.RECT);
    Line left = Line.builder(record, a).sameTail("x")
        .tailCell("left").tailPort(Port.NORTH).tailclip(false).build();
    Line right = Line.builder(record, b).sameTail("x")
        .tailCell("right").tailPort(Port.SOUTH).tailclip(true).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(left).addLine(right).build());
    FlatPoint point = sameTail(draw, left, right);
    assertOnShape(draw.getNodeDrawProp(record), point);
  }

  private FlatPoint sameTail(DrawGraph draw, Line... lines) {
    List<Line> list = Arrays.asList(lines);
    FlatPoint expected = draw.getLineDrawProp(list.get(0)).getSameTailPoint();
    Assertions.assertNotNull(expected);
    for (Line line : list) {
      LineDrawProp prop = draw.getLineDrawProp(line);
      Assertions.assertEquals(expected, prop.getSameTailPoint());
      FlatPoint actual = prop.getArrowTail() != null ? prop.getArrowTail().getAxisEnd()
          : (prop.isHeadStart() ? prop.getEnd() : prop.getStart());
      Assertions.assertEquals(expected.getX(), actual.getX(), 0.1);
      Assertions.assertEquals(expected.getY(), actual.getY(), 0.1);
    }
    return expected;
  }

  private FlatPoint sameHead(DrawGraph draw, Line... lines) {
    List<Line> list = Arrays.asList(lines);
    FlatPoint expected = draw.getLineDrawProp(list.get(0)).getSameHeadPoint();
    Assertions.assertNotNull(expected);
    for (Line line : list) {
      LineDrawProp prop = draw.getLineDrawProp(line);
      Assertions.assertEquals(expected, prop.getSameHeadPoint());
      FlatPoint actual = prop.getArrowHead() != null ? prop.getArrowHead().getAxisEnd()
          : (prop.isHeadStart() ? prop.getStart() : prop.getEnd());
      Assertions.assertEquals(expected.getX(), actual.getX(), 0.1);
      Assertions.assertEquals(expected.getY(), actual.getY(), 0.1);
    }
    return expected;
  }

  private void assertOnShape(NodeDrawProp node, FlatPoint point) {
    double centerDistance = Math.hypot(point.getX() - node.getX(), point.getY() - node.getY());
    Assertions.assertTrue(centerDistance > 0);
    FlatPoint inner = new FlatPoint(node.getX() + (point.getX() - node.getX()) * 0.99,
                                    node.getY() + (point.getY() - node.getY()) * 0.99);
    Assertions.assertTrue(node.shapeProp().in(node, inner),
                          "node=" + node + " point=" + point + " inner=" + inner);
  }

  private Node node(String id, NodeShapeEnum shape) {
    return Node.builder().id(id).label(id).shape(shape).build();
  }
}
