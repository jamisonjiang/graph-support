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

public class TrianglePropCalc implements ShapePropCalc, Serializable {

  private final boolean positive;

  private static final long serialVersionUID = 5056940900129881225L;

  public TrianglePropCalc(boolean positive) {
    this.positive = positive;
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return new FlatPoint(2 * innerHeight, 3 * innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    double left = box.getX() - box.getWidth() / 2;
    double right = box.getX() + box.getWidth() / 2;
    double top = box.getY() - box.getHeight() / 2;
    double bottom = box.getY() + box.getHeight() / 2;

    if (positive) {
      return Vectors.inAngle(left, bottom, box.getX(), top, right, bottom,
                             point.getX(), point.getY())
          && Vectors.inAngle(right, bottom, left, bottom, box.getX(), top,
                             point.getX(), point.getY());
    }

    return Vectors.inAngle(left, top, box.getX(), bottom, right, top, point.getX(), point.getY())
        && Vectors.inAngle(right, top, left, top, box.getX(), bottom, point.getX(), point.getY());
  }

  @Override
  public FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    box.check();

    double y;
    if (positive) {
      y = box.getDownBorder() - labelSize.getHeight();
    } else {
      y = box.getUpBorder() + labelSize.getHeight();
    }

    return new FlatPoint(box.getX(), y);
  }

  public boolean isPositive() {
    return positive;
  }
}
