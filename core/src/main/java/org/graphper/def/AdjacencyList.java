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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic adjacency list that can store vertices or edges with degree and selfloop tracking.
 * Uses composition with an internal ArrayList to ensure all modifications are properly tracked.
 * Enhanced with doubly-linked list structure for efficient next/previous navigation.
 *
 * @param <V> the type of vertex
 * @param <T> the type of adjacent object (vertex or edge)
 * @author Jamison Jiang
 */
public class AdjacencyList<V, T> implements Iterable<T>, Serializable {
  private static final long serialVersionUID = -1234567890123456789L;

  /**
   * Internal list for storing adjacent objects.
   */
  private final List<T> inner;

  /**
   * The vertex this adjacency list belongs to.
   */
  private final V owner;

  /**
   * Number of self-loops in this adjacency list.
   */
  private int selfLoopCount;

  /**
   * Function to determine if an adjacent object represents a self-loop.
   */
  private final Function<T, Boolean> selfLoopChecker;

  /**
   * Previous vertex in the linked list for efficient traversal.
   */
  private transient AdjacencyList<V, T> pre;

  /**
   * Next vertex in the linked list for efficient traversal.
   */
  private transient AdjacencyList<V, T> next;

  /**
   * Construct adjacency list for vertices.
   *
   * @param owner the vertex this adjacency list belongs to
   */
  public AdjacencyList(V owner) {
    this(owner, null);
  }

  /**
   * Construct adjacency list with custom self-loop checker.
   *
   * @param owner the vertex this adjacency list belongs to
   * @param selfLoopChecker function to check if an adjacent object is a self-loop
   */
  public AdjacencyList(V owner, Function<T, Boolean> selfLoopChecker) {
    this.inner = new ArrayList<>();
    this.owner = owner;
    this.selfLoopCount = 0;
    this.pre = null;
    this.next = null;
    
    // Default self-loop checker for vertices
    if (selfLoopChecker == null) {
      this.selfLoopChecker = (T adjacent) -> Objects.equals(adjacent, owner);
    } else {
      this.selfLoopChecker = selfLoopChecker;
    }
  }

  /**
   * Construct adjacency list with initial capacity.
   *
   * @param owner the vertex this adjacency list belongs to
   * @param initialCapacity initial capacity of the list
   * @param selfLoopChecker function to check if an adjacent object is a self-loop
   */
  public AdjacencyList(V owner, int initialCapacity, Function<T, Boolean> selfLoopChecker) {
    this.inner = new ArrayList<>(initialCapacity);
    this.owner = owner;
    this.selfLoopCount = 0;
    this.pre = null;
    this.next = null;
    
    // Default self-loop checker for vertices
    if (selfLoopChecker == null) {
      this.selfLoopChecker = (T adjacent) -> Objects.equals(adjacent, owner);
    } else {
      this.selfLoopChecker = selfLoopChecker;
    }
  }

  // Basic operations

  public boolean isEmpty() {
    return inner.isEmpty();
  }

  public boolean contains(Object o) {
    return inner.contains(o);
  }

  public T get(int index) {
    return inner.get(index);
  }

  @Override
  public Iterator<T> iterator() {
    return new MetadataUpdatingIterator(inner.iterator(), false);
  }

  // AdjacencyList specific methods

  /**
   * Get the number of self-loops in this adjacency list.
   */
  public int getSelfLoopCount() {
    return selfLoopCount;
  }

  /**
   * Get the degree.
   */
  public int getDegree() {
    return inner.size();
  }

  /**
   * Get the owner vertex of this adjacency list.
   */
  public V getOwner() {
    return owner;
  }

  /**
   * Check if this adjacency list contains a self-loop.
   */
  public boolean hasSelfLoop() {
    return selfLoopCount > 0;
  }

  // Linked list navigation methods

  /**
   * Get the previous adjacency list in the linked list.
   *
   * @return previous adjacency list or null if this is the first
   */
  public AdjacencyList<V, T> getPre() {
    return pre;
  }

  /**
   * Get the next adjacency list in the linked list.
   *
   * @return next adjacency list or null if this is the last
   */
  public AdjacencyList<V, T> getNext() {
    return next;
  }

  @Override
  public void forEach(Consumer<? super T> consumer) {
    Objects.requireNonNull(consumer);
    for (int i = 0; i < inner.size(); i++) {
      consumer.accept(inner.get(i));
    }
  }

  boolean add(T adjacent) {
    inner.add(adjacent);
    updateMetadataOnAdd(adjacent);
    return true;
  }

  boolean removeIf(Predicate<? super T> filter) {
    boolean modified = false;
    Iterator<T> iterator = new MetadataUpdatingIterator(inner.iterator(), true);
    while (iterator.hasNext()) {
      T element = iterator.next();
      if (filter.test(element)) {
        iterator.remove();
        updateMetadataOnRemove(element);
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Set the previous adjacency list in the linked list.
   * This method is package-private to be used by graph implementations.
   *
   * @param pre previous adjacency list
   */
  void setPre(AdjacencyList<V, T> pre) {
    this.pre = pre;
  }

  /**
   * Set the next adjacency list in the linked list.
   * This method is package-private to be used by graph implementations.
   *
   * @param next next adjacency list
   */
  void setNext(AdjacencyList<V, T> next) {
    this.next = next;
  }

  /**
   * Insert this adjacency list after the specified adjacency list.
   * This method maintains the doubly-linked list structure.
   *
   * @param after the adjacency list after which this should be inserted
   */
  void insertAfter(AdjacencyList<V, T> after) {
    if (after == null) {
      return;
    }
    
    this.pre = after;
    this.next = after.next;
    
    if (after.next != null) {
      after.next.pre = this;
    }
    after.next = this;
  }

  /**
   * Insert this adjacency list before the specified adjacency list.
   * This method maintains the doubly-linked list structure.
   *
   * @param before the adjacency list before which this should be inserted
   */
  void insertBefore(AdjacencyList<V, T> before) {
    if (before == null) {
      return;
    }
    
    this.next = before;
    this.pre = before.pre;
    
    if (before.pre != null) {
      before.pre.next = this;
    }
    before.pre = this;
  }

  /**
   * Remove this adjacency list from the linked list.
   * This method maintains the doubly-linked list structure.
   */
  void unlinkFromList() {
    if (pre != null) {
      pre.next = next;
    }
    if (next != null) {
      next.pre = pre;
    }
    pre = null;
    next = null;
  }

  /**
   * Update metadata when adding an adjacent object.
   */
  private void updateMetadataOnAdd(T adjacent) {
    if (Boolean.TRUE.equals(selfLoopChecker.apply(adjacent))) {
      selfLoopCount++;
    }
  }

  /**
   * Update metadata when removing an adjacent object.
   */
  private void updateMetadataOnRemove(T adjacent) {
    if (Boolean.TRUE.equals(selfLoopChecker.apply(adjacent))) {
      selfLoopCount--;
    }
  }

  // Factory methods

  /**
   * Create a new adjacency list for vertices.
   */
  public static <V> AdjacencyList<V, V> forUndirectedVertices(V owner) {
    return new AdjacencyList<>(owner, null);
  }

  /**
   * Create a new adjacency list for vertices.
   */
  public static <V> AdjacencyList<V, V> forDirectedVertices(V owner) {
    return new AdjacencyList<>(owner, null);
  }

  /**
   * Create a new adjacency list for edges.
   */
  public static <V, E extends BaseEdge<V, E>> AdjacencyList<V, E> forUndirectedEdges(V owner) {
    Function<E, Boolean> selfLoopChecker = (E edge) -> {
      if (edge == null) {
        return false;
      }
      V other = edge.other(owner);
      return Objects.equals(owner, other);
    };
    return new AdjacencyList<>(owner, selfLoopChecker);
  }

  /**
   * Create a new adjacency list for edges.
   */
  public static <V, E extends DirectedEdge<V, E>> AdjacencyList<V, E> forDirectedEdges(V owner) {
    Function<E, Boolean> selfLoopChecker = (E edge) -> {
      if (edge == null) return false;
      return Objects.equals(edge.from(), edge.to());
    };
    
    return new AdjacencyList<>(owner, selfLoopChecker);
  }

  // Iterator that updates metadata on remove operations

  /**
   * Iterator wrapper that updates metadata on remove operations.
   */
  private class MetadataUpdatingIterator implements Iterator<T> {
    private final Iterator<T> delegate;
    private final boolean supportRemove;
    private boolean canRemove;

    private MetadataUpdatingIterator(Iterator<T> delegate, boolean supportRemove) {
      this.delegate = delegate;
      this.supportRemove = supportRemove;
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public T next() {
      T last = delegate.next();
      canRemove = true;
      return last;
    }

    @Override
    public void remove() {
      if (!supportRemove) {
        throw new UnmodifiablePointException("Not allow to remove element from Adjacency");
      }

      if (!canRemove) {
        throw new IllegalStateException();
      }
      delegate.remove();
      canRemove = false;
    }
  }

  @Override
  public String toString() {
    return "AdjacencyList{" +
        "owner=" + owner +
        ", selfLoopCount=" + selfLoopCount +
        ", degree=" + getDegree() +
        ", elements=" + inner +
        '}';
  }
}