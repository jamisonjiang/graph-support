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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.FlatPoint;

public class AWTMeasureText extends MeasureText {

  private static final Logger log = LoggerFactory.getLogger(AWTMeasureText.class);

  private static Class<?> FONT_CLAZZ;

  private static Constructor<?> FONT_CONS;

  private static Object FONT_METRIC_CANVAS;

  private static Method FONT_METRICS_METHOD;

  private static Method FONT_METRICS_WIDTH;

  private static Method FONT_METRICS_HEIGHT;

  static {
    if (checkEnvSupport()) {
      try {
        // Get class
        Class<?> canvasClazz = Class.forName("java.awt.Canvas");
        FONT_METRIC_CANVAS = canvasClazz.newInstance();
        FONT_CLAZZ = Class.forName("java.awt.Font");
        FONT_METRICS_METHOD = canvasClazz.getMethod("getFontMetrics", FONT_CLAZZ);

        // Warm up Font metrics
        fontMetrics("Times,serif", 0);
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
      double width = 0;
      double height = 0;
      String[] lines = text.split("\n");
      Object fm = fontMetrics(fontName == null ? "Times,serif" : fontName, (int) fontSize);

      for (String line : lines) {
        FlatPoint size = getSizeWithFontMetric(fm, line);
        width = Math.max(size.getWidth(), width);
        height += size.getHeight();
      }

      return new FlatPoint(height, width);
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

  private static Object fontMetrics(String fontName, int fontSize) throws Exception {
    if (FONT_CLAZZ == null || FONT_METRICS_METHOD == null || StringUtils.isEmpty(fontName)) {
      return null;
    }

    if (FONT_CONS == null) {
      FONT_CONS = FONT_CLAZZ.getConstructor(String.class, int.class, int.class);
    }
    Object font = FONT_CONS.newInstance(fontName, 0, fontSize);

    // Font metrics
    return FONT_METRICS_METHOD.invoke(FONT_METRIC_CANVAS, font);
  }

  private static FlatPoint getSizeWithFontMetric(Object fm, String text) throws Exception {
    if (FONT_METRICS_HEIGHT == null || FONT_METRICS_WIDTH == null) {
      Class<?> fontMetrics = Class.forName("java.awt.FontMetrics");
      FONT_METRICS_HEIGHT = fontMetrics.getMethod("getHeight");
      FONT_METRICS_WIDTH = fontMetrics.getMethod("stringWidth", String.class);
    }

    int height = (int) FONT_METRICS_HEIGHT.invoke(fm);
    int width = (int) FONT_METRICS_WIDTH.invoke(fm, text);
    return new FlatPoint(height, width);
  }
}
