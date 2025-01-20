// Generated from org\graphper\parser\grammar\TABLEParser.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TABLEParser}.
 */
public interface TABLEParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TABLEParser#table}.
	 * @param ctx the parse tree
	 */
	void enterTable(TABLEParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#table}.
	 * @param ctx the parse tree
	 */
	void exitTable(TABLEParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#tr}.
	 * @param ctx the parse tree
	 */
	void enterTr(TABLEParser.TrContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#tr}.
	 * @param ctx the parse tree
	 */
	void exitTr(TABLEParser.TrContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#td}.
	 * @param ctx the parse tree
	 */
	void enterTd(TABLEParser.TdContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#td}.
	 * @param ctx the parse tree
	 */
	void exitTd(TABLEParser.TdContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#htmlContent}.
	 * @param ctx the parse tree
	 */
	void enterHtmlContent(TABLEParser.HtmlContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#htmlContent}.
	 * @param ctx the parse tree
	 */
	void exitHtmlContent(TABLEParser.HtmlContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#htmlAttribute}.
	 * @param ctx the parse tree
	 */
	void enterHtmlAttribute(TABLEParser.HtmlAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#htmlAttribute}.
	 * @param ctx the parse tree
	 */
	void exitHtmlAttribute(TABLEParser.HtmlAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#htmlChardata}.
	 * @param ctx the parse tree
	 */
	void enterHtmlChardata(TABLEParser.HtmlChardataContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#htmlChardata}.
	 * @param ctx the parse tree
	 */
	void exitHtmlChardata(TABLEParser.HtmlChardataContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#htmlComment}.
	 * @param ctx the parse tree
	 */
	void enterHtmlComment(TABLEParser.HtmlCommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#htmlComment}.
	 * @param ctx the parse tree
	 */
	void exitHtmlComment(TABLEParser.HtmlCommentContext ctx);
}