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

import static org.graphper.parser.ParserUtils.clusterAttributes;
import static org.graphper.parser.ParserUtils.graphAttributes;
import static org.graphper.parser.ParserUtils.lineAttributes;
import static org.graphper.parser.ParserUtils.nodeAttributes;
import static org.graphper.parser.ParserUtils.setLinePort;
import static org.graphper.parser.ParserUtils.subgraphAttribute;
import static org.graphper.parser.ParserUtils.subgraphAttributes;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.GraphContainer.GraphContainerBuilder;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Html;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.api.Subgraph;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.Node_idContext;
import org.graphper.parser.grammar.DOTParser.Node_stmtContext;
import org.graphper.parser.grammar.DOTParser.PortContext;
import org.graphper.parser.grammar.DOTParser.SubgraphContext;
import org.graphper.parser.grammar.DOTParser.TableContext;
import org.graphper.parser.grammar.DOTParser.Table_tdContext;
import org.graphper.parser.grammar.DOTParser.Table_trContext;
import org.graphper.parser.grammar.DOTParser.Td_dataContext;
import org.graphper.parser.grammar.DOTParserBaseListener;

public class GraphvizListener extends DOTParserBaseListener {

    private final Deque<GraphContainerBuilder> containerStack = new LinkedList<>();

    private final Deque<Consumer<Table>> tableConsumerStack = new LinkedList<>();

    private Map<String, Node> nodeMap = new HashMap<>();

    private Map<SubgraphContext, GraphContainer> subGraphMap = null;

    private Graphviz graphviz;

    private NodeBuilder currentNodeBuilder;

    @Override
    public void enterGraph(DOTParser.GraphContext ctx) {

        Graphviz.GraphvizBuilder graphvizBuilder;

        if (ctx.GRAPH() != null) {
            graphvizBuilder = Graphviz.graph();
        } else if (ctx.DIGRAPH() != null) {
            graphvizBuilder = Graphviz.digraph();
        } else {
            throw new ParseException("invalid graph");
        }

        if (ctx.id_() != null) {
            graphvizBuilder.id(ctx.id_().getText());
        }
        containerStack.push(graphvizBuilder);
    }

    @Override
    public void exitGraph(DOTParser.GraphContext ctx) {
        graphviz = (Graphviz)containerStack.pop().build();
    }

    @Override
    public void enterStmt(DOTParser.StmtContext ctx) {

        int scount = ctx.id_().size() / 2;
        for (int c = 0; c < scount; c++) {

            DOTParser.Id_Context left = ctx.id_().get(2 * c);
            DOTParser.Id_Context right = ctx.id_().get(2 * c + 1);

            String key = left.getText();
            String value = right.getText();

            if (containerStack.peek() instanceof Subgraph.SubgraphBuilder) {
                Subgraph.SubgraphBuilder sb = (Subgraph.SubgraphBuilder) containerStack.peek();

                subgraphAttribute(key, value, sb);
            } else if (containerStack.peek() instanceof Cluster.ClusterBuilder) {
                Cluster.ClusterBuilder cb = (Cluster.ClusterBuilder) containerStack.pop();

                if (containerStack.peek() instanceof Subgraph.SubgraphBuilder) {
                    Subgraph.SubgraphBuilder sb = (Subgraph.SubgraphBuilder) containerStack.peek();
                    subgraphAttribute(key, value, sb);
                }
                containerStack.push(cb);
            }
        }
    }

    @Override
    public void enterAttr_stmt(DOTParser.Attr_stmtContext ctx) {

        if (ctx.EDGE() != null) {

            Line.LineBuilder l = Line.tempLine();

            lineAttributes(ctx.attr_list(), l);
            containerStack.peek().tempLine(l.build());

        } else if (ctx.NODE() != null) {

            Node.NodeBuilder l = Node.builder();
            nodeAttributes(ctx.attr_list(), l);
            containerStack.peek().tempNode(l.build());

        } else if (ctx.GRAPH() != null) {
            if (containerStack.peek() instanceof GraphvizBuilder) {
                GraphvizBuilder gb = (GraphvizBuilder) containerStack.peek();
                graphAttributes(ctx.attr_list(), gb);
            } else if (containerStack.peek() instanceof Subgraph.SubgraphBuilder) {
                Subgraph.SubgraphBuilder sb = (Subgraph.SubgraphBuilder) containerStack.peek();
                subgraphAttributes(ctx.attr_list(), sb);
            } else if (containerStack.peek() instanceof Cluster.ClusterBuilder) {
                Cluster.ClusterBuilder sb = (Cluster.ClusterBuilder) containerStack.peek();
                clusterAttributes(ctx.attr_list(), sb);
            }
        }
        else {
            throw new ParseException("invalid attr_stmt");
        }
    }

    @Override
    public void enterGraph_a_list(DOTParser.Graph_a_listContext ctx) {
        if (containerStack.peek() instanceof GraphvizBuilder) {
            GraphvizBuilder gb = (GraphvizBuilder) containerStack.peek();
            graphAttributes(ctx.a_list(), gb);
        } else if (containerStack.peek() instanceof Subgraph.SubgraphBuilder) {
            Subgraph.SubgraphBuilder sb = (Subgraph.SubgraphBuilder) containerStack.peek();
            subgraphAttributes(ctx.a_list(), sb);
        } else if (containerStack.peek() instanceof Cluster.ClusterBuilder) {
            Cluster.ClusterBuilder sb = (Cluster.ClusterBuilder) containerStack.peek();
            clusterAttributes(ctx.a_list(), sb);
        }
    }

    @Override
    public void exitEdge_stmt(DOTParser.Edge_stmtContext ctx) {

        ParseTree first = ctx.node_id() != null ? ctx.node_id() : ctx.subgraph();

        int edgecount = ctx.edgeRHS().children.size() / 2;
        for (int c = 0; c < edgecount; c++) {
            ParseTree second = ctx.edgeRHS().children.get(2 * c + 1);

            if (first instanceof DOTParser.Node_idContext) {
                String leftId = ((DOTParser.Node_idContext) first).id_().getText();
                nodeMap.putIfAbsent(leftId, Node.builder().id(leftId).label(leftId).build());
            }

            if (second instanceof DOTParser.Node_idContext) {
                String rightId = ((DOTParser.Node_idContext) second).id_().getText();
                nodeMap.putIfAbsent(rightId, Node.builder().id(rightId).label(rightId).build());
            }

            edge(first, second, ctx.attr_list());
            first = second;
        }
    }

    private void edge(ParseTree first, ParseTree second, DOTParser. Attr_listContext attr_list) {

        if (first instanceof DOTParser.Node_idContext && second instanceof DOTParser.Node_idContext) {

            DOTParser.Node_idContext left = (DOTParser.Node_idContext)first;
            DOTParser.Node_idContext right = (DOTParser.Node_idContext)second;

            String leftId = left.id_().getText();
            String rightId = right.id_().getText();

            Node leftNode = nodeMap.get(leftId);
            Node rightNode = nodeMap.get(rightId);

            buildLine(attr_list, leftNode, rightNode, left, right);

        } else if (first instanceof DOTParser.SubgraphContext && second instanceof DOTParser.Node_idContext) {

            DOTParser.SubgraphContext left = (DOTParser.SubgraphContext)first;
            DOTParser.Node_idContext right = (DOTParser.Node_idContext)second;

            String rightId = right.id_().getText();
            Node rightNode = nodeMap.get(rightId);

            subgraphNodes(left).forEach(l -> buildLine(attr_list, l, rightNode, null, right));

        } else if (first instanceof DOTParser.Node_idContext && second instanceof DOTParser.SubgraphContext) {

            DOTParser.Node_idContext left = (DOTParser.Node_idContext)first;
            DOTParser.SubgraphContext right = (DOTParser.SubgraphContext)second;

            String leftId = left.id_().getText();
            Node leftNode = nodeMap.get(leftId);

            subgraphNodes(right).forEach(r -> buildLine(attr_list, leftNode, r, left, null));

        } else if (first instanceof DOTParser.SubgraphContext && second instanceof DOTParser.SubgraphContext) {

            DOTParser.SubgraphContext left = (DOTParser.SubgraphContext)first;
            DOTParser.SubgraphContext right = (DOTParser.SubgraphContext)second;

            subgraphNodes(left).forEach(l ->
                subgraphNodes(right).forEach(r ->
                    buildLine(attr_list, l, r, null, null)));

        }
    }

    private Iterable<Node> subgraphNodes(DOTParser.SubgraphContext sg) {
        if (subGraphMap == null) {
            throw new IllegalStateException("Cannot found subgraph when edge endpoints are subgraph/cluster");
        }

        GraphContainer container = subGraphMap.get(sg);
        if (container == null) {
            throw new IllegalStateException("Cannot found subgraph container");
        }
        return container.nodes();
    }

    private void buildLine(DOTParser.Attr_listContext attr_list, Node leftNode, Node rightNode,
                           Node_idContext leftCtx, Node_idContext rightCtx) {
        Line.LineBuilder builder = Line.builder(leftNode, rightNode);
        lineAttributes(attr_list, builder);
        setLinePort(builder, port(leftCtx, true), port(leftCtx, false), true);
        setLinePort(builder, port(rightCtx, true), port(rightCtx, false), false);
        Line line = builder.build();
        containerStack.peek().addLine(line);
    }

    private String port(Node_idContext node, boolean first) {
        if (node == null) {
            return null;
        }

        PortContext port = node.port();
        if (port == null || port.getChildCount() < 2) {
            return null;
        }
        if (first) {
            ParseTree p = port.getChild(1);
            return p != null ? p.getText() : null;
        }

        if (port.getChildCount() < 4) {
            return null;
        }
        ParseTree p = port.getChild(3);
        return p != null ? p.getText() : null;
    }

    @Override
    public void enterNode_stmt(DOTParser.Node_stmtContext ctx) {
        String id = ctx.node_id().id_().getText();
        Node.NodeBuilder builder = Node.builder();
        builder.id(id);
        nodeAttributes(ctx.attr_list(), builder);
        currentNodeBuilder = builder;
        tableConsumerStack.push(builder::table);
    }

    @Override
    public void exitNode_stmt(Node_stmtContext ctx) {
        String id = ctx.node_id().id_().getText();
        Node n = currentNodeBuilder.build();
        nodeMap.put(id, n);
        containerStack.peek().addNode(n);
        currentNodeBuilder = null;
        tableConsumerStack.pop();
    }

    @Override
    public void enterSubgraph(DOTParser.SubgraphContext ctx) {

        String id = ctx.id_() != null ? ctx.id_().getText() : null;

        if (id == null) {
            Subgraph.SubgraphBuilder builder = Subgraph.builder();
            containerStack.push(builder);
        } else if (id.startsWith("cluster")) {
            Cluster.ClusterBuilder builder = Cluster.builder();
            containerStack.push(builder);
        } else {
            Subgraph.SubgraphBuilder builder = Subgraph.builder();
            containerStack.push(builder);
        }
    }

    @Override
    public void exitSubgraph(DOTParser.SubgraphContext ctx) {

        GraphContainer.GraphContainerBuilder child = containerStack.pop();
        GraphContainer.GraphContainerBuilder parent = containerStack.peek();

        GraphContainer gc = child.build();
        if (gc.isCluster()) {
            parent.cluster((Cluster) gc);
        } else if (gc.isSubgraph()) {
            parent.subgraph((Subgraph) gc);
        }

        if (subGraphMap == null) {
            subGraphMap = new HashMap<>();
        }
        subGraphMap.put(ctx, gc);
    }

    @Override
    public void enterTable(TableContext ctx) {
        Table table = Html.table();
        tableConsumerStack.peek().accept(table);

        for (Table_trContext tr : ctx.table_tr()) {
            Td[] tds = new Td[tr.table_td().size()];
            for (int i = 0; i < tr.table_td().size(); i++) {
                Td td = Html.td();
                tds[i] = td;

                Table_tdContext tdCtx = tr.table_td(i);
                Td_dataContext tdData = tdCtx.td_data();
                if (tdData == null) {
                    continue;
                }

                if (tdData.TAG_TEXT() != null) {
                    td.text(tdData.TAG_TEXT().getText());
                    continue;
                }
//
//                if (tdData.table() != null) {
//                    tableConsumerStack.push(td::table);
//                    enterTable(tdData.table());
//                    tableConsumerStack.pop();
//                }
            }

            table.tr(tds);
        }
    }

    public Graphviz getGraphviz() {
        return graphviz;
    }
}
