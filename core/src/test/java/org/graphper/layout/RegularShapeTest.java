/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.layout;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Html;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.RegularPolylinePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegularShapeTest {

  @Test
  public void unsetAndFalseHexagonsStretchWhileTrueIsSquare() throws Exception {
    Node unset = node("unset", null);
    Node stretch = node("stretch", false);
    Node regular = node("regular", true);
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addNode(unset, stretch, regular).build());

    NodeDrawProp unsetProp = draw.getNodeDrawProp(unset);
    NodeDrawProp stretchProp = draw.getNodeDrawProp(stretch);
    NodeDrawProp regularProp = draw.getNodeDrawProp(regular);
    Assertions.assertNull(unset.nodeAttrs().getRegular());
    Assertions.assertEquals(Boolean.FALSE, stretch.nodeAttrs().getRegular());
    Assertions.assertEquals(Boolean.TRUE, regular.nodeAttrs().getRegular());
    Assertions.assertEquals(unsetProp.getWidth(), stretchProp.getWidth(), 0.001);
    Assertions.assertEquals(unsetProp.getHeight(), stretchProp.getHeight(), 0.001);
    Assertions.assertTrue(unsetProp.getWidth() > unsetProp.getHeight() * 2);
    Assertions.assertEquals(regularProp.getWidth(), regularProp.getHeight(), 0.001);
  }

  @Test
  public void stretchedPolygonPointsAndContainmentUseFinalBox() throws Exception {
    Node hexagon = node("hexagon", false);
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addNode(hexagon).build());
    NodeDrawProp prop = draw.getNodeDrawProp(hexagon);
    RegularPolylinePropCalc calc = (RegularPolylinePropCalc)
        ((org.graphper.api.attributes.NodeShape) prop.shapeProp()).getShapePropCalc();
    List<FlatPoint> points = calc.calcPoints(prop);

    Assertions.assertEquals(6, points.size());
    double minX = points.stream().mapToDouble(FlatPoint::getX).min().orElseThrow(AssertionError::new);
    double maxX = points.stream().mapToDouble(FlatPoint::getX).max().orElseThrow(AssertionError::new);
    double minY = points.stream().mapToDouble(FlatPoint::getY).min().orElseThrow(AssertionError::new);
    double maxY = points.stream().mapToDouble(FlatPoint::getY).max().orElseThrow(AssertionError::new);
    Assertions.assertEquals(prop.getWidth(), maxX - minX, 0.001);
    Assertions.assertEquals(prop.getHeight(), maxY - minY, 0.001);
    Assertions.assertTrue(prop.shapeProp().in(prop, new FlatPoint(prop.getX(), prop.getY())));
    Assertions.assertFalse(prop.shapeProp().in(prop,
        new FlatPoint(prop.getRightBorder() + 1, prop.getY())));
  }

  @Test
  public void fixedSizeStillHonorsRegularTrue() throws Exception {
    Node hexagon = Node.builder().id("fixed").shape(NodeShapeEnum.HEXAGON)
        .width(2).height(1).fixedSize(true).regular(true).build();
    NodeDrawProp prop = Layout.DOT.getLayoutEngine()
        .layout(Graphviz.digraph().addNode(hexagon).build()).getNodeDrawProp(hexagon);
    Assertions.assertEquals(prop.getWidth(), prop.getHeight(), 0.001);
    Assertions.assertEquals(2 * Graphviz.PIXEL, prop.getWidth(), 0.001);
  }

  @Test
  public void recordIgnoresRegular() throws Exception {
    Node record = Node.builder().id("record").shape(NodeShapeEnum.RECORD)
        .label("wide record label").regular(true).build();
    NodeDrawProp prop = Layout.DOT.getLayoutEngine()
        .layout(Graphviz.digraph().addNode(record).build()).getNodeDrawProp(record);
    Assertions.assertNotEquals(prop.getWidth(), prop.getHeight(), 0.001);
  }

  @Test
  public void allBuiltInPolygonsStretchAndRegularize() throws Exception {
    for (NodeShapeEnum shape : new NodeShapeEnum[]{NodeShapeEnum.PENTAGON,
        NodeShapeEnum.HEXAGON, NodeShapeEnum.SEPTAGON, NodeShapeEnum.OCTAGON}) {
      Node stretch = Node.builder().id(shape + "_stretch").shape(shape)
          .label("WWWWWWWWWWWWWWWW").build();
      Node regular = Node.builder().id(shape + "_regular").shape(shape)
          .label("WWWWWWWWWWWWWWWW").regular(true).build();
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
          Graphviz.digraph().addNode(stretch, regular).build());
      NodeDrawProp stretchProp = draw.getNodeDrawProp(stretch);
      NodeDrawProp regularProp = draw.getNodeDrawProp(regular);
      Assertions.assertTrue(stretchProp.getWidth() > stretchProp.getHeight() * 2, shape.name());
      Assertions.assertEquals(regularProp.getWidth(), regularProp.getHeight(), 0.001,
                              shape.name());
    }
  }

  @Test
  public void stretchedHexagonSupportsEveryCompassPort() throws Exception {
    for (Rankdir rankdir : Rankdir.values()) {
      for (Splines splines : new Splines[]{Splines.SPLINE, Splines.POLYLINE, Splines.ORTHO}) {
        Node hexagon = Node.builder().id("hexagon").shape(NodeShapeEnum.HEXAGON)
            .label("WWWWWWWWWWWWWWWW").build();
        List<Line> lines = new ArrayList<>();
        Graphviz.GraphvizBuilder graph = Graphviz.digraph().rankdir(rankdir).splines(splines)
            .addNode(hexagon);
        Port[] ports = Port.values();
        for (int i = 0; i < ports.length; i++) {
          Node target = Node.builder().id("target" + i).build();
          Line line = Line.builder(hexagon, target).tailPort(ports[i]).build();
          lines.add(line);
          graph.addNode(target).addLine(line);
        }
        DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph.build());
        for (Line line : lines) {
          LineDrawProp prop = draw.getLineDrawProp(line);
          Assertions.assertNotNull(prop, rankdir + " " + splines);
          Assertions.assertTrue(prop.size() >= 2, rankdir + " " + splines);
          Assertions.assertNotNull(prop.getStart(), rankdir + " " + splines);
          Assertions.assertNotNull(prop.getEnd(), rankdir + " " + splines);
        }
      }
    }
  }

  @Test
  public void regularTrueSquaresEverySupportedBuiltInShape() throws Exception {
    for (NodeShapeEnum shape : NodeShapeEnum.values()) {
      Node node = Node.builder().id("regular_" + shape).shape(shape)
          .label("wide label").regular(true).build();
      NodeDrawProp prop = Layout.DOT.getLayoutEngine()
          .layout(Graphviz.digraph().addNode(node).build()).getNodeDrawProp(node);
      if (shape == NodeShapeEnum.RECORD || shape == NodeShapeEnum.M_RECORD) {
        Assertions.assertNotEquals(prop.getWidth(), prop.getHeight(), 0.001, shape.name());
      } else {
        Assertions.assertEquals(prop.getWidth(), prop.getHeight(), 0.001, shape.name());
      }
    }
  }

  @Test
  public void starUsesGraphvizLikeIntrinsicAspectBeforeRegularization() throws Exception {
    Node stretch = Node.builder().id("star").shape(NodeShapeEnum.STAR)
        .label("WWWWWWWWWWWWWWWW").build();
    Node regular = Node.builder().id("regular_star").shape(NodeShapeEnum.STAR)
        .label("WWWWWWWWWWWWWWWW").regular(true).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addNode(stretch, regular).build());
    NodeDrawProp stretchProp = draw.getNodeDrawProp(stretch);
    NodeDrawProp regularProp = draw.getNodeDrawProp(regular);
    Assertions.assertTrue(stretchProp.getWidth() > stretchProp.getHeight());
    Assertions.assertTrue(stretchProp.getWidth() / stretchProp.getHeight() < 1.1);
    Assertions.assertEquals(regularProp.getWidth(), regularProp.getHeight(), 0.001);
  }

  @Test
  public void regularIsPreservedByEveryLayoutEngine() throws Exception {
    for (Layout layout : Layout.values()) {
      Node node = Node.builder().id("regular_" + layout).shape(NodeShapeEnum.HEXAGON)
          .label("WWWWWWWWWWWWWWWW").regular(true).build();
      NodeDrawProp prop = layout.getLayoutEngine()
          .layout(Graphviz.digraph().layout(layout).addNode(node).build())
          .getNodeDrawProp(node);
      Assertions.assertEquals(prop.getWidth(), prop.getHeight(), 0.001, layout.name());
    }
  }

  @Test
  public void stretchedPolygonsContainWideAndTallLabels() throws Exception {
    for (NodeShapeEnum shape : new NodeShapeEnum[]{NodeShapeEnum.PENTAGON,
        NodeShapeEnum.HEXAGON, NodeShapeEnum.SEPTAGON, NodeShapeEnum.OCTAGON}) {
      for (String label : new String[]{"WWWWWWWWWWWWWWWW", "A\nB\nC\nD\nE"}) {
        Node node = Node.builder().id(shape + label).shape(shape).label(label).build();
        NodeDrawProp prop = Layout.DOT.getLayoutEngine()
            .layout(Graphviz.digraph().addNode(node).build()).getNodeDrawProp(node);
        assertLabelCornersInside(prop, shape + " label=" + label);
      }
    }
  }

  @Test
  public void starContainsWideAndTallLabels() throws Exception {
    for (String label : new String[]{"WWWWWWWWWWWWWWWW", "A\nB\nC\nD\nE"}) {
      Node node = Node.builder().id("star_" + label).shape(NodeShapeEnum.STAR)
          .label(label).build();
      NodeDrawProp prop = Layout.DOT.getLayoutEngine()
          .layout(Graphviz.digraph().addNode(node).build()).getNodeDrawProp(node);
      assertLabelCornersInside(prop, "star label=" + label);
    }
  }

  @Test
  public void stretchedPolygonContainsTableCells() throws Exception {
    for (NodeShapeEnum shape : new NodeShapeEnum[]{NodeShapeEnum.HEXAGON, NodeShapeEnum.OCTAGON}) {
      Node node = Node.builder().id("table_" + shape).shape(shape)
          .table(Html.table().border(0).cellBorder(1).cellSpacing(0).cellPadding(6)
                     .tr(Html.td().id("input").text("input"),
                         Html.td().text("a very wide polygon body"),
                         Html.td().id("output").text("output")))
          .build();
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
          Graphviz.digraph().addNode(node).build());
      NodeDrawProp owner = draw.getNodeDrawProp(node);
      for (Node child : owner.getAssemble().getCells()) {
        NodeDrawProp childProp = draw.getNodeDrawProp(child);
        assertBoxCornersInside(owner, childProp, shape + " child=" + child.nodeAttrs().getId());
      }
    }
  }

  private Node node(String id, Boolean regular) {
    Node.NodeBuilder builder = Node.builder().id(id).shape(NodeShapeEnum.HEXAGON)
        .label("WWWWWWWWWWWWWWWW");
    if (regular != null) {
      builder.regular(regular);
    }
    return builder.build();
  }

  private void assertLabelCornersInside(NodeDrawProp prop, String context) {
    FlatPoint center = prop.getLabelCenter();
    FlatPoint size = prop.getLabelSize();
    double left = center.getX() - size.getWidth() / 2;
    double right = center.getX() + size.getWidth() / 2;
    double top = center.getY() - size.getHeight() / 2;
    double bottom = center.getY() + size.getHeight() / 2;
    for (FlatPoint point : new FlatPoint[]{new FlatPoint(left, top), new FlatPoint(right, top),
        new FlatPoint(right, bottom), new FlatPoint(left, bottom)}) {
      Assertions.assertTrue(prop.shapeProp().in(prop, point), context + " point=" + point);
    }
  }

  private void assertBoxCornersInside(NodeDrawProp owner, NodeDrawProp child, String context) {
    for (FlatPoint point : new FlatPoint[]{new FlatPoint(child.getLeftBorder(), child.getUpBorder()),
        new FlatPoint(child.getRightBorder(), child.getUpBorder()),
        new FlatPoint(child.getRightBorder(), child.getDownBorder()),
        new FlatPoint(child.getLeftBorder(), child.getDownBorder())}) {
      Assertions.assertTrue(owner.shapeProp().in(owner, point), context + " point=" + point);
    }
  }
}
