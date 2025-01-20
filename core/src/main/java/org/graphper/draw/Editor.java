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

/**
 * An editor for editing drawing elements, the basic unit of pipeline arrangement.
 *
 * @param <T> type of element
 * @param <B> brush type
 * @author Jamison Jiang
 */
public interface Editor<T, B extends Brush> {

  /**
   * Draw the elements in the graph, return true or false to indicate whether to continue to the
   * next editor.
   *
   * @param drawnEle elements to be drawn
   * @param brush    draw brush
   * @return <tt>true</tt> if the next editor continues editing
   */
  boolean edit(T drawnEle, B brush);
}
