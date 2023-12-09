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

import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.FlatPoint;
import org.graphper.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidMeasureText implements MeasureText, FontSelector {

  private static final Logger log = LoggerFactory.getLogger(AndroidMeasureText.class);

  private static Class<?> RECT;
  private static Class<?> TEXT_PAIN;

  static {
    try {
      RECT = Class.forName("android.graphics.Rect");
      TEXT_PAIN = Class.forName("android.text.TextPaint");
    } catch (ClassNotFoundException e) {
      // ignore
    }
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public boolean envSupport() {
    return RECT != null && TEXT_PAIN != null;
  }

  @Override
  public String defaultFont() {
    return "Times New Roman";
  }

  @Override
  public FlatPoint measure(String text, String fontName, double fontSize) {
    if (StringUtils.isEmpty(text)) {
      return new FlatPoint(0, 0);
    }

    try {
      Object myTextPaint = ClassUtils.newObject(TEXT_PAIN);
      ClassUtils.invoke(myTextPaint, "setAntiAlias", true);
      ClassUtils.invoke(myTextPaint, "setTextSize", (float) fontSize);

      Object bounds = ClassUtils.newObject(RECT);
      ClassUtils.invoke(myTextPaint, "getTextBounds", text, 0, text.length(), bounds);
      double width = (double) (int) ClassUtils.invoke(bounds, "width");
      double height = (double) (int) ClassUtils.invoke(bounds, "height") * text.split("\n").length;
      return new FlatPoint(height, width);
    } catch (Exception e) {
      log.error("Measure text size had occurred error: ", e);
      return new FlatPoint(0, 0);
    }
  }
}
