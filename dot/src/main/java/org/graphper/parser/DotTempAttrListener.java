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

/**
 * Provides a mechanism to manage and prioritize node or edge attributes defined
 * in DOT grammar statements, taking into account hierarchical (nested) subgraphs
 * and overridden attribute statements.
 *
 * <p>This abstract listener class extends {@link DOTParserBaseListener}, focusing on
 * attribute statements for nodes or edges. By tracking the attribute blocks at different
 * nesting levels, it provides a priority template: outer-level (or global) attributes
 * apply unless explicitly overridden by inner scopes or individual elements.</p>
 *
 * <p><b>Use Case Example:</b></p>
 * <pre>{@code
 * // Example DOT script:
 * digraph G {
 *     node[shape=rect fontsize=57]
 *     a0[fontcolor=yellow]
 *
 *     subgraph cluster_0 {
 *         node[style=dashed fontcolor=red]
 *         a0[fontcolor=blue]
 *     }
 *     a0[style=dashed color=red]
 *
 *     edge[color=red dir=both]
 *
 *     subgraph cluster_1 {
 *         edge[style=dashed]
 *         a->b[penwidth=3 minlen=2]
 *     }
 *
 *     node[fontsize=20]
 *     edge[color=red]
 *     a->b
 *
 *     edge[color=blue]
 *     c->d
 *
 *     edge[style=dashed]
 *     e->f
 *
 *     subgraph cluster_2 {
 *         edge[dir=both]
 *         g->h
 *
 *         subgraph cluster_3 {
 *             edge[arrowhead=vee color=GREEN]
 *             i->j
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>The attributes declared at higher levels (e.g., <tt>node[fontsize=57]</tt>)
 * are inherited unless a deeper level (e.g., <tt>node[fontsize=20]</tt> in a deeper
 * subgraph) overrides them. This class uses a stack-like mechanism to push or pop
 * nested attribute contexts, merging attributes according to scope.</p>
 *
 * <p>Concrete implementations must provide the {@code isFocusStmtType}
 * method, indicating which statement type (e.g., node or edge) this listener should
 * focus on.</p>
 *
 * @author Jamison Jiang
 */
public abstract class DotTempAttrListener extends DOTParserBaseListener {

  /**
   * A stack-like deque that tracks nested attribute contexts. Each {@link GraphTempAttrs}
   * represents a subgraph or block of attributes.
   */
  private Deque<GraphTempAttrs> tempNodeAttrsDeque;

  /**
   * Indicates whether this listener cares about a specific attribute statement type,
   * such as node or edge statements. Concrete subclasses should override this to
   * filter statements accordingly.
   *
   * @param ctx the {@link Attr_stmtContext} from the DOT parser
   * @return {@code true} if this listener focuses on the given statement type,
   *         {@code false} otherwise
   */
  protected abstract boolean isFocusStmtType(Attr_stmtContext ctx);

  /**
   * Called when entering an attribute statement block (e.g., node[...] or edge[...] lines).
   * If {@code isFocusStmtType(ctx)} is true, merges these attributes with any existing
   * attributes from outer scopes, then pushes them onto the deque.
   */
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

  /**
   * Called when entering a subgraph. Creates a new attribute context
   * for that subgraph (which may inherit from the outer scope).
   */
  @Override
  public void enterSubgraph(SubgraphContext ctx) {
    pushTempNodeAttrs(ctx, null);
  }

  /**
   * Called when exiting a subgraph. Removes the most recent attribute context
   * from the stack, returning to the outer scope's context.
   */
  @Override
  public void exitSubgraph(SubgraphContext ctx) {
    pollTempAttrs();
  }

  /**
   * Retrieves the most recently pushed (i.e., active) map of attributes for this listener.
   *
   * @return the current map of temporary node attributes, or {@code null} if none exist
   */
  protected Map<String, String> currentTempAttrs() {
    GraphTempAttrs graphTempAttrs = peekGraphTempAttrs();
    return graphTempAttrs == null ? null : graphTempAttrs.tempNodeAttrs;
  }

  /**
   * Combines existing source attributes with additional attributes parsed
   * from a given {@link Attr_listContext}, returning the merged map.
   *
   * @param sourceAttrs the existing attribute map
   * @param attr_list   the context from which new attributes are extracted
   * @return a merged map containing source and target attributes
   */
  protected Map<String, String> combineAttrs(Map<String, String> sourceAttrs,
                                             Attr_listContext attr_list) {
    return combineAttrs(sourceAttrs, getAttrMap(attr_list));
  }

  /**
   * Combines two maps of attributes, with the first map having lower priority.
   * Any attribute not present in the target map is copied from source map.
   *
   * @param sourceAttrs lower-priority attributes
   * @param targetAttrs higher-priority attributes
   * @return a merged map of attributes
   */
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

  /**
   * Pushes a new {@link GraphTempAttrs} context onto the stack, merging
   * attributes with the current scope if necessary.
   *
   * @param subgraphContext optional subgraph context
   * @param tempAttrs       the attribute map to add
   */
  private void pushTempNodeAttrs(SubgraphContext subgraphContext, Map<String, String> tempAttrs) {
    if (tempAttrs != null && !tempAttrs.isEmpty()) {
      tempAttrs.remove(ID);
    }

    if (tempNodeAttrsDeque == null) {
      tempNodeAttrsDeque = new LinkedList<>();
    }

    GraphTempAttrs currentTempAttr = tempNodeAttrsDeque.peek();
    if (currentTempAttr != null && currentTempAttr.graphContext == subgraphContext) {
      // If the top of the stack is the same subgraph context, merge with it
      tempAttrs = combineAttrs(currentTempAttr.tempNodeAttrs, tempAttrs);
      tempNodeAttrsDeque.poll();
    } else {
      // Otherwise, merge with all existing contexts
      Iterator<GraphTempAttrs> iterator = tempNodeAttrsDeque.iterator();
      while (iterator.hasNext()) {
        currentTempAttr = iterator.next();
        tempAttrs = combineAttrs(currentTempAttr.tempNodeAttrs, tempAttrs);
      }
    }

    tempNodeAttrsDeque.push(new GraphTempAttrs(subgraphContext, tempAttrs));
  }

  /**
   * Retrieves the {@link GraphTempAttrs} at the top of the stack (if any).
   *
   * @return the top-most {@link GraphTempAttrs}, or {@code null} if the stack is empty
   */
  private GraphTempAttrs peekGraphTempAttrs() {
    return tempNodeAttrsDeque == null ? null : tempNodeAttrsDeque.peek();
  }

  /**
   * Removes and returns the top-most {@link GraphTempAttrs} from the stack.
   *
   * @return the popped attribute map, or {@code null} if the stack is empty
   */
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

  private static class GraphTempAttrs {

    private final SubgraphContext graphContext;
    private final Map<String, String> tempNodeAttrs;

    GraphTempAttrs(SubgraphContext graphContext, Map<String, String> tempNodeAttrs) {
      this.graphContext = graphContext;
      this.tempNodeAttrs = tempNodeAttrs;
    }
  }
}