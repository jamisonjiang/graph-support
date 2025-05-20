package org.graphper.parser.grammar;
// Generated from /antlr4/org/graphper/parser/grammar/HTMLParser.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link HTMLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface HTMLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link HTMLParser#htmlTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlTag(HTMLParser.HtmlTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable(HTMLParser.TableContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#tagContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagContent(HTMLParser.TagContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#tr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTr(HTMLParser.TrContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#td}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTd(HTMLParser.TdContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#tdContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTdContent(HTMLParser.TdContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#htmlElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlElement(HTMLParser.HtmlElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#fontTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFontTag(HTMLParser.FontTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#bTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBTag(HTMLParser.BTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#iTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitITag(HTMLParser.ITagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#uTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUTag(HTMLParser.UTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#sTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSTag(HTMLParser.STagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#oTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOTag(HTMLParser.OTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#subTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubTag(HTMLParser.SubTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#supTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSupTag(HTMLParser.SupTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#vtTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVtTag(HTMLParser.VtTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#vbTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVbTag(HTMLParser.VbTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#vcTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVcTag(HTMLParser.VcTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#hlTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHlTag(HTMLParser.HlTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#hrTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHrTag(HTMLParser.HrTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#hcTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHcTag(HTMLParser.HcTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#brTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBrTag(HTMLParser.BrTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#htmlAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlAttribute(HTMLParser.HtmlAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#htmlChardata}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlChardata(HTMLParser.HtmlChardataContext ctx);
	/**
	 * Visit a parse tree produced by {@link HTMLParser#htmlComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlComment(HTMLParser.HtmlCommentContext ctx);
}