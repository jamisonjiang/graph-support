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

public class BoxUtils {

  private BoxUtils() {

  }

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
