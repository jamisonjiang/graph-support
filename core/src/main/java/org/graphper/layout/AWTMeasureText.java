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
import static org.graphper.api.attributes.FontStyle.containsBold;
import static org.graphper.api.attributes.FontStyle.containsItalic;
import static org.graphper.util.FontUtils.DEFAULT_FONT;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.FontStyle;
import org.graphper.def.FlatPoint;

/**
 * Measures text size using the {@code java.awt} package. This implementation leverages the AWT
 * `Font` and `FontRenderContext` classes to calculate the dimensions of text based on the specified
 * font and size.
 *
 * @author Jamison Jiang
 * @see MeasureText Interface for text measurement utilities
 * @see FontSelector Interface for font selection utilities
 * @see AbstractFontSelector Abstract base class for font selection strategies
 */
public class AWTMeasureText extends AbstractFontSelector implements MeasureText, FontSelector {

  private volatile Map<String, Font> fontCache;

  /**
   * Constructs an instance of {@code AWTMeasureText}.
   */
  public AWTMeasureText() {
    super();
  }

  /**
   * Returns the priority order of this implementation. Lower values indicate higher priority.
   *
   * @return the priority order, default is {@code 0}
   */
  @Override
  public int order() {
    return 0;
  }

  /**
   * Checks if the current environment supports text measurement using the AWT package.
   *
   * @return {@code true} if the environment supports AWT, {@code false} otherwise
   */
  @Override
  public boolean envSupport() {
    try {
      Class.forName("java.awt.Font");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Measures the width and height of the specified text based on the given font name and size.
   *
   * <p>This method calculates the dimensions of multi-line text by splitting the input into
   * lines and summing the heights of each line. The width is determined by the longest line.</p>
   *
   * <p>If the font name is invalid or not supported, a default font will be used.</p>
   *
   * @param text       the text to measure
   * @param fontName   the name of the font to use
   * @param fontSize   the size of the font in points
   * @param fontStyles the font styles of text
   * @return a {@link FlatPoint} representing the height and width of the text
   */
  @Override
  public FlatPoint measure(String text, String fontName, double fontSize, FontStyle... fontStyles) {
    if (StringUtils.isEmpty(text) || fontSize <= 0) {
      return new FlatPoint(0, 0);
    }

    fontName = exists(fontName) ? fontName : DEFAULT_FONT;
    Font font = new Font(fontName, toFontStyleTag(fontStyles), (int) fontSize);
    FontRenderContext renderContext = new FontRenderContext(null, true, true);

    String[] lines = text.split(NEW_LINE_SYMBOL);
    double maxWidth = 0;
    double totalHeight = 0;

    // Loop through each line and calculate max width and total height
    for (String line : lines) {
      Rectangle2D bounds = font.getStringBounds(line, renderContext);
      maxWidth = Math.max(bounds.getWidth(), maxWidth);
      totalHeight += bounds.getHeight();
    }

    // Return maxWidth for width and totalHeight for height in the correct order
    return new FlatPoint(totalHeight, maxWidth);
  }

  /**
   * Lists all system fonts available in the current environment.
   *
   * @return an array of available font family names
   */
  @Override
  protected String[] listAllSystemFonts() {
    try {
      return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    } catch (NoClassDefFoundError e) {
      return null;
    }
  }

  /**
   * Checks if a specific font supports rendering a given character.
   *
   * <p>This method uses a cache to store {@link Font} objects for efficient repeated checks.</p>
   *
   * @param fontName the name of the font
   * @param c        the character to check
   * @return {@code true} if the font supports the character, {@code false} otherwise
   */
  @Override
  public boolean fontSupport(String fontName, char c) {
    if (StringUtils.isEmpty(fontName)) {
      return false;
    }

    if (fontCache == null) {
      synchronized (this) {
        if (fontCache == null) {
          fontCache = new ConcurrentHashMap<>();
        }
      }
    }

    Font font = fontCache.computeIfAbsent(fontName, fn -> new Font(fn, Font.PLAIN, 0));
    return font.canDisplay(c);
  }

  private int toFontStyleTag(FontStyle... fontStyles) {
    int fs = Font.PLAIN;
    if (containsBold(fontStyles)) {
      fs = Font.BOLD;
    }
    if (containsItalic(fontStyles)) {
      fs |= Font.ITALIC;
    }
    return fs;
  }
}