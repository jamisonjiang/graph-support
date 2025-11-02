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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Edge directed graph based on {@link AbstractAdjGraph}.
 *
 * @param <V> the type of vertex
 * @param <E> the type of directed edge
 * @author Jamison Jiang
 */
public class DirectedEdgeGraph<V, E extends DirectedEdge<V, E>> extends AbstractAdjGraph<V, E>
    implements Digraph.EdgeDigraph<V, E> {

  private static final long serialVersionUID = -7910958796521952957L;

  public DirectedEdgeGraph() {
    super();
  }

  public DirectedEdgeGraph(int capacity) {
    super(capacity);
  }

  public DirectedEdgeGraph(E[] edges) {
    this();
    if (edges != null) {
      for (E edge : edges) {
        addEdge(edge);
      }
    }
  }

  public DirectedEdgeGraph(Collection<E> edges) {
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

    V from = e.from();
    V to = e.to();

    // Add vertices if they don't exist
    add(from);
    add(to);

    // Get adjacency list for source vertex
    AdjacencyList<V, E> adjFrom = edgeMap.get(from);

    // Add edge only from source to target for directed graph
    adjFrom.add(e);
    edgeNum++;
  }

  @Override
  public boolean removeEdge(E e) {
    if (e == null) {
      return false;
    }

    V from = e.from();
    AdjacencyList<V, E> adjFrom = edgeMap.get(from);

    if (adjFrom == null) {
      return false;
    }

    if (adjFrom.removeFirstIf(edge -> Objects.equals(edge, e))) {
      edgeNum--;
      return true;
    }

    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
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
  public E reverseEdge(E e) {
    Objects.requireNonNull(e, "Edge cannot be null");

    E reverse = e.reverse();
    removeEdge(e);
    addEdge(reverse);
    return reverse;
  }

  @Override
  public DirectedEdgeGraph<V, E> reverse() {
    DirectedEdgeGraph<V, E> reversed = new DirectedEdgeGraph<>(vertexNum());

    // Copy all vertices
    for (V vertex : this) {
      reversed.add(vertex);
    }

    // Copy all edges in reverse direction
    for (V vertex : this) {
      for (E edge : adjacent(vertex)) {
        E reversedEdge = edge.reverse();
        reversed.addEdge(reversedEdge);
      }

    }

    return reversed;
  }

  @Override
  protected AdjacencyList<V, E> newAdjacentList(V v) {
    return AdjacencyList.forDirectedEdges(v);
  }

  @Override
  protected void adjustAdjWhenRemoveNode(V v, AdjacencyList<V, E> adj) {
    AtomicInteger removeNum = new AtomicInteger(0);
    adj.removeFirstIf(adjacent -> {
      if (Objects.equals(adjacent.from(), v) || Objects.equals(adjacent.to(), v)) {
        removeNum.incrementAndGet();
        return true;
      }
      return false;
    });
    edgeNum -= removeNum.get();
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
    return super.hashCode() + DirectedEdgeGraph.class.hashCode();
  }

  @Override
  public String toString() {
    return "DirectedEdgeGraph{" + super.toString().substring(super.toString().indexOf('{') + 1);
  }
}

