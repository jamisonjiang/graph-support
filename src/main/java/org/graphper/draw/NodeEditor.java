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
import org.graphper.api.Node;

/**
 * Node editor.
 *
 * @param <B> node editor type
 */
public interface NodeEditor<B extends Brush> extends Editor<NodeDrawProp, B> {

  /**
   * Edit node, return <tt>false</tt> if you want to terminate the process.
   *
   * @param nodeDrawProp node to be drawn
   * @param brush        node brush
   * @return <tt>true</tt> if the next editor continues editing
   */
  @Override
  default boolean edit(NodeDrawProp nodeDrawProp, B brush) {
    Objects.requireNonNull(nodeDrawProp);

    return edit(nodeDrawProp.getNode(), brush);
  }

  /**
   * Edit node, return <tt>false</tt> if you want to terminate the process.
   *
   * @param node  node to be drawn
   * @param brush node brush
   * @return <tt>true</tt> if the next editor continues editing
   */
  default boolean edit(Node node, B brush) {
    return true;
  }
}
