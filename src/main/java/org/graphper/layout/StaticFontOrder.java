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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache_gs.commons.lang3.ArrayUtils;
import org.apache_gs.commons.lang3.StringUtils;

/**
 * Provides a pre-defined static font order for use in font selection and comparison.
 *
 * <p>This class implements a static font order based on the sequence returned by the
 * {@link #fontOrder()} method. The order does not depend on the availability of the fonts
 * in the system and may be determined by factors such as popularity, alphabetical sorting,
 * or other custom logic defined in the implementation.</p>
 *
 * <p>The font order returned by {@link #fontOrder()} is used as the static standard
 * when initializing an instance of {@code StaticFontOrder}. The position of each font
 * in the array determines its ranking, with lower indices indicating higher priority.</p>
 *
 * @author Jamison Jiang
 */
public abstract class StaticFontOrder implements FontOrder {

  private final LinkedHashMap<String, Integer> fontOrderRecord;

  /**
   * Constructs a {@code StaticFontOrder} instance and initializes the font order map
   * based on the sequence defined in the {@link #fontOrder()} method.
   */
  protected StaticFontOrder() {
    this.fontOrderRecord = getOrderMap();
  }

  /**
   * Returns an iterator over the font names in the pre-defined order.
   *
   * @return an {@link Iterator} for the font names
   */
  @Override
  public Iterator<String> iterator() {
    return fontOrderRecord == null ? Collections.emptyIterator()
        : fontOrderRecord.keySet().iterator();
  }

  /**
   * Checks whether a given font name exists in the pre-defined order.
   *
   * @param fontName the name of the font to check
   * @return {@code true} if the font exists in the pre-defined order, {@code false} otherwise
   */
  @Override
  public boolean haveRank(String fontName) {
    if (fontOrderRecord == null || StringUtils.isEmpty(fontName)) {
      return false;
    }
    return fontOrderRecord.containsKey(fontName);
  }

  /**
   * Compares two font names based on their rank in the pre-defined order. Fonts not included
   * in the order are treated as having the lowest priority.
   *
   * @param o1 the first font name
   * @param o2 the second font name
   * @return a negative integer, zero, or a positive integer as the first font is ranked higher,
   *         equal to, or lower than the second font
   */
  @Override
  public int compare(String o1, String o2) {
    if (StringUtils.isEmpty(o1)) {
      return 1;
    }
    if (StringUtils.isEmpty(o2)) {
      return -1;
    }
    return Integer.compare(fontOrder(o1), fontOrder(o2));
  }

  /**
   * Provides the pre-defined order of font names as an array. Subclasses must implement
   * this method to specify the desired font order.
   *
   * @return an array of font names defining the order
   */
  protected abstract String[] fontOrder();

  /**
   * Converts the pre-defined font order array into a {@link LinkedHashMap} for efficient
   * ranking and iteration.
   *
   * @return a {@link LinkedHashMap} mapping font names to their ranks
   */
  private LinkedHashMap<String, Integer> getOrderMap() {
    String[] order = fontOrder();
    if (ArrayUtils.isEmpty(order)) {
      return null;
    }

    LinkedHashMap<String, Integer> orderMap = new LinkedHashMap<>(order.length);
    for (int i = 0; i < order.length; i++) {
      orderMap.put(order[i], i);
    }
    return orderMap;
  }

  /**
   * Retrieves the rank of a given font name. Fonts not in the pre-defined order
   * are assigned the lowest priority rank.
   *
   * @param font the font name to check
   * @return the rank of the font, or {@link Integer#MAX_VALUE} if not ranked
   */
  private int fontOrder(String font) {
    if (fontOrderRecord == null) {
      return 0;
    }
    Integer order = fontOrderRecord.get(font);
    return order == null ? Integer.MAX_VALUE : order;
  }
}