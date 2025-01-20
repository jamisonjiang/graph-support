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

/**
 * Base class for directed graphs, the graph can only be sought from one direction specified.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public interface Digraph<V> extends BaseGraph<V> {

  /**
   * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.The
   * degree has no direction, and this interface returns the sum of the in and out degrees of this
   * vertex.
   *
   * @param v vertex to be queried
   * @return degree of this vertex in current graph
   */
  @Override
  int degree(V v);

  /**
   * Returns a copy of the {@code Digraph}.
   *
   * @return a copy of current graph
   */
  @Override
  Digraph<V> copy();

  /**
   * Returns a directed graph reversed from the current directed graph.
   *
   * @return directed graph reversed from the current directed graph
   */
  Digraph<V> reverse();

  /**
   * The directed graph that operates in the vertex dimension.
   *
   * @param <V> the type of vertex
   */
  interface VertexDigraph<V> extends Digraph<V>, VertexOpGraph<V> {

    /**
     * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
     *
     * <p>Unlike {@link #adjacent(Object)}, the degree has no direction, and this interface returns
     * the sum of the in and out degrees of this vertex.
     *
     * @param v vertex to be queried
     * @return degree of this vertex in current graph
     */
    @Override
    int degree(V v);

    /**
     * Returns a copy of the {@code VertexDigraph}.
     *
     * @return a copy of current graph
     */
    @Override
    VertexDigraph<V> copy();

    /**
     * Returns a directed graph reversed from the current directed graph.
     *
     * <p>Since for directed graphs, some methods are directional (such as {@link #adjacent}),
     * choosing an inversion graph can usually get some bidirectional properties.
     *
     * @return directed graph reversed from the current directed graph
     */
    @Override
    VertexDigraph<V> reverse();

    /**
     * Returns all vertices adjacent to the specified vertex, each pair of adjacent vertex and the
     * current vertex represents an edge.
     *
     * <p>For a directed graph, "adjacent" has a direction, so for a directed edge <tt>(v,w)</tt>,
     * {@code adjacent(v)} must contain <tt>w</tt>, but in turn {@code adjacent(w)} not contain
     * <tt>v</tt> (assuming there is no edge from w to v in the graph).
     *
     * @param v vertex to be queried
     * @return all adjacent vertices
     */
    @Override
    Iterable<V> adjacent(Object v);
  }

  /**
   * The directed graph that operates in the edge dimension.
   *
   * @param <V> the type of vertex
   * @param <E> the type of directed edge
   */
  interface EdgeDigraph<V, E extends DirectedEdge<V, E>> extends Digraph<V>, EdgeOpGraph<V, E> {

    /**
     * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
     *
     * <p>Unlike {@link #adjacent(Object)}, the degree has no direction, and this interface returns
     * the sum of the in and out degrees of this vertex.
     *
     * @param v vertex to be queried
     * @return degree of this vertex in current graph
     */
    @Override
    int degree(V v);

    /**
     * Returns a copy of the {@code EdgeDigraph}.
     *
     * @return a copy of current graph
     */
    @Override
    EdgeDigraph<V, E> copy();

    /**
     * Returns a directed graph reversed from the current directed graph.
     *
     * <p>Since for directed graphs, some methods are directional (such as {@link #adjacent}),
     * choosing an inversion graph can usually get some bidirectional properties.
     *
     * @return directed graph reversed from the current directed graph
     */
    @Override
    EdgeDigraph<V, E> reverse();

    /**
     * Returns all edges adjacent to the specified vertex.
     *
     * <p>For a directed graph, "adjacent" has a direction, so for a directed edge <tt>(v,w)</tt>,
     * {@code adjacent(v)} must contain <tt>w</tt>, but in turn {@code adjacent(w)} not contain
     * <tt>v</tt> (assuming there is no edge from w to v in the graph).
     *
     * @param v vertex to be queried
     * @return all adjacent edges
     */
    @Override
    Iterable<E> adjacent(Object v);

    /**
     * If the incoming directed edge exists in the graph, the original directed edge will be deleted
     * in the graph, and a reversed direction edge will be created and inserted into the current
     * graph, and finally the new reversed directed edge will be returned. Returns <tt>null</tt> if
     * the edge does not exist. This action is equivalent to the following sequence of actions:
     * <pre> {@code
     *   EdgeDigraph<V, E> digraph = ...;
     *   E edge = ...;
     *
     *   // Remove edge from graph
     *   if (digraph.remove(edge)) {
     *      // Manually flip edges
     *      edge = edge.reverse();
     *      // Insert the reversed edge
     *      digraph.addEdge(edge);
     *   }
     * }</pre>
     *
     * @param e edge that needs to be reversed
     * @return reversed edge
     * @throws NullPointerException if the specified edge is null
     */
    E reverseEdge(E e);
  }
}