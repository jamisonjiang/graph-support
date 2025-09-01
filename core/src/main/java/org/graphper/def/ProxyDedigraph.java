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
 * Bidirectional directed graph using two single direction directed graphs.
 *
 * @param <V> the type of vertex
 * @param <D> forward directed graph type
 * @param <R> opposite directed graph type
 * @author Jamison Jiang
 */
abstract class ProxyDedigraph<V, D extends Digraph<V>, R extends Digraph<V>>
    implements Dedigraph<V>, Serializable {

  private static final long serialVersionUID = 5269319670434302102L;

  protected final D digraph;

  protected final R reDigraph;

  protected ProxyDedigraph(D digraph, R reDigraph) {
    Objects.requireNonNull(digraph);
    Objects.requireNonNull(reDigraph);

    this.digraph = digraph;
    this.reDigraph = reDigraph;
  }

  @Override
  public int vertexNum() {
    return digraph.vertexNum();
  }

  @Override
  public int edgeNum() {
    return digraph.edgeNum();
  }

  @Override
  public boolean add(V v) {
    return digraph.add(v) && reDigraph.add(v);
  }

  @Override
  public boolean remove(Object v) {
    return digraph.remove(v) && reDigraph.remove(v);
  }

  @Override
  public boolean contains(V v) {
    return digraph.contains(v);
  }

  @Override
  public V start() {
    return digraph.start();
  }

  @Override
  public V pre(V v) {
    return digraph.pre(v);
  }

  @Override
  public V next(V v) {
    return digraph.next(v);
  }

  @Override
  public int maxDegree() {
    int maxDegree = 0;
    for (V v : digraph) {
      int d = degree(v);
      if (d > maxDegree) {
        maxDegree = d;
      }
    }

    return maxDegree;
  }

  @Override
  public int inDegree(V v) {
    return reDigraph.degree(v);
  }

  @Override
  public int outDegree(V v) {
    return digraph.degree(v);
  }

  @Override
  public int degree(V v) {
    return reDigraph.degree(v) + digraph.degree(v);
  }

  @Override
  public double averageDegree() {
    return vertexNum() != 0 ? (double) 2 * edgeNum() / vertexNum() : 0;
  }

  @Override
  public int numberOfLoops() {
    return digraph.numberOfLoops();
  }

  @Override
  public int selfLoops(V v) {
    return digraph.selfLoops(v);
  }

  @Override
  public V[] toArray() {
    return digraph.toArray();
  }

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
