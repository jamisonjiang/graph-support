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

import org.graphper.api.Line;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.LineClip;
import org.graphper.layout.LineRouter;
import org.graphper.util.CollectionUtils;

public abstract class AbstractFdpLineRouter extends LineClip implements LineRouter {

  protected AbstractFdpLineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
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

  protected void drawStraightLine(FLine fLine) {
    if (fLine == null) {
      return;
    }

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(fLine.getLine());
    if (CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }

    Line line = lineDrawProp.getLine();
    NodeDrawProp head = drawGraph.getNodeDrawProp(line.head());
    NodeDrawProp tail = drawGraph.getNodeDrawProp(line.tail());

    lineDrawProp.markIsLineSegment();
    lineDrawProp.add(new FlatPoint(tail.getX(), tail.getY()));
    lineDrawProp.add(new FlatPoint(head.getX(), head.getY()));
  }
}
