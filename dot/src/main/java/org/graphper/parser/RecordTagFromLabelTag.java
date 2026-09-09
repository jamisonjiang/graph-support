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

import java.util.ArrayList;
import java.util.List;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html;
import org.graphper.api.Html.BasicLabelTag;
import org.graphper.api.Html.BasicRecordCell;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.LabelTagType;
import org.graphper.api.Html.RecordTag;

/**
 * Recovers the record structure from an angle-bracket label on a record-shaped node, turning
 * {@code label=<{<i>a</i>|<u>b</u>}>} into a {@link RecordTag} whose cells carry rich text.
 *
 * <p>Graphviz treats {@code {}} and {@code |} as record structure even inside an HTML label.
 * The HTML lexer does not know the node's shape, so it hands them over as ordinary text. Rather
 * than teach the grammar about shapes, this class post-processes the parsed {@link LabelTag}: the
 * structural characters are consumed out of the text leaves and everything else is regrouped
 * per cell. The four grammar files stay untouched.
 *
 * <h2>Scope</h2>
 * Deliberately matched to what Graphviz itself accepts here — rich text, but no port ids. In an
 * angle-bracket label {@code <f0>} is lexed as an unknown HTML tag and fails parsing before
 * this class is reached, as it does in Graphviz. Ports combined with rich text are reachable
 * only through the Java API, {@code Html.cell(Html.italic("a")).id("f0")}.
 *
 * @author Jamison Jiang
 */
public class RecordTagFromLabelTag {

  private RecordTagFromLabelTag() {
  }

  /**
   * Converts a parsed rich-text label into a record structure.
   *
   * @param labelTag the label to convert, may be {@code null}
   * @return the record structure, or {@code null} when the label carries no record structure at all
   *     and should therefore stay an ordinary rich-text label
   */
  public static RecordTag convert(LabelTag labelTag) {
    if (labelTag == null || labelTag.getTags() == null) {
      return null;
    }

    List<Atom> atoms = new ArrayList<>();
    for (BasicLabelTag tag : labelTag.getTags()) {
      linearize(tag, atoms);
    }

    boolean structural = false;
    for (Atom atom : atoms) {
      if (atom.type != AtomType.TEXT && atom.type != AtomType.RICH) {
        structural = true;
        break;
      }
    }
    if (!structural) {
      // No '{', '|' or '}' anywhere: an ordinary rich-text label, not a record
      return null;
    }

    return build(atoms);
  }

  /**
   * Splits the flat tag list into structural atoms. Only top-level {@code TEXT} leaves are scanned:
   * a {@code {} } inside {@code <i>...</i>} belongs to the italic run's text, exactly as the string
   * grammar treats an escaped brace.
   */
  private static void linearize(BasicLabelTag tag, List<Atom> atoms) {
    if (tag.getType() != LabelTagType.TEXT || tag.getText() == null) {
      atoms.add(new Atom(AtomType.RICH, null, tag));
      return;
    }

    String text = tag.getText();
    StringBuilder run = new StringBuilder();
    boolean escaped = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (escaped) {
        // The backslash survives the ANTLR pass, so the record escapes are honoured here
        escaped = false;
        run.append(c);
        continue;
      }

      if (c == '\\') {
        escaped = true;
        continue;
      }

      AtomType structural = structuralType(c);
      if (structural == null) {
        run.append(c);
        continue;
      }

      flush(run, atoms);
      atoms.add(new Atom(structural, null, null));
    }

    if (escaped) {
      // Trailing lone backslash, keep it literal rather than failing the whole label
      run.append('\\');
    }
    flush(run, atoms);
  }

  private static AtomType structuralType(char c) {
    if (c == '{') {
      return AtomType.OPEN;
    }
    if (c == '}') {
      return AtomType.CLOSE;
    }
    if (c == '|') {
      return AtomType.SPLIT;
    }
    return null;
  }

  private static void flush(StringBuilder run, List<Atom> atoms) {
    if (run.length() > 0) {
      atoms.add(new Atom(AtomType.TEXT, run.toString(), null));
      run.delete(0, run.length());
    }
  }

  /**
   * Folds the atom stream into a record tree. As in the string grammar, the top level is horizontal
   * and every {@code {...}} flips the orientation.
   */
  private static RecordTag build(List<Atom> atoms) {
    Frame root = new Frame(true);
    Frame current = root;
    List<Frame> stack = new ArrayList<>();

    for (Atom atom : atoms) {
      switch (atom.type) {
        case OPEN:
          stack.add(current);
          current = new Frame(!current.horizontal);
          break;
        case CLOSE:
          if (stack.isEmpty()) {
            // Unbalanced closing brace, tolerated the same way the string grammar tolerates it
            break;
          }
          Frame parent = stack.remove(stack.size() - 1);
          parent.closeNested(current);
          current = parent;
          break;
        case SPLIT:
          current.endCell();
          break;
        case TEXT:
          current.addText(atom.text);
          break;
        case RICH:
          current.addRich(atom.tag);
          break;
        default:
          break;
      }
    }

    while (!stack.isEmpty()) {
      // Unclosed braces, close them implicitly so a malformed label still renders
      Frame parent = stack.remove(stack.size() - 1);
      parent.closeNested(current);
      current = parent;
    }

    return root.toRecordTag();
  }

  private enum AtomType {
    OPEN, CLOSE, SPLIT, TEXT, RICH
  }

  private static final class Atom {

    private final AtomType type;
    private final String text;
    private final BasicLabelTag tag;

    private Atom(AtomType type, String text, BasicLabelTag tag) {
      this.type = type;
      this.text = text;
      this.tag = tag;
    }
  }

  /**
   * One nesting level under construction. Accumulates the body of the cell currently being read and
   * the cells already finished at this level.
   */
  private static final class Frame {

    private final boolean horizontal;

    private final List<BasicRecordCell> cells = new ArrayList<>();

    private StringBuilder text;

    /**
     * Non-null as soon as this cell turned out to contain a styled run. Plain text buffered in
     * {@link #text} is folded in before each styled run, so the original interleaving survives.
     */
    private LabelTag richBody;

    /**
     * Set when a {@code {...}} closed at this level: it becomes the current cell's body and is
     * only committed on the next {@code |} or at the end of the level. Committing it eagerly on
     * {@code }} would make the following {@code |} open a phantom empty cell.
     */
    private RecordTag pendingNested;

    private Frame(boolean horizontal) {
      this.horizontal = horizontal;
    }

    private void addText(String s) {
      if (text == null) {
        text = new StringBuilder();
      }
      text.append(s);
    }

    private void addRich(BasicLabelTag tag) {
      if (richBody == null) {
        richBody = Html.labelTag();
        flushTextInto(richBody, true, false);
      } else {
        flushTextInto(richBody, false, false);
      }
      richBody.add(tag);
    }

    private void flushTextInto(LabelTag target, boolean trimStart, boolean trimEnd) {
      if (text == null) {
        return;
      }
      String value = text.toString();
      if (trimStart) {
        value = trimStart(value);
      }
      if (trimEnd) {
        value = trimEnd(value);
      }
      if (StringUtils.isNotEmpty(value)) {
        target.text(value);
      }
      text = null;
    }

    private String trimStart(String value) {
      int start = 0;
      while (start < value.length() && Character.isWhitespace(value.charAt(start))) {
        start++;
      }
      return value.substring(start);
    }

    private String trimEnd(String value) {
      int end = value.length();
      while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
        end--;
      }
      return value.substring(0, end);
    }

    private void closeNested(Frame nested) {
      pendingNested = nested.toRecordTag();
    }

    private void endCell() {
      cells.add(currentCell());
      resetBody();
    }

    private BasicRecordCell currentCell() {
      if (pendingNested != null) {
        /*
         * Text sitting next to a nested record ("{a}b") is dropped in favour of the structure. The
         * string grammar rejects that shape outright, but this path must not fail the whole graph
         * parse, so the structure wins and the stray text is discarded.
         */
        return Html.cell(pendingNested);
      }

      if (richBody != null) {
        flushTextInto(richBody, false, true);
        return Html.cell(richBody);
      }

      String value = text == null ? null : text.toString().trim();
      return Html.cell(StringUtils.isEmpty(value) ? null : value);
    }

    private void resetBody() {
      text = null;
      richBody = null;
      pendingNested = null;
    }

    /**
     * Closes this level. Whatever body is still buffered becomes the last cell, so {@code "a|b"}
     * yields two cells and {@code "a|"} yields a trailing blank one, matching the string grammar.
     */
    private RecordTag toRecordTag() {
      // Whatever is still buffered forms the last cell of this level
      cells.add(currentCell());
      resetBody();

      RecordTag tag = horizontal ? Html.record() : Html.verticalRecord();
      for (BasicRecordCell cell : cells) {
        tag.cell(cell);
      }
      return tag;
    }
  }
}
