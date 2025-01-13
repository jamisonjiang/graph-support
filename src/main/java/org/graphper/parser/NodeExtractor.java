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
import static org.graphper.parser.ParserUtils.nodeAttributes;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Node;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.Node_stmtContext;
import org.graphper.parser.grammar.DOTParser.SubgraphContext;
import org.graphper.parser.grammar.DOTParserBaseListener;

public class NodeExtractor extends DOTParserBaseListener {

  private Deque<GraphTempAttrs> tempNodeAttrsDeque;

  private final Map<String, Node> nodeMap = new HashMap<>();

  private final Map<String, Map<String, String>> nodeStmtContextMap = new HashMap<>();

  @Override
  public void enterAttr_stmt(DOTParser.Attr_stmtContext ctx) {
    if (ctx.NODE() == null) {
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
    pollTempNodeAttrs(ctx);
  }

  @Override
  public void enterNode_stmt(DOTParser.Node_stmtContext ctx) {
    parseNodeAttrs(null, ctx);
  }

  @Override
  public void enterEdge_stmt(DOTParser.Edge_stmtContext ctx) {
    ParseTree first = ctx.node_id() != null ? ctx.node_id() : ctx.subgraph();

    int edgecount = ctx.edgeRHS().children.size() / 2;
    for (int c = 0; c < edgecount; c++) {
      ParseTree second = ctx.edgeRHS().children.get(2 * c + 1);

      if (first instanceof DOTParser.Node_idContext) {
        String leftId = ((DOTParser.Node_idContext) first).id_().getText();
        parseNodeAttrs(leftId, null);
      }

      if (second instanceof DOTParser.Node_idContext) {
        String rightId = ((DOTParser.Node_idContext) second).id_().getText();
        parseNodeAttrs(rightId, null);
      }

      first = second;
    }

  }

  public Node getNode(String nodeId) {
    if (StringUtils.isEmpty(nodeId)) {
      return null;
    }

    return nodeMap.computeIfAbsent(nodeId, k -> {
      Node.NodeBuilder builder = Node.builder();
      builder.id(nodeId);
      Map<String, String> nodeAttrs = nodeStmtContextMap.get(k);
      if (nodeAttrs == null) {
        return builder.build();
      }

      nodeAttributes(builder, nodeAttrs);
      return builder.build();
    });
  }

  private void parseNodeAttrs(String id, Node_stmtContext ctx) {
    if (StringUtils.isEmpty(id) && ctx == null) {
      return;
    }

    if (ctx != null) {
      id = ctx.node_id().id_().getText();
    }
    Map<String, String> attrs = combineAttrs(peekTempNodeAttrs(), ctx);
    attrs = combineAttrs(nodeStmtContextMap.get(id), attrs);
    nodeStmtContextMap.put(id, attrs);
  }

  private Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
                                           Node_stmtContext targetNodeCtx) {
    Map<String, String> targetAttrs = null;
    if (targetNodeCtx != null) {
      targetAttrs = getAttrMap(targetNodeCtx.attr_list());
    }
    return combineAttrs(sourceAttrs, targetAttrs);
  }

  private static Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
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

  private Map<String, String> peekTempNodeAttrs() {
    GraphTempAttrs graphTempAttrs = peekGraphTempAttrs();
    return graphTempAttrs == null ? null : graphTempAttrs.tempNodeAttrs;
  }

  private Map<String, String> pollTempNodeAttrs(ParserRuleContext context) {
    if (tempNodeAttrsDeque == null) {
      return null;
    }
    GraphTempAttrs graphTempAttrs = tempNodeAttrsDeque.poll();
    if (graphTempAttrs == null || graphTempAttrs.graphContext != context) {
      return null;
    }
    return graphTempAttrs.tempNodeAttrs;
  }

  private static class GraphTempAttrs {

    private final SubgraphContext graphContext;

    private final Map<String, String> tempNodeAttrs;

    GraphTempAttrs(SubgraphContext graphContext, Map<String, String> tempNodeAttrs) {
      this.graphContext = graphContext;
      this.tempNodeAttrs = tempNodeAttrs;
    }
  }
}