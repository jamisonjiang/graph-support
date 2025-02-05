// Generated from org/graphper/parser/grammar/DOTLexer.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DOTLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		STRICT=1, GRAPH=2, DIGRAPH=3, NODE=4, EDGE=5, SUBGRAPH=6, TABLE=7, TR=8, 
		TD=9, NUMBER=10, STRING=11, HTML_STRING=12, ID=13, COMMENT=14, LINE_COMMENT=15, 
		PREPROC=16, WS=17, LB=18, RB=19, LSB=20, RSB=21, COLON=22, SEMI_COLON=23, 
		COMMA=24, EQUAL=25, SLASH=26, DA=27, UDA=28;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"STRICT", "GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "TABLE", "TR", 
			"TD", "NUMBER", "DIGIT", "STRING", "ESC_SEQ", "HTML_STRING", "TAG", "ID", 
			"LETTER", "COMMENT", "LINE_COMMENT", "PREPROC", "WS", "LB", "RB", "LSB", 
			"RSB", "COLON", "SEMI_COLON", "COMMA", "EQUAL", "SLASH", "DA", "UDA"
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


	public DOTLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DOTLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 11:
			STRING_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void STRING_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:

			            String content = getText().substring(1, getText().length() - 1);

			            // Process escape sequences, removing \ except for \n, \r, \t
			            content = content.replaceAll("\\\\(?![nrt])", ""); // Remove \ not followed by n, r, t

			            // Replace recognized escape sequences with actual characters
			            content = content.replace("\\n", "\n")
			                             .replace("\\r", "\r")
			                             .replace("\\t", "\t");
			            content = org.apache_gs.commons.text.StringEscapeUtils.unescapeJava(content);
			            setText(content);
			        
			break;
		}
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\36\u0103\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3"+
		"\n\3\n\3\13\5\13y\n\13\3\13\3\13\6\13}\n\13\r\13\16\13~\3\13\6\13\u0082"+
		"\n\13\r\13\16\13\u0083\3\13\3\13\7\13\u0088\n\13\f\13\16\13\u008b\13\13"+
		"\5\13\u008d\n\13\5\13\u008f\n\13\3\f\3\f\3\r\3\r\3\r\7\r\u0096\n\r\f\r"+
		"\16\r\u0099\13\r\3\r\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\7\17\u00a4"+
		"\n\17\f\17\16\17\u00a7\13\17\3\17\3\17\3\20\3\20\7\20\u00ad\n\20\f\20"+
		"\16\20\u00b0\13\20\3\20\3\20\3\21\3\21\3\21\7\21\u00b7\n\21\f\21\16\21"+
		"\u00ba\13\21\3\22\3\22\3\23\3\23\3\23\3\23\7\23\u00c2\n\23\f\23\16\23"+
		"\u00c5\13\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\7\24\u00d0\n"+
		"\24\f\24\16\24\u00d3\13\24\3\24\5\24\u00d6\n\24\3\24\3\24\3\24\3\24\3"+
		"\25\3\25\7\25\u00de\n\25\f\25\16\25\u00e1\13\25\3\25\3\25\3\26\6\26\u00e6"+
		"\n\26\r\26\16\26\u00e7\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3"+
		"\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3 \3!\3!\3"+
		"!\5\u00ae\u00c3\u00d1\2\"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\2\31\r\33\2\35\16\37\2!\17#\2%\20\'\21)\22+\23-\24/\25\61\26\63"+
		"\27\65\30\67\319\32;\33=\34?\35A\36\3\2\30\4\2UUuu\4\2VVvv\4\2TTtt\4\2"+
		"KKkk\4\2EEee\4\2IIii\4\2CCcc\4\2RRrr\4\2JJjj\4\2FFff\4\2PPpp\4\2QQqq\4"+
		"\2GGgg\4\2WWww\4\2DDdd\4\2NNnn\3\2\62;\4\2$$^^\4\2>>@@\6\2C\\aac|\u0082"+
		"\u0101\4\2\f\f\17\17\5\2\13\f\17\17\"\"\2\u0110\2\3\3\2\2\2\2\5\3\2\2"+
		"\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\31\3\2\2\2\2\35\3\2\2\2\2!\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2"+
		"\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2"+
		"\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\3C\3\2\2\2\5J\3\2\2\2\7P\3\2\2\2"+
		"\tX\3\2\2\2\13]\3\2\2\2\rb\3\2\2\2\17k\3\2\2\2\21q\3\2\2\2\23t\3\2\2\2"+
		"\25x\3\2\2\2\27\u0090\3\2\2\2\31\u0092\3\2\2\2\33\u009d\3\2\2\2\35\u00a0"+
		"\3\2\2\2\37\u00aa\3\2\2\2!\u00b3\3\2\2\2#\u00bb\3\2\2\2%\u00bd\3\2\2\2"+
		"\'\u00cb\3\2\2\2)\u00db\3\2\2\2+\u00e5\3\2\2\2-\u00eb\3\2\2\2/\u00ed\3"+
		"\2\2\2\61\u00ef\3\2\2\2\63\u00f1\3\2\2\2\65\u00f3\3\2\2\2\67\u00f5\3\2"+
		"\2\29\u00f7\3\2\2\2;\u00f9\3\2\2\2=\u00fb\3\2\2\2?\u00fd\3\2\2\2A\u0100"+
		"\3\2\2\2CD\t\2\2\2DE\t\3\2\2EF\t\4\2\2FG\t\5\2\2GH\t\6\2\2HI\t\3\2\2I"+
		"\4\3\2\2\2JK\t\7\2\2KL\t\4\2\2LM\t\b\2\2MN\t\t\2\2NO\t\n\2\2O\6\3\2\2"+
		"\2PQ\t\13\2\2QR\t\5\2\2RS\t\7\2\2ST\t\4\2\2TU\t\b\2\2UV\t\t\2\2VW\t\n"+
		"\2\2W\b\3\2\2\2XY\t\f\2\2YZ\t\r\2\2Z[\t\13\2\2[\\\t\16\2\2\\\n\3\2\2\2"+
		"]^\t\16\2\2^_\t\13\2\2_`\t\7\2\2`a\t\16\2\2a\f\3\2\2\2bc\t\2\2\2cd\t\17"+
		"\2\2de\t\20\2\2ef\t\7\2\2fg\t\4\2\2gh\t\b\2\2hi\t\t\2\2ij\t\n\2\2j\16"+
		"\3\2\2\2kl\t\3\2\2lm\t\b\2\2mn\t\20\2\2no\t\21\2\2op\t\16\2\2p\20\3\2"+
		"\2\2qr\t\3\2\2rs\t\4\2\2s\22\3\2\2\2tu\t\3\2\2uv\t\13\2\2v\24\3\2\2\2"+
		"wy\7/\2\2xw\3\2\2\2xy\3\2\2\2y\u008e\3\2\2\2z|\7\60\2\2{}\5\27\f\2|{\3"+
		"\2\2\2}~\3\2\2\2~|\3\2\2\2~\177\3\2\2\2\177\u008f\3\2\2\2\u0080\u0082"+
		"\5\27\f\2\u0081\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0081\3\2\2\2"+
		"\u0083\u0084\3\2\2\2\u0084\u008c\3\2\2\2\u0085\u0089\7\60\2\2\u0086\u0088"+
		"\5\27\f\2\u0087\u0086\3\2\2\2\u0088\u008b\3\2\2\2\u0089\u0087\3\2\2\2"+
		"\u0089\u008a\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089\3\2\2\2\u008c\u0085"+
		"\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008f\3\2\2\2\u008ez\3\2\2\2\u008e"+
		"\u0081\3\2\2\2\u008f\26\3\2\2\2\u0090\u0091\t\22\2\2\u0091\30\3\2\2\2"+
		"\u0092\u0097\7$\2\2\u0093\u0096\5\33\16\2\u0094\u0096\n\23\2\2\u0095\u0093"+
		"\3\2\2\2\u0095\u0094\3\2\2\2\u0096\u0099\3\2\2\2\u0097\u0095\3\2\2\2\u0097"+
		"\u0098\3\2\2\2\u0098\u009a\3\2\2\2\u0099\u0097\3\2\2\2\u009a\u009b\7$"+
		"\2\2\u009b\u009c\b\r\2\2\u009c\32\3\2\2\2\u009d\u009e\7^\2\2\u009e\u009f"+
		"\13\2\2\2\u009f\34\3\2\2\2\u00a0\u00a5\7>\2\2\u00a1\u00a4\5\37\20\2\u00a2"+
		"\u00a4\n\24\2\2\u00a3\u00a1\3\2\2\2\u00a3\u00a2\3\2\2\2\u00a4\u00a7\3"+
		"\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a8\3\2\2\2\u00a7"+
		"\u00a5\3\2\2\2\u00a8\u00a9\7@\2\2\u00a9\36\3\2\2\2\u00aa\u00ae\7>\2\2"+
		"\u00ab\u00ad\13\2\2\2\u00ac\u00ab\3\2\2\2\u00ad\u00b0\3\2\2\2\u00ae\u00af"+
		"\3\2\2\2\u00ae\u00ac\3\2\2\2\u00af\u00b1\3\2\2\2\u00b0\u00ae\3\2\2\2\u00b1"+
		"\u00b2\7@\2\2\u00b2 \3\2\2\2\u00b3\u00b8\5#\22\2\u00b4\u00b7\5#\22\2\u00b5"+
		"\u00b7\5\27\f\2\u00b6\u00b4\3\2\2\2\u00b6\u00b5\3\2\2\2\u00b7\u00ba\3"+
		"\2\2\2\u00b8\u00b6\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\"\3\2\2\2\u00ba\u00b8"+
		"\3\2\2\2\u00bb\u00bc\t\25\2\2\u00bc$\3\2\2\2\u00bd\u00be\7\61\2\2\u00be"+
		"\u00bf\7,\2\2\u00bf\u00c3\3\2\2\2\u00c0\u00c2\13\2\2\2\u00c1\u00c0\3\2"+
		"\2\2\u00c2\u00c5\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c4"+
		"\u00c6\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c6\u00c7\7,\2\2\u00c7\u00c8\7\61"+
		"\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00ca\b\23\3\2\u00ca&\3\2\2\2\u00cb\u00cc"+
		"\7\61\2\2\u00cc\u00cd\7\61\2\2\u00cd\u00d1\3\2\2\2\u00ce\u00d0\13\2\2"+
		"\2\u00cf\u00ce\3\2\2\2\u00d0\u00d3\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d1\u00cf"+
		"\3\2\2\2\u00d2\u00d5\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d4\u00d6\7\17\2\2"+
		"\u00d5\u00d4\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d8"+
		"\7\f\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00da\b\24\3\2\u00da(\3\2\2\2\u00db"+
		"\u00df\7%\2\2\u00dc\u00de\n\26\2\2\u00dd\u00dc\3\2\2\2\u00de\u00e1\3\2"+
		"\2\2\u00df\u00dd\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e2\3\2\2\2\u00e1"+
		"\u00df\3\2\2\2\u00e2\u00e3\b\25\3\2\u00e3*\3\2\2\2\u00e4\u00e6\t\27\2"+
		"\2\u00e5\u00e4\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e8"+
		"\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00ea\b\26\3\2\u00ea,\3\2\2\2\u00eb"+
		"\u00ec\7}\2\2\u00ec.\3\2\2\2\u00ed\u00ee\7\177\2\2\u00ee\60\3\2\2\2\u00ef"+
		"\u00f0\7]\2\2\u00f0\62\3\2\2\2\u00f1\u00f2\7_\2\2\u00f2\64\3\2\2\2\u00f3"+
		"\u00f4\7<\2\2\u00f4\66\3\2\2\2\u00f5\u00f6\7=\2\2\u00f68\3\2\2\2\u00f7"+
		"\u00f8\7.\2\2\u00f8:\3\2\2\2\u00f9\u00fa\7?\2\2\u00fa<\3\2\2\2\u00fb\u00fc"+
		"\7\61\2\2\u00fc>\3\2\2\2\u00fd\u00fe\7/\2\2\u00fe\u00ff\7@\2\2\u00ff@"+
		"\3\2\2\2\u0100\u0101\7/\2\2\u0101\u0102\7/\2\2\u0102B\3\2\2\2\25\2x~\u0083"+
		"\u0089\u008c\u008e\u0095\u0097\u00a3\u00a5\u00ae\u00b6\u00b8\u00c3\u00d1"+
		"\u00d5\u00df\u00e7\4\3\r\2\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}