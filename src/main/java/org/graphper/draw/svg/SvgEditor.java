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

import static org.graphper.util.FontUtils.DEFAULT_FONT;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.Color;
import org.graphper.api.ext.Box;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * Svg editor for graph element.
 *
 * @author Jamison Jiang
 */
public class SvgEditor implements SvgConstants {

  private static final int MAX_ROUNDED = 30;

  /**
   * Set each line text to svg.
   *
   * @param text              text element
   * @param fontSize          font size
   * @param textLineAttribute per text line attribute
   * @throws NullPointerException null text element or null text line attribute
   */
  public static void setText(Element text, double fontSize, TextLineAttribute textLineAttribute) {
    Asserts.nullArgument(text, "text element");
    Asserts.nullArgument(textLineAttribute, "text line attribute");
    text.setAttribute(X, String.valueOf(textLineAttribute.getX()));
    text.setAttribute(Y, String.valueOf(textLineAttribute.getY()));
    text.setAttribute(TEXT_ANCHOR, MIDDLE);
    text.setAttribute(FONT_SIZE, String.valueOf(fontSize));

    TextAttribute attribute = textLineAttribute.getTextAttribute();
    if (attribute.fontColor != null) {
      text.setAttribute(FILL, attribute.fontColor.value());
    }

    if (attribute.fontName != null) {
      text.setAttribute(FONT_FAMILY, attribute.fontName);
    }
  }

  /**
   * Set the coordinates of the label element.
   *
   * @param textAttribute label attribute
   * @throws NullPointerException null attribute
   */
  public static void text(TextAttribute textAttribute) {
    Asserts.nullArgument(textAttribute, "textAttribute");

    if (textAttribute.lineAttributeConsumer == null) {
      return;
    }

    double halfHeight = textAttribute.fontsize / 2;
    String[] lines = textAttribute.label.split("\n");
    int midIndex = (lines.length - 1) / 2;
    boolean oddLen = (lines.length & 1) == 1;
    double xc = textAttribute.centerPoint.getX() - halfHeight / 8;
    double yc;
    double t = halfHeight / 3;

    for (int i = 0; i < lines.length; i++) {
      yc = textAttribute.centerPoint.getY() - t;

      yc -= (midIndex - i) * textAttribute.fontsize;
      if (oddLen) {
        yc += halfHeight;
      }

      textAttribute.lineAttributeConsumer.accept(
          new TextLineAttribute(xc, yc, i, lines[i], textAttribute));
    }
  }

  /**
   * Draw polygon shape and common handle ROUNDED property for {@link org.graphper.api.Node} and
   * {@link org.graphper.api.Cluster}.
   *
   * @param drawProp draw container needs drawing
   * @param brush    svg brush need to generate element
   * @param points   polygon points
   * @throws NullPointerException     null container or null brush
   * @throws IllegalArgumentException wrong size radius or wrong close path points or wrong
   *                                  container type
   */
  public static Element polygonShape(ContainerDrawProp drawProp, SvgBrush brush, double... points) {
    Asserts.nullArgument(brush, "brush");
    Asserts.nullArgument(drawProp, "drawProp");

    Element shapeEle;
    boolean isRound = drawProp.containsRounded();
    if (drawProp.isNodeProp()) {
      NodeDrawProp nodeDrawProp = ((NodeDrawProp) drawProp);
      if (isRound) {
        shapeEle = brush.getShapeElement(nodeDrawProp, PATH_ELE);
      } else {
        shapeEle = brush.getShapeElement(nodeDrawProp, POLYGON_ELE);
      }
    } else if (drawProp.isClusterProp()) {
      ClusterDrawProp clusterDrawProp = ((ClusterDrawProp) drawProp);
      if (isRound) {
        shapeEle = brush.getShapeElement(clusterDrawProp, PATH_ELE);
      } else {
        shapeEle = brush.getShapeElement(clusterDrawProp, POLYGON_ELE);
      }
    } else {
      throw new IllegalArgumentException("Unsupport container draw properties");
    }

    polygonShape(() -> shapeEle, isRound, points);
    return shapeEle;
  }

  /**
   * Draw polygon shape and common handle ROUNDED property.
   *
   * @param elementSupplier shape element supplier
   * @param isRound         shape need rounded
   * @param points          polygon points
   * @throws NullPointerException     null element supplier or null shape element
   * @throws IllegalArgumentException wrong size radius or wrong close path points
   */
  public static void polygonShape(Supplier<Element> elementSupplier, boolean isRound,
                                  double... points) {
    Asserts.nullArgument(elementSupplier, "Element supplier");
    Element element = elementSupplier.get();
    Asserts.nullArgument(element, "Shape element");

    if (isRound) {
      element.setAttribute(D, roundedShape(points));
    } else {
      element.setAttribute(POINTS, generatePolylinePoints(points));
    }
  }

  /**
   * The point is output in a format similar to "10,10", which is mainly used for the point path of
   * svg.
   *
   * @param point points to convert
   * @return coordinate character
   * @throws NullPointerException null point
   */
  public static String getPathPointStr(FlatPoint point) {
    return getPathPointStr(point, true);
  }

  /**
   * The point is output in a format similar to "10,10" or "10,10 ", which is mainly used for the
   * point path of svg.
   *
   * @param point     points to convert
   * @param needSpace if ture, add space as suffix
   * @return coordinate character
   * @throws NullPointerException null point
   */
  public static String getPathPointStr(FlatPoint point, boolean needSpace) {
    Asserts.nullArgument(point, "point");
    String v = point.getX() + SvgConstants.COMMA + point.getY();
    return needSpace ? v + SvgConstants.SPACE : v;
  }

  /**
   * Returns the {@link #D} attributes that make up the svg {@link #PATH_ELE}, based on a list of
   * points and whether they are curves.
   *
   * @param start   start point
   * @param points  line points
   * @param isCurve if true, is a curve line
   * @return line {@link #D} attribute value of the {@link #PATH_ELE} element.
   */
  public static String pointsToSvgLine(FlatPoint start, List<FlatPoint> points, boolean isCurve) {
    if (CollectionUtils.isEmpty(points)) {
      return null;
    }

    start = start == null ? points.get(0) : start;
    StringBuilder path = new StringBuilder(PATH_START_M).append(start.getX()).append(COMMA)
        .append(start.getY());

    if (isCurve) {
      path.append(CURVE_PATH_MARK);
    } else {
      path.append(SPACE);
    }

    for (int i = 1; i < points.size(); i++) {
      FlatPoint flatPoint = points.get(i);
      path.append(flatPoint.getX()).append(COMMA).append(flatPoint.getY()).append(SPACE);
    }
    return path.toString();
  }

  /**
   * Returns the {@link #D} attributes that make up the svg {@link #PATH_ELE}.
   *
   * @param isCurve   the path whether is curve
   * @param positions the polyline border points
   * @return path {@link #D} attribute value of the {@link #POLYGON_ELE}
   * @throws NullPointerException     positions is null
   * @throws IllegalArgumentException positions is empty or positions size is not even
   */
  public static String pointsToSvgPath(boolean isCurve, double... positions) {
    Asserts.nullArgument(positions, "positions");
    Asserts.illegalArgument(positions.length == 0 || positions.length % 2 != 0,
                            "Wrong positions length, can not be empty and must be even");

    StringBuilder sb = new StringBuilder(PATH_START_M);
    for (int i = 0; i < positions.length; i += 2) {
      double horPos = positions[i];
      double verPos = positions[i + 1];
      if (i == 0 && isCurve) {
        sb.append(horPos).append(SvgConstants.COMMA).append(verPos)
            .append(SvgConstants.CURVE_PATH_MARK);
      } else {
        sb.append(horPos).append(SvgConstants.COMMA).append(verPos).append(SvgConstants.SPACE);
      }
    }
    return sb.toString();
  }

  /**
   * Return a box with rounded corners according to the size of the box and the set maximum rounded
   * corner length, using the path description.
   *
   * @param maxRoundedLen maximum rounded length
   * @param box           box
   * @return path {@link #D} attribute value of the {@link #POLYGON_ELE}
   * @throws NullPointerException     null box
   * @throws IllegalArgumentException wrong maximum rounded length
   */
  public static String roundedBox(int maxRoundedLen, Box box) {
    Asserts.nullArgument(box, "box");
    Asserts.illegalArgument(maxRoundedLen <= 0, "Max rounded length less than 0");

    double leftBorder = box.getLeftBorder();
    double rightBorder = box.getRightBorder();
    double upBorder = box.getUpBorder();
    double downBorder = box.getDownBorder();
    int cornerLen = Math.min(maxRoundedLen, (int) box.getWidth() / 2);
    cornerLen = Math.min(cornerLen, (int) box.getHeight() / 2);
    return pointsToSvgPath(true, leftBorder, upBorder + cornerLen, leftBorder, upBorder + cornerLen,
                           leftBorder, downBorder - cornerLen, leftBorder, downBorder - cornerLen,

                           leftBorder, downBorder, leftBorder, downBorder, leftBorder + cornerLen,
                           downBorder,

                           leftBorder + cornerLen, downBorder, rightBorder - cornerLen, downBorder,
                           rightBorder - cornerLen, downBorder,

                           rightBorder, downBorder, rightBorder, downBorder, rightBorder,
                           downBorder - cornerLen,

                           rightBorder, downBorder - cornerLen, rightBorder, upBorder + cornerLen,
                           rightBorder, upBorder + cornerLen,

                           rightBorder, upBorder, rightBorder, upBorder, rightBorder - cornerLen,
                           upBorder,

                           rightBorder - cornerLen, upBorder, leftBorder + cornerLen, upBorder,
                           leftBorder + cornerLen, upBorder,

                           leftBorder, upBorder, leftBorder, upBorder, leftBorder,
                           upBorder + cornerLen);
  }

  /**
   * Returns a rounded shape points according to the close path points.
   *
   * @return path {@link #D} attribute value of the {@link #POLYGON_ELE}
   * @throws IllegalArgumentException wrong size radius or wrong close path points
   */
  public static String roundedShape(double... path) {
    return roundedShape(MAX_ROUNDED, path);
  }

  /**
   * Returns a rounded shape points according to the close path points.
   *
   * @param radius rounded radius length
   * @param path   close path points
   * @return path {@link #D} attribute value of the {@link #POLYGON_ELE}
   * @throws IllegalArgumentException wrong size radius or wrong close path points
   */
  public static String roundedShape(int radius, double... path) {
    Asserts.illegalArgument(radius < 0, "radius cannnot less than 0");
    Asserts.illegalArgument(path == null || path.length < 4, "shape points not enough");
    Asserts.illegalArgument(path.length % 2 != 0, "shape points number should be even");
    Asserts.illegalArgument(path[0] != path[path.length - 2] || path[1] != path[path.length - 1],
                            "shape not enclose");

    int j = 0;
    FlatPoint current = new FlatPoint(0, 0);
    FlatPoint next = new FlatPoint(0, 0);
    double[] roundPath = new double[12 * (path.length / 2 - 1) + 2];

    for (int i = 0; i < path.length - 3; i += 2) {
      current.setX(path[i]);
      current.setY(path[i + 1]);
      next.setX(path[i + 2]);
      next.setY(path[i + 3]);

      FlatPoint dirVector = Vectors.sub(next, current);
      Asserts.illegalArgument(dirVector.similarX(0, 0.001D) && dirVector.similarY(0, 0.001D),
                              "Adjacent pair points of shape too close");
      double ratio = radius / dirVector.dist();
      if (ratio > 0.5) {
        ratio = 0.5;
      }
      FlatPoint point = Vectors.multiple(dirVector, ratio);
      point = Vectors.add(current, point);
      roundPath[j++] = point.getX();
      roundPath[j++] = point.getY();
      roundPath[j++] = point.getX();
      roundPath[j++] = point.getY();

      point = Vectors.multiple(dirVector, 1 - ratio);
      point = Vectors.add(current, point);
      roundPath[j++] = point.getX();
      roundPath[j++] = point.getY();
      roundPath[j++] = point.getX();
      roundPath[j++] = point.getY();

      roundPath[j++] = path[i + 2];
      roundPath[j++] = path[i + 3];
      roundPath[j++] = path[i + 2];
      roundPath[j++] = path[i + 3];
    }

    roundPath[roundPath.length - 2] = roundPath[0];
    roundPath[roundPath.length - 1] = roundPath[1];

    return pointsToSvgPath(true, roundPath);
  }

  /**
   * Returns the {@link #POINTS} attributes that make up the svg {@link #POLYGON_ELE} to generate a
   * {@link Box} border path.
   *
   * @param box the box that needs to be drawn
   * @return box {@link #POINTS} attribute value of the {@link #POLYGON_ELE}
   * @throws NullPointerException null box
   */
  public static String generateBox(Box box) {
    Asserts.nullArgument(box, "box");
    return generatePolylinePoints(box.getLeftBorder(), box.getUpBorder(), box.getRightBorder(),
                                  box.getUpBorder(), box.getRightBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getDownBorder(), box.getLeftBorder(),
                                  box.getUpBorder());
  }

  /**
   * Returns the {@link #POINTS} attributes that make up the svg {@link #POLYGON_ELE} to generate a
   * polyline.
   *
   * @param positions the polyline border points
   * @return polyline {@link #POINTS} attribute value of the {@link #POLYGON_ELE}
   * @throws NullPointerException     positions is null
   * @throws IllegalArgumentException positions is empty
   */
  public static String generatePolylinePoints(List<FlatPoint> positions) {
    Asserts.nullArgument(positions, "positions");
    Asserts.illegalArgument(CollectionUtils.isEmpty(positions),
                            "Wrong positions length, can not be empty and must be even");

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < positions.size(); i++) {
      FlatPoint p = positions.get(i);
      sb.append(p.getX()).append(SvgConstants.COMMA).append(p.getY()).append(SvgConstants.SPACE);
    }
    return sb.toString();
  }

  /**
   * Returns the {@link #POINTS} attributes that make up the svg {@link #POLYGON_ELE} to generate a
   * polyline.
   *
   * @param positions the polyline border points
   * @return polyline {@link #POINTS} attribute value of the {@link #POLYGON_ELE}
   * @throws NullPointerException     positions is null
   * @throws IllegalArgumentException positions is empty or positions size is not even
   */
  public static String generatePolylinePoints(double... positions) {
    Asserts.nullArgument(positions, "positions");
    Asserts.illegalArgument(positions.length == 0 || positions.length % 2 != 0,
                            "Wrong positions length, can not be empty and must be even");

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < positions.length; i += 2) {
      double horPos = positions[i];
      double verPos = positions[i + 1];
      sb.append(horPos).append(SvgConstants.COMMA).append(verPos).append(SvgConstants.SPACE);
    }
    return sb.toString();
  }

  public static double strokeWidth(double penwidth, boolean isBold) {
    return isBold ? Math.max(penwidth, 2) : penwidth;
  }

  // -------------------------------------------- subclass --------------------------------------------

  public static class TextAttribute {

    private final FlatPoint centerPoint;

    private final double fontsize;

    private final String label;

    private final Color fontColor;

    private final String fontName;

    private final Consumer<TextLineAttribute> lineAttributeConsumer;

    public TextAttribute(FlatPoint centerPoint, double fontsize, String label, Color fontColor,
                         Consumer<TextLineAttribute> lineAttributeConsumer) {
      this(centerPoint, fontsize, label, fontColor, DEFAULT_FONT, lineAttributeConsumer);
    }

    public TextAttribute(FlatPoint centerPoint, double fontsize, String label, Color fontColor,
                         String fontName, Consumer<TextLineAttribute> lineAttributeConsumer) {
      Asserts.nullArgument(centerPoint, "centerPoint");
      Asserts.illegalArgument(StringUtils.isEmpty(label), "label can not be empty");
      this.centerPoint = centerPoint;
      this.fontsize = fontsize;
      this.label = label;
      this.fontColor = fontColor;
      this.fontName = fontName;
      this.lineAttributeConsumer = lineAttributeConsumer;
    }
  }

  public static class TextLineAttribute {

    private final double x;

    private final double y;

    private final int lineNo;

    private final String line;

    private final TextAttribute textAttribute;

    public TextLineAttribute(double x, double y, int lineNo, String line,
                             TextAttribute textAttribute) {
      this.x = x;
      this.y = y;
      this.lineNo = lineNo;
      this.line = line;
      this.textAttribute = textAttribute;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public int getLineNo() {
      return lineNo;
    }

    public String getLine() {
      return line;
    }

    public TextAttribute getTextAttribute() {
      return textAttribute;
    }
  }
}
