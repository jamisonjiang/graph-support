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

    if (throughParam.throughPoints.size() < 2) {
      lineDrawPropConnect(lineDrawProp, fromPortPoints, true);
      lineDrawPropConnect(lineDrawProp, toPortPoints, false);
      return;
    }

    MultiBezierCurve curves = Curves.fitCurves(throughParam.throughPoints, 0.04);
    fixBox(throughParam.lineRouterBoxes, curves);
    multiBezierCurveToPoints(curves, lineDrawProp::add);
    lineDrawPropConnect(lineDrawProp, throughParam.fromPortPoints, true);
    lineDrawPropConnect(lineDrawProp, throughParam.toPortPoints, false);
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
