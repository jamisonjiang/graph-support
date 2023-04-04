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
import org.graphper.api.attributes.NodeShapeEnum;

public class NotePropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = -2681885778176559296L;

  public static final int RIGHT_UP_LEN = 6;

  private static final int HALF_RIGHT_UP_LEN = RIGHT_UP_LEN / 2;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return new FlatPoint(innerHeight, innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    if (box.getHeight() <= RIGHT_UP_LEN || box.getWidth() <= RIGHT_UP_LEN) {
      return NodeShapeEnum.RECT.in(box, point);
    }

    if (inLeftBox(box, point) || inRightBox(box, point)) {
      return true;
    }
    return inRightUpCorner(box, point);
  }

  private boolean inLeftBox(Box box, FlatPoint point) {
    double leftBoxWidth = box.getWidth() - RIGHT_UP_LEN;
    return Math.abs(box.getX() - HALF_RIGHT_UP_LEN - point.getX()) <= leftBoxWidth / 2
        && Math.abs(box.getY() - point.getY()) <= box.getHeight() / 2;
  }

  private boolean inRightBox(Box box, FlatPoint point) {
    double rightBoxHeight = box.getHeight() - RIGHT_UP_LEN;
    return Math.abs(box.getRightBorder() - HALF_RIGHT_UP_LEN - point.getX()) <= HALF_RIGHT_UP_LEN
        && Math.abs(box.getY() + HALF_RIGHT_UP_LEN - point.getY()) <= rightBoxHeight / 2;
  }

  private boolean inRightUpCorner(Box box, FlatPoint point) {
    double rightAngleX = box.getRightBorder() - RIGHT_UP_LEN;
    double rightAngleY = box.getUpBorder() + RIGHT_UP_LEN;
    return Vectors.inAngle(rightAngleX, rightAngleY,
                           rightAngleX, box.getUpBorder(),
                           box.getRightBorder(), rightAngleY,
                           point.getX(), point.getY())
        && Vectors.inAngle(box.getRightBorder(), rightAngleY,
                           box.getRightBorder(), rightAngleY,
                           rightAngleX, box.getUpBorder(),
                           point.getX(), point.getY());
  }
}
