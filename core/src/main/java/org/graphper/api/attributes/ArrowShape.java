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
 * Fixed shapes for a line's head or tail arrow. Hollow variants have an unfilled interior.
 * Composite arrows and left/right modifiers are not supported.
 *
 * @author Jamison Jiang
 */
public enum ArrowShape {
  VEE(0.75, true),

  CURVE(1, false),

  BOX(0.75, true),

  DOT(0.75, true),

  NONE(1, false),

  NORMAL(1, true),

  DIAMOND(1.2, true),

  INV(1, true),

  TEE(0.5, true),

  CROW(1, true),

  ICURVE(1, false),

  ONORMAL(1, false),

  OINV(1, false),

  OBOX(0.75, false),

  ODOT(0.75, false),

  ODIAMOND(1.2, false);

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
