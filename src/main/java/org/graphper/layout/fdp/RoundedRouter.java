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

package org.graphper.layout.fdp;

import static org.graphper.layout.LineHelper.connectWithRoundedCorner;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;

/**
 * A specialized {@link AroundLineRouter} implementation for routing edges with rounded corners.
 *
 * @author Jamison Jiang
 */
class RoundedRouter extends AroundLineRouter {

  public RoundedRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, fdpGraph);
  }

  @Override
  protected void drawLine(FLine line, Iterable<FlatPoint> splitPoints) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());

    List<FlatPoint> throughPoints = new ArrayList<>();
    splitPoints.forEach(throughPoints::add);

    connectWithRoundedCorner(lineDrawProp, null, null, throughPoints, null);
  }

  // --------------------------------------------- RoundedHandlerFactory ---------------------------------------------

  static class RoundedRouterFactory extends LineRouterFactory<RoundedRouter> {

    @Override
    public boolean needDeal(Graphviz graphviz) {
      return graphviz.graphAttrs().getSplines() == Splines.ROUNDED;
    }

    @Override
    public RoundedRouter newInstance(DrawGraph drawGraph, FdpGraph fdpGraph) {
      return new RoundedRouter(drawGraph, fdpGraph);
    }
  }
}
