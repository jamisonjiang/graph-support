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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Edge directed graph based on {@link AdjEdgeGraph}.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code DirectedEdgeGraph}, the vertex is
 * searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
 *
 * @param <V> the type of vertex
 * @param <E> the type of directed edge
 * @author Jamison Jiang
 */
public class DirectedEdgeGraph<V, E extends DirectedEdge<V, E>> extends AdjEdgeGraph<V, E>
    implements Digraph.EdgeDigraph<V, E> {

  private static final long serialVersionUID = 7489284046620730360L;

  /**
   * Construct graph with default capacity.
   */
  public DirectedEdgeGraph() {
    super();
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  public DirectedEdgeGraph(int capacity) {
    super(capacity);
  }

  /**
   * Initialize with edge array.
   *
   * @param edges edge array
   * @throws IllegalArgumentException vertex array is empty
   */
  public DirectedEdgeGraph(E[] edges) {
    super(edges);
  }

  /**
   * Adds a directed edge to the graph.
   *
   * @param edge edge to be added to this graph
   * @throws NullPointerException if the edge is null
   */
  @Override
  public void addEdge(E edge) {
    Objects.requireNonNull(edge, "Edge can not be null");
    V source, target;
    EdgeBag<V, E> bagSource = (EdgeBag<V, E>) adjacent(source = edge.from());
    if (bagSource == EdgeBag.EMPTY) {
      bagSource = addBag(source);
    }
    EdgeBag<V, E> bagTarget = (EdgeBag<V, E>) adjacent(target = edge.to());
    if (bagTarget == EdgeBag.EMPTY) {
      bagTarget = addBag(target);
    }
    bagSource.add(edge);
    bagTarget.degree++; // Increase the in-degree of bagTarget
    if (bagSource == bagTarget) {
      bagSource.loopNum++;
    }
    edgeNum++;
  }

  /**
   * Removes an undirected edge to the graph. If the graph changes due to removing this edge, return
   * true.
   *
   * @param e edge to be removed to this graph
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  @Override
  public boolean removeEdge(E e) {
    if (vertexNum == 0 || edgeNum == 0 || e == null) {
      return false;
    }

    EdgeBag<V, E> bagSource, bagTarget;
    if ((bagSource = (EdgeBag<V, E>) adjacent(e.from())) == EdgeBag.EMPTY
        || (bagTarget = (EdgeBag<V, E>) adjacent(e.to())) == EdgeBag.EMPTY) {
      return false;
    }
    if (!bagSource.remove(e)) {
      return false;
    }
    bagTarget.degree--; // Reduce the entry of bagTarget
    if (bagSource == bagTarget) {
      bagSource.loopNum--;
    }
    edgeNum--;
    return true;
  }

  /**
   * Remove a vertex from the graph. If the graph changes due to removing this vertex, return true.
   *
   * @param vertex vertex to be removed from this graph, if present
   * @return <tt>true</tt> if this contains the specified vertex
   */
  @Override
  public boolean remove(Object vertex) {
    int index = 0;
    EdgeBag<V, E> bag = null, tBag;
    if (vertex instanceof VertexIndex) {
      Integer i = ((VertexIndex) vertex).getGraphIndex()
          .get(checkAndReturnGraphRef());
      if (i != null
          && i >= 0
          && i < vertexNum
          && Objects.equals(bags[i].vertex, vertex)
      ) {
        bag = bags[index = i];
      }
    } else {
      for (; index < vertexNum; index++) {
        if (Objects.equals((bag = bags[index]).vertex, vertex)) {
          break;
        }
      }
      if (index == vertexNum) {
        return false;
      }
    }
    if (bag == null) {
      return false;
    }
    // The number of edges of the deleted vertex
    int bagEdges = bag.degree - bag.loopNum;
    for (E e : bag) {
      tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
      if (tBag != bag && tBag != EdgeBag.EMPTY) {
        tBag.degree--; // Reduce the in-degree of tBag
      }
    }
    if (index != vertexNum) {
      System.arraycopy(bags, index + 1, bags, index, vertexNum - index - 1);
    }
    int nv = --vertexNum;
    // Update index and remove edge pointing to removed vertex
    V bagVertex = bag.vertex;
    for (int i = 0; i < nv; i++) {
      if (bags[i].vertex instanceof VertexIndex) {
        ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
      }
      bags[i].removeIf(e -> Objects.equals(e.to(), bagVertex));
    }
    bags[nv] = null;
    edgeNum -= bagEdges;
    modCount++;
    bag.bModCount++;
    return true;
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
   * @param edge edge that needs to be reversed
   * @return reversed edge
   * @throws NullPointerException if the specified edge is null
   */
  @Override
  public E reverseEdge(E edge) {
    Objects.requireNonNull(edge);
    EdgeBag<V, E> sBag = (EdgeBag<V, E>) adjacent(edge.from());
    if (sBag == EdgeBag.EMPTY) {
      return null;
    }

    if (!sBag.remove(edge)) {
      return null;
    }
    EdgeBag<V, E> tBag = (EdgeBag<V, E>) adjacent(edge.to());
    E reserveEdge = edge.reverse();
    tBag.add(reserveEdge);
    sBag.degree++;
    tBag.degree--;
    return reserveEdge;
  }

  /**
   * Returns a copy of the {@code DirectedEdgeGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public DirectedEdgeGraph<V, E> copy() {
    DirectedEdgeGraph<V, E> graph = new DirectedEdgeGraph<>(this.bags.length);
    graph.bags = bagRepl();
    graph.vertexNum = vertexNum;
    graph.edgeNum = edgeNum;
    if (vertexNum > 0
        && graph.bags[0].vertex instanceof VertexIndex) {
      VertexIndex.GraphRef gf = graph.checkAndReturnGraphRef();
      for (int i = 0; i < graph.vertexNum; i++) {
        VertexIndex v = ((VertexIndex) graph.bags[i].vertex);
        v.getGraphIndex().put(gf, v.index(checkAndReturnGraphRef()));
      }
    }
    return graph;
  }

  /**
   * Returns a directed graph reversed from the current directed graph.
   *
   * <p>Since for directed graphs, some methods are directional (such as {@link #adjacent}),
   * choosing an inversion graph can usually get some bidirectional properties.
   *
   * @return directed graph reversed from the current directed graph
   */
  @Override
  public DirectedEdgeGraph<V, E> reverse() {
    DirectedEdgeGraph<V, E> digraph = new DirectedEdgeGraph<>();
    for (int i = 0; i < vertexNum; i++) {
      EdgeBag<V, E> bag = bags[i];
      V v = bag.vertex;
      for (E e : adjacent(v)) {
        E re = e.reverse();
        digraph.addEdge(re);
      }
      digraph.add(v);
    }
    return digraph;
  }

  @Override
  public Iterator<V> iterator() {
    return new DirectionEdgeIterator();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && this.getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return super.hashCode() + this.getClass().hashCode();
  }

  private class DirectionEdgeIterator extends AdjIterator {

    @Override
    public void remove() {
      checkIsConcurrentModify();
      if (index == 0) {
        throw new IllegalStateException();
      }
      EdgeBag<V, E> bag = bags[index - 1], tBag;
      if (bag == null) {
        throw new NoSuchElementException();
      }

      // The number of edges of the deleted vertex
      int bagEdges = bag.degree - bag.loopNum;
      for (E e : bag) {
        tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
        if (tBag != bag && tBag != EdgeBag.EMPTY) {
          tBag.degree--; // Reduce the in-degree of tBag
        }
      }
      if (index != vertexNum) {
        System.arraycopy(bags, index, bags, index - 1, vertexNum - index);
      }
      int nv = --vertexNum;
      // Update index and remove edge pointing to removed vertex
      V bagVertex = bag.vertex;
      for (int i = 0; i < nv; i++) {
        if (bags[i].vertex instanceof VertexIndex) {
          ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
        }
        bags[i].removeIf(e -> Objects.equals(e.to(), bagVertex));
      }
      bags[nv] = null;
      index--;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }
}
