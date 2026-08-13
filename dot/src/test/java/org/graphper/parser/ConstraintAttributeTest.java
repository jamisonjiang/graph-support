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

package org.graphper.parser;

import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstraintAttributeTest {

  @Test
  public void parsesConstraintFalse() {
    Graphviz graph = DotParser.parse("digraph { a -> b [constraint=false] }");
    Line line = graph.lines().iterator().next();
    Assertions.assertEquals(Boolean.FALSE, line.lineAttrs().getConstraint());
  }

  @Test
  public void parsesConstraintTrue() {
    Graphviz graph = DotParser.parse("digraph { a -> b [constraint=true] }");
    Line line = graph.lines().iterator().next();
    Assertions.assertEquals(Boolean.TRUE, line.lineAttrs().getConstraint());
  }

  @Test
  public void invalidConstraintUsesDefaultBehavior() {
    Graphviz graph = DotParser.parse("digraph { a -> b [constraint=true1] }");
    Line line = graph.lines().iterator().next();
    Assertions.assertNull(line.lineAttrs().getConstraint());
  }

  @Test
  public void edgeDefaultCanBeOverridden() {
    Graphviz graph = DotParser.parse(
        "digraph { edge [constraint=false]; a -> b; a -> c [constraint=true] }");
    int falseCount = 0;
    int trueCount = 0;
    for (Line line : graph.lines()) {
      if (Boolean.FALSE.equals(line.lineAttrs().getConstraint())) {
        falseCount++;
      }
      if (Boolean.TRUE.equals(line.lineAttrs().getConstraint())) {
        trueCount++;
      }
    }
    Assertions.assertEquals(1, falseCount);
    Assertions.assertEquals(1, trueCount);
  }
}
