// Generated from org\graphper\parser\grammar\HTMLLexer.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class HTMLLexer extends Lexer {
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
		TAG=1, ATTVALUE=2;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "TAG", "ATTVALUE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"HTML_COMMENT", "HTML_CONDITIONAL_COMMENT", "WS", "TAG_OPEN", "HTML_TEXT", 
			"TABLE", "TR", "TD", "FONT", "B", "I", "U", "S", "O", "SUB", "SUP", "BR", 
			"VT", "VB", "VC", "HL", "HR", "HC", "COMMA", "SEMI_COLON", "TAG_CLOSE", 
			"TAG_SLASH_CLOSE", "TAG_SLASH", "TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", 
			"HEXDIGIT", "DIGIT", "TAG_NameChar", "TAG_NameStartChar", "ATTVALUE_VALUE", 
			"ATTRIBUTE", "ATTCHARS", "ATTCHAR", "HEXCHARS", "DECCHARS", "DOUBLE_QUOTE_STRING", 
			"SINGLE_QUOTE_STRING"
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


	public HTMLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "HTMLLexer.g4"; }

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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2#\u0128\b\1\b\1\b"+
		"\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n"+
		"\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21"+
		"\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30"+
		"\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37"+
		"\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t"+
		"*\4+\t+\4,\t,\3\2\3\2\3\2\3\2\3\2\3\2\7\2b\n\2\f\2\16\2e\13\2\3\2\3\2"+
		"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\7\3p\n\3\f\3\16\3s\13\3\3\3\3\3\3\3\3\4\3"+
		"\4\5\4z\n\4\3\4\6\4}\n\4\r\4\16\4~\3\5\3\5\3\5\3\5\3\6\6\6\u0086\n\6\r"+
		"\6\16\6\u0087\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n"+
		"\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3"+
		"\20\3\20\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3"+
		"\24\3\25\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\27\3\30\3\30\3\30\3\31\3"+
		"\31\3\32\3\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3"+
		"\36\3\36\3\36\3\36\3\37\3\37\7\37\u00d7\n\37\f\37\16\37\u00da\13\37\3"+
		" \3 \3 \3 \3!\3!\3\"\3\"\3#\3#\3#\3#\5#\u00e8\n#\3$\5$\u00eb\n$\3%\7%"+
		"\u00ee\n%\f%\16%\u00f1\13%\3%\3%\3%\3%\3&\3&\3&\3&\3&\5&\u00fc\n&\3\'"+
		"\6\'\u00ff\n\'\r\'\16\'\u0100\3\'\5\'\u0104\n\'\3(\5(\u0107\n(\3)\3)\6"+
		")\u010b\n)\r)\16)\u010c\3*\6*\u0110\n*\r*\16*\u0111\3*\5*\u0115\n*\3+"+
		"\3+\7+\u0119\n+\f+\16+\u011c\13+\3+\3+\3,\3,\7,\u0122\n,\f,\16,\u0125"+
		"\13,\3,\3,\4cq\2-\5\3\7\4\t\5\13\6\r\7\17\b\21\t\23\n\25\13\27\f\31\r"+
		"\33\16\35\17\37\20!\21#\22%\23\'\24)\25+\26-\27/\30\61\31\63\32\65\33"+
		"\67\349\35;\36=\37? A!C\2E\2G\2I\2K\"M#O\2Q\2S\2U\2W\2Y\2\5\2\3\4\36\4"+
		"\2\13\13\"\"\3\2>>\4\2VVvv\4\2CCcc\4\2DDdd\4\2NNnn\4\2GGgg\4\2TTtt\4\2"+
		"FFff\4\2HHhh\4\2QQqq\4\2PPpp\4\2KKkk\4\2WWww\4\2UUuu\4\2RRrr\4\2XXxx\4"+
		"\2EEee\4\2JJjj\5\2\13\f\17\17\"\"\5\2\62;CHch\3\2\62;\4\2/\60aa\5\2\u00b9"+
		"\u00b9\u0302\u0371\u2041\u2042\n\2<<C\\c|\u2072\u2191\u2c02\u2ff1\u3003"+
		"\ud801\uf902\ufdd1\ufdf2\uffff\n\2%%--/<??AAC\\aac|\4\2$$>>\4\2))>>\2"+
		"\u0131\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\3"+
		"\17\3\2\2\2\3\21\3\2\2\2\3\23\3\2\2\2\3\25\3\2\2\2\3\27\3\2\2\2\3\31\3"+
		"\2\2\2\3\33\3\2\2\2\3\35\3\2\2\2\3\37\3\2\2\2\3!\3\2\2\2\3#\3\2\2\2\3"+
		"%\3\2\2\2\3\'\3\2\2\2\3)\3\2\2\2\3+\3\2\2\2\3-\3\2\2\2\3/\3\2\2\2\3\61"+
		"\3\2\2\2\3\63\3\2\2\2\3\65\3\2\2\2\3\67\3\2\2\2\39\3\2\2\2\3;\3\2\2\2"+
		"\3=\3\2\2\2\3?\3\2\2\2\3A\3\2\2\2\4K\3\2\2\2\4M\3\2\2\2\5[\3\2\2\2\7j"+
		"\3\2\2\2\t|\3\2\2\2\13\u0080\3\2\2\2\r\u0085\3\2\2\2\17\u0089\3\2\2\2"+
		"\21\u008f\3\2\2\2\23\u0092\3\2\2\2\25\u0095\3\2\2\2\27\u009a\3\2\2\2\31"+
		"\u009c\3\2\2\2\33\u009e\3\2\2\2\35\u00a0\3\2\2\2\37\u00a2\3\2\2\2!\u00a4"+
		"\3\2\2\2#\u00a8\3\2\2\2%\u00ac\3\2\2\2\'\u00af\3\2\2\2)\u00b2\3\2\2\2"+
		"+\u00b5\3\2\2\2-\u00b8\3\2\2\2/\u00bb\3\2\2\2\61\u00be\3\2\2\2\63\u00c1"+
		"\3\2\2\2\65\u00c3\3\2\2\2\67\u00c5\3\2\2\29\u00c9\3\2\2\2;\u00ce\3\2\2"+
		"\2=\u00d0\3\2\2\2?\u00d4\3\2\2\2A\u00db\3\2\2\2C\u00df\3\2\2\2E\u00e1"+
		"\3\2\2\2G\u00e7\3\2\2\2I\u00ea\3\2\2\2K\u00ef\3\2\2\2M\u00fb\3\2\2\2O"+
		"\u00fe\3\2\2\2Q\u0106\3\2\2\2S\u0108\3\2\2\2U\u010f\3\2\2\2W\u0116\3\2"+
		"\2\2Y\u011f\3\2\2\2[\\\7>\2\2\\]\7#\2\2]^\7/\2\2^_\7/\2\2_c\3\2\2\2`b"+
		"\13\2\2\2a`\3\2\2\2be\3\2\2\2cd\3\2\2\2ca\3\2\2\2df\3\2\2\2ec\3\2\2\2"+
		"fg\7/\2\2gh\7/\2\2hi\7@\2\2i\6\3\2\2\2jk\7>\2\2kl\7#\2\2lm\7]\2\2mq\3"+
		"\2\2\2np\13\2\2\2on\3\2\2\2ps\3\2\2\2qr\3\2\2\2qo\3\2\2\2rt\3\2\2\2sq"+
		"\3\2\2\2tu\7_\2\2uv\7@\2\2v\b\3\2\2\2w}\t\2\2\2xz\7\17\2\2yx\3\2\2\2y"+
		"z\3\2\2\2z{\3\2\2\2{}\7\f\2\2|w\3\2\2\2|y\3\2\2\2}~\3\2\2\2~|\3\2\2\2"+
		"~\177\3\2\2\2\177\n\3\2\2\2\u0080\u0081\7>\2\2\u0081\u0082\3\2\2\2\u0082"+
		"\u0083\b\5\2\2\u0083\f\3\2\2\2\u0084\u0086\n\3\2\2\u0085\u0084\3\2\2\2"+
		"\u0086\u0087\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\16"+
		"\3\2\2\2\u0089\u008a\t\4\2\2\u008a\u008b\t\5\2\2\u008b\u008c\t\6\2\2\u008c"+
		"\u008d\t\7\2\2\u008d\u008e\t\b\2\2\u008e\20\3\2\2\2\u008f\u0090\t\4\2"+
		"\2\u0090\u0091\t\t\2\2\u0091\22\3\2\2\2\u0092\u0093\t\4\2\2\u0093\u0094"+
		"\t\n\2\2\u0094\24\3\2\2\2\u0095\u0096\t\13\2\2\u0096\u0097\t\f\2\2\u0097"+
		"\u0098\t\r\2\2\u0098\u0099\t\4\2\2\u0099\26\3\2\2\2\u009a\u009b\t\6\2"+
		"\2\u009b\30\3\2\2\2\u009c\u009d\t\16\2\2\u009d\32\3\2\2\2\u009e\u009f"+
		"\t\17\2\2\u009f\34\3\2\2\2\u00a0\u00a1\t\20\2\2\u00a1\36\3\2\2\2\u00a2"+
		"\u00a3\t\f\2\2\u00a3 \3\2\2\2\u00a4\u00a5\t\20\2\2\u00a5\u00a6\t\17\2"+
		"\2\u00a6\u00a7\t\6\2\2\u00a7\"\3\2\2\2\u00a8\u00a9\t\20\2\2\u00a9\u00aa"+
		"\t\17\2\2\u00aa\u00ab\t\21\2\2\u00ab$\3\2\2\2\u00ac\u00ad\t\6\2\2\u00ad"+
		"\u00ae\t\t\2\2\u00ae&\3\2\2\2\u00af\u00b0\t\22\2\2\u00b0\u00b1\t\4\2\2"+
		"\u00b1(\3\2\2\2\u00b2\u00b3\t\22\2\2\u00b3\u00b4\t\6\2\2\u00b4*\3\2\2"+
		"\2\u00b5\u00b6\t\22\2\2\u00b6\u00b7\t\23\2\2\u00b7,\3\2\2\2\u00b8\u00b9"+
		"\t\24\2\2\u00b9\u00ba\t\7\2\2\u00ba.\3\2\2\2\u00bb\u00bc\t\24\2\2\u00bc"+
		"\u00bd\t\t\2\2\u00bd\60\3\2\2\2\u00be\u00bf\t\24\2\2\u00bf\u00c0\t\23"+
		"\2\2\u00c0\62\3\2\2\2\u00c1\u00c2\7.\2\2\u00c2\64\3\2\2\2\u00c3\u00c4"+
		"\7=\2\2\u00c4\66\3\2\2\2\u00c5\u00c6\7@\2\2\u00c6\u00c7\3\2\2\2\u00c7"+
		"\u00c8\b\33\3\2\u00c88\3\2\2\2\u00c9\u00ca\7\61\2\2\u00ca\u00cb\7@\2\2"+
		"\u00cb\u00cc\3\2\2\2\u00cc\u00cd\b\34\3\2\u00cd:\3\2\2\2\u00ce\u00cf\7"+
		"\61\2\2\u00cf<\3\2\2\2\u00d0\u00d1\7?\2\2\u00d1\u00d2\3\2\2\2\u00d2\u00d3"+
		"\b\36\4\2\u00d3>\3\2\2\2\u00d4\u00d8\5I$\2\u00d5\u00d7\5G#\2\u00d6\u00d5"+
		"\3\2\2\2\u00d7\u00da\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9"+
		"@\3\2\2\2\u00da\u00d8\3\2\2\2\u00db\u00dc\t\25\2\2\u00dc\u00dd\3\2\2\2"+
		"\u00dd\u00de\b \5\2\u00deB\3\2\2\2\u00df\u00e0\t\26\2\2\u00e0D\3\2\2\2"+
		"\u00e1\u00e2\t\27\2\2\u00e2F\3\2\2\2\u00e3\u00e8\5I$\2\u00e4\u00e8\t\30"+
		"\2\2\u00e5\u00e8\5E\"\2\u00e6\u00e8\t\31\2\2\u00e7\u00e3\3\2\2\2\u00e7"+
		"\u00e4\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e6\3\2\2\2\u00e8H\3\2\2\2"+
		"\u00e9\u00eb\t\32\2\2\u00ea\u00e9\3\2\2\2\u00ebJ\3\2\2\2\u00ec\u00ee\7"+
		"\"\2\2\u00ed\u00ec\3\2\2\2\u00ee\u00f1\3\2\2\2\u00ef\u00ed\3\2\2\2\u00ef"+
		"\u00f0\3\2\2\2\u00f0\u00f2\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2\u00f3\5M"+
		"&\2\u00f3\u00f4\3\2\2\2\u00f4\u00f5\b%\3\2\u00f5L\3\2\2\2\u00f6\u00fc"+
		"\5W+\2\u00f7\u00fc\5Y,\2\u00f8\u00fc\5O\'\2\u00f9\u00fc\5S)\2\u00fa\u00fc"+
		"\5U*\2\u00fb\u00f6\3\2\2\2\u00fb\u00f7\3\2\2\2\u00fb\u00f8\3\2\2\2\u00fb"+
		"\u00f9\3\2\2\2\u00fb\u00fa\3\2\2\2\u00fcN\3\2\2\2\u00fd\u00ff\5Q(\2\u00fe"+
		"\u00fd\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u00fe\3\2\2\2\u0100\u0101\3\2"+
		"\2\2\u0101\u0103\3\2\2\2\u0102\u0104\7\"\2\2\u0103\u0102\3\2\2\2\u0103"+
		"\u0104\3\2\2\2\u0104P\3\2\2\2\u0105\u0107\t\33\2\2\u0106\u0105\3\2\2\2"+
		"\u0107R\3\2\2\2\u0108\u010a\7%\2\2\u0109\u010b\t\26\2\2\u010a\u0109\3"+
		"\2\2\2\u010b\u010c\3\2\2\2\u010c\u010a\3\2\2\2\u010c\u010d\3\2\2\2\u010d"+
		"T\3\2\2\2\u010e\u0110\t\27\2\2\u010f\u010e\3\2\2\2\u0110\u0111\3\2\2\2"+
		"\u0111\u010f\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\3\2\2\2\u0113\u0115"+
		"\7\'\2\2\u0114\u0113\3\2\2\2\u0114\u0115\3\2\2\2\u0115V\3\2\2\2\u0116"+
		"\u011a\7$\2\2\u0117\u0119\n\34\2\2\u0118\u0117\3\2\2\2\u0119\u011c\3\2"+
		"\2\2\u011a\u0118\3\2\2\2\u011a\u011b\3\2\2\2\u011b\u011d\3\2\2\2\u011c"+
		"\u011a\3\2\2\2\u011d\u011e\7$\2\2\u011eX\3\2\2\2\u011f\u0123\7)\2\2\u0120"+
		"\u0122\n\35\2\2\u0121\u0120\3\2\2\2\u0122\u0125\3\2\2\2\u0123\u0121\3"+
		"\2\2\2\u0123\u0124\3\2\2\2\u0124\u0126\3\2\2\2\u0125\u0123\3\2\2\2\u0126"+
		"\u0127\7)\2\2\u0127Z\3\2\2\2\30\2\3\4cqy|~\u0087\u00d8\u00e7\u00ea\u00ef"+
		"\u00fb\u0100\u0103\u0106\u010c\u0111\u0114\u011a\u0123\6\7\3\2\6\2\2\7"+
		"\4\2\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}