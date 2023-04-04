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

import java.util.Collections;
import java.util.List;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.CollectionUtils;

/**
 * Implementation of {@link Splines#SPLINE}.
 *
 * @author Jamison Jiang
 */
class SplineRouter extends CurveFitBoxRouter {

  private SplineRouter() {
  }

  @Override
  public boolean needDeal(Splines splines) {
    return splines == Splines.SPLINE && super.needDeal(splines);
  }

  @Override
  protected boolean curveLine() {
    return true;
  }

  @Override
  protected void throughPointHandle(ThroughParam throughParam) {
    throughParam.lineDrawProp.clear();

    int pointStart = 0;
    FlatPoint leftVector;
    FlatPoint rightVector = null;
    boolean isVerticalMode = false;
    List<ThroughPoint> throughPoints = throughParam.throughPoints;
    LineDrawProp lineDrawProp = throughParam.lineDrawProp;
    List<RouterBox> lineRouterBoxes = throughParam.lineRouterBoxes;

    for (int i = 0; i < throughPoints.size() - 1; i++) {
      ThroughPoint point = throughPoints.get(i);
      ThroughPoint next = throughPoints.get(i + 1);

      // Whether adjacent control points need vertical optimization.
      if (needVerticalOptimization(point, next)) {
        // If it is already in vertical optimization mode, ignore.
        if (isVerticalMode) {
          continue;
        }

        leftVector = rightVector == null ? null : rightVector.reserve();
        rightVector = new FlatPoint(0, -1);
        isVerticalMode = true;

        // If the first and second points are almost perpendicular, skip the split.
        if (i == 0) {
          continue;
        }

        // Split non-vertical regions.
        ThroughPoint start = throughPoints.get(pointStart);
        addAllLineProp(
            lineDrawProp,
            computeSpline(
                throughPoints,
                lineRouterBoxes,
                leftVector,
                rightVector,
                pointStart,
                i,
                pointStart == 0 ? start.getBoxIndex() : start.getBoxIndex() + 1,
                point.getBoxIndex(),
                throughParam.isHorizontal
            )
        );

        pointStart = i;
        continue;
      }

      // When the control points are almost perpendicular, split the drawn curve segment and adjust
      // the slope of the tangent line at the corresponding junction so that the curve is smooth.
      if (isVerticalMode) {
        ThroughPoint start = throughPoints.get(pointStart);
        leftVector = rightVector.reserve();
        rightVector = new FlatPoint(0, -1);
        addAllLineProp(
            lineDrawProp,
            computeSpline(
                throughPoints,
                lineRouterBoxes,
                leftVector,
                rightVector,
                pointStart,
                i,
                pointStart == 0 ? start.getBoxIndex() : start.getBoxIndex() + 1,
                point.getBoxIndex(),
                throughParam.isHorizontal
            )
        );
        pointStart = i;
        isVerticalMode = false;
      }
    }

    // Complete the last segment division.
    ThroughPoint start = throughPoints.get(pointStart);
    addAllLineProp(
        lineDrawProp,
        computeSpline(
            throughPoints,
            lineRouterBoxes,
            rightVector == null ? null : rightVector.reserve(),
            null,
            pointStart,
            throughPoints.size() - 1,
            pointStart == 0 ? start.getBoxIndex() : start.getBoxIndex() + 1,
            lineRouterBoxes.size() - 1,
            throughParam.isHorizontal
        )
    );

    throughPoints.clear();

    lineDrawPropConnect(lineDrawProp, throughParam.fromPortPoints, true);
    lineDrawPropConnect(lineDrawProp, throughParam.toPortPoints, false);
    lineDrawProp.markIsBesselCurve();
  }

  // ---------------------------------------------------- private method ----------------------------------------------------

  private void addAllLineProp(LineDrawProp lineDrawProp, List<FlatPoint> flatPoints) {
    int k = CollectionUtils.isEmpty(lineDrawProp) ? 0 : 1;
    for (; k < flatPoints.size(); k++) {
      lineDrawProp.add(flatPoints.get(k));
    }
  }

  private boolean needVerticalOptimization(FlatPoint p1, FlatPoint p2) {
    return Math.abs(p1.getX() - p2.getX()) < 5 && p2.getY() - p1.getY() >
        drawGraph
            .getGraphviz()
            .graphAttrs()
            .getRankSep();
  }

  private List<FlatPoint> computeSpline(List<ThroughPoint> points, List<RouterBox> lineRouterBoxes,
                                        FlatPoint leftVector, FlatPoint rightVector,
                                        int pointStart, int pointEnd, int boxStart,
                                        int boxEnd, boolean isHorizontal) {
    if (CollectionUtils.isEmpty(points) || points.size() < 2) {
      return Collections.emptyList();
    }
    MultiBezierCurve curves = Curves
        .fitCurves(points.subList(pointStart, pointEnd + 1), leftVector, rightVector, 0);

    if (isHorizontal) {
      return multiBezierCurveToPoints(curves);
    }

    SplineFitInfo splineFitInfo;
    if (pointEnd - pointStart + 1 <= 2) {
      int count = 0;
      // Straighten the curve until it does not exceed the bounds of the box.
      while (count++ < MAX_ITERATORS
          && !(splineFitInfo = splineIsFit(curves, lineRouterBoxes, boxStart, boxEnd, false)).isFit()) {
        straightenSpline(splineFitInfo.curve);
      }

      return multiBezierCurveToPoints(curves);
    }

    // The curve has been fitted.
    if ((splineFitInfo = splineIsFit(curves, lineRouterBoxes, boxStart, boxEnd, true)).isFit()) {
      return multiBezierCurveToPoints(curves);
    }

    int count = 0;
    // Always try to adjust the curve so that it fits the box.
    do {
      refineSpline(splineFitInfo);
      splineFitInfo = splineIsFit(curves, lineRouterBoxes, boxStart, boxEnd, true);
      if (splineFitInfo.isFit()) {
        break;
      }
      count++;
    } while (count <= MAX_ITERATORS);

    // The curve has been fitted, return to the curve.
    if (splineFitInfo.isFit()) {
      return multiBezierCurveToPoints(curves);
    }

    SplitInfo split = splineSplit(curves, lineRouterBoxes, points,
                                  pointStart, pointEnd, boxStart, boxEnd);
    if (split == null) {
      return multiBezierCurveToPoints(curves);
    }
    List<FlatPoint> leftSplines = computeSpline(
        points,
        lineRouterBoxes,
        leftVector,
        split.splitVector,
        pointStart,
        split.pointsSplitIndex,
        boxStart,
        split.boxSplitIndex,
        false
    );
    List<FlatPoint> rightSplines = computeSpline(
        points,
        lineRouterBoxes,
        split.splitVector.reserve(),
        rightVector,
        split.pointsSplitIndex,
        pointEnd,
        split.boxSplitIndex + 1,
        boxEnd,
        false
    );

    rightSplines.remove(0);
    leftSplines.addAll(rightSplines);
    return leftSplines;
  }

  // --------------------------------------------- SplineRouterFactory ---------------------------------------------

  public static class SplineRouterFactory extends AbstractDotLineRouterFactory<SplineRouter> {

    @Override
    protected SplineRouter newInstance() {
      return new SplineRouter();
    }
  }
}
