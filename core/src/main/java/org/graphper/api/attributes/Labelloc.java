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
import org.graphper.util.Asserts;
import org.graphper.api.ext.LabelPositionCalc;
import org.graphper.api.ext.LabelPositionCalc.BottomLabelPositionCalc;
import org.graphper.api.ext.LabelPositionCalc.TopLabelPositionCalc;
import org.graphper.api.ext.LabelPositionCalc.VerCenterLabelPositionCalc;

/**
 * Used to control the vertical position of label.
 *
 * @author Jamison Jiang
 */
public enum Labelloc {

  /**
   * Label aligns to the top.
   */
  TOP(new TopLabelPositionCalc()),

  /**
   * Label vertical centered.
   */
  CENTER(new VerCenterLabelPositionCalc()),

  /**
   * Label aligns to the bottom.
   */
  BOTTOM(new BottomLabelPositionCalc());

  private final LabelPositionCalc labelPositionCalc;

  Labelloc(LabelPositionCalc labelPositionCalc) {
    Asserts.nullArgument(labelPositionCalc, "labelPositionCalc");
    this.labelPositionCalc = labelPositionCalc;
  }

  /**
   * Returns the y coordinate after mobilization.
   *
   * @param upperLeft  left upper conner point
   * @param lowerRight right down conner point
   * @param labelSize  label size
   * @return y coordinate after mobilization
   * @throws NullPointerException any parameter is null
   */
  public double getY(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
    return labelPositionCalc.centerPos(upperLeft, lowerRight, labelSize);
  }
}
