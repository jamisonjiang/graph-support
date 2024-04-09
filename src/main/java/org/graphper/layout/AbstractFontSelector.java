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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractFontSelector implements FontSelector {

  private static final String[] TOP_POPULAR_FONTS = {
      "Arial",
      "Times New Roman",
      "Calibri",
      "Helvetica",
      "Georgia",
      "Verdana",
      "Comic Sans MS",
      "Trebuchet MS",
      "Courier New",
      "Cambria",
      "Garamond",
      "Palatino",
      "Lucida Sans",
      "Lucida Console",
      "Futura",
      "Franklin Gothic",
      "Myriad",
      "Roboto",
      "Open Sans",
      "Baskerville",
      "Rockwell",
      "Century Gothic",
      "Tahoma",
      "Gill Sans",
      "Bodoni",
      "Copperplate",
      "Eurostile",
      "Museo",
      "Proxima Nova",
      "Lato",
      "Ubuntu",
      "DIN",
      "Arial Narrow",
      "Impact",
      "Book Antiqua",
      "Optima",
      "Segoe UI",
      "Brush Script",
      "Didot",
      "Helvetica Neue",
      "Raleway",
      "Montserrat",
      "Oswald",
      "Avenir",
      "Roboto Condensed",
      "PT Sans",
      "Source Sans Pro",
      "Merriweather",
      "Candara",
      "Courier Prime"
  };

  private volatile Set<String> ALL_AVAILABLE_FONTS;

  @Override
  public String defaultFont() {
    String[] fonts = listSystemDefaultFonts();
    if (fonts == null || fonts.length == 0) {
      return TOP_POPULAR_FONTS[0];
    }

    ALL_AVAILABLE_FONTS = new HashSet<>(fonts.length);
    ALL_AVAILABLE_FONTS.addAll(Arrays.asList(fonts));

    for (String font : TOP_POPULAR_FONTS) {
      if (ALL_AVAILABLE_FONTS.contains(font)) {
        return font;
      }
    }

    return fonts[0];
  }

  @Override
  public boolean exists(String fontName) {
    if (Objects.isNull(fontName)) {
      return false;
    }

    if (ALL_AVAILABLE_FONTS == null) {
      return true;
    }

    return ALL_AVAILABLE_FONTS.contains(fontName);
  }

  protected abstract String[] listSystemDefaultFonts();
}
