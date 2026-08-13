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

import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.def.FlatPoint;

/**
 * Shared final-size policies applied after shape-specific content sizing.
 *
 * @author Jamison Jiang
 */
public final class ShapeSizeUtils {

  private ShapeSizeUtils() {
  }

  /** Applies the Graphviz {@code regular=true} final size policy. */
  public static void applyRegular(NodeAttrs attrs, NodeShape shape, FlatPoint size) {
    if (attrs == null || shape == null || size == null || !shape.supportsRegular()) {
      return;
    }
    if (!Boolean.TRUE.equals(attrs.getRegular())
        && !NodeShapeEnum.REGULAR_POLYLINE.getName().equals(shape.getName())) {
      return;
    }
    double side = Math.max(size.getHeight(), size.getWidth());
    size.setHeight(side);
    size.setWidth(side);
  }
}
