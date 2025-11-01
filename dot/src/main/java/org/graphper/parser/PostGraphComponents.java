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

package org.graphper.parser;

import org.graphper.api.Cluster.ClusterBuilder;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line.LineBuilder;
import org.graphper.api.Node.NodeBuilder;

/**
 * An optional hook interface for customizing or "post-processing" various
 * graph components (nodes, edges, clusters, entire graphs) after they have
 * been initially constructed during the parsing phase.
 *
 * <p>By implementing this interface, callers can provide logic in any of the
 * default methods to modify or decorate the corresponding builder classes
 * (e.g., {@link NodeBuilder}, {@link LineBuilder}, {@link ClusterBuilder},
 * and {@link GraphvizBuilder}). If no customization is needed, the default
 * (empty) method bodies have no effect.</p>
 *
 * <p>Typical usage involves passing an instance of this interface to the
 * parser or listener, which will invoke these methods at appropriate times
 * before final objects are built.</p>
 *
 * <pre>
 * public class MyPostGraphComponents implements PostGraphComponents {
 *     &#64;Override
 *     public void postNode(NodeBuilder nodeBuilder) {
 *         // For example, always apply a bold style to nodes
 *         nodeBuilder.fontStyle(FontStyle.BOLD);
 *     }
 *
 *     &#64;Override
 *     public void postLine(LineBuilder lineBuilder) {
 *         // Maybe set a default color for edges
 *         lineBuilder.color(Color.BLUE);
 *     }
 *
 *     // ... etc. ...
 * }
 * </pre>
 *
 * @author Jamison Jiang
 */
public interface PostGraphComponents {

  /**
   * Called after a node is initially built, allowing further customization
   * before it is finalized.
   *
   * @param nodeBuilder the builder for the node
   */
  default void postNode(NodeBuilder nodeBuilder) {
  }

  /**
   * Called after a line (edge) is initially built, allowing further customization
   * before it is finalized.
   *
   * @param lineBuilder the builder for the line
   */
  default void postLine(LineBuilder lineBuilder) {
  }

  /**
   * Called after a cluster is initially built, allowing further customization
   * before it is finalized.
   *
   * @param clusterBuilder the builder for the cluster
   */
  default void postCluster(ClusterBuilder clusterBuilder) {
  }

  /**
   * Called after the entire graph (either a {@code graph} or {@code digraph}) has
   * been initially built, allowing further customization before it is finalized.
   *
   * @param graphvizBuilder the builder for the graph
   */
  default void postGraphviz(GraphvizBuilder graphvizBuilder) {
  }
}

