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

import org.graphper.api.Html.BasicRecordCell;
import org.graphper.api.Html.RecordTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Proves that {@link RecordTag} can express everything the frozen record grammar accepts.
 *
 * <p>Rather than hand-writing an {@code Html.record(...)} equivalent for each corpus string — which
 * is laborious and, when it disagrees with the parser, ambiguous about which side is wrong — this
 * test serialises the parsed {@code RecordTag} back to the string grammar and parses it again. If a
 * grammar feature had no home in the AST it would be lost on the way out and the second parse would
 * disagree.
 *
 * <p>This is the guard that makes {@link CellLabelCompiler} safe to keep as a pure parser: as long
 * as the round trip holds, the string front-end and the structured API describe the same space.
 */
public class RecordTagRoundTripTest {

  @Test
  public void structureSurvivesRoundTrip() {
    for (String label : RecordLabelCorpus.VALID) {
      RecordTag first = CellLabelCompiler.parse(label);
      String rendered = toStringForm(first);
      RecordTag second = CellLabelCompiler.parse(rendered);

      Assertions.assertEquals(describe(first), describe(second),
                              "Round trip lost structure for label [" + label
                                  + "] rendered as [" + rendered + "]");
    }
  }

  /**
   * The compiled geometry must also survive, which additionally pins the orientation of every
   * nesting level (structure alone would not catch a flipped level).
   */
  @Test
  public void geometrySurvivesRoundTrip() {
    for (String label : RecordLabelCorpus.VALID) {
      RecordTag first = CellLabelCompiler.parse(label);
      RecordTag second = CellLabelCompiler.parse(toStringForm(first));

      Assertions.assertEquals(geometry(RecordTagCompiler.compile(first)),
                              geometry(RecordTagCompiler.compile(second)),
                              "Round trip changed geometry for label [" + label + "]");
    }
  }

  @Test
  public void portIdsSurviveRoundTrip() {
    RecordTag tag = CellLabelCompiler.parse("{<p1>1|{{<p2>2|<p3>3}|<p4>4}|<p5>5|}");
    RecordTag again = CellLabelCompiler.parse(toStringForm(tag));

    for (String port : new String[]{"p1", "p2", "p3", "p4", "p5"}) {
      Assertions.assertNotNull(RecordTagCompiler.compile(again).getCellById(port),
                               "Port " + port + " lost on round trip");
    }
  }

  // ------------------------------- helpers -------------------------------

  /**
   * Serialises a record tag back into the frozen string grammar. Test-only: the production code has
   * no reason to go this direction.
   */
  private String toStringForm(RecordTag tag) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tag.getCells().size(); i++) {
      if (i > 0) {
        sb.append('|');
      }
      BasicRecordCell cell = tag.getCells().get(i);
      if (cell.getId() != null) {
        sb.append('<').append(escape(cell.getId())).append('>');
      }
      if (cell.getNested() != null) {
        sb.append('{').append(toStringForm(cell.getNested())).append('}');
      } else if (cell.getText() != null) {
        sb.append(escape(cell.getText()));
      }
    }
    return sb.toString();
  }

  private String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      if (c == '{' || c == '}' || c == '|' || c == '<' || c == '>' || c == '\\') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }

  private String describe(RecordTag tag) {
    StringBuilder sb = new StringBuilder();
    describe(tag, sb, 0);
    return sb.toString();
  }

  private void describe(RecordTag tag, StringBuilder sb, int depth) {
    sb.append(indent(depth)).append("tag hor=").append(tag.isHorizontal()).append('\n');
    for (BasicRecordCell cell : tag.getCells()) {
      /*
       * A null body and an empty-string body both mean "blank cell": the grammar reaches the former
       * by collapsing whitespace ("  " -> null) and the latter from a literally empty label ("").
       * Both measure as DEFAULT_SIZE and render as nothing, so the distinction is an artifact of the
       * tokenizer rather than a structural feature, and the round trip is not expected to preserve
       * it.
       */
      String text = cell.getText() == null || cell.getText().isEmpty()
          ? "<blank>" : "[" + cell.getText() + "]";
      sb.append(indent(depth + 1)).append("cell id=").append(cell.getId())
          .append(" text=").append(text).append('\n');
      if (cell.getNested() != null) {
        describe(cell.getNested(), sb, depth + 2);
      }
    }
  }

  private String geometry(Cell cell) {
    StringBuilder sb = new StringBuilder();
    geometry(cell, sb, "0");
    return sb.toString();
  }

  private void geometry(Cell cell, StringBuilder sb, String path) {
    sb.append(path).append(' ').append(cell.getId()).append(' ').append(cell.isHor())
        .append(' ').append(cell.getWidth()).append(' ').append(cell.getHeight())
        .append(' ').append(cell.getOffset()).append('\n');
    for (int i = 0; i < cell.childrenSize(); i++) {
      geometry(cell.getChild(i), sb, path + '.' + i);
    }
  }

  private String indent(int depth) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append("  ");
    }
    return sb.toString();
  }
}
