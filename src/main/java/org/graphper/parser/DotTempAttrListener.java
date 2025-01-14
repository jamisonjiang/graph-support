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

import static org.graphper.draw.svg.SvgConstants.ID;
import static org.graphper.parser.ParserUtils.getAttrMap;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.Attr_listContext;
import org.graphper.parser.grammar.DOTParser.Attr_stmtContext;
import org.graphper.parser.grammar.DOTParser.SubgraphContext;
import org.graphper.parser.grammar.DOTParserBaseListener;

public abstract class DotTempAttrListener extends DOTParserBaseListener {

  private Deque<GraphTempAttrs> tempNodeAttrsDeque;

  protected abstract boolean isFocusStmtType(Attr_stmtContext ctx);

  @Override
  public void enterAttr_stmt(DOTParser.Attr_stmtContext ctx) {
    if (!isFocusStmtType(ctx)) {
      return;
    }

    GraphTempAttrs graphTempAttrs = peekGraphTempAttrs();
    if (graphTempAttrs == null) {
      pushTempNodeAttrs(null, getAttrMap(ctx.attr_list()));
    } else {
      pushTempNodeAttrs(graphTempAttrs.graphContext, getAttrMap(ctx.attr_list()));
    }
  }

  @Override
  public void enterSubgraph(SubgraphContext ctx) {
    pushTempNodeAttrs(ctx, null);
  }

  @Override
  public void exitSubgraph(SubgraphContext ctx) {
    pollTempAttrs();
  }

  protected Map<String, String> currentTempAttrs() {
    GraphTempAttrs graphTempAttrs = peekGraphTempAttrs();
    return graphTempAttrs == null ? null : graphTempAttrs.tempNodeAttrs;
  }

  protected Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
                                             Attr_listContext attr_list) {
    return combineAttrs(sourceAttrs, getAttrMap(attr_list));
  }

  protected static Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
                                                    Map<String, String> targetAttrs) {
    if (sourceAttrs == null || sourceAttrs.isEmpty()) {
      return targetAttrs;
    }

    if (targetAttrs == null || targetAttrs.isEmpty()) {
      return new HashMap<>(sourceAttrs);
    }

    sourceAttrs.forEach(targetAttrs::putIfAbsent);
    return targetAttrs;
  }

  private void pushTempNodeAttrs(SubgraphContext subgraphContext, Map<String, String> tempAttrs) {
    if (tempAttrs != null && !tempAttrs.isEmpty()) {
      tempAttrs.remove(ID);
    }

    if (tempNodeAttrsDeque == null) {
      tempNodeAttrsDeque = new LinkedList<>();
    }

    GraphTempAttrs currentTempAttr = tempNodeAttrsDeque.peek();
    if (currentTempAttr != null && currentTempAttr.graphContext == subgraphContext) {
      // Same graph node template attributes are combined
      tempAttrs = combineAttrs(currentTempAttr.tempNodeAttrs, tempAttrs);
      tempNodeAttrsDeque.poll();
    } else {
      Iterator<GraphTempAttrs> iterator = tempNodeAttrsDeque.iterator();
      while (iterator.hasNext()) {
        currentTempAttr = iterator.next();
        tempAttrs = combineAttrs(currentTempAttr.tempNodeAttrs, tempAttrs);
      }
    }

    tempNodeAttrsDeque.push(new GraphTempAttrs(subgraphContext, tempAttrs));
  }

  private GraphTempAttrs peekGraphTempAttrs() {
    return tempNodeAttrsDeque == null ? null : tempNodeAttrsDeque.peek();
  }

  private Map<String, String> pollTempAttrs() {
    if (tempNodeAttrsDeque == null) {
      return null;
    }
    GraphTempAttrs graphTempAttrs = tempNodeAttrsDeque.poll();
    if (graphTempAttrs == null) {
      return null;
    }
    return graphTempAttrs.tempNodeAttrs;
  }

  static class GraphTempAttrs {

    private final SubgraphContext graphContext;

    private final Map<String, String> tempNodeAttrs;

    GraphTempAttrs(SubgraphContext graphContext, Map<String, String> tempNodeAttrs) {
      this.graphContext = graphContext;
      this.tempNodeAttrs = tempNodeAttrs;
    }

    public SubgraphContext getGraphContext() {
      return graphContext;
    }

    public Map<String, String> getTempNodeAttrs() {
      return tempNodeAttrs;
    }
  }
}
