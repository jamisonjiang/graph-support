package org.graphper.parser.grammar;
// Generated from /antlr4/org/graphper/parser/grammar/HTMLParser.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class HTMLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HTML_COMMENT=1, HTML_CONDITIONAL_COMMENT=2, WS=3, TAG_OPEN=4, HTML_TEXT=5, 
		TABLE=6, TR=7, TD=8, FONT=9, B=10, I=11, U=12, S=13, O=14, SUB=15, SUP=16, 
		BR=17, VT=18, VB=19, VC=20, HL=21, HR=22, HC=23, COMMA=24, SEMI_COLON=25, 
		TAG_CLOSE=26, TAG_SLASH_CLOSE=27, TAG_SLASH=28, TAG_EQUALS=29, TAG_NAME=30, 
		TAG_WHITESPACE=31, ATTVALUE_VALUE=32, ATTRIBUTE=33;
	public static final int
		RULE_htmlTag = 0, RULE_table = 1, RULE_tagContent = 2, RULE_tr = 3, RULE_td = 4, 
		RULE_tdContent = 5, RULE_htmlElement = 6, RULE_fontTag = 7, RULE_bTag = 8, 
		RULE_iTag = 9, RULE_uTag = 10, RULE_sTag = 11, RULE_oTag = 12, RULE_subTag = 13, 
		RULE_supTag = 14, RULE_vtTag = 15, RULE_vbTag = 16, RULE_vcTag = 17, RULE_hlTag = 18, 
		RULE_hrTag = 19, RULE_hcTag = 20, RULE_brTag = 21, RULE_htmlAttribute = 22, 
		RULE_htmlChardata = 23, RULE_htmlComment = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"htmlTag", "table", "tagContent", "tr", "td", "tdContent", "htmlElement", 
			"fontTag", "bTag", "iTag", "uTag", "sTag", "oTag", "subTag", "supTag", 
			"vtTag", "vbTag", "vcTag", "hlTag", "hrTag", "hcTag", "brTag", "htmlAttribute", 
			"htmlChardata", "htmlComment"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'<'", null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"','", "';'", "'>'", "'/>'", "'/'", "'='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HTML_COMMENT", "HTML_CONDITIONAL_COMMENT", "WS", "TAG_OPEN", "HTML_TEXT", 
			"TABLE", "TR", "TD", "FONT", "B", "I", "U", "S", "O", "SUB", "SUP", "BR", 
			"VT", "VB", "VC", "HL", "HR", "HC", "COMMA", "SEMI_COLON", "TAG_CLOSE", 
			"TAG_SLASH_CLOSE", "TAG_SLASH", "TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", 
			"ATTVALUE_VALUE", "ATTRIBUTE"
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
	public String getGrammarFileName() { return "HTMLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public HTMLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HtmlTagContext extends ParserRuleContext {
		public TableContext table() {
			return getRuleContext(TableContext.class,0);
		}
		public TerminalNode EOF() { return getToken(HTMLParser.EOF, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public HtmlTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHtmlTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHtmlTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHtmlTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HtmlTagContext htmlTag() throws RecognitionException {
		HtmlTagContext _localctx = new HtmlTagContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_htmlTag);
		try {
			setState(56);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50);
				table();
				setState(51);
				match(EOF);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(53);
				tagContent();
				setState(54);
				match(EOF);
				}
				break;
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

	@SuppressWarnings("CheckReturnValue")
	public static class TableContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> TABLE() { return getTokens(HTMLParser.TABLE); }
		public TerminalNode TABLE(int i) {
			return getToken(HTMLParser.TABLE, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public List<HtmlAttributeContext> htmlAttribute() {
			return getRuleContexts(HtmlAttributeContext.class);
		}
		public HtmlAttributeContext htmlAttribute(int i) {
			return getRuleContext(HtmlAttributeContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(HTMLParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(HTMLParser.WS, i);
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
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableContext table() throws RecognitionException {
		TableContext _localctx = new TableContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_table);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
			match(TAG_OPEN);
			setState(59);
			match(TABLE);
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
			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(67);
				match(WS);
				}
			}

			setState(74); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(70);
					tr();
					setState(72);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(71);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(76); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(78);
			match(TAG_OPEN);
			setState(79);
			match(TAG_SLASH);
			setState(80);
			match(TABLE);
			setState(81);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TagContentContext extends ParserRuleContext {
		public List<HtmlChardataContext> htmlChardata() {
			return getRuleContexts(HtmlChardataContext.class);
		}
		public HtmlChardataContext htmlChardata(int i) {
			return getRuleContext(HtmlChardataContext.class,i);
		}
		public List<HtmlElementContext> htmlElement() {
			return getRuleContexts(HtmlElementContext.class);
		}
		public HtmlElementContext htmlElement(int i) {
			return getRuleContext(HtmlElementContext.class,i);
		}
		public List<HtmlCommentContext> htmlComment() {
			return getRuleContexts(HtmlCommentContext.class);
		}
		public HtmlCommentContext htmlComment(int i) {
			return getRuleContext(HtmlCommentContext.class,i);
		}
		public TagContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterTagContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitTagContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitTagContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagContentContext tagContent() throws RecognitionException {
		TagContentContext _localctx = new TagContentContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_tagContent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS || _la==HTML_TEXT) {
				{
				setState(83);
				htmlChardata();
				}
			}

			setState(95);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(88);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case TAG_OPEN:
						{
						setState(86);
						htmlElement();
						}
						break;
					case HTML_COMMENT:
					case HTML_CONDITIONAL_COMMENT:
						{
						setState(87);
						htmlComment();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(91);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS || _la==HTML_TEXT) {
						{
						setState(90);
						htmlChardata();
						}
					}

					}
					} 
				}
				setState(97);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TrContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> TR() { return getTokens(HTMLParser.TR); }
		public TerminalNode TR(int i) {
			return getToken(HTMLParser.TR, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public List<TerminalNode> WS() { return getTokens(HTMLParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(HTMLParser.WS, i);
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
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterTr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitTr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitTr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TrContext tr() throws RecognitionException {
		TrContext _localctx = new TrContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tr);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			match(TAG_OPEN);
			setState(99);
			match(TR);
			setState(100);
			match(TAG_CLOSE);
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(101);
				match(WS);
				}
			}

			setState(108); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(104);
					td();
					setState(106);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(105);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(110); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(112);
			match(TAG_OPEN);
			setState(113);
			match(TAG_SLASH);
			setState(114);
			match(TR);
			setState(115);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TdContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> TD() { return getTokens(HTMLParser.TD); }
		public TerminalNode TD(int i) {
			return getToken(HTMLParser.TD, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TdContentContext tdContent() {
			return getRuleContext(TdContentContext.class,0);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
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
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterTd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitTd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitTd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TdContext td() throws RecognitionException {
		TdContext _localctx = new TdContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_td);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(TAG_OPEN);
			setState(118);
			match(TD);
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(119);
				htmlAttribute();
				}
				}
				setState(124);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(125);
			match(TAG_CLOSE);
			setState(126);
			tdContent();
			setState(127);
			match(TAG_OPEN);
			setState(128);
			match(TAG_SLASH);
			setState(129);
			match(TD);
			setState(130);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TdContentContext extends ParserRuleContext {
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
		public List<HtmlElementContext> htmlElement() {
			return getRuleContexts(HtmlElementContext.class);
		}
		public HtmlElementContext htmlElement(int i) {
			return getRuleContext(HtmlElementContext.class,i);
		}
		public List<HtmlCommentContext> htmlComment() {
			return getRuleContexts(HtmlCommentContext.class);
		}
		public HtmlCommentContext htmlComment(int i) {
			return getRuleContext(HtmlCommentContext.class,i);
		}
		public TdContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tdContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterTdContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitTdContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitTdContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TdContentContext tdContent() throws RecognitionException {
		TdContentContext _localctx = new TdContentContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tdContent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS || _la==HTML_TEXT) {
				{
				setState(132);
				htmlChardata();
				}
			}

			setState(145);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(138);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
					case 1:
						{
						setState(135);
						table();
						}
						break;
					case 2:
						{
						setState(136);
						htmlElement();
						}
						break;
					case 3:
						{
						setState(137);
						htmlComment();
						}
						break;
					}
					setState(141);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS || _la==HTML_TEXT) {
						{
						setState(140);
						htmlChardata();
						}
					}

					}
					} 
				}
				setState(147);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HtmlElementContext extends ParserRuleContext {
		public FontTagContext fontTag() {
			return getRuleContext(FontTagContext.class,0);
		}
		public BTagContext bTag() {
			return getRuleContext(BTagContext.class,0);
		}
		public ITagContext iTag() {
			return getRuleContext(ITagContext.class,0);
		}
		public UTagContext uTag() {
			return getRuleContext(UTagContext.class,0);
		}
		public STagContext sTag() {
			return getRuleContext(STagContext.class,0);
		}
		public OTagContext oTag() {
			return getRuleContext(OTagContext.class,0);
		}
		public SubTagContext subTag() {
			return getRuleContext(SubTagContext.class,0);
		}
		public SupTagContext supTag() {
			return getRuleContext(SupTagContext.class,0);
		}
		public VtTagContext vtTag() {
			return getRuleContext(VtTagContext.class,0);
		}
		public VbTagContext vbTag() {
			return getRuleContext(VbTagContext.class,0);
		}
		public VcTagContext vcTag() {
			return getRuleContext(VcTagContext.class,0);
		}
		public HlTagContext hlTag() {
			return getRuleContext(HlTagContext.class,0);
		}
		public HrTagContext hrTag() {
			return getRuleContext(HrTagContext.class,0);
		}
		public HcTagContext hcTag() {
			return getRuleContext(HcTagContext.class,0);
		}
		public BrTagContext brTag() {
			return getRuleContext(BrTagContext.class,0);
		}
		public HtmlElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHtmlElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHtmlElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHtmlElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HtmlElementContext htmlElement() throws RecognitionException {
		HtmlElementContext _localctx = new HtmlElementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_htmlElement);
		try {
			setState(163);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(148);
				fontTag();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(149);
				bTag();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(150);
				iTag();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(151);
				uTag();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(152);
				sTag();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(153);
				oTag();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(154);
				subTag();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(155);
				supTag();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(156);
				vtTag();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(157);
				vbTag();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(158);
				vcTag();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(159);
				hlTag();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(160);
				hrTag();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(161);
				hcTag();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(162);
				brTag();
				}
				break;
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

	@SuppressWarnings("CheckReturnValue")
	public static class FontTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> FONT() { return getTokens(HTMLParser.FONT); }
		public TerminalNode FONT(int i) {
			return getToken(HTMLParser.FONT, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public List<HtmlAttributeContext> htmlAttribute() {
			return getRuleContexts(HtmlAttributeContext.class);
		}
		public HtmlAttributeContext htmlAttribute(int i) {
			return getRuleContext(HtmlAttributeContext.class,i);
		}
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public FontTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fontTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterFontTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitFontTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitFontTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FontTagContext fontTag() throws RecognitionException {
		FontTagContext _localctx = new FontTagContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fontTag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			match(TAG_OPEN);
			setState(166);
			match(FONT);
			setState(170);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(167);
				htmlAttribute();
				}
				}
				setState(172);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(173);
			match(TAG_CLOSE);
			setState(175);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(174);
				tagContent();
				}
				break;
			}
			setState(177);
			match(TAG_OPEN);
			setState(178);
			match(TAG_SLASH);
			setState(179);
			match(FONT);
			setState(180);
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

	@SuppressWarnings("CheckReturnValue")
	public static class BTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> B() { return getTokens(HTMLParser.B); }
		public TerminalNode B(int i) {
			return getToken(HTMLParser.B, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public BTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterBTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitBTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitBTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BTagContext bTag() throws RecognitionException {
		BTagContext _localctx = new BTagContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_bTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(TAG_OPEN);
			setState(183);
			match(B);
			setState(184);
			match(TAG_CLOSE);
			setState(186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				setState(185);
				tagContent();
				}
				break;
			}
			setState(188);
			match(TAG_OPEN);
			setState(189);
			match(TAG_SLASH);
			setState(190);
			match(B);
			setState(191);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ITagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> I() { return getTokens(HTMLParser.I); }
		public TerminalNode I(int i) {
			return getToken(HTMLParser.I, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public ITagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterITag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitITag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitITag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ITagContext iTag() throws RecognitionException {
		ITagContext _localctx = new ITagContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_iTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(TAG_OPEN);
			setState(194);
			match(I);
			setState(195);
			match(TAG_CLOSE);
			setState(197);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(196);
				tagContent();
				}
				break;
			}
			setState(199);
			match(TAG_OPEN);
			setState(200);
			match(TAG_SLASH);
			setState(201);
			match(I);
			setState(202);
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

	@SuppressWarnings("CheckReturnValue")
	public static class UTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> U() { return getTokens(HTMLParser.U); }
		public TerminalNode U(int i) {
			return getToken(HTMLParser.U, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public UTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterUTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitUTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitUTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UTagContext uTag() throws RecognitionException {
		UTagContext _localctx = new UTagContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_uTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			match(TAG_OPEN);
			setState(205);
			match(U);
			setState(206);
			match(TAG_CLOSE);
			setState(208);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(207);
				tagContent();
				}
				break;
			}
			setState(210);
			match(TAG_OPEN);
			setState(211);
			match(TAG_SLASH);
			setState(212);
			match(U);
			setState(213);
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

	@SuppressWarnings("CheckReturnValue")
	public static class STagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> S() { return getTokens(HTMLParser.S); }
		public TerminalNode S(int i) {
			return getToken(HTMLParser.S, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public STagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterSTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitSTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitSTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final STagContext sTag() throws RecognitionException {
		STagContext _localctx = new STagContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_sTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			match(TAG_OPEN);
			setState(216);
			match(S);
			setState(217);
			match(TAG_CLOSE);
			setState(219);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(218);
				tagContent();
				}
				break;
			}
			setState(221);
			match(TAG_OPEN);
			setState(222);
			match(TAG_SLASH);
			setState(223);
			match(S);
			setState(224);
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

	@SuppressWarnings("CheckReturnValue")
	public static class OTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> O() { return getTokens(HTMLParser.O); }
		public TerminalNode O(int i) {
			return getToken(HTMLParser.O, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public OTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_oTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterOTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitOTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitOTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OTagContext oTag() throws RecognitionException {
		OTagContext _localctx = new OTagContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_oTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			match(TAG_OPEN);
			setState(227);
			match(O);
			setState(228);
			match(TAG_CLOSE);
			setState(230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(229);
				tagContent();
				}
				break;
			}
			setState(232);
			match(TAG_OPEN);
			setState(233);
			match(TAG_SLASH);
			setState(234);
			match(O);
			setState(235);
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

	@SuppressWarnings("CheckReturnValue")
	public static class SubTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> SUB() { return getTokens(HTMLParser.SUB); }
		public TerminalNode SUB(int i) {
			return getToken(HTMLParser.SUB, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public SubTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterSubTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitSubTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitSubTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubTagContext subTag() throws RecognitionException {
		SubTagContext _localctx = new SubTagContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_subTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237);
			match(TAG_OPEN);
			setState(238);
			match(SUB);
			setState(239);
			match(TAG_CLOSE);
			setState(241);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(240);
				tagContent();
				}
				break;
			}
			setState(243);
			match(TAG_OPEN);
			setState(244);
			match(TAG_SLASH);
			setState(245);
			match(SUB);
			setState(246);
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

	@SuppressWarnings("CheckReturnValue")
	public static class SupTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> SUP() { return getTokens(HTMLParser.SUP); }
		public TerminalNode SUP(int i) {
			return getToken(HTMLParser.SUP, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public SupTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_supTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterSupTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitSupTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitSupTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SupTagContext supTag() throws RecognitionException {
		SupTagContext _localctx = new SupTagContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_supTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			match(TAG_OPEN);
			setState(249);
			match(SUP);
			setState(250);
			match(TAG_CLOSE);
			setState(252);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(251);
				tagContent();
				}
				break;
			}
			setState(254);
			match(TAG_OPEN);
			setState(255);
			match(TAG_SLASH);
			setState(256);
			match(SUP);
			setState(257);
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

	@SuppressWarnings("CheckReturnValue")
	public static class VtTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> VT() { return getTokens(HTMLParser.VT); }
		public TerminalNode VT(int i) {
			return getToken(HTMLParser.VT, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public VtTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vtTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterVtTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitVtTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitVtTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VtTagContext vtTag() throws RecognitionException {
		VtTagContext _localctx = new VtTagContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_vtTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			match(TAG_OPEN);
			setState(260);
			match(VT);
			setState(261);
			match(TAG_CLOSE);
			setState(263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(262);
				tagContent();
				}
				break;
			}
			setState(265);
			match(TAG_OPEN);
			setState(266);
			match(TAG_SLASH);
			setState(267);
			match(VT);
			setState(268);
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

	@SuppressWarnings("CheckReturnValue")
	public static class VbTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> VB() { return getTokens(HTMLParser.VB); }
		public TerminalNode VB(int i) {
			return getToken(HTMLParser.VB, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public VbTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vbTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterVbTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitVbTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitVbTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VbTagContext vbTag() throws RecognitionException {
		VbTagContext _localctx = new VbTagContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_vbTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			match(TAG_OPEN);
			setState(271);
			match(VB);
			setState(272);
			match(TAG_CLOSE);
			setState(274);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(273);
				tagContent();
				}
				break;
			}
			setState(276);
			match(TAG_OPEN);
			setState(277);
			match(TAG_SLASH);
			setState(278);
			match(VB);
			setState(279);
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

	@SuppressWarnings("CheckReturnValue")
	public static class VcTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> VC() { return getTokens(HTMLParser.VC); }
		public TerminalNode VC(int i) {
			return getToken(HTMLParser.VC, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public VcTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vcTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterVcTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitVcTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitVcTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VcTagContext vcTag() throws RecognitionException {
		VcTagContext _localctx = new VcTagContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_vcTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			match(TAG_OPEN);
			setState(282);
			match(VC);
			setState(283);
			match(TAG_CLOSE);
			setState(285);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(284);
				tagContent();
				}
				break;
			}
			setState(287);
			match(TAG_OPEN);
			setState(288);
			match(TAG_SLASH);
			setState(289);
			match(VC);
			setState(290);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HlTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> HL() { return getTokens(HTMLParser.HL); }
		public TerminalNode HL(int i) {
			return getToken(HTMLParser.HL, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public HlTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hlTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHlTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHlTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHlTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HlTagContext hlTag() throws RecognitionException {
		HlTagContext _localctx = new HlTagContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_hlTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(292);
			match(TAG_OPEN);
			setState(293);
			match(HL);
			setState(294);
			match(TAG_CLOSE);
			setState(296);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(295);
				tagContent();
				}
				break;
			}
			setState(298);
			match(TAG_OPEN);
			setState(299);
			match(TAG_SLASH);
			setState(300);
			match(HL);
			setState(301);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HrTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> HR() { return getTokens(HTMLParser.HR); }
		public TerminalNode HR(int i) {
			return getToken(HTMLParser.HR, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public HrTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hrTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHrTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHrTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHrTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HrTagContext hrTag() throws RecognitionException {
		HrTagContext _localctx = new HrTagContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_hrTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(303);
			match(TAG_OPEN);
			setState(304);
			match(HR);
			setState(305);
			match(TAG_CLOSE);
			setState(307);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				{
				setState(306);
				tagContent();
				}
				break;
			}
			setState(309);
			match(TAG_OPEN);
			setState(310);
			match(TAG_SLASH);
			setState(311);
			match(HR);
			setState(312);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HcTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(HTMLParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(HTMLParser.TAG_OPEN, i);
		}
		public List<TerminalNode> HC() { return getTokens(HTMLParser.HC); }
		public TerminalNode HC(int i) {
			return getToken(HTMLParser.HC, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(HTMLParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(HTMLParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(HTMLParser.TAG_SLASH, 0); }
		public TagContentContext tagContent() {
			return getRuleContext(TagContentContext.class,0);
		}
		public HcTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hcTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHcTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHcTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHcTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HcTagContext hcTag() throws RecognitionException {
		HcTagContext _localctx = new HcTagContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_hcTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(314);
			match(TAG_OPEN);
			setState(315);
			match(HC);
			setState(316);
			match(TAG_CLOSE);
			setState(318);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(317);
				tagContent();
				}
				break;
			}
			setState(320);
			match(TAG_OPEN);
			setState(321);
			match(TAG_SLASH);
			setState(322);
			match(HC);
			setState(323);
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

	@SuppressWarnings("CheckReturnValue")
	public static class BrTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPEN() { return getToken(HTMLParser.TAG_OPEN, 0); }
		public TerminalNode BR() { return getToken(HTMLParser.BR, 0); }
		public TerminalNode TAG_SLASH_CLOSE() { return getToken(HTMLParser.TAG_SLASH_CLOSE, 0); }
		public BrTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterBrTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitBrTag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitBrTag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BrTagContext brTag() throws RecognitionException {
		BrTagContext _localctx = new BrTagContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_brTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(325);
			match(TAG_OPEN);
			setState(326);
			match(BR);
			setState(327);
			match(TAG_SLASH_CLOSE);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HtmlAttributeContext extends ParserRuleContext {
		public TerminalNode TAG_NAME() { return getToken(HTMLParser.TAG_NAME, 0); }
		public TerminalNode TAG_EQUALS() { return getToken(HTMLParser.TAG_EQUALS, 0); }
		public TerminalNode ATTVALUE_VALUE() { return getToken(HTMLParser.ATTVALUE_VALUE, 0); }
		public HtmlAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHtmlAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHtmlAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHtmlAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HtmlAttributeContext htmlAttribute() throws RecognitionException {
		HtmlAttributeContext _localctx = new HtmlAttributeContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_htmlAttribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(329);
			match(TAG_NAME);
			setState(332);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_EQUALS) {
				{
				setState(330);
				match(TAG_EQUALS);
				setState(331);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HtmlChardataContext extends ParserRuleContext {
		public TerminalNode HTML_TEXT() { return getToken(HTMLParser.HTML_TEXT, 0); }
		public TerminalNode WS() { return getToken(HTMLParser.WS, 0); }
		public HtmlChardataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlChardata; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHtmlChardata(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHtmlChardata(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHtmlChardata(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HtmlChardataContext htmlChardata() throws RecognitionException {
		HtmlChardataContext _localctx = new HtmlChardataContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_htmlChardata);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
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

	@SuppressWarnings("CheckReturnValue")
	public static class HtmlCommentContext extends ParserRuleContext {
		public TerminalNode HTML_COMMENT() { return getToken(HTMLParser.HTML_COMMENT, 0); }
		public TerminalNode HTML_CONDITIONAL_COMMENT() { return getToken(HTMLParser.HTML_CONDITIONAL_COMMENT, 0); }
		public HtmlCommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlComment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).enterHtmlComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HTMLParserListener ) ((HTMLParserListener)listener).exitHtmlComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HTMLParserVisitor ) return ((HTMLParserVisitor<? extends T>)visitor).visitHtmlComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HtmlCommentContext htmlComment() throws RecognitionException {
		HtmlCommentContext _localctx = new HtmlCommentContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_htmlComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
			_la = _input.LA(1);
			if ( !(_la==HTML_COMMENT || _la==HTML_CONDITIONAL_COMMENT) ) {
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
		"\u0004\u0001!\u0153\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u00009\b\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001"+
		">\b\u0001\n\u0001\f\u0001A\t\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"E\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001I\b\u0001\u0004\u0001K\b"+
		"\u0001\u000b\u0001\f\u0001L\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0003\u0002U\b\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002Y\b\u0002\u0001\u0002\u0003\u0002\\\b\u0002\u0005\u0002"+
		"^\b\u0002\n\u0002\f\u0002a\t\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0003\u0003g\b\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"k\b\u0003\u0004\u0003m\b\u0003\u000b\u0003\f\u0003n\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0005\u0004y\b\u0004\n\u0004\f\u0004|\t\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0003\u0005\u0086\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005\u008b\b\u0005\u0001\u0005\u0003\u0005\u008e\b\u0005\u0005\u0005"+
		"\u0090\b\u0005\n\u0005\f\u0005\u0093\t\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0003\u0006\u00a4\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0005"+
		"\u0007\u00a9\b\u0007\n\u0007\f\u0007\u00ac\t\u0007\u0001\u0007\u0001\u0007"+
		"\u0003\u0007\u00b0\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u00bb\b\b\u0001\b"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0003"+
		"\t\u00c6\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0003\n\u00d1\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00dc\b\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0003\f\u00e7\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00f2\b\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003"+
		"\u000e\u00fd\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0108"+
		"\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0113\b\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u011e\b\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0003\u0012\u0129\b\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0003\u0013\u0134\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003"+
		"\u0014\u013f\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u014d\b\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0000\u0000\u0019\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"0\u0000\u0002\u0002\u0000\u0003\u0003\u0005\u0005\u0001\u0000\u0001\u0002"+
		"\u0169\u00008\u0001\u0000\u0000\u0000\u0002:\u0001\u0000\u0000\u0000\u0004"+
		"T\u0001\u0000\u0000\u0000\u0006b\u0001\u0000\u0000\u0000\bu\u0001\u0000"+
		"\u0000\u0000\n\u0085\u0001\u0000\u0000\u0000\f\u00a3\u0001\u0000\u0000"+
		"\u0000\u000e\u00a5\u0001\u0000\u0000\u0000\u0010\u00b6\u0001\u0000\u0000"+
		"\u0000\u0012\u00c1\u0001\u0000\u0000\u0000\u0014\u00cc\u0001\u0000\u0000"+
		"\u0000\u0016\u00d7\u0001\u0000\u0000\u0000\u0018\u00e2\u0001\u0000\u0000"+
		"\u0000\u001a\u00ed\u0001\u0000\u0000\u0000\u001c\u00f8\u0001\u0000\u0000"+
		"\u0000\u001e\u0103\u0001\u0000\u0000\u0000 \u010e\u0001\u0000\u0000\u0000"+
		"\"\u0119\u0001\u0000\u0000\u0000$\u0124\u0001\u0000\u0000\u0000&\u012f"+
		"\u0001\u0000\u0000\u0000(\u013a\u0001\u0000\u0000\u0000*\u0145\u0001\u0000"+
		"\u0000\u0000,\u0149\u0001\u0000\u0000\u0000.\u014e\u0001\u0000\u0000\u0000"+
		"0\u0150\u0001\u0000\u0000\u000023\u0003\u0002\u0001\u000034\u0005\u0000"+
		"\u0000\u000149\u0001\u0000\u0000\u000056\u0003\u0004\u0002\u000067\u0005"+
		"\u0000\u0000\u000179\u0001\u0000\u0000\u000082\u0001\u0000\u0000\u0000"+
		"85\u0001\u0000\u0000\u00009\u0001\u0001\u0000\u0000\u0000:;\u0005\u0004"+
		"\u0000\u0000;?\u0005\u0006\u0000\u0000<>\u0003,\u0016\u0000=<\u0001\u0000"+
		"\u0000\u0000>A\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000?@\u0001"+
		"\u0000\u0000\u0000@B\u0001\u0000\u0000\u0000A?\u0001\u0000\u0000\u0000"+
		"BD\u0005\u001a\u0000\u0000CE\u0005\u0003\u0000\u0000DC\u0001\u0000\u0000"+
		"\u0000DE\u0001\u0000\u0000\u0000EJ\u0001\u0000\u0000\u0000FH\u0003\u0006"+
		"\u0003\u0000GI\u0005\u0003\u0000\u0000HG\u0001\u0000\u0000\u0000HI\u0001"+
		"\u0000\u0000\u0000IK\u0001\u0000\u0000\u0000JF\u0001\u0000\u0000\u0000"+
		"KL\u0001\u0000\u0000\u0000LJ\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000"+
		"\u0000MN\u0001\u0000\u0000\u0000NO\u0005\u0004\u0000\u0000OP\u0005\u001c"+
		"\u0000\u0000PQ\u0005\u0006\u0000\u0000QR\u0005\u001a\u0000\u0000R\u0003"+
		"\u0001\u0000\u0000\u0000SU\u0003.\u0017\u0000TS\u0001\u0000\u0000\u0000"+
		"TU\u0001\u0000\u0000\u0000U_\u0001\u0000\u0000\u0000VY\u0003\f\u0006\u0000"+
		"WY\u00030\u0018\u0000XV\u0001\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000"+
		"Y[\u0001\u0000\u0000\u0000Z\\\u0003.\u0017\u0000[Z\u0001\u0000\u0000\u0000"+
		"[\\\u0001\u0000\u0000\u0000\\^\u0001\u0000\u0000\u0000]X\u0001\u0000\u0000"+
		"\u0000^a\u0001\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000"+
		"\u0000\u0000`\u0005\u0001\u0000\u0000\u0000a_\u0001\u0000\u0000\u0000"+
		"bc\u0005\u0004\u0000\u0000cd\u0005\u0007\u0000\u0000df\u0005\u001a\u0000"+
		"\u0000eg\u0005\u0003\u0000\u0000fe\u0001\u0000\u0000\u0000fg\u0001\u0000"+
		"\u0000\u0000gl\u0001\u0000\u0000\u0000hj\u0003\b\u0004\u0000ik\u0005\u0003"+
		"\u0000\u0000ji\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000km\u0001"+
		"\u0000\u0000\u0000lh\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000"+
		"nl\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000op\u0001\u0000\u0000"+
		"\u0000pq\u0005\u0004\u0000\u0000qr\u0005\u001c\u0000\u0000rs\u0005\u0007"+
		"\u0000\u0000st\u0005\u001a\u0000\u0000t\u0007\u0001\u0000\u0000\u0000"+
		"uv\u0005\u0004\u0000\u0000vz\u0005\b\u0000\u0000wy\u0003,\u0016\u0000"+
		"xw\u0001\u0000\u0000\u0000y|\u0001\u0000\u0000\u0000zx\u0001\u0000\u0000"+
		"\u0000z{\u0001\u0000\u0000\u0000{}\u0001\u0000\u0000\u0000|z\u0001\u0000"+
		"\u0000\u0000}~\u0005\u001a\u0000\u0000~\u007f\u0003\n\u0005\u0000\u007f"+
		"\u0080\u0005\u0004\u0000\u0000\u0080\u0081\u0005\u001c\u0000\u0000\u0081"+
		"\u0082\u0005\b\u0000\u0000\u0082\u0083\u0005\u001a\u0000\u0000\u0083\t"+
		"\u0001\u0000\u0000\u0000\u0084\u0086\u0003.\u0017\u0000\u0085\u0084\u0001"+
		"\u0000\u0000\u0000\u0085\u0086\u0001\u0000\u0000\u0000\u0086\u0091\u0001"+
		"\u0000\u0000\u0000\u0087\u008b\u0003\u0002\u0001\u0000\u0088\u008b\u0003"+
		"\f\u0006\u0000\u0089\u008b\u00030\u0018\u0000\u008a\u0087\u0001\u0000"+
		"\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008a\u0089\u0001\u0000"+
		"\u0000\u0000\u008b\u008d\u0001\u0000\u0000\u0000\u008c\u008e\u0003.\u0017"+
		"\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000"+
		"\u0000\u008e\u0090\u0001\u0000\u0000\u0000\u008f\u008a\u0001\u0000\u0000"+
		"\u0000\u0090\u0093\u0001\u0000\u0000\u0000\u0091\u008f\u0001\u0000\u0000"+
		"\u0000\u0091\u0092\u0001\u0000\u0000\u0000\u0092\u000b\u0001\u0000\u0000"+
		"\u0000\u0093\u0091\u0001\u0000\u0000\u0000\u0094\u00a4\u0003\u000e\u0007"+
		"\u0000\u0095\u00a4\u0003\u0010\b\u0000\u0096\u00a4\u0003\u0012\t\u0000"+
		"\u0097\u00a4\u0003\u0014\n\u0000\u0098\u00a4\u0003\u0016\u000b\u0000\u0099"+
		"\u00a4\u0003\u0018\f\u0000\u009a\u00a4\u0003\u001a\r\u0000\u009b\u00a4"+
		"\u0003\u001c\u000e\u0000\u009c\u00a4\u0003\u001e\u000f\u0000\u009d\u00a4"+
		"\u0003 \u0010\u0000\u009e\u00a4\u0003\"\u0011\u0000\u009f\u00a4\u0003"+
		"$\u0012\u0000\u00a0\u00a4\u0003&\u0013\u0000\u00a1\u00a4\u0003(\u0014"+
		"\u0000\u00a2\u00a4\u0003*\u0015\u0000\u00a3\u0094\u0001\u0000\u0000\u0000"+
		"\u00a3\u0095\u0001\u0000\u0000\u0000\u00a3\u0096\u0001\u0000\u0000\u0000"+
		"\u00a3\u0097\u0001\u0000\u0000\u0000\u00a3\u0098\u0001\u0000\u0000\u0000"+
		"\u00a3\u0099\u0001\u0000\u0000\u0000\u00a3\u009a\u0001\u0000\u0000\u0000"+
		"\u00a3\u009b\u0001\u0000\u0000\u0000\u00a3\u009c\u0001\u0000\u0000\u0000"+
		"\u00a3\u009d\u0001\u0000\u0000\u0000\u00a3\u009e\u0001\u0000\u0000\u0000"+
		"\u00a3\u009f\u0001\u0000\u0000\u0000\u00a3\u00a0\u0001\u0000\u0000\u0000"+
		"\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a2\u0001\u0000\u0000\u0000"+
		"\u00a4\r\u0001\u0000\u0000\u0000\u00a5\u00a6\u0005\u0004\u0000\u0000\u00a6"+
		"\u00aa\u0005\t\u0000\u0000\u00a7\u00a9\u0003,\u0016\u0000\u00a8\u00a7"+
		"\u0001\u0000\u0000\u0000\u00a9\u00ac\u0001\u0000\u0000\u0000\u00aa\u00a8"+
		"\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001\u0000\u0000\u0000\u00ab\u00ad"+
		"\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ad\u00af"+
		"\u0005\u001a\u0000\u0000\u00ae\u00b0\u0003\u0004\u0002\u0000\u00af\u00ae"+
		"\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0\u00b1"+
		"\u0001\u0000\u0000\u0000\u00b1\u00b2\u0005\u0004\u0000\u0000\u00b2\u00b3"+
		"\u0005\u001c\u0000\u0000\u00b3\u00b4\u0005\t\u0000\u0000\u00b4\u00b5\u0005"+
		"\u001a\u0000\u0000\u00b5\u000f\u0001\u0000\u0000\u0000\u00b6\u00b7\u0005"+
		"\u0004\u0000\u0000\u00b7\u00b8\u0005\n\u0000\u0000\u00b8\u00ba\u0005\u001a"+
		"\u0000\u0000\u00b9\u00bb\u0003\u0004\u0002\u0000\u00ba\u00b9\u0001\u0000"+
		"\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000"+
		"\u0000\u0000\u00bc\u00bd\u0005\u0004\u0000\u0000\u00bd\u00be\u0005\u001c"+
		"\u0000\u0000\u00be\u00bf\u0005\n\u0000\u0000\u00bf\u00c0\u0005\u001a\u0000"+
		"\u0000\u00c0\u0011\u0001\u0000\u0000\u0000\u00c1\u00c2\u0005\u0004\u0000"+
		"\u0000\u00c2\u00c3\u0005\u000b\u0000\u0000\u00c3\u00c5\u0005\u001a\u0000"+
		"\u0000\u00c4\u00c6\u0003\u0004\u0002\u0000\u00c5\u00c4\u0001\u0000\u0000"+
		"\u0000\u00c5\u00c6\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000"+
		"\u0000\u00c7\u00c8\u0005\u0004\u0000\u0000\u00c8\u00c9\u0005\u001c\u0000"+
		"\u0000\u00c9\u00ca\u0005\u000b\u0000\u0000\u00ca\u00cb\u0005\u001a\u0000"+
		"\u0000\u00cb\u0013\u0001\u0000\u0000\u0000\u00cc\u00cd\u0005\u0004\u0000"+
		"\u0000\u00cd\u00ce\u0005\f\u0000\u0000\u00ce\u00d0\u0005\u001a\u0000\u0000"+
		"\u00cf\u00d1\u0003\u0004\u0002\u0000\u00d0\u00cf\u0001\u0000\u0000\u0000"+
		"\u00d0\u00d1\u0001\u0000\u0000\u0000\u00d1\u00d2\u0001\u0000\u0000\u0000"+
		"\u00d2\u00d3\u0005\u0004\u0000\u0000\u00d3\u00d4\u0005\u001c\u0000\u0000"+
		"\u00d4\u00d5\u0005\f\u0000\u0000\u00d5\u00d6\u0005\u001a\u0000\u0000\u00d6"+
		"\u0015\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005\u0004\u0000\u0000\u00d8"+
		"\u00d9\u0005\r\u0000\u0000\u00d9\u00db\u0005\u001a\u0000\u0000\u00da\u00dc"+
		"\u0003\u0004\u0002\u0000\u00db\u00da\u0001\u0000\u0000\u0000\u00db\u00dc"+
		"\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00de"+
		"\u0005\u0004\u0000\u0000\u00de\u00df\u0005\u001c\u0000\u0000\u00df\u00e0"+
		"\u0005\r\u0000\u0000\u00e0\u00e1\u0005\u001a\u0000\u0000\u00e1\u0017\u0001"+
		"\u0000\u0000\u0000\u00e2\u00e3\u0005\u0004\u0000\u0000\u00e3\u00e4\u0005"+
		"\u000e\u0000\u0000\u00e4\u00e6\u0005\u001a\u0000\u0000\u00e5\u00e7\u0003"+
		"\u0004\u0002\u0000\u00e6\u00e5\u0001\u0000\u0000\u0000\u00e6\u00e7\u0001"+
		"\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000\u00e8\u00e9\u0005"+
		"\u0004\u0000\u0000\u00e9\u00ea\u0005\u001c\u0000\u0000\u00ea\u00eb\u0005"+
		"\u000e\u0000\u0000\u00eb\u00ec\u0005\u001a\u0000\u0000\u00ec\u0019\u0001"+
		"\u0000\u0000\u0000\u00ed\u00ee\u0005\u0004\u0000\u0000\u00ee\u00ef\u0005"+
		"\u000f\u0000\u0000\u00ef\u00f1\u0005\u001a\u0000\u0000\u00f0\u00f2\u0003"+
		"\u0004\u0002\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001"+
		"\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f4\u0005"+
		"\u0004\u0000\u0000\u00f4\u00f5\u0005\u001c\u0000\u0000\u00f5\u00f6\u0005"+
		"\u000f\u0000\u0000\u00f6\u00f7\u0005\u001a\u0000\u0000\u00f7\u001b\u0001"+
		"\u0000\u0000\u0000\u00f8\u00f9\u0005\u0004\u0000\u0000\u00f9\u00fa\u0005"+
		"\u0010\u0000\u0000\u00fa\u00fc\u0005\u001a\u0000\u0000\u00fb\u00fd\u0003"+
		"\u0004\u0002\u0000\u00fc\u00fb\u0001\u0000\u0000\u0000\u00fc\u00fd\u0001"+
		"\u0000\u0000\u0000\u00fd\u00fe\u0001\u0000\u0000\u0000\u00fe\u00ff\u0005"+
		"\u0004\u0000\u0000\u00ff\u0100\u0005\u001c\u0000\u0000\u0100\u0101\u0005"+
		"\u0010\u0000\u0000\u0101\u0102\u0005\u001a\u0000\u0000\u0102\u001d\u0001"+
		"\u0000\u0000\u0000\u0103\u0104\u0005\u0004\u0000\u0000\u0104\u0105\u0005"+
		"\u0012\u0000\u0000\u0105\u0107\u0005\u001a\u0000\u0000\u0106\u0108\u0003"+
		"\u0004\u0002\u0000\u0107\u0106\u0001\u0000\u0000\u0000\u0107\u0108\u0001"+
		"\u0000\u0000\u0000\u0108\u0109\u0001\u0000\u0000\u0000\u0109\u010a\u0005"+
		"\u0004\u0000\u0000\u010a\u010b\u0005\u001c\u0000\u0000\u010b\u010c\u0005"+
		"\u0012\u0000\u0000\u010c\u010d\u0005\u001a\u0000\u0000\u010d\u001f\u0001"+
		"\u0000\u0000\u0000\u010e\u010f\u0005\u0004\u0000\u0000\u010f\u0110\u0005"+
		"\u0013\u0000\u0000\u0110\u0112\u0005\u001a\u0000\u0000\u0111\u0113\u0003"+
		"\u0004\u0002\u0000\u0112\u0111\u0001\u0000\u0000\u0000\u0112\u0113\u0001"+
		"\u0000\u0000\u0000\u0113\u0114\u0001\u0000\u0000\u0000\u0114\u0115\u0005"+
		"\u0004\u0000\u0000\u0115\u0116\u0005\u001c\u0000\u0000\u0116\u0117\u0005"+
		"\u0013\u0000\u0000\u0117\u0118\u0005\u001a\u0000\u0000\u0118!\u0001\u0000"+
		"\u0000\u0000\u0119\u011a\u0005\u0004\u0000\u0000\u011a\u011b\u0005\u0014"+
		"\u0000\u0000\u011b\u011d\u0005\u001a\u0000\u0000\u011c\u011e\u0003\u0004"+
		"\u0002\u0000\u011d\u011c\u0001\u0000\u0000\u0000\u011d\u011e\u0001\u0000"+
		"\u0000\u0000\u011e\u011f\u0001\u0000\u0000\u0000\u011f\u0120\u0005\u0004"+
		"\u0000\u0000\u0120\u0121\u0005\u001c\u0000\u0000\u0121\u0122\u0005\u0014"+
		"\u0000\u0000\u0122\u0123\u0005\u001a\u0000\u0000\u0123#\u0001\u0000\u0000"+
		"\u0000\u0124\u0125\u0005\u0004\u0000\u0000\u0125\u0126\u0005\u0015\u0000"+
		"\u0000\u0126\u0128\u0005\u001a\u0000\u0000\u0127\u0129\u0003\u0004\u0002"+
		"\u0000\u0128\u0127\u0001\u0000\u0000\u0000\u0128\u0129\u0001\u0000\u0000"+
		"\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a\u012b\u0005\u0004\u0000"+
		"\u0000\u012b\u012c\u0005\u001c\u0000\u0000\u012c\u012d\u0005\u0015\u0000"+
		"\u0000\u012d\u012e\u0005\u001a\u0000\u0000\u012e%\u0001\u0000\u0000\u0000"+
		"\u012f\u0130\u0005\u0004\u0000\u0000\u0130\u0131\u0005\u0016\u0000\u0000"+
		"\u0131\u0133\u0005\u001a\u0000\u0000\u0132\u0134\u0003\u0004\u0002\u0000"+
		"\u0133\u0132\u0001\u0000\u0000\u0000\u0133\u0134\u0001\u0000\u0000\u0000"+
		"\u0134\u0135\u0001\u0000\u0000\u0000\u0135\u0136\u0005\u0004\u0000\u0000"+
		"\u0136\u0137\u0005\u001c\u0000\u0000\u0137\u0138\u0005\u0016\u0000\u0000"+
		"\u0138\u0139\u0005\u001a\u0000\u0000\u0139\'\u0001\u0000\u0000\u0000\u013a"+
		"\u013b\u0005\u0004\u0000\u0000\u013b\u013c\u0005\u0017\u0000\u0000\u013c"+
		"\u013e\u0005\u001a\u0000\u0000\u013d\u013f\u0003\u0004\u0002\u0000\u013e"+
		"\u013d\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f"+
		"\u0140\u0001\u0000\u0000\u0000\u0140\u0141\u0005\u0004\u0000\u0000\u0141"+
		"\u0142\u0005\u001c\u0000\u0000\u0142\u0143\u0005\u0017\u0000\u0000\u0143"+
		"\u0144\u0005\u001a\u0000\u0000\u0144)\u0001\u0000\u0000\u0000\u0145\u0146"+
		"\u0005\u0004\u0000\u0000\u0146\u0147\u0005\u0011\u0000\u0000\u0147\u0148"+
		"\u0005\u001b\u0000\u0000\u0148+\u0001\u0000\u0000\u0000\u0149\u014c\u0005"+
		"\u001e\u0000\u0000\u014a\u014b\u0005\u001d\u0000\u0000\u014b\u014d\u0005"+
		" \u0000\u0000\u014c\u014a\u0001\u0000\u0000\u0000\u014c\u014d\u0001\u0000"+
		"\u0000\u0000\u014d-\u0001\u0000\u0000\u0000\u014e\u014f\u0007\u0000\u0000"+
		"\u0000\u014f/\u0001\u0000\u0000\u0000\u0150\u0151\u0007\u0001\u0000\u0000"+
		"\u01511\u0001\u0000\u0000\u0000\"8?DHLTX[_fjnz\u0085\u008a\u008d\u0091"+
		"\u00a3\u00aa\u00af\u00ba\u00c5\u00d0\u00db\u00e6\u00f1\u00fc\u0107\u0112"+
		"\u011d\u0128\u0133\u013e\u014c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}