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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.FlatPoint;

/**
 * Measure text size by {@code java.awt} package.
 *
 * @author Jamison Jiang
 */
public class AWTMeasureText extends AbstractFontSelector implements MeasureText, FontSelector {

  private volatile Map<String, Font> fontCache;

  public AWTMeasureText() {
    super();
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public boolean envSupport() {
    try {
      Class.forName("java.awt.Font");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @Override
  public FlatPoint measure(String text, String fontName, double fontSize) {
    if (StringUtils.isEmpty(text) || fontSize <= 0) {
      return new FlatPoint(0, 0);
    }

    fontName = exists(fontName) ? fontName : DEFAULT_FONT;
    Font font = new Font(fontName, Font.PLAIN, (int) fontSize);
    FontRenderContext renderContext = new FontRenderContext(null, true, true);

    String[] lines = text.split("\n");
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

  @Override
  protected String[] listAllSystemFonts() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
  }

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
}
