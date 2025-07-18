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
 * Bidirectional directed graph for vertex operations.
 *
 * <p>This implementation uses Map-based adjacency storage for efficient O(1) vertex lookups
 * and better cache locality with array-based adjacency storage. The vertex type can be any object
 * and vertex operations have <tt>O(1)</tt> complexity.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public class DedirectedGraph<V> extends ProxyDedigraph<V, DirectedGraph<V>, DirectedGraph<V>>
    implements VertexDedigraph<V> {

  private static final long serialVersionUID = -7910958796521952954L;

  public DedirectedGraph() {
    this(new DirectedGraph<>(), new DirectedGraph<>());
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  public DedirectedGraph(int capacity) {
    this(new DirectedGraph<>(capacity), new DirectedGraph<>(capacity));
  }

  private DedirectedGraph(DirectedGraph<V> digraph, DirectedGraph<V> reDigraph) {
    super(digraph, reDigraph);
  }

  /**
   * Returns a directed graph reversed from the current directed graph.
   *
   * @return directed graph reversed from the current directed graph
   */
  @Override
  public DedirectedGraph<V> reverse() {
    return new DedirectedGraph<>(digraph.reverse(), reDigraph.reverse());
  }

  /**
   * Adds an edge to the graph, which may or may not be directed.
   *
   * @param v vertex of edge endpoint
   * @param w vertex of edge endpoint
   * @throws NullPointerException if the specified endpoint vertex is null
   */
  @Override
  public void addEdge(V v, V w) {
    if (Objects.isNull(v) || Objects.isNull(w)) {
      throw new NullPointerException();
    }

    digraph.addEdge(v, w);
    reDigraph.addEdge(w, v);
  }

  /**
   * Removes an edge to the graph. If the graph changes due to removing this edge, return true.
   *
   * @param v vertex of edge endpoint
   * @param w vertex of edge endpoint
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  @Override
  public boolean removeEdge(Object v, Object w) {
    return digraph.removeEdge(v, w) && reDigraph.removeEdge(w, v);
  }

  /**
   * Returns all vertices adjacent to the specified vertex, each pair of adjacent vertex and the
   * current vertex represents an edge.
   *
   * <p>For a de-directed graph, "adjacent" contains edges in both directions. The function seems
   * to fall back to the state of {@link org.graphper.def.Graph.VertexGraph#adjacent(Object)} from
   * {@link org.graphper.def.Digraph.VertexDigraph#adjacent(Object)}.
   *
   * @param v vertex to be queried
   * @return all adjacent vertices
   */
  @Override
  public Iterable<V> adjacent(Object v) {
    return new UnaryConcatIterable<>(outAdjacent(v), inAdjacent(v));
  }

  /**
   * Returns the adjacent vertices representing all incoming edges of the vertex. The number of
   * vertices corresponds to the in-degree ({@link #inDegree(Object)}).
   *
   * @param v vertex to be queried
   * @return all incoming adjacent vertices
   */
  @Override
  public Iterable<V> inAdjacent(Object v) {
    return reDigraph.adjacent(v);
  }

  /**
   * Returns the adjacent vertices representing all outgoing edges of the vertex. The number of *
   * vertices corresponds to the out-degree ({@link #outDegree(Object)}).
   *
   * @param v vertex to be queried
   * @return all outgoing adjacent vertices
   */
  @Override
  public Iterable<V> outAdjacent(Object v) {
    return digraph.adjacent(v);
  }

  /**
   * Performs the given action for each incoming adjacent vertex of the specified vertex until all
   * incoming adjacent vertices have been processed or the action throws an exception. This method
   * delegates to the underlying reverse graph's forEachAdjacent method to avoid creating intermediate
   * iterable objects, reducing GC pressure.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each incoming adjacent vertex
   * @throws NullPointerException if the specified action is null
   */
  @Override
  public void forEachInAdjacent(Object v, Consumer<V> action) {
    Objects.requireNonNull(action);
    AdjacencyList<V, V> adj = reDigraph.edgeMap.get(v);
    if (adj == null) {
      return;
    }

    for (int i = 0; i < adj.getDegree(); i++) {
      action.accept(adj.get(i));
    }
  }

  /**
   * Performs the given action for each outgoing adjacent vertex of the specified vertex until all
   * outgoing adjacent vertices have been processed or the action throws an exception. This method
   * delegates to the underlying forward graph's forEachAdjacent method to avoid creating intermediate
   * iterable objects, reducing GC pressure.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each outgoing adjacent vertex
   * @throws NullPointerException if the specified action is null
   */
  @Override
  public void forEachOutAdjacent(Object v, Consumer<V> action) {
    Objects.requireNonNull(action);
    digraph.forEachAdjacent(v, action);
  }
}
