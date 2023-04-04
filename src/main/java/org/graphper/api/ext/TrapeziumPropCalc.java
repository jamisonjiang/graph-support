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
    double leftTopX = box.getLeftBorder() + box.getWidth() / 4;
    double rightTopX = box.getRightBorder() - box.getWidth() / 4;

    return Vectors.inAngle(left, down, leftTopX,
                           up, right, down, point.getX(), point.getY())
        && Vectors.inAngle(right, down, rightTopX,
                           up, left, down, point.getX(), point.getY())
        && Vectors.inAngle(leftTopX, up, left,
                           down, rightTopX, up, point.getX(), point.getY());
  }
}
