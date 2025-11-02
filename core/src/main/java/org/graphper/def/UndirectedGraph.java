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
 * Vertex undirected graph based on {@link AbstractAdjGraph}.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public class UndirectedGraph<V> extends AbstractAdjGraph<V, V>
    implements Graph.VertexGraph<V> {

  private static final long serialVersionUID = -7910958796521952954L;

  public UndirectedGraph() {
    super();
  }

  public UndirectedGraph(int capacity) {
    super(capacity);
  }

  @Override
  public void addEdge(V v, V w) {
    Objects.requireNonNull(v, "Vertex v cannot be null");
    Objects.requireNonNull(w, "Vertex w cannot be null");

    // Add vertices if they don't exist
    add(v);
    add(w);

    // Get adjacency lists
    AdjacencyList<V, V> adjV = edgeMap.get(v);
    AdjacencyList<V, V> adjW = edgeMap.get(w);

    // Add edge in both directions for undirected graph
    // For self-loops, only add once since adjV and adjW are the same
    adjV.add(w);
    if (!Objects.equals(v, w)) {
      adjW.add(v);
    }
    edgeNum++;
  }

  @Override
  public boolean removeEdge(Object v, Object w) {
    if (v == null || w == null) {
      return false;
    }

    AdjacencyList<V, V> adjV = edgeMap.get(v);
    AdjacencyList<V, V> adjW = edgeMap.get(w);

    if (adjV == null || adjW == null) {
      return false;
    }

    boolean removedV = adjV.removeFirstIf(adj -> Objects.equals(adj, w));
    boolean removedW = adjW.removeFirstIf(adj -> Objects.equals(adj, v));

    if (removedV || removedW) {
      edgeNum--;
      return true;
    }

    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
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
  public double averageDegree() {
    return vertexNum() != 0 ? (double) 2 * edgeNum() / vertexNum() : 0;
  }

  @Override
  protected AdjacencyList<V, V> newAdjacentList(V v) {
    return AdjacencyList.forUndirectedVertices(v);
  }

  @Override
  protected void adjustAdjWhenRemoveNode(V v, AdjacencyList<V, V> adj) {
    adj.removeFirstIf(adjacent -> Objects.equals(adjacent, v));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + UndirectedGraph.class.hashCode();
  }

  @Override
  public String toString() {
    return "UndirectedGraph{" + super.toString().substring(super.toString().indexOf('{') + 1);
  }
}
