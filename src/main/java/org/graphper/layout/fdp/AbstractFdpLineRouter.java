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
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.ANode;
import org.graphper.layout.LineClip;
import org.graphper.layout.LineRouter;
import org.graphper.layout.PortHelper;
import org.graphper.layout.PortHelper.PortPoint;
import org.graphper.util.CollectionUtils;

/**
 * Abstract base class for implementing force-directed placement (FDP) line routing strategies.
 *
 * @author Jamison Jiang
 */
abstract class AbstractFdpLineRouter extends LineClip implements LineRouter {

  /**
   * Constructs an FDP line router with the specified drawing graph and layout graph.
   *
   * @param drawGraph the {@link DrawGraph} used for managing drawing properties
   * @param fdpGraph  the {@link FdpGraph} used for force-directed placement layout
   */
  protected AbstractFdpLineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    this.drawGraph = drawGraph;
    this.layoutGraph = fdpGraph;
  }

  /**
   * Routes all lines in the layout graph using the implemented routing logic.
   *
   * <p>Iterates through all nodes and their outgoing edges in the {@link FdpGraph}.
   * For each line:
   * <ul>
   *   <li>Skips virtual lines and self-loops.</li>
   *   <li>If the line is a parallel merge and not virtual, invokes specific handling logic.</li>
   *   <li>Delegates other lines to the {@link #handle(FLine)} method for routing.</li>
   * </ul>
   */
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

  /**
   * Handles routing of a single line.
   *
   * <p>This method must be implemented by subclasses to define the specific routing logic
   * for each line in the graph.</p>
   *
   * @param line the {@link FLine} to be routed
   */
  protected abstract void handle(FLine line);

  /**
   * Draws a straight line between the tail and head nodes of the specified line.
   *
   * <p>This method directly connects the tail and head points of the line with a straight segment.
   * It skips processing if the line has already been drawn.</p>
   *
   * @param fLine the {@link FLine} to draw as a straight line
   */
  protected void drawStraightLine(FLine fLine) {
    if (fLine == null) {
      return;
    }

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(fLine.getLine());
    if (CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }

    ANode tail = layoutGraph.getNode(lineDrawProp.getLine().tail());
    ANode head = layoutGraph.getNode(lineDrawProp.getLine().head());
    PortPoint tailPoint = PortHelper.getPortPoint(lineDrawProp.getLine(), tail, drawGraph);
    PortPoint headPoint = PortHelper.getPortPoint(lineDrawProp.getLine(), head, drawGraph);

    lineDrawProp.markIsLineSegment();
    lineDrawProp.add(tailPoint);
    lineDrawProp.add(headPoint);
  }
}