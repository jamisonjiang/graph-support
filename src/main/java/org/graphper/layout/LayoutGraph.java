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
import java.util.List;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.def.BiConcatIterable;
import org.graphper.def.ConcatIterable;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.Digraph.EdgeDigraph;
import org.graphper.def.DirectedEdgeGraph;
import org.graphper.def.EdgeOpGraph;
import org.graphper.util.CollectionUtils;

public class LayoutGraph<N extends ANode, E extends ALine<N, E>> implements EdgeOpGraph<N, E> {

  protected final Graphviz graphviz;

  protected final Map<Node, N> nodeMap;

  protected final EdgeDigraph<N, E> graph;

  protected Map<GraphContainer, GraphGroup> containerMap;

  public LayoutGraph(int capacity) {
    this(capacity, null, null, false);
  }

  public LayoutGraph(int capacity, Graphviz graphviz, Map<Node, N> nodeMap, boolean isDedigraph) {
    this.graphviz = graphviz;
    this.nodeMap = nodeMap;
    graph = isDedigraph ? new DedirectedEdgeGraph<>(capacity) : new DirectedEdgeGraph<>(capacity);
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

    if (!notContains(graphviz, from.getContainer(), to.getContainer())) {
      container = from.getContainer();
    } else if (!notContains(graphviz, to.getContainer(), from.getContainer())) {
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
  public EdgeOpGraph<N, E> copy() {
    return graph.copy();
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

  public static <N extends ANode, E extends ALine<N, E>> Iterable<Cluster> clusters(
      GraphContainer container) {
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
      return container.clusters();
    }

    iterables.add(container.clusters());
    return new BiConcatIterable<>(iterables);
  }

  public static <N extends ANode, E extends ALine<N, E>> boolean notContains(Graphviz graphviz,
                                                                             GraphContainer father,
                                                                             GraphContainer container) {
    if (father == null || container == null) {
      return true;
    }

    GraphContainer p = container;
    while (p != father && p != null) {
      p = graphviz.father(p);
    }
    return p == null;
  }

  /**
   * Finds up to the first common parent container containing two nodes within the specified root
   * container.
   *
   * @param graphviz root container
   * @param n        node
   * @param w        node
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

  private boolean notAddChildContainer(N node) {
    return graphviz == null || node.getContainer() == graphviz || node.empty();
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

    private List<Line> patchLines;

    private GraphGroup(GraphContainer container) {
      this.container = container;
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

    private ConcatIterable<Node, N> nodes() {
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

    private boolean nodeFilter(N node) {
      GraphContainer c = node.getContainer();
      return !notContains(graphviz, container, c);
    }

    private boolean lineFilter(Line line) {
      N from = nodeMap.get(line.tail());
      N to = nodeMap.get(line.head());

      GraphContainer c = commonParent(graphviz, from, to);
      return !notContains(graphviz, container, c);
    }
  }
}
