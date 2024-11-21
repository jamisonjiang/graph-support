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
import org.graphper.def.Vectors;
import org.graphper.draw.ArrowDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.LineEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class LineArrowEditor implements LineEditor<SvgBrush>, SvgConstants {

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    if (lineDrawProp.getArrowHead() != null) {
      drawArrow(lineDrawProp, brush, lineDrawProp.getArrowHead(), "head",
                lineDrawProp.lineAttrs().getArrowHead());
    }

    if (lineDrawProp.getArrowTail() != null) {
      drawArrow(lineDrawProp, brush, lineDrawProp.getArrowTail(), "tail",
                lineDrawProp.lineAttrs().getArrowTail());
    }

    return true;
  }

  private void drawArrow(LineDrawProp lineDrawProp, SvgBrush brush,
                         ArrowDrawProp arrow, String id, ArrowShape shape) {
    switch (shape) {
      case NORMAL:
        normal(brush, arrow, id);
        break;
      case BOX:
        box(brush, arrow, id);
        break;
      case DOT:
        dot(brush, arrow, id);
        break;
      case VEE:
        vee(brush, arrow, id);
        break;
      case CURVE:
        curve(brush, arrow, id);
        break;
      default:
        break;
    }
  }

  private void normal(SvgBrush brush, ArrowDrawProp arrow, String id) {
    Element arrowElement = brush.getOrCreateChildElementById(POLYGON_ELE + id, POLYGON_ELE);
    setBasicProp(arrowElement);

    FlatPoint axisBegin = arrow.getAxisBegin();
    FlatPoint axisEnd = arrow.getAxisEnd();
    String points = axisEnd.getX() + SvgConstants.COMMA + axisEnd.getY() + SvgConstants.SPACE;

    FlatPoint axis = Vectors.sub(axisBegin, axisEnd);
    FlatPoint dirVector = new FlatPoint(-axis.getY(), axis.getX());

    double offsetX = axis.dist() * Math.abs(dirVector.getX()) / (3 * dirVector.dist());
    double offsetY = axis.dist() * Math.abs(dirVector.getY()) / (3 * dirVector.dist());
    points += getArrowSide(posSlope(axisBegin, axisEnd), offsetX, offsetY, true, axisBegin);
    points = points + axisEnd.getX() + SvgConstants.COMMA + axisEnd.getY();
    arrowElement.setAttribute(SvgConstants.POINTS, points);

    brush.addGroup(arrowGroup(arrow), arrowElement);
  }

  private void box(SvgBrush brush, ArrowDrawProp arrow, String id) {
    Element arrowElement = brush.getOrCreateChildElementById(POLYGON_ELE + id, POLYGON_ELE);

    setBasicProp(arrowElement);

    FlatPoint axisBegin = arrow.getAxisBegin();
    FlatPoint axisEnd = arrow.getAxisEnd();

    FlatPoint axis = Vectors.sub(axisBegin, axisEnd);
    FlatPoint dirVector = new FlatPoint(-axis.getY(), axis.getX());

    double offsetX = axis.dist() * Math.abs(dirVector.getX()) / (2 * dirVector.dist());
    double offsetY = axis.dist() * Math.abs(dirVector.getY()) / (2 * dirVector.dist());

    String points = getArrowSide(posSlope(axisBegin, axisEnd), offsetX, offsetY, true, axisBegin);
    points += getArrowSide(posSlope(axisBegin, axisEnd), offsetX, offsetY, false, axisEnd);
    arrowElement.setAttribute(SvgConstants.POINTS, points);
    brush.addGroup(arrowGroup(arrow), arrowElement);
  }

  private void dot(SvgBrush brush, ArrowDrawProp arrow, String id) {
    Element arrowElement = brush.getOrCreateChildElementById(ELLIPSE_ELE + id, ELLIPSE_ELE);

    setBasicProp(arrowElement);

    FlatPoint axisBegin = arrow.getAxisBegin();
    FlatPoint axisEnd = arrow.getAxisEnd();

    FlatPoint axis = Vectors.sub(axisBegin, axisEnd);
    double radius = axis.dist() / 2;

    double x = (axisBegin.getX() + axisEnd.getX()) / 2;
    double y = (axisBegin.getY() + axisEnd.getY()) / 2;
    arrowElement.setAttribute(CX, String.valueOf(x));
    arrowElement.setAttribute(CY, String.valueOf(y));
    arrowElement.setAttribute(RX, String.valueOf(radius));
    arrowElement.setAttribute(RY, String.valueOf(radius));
    brush.addGroup(arrowGroup(arrow), arrowElement);
  }

  private void vee(SvgBrush brush, ArrowDrawProp arrow, String id) {
    Element arrowElement = brush.getOrCreateChildElementById(POLYGON_ELE + id, POLYGON_ELE);

    setBasicProp(arrowElement);

    FlatPoint axisBegin = arrow.getAxisBegin();
    FlatPoint axisEnd = arrow.getAxisEnd();

    FlatPoint axis = Vectors.sub(axisBegin, axisEnd);
    FlatPoint veeEndPoint = Vectors.add(axisBegin, axis);
    axis.setX(axis.getX() * 2);
    axis.setY(axis.getY() * 2);
    FlatPoint dirVector = new FlatPoint(-axis.getY(), axis.getX());

    double offsetX = axis.dist() * Math.abs(dirVector.getX()) / (3 * dirVector.dist());
    double offsetY = axis.dist() * Math.abs(dirVector.getY()) / (3 * dirVector.dist());

    FlatPointPair pointPair = getArrowSidePoint(posSlope(axisBegin, axisEnd), offsetX, offsetY,
                                                true, veeEndPoint);

    String points = getPathPointStr(pointPair.p1) + getPathPointStr(axisBegin)
        + getPathPointStr(pointPair.p2) + getPathPointStr(axisEnd)
        + getPathPointStr(pointPair.p1, false);
    arrowElement.setAttribute(POINTS, points);
    brush.addGroup(arrowGroup(arrow), arrowElement);
  }

  private void curve(SvgBrush brush, ArrowDrawProp arrow, String id) {
    Element arrowEle1 = brush.getOrCreateChildElementById(PATH_ELE + id + "0", PATH_ELE);
    Element arrowEle2 = brush.getOrCreateChildElementById(PATH_ELE + id + "1", PATH_ELE);
    Element axisEle = brush.getOrCreateChildElementById(PATH_ELE + id + "2", PATH_ELE);

    setBasicProp(arrowEle1, false);
    setBasicProp(arrowEle2, false);
    setBasicProp(axisEle, false);

    FlatPoint axisBegin = arrow.getAxisBegin();
    FlatPoint axisEnd = arrow.getAxisEnd();

    FlatPoint axis = Vectors.sub(axisBegin, axisEnd);
    FlatPoint dirVector = new FlatPoint(-axis.getY(), axis.getX());

    double offsetX = axis.dist() * Math.abs(dirVector.getX()) / (2 * dirVector.dist());
    double offsetY = axis.dist() * Math.abs(dirVector.getY()) / (2 * dirVector.dist());

    FlatPointPair pair1 = getArrowSidePoint(posSlope(axisBegin, axisEnd), offsetX,
                                            offsetY, true, axisBegin);
    FlatPointPair pair2 = getArrowSidePoint(posSlope(axisBegin, axisEnd), offsetX,
                                            offsetY, true, axisEnd);

    String points = PATH_START_M + getPathPointStr(pair1.p1, false) + CURVE_PATH_MARK
        + getPathPointStr(pair1.p1) + getPathPointStr(pair2.p1)
        + getPathPointStr(axisEnd, false);
    arrowEle1.setAttribute(D, points);

    points = PATH_START_M + getPathPointStr(pair1.p2, false) + CURVE_PATH_MARK
        + getPathPointStr(pair1.p2) + getPathPointStr(pair2.p2)
        + getPathPointStr(axisEnd, false);
    arrowEle2.setAttribute(D, points);

    points = PATH_START_M + getPathPointStr(axisBegin) + getPathPointStr(axisEnd, false);
    axisEle.setAttribute(D, points);
    brush.addGroup(arrowGroup(arrow), axisEle, arrowEle1, arrowEle2);
  }

  private void setBasicProp(Element arrowElement) {
    setBasicProp(arrowElement, true);
  }

  private void setBasicProp(Element arrowElement, boolean needFill) {
    if (needFill) {
      arrowElement.setAttribute(SvgConstants.FILL, Color.BLACK.value());
    } else {
      arrowElement.setAttribute(SvgConstants.FILL, NONE);
    }
    arrowElement.setAttribute(SvgConstants.STROKE, Color.BLACK.value());
  }

  private String getArrowSide(boolean posSlope, double offsetX, double offsetY,
                              boolean pos, FlatPoint point) {
    FlatPointPair pointPair = getArrowSidePoint(posSlope, offsetX, offsetY, pos, point);
    return pointPair.p1.getX() + SvgConstants.COMMA + pointPair.p1.getY() + SvgConstants.SPACE
        + pointPair.p2.getX() + SvgConstants.COMMA + pointPair.p2.getY() + SvgConstants.SPACE;
  }

  private FlatPointPair getArrowSidePoint(boolean posSlope, double offsetX, double offsetY,
                                          boolean pos, FlatPoint point) {
    if (posSlope) {
      if (pos) {
        return new FlatPointPair(
            new FlatPoint(point.getX() - offsetX, point.getY() + offsetY),
            new FlatPoint(point.getX() + offsetX, point.getY() - offsetY)
        );
      }

      return new FlatPointPair(
          new FlatPoint(point.getX() + offsetX, point.getY() - offsetY),
          new FlatPoint(point.getX() - offsetX, point.getY() + offsetY)
      );
    }

    if (pos) {
      return new FlatPointPair(
          new FlatPoint(point.getX() - offsetX, point.getY() - offsetY),
          new FlatPoint(point.getX() + offsetX, point.getY() + offsetY)
      );
    }

    return new FlatPointPair(
        new FlatPoint(point.getX() + offsetX, point.getY() + offsetY),
        new FlatPoint(point.getX() - offsetX, point.getY() - offsetY)
    );
  }

  private boolean posSlope(FlatPoint start, FlatPoint end) {
    return start.getY() - end.getY() < 0 == start.getX() - end.getX() < 0;
  }

  private String arrowGroup(ArrowDrawProp arrowDrawProp) {
    return arrowDrawProp.isHead() ? HEAD_ARROW_GROUP_KEY : TAIL_ARROW_GROUP_KEY;
  }

  private static class FlatPointPair {

    private final FlatPoint p1;
    private final FlatPoint p2;

    private FlatPointPair(FlatPoint p1, FlatPoint p2) {
      this.p1 = p1;
      this.p2 = p2;
    }
  }
}
