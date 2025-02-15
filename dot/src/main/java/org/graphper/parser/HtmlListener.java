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

import static org.graphper.api.Html.fontAttrs;
import static org.graphper.parser.ParserUtils.setFontAttributes;
import static org.graphper.parser.ParserUtils.setTableAttributes;
import static org.graphper.parser.ParserUtils.setTdAttributes;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache_gs.commons.lang3.StringUtils;
import org.apache_gs.commons.text.StringEscapeUtils;
import org.graphper.api.Html;
import org.graphper.api.Html.FontAttrs;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.parser.grammar.HTMLParser.BTagContext;
import org.graphper.parser.grammar.HTMLParser.BrTagContext;
import org.graphper.parser.grammar.HTMLParser.FontTagContext;
import org.graphper.parser.grammar.HTMLParser.HcTagContext;
import org.graphper.parser.grammar.HTMLParser.HlTagContext;
import org.graphper.parser.grammar.HTMLParser.HrTagContext;
import org.graphper.parser.grammar.HTMLParser.HtmlAttributeContext;
import org.graphper.parser.grammar.HTMLParser.HtmlChardataContext;
import org.graphper.parser.grammar.HTMLParser.HtmlElementContext;
import org.graphper.parser.grammar.HTMLParser.HtmlTagContext;
import org.graphper.parser.grammar.HTMLParser.ITagContext;
import org.graphper.parser.grammar.HTMLParser.OTagContext;
import org.graphper.parser.grammar.HTMLParser.STagContext;
import org.graphper.parser.grammar.HTMLParser.SubTagContext;
import org.graphper.parser.grammar.HTMLParser.SupTagContext;
import org.graphper.parser.grammar.HTMLParser.TableContext;
import org.graphper.parser.grammar.HTMLParser.TagContentContext;
import org.graphper.parser.grammar.HTMLParser.TdContentContext;
import org.graphper.parser.grammar.HTMLParser.TdContext;
import org.graphper.parser.grammar.HTMLParser.TrContext;
import org.graphper.parser.grammar.HTMLParser.UTagContext;
import org.graphper.parser.grammar.HTMLParser.VbTagContext;
import org.graphper.parser.grammar.HTMLParser.VcTagContext;
import org.graphper.parser.grammar.HTMLParser.VtTagContext;
import org.graphper.parser.grammar.HTMLParserBaseListener;
import org.graphper.util.CollectionUtils;

/**
 * A listener for parsing a subset of HTML-like syntax and converting it into a {@link Table} or
 * {@link LabelTag} structure, depending on the input.
 * <p>
 * This listener extends {@link HTMLParserBaseListener} and processes events such as entering and
 * exiting tags ({@code <table>}, {@code <td>}, etc.), gathering content (text or nested tags), and
 * applying attributes (e.g., font color, size). The result can be retrieved using:
 * <ul>
 *   <li>{@link #getLabel()} if the root is a simple string label,</li>
 *   <li>{@link #getTable()} if the root is an HTML table, or</li>
 *   <li>{@link #getLabelTag()} if the root is an HTML-based {@link LabelTag}.</li>
 * </ul>
 *
 * @author Jamison Jiang
 */
public class HtmlListener extends HTMLParserBaseListener {

  /**
   * The final root object of the parsed HTML, which could be a {@link Table}, a {@link LabelTag},
   * or a {@link String}.
   */
  private Object root;

  /**
   * A stack of {@link Table} objects corresponding to nested {@code <table>} tags.
   */
  private Deque<Table> tableQueue;

  /**
   * A stack of {@link List}{@code <Td>} objects corresponding to nested {@code <tr>} tags. Each
   * list holds the {@link Td} elements within that row.
   */
  private Deque<List<Td>> tdQueue;

  /**
   * A stack of {@link Tag} objects used to build and nest {@link LabelTag} structures.
   */
  private Deque<Tag> tagQueue;

  /**
   * Called upon entering any HTML tag. If the tag is not a {@code <table>}, this method checks if
   * the content is just text or nested elements.
   */
  @Override
  public void enterHtmlTag(HtmlTagContext ctx) {
    if (ctx.table() != null) {
      return;
    }

    TagContentContext tagCtx = ctx.tagContent();
    if (tagCtx == null) {
      return;
    }

    // If the element has no sub-elements, treat it as pure text root
    if (CollectionUtils.isEmpty(tagCtx.htmlElement())) {
      root = tagCtx.getText();
      return;
    }
    // Otherwise, prepare for a nested tag structure
    pushTag(new Tag(labelTag -> root = labelTag));
  }

  /**
   * Called upon exiting an HTML tag. Consumes any pending {@link Tag} on the stack, finalizing the
   * nested label structure.
   */
  @Override
  public void exitHtmlTag(HtmlTagContext ctx) {
    pollAndConsTag();
  }

  /**
   * Called upon entering a {@code <table>} tag. Creates a new {@link Table} instance and, if we're
   * currently inside a {@code <td>}, associates the table with that cell. If no root is set yet,
   * this table becomes the root.
   */
  @Override
  public void enterTable(TableContext ctx) {
    Table t = Html.table();
    List<Td> tds = peekTds();
    if (CollectionUtils.isNotEmpty(tds)) {
      Td td = tds.get(tds.size() - 1);
      td.table(t);
    }

    pushTable(t);
    if (root == null) {
      root = t;
    }
  }

  /**
   * Called upon exiting a {@code <table>} tag. Pops the current table off the stack, effectively
   * returning to the parent container (or no container at all).
   */
  @Override
  public void exitTable(TableContext ctx) {
    popCurrentTable();
  }

  /**
   * Called upon entering a {@code <tr>} tag. Prepares a new list of {@link Td} objects to hold the
   * row's cells.
   */
  @Override
  public void enterTr(TrContext ctx) {
    pushTds();
  }

  /**
   * Called upon exiting a {@code <tr>} tag. The list of {@link Td} objects for this row is
   * retrieved and added to the current {@link Table} as a single row.
   *
   * @throws ParseException if no {@link Td} elements are found for the row
   */
  @Override
  public void exitTr(TrContext ctx) {
    List<Td> tds = peekTds();
    if (CollectionUtils.isEmpty(tds)) {
      throw new ParseException("<tr> not contains any <td>");
    }
    curretTable().tr(tds.toArray(new Td[0]));
    pollTds();
  }

  /**
   * Called upon entering a {@code <td>} tag. Creates a new {@link Td} and adds it to the current
   * row. If the cell contains nested HTML elements, we prepare a new {@link Tag} to capture them.
   *
   * @throws ParseException if the {@code <td>} is not within any {@code <tr>}
   */
  @Override
  public void enterTd(TdContext ctx) {
    List<Td> tds = peekTds();
    if (tds == null) {
      throw new ParseException("<td> not belong to any <tr>");
    }

    Td td = Html.td();
    tds.add(td);

    TdContentContext tdCtx = ctx.tdContent();
    if (pureText(tdCtx)) {
      String text = tdCtx.getText();
      text = StringUtils.isEmpty(text) ? StringUtils.EMPTY : text.trim();
      text = StringEscapeUtils.escapeHtml4(text);
      td.text(text);
      return;
    }

    if (CollectionUtils.isNotEmpty(tdCtx.htmlElement())) {
      pushTag(new Tag(currentTd()::textTag));
    }
  }

  /**
   * Called upon exiting a {@code <td>} tag. Consumes any pending {@link Tag} on the stack,
   * finalizing the cell's nested label structure.
   */
  @Override
  public void exitTd(TdContext ctx) {
    pollAndConsTag();
  }

  /**
   * Called upon entering any attribute definition (e.g. {@code width="200"}). Applies the
   * attributes to either the {@link Td} or the {@link Table}, depending on the parent context.
   */
  @Override
  public void enterHtmlAttribute(HtmlAttributeContext ctx) {
    if (ctx.TAG_NAME() == null || ctx.ATTVALUE_VALUE() == null) {
      return;
    }

    String attributeValue = ctx.ATTVALUE_VALUE().getText();
    if (StringUtils.isEmpty(attributeValue)) {
      return;
    }

    attributeValue = adaptAttrValue(attributeValue);

    ParserRuleContext parent = ctx.getParent();
    if (parent instanceof TdContext) {
      setTdAttributes(currentTd(), ctx.TAG_NAME().getText(), attributeValue);
    }
    if (parent instanceof TableContext) {
      setTableAttributes(curretTable(), ctx.TAG_NAME().getText(), attributeValue);
    }
  }

  /**
   * Called upon entering a generic HTML element (e.g., {@code <b>}, {@code <i>}, {@code <font>}).
   * Depending on the type of tag, it applies appropriate formatting or alignment by constructing or
   * updating the {@link LabelTag}.
   */
  @Override
  public void enterHtmlElement(HtmlElementContext ctx) {
    FontTagContext font = ctx.fontTag();
    BTagContext bold = ctx.bTag();
    ITagContext italic = ctx.iTag();
    UTagContext underline = ctx.uTag();
    STagContext strikethrough = ctx.sTag();
    OTagContext overline = ctx.oTag();
    SupTagContext superscript = ctx.supTag();
    SubTagContext subscript = ctx.subTag();
    VtTagContext verticalTop = ctx.vtTag();
    VbTagContext verticalBottom = ctx.vbTag();
    VcTagContext verticalCenter = ctx.vcTag();
    HlTagContext horizontalLeft = ctx.hlTag();
    HrTagContext horizontalRight = ctx.hrTag();
    HcTagContext horizontalCenter = ctx.hcTag();
    BrTagContext br = ctx.brTag();

    // Handling <font> with optional attributes
    if (font != null) {
      FontAttrs fontAttrs = parseFontAttrs(font.htmlAttribute());
      if (fontAttrs == null) {
        handleTagCtx(font.tagContent(), Html::font, LabelTag::font);
      } else {
        handleTagCtx(font.tagContent(), tag -> Html.font(tag, fontAttrs),
                     (tag, childTag) -> tag.font(childTag, fontAttrs));
      }
    }

    if (bold != null) {
      handleTagCtx(bold.tagContent(), Html::bold, LabelTag::bold);
    }

    if (italic != null) {
      handleTagCtx(italic.tagContent(), Html::italic, LabelTag::italic);
    }

    if (underline != null) {
      handleTagCtx(underline.tagContent(), Html::underline, LabelTag::underline);
    }

    if (strikethrough != null) {
      handleTagCtx(strikethrough.tagContent(), Html::strikeThrough, LabelTag::strikeThrough);
    }

    if (overline != null) {
      handleTagCtx(overline.tagContent(), Html::overline, LabelTag::overline);
    }

    if (superscript != null) {
      handleTagCtx(superscript.tagContent(), Html::superscript, LabelTag::superscript);
    }

    if (subscript != null) {
      handleTagCtx(subscript.tagContent(), Html::subscript, LabelTag::subscript);
    }

    if (verticalTop != null) {
      handleTagCtx(verticalTop.tagContent(), Html::top, LabelTag::top);
    }

    if (verticalBottom != null) {
      handleTagCtx(verticalBottom.tagContent(), Html::bottom, LabelTag::bottom);
    }

    if (verticalCenter != null) {
      handleTagCtx(verticalCenter.tagContent(), Html::verticalCenter, LabelTag::verticalCenter);
    }

    if (horizontalLeft != null) {
      handleTagCtx(horizontalLeft.tagContent(), Html::left, LabelTag::left);
    }

    if (horizontalRight != null) {
      handleTagCtx(horizontalRight.tagContent(), Html::right, LabelTag::right);
    }

    if (horizontalCenter != null) {
      handleTagCtx(horizontalCenter.tagContent(), Html::horizontalCenter,
                   LabelTag::horizontalCenter);
    }

    if (br != null) {
      peekAndConsTagIfAbsent(Html::br, LabelTag::br);
    }
  }

  /**
   * Called upon exiting a generic HTML element. If the element is not a {@code <br>}, we pop and
   * consume any pending {@link Tag}.
   */
  @Override
  public void exitHtmlElement(HtmlElementContext ctx) {
    BrTagContext br = ctx.brTag();
    if (br == null) {
      pollAndConsTag();
    }
  }

  /**
   * Called upon encountering character data within an HTML element. If the root is not already set
   * to a {@link String}, it attempts to treat this data as text within the current {@link Tag}.
   */
  @Override
  public void enterHtmlChardata(HtmlChardataContext ctx) {
    // If we already have a string root, do nothing
    if (getLabel() != null) {
      return;
    }

    String text = StringUtils.isEmpty(ctx.getText()) ? StringUtils.EMPTY : ctx.getText();
    // Trim whitespace artifacts like newlines or tabs
    text = text.replace("\n", "").replace("\r", "").replace("\t", "");
    text = StringEscapeUtils.escapeHtml4(text);
    String t = text;
    peekAndConsTagIfAbsent(() -> Html.text(t), labelTag -> labelTag.text(t));
  }

  /**
   * Retrieves the final label if the parsed root is a simple text {@link String}.
   *
   * @return the label as a string, or {@code null} if not available
   */
  public String getLabel() {
    if (root instanceof String) {
      return (String) root;
    }
    return null;
  }

  /**
   * Retrieves the parsed {@link Table} if the root is an HTML table.
   *
   * @return the {@link Table} root, or {@code null} if not available
   */
  public Table getTable() {
    if (root instanceof Table) {
      return (Table) root;
    }
    return null;
  }

  /**
   * Retrieves the parsed {@link LabelTag} if the root is an HTML-like label structure.
   *
   * @return the {@link LabelTag} root, or {@code null} if not available
   */
  public LabelTag getLabelTag() {
    if (root instanceof LabelTag) {
      return (LabelTag) root;
    }
    return null;
  }

  /**
   * Obtains the current {@link Td} (the last in the current row).
   *
   * @return the current {@link Td}
   * @throws ParseException if no current {@link Td} is found
   */
  private Td currentTd() {
    List<Td> tds = peekTds();
    if (CollectionUtils.isEmpty(tds)) {
      throw new ParseException("Cannot found current td");
    }
    return tds.get(tds.size() - 1);
  }

  /**
   * Obtains the current {@link Table} (the last pushed onto the {@link #tableQueue}).
   *
   * @return the current {@link Table}
   * @throws ParseException if there is no table context on the stack
   */
  private Table curretTable() {
    if (tableQueue == null) {
      throw new ParseException("Cannot found current table");
    }

    Table t = tableQueue.peek();
    if (t == null) {
      throw new ParseException("Cannot found current table");
    }
    return t;
  }

  /**
   * Removes the topmost {@link Table} from the stack, returning to a higher-level context (or none
   * if the stack becomes empty).
   */
  private void popCurrentTable() {
    if (tableQueue == null) {
      return;
    }
    tableQueue.pop();
  }

  /**
   * Pushes a new {@link Table} onto the stack, making it the current table context.
   *
   * @param table the new {@link Table} to push
   */
  private void pushTable(Table table) {
    if (tableQueue == null) {
      tableQueue = new LinkedList<>();
    }
    tableQueue.push(table);
  }

  /**
   * Removes and returns the current list of {@link Td} objects from {@link #tdQueue}.
   *
   * @return the list of {@link Td} elements for the most recent row
   */
  private List<Td> pollTds() {
    if (tdQueue == null) {
      return null;
    }
    return tdQueue.poll();
  }

  /**
   * Returns the list of {@link Td} objects for the current row (top of the {@link #tdQueue}), or
   * {@code null} if none are active.
   */
  private List<Td> peekTds() {
    if (tdQueue == null) {
      return null;
    }
    return tdQueue.peek();
  }

  /**
   * Pushes a new list of {@link Td} elements to represent a newly encountered row ({@code <tr>}).
   */
  private void pushTds() {
    if (tdQueue == null) {
      tdQueue = new LinkedList<>();
    }
    tdQueue.push(new ArrayList<>());
  }

  /**
   * Pushes a new {@link Tag} onto the stack, used to build or modify {@link LabelTag} structures.
   *
   * @param tag the {@link Tag} to add
   */
  private void pushTag(Tag tag) {
    if (tagQueue == null) {
      tagQueue = new LinkedList<>();
    }
    tagQueue.push(tag);
  }

  /**
   * Retrieves the topmost {@link Tag} from the stack without removing it.
   *
   * @return the current {@link Tag}, or {@code null} if none
   */
  private Tag peekTag() {
    if (tagQueue == null) {
      return null;
    }
    return tagQueue.peek();
  }

  /**
   * Removes and returns the topmost {@link Tag} from the stack.
   *
   * @return the {@link Tag} or {@code null} if none
   */
  private Tag pollTag() {
    if (tagQueue == null) {
      return null;
    }
    return tagQueue.poll();
  }

  /**
   * Removes the topmost {@link Tag} from the stack and finalizes its effect by calling
   * {@link Tag#consumeTag()}.
   */
  private void pollAndConsTag() {
    Tag tag = pollTag();
    if (tag != null) {
      tag.consumeTag();
    }
  }

  /**
   * Ensures a label-tag is present at the top of the stack. If absent, applies the given supplier
   * to create one. Otherwise, uses the provided consumer to modify the existing label-tag.
   */
  private void peekAndConsTagIfAbsent(Supplier<LabelTag> supplier,
                                      Consumer<LabelTag> labelTagCons) {
    Tag tag = peekTag();
    if (tag == null) {
      return;
    }
    if (tag.labelTag != null) {
      labelTagCons.accept(tag.labelTag);
    } else {
      tag.labelTag = supplier.get();
    }
  }

  /**
   * Handles a nested tag context (such as {@code <font>}, {@code <b>}, etc.) by pushing a new
   * {@link Tag} that, when consumed, applies either a supplier or a chained consumer to build the
   * {@link LabelTag}.
   *
   * @param tagCtx              the context containing nested elements or text
   * @param tagTagSupplier      a {@link UnaryOperator} that generates a {@link LabelTag}
   * @param tagChainTagConsumer a {@link BiConsumer} that merges child tags into a parent
   */
  private void handleTagCtx(TagContentContext tagCtx, UnaryOperator<LabelTag> tagTagSupplier,
                            BiConsumer<LabelTag, LabelTag> tagChainTagConsumer) {
    if (tagCtx == null) {
      return;
    }
    pushTag(new Tag(labelTag -> peekAndConsTagIfAbsent(() -> tagTagSupplier.apply(labelTag),
                                                       lt -> tagChainTagConsumer.accept(lt, labelTag))));
  }

  /**
   * Determines if a {@code <td>} context is pure text (no nested {@code <table>} or HTML
   * elements).
   */
  private boolean pureText(TdContentContext tdCtx) {
    return CollectionUtils.isEmpty(tdCtx.table()) && CollectionUtils.isEmpty(tdCtx.htmlElement());
  }

  /**
   * Parses a list of font attributes (e.g., {@code size="12"} or {@code face="Arial"}). Builds a
   * {@link FontAttrs} object if any valid attributes are found.
   *
   * @param fontAttrsCtx list of HTML attributes found within a {@code <font>} tag
   * @return a {@link FontAttrs} instance containing parsed attributes, or {@code null} if none
   */
  private FontAttrs parseFontAttrs(List<HtmlAttributeContext> fontAttrsCtx) {
    if (CollectionUtils.isEmpty(fontAttrsCtx)) {
      return null;
    }

    FontAttrs fontAttrs = fontAttrs();
    for (HtmlAttributeContext attr : fontAttrsCtx) {
      String attributeValue = attr.ATTVALUE_VALUE().getText();
      if (StringUtils.isEmpty(attributeValue)) {
        continue;
      }
      setFontAttributes(fontAttrs, attr.TAG_NAME().getText(), adaptAttrValue(attributeValue));
    }
    return fontAttrs;
  }

  /**
   * Normalizes an attribute value by trimming quotes and surrounding whitespace.
   *
   * @param attributeValue the raw attribute string, possibly including quotes
   * @return the cleaned string
   */
  private static String adaptAttrValue(String attributeValue) {
    attributeValue = attributeValue.trim();
    if ((attributeValue.startsWith("'") && attributeValue.endsWith("'")) || (
        attributeValue.startsWith("\"") && attributeValue.endsWith("\""))) {
      attributeValue = attributeValue.substring(1, attributeValue.length() - 1).trim();
    }
    return attributeValue;
  }

  /**
   * Represents a pending tag in the parse stack, holding a reference to a possible {@link LabelTag}
   * and a consumer that applies this tag to a parent context when {@link #consumeTag()} is called.
   */
  private static class Tag {

    private LabelTag labelTag;
    private final Consumer<LabelTag> labelTagCons;

    /**
     * Constructs a new {@code Tag}.
     *
     * @param labelTagCons a consumer that applies the {@link LabelTag} to a parent or container
     */
    public Tag(Consumer<LabelTag> labelTagCons) {
      this.labelTagCons = labelTagCons;
    }

    /**
     * Finalizes this tag by calling its consumer with the current {@link #labelTag}.
     */
    private void consumeTag() {
      labelTagCons.accept(labelTag);
    }
  }
}