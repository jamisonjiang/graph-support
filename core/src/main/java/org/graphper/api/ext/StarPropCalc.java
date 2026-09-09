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
import org.graphper.util.Asserts;
import org.graphper.util.ValueUtils;

/**
 * Geometry of the five pointed star.
 *
 * <p>The rendered outline is the simple decagon whose vertices alternate between a tip on the box
 * ellipse and an inward notch at {@code 1 / IN_OUT_RATIO} of it, stepping by {@link #UNIT_ARC} from
 * {@link #START_ARC}. Every predicate here is evaluated against exactly that polygon, so
 * containment answers and the drawn shape cannot drift apart.
 */
public class StarPropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 1146903395637968118L;

  public static final double START_ARC = Math.PI / 2;

  public static final double UNIT_ARC = Math.PI / 5;

  /**
   * Angular step between two consecutive tips.
   *
   * @deprecated Retained for source and binary compatibility. The geometry below walks the rendered
   *     decagon tip by notch with {@link #UNIT_ARC} instead.
   */
  @Deprecated public static final double AXIS_ARC = 2 * UNIT_ARC;

  public static final double IN_OUT_RATIO =
      ValueUtils.cos(36) + ValueUtils.cos(54) * ValueUtils.tan(72);

  /** Number of vertices of the rendered outline: five tips interleaved with five notches. */
  private static final int VERTEX_NUM = 10;

  /**
   * Relative width of the bracket the container search narrows down to. Bisecting a bracket of
   * {@code [s, 2s]} down to this width takes {@link #MAX_SEARCH_STEPS} halvings, and going further
   * cannot change the returned size in double precision.
   */
  private static final double SCALE_TOLERANCE = 1e-10;

  private static final int MAX_SEARCH_STEPS =
      (int) Math.ceil(Math.log(1 / SCALE_TOLERANCE) / Math.log(2));

  /** Upper bound of the growth factor applied to the estimated container before giving up. */
  private static final double MAX_SCALE = 1_048_576;

  /**
   * Points within this fraction of the box size of the outline count as inside, so that a point
   * placed exactly on the border - a compass port, for instance - is not reported outside by
   * rounding alone.
   */
  private static final double BORDER_TOLERANCE = 1e-9;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return minContainerSize(innerHeight, innerWidth, 0, 0);
  }

  /**
   * Smallest star container that provably covers the inscribed rectangle.
   *
   * <p>The closed form below fixes the aspect ratio of the container and is exact for most inner
   * rectangles, but for some proportions the rectangle pokes through one of the two edges that meet
   * at an inward notch. Those cases are repaired by growing the estimate uniformly, which is sound
   * because the star is star shaped about its centre: {@code s * Star} is contained in {@code s' *
   * Star} for every {@code s' > s}, so containment is monotone in the growth factor and can be
   * bracketed by bisection.
   */
  @Override
  public FlatPoint minContainerSize(
      double innerHeight, double innerWidth, double minHeight, double minWidth) {
    FlatPoint estimate = estimateContainerSize(innerHeight, innerWidth);
    double height = Math.max(estimate.getHeight(), minHeight);
    double width = Math.max(estimate.getWidth(), minWidth);
    if (height <= 0 || width <= 0) {
      return new FlatPoint(height, width);
    }

    double[] vertices = new double[2 * VERTEX_NUM];
    if (covers(vertices, height, width, innerHeight, innerWidth)) {
      return new FlatPoint(height, width);
    }

    double scale = 1;
    while (scale < MAX_SCALE
        && !covers(vertices, height * scale, width * scale, innerHeight, innerWidth)) {
      scale *= 2;
    }

    double low = scale / 2;
    double high = scale;
    for (int i = 0; i < MAX_SEARCH_STEPS && high - low > SCALE_TOLERANCE * high; i++) {
      double mid = (low + high) / 2;
      if (covers(vertices, height * mid, width * mid, innerHeight, innerWidth)) {
        high = mid;
      } else {
        low = mid;
      }
    }
    return new FlatPoint(height * high, width * high);
  }

  /**
   * Closed form container estimate. Keeps the intrinsic proportion of the shape and is the size
   * this shape has always reported; {@link #minContainerSize(double, double, double, double)} only
   * grows it when it does not actually cover the rectangle.
   */
  private FlatPoint estimateContainerSize(double innerHeight, double innerWidth) {
    double alpha = Math.PI / 10;
    double radiusX = innerWidth / (2 * Math.cos(alpha));
    double radiusY = innerHeight / (Math.sin(alpha) + Math.sin(3 * alpha));
    double radius0 = Math.max(radiusX, radiusY);
    double radius =
        radius0
            * Math.sin(4 * alpha)
            * Math.cos(2 * alpha)
            / (Math.cos(alpha) * Math.cos(4 * alpha));
    return new FlatPoint(radius * (1 + Math.sin(3 * alpha)), 2 * radius * Math.cos(alpha));
  }

  /**
   * Writes the vertices of the rendered outline into {@code vertices} as consecutive x, y pairs.
   */
  private static void vertices(
      double[] vertices, double centerX, double centerY, double height, double width) {
    double outerRadiusX = width / 2;
    double outerRadiusY = height / 2;
    double innerRadiusX = outerRadiusX / IN_OUT_RATIO;
    double innerRadiusY = outerRadiusY / IN_OUT_RATIO;

    double arc = START_ARC;
    for (int i = 0; i < VERTEX_NUM; i++) {
      boolean tip = i % 2 == 0;
      double radiusX = tip ? outerRadiusX : innerRadiusX;
      double radiusY = tip ? outerRadiusY : innerRadiusY;
      vertices[i * 2] = centerX + Math.cos(arc) * radiusX;
      vertices[i * 2 + 1] = centerY - Math.sin(arc) * radiusY;
      arc += UNIT_ARC;
    }
  }

  /**
   * Whether the centred rectangle {@code innerWidth x innerHeight} lies completely inside the star
   * of the centred box {@code width x height}.
   *
   * <p>Exact, and in particular exact around the inward notches, where sampling the rectangle
   * border is not: the notch can cut a band out of a rectangle edge that is narrower than any fixed
   * sample step. The rectangle is convex and the outline is a simple closed polygon, so if no
   * outline edge meets the closed rectangle then the whole rectangle sits in one component of the
   * complement of the outline, and the centre decides which one.
   *
   * @param vertices scratch buffer of {@code 2 * VERTEX_NUM} doubles, overwritten by this call
   */
  private static boolean covers(
      double[] vertices, double height, double width, double innerHeight, double innerWidth) {
    if (height <= 0 || width <= 0) {
      return false;
    }

    vertices(vertices, 0, 0, height, width);
    double halfInnerWidth = Math.max(innerWidth, 0) / 2;
    double halfInnerHeight = Math.max(innerHeight, 0) / 2;
    for (int i = 0, j = VERTEX_NUM - 1; i < VERTEX_NUM; j = i++) {
      if (meetsRectangle(
          vertices[j * 2],
          vertices[j * 2 + 1],
          vertices[i * 2],
          vertices[i * 2 + 1],
          halfInnerWidth,
          halfInnerHeight)) {
        return false;
      }
    }
    return in(vertices, 0, 0, 0);
  }

  /**
   * Whether the segment meets the closed rectangle centred on the origin, by clipping the segment
   * against the two rectangle slabs.
   */
  private static boolean meetsRectangle(
      double x1, double y1, double x2, double y2, double halfWidth, double halfHeight) {
    double enter = 0;
    double exit = 1;

    double dx = x2 - x1;
    if (dx == 0) {
      if (x1 < -halfWidth || x1 > halfWidth) {
        return false;
      }
    } else {
      double t1 = (-halfWidth - x1) / dx;
      double t2 = (halfWidth - x1) / dx;
      enter = Math.max(enter, Math.min(t1, t2));
      exit = Math.min(exit, Math.max(t1, t2));
    }

    double dy = y2 - y1;
    if (dy == 0) {
      if (y1 < -halfHeight || y1 > halfHeight) {
        return false;
      }
    } else {
      double t1 = (-halfHeight - y1) / dy;
      double t2 = (halfHeight - y1) / dy;
      enter = Math.max(enter, Math.min(t1, t2));
      exit = Math.min(exit, Math.max(t1, t2));
    }

    return enter <= exit;
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    Asserts.nullArgument(box, "box");
    Asserts.nullArgument(point, "point");

    double[] vertices = new double[2 * VERTEX_NUM];
    vertices(vertices, box.getX(), box.getY(), box.getHeight(), box.getWidth());
    double tolerance = BORDER_TOLERANCE * Math.max(box.getHeight(), box.getWidth());
    return in(vertices, point.getX(), point.getY(), tolerance);
  }

  /**
   * Crossing number test against the rendered outline, with points within {@code tolerance} of the
   * outline counted as inside.
   */
  private static boolean in(double[] vertices, double x, double y, double tolerance) {
    boolean inside = false;
    for (int i = 0, j = VERTEX_NUM - 1; i < VERTEX_NUM; j = i++) {
      double xi = vertices[i * 2];
      double yi = vertices[i * 2 + 1];
      double xj = vertices[j * 2];
      double yj = vertices[j * 2 + 1];

      if (tolerance > 0 && distanceToSegment(xj, yj, xi, yi, x, y) <= tolerance) {
        return true;
      }
      if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
        inside = !inside;
      }
    }
    return inside;
  }

  private static double distanceToSegment(
      double x1, double y1, double x2, double y2, double x, double y) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double squaredLen = dx * dx + dy * dy;
    double ratio = 0;
    if (squaredLen > 0) {
      ratio = Math.max(0, Math.min(1, ((x - x1) * dx + (y - y1) * dy) / squaredLen));
    }
    double nearestX = x1 + ratio * dx;
    double nearestY = y1 + ratio * dy;
    return Math.sqrt((x - nearestX) * (x - nearestX) + (y - nearestY) * (y - nearestY));
  }
}
