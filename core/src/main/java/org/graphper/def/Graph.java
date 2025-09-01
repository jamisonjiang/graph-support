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
 * Base class for undirected graphs.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public interface Graph<V> extends BaseGraph<V> {

  /**
   * Returns the number of all vertex neighbors. Returns 0 if the vertex does not exist in the
   * graph.
   *
   * @param v vertex to be queried
   * @return degree of this vertex in current graph
   */
  @Override
  int degree(V v);

  /**
   * The undirected graph that operates in the vertex dimension.
   *
   * @param <V> the type of vertex
   */
  interface VertexGraph<V> extends Graph<V>, VertexOpGraph<V> {

    /**
     * Returns all vertices adjacent to the specified vertex, each pair of adjacent vertex and the
     * current vertex represents an edge.
     *
     * <p>For undirected graphs, adjacent elements are symmetrical. This means that for
     * two adjacent vertices <tt>v</tt> and <tt>w</tt>, <tt>w</tt> must be found in
     * {@code adjacent(v)}, and <tt>v</tt> must be found in {@code adjacent(w)}.
     *
     * <p>For undirected graphs, the adjacent method returns all neighbor vertices connected to the
     * specified vertex, regardless of direction (since undirected graphs have no concept of
     * direction).
     *
     * @param v vertex to be queried
     * @return all adjacent vertices (all neighbors)
     */
    @Override
    Iterable<V> adjacent(Object v);
  }

  /**
   * The undirected graph that operates in the edge dimension.
   *
   * @param <V> the type of vertex
   * @param <E> the type of undirected edge
   */
  interface EdgeGraph<V, E extends Edge<V, E>> extends Graph<V>, EdgeOpGraph<V, E> {

    /**
     * Returns all edges adjacent to the specified vertex.
     *
     * <p>For undirected graphs, adjacent elements are symmetrical. This means that for
     * two adjacent vertices <tt>v</tt> and <tt>w</tt>, <tt>w</tt> must be found in
     * {@code adjacent(v)}, and <tt>v</tt> must be found in {@code adjacent(w)}.
     *
     * <p>For undirected graphs, the adjacent method returns all neighbor edges connected to the
     * specified vertex, regardless of direction (since undirected graphs have no concept of
     * direction).
     *
     * @param v vertex to be queried
     * @return all adjacent edges (all neighboring edges)
     */
    @Override
    Iterable<E> adjacent(Object v);
  }
}