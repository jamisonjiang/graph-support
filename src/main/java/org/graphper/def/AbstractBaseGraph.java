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
 * Public abstract graph class.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public abstract class AbstractBaseGraph<V> implements BaseGraph<V> {

  protected AbstractBaseGraph() {
  }

  /**
   * Returns the maximum of all degrees of all vertices in the graph.
   *
   * @return maximum degree in current graph
   */
  @Override
  public int maxDegree() {
    int maxDegree = 0;
    for (V value : this) {
      int d;
      if ((d = degree(value)) > maxDegree) {
        maxDegree = d;
      }
    }
    return maxDegree;
  }

  /**
   * Returns the average degree of all vertices in the graph.
   *
   * @return average degree of current graph
   */
  @Override
  public double averageDegree() {
    return vertexNum() != 0 ? (double) 2 * edgeNum() / vertexNum() : 0;
  }

  public abstract static class AbstractVertexOpBase<V> extends AbstractBaseGraph<V>
      implements VertexOpGraph<V> {

    @Override
    public String toString() {
      StringBuilder print = new StringBuilder(
          "vertices " + vertexNum() + ", edges:" + edgeNum() + "\n");
      for (V v : this) {
        print.append("[").append(v).append("] ");
        for (V n : adjacent(v)) {
          print.append(v).append(":").append(n).append(" ");
        }
        print.append("\n");
      }
      return print.toString();
    }
  }

  public abstract static class AbstractVertexOpGraph<V> extends AbstractVertexOpBase<V>
      implements Graph.VertexGraph<V> {

    /**
     * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
     *
     * @param v vertex to be queried
     * @return degree of this vertex in current graph
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (V ignored : adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }

  public abstract static class AbstractVertexOpDigraph<V> extends AbstractVertexOpBase<V>
      implements Digraph.VertexDigraph<V> {


    /**
     * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
     *
     * @param v vertex to be queried
     * @return degree of this vertex in current graph
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (V ignored : adjacent(v)) {
        degree++;
      }
      // Reverse the directed graph
      Digraph.VertexDigraph<V> digraph = reverse();
      for (V ignored : digraph.adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }

  public abstract static class AbstractEdgeOpBase<V, E extends BaseEdge<V, E>>
      extends AbstractBaseGraph<V> implements EdgeOpGraph<V, E> {

    @Override
    public String toString() {
      StringBuilder print = new StringBuilder(
          "vertices " + vertexNum() + ", edges:" + edgeNum() + "\n");
      for (V v : this) {
        print.append("[").append(v).append("]\n");
        for (E e : adjacent(v)) {
          print.append(e).append("\n");
        }
        print.append("\n");
      }
      return print.toString();
    }
  }

  public abstract static class AbstractEdgeOpGraph<V, E extends Edge<V, E>>
      extends AbstractEdgeOpBase<V, E> implements Graph.EdgeGraph<V, E> {

    /**
     * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
     *
     * @param v vertex to be queried
     * @return degree of this vertex in current graph
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (E ignored : adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }

  public abstract static class AbstractEdgeOpDigraph<V, E extends DirectedEdge<V, E>>
      extends AbstractEdgeOpBase<V, E> implements Digraph.EdgeDigraph<V, E> {

    /**
     * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
     *
     * @param v vertex to be queried
     * @return degree of this vertex in current graph
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (E ignored : adjacent(v)) {
        degree++;
      }
      // Reverse directed graph
      Digraph.EdgeDigraph<V, E> digraph = reverse();
      for (E ignored : digraph.adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }
}