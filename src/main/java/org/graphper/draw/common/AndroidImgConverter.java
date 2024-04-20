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
import org.graphper.util.ClassUtils;
import org.graphper.util.FontUtils;

public class AndroidImgConverter implements SvgConverter, SvgConstants {

  private static Class<?> PATH;
  private static Class<?> COLOR;
  private static Class<?> STYLE;
  private static Class<?> PAINT;
  private static Class<?> RECTF;
  private static Class<?> CONFIG;
  private static Class<?> POINTF;
  private static Class<?> CANVAS;
  private static Class<?> MATRIX;
  private static Class<?> BIT_MAP;
  private static Class<?> TYPE_FACE;
  private static Class<?> PATH_EFFECT;
  private static Class<?> COMPRESS_FORMAT;
  private static Class<?> DASH_PATH_EFFECT;

  static {
    try {
      PATH = Class.forName("android.graphics.Path");
      COLOR = Class.forName("android.graphics.Color");
      STYLE = Class.forName("android.graphics.Paint$Style");
      PAINT = Class.forName("android.graphics.Paint");
      RECTF = Class.forName("android.graphics.RectF");
      CONFIG = Class.forName("android.graphics.Bitmap$Config");
      POINTF = Class.forName("android.graphics.PointF");
      CANVAS = Class.forName("android.graphics.Canvas");
      MATRIX = Class.forName("android.graphics.Matrix");
      BIT_MAP = Class.forName("android.graphics.Bitmap");
      TYPE_FACE = Class.forName("android.graphics.Typeface");
      PATH_EFFECT = Class.forName("android.graphics.PathEffect");
      COMPRESS_FORMAT = Class.forName("android.graphics.Bitmap$CompressFormat");
      DASH_PATH_EFFECT = Class.forName("android.graphics.DashPathEffect");
    } catch (Exception e) {
      // Ignore
    }
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public boolean envSupport() {
    return PATH != null && DASH_PATH_EFFECT != null;
  }

  @Override
  public FileType[] supportFileTypes() {
    return new FileType[]{FileType.PNG, FileType.JPEG};
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
          initImage(drawGraph, imgContext, ele);
          return;
        }
        Object canvas = imgContext.canvas;
        if (canvas == null) {
          return;
        }

        if (Objects.equals(ele.tagName(), ELLIPSE_ELE)) {
          drawEllipse(ele, canvas);
          return;
        }

        if (Objects.equals(ele.tagName(), TEXT_ELE)) {
          drawString(ele, canvas);
          return;
        }

        if (Objects.equals(ele.tagName(), POLYGON_ELE)) {
          drawPolygon(ele, canvas);
          return;
        }

        if (Objects.equals(ele.tagName(), PATH_ELE)) {
          drawPath(ele, canvas);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }));

    if (imgContext.img == null) {
      return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ClassUtils.invoke(imgContext.img, "compress",
                        new Class[]{COMPRESS_FORMAT, int.class, OutputStream.class},
                        Enum.valueOf((Class<? extends Enum>) COMPRESS_FORMAT,
                                     fileType.getType().toUpperCase()), 100, baos);
      String label = drawGraph.getGraphviz().graphAttrs().getLabel();
      return new DefaultGraphResource(label, fileType.getType(), baos);
    } catch (Exception e) {
      throw new FailInitResourceException(e);
    }
  }

  private void initImage(DrawGraph drawGraph, ImgContext imgContext, Element ele) throws Exception {
    int h = toInt(ele.getAttribute(HEIGHT));
    int w = toInt(ele.getAttribute(WIDTH));

    imgContext.img = ClassUtils.invokeStatic(BIT_MAP, "createBitmap",
                                             new Class[]{int.class, int.class, CONFIG}, w, h,
                                             Enum.valueOf((Class<? extends Enum>) CONFIG,
                                                          "ARGB_8888"));
    imgContext.canvas = ClassUtils.newObject(CANVAS, imgContext.img);
    ClassUtils.invoke(imgContext.canvas, "drawColor",
                      ClassUtils.getStaticField(COLOR, "WHITE"));

    FlatPoint scale = drawGraph.getGraphviz().graphAttrs().getScale();
    if (scale != null) {
      Object transform = ClassUtils.newObject(MATRIX);
      ClassUtils.invoke(transform, "setScale", (float) scale.getX(), (float) scale.getY());
      ClassUtils.invoke(imgContext.canvas, "setMatrix", transform);
    }
  }

  private void drawEllipse(Element ele, Object canvas) throws Exception {
    float x = toFloat(ele.getAttribute(CX));
    float y = toFloat(ele.getAttribute(CY));
    float rx = toFloat(ele.getAttribute(RX));
    float ry = toFloat(ele.getAttribute(RY));

    Object rectF = ClassUtils.newObject(RECTF, x - rx, y - ry, x + rx, y + ry);

    Object border = ClassUtils.newObject(PAINT);
    Object fill = ClassUtils.newObject(PAINT);
    if (setShapeCommonAttr(ele, border, true)) {
      ClassUtils.invoke(canvas, "drawOval", rectF, border);
    }
    if (setShapeCommonAttr(ele, fill, false)) {
      ClassUtils.invoke(canvas, "drawOval", rectF, fill);
    }
  }

  private void drawString(Element ele, Object canvas) throws Exception {
    String text = ele.textContext();
    if (StringUtils.isNotEmpty(text)) {
      text = StringEscapeUtils.unescapeXml(text);
    }
    int fontSize = toInt(ele.getAttribute(FONT_SIZE));
    double x = toDouble(ele.getAttribute(X));
    double y = toDouble(ele.getAttribute(Y));
    String fontName = ele.getAttribute(FONT_FAMILY);
    fontName = FontUtils.fontExists(fontName) ? fontName : DEFAULT_FONT;
    FlatPoint size = FontUtils.measure(text, fontName, fontSize, 0);
    Integer color = toColor(ele.getAttribute(FILL));

    Object paint = ClassUtils.newObject(PAINT);
    if (color != null) {
      ClassUtils.invoke(paint, "setColor", color);
    }

    Object typeface = ClassUtils.invokeStatic(TYPE_FACE, "create",
                                              new Class[]{String.class, int.class},
                                              fontName,
                                              ClassUtils.getStaticField(TYPE_FACE, "NORMAL"));
    ClassUtils.invoke(paint, "setTypeface", typeface);
    ClassUtils.invoke(paint, "setTextSize", (float) fontSize);
    ClassUtils.invoke(canvas, "drawText",
                      new Class[]{String.class, float.class, float.class, PAINT},
                      text, (float) (x - (size.getWidth() / 2)), (float) y, paint);
  }

  private void drawPolygon(Element ele, Object canvas) throws Exception {
    Object[] path = toPoints(ele.getAttribute(POINTS));
    if (path == null) {
      return;
    }

    drawPath(ele, canvas, path, false, true);
  }

  private void drawPath(Element ele, Object canvas) throws Exception {
    String points = ele.getAttribute(D);
    Object[] pointFs = toPoints(points);
    if (pointFs == null || points.length() == 0) {
      return;
    }

    drawPath(ele, canvas, pointFs, points.contains(CURVE_PATH_MARK), false);
  }

  private void drawPath(Element ele, Object canvas, Object[] pointFs,
                        boolean isCurve, boolean needClose) throws Exception {
    Object path = ClassUtils.newObject(PATH);
    ClassUtils.invoke(path, "moveTo", getX(pointFs[0]), getY(pointFs[0]));
    if (isCurve) {
      for (int i = 1; i < pointFs.length; i += 3) {
        Object p1 = pointFs[i];
        Object p2 = pointFs[i + 1];
        Object p3 = pointFs[i + 2];
        ClassUtils.invoke(path, "cubicTo",
                          getX(p1), getY(p1),
                          getX(p2), getY(p2),
                          getX(p3), getY(p3)
        );
        ClassUtils.invoke(path, "moveTo", getX(p3), getY(p3));
      }
    } else {
      for (int i = 1; i < pointFs.length; i++) {
        ClassUtils.invoke(path, "lineTo", getX(pointFs[i]), getY(pointFs[i]));
      }
    }
    if (needClose) {
      ClassUtils.invoke(path, "close");
    }

    Object border = ClassUtils.newObject(PAINT);
    Object fill = ClassUtils.newObject(PAINT);
    if (setShapeCommonAttr(ele, border, true)) {
      ClassUtils.invoke(canvas, "drawPath", path, border);
    }
    if (setShapeCommonAttr(ele, fill, false)) {
      ClassUtils.invoke(canvas, "drawPath", path, fill);
    }
  }

  private boolean setShapeCommonAttr(Element ele, Object paint, boolean isBorder) throws Exception {
    Integer color;
    if (isBorder) {
      color = toColor(ele.getAttribute(STROKE));
    } else {
      color = toColor(ele.getAttribute(FILL));
    }

    if (color == null) {
      return false;
    }

    ClassUtils.invoke(paint, "setColor", color);
    if (isBorder) {
      ClassUtils.invoke(paint, "setStyle", Enum.valueOf((Class<? extends Enum>) STYLE, "STROKE"));
      String sw = ele.getAttribute(STROKE_WIDTH);
      double strokeWidth = sw != null ? toDouble(sw) : 1D;
      if (strokeWidth <= 0) {
        return false;
      }
      float[] dashPattern = toFloatPair(ele.getAttribute(STROKE_DASHARRAY));
      if (dashPattern != null) {
        Object dashPathEffect = ClassUtils.newObject(DASH_PATH_EFFECT, dashPattern, (float) 0);
        ClassUtils.invokeOne(paint, "setPathEffect", PATH_EFFECT, dashPathEffect);
      }
      ClassUtils.invoke(paint, "setStrokeWidth", (float) strokeWidth);
    } else {
      ClassUtils.invoke(paint, "setStyle", Enum.valueOf((Class<? extends Enum>) STYLE, "FILL"));
    }

    return true;
  }

  private Object getX(Object point) throws Exception {
    return ClassUtils.getField(point, "x");
  }

  private Object getY(Object point) throws Exception {
    return ClassUtils.getField(point, "y");
  }

  private int toInt(String doubleStr) {
    if (StringUtils.isEmpty(doubleStr)) {
      return 0;
    }
    return (int) Double.parseDouble(doubleStr.replaceAll(PT, StringUtils.EMPTY));
  }

  private float toFloat(String doubleStr) {
    if (StringUtils.isEmpty(doubleStr)) {
      return 0;
    }
    return Float.parseFloat(doubleStr.replaceAll(PT, StringUtils.EMPTY));
  }

  private double toDouble(String doubleStr) {
    if (StringUtils.isEmpty(doubleStr)) {
      return 0;
    }
    return Double.parseDouble(doubleStr.replaceAll(PT, StringUtils.EMPTY));
  }

  public Integer toColor(String hexColorCode) throws Exception {
    if (hexColorCode == null || NONE.equals(hexColorCode)) {
      return null;
    }
    return (Integer) ClassUtils.invokeStatic(COLOR, "parseColor",
                                             new Class[]{String.class}, hexColorCode);
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
    Object[] pointFs = new Object[pointPairs.length];
    for (int i = 0; i < pointPairs.length; i++) {
      String[] point = pointPairs[i].split(COMMA);
      pointFs[i] = ClassUtils.newObject(POINTF, toFloat(point[0]), toFloat(point[1]));
    }
    return pointFs;
  }


  private static class ImgContext {

    private Object img;
    private Object canvas;
  }
}