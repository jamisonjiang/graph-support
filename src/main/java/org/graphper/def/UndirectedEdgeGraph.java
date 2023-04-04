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
 * Edge undirected graph based on {@link AdjEdgeGraph}.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code UndirectedEdgeGraph}, the vertex
 * is searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
 *
 * @param <V> the type of vertex
 * @param <E> the type of directed edge
 * @author Jamison Jiang
 */
public class UndirectedEdgeGraph<V, E extends Edge<V, E>> extends AdjEdgeGraph<V, E>
    implements Graph.EdgeGraph<V, E> {

  private static final long serialVersionUID = -3215868703245301095L;

  /**
   * Construct graph with default capacity.
   */
  public UndirectedEdgeGraph() {
    super();
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  public UndirectedEdgeGraph(int capacity) {
    super(capacity);
  }

  /**
   * Initialize with edge array.
   *
   * @param edges edge array
   * @throws IllegalArgumentException vertex array is empty
   */
  public UndirectedEdgeGraph(E[] edges) {
    super(edges);
  }

  /**
   * Adds an undirected edge to the graph.
   *
   * @param e edge to be added to this graph
   * @throws NullPointerException if the edge is null
   */
  @Override
  public void addEdge(E e) {
    Objects.requireNonNull(e);
    V v, w;
    EdgeBag<V, E> bagV = (EdgeBag<V, E>) adjacent(v = e.either());
    if (bagV == EdgeBag.EMPTY) {
      bagV = addBag(v);
    }
    EdgeBag<V, E> bagW = (EdgeBag<V, E>) adjacent(w = e.other(v));
    if (bagW == EdgeBag.EMPTY) {
      bagW = addBag(w);
    }
    bagV.add(e);
    bagW.add(e);
    // The added edge is a self-loop
    if (bagV == bagW) {
      bagV.loopNum++;
    }
    edgeNum++;
  }

  /**
   * Removes a directed edge to the graph. If the graph changes due to removing this edge, return
   * true.
   *
   * @param edge edge to be removed to this graph
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  @Override
  public boolean removeEdge(E edge) {
    if (vertexNum == 0 || edgeNum == 0 || edge == null) {
      return false;
    }

    EdgeBag<V, E> bagV, bagW;
    if ((bagV = (EdgeBag<V, E>) adjacent(edge.either())) == EdgeBag.EMPTY
        || (bagW = (EdgeBag<V, E>) adjacent(edge.other(edge.either()))) == EdgeBag.EMPTY) {
      return false;
    }
    if (!bagV.remove(edge) || !bagW.remove(edge)) {
      return false;
    }
    // The removed edges are self-loops
    if (bagV == bagW) {
      bagV.loopNum--;
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
    // Delete records of adjacent vertices and edges
    for (E e : bag) {
      tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
      if (bag == tBag) {
        continue;
      }
      if (tBag != EdgeBag.EMPTY) {
        tBag.remove(e);
      }
    }
    if (index != vertexNum) {
      System.arraycopy(bags, index + 1, bags, index, vertexNum - index - 1);
    }
    int nv = --vertexNum;
    if (bag.vertex instanceof VertexIndex) {
      for (int i = index; i < nv; i++) {
        ((VertexIndex) bags[i].vertex)
            .getGraphIndex()
            .computeIfPresent(checkAndReturnGraphRef(), (k, v) -> v - 1);// Update index
      }
    }
    bags[nv] = null;
    edgeNum -= bagEdges;
    modCount++;
    bag.bModCount++;
    return true;
  }

  /**
   * Returns a copy of the {@code UndirectedEdgeGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public UndirectedEdgeGraph<V, E> copy() {
    UndirectedEdgeGraph<V, E> graph = new UndirectedEdgeGraph<>(this.bags.length);
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

  @Override
  public Iterator<V> iterator() {
    return new UndirectedEdgeIterator();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && this.getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return super.hashCode() + this.getClass().hashCode();
  }

  private class UndirectedEdgeIterator extends AdjIterator {

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
      // Delete records of adjacent vertices and edges
      for (E e : bag) {
        tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
        // Self-loop does not need to be processed
        if (tBag == bag) {
          continue;
        }
        if (tBag != EdgeBag.EMPTY) {
          tBag.remove(e);
        }
      }
      if (index != vertexNum) {
        System.arraycopy(bags, index, bags, index - 1, vertexNum - index);
      }
      int nv = --vertexNum;
      --index;
      if (bag.vertex instanceof VertexIndex) {
        for (int i = index; i < nv; i++) {
          ((VertexIndex) bags[i].vertex)
              .getGraphIndex()
              .computeIfPresent(checkAndReturnGraphRef(), (k, v) -> v - 1);// Update index
        }
      }
      bags[nv] = null;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }
}
