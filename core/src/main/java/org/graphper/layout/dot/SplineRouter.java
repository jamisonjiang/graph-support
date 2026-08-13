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

import static org.graphper.layout.LineHelper.lineDrawPropConnect;
import static org.graphper.layout.LineHelper.multiBezierCurveToPoints;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.attributes.Splines;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.draw.LineDrawProp;

/**
 * Implementation of {@link Splines#SPLINE}.
 *
 * @author Jamison Jiang
 */
class SplineRouter extends CurveFitBoxRouter {

  private SplineRouter() {
  }

  @Override
  protected boolean curveLine() {
    return true;
  }

  @Override
  protected void throughPointHandle(ThroughParam throughParam) {
    LineDrawProp lineDrawProp = throughParam.lineDrawProp;
    lineDrawProp.clear();

    List<FlatPoint> fromPortPoints = throughParam.fromPortPoints;
    List<FlatPoint> toPortPoints = throughParam.toPortPoints;
    lineDrawProp.markIsBesselCurve();

    List<ThroughPoint> throughPoints = simplifyThroughPoints(throughParam.throughPoints);
    if (throughPoints.size() < 2) {
      lineDrawPropConnect(lineDrawProp, fromPortPoints, true);
      lineDrawPropConnect(lineDrawProp, toPortPoints, false);
      return;
    }

    MultiBezierCurve curves = Curves.fitCurves(throughPoints, 0.04);
    fixBox(throughParam.lineRouterBoxes, curves);
    multiBezierCurveToPoints(curves, lineDrawProp::add);
    lineDrawPropConnect(lineDrawProp, throughParam.fromPortPoints, true);
    lineDrawPropConnect(lineDrawProp, throughParam.toPortPoints, false);
  }

  static List<ThroughPoint> simplifyThroughPoints(List<ThroughPoint> points) {
    if (points == null || points.size() < 3) {
      return points;
    }
    List<ThroughPoint> simplified = new ArrayList<>(points.size());
    for (ThroughPoint point : points) {
      simplified.add(point);
      while (simplified.size() >= 3) {
        int last = simplified.size() - 1;
        ThroughPoint a = simplified.get(last - 2);
        ThroughPoint b = simplified.get(last - 1);
        ThroughPoint c = simplified.get(last);
        if (!sameDirection(a, b, c) || !axisAligned(a, b, c)) {
          break;
        }
        simplified.remove(last - 1);
      }
    }
    return simplified;
  }

  private static boolean sameDirection(FlatPoint a, FlatPoint b, FlatPoint c) {
    double abX = b.getX() - a.getX();
    double abY = b.getY() - a.getY();
    double bcX = c.getX() - b.getX();
    double bcY = c.getY() - b.getY();
    return abX * bcX + abY * bcY >= 0;
  }

  private static boolean axisAligned(FlatPoint a, FlatPoint b, FlatPoint c) {
    return Math.abs(a.getX() - b.getX()) <= 0.1 && Math.abs(b.getX() - c.getX()) <= 0.1
        || Math.abs(a.getY() - b.getY()) <= 0.1 && Math.abs(b.getY() - c.getY()) <= 0.1;
  }

  // --------------------------------------------- SplineRouterFactory ---------------------------------------------

  public static class SplineRouterFactory extends AbstractDotLineRouterFactory<SplineRouter> {

    @Override
    public boolean needDeal(Splines splines) {
      return splines == Splines.SPLINE && super.needDeal(splines);
    }

    @Override
    protected SplineRouter newInstance() {
      return new SplineRouter();
    }
  }
}
