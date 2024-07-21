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

import java.util.Map;
import org.graphper.api.Node;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.LayoutAttach;
import org.graphper.util.Asserts;

public class FdpAttachment extends LayoutAttach {

  private final FdpGraph fdpGraph;

  private final Map<Node, FNode> nodeRecord;

  public FdpAttachment(FdpGraph fdpGraph, DrawGraph drawGraph, Map<Node, FNode> nodeRecord) {
    super(drawGraph);
    Asserts.nullArgument(drawGraph, "drawGraph");
    Asserts.nullArgument(fdpGraph, "fdpGraph");
    this.fdpGraph = fdpGraph;
    this.nodeRecord = nodeRecord;
  }

  FNode get(Node node) {
    return fdpGraph.getNode(node);
  }

  public FdpGraph getFdpGraph() {
    return fdpGraph;
  }

  public DrawGraph getDrawGraph() {
    return drawGraph;
  }
}
