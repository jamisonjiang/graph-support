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

import org.graphper.api.ext.DefaultBox;

public class Rectangle extends DefaultBox {

  public Rectangle() {
    init();
  }

  public void init() {
    leftBorder = Double.MAX_VALUE;
    rightBorder = -Double.MAX_VALUE;
    upBorder = Double.MAX_VALUE;
    downBorder = -Double.MAX_VALUE;
  }

  public void flip() {
    double tmp = leftBorder;
    leftBorder = upBorder;
    upBorder = tmp;
    tmp = rightBorder;
    rightBorder = downBorder;
    downBorder = tmp;
  }

  public void updateXAxisRange(double x) {
    leftBorder = Math.min(x, leftBorder);
    rightBorder = Math.max(x, rightBorder);
  }

  public void updateYAxisRange(double y) {
    upBorder = Math.min(y, upBorder);
    downBorder = Math.max(y, downBorder);
  }
}
