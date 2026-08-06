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

import static org.graphper.api.Html.bold;
import static org.graphper.api.Html.cell;
import static org.graphper.api.Html.font;
import static org.graphper.api.Html.fontAttrs;
import static org.graphper.api.Html.italic;
import static org.graphper.api.Html.record;
import static org.graphper.api.Html.underline;
import static org.graphper.api.Html.vertical;
import static org.graphper.api.Html.verticalRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.graphper.api.Graphviz;
import org.graphper.api.Html.RecordTag;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rankdir;
import org.graphper.draw.ExecuteException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Renders record nodes and checks the resulting svg for the failure modes that a geometry bug
 * actually produces: text drawn outside the node, text drifting out of its own cell, and cells that
 * overlap each other.
 *
 * <p>These are invariants rather than golden values, so they hold for both the string front-end and
 * the structured API and stay meaningful when the corpus grows.
 */
public class RecordRenderGeometryTest {

  /** Half a pixel of slack, to absorb the rounding in the svg attribute strings. */
  private static final double EPS = 0.5;

  @Test
  public void plainRecordTextStaysInsideNode() throws ExecuteException {
    assertTextInsideNode(Node.builder()
                             .shape(NodeShapeEnum.RECORD)
                             .label("111|222|{<P0>333|444}")
                             .build());
  }

  @Test
  public void deepNestedRecordTextStaysInsideNode() throws ExecuteException {
    assertTextInsideNode(Node.builder()
                             .shape(NodeShapeEnum.RECORD)
                             .label("{{Name|Sex|Age}|{Michael|Men|15}|{Abigail|Female|18}}")
                             .build());
  }

  @Test
  public void apiBuiltRecordMatchesStringEquivalent() throws ExecuteException {
    String svgFromString = svg(Node.builder()
                                   .shape(NodeShapeEnum.RECORD)
                                   .label("a|{b|c}|d")
                                   .build());
    RecordTag tag = record(cell("a"), vertical(cell("b"), cell("c")), cell("d"));
    String svgFromApi = svg(Node.builder()
                                .shape(NodeShapeEnum.RECORD)
                                .recordTag(tag)
                                .build());

    Assertions.assertEquals(texts(svgFromString).toString(), texts(svgFromApi).toString(),
                            "The structured API must place text exactly like the equivalent string "
                                + "label, since both go through RecordTagCompiler");
  }

  @Test
  public void richTextRecordStaysInsideNode() throws ExecuteException {
    RecordTag tag = record(
        cell(italic("alpha")),
        cell(underline("beta")),
        cell(font("gamma", fontAttrs().color(Color.RED)))
    );
    assertTextInsideNode(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build());
  }

  @Test
  public void richTextRecordCellsDoNotOverlap() throws ExecuteException {
    RecordTag tag = record(
        cell(bold("wide bold text")),
        vertical(cell(italic("i")), cell(underline("u"))),
        cell("plain")
    );
    String svg = svg(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build());

    List<Text> texts = texts(svg);
    Assertions.assertFalse(texts.isEmpty(), "Rich record produced no text at all");
    for (int i = 0; i < texts.size(); i++) {
      for (int j = i + 1; j < texts.size(); j++) {
        Text a = texts.get(i);
        Text b = texts.get(j);
        boolean sameSpot = Math.abs(a.x - b.x) < EPS && Math.abs(a.y - b.y) < EPS;
        Assertions.assertFalse(sameSpot,
                               "Two runs were drawn on top of each other: '" + a.content + "' and '"
                                   + b.content + "' both at (" + a.x + ", " + a.y + ")");
      }
    }
  }

  @Test
  public void richTextRunsKeepDeclarationOrderLeftToRight() throws ExecuteException {
    RecordTag tag = record(cell(italic("one").bold("two").underline("three")));
    String svg = svg(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build());

    List<Text> texts = texts(svg);
    Text one = find(texts, "one");
    Text two = find(texts, "two");
    Text three = find(texts, "three");

    Assertions.assertTrue(one.x < two.x,
                          "'one' (" + one.x + ") should be left of 'two' (" + two.x + ")");
    Assertions.assertTrue(two.x < three.x,
                          "'two' (" + two.x + ") should be left of 'three' (" + three.x + ")");
    Assertions.assertEquals(one.y, two.y, EPS, "runs on one line share a baseline");
    Assertions.assertEquals(two.y, three.y, EPS, "runs on one line share a baseline");
  }

  /**
   * A single unstyled run must land on exactly the baseline the plain path would use. This caught the
   * rich path sitting about a pixel high, from treating the font size as the glyph height when the
   * measured height of a line is slightly larger.
   */
  @Test
  public void richTextBaselineMatchesPlainTextBaseline() throws ExecuteException {
    String plain = svg(Node.builder()
                           .shape(NodeShapeEnum.RECORD)
                           .label("alpha|beta")
                           .build());
    RecordTag tag = record(cell(org.graphper.api.Html.labelTag().text("alpha")),
                           cell(org.graphper.api.Html.labelTag().text("beta")));
    String rich = svg(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build());

    List<Text> plainTexts = texts(plain);
    List<Text> richTexts = texts(rich);
    Assertions.assertEquals(plainTexts.size(), richTexts.size());
    for (int i = 0; i < plainTexts.size(); i++) {
      Assertions.assertEquals(plainTexts.get(i).x, richTexts.get(i).x, EPS,
                              "rich run drifted horizontally from the plain baseline");
      Assertions.assertEquals(plainTexts.get(i).y, richTexts.get(i).y, EPS,
                              "rich run drifted vertically from the plain baseline");
    }
  }

  @Test
  public void richTextStylesReachTheSvg() throws ExecuteException {
    RecordTag tag = record(
        cell(bold("B")),
        cell(italic("I")),
        cell(underline("U")),
        cell(font("C", fontAttrs().color(Color.RED)))
    );
    String svg = svg(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build());

    Assertions.assertTrue(svg.contains("font-weight=\"bold\""), "bold run lost its weight");
    Assertions.assertTrue(svg.contains("font-style=\"italic\""), "italic run lost its style");
    Assertions.assertTrue(svg.contains("text-decoration=\"underline\""),
                          "underlined run lost its decoration");
    Assertions.assertTrue(svg.toLowerCase().contains("fill=\"#ff0000\""),
                          "coloured run lost its colour, svg was:\n" + svg);
  }

  /**
   * Node level {@code fontStyle} used to be dropped for record shapes: only NodeLabelEditor applied
   * it, and that editor bails out as soon as a cell tree exists.
   */
  @Test
  public void nodeLevelFontStyleAppliesToRecordCells() throws ExecuteException {
    String svg = svg(Node.builder()
                         .shape(NodeShapeEnum.RECORD)
                         .fontStyle(org.graphper.api.attributes.FontStyle.BOLD)
                         .label("a|b")
                         .build());
    Assertions.assertTrue(svg.contains("font-weight=\"bold\""),
                          "record cells ignored the node's fontStyle, svg was:\n" + svg);
  }

  @Test
  public void verticalRecordStacksTopToBottom() throws ExecuteException {
    RecordTag tag = verticalRecord(cell("top"), cell("middle"), cell("bottom"));
    String svg = svg(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build());

    List<Text> texts = texts(svg);
    Text top = find(texts, "top");
    Text middle = find(texts, "middle");
    Text bottom = find(texts, "bottom");

    Assertions.assertTrue(top.y < middle.y, "vertical record must stack downwards");
    Assertions.assertTrue(middle.y < bottom.y, "vertical record must stack downwards");
    Assertions.assertEquals(top.x, middle.x, EPS, "stacked cells share a horizontal centre");
  }

  @Test
  public void rankdirLrStillKeepsTextInsideNode() throws ExecuteException {
    RecordTag tag = record(cell("a"), vertical(cell("b"), cell(italic("c"))));
    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.LR)
        .addNode(Node.builder().shape(NodeShapeEnum.RECORD).recordTag(tag).build())
        .build();
    assertTextInsideNode(graphviz.toSvgStr());
  }

  // ------------------------------- helpers -------------------------------

  private void assertTextInsideNode(Node node) throws ExecuteException {
    assertTextInsideNode(svg(node));
  }

  /**
   * Every {@code <text>} anchor must sit inside the record's outline. The outline is a polygon (or a
   * rounded path for Mrecord), so the bounding box of all drawn coordinates is used as the frame.
   */
  private void assertTextInsideNode(String svg) {
    Box border = polygonBox(svg);
    Assertions.assertNotNull(border, "no record border polygon in svg:\n" + svg);

    List<Text> texts = texts(svg);
    Assertions.assertFalse(texts.isEmpty(), "record produced no text at all:\n" + svg);

    for (Text text : texts) {
      Assertions.assertTrue(text.x >= border.left - EPS && text.x <= border.right + EPS,
                            "text '" + text.content + "' at x=" + text.x
                                + " overflows the node horizontally [" + border.left + ", "
                                + border.right + "]");
      Assertions.assertTrue(text.y >= border.top - EPS && text.y <= border.bottom + EPS,
                            "text '" + text.content + "' at y=" + text.y
                                + " overflows the node vertically [" + border.top + ", "
                                + border.bottom + "]");
    }
  }

  private String svg(Node node) throws ExecuteException {
    return Graphviz.digraph().addNode(node).build().toSvgStr();
  }

  private Text find(List<Text> texts, String content) {
    for (Text text : texts) {
      if (content.equals(text.content)) {
        return text;
      }
    }
    throw new AssertionError("No text '" + content + "' in " + texts);
  }

  private static final Pattern TEXT_PATTERN = Pattern.compile(
      "<text[^>]*\\sx=\"([-0-9.eE]+)\"[^>]*\\sy=\"([-0-9.eE]+)\"[^>]*>([^<]*)</text>");

  private static final Pattern POINTS_PATTERN = Pattern.compile("points=\"([^\"]+)\"");

  private List<Text> texts(String svg) {
    List<Text> texts = new ArrayList<>();
    Matcher matcher = TEXT_PATTERN.matcher(svg);
    while (matcher.find()) {
      texts.add(new Text(Double.parseDouble(matcher.group(1)),
                         Double.parseDouble(matcher.group(2)),
                         matcher.group(3)));
    }
    return texts;
  }

  private Box polygonBox(String svg) {
    Matcher matcher = POINTS_PATTERN.matcher(svg);
    Box box = null;
    while (matcher.find()) {
      for (String pair : matcher.group(1).trim().split("\\s+")) {
        String[] xy = pair.split(",");
        if (xy.length != 2) {
          continue;
        }
        double x = Double.parseDouble(xy[0]);
        double y = Double.parseDouble(xy[1]);
        if (box == null) {
          box = new Box(x, x, y, y);
        } else {
          box.left = Math.min(box.left, x);
          box.right = Math.max(box.right, x);
          box.top = Math.min(box.top, y);
          box.bottom = Math.max(box.bottom, y);
        }
      }
    }
    return box;
  }

  private static final class Text {

    private final double x;
    private final double y;
    private final String content;

    private Text(double x, double y, String content) {
      this.x = x;
      this.y = y;
      this.content = content;
    }

    @Override
    public String toString() {
      return "'" + content + "'@(" + x + "," + y + ")";
    }
  }

  private static final class Box {

    private double left;
    private double right;
    private double top;
    private double bottom;

    private Box(double left, double right, double top, double bottom) {
      this.left = left;
      this.right = right;
      this.top = top;
      this.bottom = bottom;
    }
  }
}
