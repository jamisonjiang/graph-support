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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apache_gs.commons.lang3.CharUtils;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html;
import org.graphper.api.Html.BasicRecordCell;
import org.graphper.api.Html.RecordTag;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.def.FlatPoint;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.CollectionUtils;

/**
 * Parser of <strong>Cell Expression</strong>, the record-label string grammar.
 *
 * <h2>This grammar is frozen</h2>
 *
 * <p>It exists to keep {@code label("{a|<p0>b}")} working and accepts no new features. Anything
 * richer — notably per-cell rich text — is expressed with {@link org.graphper.api.Html#record} and
 * reaches layout as a {@link RecordTag}. Both routes converge on {@link RecordTagCompiler}, which
 * owns all record geometry, so this class only ever turns text into structure.
 *
 * <p><strong>Cell Expression</strong> need specified by shape values of {@link
 * NodeShapeEnum#RECORD} and {@link NodeShapeEnum#M_RECORD}, The structure of a record-based node is
 * determined by its label, which has the following schema:
 *
 * <ul>
 *   <li>rlabel = field ( '|' field )*
 *   <li>where field = fieldId or '{' rlabel '}
 *   <li>and fieldId = [ '&lt;' string '&gt;'] [ string ]
 * </ul>
 *
 * <p>Braces, vertical bars and angle brackets must be escaped with a backslash character if you
 * wish them to appear as a literal character. Spaces are interpreted as separators between tokens,
 * so they must be escaped if you want spaces in the text.
 *
 * <p>Flipping between horizontal and vertical layouts is done by nesting fields in braces "{...}".
 * The top-level orientation in a record is horizontal. Thus, a record with label "A | B | C | D"
 * will have 4 fields oriented left to right, while "{A | B | C | D}" will have them from top to
 * bottom and "A | { B | C } | D" will have "B" over "C", with "A" to the left and "D" to the right
 * of "B" and "C".
 *
 * @author Jamison Jiang
 */
public class CellLabelCompiler {

  public static final int SPLIT = 0;

  public static final int PARENT = 1;

  public static final int TEXT = 2;

  public static final int ID = 3;

  public static final FlatPoint DEFAULT_SIZE = new FlatPoint(10, 10);

  private final String label;

  private RecordTag recordTag;

  private CellLabelCompiler(String label) {
    if (label == null) {
      throw newFormatError();
    }
    this.label = label;
    init();
  }

  /**
   * Compile the label of the cell type and convert it into an {@link Cell} object with a tree
   * structure. The initial default split direction is horizontal.Nodes with common nodes in the
   * tree split the same cell, and each nesting of nodes means the conversion of the split
   * direction.
   *
   * @param label label to be compiled
   * @return tree-level structure of label cell
   * @throws LabelFormatException The format of the label is wrong
   */
  public static RootCell compile(String label) throws LabelFormatException {
    return compile(label, false);
  }

  /**
   * Compile the label of the cell type and convert it into an {@link Cell} object with a tree
   * structure. Set an initial default split direction (horizontal or vertical).Nodes with common
   * nodes in the tree split the same cell, and each nesting of nodes means the conversion of the
   * split direction.
   *
   * @param label label to be compiled
   * @param defaultVer default vertical split
   * @return tree-level structure of label cell
   * @throws LabelFormatException The format of the label is wrong
   */
  public static RootCell compile(String label, boolean defaultVer) throws LabelFormatException {
    return compile(label, null, 14, null, null, defaultVer);
  }

  /**
   * Compile the label of the cell type and convert it into an {@link Cell} object with a tree
   * structure. Set an initial default split direction (horizontal or vertical).Nodes with common
   * nodes in the tree split the same cell, and each nesting of nodes means the conversion of the
   * split direction.
   *
   * @param label label to be compiled
   * @param fontName label font name
   * @param fontSize label font size
   * @param margin per cell margin
   * @param minCellSize label cell minimum width and height
   * @param defaultVer default vertical split
   * @return tree-level structure of label cell
   * @throws LabelFormatException The format of the label is wrong
   */
  public static RootCell compile(
      String label,
      String fontName,
      double fontSize,
      FlatPoint margin,
      FlatPoint minCellSize,
      boolean defaultVer)
      throws LabelFormatException {
    return RecordTagCompiler.compile(
        parse(label), fontName, fontSize, margin, minCellSize, defaultVer);
  }

  /**
   * Parses a record-label string into the structured {@link RecordTag} representation, without
   * computing any geometry.
   *
   * <p>This is the only place the frozen record grammar lives. The grammar accepts no new features:
   * richer record content (per-cell rich text) is expressed through {@code Html.record(...)}
   * instead, and both routes converge on {@link RecordTagCompiler}.
   *
   * <p>The returned tag's top level is horizontal, matching the grammar's default; each level of
   * {@code {...}} nesting flips the orientation.
   *
   * @param label label to be parsed
   * @return structured representation of the record label
   * @throws LabelFormatException The format of the label is wrong
   */
  public static RecordTag parse(String label) throws LabelFormatException {
    return new CellLabelCompiler(label).recordTag;
  }

  private RecordTag init() throws LabelFormatException {
    if (recordTag != null) {
      return recordTag;
    }

    List<LabelToken> tokens = tokenizer(label);
    LabelAstNode ast = generateAstNodes(tokens);

    // Grammar validation, must run before the tree is reshaped
    expressAccess(ast, 0, null);

    this.recordTag = toRecordTag(ast, true);
    return recordTag;
  }

  /**
   * Lowers the grammar-shaped parse tree into the structure-shaped {@link RecordTag}.
   *
   * <p>The parse tree carries {@code SPLIT} and {@code ID} nodes because the grammar validation
   * needs to inspect neighbours; the record structure does not — sibling order replaces {@code
   * SPLIT}, and a port id becomes a field on the cell it belongs to. Dropping those nodes here is
   * what keeps the loose {@link LabelAstNode} representation from leaking past the parser.
   */
  private RecordTag toRecordTag(LabelAstNode express, boolean horizontal) {
    RecordTag tag = horizontal ? Html.record() : Html.verticalRecord();

    LabelAstNode pre = null;
    for (Object param : express.params) {
      if (!(param instanceof LabelAstNode)) {
        throw newFormatError();
      }

      LabelAstNode node = (LabelAstNode) param;
      if (node.emptyParams()) {
        // An unclosed or empty port expression such as "<>" is not a legal field id
        throw newFormatError();
      }

      if (node.isText()) {
        BasicRecordCell recordCell = Html.cell().text(node.getTextValue());
        if (pre != null && pre.isId()) {
          recordCell.id(pre.getIdValue());
        }
        tag.cell(recordCell);
      } else if (node.isExpress()) {
        tag.cell(Html.cell(toRecordTag(node, !horizontal)));
      }
      // SPLIT and ID nodes carry no cell of their own; sibling order replaces SPLIT and the port id
      // is folded into the text cell that follows it.

      pre = node;
    }

    return tag;
  }

  private List<LabelToken> tokenizer(String label) {
    if (StringUtils.isEmpty(label)) {
      return Collections.singletonList(new LabelToken(TEXT, label));
    }
    /*
     * Analyze the compiled string, extract the basic types supported by the current rules in the
     * original text content, each element constitutes a basic token.
     *
     * 1|{{2|3}|4}|5 ->
     * [
     *    {"type" : 2, value: "1"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 1, value: "{"},
     *    {"type" : 1, value: "{"},
     *    {"type" : 2, value: "2"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 2, value: "3"},
     *    {"type" : 2, value: "}"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 2, value: "4"},
     *    {"type" : 1, value: "}"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 2, value: "5"},
     * ]
     */

    // Mark pre character is a valid escape tag '\'
    boolean preIsEscapeChar = false;
    StringBuilder labelAppend = null;
    List<LabelToken> tokens = null;

    for (int i = 0; i < label.length(); i++) {
      char c = label.charAt(i);
      /*
       * The current character is an escape, skip the token generation, and mark the next character
       *  no matter what value it is, it must be regarded as a normal character.
       */
      if (!preIsEscapeChar && c == CharUtils.SLOP) {
        preIsEscapeChar = true;
        continue;
      }

      if (tokens == null) {
        labelAppend = new StringBuilder();
        tokens = new ArrayList<>(2);
      }

      // Consume the previous escape character and directly add the current symbol to the current
      // label token
      if (preIsEscapeChar) {
        preIsEscapeChar = false;
        append(labelAppend, c);
        continue;
      }

      if (c == CharUtils.SPLIT) {
        // Liquidation label token
        addLabelToken(labelAppend, tokens);
        // If the previous one or the first character is | or [ or { or >, add an empty label token
        if (needFillNoneLabel(tokens)) {
          tokens.add(new LabelToken(TEXT, null));
        }
        tokens.add(new LabelToken(SPLIT, CharUtils.SPLIT));
        continue;
      }

      if (c == CharUtils.LEFT_BRACE) {
        // Liquidation label token
        addLabelToken(labelAppend, tokens);
        if (lastIsRight(tokens)) {
          throw newFormatError();
        }
        tokens.add(new LabelToken(PARENT, CharUtils.LEFT_BRACE));
        continue;
      }

      if (c == CharUtils.RIGHT_BRACE) {
        // Liquidation label token
        addLabelToken(labelAppend, tokens);
        // If the previous one is |, add an empty label token
        if (lastIsSplitOrIdRight(tokens)) {
          tokens.add(new LabelToken(TEXT, null));
        }
        tokens.add(new LabelToken(PARENT, CharUtils.RIGHT_BRACE));
        continue;
      }

      if (c == CharUtils.LT) {
        // Liquidation label token
        addLabelToken(labelAppend, tokens);
        tokens.add(new LabelToken(ID, CharUtils.LT));
        continue;
      }

      if (c == CharUtils.GT) {
        // Liquidation label token
        addLabelToken(labelAppend, tokens);
        tokens.add(new LabelToken(ID, CharUtils.GT));
        continue;
      }

      append(labelAppend, c);
    }

    // Escaped char need to be consumed
    if (preIsEscapeChar) {
      throw newFormatError();
    }

    addLabelToken(labelAppend, tokens);
    if (CollectionUtils.isEmpty(tokens)) {
      return Collections.singletonList(new LabelToken(TEXT, null));
    }

    // If the two endpoints are | symbols or id, additional labels need to be added at both ends
    handleEndpoint(tokens);
    return tokens;
  }

  private void handleEndpoint(List<LabelToken> tokens) {
    LabelToken first = tokens.get(0);
    LabelToken last = tokens.get(tokens.size() - 1);
    if (first.isSplit()) {
      tokens.add(0, new LabelToken(TEXT, null));
    }
    if (last.isSplit() || last.isIdRight()) {
      tokens.add(new LabelToken(TEXT, null));
    }
  }

  private boolean addLabelToken(StringBuilder labelAppend, List<LabelToken> tokens) {
    if (tokens == null || labelAppend == null || labelAppend.length() == 0) {
      return false;
    }
    tokens.add(new LabelToken(TEXT, labelAppend.toString()));
    labelAppend.delete(0, labelAppend.length());
    return true;
  }

  private LabelAstNode generateAstNodes(List<LabelToken> tokens) {
    if (CollectionUtils.isEmpty(tokens)) {
      throw newFormatError();
    }

    /*
     * Generate the corresponding AST nodes according to the token list.
     *
     * Token list:
     * [
     *    {"type" : 2, value: "1"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 1, value: "{"},
     *    {"type" : 1, value: "{"},
     *    {"type" : 2, value: "2"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 2, value: "3"},
     *    {"type" : 2, value: "}"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 2, value: "4"},
     *    {"type" : 1, value: "}"},
     *    {"type" : 0, value: "|"},
     *    {"type" : 2, value: "5"},
     * ]
     * =>
     * Ast nodes
     * [
     *    {
     *        "type" : 2,
     *        "params" : ["1"]
     *    },
     *    {
     *        "type" : 1,
     *        "params" : [
     *            {
     *                "type" : 1,
     *                "params" : [
     *                    {
     *                        "type" : 2,
     *                        "params" : ["2"]
     *                    },
     *                    {
     *                        "type" : 2,
     *                        "params" : ["3"]
     *                    }
     *                ]
     *            }
     *            {
     *                "type" : 2,
     *                "params" : ["4"]
     *            }
     *        ]
     *    },
     *    {
     *        "type" : 2,
     *        "params" : ["5"]
     *    }
     * ]
     */
    LabelAstNode ast = new LabelAstNode();
    ast.type = PARENT;
    ast.params = new ArrayList<>();
    Iterator<LabelToken> iterator = tokens.iterator();
    while (iterator.hasNext()) {
      LabelAstNode node = access(iterator, iterator.next());
      if (node == null) {
        continue;
      }
      ast.params.add(node);
    }
    return ast;
  }

  private LabelAstNode access(Iterator<LabelToken> tokenIterator, LabelToken token) {
    // Label node, directed add to tree
    if (token.isLabel() || token.isSplit()) {
      LabelAstNode node = new LabelAstNode();
      node.type = token.type;
      node.params = new ArrayList<>(1);
      node.params.add(token.value);
      return node;
    }

    // Find subexpressions, create nodes for subexpressions
    if (token.isLeftBrace()) {
      LabelAstNode node = new LabelAstNode();
      node.type = token.type;
      node.params = new ArrayList<>();
      while (tokenIterator.hasNext()) {
        token = tokenIterator.next();
        // We must guarantee the "closure" of the subexpression
        if (token.isRightBrace()) {
          return node;
        }

        // Create child nodes recursively
        LabelAstNode n = access(tokenIterator, token);
        if (n == null) {
          continue;
        }
        node.params.add(n);
      }

      // Express not close
      throw newFormatError();
    }

    // Find id express
    if (token.isIdLeft()) {
      LabelAstNode node = new LabelAstNode();
      node.type = token.type;
      node.params = new ArrayList<>();
      while (tokenIterator.hasNext()) {
        token = tokenIterator.next();
        // We must guarantee the "closure" of the subexpression
        if (token.isIdRight()) {
          return node;
        }

        if (!token.isLabel()) {
          throw newFormatError();
        }

        // Create child nodes recursively
        LabelAstNode n = access(tokenIterator, token);
        if (n == null) {
          continue;
        }
        node.params.add(n);
      }

      // Express not close
      throw newFormatError();
    }

    // Find not closed GT
    if (token.isIdRight()) {
      throw newFormatError();
    }

    return null;
  }

  private void expressAccess(LabelAstNode node, int idx, List<Object> params) {
    if (!node.isExpress()) {
      return;
    }

    // The expression must be surrounded by null or | symbols
    if (!preIsNullOrSplit(params, idx) || !nextIsNullOrSplit(params, idx)) {
      throw newFormatError();
    }

    // If it is an empty expression, insert an empty label node.
    if (CollectionUtils.isEmpty(node.params)) {
      addNullLabelAstNode(node);
      return;
    }

    int i = -1;
    Iterator<Object> iterator = node.params.iterator();
    while (iterator.hasNext()) {
      i++;
      Object param = iterator.next();
      if (!(param instanceof LabelAstNode)) {
        throw newFormatError();
      }

      // Child node verification
      LabelAstNode n = (LabelAstNode) param;
      idAccess(n, i, node.params);
      labelAccess(n, i, node.params);
      expressAccess(n, i, node.params);
    }
  }

  private void labelAccess(LabelAstNode node, int idx, List<Object> params) {
    if (!node.isText()) {
      return;
    }

    if (node.emptyParams()
        || !nextIsNullOrSplit(params, idx)
        || (!preIsNullOrSplit(params, idx) && !preIsIdExpress(params, idx))) {
      throw newFormatError();
    }
  }

  private void idAccess(LabelAstNode node, int idx, List<Object> params) {
    if (!node.isId()) {
      return;
    }

    if (preIsNullOrSplit(params, idx) && nextIsLabel(params, idx)) {
      return;
    }

    throw newFormatError();
  }

  private void addNullLabelAstNode(LabelAstNode node) {
    if (CollectionUtils.isNotEmpty(node.params)) {
      return;
    }
    node.params = new ArrayList<>(1);
    node.params.add(newNullLabelAstNode());
  }

  private LabelAstNode newNullLabelAstNode() {
    LabelAstNode node = new LabelAstNode();
    node.type = TEXT;
    node.params = new ArrayList<>(1);
    node.params.add(null);
    return node;
  }

  private boolean nextIsLabel(List<Object> params, int idx) {
    Object next = nextParam(params, idx);
    if (!(next instanceof LabelAstNode)) {
      return false;
    }
    return ((LabelAstNode) next).isText();
  }

  private boolean preIsIdExpress(List<Object> params, int idx) {
    Object pre = preParam(params, idx);
    if (!(pre instanceof LabelAstNode)) {
      return false;
    }
    return ((LabelAstNode) pre).isId();
  }

  private boolean preIsSplit(List<Object> params, int idx) {
    Object pre = preParam(params, idx);
    if (!(pre instanceof LabelAstNode)) {
      return false;
    }
    return ((LabelAstNode) pre).isSplit();
  }

  private boolean nextIsSplit(List<Object> params, int idx) {
    Object next = nextParam(params, idx);
    if (!(next instanceof LabelAstNode)) {
      return false;
    }
    return ((LabelAstNode) next).isSplit();
  }

  private boolean preIsNullOrSplit(List<Object> params, int idx) {
    return preIsNull(params, idx) || preIsSplit(params, idx);
  }

  private boolean nextIsNullOrSplit(List<Object> params, int idx) {
    return nextIsNull(params, idx) || nextIsSplit(params, idx);
  }

  private boolean preIsNull(List<Object> params, int idx) {
    return preParam(params, idx) == null;
  }

  private boolean nextIsNull(List<Object> params, int idx) {
    return nextParam(params, idx) == null;
  }

  private Object preParam(List<Object> params, int idx) {
    if (CollectionUtils.isEmpty(params) || idx <= 0 || idx >= params.size()) {
      return null;
    }
    return params.get(idx - 1);
  }

  private Object nextParam(List<Object> params, int idx) {
    if (CollectionUtils.isEmpty(params) || idx >= params.size() - 1) {
      return null;
    }
    return params.get(idx + 1);
  }

  private LabelFormatException newFormatError() {
    return new LabelFormatException("Bad label format " + label);
  }

  private boolean needFillNoneLabel(List<LabelToken> tokens) {
    return lastIsLeft(tokens) || lastIsSplit(tokens) || lastIsIdRight(tokens);
  }

  public boolean lastIsSplitOrIdRight(List<LabelToken> tokens) {
    return lastIsSplit(tokens) || lastIsIdRight(tokens);
  }

  private boolean lastIsSplit(List<LabelToken> tokens) {
    if (CollectionUtils.isEmpty(tokens)) {
      return false;
    }
    LabelToken last = tokens.get(tokens.size() - 1);
    return last.isSplit();
  }

  private boolean lastIsIdRight(List<LabelToken> tokens) {
    if (CollectionUtils.isEmpty(tokens)) {
      return false;
    }
    LabelToken last = tokens.get(tokens.size() - 1);
    return last.isIdRight();
  }

  private boolean lastIsLeft(List<LabelToken> tokens) {
    if (CollectionUtils.isEmpty(tokens)) {
      return false;
    }
    LabelToken last = tokens.get(tokens.size() - 1);
    return last.isLeftBrace();
  }

  private boolean lastIsRight(List<LabelToken> tokens) {
    if (CollectionUtils.isEmpty(tokens)) {
      return false;
    }
    LabelToken last = tokens.get(tokens.size() - 1);
    return last.isRightBrace();
  }

  private void append(StringBuilder sb, char c) {
    if (c != CharUtils.SPACE || (sb.length() > 0 && !lastIsSpace(sb))) {
      sb.append(c);
    }
  }

  private int lastIdx(StringBuilder sb) {
    if (sb.length() == 0) {
      throw new IllegalArgumentException();
    }
    return sb.length() - 1;
  }

  private boolean lastIsSpace(StringBuilder sb) {
    if (sb.length() == 0) {
      return false;
    }
    return sb.charAt(lastIdx(sb)) == CharUtils.SPACE;
  }

  // ------------------------------------------------- private static class
  // -------------------------------------------------
  private static class LabelToken {

    final int type;

    private final Object value;

    public LabelToken(int type, Object value) {
      this.type = type;
      this.value = value;
    }

    boolean isSplit() {
      return type == SPLIT && Objects.equals(value, CharUtils.SPLIT);
    }

    boolean isParent() {
      return type == PARENT;
    }

    boolean isLeftBrace() {
      return isParent() && Objects.equals(value, CharUtils.LEFT_BRACE);
    }

    boolean isRightBrace() {
      return isParent() && Objects.equals(value, CharUtils.RIGHT_BRACE);
    }

    boolean isId() {
      return type == ID;
    }

    boolean isIdLeft() {
      return isId() && Objects.equals(value, CharUtils.LT);
    }

    boolean isIdRight() {
      return isId() && Objects.equals(value, CharUtils.GT);
    }

    boolean isLabel() {
      return type == TEXT;
    }
  }

  private static class LabelAstNode {

    private int type;

    private List<Object> params;

    boolean isSplit() {
      return type == SPLIT;
    }

    boolean isExpress() {
      return type == PARENT;
    }

    boolean isText() {
      return type == TEXT;
    }

    boolean isId() {
      return type == ID;
    }

    String getTextValue() {
      if (!isText() || emptyParams()) {
        return null;
      }

      Object first = params.get(0);
      return first instanceof String ? (String) first : null;
    }

    String getIdValue() {
      if (!isId() || emptyParams()) {
        return null;
      }

      Object first = params.get(0);
      if (!(first instanceof LabelAstNode)) {
        return null;
      }
      LabelAstNode idText = (LabelAstNode) first;
      return idText.getTextValue();
    }

    boolean emptyParams() {
      return CollectionUtils.isEmpty(params);
    }
  }
}
