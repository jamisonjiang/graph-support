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

package org.graphper.def;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Vertex directed graph based on {@link AbstractAdjGraph}.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public class DirectedGraph<V> extends AbstractAdjGraph<V, V>
    implements Digraph.VertexDigraph<V> {

  private static final long serialVersionUID = -7910958796521952955L;

  public DirectedGraph() {
    super();
  }

  public DirectedGraph(int capacity) {
    super(capacity);
  }

  @Override
  public void addEdge(V v, V w) {
    Objects.requireNonNull(v, "Vertex v cannot be null");
    Objects.requireNonNull(w, "Vertex w cannot be null");

    // Add vertices if they don't exist
    add(v);
    add(w);

    // Get adjacency list for source vertex
    AdjacencyList<V, V> adjV = edgeMap.get(v);

    // Add edge only in the direction v -> w for directed graph
    adjV.add(w);
    edgeNum++;
  }

  @Override
  public boolean removeEdge(Object v, Object w) {
    if (v == null || w == null) {
      return false;
    }

    AdjacencyList<V, V> adjV = edgeMap.get(v);
    if (adjV == null) {
      return false;
    }

    if (adjV.removeIf(adj -> Objects.equals(adj, w))) {
      edgeNum--;
      return true;
    }

    return false;
  }

  @Override
  public Iterable<V> adjacent(Object v) {
    AdjacencyList<V, V> adj = edgeMap.get(v);
    return adj != null ? adj : new AdjacencyList<>((V) v);
  }

  @Override
  public void forEachAdjacent(Object v, Consumer<V> action) {
    Objects.requireNonNull(action);
    AdjacencyList<V, V> adj = edgeMap.get(v);
    if (adj != null) {
      adj.forEach(action);
    }
  }

  @Override
  public DirectedGraph<V> reverse() {
    DirectedGraph<V> reversed = new DirectedGraph<>(vertexNum());

    // Copy all vertices
    for (V vertex : this) {
      reversed.add(vertex);
    }

    // Copy all edges in reverse direction
    for (V vertex : this) {
      for (V adjacent : adjacent(vertex)) {
        reversed.addEdge(adjacent, vertex);
      }
    }

    return reversed;
  }

  @Override
  protected AdjacencyList<V, V> newAdjacentList(V v) {
    return AdjacencyList.forDirectedVertices(v);
  }

  @Override
  protected boolean adjustAdjWhenRemoveNode(V v, AdjacencyList<V, V> adj) {
    return adj.removeIf(adjacent -> Objects.equals(adjacent, v));
  }
}
