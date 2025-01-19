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

import static org.graphper.parser.ParserUtils.nodeAttributes;

import java.util.HashMap;
import java.util.Map;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Node;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.Attr_stmtContext;
import org.graphper.parser.grammar.DOTParser.Node_stmtContext;

public class NodeExtractor extends DotTempAttrListener {

  private final Map<String, Node> nodeMap = new HashMap<>();

  private final Map<String, Map<String, String>> nodeStmtContextMap = new HashMap<>();

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
    Map<String, String> attrs = combineAttrs(nodeStmtContextMap.get(id), ctx);
    attrs = combineAttrs(currentTempAttrs(), attrs);
    nodeStmtContextMap.put(id, attrs);
  }

  private Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
                                           Node_stmtContext targetNodeCtx) {
    return combineAttrs(sourceAttrs, targetNodeCtx != null ? targetNodeCtx.attr_list() : null);
  }

  @Override
  protected boolean isFocusStmtType(Attr_stmtContext ctx) {
    return ctx.NODE() != null;
  }
}