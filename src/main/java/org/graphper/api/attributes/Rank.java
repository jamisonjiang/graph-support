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

package org.graphper.api.attributes;

import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Subgraph;

/**
 * Rank constraints on the nodes in {@link Subgraph}, only valid for {@link Layout#DOT}.
 *
 * <p> The effective range of this attribute is the <strong>closest effective container</strong> of
 * the node. Here is an example of what is the <strong>closest effective container</strong> for a
 * node:
 * <pre>{@code
 * digraph G {
 *   A
 *   // Subgraph
 *   {
 *     rank = same
 *     B -> C -> D
 *   }
 *   // Cluster
 *   subgraph cluster_A {
 *     E
 *     subgraph cluster_B {
 *       F
 *     }
 *   }
 * }
 * }</pre>
 * For the above dot script, the containers contained in it are the root container <tt>G</tt>
 * ({@link Graphviz}), a {@link Subgraph} and two {@link Cluster}s (one nested inside the other).
 * The following table shows the <strong>closest effective container</strong> in which all nodes are
 * located:
 * <pre>
 *  +----------------+-----------------------------+
 *  |      Nodes     | Closest Effective Container |
 *  +----------------------------------------------+
 *  |    A,B,C,D     |        G (Graphviz)         |
 *  +----------------------------------------------+
 *  |       E        |     cluster_A (Cluster)     |
 *  +----------------------------------------------+
 *  |       F        |     cluster_B (Cluster)     |
 *  +----------------------------------------------+
 * </pre>
 *
 * @author Jamison Jiang
 */
public enum Rank {

  /**
   * Indicates that all nodes under the subgraph must be at the same level.
   */
  SAME,

  /**
   * Indicates that all nodes should be at the minimum rank of the <strong>Closest Effective
   * Container</strong>. Other nodes that are not in the current subgraph are allowed in the minimum
   * rank.
   */
  MIN,

  /**
   * Indicates that all nodes should be at the minimum rank of the <strong>Closest Effective
   * Container</strong>. Other nodes that are not in the current subgraph are not allowed in the
   * minimum rank (The minimum rank will only have the nodes of the current subgraph).
   */
  SOURCE,

  /**
   * Indicates that all nodes should be at the maximum rank of the <strong>Closest Effective
   * Container</strong>. Other nodes that are not in the current subgraph are allowed in the maximum
   * rank.
   */
  MAX,

  /**
   * Indicates that all nodes should be at the maximum rank of the <strong>Closest Effective
   * Container</strong>. Other nodes that are not in the current subgraph are not allowed in the
   * maximum rank (The maximum rank will only have the nodes of the current subgraph).
   */
  SINK;
}
