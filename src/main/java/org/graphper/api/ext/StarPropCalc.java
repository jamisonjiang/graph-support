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
import org.graphper.util.ValueUtils;

public class StarPropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 1146903395637968118L;

  public static final double START_ARC = Math.PI / 2;

  public static final double UNIT_ARC = Math.PI / 5;

  public static final double AXIS_ARC = 2 * UNIT_ARC;

  public static final double IN_OUT_RATIO = ValueUtils.cos(36)
      + ValueUtils.cos(54) * ValueUtils.tan(72);

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    double l = Math.max(innerHeight, innerWidth) / 2;
    double radius = (l / (Math.pow(ValueUtils.cos(36), 2))) * IN_OUT_RATIO;
    return new FlatPoint(2 * radius, 2 * radius);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    double radius = box.getHeight() / 2;

    double arc = StarPropCalc.START_ARC;
    FlatPoint[] points = new FlatPoint[5];
    for (int i = 0; i < 5; i++) {
      points[i] = new FlatPoint(box.getX() + Math.cos(arc) * radius,
                                box.getY() - Math.sin(arc) * radius);
      arc += StarPropCalc.AXIS_ARC;
    }

    return inCheckTriangle(0, points, point)
        || inCheckTriangle(1, points, point)
        || inCheckTriangle(2, points, point);
  }

  private boolean inCheckTriangle(int idx, FlatPoint[] points, FlatPoint target) {
    FlatPoint left = cornerAdj(idx, points, false);
    FlatPoint right = cornerAdj(idx, points, true);
    if (!Vectors.inAngle(points[idx], left, right, target)) {
      return false;
    }

    int leftAdjIdx = getAdjIdx(idx, false);
    return Vectors.inAngle(left, cornerAdj(leftAdjIdx, points, false),
                           cornerAdj(leftAdjIdx, points, true), target);
  }

  FlatPoint cornerAdj(int idx, FlatPoint[] points, boolean clockwise) {
    int adj;
    adj = getAdjIdx(idx, clockwise);

    return points[adj];
  }

  private int getAdjIdx(int idx, boolean clockwise) {
    int adj;
    if (clockwise) {
      adj = idx - 2;
      adj = adj < 0 ? 5 + adj : adj;
    } else {
      adj = (idx + 2) % 5;
    }
    return adj;
  }
}
