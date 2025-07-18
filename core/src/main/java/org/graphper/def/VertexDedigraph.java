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
 * The de-directed graph that operates in the vertex dimension.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public interface VertexDedigraph<V> extends Dedigraph<V>, Digraph.VertexDigraph<V> {

  /**
   * Returns a de-directed graph reversed from the current directed graph.
   *
   * @return de-directed graph reversed from the current directed graph
   */
  @Override
  VertexDedigraph<V> reverse();

  /**
   * Returns all vertices adjacent to the specified vertex, each pair of adjacent vertex and the
   * current vertex represents an edge.
   *
   * <p>For bidirectional directed graphs, the adjacent method returns all neighbor vertices 
   * connected to the specified vertex in both directions (both incoming and outgoing neighbors). 
   * This is different from single-direction directed graphs where adjacent returns out-neighbors only.
   *
   * @param v vertex to be queried
   * @return all adjacent vertices (both incoming and outgoing neighbors)
   */
  @Override
  Iterable<V> adjacent(Object v);

  /**
   * Returns the adjacent vertices representing all incoming edges of the vertex. The number of
   * vertices corresponds to the in-degree ({@link #inDegree(Object)}).
   *
   * @param v vertex to be queried
   * @return all incoming adjacent vertices
   */
  Iterable<V> inAdjacent(Object v);

  /**
   * Returns the adjacent vertices representing all outgoing edges of the vertex. The number of *
   * vertices corresponds to the out-degree ({@link #outDegree(Object)}).
   *
   * @param v vertex to be queried
   * @return all outgoing adjacent vertices
   */
  Iterable<V> outAdjacent(Object v);

  /**
   * Performs the given action for each incoming adjacent vertex of the specified vertex until all
   * incoming adjacent vertices have been processed or the action throws an exception.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each incoming adjacent vertex
   * @throws NullPointerException if the specified action is null
   */
  default void forEachInAdjacent(Object v, Consumer<V> action) {
    Objects.requireNonNull(action);
    for (V adjacent : inAdjacent(v)) {
      action.accept(adjacent);
    }
  }

  /**
   * Performs the given action for each outgoing adjacent vertex of the specified vertex until all
   * outgoing adjacent vertices have been processed or the action throws an exception.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each outgoing adjacent vertex
   * @throws NullPointerException if the specified action is null
   */
  default void forEachOutAdjacent(Object v, Consumer<V> action) {
    Objects.requireNonNull(action);
    for (V adjacent : outAdjacent(v)) {
      action.accept(adjacent);
    }
  }
}
