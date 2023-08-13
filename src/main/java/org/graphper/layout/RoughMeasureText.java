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

public class RoughMeasureText implements MeasureText {

  @Override
  public int order() {
    return 2;
  }

  @Override
  public boolean envSupport() {
    return true;
  }

  @Override
  public FlatPoint measure(String text, String fontName, double fontSize) {
    if (StringUtils.isEmpty(text) || fontSize <= 0) {
      return new FlatPoint(0, 0);
    }

    String[] lines = text.split("\n");
    int ll = lines.length;
    double height = ll * fontSize;
    double width = 0;
    for (String line : lines) {
      double w = 0;
      char[] chars = line.toCharArray();
      for (char c : chars) {
        if (StringUtils.isChineseChar(c)) {
          w += fontSize;
        } else {
          w += (fontSize / 2);
        }
      }
      if (w > width) {
        width = w;
      }
    }

    return new FlatPoint(height, width);
  }
}
