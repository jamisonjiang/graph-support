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

import org.graphper.api.Graphviz;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.RenderEngine;

/**
 * The layout engine, according to the original {@link Graphviz}, generates corresponding node
 * coordinates, line coordinates, and various layout attributes according to different layout
 * algorithms, and finally returns a graph description object that can be rendered.
 *
 * @author Jamison Jiang
 */
public interface LayoutEngine {

  /**
   * According to the original {@link Graphviz}, returns a draw graph object.
   *
   * @param graphviz graphviz
   * @return draw graph object
   */
  default DrawGraph layout(Graphviz graphviz) throws ExecuteException {
    return layout(graphviz, null);
  }

  /**
   * According to the original {@link Graphviz} and {@link RenderEngine}, returns a draw graph
   * object.
   *
   * @param graphviz graphviz
   * @param renderEngine render engine
   * @return draw graph object
   */
  DrawGraph layout(Graphviz graphviz, RenderEngine renderEngine) throws ExecuteException;
}
