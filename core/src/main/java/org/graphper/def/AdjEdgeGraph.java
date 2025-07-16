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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stored graph using an adjacency array of edges. For the following undirected graph:<br>
 * <pre>
 *        0 --------- 2
 *       / \        / | \
 *     /    \     /   |   \
 *    5      \  /     3 -- 4
 *            1
 * Expressed using an adjacency array as:
 *    Bags Arrays
 *    bag[0] vertex: 0 adjs:Edge(0,5,0) -&gt; Edge(0,1,0) -&gt; Edge(0,2,0)
 *    bag[1] vertex: 1 adjs:Edge(1,0,0) -&gt; Edge(1,2,0)
 *    bag[2] vertex: 2 adjs:Edge(2,0,0) -&gt; Edge(2,1,0) -&gt; Edge(2,3,0) -&gt; Edge(2,4,0)
 *    bag[3] vertex: 3 adjs:Edge(3,2,0) -&gt; Edge(3,4,0)
 *    bag[4] vertex: 4 adjs:Edge(4,3,0) -&gt; Edge(4,2,0)
 *    bag[5] vertex: 5 adjs:Edge(5,0,0)
 * </pre>
 *
 * <p>In the directed graph {@link Digraph}, an edge will only be stored by one array member
 * {@code EdgeBag}, while the {@link Graph}, needs to be stored twice in the {@code EdgeBag} of the
 * index where the <tt>source</tt> and <tt>target</tt> are located.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code AdjEdgeGraph}, the vertex is
 * searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
 *
 * @param <V> the type of vertex
 * @param <E> the type of edge
 * @author Jamison Jiang
 * @see Edge Undirected Edge
 * @see DirectedEdge Directed Edge
 */
abstract class AdjEdgeGraph<V, E extends BaseEdge<V, E>> extends
    AbstractBaseGraph.AbstractEdgeOpBase<V, E> implements Serializable {

  private static final long serialVersionUID = -7783803325974568478L;

  /**
   * Default initialization capacity.
   */
  private static final int DEFAULT_CAPACITY = 1 << 5;

  /**
   * Number of vertices.
   */
  int vertexNum;

  /**
   * Number of edges.
   */
  int edgeNum;

  /**
   * Adjacency list array object, used to store all vertices and all edges of vertices.
   */
  transient EdgeBag<V, E>[] bags;

  /**
   * Record the number of modifications, mainly to avoid unpredictable structures caused by
   * concurrent deletions.
   */
  protected transient int modCount;

  /**
   * The positioning key of the vertex index {@code VertexIndex}.
   */
  private transient VertexIndex.GraphRef graphRef;

  /**
   * Construct graph with default capacity.
   */
  @SuppressWarnings("unchecked")
  AdjEdgeGraph() {
    bags = new EdgeBag[DEFAULT_CAPACITY];
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  @SuppressWarnings("unchecked")
  AdjEdgeGraph(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + capacity);
    }
    bags = new EdgeBag[capacity];
  }

  /**
   * Initialize with edge array.
   *
   * @param edges edge array
   * @throws IllegalArgumentException vertex array is empty
   */
  AdjEdgeGraph(E[] edges) {
    this();
    if (edges == null || edges.length == 0) {
      throw new IllegalArgumentException("edges can not be empty");
    }
    for (E edge : edges) {
      addEdge(edge);
    }
  }

  /**
   * Returns the number of vertices.
   *
   * @return the number of vertices
   */
  @Override
  public int vertexNum() {
    return vertexNum;
  }

  /**
   * Returns the number of edges.
   *
   * @return the number of edges
   */
  @Override
  public int edgeNum() {
    return edgeNum;
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
    Objects.requireNonNull(v);
    EdgeBag<V, E> bag = (EdgeBag<V, E>) adjacent(v);
    if (bag != EdgeBag.EMPTY) {
      return false;
    }
    addBag(v);
    return true;
  }

  /**
   * Returns all edges adjacent to the specified vertex.
   *
   * <p>If the vertex is a subclass of {@link VertexIndex}, it will be queried in the graph with
   * <tt>O(1)</tt> complexity, otherwise it will be retrieved with <tt>O(n)</tt> complexity.
   *
   * @param v vertex to be queried
   * @return all adjacent edges
   */
  @Override
  public Iterable<E> adjacent(Object v) {
    return adjacent(v, false);
  }

  /**
   * Performs the given action for each adjacent edge of the specified vertex until all adjacent
   * edges have been processed or the action throws an exception. This method uses Bag#forEach
   * directly to avoid creating intermediate iterable objects, reducing GC pressure.
   *
   * @param v vertex to be queried
   * @param action The action to be performed for each adjacent edge
   * @throws NullPointerException if the specified action is null
   */
  @Override
  public void forEachAdjacent(Object v, Consumer<E> action) {
    Objects.requireNonNull(action);
    EdgeBag<V, E> bag = (EdgeBag<V, E>) adjacent(v, false);
    if (bag != EdgeBag.EMPTY) {
      bag.forEach(action);
    }
  }

  /**
   * Returns all edges in the graph.
   *
   * @return all edges in the graph
   */
  @Override
  public Iterable<E> edges() {
    List<E> edges = null;
    for (int i = 0; i < vertexNum; i++) {
      if (edges == null) {
        edges = new ArrayList<>(edgeNum);
      }
      for (E e : adjacent(bags[i].vertex)) {
        edges.add(e);
      }
    }
    return edges == null ? Collections.emptyList() : edges;
  }

  @Override
  public void forEachEdges(Consumer<E> action) {
    Objects.requireNonNull(action);
    for (int i = 0; i < vertexNum; i++) {
      for (E e : adjacent(bags[i].vertex)) {
        action.accept(e);
      }
    }
  }

  @Override
  public void forEach(Consumer<? super V> action) {
    Objects.requireNonNull(action);
    for (int i = 0; i < vertexNum; i++) {
      action.accept(bags[i].vertex);
    }
  }

  /**
   * Returns the number of vertex neighbors. Returns 0 if the vertex does not exist in the graph.
   *
   * @param vertex vertex to be queried
   * @return degree of this vertex in current graph
   */
  @Override
  public int degree(V vertex) {
    EdgeBag<V, E> v = (EdgeBag<V, E>) adjacent(vertex);
    return v != EdgeBag.EMPTY ? v.degree : 0;
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
    return ((EdgeBag<V, E>) adjacent(v)).loopNum;
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
    if (v == null) {
      return null;
    }

    EdgeBag<V, E> bag = (EdgeBag<V, E>) adjacent(v, true);
    return bag.vertex;
  }

  /**
   * Returns the number of all self-loops in the graph.
   *
   * @return number of all self-loops in current graph
   */
  @Override
  public int numberOfLoops() {
    int count = 0;
    for (int i = 0; i < vertexNum; i++) {
      count += bags[i].loopNum;
    }
    return count;
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
    if (vertexNum <= 0) {
      return null;
    }
    Class<?> clazz = bags[0].vertex.getClass();
    @SuppressWarnings("unchecked") V[] vertexs = (V[]) Array.newInstance(clazz, vertexNum);
    for (int i = 0; i < vertexNum; i++) {
      vertexs[i] = bags[i].vertex;
    }
    return vertexs;
  }

  /**
   * Returns a copy of the {@code AdjEdgeGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public abstract AdjEdgeGraph<V, E> copy();

  /**
   * Removes all the vertices and edges from this graph.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    vertexNum = 0;
    edgeNum = 0;
    modCount = 0;
    graphRef = null;
    bags = new EdgeBag[DEFAULT_CAPACITY];
  }

  /**
   * Add EdgeBag and return object.
   *
   * @param v vertex to be added to this graph
   * @return adjacency vertex container for edges
   */
  protected EdgeBag<V, E> addBag(V v) {
    EdgeBag<V, E> bag;
    // Capacity is full
    if (vertexNum == bags.length) {
      resize();
    }
    int vn = vertexNum++;
    bags[vn] = bag = new EdgeBag<>(v);
    // Add vertex index
    if (v instanceof VertexIndex) {
      ((VertexIndex) v).getGraphIndex().put(checkAndReturnGraphRef(), vn);
    }
    modCount++;
    return bag;
  }

  /**
   * When the capacity is full, expand according to the following rules: critical =
   * {@link #rightRangeMinPowerOf2()}; vertexNum < critical * 3/4, expand to critical vertexNum >=
   * critical * 3/4, expand to 2 * critical
   */
  private void resize() {
    int critical = rightRangeMinPowerOf2();
    int newCap = vertexNum < (critical - (critical >>> 2)) ? critical : critical << 1;
    bags = Arrays.copyOf(bags, newCap);
  }

  private int rightRangeMinPowerOf2() {
    int capacity = bags.length;
    return (capacity & (1 << (Integer.SIZE - Integer.numberOfLeadingZeros(capacity) - 1))) << 1;
  }

  /*
   * Validate references, confirm and initialize.
   */
  protected VertexIndex.GraphRef checkAndReturnGraphRef() {
    if (graphRef == null) {
      graphRef = new VertexIndex.GraphRef(this);
    }
    return graphRef;
  }

  @SuppressWarnings("unchecked")
  private Iterable<E> adjacent(Object v, boolean next) {
    if (v == null) {
      return (EdgeBag<V, E>) EdgeBag.EMPTY;
    }
    if (v instanceof VertexIndex) {
      Integer index;
      index = ((VertexIndex) v).getGraphIndex().get(checkAndReturnGraphRef());
      if (index == null) {
        return (EdgeBag<V, E>) EdgeBag.EMPTY;
      }
      if (index >= 0 && index < vertexNum && v.equals(bags[index].vertex)) {
        if (!next) {
          return bags[index];
        }

        return index < vertexNum - 1 ? bags[index + 1] : (EdgeBag<V, E>) EdgeBag.EMPTY;
      }
    }
    return position(v, next);
  }

  // O(n) find
  @SuppressWarnings("unchecked")
  private EdgeBag<V, E> position(Object v, boolean next) {
    for (int i = 0; i < vertexNum; i++) {
      if (v.equals(bags[i].vertex)) {
        if (!next) {
          return bags[i];
        }

        if (i < vertexNum - 1) {
          return bags[i + 1];
        }
      }
    }
    return (EdgeBag<V, E>) EdgeBag.EMPTY;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdjEdgeGraph<?, ?> that = (AdjEdgeGraph<?, ?>) o;
    if (vertexNum != that.vertexNum || edgeNum != that.edgeNum) {
      return false;
    }
    for (int i = 0; i < vertexNum; i++) {
      if (!Objects.equals(bags[i], that.bags[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(vertexNum, edgeNum);
    result = 31 * result;
    for (int i = 0; i < vertexNum; i++) {
      result += bags[i].hashCode();
    }
    return result;
  }

  /*
   * Create and return a copy of bags
   */
  protected EdgeBag<V, E>[] bagRepl() {
    @SuppressWarnings("unchecked") EdgeBag<V, E>[] newBag = new EdgeBag[bags.length];
    for (int i = 0; i < vertexNum; i++) {
      newBag[i] = bags[i].clone();
    }
    return newBag;
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    // Only serialize the bag of the number of vertices
    for (int i = 0; i < vertexNum; i++) {
      oos.writeObject(bags[i]);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    if (vertexNum >= 0) {
      bags = new EdgeBag[vertexNum];
      for (int i = 0; i < vertexNum; i++) {
        bags[i] = (EdgeBag<V, E>) ois.readObject();
        if (bags[i].vertex instanceof VertexIndex) {
          VertexIndex vertexIndex = (VertexIndex) bags[i].vertex;
          // Indexes are only added to the current graph's
          vertexIndex.getGraphIndex().put(checkAndReturnGraphRef(), i);
        }
      }
    }
  }

  /**
   * Vertex iterator.
   */
  protected class AdjIterator implements Iterator<V> {

    // access object index
    int index;

    // The number of next revisions
    int exceptModCount = modCount;

    @Override
    public boolean hasNext() {
      return index < vertexNum;
    }

    @Override
    public V next() {
      checkIsConcurrentModify();
      if (index > vertexNum) {
        throw new NoSuchElementException();
      }
      return bags[index++].vertex;
    }

    void checkIsConcurrentModify() {
      if (exceptModCount != modCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /**
   * Vertex Adjacency List.
   */
  protected static class EdgeBag<V, E extends BaseEdge<V, E>> extends Bag<V, E> implements
      Cloneable {

    private static final long serialVersionUID = -4204284993141072092L;

    static final EdgeBag<?, ?> EMPTY = new EdgeBag<>(true);

    // Vertex degree
    int degree;

    // Number of loops
    int loopNum;

    EdgeBag(V vertex) {
      super(vertex);
    }

    EdgeBag(Boolean unmodify) {
      super(unmodify);
    }

    @Override
    public Iterator<E> iterator() {
      return new EdgeBagIterator();
    }

    @Override
    void add(E e) {
      super.add(e);
      degree++;
    }

    @Override
    boolean remove(Object edge) {
      if (super.remove(edge)) {
        degree--;
        return true;
      }
      return false;
    }

    @Override
    boolean removeIf(Predicate<E> predicate) {
      if (super.removeIf(predicate)) {
        degree--;
        return true;
      }
      return false;
    }

    @Override
    @SuppressWarnings("all")
    public EdgeBag<V, E> clone() {
      EdgeBag<V, E> bag = new EdgeBag<>(vertex);
      bag.loopNum = loopNum;
      bag.degree = degree;
      for (E e : this) {
        bag.add(e.copy());
        bag.degree--;
      }
      return bag;
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
      EdgeBag<?, ?> edgeBag = (EdgeBag<?, ?>) o;
      return degree == edgeBag.degree && loopNum == edgeBag.loopNum;
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), degree, loopNum);
    }

    private class EdgeBagIterator extends BagIterator {

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Adjacent Iterator not support delete!");
      }
    }
  }
}
