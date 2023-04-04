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

import java.util.List;
import java.util.function.Function;
import org.graphper.util.Asserts;

/**
 * The abstract graph element rendering execution pipeline triggers all editors according to the
 * incoming graph elements.
 *
 * @param <I> The type of graph element to render
 * @param <B> The brush type for the current element
 * @param <T> The editor type of the current element
 * @param <S> Current pipeline trigger type
 * @author Jamison Jiang
 */
public abstract class AbstractPipelineTrigger<
    I,
    B extends Brush,
    T extends Editor<I, B>,
    S extends AbstractPipelineTrigger<I, B, T, S>> {

  protected final List<T> editors;

  protected final DrawGraph drawGraph;

  protected AbstractPipelineTrigger(List<T> editors, DrawGraph drawGraph) {
    Asserts.nullArgument(editors, "editors");
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.editors = editors;
    this.drawGraph = drawGraph;
  }

  /**
   * Trigger all editors of the element, the order of triggering is equal to the iteration order of
   * {@link #renderItems()}.
   *
   * @param brushFactory brush factory, which produces the corresponding {@link Brush} for the
   *                     corresponding type of element
   */
  public void trigger(Function<I, B> brushFactory) {
    Asserts.nullArgument(brushFactory, "brushFactory");

    // All graph items
    for (I item : renderItems()) {
      B brush = brushFactory.apply(item);

      Asserts.illegalArgument(brush == null, "BrushFactory cannot create null brush!");

      // Trigger all editors
      for (int i = 0; i < editors.size(); i++) {
        T editor = editors.get(i);
        if (!editor.edit(item, brush)) {
          break;
        }
      }
    }
  }

  /**
   * Returns all graph elements that currently need to be edited.
   *
   * @return all graph elements that currently need to edited
   */
  protected abstract Iterable<I> renderItems();
}
