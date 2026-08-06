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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.FontStyle;
import org.graphper.def.FlatPoint;
import org.graphper.layout.FontSelector;
import org.graphper.layout.MeasureText;

/**
 * Utility class for font-related operations. Provides methods to measure text dimensions, check
 * font existence, and select fonts that support specific characters or text content.
 *
 * @author Jamison Jiang
 */
public class FontUtils {

  private FontUtils() {
    // Private constructor to prevent instantiation
  }

  private static final MeasureText MEASURE_TEXT = selectMeasureText();
  private static final FontSelector FONT_SELECTOR = selectFontSelector();

  /**
   * Default font to use when no suitable font is specified or found.
   */
  public static final String DEFAULT_FONT = defaultFont();

  private static String defaultFont() {
    String defaultFont = FONT_SELECTOR.defaultFont();
    return StringUtils.isEmpty(defaultFont) ? "Times New Roman" : defaultFont;
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

  private static FontSelector selectFontSelector() {
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

    if (fontSelector == null) {
      throw new RuntimeException("Could not find any available FontSelector");
    }
    return fontSelector;
  }

  /**
   * Measures the actual size of a label's bounding box based on its content, font, and size.
   *
   * @param label      the label content to measure
   * @param fontName   the name of the font used for the label
   * @param fontSize   the size of the font
   * @param widthIncr  additional width added for fault tolerance
   * @param fontStyles the font styles of label
   * @return a {@link FlatPoint} representing the width and height of the label
   * @throws RuntimeException if the measurement operation fails or returns null
   */
  public static FlatPoint measure(String label, String fontName, double fontSize,
                                  double widthIncr, FontStyle... fontStyles) {
    FlatPoint size = MEASURE_TEXT.measure(label, fontName, fontSize, fontStyles);
    if (size == null) {
      throw new RuntimeException(
          "Unexpected error: MeasureText returned null for label size");
    }
    size.setWidth(size.getWidth() + widthIncr);
    return size;
  }

  /**
   * Checks if a specific font exists in the current environment.
   *
   * @param fontName the name of the font to check
   * @return {@code true} if the font exists, {@code false} otherwise
   */
  public static boolean fontExists(String fontName) {
    return FONT_SELECTOR.exists(fontName);
  }

  /**
   * Finds the first font that supports a given character.
   *
   * @param c the character to check
   * @return the name of the first font that supports the character, or {@code null} if none is
   * found
   */
  public static String findFirstSupportFont(char c) {
    return FONT_SELECTOR.findFirstSupportFont(c);
  }

  /**
   * Selects an appropriate font for the given text. If the specified font supports all characters
   * in the text, it is returned. Otherwise, the font that supports the most characters in the text
   * is selected.
   *
   * @param text     the text to be rendered
   * @param fontName the preferred font name
   * @return the selected font name
   */
  public static String selectFont(String text, String fontName) {
    if (StringUtils.isEmpty(text)) {
      return fontName;
    }

    /*
     * 1. Return manual set fontName if fontName supports all characters;
     * 2. Otherwise, return the font that supports the most characters.
     */
    boolean supportAll = true;
    Map<String, Integer> fontCount = null;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (!FONT_SELECTOR.fontSupport(fontName, c)) {
        supportAll = false;
      }

      String font = findFirstSupportFont(c);
      if (font == null) {
        continue;
      }
      if (fontCount == null) {
        fontCount = new HashMap<>(1);
      }
      fontCount.compute(font, (f, n) -> n == null ? 1 : n + 1);
    }

    if (supportAll) {
      return fontName;
    }
    if (fontCount == null) {
      return DEFAULT_FONT;
    }

    int max = Integer.MIN_VALUE;
    String maxSupportFont = null;
    for (Entry<String, Integer> entry : fontCount.entrySet()) {
      if (entry.getValue() > max) {
        max = entry.getValue();
        maxSupportFont = entry.getKey();
      }
    }

    return maxSupportFont == null ? DEFAULT_FONT : maxSupportFont;
  }
}
