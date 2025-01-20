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

import java.util.Objects;
import org.graphper.util.Asserts;

/**
 * The default implementation of {@link Box}.
 *
 * @author Jamison Jiang
 */
public class DefaultBox implements Box {

  protected double leftBorder;

  protected double rightBorder;

  protected double upBorder;

  protected double downBorder;

  public DefaultBox() {
  }

  public DefaultBox(double leftBorder, double rightBorder, double upBorder, double downBorder) {
    Asserts.illegalArgument(leftBorder > rightBorder, HORIZONTAL_ERROR);
    Asserts.illegalArgument(upBorder > downBorder, VERTICAL_ERROR);
    this.leftBorder = leftBorder;
    this.rightBorder = rightBorder;
    this.upBorder = upBorder;
    this.downBorder = downBorder;
  }

  @Override
  public double getLeftBorder() {
    return leftBorder;
  }

  @Override
  public double getRightBorder() {
    return rightBorder;
  }

  @Override
  public double getUpBorder() {
    return upBorder;
  }

  @Override
  public double getDownBorder() {
    return downBorder;
  }

  public void setLeftBorder(double leftBorder) {
    this.leftBorder = leftBorder;
  }

  public void setRightBorder(double rightBorder) {
    this.rightBorder = rightBorder;
  }

  public void setUpBorder(double upBorder) {
    this.upBorder = upBorder;
  }

  public void setDownBorder(double downBorder) {
    this.downBorder = downBorder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultBox routerBox = (DefaultBox) o;
    return Double.compare(routerBox.leftBorder, leftBorder) == 0 &&
        Double.compare(routerBox.rightBorder, rightBorder) == 0 &&
        Double.compare(routerBox.upBorder, upBorder) == 0 &&
        Double.compare(routerBox.downBorder, downBorder) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(leftBorder, rightBorder, upBorder, downBorder);
  }

  @Override
  public String toString() {
    return "DefaultBox{" +
        "leftBorder=" + leftBorder +
        ", rightBorder=" + rightBorder +
        ", upBorder=" + upBorder +
        ", downBorder=" + downBorder +
        '}';
  }
}
