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

package org.graphper.util;

import java.util.ServiceLoader;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.FlatPoint;
import org.graphper.layout.FontSelector;
import org.graphper.layout.MeasureText;

public class FontUtils {

  private FontUtils() {
  }

  public static final String DEFAULT_FONT;

  private static final MeasureText MEASURE_TEXT;

  static {
    MEASURE_TEXT = selectMeasureText();
    DEFAULT_FONT = selectDefaultFont();
  }

  private static MeasureText selectMeasureText() {
    ServiceLoader<MeasureText> measureTextServiceLoader = ServiceLoader.load(MeasureText.class);
    MeasureText measureText = null;
    for (MeasureText ms : measureTextServiceLoader) {
      if (!ms.envSupport()) {
        continue;
      }

      if (measureText == null || measureText.order() > ms.order()) {
        measureText = ms;
      }
    }

    if (measureText == null) {
      throw new RuntimeException("Could not find any available MeasureText");
    }

    return measureText;
  }

  private static String selectDefaultFont() {
    ServiceLoader<FontSelector> fontSelectorServiceLoader = ServiceLoader.load(FontSelector.class);
    FontSelector fontSelector = null;
    for (FontSelector fs : fontSelectorServiceLoader) {
      if (!fs.envSupport()) {
        continue;
      }

      if (fontSelector == null || fontSelector.order() > fs.order()) {
        fontSelector = fs;
      }
    }

    String defaultFont = fontSelector != null ? fontSelector.defaultFont() : null;
    return StringUtils.isEmpty(defaultFont) ? "Times New Roman" : defaultFont;
  }

  /**
   * Calculate the actual size of the label container based on the font and size of the label.
   *
   * @param label     label content
   * @param fontName  font name
   * @param fontSize  font size
   * @param widthIncr increased width for fault tolerance
   * @return label size
   */
  public static FlatPoint measure(String label, String fontName,
                                  double fontSize, double widthIncr) {
    FlatPoint size = MEASURE_TEXT.measure(label, fontName, fontSize);
    if (size == null) {
      throw new RuntimeException(
          "Occurred unexpected error, MeasureText can not work and return null label size");
    }
    size.setWidth(size.getWidth() + widthIncr);
    return size;
  }
}
