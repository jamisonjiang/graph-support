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
package org.graphper.draw.svg;

import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.graphper.api.FileType;
import org.graphper.api.Graphviz;
import org.graphper.api.Html;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.api.ext.PeripheryNodePropCalc;
import org.graphper.api.ext.PolygonNodePropCalc;
import org.graphper.api.ext.RegularPolylinePropCalc;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.api.ext.StretchablePolygonPropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.common.DefaultImgConverter;
import org.graphper.draw.svg.node.NodeColorEditor;
import org.graphper.draw.svg.node.NodeShapeEditor;
import org.graphper.draw.svg.node.NodeStyleEditor;
import org.graphper.layout.StraightPathClip;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpandedNodeShapeTest {

  private static final NodeShapeEnum[] SHAPES = {
      NodeShapeEnum.RECTANGLE, NodeShapeEnum.OVAL, NodeShapeEnum.NONE,
      NodeShapeEnum.SQUARE, NodeShapeEnum.POLYGON, NodeShapeEnum.HOUSE,
      NodeShapeEnum.INVHOUSE, NodeShapeEnum.DOUBLECIRCLE, NodeShapeEnum.DOUBLEOCTAGON,
      NodeShapeEnum.TRIPLEOCTAGON, NodeShapeEnum.M_DIAMOND, NodeShapeEnum.M_SQUARE,
      NodeShapeEnum.M_CIRCLE, NodeShapeEnum.TAB, NodeShapeEnum.FOLDER,
      NodeShapeEnum.BOX3D, NodeShapeEnum.COMPONENT
  };

  @Test
  void labelsFitOutlinesAndInnermostPeripheries() throws Exception {
    for (NodeShapeEnum shape : SHAPES) {
      for (String label : new String[]{"WWWWWWWWWWWWWWWW", "A\nB\nC\nD\nE", "x"}) {
        for (Rankdir rankdir : Rankdir.values()) {
          Node node = Node.builder().shape(shape).label(label).build();
          NodeDrawProp prop = Layout.DOT.getLayoutEngine().layout(
              Graphviz.digraph().rankdir(rankdir).addNode(node).build()).getNodeDrawProp(node);
          ShapePropCalc calc = prop.nodeAttrs().getShape().getShapePropCalc();
          Box box = prop;
          if (calc instanceof PeripheryNodePropCalc) {
            PeripheryNodePropCalc periphery = (PeripheryNodePropCalc) calc;
            box = periphery.innerBox(prop, periphery.getBorders() - 1);
            calc = periphery.getOutline();
          }
          FlatPoint center = prop.getLabelCenter();
          FlatPoint size = prop.getLabelSize();
          for (int x : new int[]{-1, 1}) {
            for (int y : new int[]{-1, 1}) {
              Assertions.assertTrue(calc.in(box, new FlatPoint(
                  center.getX() + x * size.getWidth() / 2,
                  center.getY() + y * size.getHeight() / 2)), shape + " " + rankdir + " " + label);
            }
          }
        }
      }
    }
  }

  @Test
  void polygonDefaultsToFourStretchableSidesAndHonorsSides() throws Exception {
    for (int sides : new int[]{4, 5, 8, 11, 20}) {
      Node.NodeBuilder builder = Node.builder().shape(NodeShapeEnum.POLYGON)
          .width(3).height(1).fixedSize(true);
      if (sides != 4) {
        builder.sides(sides);
      }
      Node node = builder.build();
      NodeDrawProp prop = layout(node).getNodeDrawProp(node);
      Assertions.assertEquals(3, prop.getWidth() / prop.getHeight(), 1e-8);
      RegularPolylinePropCalc calc = (RegularPolylinePropCalc)
          prop.nodeAttrs().getShape().getShapePropCalc();
      Assertions.assertEquals(sides, calc.calcPoints(prop).size());
      SvgDrawBoard board = render(layout(node), node);
      Assertions.assertEquals(sides + 1, board.svgDocument.getElementById(
          board.nodeId(node) + "_polygon").getAttribute("points").trim().split("\\s+").length);
    }
    for (NodeShapeEnum shape : new NodeShapeEnum[]{NodeShapeEnum.SQUARE,
        NodeShapeEnum.M_SQUARE, NodeShapeEnum.DOUBLECIRCLE, NodeShapeEnum.M_CIRCLE}) {
      Node node = Node.builder().shape(shape).width(3).height(1).fixedSize(true).build();
      NodeDrawProp prop = layout(node).getNodeDrawProp(node);
      Assertions.assertEquals(prop.getWidth(), prop.getHeight(), 1e-8, shape.name());
    }
  }

  @Test
  void polygonGeometryAlsoSupportsThreeSides() {
    StretchablePolygonPropCalc calc = new StretchablePolygonPropCalc(3);
    FlatPoint size = calc.minContainerSize(20, 100);
    Box box = new DefaultBox(-size.getWidth() / 2, size.getWidth() / 2,
                            -size.getHeight() / 2, size.getHeight() / 2);
    Assertions.assertEquals(3, calc.calcPoints(box).size());
    Assertions.assertTrue(calc.in(box, new FlatPoint(-50, -10)));
    Assertions.assertTrue(calc.in(box, new FlatPoint(50, 10)));
  }

  @Test
  void polygonLabelsFitConfiguredSidesAndRegularModeRemainsOptIn() throws Exception {
    for (int sides : new int[]{4, 5, 7, 12, 20}) {
      for (boolean regular : new boolean[]{false, true}) {
        Node node = Node.builder().shape(NodeShapeEnum.POLYGON).sides(sides)
            .regular(regular).label("WWWWWWWWWWWWWWWW").build();
        NodeDrawProp prop = layout(node).getNodeDrawProp(node);
        if (regular) {
          Assertions.assertEquals(prop.getWidth(), prop.getHeight(), 1e-8);
        } else {
          Assertions.assertTrue(prop.getWidth() > prop.getHeight() * 2);
        }
        FlatPoint center = prop.getLabelCenter();
        FlatPoint size = prop.getLabelSize();
        for (int x : new int[]{-1, 1}) {
          for (int y : new int[]{-1, 1}) {
            Assertions.assertTrue(prop.shapeProp().in(prop, new FlatPoint(
                center.getX() + x * size.getWidth() / 2,
                center.getY() + y * size.getHeight() / 2)), "sides=" + sides);
          }
        }
      }
    }
  }

  @Test
  void tableCellsFitExpandedShapes() throws Exception {
    for (NodeShapeEnum shape : SHAPES) {
      Node node = Node.builder().shape(shape).table(Html.table().border(0)
          .cellBorder(1).cellSpacing(0).cellPadding(6)
          .tr(Html.td().text("input"), Html.td().text("wide body"))).build();
      DrawGraph draw = layout(node);
      NodeDrawProp owner = draw.getNodeDrawProp(node);
      for (Node child : owner.getAssemble().getCells()) {
        NodeDrawProp cell = draw.getNodeDrawProp(child);
        for (double x : new double[]{cell.getLeftBorder(), cell.getRightBorder()}) {
          for (double y : new double[]{cell.getUpBorder(), cell.getDownBorder()}) {
            Assertions.assertTrue(owner.shapeProp().in(owner, new FlatPoint(x, y)), shape.name());
          }
        }
      }
    }
  }

  @Test
  void aliasesPreserveExistingDimensionsAndNoneRetainsLabel() throws Exception {
    NodeShapeEnum[][] pairs = {{NodeShapeEnum.RECTANGLE, NodeShapeEnum.RECT},
        {NodeShapeEnum.OVAL, NodeShapeEnum.ELLIPSE},
        {NodeShapeEnum.NONE, NodeShapeEnum.PLAINTEXT}};
    for (NodeShapeEnum[] pair : pairs) {
      Node a = Node.builder().shape(pair[0]).label("alias label").build();
      Node b = Node.builder().shape(pair[1]).label("alias label").build();
      NodeDrawProp pa = layout(a).getNodeDrawProp(a);
      NodeDrawProp pb = layout(b).getNodeDrawProp(b);
      Assertions.assertEquals(pb.getWidth(), pa.getWidth(), 1e-8);
      Assertions.assertEquals(pb.getHeight(), pa.getHeight(), 1e-8);
      String svg = Graphviz.digraph().addNode(a).build().toSvgStr();
      Assertions.assertTrue(svg.contains("alias&#160;label"), pair[0] + " " + svg);
      if (pair[0] == NodeShapeEnum.NONE) {
        Assertions.assertFalse(svg.contains("class=\"shape\""));
      }
    }
  }

  @Test
  void tinyPeripheriesHavePositiveNestedDimensions() throws Exception {
    for (NodeShapeEnum shape : new NodeShapeEnum[]{NodeShapeEnum.DOUBLECIRCLE,
        NodeShapeEnum.DOUBLEOCTAGON, NodeShapeEnum.TRIPLEOCTAGON}) {
      Node node = Node.builder().shape(shape).label("").width(.02).height(.01)
          .fixedSize(true).build();
      NodeDrawProp prop = layout(node).getNodeDrawProp(node);
      PeripheryNodePropCalc calc = (PeripheryNodePropCalc) shape.getShapePropCalc();
      Box previous = prop;
      for (int i = 1; i < calc.getBorders(); i++) {
        Box inner = calc.innerBox(prop, i);
        Assertions.assertTrue(inner.getWidth() > 0 && inner.getHeight() > 0);
        Assertions.assertTrue(inner.getWidth() < previous.getWidth());
        Assertions.assertTrue(inner.getHeight() < previous.getHeight());
        previous = inner;
      }
    }
  }

  @Test
  void renderedSilhouetteMatchesContainmentAndDecorationsStayUnfilled() throws Exception {
    for (NodeShapeEnum shape : SHAPES) {
      Node node = Node.builder().shape(shape).label("").width(2).height(1)
          .fixedSize(true).fillColor(Color.RED).color(Color.BLUE)
          .style(NodeStyle.DASHED).penWidth(2).build();
      DrawGraph draw = layout(node);
      NodeDrawProp prop = draw.getNodeDrawProp(node);
      SvgDrawBoard board = render(draw, node);
      final int[] outlines = {0};
      final int[] decorations = {0};
      board.svgDocument.accessEles((element, children) -> {
        if (!"shape".equals(element.getAttribute("class"))) {
          return;
        }
        Assertions.assertEquals(Color.BLUE.value(), element.getAttribute("stroke"));
        Assertions.assertEquals("5,2", element.getAttribute("stroke-dasharray"));
        Assertions.assertEquals("2.0", element.getAttribute("stroke-width"));
        if ("true".equals(element.getAttribute("data-node-decoration"))) {
          decorations[0]++;
          Assertions.assertEquals("none", element.getAttribute("fill"), shape.name());
          return;
        }
        outlines[0]++;
        Assertions.assertEquals(Color.RED.value(), element.getAttribute("fill"));
        if ("polygon".equals(element.tagName())) {
          Path2D path = new Path2D.Double();
          String[] points = element.getAttribute("points").trim().split("\\s+");
          for (int i = 0; i < points.length; i++) {
            String[] xy = points[i].split(",");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            if (i == 0) {
              path.moveTo(x, y);
            } else {
              path.lineTo(x, y);
            }
          }
          path.closePath();
          // Avoid boundary samples so this independently checks the entire filled silhouette.
          for (double x = -.07; x < 1.1; x += .037) {
            for (double y = -.07; y < 1.1; y += .037) {
              FlatPoint p = new FlatPoint(prop.getLeftBorder() + x * prop.getWidth(),
                                          prop.getUpBorder() + y * prop.getHeight());
              Assertions.assertEquals(path.contains(p.getX(), p.getY()),
                                      prop.shapeProp().in(prop, p), shape.name());
            }
          }
        } else {
          Assertions.assertEquals("ellipse", element.tagName());
          Assertions.assertEquals(prop.getWidth() / 2,
              Double.parseDouble(element.getAttribute("rx")), 1e-8);
          Assertions.assertEquals(prop.getHeight() / 2,
              Double.parseDouble(element.getAttribute("ry")), 1e-8);
        }
      });
      Assertions.assertEquals(shape == NodeShapeEnum.NONE ? 0 : 1, outlines[0], shape.name());
      int expectedDecorations;
      switch (shape) {
        case DOUBLECIRCLE:
        case DOUBLEOCTAGON:
        case TAB:
        case FOLDER:
          expectedDecorations = 1;
          break;
        case TRIPLEOCTAGON:
        case M_CIRCLE:
        case BOX3D:
        case COMPONENT:
          expectedDecorations = 2;
          break;
        case M_DIAMOND:
        case M_SQUARE:
          expectedDecorations = 4;
          break;
        default:
          expectedDecorations = 0;
          break;
      }
      Assertions.assertEquals(expectedDecorations, decorations[0], shape.name());
      try (DefaultGraphResource resource = new DefaultImgConverter()
          .convert(board.svgDocument, draw, FileType.PNG)) {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(resource.bytes()));
        Assertions.assertNotNull(image, shape.name());
        if (shape != NodeShapeEnum.NONE) {
          Assertions.assertEquals(java.awt.Color.RED.getRGB(),
              image.getRGB((int) prop.getX(), (int) prop.getY()), shape.name());
        }
      }
    }
  }

  @Test
  void concaveNotchesAreNotClippingObstacles() {
    Box box = new DefaultBox(0, 100, 0, 100);
    Assertions.assertFalse(NodeShapeEnum.FOLDER.in(box, new FlatPoint(80, 5)));
    Assertions.assertFalse(NodeShapeEnum.HOUSE.in(box, new FlatPoint(5, 5)));
    Assertions.assertFalse(NodeShapeEnum.INVHOUSE.in(box, new FlatPoint(5, 95)));
  }

  @Test
  void componentDiagonalClipDoesNotReenterTerminal() {
    Box box = new DefaultBox(0, 100, 0, 100);
    ShapePropCalc calc = NodeShapeEnum.COMPONENT.getShapePropCalc();
    FlatPoint center = new FlatPoint(50, 50);
    FlatPoint outside = new FlatPoint(-28, 30.5);
    FlatPoint clipped = StraightPathClip.straightLineClipShape(box, calc, center, outside);
    Assertions.assertEquals(0, clipped.getX(), .1);
    Assertions.assertEquals(37.5, clipped.getY(), .1);
    // The former notch endpoint (15, 41.25) left a shaft crossing the upper terminal.
    for (int i = 1; i <= 100; i++) {
      double t = i / 100D;
      Assertions.assertFalse(calc.in(box, new FlatPoint(
          clipped.getX() + t * (outside.getX() - clipped.getX()),
          clipped.getY() + t * (outside.getY() - clipped.getY()))));
    }
  }

  @Test
  void componentOutlineIsConvexAndCenterRaysExitOnlyOnce() {
    PolygonNodePropCalc calc = (PolygonNodePropCalc) NodeShapeEnum.COMPONENT.getShapePropCalc();
    for (Box box : new Box[]{new DefaultBox(0, 100, 0, 100),
        new DefaultBox(-130, 270, 20, 60), new DefaultBox(10, 50, -200, 200)}) {
      List<FlatPoint> points = calc.calcPoints(box);
      // Every vertex and the center lie on the interior side of every oriented edge.
      // This proves convexity (stronger than center-star-convexity), including after stretching.
      for (int i = 0; i < points.size(); i++) {
        FlatPoint a = points.get(i);
        FlatPoint b = points.get((i + 1) % points.size());
        for (int j = 0; j <= points.size(); j++) {
          FlatPoint p = j == points.size() ? new FlatPoint(box.getX(), box.getY()) : points.get(j);
          double cross = (b.getX() - a.getX()) * (p.getY() - a.getY())
              - (b.getY() - a.getY()) * (p.getX() - a.getX());
          Assertions.assertTrue(cross >= 0);
        }
      }
      for (int angle = 0; angle < 360; angle++) {
        double dx = box.getWidth() * Math.cos(Math.toRadians(angle));
        double dy = box.getHeight() * Math.sin(Math.toRadians(angle));
        boolean exited = false;
        for (int i = 0; i <= 100; i++) {
          boolean inside = calc.in(box,
              new FlatPoint(box.getX() + dx * i / 100, box.getY() + dy * i / 100));
          Assertions.assertFalse(exited && inside, "Ray re-entered at angle " + angle);
          exited |= !inside;
        }
        Assertions.assertTrue(exited);
      }
    }
  }

  @Test
  void edgesClipToOuterSilhouette() throws Exception {
    for (NodeShapeEnum shape : SHAPES) {
      for (Rankdir rankdir : Rankdir.values()) {
        for (Splines splines : new Splines[]{Splines.LINE, Splines.POLYLINE, Splines.SPLINE,
            Splines.ORTHO}) {
          Node a = Node.builder().shape(shape).label("shape").build();
          Node b = Node.builder().shape(shape).label("shape").build();
          Line line = Line.builder(a, b).arrowHead(ArrowShape.NONE).build();
          DrawGraph draw = Layout.DOT.getLayoutEngine().layout(Graphviz.digraph()
              .rankdir(rankdir).splines(splines).addLine(line).build());
          LineDrawProp prop = draw.getLineDrawProp(line);
          assertBoundary(draw.getNodeDrawProp(a), prop.isHeadStart() ? prop.getEnd() : prop.getStart());
          assertBoundary(draw.getNodeDrawProp(b), prop.isHeadStart() ? prop.getStart() : prop.getEnd());
        }
      }
    }
  }

  private void assertBoundary(NodeDrawProp node, FlatPoint endpoint) {
    double dx = endpoint.getX() - node.getX();
    double dy = endpoint.getY() - node.getY();
    double length = Math.hypot(dx, dy);
    double tolerance = 1.5;
    Assertions.assertTrue(node.shapeProp().in(node, new FlatPoint(
        endpoint.getX() - tolerance * dx / length, endpoint.getY() - tolerance * dy / length)),
        node.nodeAttrs().getShape().getName());
    Assertions.assertFalse(node.shapeProp().in(node, new FlatPoint(
        endpoint.getX() + tolerance * dx / length, endpoint.getY() + tolerance * dy / length)),
        node.nodeAttrs().getShape().getName());
  }

  private DrawGraph layout(Node node) throws Exception {
    return Layout.DOT.getLayoutEngine().layout(Graphviz.digraph().addNode(node).build());
  }

  private SvgDrawBoard render(DrawGraph draw, Node node) {
    SvgDrawBoard board = new SvgDrawBoard(draw);
    NodeDrawProp prop = draw.getNodeDrawProp(node);
    SvgBrush brush = board.drawNode(prop);
    new NodeShapeEditor().edit(prop, brush);
    new NodeStyleEditor().edit(prop, brush);
    new NodeColorEditor().edit(prop, brush);
    return board;
  }
}
