// Generated from org\graphper\parser\grammar\TABLEParser.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TABLEParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HTML_CONDITIONAL_COMMENT=1, HTML_COMMENT=2, WS=3, TAG_OPEN=4, HTML_TEXT=5, 
		TABLE=6, TR=7, TD=8, FONT=9, B=10, I=11, U=12, SUB=13, SUP=14, BR=15, 
		HR=16, IMG=17, CENTER=18, COMMA=19, SEMI_COLON=20, TAG_CLOSE=21, TAG_SLASH_CLOSE=22, 
		TAG_SLASH=23, TAG_EQUALS=24, TAG_NAME=25, TAG_WHITESPACE=26, ATTVALUE_VALUE=27, 
		ATTRIBUTE=28;
	public static final int
		RULE_table = 0, RULE_tr = 1, RULE_td = 2, RULE_tdContent = 3, RULE_fontContent = 4, 
		RULE_htmlElement = 5, RULE_fontTag = 6, RULE_bTag = 7, RULE_iTag = 8, 
		RULE_uTag = 9, RULE_subTag = 10, RULE_supTag = 11, RULE_brTag = 12, RULE_hrTag = 13, 
		RULE_imgTag = 14, RULE_centerTag = 15, RULE_tableTag = 16, RULE_htmlAttribute = 17, 
		RULE_htmlChardata = 18, RULE_htmlComment = 19;
	private static String[] makeRuleNames() {
		return new String[] {
			"table", "tr", "td", "tdContent", "fontContent", "htmlElement", "fontTag", 
			"bTag", "iTag", "uTag", "subTag", "supTag", "brTag", "hrTag", "imgTag", 
			"centerTag", "tableTag", "htmlAttribute", "htmlChardata", "htmlComment"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'<'", null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, "','", "';'", "'>'", "'/>'", 
			"'/'", "'='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HTML_CONDITIONAL_COMMENT", "HTML_COMMENT", "WS", "TAG_OPEN", "HTML_TEXT", 
			"TABLE", "TR", "TD", "FONT", "B", "I", "U", "SUB", "SUP", "BR", "HR", 
			"IMG", "CENTER", "COMMA", "SEMI_COLON", "TAG_CLOSE", "TAG_SLASH_CLOSE", 
			"TAG_SLASH", "TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", "ATTVALUE_VALUE", 
			"ATTRIBUTE"
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
			setState(40);
			match(TAG_OPEN);
			setState(41);
			match(TABLE);
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(42);
				htmlAttribute();
				}
				}
				setState(47);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(48);
			match(TAG_CLOSE);
			setState(50);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(49);
				match(WS);
				}
			}

			setState(56); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(52);
					tr();
					setState(54);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(53);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(58); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			setState(60);
			match(TAG_OPEN);
			setState(61);
			match(TAG_SLASH);
			setState(62);
			match(TABLE);
			setState(63);
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
			setState(65);
			match(TAG_OPEN);
			setState(66);
			match(TR);
			setState(67);
			match(TAG_CLOSE);
			setState(69);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(68);
				match(WS);
				}
			}

			setState(75); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(71);
					td();
					setState(73);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(72);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(77); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			setState(79);
			match(TAG_OPEN);
			setState(80);
			match(TAG_SLASH);
			setState(81);
			match(TR);
			setState(82);
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
		public TdContentContext tdContent() {
			return getRuleContext(TdContentContext.class,0);
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
			setState(84);
			match(TAG_OPEN);
			setState(85);
			match(TD);
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(86);
				htmlAttribute();
				}
				}
				setState(91);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(92);
			match(TAG_CLOSE);
			setState(93);
			tdContent();
			setState(94);
			match(TAG_OPEN);
			setState(95);
			match(TAG_SLASH);
			setState(96);
			match(TD);
			setState(97);
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
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterTdContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitTdContent(this);
		}
	}

	public final TdContentContext tdContent() throws RecognitionException {
		TdContentContext _localctx = new TdContentContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tdContent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS || _la==HTML_TEXT) {
				{
				setState(99);
				htmlChardata();
				}
			}

			setState(112);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(105);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						setState(102);
						table();
						}
						break;
					case 2:
						{
						setState(103);
						htmlElement();
						}
						break;
					case 3:
						{
						setState(104);
						htmlComment();
						}
						break;
					}
					setState(108);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS || _la==HTML_TEXT) {
						{
						setState(107);
						htmlChardata();
						}
					}

					}
					} 
				}
				setState(114);
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

	public static class FontContentContext extends ParserRuleContext {
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
		public FontContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fontContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterFontContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitFontContent(this);
		}
	}

	public final FontContentContext fontContent() throws RecognitionException {
		FontContentContext _localctx = new FontContentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_fontContent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS || _la==HTML_TEXT) {
				{
				setState(115);
				htmlChardata();
				}
			}

			setState(124);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(118);
					htmlElement();
					setState(120);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS || _la==HTML_TEXT) {
						{
						setState(119);
						htmlChardata();
						}
					}

					}
					} 
				}
				setState(126);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
		public SubTagContext subTag() {
			return getRuleContext(SubTagContext.class,0);
		}
		public SupTagContext supTag() {
			return getRuleContext(SupTagContext.class,0);
		}
		public BrTagContext brTag() {
			return getRuleContext(BrTagContext.class,0);
		}
		public HrTagContext hrTag() {
			return getRuleContext(HrTagContext.class,0);
		}
		public ImgTagContext imgTag() {
			return getRuleContext(ImgTagContext.class,0);
		}
		public CenterTagContext centerTag() {
			return getRuleContext(CenterTagContext.class,0);
		}
		public HtmlElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_htmlElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterHtmlElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitHtmlElement(this);
		}
	}

	public final HtmlElementContext htmlElement() throws RecognitionException {
		HtmlElementContext _localctx = new HtmlElementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_htmlElement);
		try {
			setState(137);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(127);
				fontTag();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(128);
				bTag();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(129);
				iTag();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(130);
				uTag();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(131);
				subTag();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(132);
				supTag();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(133);
				brTag();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(134);
				hrTag();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(135);
				imgTag();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(136);
				centerTag();
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

	public static class FontTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> FONT() { return getTokens(TABLEParser.FONT); }
		public TerminalNode FONT(int i) {
			return getToken(TABLEParser.FONT, i);
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
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public FontTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fontTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterFontTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitFontTag(this);
		}
	}

	public final FontTagContext fontTag() throws RecognitionException {
		FontTagContext _localctx = new FontTagContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_fontTag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(TAG_OPEN);
			setState(140);
			match(FONT);
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(141);
				htmlAttribute();
				}
				}
				setState(146);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(147);
			match(TAG_CLOSE);
			setState(149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(148);
				fontContent();
				}
				break;
			}
			setState(151);
			match(TAG_OPEN);
			setState(152);
			match(TAG_SLASH);
			setState(153);
			match(FONT);
			setState(154);
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

	public static class BTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> B() { return getTokens(TABLEParser.B); }
		public TerminalNode B(int i) {
			return getToken(TABLEParser.B, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public BTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterBTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitBTag(this);
		}
	}

	public final BTagContext bTag() throws RecognitionException {
		BTagContext _localctx = new BTagContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_bTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			match(TAG_OPEN);
			setState(157);
			match(B);
			setState(158);
			match(TAG_CLOSE);
			setState(160);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(159);
				fontContent();
				}
				break;
			}
			setState(162);
			match(TAG_OPEN);
			setState(163);
			match(TAG_SLASH);
			setState(164);
			match(B);
			setState(165);
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

	public static class ITagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> I() { return getTokens(TABLEParser.I); }
		public TerminalNode I(int i) {
			return getToken(TABLEParser.I, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public ITagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterITag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitITag(this);
		}
	}

	public final ITagContext iTag() throws RecognitionException {
		ITagContext _localctx = new ITagContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_iTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			match(TAG_OPEN);
			setState(168);
			match(I);
			setState(169);
			match(TAG_CLOSE);
			setState(171);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(170);
				fontContent();
				}
				break;
			}
			setState(173);
			match(TAG_OPEN);
			setState(174);
			match(TAG_SLASH);
			setState(175);
			match(I);
			setState(176);
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

	public static class UTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> U() { return getTokens(TABLEParser.U); }
		public TerminalNode U(int i) {
			return getToken(TABLEParser.U, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public UTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterUTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitUTag(this);
		}
	}

	public final UTagContext uTag() throws RecognitionException {
		UTagContext _localctx = new UTagContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_uTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			match(TAG_OPEN);
			setState(179);
			match(U);
			setState(180);
			match(TAG_CLOSE);
			setState(182);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				setState(181);
				fontContent();
				}
				break;
			}
			setState(184);
			match(TAG_OPEN);
			setState(185);
			match(TAG_SLASH);
			setState(186);
			match(U);
			setState(187);
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

	public static class SubTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> SUB() { return getTokens(TABLEParser.SUB); }
		public TerminalNode SUB(int i) {
			return getToken(TABLEParser.SUB, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public SubTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterSubTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitSubTag(this);
		}
	}

	public final SubTagContext subTag() throws RecognitionException {
		SubTagContext _localctx = new SubTagContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_subTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(TAG_OPEN);
			setState(190);
			match(SUB);
			setState(191);
			match(TAG_CLOSE);
			setState(193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(192);
				fontContent();
				}
				break;
			}
			setState(195);
			match(TAG_OPEN);
			setState(196);
			match(TAG_SLASH);
			setState(197);
			match(SUB);
			setState(198);
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

	public static class SupTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> SUP() { return getTokens(TABLEParser.SUP); }
		public TerminalNode SUP(int i) {
			return getToken(TABLEParser.SUP, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public SupTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_supTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterSupTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitSupTag(this);
		}
	}

	public final SupTagContext supTag() throws RecognitionException {
		SupTagContext _localctx = new SupTagContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_supTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(TAG_OPEN);
			setState(201);
			match(SUP);
			setState(202);
			match(TAG_CLOSE);
			setState(204);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(203);
				fontContent();
				}
				break;
			}
			setState(206);
			match(TAG_OPEN);
			setState(207);
			match(TAG_SLASH);
			setState(208);
			match(SUP);
			setState(209);
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

	public static class BrTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPEN() { return getToken(TABLEParser.TAG_OPEN, 0); }
		public TerminalNode BR() { return getToken(TABLEParser.BR, 0); }
		public TerminalNode TAG_SLASH_CLOSE() { return getToken(TABLEParser.TAG_SLASH_CLOSE, 0); }
		public BrTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterBrTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitBrTag(this);
		}
	}

	public final BrTagContext brTag() throws RecognitionException {
		BrTagContext _localctx = new BrTagContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_brTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			match(TAG_OPEN);
			setState(212);
			match(BR);
			setState(213);
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

	public static class HrTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPEN() { return getToken(TABLEParser.TAG_OPEN, 0); }
		public TerminalNode HR() { return getToken(TABLEParser.HR, 0); }
		public TerminalNode TAG_SLASH_CLOSE() { return getToken(TABLEParser.TAG_SLASH_CLOSE, 0); }
		public HrTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hrTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterHrTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitHrTag(this);
		}
	}

	public final HrTagContext hrTag() throws RecognitionException {
		HrTagContext _localctx = new HrTagContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_hrTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			match(TAG_OPEN);
			setState(216);
			match(HR);
			setState(217);
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

	public static class ImgTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPEN() { return getToken(TABLEParser.TAG_OPEN, 0); }
		public TerminalNode IMG() { return getToken(TABLEParser.IMG, 0); }
		public TerminalNode TAG_SLASH_CLOSE() { return getToken(TABLEParser.TAG_SLASH_CLOSE, 0); }
		public List<HtmlAttributeContext> htmlAttribute() {
			return getRuleContexts(HtmlAttributeContext.class);
		}
		public HtmlAttributeContext htmlAttribute(int i) {
			return getRuleContext(HtmlAttributeContext.class,i);
		}
		public ImgTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imgTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterImgTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitImgTag(this);
		}
	}

	public final ImgTagContext imgTag() throws RecognitionException {
		ImgTagContext _localctx = new ImgTagContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_imgTag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			match(TAG_OPEN);
			setState(220);
			match(IMG);
			setState(224);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(221);
				htmlAttribute();
				}
				}
				setState(226);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(227);
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

	public static class CenterTagContext extends ParserRuleContext {
		public List<TerminalNode> TAG_OPEN() { return getTokens(TABLEParser.TAG_OPEN); }
		public TerminalNode TAG_OPEN(int i) {
			return getToken(TABLEParser.TAG_OPEN, i);
		}
		public List<TerminalNode> CENTER() { return getTokens(TABLEParser.CENTER); }
		public TerminalNode CENTER(int i) {
			return getToken(TABLEParser.CENTER, i);
		}
		public List<TerminalNode> TAG_CLOSE() { return getTokens(TABLEParser.TAG_CLOSE); }
		public TerminalNode TAG_CLOSE(int i) {
			return getToken(TABLEParser.TAG_CLOSE, i);
		}
		public TerminalNode TAG_SLASH() { return getToken(TABLEParser.TAG_SLASH, 0); }
		public FontContentContext fontContent() {
			return getRuleContext(FontContentContext.class,0);
		}
		public CenterTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_centerTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterCenterTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitCenterTag(this);
		}
	}

	public final CenterTagContext centerTag() throws RecognitionException {
		CenterTagContext _localctx = new CenterTagContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_centerTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(TAG_OPEN);
			setState(230);
			match(CENTER);
			setState(231);
			match(TAG_CLOSE);
			setState(233);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(232);
				fontContent();
				}
				break;
			}
			setState(235);
			match(TAG_OPEN);
			setState(236);
			match(TAG_SLASH);
			setState(237);
			match(CENTER);
			setState(238);
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

	public static class TableTagContext extends ParserRuleContext {
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
		public List<TrContext> tr() {
			return getRuleContexts(TrContext.class);
		}
		public TrContext tr(int i) {
			return getRuleContext(TrContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(TABLEParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(TABLEParser.WS, i);
		}
		public TableTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).enterTableTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TABLEParserListener ) ((TABLEParserListener)listener).exitTableTag(this);
		}
	}

	public final TableTagContext tableTag() throws RecognitionException {
		TableTagContext _localctx = new TableTagContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_tableTag);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
			match(TAG_OPEN);
			setState(241);
			match(TABLE);
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==TAG_NAME) {
				{
				{
				setState(242);
				htmlAttribute();
				}
				}
				setState(247);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(248);
			match(TAG_CLOSE);
			setState(253); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(249);
					tr();
					setState(251);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(250);
						match(WS);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(255); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			setState(257);
			match(TAG_OPEN);
			setState(258);
			match(TAG_SLASH);
			setState(259);
			match(TABLE);
			setState(260);
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
		enterRule(_localctx, 34, RULE_htmlAttribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(262);
			match(TAG_NAME);
			setState(265);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_EQUALS) {
				{
				setState(263);
				match(TAG_EQUALS);
				setState(264);
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
		enterRule(_localctx, 36, RULE_htmlChardata);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
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
		enterRule(_localctx, 38, RULE_htmlComment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(269);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\36\u0112\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\2\7\2.\n\2\f\2\16\2\61\13\2\3"+
		"\2\3\2\5\2\65\n\2\3\2\3\2\5\29\n\2\6\2;\n\2\r\2\16\2<\3\2\3\2\3\2\3\2"+
		"\3\2\3\3\3\3\3\3\3\3\5\3H\n\3\3\3\3\3\5\3L\n\3\6\3N\n\3\r\3\16\3O\3\3"+
		"\3\3\3\3\3\3\3\3\3\4\3\4\3\4\7\4Z\n\4\f\4\16\4]\13\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\5\5\5g\n\5\3\5\3\5\3\5\5\5l\n\5\3\5\5\5o\n\5\7\5q\n\5\f\5"+
		"\16\5t\13\5\3\6\5\6w\n\6\3\6\3\6\5\6{\n\6\7\6}\n\6\f\6\16\6\u0080\13\6"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u008c\n\7\3\b\3\b\3\b\7\b"+
		"\u0091\n\b\f\b\16\b\u0094\13\b\3\b\3\b\5\b\u0098\n\b\3\b\3\b\3\b\3\b\3"+
		"\b\3\t\3\t\3\t\3\t\5\t\u00a3\n\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\5"+
		"\n\u00ae\n\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\5\13\u00b9\n\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\5\f\u00c4\n\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\r\3\r\3\r\3\r\5\r\u00cf\n\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16"+
		"\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\7\20\u00e1\n\20\f\20\16\20\u00e4"+
		"\13\20\3\20\3\20\3\21\3\21\3\21\3\21\5\21\u00ec\n\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\22\3\22\3\22\7\22\u00f6\n\22\f\22\16\22\u00f9\13\22\3\22\3"+
		"\22\3\22\5\22\u00fe\n\22\6\22\u0100\n\22\r\22\16\22\u0101\3\22\3\22\3"+
		"\22\3\22\3\22\3\23\3\23\3\23\5\23\u010c\n\23\3\24\3\24\3\25\3\25\3\25"+
		"\2\2\26\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(\2\4\4\2\5\5\7\7\3"+
		"\2\3\4\2\u0123\2*\3\2\2\2\4C\3\2\2\2\6V\3\2\2\2\bf\3\2\2\2\nv\3\2\2\2"+
		"\f\u008b\3\2\2\2\16\u008d\3\2\2\2\20\u009e\3\2\2\2\22\u00a9\3\2\2\2\24"+
		"\u00b4\3\2\2\2\26\u00bf\3\2\2\2\30\u00ca\3\2\2\2\32\u00d5\3\2\2\2\34\u00d9"+
		"\3\2\2\2\36\u00dd\3\2\2\2 \u00e7\3\2\2\2\"\u00f2\3\2\2\2$\u0108\3\2\2"+
		"\2&\u010d\3\2\2\2(\u010f\3\2\2\2*+\7\6\2\2+/\7\b\2\2,.\5$\23\2-,\3\2\2"+
		"\2.\61\3\2\2\2/-\3\2\2\2/\60\3\2\2\2\60\62\3\2\2\2\61/\3\2\2\2\62\64\7"+
		"\27\2\2\63\65\7\5\2\2\64\63\3\2\2\2\64\65\3\2\2\2\65:\3\2\2\2\668\5\4"+
		"\3\2\679\7\5\2\28\67\3\2\2\289\3\2\2\29;\3\2\2\2:\66\3\2\2\2;<\3\2\2\2"+
		"<:\3\2\2\2<=\3\2\2\2=>\3\2\2\2>?\7\6\2\2?@\7\31\2\2@A\7\b\2\2AB\7\27\2"+
		"\2B\3\3\2\2\2CD\7\6\2\2DE\7\t\2\2EG\7\27\2\2FH\7\5\2\2GF\3\2\2\2GH\3\2"+
		"\2\2HM\3\2\2\2IK\5\6\4\2JL\7\5\2\2KJ\3\2\2\2KL\3\2\2\2LN\3\2\2\2MI\3\2"+
		"\2\2NO\3\2\2\2OM\3\2\2\2OP\3\2\2\2PQ\3\2\2\2QR\7\6\2\2RS\7\31\2\2ST\7"+
		"\t\2\2TU\7\27\2\2U\5\3\2\2\2VW\7\6\2\2W[\7\n\2\2XZ\5$\23\2YX\3\2\2\2Z"+
		"]\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\^\3\2\2\2][\3\2\2\2^_\7\27\2\2_`\5\b\5"+
		"\2`a\7\6\2\2ab\7\31\2\2bc\7\n\2\2cd\7\27\2\2d\7\3\2\2\2eg\5&\24\2fe\3"+
		"\2\2\2fg\3\2\2\2gr\3\2\2\2hl\5\2\2\2il\5\f\7\2jl\5(\25\2kh\3\2\2\2ki\3"+
		"\2\2\2kj\3\2\2\2ln\3\2\2\2mo\5&\24\2nm\3\2\2\2no\3\2\2\2oq\3\2\2\2pk\3"+
		"\2\2\2qt\3\2\2\2rp\3\2\2\2rs\3\2\2\2s\t\3\2\2\2tr\3\2\2\2uw\5&\24\2vu"+
		"\3\2\2\2vw\3\2\2\2w~\3\2\2\2xz\5\f\7\2y{\5&\24\2zy\3\2\2\2z{\3\2\2\2{"+
		"}\3\2\2\2|x\3\2\2\2}\u0080\3\2\2\2~|\3\2\2\2~\177\3\2\2\2\177\13\3\2\2"+
		"\2\u0080~\3\2\2\2\u0081\u008c\5\16\b\2\u0082\u008c\5\20\t\2\u0083\u008c"+
		"\5\22\n\2\u0084\u008c\5\24\13\2\u0085\u008c\5\26\f\2\u0086\u008c\5\30"+
		"\r\2\u0087\u008c\5\32\16\2\u0088\u008c\5\34\17\2\u0089\u008c\5\36\20\2"+
		"\u008a\u008c\5 \21\2\u008b\u0081\3\2\2\2\u008b\u0082\3\2\2\2\u008b\u0083"+
		"\3\2\2\2\u008b\u0084\3\2\2\2\u008b\u0085\3\2\2\2\u008b\u0086\3\2\2\2\u008b"+
		"\u0087\3\2\2\2\u008b\u0088\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008a\3\2"+
		"\2\2\u008c\r\3\2\2\2\u008d\u008e\7\6\2\2\u008e\u0092\7\13\2\2\u008f\u0091"+
		"\5$\23\2\u0090\u008f\3\2\2\2\u0091\u0094\3\2\2\2\u0092\u0090\3\2\2\2\u0092"+
		"\u0093\3\2\2\2\u0093\u0095\3\2\2\2\u0094\u0092\3\2\2\2\u0095\u0097\7\27"+
		"\2\2\u0096\u0098\5\n\6\2\u0097\u0096\3\2\2\2\u0097\u0098\3\2\2\2\u0098"+
		"\u0099\3\2\2\2\u0099\u009a\7\6\2\2\u009a\u009b\7\31\2\2\u009b\u009c\7"+
		"\13\2\2\u009c\u009d\7\27\2\2\u009d\17\3\2\2\2\u009e\u009f\7\6\2\2\u009f"+
		"\u00a0\7\f\2\2\u00a0\u00a2\7\27\2\2\u00a1\u00a3\5\n\6\2\u00a2\u00a1\3"+
		"\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a5\7\6\2\2\u00a5"+
		"\u00a6\7\31\2\2\u00a6\u00a7\7\f\2\2\u00a7\u00a8\7\27\2\2\u00a8\21\3\2"+
		"\2\2\u00a9\u00aa\7\6\2\2\u00aa\u00ab\7\r\2\2\u00ab\u00ad\7\27\2\2\u00ac"+
		"\u00ae\5\n\6\2\u00ad\u00ac\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00af\3\2"+
		"\2\2\u00af\u00b0\7\6\2\2\u00b0\u00b1\7\31\2\2\u00b1\u00b2\7\r\2\2\u00b2"+
		"\u00b3\7\27\2\2\u00b3\23\3\2\2\2\u00b4\u00b5\7\6\2\2\u00b5\u00b6\7\16"+
		"\2\2\u00b6\u00b8\7\27\2\2\u00b7\u00b9\5\n\6\2\u00b8\u00b7\3\2\2\2\u00b8"+
		"\u00b9\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00bb\7\6\2\2\u00bb\u00bc\7\31"+
		"\2\2\u00bc\u00bd\7\16\2\2\u00bd\u00be\7\27\2\2\u00be\25\3\2\2\2\u00bf"+
		"\u00c0\7\6\2\2\u00c0\u00c1\7\17\2\2\u00c1\u00c3\7\27\2\2\u00c2\u00c4\5"+
		"\n\6\2\u00c3\u00c2\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5"+
		"\u00c6\7\6\2\2\u00c6\u00c7\7\31\2\2\u00c7\u00c8\7\17\2\2\u00c8\u00c9\7"+
		"\27\2\2\u00c9\27\3\2\2\2\u00ca\u00cb\7\6\2\2\u00cb\u00cc\7\20\2\2\u00cc"+
		"\u00ce\7\27\2\2\u00cd\u00cf\5\n\6\2\u00ce\u00cd\3\2\2\2\u00ce\u00cf\3"+
		"\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d1\7\6\2\2\u00d1\u00d2\7\31\2\2\u00d2"+
		"\u00d3\7\20\2\2\u00d3\u00d4\7\27\2\2\u00d4\31\3\2\2\2\u00d5\u00d6\7\6"+
		"\2\2\u00d6\u00d7\7\21\2\2\u00d7\u00d8\7\30\2\2\u00d8\33\3\2\2\2\u00d9"+
		"\u00da\7\6\2\2\u00da\u00db\7\22\2\2\u00db\u00dc\7\30\2\2\u00dc\35\3\2"+
		"\2\2\u00dd\u00de\7\6\2\2\u00de\u00e2\7\23\2\2\u00df\u00e1\5$\23\2\u00e0"+
		"\u00df\3\2\2\2\u00e1\u00e4\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e2\u00e3\3\2"+
		"\2\2\u00e3\u00e5\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e5\u00e6\7\30\2\2\u00e6"+
		"\37\3\2\2\2\u00e7\u00e8\7\6\2\2\u00e8\u00e9\7\24\2\2\u00e9\u00eb\7\27"+
		"\2\2\u00ea\u00ec\5\n\6\2\u00eb\u00ea\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec"+
		"\u00ed\3\2\2\2\u00ed\u00ee\7\6\2\2\u00ee\u00ef\7\31\2\2\u00ef\u00f0\7"+
		"\24\2\2\u00f0\u00f1\7\27\2\2\u00f1!\3\2\2\2\u00f2\u00f3\7\6\2\2\u00f3"+
		"\u00f7\7\b\2\2\u00f4\u00f6\5$\23\2\u00f5\u00f4\3\2\2\2\u00f6\u00f9\3\2"+
		"\2\2\u00f7\u00f5\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8\u00fa\3\2\2\2\u00f9"+
		"\u00f7\3\2\2\2\u00fa\u00ff\7\27\2\2\u00fb\u00fd\5\4\3\2\u00fc\u00fe\7"+
		"\5\2\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe\u0100\3\2\2\2\u00ff"+
		"\u00fb\3\2\2\2\u0100\u0101\3\2\2\2\u0101\u00ff\3\2\2\2\u0101\u0102\3\2"+
		"\2\2\u0102\u0103\3\2\2\2\u0103\u0104\7\6\2\2\u0104\u0105\7\31\2\2\u0105"+
		"\u0106\7\b\2\2\u0106\u0107\7\27\2\2\u0107#\3\2\2\2\u0108\u010b\7\33\2"+
		"\2\u0109\u010a\7\32\2\2\u010a\u010c\7\35\2\2\u010b\u0109\3\2\2\2\u010b"+
		"\u010c\3\2\2\2\u010c%\3\2\2\2\u010d\u010e\t\2\2\2\u010e\'\3\2\2\2\u010f"+
		"\u0110\t\3\2\2\u0110)\3\2\2\2\37/\648<GKO[fknrvz~\u008b\u0092\u0097\u00a2"+
		"\u00ad\u00b8\u00c3\u00ce\u00e2\u00eb\u00f7\u00fd\u0101\u010b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}