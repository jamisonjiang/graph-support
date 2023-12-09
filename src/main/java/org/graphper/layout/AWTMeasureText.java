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

package org.graphper.layout;

import static org.graphper.util.FontUtils.DEFAULT_FONT;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.FlatPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measure text size by {@code java.awt} package.
 *
 * @author Jamison Jiang
 */
public class AWTMeasureText implements MeasureText, FontSelector {

  private static final Logger log = LoggerFactory.getLogger(AWTMeasureText.class);

  private static Constructor<?> FONT_CONS;

  private static Method GET_TRANSFORM;

  private static Method TEXT_GET_WIDTH;

  private static Method TEXT_GET_HEIGHT;

  private static Method STRING_BOUNDS_METHOD;

  private static Constructor<?> RENDER_CONSTRUCTOR;

  static {
    if (checkEnvSupport()) {
      try {
        // Get class
        Class<?> canvasClazz = Class.forName("java.awt.Canvas");
        Object fontMetricCanvas = canvasClazz.newInstance();
        Class<?> fontClazz = Class.forName("java.awt.Font");
        GET_TRANSFORM = fontClazz.getMethod("getTransform");
        FONT_CONS = fontClazz.getConstructor(String.class, int.class, int.class);
        Class<?> affineTransform = Class.forName("java.awt.geom.AffineTransform");
        Class<?> fontRenderContext = Class.forName("java.awt.font.FontRenderContext");
        RENDER_CONSTRUCTOR = fontRenderContext
            .getConstructor(affineTransform, boolean.class, boolean.class);
        STRING_BOUNDS_METHOD = fontClazz.getMethod("getStringBounds", String.class,
                                                   fontRenderContext);
        Class<?> rectangle2D = Class.forName("java.awt.geom.Rectangle2D");
        TEXT_GET_WIDTH = rectangle2D.getMethod("getWidth");
        TEXT_GET_HEIGHT = rectangle2D.getMethod("getHeight");
        // Warm up Font metrics
        fontMetrics(null, 0, fontMetricCanvas, canvasClazz, fontClazz);
      } catch (Exception e) {
      }
    }
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public boolean envSupport() {
    return checkEnvSupport();
  }

  @Override
  public FlatPoint measure(String text, String fontName, double fontSize) {
    if (StringUtils.isEmpty(text) || fontSize <= 0) {
      return new FlatPoint(0, 0);
    }

    try {
      fontName = fontName == null ? DEFAULT_FONT : fontName;

      Object font = newFont(fontName, (int) fontSize);
      Object affineTransform = GET_TRANSFORM.invoke(font);

      Object render = RENDER_CONSTRUCTOR.newInstance(affineTransform, true, true);
      Object rectangle = STRING_BOUNDS_METHOD.invoke(font, text, render);

      double w = (double) TEXT_GET_WIDTH.invoke(rectangle);
      double h = (double) TEXT_GET_HEIGHT.invoke(rectangle) * text.split("\n").length;

      return new FlatPoint(h, w);
    } catch (Exception e) {
      log.error("Measure text size had occurred error: ", e);
      return new FlatPoint(0, 0);
    }
  }

  private static boolean checkEnvSupport() {
    try {
      Class.forName("java.awt.Canvas");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static Object newFont(String fontName, int fontSize) throws Exception {
    return FONT_CONS.newInstance(fontName, 0, fontSize);
  }

  private static Object fontMetrics(String fontName, int fontSize, Object fontMetricCanvas,
                                    Class<?> canvasClazz, Class<?> fontClazz) throws Exception {
    Object font = newFont(fontName, fontSize);
    Method fontMetrics = canvasClazz.getMethod("getFontMetrics", fontClazz);
    fontMetrics.invoke(fontMetricCanvas, font);
    return font;
  }

  @Override
  public String defaultFont() {
    return "Default";
  }
}
