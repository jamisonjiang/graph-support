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

package org.graphper.layout.dot;

import java.util.Objects;
import org.graphper.def.AbstractUndirectedEdge;

class ULine extends AbstractUndirectedEdge<DNode, ULine> {

  private static final long serialVersionUID = -8382575086140748424L;

  private final DLine dLine;

  public ULine(DNode left, DNode right, DLine dLine, double weight) {
    super(left, right, weight);
    Objects.requireNonNull(dLine);
    this.dLine = dLine;
  }

  DLine getdLine() {
    return dLine;
  }

  int slack() {
    return dLine.slack();
  }

  int reduceLen() {
    return dLine.reduceLen();
  }

  int limit() {
    return dLine.limit();
  }

  double cutVal() {
    return dLine.getCutVal();
  }

  @Override
  public ULine copy() {
    return new ULine(left, right, dLine, weight);
  }

  @Override
  public String toString() {
    return "ULine{" +
        "left=" + left.name() +
        ", right=" + right.name() +
        ", weight=" + weight +
        ", limit=" + limit() +
        ", cutval=" + cutVal() +
        '}';
  }
}
