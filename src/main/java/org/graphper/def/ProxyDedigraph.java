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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

/**
 * Bidirectional directed graph using two directed graphs.
 *
 * @param <V> the type of vertex
 * @param <D> forward directed graph type
 * @param <R> opposite directed graph type
 * @author Jamison Jiang
 */
abstract class ProxyDedigraph<V, D extends Digraph<V>, R extends Digraph<V>> extends
    AbstractBaseGraph<V> implements Dedigraph<V>, Serializable {

  private static final long serialVersionUID = 5269319670434302102L;

  protected final D digraph;

  protected final R reDigraph;

  protected ProxyDedigraph(D digraph, R reDigraph) {
    Objects.requireNonNull(digraph);
    Objects.requireNonNull(reDigraph);

    this.digraph = digraph;
    this.reDigraph = reDigraph;
  }

  /**
   * Returns the number of vertices.
   *
   * @return the number of vertices
   */
  @Override
  public int vertexNum() {
    return digraph.vertexNum();
  }

  /**
   * Returns the number of edges.
   *
   * @return the number of edges
   */
  @Override
  public int edgeNum() {
    return digraph.edgeNum();
  }

  /**
   * Add a vertex to the graph. If the graph changes due to adding this vertex, return true.
   *
   * @param v vertex to be added to this graph
   * @return <tt>true</tt> if this graph changed as a result of the call
   * @throws NullPointerException if the specified vertex is null
   */
  @Override
  public boolean add(V v) {
    return digraph.add(v) && reDigraph.add(v);
  }

  /**
   * Remove a vertex from the graph. If the graph changes due to removing this vertex, return true.
   *
   * @param v vertex to be removed from this graph, if present
   * @return <tt>true</tt> if this contains the specified vertex
   */
  @Override
  public boolean remove(Object v) {
    return digraph.remove(v) && reDigraph.remove(v);
  }

  /**
   * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.The
   * degree has no direction, and this interface returns the sum of the in and out degrees of this
   * vertex.
   *
   * @param v vertex to be queried
   * @return degree of this vertex in current graph
   */
  @Override
  public int degree(V v) {
    return digraph.degree(v);
  }

  /**
   * Returns the in-degree of the vertex. Returns 0 if the vertex does not exist in the graph.
   *
   * @param v vertex to be queried
   * @return in-degree of this vertex in current graph
   */
  @Override
  public int inDegree(V v) {
    int count = 0;
    for (Object ignore : inIte(v)) {
      count++;
    }

    return count;
  }

  /**
   * Returns the out-degree of the vertex. Returns 0 if the vertex does not exist in the graph.
   *
   * @param v vertex to be queried
   * @return out-degree of this vertex in current graph
   */
  @Override
  public int outDegree(V v) {
    int count = 0;
    for (Object ignore : outIte(v)) {
      count++;
    }

    return count;
  }

  /**
   * Returns the number of all self-loops in the graph.
   *
   * @return number of all self-loops in current graph
   */
  @Override
  public int numberOfLoops() {
    return digraph.numberOfLoops();
  }

  /**
   * Returns the number of self-loops of the current vertex in the graph. A self-loop means that
   * there exists an edge which both endpoints are incoming vertices. Returns 0 if the vertex does
   * not exist in the graph.
   *
   * @param v vertex to be queried
   * @return self-loops of this vertex in current graph
   */
  @Override
  public int selfLoops(V v) {
    return digraph.selfLoops(v);
  }

  /**
   * Returns an array containing all the vertices in this collection.
   *
   * <p>Note: If the graph does not contain any vertices, then <tt>null</tt> will be returned.
   * Please be sure to check for <tt>null</tt> before using the returned result, or check that
   * {@link #vertexNum()} is greater than zero before calling this method.
   *
   * @return an array containing all the vertices
   */
  @Override
  public V[] toArray() {
    return digraph.toArray();
  }

  /**
   * Removes all the vertices and edges from this graph.
   */
  @Override
  public void clear() {
    digraph.clear();
    reDigraph.clear();
  }

  @Override
  public Iterator<V> iterator() {
    return new ProxyIterator<>(digraph);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProxyDedigraph<?, ?, ?> that = (ProxyDedigraph<?, ?, ?>) o;
    return Objects.equals(digraph, that.digraph)
        && Objects.equals(reDigraph, that.reDigraph);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digraph, reDigraph);
  }

  @Override
  public String toString() {
    return digraph.toString();
  }

  protected abstract <T> Iterable<T> inIte(Object v);

  protected abstract <T> Iterable<T> outIte(Object v);

  /*------------------------------------------- Iterable or Iterator Object -------------------------------------------*/

  private static class ProxyIterator<V, D extends Digraph<V>> implements Iterator<V> {

    private final Iterator<V> iterator;

    private ProxyIterator(D digraph) {
      Objects.requireNonNull(digraph);
      this.iterator = digraph.iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public V next() {
      return iterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "ProxyDedigraph's vertex cannot be deleted by iterator");
    }
  }
}
