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

import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.CollectionUtils;

class PolylineRouter extends AroundLineRouter {

  public PolylineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, fdpGraph);
  }

  @Override
  public boolean needDeal(Splines splines) {
    return splines == Splines.POLYLINE && super.needDeal(splines);
  }

  @Override
  protected void drawLine(FLine line, Iterable<FlatPoint> splitPoints) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
    if (CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }

    lineDrawProp.add(new FlatPoint(line.from().getX(), line.from().getY()));
    splitPoints.forEach(lineDrawProp::add);
    lineDrawProp.add(new FlatPoint(line.to().getX(), line.to().getY()));
  }

  public static class PolylineRouterFactory implements LineRouterFactory<PolylineRouter> {

    @Override
    public PolylineRouter newInstance(DrawGraph drawGraph, FdpGraph fdpGraph) {
      return new PolylineRouter(drawGraph, fdpGraph);
    }
  }
}