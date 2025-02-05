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

public class HtmlListener extends HTMLParserBaseListener {

  private Object root;

  private Deque<Table> tableQueue;

  private Deque<List<Td>> tdQueue;

  private Deque<Tag> tagQueue;

  @Override
  public void enterHtmlTag(HtmlTagContext ctx) {
    if (ctx.table() != null) {
      return;
    }

    TagContentContext tagCtx = ctx.tagContent();
    if (tagCtx == null) {
      return;
    }

    if (CollectionUtils.isEmpty(tagCtx.htmlElement())) {
      root = tagCtx.getText();
      return;
    }
    pushTag(new Tag(labelTag -> root = labelTag));
  }

  @Override
  public void exitHtmlTag(HtmlTagContext ctx) {
    pollAndConsTag();
  }

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

  @Override
  public void exitTable(TableContext ctx) {
    popCurrentTable();
  }

  @Override
  public void enterTr(TrContext ctx) {
    pushTds();
  }

  @Override
  public void exitTr(TrContext ctx) {
    List<Td> tds = peekTds();
    if (CollectionUtils.isEmpty(tds)) {
      throw new ParseException("<tr> not contains any <td>");
    }
    curretTable().tr(tds.toArray(new Td[0]));
    pollTds();
  }

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
      td.text(text);
      return;
    }

    if (CollectionUtils.isNotEmpty(tdCtx.htmlElement())) {
      pushTag(new Tag(currentTd()::textTag));
    }
  }

  @Override
  public void exitTd(TdContext ctx) {
    pollAndConsTag();
  }

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

  @Override
  public void exitHtmlElement(HtmlElementContext ctx) {
    BrTagContext br = ctx.brTag();
    if (br == null) {
      pollAndConsTag();
    }
  }

  @Override
  public void enterHtmlChardata(HtmlChardataContext ctx) {
    // Entire html content is String
    if (getLabel() != null) {
      return;
    }

    String text = StringUtils.isEmpty(ctx.getText()) ? StringUtils.EMPTY : ctx.getText();
    text = text.replace("\n", "")
        .replace("\r", "")
        .replace("\t", "");
    String t = text;
    peekAndConsTagIfAbsent(() -> Html.text(t), labelTag -> labelTag.text(t));
  }

  public String getLabel() {
    if (root instanceof String) {
      return (String) root;
    }
    return null;
  }

  public Table getTable() {
    if (root instanceof Table) {
      return (Table) root;
    }
    return null;
  }

  public LabelTag getLabelTag() {
    if (root instanceof LabelTag) {
      return (LabelTag) root;
    }
    return null;
  }

  private Td currentTd() {
    List<Td> tds = peekTds();
    if (CollectionUtils.isEmpty(tds)) {
      throw new ParseException("Cannot found current td");
    }
    return tds.get(tds.size() - 1);
  }

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

  private void popCurrentTable() {
    if (tableQueue == null) {
      return;
    }
    tableQueue.pop();
  }

  private void pushTable(Table table) {
    if (tableQueue == null) {
      tableQueue = new LinkedList<>();
    }
    tableQueue.push(table);
  }

  private List<Td> pollTds() {
    if (tdQueue == null) {
      return null;
    }

    return tdQueue.poll();
  }

  private List<Td> peekTds() {
    if (tdQueue == null) {
      return null;
    }

    return tdQueue.peek();
  }

  private void pushTds() {
    if (tdQueue == null) {
      tdQueue = new LinkedList<>();
    }
    tdQueue.push(new ArrayList<>());
  }

  private void pushTag(Tag tag) {
    if (tagQueue == null) {
      tagQueue = new LinkedList<>();
    }
    tagQueue.push(tag);
  }

  private Tag peekTag() {
    if (tagQueue == null) {
      return null;
    }
    return tagQueue.peek();
  }

  private Tag pollTag() {
    if (tagQueue == null) {
      return null;
    }
    return tagQueue.poll();
  }

  private void pollAndConsTag() {
    Tag tag = pollTag();
    if (tag != null) {
      tag.consumeTag();
    }
  }

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

  private void handleTagCtx(TagContentContext tagCtx, UnaryOperator<LabelTag> tagTagSupplier,
                            BiConsumer<LabelTag, LabelTag> tagChainTagConsumer) {
    if (tagCtx == null) {
      return;
    }

    pushTag(new Tag(labelTag -> peekAndConsTagIfAbsent(() -> tagTagSupplier.apply(labelTag),
                                                       lt -> tagChainTagConsumer.accept(lt, labelTag))));
  }
  private boolean pureText(TdContentContext tdCtx) {
    return CollectionUtils.isEmpty(tdCtx.table()) && CollectionUtils.isEmpty(tdCtx.htmlElement());
  }

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

  private static String adaptAttrValue(String attributeValue) {
    attributeValue = attributeValue.trim();
    if ((attributeValue.startsWith("'") && attributeValue.endsWith("'"))
        || (attributeValue.startsWith("\"") && attributeValue.endsWith("\""))) {
      attributeValue = attributeValue.substring(1, attributeValue.length() - 1).trim();
    }
    return attributeValue;
  }

  private static class Tag {

    private LabelTag labelTag;

    private final Consumer<LabelTag> labelTagCons;

    public Tag(Consumer<LabelTag> labelTagCons) {
      this.labelTagCons = labelTagCons;
    }

    private void consumeTag() {
      labelTagCons.accept(labelTag);
    }
  }
}
