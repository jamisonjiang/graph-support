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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache_gs.commons.lang3.StringUtils;
import org.apache_gs.commons.text.StringEscapeUtils;
import org.graphper.api.FileType;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.svg.Document;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.util.EnvProp;
import org.graphper.util.FontUtils;

/**
 * Default implementation of {@link SvgConverter} to convert SVG elements into image files. This
 * implementation supports multiple image formats, including PNG, JPG, JPEG, and GIF, and uses the
 * Java AWT library to perform the rendering.
 *
 * @author Jamison Jiang
 */
public class DefaultImgConverter implements SvgConverter, SvgConstants {

  /**
   * Returns the priority order of this converter. If local image converter is used, returns the
   * lowest priority.
   *
   * @return the priority order of this converter
   */
  @Override
  public int order() {
    if (EnvProp.useLocalImgConverter()) {
      return Integer.MIN_VALUE;
    }
    return 1;
  }

  /**
   * Checks if the current environment supports the image conversion. Specifically, it checks if the
   * {@link Graphics2D} class is available.
   *
   * @return {@code true} if the environment supports image conversion, {@code false} otherwise
   */
  @Override
  public boolean envSupport() {
    try {
      Class.forName("java.awt.Graphics2D");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Returns the supported file types for the conversion.
   *
   * @return an array of supported {@link FileType}
   */
  @Override
  public FileType[] supportFileTypes() {
    return new FileType[]{FileType.PNG, FileType.JPG, FileType.JPEG, FileType.GIF};
  }

  /**
   * Converts the given SVG document into an image of the specified type. Processes each element of
   * the SVG and renders it using {@link Graphics2D}.
   *
   * @param document  the SVG document to convert
   * @param drawGraph the drawing context with graph-related attributes
   * @param imageType the target image type for conversion
   * @return a {@link DefaultGraphResource} representing the converted image
   * @throws FailInitResourceException if the conversion fails or if parameters are missing
   */
  @Override
  public DefaultGraphResource convert(Document document, DrawGraph drawGraph, FileType imageType)
      throws FailInitResourceException {
    if (document == null || drawGraph == null || imageType == null) {
      throw new FailInitResourceException("Lack parameters to convert image");
    }

    ImgContext imgContext = new ImgContext();
    document.accessEles(((ele, children) -> {
      if (Objects.equals(ele.tagName(), SVG_ELE)) {
        initImage(drawGraph, imageType, imgContext, ele);
        return;
      }
      Graphics2D g2d = imgContext.g2d;
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
    }));

    if (imgContext.img == null) {
      return null;
    }
    imgContext.g2d.dispose();

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(imgContext.img, imageType.getType(), baos);
      String label = drawGraph.getGraphviz().graphAttrs().getLabel();
      return new DefaultGraphResource(label, imageType.getType(), baos);
    } catch (IOException e) {
      throw new FailInitResourceException(e);
    }
  }

  /**
   * Initializes an image based on the provided dimensions and scale.
   *
   * @param drawGraph  the drawing context
   * @param imageType  the target image type
   * @param imgContext the image context to be initialized
   * @param ele        the SVG element containing the attributes
   */
  private void initImage(DrawGraph drawGraph, FileType imageType,
                         ImgContext imgContext, Element ele) {
    int h = toInt(ele.getAttribute(HEIGHT));
    int w = toInt(ele.getAttribute(WIDTH));
    FlatPoint scale = drawGraph.getGraphviz().graphAttrs().getScale();
    AffineTransform transform = new AffineTransform();
    if (scale != null) {
      transform.scale(scale.getX() * 1.3333, scale.getY() * 1.3333);
    }

    w = (int) (w * 1.3333);
    h = (int) (h * 1.3333);
    if (imageType == FileType.PNG) {
      imgContext.setImg(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
    } else {
      imgContext.setImg(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB));
    }
    imgContext.g2d.setTransform(transform);
    imgContext.g2d.setBackground(Color.WHITE);
    imgContext.g2d.clearRect(0, 0, w, h);
  }

  /**
   * Draws an ellipse based on the attributes of the given SVG element.
   *
   * @param ele the SVG element representing the ellipse
   * @param g2d the graphics context used to draw the ellipse
   */
  private void drawEllipse(Element ele, Graphics2D g2d) {
    g2d.setColor(Color.BLACK);
    double x = toDouble(ele.getAttribute(CX));
    double y = toDouble(ele.getAttribute(CY));
    double w = toDouble(ele.getAttribute(RX));
    double h = toDouble(ele.getAttribute(RY));
    Ellipse2D ellipse = new Ellipse2D.Double(x - w, y - h, 2 * w, 2 * h);
    setShapeCommonAttr(ele, g2d, ellipse);
  }

  /**
   * Draws a text string based on the attributes of the given SVG element.
   *
   * @param ele the SVG element representing the text
   * @param g2d the graphics context used to draw the text
   */
  public void drawString(Element ele, Graphics2D g2d) {
    String text = ele.textContext();
    if (StringUtils.isEmpty(text)) {
      return;
    }

    text = StringEscapeUtils.unescapeXml(text);
    int fontSize = toInt(ele.getAttribute(FONT_SIZE));
    double x = toDouble(ele.getAttribute(X));
    double y = toDouble(ele.getAttribute(Y));
    Color color = toColor(ele.getAttribute(FILL));
    if (color == null) {
      color = Color.BLACK;
    }
    g2d.setColor(color);
    g2d.setPaint(color);

    String fontName = ele.getAttribute(FONT_FAMILY);
    fontName = FontUtils.fontExists(fontName) ? fontName : DEFAULT_FONT;
    Font defaultFont = new Font(fontName, Font.PLAIN, fontSize);

    int pre = 0;
    Font font = null;
    FlatPoint size = FontUtils.measure(text, fontName, fontSize, 0);
    x = x - (size.getWidth() / 2);

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (font != null && font.canDisplay(c)) {
        continue;
      }

      if (font != null) {
        AWTextRender awTextRender = new AWTextRender(font, text.substring(pre, i), x, y, g2d);
        x += awTextRender.draw();
        pre = i;
      }

      if (defaultFont.canDisplay(c)) {
        font = defaultFont;
      } else {
        String supportFont = FontUtils.findFirstSupportFont(c);
        if (supportFont == null) {
          font = null;
        } else {
          font = new Font(supportFont, Font.PLAIN, fontSize);
        }
      }
    }

    font = font != null ? font : defaultFont;
    AWTextRender awTextRender = new AWTextRender(font, text.substring(pre), x, y, g2d);
    awTextRender.draw();
  }

  /**
   * Draws a polygon based on the attributes of the given SVG element.
   *
   * @param ele the SVG element representing the polygon
   * @param g2d the graphics context used to draw the polygon
   */
  private void drawPolygon(Element ele, Graphics2D g2d) {
    Path2D.Double polygon = new Path2D.Double();
    Point2D.Double[] path = toPoints(ele.getAttribute(POINTS));
    if (path == null) {
      return;
    }
    for (int i = 0; i < path.length; i++) {
      if (i == 0) {
        polygon.moveTo(path[i].x, path[i].y);
      } else {
        polygon.lineTo(path[i].x, path[i].y);
      }
    }
    polygon.closePath();
    setShapeCommonAttr(ele, g2d, polygon);
  }

  /**
   * Draws a path based on the attributes of the given SVG element.
   *
   * @param ele the SVG element representing the path
   * @param g2d the graphics context used to draw the path
   */
  private void drawPath(Element ele, Graphics2D g2d) {
    String points = ele.getAttribute(D);
    Point2D.Double[] path = toPoints(points);
    if (path == null) {
      return;
    }
    g2d.setColor(Color.BLACK);
    Path2D path2D = new Path2D.Double();
    if (points.contains(CURVE_PATH_MARK)) {
      for (int i = 3; i < path.length; i += 3) {
        Point2D.Double p1 = path[i - 3];
        Point2D.Double p2 = path[i - 2];
        Point2D.Double p3 = path[i - 1];
        Point2D.Double p4 = path[i];
        CubicCurve2D.Double curve = new CubicCurve2D.Double(p1.x, p1.y, p2.x, p2.y,
                                                            p3.x, p3.y, p4.x, p4.y);
        path2D.append(curve, true);
      }

    } else {
      for (int i = 1; i < path.length; i++) {
        Point2D.Double start = path[i - 1];
        Point2D.Double end = path[i];
        Line2D.Double line = new Line2D.Double(start.getX(), start.getY(),
                                               end.getX(), end.getY());
        path2D.append(line, true);
      }
    }

    setShapeCommonAttr(ele, g2d, path2D);
  }

  /**
   * Sets the common attributes of a shape such as fill color, stroke color, and stroke width.
   *
   * @param ele   the SVG element with attributes to be set
   * @param g2d   the graphics context used to apply these attributes
   * @param shape the shape to which the attributes are applied
   */
  private void setShapeCommonAttr(Element ele, Graphics2D g2d, Shape shape) {
    Color color = toColor(ele.getAttribute(FILL));
    Color borderColor = toColor(ele.getAttribute(STROKE));
    if (color == null && borderColor == null) {
      return;
    }

    String sw = ele.getAttribute(STROKE_WIDTH);
    double strokeWidth = sw != null ? toDouble(sw) : 1D;
    if (strokeWidth <= 0 && color == null) {
      return;
    }

    if (color != null) {
      g2d.setColor(color);
      g2d.fill(shape);
    }

    float[] dashPattern = toFloatPair(ele.getAttribute(STROKE_DASHARRAY));
    BasicStroke stroke;
    if (dashPattern != null) {
      stroke = new BasicStroke((float) strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                               5.0f, dashPattern, 0);
    } else {
      stroke = new BasicStroke((float) strokeWidth);
    }
    g2d.setStroke(stroke);
    g2d.setColor(borderColor);
    g2d.draw(shape);
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

  private Point2D.Double[] toPoints(String points) {
    if (StringUtils.isEmpty(points)) {
      return null;
    }
    points = points.replace(PATH_START_M, StringUtils.EMPTY);
    points = points.replace(CURVE_PATH_MARK, SPACE);
    String[] pointPairs = points.split(SPACE);
    Point2D.Double[] point2d = new Point2D.Double[pointPairs.length];
    for (int i = 0; i < pointPairs.length; i++) {
      String[] point = pointPairs[i].split(COMMA);
      point2d[i] = new Point2D.Double(toDouble(point[0]), toDouble(point[1]));
    }
    return point2d;
  }

  private Color toColor(String hexColorCode) {
    if (hexColorCode == null || NONE.equals(hexColorCode)) {
      return null;
    }
    int rgb = Integer.parseInt(hexColorCode.substring(1), 16);
    return new Color(rgb);
  }

  private static class ImgContext {

    private BufferedImage img;
    private Graphics2D g2d;

    public void setImg(BufferedImage img) {
      this.img = img;
      if (this.img != null) {
        g2d = this.img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }
    }
  }
}
