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

import java.util.Objects;
import org.graphper.api.Line;

/**
 * Line editor.
 *
 * @param <B> line editor type
 */
public interface LineEditor<B extends Brush> extends Editor<LineDrawProp, B> {

  /**
   * Edit line, return <tt>false</tt> if you want to terminate the process.
   *
   * @param lineDrawProp line to be drawn
   * @param brush        line brush
   * @return <tt>true</tt> if the next editor continues editing
   */
  @Override
  default boolean edit(LineDrawProp lineDrawProp, B brush) {
    Objects.requireNonNull(lineDrawProp);

    return edit(lineDrawProp.getLine(), brush);
  }

  /**
   * Edit line, return <tt>false</tt> if you want to terminate the process.
   *
   * @param line  line to be drawn
   * @param brush line brush
   * @return <tt>true</tt> if the next editor continues editing
   */
  default boolean edit(Line line, B brush) {
    return true;
  }
}
