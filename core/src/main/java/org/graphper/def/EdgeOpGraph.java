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
 * Use an additional edge object to represent the graph of two connected vertices, where the type of
 * the edge must be a subtype of {@link BaseEdge}, which means that the edge can carry information
 * other than the two vertices.
 *
 * @param <V> the type of vertex
 * @param <E> the type of edge
 * @author Jamison Jiang
 */
public interface EdgeOpGraph<V, E extends BaseEdge<V, E>> extends BaseGraph<V> {

  /**
   * Returns a copy of the {@code EdgeOpGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  EdgeOpGraph<V, E> copy();

  /**
   * Adds an edge to the graph, which may or may not be directed.
   *
   * @param e edge to be added to this graph
   * @throws NullPointerException if the edge is null
   */
  void addEdge(E e);

  /**
   * Removes an edge to the graph. If the graph changes due to removing this edge, return true.
   *
   * @param e edge to be removed to this graph
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  boolean removeEdge(E e);

  /**
   * Returns all edges adjacent to the specified vertex.
   *
   * @param v vertex to be queried
   * @return all adjacent edges
   */
  Iterable<E> adjacent(Object v);

  /**
   * Performs the given action for each adjacent edge of the specified vertex until all adjacent
   * edges have been processed or the action throws an exception. This method avoids creating
   * intermediate iterable objects, reducing GC pressure.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each adjacent edge
   * @throws NullPointerException if the specified action is null
   */
  default void forEachAdjacent(Object v, Consumer<E> action) {
    Objects.requireNonNull(action);
    for (E adjacent : adjacent(v)) {
      action.accept(adjacent);
    }
  }

  /**
   * Returns all edges in the graph.
   *
   * @return all edges in the graph
   */
  Iterable<E> edges();

  /**
   * Performs the given action for each edge of the graph until all edges have been processed or the
   * action throws an exception. Unless otherwise specified by the implementing class, actions are
   * performed in the order of {@link #edges()}(if an iteration order is specified). Exceptions
   * thrown by the action are relayed to the caller.
   *
   * @param action The action to be performed for each element
   * @throws NullPointerException if the specified action is null
   */
  default void forEachEdges(Consumer<E> action) {
    Objects.requireNonNull(action);
    for (E edge : edges()) {
      action.accept(edge);
    }
  }
}
