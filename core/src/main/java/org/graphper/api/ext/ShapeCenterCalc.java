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

/**
 * The description function of the node center of gravity. Use a rectangular box as a container for
 * placing nodes, different node shapes may have different centers of gravity. This method describes
 * where the center of gravity is using the distance from the left, right, top, bottom borders.
 *
 * @author Jamison Jiang
 */
public interface ShapeCenterCalc {

  /**
   * Returns the distance from the center of gravity to the left border.
   *
   * @param width box width
   * @return distance from the center of gravity to the left border
   */
  double leftWidth(Double width);

  /**
   * Returns the distance from the center of gravity to the right border.
   *
   * @param width box width
   * @return distance from the center of gravity to the right border
   */
  double rightWidth(Double width);

  /**
   * Returns the distance from the center of gravity to the top border.
   *
   * @param height box height
   * @return distance from the center of gravity to the top border
   */
  double topHeight(Double height);

  /**
   * Returns the distance from the center of gravity to the bottom border.
   *
   * @param height box height
   * @return distance from the center of gravity to the bottom border
   */
  double bottomHeight(Double height);
}
