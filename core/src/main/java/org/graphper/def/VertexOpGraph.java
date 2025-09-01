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
 * The graph that operates in the vertex dimension, which means that an edge cannot carry any
 * information other than two vertices.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public interface VertexOpGraph<V> extends BaseGraph<V> {

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

  /**
   * Performs the given action for each adjacent vertex of the specified vertex until all adjacent
   * vertices have been processed or the action throws an exception.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each adjacent vertex
   * @throws NullPointerException if the specified action is null
   */
  default void forEachAdjacent(Object v, Consumer<V> action) {
    Objects.requireNonNull(action);
    for (V adjacent : adjacent(v)) {
      action.accept(adjacent);
    }
  }
}
