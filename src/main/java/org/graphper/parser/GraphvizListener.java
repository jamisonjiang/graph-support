package org.graphper.parser;

import static org.graphper.parser.ParserUtils.clusterAttributes;
import static org.graphper.parser.ParserUtils.graphAttributes;
import static org.graphper.parser.ParserUtils.lineAttributes;
import static org.graphper.parser.ParserUtils.nodeAttributes;
import static org.graphper.parser.ParserUtils.subgraphAttribute;
import static org.graphper.parser.ParserUtils.subgraphAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.parser.grammar.DOTBaseListener;
import org.graphper.parser.grammar.DOTParser;
import org.graphper.parser.grammar.DOTParser.PortContext;

public class GraphvizListener extends DOTBaseListener {

    private Stack<GraphContainer.GraphContainerBuilder> containerStack = new Stack();

    private Map<String, Node> nodeMap = new HashMap<>();

    private Graphviz graphviz;

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
            ParseTree edgeop = ctx.edgeRHS().children.get(2 * c);
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

            buildLine(attr_list, leftNode, rightNode);

        } else if (first instanceof DOTParser.SubgraphContext && second instanceof DOTParser.Node_idContext) {

            DOTParser.SubgraphContext left = (DOTParser.SubgraphContext)first;
            DOTParser.Node_idContext right = (DOTParser.Node_idContext)second;

            String rightId = right.id_().getText();
            Node rightNode = nodeMap.get(rightId);

            subgraphNodes(left).forEach(l -> buildLine(attr_list, l, rightNode));

        } else if (first instanceof DOTParser.Node_idContext && second instanceof DOTParser.SubgraphContext) {

            DOTParser.Node_idContext left = (DOTParser.Node_idContext)first;
            DOTParser.SubgraphContext right = (DOTParser.SubgraphContext)second;

            String leftId = left.id_().getText();
            Node leftNode = nodeMap.get(leftId);

            subgraphNodes(right).forEach(r -> buildLine(attr_list, leftNode, r));

        } else if (first instanceof DOTParser.SubgraphContext && second instanceof DOTParser.SubgraphContext) {

            DOTParser.SubgraphContext left = (DOTParser.SubgraphContext)first;
            DOTParser.SubgraphContext right = (DOTParser.SubgraphContext)second;

            subgraphNodes(left).forEach(l ->
                subgraphNodes(right).forEach(r ->
                    buildLine(attr_list, l, r)));

        }
    }

    private List<Node> subgraphNodes(DOTParser.SubgraphContext sg) {

        List<Node> nodes = new ArrayList<>();

        if (sg.stmt_list() != null) {
            for (DOTParser.StmtContext stmt : sg.stmt_list().stmt()) {

                if (stmt.node_stmt() != null) {
                    String rightId = stmt.node_stmt().node_id().id_().getText();
                    nodes.add(nodeMap.get(rightId));
                }
                if (stmt.subgraph() != null) {
                    nodes.addAll(subgraphNodes(stmt.subgraph()));
                }
            }
        }

        return nodes;
    }

    private void buildLine(DOTParser.Attr_listContext attr_list, Node leftNode, Node rightNode) {
        Line.LineBuilder builder = Line.builder(leftNode, rightNode);
        lineAttributes(attr_list, builder);
        Line line = builder.build();
        containerStack.peek().addLine(line);
    }

    @Override
    public void enterNode_stmt(DOTParser.Node_stmtContext ctx) {
        String id = ctx.node_id().id_().getText();
        Node.NodeBuilder builder = Node.builder();
        builder.id(id);
        builder.label(id);

        nodeAttributes(ctx.attr_list(), builder);

        Node n = builder.build();
        nodeMap.put(id, n);

        containerStack.peek().addNode(n);
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
    }

    @Override
    public void enterPort(PortContext ctx) {
       ParserRuleContext parent = ctx.getParent();
    }

    public Graphviz getGraphviz() {
        return graphviz;
    }
}
