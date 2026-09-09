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

import java.util.Locale;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpandedShapeParsingTest {

  @Test
  void everyNodeNameResolvesIncludingMarkedShapes() throws Exception {
    for (NodeShapeEnum shape : NodeShapeEnum.values()) {
      Graphviz graph = DotParser.parse("digraph { a [shape=\""
          + shape.getName().toUpperCase(Locale.ROOT) + "\"]; }");
      Node node = graph.nodes().iterator().next();
      Assertions.assertEquals(shape, node.nodeAttrs().getShape(), shape.getName());
      Assertions.assertTrue(graph.toSvg().bytes().length > 0);
    }
  }

  @Test
  void everyArrowNameResolvesAtBothEnds() throws Exception {
    for (ArrowShape shape : ArrowShape.values()) {
      Graphviz graph = DotParser.parse("digraph { edge [dir=both arrowhead=\""
          + shape.name().toLowerCase(Locale.ROOT) + "\" arrowtail=" + shape.name() + "]; a->b }");
      Line line = graph.lines().iterator().next();
      Assertions.assertEquals(shape, line.lineAttrs().getArrowHead());
      Assertions.assertEquals(shape, line.lineAttrs().getArrowTail());
      Assertions.assertTrue(graph.toSvg().bytes().length > 0);
    }
  }

  @Test
  void legacyArrowAliasesAndExplicitOverrides() {
    String[] aliases = {"empty", "invempty", "open"};
    ArrowShape[] expected = {ArrowShape.ONORMAL, ArrowShape.OINV, ArrowShape.VEE};
    for (int i = 0; i < aliases.length; i++) {
      Graphviz graph = DotParser.parse("digraph { edge [arrowhead=diamond arrowtail=odot];"
          + "a->b [arrowhead=" + aliases[i] + "]; }");
      Line line = graph.lines().iterator().next();
      Assertions.assertEquals(expected[i], line.lineAttrs().getArrowHead());
      Assertions.assertEquals(ArrowShape.ODOT, line.lineAttrs().getArrowTail());
    }
  }

  @Test
  void polygonSupportsThreeToTwentySides() throws Exception {
    for (int sides = 3; sides <= 20; sides++) {
      Graphviz graph = DotParser.parse("digraph { a [shape=polygon sides=" + sides + "]; }");
      Node node = graph.nodes().iterator().next();
      Assertions.assertEquals(sides, node.nodeAttrs().getSides().intValue());
      Assertions.assertEquals(NodeShapeEnum.POLYGON, node.nodeAttrs().getShape());
      Assertions.assertTrue(graph.toSvg().bytes().length > 0);
    }
    Assertions.assertThrows(IllegalArgumentException.class, () -> Node.builder().sides(2));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Node.builder().sides(21));
  }
}
