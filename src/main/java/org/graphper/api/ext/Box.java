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

import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * Public interface with Box traits.
 *
 * @author Jamison Jiang
 */
public interface Box {

  String HORIZONTAL_ERROR = "Box's right wall must be greater than left wall";

  String VERTICAL_ERROR = "Box's down wall must be greater than up wall";

  /**
   * Returns the left border.
   *
   * @return left border
   */
  double getLeftBorder();

  /**
   * Returns the right border.
   *
   * @return right border
   */
  double getRightBorder();

  /**
   * Returns the up border.
   *
   * @return up border
   */
  double getUpBorder();

  /**
   * Returns the down border.
   *
   * @return down border
   */
  double getDownBorder();

  /**
   * Returns the height of box.
   *
   * @return height of box
   */
  default double getHeight() {
    return Math.abs(getUpBorder() - getDownBorder());
  }

  /**
   * Returns the width of box.
   *
   * @return width of box
   */
  default double getWidth() {
    return Math.abs(getLeftBorder() - getRightBorder());
  }

  /**
   * Returns the x-coordinate of box.
   *
   * @return x-coordinate of box
   */
  default double getX() {
    return (getLeftBorder() + getRightBorder()) / 2;
  }

  /**
   * Returns the y-coordinate of box.
   *
   * @return y-coordinate of box
   */
  default double getY() {
    return (getUpBorder() + getDownBorder()) / 2;
  }

  /**
   * Returns the left-up corner coordinate.
   *
   * @return left-up corner coordinate
   */
  default FlatPoint getLeftUp() {
    return new FlatPoint(getLeftBorder(), getUpBorder());
  }

  /**
   * Returns the right-down corner coordinate.
   *
   * @return right-down corner coordinate
   */
  default FlatPoint getRightDown() {
    return new FlatPoint(getRightBorder(), getDownBorder());
  }

  /**
   * Check the box whether is valid. Valid means that the left bound must be smaller than the right
   * bound, and the upper bound must be smaller than the lower bound.
   *
   * @throws IllegalArgumentException The following two scenarios will report an error:
   *                                  <ul>
   *                                    <li>leftBorder greater than rightBorder
   *                                    <li>upBorder greater than downBorder
   *                                  </ul>
   */
  default void check() {
    Asserts.illegalArgument(getLeftBorder() > getRightBorder(), HORIZONTAL_ERROR);
    Asserts.illegalArgument(getUpBorder() > getDownBorder(), VERTICAL_ERROR);
  }

  /**
   * Returns coordinate whether in box area.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @return true - in box area
   *         false - not in box area
   */
  default boolean in(double x, double y) {
    return x >= getLeftBorder() - 0.1 && x <= getRightBorder() + 0.1
        && y >= getUpBorder() - 0.1 && y <= getDownBorder() + 0.1;
  }
}
