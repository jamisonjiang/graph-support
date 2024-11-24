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

import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.layout.ALine;

public class FLine extends ALine<FNode, FLine> {

  private static final long serialVersionUID = -2526416728355227232L;

  public FLine(FNode from, FNode to, LineAttrs lineAttrs) {
    super(from, to, null, lineAttrs);
  }

  public FLine(FNode from, FNode to, Line line, LineAttrs lineAttrs) {
    super(from, to, line, lineAttrs);
  }

  public FLine(FNode from, FNode to, double weight, Line line, LineAttrs lineAttrs) {
    super(from, to, weight, line, lineAttrs);
  }
}
