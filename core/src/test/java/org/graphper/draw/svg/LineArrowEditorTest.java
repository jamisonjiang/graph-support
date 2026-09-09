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

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ArrowDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.common.DefaultImgConverter;
import org.graphper.draw.svg.line.LineArrowEditor;
import org.graphper.draw.svg.line.LineStyleEditor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.w3c.dom.NodeList;

class LineArrowEditorTest implements SvgConstants {

  @ParameterizedTest
  @EnumSource(ArrowShape.class)
  void geometryRotatesAndScalesForBothEnds(ArrowShape shape) {
    SvgBrush reference = render(shape, shape, new FlatPoint(0, 0), new FlatPoint(1, 0));
    double[][] axes = {{30, 0}, {-30, 0}, {0, 30}, {0, -30},
        {18, 24}, {-18, 24}, {18, -24}, {-18, -24}};
    for (double[] axis : axes) {
      FlatPoint begin = new FlatPoint(80, 90);
      FlatPoint end = new FlatPoint(80 + axis[0], 90 + axis[1]);
      SvgBrush actual = render(shape, shape, begin, end);
      for (String group : new String[]{HEAD_ARROW_GROUP_KEY, TAIL_ARROW_GROUP_KEY}) {
        List<Element> expectedElements = reference.getEleGroup(group);
        List<Element> elements = actual.getEleGroup(group);
        assertEquals(expectedElements.size(), elements.size(), shape.name());
        assertEquals(shape == ArrowShape.NONE ? 0
            : shape == ArrowShape.CURVE || shape == ArrowShape.ICURVE ? 3
            : shape == ArrowShape.TEE ? 2 : 1, elements.size());
        for (int i = 0; i < elements.size(); i++) {
          Element expected = expectedElements.get(i);
          Element element = elements.get(i);
          assertEquals(expected.tagName(), element.tagName());
          double[] referencePoints = coordinates(expected);
          double[] actualPoints = coordinates(element);
          assertEquals(referencePoints.length, actualPoints.length);
          for (int j = 0; j < actualPoints.length; j += 2) {
            double x = referencePoints[j];
            double y = referencePoints[j + 1];
            assertEquals(80 + x * axis[0] - y * axis[1], actualPoints[j], 1e-9);
            assertEquals(90 + x * axis[1] + y * axis[0], actualPoints[j + 1], 1e-9);
          }
          if (ELLIPSE_ELE.equals(element.tagName())) {
            assertEquals(15, Double.parseDouble(element.getAttribute(RX)), 1e-9);
            assertEquals(element.getAttribute(RX), element.getAttribute(RY));
          }
        }
      }
    }
  }

  @Test
  void silhouettesAndLegacyRatios() {
    assertOutline(ArrowShape.NORMAL, 1, 0, 0, 1d / 3, 0, -1d / 3, 1, 0);
    assertOutline(ArrowShape.INV, 0, 0, 1, 1d / 3, 1, -1d / 3, 0, 0);
    assertOutline(ArrowShape.BOX, 0, 0.5, 0, -0.5, 1, -0.5, 1, 0.5);
    assertOutline(ArrowShape.DIAMOND, 0, 0, 0.5, 1d / 3, 1, 0, 0.5, -1d / 3);
    assertOutline(ArrowShape.VEE, -1, 2d / 3, 0, 0, -1, -2d / 3, 1, 0, -1, 2d / 3);
    assertOutline(ArrowShape.CROW, 0, 0, 1, 0.45, 0.5, 0, 1, 0,
                  0.5, 0, 1, -0.45, 0, 0);
    assertOutline(ArrowShape.TEE, 0.4, 1, 0.8, 1, 0.8, -1, 0.4, -1);
    assertOutline(ArrowShape.CURVE, 0, 0.5, 0, 0.5, 1, 0.5, 1, 0);
    assertOutline(ArrowShape.ICURVE, 1, 0.5, 1, 0.5, 0, 0.5, 0, 0);
    assertOutline(ArrowShape.DOT, 0.5, 0);
    ArrowShape[] legacy = {ArrowShape.VEE, ArrowShape.CURVE, ArrowShape.BOX,
        ArrowShape.DOT, ArrowShape.NONE, ArrowShape.NORMAL};
    double[] ratios = {0.75, 1, 0.75, 0.75, 1, 1};
    for (int i = 0; i < legacy.length; i++) {
      assertEquals(i, legacy[i].ordinal());
      assertEquals(ratios[i], legacy[i].getClipRatio());
    }
  }

  @ParameterizedTest
  @EnumSource(ArrowShape.class)
  void zeroAxesStayFinite(ArrowShape shape) {
    SvgBrush brush = render(shape, shape, new FlatPoint(40, 50), new FlatPoint(40, 50));
    for (String group : new String[]{HEAD_ARROW_GROUP_KEY, TAIL_ARROW_GROUP_KEY}) {
      for (Element element : brush.getEleGroup(group)) {
        double[] points = coordinates(element);
        for (int i = 0; i < points.length; i += 2) {
          assertEquals(40, points[i]);
          assertEquals(50, points[i + 1]);
        }
        if (ELLIPSE_ELE.equals(element.tagName())) {
          assertEquals(0, Double.parseDouble(element.getAttribute(RX)));
        }
      }
    }
  }

  @Test
  void hollowVariantsShareGeometryAndKeepIndependentHeadTailStyles() {
    ArrowShape[] solid = {ArrowShape.NORMAL, ArrowShape.INV, ArrowShape.BOX,
        ArrowShape.DOT, ArrowShape.DIAMOND};
    ArrowShape[] hollow = {ArrowShape.ONORMAL, ArrowShape.OINV, ArrowShape.OBOX,
        ArrowShape.ODOT, ArrowShape.ODIAMOND};
    for (int i = 0; i < solid.length; i++) {
      assertTrue(solid[i].isNeedFill());
      assertFalse(hollow[i].isNeedFill());
      assertEquals(solid[i].getClipRatio(), hollow[i].getClipRatio());
      for (boolean reverse : new boolean[]{false, true}) {
        ArrowShape head = reverse ? hollow[i] : solid[i];
        ArrowShape tail = reverse ? solid[i] : hollow[i];
        SvgBrush brush = render(head, tail, new FlatPoint(0, 0), new FlatPoint(30, 0));
        Element headElement = brush.getEleGroup(HEAD_ARROW_GROUP_KEY).get(0);
        Element tailElement = brush.getEleGroup(TAIL_ARROW_GROUP_KEY).get(0);
        assertArrayEquals(coordinates(headElement), coordinates(tailElement), 1e-9);
        assertEquals(head.isNeedFill() ? Color.RED.value() : NONE, headElement.getAttribute(FILL));
        assertEquals(tail.isNeedFill() ? Color.RED.value() : NONE, tailElement.getAttribute(FILL));
      }
    }
  }

  @ParameterizedTest
  @EnumSource(ArrowShape.class)
  void defaultRasterConverterDrawsEveryShape(ArrowShape shape) throws Exception {
    DrawGraph graph = new DrawGraph(Graphviz.digraph().build());
    SvgDocument document = new SvgDocument();
    Element root = document.createElement(SVG_ELE);
    root.setAttribute(WIDTH, "240pt");
    root.setAttribute(HEIGHT, "240pt");
    SvgBrush brush = new SvgBrush("edge", root, document, new SvgDrawBoard(graph));
    Line line = line(shape, shape);
    LineDrawProp prop = new LineDrawProp(line, line.lineAttrs(), graph);
    prop.setArrowHead(new ArrowDrawProp(true, new FlatPoint(80, 60), new FlatPoint(140, 60)));
    prop.setArrowTail(new ArrowDrawProp(false, new FlatPoint(140, 180), new FlatPoint(80, 180)));
    new LineArrowEditor().edit(prop, brush);
    new LineStyleEditor().edit(prop, brush);
    try (GraphResource resource = new DefaultImgConverter().convert(document, graph, FileType.PNG)) {
      BufferedImage image = ImageIO.read(resource.inputStream());
      assertNotNull(image);
      for (int top : new int[]{0, 160}) {
        int painted = 0;
        for (int y = top; y < Math.min(top + 160, image.getHeight()); y++) {
          for (int x = 0; x < image.getWidth(); x++) {
            if ((image.getRGB(x, y) & 0xffffff) != 0xffffff) {
              painted++;
            }
          }
        }
        assertEquals(shape != ArrowShape.NONE, painted > 0, shape.name());
      }
      if (shape.name().startsWith("O")) {
        assertEquals(0xffffff, image.getRGB(147, 80) & 0xffffff);
        assertEquals(0xffffff, image.getRGB(147, 240) & 0xffffff);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(ArrowShape.class)
  void layoutSvgSupportsDirectionsAndZeroSize(ArrowShape shape) throws Exception {
    for (Rankdir rankdir : Rankdir.values()) {
      for (Dir dir : Dir.values()) {
        for (double size : new double[]{0, 1}) {
          Line line = Line.builder(Node.builder().label("a").build(), Node.builder().label("b").build())
              .arrowHead(shape).arrowTail(shape).dir(dir).arrowSize(size).color(Color.RED).build();
          String svg = Graphviz.digraph().rankdir(rankdir).splines(Splines.LINE)
              .addLine(line).build().toSvgStr();
          assertFalse(svg.contains("NaN"), shape.name());
          assertFalse(svg.contains("Infinity"), shape.name());
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
          NodeList elements = factory.newDocumentBuilder()
              .parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)))
              .getElementsByTagName("*");
          int heads = 0;
          int tails = 0;
          for (int i = 0; i < elements.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(i);
            if (!ARROWHEAD.equals(element.getAttribute(CLASS))) {
              continue;
            }
            assertEquals(shape.isNeedFill() ? Color.RED.value() : NONE, element.getAttribute(FILL));
            if (element.getAttribute(ID).contains("head")) {
              heads++;
            } else if (element.getAttribute(ID).contains("tail")) {
              tails++;
            }
          }
          assertEquals(shape != ArrowShape.NONE && (dir == Dir.FORWARD || dir == Dir.BOTH), heads > 0);
          assertEquals(shape != ArrowShape.NONE && (dir == Dir.BACK || dir == Dir.BOTH), tails > 0);
        }
      }
    }
  }

  private void assertOutline(ArrowShape shape, double... expected) {
    SvgBrush brush = render(shape, shape, new FlatPoint(0, 0), new FlatPoint(1, 0));
    assertArrayEquals(expected, coordinates(brush.getEleGroup(HEAD_ARROW_GROUP_KEY).get(0)), 1e-9);
  }

  private SvgBrush render(ArrowShape head, ArrowShape tail, FlatPoint begin, FlatPoint end) {
    DrawGraph graph = new DrawGraph(Graphviz.digraph().build());
    SvgDocument document = new SvgDocument();
    SvgBrush brush = new SvgBrush("edge", document.createElement(SVG_ELE), document,
                                  new SvgDrawBoard(graph));
    Line line = line(head, tail);
    LineDrawProp prop = new LineDrawProp(line, line.lineAttrs(), graph);
    prop.setArrowHead(new ArrowDrawProp(true, begin, end));
    prop.setArrowTail(new ArrowDrawProp(false, begin, end));
    new LineArrowEditor().edit(prop, brush);
    for (String group : new String[]{HEAD_ARROW_GROUP_KEY, TAIL_ARROW_GROUP_KEY}) {
      ArrowShape shape = HEAD_ARROW_GROUP_KEY.equals(group) ? head : tail;
      for (Element element : brush.getEleGroup(group)) {
        if (!shape.isNeedFill()) {
          assertEquals(NONE, element.getAttribute(FILL));
        }
      }
    }
    new LineStyleEditor().edit(prop, brush);
    for (String group : new String[]{HEAD_ARROW_GROUP_KEY, TAIL_ARROW_GROUP_KEY}) {
      for (Element element : brush.getEleGroup(group)) {
        assertEquals(Color.RED.value(), element.getAttribute(STROKE));
        assertEquals("2.0", element.getAttribute(STROKE_WIDTH));
        assertNull(element.getAttribute(STROKE_DASHARRAY));
      }
    }
    return brush;
  }

  private Line line(ArrowShape head, ArrowShape tail) {
    return Line.builder(Node.builder().build(), Node.builder().build())
        .arrowHead(head).arrowTail(tail).dir(Dir.BOTH).color(Color.RED)
        .penWidth(2).style(LineStyle.BOLD, LineStyle.DASHED).build();
  }

  private double[] coordinates(Element element) {
    if (ELLIPSE_ELE.equals(element.tagName())) {
      return new double[]{Double.parseDouble(element.getAttribute(CX)),
          Double.parseDouble(element.getAttribute(CY))};
    }
    String data = element.getAttribute(POLYGON_ELE.equals(element.tagName()) ? POINTS : D);
    // Only the simple move/line and cubic syntax supported by the core rasterizer is allowed.
    String[] tokens = data.replace("M", " ").replace("C", " ").trim().split("[ ,]+");
    List<Double> numbers = new ArrayList<>();
    for (String token : tokens) {
      double value = Double.parseDouble(token);
      assertTrue(Double.isFinite(value));
      numbers.add(value);
    }
    assertEquals(0, numbers.size() % 2);
    double[] result = new double[numbers.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = numbers.get(i);
    }
    return result;
  }
}
