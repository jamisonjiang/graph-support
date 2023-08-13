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

package org.graphper.draw.common;

import static org.graphper.util.FontUtils.DEFAULT_FONT;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Objects;
import org.apache_gs.commons.lang3.StringUtils;
import org.apache_gs.commons.text.StringEscapeUtils;
import org.graphper.api.FileType;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.svg.Document;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.util.FontUtils;
import org.graphper.util.ClassUtils;

public class DefaultImgConverter implements SvgConverter, SvgConstants {

  private static Class<?> KEY;

  private static Class<?> FONT;

  private static Class<?> COLOR;

  private static Class<?> SHAPE;

  private static Class<?> LINE2D;

  private static Class<?> PATH2D;

  private static Class<?> POINT2D;

  private static Class<?> CURVE2D;

  private static Class<?> ELLIPSE;

  private static Class<?> IMAGEIO;

  private static Class<?> STROKE_C;

  private static Class<?> RENDER_IMG;

  private static Class<?> BUFFERED_IMG;

  private static Class<?> BASIC_STROKE;

  private static Class<?> AFFINE_TRANSFORM;

  static {
    try {
      KEY = Class.forName("java.awt.RenderingHints$Key");
      FONT = Class.forName("java.awt.Font");
      COLOR = Class.forName("java.awt.Color");
      SHAPE = Class.forName("java.awt.Shape");
      LINE2D = Class.forName("java.awt.geom.Line2D$Double");
      PATH2D = Class.forName("java.awt.geom.Path2D$Double");
      POINT2D = Class.forName("java.awt.geom.Point2D$Double");
      CURVE2D = Class.forName("java.awt.geom.CubicCurve2D$Double");
      ELLIPSE = Class.forName("java.awt.geom.Ellipse2D$Double");
      IMAGEIO = Class.forName("javax.imageio.ImageIO");
      STROKE_C = Class.forName("java.awt.Stroke");
      BASIC_STROKE = Class.forName("java.awt.BasicStroke");
      RENDER_IMG = Class.forName("java.awt.image.RenderedImage");
      BUFFERED_IMG = Class.forName("java.awt.image.BufferedImage");
      AFFINE_TRANSFORM = Class.forName("java.awt.geom.AffineTransform");
    } catch (Exception e) {
      // ignore
    }
  }

  @Override
  public int order() {
    if (Boolean.TRUE.toString().equals(System.getProperty("use.local.img.converter"))) {
      return Integer.MIN_VALUE;
    }
    return 1;
  }

  @Override
  public boolean envSupport() {
    return KEY != null && AFFINE_TRANSFORM != null;
  }

  @Override
  public FileType[] supportFileTypes() {
    return new FileType[]{FileType.PNG, FileType.JPG, FileType.JPEG, FileType.GIF};
  }

  @Override
  public DefaultGraphResource convert(Document document, DrawGraph drawGraph, FileType fileType)
      throws FailInitResourceException {
    if (document == null || drawGraph == null || fileType == null) {
      throw new FailInitResourceException("Lack parameters to convert image");
    }

    ImgContext imgContext = new ImgContext();
    document.accessEles(((ele, children) -> {
      try {
        if (Objects.equals(ele.tagName(), SVG_ELE)) {
          initImage(drawGraph, fileType, imgContext, ele);
          return;
        }
        Object g2d = imgContext.g2d;
        if (g2d == null) {
          return;
        }

        if (Objects.equals(ele.tagName(), ELLIPSE_ELE)) {
          drawEllipse(ele, g2d);
          return;
        }
        if (Objects.equals(ele.tagName(), TEXT_ELE)) {
          drawString(ele, g2d);
          return;
        }

        if (Objects.equals(ele.tagName(), POLYGON_ELE)) {
          drawPolygon(ele, g2d);
          return;
        }

        if (Objects.equals(ele.tagName(), PATH_ELE)) {
          drawPath(ele, g2d);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }));

    if (imgContext.img == null) {
      return null;
    }

    try {
      ClassUtils.invoke(imgContext.g2d, "dispose");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ClassUtils.invokeStatic(IMAGEIO, "write",
                              new Class[]{RENDER_IMG, String.class, OutputStream.class},
                              imgContext.img, fileType.getType(), baos);
      String label = drawGraph.getGraphviz().graphAttrs().getLabel();
      return new DefaultGraphResource(label, fileType.getType(), baos);
    } catch (Exception e) {
      throw new FailInitResourceException(e);
    }
  }

  private void initImage(DrawGraph drawGraph, FileType fileType, ImgContext imgContext,
                         Element ele) throws Exception {
    int h = toInt(ele.getAttribute(HEIGHT));
    int w = toInt(ele.getAttribute(WIDTH));
    FlatPoint scale = drawGraph.getGraphviz().graphAttrs().getScale();
    Object transform = ClassUtils.newObject(AFFINE_TRANSFORM);
    if (scale != null) {
      ClassUtils.invoke(transform, "scale", scale.getX() * 1.3333, scale.getY() * 1.3333);
    }

    w = (int) (w * 1.3333);
    h = (int) (h * 1.3333);
    if (fileType == FileType.PNG) {
      imgContext.setImg(ClassUtils.newObject(BUFFERED_IMG, w, h,
                                             ClassUtils.getStaticField(BUFFERED_IMG,
                                                                       "TYPE_INT_ARGB")));
    } else {
      imgContext.setImg(ClassUtils.newObject(BUFFERED_IMG, w, h,
                                             ClassUtils.getStaticField(BUFFERED_IMG,
                                                                       "TYPE_INT_RGB")));

    }
    Object g2d = imgContext.g2d;
    ClassUtils.invoke(g2d, "setTransform", transform);
    ClassUtils.invoke(g2d, "setBackground", ClassUtils.getStaticField(COLOR, "WHITE"));
    ClassUtils.invoke(g2d, "clearRect", 0, 0, w, h);
  }

  private void drawEllipse(Element ele, Object g2d) throws Exception {
    double x = toDouble(ele.getAttribute(CX));
    double y = toDouble(ele.getAttribute(CY));
    double w = toDouble(ele.getAttribute(RX));
    double h = toDouble(ele.getAttribute(RY));

    Object ellipse = ClassUtils.newObject(ELLIPSE, x - w, y - h, 2 * w, 2 * h);
    setShapeCommonAttr(ele, g2d, ellipse);
  }

  private void drawString(Element ele, Object g2d) throws Exception {
    String text = ele.textContext();
    if (StringUtils.isNotEmpty(text)) {
      text = StringEscapeUtils.unescapeXml(text);
    }
    int fontSize = toInt(ele.getAttribute(FONT_SIZE));
    double x = toDouble(ele.getAttribute(X));
    double y = toDouble(ele.getAttribute(Y));
    String fontName = ele.getAttribute(FONT_FAMILY);
    FlatPoint size = FontUtils.measure(text, fontName, fontSize, 0);
    Object color = toColor(ele.getAttribute(FILL));
    if (color == null) {
      color = ClassUtils.getStaticField(COLOR, "BLACK");
    }
    ClassUtils.invoke(g2d, "setColor", color);

    fontName = fontName == null ? DEFAULT_FONT : fontName;
    ClassUtils.invoke(g2d, "setFont",
                      ClassUtils.newObject(FONT, fontName,
                                           ClassUtils.getStaticField(FONT, "PLAIN"),
                                           fontSize));
    ClassUtils.invoke(g2d, "drawString", text, (float) (x - (size.getWidth() / 2)), (float) y);
  }

  private void drawPolygon(Element ele, Object g2d) throws Exception {
    Object polygon = ClassUtils.newObject(PATH2D);
    Object[] path = toPoints(ele.getAttribute(POINTS));
    if (path == null) {
      return;
    }
    for (int i = 0; i < path.length; i++) {
      if (i == 0) {
        ClassUtils.invoke(polygon, "moveTo", getPointX(path[i]), getPointY(path[i]));
      } else {
        ClassUtils.invoke(polygon, "lineTo", getPointX(path[i]), getPointY(path[i]));
      }
    }
    ClassUtils.invoke(polygon, "closePath");
    setShapeCommonAttr(ele, g2d, polygon);
  }

  private void drawPath(Element ele, Object g2d) throws Exception {
    String points = ele.getAttribute(D);
    Object[] path = toPoints(points);
    if (path == null) {
      return;
    }

    Object path2D = ClassUtils.newObject(PATH2D);
    if (points.contains(CURVE_PATH_MARK)) {
      for (int i = 3; i < path.length; i += 3) {
        Object p1 = path[i - 3];
        Object p2 = path[i - 2];
        Object p3 = path[i - 1];
        Object p4 = path[i];
        Object curve = ClassUtils.newObject(CURVE2D, getPointX(p1), getPointY(p1),
                                            getPointX(p2), getPointY(p2),
                                            getPointX(p3), getPointY(p3),
                                            getPointX(p4), getPointY(p4));
        ClassUtils.invoke(path2D, "append", new Class[]{SHAPE, boolean.class}, curve, true);
      }

    } else {
      for (int i = 1; i < path.length; i++) {
        Object start = path[i - 1];
        Object end = path[i];
        Object line = ClassUtils.newObject(LINE2D, getPointX(start), getPointY(start),
                                           getPointX(end), getPointY(end));
        ClassUtils.invoke(path2D, "append", new Class[]{SHAPE, boolean.class}, line, true);
      }
    }

    setShapeCommonAttr(ele, g2d, path2D);
  }

  private void setShapeCommonAttr(Element ele, Object g2d, Object shape) throws Exception {
    Object color = toColor(ele.getAttribute(FILL));
    Object borderColor = toColor(ele.getAttribute(STROKE));
    if (color == null && borderColor == null) {
      return;
    }

    String sw = ele.getAttribute(STROKE_WIDTH);
    double strokeWidth = sw != null ? toDouble(sw) : 1D;
    if (strokeWidth <= 0 && color == null) {
      return;
    }

    if (color != null) {
      ClassUtils.invoke(g2d, "setColor", color);
      ClassUtils.invokeOne(g2d, "fill", SHAPE, shape);
    }

    float[] dashPattern = toFloatPair(ele.getAttribute(STROKE_DASHARRAY));
    Object stroke;
    if (dashPattern != null) {
      stroke = ClassUtils.newObject(BASIC_STROKE,
                                    new Class[]{float.class, int.class, int.class, float.class,
                                        float[].class, float.class},
                                    (float) strokeWidth,
                                    ClassUtils.getStaticField(BASIC_STROKE, "CAP_BUTT"),
                                    ClassUtils.getStaticField(BASIC_STROKE, "JOIN_ROUND"),
                                    5.0f, dashPattern, 0);
    } else {
      stroke = ClassUtils.newObject(BASIC_STROKE, (float) strokeWidth);
    }

    ClassUtils.invokeOne(g2d, "setStroke", STROKE_C, stroke);
    ClassUtils.invokeOne(g2d, "setColor", COLOR, borderColor);
    ClassUtils.invokeOne(g2d, "draw", SHAPE, shape);
  }

  private Object getPointX(Object point) throws Exception {
    return ClassUtils.invoke(point, "getX");
  }

  private Object getPointY(Object point) throws Exception {
    return ClassUtils.invoke(point, "getY");
  }

  private int toInt(String doubleStr) {
    if (StringUtils.isEmpty(doubleStr)) {
      return 0;
    }
    return (int) Double.parseDouble(doubleStr.replaceAll(PT, StringUtils.EMPTY));
  }

  private double toDouble(String doubleStr) {
    if (StringUtils.isEmpty(doubleStr)) {
      return 0;
    }
    return Double.parseDouble(doubleStr.replaceAll(PT, StringUtils.EMPTY));
  }

  private float toFloat(String doubleStr) {
    if (StringUtils.isEmpty(doubleStr)) {
      return 0;
    }
    return Float.parseFloat(doubleStr.replaceAll(PT, StringUtils.EMPTY));
  }

  private float[] toFloatPair(String pair) {
    if (StringUtils.isEmpty(pair)) {
      return null;
    }
    String[] p = pair.split(COMMA);
    float[] fp = new float[2];
    fp[0] = toFloat(p[0]);
    fp[1] = toFloat(p[1]);
    return fp;
  }

  private Object[] toPoints(String points) throws Exception {
    if (StringUtils.isEmpty(points)) {
      return null;
    }
    points = points.replace(PATH_START_M, StringUtils.EMPTY);
    points = points.replace(CURVE_PATH_MARK, SPACE);
    String[] pointPairs = points.split(SPACE);
    Object[] point2d = new Object[pointPairs.length];
    for (int i = 0; i < pointPairs.length; i++) {
      String[] point = pointPairs[i].split(COMMA);
      point2d[i] = ClassUtils.newObject(POINT2D, toDouble(point[0]), toDouble(point[1]));
    }
    return point2d;
  }

  public Object toColor(String hexColorCode) throws Exception {
    if (hexColorCode == null || NONE.equals(hexColorCode)) {
      return null;
    }
    int rgb = Integer.parseInt(hexColorCode.substring(1), 16);
    return ClassUtils.newObject(COLOR, rgb);
  }

  private static class ImgContext {

    private Object img;
    private Object g2d;

    public void setImg(Object img) throws Exception {
      this.img = img;
      if (this.img != null) {
        g2d = ClassUtils.invoke(img, "createGraphics");
        ClassUtils.invoke(g2d, "setRenderingHint",
                          new Class[]{KEY, Object.class},
                          ClassUtils.getStaticField(Class.forName("java.awt.RenderingHints"),
                                                    "KEY_ANTIALIASING"),
                          ClassUtils.getStaticField(Class.forName("java.awt.RenderingHints"),
                                                    "VALUE_ANTIALIAS_ON"));
      }
    }
  }
}
