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

package org.graphper.layout;

import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;

/**
 * Diagram Mover, which provides methods for moving various elements in the graph.
 *
 * @author Jamison Jiang
 */
public interface Shifter {

  /**
   * Move graphviz item
   *
   * @param graphvizDrawProp graphviz properties object
   */
  void graph(GraphvizDrawProp graphvizDrawProp);

  /**
   * Move cluster item
   *
   * @param clusterDrawProp cluster properties object
   */
  void cluster(ClusterDrawProp clusterDrawProp);

  /**
   * Move node item
   *
   * @param nodeDrawProp node properties object
   */
  void node(NodeDrawProp nodeDrawProp);

  /**
   * Move line item
   *
   * @param lineDrawProp line properties object
   */
  void line(LineDrawProp lineDrawProp);
}
