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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.graphper.def.DedirectedEdgeGraph.ReverseEdge;
import org.graphper.util.CollectionUtils;

/**
 * A bidirectional directed graph of edge operations.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code DirectedEdgeGraph}, the vertex is
 * searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
 *
 * @param <V> the type of vertex
 * @param <E> the type of directed edge
 * @author Jamison Jiang
 */
public class DedirectedEdgeGraph<V, E extends DirectedEdge<V, E>>
    extends ProxyDedigraph<V, DirectedEdgeGraph<V, E>, DirectedEdgeGraph<V, ReverseEdge<V, E>>>
    implements EdgeDedigraph<V, E> {

  private static final long serialVersionUID = -5712574722294920575L;

  /*
   * Record the reverse edges
   */
  private final HashMap<E, List<ReverseEdge<V, E>>> reverseEdgeMap;

  public DedirectedEdgeGraph() {
    this(new DirectedEdgeGraph<>(), new DirectedEdgeGraph<>());
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  public DedirectedEdgeGraph(int capacity) {
    this(new DirectedEdgeGraph<>(capacity), new DirectedEdgeGraph<>(capacity));
  }

  /**
   * Initialize with edge array.
   *
   * @param edges edge array
   * @throws IllegalArgumentException vertex array is empty
   */
  public DedirectedEdgeGraph(E[] edges) {
    this(new DirectedEdgeGraph<>(), new DirectedEdgeGraph<>());

    if (edges == null || edges.length == 0) {
      throw new IllegalArgumentException("edges can not be empty");
    }

    for (E edge : edges) {
      addEdge(edge);
    }
  }

  /**
   * Initialize with edge collections.
   *
   * @param edges edge collection
   * @throws IllegalArgumentException vertex array is empty
   */
  public DedirectedEdgeGraph(Collection<E> edges) {
    this(new DirectedEdgeGraph<>(), new DirectedEdgeGraph<>());

    if (CollectionUtils.isEmpty(edges)) {
      throw new IllegalArgumentException("edges can not be empty");
    }

    for (E edge : edges) {
      addEdge(edge);
    }
  }

  private DedirectedEdgeGraph(DirectedEdgeGraph<V, E> digraph,
                              DirectedEdgeGraph<V, ReverseEdge<V, E>> reDigraph) {
    super(digraph, reDigraph);

    this.reverseEdgeMap = new HashMap<>(digraph.edgeNum());
    reDigraph.forEachEdges(edge -> putEdgeMap(edge.edge, edge));
  }

  @Override
  public void clear() {
    super.clear();
    if (reverseEdgeMap != null) {
      reverseEdgeMap.clear();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<ReverseEdge<V, E>> inIte(Object v) {
    return reDigraph.adjacent(v);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<E> outIte(Object v) {
    return digraph.adjacent(v);
  }

  /**
   * Adds an edge to the graph, which may or may not be directed.
   *
   * @param e edge to be added to this graph
   * @throws NullPointerException if the edge is null
   */
  @Override
  public void addEdge(E e) {
    Objects.requireNonNull(e);
    digraph.addEdge(e);
    ReverseEdge<V, E> re = new ReverseEdge<>(e.to(), e.from(), e.weight(), e);
    putEdgeMap(e, re);
    reDigraph.addEdge(re);
  }

  /**
   * Remove a vertex from the graph. If the graph changes due to removing this vertex, return true.
   *
   * @param v vertex to be removed from this graph, if present
   * @return <tt>true</tt> if this contains the specified vertex
   */
  @Override
  public boolean remove(Object v) {
    // Remove the reverse edges record
    for (E e : adjacent(v)) {
      List<ReverseEdge<V, E>> reverseEdges = reverseEdgeMap.get(e);
      if (CollectionUtils.isEmpty(reverseEdges)) {
        continue;
      }
      reverseEdges.remove(reverseEdges.size() - 1);
      if (CollectionUtils.isEmpty(reverseEdges)) {
        reverseEdgeMap.remove(e);
      }
    }
    return super.remove(v);
  }

  /**
   * Return next node in current graph and sequence strategy considered by different attribute
   * graphs, return null if graph iteration finished.
   *
   * @param v vertex to be queried
   * @return next node in current graph
   */
  @Override
  public V next(V v) {
    return digraph.next(v);
  }

  /**
   * Removes an edge to the graph. If the graph changes due to removing this edge, return true.
   *
   * @param e edge to be removed to this graph
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  @Override
  public boolean removeEdge(E e) {
    if (vertexNum() == 0 || edgeNum() == 0 || e == null) {
      return false;
    }

    boolean result = digraph.removeEdge(e);
    if (!result) {
      return false;
    }

    List<ReverseEdge<V, E>> reverseEdges = reverseEdgeMap.get(e);
    ReverseEdge<V, E> reverseEdge = reverseEdges.remove(reverseEdges.size() - 1);
    reDigraph.removeEdge(reverseEdge);

    if (CollectionUtils.isEmpty(reverseEdges)) {
      reverseEdgeMap.remove(e);
    }

    return true;
  }

  /**
   * Returns all edges in the graph.
   *
   * @return all edges in the graph
   */
  @Override
  public Iterable<E> edges() {
    return digraph.edges();
  }

  @Override
  public void forEachEdges(Consumer<E> consumer) {
    digraph.forEachEdges(consumer);
  }

  /**
   * If the incoming directed edge exists in the graph, the original directed edge will be deleted
   * in the graph, and a reversed direction edge will be created and inserted into the current
   * graph, and finally the new reversed directed edge will be returned. Returns <tt>null</tt> if
   * the edge does not exist. This action is equivalent to the following sequence of actions:
   * <pre> {@code
   *   EdgeDigraph<V, E> digraph = ...;
   *   E edge = ...;
   *
   *   // Remove edge from graph
   *   if (digraph.remove(edge)) {
   *      // Manually flip edges
   *      edge = edge.reverse();
   *      // Insert the reversed edge
   *      digraph.addEdge(edge);
   *   }
   * }</pre>
   *
   * @param e edge that needs to be reversed
   * @return reversed edge
   * @throws NullPointerException if the specified edge is null
   */
  @Override
  public E reverseEdge(E e) {
    Objects.requireNonNull(e);

    if (!removeEdge(e)) {
      return null;
    }

    E reverse = e.reverse();
    addEdge(reverse);

    return reverse;
  }

  /**
   * Returns a copy of the {@code DedirectedEdgeGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public DedirectedEdgeGraph<V, E> copy() {
    return new DedirectedEdgeGraph<>(digraph.copy(), reDigraph.copy());
  }

  /**
   * Returns a directed graph reversed from the current directed graph.
   *
   * @return directed graph reversed from the current directed graph
   */
  @Override
  public DedirectedEdgeGraph<V, E> reverse() {
    if (edgeNum() == 0) {
      return new DedirectedEdgeGraph<>();
    }

    List<E> res = new ArrayList<>(edgeNum());
    digraph.forEachEdges(edge -> res.add(edge.reverse()));
    DedirectedEdgeGraph<V, E> g = new DedirectedEdgeGraph<>(res);
    for (V v : digraph) {
      g.add(v);
    }
    return g;
  }

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
  @SuppressWarnings("unchecked")
  public Iterable<E> adjacent(Object v) {
    return new ComConcatItr<>(outAdjacent(v), inAdjacent(v));
  }

  /**
   * Returns all incoming edges representing the vertex.
   *
   * @param v vertex to be queried
   * @return all incoming adjacent edges
   */
  @Override
  public Iterable<E> inAdjacent(Object v) {
    return () -> new ReverseIterator<>(reDigraph.adjacent(v).iterator());
  }

  /**
   * Returns all outgoing edges representing the vertex.
   *
   * @param v vertex to be queried
   * @return all outgoing adjacent edges
   */
  @Override
  public Iterable<E> outAdjacent(Object v) {
    return new UnaryConcatIterable<>(digraph.adjacent(v), Collections.emptyList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DedirectedEdgeGraph<?, ?> that = (DedirectedEdgeGraph<?, ?>) o;
    return Objects.equals(reverseEdgeMap, that.reverseEdgeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), reverseEdgeMap);
  }

  private void putEdgeMap(E edge, ReverseEdge<V, E> reverseEdge) {
    reverseEdgeMap.compute(edge, (k, v) -> {
      if (v == null) {
        v = new ArrayList<>(1);
      }
      v.add(reverseEdge);
      return v;
    });
  }

  // ------------------------------------------- Subclass -------------------------------------------

  static class ComConcatItr<V, E extends DirectedEdge<V, E>> extends UnaryConcatIterable<E> {

    public ComConcatItr(Iterable<? extends E>... iterables) {
      super(iterables);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ComConcatItr)) {
        return false;
      }
      Iterator<E> i1 = iterator();
      Iterator<?> i2 = ((ComConcatItr<?, ?>) o).iterator();

      while (i1.hasNext() && i2.hasNext()) {
        if (!Objects.equals(i1.next(), i2.next())) {
          return false;
        }
      }
      return i1.hasNext() == i2.hasNext();
    }

    @Override
    public int hashCode() {
      int hashCode = 1;
      Iterator<E> iterator = iterator();
      while (iterator.hasNext()) {
        E e = iterator.next();
        hashCode += e.hashCode();
      }
      return hashCode;
    }
  }

  static class ReverseIterator<V, E extends DirectedEdge<V, E>> implements Iterator<E> {

    private final Iterator<ReverseEdge<V, E>> reverseEdgeIterator;

    private ReverseIterator(Iterator<ReverseEdge<V, E>> reverseEdgeIterator) {
      Objects.requireNonNull(reverseEdgeIterator);
      this.reverseEdgeIterator = reverseEdgeIterator;
    }

    @Override
    public boolean hasNext() {
      return reverseEdgeIterator.hasNext();
    }

    @Override
    public E next() {
      return reverseEdgeIterator.next().edge;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Adjacent Iterator not support delete!");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ReverseIterator<?, ?> that = (ReverseIterator<?, ?>) o;
      return Objects.equals(reverseEdgeIterator, that.reverseEdgeIterator);
    }

    @Override
    public int hashCode() {
      return Objects.hash(reverseEdgeIterator);
    }
  }

  static class ReverseEdge<V, B extends DirectedEdge<V, B>>
      extends AbstractEdge<V, ReverseEdge<V, B>>
      implements DirectedEdge<V, ReverseEdge<V, B>>, Serializable {

    private static final long serialVersionUID = 4362288930468885917L;

    private final B edge;

    protected ReverseEdge(V from, V to, double weight, B edge) {
      super(from, to, weight);
      Objects.requireNonNull(edge);
      this.edge = edge;
    }

    @Override
    public V from() {
      return left;
    }

    @Override
    public V to() {
      return right;
    }

    @Override
    public ReverseEdge<V, B> reverse() {
      return new ReverseEdge<>(to(), from(), weight, edge.reverse());
    }

    @Override
    public ReverseEdge<V, B> copy() {
      return new ReverseEdge<>(from(), to(), weight, edge);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      ReverseEdge<?, ?> that = (ReverseEdge<?, ?>) o;
      return Objects.equals(edge, that.edge);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), edge);
    }
  }
}
