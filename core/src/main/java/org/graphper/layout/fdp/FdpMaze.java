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
import org.graphper.layout.Grid.GridBuilder;
import org.graphper.layout.Maze;

public class FdpMaze extends Maze {

  private final FdpGraph fdpGraph;

  public FdpMaze(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, true);
    this.fdpGraph = fdpGraph;
    init();
  }

  @Override
  protected void initGrid(GridBuilder gridBuilder) {
    for (FNode node : fdpGraph) {
      if (node.isVirtual()) {
        continue;
      }

      addCell(node, new NodeCell(node), gridBuilder);
    }
  }
}
