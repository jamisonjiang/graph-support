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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A data structure of a leader and its members, and the members are stored in a linked list.
 *
 * @param <V> the type of vertex
 * @param <E> the type of edge
 * @author Jamison Jiang
 */
class Bag<V, E> implements Iterable<E>, Serializable {

  private static final long serialVersionUID = -3433776704595616074L;

  Boolean unmodify;

  final V vertex;

  Node<E> header;

  Node<E> tail;

  transient int bModCount;

  Bag(V vertex) {
    this.vertex = vertex;
  }

  Bag(boolean unmodify) {
    this.vertex = null;
    this.unmodify = unmodify;
  }

  @Override
  public Iterator<E> iterator() {
    return new Bag<V, E>.BagIterator();
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    Node<E> current = header;
    while (current != null) {
      action.accept(current.value);
      current = current.next;
    }
  }

  void add(E e) {
    checkIsUnmodify();
    if (header == null) {
      header = new Node<>(e, null, null);
      tail = header;
    } else {
      tail = tail.next = new Node<>(e, tail, null);
    }
    bModCount++;
  }

  boolean remove(Object obj) {
    checkIsUnmodify();
    Node<E> pre;
    Node<E> current = header;
    // Find adjacent vertices that need to be deleted.
    while (current != null) {
      if (Objects.equals(current.value, obj)) {
        break;
      }
      current = current.next;
    }
    if (current == null) {
      return false;
    }
    // Iteration complete
    if (current == tail) {
      tail = tail.pre;
      if (tail != null) {
        tail.next = null;
      } else {
        header = null;  // header and tail overlap
      }
    }
    // Delete header
    else if (current == header) {
      header = header.next;
      header.pre = null;
    } else if ((pre = current.pre) != null) {
      pre.next = current.next;
      current.next.pre = pre;
    }
    bModCount++;
    return true;
  }

  /**
   * Removes all the elements of this collection that satisfy the given predicate. Errors or runtime
   * exceptions thrown during iteration or by the predicate are relayed to the caller.
   *
   * @param predicate a predicate which returns {@code true} for elements to be removed
   * @throws NullPointerException if the predicate is null
   */
  boolean removeIf(Predicate<E> predicate) {
    Objects.requireNonNull(predicate);
    checkIsUnmodify();
    Node<E> point = header;
    while (point != null) {
      if (predicate.test(point.value)) {
        Node<E> pre = point.pre;
        Node<E> next = point.next;
        if (pre != null) {
          pre.next = next;
        }
        if (next != null) {
          next.pre = pre;
        }
        if (point == header) {
          header = next;
        }
        if (point == tail) {
          tail = pre;
        }
        bModCount++;
        return true;
      }
      point = point.next;
    }

    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Bag)) {
      return false;
    }
    Bag<?, ?> bag = (Bag<?, ?>) obj;
    if (!Objects.equals(unmodify, bag.unmodify)) {
      return false;
    }
    if (!Objects.equals(this.vertex, bag.vertex)) {
      return false;
    }
    if (!Objects.equals(getNodeValue(header), getNodeValue(bag.header))) {
      return false;
    }
    if (!Objects.equals(getNodeValue(tail), getNodeValue(bag.tail))) {
      return false;
    }

    Node<E> c1 = header;
    Node<?> c2 = bag.header;
    while (Objects.equals(getNodeValue(c1), getNodeValue(c2)) && Objects.nonNull(c1)) {
      c1 = c1.next;
      c2 = c2.next;
    }
    return c1 == null && c2 == null;
  }

  @Override
  public int hashCode() {
    int hashCode = vertex != null ? vertex.hashCode() : 1;
    Node<E> point = header;
    while (point != null) {
      if (point.value != null) {
        hashCode += point.value.hashCode();
      } else {
        hashCode++;
      }
      point = point.next;
    }
    hashCode += Bag.class.hashCode();
    return hashCode;
  }

  void checkIsUnmodify() {
    if (unmodify != null && unmodify) {
      throw new UnmodifiableBagException("Bag cannot be modify");
    }
  }

  private Object getNodeValue(Node<?> node) {
    if (node == null) {
      return null;
    }
    return node.value;
  }

  protected class BagIterator implements Iterator<E> {

    Node<E> point; // Iterator pointer
    private int exceptModCount = bModCount; // The number of next revisions

    BagIterator() {
      this.point = header;
    }

    @Override
    public boolean hasNext() {
      return point != null;
    }

    @Override
    public E next() {
      checkIsConcurrentModify();
      if (point == null) {
        throw new NoSuchElementException();
      }
      E value = point.value;
      point = point.next;
      return value;
    }

    @Override
    public void remove() {
      checkIsUnmodify();
      checkIsConcurrentModify();
      Node<E> pre;
      Node<E> prepre;
      if (point == header) {
        throw new IllegalStateException("Iterator not specified");
      }
      // Iteration complete
      if (point == null) {
        if (tail != null) {
          tail = tail.pre;
          if (tail != null) {
            tail.next = null;
          } else {
            header = null;  // header and tail overlap
          }
        }
      }
      // Delete header
      else if ((pre = point.pre) == header) {
        header = header.next;
        header.pre = null;
      } else if (pre != null && (prepre = pre.pre) != null) {
        prepre.next = point;
        point.pre = prepre;
      }
      exceptModCount++;
      bModCount++;
    }

    private void checkIsConcurrentModify() {
      if (exceptModCount != bModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /**
   * A doubly linked list for retrieving all adjacent vertices of a vertex
   */
  private static final class Node<E> implements Serializable {

    private static final long serialVersionUID = 6069018214912306217L;

    final E value;

    Node<E> pre;

    Node<E> next;

    Node(E value) {
      this.value = value;
    }

    Node(E value, Node<E> pre, Node<E> next) {
      this(value);
      this.pre = pre;
      this.next = next;
    }
  }
}
