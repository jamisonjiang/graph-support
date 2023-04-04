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

package org.graphper.api.attributes;

/**
 * An enumeration of the different shapes of the line's arrowhead.
 *
 * @author Jamison Jiang
 */
public enum ArrowShape {

  VEE(0.75, true),

  CURVE(1, false),

  BOX(0.75, true),

  DOT(0.75, true),

  NONE(1, false),

  NORMAL(1, true);

  ArrowShape(double clipRatio, boolean needFill) {
    this.clipRatio = clipRatio;
    this.needFill = needFill;
  }

  private final double clipRatio;

  private final boolean needFill;

  /**
   * Returns the basic size ratio of arrow shape.
   *
   * @return the basic size ratio
   */
  public double getClipRatio() {
    return clipRatio;
  }

  /**
   * Returns whether this arrow's shape needs filled.
   *
   * @return <tt>true</tt> if arrow's shape need filled
   */
  public boolean isNeedFill() {
    return needFill;
  }
}
