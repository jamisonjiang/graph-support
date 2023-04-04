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
 * @see Digraph Directed graph
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
   * @throws NullPointerException if the specified vertex is null
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
   * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
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
   * Returns a copy of the {@code BaseGraph}. For the specific copy strategy (deep clone or shallow
   * clone) and features, please use it flexibly according to different implementation classes.
   *
   * @return a copy of current graph
   */
  BaseGraph<V> copy();

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