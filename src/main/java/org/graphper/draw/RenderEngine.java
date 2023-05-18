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

import java.util.Collections;
import java.util.List;
import org.graphper.layout.ShifterStrategy;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Layout;

/**
 * {@link Graphviz}'s rendering engine first executes the {@link Layout} engine and then executes
 * the drawing process.
 *
 * @author Jamison Jiang
 */
public interface RenderEngine {

  /**
   * Render the graph according to {@link DrawGraph} and return the rendered result.
   *
   * @param graphviz the graphviz
   * @return render the result of graph
   * @throws ExecuteException rendering exception
   */
  GraphResource render(Graphviz graphviz) throws ExecuteException;

  default List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph) {
    return Collections.emptyList();
  }

  default boolean needShift(DrawGraph drawGraph) {
    return false;
  }
}
