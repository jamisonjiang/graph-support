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

import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import org.apache_gs.commons.lang3.StringUtils;
import org.apache_gs.commons.text.StringEscapeUtils;
import org.graphper.api.FileType;
import org.graphper.api.attributes.FontStyle;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.svg.Document;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.util.ClassUtils;
import org.graphper.util.FontUtils;

/**
 * Implementation of {@link SvgConverter} to convert SVG elements into Android Bitmap images. This
 * class uses Android's graphics library to perform the rendering and supports both PNG and JPEG
 * formats.
 *
 * @author Jamison Jiang
 */
@SuppressWarnings("unchecked")
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

  private static Class<?> BITMAP_FACTORY;

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
      BITMAP_FACTORY = Class.forName("android.graphics.BitmapFactory");
      DASH_PATH_EFFECT = Class.forName("android.graphics.DashPathEffect");
      COMPRESS_FORMAT = Class.forName("android.graphics.Bitmap$CompressFormat");
    } catch (Exception e) {
      // Ignore
    }
  }

  /**
   * Returns the priority order of this converter. The default order for Android implementation is
   * set to 0.
   *
   * @return the priority order of this converter
   */
  @Override
  public int order() {
    return 0;
  }

  /**
   * Checks if the current environment supports image conversion on Android. This requires certain
   * Android graphics classes to be present.
   *
   * @return {@code true} if the environment supports image conversion, {@code false} otherwise
   */
  @Override
  public boolean envSupport() {
    return PATH != null && COMPRESS_FORMAT != null;
  }

  /**
   * Returns the supported file types for the conversion.
   *
   * @return an array of supported {@link FileType}
   */
  @Override
  public FileType[] supportFileTypes() {
    return new FileType[]{FileType.PNG, FileType.JPEG};
  }

  /**
   * Converts the given SVG document into an image of the specified type. Processes each element of
   * the SVG and renders it using Android's graphics classes.
   *
   * @param document  the SVG document to convert
   * @param drawGraph the drawing context with graph-related attributes
   * @param fileType  the target image type for conversion
   * @return a {@link DefaultGraphResource} representing the converted image
   * @throws FailInitResourceException if the conversion fails or if parameters are missing
   */
  @Override
  @SuppressWarnings("unchecked")
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
          return;
        }

        if (Objects.equals(ele.tagName(), IMAGE_ELE)) {
          drawImage(ele, canvas);
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

  /**
   * Initializes an image based on the provided dimensions and scale.
   *
   * @param drawGraph  the drawing context
   * @param imgContext the image context to be initialized
   * @param ele        the SVG element containing the attributes
   * @throws Exception if initialization fails
   */
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
      ClassUtils.invoke(transform, "setScale", (float) (scale.getX() / 10),
                        (float) (scale.getY() / 10));
      ClassUtils.invoke(imgContext.canvas, "setMatrix", transform);
    }
  }

  /**
   * Draws an ellipse based on the attributes of the given SVG element.
   *
   * @param ele    the SVG element representing the ellipse
   * @param canvas the graphics context used to draw the ellipse
   * @throws Exception if drawing fails
   */
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

  /**
   * Draws a text string based on the attributes of the given SVG element.
   *
   * @param ele    the SVG element representing the text
   * @param canvas the graphics context used to draw the text
   * @throws Exception if drawing fails
   */
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
                                              fontName, fontStyle(ele));
    ClassUtils.invoke(paint, "setTypeface", typeface);
    ClassUtils.invoke(paint, "setTextSize", (float) fontSize);

    float textX = (float) (x - (size.getWidth() / 2));
    float textY = (float) y;
    ClassUtils.invoke(canvas, "drawText",
                      new Class[]{String.class, float.class, float.class, PAINT},
                      text, textX, textY, paint);

    float strokeWidth = fontSize / 10.0f; // Proportional to font size
    ClassUtils.invoke(paint, "setStrokeWidth", strokeWidth);

    if (haveFontOverline(ele)) {
      int overline = (int) (y - size.getHeight() + Math.max(1.0f, fontSize / 4.0f));
      ClassUtils.invoke(canvas, "drawLine",
                        new Class[]{float.class, float.class, float.class, float.class, PAINT},
                        textX, overline, textX + (float) size.getWidth(), overline, paint);
    }

    if (haveFontUnderline(ele)) {
      float underlineY = (float) (textY + (size.getHeight() / 10.0f) + strokeWidth);
      ClassUtils.invoke(canvas, "drawLine",
                        new Class[]{float.class, float.class, float.class, float.class, PAINT},
                        textX, underlineY, textX + (float) size.getWidth(), underlineY, paint);
    }

    if (haveFontStrikeThrough(ele)) {
      float strikeThroughY = (float) (textY - (size.getHeight() / 3.0f));
      ClassUtils.invoke(canvas, "drawLine",
                        new Class[]{float.class, float.class, float.class, float.class, PAINT},
                        textX, strikeThroughY, textX + (float) size.getWidth(), strikeThroughY,
                        paint);
    }
  }

  /**
   * Draws a polygon based on the attributes of the given SVG element.
   *
   * @param ele    the SVG element representing the polygon
   * @param canvas the graphics context used to draw the polygon
   * @throws Exception if drawing fails
   */
  private void drawPolygon(Element ele, Object canvas) throws Exception {
    Object[] path = toPoints(ele.getAttribute(POINTS));
    if (path == null) {
      return;
    }

    drawPath(ele, canvas, path, false, true);
  }

  /**
   * Draws a path based on the attributes of the given SVG element.
   *
   * @param ele    the SVG element representing the path
   * @param canvas the graphics context used to draw the path
   * @throws Exception if drawing fails
   */
  private void drawPath(Element ele, Object canvas) throws Exception {
    String points = ele.getAttribute(D);
    Object[] pointFs = toPoints(points);
    if (pointFs == null || points.length() == 0) {
      return;
    }

    drawPath(ele, canvas, pointFs, points.contains(CURVE_PATH_MARK), false);
  }

  /**
   * Draws a path based on given points.
   *
   * @param ele       the SVG element
   * @param canvas    the graphics context
   * @param pointFs   the points defining the path
   * @param isCurve   {@code true} if the path contains curves, {@code false} otherwise
   * @param needClose {@code true} if the path should be closed, {@code false} otherwise
   * @throws Exception if drawing fails
   */
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

  private void drawImage(Element ele, Object canvas) throws Exception {
    // 1) Extract href from the <image> tag
    String href = ele.getAttribute("xlink:href");
    if (StringUtils.isEmpty(href)) {
      href = ele.getAttribute("href");
    }
    if (StringUtils.isEmpty(href)) {
      // No image source
      return;
    }
    // Unescape HTML entities (&amp; -> & etc.)
    href = StringEscapeUtils.unescapeHtml4(href);

    // 2) Load the Bitmap
    Object bitmap = loadBitmap(href);
    if (bitmap == null) {
      // Could not decode the image
      return;
    }

    // 3) Extract the bounding box from SVG <image> attributes
    double x = toDouble(ele.getAttribute("x"));
    double y = toDouble(ele.getAttribute("y"));
    double boxW = toDouble(ele.getAttribute("width"));
    double boxH = toDouble(ele.getAttribute("height"));

    // If no bounding box, just draw at natural size
    if (boxW <= 0 || boxH <= 0) {
      // Draw at (x, y) with the image’s intrinsic size
      ClassUtils.invoke(canvas, "drawBitmap",
                        new Class[]{BIT_MAP, float.class, float.class, PAINT},
                        bitmap, (float)x, (float)y, null
      );
      return;
    }

    // 4) Get natural (intrinsic) size of the loaded bitmap
    int imgW = (int) ClassUtils.invoke(bitmap, "getWidth");
    int imgH = (int) ClassUtils.invoke(bitmap, "getHeight");

    // 5) Compute scale so the entire image fits in the box
    double scale = Math.min(boxW / imgW, boxH / imgH);

    // 6) Final drawn size
    double finalW = imgW * scale;
    double finalH = imgH * scale;

    // 7) Center the image in (boxW, boxH)
    double xOffset = x + (boxW - finalW) / 2.0;
    double yOffset = y + (boxH - finalH) / 2.0;

    // 8) Make a Matrix for scaling, so we can call drawBitmap(Bitmap, Matrix, Paint).
    Object matrix = ClassUtils.newObject(MATRIX);
    // First translate so top-left is at (xOffset, yOffset)
    ClassUtils.invoke(matrix, "postTranslate", (float)xOffset, (float)yOffset);
    // Then scale the image’s top-left corner
    ClassUtils.invoke(matrix, "preScale",
                      (float)(scale), (float)(scale)
    );

    // 9) Draw the image using drawBitmap(Bitmap, Matrix, Paint)
    ClassUtils.invoke(canvas, "drawBitmap",
                      new Class[]{BIT_MAP, MATRIX, PAINT},
                      bitmap, matrix, null
    );
  }

  private Object loadBitmap(String href) throws Exception {
    try {
      // Try a URL
      URL url = new URL(href);
      try(InputStream in = url.openStream()) {
        return ClassUtils.invokeStatic(BITMAP_FACTORY, "decodeStream",
                                       new Class[]{InputStream.class}, in);
      }

    } catch (MalformedURLException e) {
      // Not a valid URL -> local file path?
      File file = new File(href);
      if (!file.exists()) {
        System.err.println("File not found: {}" + file.getAbsolutePath());
        return null;
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        return ClassUtils.invokeStatic(BITMAP_FACTORY, "decodeStream",
                                       new Class[]{InputStream.class}, fis);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Sets the common attributes of a shape such as fill color, stroke color, and stroke width.
   *
   * @param ele      the SVG element with attributes to be set
   * @param paint    the paint object to apply these attributes
   * @param isBorder {@code true} if setting border attributes, {@code false} for fill attributes
   * @return {@code true} if the shape attributes were successfully set, {@code false} otherwise
   * @throws Exception if setting attributes fails
   */
  @SuppressWarnings("unchecked")
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

  /**
   * Retrieves the x-coordinate from a given point object.
   *
   * @param point the point object
   * @return the x-coordinate
   * @throws Exception if retrieval fails
   */
  private Object getX(Object point) throws Exception {
    return ClassUtils.getField(point, "x");
  }

  /**
   * Retrieves the y-coordinate from a given point object.
   *
   * @param point the point object
   * @return the y-coordinate
   * @throws Exception if retrieval fails
   */
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

  /**
   * Converts a hex color code to an Android color integer.
   *
   * @param hexColorCode the hex color code to convert
   * @return the corresponding Android color integer, or {@code null} if conversion fails
   * @throws Exception if conversion fails
   */
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

  private boolean haveFontOverline(Element textEle) {
    return FontStyle.OVERLINE.name().equalsIgnoreCase(textEle.getAttribute(TEXT_DECORATION));
  }

  private boolean haveFontUnderline(Element textEle) {
    return FontStyle.UNDERLINE.name().equalsIgnoreCase(textEle.getAttribute(TEXT_DECORATION));
  }

  private boolean haveFontStrikeThrough(Element textEle) {
    return LINE_THROUGH.equalsIgnoreCase(textEle.getAttribute(TEXT_DECORATION));
  }

  private int fontStyle(Element textEle) {
    int fs = Font.PLAIN;
    if (isFontBold(textEle)) {
      fs = Font.BOLD;
    }
    if (isFontItalic(textEle)) {
      fs |= Font.ITALIC;
    }
    return fs;
  }

  private boolean isFontItalic(Element textEle) {
    return FontStyle.ITALIC.name().equalsIgnoreCase(textEle.getAttribute(FONT_STYLE));
  }

  private boolean isFontBold(Element textEle) {
    return FontStyle.BOLD.name().equalsIgnoreCase(textEle.getAttribute(FONT_WEIGHT));
  }

  private static class ImgContext {

    private Object img;
    private Object canvas;
  }
}