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
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.Color;
import org.graphper.api.ext.Box;

/**
 * Svg editor for graph element.
 *
 * @author Jamison Jiang
 */
public class SvgEditor implements SvgConstants {

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
          new TextLineAttribute(xc, yc, i, lines[i], textAttribute)
      );
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
    StringBuilder path = new StringBuilder(PATH_START_M)
        .append(start.getX())
        .append(COMMA)
        .append(start.getY());

    if (isCurve) {
      path.append(CURVE_PATH_MARK);
    } else {
      path.append(SPACE);
    }

    for (int i = 1; i < points.size(); i++) {
      FlatPoint flatPoint = points.get(i);
      path.append(flatPoint.getX())
          .append(COMMA)
          .append(flatPoint.getY())
          .append(SPACE);
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
   * @throws IllegalArgumentException wring maximum rounded length
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
    return pointsToSvgPath(true, leftBorder, upBorder + cornerLen,
                           leftBorder, upBorder + cornerLen,
                           leftBorder, downBorder - cornerLen,
                           leftBorder, downBorder - cornerLen,

                           leftBorder, downBorder,
                           leftBorder, downBorder,
                           leftBorder + cornerLen, downBorder,

                           leftBorder + cornerLen, downBorder,
                           rightBorder - cornerLen, downBorder,
                           rightBorder - cornerLen, downBorder,

                           rightBorder, downBorder,
                           rightBorder, downBorder,
                           rightBorder, downBorder - cornerLen,

                           rightBorder, downBorder - cornerLen,
                           rightBorder, upBorder + cornerLen,
                           rightBorder, upBorder + cornerLen,

                           rightBorder, upBorder,
                           rightBorder, upBorder,
                           rightBorder - cornerLen, upBorder,

                           rightBorder - cornerLen, upBorder,
                           leftBorder + cornerLen, upBorder,
                           leftBorder + cornerLen, upBorder,

                           leftBorder, upBorder,
                           leftBorder, upBorder,
                           leftBorder, upBorder + cornerLen);
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
    return generatePolylinePoints(box.getLeftBorder(), box.getUpBorder(),
                                  box.getRightBorder(), box.getUpBorder(),
                                  box.getRightBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getUpBorder());
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

    public TextAttribute(FlatPoint centerPoint, double fontsize,
                         String label, Color fontColor, String fontName,
                         Consumer<TextLineAttribute> lineAttributeConsumer) {
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
