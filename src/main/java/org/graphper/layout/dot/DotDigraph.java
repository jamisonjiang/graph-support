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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.def.BiConcatIterable;
import org.graphper.def.ConcatIterable;
import org.graphper.def.DirectedEdgeGraph;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;

class DotDigraph extends DirectedEdgeGraph<DNode, DLine> {

  private static final long serialVersionUID = -4312972825511898843L;

  private final Graphviz graphviz;

  private final Map<Node, DNode> nodeMap;

  private Map<GraphContainer, GraphGroup> containerMap;

  DotDigraph(int capacity) {
    this(capacity, null, null);
  }

  DotDigraph(int capacity, Graphviz graphviz, Map<Node, DNode> nodeMap) {
    super(capacity);
    this.graphviz = graphviz;
    this.nodeMap = nodeMap;
  }

  DNode getDNode(Node node) {
    return nodeMap != null ? nodeMap.get(node) : null;
  }

  @Override
  public boolean add(DNode node) {
    super.add(node);

    if (notAddChildContainer(node)) {
      return true;
    }

    GraphContainer container = node.getContainer();
    addContainerGroup(container);

    return true;
  }

  @Override
  public void addEdge(DLine edge) {
    super.addEdge(edge);

    if (notAddChildContainer(edge.from()) || notAddChildContainer(edge.to())) {
      return;
    }

    DNode from = edge.from();
    DNode to = edge.to();
    GraphContainer container;

    if (!DotAttachment.notContain(graphviz, from.getContainer(), to.getContainer())) {
      container = from.getContainer();
    } else if (!DotAttachment.notContain(graphviz, to.getContainer(), from.getContainer())) {
      container = to.getContainer();
    } else {
      container = DotAttachment.commonParent(graphviz, edge.from(), edge.to());
    }

    GraphGroup graphGroup = containerMap().computeIfAbsent(container, GraphGroup::new);
    if (!graphGroup.contains(edge)) {
      graphGroup.addPatchLine(edge);
    }
  }

  Iterable<DNode> nodes(GraphContainer graphContainer) {
    if (graphviz == null) {
      return Collections.emptyList();
    }
    if (graphviz == graphContainer) {
      return this;
    }

    GraphGroup graphGroup = containerMap().get(graphContainer);
    return graphGroup == null ? Collections.emptySet() : graphGroup.nodes();
  }

  Iterable<Line> lines(GraphContainer graphContainer) {
    if (graphviz == null) {
      return Collections.emptyList();
    }
    if (graphviz == graphContainer) {
      return graphviz.lines();
    }

    GraphGroup graphGroup = containerMap().get(graphContainer);
    return graphGroup == null ? Collections.emptySet() : graphGroup.lines();
  }

  private void addContainerGroup(GraphContainer container) {
    do {
      containerMap().computeIfAbsent(container, GraphGroup::new);
      container = graphviz.effectiveFather(container);
    } while (!container.isGraphviz());
  }

  private Map<GraphContainer, GraphGroup> containerMap() {
    if (containerMap == null) {
      containerMap = new HashMap<>(graphviz != null ? graphviz.clusters().size() : 1);
    }

    return containerMap;
  }

  private boolean notAddChildContainer(DNode node) {
    return graphviz == null || node.getContainer() == graphviz || node.isVirtual();
  }

  //------------------------------ GraphGroup ------------------------------

  private class GraphGroup {

    private final GraphContainer container;

    private List<Line> patchLines;

    private GraphGroup(GraphContainer container) {
      this.container = container;
    }

    private boolean contains(DNode node) {
      return container.containsNode(node.getNode());
    }

    private boolean contains(DLine line) {
      return container.containsLine(line.getLine());
    }

    private void addPatchLine(DLine line) {
      if (patchLines == null) {
        patchLines = new ArrayList<>(2);
      }

      patchLines.add(line.getLine());
    }

    private ConcatIterable<Node, DNode> nodes() {
      return new ConcatIterable<>(this::nodeFilter, nodeMap::get, container.nodes());
    }

    private BiConcatIterable<Line> lines() {
      List<Iterable<Line>> iterables = null;
      for (Cluster cluster : container.clusters()) {
        GraphGroup graphGroup = containerMap().get(cluster);
        if (graphGroup == null) {
          continue;
        }

        if (iterables == null) {
          iterables = new ArrayList<>(2);
        }
        iterables.add(graphGroup.lines());
      }
      if (CollectionUtils.isEmpty(iterables)) {
        return new BiConcatIterable<>(this::lineFilter, container.lines(), patchLines);
      }

      iterables.add(new BiConcatIterable<>(this::lineFilter, container.lines(), patchLines));
      return new BiConcatIterable<>(this::lineFilter, iterables);
    }

    private boolean nodeFilter(DNode node) {
      GraphContainer c = node.getContainer();
      return !DotAttachment.notContain(graphviz, container, c);
    }

    private boolean lineFilter(Line line) {
      DNode from = nodeMap.get(line.tail());
      DNode to = nodeMap.get(line.head());

      GraphContainer c = DotAttachment.commonParent(graphviz, from, to);
      return !DotAttachment.notContain(graphviz, container, c);
    }
  }
}
