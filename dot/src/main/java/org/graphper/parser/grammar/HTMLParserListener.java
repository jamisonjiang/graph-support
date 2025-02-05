// Generated from org/graphper/parser/grammar/HTMLParser.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link HTMLParser}.
 */
public interface HTMLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link HTMLParser#htmlTag}.
	 * @param ctx the parse tree
	 */
	void enterHtmlTag(HTMLParser.HtmlTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#htmlTag}.
	 * @param ctx the parse tree
	 */
	void exitHtmlTag(HTMLParser.HtmlTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#table}.
	 * @param ctx the parse tree
	 */
	void enterTable(HTMLParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#table}.
	 * @param ctx the parse tree
	 */
	void exitTable(HTMLParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#tagContent}.
	 * @param ctx the parse tree
	 */
	void enterTagContent(HTMLParser.TagContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#tagContent}.
	 * @param ctx the parse tree
	 */
	void exitTagContent(HTMLParser.TagContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#tr}.
	 * @param ctx the parse tree
	 */
	void enterTr(HTMLParser.TrContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#tr}.
	 * @param ctx the parse tree
	 */
	void exitTr(HTMLParser.TrContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#td}.
	 * @param ctx the parse tree
	 */
	void enterTd(HTMLParser.TdContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#td}.
	 * @param ctx the parse tree
	 */
	void exitTd(HTMLParser.TdContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#tdContent}.
	 * @param ctx the parse tree
	 */
	void enterTdContent(HTMLParser.TdContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#tdContent}.
	 * @param ctx the parse tree
	 */
	void exitTdContent(HTMLParser.TdContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#htmlElement}.
	 * @param ctx the parse tree
	 */
	void enterHtmlElement(HTMLParser.HtmlElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#htmlElement}.
	 * @param ctx the parse tree
	 */
	void exitHtmlElement(HTMLParser.HtmlElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#fontTag}.
	 * @param ctx the parse tree
	 */
	void enterFontTag(HTMLParser.FontTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#fontTag}.
	 * @param ctx the parse tree
	 */
	void exitFontTag(HTMLParser.FontTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#bTag}.
	 * @param ctx the parse tree
	 */
	void enterBTag(HTMLParser.BTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#bTag}.
	 * @param ctx the parse tree
	 */
	void exitBTag(HTMLParser.BTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#iTag}.
	 * @param ctx the parse tree
	 */
	void enterITag(HTMLParser.ITagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#iTag}.
	 * @param ctx the parse tree
	 */
	void exitITag(HTMLParser.ITagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#uTag}.
	 * @param ctx the parse tree
	 */
	void enterUTag(HTMLParser.UTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#uTag}.
	 * @param ctx the parse tree
	 */
	void exitUTag(HTMLParser.UTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#sTag}.
	 * @param ctx the parse tree
	 */
	void enterSTag(HTMLParser.STagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#sTag}.
	 * @param ctx the parse tree
	 */
	void exitSTag(HTMLParser.STagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#oTag}.
	 * @param ctx the parse tree
	 */
	void enterOTag(HTMLParser.OTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#oTag}.
	 * @param ctx the parse tree
	 */
	void exitOTag(HTMLParser.OTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#subTag}.
	 * @param ctx the parse tree
	 */
	void enterSubTag(HTMLParser.SubTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#subTag}.
	 * @param ctx the parse tree
	 */
	void exitSubTag(HTMLParser.SubTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#supTag}.
	 * @param ctx the parse tree
	 */
	void enterSupTag(HTMLParser.SupTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#supTag}.
	 * @param ctx the parse tree
	 */
	void exitSupTag(HTMLParser.SupTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#vtTag}.
	 * @param ctx the parse tree
	 */
	void enterVtTag(HTMLParser.VtTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#vtTag}.
	 * @param ctx the parse tree
	 */
	void exitVtTag(HTMLParser.VtTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#vbTag}.
	 * @param ctx the parse tree
	 */
	void enterVbTag(HTMLParser.VbTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#vbTag}.
	 * @param ctx the parse tree
	 */
	void exitVbTag(HTMLParser.VbTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#vcTag}.
	 * @param ctx the parse tree
	 */
	void enterVcTag(HTMLParser.VcTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#vcTag}.
	 * @param ctx the parse tree
	 */
	void exitVcTag(HTMLParser.VcTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#hlTag}.
	 * @param ctx the parse tree
	 */
	void enterHlTag(HTMLParser.HlTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#hlTag}.
	 * @param ctx the parse tree
	 */
	void exitHlTag(HTMLParser.HlTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#hrTag}.
	 * @param ctx the parse tree
	 */
	void enterHrTag(HTMLParser.HrTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#hrTag}.
	 * @param ctx the parse tree
	 */
	void exitHrTag(HTMLParser.HrTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#hcTag}.
	 * @param ctx the parse tree
	 */
	void enterHcTag(HTMLParser.HcTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#hcTag}.
	 * @param ctx the parse tree
	 */
	void exitHcTag(HTMLParser.HcTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#brTag}.
	 * @param ctx the parse tree
	 */
	void enterBrTag(HTMLParser.BrTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#brTag}.
	 * @param ctx the parse tree
	 */
	void exitBrTag(HTMLParser.BrTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#htmlAttribute}.
	 * @param ctx the parse tree
	 */
	void enterHtmlAttribute(HTMLParser.HtmlAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#htmlAttribute}.
	 * @param ctx the parse tree
	 */
	void exitHtmlAttribute(HTMLParser.HtmlAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#htmlChardata}.
	 * @param ctx the parse tree
	 */
	void enterHtmlChardata(HTMLParser.HtmlChardataContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#htmlChardata}.
	 * @param ctx the parse tree
	 */
	void exitHtmlChardata(HTMLParser.HtmlChardataContext ctx);
	/**
	 * Enter a parse tree produced by {@link HTMLParser#htmlComment}.
	 * @param ctx the parse tree
	 */
	void enterHtmlComment(HTMLParser.HtmlCommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link HTMLParser#htmlComment}.
	 * @param ctx the parse tree
	 */
	void exitHtmlComment(HTMLParser.HtmlCommentContext ctx);
}