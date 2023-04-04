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
 * Vertex undirected graph based on {@link AdjVertexGraph}.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code DirectedGraph}, the vertex is
 * searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public class UndirectedGraph<V> extends AdjVertexGraph<V>
    implements Graph.VertexGraph<V> {

  private static final long serialVersionUID = -1768121664171529422L;

  /**
   * Construct graph with default capacity.
   */
  public UndirectedGraph() {
    super();
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  public UndirectedGraph(int capacity) {
    super(capacity);
  }

  /**
   * Initialize with vertex array.
   *
   * @param vertices vertex array
   * @throws IllegalArgumentException vertex array is empty
   */
  public UndirectedGraph(V[] vertices) {
    super(vertices);
  }

  /**
   * Removes an undirected edge to the graph.
   *
   * @param v vertex of edge endpoint
   * @param w vertex of edge endpoint
   */
  @Override
  public void addEdge(V v, V w) {
    if (v == null || w == null) {
      throw new NullPointerException();
    }
    VertexBag<V> bagV = (VertexBag<V>) adjacent(v);
    if (bagV == VertexBag.EMPTY) {
      bagV = addBag(v);
    }
    VertexBag<V> bagW = (VertexBag<V>) adjacent(w);
    if (bagW == VertexBag.EMPTY) {
      bagW = addBag(w);
    }
    bagV.add(w);
    bagW.add(v);
    // The added edge is a self-loop
    if (bagV == bagW) {
      bagV.loopNum++;
    }
    edgeNum++;
  }

  /**
   * Removes an undirected edge to the graph. If the graph changes due to removing this edge, return
   * true.
   *
   * @param v vertex of edge endpoint
   * @param w vertex of edge endpoint
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  @Override
  public boolean removeEdge(Object v, Object w) {
    VertexBag<V> bagV, bagW;
    if ((bagV = (VertexBag<V>) adjacent(v)) == VertexBag.EMPTY
        || (bagW = (VertexBag<V>) adjacent(w)) == VertexBag.EMPTY) {
      return false;
    }
    if (!bagV.remove(w) || !bagW.remove(v)) {
      return false;
    }
    // The removed edges are self-loops.
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
    VertexBag<V> bag = null, tBag;
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
    for (V v : bag) {
      tBag = (VertexBag<V>) adjacent(v);
      if (bag == tBag) {
        continue;
      }
      if (tBag != VertexBag.EMPTY) {
        tBag.remove(bag.vertex);
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
   * Returns a copy of the {@code UndirectedGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public UndirectedGraph<V> copy() {
    UndirectedGraph<V> undirectedGraph = new UndirectedGraph<>(this.bags.length);
    undirectedGraph.bags = bagRepl();
    undirectedGraph.vertexNum = vertexNum;
    undirectedGraph.edgeNum = edgeNum;
    if (vertexNum > 0
        && undirectedGraph.bags[0].vertex instanceof VertexIndex) {
      VertexIndex.GraphRef gf = undirectedGraph.checkAndReturnGraphRef();
      for (int i = 0; i < undirectedGraph.vertexNum; i++) {
        VertexIndex v = ((VertexIndex) undirectedGraph.bags[i].vertex);
        v.getGraphIndex().put(gf, v.index(checkAndReturnGraphRef()));
      }
    }
    return undirectedGraph;
  }

  @Override
  public Iterator<V> iterator() {
    return new UndirectionIterator();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && this.getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return super.hashCode() + this.getClass().hashCode();
  }

  private class UndirectionIterator extends AdjIterator {

    @Override
    public void remove() {
      checkIsConcurrentModify();
      if (index == 0) {
        throw new IllegalStateException();
      }
      VertexBag<V> bag = bags[index - 1], tBag;
      if (bag == null) {
        throw new NoSuchElementException();
      }

      // The number of edges of the deleted vertex
      int bagEdges = bag.degree - bag.loopNum;
      // Delete records of adjacent vertices and edges
      for (V v : bag) {
        tBag = (VertexBag<V>) adjacent(v);
        // Self-loop does not need to be processed
        if (tBag == bag) {
          continue;
        }
        if (tBag != VertexBag.EMPTY) {
          tBag.remove(bag.vertex);
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
              .computeIfPresent(checkAndReturnGraphRef(), (k, v) -> v - 1);
        }
      }
      bags[nv] = null;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }
}
