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
import org.graphper.api.Node.NodeBuilder;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.Attr_stmtContext;
import org.graphper.parser.grammar.DOTParser.Node_stmtContext;

/**
 * Extracts information about nodes from a DOT parse tree, tracking and merging node
 * attributes found in {@code node_stmt} and {@code edge_stmt} declarations.
 *
 * <p>This class extends {@link DotTempAttrListener} to leverage its mechanism
 * for handling nested attribute scopes. It focuses on {@code node} attribute
 * statements, merging global or subgraph-level attributes with node-specific ones.
 *
 * @author Jamison Jiang
 */
public class NodeExtractor extends DotTempAttrListener {

  /** Maintains a map of node IDs to their corresponding {@link Node} instances. */
  private final Map<String, Node> nodeMap = new HashMap<>();

  /** Tracks per-node attribute maps. Each node ID maps to a combined set of attributes. */
  private final Map<String, Map<String, String>> nodeStmtContextMap = new HashMap<>();

  private final PostGraphComponents postGraphComponents;

  public NodeExtractor(PostGraphComponents postGraphComponents) {
    this.postGraphComponents = postGraphComponents;
  }

  @Override
  public void enterNode_stmt(DOTParser.Node_stmtContext ctx) {
    parseNodeAttrs(null, ctx);
  }

  @Override
  public void enterEdge_stmt(DOTParser.Edge_stmtContext ctx) {
    // The first node or subgraph in the edge statement
    ParseTree first = ctx.node_id() != null ? ctx.node_id() : ctx.subgraph();

    int edgeCount = ctx.edgeRHS().children.size() / 2;
    for (int c = 0; c < edgeCount; c++) {
      ParseTree second = ctx.edgeRHS().children.get(2 * c + 1);

      // Merge or register attributes for the left node ID
      if (first instanceof DOTParser.Node_idContext) {
        String leftId = ((DOTParser.Node_idContext) first).id_().getText();
        parseNodeAttrs(leftId, null);
      }

      // Merge or register attributes for the right node ID
      if (second instanceof DOTParser.Node_idContext) {
        String rightId = ((DOTParser.Node_idContext) second).id_().getText();
        parseNodeAttrs(rightId, null);
      }

      first = second;
    }
  }

  /**
   * Retrieves or creates a {@link Node} for the specified node ID. Attributes are
   * applied from any previously discovered node statements, plus any relevant
   * template attributes from outer scopes.
   *
   * @param nodeId the identifier of the node in the DOT script
   * @return the corresponding {@link Node}, or {@code null} if no ID is provided
   */
  public Node getNode(String nodeId) {
    if (nodeId == null) {
      return null;
    }

    return nodeMap.computeIfAbsent(nodeId, k -> {
      Node.NodeBuilder builder = Node.builder();
      builder.id(nodeId);

      // Retrieve merged attributes for this node
      Map<String, String> nodeAttrs = nodeStmtContextMap.get(k);
      if (nodeAttrs == null) {
        postNode(builder);
        return builder.build();
      }

      // Apply the node attributes
      nodeAttributes(builder, nodeAttrs);

      // Allow post-graph components to modify the node builder
      postNode(builder);

      // Build and store the Node
      return builder.build();
    });
  }

  private void postNode(NodeBuilder builder) {
    if (postGraphComponents != null) {
      postGraphComponents.postNode(builder);
    }
  }

  private void parseNodeAttrs(String id, Node_stmtContext ctx) {
    if (StringUtils.isEmpty(id) && ctx == null) {
      return;
    }

    if (ctx != null) {
      id = ctx.node_id().id_().getText();
    }
    // Merge node-specific attributes from existing context...
    Map<String, String> attrs = combineAttrs(nodeStmtContextMap.get(id), ctx);
    // ...then merge with any current scope template attributes
    attrs = combineAttrs(currentTempAttrs(), attrs);

    nodeStmtContextMap.put(id, attrs);
  }

  /**
   * Combines an existing attribute map with any attributes derived from a
   * {@code Node_stmtContext}, returning the merged result.
   *
   * @param sourceAttrs     the existing attributes to merge with
   * @param targetNodeCtx   the node statement context providing new attributes
   * @return the merged attribute map
   */
  private Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
                                           Node_stmtContext targetNodeCtx) {
    return combineAttrs(sourceAttrs, targetNodeCtx != null ? targetNodeCtx.attr_list() : null);
  }

  @Override
  protected boolean isFocusStmtType(Attr_stmtContext ctx) {
    return ctx.NODE() != null;
  }
}