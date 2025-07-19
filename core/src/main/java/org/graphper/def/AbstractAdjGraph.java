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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Abstract base class for adjacency-based graph implementations that combines efficient lookup with
 * ordered traversal capabilities.
 *
 * <p>This class provides a hybrid data structure that maintains both:
 * <ul>
 *   <li>A {@link LinkedHashMap} for O(1) vertex lookup and adjacency list access</li>
 *   <li>A doubly-linked list structure for efficient sequential traversal using
 *       {@link #start()}, {@link #next(Object)}, and {@link #pre(Object)} methods</li>
 * </ul>
 *
 * <p>The dual structure allows for both fast random access to vertices and their adjacencies,
 * as well as efficient ordered iteration without the overhead of creating new iterator objects.
 *
 * <h3>Data Structure Design</h3>
 * <p>Each vertex in the graph is associated with an {@link AdjacencyList} that stores adjacent
 * elements of type {@code E}. The adjacency lists are connected in a doubly-linked list structure,
 * allowing for efficient forward and backward traversal while maintaining insertion order.
 *
 * <h3>Abstract Methods</h3>
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #newAdjacentList(Object)} - Factory method to create adjacency lists</li>
 *   <li>{@link #adjustAdjWhenRemoveNode(Object, AdjacencyList)} - Handle adjacency updates when removing vertices</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 * <p>This class is <strong>not thread-safe</strong>. External synchronization is required
 * for concurrent access from multiple threads.
 *
 * <h3>Performance Characteristics</h3>
 * <ul>
 *   <li>Vertex lookup: O(1) average case</li>
 *   <li>Add vertex: O(1) average case</li>
 *   <li>Remove vertex: O(V*E) due to adjacency updates</li>
 *   <li>Sequential traversal: O(1) per step</li>
 *   <li>Memory overhead: Additional pointers for doubly-linked list structure</li>
 * </ul>
 *
 * @param <V> the type of vertices in the graph
 * @param <E> the type of elements stored in adjacency lists (typically vertices or edges)
 * @author Jamison Jiang
 */
public abstract class AbstractAdjGraph<V, E> implements BaseGraph<V>, Serializable {

  private static final long serialVersionUID = -7783803325974568478L;

  /**
   * Default initialization capacity.
   */
  private static final int DEFAULT_CAPACITY = 1 << 4;

  /**
   * Number of edges.
   */
  protected int edgeNum;

  /**
   * Map from vertex to its adjacency edge list.
   */
  protected transient LinkedHashMap<V, AdjacencyList<V, E>> edgeMap;

  /**
   * Head of the doubly-linked list for efficient next/previous navigation.
   */
  protected transient AdjacencyList<V, E> head;

  /**
   * Tail of the doubly-linked list for efficient next/previous navigation.
   */
  protected transient AdjacencyList<V, E> tail;

  /**
   * Construct graph with default capacity.
   */
  AbstractAdjGraph() {
    this(DEFAULT_CAPACITY);
  }

  /**
   * Construct graph with specified capacity.
   */
  AbstractAdjGraph(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be positive");
    }
    this.edgeMap = new LinkedHashMap<>(capacity);
    this.head = null;
    this.tail = null;
  }

  /**
   * Factory method to create a new adjacency list for the specified vertex.
   *
   * <p>This method allows subclasses to customize the type and behavior of adjacency lists
   * based on their specific requirements (e.g., directed vs undirected, vertex-based vs
   * edge-based).
   *
   * @param v the vertex for which to create an adjacency list
   * @return a new adjacency list for the vertex, or {@code null} if the vertex cannot be added
   * @throws IllegalArgumentException if the vertex is invalid for this graph type
   */
  protected abstract AdjacencyList<V, E> newAdjacentList(V v);

  /**
   * Adjusts the adjacency list when a vertex is removed from the graph.
   *
   * <p>This method is called for each remaining vertex in the graph when a vertex is removed.
   * It allows subclasses to update adjacency lists to remove references to the deleted vertex and
   * maintain graph consistency.
   *
   * @param v   the vertex that was removed from the graph
   * @param adj the adjacency list to potentially adjust
   */
  protected abstract void adjustAdjWhenRemoveNode(V v, AdjacencyList<V, E> adj);

  /**
   * Returns the maximum of all degrees of all vertices in the graph.
   *
   * @return maximum degree in current graph
   */
  @Override
  public int maxDegree() {
    int maxDegree = 0;
    for (Entry<V, AdjacencyList<V, E>> entry : edgeMap.entrySet()) {
      int d = entry.getValue().getDegree();
      if (d > maxDegree) {
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
    if (vertexNum() == 0) {
      return 0;
    }

    double degree = 0;
    for (Entry<V, AdjacencyList<V, E>> entry : edgeMap.entrySet()) {
      degree += entry.getValue().getDegree();
    }

    return degree / vertexNum();
  }

  /**
   * Return a start node by specific strategy, and use this api bound with {@link #next(Object)} to
   * iterated graph rather than {@link #iterator()}.
   *
   * @return start node
   */
  @Override
  public V start() {
    return head != null ? head.getOwner() : null;
  }

  @Override
  public boolean add(V vertex) {
    if (vertex == null) {
      return false;
    }

    if (edgeMap.containsKey(vertex)) {
      return false;
    }

    AdjacencyList<V, E> adj = newAdjacentList(vertex);
    if (adj == null) {
      return false;
    }

    edgeMap.put(vertex, adj);

    // Add to the end of the linked list
    if (tail == null) {
      // First node
      head = tail = adj;
    } else {
      // Add to the end
      adj.insertAfter(tail);
      tail = adj;
    }

    return true;
  }

  @Override
  public boolean remove(Object vertex) {
    if (vertex == null) {
      return false;
    }

    // Remove node
    AdjacencyList<V, E> adjToRemove = edgeMap.remove(vertex);
    if (adjToRemove == null) {
      return false;
    }

    // Update head and tail pointers if necessary
    if (adjToRemove == head) {
      head = adjToRemove.getNext();
    }
    if (adjToRemove == tail) {
      tail = adjToRemove.getPre();
    }

    // Remove from linked list
    adjToRemove.unlinkFromList();

    edgeMap.forEach((k, a) -> adjustAdjWhenRemoveNode((V) vertex, a));
    edgeNum -= adjToRemove.size();
    return true;
  }

  @Override
  public boolean contains(Object vertex) {
    return edgeMap.containsKey(vertex);
  }

  @Override
  public int vertexNum() {
    return edgeMap.size();
  }

  @Override
  public int edgeNum() {
    return edgeNum;
  }

  @Override
  public Iterator<V> iterator() {
    return new VertexIterator();
  }

  @Override
  public int degree(V vertex) {
    Objects.requireNonNull(vertex, "Vertex cannot be null");

    AdjacencyList<V, E> adjacency = edgeMap.get(vertex);
    return adjacency != null ? adjacency.getDegree() : 0;
  }

  @Override
  public int selfLoops(V vertex) {
    Objects.requireNonNull(vertex, "Vertex cannot be null");

    AdjacencyList<V, E> adjacency = edgeMap.get(vertex);
    return adjacency != null ? adjacency.getSelfLoopCount() : 0;
  }

  @Override
  public V pre(V v) {
    Objects.requireNonNull(v, "Vertex cannot be null");

    AdjacencyList<V, E> adjacency = edgeMap.get(v);
    if (adjacency == null) {
      return null;
    }

    AdjacencyList<V, E> prevAdjacency = adjacency.getPre();
    return prevAdjacency != null ? prevAdjacency.getOwner() : null;
  }

  @Override
  public V next(V vertex) {
    Objects.requireNonNull(vertex, "Vertex cannot be null");

    AdjacencyList<V, E> adjacency = edgeMap.get(vertex);
    if (adjacency == null) {
      return null;
    }

    AdjacencyList<V, E> nextAdjacency = adjacency.getNext();
    return nextAdjacency != null ? nextAdjacency.getOwner() : null;
  }

  @Override
  public int numberOfLoops() {
    int loops = 0;
    for (AdjacencyList<V, E> adjacency : edgeMap.values()) {
      loops += adjacency.getSelfLoopCount();
    }
    return loops;
  }

  @Override
  public V[] toArray() {
    if (edgeMap.isEmpty()) {
      return null;
    }

    // Get the first vertex to determine the component type
    V firstVertex = edgeMap.keySet().iterator().next();
    @SuppressWarnings("unchecked")
    V[] result = (V[]) Array.newInstance(
        firstVertex.getClass(), edgeMap.size());

    int i = 0;
    for (V vertex : edgeMap.keySet()) {
      result[i++] = vertex;
    }
    return result;
  }

  @Override
  public void forEach(Consumer<? super V> action) {
    Objects.requireNonNull(action, "Action cannot be null");

    for (Map.Entry<V, AdjacencyList<V, E>> entry : edgeMap.entrySet()) {
      V vertex = entry.getKey();
      action.accept(vertex);
    }
  }

  @Override
  public void clear() {
    edgeMap.clear();
    head = null;
    tail = null;
    edgeNum = 0;
  }

  /**
   * Vertex iterator for this graph.
   */
  private class VertexIterator implements Iterator<V> {

    private final Iterator<Map.Entry<V, AdjacencyList<V, E>>> entryIterator;
    private Map.Entry<V, AdjacencyList<V, E>> current;

    VertexIterator() {
      this.entryIterator = edgeMap.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return entryIterator.hasNext();
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      current = entryIterator.next();
      return current.getKey();
    }

    @Override
    public void remove() {
      if (current == null) {
        throw new IllegalStateException();
      }

      V v = current.getKey();
      AdjacencyList<V, E> adjToRemove = current.getValue();

      // Update head and tail pointers if necessary
      if (adjToRemove == head) {
        head = adjToRemove.getNext();
      }
      if (adjToRemove == tail) {
        tail = adjToRemove.getPre();
      }

      // Remove from linked list
      adjToRemove.unlinkFromList();

      entryIterator.remove();
      edgeMap.forEach((k, a) -> adjustAdjWhenRemoveNode(v, a));
      edgeNum -= adjToRemove.size();
      current = null;
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();

    // Write the edgeMap size
    out.writeInt(edgeMap.size());

    // Write all vertices and their adjacencies in iteration order
    for (Map.Entry<V, AdjacencyList<V, E>> entry : edgeMap.entrySet()) {
      V vertex = entry.getKey();
      AdjacencyList<V, E> adjacency = entry.getValue();

      // Write vertex
      out.writeObject(vertex);

      // Write the number of edges for this vertex
      out.writeInt(adjacency.size());

      // Write all edges for this vertex
      for (E edge : adjacency) {
        out.writeObject(edge);
      }
    }
  }


  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    // Initialize transient fields
    this.edgeMap = new LinkedHashMap<>();
    this.head = null;
    this.tail = null;

    // Read vertex count
    int vertexCount = in.readInt();

    // Read all vertices and their adjacencies, building the linked list structure
    for (int i = 0; i < vertexCount; i++) {
      V vertex = (V) in.readObject();

      // Read the number of edges for this vertex
      int edgeCount = in.readInt();

      // Create new adjacency list
      AdjacencyList<V, E> adjacency = newAdjacentList(vertex);

      // Read the specified number of edges
      for (int j = 0; j < edgeCount; j++) {
        E edge = (E) in.readObject();
        adjacency.add(edge);
      }

      // Add to edgeMap
      edgeMap.put(vertex, adjacency);

      // Build the linked list structure in the order vertices are read
      if (head == null) {
        // First vertex
        head = tail = adjacency;
      } else {
        // Link to the end of the list
        adjacency.setPre(tail);
        tail.setNext(adjacency);
        tail = adjacency;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    
    @SuppressWarnings("unchecked")
    AbstractAdjGraph<V, E> other = (AbstractAdjGraph<V, E>) obj;
    
    // Check basic properties
    if (edgeNum != other.edgeNum || vertexNum() != other.vertexNum()) {
      return false;
    }
    
    // Check if all vertices and their adjacencies are equal
    if (!Objects.equals(edgeMap.keySet(), other.edgeMap.keySet())) {
      return false;
    }
    
    // Check adjacency lists for each vertex
    for (V vertex : edgeMap.keySet()) {
      AdjacencyList<V, E> thisAdj = edgeMap.get(vertex);
      AdjacencyList<V, E> otherAdj = other.edgeMap.get(vertex);
      
      if (!Objects.equals(thisAdj, otherAdj)) {
        return false;
      }
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(edgeNum, getClass());
    
    // Include vertices and their adjacencies in hash
    for (Map.Entry<V, AdjacencyList<V, E>> entry : edgeMap.entrySet()) {
      result = 31 * result + Objects.hashCode(entry.getKey());
      result = 31 * result + Objects.hashCode(entry.getValue());
    }
    
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName()).append("{");
    sb.append("vertices=").append(vertexNum());
    sb.append(", edges=").append(edgeNum);
    sb.append(", maxDegree=").append(maxDegree());
    sb.append(", averageDegree=").append(String.format("%.2f", averageDegree()));
    
    if (!edgeMap.isEmpty()) {
      sb.append(", adjacency={");
      boolean firstVertex = true;
      for (Map.Entry<V, AdjacencyList<V, E>> entry : edgeMap.entrySet()) {
        V vertex = entry.getKey();
        AdjacencyList<V, E> adjacency = entry.getValue();
        
        if (!firstVertex) {
          sb.append(", ");
        }
        sb.append(vertex).append(":[");
        
        boolean firstNeighbor = true;
        for (E neighbor : adjacency) {
          if (!firstNeighbor) {
            sb.append(", ");
          }
          sb.append(neighbor);
          firstNeighbor = false;
        }
        sb.append("]");
        firstVertex = false;
      }
      sb.append("}");
    }
    
    sb.append("}");
    return sb.toString();
  }
}
