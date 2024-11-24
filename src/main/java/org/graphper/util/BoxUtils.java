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

package org.graphper.util;

import java.util.Objects;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;

/**
 * A utility class for performing operations on {@link Box} objects.
 *
 * @author Jamison Jiang
 */
public class BoxUtils {

  private BoxUtils() {
    // Prevent instantiation of utility class
  }

  /**
   * Creates a new {@link Box} that represents the combined bounds of the two input boxes.
   *
   * <p>The resulting box will have borders that encompass both input boxes:
   * <ul>
   *   <li>The left border is the minimum of the two boxes' left borders.</li>
   *   <li>The right border is the maximum of the two boxes' right borders.</li>
   *   <li>The upper border is the minimum of the two boxes' upper borders.</li>
   *   <li>The lower border is the maximum of the two boxes' lower borders.</li>
   * </ul>
   *
   * @param origin the first box to combine
   * @param expand the second box to combine
   * @return a new {@link Box} representing the combined bounds
   * @throws NullPointerException if either {@code origin} or {@code expand} is {@code null}
   */
  public static Box newCombineBox(Box origin, Box expand) {
    Objects.requireNonNull(origin);
    Objects.requireNonNull(expand);
    return new DefaultBox(
        Math.min(origin.getLeftBorder(), expand.getLeftBorder()),
        Math.max(origin.getRightBorder(), expand.getRightBorder()),
        Math.min(origin.getUpBorder(), expand.getUpBorder()),
        Math.max(origin.getDownBorder(), expand.getDownBorder())
    );
  }
}

