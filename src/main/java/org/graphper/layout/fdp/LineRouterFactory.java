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

package org.graphper.layout.fdp;

import org.graphper.api.Graphviz;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.LineRouter;

/**
 * Abstract factory for creating instances of {@link LineRouter}.
 *
 * <p>This factory defines methods for determining whether a specific type of line routing
 * logic is applicable and for creating instances of the appropriate {@link LineRouter}
 * implementation.</p>
 *
 * @param <T> the type of {@link LineRouter} produced by this factory
 * @author Jamison Jiang
 */
public abstract class LineRouterFactory<T extends LineRouter> {

  /**
   * Determines whether this factory's routing logic is applicable to the given {@link Graphviz} instance.
   *
   * @param graphviz the {@link Graphviz} instance to evaluate
   * @return {@code true} if this factory should handle routing for the given {@link Graphviz} instance,
   *         {@code false} otherwise
   */
  protected abstract boolean needDeal(Graphviz graphviz);

  /**
   * Creates a new instance of the {@link LineRouter} for the specified graph structures.
   *
   * @param drawGraph the {@link DrawGraph} instance containing graphical representations of the graph
   * @param fdpGraph  the {@link FdpGraph} instance representing the layout graph
   * @return a new instance of the {@link LineRouter}
   */
  protected abstract T newInstance(DrawGraph drawGraph, FdpGraph fdpGraph);
}