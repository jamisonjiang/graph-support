// Generated from org\graphper\parser\grammar\DOTParser.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DOTParser}.
 */
public interface DOTParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DOTParser#graphs}.
	 * @param ctx the parse tree
	 */
	void enterGraphs(DOTParser.GraphsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#graphs}.
	 * @param ctx the parse tree
	 */
	void exitGraphs(DOTParser.GraphsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#graph}.
	 * @param ctx the parse tree
	 */
	void enterGraph(DOTParser.GraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#graph}.
	 * @param ctx the parse tree
	 */
	void exitGraph(DOTParser.GraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterStmt_list(DOTParser.Stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitStmt_list(DOTParser.Stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(DOTParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(DOTParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#attr_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAttr_stmt(DOTParser.Attr_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#attr_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAttr_stmt(DOTParser.Attr_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#attr_list}.
	 * @param ctx the parse tree
	 */
	void enterAttr_list(DOTParser.Attr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#attr_list}.
	 * @param ctx the parse tree
	 */
	void exitAttr_list(DOTParser.Attr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#graph_a_list}.
	 * @param ctx the parse tree
	 */
	void enterGraph_a_list(DOTParser.Graph_a_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#graph_a_list}.
	 * @param ctx the parse tree
	 */
	void exitGraph_a_list(DOTParser.Graph_a_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#a_list}.
	 * @param ctx the parse tree
	 */
	void enterA_list(DOTParser.A_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#a_list}.
	 * @param ctx the parse tree
	 */
	void exitA_list(DOTParser.A_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#edge_stmt}.
	 * @param ctx the parse tree
	 */
	void enterEdge_stmt(DOTParser.Edge_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#edge_stmt}.
	 * @param ctx the parse tree
	 */
	void exitEdge_stmt(DOTParser.Edge_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#edgeRHS}.
	 * @param ctx the parse tree
	 */
	void enterEdgeRHS(DOTParser.EdgeRHSContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#edgeRHS}.
	 * @param ctx the parse tree
	 */
	void exitEdgeRHS(DOTParser.EdgeRHSContext ctx);
	/**
	 * Enter a parse tree produced by the {@code directedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterDirectedEdge(DOTParser.DirectedEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code directedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitDirectedEdge(DOTParser.DirectedEdgeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code undirectedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterUndirectedEdge(DOTParser.UndirectedEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code undirectedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitUndirectedEdge(DOTParser.UndirectedEdgeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code invalidDirectedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterInvalidDirectedEdge(DOTParser.InvalidDirectedEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code invalidDirectedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitInvalidDirectedEdge(DOTParser.InvalidDirectedEdgeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code invalidUndirectedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterInvalidUndirectedEdge(DOTParser.InvalidUndirectedEdgeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code invalidUndirectedEdge}
	 * labeled alternative in {@link DOTParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitInvalidUndirectedEdge(DOTParser.InvalidUndirectedEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#node_stmt}.
	 * @param ctx the parse tree
	 */
	void enterNode_stmt(DOTParser.Node_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#node_stmt}.
	 * @param ctx the parse tree
	 */
	void exitNode_stmt(DOTParser.Node_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#node_id}.
	 * @param ctx the parse tree
	 */
	void enterNode_id(DOTParser.Node_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#node_id}.
	 * @param ctx the parse tree
	 */
	void exitNode_id(DOTParser.Node_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#port}.
	 * @param ctx the parse tree
	 */
	void enterPort(DOTParser.PortContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#port}.
	 * @param ctx the parse tree
	 */
	void exitPort(DOTParser.PortContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#subgraph}.
	 * @param ctx the parse tree
	 */
	void enterSubgraph(DOTParser.SubgraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#subgraph}.
	 * @param ctx the parse tree
	 */
	void exitSubgraph(DOTParser.SubgraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link DOTParser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(DOTParser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DOTParser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(DOTParser.Id_Context ctx);
}