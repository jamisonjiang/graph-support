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
	 * Enter a parse tree produced by {@link TABLEParser#tdContent}.
	 * @param ctx the parse tree
	 */
	void enterTdContent(TABLEParser.TdContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#tdContent}.
	 * @param ctx the parse tree
	 */
	void exitTdContent(TABLEParser.TdContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#fontContent}.
	 * @param ctx the parse tree
	 */
	void enterFontContent(TABLEParser.FontContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#fontContent}.
	 * @param ctx the parse tree
	 */
	void exitFontContent(TABLEParser.FontContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#htmlElement}.
	 * @param ctx the parse tree
	 */
	void enterHtmlElement(TABLEParser.HtmlElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#htmlElement}.
	 * @param ctx the parse tree
	 */
	void exitHtmlElement(TABLEParser.HtmlElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#fontTag}.
	 * @param ctx the parse tree
	 */
	void enterFontTag(TABLEParser.FontTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#fontTag}.
	 * @param ctx the parse tree
	 */
	void exitFontTag(TABLEParser.FontTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#bTag}.
	 * @param ctx the parse tree
	 */
	void enterBTag(TABLEParser.BTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#bTag}.
	 * @param ctx the parse tree
	 */
	void exitBTag(TABLEParser.BTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#iTag}.
	 * @param ctx the parse tree
	 */
	void enterITag(TABLEParser.ITagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#iTag}.
	 * @param ctx the parse tree
	 */
	void exitITag(TABLEParser.ITagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#uTag}.
	 * @param ctx the parse tree
	 */
	void enterUTag(TABLEParser.UTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#uTag}.
	 * @param ctx the parse tree
	 */
	void exitUTag(TABLEParser.UTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#subTag}.
	 * @param ctx the parse tree
	 */
	void enterSubTag(TABLEParser.SubTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#subTag}.
	 * @param ctx the parse tree
	 */
	void exitSubTag(TABLEParser.SubTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#supTag}.
	 * @param ctx the parse tree
	 */
	void enterSupTag(TABLEParser.SupTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#supTag}.
	 * @param ctx the parse tree
	 */
	void exitSupTag(TABLEParser.SupTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#brTag}.
	 * @param ctx the parse tree
	 */
	void enterBrTag(TABLEParser.BrTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#brTag}.
	 * @param ctx the parse tree
	 */
	void exitBrTag(TABLEParser.BrTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#hrTag}.
	 * @param ctx the parse tree
	 */
	void enterHrTag(TABLEParser.HrTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#hrTag}.
	 * @param ctx the parse tree
	 */
	void exitHrTag(TABLEParser.HrTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#imgTag}.
	 * @param ctx the parse tree
	 */
	void enterImgTag(TABLEParser.ImgTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#imgTag}.
	 * @param ctx the parse tree
	 */
	void exitImgTag(TABLEParser.ImgTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#centerTag}.
	 * @param ctx the parse tree
	 */
	void enterCenterTag(TABLEParser.CenterTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#centerTag}.
	 * @param ctx the parse tree
	 */
	void exitCenterTag(TABLEParser.CenterTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TABLEParser#tableTag}.
	 * @param ctx the parse tree
	 */
	void enterTableTag(TABLEParser.TableTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TABLEParser#tableTag}.
	 * @param ctx the parse tree
	 */
	void exitTableTag(TABLEParser.TableTagContext ctx);
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