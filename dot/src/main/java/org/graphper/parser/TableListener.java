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

import static org.graphper.parser.ParserUtils.setTableAttributes;
import static org.graphper.parser.ParserUtils.setTdAttributes;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.parser.grammar.TABLEParser.FontContentContext;
import org.graphper.parser.grammar.TABLEParser.HtmlAttributeContext;
import org.graphper.parser.grammar.TABLEParser.TableContext;
import org.graphper.parser.grammar.TABLEParser.TdContentContext;
import org.graphper.parser.grammar.TABLEParser.TdContext;
import org.graphper.parser.grammar.TABLEParser.TrContext;
import org.graphper.parser.grammar.TABLEParserBaseListener;
import org.graphper.util.CollectionUtils;

public class TableListener extends TABLEParserBaseListener {

  private Table table;

  private final Deque<Table> tableQueue = new LinkedList<>();

  private final Deque<List<Td>> tdQueue = new LinkedList<>();

  @Override
  public void enterTable(TableContext ctx) {
    Table t = Html.table();
    List<Td> tds = tdQueue.peek();
    if (CollectionUtils.isNotEmpty(tds)) {
      Td td = tds.get(tds.size() - 1);
      td.table(t);
    }

    pushTable(t);
    if (table == null) {
      table = t;
    }
  }

  @Override
  public void exitTable(TableContext ctx) {
    popCurrentTable();
  }

  @Override
  public void enterTr(TrContext ctx) {
    tdQueue.push(new ArrayList<>());
  }

  @Override
  public void exitTr(TrContext ctx) {
    List<Td> tds = tdQueue.peek();
    if (CollectionUtils.isEmpty(tds)) {
      throw new ParseException("<tr> not contains any <td>");
    }
    curretTable().tr(tds.toArray(new Td[0]));
    tdQueue.poll();
  }

  @Override
  public void enterTd(TdContext ctx) {
    List<Td> tds = tdQueue.peek();
    if (tds == null) {
      throw new ParseException("<td> not belong to any <tr>");
    }

    Td td = Html.td();
    tds.add(td);

    TdContentContext content = ctx.tdContent();
    if (content != null && CollectionUtils.isEmpty(content.table())) {
      td.text(content.getText());
      return;
    }
  }

  @Override
  public void enterFontContent(FontContentContext ctx) {

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

    attributeValue = attributeValue.trim();
    if ((attributeValue.startsWith("'") && attributeValue.endsWith("'"))
        || (attributeValue.startsWith("\"") && attributeValue.endsWith("\""))) {
      attributeValue = attributeValue.substring(1, attributeValue.length() - 1).trim();
    }

    ParserRuleContext parent = ctx.getParent();
    if (parent instanceof TdContext) {
      setTdAttributes(currentTd(), ctx.TAG_NAME().getText(), attributeValue);
    }
    if (parent instanceof TableContext) {
      setTableAttributes(curretTable(), ctx.TAG_NAME().getText(), attributeValue);
    }
  }

  public Table getTable() {
    return table;
  }

  private Td currentTd() {
    List<Td> tds = tdQueue.peek();
    if (CollectionUtils.isEmpty(tds)) {
      throw new ParseException("Cannot found current td");
    }
    return tds.get(tds.size() - 1);
  }

  private Table curretTable() {
    Table t = tableQueue.peek();
    if (t == null) {
      throw new ParseException("Cannot found current table");
    }
    return t;
  }

  private void popCurrentTable() {
    tableQueue.pop();
  }

  private void pushTable(Table table) {
    tableQueue.push(table);
  }
}
