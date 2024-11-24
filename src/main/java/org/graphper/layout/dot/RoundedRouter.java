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

import static org.graphper.layout.LineHelper.connectWithRoundedCorner;

import java.util.List;
import org.graphper.api.attributes.Splines;
import org.graphper.draw.LineDrawProp;

/**
 * Implementation of {@link Splines#ROUNDED}.
 *
 * @author Jamison Jiang
 */
class RoundedRouter extends CurveFitBoxRouter {

  @Override
  protected boolean curveLine() {
    return true;
  }

  @Override
  protected void throughPointHandle(ThroughParam throughParam) {
    List<ThroughPoint> throughPoints = throughParam.throughPoints;
    LineDrawProp lineDrawProp = throughParam.lineDrawProp;

    connectWithRoundedCorner(lineDrawProp, throughParam.fromPortPoints,
                             throughParam.toPortPoints, throughPoints,
                             curves -> fixBox(throughParam.lineRouterBoxes, curves));
  }

  // --------------------------------------------- RoundedHandlerFactory ---------------------------------------------

  static class RoundedRouterFactory extends AbstractDotLineRouterFactory<RoundedRouter> {

    @Override
    public boolean needDeal(Splines splines) {
      return splines == null || splines == Splines.ROUNDED && super.needDeal(splines);
    }

    @Override
    protected RoundedRouter newInstance() {
      return new RoundedRouter();
    }
  }
}
