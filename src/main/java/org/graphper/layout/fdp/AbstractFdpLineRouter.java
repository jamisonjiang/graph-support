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

import org.graphper.draw.DrawGraph;
import org.graphper.layout.LineHandler;
import org.graphper.layout.LineRouter;

public abstract class AbstractFdpLineRouter extends LineHandler implements LineRouter {

  public AbstractFdpLineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    this.drawGraph = drawGraph;
    this.layoutGraph = fdpGraph;
  }

  @Override
  public void route() {
    FdpGraph fdpGraph = (FdpGraph) layoutGraph;
    for (FNode node : fdpGraph) {
      for (FLine line : fdpGraph.outAdjacent(node)) {
        if (line.isVirtual() || line.isSelf()) {
          continue;
        }

        if (line.isParallelMerge() && !line.isVirtual()) {
          parallelLineHandle(line);
          continue;
        }

        handle(line);
      }
    }
  }

  protected abstract void handle(FLine line);
}
