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

package org.graphper.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.def.UnaryConcatIterable;
import org.graphper.def.ConcatIterable;
import org.graphper.def.Digraph.EdgeDigraph;
import org.graphper.def.EdgeOpGraph;
import org.graphper.util.CollectionUtils;

public abstract class LayoutGraph<N extends ANode, E extends ALine<N, E>> implements
    EdgeOpGraph<N, E> {

  protected final Graphviz graphviz;

  protected final Map<Node, N> nodeMap;

  protected final EdgeDigraph<N, E> graph;

  protected Map<GraphContainer, GraphGroup> containerMap;

  public LayoutGraph(int capacity) {
    this(capacity, null, null);
  }

  public LayoutGraph(int capacity, Graphviz graphviz, Map<Node, N> nodeMap) {
    this.graphviz = graphviz;
    this.nodeMap = nodeMap;
    graph = newGraph(capacity);
  }

  protected abstract EdgeDigraph<N, E> newGraph(int capacity);

  public EdgeDigraph<N, E> getGraph() {
    return graph;
  }

  public GraphContainer add(N node, GraphContainer container) {
    GraphContainer originalContainer = container;
    if (container.isSubgraph()) {
      container = getGraphviz().effectiveFather(container);
    }

    // Set node parent container
    if (node.getContainer() == null || node.getContainer().isGraphviz()) {
      node.setContainer(container);
    } else if (node.getContainer().containsContainer(container)) {
      node.setContainer(container);
    }

    add(node);
    if (originalContainer != node.getContainer()) {
      GraphContainer repeatContainer;
      if (originalContainer.containsContainer(node.getContainer())) {
        repeatContainer = originalContainer;
      } else if (node.getContainer().containsContainer(originalContainer)) {
        repeatContainer = node.getContainer();
      } else {
        return container;
      }

      addContainerGroupForRepeatNodes(repeatContainer);
      GraphGroup graphGroup = containerMap().get(repeatContainer);
      graphGroup.addRepeatNode(node.getNode());
    }
    return container;
  }

  @Override
  public boolean add(N node) {
    graph.add(node);

    if (nodeMap != null) {
      nodeMap.put(node.getNode(), node);
    }

    if (notAddChildContainer(node)) {
      return true;
    }

    GraphContainer container = node.getContainer();
    addContainerGroup(container);

    return true;
  }

  @Override
  public boolean remove(Object v) {
    return graph.remove(v);
  }

  @Override
  public Iterable<E> adjacent(Object v) {
    return graph.adjacent(v);
  }

  @Override
  public Iterable<E> edges() {
    return graph.edges();
  }

  @Override
  public void addEdge(E edge) {
    graph.addEdge(edge);

    if (notAddChildContainer(edge.from()) || notAddChildContainer(edge.to())) {
      return;
    }

    N from = edge.from();
    N to = edge.to();
    GraphContainer container;

    if (containsContainer(graphviz, from.getContainer(), to.getContainer())) {
      container = from.getContainer();
    } else if (containsContainer(graphviz, to.getContainer(), from.getContainer())) {
      container = to.getContainer();
    } else {
      container = commonParent(graphviz, edge.from(), edge.to());
    }

    GraphGroup graphGroup = containerMap().computeIfAbsent(container, GraphGroup::new);
    if (!graphGroup.contains(edge)) {
      graphGroup.addPatchLine(edge);
    }
  }

  public N getNode(Node node) {
    return nodeMap != null ? nodeMap.get(node) : null;
  }

  public Iterable<N> nodes(GraphContainer graphContainer) {
    if (graphviz == null) {
      return Collections.emptyList();
    }
    if (graphviz == graphContainer) {
      return graph;
    }

    GraphGroup graphGroup = containerMap().get(graphContainer);
    return graphGroup == null ? Collections.emptySet() : graphGroup.nodes();
  }

  public Iterable<Line> lines(GraphContainer graphContainer) {
    if (graphviz == null) {
      return Collections.emptyList();
    }
    if (graphviz == graphContainer) {
      return graphviz.lines();
    }

    GraphGroup graphGroup = containerMap().get(graphContainer);
    return graphGroup == null ? Collections.emptySet() : graphGroup.lines();
  }

  @Override
  public int vertexNum() {
    return graph.vertexNum();
  }

  @Override
  public int edgeNum() {
    return graph.edgeNum();
  }

  @Override
  public int degree(N n) {
    return graph.degree(n);
  }

  @Override
  public int selfLoops(N n) {
    return graph.selfLoops(n);
  }

  @Override
  public N start() {
    return graph.start();
  }

  @Override
  public boolean contains(N n) {
    return graph.contains(n);
  }

  @Override
  public N pre(N n) {
    return graph.pre(n);
  }

  @Override
  public N next(N n) {
    return graph.next(n);
  }

  @Override
  public int maxDegree() {
    return graph.maxDegree();
  }

  @Override
  public double averageDegree() {
    return graph.maxDegree();
  }

  @Override
  public int numberOfLoops() {
    return graph.numberOfLoops();
  }

  @Override
  public N[] toArray() {
    return graph.toArray();
  }

  @Override
  public void clear() {
    graph.clear();
  }

  @Override
  public boolean removeEdge(E e) {
    return graph.removeEdge(e);
  }

  public E reverseEdge(E e) {
    return graph.reverseEdge(e);
  }

  @Override
  public Iterator<N> iterator() {
    return graph.iterator();
  }

  public Graphviz getGraphviz() {
    return graphviz;
  }

  public boolean isNotEmptyGraphContainer(GraphContainer graphContainer) {
    return !isEmptyGraphContainer(graphContainer);
  }

  public boolean isEmptyGraphContainer(GraphContainer graphContainer) {
    GraphGroup graphGroup = containerMap().get(graphContainer);
    return graphGroup == null || graphGroup.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public Iterable<Cluster> clusters(GraphContainer container) {
    List<Iterable<Cluster>> iterables = null;

    for (Subgraph subgraph : container.subgraphs()) {
      if (!subgraph.isTransparent()) {
        continue;
      }

      Iterable<Cluster> clusters = clusters(subgraph);
      if (clusters == null) {
        continue;
      }
      if (iterables == null) {
        iterables = new ArrayList<>(2);
      }
      iterables.add(clusters);
    }

    if (iterables == null) {
      return new UnaryConcatIterable<>(this::isNotEmptyGraphContainer, container.clusters());
    }

    iterables.add(container.clusters());
    return new UnaryConcatIterable<>(this::isNotEmptyGraphContainer, iterables);
  }

  public static <N extends ANode, E extends ALine<N, E>> boolean containsContainer(
      Graphviz graphviz,
      GraphContainer father,
      GraphContainer container) {
    if (father == null || container == null) {
      return false;
    }

    GraphContainer p = container;
    while (p != father && p != null) {
      p = graphviz.father(p);
    }
    return p != null;
  }

  /**
   * Finds up to the first common parent container containing two nodes within the specified root
   * container.
   *
   * @param graphviz root container
   * @param n        node
   * @param w        node
   * @param <N>      node type
   * @param <E>      edge type
   * @return The first common parent container of both nodes
   */
  public static <N extends ANode, E extends ALine<N, E>> GraphContainer commonParent(
      Graphviz graphviz, N n, N w) {
    GraphContainer c1 = n.getContainer();
    GraphContainer c2 = w.getContainer();

    return commonParent(graphviz, c1, c2);
  }

  /**
   * Look up the first common parent container containing two containers in the specified root
   * container.
   *
   * @param graphviz root container
   * @param c1       container one
   * @param c2       container two
   * @param <N>      node type
   * @param <E>      edge type
   * @return The first common parent container of both nodes
   */
  public static <N extends ANode, E extends ALine<N, E>> GraphContainer commonParent(
      Graphviz graphviz,
      GraphContainer c1,
      GraphContainer c2) {
    if (c1 == c2) {
      return c1;
    }

    if (graphviz.effectiveFather(c1) == c2) {
      return c2;
    }

    if (graphviz.effectiveFather(c2) == c1) {
      return c1;
    }

    GraphContainer t;
    GraphContainer tn = c1;
    GraphContainer tw = c2;
    Map<GraphContainer, GraphContainer> path = new HashMap<>(4);
    while (c1 != null || c2 != null) {
      if (c1 != null) {
        t = path.get(c1);
        if (t != null && t == tw) {
          return c1;
        }

        path.put(c1, tn);
        c1 = graphviz.effectiveFather(c1);
      }

      if (c2 != null) {
        t = path.get(c2);
        if (t != null && t == tn) {
          return c2;
        }

        path.put(c2, tw);
        c2 = graphviz.effectiveFather(c2);
      }
    }

    return c1;
  }

  private void addContainerGroup(GraphContainer container) {
    do {
      containerMap().computeIfAbsent(container, GraphGroup::new);
      container = graphviz.effectiveFather(container);
    } while (!container.isGraphviz());
  }

  private void addContainerGroupForRepeatNodes(GraphContainer container) {
    do {
      containerMap().computeIfAbsent(container, GraphGroup::new);
      container = graphviz.effectiveFather(container);
    } while (container != null);
  }

  private boolean notAddChildContainer(N node) {
    return graphviz == null || node.getContainer() == graphviz || node.isVirtual();
  }

  private Map<GraphContainer, GraphGroup> containerMap() {
    if (containerMap == null) {
      containerMap = new HashMap<>(graphviz != null ? graphviz.clusters().size() : 1);
    }

    return containerMap;
  }

  //------------------------------ GraphGroup ------------------------------

  private class GraphGroup {

    private final GraphContainer container;

    private Boolean isEmpty;

    private Set<Node> repeatNodes;

    private List<Line> patchLines;

    private final UnaryConcatIterable<Node> containerNodes;

    @SuppressWarnings("unchecked")
    private GraphGroup(GraphContainer container) {
      this.container = container;
      this.containerNodes = new UnaryConcatIterable<>(
          node -> repeatNodes == null || !repeatNodes.contains(node), container.nodes());
    }

    private boolean isEmpty() {
      if (isEmpty != null) {
        return isEmpty;
      }

      isEmpty = true;
      for (N n : nodes()) {
        isEmpty = false;
        break;
      }

      return isEmpty;
    }

    private boolean contains(E line) {
      return container.containsLine(line.getLine());
    }

    private void addPatchLine(E line) {
      if (patchLines == null) {
        patchLines = new ArrayList<>(2);
      }

      patchLines.add(line.getLine());
    }

    @SuppressWarnings("unchecked")
    private ConcatIterable<Node, N> nodes() {
      return new ConcatIterable<>(this::nodeFilter, nodeMap::get, containerNodes, repeatNodes);
    }

    @SuppressWarnings("unchecked")
    private UnaryConcatIterable<Line> lines() {
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
        return new UnaryConcatIterable<>(this::lineFilter, container.lines(), patchLines);
      }

      iterables.add(new UnaryConcatIterable<>(this::lineFilter, container.lines(), patchLines));
      return new UnaryConcatIterable<>(this::lineFilter, iterables);
    }

    private void addRepeatNode(Node node) {
      if (node == null) {
        return;
      }
      if (repeatNodes == null) {
        repeatNodes = new LinkedHashSet<>();
      }
      repeatNodes.add(node);
    }

    private boolean nodeFilter(N node) {
      GraphContainer c = node.getContainer();
      return containsContainer(graphviz, container, c);
    }

    private boolean lineFilter(Line line) {
      N from = nodeMap.get(line.tail());
      N to = nodeMap.get(line.head());

      GraphContainer c = commonParent(graphviz, from, to);
      return containsContainer(graphviz, container, c);
    }
  }
}
