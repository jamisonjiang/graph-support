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

import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.def.FlatPoint;

public class LabelSizeHelper {

  private static final Logger log = LoggerFactory.getLogger(LabelSizeHelper.class);

  private static final MeasureText MEASURE_TEXT;

  static {
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

    log.info("MeasureText: {} was loaded as the primary measurement method.",
             measureText.getClass().getName());
    MEASURE_TEXT = measureText;
  }

  private LabelSizeHelper() {
  }

  /**
   * Calculate the actual size of the label container based on the font and size of the label.
   *
   * @param label     label content
   * @param fontName  font name
   * @param fontSize  font size
   * @param widthIncr increased width for fault tolerance
   * @return label size
   */
  public static FlatPoint measure(String label, String fontName,
                                  double fontSize, double widthIncr) {
    FlatPoint size = MEASURE_TEXT.measure(label, fontName, fontSize);
    size.setWidth(size.getWidth() + widthIncr);
    return size;
  }
}
