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
 * Property brush, use this object to paint elements.
 *
 * @author Jamison Jiang
 */
public interface Brush {

  /**
   * Returns the current draw board.
   *
   * @param <N> node brush type
   * @param <L> line brush type
   * @param <C> cluster brush type
   * @param <G> graphviz brush type
   * @return current draw board
   */
  <N extends Brush, L extends Brush, C extends Brush, G extends Brush> DrawBoard<N, L, C, G> drawBoard();
}
