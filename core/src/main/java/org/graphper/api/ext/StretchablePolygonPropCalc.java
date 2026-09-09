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

import java.util.ArrayList;
import java.util.List;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * Polygon geometry whose horizontal and vertical radii scale independently.
 *
 * @author Jamison Jiang
 */
public class StretchablePolygonPropCalc extends RegularPolylinePropCalc {

  private static final long serialVersionUID = 7821043182324011156L;

  private final boolean regularPoints;

  /**
   * Creates a stretchable polygon.
   *
   * @param side number of polygon sides
   */
  public StretchablePolygonPropCalc(int side) {
    this(side, false);
  }

  /**
   * Creates polygon geometry with independently selected point scaling.
   *
   * @param side number of polygon sides
   * @param regularPoints whether points must remain on a circle
   */
  public StretchablePolygonPropCalc(int side, boolean regularPoints) {
    super(side);
    Asserts.illegalArgument(side < 3, "A polygon must have at least three sides");
    this.regularPoints = regularPoints;
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return minContainerSize(innerHeight, innerWidth, 0, 0);
  }

  @Override
  public FlatPoint minContainerSize(
      double innerHeight, double innerWidth, double minHeight, double minWidth) {
    if (innerHeight == 0 || innerWidth == 0) {
      return new FlatPoint(minHeight, minWidth);
    }

    // Preserve the established size when it already contains the label; only enlarge cases where
    // that estimate under-sized odd/custom polygons.
    double height;
    double width;
    double diagonalHeight = innerHeight * Math.sqrt(2);
    if (minHeight > diagonalHeight) {
      height = innerHeight;
      double ratio = innerHeight / minHeight;
      width = innerWidth * Math.sqrt(1D / (1D - ratio * ratio));
    } else {
      height = diagonalHeight;
      width = innerWidth * Math.sqrt(2);
    }
    double polygonInset = Math.cos(Math.PI / getSide());
    height = Math.max(height / polygonInset, minHeight);
    width = Math.max(width / polygonInset, minWidth);
    DefaultBox box = new DefaultBox(-width / 2, width / 2, -height / 2, height / 2);
    FlatPoint[] corners = innerCorners(innerHeight, innerWidth);

    if (contains(box, corners)) {
      return new FlatPoint(height, width);
    }

    double scale = 1;
    while (!contains(box, corners) && scale < 1_000_000) {
      scale *= 2;
      resize(box, height * scale, width * scale);
    }

    double low = scale / 2;
    double high = scale;
    for (int i = 0; i < 60; i++) {
      double mid = (low + high) / 2;
      resize(box, height * mid, width * mid);
      if (contains(box, corners)) {
        high = mid;
      } else {
        low = mid;
      }
    }
    return new FlatPoint(height * high * (1 + 1e-10), width * high * (1 + 1e-10));
  }

  private FlatPoint[] innerCorners(double height, double width) {
    return new FlatPoint[] {
      new FlatPoint(-width / 2, -height / 2),
      new FlatPoint(width / 2, -height / 2),
      new FlatPoint(width / 2, height / 2),
      new FlatPoint(-width / 2, height / 2)
    };
  }

  private boolean contains(DefaultBox box, FlatPoint[] corners) {
    for (FlatPoint corner : corners) {
      if (!in(box, corner)) {
        return false;
      }
    }
    return true;
  }

  private void resize(DefaultBox box, double height, double width) {
    box.setLeftBorder(-width / 2);
    box.setRightBorder(width / 2);
    box.setUpBorder(-height / 2);
    box.setDownBorder(height / 2);
  }

  @Override
  public List<FlatPoint> calcPoints(Box box) {
    Asserts.nullArgument(box, "box");
    if (regularPoints) {
      return super.calcPoints(box);
    }
    List<FlatPoint> unitPoints = new ArrayList<>(getSide());
    double perSideArc = 2 * Math.PI / getSide();
    double arc = getStartArc(perSideArc);
    double maxX = 0;
    double maxY = 0;
    for (int i = 0; i < getSide(); i++) {
      double x = Math.cos(arc);
      double y = -Math.sin(arc);
      unitPoints.add(new FlatPoint(x, y));
      maxX = Math.max(maxX, Math.abs(x));
      maxY = Math.max(maxY, Math.abs(y));
      arc += perSideArc;
    }

    double xScale = box.getWidth() / (2 * maxX);
    double yScale = box.getHeight() / (2 * maxY);
    List<FlatPoint> points = new ArrayList<>(getSide());
    for (FlatPoint point : unitPoints) {
      points.add(
          new FlatPoint(box.getX() + point.getX() * xScale, box.getY() + point.getY() * yScale));
    }
    return points;
  }
}
