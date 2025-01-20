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
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vertex index, the adjacency array will use the index to locate the vertex. When the vertex is
 * serialized together with the adjacency array, when deserializing, the vertex index will only
 * record the index position of the vertex in this adjacency array. Other index records are
 * missing.
 *
 * @author Jamison Jiang
 */
public class VertexIndex implements Serializable {

  private static final long serialVersionUID = -826073470335347686L;

  /**
   * Vertex index record, the index position of the current index in different graphs.
   */
  private transient volatile Map<GraphRef, Integer> graphIndex;

  public VertexIndex() {
  }

  Map<GraphRef, Integer> getGraphIndex() {
    if (graphIndex == null) {
      synchronized (this) {
        if (graphIndex == null) {
          graphIndex = new ConcurrentHashMap<>(1);
        }
      }
    }

    return graphIndex;
  }

  Integer index(GraphRef graphRef) {
    return graphIndex.get(graphRef);
  }

  /**
   * If the map is only used by indexes, it is directly GC. Otherwise, when the index is used in
   * multiple graphs, the graph may not be referenced, but the vertex is applied to another graph,
   * and the graph without references cannot be GC. And using {@code GraphRef} instead of using the
   * original graph object can avoid reference changes after the {@link BaseGraph} subclass rewrites
   * hashCode.
   */
  static class GraphRef extends WeakReference<BaseGraph<?>> {

    public GraphRef(BaseGraph<?> referent) {
      super(referent);
    }
  }
}
