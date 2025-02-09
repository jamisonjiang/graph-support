// Generated from org\graphper\parser\grammar\DOTParser.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;

import java.util.List;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuleContext;
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
public class DOTParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		STRICT=1, GRAPH=2, DIGRAPH=3, NODE=4, EDGE=5, SUBGRAPH=6, TABLE=7, TR=8, 
		TD=9, NUMBER=10, STRING=11, HTML_STRING=12, ID=13, COMMENT=14, LINE_COMMENT=15, 
		PREPROC=16, WS=17, LB=18, RB=19, LSB=20, RSB=21, COLON=22, SEMI_COLON=23, 
		COMMA=24, EQUAL=25, SLASH=26, DA=27, UDA=28;
	public static final int
		RULE_graphs = 0, RULE_graph = 1, RULE_stmt_list = 2, RULE_stmt = 3, RULE_attr_stmt = 4, 
		RULE_attr_list = 5, RULE_graph_a_list = 6, RULE_a_list = 7, RULE_edge_stmt = 8, 
		RULE_edgeRHS = 9, RULE_edgeop = 10, RULE_node_stmt = 11, RULE_node_id = 12, 
		RULE_port = 13, RULE_subgraph = 14, RULE_id_ = 15;
	private static String[] makeRuleNames() {
		return new String[] {
			"graphs", "graph", "stmt_list", "stmt", "attr_stmt", "attr_list", "graph_a_list", 
			"a_list", "edge_stmt", "edgeRHS", "edgeop", "node_stmt", "node_id", "port", 
			"subgraph", "id_"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "'{'", "'}'", "'['", "']'", "':'", 
			"';'", "','", "'='", "'/'", "'->'", "'--'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "STRICT", "GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "TABLE", 
			"TR", "TD", "NUMBER", "STRING", "HTML_STRING", "ID", "COMMENT", "LINE_COMMENT", 
			"PREPROC", "WS", "LB", "RB", "LSB", "RSB", "COLON", "SEMI_COLON", "COMMA", 
			"EQUAL", "SLASH", "DA", "UDA"
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
	public String getGrammarFileName() { return "DOTParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	    // Variable to track if the current graph is directed
	    boolean directed = false;

	public DOTParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class GraphsContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(DOTParser.EOF, 0); }
		public List<GraphContext> graph() {
			return getRuleContexts(GraphContext.class);
		}
		public GraphContext graph(int i) {
			return getRuleContext(GraphContext.class,i);
		}
		public GraphsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterGraphs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitGraphs(this);
		}
	}

	public final GraphsContext graphs() throws RecognitionException {
		GraphsContext _localctx = new GraphsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_graphs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(32);
				graph();
				}
				}
				setState(35); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRICT) | (1L << GRAPH) | (1L << DIGRAPH))) != 0) );
			setState(37);
			match(EOF);
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

	public static class GraphContext extends ParserRuleContext {
		public TerminalNode LB() { return getToken(DOTParser.LB, 0); }
		public Stmt_listContext stmt_list() {
			return getRuleContext(Stmt_listContext.class,0);
		}
		public TerminalNode RB() { return getToken(DOTParser.RB, 0); }
		public TerminalNode GRAPH() { return getToken(DOTParser.GRAPH, 0); }
		public TerminalNode DIGRAPH() { return getToken(DOTParser.DIGRAPH, 0); }
		public TerminalNode STRICT() { return getToken(DOTParser.STRICT, 0); }
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public GraphContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graph; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterGraph(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitGraph(this);
		}
	}

	public final GraphContext graph() throws RecognitionException {
		GraphContext _localctx = new GraphContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_graph);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRICT) {
				{
				setState(39);
				match(STRICT);
				}
			}

			setState(46);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GRAPH:
				{
				setState(42);
				match(GRAPH);
				 directed = false; 
				}
				break;
			case DIGRAPH:
				{
				setState(44);
				match(DIGRAPH);
				 directed = true; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(49);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << STRING) | (1L << HTML_STRING) | (1L << ID))) != 0)) {
				{
				setState(48);
				id_();
				}
			}

			setState(51);
			match(LB);
			setState(52);
			stmt_list();
			setState(53);
			match(RB);
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

	public static class Stmt_listContext extends ParserRuleContext {
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DOTParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DOTParser.COMMA, i);
		}
		public List<TerminalNode> SEMI_COLON() { return getTokens(DOTParser.SEMI_COLON); }
		public TerminalNode SEMI_COLON(int i) {
			return getToken(DOTParser.SEMI_COLON, i);
		}
		public Stmt_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterStmt_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitStmt_list(this);
		}
	}

	public final Stmt_listContext stmt_list() throws RecognitionException {
		Stmt_listContext _localctx = new Stmt_listContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_stmt_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GRAPH) | (1L << NODE) | (1L << EDGE) | (1L << SUBGRAPH) | (1L << NUMBER) | (1L << STRING) | (1L << HTML_STRING) | (1L << ID) | (1L << LB))) != 0)) {
				{
				{
				setState(55);
				stmt();
				setState(57);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SEMI_COLON || _la==COMMA) {
					{
					setState(56);
					_la = _input.LA(1);
					if ( !(_la==SEMI_COLON || _la==COMMA) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class StmtContext extends ParserRuleContext {
		public Node_stmtContext node_stmt() {
			return getRuleContext(Node_stmtContext.class,0);
		}
		public Edge_stmtContext edge_stmt() {
			return getRuleContext(Edge_stmtContext.class,0);
		}
		public Attr_stmtContext attr_stmt() {
			return getRuleContext(Attr_stmtContext.class,0);
		}
		public Graph_a_listContext graph_a_list() {
			return getRuleContext(Graph_a_listContext.class,0);
		}
		public List<Id_Context> id_() {
			return getRuleContexts(Id_Context.class);
		}
		public Id_Context id_(int i) {
			return getRuleContext(Id_Context.class,i);
		}
		public TerminalNode EQUAL() { return getToken(DOTParser.EQUAL, 0); }
		public SubgraphContext subgraph() {
			return getRuleContext(SubgraphContext.class,0);
		}
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitStmt(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_stmt);
		try {
			setState(73);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(64);
				node_stmt();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(65);
				edge_stmt();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(66);
				attr_stmt();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(67);
				graph_a_list();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(68);
				id_();
				setState(69);
				match(EQUAL);
				setState(70);
				id_();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(72);
				subgraph();
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

	public static class Attr_stmtContext extends ParserRuleContext {
		public Attr_listContext attr_list() {
			return getRuleContext(Attr_listContext.class,0);
		}
		public TerminalNode GRAPH() { return getToken(DOTParser.GRAPH, 0); }
		public TerminalNode NODE() { return getToken(DOTParser.NODE, 0); }
		public TerminalNode EDGE() { return getToken(DOTParser.EDGE, 0); }
		public Attr_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attr_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterAttr_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitAttr_stmt(this);
		}
	}

	public final Attr_stmtContext attr_stmt() throws RecognitionException {
		Attr_stmtContext _localctx = new Attr_stmtContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_attr_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GRAPH) | (1L << NODE) | (1L << EDGE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(76);
			attr_list();
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

	public static class Attr_listContext extends ParserRuleContext {
		public List<TerminalNode> LSB() { return getTokens(DOTParser.LSB); }
		public TerminalNode LSB(int i) {
			return getToken(DOTParser.LSB, i);
		}
		public List<TerminalNode> RSB() { return getTokens(DOTParser.RSB); }
		public TerminalNode RSB(int i) {
			return getToken(DOTParser.RSB, i);
		}
		public List<A_listContext> a_list() {
			return getRuleContexts(A_listContext.class);
		}
		public A_listContext a_list(int i) {
			return getRuleContext(A_listContext.class,i);
		}
		public Attr_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attr_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterAttr_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitAttr_list(this);
		}
	}

	public final Attr_listContext attr_list() throws RecognitionException {
		Attr_listContext _localctx = new Attr_listContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_attr_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(78);
				match(LSB);
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << STRING) | (1L << HTML_STRING) | (1L << ID))) != 0)) {
					{
					setState(79);
					a_list();
					}
				}

				setState(82);
				match(RSB);
				}
				}
				setState(85); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LSB );
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

	public static class Graph_a_listContext extends ParserRuleContext {
		public A_listContext a_list() {
			return getRuleContext(A_listContext.class,0);
		}
		public Graph_a_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graph_a_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterGraph_a_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitGraph_a_list(this);
		}
	}

	public final Graph_a_listContext graph_a_list() throws RecognitionException {
		Graph_a_listContext _localctx = new Graph_a_listContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_graph_a_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			a_list();
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

	public static class A_listContext extends ParserRuleContext {
		public List<Id_Context> id_() {
			return getRuleContexts(Id_Context.class);
		}
		public Id_Context id_(int i) {
			return getRuleContext(Id_Context.class,i);
		}
		public List<TerminalNode> EQUAL() { return getTokens(DOTParser.EQUAL); }
		public TerminalNode EQUAL(int i) {
			return getToken(DOTParser.EQUAL, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DOTParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DOTParser.COMMA, i);
		}
		public List<TerminalNode> SEMI_COLON() { return getTokens(DOTParser.SEMI_COLON); }
		public TerminalNode SEMI_COLON(int i) {
			return getToken(DOTParser.SEMI_COLON, i);
		}
		public A_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterA_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitA_list(this);
		}
	}

	public final A_listContext a_list() throws RecognitionException {
		A_listContext _localctx = new A_listContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_a_list);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(95); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(89);
					id_();
					setState(90);
					match(EQUAL);
					setState(91);
					id_();
					setState(93);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						setState(92);
						_la = _input.LA(1);
						if ( !(_la==SEMI_COLON || _la==COMMA) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(97); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
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

	public static class Edge_stmtContext extends ParserRuleContext {
		public EdgeRHSContext edgeRHS() {
			return getRuleContext(EdgeRHSContext.class,0);
		}
		public Node_idContext node_id() {
			return getRuleContext(Node_idContext.class,0);
		}
		public SubgraphContext subgraph() {
			return getRuleContext(SubgraphContext.class,0);
		}
		public Attr_listContext attr_list() {
			return getRuleContext(Attr_listContext.class,0);
		}
		public Edge_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edge_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterEdge_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitEdge_stmt(this);
		}
	}

	public final Edge_stmtContext edge_stmt() throws RecognitionException {
		Edge_stmtContext _localctx = new Edge_stmtContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_edge_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
			case STRING:
			case HTML_STRING:
			case ID:
				{
				setState(99);
				node_id();
				}
				break;
			case SUBGRAPH:
			case LB:
				{
				setState(100);
				subgraph();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(103);
			edgeRHS();
			setState(105);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LSB) {
				{
				setState(104);
				attr_list();
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

	public static class EdgeRHSContext extends ParserRuleContext {
		public List<EdgeopContext> edgeop() {
			return getRuleContexts(EdgeopContext.class);
		}
		public EdgeopContext edgeop(int i) {
			return getRuleContext(EdgeopContext.class,i);
		}
		public List<Node_idContext> node_id() {
			return getRuleContexts(Node_idContext.class);
		}
		public Node_idContext node_id(int i) {
			return getRuleContext(Node_idContext.class,i);
		}
		public List<SubgraphContext> subgraph() {
			return getRuleContexts(SubgraphContext.class);
		}
		public SubgraphContext subgraph(int i) {
			return getRuleContext(SubgraphContext.class,i);
		}
		public EdgeRHSContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edgeRHS; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterEdgeRHS(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitEdgeRHS(this);
		}
	}

	public final EdgeRHSContext edgeRHS() throws RecognitionException {
		EdgeRHSContext _localctx = new EdgeRHSContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_edgeRHS);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(112); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(107);
					edgeop();
					setState(110);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case NUMBER:
					case STRING:
					case HTML_STRING:
					case ID:
						{
						setState(108);
						node_id();
						}
						break;
					case SUBGRAPH:
					case LB:
						{
						setState(109);
						subgraph();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(114); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
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

	public static class EdgeopContext extends ParserRuleContext {
		public EdgeopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edgeop; }
	 
		public EdgeopContext() { }
		public void copyFrom(EdgeopContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InvalidDirectedEdgeContext extends EdgeopContext {
		public TerminalNode DA() { return getToken(DOTParser.DA, 0); }
		public InvalidDirectedEdgeContext(EdgeopContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterInvalidDirectedEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitInvalidDirectedEdge(this);
		}
	}
	public static class InvalidUndirectedEdgeContext extends EdgeopContext {
		public TerminalNode UDA() { return getToken(DOTParser.UDA, 0); }
		public InvalidUndirectedEdgeContext(EdgeopContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterInvalidUndirectedEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitInvalidUndirectedEdge(this);
		}
	}
	public static class UndirectedEdgeContext extends EdgeopContext {
		public TerminalNode UDA() { return getToken(DOTParser.UDA, 0); }
		public UndirectedEdgeContext(EdgeopContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterUndirectedEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitUndirectedEdge(this);
		}
	}
	public static class DirectedEdgeContext extends EdgeopContext {
		public TerminalNode DA() { return getToken(DOTParser.DA, 0); }
		public DirectedEdgeContext(EdgeopContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterDirectedEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitDirectedEdge(this);
		}
	}

	public final EdgeopContext edgeop() throws RecognitionException {
		EdgeopContext _localctx = new EdgeopContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_edgeop);
		try {
			setState(124);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				_localctx = new DirectedEdgeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				if (!(directed)) throw new FailedPredicateException(this, "directed");
				setState(117);
				match(DA);
				}
				break;
			case 2:
				_localctx = new UndirectedEdgeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				if (!( !directed )) throw new FailedPredicateException(this, " !directed ");
				setState(119);
				match(UDA);
				}
				break;
			case 3:
				_localctx = new InvalidDirectedEdgeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(120);
				match(DA);

				            if (!directed) {
				                notifyErrorListeners("Cannot use '->' in an undirected graph.");
				            }
				        
				}
				break;
			case 4:
				_localctx = new InvalidUndirectedEdgeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(122);
				match(UDA);

				            if (directed) {
				                notifyErrorListeners("Cannot use '--' in a directed graph.");
				            }
				        
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

	public static class Node_stmtContext extends ParserRuleContext {
		public Node_idContext node_id() {
			return getRuleContext(Node_idContext.class,0);
		}
		public Attr_listContext attr_list() {
			return getRuleContext(Attr_listContext.class,0);
		}
		public Node_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterNode_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitNode_stmt(this);
		}
	}

	public final Node_stmtContext node_stmt() throws RecognitionException {
		Node_stmtContext _localctx = new Node_stmtContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_node_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			node_id();
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LSB) {
				{
				setState(127);
				attr_list();
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

	public static class Node_idContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public PortContext port() {
			return getRuleContext(PortContext.class,0);
		}
		public Node_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterNode_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitNode_id(this);
		}
	}

	public final Node_idContext node_id() throws RecognitionException {
		Node_idContext _localctx = new Node_idContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_node_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			id_();
			setState(132);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(131);
				port();
				}
				break;
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

	public static class PortContext extends ParserRuleContext {
		public List<TerminalNode> COLON() { return getTokens(DOTParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(DOTParser.COLON, i);
		}
		public List<Id_Context> id_() {
			return getRuleContexts(Id_Context.class);
		}
		public Id_Context id_(int i) {
			return getRuleContext(Id_Context.class,i);
		}
		public PortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_port; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterPort(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitPort(this);
		}
	}

	public final PortContext port() throws RecognitionException {
		PortContext _localctx = new PortContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_port);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(COLON);
			setState(135);
			id_();
			setState(138);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(136);
				match(COLON);
				setState(137);
				id_();
				}
				break;
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

	public static class SubgraphContext extends ParserRuleContext {
		public TerminalNode LB() { return getToken(DOTParser.LB, 0); }
		public Stmt_listContext stmt_list() {
			return getRuleContext(Stmt_listContext.class,0);
		}
		public TerminalNode RB() { return getToken(DOTParser.RB, 0); }
		public TerminalNode SUBGRAPH() { return getToken(DOTParser.SUBGRAPH, 0); }
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public SubgraphContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subgraph; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterSubgraph(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitSubgraph(this);
		}
	}

	public final SubgraphContext subgraph() throws RecognitionException {
		SubgraphContext _localctx = new SubgraphContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_subgraph);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUBGRAPH) {
				{
				setState(140);
				match(SUBGRAPH);
				setState(142);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << STRING) | (1L << HTML_STRING) | (1L << ID))) != 0)) {
					{
					setState(141);
					id_();
					}
				}

				}
			}

			setState(146);
			match(LB);
			setState(147);
			stmt_list();
			setState(148);
			match(RB);
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

	public static class Id_Context extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DOTParser.ID, 0); }
		public TerminalNode STRING() { return getToken(DOTParser.STRING, 0); }
		public TerminalNode NUMBER() { return getToken(DOTParser.NUMBER, 0); }
		public TerminalNode HTML_STRING() { return getToken(DOTParser.HTML_STRING, 0); }
		public Id_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).enterId_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DOTParserListener ) ((DOTParserListener)listener).exitId_(this);
		}
	}

	public final Id_Context id_() throws RecognitionException {
		Id_Context _localctx = new Id_Context(_ctx, getState());
		enterRule(_localctx, 30, RULE_id_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << STRING) | (1L << HTML_STRING) | (1L << ID))) != 0)) ) {
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 10:
			return edgeop_sempred((EdgeopContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean edgeop_sempred(EdgeopContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return directed;
		case 1:
			return  !directed ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\36\u009b\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\6\2"+
		"$\n\2\r\2\16\2%\3\2\3\2\3\3\5\3+\n\3\3\3\3\3\3\3\3\3\5\3\61\n\3\3\3\5"+
		"\3\64\n\3\3\3\3\3\3\3\3\3\3\4\3\4\5\4<\n\4\7\4>\n\4\f\4\16\4A\13\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5L\n\5\3\6\3\6\3\6\3\7\3\7\5\7S\n\7"+
		"\3\7\6\7V\n\7\r\7\16\7W\3\b\3\b\3\t\3\t\3\t\3\t\5\t`\n\t\6\tb\n\t\r\t"+
		"\16\tc\3\n\3\n\5\nh\n\n\3\n\3\n\5\nl\n\n\3\13\3\13\3\13\5\13q\n\13\6\13"+
		"s\n\13\r\13\16\13t\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f\177\n\f\3\r\3\r"+
		"\5\r\u0083\n\r\3\16\3\16\5\16\u0087\n\16\3\17\3\17\3\17\3\17\5\17\u008d"+
		"\n\17\3\20\3\20\5\20\u0091\n\20\5\20\u0093\n\20\3\20\3\20\3\20\3\20\3"+
		"\21\3\21\3\21\2\2\22\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \2\5\3\2\31"+
		"\32\4\2\4\4\6\7\3\2\f\17\2\u00a5\2#\3\2\2\2\4*\3\2\2\2\6?\3\2\2\2\bK\3"+
		"\2\2\2\nM\3\2\2\2\fU\3\2\2\2\16Y\3\2\2\2\20a\3\2\2\2\22g\3\2\2\2\24r\3"+
		"\2\2\2\26~\3\2\2\2\30\u0080\3\2\2\2\32\u0084\3\2\2\2\34\u0088\3\2\2\2"+
		"\36\u0092\3\2\2\2 \u0098\3\2\2\2\"$\5\4\3\2#\"\3\2\2\2$%\3\2\2\2%#\3\2"+
		"\2\2%&\3\2\2\2&\'\3\2\2\2\'(\7\2\2\3(\3\3\2\2\2)+\7\3\2\2*)\3\2\2\2*+"+
		"\3\2\2\2+\60\3\2\2\2,-\7\4\2\2-\61\b\3\1\2./\7\5\2\2/\61\b\3\1\2\60,\3"+
		"\2\2\2\60.\3\2\2\2\61\63\3\2\2\2\62\64\5 \21\2\63\62\3\2\2\2\63\64\3\2"+
		"\2\2\64\65\3\2\2\2\65\66\7\24\2\2\66\67\5\6\4\2\678\7\25\2\28\5\3\2\2"+
		"\29;\5\b\5\2:<\t\2\2\2;:\3\2\2\2;<\3\2\2\2<>\3\2\2\2=9\3\2\2\2>A\3\2\2"+
		"\2?=\3\2\2\2?@\3\2\2\2@\7\3\2\2\2A?\3\2\2\2BL\5\30\r\2CL\5\22\n\2DL\5"+
		"\n\6\2EL\5\16\b\2FG\5 \21\2GH\7\33\2\2HI\5 \21\2IL\3\2\2\2JL\5\36\20\2"+
		"KB\3\2\2\2KC\3\2\2\2KD\3\2\2\2KE\3\2\2\2KF\3\2\2\2KJ\3\2\2\2L\t\3\2\2"+
		"\2MN\t\3\2\2NO\5\f\7\2O\13\3\2\2\2PR\7\26\2\2QS\5\20\t\2RQ\3\2\2\2RS\3"+
		"\2\2\2ST\3\2\2\2TV\7\27\2\2UP\3\2\2\2VW\3\2\2\2WU\3\2\2\2WX\3\2\2\2X\r"+
		"\3\2\2\2YZ\5\20\t\2Z\17\3\2\2\2[\\\5 \21\2\\]\7\33\2\2]_\5 \21\2^`\t\2"+
		"\2\2_^\3\2\2\2_`\3\2\2\2`b\3\2\2\2a[\3\2\2\2bc\3\2\2\2ca\3\2\2\2cd\3\2"+
		"\2\2d\21\3\2\2\2eh\5\32\16\2fh\5\36\20\2ge\3\2\2\2gf\3\2\2\2hi\3\2\2\2"+
		"ik\5\24\13\2jl\5\f\7\2kj\3\2\2\2kl\3\2\2\2l\23\3\2\2\2mp\5\26\f\2nq\5"+
		"\32\16\2oq\5\36\20\2pn\3\2\2\2po\3\2\2\2qs\3\2\2\2rm\3\2\2\2st\3\2\2\2"+
		"tr\3\2\2\2tu\3\2\2\2u\25\3\2\2\2vw\6\f\2\2w\177\7\35\2\2xy\6\f\3\2y\177"+
		"\7\36\2\2z{\7\35\2\2{\177\b\f\1\2|}\7\36\2\2}\177\b\f\1\2~v\3\2\2\2~x"+
		"\3\2\2\2~z\3\2\2\2~|\3\2\2\2\177\27\3\2\2\2\u0080\u0082\5\32\16\2\u0081"+
		"\u0083\5\f\7\2\u0082\u0081\3\2\2\2\u0082\u0083\3\2\2\2\u0083\31\3\2\2"+
		"\2\u0084\u0086\5 \21\2\u0085\u0087\5\34\17\2\u0086\u0085\3\2\2\2\u0086"+
		"\u0087\3\2\2\2\u0087\33\3\2\2\2\u0088\u0089\7\30\2\2\u0089\u008c\5 \21"+
		"\2\u008a\u008b\7\30\2\2\u008b\u008d\5 \21\2\u008c\u008a\3\2\2\2\u008c"+
		"\u008d\3\2\2\2\u008d\35\3\2\2\2\u008e\u0090\7\b\2\2\u008f\u0091\5 \21"+
		"\2\u0090\u008f\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u0093\3\2\2\2\u0092\u008e"+
		"\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0095\7\24\2\2"+
		"\u0095\u0096\5\6\4\2\u0096\u0097\7\25\2\2\u0097\37\3\2\2\2\u0098\u0099"+
		"\t\4\2\2\u0099!\3\2\2\2\27%*\60\63;?KRW_cgkpt~\u0082\u0086\u008c\u0090"+
		"\u0092";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}