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

import java.util.List;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.def.Digraph.EdgeDigraph;
import org.graphper.def.DirectedEdgeGraph;
import org.graphper.layout.LayoutGraph;

class DotDigraph extends LayoutGraph<DNode, DLine> {

  public DotDigraph(int capacity) {
    super(capacity);
  }

  public DotDigraph(int capacity, Graphviz graphviz, Map<Node, DNode> nodeMap) {
    super(capacity, graphviz, nodeMap);
  }

  @Override
  protected EdgeDigraph<DNode, DLine> newGraph(int capacity) {
    return new DirectedEdgeGraph<>(capacity);
  }

  void suspendEdges(List<DLine> lines) {
    for (int i = 0; i < lines.size(); i++) {
      DLine line = lines.get(i);
      if (graph.removeEdge(line)) {
        continue;
      }
      for (int j = 0; j < i; j++) {
        graph.addEdge(lines.get(j));
      }
      throw new IllegalStateException("Can not suspend non-constraint line: " + line);
    }
  }

  void restoreEdges(List<DLine> lines) {
    for (DLine line : lines) {
      graph.addEdge(line);
    }
  }
}
