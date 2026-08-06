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

package org.graphper.api;

import static org.graphper.api.Html.bold;
import static org.graphper.api.Html.cell;
import static org.graphper.api.Html.horizontal;
import static org.graphper.api.Html.italic;
import static org.graphper.api.Html.record;
import static org.graphper.api.Html.underline;
import static org.graphper.api.Html.vertical;
import static org.graphper.api.Html.verticalRecord;

import java.util.List;
import org.graphper.api.Html.BasicRecordCell;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.RecordTag;
import org.graphper.api.attributes.NodeShapeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the structured {@link RecordTag} / {@link BasicRecordCell}
 * API on {@link Html}.
 *
 * <p>Phase 1 scope: the API is wired into {@link NodeAttrs} / {@link LineAttrs}
 * / {@link ClusterAttrs} / {@link GraphAttrs} / {@link FloatLabel}, but it is
 * not yet consumed by the layout engine. These tests therefore focus on AST
 * construction, invariants and slot plumbing only.
 */
public class RecordTagApiTest {

  // -------------------------------------------------------------------------
  // Construction
  // -------------------------------------------------------------------------

  @Test
  public void testHorizontalRecordRoot() {
    RecordTag tag = record(cell("a"), cell("b"), cell("c"));
    Assertions.assertTrue(tag.isHorizontal());
    Assertions.assertEquals(3, tag.getCells().size());
    Assertions.assertEquals("a", tag.getCells().get(0).getText());
    Assertions.assertEquals("b", tag.getCells().get(1).getText());
    Assertions.assertEquals("c", tag.getCells().get(2).getText());
  }

  @Test
  public void testVerticalRecordRoot() {
    RecordTag tag = verticalRecord(cell("a"), cell("b"));
    Assertions.assertFalse(tag.isHorizontal());
    Assertions.assertEquals(2, tag.getCells().size());
  }

  @Test
  public void testEmptyRecord() {
    RecordTag tag = record();
    Assertions.assertTrue(tag.isHorizontal());
    Assertions.assertTrue(tag.getCells().isEmpty());
  }

  @Test
  public void testRecordAllowsNullVarargs() {
    // varargs collapsed to null should not NPE, and should behave like an
    // empty record.
    RecordTag tag = record((BasicRecordCell[]) null);
    Assertions.assertTrue(tag.getCells().isEmpty());
  }

  @Test
  public void testFluentAppend() {
    RecordTag tag = record()
        .cell("a")
        .cell(italic("b"))
        .nested(verticalRecord(cell("c"), cell("d")));
    Assertions.assertEquals(3, tag.getCells().size());
    Assertions.assertEquals("a", tag.getCells().get(0).getText());
    Assertions.assertNotNull(tag.getCells().get(1).getTextTag());
    Assertions.assertNotNull(tag.getCells().get(2).getNested());
    Assertions.assertFalse(tag.getCells().get(2).getNested().isHorizontal());
  }

  // -------------------------------------------------------------------------
  // BasicRecordCell: mutual exclusion of body fields
  // -------------------------------------------------------------------------

  @Test
  public void testPlainTextCell() {
    BasicRecordCell c = cell("a");
    Assertions.assertEquals("a", c.getText());
    Assertions.assertNull(c.getTextTag());
    Assertions.assertNull(c.getNested());
    Assertions.assertNull(c.getId());
    Assertions.assertFalse(c.isBlank());
  }

  @Test
  public void testRichTextCell() {
    LabelTag rich = bold("bold");
    BasicRecordCell c = cell(rich);
    Assertions.assertNull(c.getText());
    Assertions.assertSame(rich, c.getTextTag());
    Assertions.assertNull(c.getNested());
  }

  @Test
  public void testNestedCell() {
    RecordTag inner = verticalRecord(cell("a"), cell("b"));
    BasicRecordCell c = cell(inner);
    Assertions.assertNull(c.getText());
    Assertions.assertNull(c.getTextTag());
    Assertions.assertSame(inner, c.getNested());
  }

  @Test
  public void testBlankCell() {
    BasicRecordCell c = cell();
    Assertions.assertTrue(c.isBlank());
    Assertions.assertNull(c.getText());
    Assertions.assertNull(c.getTextTag());
    Assertions.assertNull(c.getNested());
  }

  @Test
  public void testSettingOneBodyClearsOthers() {
    BasicRecordCell c = cell("a");
    c.text(italic("b"));
    Assertions.assertNull(c.getText());
    Assertions.assertNotNull(c.getTextTag());

    c.nested(record(cell("x")));
    Assertions.assertNull(c.getTextTag());
    Assertions.assertNotNull(c.getNested());

    c.text("back to plain");
    Assertions.assertEquals("back to plain", c.getText());
    Assertions.assertNull(c.getTextTag());
    Assertions.assertNull(c.getNested());
  }

  @Test
  public void testPortIdOnCell() {
    BasicRecordCell c = cell("a").id("f0");
    Assertions.assertEquals("f0", c.getId());
  }

  @Test
  public void testNullTextTagRejected() {
    // Asserts.nullArgument uses Objects.requireNonNull → NullPointerException
    Assertions.assertThrows(NullPointerException.class,
                            () -> cell().text((LabelTag) null));
  }

  @Test
  public void testNullNestedRejected() {
    Assertions.assertThrows(NullPointerException.class,
                            () -> cell().nested(null));
  }

  @Test
  public void testNullCellAppendRejected() {
    RecordTag tag = record();
    Assertions.assertThrows(NullPointerException.class,
                            () -> tag.cell((BasicRecordCell) null));
  }

  // -------------------------------------------------------------------------
  // Sugar factories
  // -------------------------------------------------------------------------

  @Test
  public void testVerticalSugar() {
    BasicRecordCell nested = vertical(cell("a"), cell("b"));
    Assertions.assertNotNull(nested.getNested());
    Assertions.assertFalse(nested.getNested().isHorizontal());
    Assertions.assertEquals(2, nested.getNested().getCells().size());
  }

  @Test
  public void testHorizontalSugar() {
    BasicRecordCell nested = horizontal(cell("a"), cell("b"));
    Assertions.assertNotNull(nested.getNested());
    Assertions.assertTrue(nested.getNested().isHorizontal());
  }

  // -------------------------------------------------------------------------
  // Slot plumbing on attrs classes
  // -------------------------------------------------------------------------

  @Test
  public void testNodeBuilderRecordTagSlot() {
    RecordTag tag = record(cell("a"), cell("b"));
    Node n = Node.builder()
        .shape(NodeShapeEnum.RECORD)
        .recordTag(tag)
        .build();
    Assertions.assertSame(tag, n.nodeAttrs().getRecordTag());
    // plain-label slot is independent
    Assertions.assertNull(n.nodeAttrs().getRealLabel());
  }

  @Test
  public void testNodeBuilderRejectsNullRecordTag() {
    Assertions.assertThrows(NullPointerException.class,
                            () -> Node.builder().recordTag(null));
  }

  @Test
  public void testNodeAttrsEqualityIncludesRecordTag() {
    RecordTag tag = record(cell("a"));
    Node n1 = Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build();
    Node n2 = Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build();
    Node n3 = Node.builder().shape(NodeShapeEnum.RECORD).build();
    Assertions.assertEquals(n1.nodeAttrs(), n2.nodeAttrs());
    Assertions.assertNotEquals(n1.nodeAttrs(), n3.nodeAttrs());
  }

  /**
   * A {@code RecordTag} is only meaningful on a node: record compilation happens exclusively in
   * {@code AbstractLayoutEngine.nodeContainerSet}, so there is deliberately no {@code recordTag}
   * slot on lines, clusters, graphs or float labels. Adding one would be public API that nothing
   * ever reads.
   */
  @Test
  public void testRecordTagIsNodeOnly() throws NoSuchMethodException {
    Assertions.assertNotNull(Node.NodeBuilder.class.getMethod("recordTag", RecordTag.class));

    for (Class<?> builder : new Class<?>[]{Line.LineBuilder.class, Cluster.ClusterBuilder.class,
        Graphviz.GraphvizBuilder.class, FloatLabel.FloatLabelBuilder.class}) {
      Assertions.assertThrows(NoSuchMethodException.class,
                              () -> builder.getMethod("recordTag", RecordTag.class),
                              builder.getSimpleName()
                                  + " must not expose an unconsumed recordTag slot");
    }
  }

  // -------------------------------------------------------------------------
  // Nested structure sanity
  // -------------------------------------------------------------------------

  @Test
  public void testDeepNestedStructure() {
    // Mirrors the legacy string label "{<f0>a|{b|c}|<f1>d}".
    RecordTag tag = record(
        cell("a").id("f0"),
        vertical(cell("b"), cell("c")),
        cell("d").id("f1")
    );

    List<BasicRecordCell> cells = tag.getCells();
    Assertions.assertEquals(3, cells.size());

    Assertions.assertEquals("f0", cells.get(0).getId());
    Assertions.assertEquals("a", cells.get(0).getText());

    RecordTag middle = cells.get(1).getNested();
    Assertions.assertNotNull(middle);
    Assertions.assertFalse(middle.isHorizontal());
    Assertions.assertEquals(2, middle.getCells().size());
    Assertions.assertEquals("b", middle.getCells().get(0).getText());
    Assertions.assertEquals("c", middle.getCells().get(1).getText());

    Assertions.assertEquals("f1", cells.get(2).getId());
    Assertions.assertEquals("d", cells.get(2).getText());
  }

  @Test
  public void testRichTextMixedWithPlainText() {
    // Something that has no string-label equivalent today.
    RecordTag tag = record(
        cell(italic("alpha")),
        cell(bold(underline("beta"))),
        cell("plain")
    );
    Assertions.assertNotNull(tag.getCells().get(0).getTextTag());
    Assertions.assertNotNull(tag.getCells().get(1).getTextTag());
    Assertions.assertEquals("plain", tag.getCells().get(2).getText());
  }
}
