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
 * Vertex directed graph based on {@link AdjVertexGraph}.
 *
 * <p>The type of vertex is recommended to use the subclass of {@link VertexIndex}. When the
 * subclass of {@link VertexIndex} is stored as a vertex in {@code DirectedGraph}, the vertex is
 * searched with a complexity of <tt>O(1)</tt>, otherwise it is <tt>O(N)</tt>.
 *
 * @param <V> the type of vertex
 * @author Jamison Jiang
 */
public class DirectedGraph<V> extends AdjVertexGraph<V>
    implements Digraph.VertexDigraph<V> {

  private static final long serialVersionUID = 1062819133746040326L;

  /**
   * Returns a directed graph reversed from the current directed graph.
   */
  public DirectedGraph() {
    super();
  }

  /**
   * Construct a graph with a specified capacity.
   *
   * @param capacity specified capacity
   * @throws IllegalArgumentException Capacity is less than or equal to 0
   */
  public DirectedGraph(int capacity) {
    super(capacity);
  }

  /**
   * Initialize with vertex array.
   *
   * @param vertices vertex array
   * @throws IllegalArgumentException vertex array is empty
   */
  public DirectedGraph(V[] vertices) {
    super(vertices);
  }

  /**
   * Adds a directed edge to the graph.
   *
   * @param source the source vertex of directed edge
   * @param target the target vertex of directed edge
   * @throws NullPointerException if the specified endpoint vertex is null
   */
  @Override
  public void addEdge(V source, V target) {
    if (source == null || target == null) {
      throw new NullPointerException();
    }
    VertexBag<V> bagSource = (VertexBag<V>) adjacent(source);
    if (bagSource == VertexBag.EMPTY) {
      bagSource = addBag(source);
    }
    VertexBag<V> bagTarget = (VertexBag<V>) adjacent(target);
    if (bagTarget == VertexBag.EMPTY) {
      bagTarget = addBag(target);
    }
    bagSource.add(target);
    bagTarget.degree++; // Increase the in-degree of bagTarget
    if (bagSource == bagTarget) {
      bagSource.loopNum++;
    }
    edgeNum++;
  }

  /**
   * Removes a directed edge to the graph. If the graph changes due to removing this edge, return
   * true.
   *
   * @param source the source vertex of directed edge
   * @param target the target vertex of directed edge
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  @Override
  public boolean removeEdge(Object source, Object target) {
    VertexBag<V> bagSource, bagTarget;
    if ((bagSource = (VertexBag<V>) adjacent(source)) == VertexBag.EMPTY
        || (bagTarget = (VertexBag<V>) adjacent(target)) == VertexBag.EMPTY) {
      return false;
    }
    if (!bagSource.remove(target)) {
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
    for (V v : bag) {
      tBag = (VertexBag<V>) adjacent(v);
      if (tBag != bag && tBag != VertexBag.EMPTY) {
        tBag.degree--; // Reduce the in-degree of tBag
      }
    }
    if (index != vertexNum) {
      System.arraycopy(bags, index + 1, bags, index, vertexNum - index - 1);
    }
    int nv = --vertexNum;
    // update index and remove edge pointing to removed vertex
    for (int i = 0; i < nv; i++) {
      if (bags[i].vertex instanceof VertexIndex) {
        ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
      }
      bags[i].remove(bag.vertex);
    }
    bags[nv] = null;
    edgeNum -= bagEdges;
    modCount++;
    bag.bModCount++;
    return true;
  }

  /**
   * Returns a copy of the {@code DirectedGraph}.
   *
   * @return a copy of current graph
   */
  @Override
  public DirectedGraph<V> copy() {
    DirectedGraph<V> directedGraph = new DirectedGraph<>(this.bags.length);
    directedGraph.bags = bagRepl();
    directedGraph.vertexNum = vertexNum;
    directedGraph.edgeNum = edgeNum;
    if (vertexNum > 0
        && directedGraph.bags[0].vertex instanceof VertexIndex) {
      VertexIndex.GraphRef gf = directedGraph.checkAndReturnGraphRef();
      for (int i = 0; i < directedGraph.vertexNum; i++) {
        VertexIndex v = ((VertexIndex) directedGraph.bags[i].vertex);
        v.getGraphIndex().put(gf, v.index(checkAndReturnGraphRef()));
      }
    }
    return directedGraph;
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
  public DirectedGraph<V> reverse() {
    DirectedGraph<V> digraph = new DirectedGraph<>(toArray());
    for (int i = 0; i < vertexNum; i++) {
      VertexBag<V> bag = bags[i];
      V v = bag.vertex;
      for (V w : bag) {
        digraph.addEdge(w, v);
      }
      digraph.add(v);
    }
    return digraph;
  }

  @Override
  public Iterator<V> iterator() {
    return new DirectionIterator();
  }

  private class DirectionIterator extends AdjIterator {

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
      for (V v : bag) {
        tBag = (VertexBag<V>) adjacent(v);
        if (tBag != bag && tBag != VertexBag.EMPTY) {
          tBag.degree--; // Reduce the in-degree of tBag
        }
      }
      if (index != vertexNum) {
        System.arraycopy(bags, index, bags, index - 1, vertexNum - index);
      }
      int nv = --vertexNum;
      // Update index and remove edge pointing to removed vertex
      for (int i = 0; i < nv; i++) {
        if (bags[i].vertex instanceof VertexIndex) {
          ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
        }
        bags[i].remove(bag.vertex);
      }
      bags[nv] = null;
      index--;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && this.getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return super.hashCode() + this.getClass().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder print = new StringBuilder("vertices " + vertexNum() + ", edges:\n");
    for (V v : this) {
      print.append("[").append(v).append("] ");
      for (V n : adjacent(v)) {
        print.append(v).append("->").append(n).append(" ");
      }
      print.append("\n");
    }
    return print.toString();
  }
}
