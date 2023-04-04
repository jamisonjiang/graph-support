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
 * The graph that operates in the vertex dimension, which means that an edge cannot carry any
 * information other than two vertices.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public interface VertexOpGraph<V> extends BaseGraph<V> {

  /**
   * Returns a copy of the {@code VertexOpGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  VertexOpGraph<V> copy();

  /**
   * Adds an edge to the graph, which may or may not be directed.
   *
   * @param v vertex of edge endpoint
   * @param w vertex of edge endpoint
   * @throws NullPointerException if the specified endpoint vertex is null
   */
  void addEdge(V v, V w);

  /**
   * Removes an edge to the graph. If the graph changes due to removing this edge, return true.
   *
   * @param v vertex of edge endpoint
   * @param w vertex of edge endpoint
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  boolean removeEdge(Object v, Object w);

  /**
   * Returns all vertices adjacent to the specified vertex, each pair of adjacent vertex and the
   * current vertex represents an edge.
   *
   * @param v vertex to be queried
   * @return all adjacent vertices
   */
  Iterable<V> adjacent(Object v);
}
