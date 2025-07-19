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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Edge undirected graph based on {@link AbstractAdjGraph}.
 *
 * @param <V> the type of vertex
 * @param <E> the type of undirected edge
 * @author Jamison Jiang
 */
public class UndirectedEdgeGraph<V, E extends Edge<V, E>> extends AbstractAdjGraph<V, E>
    implements Graph.EdgeGraph<V, E> {

  private static final long serialVersionUID = -7910958796521952956L;

  public UndirectedEdgeGraph() {
    super();
  }

  public UndirectedEdgeGraph(int capacity) {
    super(capacity);
  }

  public UndirectedEdgeGraph(E[] edges) {
    this();
    if (edges != null) {
      for (E edge : edges) {
        addEdge(edge);
      }
    }
  }

  public UndirectedEdgeGraph(Collection<E> edges) {
    this();
    if (edges != null) {
      for (E edge : edges) {
        addEdge(edge);
      }
    }
  }

  @Override
  public void addEdge(E e) {
    Objects.requireNonNull(e, "Edge cannot be null");

    V v = e.either();
    V w = e.other(v);

    // Add vertices if they don't exist
    add(v);
    add(w);

    // Get adjacency lists
    AdjacencyList<V, E> adjV = edgeMap.get(v);
    AdjacencyList<V, E> adjW = edgeMap.get(w);

    // Add edge to both vertices for undirected graph
    // For self-loops, only add once since adjV and adjW are the same
    adjV.add(e);
    if (!Objects.equals(v, w)) {
      adjW.add(e);
    }
    edgeNum++;
  }

  @Override
  public boolean removeEdge(E e) {
    if (e == null) {
      return false;
    }

    V v = e.either();
    V w = e.other(v);

    AdjacencyList<V, E> adjV = edgeMap.get(v);
    AdjacencyList<V, E> adjW = edgeMap.get(w);

    if (adjV == null || adjW == null) {
      return false;
    }

    boolean removedV = adjV.removeIf(edge -> Objects.equals(edge, e));
    boolean removedW = adjW.removeIf(edge -> Objects.equals(edge, e));

    if (removedV || removedW) {
      edgeNum--;
      return true;
    }

    return false;
  }

  @Override
  public Iterable<E> adjacent(Object v) {
    AdjacencyList<V, E> adj = edgeMap.get(v);
    return adj != null ? adj : new AdjacencyList<>((V) v);
  }

  @Override
  public void forEachAdjacent(Object v, Consumer<E> action) {
    Objects.requireNonNull(action);
    AdjacencyList<V, E> adj = edgeMap.get(v);
    if (adj != null) {
      adj.forEach(action);
    }
  }

  @Override
  public Iterable<E> edges() {
    return EdgeIterator::new;
  }

  @Override
  public double averageDegree() {
    return vertexNum() != 0 ? (double) 2 * edgeNum() / vertexNum() : 0;
  }

  @Override
  protected AdjacencyList<V, E> newAdjacentList(V v) {
    return AdjacencyList.forUndirectedEdges(v);
  }

  @Override
  protected void adjustAdjWhenRemoveNode(V v, AdjacencyList<V, E> adj) {
    adj.removeIf(edge -> Objects.equals(edge.either(), v)
        || Objects.equals(edge.other(edge.either()), v));
  }

  /**
   * Iterator for all edges in the graph.
   */
  private class EdgeIterator implements Iterator<E> {

    private final Iterator<AdjacencyList<V, E>> adjIterator;
    private Iterator<E> currentEdgeIterator;
    private E nextEdge;
    private boolean hasNext;

    EdgeIterator() {
      this.adjIterator = edgeMap.values().iterator();
      this.hasNext = false;
      findNext();
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public E next() {
      if (!hasNext) {
        throw new java.util.NoSuchElementException();
      }
      E result = nextEdge;
      findNext();
      return result;
    }

    private void findNext() {
      while (currentEdgeIterator == null || !currentEdgeIterator.hasNext()) {
        if (!adjIterator.hasNext()) {
          hasNext = false;
          return;
        }
        currentEdgeIterator = adjIterator.next().iterator();
      }

      nextEdge = currentEdgeIterator.next();
      hasNext = true;
    }
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
    return super.hashCode() + UndirectedEdgeGraph.class.hashCode();
  }

  @Override
  public String toString() {
    return "UndirectedEdgeGraph{" + super.toString().substring(super.toString().indexOf('{') + 1);
  }
}