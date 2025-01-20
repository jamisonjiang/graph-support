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
 * Basic identification of graph directed edges.
 *
 * @param <V> vertex type
 * @param <E> subclass type
 * @author Jamison Jiang
 */
public interface DirectedEdge<V, E extends DirectedEdge<V, E>> extends BaseEdge<V, E> {

  /**
   * Return weight of edge.
   *
   * @return weight of edge
   */
  @Override
  double weight();

  /**
   * Returns another vertex of the edge based on the vertex of the edge, or null if the vertex is
   * not part of the edge.
   *
   * @param v endpoint of edge
   * @return edge another vertex
   */
  @Override
  V other(V v);

  /**
   * Returns starting vertex of the directed edge.
   *
   * @return starting vertex of the directed edge
   */
  V from();

  /**
   * Returns ending vertex of the directed edge.
   *
   * @return ending vertex of the directed edge
   */
  V to();

  /**
   * Returns a flipped directed edge.
   *
   * @return flipped edge
   */
  E reverse();

  /**
   * Create a copy of the edge.
   *
   * @return a copy of the edge
   */
  @Override
  E copy();
}
