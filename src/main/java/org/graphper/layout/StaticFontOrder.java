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

public abstract class StaticFontOrder implements FontOrder {

  private final LinkedHashMap<String, Integer> fontOrderRecord;

  protected StaticFontOrder() {
    this.fontOrderRecord = getOrderMap();
  }

  @Override
  public Iterator<String> iterator() {
    return fontOrderRecord == null ? Collections.emptyIterator()
        : fontOrderRecord.keySet().iterator();
  }

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

  protected abstract String[] fontOrder();

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

  private int fontOrder(String font) {
    if (fontOrderRecord == null) {
      return 0;
    }
    Integer order = fontOrderRecord.get(font);
    return order == null ? Integer.MIN_VALUE : order;
  }
}
