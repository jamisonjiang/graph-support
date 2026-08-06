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

import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Html.BasicLabelTag;
import org.graphper.api.Html.BasicRecordCell;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.LabelTagType;
import org.graphper.api.Html.RecordTag;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShapeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Covers the recovery of record structure from an angle-bracket label, i.e. the case Graphviz renders
 * as a record with formatted cells but graph-support previously showed as one run of text with visible
 * braces and bars.
 */
public class RecordTagFromLabelTagTest {

  @Test
  public void richCellsBecomeRecordCells() {
    RecordTag tag = convert("<{<i>a</i>|<u>b</u>|<b>c</b>}>");

    // The outer {...} is one top-level field holding a vertical record, same as the string grammar
    Assertions.assertEquals(1, tag.getCells().size());
    RecordTag inner = tag.getCells().get(0).getNested();
    Assertions.assertNotNull(inner, "outer braces should produce a nested record");
    Assertions.assertFalse(inner.isHorizontal(), "nesting flips orientation");

    List<BasicRecordCell> cells = inner.getCells();
    Assertions.assertEquals(3, cells.size());
    for (BasicRecordCell cell : cells) {
      Assertions.assertNotNull(cell.getTextTag(), "each cell should carry rich text");
      Assertions.assertNull(cell.getText());
    }
  }

  @Test
  public void plainTextBetweenStyledRunsStaysInTheSameCell() {
    RecordTag tag = convert("<{pre <i>mid</i> post|second}>");
    RecordTag inner = tag.getCells().get(0).getNested();

    LabelTag first = inner.getCells().get(0).getTextTag();
    Assertions.assertNotNull(first);
    // "pre ", the italic run, and " post" are three fragments of one cell
    Assertions.assertEquals(3, first.getTags().size());

    Assertions.assertEquals("second", inner.getCells().get(1).getText());
  }

  @Test
  public void multilineIndentationDoesNotBecomeRichCellContent() {
    RecordTag tag = convert("<{\n"
                                + "        <b>API Gateway</b>|\n"
                                + "        {<i>Responsibilities</i>|Routing}\n"
                                + "      }>");

    RecordTag inner = tag.getCells().get(0).getNested();
    LabelTag gateway = inner.getCells().get(0).getTextTag();
    Assertions.assertNotNull(gateway);
    Assertions.assertEquals(1, gateway.getTags().size(),
                            "formatting indentation must not become a separate text run");
    BasicLabelTag bold = gateway.getTags().get(0);
    Assertions.assertEquals(LabelTagType.BOLD, bold.getType());
    Assertions.assertNotNull(bold.getSubLabelTag());
    Assertions.assertEquals("API Gateway", bold.getSubLabelTag().getTags().get(0).getText());
  }

  @Test
  public void nestedBracesFlipOrientationAtEachLevel() {
    RecordTag tag = convert("<<i>a</i>|{<i>b</i>|{<i>c</i>|<i>d</i>}}>");

    Assertions.assertTrue(tag.isHorizontal());
    Assertions.assertEquals(2, tag.getCells().size());

    RecordTag level1 = tag.getCells().get(1).getNested();
    Assertions.assertNotNull(level1);
    Assertions.assertFalse(level1.isHorizontal());

    RecordTag level2 = level1.getCells().get(1).getNested();
    Assertions.assertNotNull(level2);
    Assertions.assertTrue(level2.isHorizontal());
    Assertions.assertEquals(2, level2.getCells().size());
  }

  @Test
  public void splitAfterNestedRecordDoesNotCreatePhantomCell() {
    RecordTag tag = convert("<<i>a</i>|{<i>b</i>|<i>c</i>}|<i>d</i>>");

    Assertions.assertEquals(3, tag.getCells().size(),
                            "a|{b|c}|d must be exactly three fields");
    Assertions.assertNotNull(tag.getCells().get(0).getTextTag());
    Assertions.assertNotNull(tag.getCells().get(1).getNested());
    Assertions.assertNotNull(tag.getCells().get(2).getTextTag());
  }

  @Test
  public void escapedRecordCharactersStayLiteral() {
    // ANTLR keeps the backslash, so the record escapes still apply on this path
    RecordTag tag = convert("<a\\|b|<i>c</i>>");

    Assertions.assertEquals(2, tag.getCells().size());
    Assertions.assertEquals("a|b", tag.getCells().get(0).getText());
  }

  @Test
  public void trailingSplitLeavesBlankCell() {
    RecordTag tag = convert("<<i>a</i>|>");

    Assertions.assertEquals(2, tag.getCells().size());
    Assertions.assertTrue(tag.getCells().get(1).isBlank());
  }

  @Test
  public void richTextWithoutRecordCharactersIsNotARecord() {
    Assertions.assertNull(convert("<<i>just italic</i>>"),
                          "a plain rich-text label must keep flowing through the labelTag slot");
  }

  @Test
  public void unbalancedBracesStillProduceARecord() {
    Assertions.assertNotNull(convert("<{<i>a</i>|<i>b</i>>"), "unclosed brace should be tolerated");
    Assertions.assertNotNull(convert("<<i>a</i>|<i>b</i>}}>"), "stray closing braces are ignored");
  }

  // ------------------------- end to end through the parser -------------------------

  @Test
  public void dotSourceWithRecordShapeGetsRecordTag() {
    Graphviz graphviz = DotParser.parse(
        "digraph { n[shape=record, label=<{<i>a</i>|<u>b</u>}>]; }");

    NodeAttrs attrs = onlyNode(graphviz).nodeAttrs();
    Assertions.assertNotNull(attrs.getRecordTag(),
                            "record-shaped node with an angle-bracket label should get a recordTag");
  }

  /**
   * Attribute order is not constrained in DOT, so the shape may be written after the label. The whole
   * attribute map is inspected up front to make this work.
   */
  @Test
  public void shapeDeclaredAfterLabelStillTriggersConversion() {
    Graphviz graphviz = DotParser.parse(
        "digraph { n[label=<{<i>a</i>|<u>b</u>}>, shape=record]; }");

    Assertions.assertNotNull(onlyNode(graphviz).nodeAttrs().getRecordTag(),
                             "attribute order must not decide whether the record is recovered");
  }

  @Test
  public void nonRecordShapeKeepsBracesAsText() {
    Graphviz graphviz = DotParser.parse(
        "digraph { n[shape=ellipse, label=<{<i>a</i>|<u>b</u>}>]; }");

    NodeAttrs attrs = onlyNode(graphviz).nodeAttrs();
    Assertions.assertNull(attrs.getRecordTag(),
                          "only record shapes reinterpret braces and bars as structure");
    Assertions.assertNotNull(attrs.getLabelTag(), "it must remain an ordinary rich-text label");
  }

  @Test
  public void plainStringRecordLabelStillUsesTheFrozenStringPath() {
    Graphviz graphviz = DotParser.parse("digraph { n[shape=record, label=\"{a|b|c}\"]; }");

    NodeAttrs attrs = onlyNode(graphviz).nodeAttrs();
    Assertions.assertNull(attrs.getRecordTag(),
                          "a quoted record label must keep going through CellLabelCompiler");
    Assertions.assertEquals("{a|b|c}", attrs.getLabel());
  }

  /**
   * An angle-bracket label with no tags at all is reported by the html parser as a plain string, so
   * it also stays on the frozen string path.
   */
  @Test
  public void angleBracketLabelWithoutTagsStaysAString() {
    Graphviz graphviz = DotParser.parse("digraph { n[shape=record, label=<{a|b|c}>]; }");

    NodeAttrs attrs = onlyNode(graphviz).nodeAttrs();
    Assertions.assertNull(attrs.getRecordTag());
    Assertions.assertEquals("{a|b|c}", attrs.getLabel());
  }

  @Test
  public void mrecordShapeAlsoConverts() {
    Graphviz graphviz = DotParser.parse(
        "digraph { n[shape=Mrecord, label=<{<i>a</i>|<u>b</u>}>]; }");

    Assertions.assertNotNull(onlyNode(graphviz).nodeAttrs().getRecordTag());
  }

  @Test
  public void recoveredRecordActuallyRenders() throws Exception {
    Graphviz graphviz = DotParser.parse(
        "digraph { n[shape=record, label=<{<i>alpha</i>|<u>beta</u>}>]; }");

    String svg = graphviz.toSvgStr();
    Assertions.assertTrue(svg.contains(">alpha<"), "cell text missing from svg:\n" + svg);
    Assertions.assertTrue(svg.contains(">beta<"), "cell text missing from svg:\n" + svg);
    Assertions.assertTrue(svg.contains("font-style=\"italic\""), "italic cell lost its style");
    Assertions.assertTrue(svg.contains("text-decoration=\"underline\""),
                          "underlined cell lost its decoration");
    Assertions.assertFalse(svg.contains(">{<") || svg.contains(">|<"),
                            "record characters must not be drawn as text:\n" + svg);
  }

  @Test
  public void indentedRichRecordDoesNotRenderWhitespaceOnlyRuns() throws Exception {
    Graphviz graphviz = DotParser.parse("digraph { n[shape=record, label=<{\n"
                                               + "        <b>API Gateway</b>|\n"
                                               + "        <i>Requests</i>\n"
                                               + "      }>]; }");

    String svg = graphviz.toSvgStr();
    Assertions.assertFalse(svg.matches("(?s).*<text[^>]*>(?:&#160;)+</text>.*"),
                           "source indentation must not be rendered as visible cell content:\n"
                               + svg);
  }

  // ------------------------------- helpers -------------------------------

  private RecordTag convert(String label) {
    HtmlListener listener = HtmlParser.parse(label);
    Assertions.assertNotNull(listener, "html parser returned nothing for " + label);
    LabelTag labelTag = listener.getLabelTag();
    Assertions.assertNotNull(labelTag, "expected a rich-text label for " + label
        + ", got plain [" + listener.getLabel() + "]");
    return RecordTagFromLabelTag.convert(labelTag);
  }

  private Node onlyNode(Graphviz graphviz) {
    for (Node node : graphviz.nodes()) {
      if (node.nodeAttrs().getShape() == NodeShapeEnum.RECORD
          || node.nodeAttrs().getShape() == NodeShapeEnum.M_RECORD
          || node.nodeAttrs().getShape() == NodeShapeEnum.ELLIPSE) {
        return node;
      }
    }
    throw new AssertionError("no node found");
  }
}
