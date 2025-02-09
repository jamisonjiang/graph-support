// Generated from org\graphper\parser\grammar\HTMLParser.g4 by ANTLR 4.9.3
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
public class HTMLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

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
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
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
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
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
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
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
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3#\u0155\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\3\2\3\2\3\2\3\2\3\2\3\2\5\2;\n\2\3\3\3\3\3\3\7\3@\n\3\f\3\16"+
		"\3C\13\3\3\3\3\3\5\3G\n\3\3\3\3\3\5\3K\n\3\6\3M\n\3\r\3\16\3N\3\3\3\3"+
		"\3\3\3\3\3\3\3\4\5\4W\n\4\3\4\3\4\5\4[\n\4\3\4\5\4^\n\4\7\4`\n\4\f\4\16"+
		"\4c\13\4\3\5\3\5\3\5\3\5\5\5i\n\5\3\5\3\5\5\5m\n\5\6\5o\n\5\r\5\16\5p"+
		"\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\7\6{\n\6\f\6\16\6~\13\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\7\5\7\u0088\n\7\3\7\3\7\3\7\5\7\u008d\n\7\3\7\5\7\u0090"+
		"\n\7\7\7\u0092\n\7\f\7\16\7\u0095\13\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00a6\n\b\3\t\3\t\3\t\7\t\u00ab\n\t\f"+
		"\t\16\t\u00ae\13\t\3\t\3\t\5\t\u00b2\n\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3"+
		"\n\3\n\5\n\u00bd\n\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\5\13\u00c8"+
		"\n\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\5\f\u00d3\n\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\r\3\r\3\r\3\r\5\r\u00de\n\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16"+
		"\3\16\3\16\5\16\u00e9\n\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17"+
		"\5\17\u00f4\n\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\5\20\u00ff"+
		"\n\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\5\21\u010a\n\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\5\22\u0115\n\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\23\3\23\3\23\3\23\5\23\u0120\n\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\24\3\24\3\24\3\24\5\24\u012b\n\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25"+
		"\3\25\3\25\5\25\u0136\n\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26"+
		"\5\26\u0141\n\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\30\3\30"+
		"\3\30\5\30\u014f\n\30\3\31\3\31\3\32\3\32\3\32\2\2\33\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36 \"$&(*,.\60\62\2\4\4\2\5\5\7\7\3\2\3\4\2\u016b\2"+
		":\3\2\2\2\4<\3\2\2\2\6V\3\2\2\2\bd\3\2\2\2\nw\3\2\2\2\f\u0087\3\2\2\2"+
		"\16\u00a5\3\2\2\2\20\u00a7\3\2\2\2\22\u00b8\3\2\2\2\24\u00c3\3\2\2\2\26"+
		"\u00ce\3\2\2\2\30\u00d9\3\2\2\2\32\u00e4\3\2\2\2\34\u00ef\3\2\2\2\36\u00fa"+
		"\3\2\2\2 \u0105\3\2\2\2\"\u0110\3\2\2\2$\u011b\3\2\2\2&\u0126\3\2\2\2"+
		"(\u0131\3\2\2\2*\u013c\3\2\2\2,\u0147\3\2\2\2.\u014b\3\2\2\2\60\u0150"+
		"\3\2\2\2\62\u0152\3\2\2\2\64\65\5\4\3\2\65\66\7\2\2\3\66;\3\2\2\2\678"+
		"\5\6\4\289\7\2\2\39;\3\2\2\2:\64\3\2\2\2:\67\3\2\2\2;\3\3\2\2\2<=\7\6"+
		"\2\2=A\7\b\2\2>@\5.\30\2?>\3\2\2\2@C\3\2\2\2A?\3\2\2\2AB\3\2\2\2BD\3\2"+
		"\2\2CA\3\2\2\2DF\7\34\2\2EG\7\5\2\2FE\3\2\2\2FG\3\2\2\2GL\3\2\2\2HJ\5"+
		"\b\5\2IK\7\5\2\2JI\3\2\2\2JK\3\2\2\2KM\3\2\2\2LH\3\2\2\2MN\3\2\2\2NL\3"+
		"\2\2\2NO\3\2\2\2OP\3\2\2\2PQ\7\6\2\2QR\7\36\2\2RS\7\b\2\2ST\7\34\2\2T"+
		"\5\3\2\2\2UW\5\60\31\2VU\3\2\2\2VW\3\2\2\2Wa\3\2\2\2X[\5\16\b\2Y[\5\62"+
		"\32\2ZX\3\2\2\2ZY\3\2\2\2[]\3\2\2\2\\^\5\60\31\2]\\\3\2\2\2]^\3\2\2\2"+
		"^`\3\2\2\2_Z\3\2\2\2`c\3\2\2\2a_\3\2\2\2ab\3\2\2\2b\7\3\2\2\2ca\3\2\2"+
		"\2de\7\6\2\2ef\7\t\2\2fh\7\34\2\2gi\7\5\2\2hg\3\2\2\2hi\3\2\2\2in\3\2"+
		"\2\2jl\5\n\6\2km\7\5\2\2lk\3\2\2\2lm\3\2\2\2mo\3\2\2\2nj\3\2\2\2op\3\2"+
		"\2\2pn\3\2\2\2pq\3\2\2\2qr\3\2\2\2rs\7\6\2\2st\7\36\2\2tu\7\t\2\2uv\7"+
		"\34\2\2v\t\3\2\2\2wx\7\6\2\2x|\7\n\2\2y{\5.\30\2zy\3\2\2\2{~\3\2\2\2|"+
		"z\3\2\2\2|}\3\2\2\2}\177\3\2\2\2~|\3\2\2\2\177\u0080\7\34\2\2\u0080\u0081"+
		"\5\f\7\2\u0081\u0082\7\6\2\2\u0082\u0083\7\36\2\2\u0083\u0084\7\n\2\2"+
		"\u0084\u0085\7\34\2\2\u0085\13\3\2\2\2\u0086\u0088\5\60\31\2\u0087\u0086"+
		"\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u0093\3\2\2\2\u0089\u008d\5\4\3\2\u008a"+
		"\u008d\5\16\b\2\u008b\u008d\5\62\32\2\u008c\u0089\3\2\2\2\u008c\u008a"+
		"\3\2\2\2\u008c\u008b\3\2\2\2\u008d\u008f\3\2\2\2\u008e\u0090\5\60\31\2"+
		"\u008f\u008e\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0092\3\2\2\2\u0091\u008c"+
		"\3\2\2\2\u0092\u0095\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094"+
		"\r\3\2\2\2\u0095\u0093\3\2\2\2\u0096\u00a6\5\20\t\2\u0097\u00a6\5\22\n"+
		"\2\u0098\u00a6\5\24\13\2\u0099\u00a6\5\26\f\2\u009a\u00a6\5\30\r\2\u009b"+
		"\u00a6\5\32\16\2\u009c\u00a6\5\34\17\2\u009d\u00a6\5\36\20\2\u009e\u00a6"+
		"\5 \21\2\u009f\u00a6\5\"\22\2\u00a0\u00a6\5$\23\2\u00a1\u00a6\5&\24\2"+
		"\u00a2\u00a6\5(\25\2\u00a3\u00a6\5*\26\2\u00a4\u00a6\5,\27\2\u00a5\u0096"+
		"\3\2\2\2\u00a5\u0097\3\2\2\2\u00a5\u0098\3\2\2\2\u00a5\u0099\3\2\2\2\u00a5"+
		"\u009a\3\2\2\2\u00a5\u009b\3\2\2\2\u00a5\u009c\3\2\2\2\u00a5\u009d\3\2"+
		"\2\2\u00a5\u009e\3\2\2\2\u00a5\u009f\3\2\2\2\u00a5\u00a0\3\2\2\2\u00a5"+
		"\u00a1\3\2\2\2\u00a5\u00a2\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a4\3\2"+
		"\2\2\u00a6\17\3\2\2\2\u00a7\u00a8\7\6\2\2\u00a8\u00ac\7\13\2\2\u00a9\u00ab"+
		"\5.\30\2\u00aa\u00a9\3\2\2\2\u00ab\u00ae\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ac"+
		"\u00ad\3\2\2\2\u00ad\u00af\3\2\2\2\u00ae\u00ac\3\2\2\2\u00af\u00b1\7\34"+
		"\2\2\u00b0\u00b2\5\6\4\2\u00b1\u00b0\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2"+
		"\u00b3\3\2\2\2\u00b3\u00b4\7\6\2\2\u00b4\u00b5\7\36\2\2\u00b5\u00b6\7"+
		"\13\2\2\u00b6\u00b7\7\34\2\2\u00b7\21\3\2\2\2\u00b8\u00b9\7\6\2\2\u00b9"+
		"\u00ba\7\f\2\2\u00ba\u00bc\7\34\2\2\u00bb\u00bd\5\6\4\2\u00bc\u00bb\3"+
		"\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00bf\7\6\2\2\u00bf"+
		"\u00c0\7\36\2\2\u00c0\u00c1\7\f\2\2\u00c1\u00c2\7\34\2\2\u00c2\23\3\2"+
		"\2\2\u00c3\u00c4\7\6\2\2\u00c4\u00c5\7\r\2\2\u00c5\u00c7\7\34\2\2\u00c6"+
		"\u00c8\5\6\4\2\u00c7\u00c6\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\u00c9\3\2"+
		"\2\2\u00c9\u00ca\7\6\2\2\u00ca\u00cb\7\36\2\2\u00cb\u00cc\7\r\2\2\u00cc"+
		"\u00cd\7\34\2\2\u00cd\25\3\2\2\2\u00ce\u00cf\7\6\2\2\u00cf\u00d0\7\16"+
		"\2\2\u00d0\u00d2\7\34\2\2\u00d1\u00d3\5\6\4\2\u00d2\u00d1\3\2\2\2\u00d2"+
		"\u00d3\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d5\7\6\2\2\u00d5\u00d6\7\36"+
		"\2\2\u00d6\u00d7\7\16\2\2\u00d7\u00d8\7\34\2\2\u00d8\27\3\2\2\2\u00d9"+
		"\u00da\7\6\2\2\u00da\u00db\7\17\2\2\u00db\u00dd\7\34\2\2\u00dc\u00de\5"+
		"\6\4\2\u00dd\u00dc\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00df\3\2\2\2\u00df"+
		"\u00e0\7\6\2\2\u00e0\u00e1\7\36\2\2\u00e1\u00e2\7\17\2\2\u00e2\u00e3\7"+
		"\34\2\2\u00e3\31\3\2\2\2\u00e4\u00e5\7\6\2\2\u00e5\u00e6\7\20\2\2\u00e6"+
		"\u00e8\7\34\2\2\u00e7\u00e9\5\6\4\2\u00e8\u00e7\3\2\2\2\u00e8\u00e9\3"+
		"\2\2\2\u00e9\u00ea\3\2\2\2\u00ea\u00eb\7\6\2\2\u00eb\u00ec\7\36\2\2\u00ec"+
		"\u00ed\7\20\2\2\u00ed\u00ee\7\34\2\2\u00ee\33\3\2\2\2\u00ef\u00f0\7\6"+
		"\2\2\u00f0\u00f1\7\21\2\2\u00f1\u00f3\7\34\2\2\u00f2\u00f4\5\6\4\2\u00f3"+
		"\u00f2\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f6\7\6"+
		"\2\2\u00f6\u00f7\7\36\2\2\u00f7\u00f8\7\21\2\2\u00f8\u00f9\7\34\2\2\u00f9"+
		"\35\3\2\2\2\u00fa\u00fb\7\6\2\2\u00fb\u00fc\7\22\2\2\u00fc\u00fe\7\34"+
		"\2\2\u00fd\u00ff\5\6\4\2\u00fe\u00fd\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff"+
		"\u0100\3\2\2\2\u0100\u0101\7\6\2\2\u0101\u0102\7\36\2\2\u0102\u0103\7"+
		"\22\2\2\u0103\u0104\7\34\2\2\u0104\37\3\2\2\2\u0105\u0106\7\6\2\2\u0106"+
		"\u0107\7\24\2\2\u0107\u0109\7\34\2\2\u0108\u010a\5\6\4\2\u0109\u0108\3"+
		"\2\2\2\u0109\u010a\3\2\2\2\u010a\u010b\3\2\2\2\u010b\u010c\7\6\2\2\u010c"+
		"\u010d\7\36\2\2\u010d\u010e\7\24\2\2\u010e\u010f\7\34\2\2\u010f!\3\2\2"+
		"\2\u0110\u0111\7\6\2\2\u0111\u0112\7\25\2\2\u0112\u0114\7\34\2\2\u0113"+
		"\u0115\5\6\4\2\u0114\u0113\3\2\2\2\u0114\u0115\3\2\2\2\u0115\u0116\3\2"+
		"\2\2\u0116\u0117\7\6\2\2\u0117\u0118\7\36\2\2\u0118\u0119\7\25\2\2\u0119"+
		"\u011a\7\34\2\2\u011a#\3\2\2\2\u011b\u011c\7\6\2\2\u011c\u011d\7\26\2"+
		"\2\u011d\u011f\7\34\2\2\u011e\u0120\5\6\4\2\u011f\u011e\3\2\2\2\u011f"+
		"\u0120\3\2\2\2\u0120\u0121\3\2\2\2\u0121\u0122\7\6\2\2\u0122\u0123\7\36"+
		"\2\2\u0123\u0124\7\26\2\2\u0124\u0125\7\34\2\2\u0125%\3\2\2\2\u0126\u0127"+
		"\7\6\2\2\u0127\u0128\7\27\2\2\u0128\u012a\7\34\2\2\u0129\u012b\5\6\4\2"+
		"\u012a\u0129\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u012c\3\2\2\2\u012c\u012d"+
		"\7\6\2\2\u012d\u012e\7\36\2\2\u012e\u012f\7\27\2\2\u012f\u0130\7\34\2"+
		"\2\u0130\'\3\2\2\2\u0131\u0132\7\6\2\2\u0132\u0133\7\30\2\2\u0133\u0135"+
		"\7\34\2\2\u0134\u0136\5\6\4\2\u0135\u0134\3\2\2\2\u0135\u0136\3\2\2\2"+
		"\u0136\u0137\3\2\2\2\u0137\u0138\7\6\2\2\u0138\u0139\7\36\2\2\u0139\u013a"+
		"\7\30\2\2\u013a\u013b\7\34\2\2\u013b)\3\2\2\2\u013c\u013d\7\6\2\2\u013d"+
		"\u013e\7\31\2\2\u013e\u0140\7\34\2\2\u013f\u0141\5\6\4\2\u0140\u013f\3"+
		"\2\2\2\u0140\u0141\3\2\2\2\u0141\u0142\3\2\2\2\u0142\u0143\7\6\2\2\u0143"+
		"\u0144\7\36\2\2\u0144\u0145\7\31\2\2\u0145\u0146\7\34\2\2\u0146+\3\2\2"+
		"\2\u0147\u0148\7\6\2\2\u0148\u0149\7\23\2\2\u0149\u014a\7\35\2\2\u014a"+
		"-\3\2\2\2\u014b\u014e\7 \2\2\u014c\u014d\7\37\2\2\u014d\u014f\7\"\2\2"+
		"\u014e\u014c\3\2\2\2\u014e\u014f\3\2\2\2\u014f/\3\2\2\2\u0150\u0151\t"+
		"\2\2\2\u0151\61\3\2\2\2\u0152\u0153\t\3\2\2\u0153\63\3\2\2\2$:AFJNVZ]"+
		"ahlp|\u0087\u008c\u008f\u0093\u00a5\u00ac\u00b1\u00bc\u00c7\u00d2\u00dd"+
		"\u00e8\u00f3\u00fe\u0109\u0114\u011f\u012a\u0135\u0140\u014e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}