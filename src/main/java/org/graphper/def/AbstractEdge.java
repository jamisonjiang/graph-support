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
import java.util.Objects;
import org.graphper.util.Asserts;

/**
 * Abstract implementation of graph edges, record the two endpoints of an edge using two local
 * properties.
 *
 * @param <V> vertex type
 * @param <E> subclass type
 * @author Jamison Jiang
 */
public abstract class AbstractEdge<V, E extends AbstractEdge<V, E>> implements
    BaseEdge<V, E>, Serializable {

  private static final long serialVersionUID = -7932182426978603206L;

  /**
   * endpoint of edge
   */
  protected final V left;

  /**
   * endpoint of edge
   */
  protected final V right;

  /**
   * weight of edge
   */
  protected final double weight;

  /**
   * Initialize edges based on vertices, with weights defaulting to 0.
   *
   * @param left  endpoint of edge
   * @param right endpoint of edge
   */
  protected AbstractEdge(V left, V right) {
    this(left, right, 0);
  }

  /**
   * Initialize edges from vertices and weights.
   *
   * @param left   endpoint of edge
   * @param right  endpoint of edge
   * @param weight weight of edge
   */
  protected AbstractEdge(V left, V right, double weight) {
    Asserts.nullArgument(left, "left");
    Asserts.nullArgument(left, "right");

    this.left = left;
    this.right = right;
    this.weight = weight;
  }

  /**
   * Initialize the current edge with an edge.
   *
   * @param edge template edge
   */
  protected AbstractEdge(E edge) {
    Objects.requireNonNull(edge);
    this.left = edge.left;
    this.right = edge.right;
    this.weight = edge.weight;
  }

  /**
   * Return weight of edge.
   *
   * @return weight of edge
   */
  @Override
  public double weight() {
    return weight;
  }

  /**
   * Return one of the two endpoints.
   *
   * @return one of the two endpoints
   */
  public V either() {
    return left;
  }

  /**
   * Returns another vertex of the edge based on the vertex of the edge, or null if the vertex is
   * not part of the edge.
   *
   * @param v endpoint of edge
   * @return edge another vertex
   */
  public V other(V v) {
    if (v == left) {
      return right;
    }
    if (v == right) {
      return left;
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AbstractEdge)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    AbstractEdge<V, E> edge = (AbstractEdge<V, E>) obj;
    return Objects.equals(edge.left, left) && Objects.equals(edge.right, right)
        && Objects.equals(edge.weight, weight);
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    hashCode += left.hashCode();
    hashCode += right.hashCode();
    hashCode += weight;
    return hashCode;
  }

  @Override
  public String toString() {
    return "AbstractEdge{" +
        "left=" + left +
        ", right=" + right +
        ", weight=" + weight +
        '}';
  }
}
