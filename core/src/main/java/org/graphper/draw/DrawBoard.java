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

package org.graphper.draw;

import org.graphper.api.GraphResource;
import org.graphper.api.Line;
import org.graphper.api.Node;

/**
 * The renderer's draw board. Draw the basic elements on the draw board, remove the basic elements,
 * and then return the adjustable brush object to continue fine-tuning the corresponding elements.
 *
 * @param <N> node brush type
 * @param <L> line brush type
 * @param <C> cluster brush type
 * @param <G> graphviz brush type
 * @author Jamison Jiang
 */
public interface DrawBoard<N extends Brush, L extends Brush, C extends Brush, G extends Brush> {

  /**
   * Draw a basic object of graphviz, and then return the corresponding editable brush.
   *
   * @param graphviz graphviz draw properties
   * @return graphviz brush
   */
  G drawGraph(GraphvizDrawProp graphviz);

  /**
   * Draw a basic object of cluster, and then return the corresponding editable brush.
   *
   * @param cluster cluster draw properties
   * @return cluster brush
   */
  C drawCluster(ClusterDrawProp cluster);

  /**
   * Draw a basic object of node, and then return the corresponding editable brush.
   *
   * @param node node draw properties
   * @return node brush
   */
  N drawNode(NodeDrawProp node);

  /**
   * Draw a basic object of line, and then return the corresponding editable brush.
   *
   * @param line line draw properties
   * @return line brush
   */
  L drawLine(LineDrawProp line);

  /**
   * Remove the corresponding node from the draw board.
   *
   * @param node node to be removed
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  boolean removeNode(Node node);

  /**
   * Remove the corresponding line from the draw board.
   *
   * @param line line to be removed
   * @return <tt>true</tt> if this graph changed as a result of the call
   */
  boolean removeLine(Line line);

  /**
   * Returns the content of the draw board.
   *
   * @return draw board content
   * @throws FailInitResourceException cannot convert to {@code GraphResource}
   */
  GraphResource graphResource() throws FailInitResourceException;
}
