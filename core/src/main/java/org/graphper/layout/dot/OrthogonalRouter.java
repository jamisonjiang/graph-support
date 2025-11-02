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

import java.util.Objects;
import org.graphper.api.attributes.Splines;
import org.graphper.def.UnaryConcatIterable;
import org.graphper.def.EdgeDedigraph;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.ALine;
import org.graphper.layout.ANode;
import org.graphper.layout.AbstractOrthogonalRouter;
import org.graphper.layout.LayoutGraph;
import org.graphper.layout.LineRouter;
import org.graphper.layout.dot.RankContent.RankNode;

/**
 * Implementation of {@link Splines#ORTHO}.
 *
 * @author Jamison Jiang
 */
class OrthogonalRouter extends AbstractDotLineRouter implements LineRouter {

  @Override
  public void route() {
    DotOrthogonalRouter router = new DotOrthogonalRouter(rankContent, drawGraph,
                                                         layoutGraph, digraphProxy);
    router.route();
  }

  static class DotOrthogonalRouter extends AbstractOrthogonalRouter {

    private final RankContent rankContent;

    protected EdgeDedigraph<DNode, DLine> digraphProxy;

    public DotOrthogonalRouter(RankContent rankContent, DrawGraph drawGraph,
                               LayoutGraph<?, ?> layoutGraph,
                               EdgeDedigraph<DNode, DLine> digraphProxy) {
      super(drawGraph, layoutGraph);
      this.maze = new DotMaze(rankContent, drawGraph);
      this.rankContent = rankContent;
      this.digraphProxy = digraphProxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Iterable<ANode> nodes() {
      UnaryConcatIterable<ANode> nodeConcatIterable = null;
      for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
        RankNode rankNode = rankContent.get(i);
        nodeConcatIterable = new UnaryConcatIterable<>(nodeConcatIterable, rankNode);
      }
      Objects.requireNonNull(nodeConcatIterable);
      return nodeConcatIterable;
    }

    @Override
    protected Iterable<? extends ALine> adjacentLines(ANode node) {
      return digraphProxy.outAdjacent(node);
    }

    void route() {
      generateEdge();
    }
  }

  // --------------------------------------------- OrthogonalRouterFactory ---------------------------------------------

  static class OrthogonalRouterFactory extends
      AbstractDotLineRouterFactory<OrthogonalRouter> {

    @Override
    public boolean needDeal(Splines splines) {
      return splines == Splines.ORTHO && super.needDeal(splines);
    }

    @Override
    protected OrthogonalRouter newInstance() {
      return new OrthogonalRouter();
    }
  }
}
