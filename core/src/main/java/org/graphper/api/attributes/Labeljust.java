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

import org.graphper.def.FlatPoint;
import org.graphper.api.ext.LabelPositionCalc;
import org.graphper.api.ext.LabelPositionCalc.HorCenterLabelPositionCalc;
import org.graphper.api.ext.LabelPositionCalc.LeftLabelPositionCalc;
import org.graphper.api.ext.LabelPositionCalc.RightLabelPositionCalc;

/**
 * Used to control the horizontal position of label.
 *
 * @author Jamison Jiang
 */
public enum Labeljust {

  /**
   * Label aligns to the left.
   */
  LEFT(new LeftLabelPositionCalc()),

  /**
   * Label horizontal centered.
   */
  CENTER(new HorCenterLabelPositionCalc()),

  /**
   * Label aligns to the right.
   */
  RIGHT(new RightLabelPositionCalc());

  private final LabelPositionCalc labelPositionCalc;

  Labeljust(LabelPositionCalc labelPositionCalc) {
    this.labelPositionCalc = labelPositionCalc;
  }

  /**
   * Returns the x coordinate after mobilization.
   *
   * @param upperLeft  left upper conner point
   * @param lowerRight right down conner point
   * @param labelSize  label size
   * @return x coordinate after mobilization
   * @throws NullPointerException any parameter is null
   */
  public double getX(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
    return labelPositionCalc.centerPos(upperLeft, lowerRight, labelSize);
  }
}
