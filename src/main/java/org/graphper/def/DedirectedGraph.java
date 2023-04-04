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

import java.util.Collections;
import java.util.Objects;

/**
 * Bidirectional directed graph for vertex operations.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code DirectedEdgeGraph}, the vertex is
 * searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
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

  /**
   * Initialize with vertex array.
   *
   * @param vertices vertex array
   * @throws IllegalArgumentException vertex array is empty
   */
  public DedirectedGraph(V[] vertices) {
    this(new DirectedGraph<>(vertices), new DirectedGraph<>(vertices));
  }

  private DedirectedGraph(DirectedGraph<V> digraph, DirectedGraph<V> reDigraph) {
    super(digraph, reDigraph);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<V> inIte(Object v) {
    return reDigraph.adjacent(v);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<V> outIte(Object v) {
    return digraph.adjacent(v);
  }

  /**
   * Returns a copy of the {@code DedirectedGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public DedirectedGraph<V> copy() {
    return new DedirectedGraph<>(digraph.copy(), reDigraph.copy());
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
    return new BiConcatIterable<>(outAdjacent(v), inAdjacent(v));
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
    return new BiConcatIterable<>(reDigraph.adjacent(v), Collections.emptyList());
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
    return new BiConcatIterable<>(digraph.adjacent(v), Collections.emptyList());
  }
}
