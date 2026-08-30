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

package org.graphper.layout.dot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;

/**
 * Routes an explicit spline through the ordered {@link RouterBox} corridor of a single line.
 *
 * <p>The router boxes describe where an edge is allowed to travel. Treating the centre of every
 * routing-only virtual node as a point the curve must interpolate reproduces the jitter of the
 * crossing-minimised node chain as visible waves. Instead the corridor is pulled taut with a funnel
 * sweep, so the route only bends where the corridor actually forces it to, and each remaining bend
 * is replaced by the widest tangent continuous fillet that still fits inside the corridor.
 *
 * <p>Edge-label boxes remain part of the corridor, but their centres are not interpolation points.
 * Requiring the curve to hit a label centre adds two nearby bends around every label even when the
 * whole corridor admits one smooth route.
 *
 * @author Jamison Jiang
 */
class CorridorSpline {

  /** Fraction of a segment used for its Bezier handles. */
  private static final double HANDLE_RATIO = 1.0 / 3;

  private static final double EPS = 1e-6;

  /** Corridor containment tolerance, matching the rounding used when boxes are built. */
  private static final double IN_TOLERANCE = 1.5;

  /** Samples used when verifying that a segment stays inside the corridor. */
  private static final int CURVE_SAMPLES = 12;

  private static final int HANDLE_SHRINK_STEPS = 6;

  /**
   * Clearance kept between the route and a gate wall.
   *
   * <p>Router boxes only approximate the free area: {@code newTwoNodeRangeBox} widens a box to at
   * least its own node span, so a box can overlap the neighbour it is supposed to stop against when
   * the rank is tight. Riding the wall would then put the curve inside that neighbour, so gates give
   * up a little room on both sides. The inset never consumes more than a quarter of a gate, which
   * keeps genuinely narrow gates usable.
   */
  private static final double GATE_CLEARANCE = 8;

  private static final double GATE_CLEARANCE_RATIO = 0.25;

  /**
   * How far the smoothed route may leave the route the layout already chose.
   *
   * <p>A router box only records the free space of one rank; it says nothing about cluster borders.
   * An unbounded shortest corridor path is therefore free to cut diagonally through a cluster that
   * the virtual node chain deliberately routed around. The defect this class removes is local jitter
   * of that chain, which is small relative to the node separation, so the route is allowed to
   * straighten only within a short distance of the layout's own decision. That keeps the routing
   * topology the layout committed to while still discarding the jitter.
   */
  private static final double MAX_DEVIATION = 16;

  private final List<RouterBox> boxes;

  private final boolean vertical;

  /** The route chosen by the layout, as {@code {u, w}} pairs ordered by travel direction. */
  private List<double[]> reference;

  CorridorSpline(List<RouterBox> boxes, boolean vertical) {
    this.boxes = boxes;
    this.vertical = vertical;
  }

  /**
   * Routes the line described by the parameter through its corridor.
   *
   * @param param routing parameter produced by {@link BoxGuideLineRouter}
   * @return the routed curve, or null when the corridor cannot describe the route and the caller
   *         should fall back to the generic fitter
   */
  static MultiBezierCurve route(BoxGuideLineRouter.ThroughParam param) {
    List<RouterBox> boxes = param.lineRouterBoxes;
    List<BoxGuideLineRouter.ThroughPoint> through = param.throughPoints;
    if (boxes == null || boxes.size() < 2 || through == null || through.size() < 2) {
      return null;
    }

    BoxGuideLineRouter.ThroughPoint start = through.get(0);
    BoxGuideLineRouter.ThroughPoint end = through.get(through.size() - 1);
    int startBox = start.getBoxIndex();
    int endBox = end.getBoxIndex();
    if (startBox < 0 || endBox < 0 || startBox >= boxes.size() || endBox >= boxes.size()) {
      return null;
    }

    CorridorSpline corridor = new CorridorSpline(boxes, !param.isHorizontal);
    corridor.setReference(through);
    List<FlatPoint> path = corridor.tautPath(startBox, endBox, start, end);
    if (path == null || path.size() < 2) {
      return null;
    }
    MultiBezierCurve curves = corridor.smooth(path);
    if (curves == null || !corridor.inCorridor(curves)) {
      // The corridor could not be described as a legal smooth route. Leave the decision to the
      // generic fitter rather than emitting a curve that leaves the allowed area.
      return null;
    }
    return curves;
  }

  /** Verifies that every emitted segment stays inside the corridor. */
  boolean inCorridor(MultiBezierCurve curves) {
    for (ThirdOrderBezierCurve curve : curves) {
      if (!inCorridor(curve)) {
        return false;
      }
    }
    return true;
  }

  /** Pulls the corridor taut between the two endpoints. */
  List<FlatPoint> tautPath(int startBox, int endBox, FlatPoint start, FlatPoint end) {
    List<FlatPoint> path = pullTaut(startBox, endBox, start, end);
    return path == null ? null : dedupe(path);
  }

  /**
   * Funnel sweep across the portals shared by consecutive boxes.
   */
  private List<FlatPoint> pullTaut(int fromBox, int toBox, FlatPoint start, FlatPoint end) {
    List<double[]> portals = portals(fromBox, toBox, start, end);
    if (portals == null) {
      return null;
    }

    boolean forward = u(end) >= u(start);
    List<FlatPoint> result = new ArrayList<>();
    result.add(start);

    double[] apex = {w(start), u(start)};
    double[] left = apex;
    double[] right = apex;
    int apexIndex = 0;
    int leftIndex = 0;
    int rightIndex = 0;

    // The apex restart below rewinds the sweep, so bound the total work explicitly: every restart
    // must consume a portal, and a corridor can never need more than a few passes per gate.
    int budget = 4 * portals.size() + 8;
    for (int i = 1; i < portals.size(); i++) {
      if (--budget < 0) {
        break;
      }
      double[] portal = portals.get(i);
      double[] l = {portal[1], portal[0]};
      double[] r = {portal[2], portal[0]};
      if (!forward) {
        // Travelling towards decreasing u mirrors the handedness of the funnel.
        double[] swap = l;
        l = r;
        r = swap;
      }

      if (cross(apex, right, r) >= 0) {
        if (same(apex, right) || cross(apex, left, r) < 0) {
          right = r;
          rightIndex = i;
        } else {
          if (leftIndex <= apexIndex) {
            // No portal was consumed, so rewinding cannot make progress.
            break;
          }
          double[] corner = point(left);
          result.add(new FlatPoint(corner[0], corner[1]));
          apex = left;
          apexIndex = leftIndex;
          left = apex;
          right = apex;
          rightIndex = apexIndex;
          i = apexIndex;
          continue;
        }
      }

      if (cross(apex, left, l) <= 0) {
        if (same(apex, left) || cross(apex, right, l) > 0) {
          left = l;
          leftIndex = i;
        } else {
          if (rightIndex <= apexIndex) {
            break;
          }
          double[] corner = point(right);
          result.add(new FlatPoint(corner[0], corner[1]));
          apex = right;
          apexIndex = rightIndex;
          left = apex;
          right = apex;
          leftIndex = apexIndex;
          i = apexIndex;
          continue;
        }
      }
    }

    result.add(end);
    return result;
  }

  /**
   * Builds the ordered gate list, one gate per shared boundary of consecutive boxes.
   */
  private List<double[]> portals(int fromBox, int toBox, FlatPoint start, FlatPoint end) {
    List<double[]> portals = new ArrayList<>();
    portals.add(new double[]{u(start), w(start), w(start)});

    int step = fromBox <= toBox ? 1 : -1;
    boolean forward = u(end) >= u(start);
    double previous = u(start);

    for (int i = fromBox; i != toBox; i += step) {
      RouterBox current = boxes.get(i);
      RouterBox next = boxes.get(i + step);

      double gate = sharedGate(current, next, forward);
      double lo = Math.max(wLow(current), wLow(next));
      double hi = Math.min(wHigh(current), wHigh(next));
      if (lo > hi) {
        // Boxes do not overlap across the corridor. Aim at the seam so the route still advances
        // instead of pretending an impossible gate exists.
        double mid = (lo + hi) / 2;
        lo = mid;
        hi = mid;
      } else {
        double inset = Math.min(GATE_CLEARANCE, (hi - lo) * GATE_CLEARANCE_RATIO);
        lo += inset;
        hi -= inset;

        Double anchor = referenceW(gate);
        if (anchor != null) {
          double low = Math.max(lo, anchor - MAX_DEVIATION);
          double high = Math.min(hi, anchor + MAX_DEVIATION);
          if (low <= high) {
            lo = low;
            hi = high;
          } else {
            // The layout route is already outside the legal gate; keep the nearest legal position.
            double clamped = Math.min(Math.max(anchor, lo), hi);
            lo = clamped;
            hi = clamped;
          }
        }
      }

      if (forward ? gate <= previous : gate >= previous) {
        continue;
      }
      previous = gate;
      portals.add(new double[]{gate, lo, hi});
    }

    portals.add(new double[]{u(end), w(end), w(end)});
    return portals.size() < 2 ? null : portals;
  }

  /** Records the layout's own route so the smoothed route can be kept close to it. */
  void setReference(List<? extends FlatPoint> points) {
    List<double[]> pairs = new ArrayList<>(points.size());
    for (FlatPoint point : points) {
      pairs.add(new double[]{u(point), w(point)});
    }
    pairs.sort((a, b) -> Double.compare(a[0], b[0]));
    this.reference = pairs;
  }

  /** Cross-axis position of the layout route at the given monotone coordinate. */
  private Double referenceW(double gate) {
    if (reference == null || reference.size() < 2) {
      return null;
    }
    double[] first = reference.get(0);
    double[] last = reference.get(reference.size() - 1);
    if (gate <= first[0]) {
      return first[1];
    }
    if (gate >= last[0]) {
      return last[1];
    }
    for (int i = 1; i < reference.size(); i++) {
      double[] previous = reference.get(i - 1);
      double[] current = reference.get(i);
      if (gate > current[0]) {
        continue;
      }
      double span = current[0] - previous[0];
      if (span <= EPS) {
        return current[1];
      }
      double ratio = (gate - previous[0]) / span;
      return previous[1] + (current[1] - previous[1]) * ratio;
    }
    return last[1];
  }

  private double sharedGate(RouterBox current, RouterBox next, boolean forward) {
    double lo = Math.max(uLow(current), uLow(next));
    double hi = Math.min(uHigh(current), uHigh(next));
    if (lo <= hi) {
      return (lo + hi) / 2;
    }
    return forward ? (uHigh(current) + uLow(next)) / 2 : (uLow(current) + uHigh(next)) / 2;
  }

  /**
   * Turns the taut path into a curve that bends through every vertex.
   *
   * <p>Rounding each corner in isolation leaves the rest of the route perfectly straight, so a
   * corner in a narrow box, where the arc has to stay small, shows up as a kink between two straight
   * runs. Label stations make that worse because they add a short segment whose corner can only hold
   * a tiny arc. Instead each vertex gets one tangent direction shared by the segments on both sides,
   * which makes the whole route continuous: a short or tightly boxed segment simply bends less.
   */
  MultiBezierCurve smooth(List<FlatPoint> path) {
    int size = path.size();
    MultiBezierCurve curves = new MultiBezierCurve(size);
    if (size == 2) {
      curves.add(straight(path.get(0), path.get(1)));
      return curves;
    }

    FlatPoint[] tangents = new FlatPoint[size];
    tangents[0] = direction(path.get(0), path.get(1));
    tangents[size - 1] = direction(path.get(size - 2), path.get(size - 1));
    for (int i = 1; i < size - 1; i++) {
      FlatPoint previous = path.get(i - 1);
      FlatPoint next = path.get(i + 1);
      FlatPoint tangent = direction(previous, next);
      if (tangent == null || almostReversal(previous, path.get(i), next)) {
        // A near reversal has no meaningful average direction; keep the incoming heading so the
        // curve cannot fold back on itself.
        tangent = direction(previous, path.get(i));
      }
      tangents[i] = tangent;
    }

    double[] length = new double[size];
    for (int i = 1; i < size; i++) {
      length[i] = distance(path.get(i - 1), path.get(i));
    }

    for (int i = 1; i < size; i++) {
      // Clamping each handle by the neighbouring segment stops a long segment from pushing a curve
      // far past a short one, which is what turns an averaged tangent into a visible wobble.
      double before = i > 1 ? length[i - 1] : length[i];
      double after = i < size - 1 ? length[i + 1] : length[i];
      double fromHandle = Math.min(length[i], before) * HANDLE_RATIO;
      double toHandle = Math.min(length[i], after) * HANDLE_RATIO;
      curves.add(fitSegment(path.get(i - 1), path.get(i), tangents[i - 1], tangents[i],
                            fromHandle, toHandle));
    }
    return curves.isEmpty() ? null : curves;
  }

  /**
   * Shortens the handles until the segment fits the corridor, so a tight box flattens that one
   * segment instead of breaking continuity for the whole route.
   */
  private ThirdOrderBezierCurve fitSegment(FlatPoint from, FlatPoint to, FlatPoint fromTangent,
                                           FlatPoint toTangent, double fromHandle,
                                           double toHandle) {
    if (fromTangent == null || toTangent == null) {
      return straight(from, to);
    }

    double scale = 1;
    for (int attempt = 0; attempt <= HANDLE_SHRINK_STEPS; attempt++) {
      if (fromHandle * scale <= EPS && toHandle * scale <= EPS) {
        break;
      }
      ThirdOrderBezierCurve candidate = segment(from, to, fromTangent, toTangent,
                                                fromHandle * scale, toHandle * scale);
      if (inCorridor(candidate)) {
        return candidate;
      }
      scale /= 2;
    }
    return straight(from, to);
  }

  private ThirdOrderBezierCurve segment(FlatPoint from, FlatPoint to, FlatPoint fromTangent,
                                        FlatPoint toTangent, double fromHandle, double toHandle) {
    return new ThirdOrderBezierCurve(
        new FlatPoint(from.getX(), from.getY()),
        new FlatPoint(from.getX() + fromTangent.getX() * fromHandle,
                      from.getY() + fromTangent.getY() * fromHandle),
        new FlatPoint(to.getX() - toTangent.getX() * toHandle,
                      to.getY() - toTangent.getY() * toHandle),
        new FlatPoint(to.getX(), to.getY()));
  }

  private static boolean almostReversal(FlatPoint previous, FlatPoint corner, FlatPoint next) {
    FlatPoint in = direction(previous, corner);
    FlatPoint out = direction(corner, next);
    if (in == null || out == null) {
      return true;
    }
    return in.getX() * out.getX() + in.getY() * out.getY() < -0.85;
  }

  private boolean inCorridor(ThirdOrderBezierCurve curve) {
    for (int i = 0; i <= CURVE_SAMPLES; i++) {
      double t = (double) i / CURVE_SAMPLES;
      if (!inCorridor(bezier(curve, t))) {
        return false;
      }
    }
    return true;
  }

  boolean inCorridor(FlatPoint point) {
    for (RouterBox box : boxes) {
      if (point.getX() >= box.getLeftBorder() - IN_TOLERANCE
          && point.getX() <= box.getRightBorder() + IN_TOLERANCE
          && point.getY() >= box.getUpBorder() - IN_TOLERANCE
          && point.getY() <= box.getDownBorder() + IN_TOLERANCE) {
        return true;
      }
    }
    return false;
  }

  private static FlatPoint bezier(ThirdOrderBezierCurve curve, double t) {
    double n = 1 - t;
    double a = n * n * n;
    double b = 3 * n * n * t;
    double c = 3 * n * t * t;
    double d = t * t * t;
    return new FlatPoint(
        a * curve.getV1().getX() + b * curve.getV2().getX()
            + c * curve.getV3().getX() + d * curve.getV4().getX(),
        a * curve.getV1().getY() + b * curve.getV2().getY()
            + c * curve.getV3().getY() + d * curve.getV4().getY());
  }

  private static ThirdOrderBezierCurve straight(FlatPoint from, FlatPoint to) {
    FlatPoint v2 = new FlatPoint(from.getX() + (to.getX() - from.getX()) / 3,
                                 from.getY() + (to.getY() - from.getY()) / 3);
    FlatPoint v3 = new FlatPoint(from.getX() + (to.getX() - from.getX()) * 2 / 3,
                                 from.getY() + (to.getY() - from.getY()) * 2 / 3);
    return new ThirdOrderBezierCurve(new FlatPoint(from.getX(), from.getY()), v2, v3,
                                     new FlatPoint(to.getX(), to.getY()));
  }

  private static FlatPoint direction(FlatPoint from, FlatPoint to) {
    double dx = to.getX() - from.getX();
    double dy = to.getY() - from.getY();
    double len = Math.sqrt(dx * dx + dy * dy);
    if (len <= EPS) {
      return null;
    }
    return new FlatPoint(dx / len, dy / len);
  }

  private static double distance(FlatPoint from, FlatPoint to) {
    return FlatPoint.twoFlatPointDistance(from, to);
  }

  private static List<FlatPoint> dedupe(List<FlatPoint> points) {
    List<FlatPoint> result = new ArrayList<>(points.size());
    for (FlatPoint point : points) {
      if (result.isEmpty() || distance(result.get(result.size() - 1), point) > EPS) {
        result.add(point);
      }
    }
    return result.size() < 2 ? Collections.emptyList() : result;
  }

  private static double cross(double[] origin, double[] a, double[] b) {
    return (a[0] - origin[0]) * (b[1] - origin[1]) - (a[1] - origin[1]) * (b[0] - origin[0]);
  }

  private static boolean same(double[] a, double[] b) {
    return Math.abs(a[0] - b[0]) < EPS && Math.abs(a[1] - b[1]) < EPS;
  }

  private double[] point(double[] funnelPoint) {
    return vertical
        ? new double[]{funnelPoint[0], funnelPoint[1]}
        : new double[]{funnelPoint[1], funnelPoint[0]};
  }

  private double u(FlatPoint point) {
    return vertical ? point.getY() : point.getX();
  }

  private double w(FlatPoint point) {
    return vertical ? point.getX() : point.getY();
  }

  private double uLow(RouterBox box) {
    return vertical ? box.getUpBorder() : box.getLeftBorder();
  }

  private double uHigh(RouterBox box) {
    return vertical ? box.getDownBorder() : box.getRightBorder();
  }

  private double wLow(RouterBox box) {
    return vertical ? box.getLeftBorder() : box.getUpBorder();
  }

  private double wHigh(RouterBox box) {
    return vertical ? box.getRightBorder() : box.getDownBorder();
  }
}
