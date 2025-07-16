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
 * The de-directed graph that operates in the edge dimension.
 *
 * @param <V> the type of vertex
 * @param <E> the type of directed edge
 * @author Jamison Jiang
 */
public interface EdgeDedigraph<V, E extends DirectedEdge<V, E>>
    extends Dedigraph<V>, Digraph.EdgeDigraph<V, E> {

  /**
   * Returns a copy of the {@code EdgeDedigraph}.
   *
   * @return a copy of current graph
   */
  @Override
  EdgeDedigraph<V, E> copy();

  /**
   * Returns a directed graph reversed from the current directed graph.
   *
   * @return directed graph reversed from the current directed graph
   */
  @Override
  EdgeDedigraph<V, E> reverse();

  /**
   * Returns all edges adjacent to the specified vertex.
   *
   * <p>For a de-directed graph, "adjacent" contains edges in both directions. The function seems
   * to fall back to the state of {@link org.graphper.def.Graph.EdgeGraph#adjacent(Object)} from
   * {@link org.graphper.def.Digraph.EdgeDigraph#adjacent(Object)}.
   *
   * @param v vertex to be queried
   * @return all adjacent edges
   */
  @Override
  Iterable<E> adjacent(Object v);

  /**
   * Returns all incoming edges representing the vertex.
   *
   * @param v vertex to be queried
   * @return all incoming adjacent edges
   */
  Iterable<E> inAdjacent(Object v);

  /**
   * Returns all outgoing edges representing the vertex.
   *
   * @param v vertex to be queried
   * @return all outgoing adjacent edges
   */
  Iterable<E> outAdjacent(Object v);

  /**
   * Performs the given action for each incoming adjacent edge of the specified vertex until all
   * incoming adjacent edges have been processed or the action throws an exception.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each incoming adjacent edge
   * @throws NullPointerException if the specified action is null
   */
  default void forEachInAdjacent(Object v, Consumer<E> action) {
    Objects.requireNonNull(action);
    for (E adjacent : inAdjacent(v)) {
      action.accept(adjacent);
    }
  }

  /**
   * Performs the given action for each outgoing adjacent edge of the specified vertex until all
   * outgoing adjacent edges have been processed or the action throws an exception.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each outgoing adjacent edge
   * @throws NullPointerException if the specified action is null
   */
  default void forEachOutAdjacent(Object v, Consumer<E> action) {
    Objects.requireNonNull(action);
    for (E adjacent : outAdjacent(v)) {
      action.accept(adjacent);
    }
  }
}
