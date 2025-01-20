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

/**
 * Default undirected edge object.
 *
 * @param <V> vertex type
 * @param <E> subclass type
 * @author Jamison Jiang
 */
public abstract class AbstractUndirectedEdge<V, E extends AbstractUndirectedEdge<V, E>>
    extends AbstractEdge<V, E> implements Edge<V, E>, Serializable {

  private static final long serialVersionUID = -8743193141849601861L;

  /**
   * Initialize edges based on vertices, with weights defaulting to 0.
   *
   * @param left  endpoint of edge
   * @param right endpoint of edge
   */
  protected AbstractUndirectedEdge(V left, V right) {
    super(left, right);
  }

  /**
   * Initialize edges from vertices and weights.
   *
   * @param left   endpoint of edge
   * @param right  endpoint of edge
   * @param weight weight of edge
   */
  protected AbstractUndirectedEdge(V left, V right, double weight) {
    super(left, right, weight);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && obj instanceof AbstractUndirectedEdge;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + AbstractUndirectedEdge.class.hashCode();
  }
}
