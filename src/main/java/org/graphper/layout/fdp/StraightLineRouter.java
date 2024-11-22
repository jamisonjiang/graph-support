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

import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Splines;
import org.graphper.draw.DrawGraph;

/**
 * A router for drawing straight lines between nodes in a graph.
 *
 * @author Jamison Jiang
 */
public class StraightLineRouter extends AbstractFdpLineRouter {

  public StraightLineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, fdpGraph);
  }

  @Override
  protected void handle(FLine fline) {
    drawStraightLine(fline);
  }

  public static class StraightLineRouterFactory extends LineRouterFactory<StraightLineRouter> {

    @Override
    public boolean needDeal(Graphviz graphviz) {
      Splines splines = graphviz.graphAttrs().getSplines();
      return splines == null || Splines.LINE == splines;
    }

    @Override
    public StraightLineRouter newInstance(DrawGraph drawGraph, FdpGraph fdpGraph) {
      return new StraightLineRouter(drawGraph, fdpGraph);
    }
  }
}
