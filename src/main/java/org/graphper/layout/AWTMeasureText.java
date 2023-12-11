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
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.def.FlatPoint;

/**
 * Measure text size by {@code java.awt} package.
 *
 * @author Jamison Jiang
 */
public class AWTMeasureText implements MeasureText, FontSelector {

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

    fontName = fontName == null ? DEFAULT_FONT : fontName;
    Font font = new Font(fontName, Font.PLAIN, (int) fontSize);
    AffineTransform transform = font.getTransform();
    FontRenderContext render = new FontRenderContext(transform, true, true);
    Rectangle2D rectangle = font.getStringBounds(text, render);

    double w = rectangle.getWidth();
    double h = rectangle.getHeight() * text.split("\n").length;

    return new FlatPoint(h, w);
  }

  @Override
  public String defaultFont() {
    return "Default";
  }
}
