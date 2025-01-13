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
import static org.graphper.parser.ParserUtils.setLinePort;
import static org.graphper.parser.ParserUtils.subgraphAttribute;
import static org.graphper.parser.ParserUtils.subgraphAttributes;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import org.antlr.v4.runtime.tree.ParseTree;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.GraphContainer.GraphContainerBuilder;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.Node_idContext;
import org.graphper.parser.grammar.DOTParser.Node_stmtContext;
import org.graphper.parser.grammar.DOTParser.PortContext;
import org.graphper.parser.grammar.DOTParser.SubgraphContext;
import org.graphper.parser.grammar.DOTParserBaseListener;

public class GraphvizListener extends DOTParserBaseListener {

    private final Deque<GraphContainerBuilder> containerStack = new LinkedList<>();

    private Map<SubgraphContext, GraphContainer> subGraphMap = null;

    private Graphviz graphviz;

    private final NodeExtractor nodeExtractor;

    public GraphvizListener(NodeExtractor nodeExtractor) {
        Objects.requireNonNull(nodeExtractor);
        this.nodeExtractor = nodeExtractor;
    }

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

        }
        if (ctx.GRAPH() != null) {
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

            Node leftNode = getNode(leftId);
            Node rightNode = getNode(rightId);

            buildLine(attr_list, leftNode, rightNode, left, right);

        } else if (first instanceof DOTParser.SubgraphContext && second instanceof DOTParser.Node_idContext) {

            DOTParser.SubgraphContext left = (DOTParser.SubgraphContext)first;
            DOTParser.Node_idContext right = (DOTParser.Node_idContext)second;

            String rightId = right.id_().getText();
            Node rightNode = getNode(rightId);

            subgraphNodes(left).forEach(l -> buildLine(attr_list, l, rightNode, null, right));

        } else if (first instanceof DOTParser.Node_idContext && second instanceof DOTParser.SubgraphContext) {

            DOTParser.Node_idContext left = (DOTParser.Node_idContext)first;
            DOTParser.SubgraphContext right = (DOTParser.SubgraphContext)second;

            String leftId = left.id_().getText();
            Node leftNode = getNode(leftId);

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
    public void exitNode_stmt(Node_stmtContext ctx) {
        String id = ctx.node_id().id_().getText();
        containerStack.peek().addNode(getNode(id));
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

    public Graphviz getGraphviz() {
        return graphviz;
    }

    private Node getNode(String nodeId) {
        return nodeExtractor.getNode(nodeId);
    }
}
