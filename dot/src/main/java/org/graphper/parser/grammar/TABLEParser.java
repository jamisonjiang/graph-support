// Generated from org\graphper\parser\grammar\TABLEParser.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;

import java.util.List;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TABLEParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HTML_CONDITIONAL_COMMENT=1, WS=2, TAG_OPEN=3, HTML_TEXT=4, TABLE=5, TR=6, 
		TD=7, COMMA=8, SEMI_COLON=9, TAG_CLOSE=10, TAG_SLASH_CLOSE=11, TAG_SLASH=12, 
		TAG_EQUALS=13, TAG_NAME=14, TAG_WHITESPACE=15, ATTVALUE_VALUE=16, ATTRIBUTE=17, 
		HTML_COMMENT=18;
	public static final int
		RULE_table = 0, RULE_tr = 1, RULE_td = 2, RULE_htmlContent = 3, RULE_htmlAttribute = 4, 
		RULE_htmlChardata = 5, RULE_htmlComment = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"table", "tr", "td", "htmlContent", "htmlAttribute", "htmlChardata", 
			"htmlComment"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'<'", null, null, null, null, "','", "';'", "'>'", 
			"'/>'", "'/'", "'='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HTML_CONDITIONAL_COMMENT", "WS", "TAG_OPEN", "HTML_TEXT", "TABLE", 
			"TR", "TD", "COMMA", "SEMI_COLON", "TAG_CLOSE", "TAG_SLASH_CLOSE", "TAG_SLASH", 
			"TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", "ATTVALUE_VALUE", "ATTRIBUTE", 
			"HTML_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "TABLEParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TABLEParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class TableContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> TABLE() { return getTokens(TABLEParser.TABLE); }
		public TerminalNode TABLE(int i) {
			return getToken(TABLEParser.TABLE, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public List<HtmlAttributeContext> htmlAttribute() {
			return getRuleContexts(HtmlAttributeContext.class);
		}
		public HtmlAttributeContext htmlAttribute(int i) {
			return getRuleContext(HtmlAttributeContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(TABLEParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(TABLEParser.WS, i);
		}
		public List<TrContext> tr() {
			return getRuleContexts(TrContext.class);
		}
		public TrContext tr(int i) {
			return getRuleContext(TrContext.class,i);
		}
		public TableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitTable(this);
		}
	}

	public final TableContext table() throws RecognitionException {
		TableContext _localctx = new TableContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_table);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			match(TAG_OPEN);
			setState(15);
			match(TABLE);
			setState(19);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(16);
				htmlAttribute();
				}
				}
				setState(21);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(22);
			match(TAG_CLOSE);
			setState(24);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(23);
				match(WS);
				}
			}

			setState(30); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(26);
					tr();
					setState(28);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(27);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(32); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			setState(34);
			match(TAG_OPEN);
			setState(35);
			match(TAG_SLASH);
			setState(36);
			match(TABLE);
			setState(37);
			match(TAG_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TrContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> TR() { return getTokens(TABLEParser.TR); }
		public TerminalNode TR(int i) {
			return getToken(TABLEParser.TR, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public List<TerminalNode> WS() { return getTokens(TABLEParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(TABLEParser.WS, i);
		}
		public List<TdContext> td() {
			return getRuleContexts(TdContext.class);
		}
		public TdContext td(int i) {
			return getRuleContext(TdContext.class,i);
		}
		public TrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterTr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitTr(this);
		}
	}

	public final TrContext tr() throws RecognitionException {
		TrContext _localctx = new TrContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_tr);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			match(TAG_OPEN);
			setState(40);
			match(TR);
			setState(41);
			match(TAG_CLOSE);
			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(42);
				match(WS);
				}
			}

			setState(49); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(45);
					td();
					setState(47);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(46);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(51); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			setState(53);
			match(TAG_OPEN);
			setState(54);
			match(TAG_SLASH);
			setState(55);
			match(TR);
			setState(56);
			match(TAG_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TdContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> TD() { return getTokens(TABLEParser.TD); }
		public TerminalNode TD(int i) {
			return getToken(TABLEParser.TD, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public HtmlContentContext htmlContent() {
			return getRuleContext(HtmlContentContext.class,0);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public List<HtmlAttributeContext> htmlAttribute() {
			return getRuleContexts(HtmlAttributeContext.class);
		}
		public HtmlAttributeContext htmlAttribute(int i) {
			return getRuleContext(HtmlAttributeContext.class,i);
		}
		public TdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_td; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterTd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitTd(this);
		}
	}

	public final TdContext td() throws RecognitionException {
		TdContext _localctx = new TdContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_td);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
			match(TAG_OPEN);
			setState(59);
			match(TD);
			setState(63);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(60);
				htmlAttribute();
				}
				}
				setState(65);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(66);
			match(TAG_CLOSE);
			setState(67);
			htmlContent();
			setState(68);
			match(TAG_OPEN);
			setState(69);
			match(TAG_SLASH);
			setState(70);
			match(TD);
			setState(71);
			match(TAG_CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HtmlContentContext extends ParserRuleContext {
		public List<HtmlChardataContext> htmlChardata() {
			return getRuleContexts(HtmlChardataContext.class);
		}
		public HtmlChardataContext htmlChardata(int i) {
			return getRuleContext(HtmlChardataContext.class,i);
		}
		public List<TableContext> table() {
			return getRuleContexts(TableContext.class);
		}
		public TableContext table(int i) {
			return getRuleContext(TableContext.class,i);
		}
		public List<HtmlCommentContext> htmlComment() {
			return getRuleContexts(HtmlCommentContext.class);
		}
		public HtmlCommentContext htmlComment(int i) {
			return getRuleContext(HtmlCommentContext.class,i);
		}
		public HtmlContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterHtmlContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitHtmlContent(this);
		}
	}

	public final HtmlContentContext htmlContent() throws RecognitionException {
		HtmlContentContext _localctx = new HtmlContentContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_htmlContent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS || _la==HTML_TEXT) {
				{
				setState(73);
				htmlChardata();
				}
			}

			setState(85);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(78);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case TAG_OPEN:
						{
						setState(76);
						table();
						}
						break;
					case HTML_CONDITIONAL_COMMENT:
					case HTML_COMMENT:
						{
						setState(77);
						htmlComment();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(81);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS || _la==HTML_TEXT) {
						{
						setState(80);
						htmlChardata();
						}
					}

					}
					} 
				}
				setState(87);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HtmlAttributeContext extends ParserRuleContext {
		public TerminalNode TAG_NAME() { return getToken(TABLEParser.TAG_NAME, 0); }
		public TerminalNode TAG_EQUALS() { return getToken(TABLEParser.TAG_EQUALS, 0); }
		public TerminalNode ATTVALUE_VALUE() { return getToken(TABLEParser.ATTVALUE_VALUE, 0); }
		public HtmlAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterHtmlAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitHtmlAttribute(this);
		}
	}

	public final HtmlAttributeContext htmlAttribute() throws RecognitionException {
		HtmlAttributeContext _localctx = new HtmlAttributeContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_htmlAttribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(TAG_NAME);
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_EQUALS) {
				{
				setState(89);
				match(TAG_EQUALS);
				setState(90);
				match(ATTVALUE_VALUE);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HtmlChardataContext extends ParserRuleContext {
		public TerminalNode HTML_TEXT() { return getToken(TABLEParser.HTML_TEXT, 0); }
		public TerminalNode WS() { return getToken(TABLEParser.WS, 0); }
		public HtmlChardataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlChardata; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterHtmlChardata(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitHtmlChardata(this);
		}
	}

	public final HtmlChardataContext htmlChardata() throws RecognitionException {
		HtmlChardataContext _localctx = new HtmlChardataContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_htmlChardata);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			_la = _input.LA(1);
			if ( !(_la==WS || _la==HTML_TEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HtmlCommentContext extends ParserRuleContext {
		public TerminalNode HTML_COMMENT() { return getToken(TABLEParser.HTML_COMMENT, 0); }
		public TerminalNode HTML_CONDITIONAL_COMMENT() { return getToken(TABLEParser.HTML_CONDITIONAL_COMMENT, 0); }
		public HtmlCommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlComment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterHtmlComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitHtmlComment(this);
		}
	}

	public final HtmlCommentContext htmlComment() throws RecognitionException {
		HtmlCommentContext _localctx = new HtmlCommentContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_htmlComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			_la = _input.LA(1);
			if ( !(_la==HTML_CONDITIONAL_COMMENT || _la==HTML_COMMENT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\24d\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\7\2\24\n\2\f\2"+
		"\16\2\27\13\2\3\2\3\2\5\2\33\n\2\3\2\3\2\5\2\37\n\2\6\2!\n\2\r\2\16\2"+
		"\"\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\5\3.\n\3\3\3\3\3\5\3\62\n\3\6\3"+
		"\64\n\3\r\3\16\3\65\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\7\4@\n\4\f\4\16\4"+
		"C\13\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\5\5M\n\5\3\5\3\5\5\5Q\n\5\3\5\5"+
		"\5T\n\5\7\5V\n\5\f\5\16\5Y\13\5\3\6\3\6\3\6\5\6^\n\6\3\7\3\7\3\b\3\b\3"+
		"\b\2\2\t\2\4\6\b\n\f\16\2\4\4\2\4\4\6\6\4\2\3\3\24\24\2i\2\20\3\2\2\2"+
		"\4)\3\2\2\2\6<\3\2\2\2\bL\3\2\2\2\nZ\3\2\2\2\f_\3\2\2\2\16a\3\2\2\2\20"+
		"\21\7\5\2\2\21\25\7\7\2\2\22\24\5\n\6\2\23\22\3\2\2\2\24\27\3\2\2\2\25"+
		"\23\3\2\2\2\25\26\3\2\2\2\26\30\3\2\2\2\27\25\3\2\2\2\30\32\7\f\2\2\31"+
		"\33\7\4\2\2\32\31\3\2\2\2\32\33\3\2\2\2\33 \3\2\2\2\34\36\5\4\3\2\35\37"+
		"\7\4\2\2\36\35\3\2\2\2\36\37\3\2\2\2\37!\3\2\2\2 \34\3\2\2\2!\"\3\2\2"+
		"\2\" \3\2\2\2\"#\3\2\2\2#$\3\2\2\2$%\7\5\2\2%&\7\16\2\2&\'\7\7\2\2\'("+
		"\7\f\2\2(\3\3\2\2\2)*\7\5\2\2*+\7\b\2\2+-\7\f\2\2,.\7\4\2\2-,\3\2\2\2"+
		"-.\3\2\2\2.\63\3\2\2\2/\61\5\6\4\2\60\62\7\4\2\2\61\60\3\2\2\2\61\62\3"+
		"\2\2\2\62\64\3\2\2\2\63/\3\2\2\2\64\65\3\2\2\2\65\63\3\2\2\2\65\66\3\2"+
		"\2\2\66\67\3\2\2\2\678\7\5\2\289\7\16\2\29:\7\b\2\2:;\7\f\2\2;\5\3\2\2"+
		"\2<=\7\5\2\2=A\7\t\2\2>@\5\n\6\2?>\3\2\2\2@C\3\2\2\2A?\3\2\2\2AB\3\2\2"+
		"\2BD\3\2\2\2CA\3\2\2\2DE\7\f\2\2EF\5\b\5\2FG\7\5\2\2GH\7\16\2\2HI\7\t"+
		"\2\2IJ\7\f\2\2J\7\3\2\2\2KM\5\f\7\2LK\3\2\2\2LM\3\2\2\2MW\3\2\2\2NQ\5"+
		"\2\2\2OQ\5\16\b\2PN\3\2\2\2PO\3\2\2\2QS\3\2\2\2RT\5\f\7\2SR\3\2\2\2ST"+
		"\3\2\2\2TV\3\2\2\2UP\3\2\2\2VY\3\2\2\2WU\3\2\2\2WX\3\2\2\2X\t\3\2\2\2"+
		"YW\3\2\2\2Z]\7\20\2\2[\\\7\17\2\2\\^\7\22\2\2][\3\2\2\2]^\3\2\2\2^\13"+
		"\3\2\2\2_`\t\2\2\2`\r\3\2\2\2ab\t\3\2\2b\17\3\2\2\2\17\25\32\36\"-\61"+
		"\65ALPSW]";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}