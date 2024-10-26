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
import org.graphper.util.CollectionUtils;

class BoxGuideLineRouter extends AbstractFdpLineRouter {

  public BoxGuideLineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, fdpGraph);
  }

  @Override
  public void route() {

  }

  @Override
  protected void handle(FLine line) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
    if (CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }


  }
}
