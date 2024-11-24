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
import org.graphper.layout.ALine;
import org.graphper.layout.ANode;
import org.graphper.layout.AbstractOrthogonalRouter;
import org.graphper.layout.LineRouter;

/**
 * A specialized {@link LineRouter} implementation for routing edges in an orthogonal layout.
 *
 * @author Jamison Jiang
 */
class OrthogonalRouter extends AbstractOrthogonalRouter implements LineRouter {

  protected OrthogonalRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, fdpGraph);
  }

  @Override
  public void route() {
    if (drawGraph.getGraphviz().graphAttrs().isOverlap()) {
      new StraightLineRouter(drawGraph, (FdpGraph) layoutGraph).route();
      return;
    }
    this.maze = new FdpMaze(drawGraph, (FdpGraph) layoutGraph);
    generateEdge();
  }

  @Override
  protected Iterable<ANode> nodes() {
    return (Iterable<ANode>) layoutGraph;
  }

  @Override
  protected Iterable<? extends ALine> adjacentLines(ANode node) {
    return ((FdpGraph) layoutGraph).outAdjacent(node);
  }

  static class OrthogonalRouterFactory extends LineRouterFactory<OrthogonalRouter> {

    @Override
    public boolean needDeal(Graphviz graphviz) {
      return Splines.ORTHO == graphviz.graphAttrs().getSplines();
    }

    @Override
    public OrthogonalRouter newInstance(DrawGraph drawGraph, FdpGraph fdpGraph) {
      return new OrthogonalRouter(drawGraph, fdpGraph);
    }
  }
}
