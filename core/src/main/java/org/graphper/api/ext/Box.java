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
import org.graphper.util.EnvProp;

/**
 * Public interface with Box traits.
 *
 * @author Jamison Jiang
 */
public interface Box {

  String HORIZONTAL_ERROR = "Box's right wall must be greater than left wall";

  String VERTICAL_ERROR = "Box's down wall must be greater than up wall";

  String NOT_POSITIVE_BOX = "Box don't have positive area";

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
   * Returns the left-down corner coordinate.
   *
   * @return left-down corner coordinate
   */
  default FlatPoint getLeftDown() {
    return new FlatPoint(getLeftBorder(), getDownBorder());
  }

  /**
   * Returns the right-up corner coordinate.
   *
   * @return right-up corner coordinate
   */
  default FlatPoint getRightUp() {
    return new FlatPoint(getRightBorder(), getUpBorder());
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
    if (EnvProp.ignoreBoxCheck()) {
      return;
    }

    Asserts.illegalArgument(!positive(), NOT_POSITIVE_BOX);
  }

  /**
   * Returns true if box have positive area.
   *
   * @return true if box have positive area
   */
  default boolean positive() {
    return getLeftBorder() <= getRightBorder() && getUpBorder() <= getDownBorder();
  }

  /**
   * Returns true if point in box area.
   *
   * @param point flat point
   * @return true - point in box area false - point not in box area
   */
  default boolean in(FlatPoint point) {
    if (point == null) {
      return false;
    }
    return in(point.getX(), point.getY());
  }

  /**
   * Returns true if coordinate in box area.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @return true - in box area false - not in box area
   */
  default boolean in(double x, double y) {
    if (!positive()) {
      return false;
    }
    return x >= getLeftBorder() + 0.01 && x <= getRightBorder() - 0.01
        && y >= getUpBorder() + 0.01 && y <= getDownBorder() - 0.01;
  }

  /**
   * Calculates the area of the box.
   *
   * @return the area of the box
   */
  default double getArea() {
    return getWidth() * getHeight();
  }

  /**
   * Calculates the distance between the centers of this box and another box.
   *
   * @param box the other box
   * @return the distance between the centers of the two boxes
   * @throws IllegalArgumentException if the other box is null
   */
  default double distanceTo(Box box) {
    Asserts.illegalArgument(box == null, "The other box must not be null");

    double deltaX = getX() - box.getX();
    double deltaY = getY() - box.getY();
    return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
  }

  /**
   * Checks if this box overlaps with another box.
   *
   * @param box the box to check for overlap
   * @return {@code true} if the boxes overlap, {@code false} otherwise
   */
  default boolean isOverlap(Box box) {
    if (box == null) {
      return false;
    }
    double deltaX = Math.abs(getX() - box.getX());
    double deltaY = Math.abs(getY() - box.getY());
    return deltaX <= wd2() + box.wd2() && deltaY <= ht2() + box.ht2();
  }

  /**
   * Returns half the width of the box.
   *
   * @return half the width of the box
   */
  default double wd2() {
    return getWidth() / 2;
  }

  /**
   * Returns half the height of the box.
   *
   * @return half the height of the box
   */
  default double ht2() {
    return getHeight() / 2;
  }
}
