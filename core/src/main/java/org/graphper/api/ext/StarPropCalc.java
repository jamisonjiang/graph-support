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
    double alpha = Math.PI / 10;
    double radiusX = innerWidth / (2 * Math.cos(alpha));
    double radiusY = innerHeight / (Math.sin(alpha) + Math.sin(3 * alpha));
    double radius0 = Math.max(radiusX, radiusY);
    double radius = radius0 * Math.sin(4 * alpha) * Math.cos(2 * alpha)
        / (Math.cos(alpha) * Math.cos(4 * alpha));
    return new FlatPoint(radius * (1 + Math.sin(3 * alpha)),
                         2 * radius * Math.cos(alpha));
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth,
                                    double minHeight, double minWidth) {
    FlatPoint size = minContainerSize(innerHeight, innerWidth);
    double width = Math.max(size.getWidth(), minWidth);
    double height = Math.max(size.getHeight(), minHeight);
    if (containsInnerRectangle(height, width, innerHeight, innerWidth)) {
      return new FlatPoint(height, width);
    }
    double scale = 1;
    while (!containsInnerRectangle(height * scale, width * scale, innerHeight, innerWidth)
        && scale < 1_000_000) {
      scale *= 2;
    }

    double low = scale / 2;
    double high = scale;
    for (int i = 0; i < 60; i++) {
      double mid = (low + high) / 2;
      if (containsInnerRectangle(height * mid, width * mid, innerHeight, innerWidth)) {
        high = mid;
      } else {
        low = mid;
      }
    }
    return new FlatPoint(height * high * (1 + 1e-10), width * high * (1 + 1e-10));
  }

  private boolean containsInnerRectangle(double height, double width,
                                         double innerHeight, double innerWidth) {
    DefaultBox box = new DefaultBox(-width / 2, width / 2, -height / 2, height / 2);
    // A star is concave, so sample each rectangle edge rather than checking corners only.
    for (int i = 0; i <= 32; i++) {
      double ratio = i / 32D;
      double x = -innerWidth / 2 + innerWidth * ratio;
      double y = -innerHeight / 2 + innerHeight * ratio;
      if (!in(box, new FlatPoint(x, -innerHeight / 2))
          || !in(box, new FlatPoint(x, innerHeight / 2))
          || !in(box, new FlatPoint(-innerWidth / 2, y))
          || !in(box, new FlatPoint(innerWidth / 2, y))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    double xRadius = box.getWidth() / 2;
    double yRadius = box.getHeight() / 2;

    double arc = StarPropCalc.START_ARC;
    FlatPoint[] points = new FlatPoint[5];
    for (int i = 0; i < 5; i++) {
      points[i] = new FlatPoint(box.getX() + Math.cos(arc) * xRadius,
                                box.getY() - Math.sin(arc) * yRadius);
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
