// Generated from org\graphper\parser\grammar\DOTLexer.g4 by ANTLR 4.9.3
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
			"TD", "NUMBER", "DIGIT", "STRING", "ESC_SEQ", "HEX_DIGIT", "HTML_STRING", 
			"TAG", "ID", "LETTER", "COMMENT", "LINE_COMMENT", "PREPROC", "WS", "LB", 
			"RB", "LSB", "RSB", "COLON", "SEMI_COLON", "COMMA", "EQUAL", "SLASH", 
			"DA", "UDA"
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
			            content = content.replaceAll("\\\\(?![nrt\\\\])", ""); // Remove \ not followed by n, r, t, \

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\36\u010f\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\13\5\13{\n\13\3\13\3\13\6\13\177\n\13\r\13\16\13\u0080"+
		"\3\13\6\13\u0084\n\13\r\13\16\13\u0085\3\13\3\13\7\13\u008a\n\13\f\13"+
		"\16\13\u008d\13\13\5\13\u008f\n\13\5\13\u0091\n\13\3\f\3\f\3\r\3\r\3\r"+
		"\7\r\u0098\n\r\f\r\16\r\u009b\13\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\5\16\u00a9\n\16\3\17\3\17\3\20\3\20\3\20\7\20\u00b0"+
		"\n\20\f\20\16\20\u00b3\13\20\3\20\3\20\3\21\3\21\7\21\u00b9\n\21\f\21"+
		"\16\21\u00bc\13\21\3\21\3\21\3\22\3\22\3\22\7\22\u00c3\n\22\f\22\16\22"+
		"\u00c6\13\22\3\23\3\23\3\24\3\24\3\24\3\24\7\24\u00ce\n\24\f\24\16\24"+
		"\u00d1\13\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\7\25\u00dc\n"+
		"\25\f\25\16\25\u00df\13\25\3\25\5\25\u00e2\n\25\3\25\3\25\3\25\3\25\3"+
		"\26\3\26\7\26\u00ea\n\26\f\26\16\26\u00ed\13\26\3\26\3\26\3\27\6\27\u00f2"+
		"\n\27\r\27\16\27\u00f3\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3"+
		"\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3!\3\"\3\"\3\""+
		"\5\u00ba\u00cf\u00dd\2#\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f"+
		"\27\2\31\r\33\2\35\2\37\16!\2#\17%\2\'\20)\21+\22-\23/\24\61\25\63\26"+
		"\65\27\67\309\31;\32=\33?\34A\35C\36\3\2\32\4\2UUuu\4\2VVvv\4\2TTtt\4"+
		"\2KKkk\4\2EEee\4\2IIii\4\2CCcc\4\2RRrr\4\2JJjj\4\2FFff\4\2PPpp\4\2QQq"+
		"q\4\2GGgg\4\2WWww\4\2DDdd\4\2NNnn\3\2\62;\4\2$$^^\f\2\f\f\17\17$$\60\60"+
		"^^ddhhppttvv\5\2\62;CHch\4\2>>@@\6\2C\\aac|\u0082\u0101\4\2\f\f\17\17"+
		"\5\2\13\f\17\17\"\"\2\u011c\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3"+
		"\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2"+
		"\2\25\3\2\2\2\2\31\3\2\2\2\2\37\3\2\2\2\2#\3\2\2\2\2\'\3\2\2\2\2)\3\2"+
		"\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3"+
		"\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2"+
		"\2\2\2C\3\2\2\2\3E\3\2\2\2\5L\3\2\2\2\7R\3\2\2\2\tZ\3\2\2\2\13_\3\2\2"+
		"\2\rd\3\2\2\2\17m\3\2\2\2\21s\3\2\2\2\23v\3\2\2\2\25z\3\2\2\2\27\u0092"+
		"\3\2\2\2\31\u0094\3\2\2\2\33\u00a8\3\2\2\2\35\u00aa\3\2\2\2\37\u00ac\3"+
		"\2\2\2!\u00b6\3\2\2\2#\u00bf\3\2\2\2%\u00c7\3\2\2\2\'\u00c9\3\2\2\2)\u00d7"+
		"\3\2\2\2+\u00e7\3\2\2\2-\u00f1\3\2\2\2/\u00f7\3\2\2\2\61\u00f9\3\2\2\2"+
		"\63\u00fb\3\2\2\2\65\u00fd\3\2\2\2\67\u00ff\3\2\2\29\u0101\3\2\2\2;\u0103"+
		"\3\2\2\2=\u0105\3\2\2\2?\u0107\3\2\2\2A\u0109\3\2\2\2C\u010c\3\2\2\2E"+
		"F\t\2\2\2FG\t\3\2\2GH\t\4\2\2HI\t\5\2\2IJ\t\6\2\2JK\t\3\2\2K\4\3\2\2\2"+
		"LM\t\7\2\2MN\t\4\2\2NO\t\b\2\2OP\t\t\2\2PQ\t\n\2\2Q\6\3\2\2\2RS\t\13\2"+
		"\2ST\t\5\2\2TU\t\7\2\2UV\t\4\2\2VW\t\b\2\2WX\t\t\2\2XY\t\n\2\2Y\b\3\2"+
		"\2\2Z[\t\f\2\2[\\\t\r\2\2\\]\t\13\2\2]^\t\16\2\2^\n\3\2\2\2_`\t\16\2\2"+
		"`a\t\13\2\2ab\t\7\2\2bc\t\16\2\2c\f\3\2\2\2de\t\2\2\2ef\t\17\2\2fg\t\20"+
		"\2\2gh\t\7\2\2hi\t\4\2\2ij\t\b\2\2jk\t\t\2\2kl\t\n\2\2l\16\3\2\2\2mn\t"+
		"\3\2\2no\t\b\2\2op\t\20\2\2pq\t\21\2\2qr\t\16\2\2r\20\3\2\2\2st\t\3\2"+
		"\2tu\t\4\2\2u\22\3\2\2\2vw\t\3\2\2wx\t\13\2\2x\24\3\2\2\2y{\7/\2\2zy\3"+
		"\2\2\2z{\3\2\2\2{\u0090\3\2\2\2|~\7\60\2\2}\177\5\27\f\2~}\3\2\2\2\177"+
		"\u0080\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0091\3\2\2\2"+
		"\u0082\u0084\5\27\f\2\u0083\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0083"+
		"\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u008e\3\2\2\2\u0087\u008b\7\60\2\2"+
		"\u0088\u008a\5\27\f\2\u0089\u0088\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089"+
		"\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u008f\3\2\2\2\u008d\u008b\3\2\2\2\u008e"+
		"\u0087\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0091\3\2\2\2\u0090|\3\2\2\2"+
		"\u0090\u0083\3\2\2\2\u0091\26\3\2\2\2\u0092\u0093\t\22\2\2\u0093\30\3"+
		"\2\2\2\u0094\u0099\7$\2\2\u0095\u0098\5\33\16\2\u0096\u0098\n\23\2\2\u0097"+
		"\u0095\3\2\2\2\u0097\u0096\3\2\2\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2"+
		"\2\2\u0099\u009a\3\2\2\2\u009a\u009c\3\2\2\2\u009b\u0099\3\2\2\2\u009c"+
		"\u009d\7$\2\2\u009d\u009e\b\r\2\2\u009e\32\3\2\2\2\u009f\u00a0\7^\2\2"+
		"\u00a0\u00a9\t\24\2\2\u00a1\u00a2\7^\2\2\u00a2\u00a3\7w\2\2\u00a3\u00a4"+
		"\5\35\17\2\u00a4\u00a5\5\35\17\2\u00a5\u00a6\5\35\17\2\u00a6\u00a7\5\35"+
		"\17\2\u00a7\u00a9\3\2\2\2\u00a8\u009f\3\2\2\2\u00a8\u00a1\3\2\2\2\u00a9"+
		"\34\3\2\2\2\u00aa\u00ab\t\25\2\2\u00ab\36\3\2\2\2\u00ac\u00b1\7>\2\2\u00ad"+
		"\u00b0\5!\21\2\u00ae\u00b0\n\26\2\2\u00af\u00ad\3\2\2\2\u00af\u00ae\3"+
		"\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2"+
		"\u00b4\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b5\7@\2\2\u00b5 \3\2\2\2\u00b6"+
		"\u00ba\7>\2\2\u00b7\u00b9\13\2\2\2\u00b8\u00b7\3\2\2\2\u00b9\u00bc\3\2"+
		"\2\2\u00ba\u00bb\3\2\2\2\u00ba\u00b8\3\2\2\2\u00bb\u00bd\3\2\2\2\u00bc"+
		"\u00ba\3\2\2\2\u00bd\u00be\7@\2\2\u00be\"\3\2\2\2\u00bf\u00c4\5%\23\2"+
		"\u00c0\u00c3\5%\23\2\u00c1\u00c3\5\27\f\2\u00c2\u00c0\3\2\2\2\u00c2\u00c1"+
		"\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5"+
		"$\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c7\u00c8\t\27\2\2\u00c8&\3\2\2\2\u00c9"+
		"\u00ca\7\61\2\2\u00ca\u00cb\7,\2\2\u00cb\u00cf\3\2\2\2\u00cc\u00ce\13"+
		"\2\2\2\u00cd\u00cc\3\2\2\2\u00ce\u00d1\3\2\2\2\u00cf\u00d0\3\2\2\2\u00cf"+
		"\u00cd\3\2\2\2\u00d0\u00d2\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d2\u00d3\7,"+
		"\2\2\u00d3\u00d4\7\61\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\b\24\3\2\u00d6"+
		"(\3\2\2\2\u00d7\u00d8\7\61\2\2\u00d8\u00d9\7\61\2\2\u00d9\u00dd\3\2\2"+
		"\2\u00da\u00dc\13\2\2\2\u00db\u00da\3\2\2\2\u00dc\u00df\3\2\2\2\u00dd"+
		"\u00de\3\2\2\2\u00dd\u00db\3\2\2\2\u00de\u00e1\3\2\2\2\u00df\u00dd\3\2"+
		"\2\2\u00e0\u00e2\7\17\2\2\u00e1\u00e0\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2"+
		"\u00e3\3\2\2\2\u00e3\u00e4\7\f\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6\b\25"+
		"\3\2\u00e6*\3\2\2\2\u00e7\u00eb\7%\2\2\u00e8\u00ea\n\30\2\2\u00e9\u00e8"+
		"\3\2\2\2\u00ea\u00ed\3\2\2\2\u00eb\u00e9\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec"+
		"\u00ee\3\2\2\2\u00ed\u00eb\3\2\2\2\u00ee\u00ef\b\26\3\2\u00ef,\3\2\2\2"+
		"\u00f0\u00f2\t\31\2\2\u00f1\u00f0\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00f1"+
		"\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f6\b\27\3\2"+
		"\u00f6.\3\2\2\2\u00f7\u00f8\7}\2\2\u00f8\60\3\2\2\2\u00f9\u00fa\7\177"+
		"\2\2\u00fa\62\3\2\2\2\u00fb\u00fc\7]\2\2\u00fc\64\3\2\2\2\u00fd\u00fe"+
		"\7_\2\2\u00fe\66\3\2\2\2\u00ff\u0100\7<\2\2\u01008\3\2\2\2\u0101\u0102"+
		"\7=\2\2\u0102:\3\2\2\2\u0103\u0104\7.\2\2\u0104<\3\2\2\2\u0105\u0106\7"+
		"?\2\2\u0106>\3\2\2\2\u0107\u0108\7\61\2\2\u0108@\3\2\2\2\u0109\u010a\7"+
		"/\2\2\u010a\u010b\7@\2\2\u010bB\3\2\2\2\u010c\u010d\7/\2\2\u010d\u010e"+
		"\7/\2\2\u010eD\3\2\2\2\26\2z\u0080\u0085\u008b\u008e\u0090\u0097\u0099"+
		"\u00a8\u00af\u00b1\u00ba\u00c2\u00c4\u00cf\u00dd\u00e1\u00eb\u00f3\4\3"+
		"\r\2\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}