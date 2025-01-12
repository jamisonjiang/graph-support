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

import java.util.Deque;
import java.util.LinkedList;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.parser.grammar.HTMLParser.HtmlAttributeContext;
import org.graphper.parser.grammar.HTMLParser.TableContext;
import org.graphper.parser.grammar.HTMLParser.TdContext;
import org.graphper.parser.grammar.HTMLParser.TrContext;
import org.graphper.parser.grammar.HTMLParserBaseListener;

public class HtmlListener extends HTMLParserBaseListener {

  private Table table;

  private Deque<Td> tdDeque = new LinkedList<>();

  @Override
  public void enterTable(TableContext ctx) {
//    System.out.println(ctx.TABLE());
  }

  @Override
  public void enterTr(TrContext ctx) {
//    System.out.println(ctx.TR());
  }

  @Override
  public void enterTd(TdContext ctx) {
//    System.out.println(ctx.TD());
  }

  @Override
  public void enterHtmlAttribute(HtmlAttributeContext ctx) {
    System.out.println(ctx.TAG_NAME().getText() + "=" + ctx.ATTVALUE_VALUE().getText());
  }

  public Table getTable() {
    return table;
  }
}
