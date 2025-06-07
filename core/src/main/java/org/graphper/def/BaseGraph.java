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

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@code BaseGraph} is a basic data structure composed of vertices, each vertex has a series of
 * adjacent elements, and the vertices and their adjacent elements represent edge records. The
 * vertices of the graph are used as the main object, and directly iterating the current graph is to
 * obtain all the vertices in the graph.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 * @see Graph Undirected graph
 * @see Digraph Single out direction directed graph
 * @see Dedigraph Dual in-out direation directed graph
 * @see EdgeOpGraph Edge manipulation graph
 * @see VertexOpGraph Vertex manipulation graph
 */
public interface BaseGraph<V> extends Iterable<V> {

  /**
   * Returns the number of vertices.
   *
   * @return the number of vertices
   */
  int vertexNum();

  /**
   * Returns the number of edges.
   *
   * @return the number of edges
   */
  int edgeNum();

  /**
   * Add a vertex to the graph. If the graph changes due to adding this vertex, return true.
   *
   * @param v vertex to be added to this graph
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  boolean add(V v);

  /**
   * Remove a vertex from the graph. If the graph changes due to removing this vertex, return true.
   *
   * @param v vertex to be removed from this graph, if present
   * @return <tt>true</tt> if this contains the specified vertex
   */
  boolean remove(Object v);

  /**
   * Returns {@code true} if this graph contains the specified vertex.
   *
   * @param v the vertex whose presence in this graph is to be tested
   * @return {@code true} if this graph contains the specified vertex, {@code false} otherwise
   */
  boolean contains(V v);

  /**
   * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
   *
   * <p>The meaning of "degree" varies depending on the graph type:
   * <ul>
   * <li>For undirected graphs ({@link Graph}): degree indicates all neighbor nodes connected to the vertex</li>
   * <li>For bidirectional directed graphs ({@link EdgeDedigraph}, {@link VertexDedigraph}):
   * degree indicates all neighbor nodes (sum of in-degree and out-degree)</li>
   * <li>For single-direction directed graphs ({@link Digraph}): degree indicates the out-degree (outgoing neighbors only)</li>
   * </ul>
   *
   * @param v vertex to be queried
   * @return degree of this vertex in current graph
   */
  int degree(V v);

  /**
   * Returns the number of self-loops of the current vertex in the graph. A self-loop means that
   * there exists an edge which both endpoints are incoming vertices. Returns 0 if the vertex does
   * not exist in the graph.
   *
   * @param v vertex to be queried
   * @return self-loops of this vertex in current graph
   */
  int selfLoops(V v);

  /**
   * Return a start node by specific strategy, and use this api bound with {@link #next(Object)} to
   * iterated graph rather than {@link #iterator()}, return null if graph is empty.
   *
   * @return start node
   */
  V start();

  /**
   * Return the previous v in the linked list. This provides efficient backward traversal.
   *
   * @param v the v to find the previous v for
   * @return the previous v or null if v is the first or doesn't exist
   */
  V pre(V v);

  /**
   * Return next node in current graph and sequence strategy considered by different attribute
   * graphs, return null if graph iteration finished.
   *
   * @param v vertex to be queried
   * @return next node in current graph
   */
  V next(V v);

  /**
   * Returns the maximum of all degrees of all vertices in the graph.
   *
   * @return maximum degree in current graph
   */
  int maxDegree();

  /**
   * Returns the average degree of all vertices in the graph.
   *
   * @return average degree of current graph
   */
  double averageDegree();

  /**
   * Returns the number of all self-loops in the graph.
   *
   * @return number of all self-loops in current graph
   */
  int numberOfLoops();

  /**
   * Returns an array containing all the vertices in this collection.
   *
   * <p>Note: If the graph does not contain any vertices, then <tt>null</tt> will be returned.
   * Please be sure to check for <tt>null</tt> before using the returned result, or check that
   * {@link #vertexNum()} is greater than zero before calling this method.
   *
   * @return an array containing all the vertices
   */
  V[] toArray();

  /**
   * Removes all the vertices and edges from this graph.
   */
  void clear();

  /**
   * Returns a sequential Stream with this graph as its source.
   *
   * @return a sequential {@code Stream} over the vertices in this graph
   */
  default Stream<V> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}