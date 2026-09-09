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

package org.graphper.draw.svg.line;

import static org.graphper.draw.svg.SvgEditor.getPathPointStr;

import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ArrowDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.LineEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

/** Draws head and tail arrow shapes for SVG edges. */
public class LineArrowEditor implements LineEditor<SvgBrush>, SvgConstants {

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    if (lineDrawProp.getArrowHead() != null) {
      drawArrow(
          brush, lineDrawProp.getArrowHead(), "head", lineDrawProp.lineAttrs().getArrowHead());
    }
    if (lineDrawProp.getArrowTail() != null) {
      drawArrow(
          brush, lineDrawProp.getArrowTail(), "tail", lineDrawProp.lineAttrs().getArrowTail());
    }
    return true;
  }

  private void drawArrow(SvgBrush brush, ArrowDrawProp arrow, String id, ArrowShape shape) {
    switch (shape) {
      case NORMAL:
      case ONORMAL:
        polygon(brush, arrow, id, shape, 1, 0, 0, 1d / 3, 0, -1d / 3, 1, 0);
        break;
      case INV:
      case OINV:
        polygon(brush, arrow, id, shape, 0, 0, 1, 1d / 3, 1, -1d / 3, 0, 0);
        break;
      case BOX:
      case OBOX:
        polygon(brush, arrow, id, shape, 0, 0.5, 0, -0.5, 1, -0.5, 1, 0.5);
        break;
      case DIAMOND:
      case ODIAMOND:
        polygon(brush, arrow, id, shape, 0, 0, 0.5, 1d / 3, 1, 0, 0.5, -1d / 3);
        break;
      case DOT:
      case ODOT:
        dot(brush, arrow, id, shape);
        break;
      case VEE:
        // Preserve the existing vee's wings extending behind the clipped shaft endpoint.
        polygon(brush, arrow, id, shape, -1, 2d / 3, 0, 0, -1, -2d / 3, 1, 0, -1, 2d / 3);
        break;
      case CROW:
        polygon(brush, arrow, id, shape, 0, 0, 1, 0.45, 0.5, 0, 1, 0, 0.5, 0, 1, -0.45, 0, 0);
        break;
      case TEE:
        polygon(brush, arrow, id, shape, 0.4, 1, 0.8, 1, 0.8, -1, 0.4, -1);
        axis(brush, arrow, id);
        break;
      case CURVE:
      case ICURVE:
        curve(brush, arrow, id, shape == ArrowShape.ICURVE);
        break;
      default:
        break;
    }
  }

  private void polygon(
      SvgBrush brush, ArrowDrawProp arrow, String id, ArrowShape shape, double... coordinates) {
    Element element = brush.getOrCreateChildElementById(POLYGON_ELE + id, POLYGON_ELE);
    setBasicProp(element, shape.isNeedFill());
    StringBuilder points = new StringBuilder();
    for (int i = 0; i < coordinates.length; i += 2) {
      points.append(getPathPointStr(point(arrow, coordinates[i], coordinates[i + 1])));
    }
    element.setAttribute(POINTS, points.toString());
    brush.addGroup(arrowGroup(arrow), element);
  }

  private void dot(SvgBrush brush, ArrowDrawProp arrow, String id, ArrowShape shape) {
    Element element = brush.getOrCreateChildElementById(ELLIPSE_ELE + id, ELLIPSE_ELE);
    setBasicProp(element, shape.isNeedFill());
    FlatPoint center = point(arrow, 0.5, 0);
    double radius =
        Math.hypot(
                arrow.getAxisEnd().getX() - arrow.getAxisBegin().getX(),
                arrow.getAxisEnd().getY() - arrow.getAxisBegin().getY())
            / 2;
    element.setAttribute(CX, String.valueOf(center.getX()));
    element.setAttribute(CY, String.valueOf(center.getY()));
    element.setAttribute(RX, String.valueOf(radius));
    element.setAttribute(RY, String.valueOf(radius));
    brush.addGroup(arrowGroup(arrow), element);
  }

  private void curve(SvgBrush brush, ArrowDrawProp arrow, String id, boolean inverted) {
    double from = inverted ? 1 : 0;
    double to = inverted ? 0 : 1;
    for (int i = 0; i < 2; i++) {
      Element element = brush.getOrCreateChildElementById(PATH_ELE + id + i, PATH_ELE);
      setBasicProp(element, false);
      double side = i == 0 ? 0.5 : -0.5;
      FlatPoint start = point(arrow, from, side);
      element.setAttribute(
          D,
          PATH_START_M
              + getPathPointStr(start, false)
              + CURVE_PATH_MARK
              + getPathPointStr(start)
              + getPathPointStr(point(arrow, to, side))
              + getPathPointStr(point(arrow, to, 0), false));
      brush.addGroup(arrowGroup(arrow), element);
    }
    axis(brush, arrow, id);
  }

  private void axis(SvgBrush brush, ArrowDrawProp arrow, String id) {
    Element element = brush.getOrCreateChildElementById(PATH_ELE + id + "2", PATH_ELE);
    setBasicProp(element, false);
    element.setAttribute(
        D,
        PATH_START_M
            + getPathPointStr(arrow.getAxisBegin())
            + getPathPointStr(arrow.getAxisEnd(), false));
    brush.addGroup(arrowGroup(arrow), element);
  }

  private FlatPoint point(ArrowDrawProp arrow, double along, double side) {
    FlatPoint begin = arrow.getAxisBegin();
    double dx = arrow.getAxisEnd().getX() - begin.getX();
    double dy = arrow.getAxisEnd().getY() - begin.getY();
    // The unnormalized perpendicular scales with the axis and stays finite for a zero axis.
    return new FlatPoint(
        begin.getX() + along * dx - side * dy, begin.getY() + along * dy + side * dx);
  }

  private void setBasicProp(Element element, boolean needFill) {
    element.setAttribute(CLASS, ARROWHEAD);
    element.setAttribute(FILL, needFill ? Color.BLACK.value() : NONE);
    element.setAttribute(STROKE, Color.BLACK.value());
  }

  private String arrowGroup(ArrowDrawProp arrow) {
    return arrow.isHead() ? HEAD_ARROW_GROUP_KEY : TAIL_ARROW_GROUP_KEY;
  }
}
