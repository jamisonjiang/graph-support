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

import java.util.Comparator;

/**
 * Defines the order of font preferences and provides utility methods to compare, rank,
 * and iterate through fonts in a specific order.
 *
 * <p>This interface extends {@link Comparator} to define a custom font comparison logic
 * and {@link Iterable} to allow iteration through the ordered list of fonts.</p>
 *
 * @author Jamison Jiang
 */
public interface FontOrder extends Comparator<String>, Iterable<String> {

  /**
   * Checks whether a given font has a defined rank in this font order.
   *
   * @param fontName the name of the font to check
   * @return {@code true} if the font has a rank, {@code false} otherwise
   */
  boolean haveRank(String fontName);

  /**
   * Compares two font names based on their ranking. Fonts with a higher preference
   * should be ranked lower (e.g., return a negative value when the first font is preferred).
   *
   * @param o1 the first font name
   * @param o2 the second font name
   * @return a negative integer, zero, or a positive integer as the first font is ranked higher,
   *         equal to, or lower than the second font
   */
  @Override
  int compare(String o1, String o2);

  /**
   * Retrieves the first font in the ranking order. This is typically the font with the highest
   * preference in the ranking.
   *
   * @return the first font in the order, or {@code null} if there are no ranked fonts
   */
  default String first() {
    for (String font : this) {
      return font;
    }
    return null;
  }
}
