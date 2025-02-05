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

import static org.apache_gs.commons.lang3.StringUtils.NEW_LINE_SYMBOL;

import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.FontStyle;
import org.graphper.def.FlatPoint;
import org.graphper.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MeasureText} and {@link FontSelector} for measuring text dimensions on
 * Android platforms. This class uses reflection to interact with Android's native {@code TextPaint}
 * and {@code Rect} classes to calculate the width and height of a given text string based on the
 * specified font and size.
 *
 * @author Jamison Jiang
 */
public class AndroidMeasureText implements MeasureText, FontSelector {

  private static final Logger log = LoggerFactory.getLogger(AndroidMeasureText.class);

  private static Class<?> RECT;
  private static Class<?> TEXT_PAIN;

  static {
    try {
      RECT = Class.forName("android.graphics.Rect");
      TEXT_PAIN = Class.forName("android.text.TextPaint");
    } catch (ClassNotFoundException e) {
      // Ignore missing classes
    }
  }

  /**
   * Returns the order of this implementation for environments where multiple strategies exist.
   * Lower values indicate higher priority.
   *
   * @return the priority order, default is {@code 0}
   */
  @Override
  public int order() {
    return 0;
  }

  /**
   * Checks if the current environment supports Android text measurement by verifying the
   * availability of required classes ({@code Rect} and {@code TextPaint}).
   *
   * @return {@code true} if the environment supports Android text measurement, {@code false}
   * otherwise
   */
  @Override
  public boolean envSupport() {
    return RECT != null && TEXT_PAIN != null;
  }

  /**
   * Returns the default font name used when no specific font is provided.
   *
   * @return the default font name, "Times New Roman"
   */
  @Override
  public String defaultFont() {
    return "Times New Roman";
  }

  /**
   * Measures the width and height of the specified text based on the given font name and size.
   *
   * <p>This method uses Android's {@code TextPaint} and {@code Rect} classes to calculate
   * dimensions. The height accounts for multiple lines by multiplying the height of a single line
   * by the number of lines in the text.</p>
   *
   * <p>If an error occurs during measurement, it logs the error and returns a {@link FlatPoint}
   * with both dimensions set to {@code 0}.</p>
   *
   * @param text       the text to be measured
   * @param fontName   the name of the font (not used in this implementation)
   * @param fontSize   the size of the font in points
   * @param fontStyles the font styles of text
   * @return a {@link FlatPoint} representing the height and width of the text
   */
  @Override
  public FlatPoint measure(String text, String fontName, double fontSize, FontStyle... fontStyles) {
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
      double height =
          (double) (int) ClassUtils.invoke(bounds, "height") * text.split(NEW_LINE_SYMBOL).length;
      return new FlatPoint(height, width);
    } catch (Exception e) {
      log.error("Measure text size encountered an error: ", e);
      return new FlatPoint(0, 0);
    }
  }
}