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
import org.graphper.api.Node;
import org.graphper.api.attributes.NodeShapeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@code ParserUtils#setNodeShape} resolves a shape by scanning {@code NodeShapeEnum.values()},
 * so the declaration order of the enum could in principle change which constant a script gets.
 * These cases pin the resolution of the alias pairs that sit next to each other.
 */
class NodeShapeAliasResolutionTest {

  @Test
  void aliasNamesResolveToTheirOwnConstant() {
    assertShape("oval", NodeShapeEnum.OVAL);
    assertShape("ellipse", NodeShapeEnum.ELLIPSE);
    assertShape("rectangle", NodeShapeEnum.RECTANGLE);
    assertShape("rect", NodeShapeEnum.RECT);
    assertShape("box", NodeShapeEnum.BOX);
    assertShape("none", NodeShapeEnum.NONE);
    assertShape("circle", NodeShapeEnum.CIRCLE);
    assertShape("doublecircle", NodeShapeEnum.DOUBLECIRCLE);
    assertShape("Mcircle", NodeShapeEnum.M_CIRCLE);
    assertShape("square", NodeShapeEnum.SQUARE);
    assertShape("Msquare", NodeShapeEnum.M_SQUARE);
    assertShape("octagon", NodeShapeEnum.OCTAGON);
    assertShape("doubleoctagon", NodeShapeEnum.DOUBLEOCTAGON);
    assertShape("tripleoctagon", NodeShapeEnum.TRIPLEOCTAGON);
    assertShape("house", NodeShapeEnum.HOUSE);
    assertShape("invhouse", NodeShapeEnum.INVHOUSE);
    assertShape("triangle", NodeShapeEnum.TRIANGLE);
    assertShape("invtriangle", NodeShapeEnum.INVTRIANGLE);
    assertShape("diamond", NodeShapeEnum.DIAMOND);
    assertShape("Mdiamond", NodeShapeEnum.M_DIAMOND);
  }

  @Test
  void enumConstantNamesAlsoResolve() {
    assertShape("M_RECORD", NodeShapeEnum.M_RECORD);
    assertShape("m_record", NodeShapeEnum.M_RECORD);
    assertShape("Mrecord", NodeShapeEnum.M_RECORD);
    assertShape("REGULAR_POLYLINE", NodeShapeEnum.REGULAR_POLYLINE);
    assertShape("regular_polyline", NodeShapeEnum.REGULAR_POLYLINE);
  }

  private static void assertShape(String dotName, NodeShapeEnum expected) {
    Graphviz graph = DotParser.parse("digraph { a [shape=\"" + dotName + "\"]; }");
    Node node = graph.nodes().iterator().next();
    Assertions.assertEquals(expected, node.nodeAttrs().getShape(), dotName);
  }
}
