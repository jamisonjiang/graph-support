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

import java.io.Serializable;
import org.graphper.util.Asserts;

public class RatioPortPosition implements PortPosition, Serializable {

  private static final long serialVersionUID = -1767859727952040562L;

  private final double xRatio;

  private final double yRatio;

  public RatioPortPosition(double xRatio, double yRatio) {
    this.xRatio = xRatio;
    this.yRatio = yRatio;
  }

  @Override
  public double horOffset(Box box) {
    Asserts.nullArgument(box, "box");
    return box.getWidth() * xRatio;
  }

  @Override
  public double verOffset(Box box) {
    Asserts.nullArgument(box, "box");
    return box.getHeight() * yRatio;
  }

  public double getxRatio() {
    return xRatio;
  }

  public double getyRatio() {
    return yRatio;
  }
}
