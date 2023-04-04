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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.Vectors;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.CollectionUtils;

/**
 *
 * Implementation of {@link Splines#ROUNDED}.
 *
 * @author Jamison Jiang
 */
class RoundedRouter extends CurveFitBoxRouter {

  @Override
  public boolean needDeal(Splines splines) {
    return splines == Splines.ROUNDED && super.needDeal(splines);
  }

  @Override
  protected boolean curveLine() {
    return true;
  }

  @Override
  protected void throughPointHandle(ThroughParam throughParam) {
    LineAttrs lineAttrs = drawGraph.getLineDrawProp(throughParam.line).lineAttrs();

    List<ThroughPoint> throughPoints = throughParam.throughPoints;
    LineDrawProp lineDrawProp = throughParam.lineDrawProp;
    lineDrawProp.markIsBesselCurve();

    if (CollectionUtils.isEmpty(throughParam.throughPoints)) {
      lineDrawPropConnect(lineDrawProp, throughParam.fromPortPoints, true);
      lineDrawPropConnect(lineDrawProp, throughParam.toPortPoints, false);
      return;
    }

    if (throughPoints.size() <= 2) {
      ThroughPoint first = throughPoints.get(0);
      ThroughPoint last = throughPoints.get(throughPoints.size() - 1);
      lineDrawProp.add(first);
      lineDrawProp.add(first);
      lineDrawProp.add(last);
      lineDrawProp.add(last);

      lineDrawPropConnect(lineDrawProp, throughParam.fromPortPoints, true);
      lineDrawPropConnect(lineDrawProp, throughParam.toPortPoints, false);
      return;
    }

    ThroughPoint pre = null;
    ThroughPoint prepre = null;
    double radian = lineAttrs.getRadian() != null ? lineAttrs.getRadian() : 10;

    for (int i = 2; i < throughPoints.size(); i++) {
      ThroughPoint p1 = prepre == null ? throughPoints.get(i - 2) : prepre;
      ThroughPoint p2 = pre == null ? throughPoints.get(i - 1) : pre;
      ThroughPoint p3 = throughPoints.get(i);

      if (prepre == null) {
        prepre = p1;
        lineDrawProp.add(p1);
      }

      if (isCorner(p1, p2, p3)) {
        ThirdOrderBezierCurve corner = null;
        ThroughPoint lt = getLeftTangle(lineDrawProp);
        ThroughPoint rt = i < throughPoints.size() - 1 ? throughPoints.get(i + 1) : null;

        MultiBezierCurve curves = getCorner(lt, p1, p2, p3, rt, radian, throughParam.lineRouterBoxes);
        if (curves.size() == 1) {
          lineDrawProp.add(p1);
        }

        for (ThirdOrderBezierCurve curve : curves) {
          if (corner == null && curves.size() == 1) {
            lineDrawProp.add(curve.getV1());
            lineDrawProp.add(curve.getV1());
          }

          lineDrawProp.add(curve.getV2());
          lineDrawProp.add(curve.getV3());
          lineDrawProp.add(curve.getV4());

          corner = curve;
        }

        if (corner != null) {
          prepre = new ThroughPoint(corner.getV4());
          pre = p3;
        }

        if (corner != null && i == throughPoints.size() - 1) {
          lineDrawProp.add(corner.getV4());
          lineDrawProp.add(p3);
          lineDrawProp.add(p3);
        }
      } else {
        if (i == throughPoints.size() - 1) {
          lineDrawProp.add(p1);
          lineDrawProp.add(p3);
          lineDrawProp.add(p3);
        } else {
          pre = p3;
        }
      }
    }

    lineDrawPropConnect(lineDrawProp, throughParam.fromPortPoints, true);
    lineDrawPropConnect(lineDrawProp, throughParam.toPortPoints, false);
  }

  private MultiBezierCurve getCorner(ThroughPoint lt, ThroughPoint p1, ThroughPoint p2,
                                     ThroughPoint p3, ThroughPoint rt, double radian,
                                     List<RouterBox> lineRouterBoxes) {
    double d1 = FlatPoint.twoFlatPointDistance(p1, p2);
    double d2 = FlatPoint.twoFlatPointDistance(p2, p3);

    if (d1 < radian || d2 < radian) {
      MultiBezierCurve curves = Curves.fitCurves(Arrays.asList(p1, p3),
                                                 lt != null ? Vectors.sub(p1, lt) : null,
                                                 rt != null ? Vectors.sub(p3, rt) : null, 0);

      return checkFixBox(lineRouterBoxes, curves);
    }

    FlatPoint dir = Vectors.sub(p2, p1);
    double ratio = (d1 - radian) / d1;
    FlatPoint v1 = Vectors.add(p1, Vectors.multiple(dir, ratio));

    ratio = (d1 - radian / 2) / d1;
    FlatPoint v2 = Vectors.add(p1, Vectors.multiple(dir, ratio));

    dir = Vectors.sub(p2, p3);
    ratio = (d2 - radian) / d2;
    FlatPoint v4 = Vectors.add(p3, Vectors.multiple(dir, ratio));

    ratio = (d2 - radian / 2) / d2;
    FlatPoint v3 = Vectors.add(p3, Vectors.multiple(dir, ratio));

    MultiBezierCurve thirdOrderBezierCurves = new MultiBezierCurve(1);
    thirdOrderBezierCurves.add(new ThirdOrderBezierCurve(v1, v2, v3, v4));
    return thirdOrderBezierCurves;
  }

  private MultiBezierCurve checkFixBox(List<RouterBox> lineRouterBoxes, MultiBezierCurve curves) {
    SplineFitInfo splineFitInfo = splineIsFit(curves, lineRouterBoxes, 0, lineRouterBoxes.size() - 1, false);
    if (splineFitInfo.isFit()) {
      return curves;
    }

    int count = 0;
    // Always try to adjust the curve so that it fits the box.
    do {
      refineSpline(splineFitInfo);
      splineFitInfo = splineIsFit(curves, lineRouterBoxes, 0, lineRouterBoxes.size() - 1, true);
      if (splineFitInfo.isFit()) {
        break;
      }
      count++;
    } while (count <= MAX_ITERATORS);

    return curves;
  }

  private boolean isCorner(ThroughPoint p1, ThroughPoint p2, ThroughPoint p3) {
    if (p1 == null || p2 == null || p3 == null) {
      return false;
    }

    if (Objects.equals(p1, p2) || Objects.equals(p2, p3) || Objects.equals(p1, p3)) {
      return false;
    }

    if (p1.similarX(p2.getX(), 1) && p1.similarX(p3.getX(), 1) && p2.similarX(p3.getX(), 1)) {
      return false;
    }

    if (p1.similarY(p2.getY(), 1) && p1.similarY(p3.getY(), 1) && p2.similarY(p3.getY(), 1)) {
      return false;
    }

    double slope1 = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
    double slope2 = (p3.getY() - p2.getY()) / (p3.getX() - p2.getX());

    return Math.abs(slope1 - slope2) > 0.06;
  }

  private ThroughPoint getLeftTangle(LineDrawProp lineDrawProp) {
    if (lineDrawProp.size() <= 3) {
      return null;
    }

    FlatPoint v4 = lineDrawProp.get(lineDrawProp.size() - 1);
    FlatPoint v3 = lineDrawProp.get(lineDrawProp.size() - 2);
    FlatPoint v2 = lineDrawProp.get(lineDrawProp.size() - 3);
    return new ThroughPoint(v3.equals(v4) ? v2 : v3);
  }

  // --------------------------------------------- RoundedHandlerFactory ---------------------------------------------

  static class RoundedRouterFactory extends AbstractDotLineRouterFactory<RoundedRouter> {

    @Override
    protected RoundedRouter newInstance() {
      return new RoundedRouter();
    }
  }
}
