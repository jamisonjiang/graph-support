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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graphper.util.Asserts;

/**
 * Provide the basic font select policy is try best to fount the most popular font from exists fonts
 * int system as the {@link #defaultFont()} when caller not set any font or set font not exists in
 * system (detected by {@link #exists(String)}).
 *
 * @author Jamison Jiang
 */
public abstract class AbstractFontSelector implements FontSelector {

  private String defaultFont;

  private FontOrder fontOrder;

  private LinkedHashSet<String> allAvailableFonts;

  protected AbstractFontSelector() {
    // moved to #possiblyLoad()
  }
  
  private boolean loaded = false;
  
  protected void possiblyLoad() {
    if (loaded == false) {
      initFontComparator();
      Asserts.nullArgument(fontOrder, "Cannot found font comparator");
      initDefaultFont();
      loaded = true;
    }
  }

  /**
   * Return all system available fonts.
   *
   * @return all system available fonts
   */
  protected abstract String[] listAllSystemFonts();

  @Override
  public abstract boolean fontSupport(String fontName, char c);

  /**
   * Returns default font name when not set fontName attribute.
   *
   * @return default font name
   */
  @Override
  public String defaultFont() {
   possiblyLoad();
   return defaultFont;
  }

  /**
   * Return true if font exists in system.
   *
   * @param fontName font name
   * @return true if font exists in system
   */
  @Override
  public boolean exists(String fontName) {
    possiblyLoad();
    if (Objects.isNull(fontName)) {
      return false;
    }

    return allAvailableFonts.contains(fontName);
  }

  @Override
  public String findFirstSupportFont(char c) {
    possiblyLoad();
    for (String font : allAvailableFonts) {
      if (fontSupport(font, c)) {
        return font;
      }
    }

    return null;
  }

  private void initFontComparator() {
    ServiceLoader<FontOrder> fontComparatorServiceLoader = ServiceLoader
        .load(FontOrder.class);
    for (FontOrder comparator : fontComparatorServiceLoader) {
      fontOrder = comparator;
    }
  }

  private void initDefaultFont() {
    String[] fonts = listAllSystemFonts();
    if (fonts == null || fonts.length == 0) {
      defaultFont = fontOrder.first();
      Asserts.nullArgument(defaultFont, "Cannot init default Font");
      allAvailableFonts = new LinkedHashSet<>();
      return;
    }

    List<String> orderFonts = Stream.of(fonts).sorted(fontOrder).collect(Collectors.toList());
    this.allAvailableFonts = new LinkedHashSet<>(orderFonts);
    this.defaultFont = orderFonts.get(0);
  }

}
