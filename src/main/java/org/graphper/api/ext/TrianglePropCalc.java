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

  private static final long serialVersionUID = 5056940900129881225L;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    double a = 2 * Math.sqrt(innerWidth * innerWidth + 4 * innerHeight * innerHeight);
    double h = ((3 * innerHeight * innerWidth) / a) + 1.5 * innerHeight;
    h = Math.max(h, 2 * innerHeight);
    return new FlatPoint(h, h * innerWidth / innerHeight);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    double left = box.getX() - box.getWidth() / 2;
    double right = box.getX() + box.getWidth() / 2;
    double top = box.getY() - box.getHeight() / 2;
    double bottom = box.getY() + box.getHeight() / 2;

    return Vectors.inAngle(left, bottom, box.getX(),
                           top, right, bottom, point.getX(), point.getY())
        && Vectors.inAngle(right, bottom, box.getX(),
                           top, right, bottom, point.getX(), point.getY());
  }

  @Override
  public FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    box.check();

    double y = box.getUpBorder() + 1.5 * labelSize.getHeight();
    return new FlatPoint(box.getX(), y);
  }
}
