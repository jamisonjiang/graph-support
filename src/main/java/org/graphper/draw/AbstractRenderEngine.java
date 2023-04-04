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

import org.graphper.layout.LayoutEngine;
import org.graphper.util.Asserts;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Layout;

/**
 * Abstract rendering engine.
 *
 * @author Jamison Jiang
 */
public abstract class AbstractRenderEngine implements RenderEngine {

  @Override
  public GraphResource render(Graphviz graphviz) throws ExecuteException {
    Asserts.nullArgument(graphviz, "graphviz");

    // Get layout
    Layout layout = graphviz.graphAttrs().getLayout();
    layout = layout == null ? Layout.DOT : layout;

    try {
      LayoutEngine layoutEngine = layout.getLayoutEngine();
      return render0(layoutEngine.layout(graphviz, this));
    } catch (Exception e) {
      throw new ExecuteException("Layout engine execute error: ", e);
    }
  }

  /**
   * Render the graph according to {@link DrawGraph} and return the rendered result.
   *
   * @param drawGraph the graph description object after layout
   * @return render the result of graph
   * @throws ExecuteException rendering exception
   */
  protected abstract GraphResource render0(DrawGraph drawGraph) throws ExecuteException;
}
