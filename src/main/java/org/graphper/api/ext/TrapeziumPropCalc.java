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
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;

public class TrapeziumPropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 4151640946566130589L;

  private final boolean positive;

  public TrapeziumPropCalc(boolean positive) {
    this.positive = positive;
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return new FlatPoint(innerHeight, 2 * innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    double left = box.getLeftBorder();
    double right = box.getRightBorder();
    double up = box.getUpBorder();
    double down = box.getDownBorder();
    double leftX = box.getLeftBorder() + box.getWidth() / 4;
    double rightX = box.getRightBorder() - box.getWidth() / 4;

    if (positive) {
      return Vectors.inAngle(left, down, leftX, up, right, down, point.getX(), point.getY())
          && Vectors.inAngle(right, down, rightX, up, left, down, point.getX(), point.getY())
          && Vectors.inAngle(leftX, up, left, down, rightX, up, point.getX(), point.getY());
    }

    return Vectors.inAngle(left, up, leftX, down, right, up, point.getX(), point.getY())
        && Vectors.inAngle(right, up, rightX, down, left, up, point.getX(), point.getY())
        && Vectors.inAngle(leftX, down, left, up, rightX, down, point.getX(), point.getY());
  }
}
