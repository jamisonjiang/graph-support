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
 * Directed graph with bidirectional iteration capability.
 *
 * <p>One-way directed graphs have limitations, and there is no corresponding method to search from
 * a specified direction (forward or reverse). The bidirectional graph distinguishes the out-edge
 * and in-edge of vertices, and also distinguishes the out-degree and in-degree of vertices, which
 * can facilitate the search of the graph.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 * @see EdgeDedigraph Edge Bidirectional Iterative Directed Graph
 * @see VertexDedigraph Vertex Bidirectional Iterative Directed Graph
 */
public interface Dedigraph<V> extends Digraph<V> {

  /**
   * Returns the in-degree of the vertex. Returns 0 if the vertex does not exist in the graph.
   *
   * @param v vertex to be queried
   * @return in-degree of this vertex in current graph
   */
  int inDegree(V v);

  /**
   * Returns the out-degree of the vertex. Returns 0 if the vertex does not exist in the graph.
   *
   * @param v vertex to be queried
   * @return out-degree of this vertex in current graph
   */
  int outDegree(V v);

  /**
   * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
   *
   * <p>For bidirectional directed graphs, degree indicates all neighbor nodes (sum of in-degree 
   * and out-degree). This is different from single-direction directed graphs where degree 
   * indicates the out-degree only.
   *
   * @param v vertex to be queried
   * @return degree of this vertex in current graph (in-degree + out-degree)
   */
  @Override
  int degree(V v);

  /**
   * Returns a directed graph reversed from the current directed graph.
   *
   * @return directed graph reversed from the current directed graph
   */
  @Override
  Dedigraph<V> reverse();
}
