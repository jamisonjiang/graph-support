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

import java.util.List;
import java.util.function.IntPredicate;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;

abstract class CurveFitBoxRouter extends BoxGuideLineRouter {

  protected static final int MAX_ITERATORS = 24;

  protected void refineSpline(SplineFitInfo splineFitInfo) {
    splineFitInfo.curve.adjust(0.9, 0.9);
  }

  protected MultiBezierCurve fixBox(List<RouterBox> lineRouterBoxes, MultiBezierCurve curves) {
    SplineFitInfo splineFitInfo = splineIsFit(curves, lineRouterBoxes, false);
    if (splineFitInfo.isFit()) {
      return curves;
    }

    // Always try to adjust the curve so that it fits the box.
    int count = 0;
    do {
      refineSpline(splineFitInfo);
      splineFitInfo = splineIsFit(curves, lineRouterBoxes, true);
      if (splineFitInfo.isFit()) {
        break;
      }
      count++;
    } while (count <= MAX_ITERATORS);

    return curves;
  }


  private SplineFitInfo splineIsFit(MultiBezierCurve curves, List<RouterBox> boxes, boolean needOffset) {
    SplineFitInfo splineFitInfo = new SplineFitInfo();
    splineFitInfo.needOffset = needOffset;

    RouterBox first = boxes.get(0);
    RouterBox last = boxes.get(boxes.size() - 1);
    double boxMinY = (first.getUpBorder() + first.getDownBorder()) / 2;
    double boxMaxY = (last.getUpBorder() + last.getDownBorder()) / 2;

    for (int j = 0; j < curves.size(); j++) {
      ThirdOrderBezierCurve curve = curves.get(j);

      // Estimate number of line segments from curve curvature.
      int segmentNum = 2;
      for (int i = 0; i < boxes.size(); i++) {
        RouterBox lineRouterBox = boxes.get(i);
        double minY = Math.min(curve.getV1().getY(), curve.getV4().getY());
        minY = Math.min(curve.getV2().getY(), minY);
        minY = Math.min(curve.getV3().getY(), minY);
        double maxY = Math.max(curve.getV1().getY(), curve.getV4().getY());
        maxY = Math.max(curve.getV2().getY(), maxY);
        maxY = Math.max(curve.getV3().getY(), maxY);

        double upBorder = lineRouterBox.getUpBorder();
        double downBorder = lineRouterBox.getDownBorder();

        // The Y axis must have common intervals.
        if (minY != maxY && (minY <= upBorder && maxY >= upBorder)
            || (minY <= downBorder && maxY >= downBorder)) {

          double ratio = Math.abs(
              minY != maxY ? (Math.abs(upBorder + downBorder) / 2 - minY) / (maxY - minY) : 0.5);
          ratio = ratio > 1 ? 1 : ratio;

          if (isInBoxOrStraightenMeaningless(curve, lineRouterBox, (int) (ratio * segmentNum),
                                              segmentNum, boxMinY, boxMaxY, splineFitInfo)) {
            continue;
          }

          splineFitInfo.routerBox = lineRouterBox;
          splineFitInfo.boxIndex = i;
          splineFitInfo.curve = curve;
          return splineFitInfo;
        }

        if (lineRouterBox.getUpBorder() > curve.getV4().getY()) {
          break;
        }
      }
    }

    return splineFitInfo;
  }

  private boolean isInBoxOrStraightenMeaningless(ThirdOrderBezierCurve curve, RouterBox routerBox,
                                                 int currentSegment, int segmentNum, double minY,
                                                 double maxY, SplineFitInfo splineFitInfo) {
    double unit = (double) 1 / segmentNum;

    ThirdOrderBezierCurve detectCurve = new ThirdOrderBezierCurve(curve);
    detectCurve.adjust(0.1, 0.1);
    FlatPoint p1 = Curves.besselEquationCalc(unit * currentSegment,
                                             detectCurve.getV1(), detectCurve.getV2(),
                                             detectCurve.getV3(), detectCurve.getV4());
    if (!inBox(detectCurve, p1, routerBox, unit, currentSegment, 1,
               minY, maxY, c -> c < segmentNum, splineFitInfo)) {
      // Meaningful to straighten curve because in the extreme case still not work
      return true;
    }

    p1 = Curves.besselEquationCalc(unit * currentSegment,
                                   curve.getV1(), curve.getV2(),
                                   curve.getV3(), curve.getV4());

    // There is at least one vertex whose coordinates are within the y interval,
    // and it is accessed from two directions until it exceeds the y area.
    if (!inBox(curve, p1, routerBox, unit, currentSegment, -1,
               minY, maxY, c -> 0 < c, splineFitInfo)
        && !splineFitInfo.needOffset) {
      return false;
    }

    return inBox(curve, p1, routerBox, unit, currentSegment, 1,
                 minY, maxY, c -> c < segmentNum, splineFitInfo);
  }

  private boolean inBox(ThirdOrderBezierCurve curve, FlatPoint p1, RouterBox routerBox, double unit,
                        int currentSegment, int addNum, double minY, double maxY,
                        IntPredicate breakCondition, SplineFitInfo splineFitInfo) {
    FlatPoint p2;
    boolean result = true;
    Integer count = null;
    while (breakCondition.test(currentSegment)) {
      p2 = Curves.besselEquationCalc(unit * (currentSegment += addNum),
                                     curve.getV1(), curve.getV2(), curve.getV3(), curve.getV4());

      // Any point jumps out of the boundary of the box.
      if (!RouterBox.inRange(minY, maxY, p1.getY()) || !RouterBox.inRange(minY, maxY, p2.getY())) {
        result = false;
        break;
      }

      if (routerBox.getUpBorder() > p1.getY() && routerBox.getUpBorder() > p2.getY()) {
        continue;
      }

      if (!approximatelyInBoxY(routerBox, p1.getY()) && !approximatelyInBoxY(routerBox, p2.getY())) {
        break;
      }

      if ((approximatelyInBoxY(routerBox, p1.getY()) && !approximatelyInBox(routerBox, p1))
          || (approximatelyInBoxY(routerBox, p2.getY()) && !approximatelyInBox(routerBox, p2))) {
        result = false;
        if (!splineFitInfo.needOffset) {
          break;
        }

        if (count == null) {
          count = 1;
        } else {
          count++;
        }
        splineFitInfo.offset += offset(p1, routerBox);
        splineFitInfo.offset += offset(p2, routerBox);
      }
      p1 = p2;
    }

    if (splineFitInfo.needOffset && count != null) {
      splineFitInfo.offset /= count;
    }

    return result;
  }

  private double offset(FlatPoint p, RouterBox routerBox) {
    if (approximatelyInBoxX(routerBox, p.getX())) {
      return 0;
    }

    return Math.abs(routerBox.closerVerWall(p.getX()) - p.getX());
  }

  private boolean approximatelyInBoxX(RouterBox routerBox, double p) {
    return approximatelyInRange(routerBox.getLeftBorder(), routerBox.getRightBorder(), p);
  }

  private boolean approximatelyInBoxY(RouterBox routerBox, double p) {
    return approximatelyInRange(routerBox.getUpBorder(), routerBox.getDownBorder(), p);
  }

  private boolean approximatelyInRange(double left, double right, double v) {
    return RouterBox.inRange(left, right, v)
        || RouterBox.inRange(left, right, v - 1)
        || RouterBox.inRange(left, right, v + 1);
  }

  private boolean approximatelyInBox(RouterBox routerBox, FlatPoint p) {
    return approximatelyInRange(routerBox.getLeftBorder(), routerBox.getRightBorder(), p.getX())
        && approximatelyInRange(routerBox.getUpBorder(), routerBox.getDownBorder(), p.getY());
  }

  protected static class SplineFitInfo {

    protected ThirdOrderBezierCurve curve;

    protected RouterBox routerBox;

    protected int boxIndex;

    protected double offset;

    protected boolean needOffset;

    protected SplineFitInfo() {
    }

    protected boolean isFit() {
      return curve == null;
    }
  }
}
