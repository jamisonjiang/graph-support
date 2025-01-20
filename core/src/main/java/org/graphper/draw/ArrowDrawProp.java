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

package org.graphper.draw;

import java.io.Serializable;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * Arrow's rendering description object.
 *
 * @author Jamison Jiang
 */
public class ArrowDrawProp implements Serializable {

  private static final long serialVersionUID = 9190033726546148926L;

  private final FlatPoint axisBegin;

  private final FlatPoint axisEnd;

  private final boolean isHead;

  public ArrowDrawProp(boolean isHead, FlatPoint axisBegin, FlatPoint axisEnd) {
    Asserts.nullArgument(axisBegin, "axisBegin");
    Asserts.nullArgument(axisEnd, "axisEnd");
    this.axisBegin = axisBegin;
    this.axisEnd = axisEnd;
    this.isHead = isHead;
  }

  /**
   * Returns the starting point of the axis of symmetry of the arrow.
   *
   * @return starting point of axis of arrow
   */
  public FlatPoint getAxisBegin() {
    return axisBegin;
  }

  /**
   * Returns the end point of the axis of symmetry of the arrow.
   *
   * @return end point of axis of arrow
   */
  public FlatPoint getAxisEnd() {
    return axisEnd;
  }

  /**
   * Returns whether the current arrow is the head arrow.
   *
   * @return <tt>true</tt> if current arrow is head arrow
   */
  public boolean isHead() {
    return isHead;
  }

  @Override
  public String toString() {
    return "ArrowDrawProp{" +
        "axisBegin=" + axisBegin +
        ", axisEnd=" + axisEnd +
        '}';
  }
}
