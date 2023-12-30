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

package org.graphper.api.ext;

import org.graphper.api.attributes.Color;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * A description of some characteristics of the shape, used for layout, line clipping, rendering and
 * other processes.
 *
 * @author Jamison Jiang
 */
public interface ShapePropCalc {

  /**
   * According to an inscribed rectangle, return the smallest container size that can ensure that
   * the current shape can completely cover the inscribed rectangle.
   *
   * @param innerHeight inscribed rectangle height
   * @param innerWidth  inscribed rectangle width
   * @return smallest container size of current shape
   */
  FlatPoint minContainerSize(double innerHeight, double innerWidth);

  /**
   * Confirm whether a specified point is within the shape through the center coordinates of the
   * shape and the width and height of the shape. This method uses this method as the judgment
   * standard for Newton's method iteration when performing line segment cutting.
   *
   * @param box   box information
   * @param point coordinates of the point to be detected
   * @return true - point in shape false - point not in shape
   * @throws NullPointerException null box or point
   */
  boolean in(Box box, FlatPoint point);

  /**
   * Node length and width post-adjustment. When the rectangle calculated by the node based on the
   * inscribed rectangle is not necessarily the final shape, it will be adjusted according to some
   * logic. The adjusted rectangle may lose the proportion of the original shape, and it needs to be
   * adjusted according to the shape.
   *
   * @param boxSize external container size
   */
  default void ratio(FlatPoint boxSize) {
  }

  /**
   * According to the size of the external container and the size of the label, set which position
   * the label should be placed in which container.
   *
   * @param labelSize label size
   * @param box       external container size
   * @return the label center coordinate
   */
  default FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    Asserts.nullArgument(box, "box");
    Asserts.nullArgument(labelSize, "labelSize");
    box.check();

    return new FlatPoint(box.getX(), box.getY());
  }

  /**
   * For the special version of {@link #ratio(FlatPoint)}, the length and width are both set to the
   * largest of the length and width.
   *
   * @param boxSize external container size
   */
  default void squareRatio(FlatPoint boxSize) {
    if (boxSize == null) {
      return;
    }

    double max = Math.max(boxSize.getWidth(), boxSize.getHeight());
    boxSize.setWidth(max);
    boxSize.setHeight(max);
  }

  /**
   * Returns whether to enable the margin attribute.
   *
   * @return <tt>true</tt> if enable the margin property
   */
  default boolean needMargin() {
    return true;
  }

  /**
   * Returns whether to ignore the label attribute.
   *
   * @return <tt>true</tt> if ignore the label attribute
   */
  default boolean ignoreLabel() {
    return false;
  }

  /**
   * Returns the default filling color of the shape box, and returning null means no filling by
   * default.
   *
   * @return default filling color
   */
  default Color defaultFillColor() {
    return null;
  }
}
