package org.graphper.parser.grammar;
// Generated from /antlr4/org/graphper/parser/grammar/DOTParser.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class DOTParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitGraphs(this);
			else return visitor.visitChildren(this);
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
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 14L) != 0) );
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitGraph(this);
			else return visitor.visitChildren(this);
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
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 15360L) != 0)) {
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitStmt_list(this);
			else return visitor.visitChildren(this);
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
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 277620L) != 0)) {
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitStmt(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitAttr_stmt(this);
			else return visitor.visitChildren(this);
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
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 52L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitAttr_list(this);
			else return visitor.visitChildren(this);
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
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 15360L) != 0)) {
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitGraph_a_list(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitA_list(this);
			else return visitor.visitChildren(this);
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
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitEdge_stmt(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitEdgeRHS(this);
			else return visitor.visitChildren(this);
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
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitInvalidDirectedEdge(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitInvalidUndirectedEdge(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitUndirectedEdge(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitDirectedEdge(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitNode_stmt(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitNode_id(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitPort(this);
			else return visitor.visitChildren(this);
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitSubgraph(this);
			else return visitor.visitChildren(this);
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
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 15360L) != 0)) {
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

	@SuppressWarnings("CheckReturnValue")
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DOTParserVisitor ) return ((DOTParserVisitor<? extends T>)visitor).visitId_(this);
			else return visitor.visitChildren(this);
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
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 15360L) != 0)) ) {
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
		"\u0004\u0001\u001c\u0099\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0001\u0000\u0004\u0000\"\b\u0000\u000b\u0000\f\u0000#\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0003\u0001)\b\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001/\b\u0001\u0001\u0001\u0003\u0001"+
		"2\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0003\u0002:\b\u0002\u0005\u0002<\b\u0002\n\u0002\f\u0002"+
		"?\t\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003J\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0003\u0005"+
		"Q\b\u0005\u0001\u0005\u0004\u0005T\b\u0005\u000b\u0005\f\u0005U\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007^\b\u0007\u0004\u0007`\b\u0007\u000b\u0007\f\u0007a\u0001\b\u0001"+
		"\b\u0003\bf\b\b\u0001\b\u0001\b\u0003\bj\b\b\u0001\t\u0001\t\u0001\t\u0003"+
		"\to\b\t\u0004\tq\b\t\u000b\t\f\tr\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0003\n}\b\n\u0001\u000b\u0001\u000b\u0003"+
		"\u000b\u0081\b\u000b\u0001\f\u0001\f\u0003\f\u0085\b\f\u0001\r\u0001\r"+
		"\u0001\r\u0001\r\u0003\r\u008b\b\r\u0001\u000e\u0001\u000e\u0003\u000e"+
		"\u008f\b\u000e\u0003\u000e\u0091\b\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0000\u0000\u0010"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e\u0000\u0003\u0001\u0000\u0017\u0018\u0002\u0000\u0002\u0002"+
		"\u0004\u0005\u0001\u0000\n\r\u00a3\u0000!\u0001\u0000\u0000\u0000\u0002"+
		"(\u0001\u0000\u0000\u0000\u0004=\u0001\u0000\u0000\u0000\u0006I\u0001"+
		"\u0000\u0000\u0000\bK\u0001\u0000\u0000\u0000\nS\u0001\u0000\u0000\u0000"+
		"\fW\u0001\u0000\u0000\u0000\u000e_\u0001\u0000\u0000\u0000\u0010e\u0001"+
		"\u0000\u0000\u0000\u0012p\u0001\u0000\u0000\u0000\u0014|\u0001\u0000\u0000"+
		"\u0000\u0016~\u0001\u0000\u0000\u0000\u0018\u0082\u0001\u0000\u0000\u0000"+
		"\u001a\u0086\u0001\u0000\u0000\u0000\u001c\u0090\u0001\u0000\u0000\u0000"+
		"\u001e\u0096\u0001\u0000\u0000\u0000 \"\u0003\u0002\u0001\u0000! \u0001"+
		"\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000#!\u0001\u0000\u0000\u0000"+
		"#$\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000%&\u0005\u0000\u0000"+
		"\u0001&\u0001\u0001\u0000\u0000\u0000\')\u0005\u0001\u0000\u0000(\'\u0001"+
		"\u0000\u0000\u0000()\u0001\u0000\u0000\u0000).\u0001\u0000\u0000\u0000"+
		"*+\u0005\u0002\u0000\u0000+/\u0006\u0001\uffff\uffff\u0000,-\u0005\u0003"+
		"\u0000\u0000-/\u0006\u0001\uffff\uffff\u0000.*\u0001\u0000\u0000\u0000"+
		".,\u0001\u0000\u0000\u0000/1\u0001\u0000\u0000\u000002\u0003\u001e\u000f"+
		"\u000010\u0001\u0000\u0000\u000012\u0001\u0000\u0000\u000023\u0001\u0000"+
		"\u0000\u000034\u0005\u0012\u0000\u000045\u0003\u0004\u0002\u000056\u0005"+
		"\u0013\u0000\u00006\u0003\u0001\u0000\u0000\u000079\u0003\u0006\u0003"+
		"\u00008:\u0007\u0000\u0000\u000098\u0001\u0000\u0000\u00009:\u0001\u0000"+
		"\u0000\u0000:<\u0001\u0000\u0000\u0000;7\u0001\u0000\u0000\u0000<?\u0001"+
		"\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000=>\u0001\u0000\u0000\u0000"+
		">\u0005\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000@J\u0003\u0016"+
		"\u000b\u0000AJ\u0003\u0010\b\u0000BJ\u0003\b\u0004\u0000CJ\u0003\f\u0006"+
		"\u0000DE\u0003\u001e\u000f\u0000EF\u0005\u0019\u0000\u0000FG\u0003\u001e"+
		"\u000f\u0000GJ\u0001\u0000\u0000\u0000HJ\u0003\u001c\u000e\u0000I@\u0001"+
		"\u0000\u0000\u0000IA\u0001\u0000\u0000\u0000IB\u0001\u0000\u0000\u0000"+
		"IC\u0001\u0000\u0000\u0000ID\u0001\u0000\u0000\u0000IH\u0001\u0000\u0000"+
		"\u0000J\u0007\u0001\u0000\u0000\u0000KL\u0007\u0001\u0000\u0000LM\u0003"+
		"\n\u0005\u0000M\t\u0001\u0000\u0000\u0000NP\u0005\u0014\u0000\u0000OQ"+
		"\u0003\u000e\u0007\u0000PO\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000"+
		"\u0000QR\u0001\u0000\u0000\u0000RT\u0005\u0015\u0000\u0000SN\u0001\u0000"+
		"\u0000\u0000TU\u0001\u0000\u0000\u0000US\u0001\u0000\u0000\u0000UV\u0001"+
		"\u0000\u0000\u0000V\u000b\u0001\u0000\u0000\u0000WX\u0003\u000e\u0007"+
		"\u0000X\r\u0001\u0000\u0000\u0000YZ\u0003\u001e\u000f\u0000Z[\u0005\u0019"+
		"\u0000\u0000[]\u0003\u001e\u000f\u0000\\^\u0007\u0000\u0000\u0000]\\\u0001"+
		"\u0000\u0000\u0000]^\u0001\u0000\u0000\u0000^`\u0001\u0000\u0000\u0000"+
		"_Y\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000a_\u0001\u0000\u0000"+
		"\u0000ab\u0001\u0000\u0000\u0000b\u000f\u0001\u0000\u0000\u0000cf\u0003"+
		"\u0018\f\u0000df\u0003\u001c\u000e\u0000ec\u0001\u0000\u0000\u0000ed\u0001"+
		"\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000gi\u0003\u0012\t\u0000hj\u0003"+
		"\n\u0005\u0000ih\u0001\u0000\u0000\u0000ij\u0001\u0000\u0000\u0000j\u0011"+
		"\u0001\u0000\u0000\u0000kn\u0003\u0014\n\u0000lo\u0003\u0018\f\u0000m"+
		"o\u0003\u001c\u000e\u0000nl\u0001\u0000\u0000\u0000nm\u0001\u0000\u0000"+
		"\u0000oq\u0001\u0000\u0000\u0000pk\u0001\u0000\u0000\u0000qr\u0001\u0000"+
		"\u0000\u0000rp\u0001\u0000\u0000\u0000rs\u0001\u0000\u0000\u0000s\u0013"+
		"\u0001\u0000\u0000\u0000tu\u0004\n\u0000\u0000u}\u0005\u001b\u0000\u0000"+
		"vw\u0004\n\u0001\u0000w}\u0005\u001c\u0000\u0000xy\u0005\u001b\u0000\u0000"+
		"y}\u0006\n\uffff\uffff\u0000z{\u0005\u001c\u0000\u0000{}\u0006\n\uffff"+
		"\uffff\u0000|t\u0001\u0000\u0000\u0000|v\u0001\u0000\u0000\u0000|x\u0001"+
		"\u0000\u0000\u0000|z\u0001\u0000\u0000\u0000}\u0015\u0001\u0000\u0000"+
		"\u0000~\u0080\u0003\u0018\f\u0000\u007f\u0081\u0003\n\u0005\u0000\u0080"+
		"\u007f\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000\u0000\u0081"+
		"\u0017\u0001\u0000\u0000\u0000\u0082\u0084\u0003\u001e\u000f\u0000\u0083"+
		"\u0085\u0003\u001a\r\u0000\u0084\u0083\u0001\u0000\u0000\u0000\u0084\u0085"+
		"\u0001\u0000\u0000\u0000\u0085\u0019\u0001\u0000\u0000\u0000\u0086\u0087"+
		"\u0005\u0016\u0000\u0000\u0087\u008a\u0003\u001e\u000f\u0000\u0088\u0089"+
		"\u0005\u0016\u0000\u0000\u0089\u008b\u0003\u001e\u000f\u0000\u008a\u0088"+
		"\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b\u001b"+
		"\u0001\u0000\u0000\u0000\u008c\u008e\u0005\u0006\u0000\u0000\u008d\u008f"+
		"\u0003\u001e\u000f\u0000\u008e\u008d\u0001\u0000\u0000\u0000\u008e\u008f"+
		"\u0001\u0000\u0000\u0000\u008f\u0091\u0001\u0000\u0000\u0000\u0090\u008c"+
		"\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0092"+
		"\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u0012\u0000\u0000\u0093\u0094"+
		"\u0003\u0004\u0002\u0000\u0094\u0095\u0005\u0013\u0000\u0000\u0095\u001d"+
		"\u0001\u0000\u0000\u0000\u0096\u0097\u0007\u0002\u0000\u0000\u0097\u001f"+
		"\u0001\u0000\u0000\u0000\u0015#(.19=IPU]aeinr|\u0080\u0084\u008a\u008e"+
		"\u0090";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}