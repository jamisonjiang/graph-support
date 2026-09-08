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

package org.graphper.api.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pins the declaration order of {@link NodeShapeEnum} the same way {@code LineArrowEditorTest}
 * pins {@link ArrowShape}: the historical constants keep their original ordinals and every new
 * shape is appended after them.
 */
class NodeShapeEnumOrdinalTest {

  /**
   * The constants that existed before the expanded-shape work, in their original declaration
   * order. Ordinal {@code i} of this array is the ordinal the constant must still report.
   */
  private static final NodeShapeEnum[] HISTORICAL = {
      NodeShapeEnum.NOTE,
      NodeShapeEnum.PLAIN,
      NodeShapeEnum.PLAINTEXT,
      NodeShapeEnum.UNDERLINE,
      NodeShapeEnum.ELLIPSE,
      NodeShapeEnum.CIRCLE,
      NodeShapeEnum.BOX,
      NodeShapeEnum.RECT,
      NodeShapeEnum.POINT,
      NodeShapeEnum.TRIANGLE,
      NodeShapeEnum.INVTRIANGLE,
      NodeShapeEnum.DIAMOND,
      NodeShapeEnum.TRAPEZIUM,
      NodeShapeEnum.INVTRAPEZIUM,
      NodeShapeEnum.PARALLELOGRAM,
      NodeShapeEnum.STAR,
      NodeShapeEnum.CYLINDER,
      NodeShapeEnum.PENTAGON,
      NodeShapeEnum.HEXAGON,
      NodeShapeEnum.SEPTAGON,
      NodeShapeEnum.OCTAGON,
      NodeShapeEnum.REGULAR_POLYLINE,
      NodeShapeEnum.RECORD,
      NodeShapeEnum.M_RECORD
  };

  /** Shapes appended after {@link #HISTORICAL}, in their current declaration order. */
  private static final NodeShapeEnum[] APPENDED = {
      NodeShapeEnum.OVAL,
      NodeShapeEnum.NONE,
      NodeShapeEnum.RECTANGLE,
      NodeShapeEnum.SQUARE,
      NodeShapeEnum.POLYGON,
      NodeShapeEnum.HOUSE,
      NodeShapeEnum.INVHOUSE,
      NodeShapeEnum.DOUBLECIRCLE,
      NodeShapeEnum.DOUBLEOCTAGON,
      NodeShapeEnum.TRIPLEOCTAGON,
      NodeShapeEnum.M_DIAMOND,
      NodeShapeEnum.M_SQUARE,
      NodeShapeEnum.M_CIRCLE,
      NodeShapeEnum.TAB,
      NodeShapeEnum.FOLDER,
      NodeShapeEnum.BOX3D,
      NodeShapeEnum.COMPONENT
  };

  @Test
  void historicalConstantsKeepTheirOriginalOrdinals() {
    for (int i = 0; i < HISTORICAL.length; i++) {
      assertEquals(i, HISTORICAL[i].ordinal(), HISTORICAL[i].name());
    }
  }

  @Test
  void newConstantsAreAppendedAfterTheHistoricalBlock() {
    for (int i = 0; i < APPENDED.length; i++) {
      assertEquals(HISTORICAL.length + i, APPENDED[i].ordinal(), APPENDED[i].name());
    }
    assertEquals(HISTORICAL.length + APPENDED.length, NodeShapeEnum.values().length,
                 "a new shape must be appended and listed in NodeShapeEnumOrdinalTest#APPENDED");
  }

  @Test
  void valuesIterationMatchesTheHistoricalBlockThenTheAppendedBlock() {
    NodeShapeEnum[] values = NodeShapeEnum.values();
    for (int i = 0; i < HISTORICAL.length; i++) {
      assertSame(HISTORICAL[i], values[i]);
    }
    for (int i = 0; i < APPENDED.length; i++) {
      assertSame(APPENDED[i], values[HISTORICAL.length + i]);
    }
  }

  /**
   * The DOT parser resolves a shape by scanning {@code values()} and taking the first constant
   * whose {@code getName()} or {@code name()} matches case-insensitively. That scan is only
   * order-independent while no two constants answer to the same token, so the reordering above
   * cannot change which constant a script resolves to.
   */
  @Test
  void everyLookupTokenIsOwnedByExactlyOneConstant() {
    Map<String, NodeShapeEnum> owners = new HashMap<>();
    for (NodeShapeEnum shape : NodeShapeEnum.values()) {
      for (String token : new String[]{shape.getName(), shape.name()}) {
        NodeShapeEnum previous = owners.put(token.toLowerCase(Locale.ROOT), shape);
        assertTrue(previous == null || previous == shape,
                   "token '" + token + "' is claimed by both " + previous + " and " + shape);
      }
    }
  }

  @Test
  void aliasesStayDistinctFromTheShapesTheyAlias() {
    assertEquals("oval", NodeShapeEnum.OVAL.getName());
    assertEquals("ellipse", NodeShapeEnum.ELLIPSE.getName());
    assertEquals("rectangle", NodeShapeEnum.RECTANGLE.getName());
    assertEquals("rect", NodeShapeEnum.RECT.getName());
    assertEquals("box", NodeShapeEnum.BOX.getName());
    assertSame(NodeShapeEnum.OVAL, valueOfName("oval"));
    assertSame(NodeShapeEnum.ELLIPSE, valueOfName("ELLIPSE"));
    assertSame(NodeShapeEnum.RECTANGLE, valueOfName("Rectangle"));
    assertSame(NodeShapeEnum.RECT, valueOfName("rect"));
    assertSame(NodeShapeEnum.BOX, valueOfName("box"));
    assertSame(NodeShapeEnum.M_RECORD, valueOfName("Mrecord"));
    assertSame(NodeShapeEnum.M_RECORD, valueOfName("m_record"));
  }

  /** Mirrors {@code ParserUtils#setNodeShape}, which is the production lookup. */
  private static NodeShapeEnum valueOfName(String name) {
    for (NodeShapeEnum shape : NodeShapeEnum.values()) {
      if (shape.getName().equalsIgnoreCase(name) || shape.name().equalsIgnoreCase(name)) {
        return shape;
      }
    }
    return null;
  }
}
