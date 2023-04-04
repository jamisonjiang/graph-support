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
 * Basic identification of graph edges.
 *
 * @param <V> vertex type
 * @param <B> subclass type
 * @author Jamison Jiang
 * @see Edge undirected edge
 * @see DirectedEdge directed edge
 */
public interface BaseEdge<V, B extends BaseEdge<V, B>> {

  /**
   * Returns weight of edge.
   *
   * @return weight of edge
   */
  double weight();

  /**
   * Returns an endpoint of an edge.
   *
   * @return edge endpoint
   */
  V either();

  /**
   * Returns another vertex of the edge based on the vertex of the edge, or null if the vertex is
   * not part of the edge.
   *
   * @param v endpoint of edge
   * @return edge another vertex
   */
  V other(V v);

  /**
   * Create a copy of the edge.
   *
   * @return a copy of the edge
   */
  B copy();
}
