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

import org.graphper.api.Line;
import org.graphper.def.AbstractDirectedEdge;

public class ALine<N extends ANode, E extends ALine<N, E>> extends AbstractDirectedEdge<N, E> {

  private static final long serialVersionUID = 7155243474988517017L;
  protected final Line line;

  public ALine(N from, N to, Line line) {
    super(from, to);
    this.line = line;
  }

  public ALine(N from, N to, double weight, Line line) {
    super(from, to, weight);
    this.line = line;
  }

  @Override
  public E reverse() {
    return null;
  }

  @Override
  public E copy() {
    return null;
  }

  public Line getLine() {
    return line;
  }

  public boolean empty() {
    return line == null;
  }
}