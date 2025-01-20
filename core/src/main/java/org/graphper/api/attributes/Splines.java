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

import org.graphper.api.Line.LineBuilder;

/**
 * Line routing mode.
 *
 * @author Jamison Jiang
 */
public enum Splines {

  /**
   * Line routing is not performed and all lines are hidden.
   */
  NONE,

  /**
   * Use boxes to avoid other elements in the graph for line segment routing, and finally smooth the
   * connected corner between different line segments. Use this {@link LineBuilder#radian(double)}
   * to adjust the smoothness of line segment joins.
   */
  ROUNDED,

  /**
   * Use boxes to avoid other elements in the graph for line segment routing. Use a smooth curve
   * through the all endpoints of all line segments.
   */
  SPLINE,

  /**
   * Connect a line directly between two nodes.
   */
  LINE,

  /**
   * Use boxes to avoid other elements in the graph for line segment routing, And draw the edges
   * using a series of line segments.
   */
  POLYLINE,

  /**
   * Draw lines using horizontal or vertical segments.
   */
  ORTHO;
}
