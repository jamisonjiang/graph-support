// Generated from org\graphper\parser\grammar\TABLELexer.g4 by ANTLR 4.9.3
package org.graphper.parser.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TABLELexer extends Lexer {
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
		TAG=1, ATTVALUE=2;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "TAG", "ATTVALUE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"HTML_CONDITIONAL_COMMENT", "HTML_COMMENT", "WS", "TAG_OPEN", "HTML_TEXT", 
			"TABLE", "TR", "TD", "FONT", "B", "I", "U", "SUB", "SUP", "BR", "HR", 
			"IMG", "CENTER", "COMMA", "SEMI_COLON", "TAG_CLOSE", "TAG_SLASH_CLOSE", 
			"TAG_SLASH", "TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", "HEXDIGIT", 
			"DIGIT", "TAG_NameChar", "TAG_NameStartChar", "ATTVALUE_VALUE", "ATTRIBUTE", 
			"ATTCHARS", "ATTCHAR", "HEXCHARS", "DECCHARS", "DOUBLE_QUOTE_STRING", 
			"SINGLE_QUOTE_STRING"
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


	public TABLELexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "TABLELexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\36\u0116\b\1\b\1"+
		"\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4"+
		"\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t"+
		"\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t"+
		"\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t"+
		"\37\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\3\2\3\2\3\2\3"+
		"\2\3\2\7\2W\n\2\f\2\16\2Z\13\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\7\3"+
		"e\n\3\f\3\16\3h\13\3\3\3\3\3\3\3\3\3\3\4\3\4\5\4p\n\4\3\4\6\4s\n\4\r\4"+
		"\16\4t\3\5\3\5\3\5\3\5\3\6\6\6|\n\6\r\6\16\6}\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r"+
		"\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21"+
		"\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25"+
		"\3\25\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\31\3\31"+
		"\3\31\3\31\3\32\3\32\7\32\u00c5\n\32\f\32\16\32\u00c8\13\32\3\33\3\33"+
		"\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\36\3\36\5\36\u00d6\n\36\3\37"+
		"\5\37\u00d9\n\37\3 \7 \u00dc\n \f \16 \u00df\13 \3 \3 \3 \3 \3!\3!\3!"+
		"\3!\3!\5!\u00ea\n!\3\"\6\"\u00ed\n\"\r\"\16\"\u00ee\3\"\5\"\u00f2\n\""+
		"\3#\5#\u00f5\n#\3$\3$\6$\u00f9\n$\r$\16$\u00fa\3%\6%\u00fe\n%\r%\16%\u00ff"+
		"\3%\5%\u0103\n%\3&\3&\7&\u0107\n&\f&\16&\u010a\13&\3&\3&\3\'\3\'\7\'\u0110"+
		"\n\'\f\'\16\'\u0113\13\'\3\'\3\'\4Xf\2(\5\3\7\4\t\5\13\6\r\7\17\b\21\t"+
		"\23\n\25\13\27\f\31\r\33\16\35\17\37\20!\21#\22%\23\'\24)\25+\26-\27/"+
		"\30\61\31\63\32\65\33\67\349\2;\2=\2?\2A\35C\36E\2G\2I\2K\2M\2O\2\5\2"+
		"\3\4\37\4\2\13\13\"\"\3\2>>\4\2VVvv\4\2CCcc\4\2DDdd\4\2NNnn\4\2GGgg\4"+
		"\2TTtt\4\2FFff\4\2HHhh\4\2QQqq\4\2PPpp\4\2KKkk\4\2WWww\4\2UUuu\4\2RRr"+
		"r\4\2JJjj\4\2OOoo\4\2IIii\4\2EEee\5\2\13\f\17\17\"\"\5\2\62;CHch\3\2\62"+
		";\4\2/\60aa\5\2\u00b9\u00b9\u0302\u0371\u2041\u2042\n\2<<C\\c|\u2072\u2191"+
		"\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff\n\2%%--/<??AAC\\aac|"+
		"\4\2$$>>\4\2))>>\2\u011f\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\3\17\3\2\2\2\3\21\3\2\2\2\3\23\3\2\2\2\3\25\3\2\2\2\3"+
		"\27\3\2\2\2\3\31\3\2\2\2\3\33\3\2\2\2\3\35\3\2\2\2\3\37\3\2\2\2\3!\3\2"+
		"\2\2\3#\3\2\2\2\3%\3\2\2\2\3\'\3\2\2\2\3)\3\2\2\2\3+\3\2\2\2\3-\3\2\2"+
		"\2\3/\3\2\2\2\3\61\3\2\2\2\3\63\3\2\2\2\3\65\3\2\2\2\3\67\3\2\2\2\4A\3"+
		"\2\2\2\4C\3\2\2\2\5Q\3\2\2\2\7^\3\2\2\2\tr\3\2\2\2\13v\3\2\2\2\r{\3\2"+
		"\2\2\17\177\3\2\2\2\21\u0085\3\2\2\2\23\u0088\3\2\2\2\25\u008b\3\2\2\2"+
		"\27\u0090\3\2\2\2\31\u0092\3\2\2\2\33\u0094\3\2\2\2\35\u0096\3\2\2\2\37"+
		"\u009a\3\2\2\2!\u009e\3\2\2\2#\u00a1\3\2\2\2%\u00a4\3\2\2\2\'\u00a8\3"+
		"\2\2\2)\u00af\3\2\2\2+\u00b1\3\2\2\2-\u00b3\3\2\2\2/\u00b7\3\2\2\2\61"+
		"\u00bc\3\2\2\2\63\u00be\3\2\2\2\65\u00c2\3\2\2\2\67\u00c9\3\2\2\29\u00cd"+
		"\3\2\2\2;\u00cf\3\2\2\2=\u00d5\3\2\2\2?\u00d8\3\2\2\2A\u00dd\3\2\2\2C"+
		"\u00e9\3\2\2\2E\u00ec\3\2\2\2G\u00f4\3\2\2\2I\u00f6\3\2\2\2K\u00fd\3\2"+
		"\2\2M\u0104\3\2\2\2O\u010d\3\2\2\2QR\7>\2\2RS\7#\2\2ST\7]\2\2TX\3\2\2"+
		"\2UW\13\2\2\2VU\3\2\2\2WZ\3\2\2\2XY\3\2\2\2XV\3\2\2\2Y[\3\2\2\2ZX\3\2"+
		"\2\2[\\\7_\2\2\\]\7@\2\2]\6\3\2\2\2^_\7>\2\2_`\7#\2\2`a\7/\2\2ab\7/\2"+
		"\2bf\3\2\2\2ce\13\2\2\2dc\3\2\2\2eh\3\2\2\2fg\3\2\2\2fd\3\2\2\2gi\3\2"+
		"\2\2hf\3\2\2\2ij\7/\2\2jk\7/\2\2kl\7@\2\2l\b\3\2\2\2ms\t\2\2\2np\7\17"+
		"\2\2on\3\2\2\2op\3\2\2\2pq\3\2\2\2qs\7\f\2\2rm\3\2\2\2ro\3\2\2\2st\3\2"+
		"\2\2tr\3\2\2\2tu\3\2\2\2u\n\3\2\2\2vw\7>\2\2wx\3\2\2\2xy\b\5\2\2y\f\3"+
		"\2\2\2z|\n\3\2\2{z\3\2\2\2|}\3\2\2\2}{\3\2\2\2}~\3\2\2\2~\16\3\2\2\2\177"+
		"\u0080\t\4\2\2\u0080\u0081\t\5\2\2\u0081\u0082\t\6\2\2\u0082\u0083\t\7"+
		"\2\2\u0083\u0084\t\b\2\2\u0084\20\3\2\2\2\u0085\u0086\t\4\2\2\u0086\u0087"+
		"\t\t\2\2\u0087\22\3\2\2\2\u0088\u0089\t\4\2\2\u0089\u008a\t\n\2\2\u008a"+
		"\24\3\2\2\2\u008b\u008c\t\13\2\2\u008c\u008d\t\f\2\2\u008d\u008e\t\r\2"+
		"\2\u008e\u008f\t\4\2\2\u008f\26\3\2\2\2\u0090\u0091\t\6\2\2\u0091\30\3"+
		"\2\2\2\u0092\u0093\t\16\2\2\u0093\32\3\2\2\2\u0094\u0095\t\17\2\2\u0095"+
		"\34\3\2\2\2\u0096\u0097\t\20\2\2\u0097\u0098\t\17\2\2\u0098\u0099\t\6"+
		"\2\2\u0099\36\3\2\2\2\u009a\u009b\t\20\2\2\u009b\u009c\t\17\2\2\u009c"+
		"\u009d\t\21\2\2\u009d \3\2\2\2\u009e\u009f\t\6\2\2\u009f\u00a0\t\t\2\2"+
		"\u00a0\"\3\2\2\2\u00a1\u00a2\t\22\2\2\u00a2\u00a3\t\t\2\2\u00a3$\3\2\2"+
		"\2\u00a4\u00a5\t\16\2\2\u00a5\u00a6\t\23\2\2\u00a6\u00a7\t\24\2\2\u00a7"+
		"&\3\2\2\2\u00a8\u00a9\t\25\2\2\u00a9\u00aa\t\b\2\2\u00aa\u00ab\t\r\2\2"+
		"\u00ab\u00ac\t\4\2\2\u00ac\u00ad\t\b\2\2\u00ad\u00ae\t\t\2\2\u00ae(\3"+
		"\2\2\2\u00af\u00b0\7.\2\2\u00b0*\3\2\2\2\u00b1\u00b2\7=\2\2\u00b2,\3\2"+
		"\2\2\u00b3\u00b4\7@\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b6\b\26\3\2\u00b6"+
		".\3\2\2\2\u00b7\u00b8\7\61\2\2\u00b8\u00b9\7@\2\2\u00b9\u00ba\3\2\2\2"+
		"\u00ba\u00bb\b\27\3\2\u00bb\60\3\2\2\2\u00bc\u00bd\7\61\2\2\u00bd\62\3"+
		"\2\2\2\u00be\u00bf\7?\2\2\u00bf\u00c0\3\2\2\2\u00c0\u00c1\b\31\4\2\u00c1"+
		"\64\3\2\2\2\u00c2\u00c6\5?\37\2\u00c3\u00c5\5=\36\2\u00c4\u00c3\3\2\2"+
		"\2\u00c5\u00c8\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\66"+
		"\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c9\u00ca\t\26\2\2\u00ca\u00cb\3\2\2\2"+
		"\u00cb\u00cc\b\33\5\2\u00cc8\3\2\2\2\u00cd\u00ce\t\27\2\2\u00ce:\3\2\2"+
		"\2\u00cf\u00d0\t\30\2\2\u00d0<\3\2\2\2\u00d1\u00d6\5?\37\2\u00d2\u00d6"+
		"\t\31\2\2\u00d3\u00d6\5;\35\2\u00d4\u00d6\t\32\2\2\u00d5\u00d1\3\2\2\2"+
		"\u00d5\u00d2\3\2\2\2\u00d5\u00d3\3\2\2\2\u00d5\u00d4\3\2\2\2\u00d6>\3"+
		"\2\2\2\u00d7\u00d9\t\33\2\2\u00d8\u00d7\3\2\2\2\u00d9@\3\2\2\2\u00da\u00dc"+
		"\7\"\2\2\u00db\u00da\3\2\2\2\u00dc\u00df\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd"+
		"\u00de\3\2\2\2\u00de\u00e0\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0\u00e1\5C"+
		"!\2\u00e1\u00e2\3\2\2\2\u00e2\u00e3\b \3\2\u00e3B\3\2\2\2\u00e4\u00ea"+
		"\5M&\2\u00e5\u00ea\5O\'\2\u00e6\u00ea\5E\"\2\u00e7\u00ea\5I$\2\u00e8\u00ea"+
		"\5K%\2\u00e9\u00e4\3\2\2\2\u00e9\u00e5\3\2\2\2\u00e9\u00e6\3\2\2\2\u00e9"+
		"\u00e7\3\2\2\2\u00e9\u00e8\3\2\2\2\u00eaD\3\2\2\2\u00eb\u00ed\5G#\2\u00ec"+
		"\u00eb\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ee\u00ef\3\2"+
		"\2\2\u00ef\u00f1\3\2\2\2\u00f0\u00f2\7\"\2\2\u00f1\u00f0\3\2\2\2\u00f1"+
		"\u00f2\3\2\2\2\u00f2F\3\2\2\2\u00f3\u00f5\t\34\2\2\u00f4\u00f3\3\2\2\2"+
		"\u00f5H\3\2\2\2\u00f6\u00f8\7%\2\2\u00f7\u00f9\t\27\2\2\u00f8\u00f7\3"+
		"\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb"+
		"J\3\2\2\2\u00fc\u00fe\t\30\2\2\u00fd\u00fc\3\2\2\2\u00fe\u00ff\3\2\2\2"+
		"\u00ff\u00fd\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0102\3\2\2\2\u0101\u0103"+
		"\7\'\2\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2\2\u0103L\3\2\2\2\u0104"+
		"\u0108\7$\2\2\u0105\u0107\n\35\2\2\u0106\u0105\3\2\2\2\u0107\u010a\3\2"+
		"\2\2\u0108\u0106\3\2\2\2\u0108\u0109\3\2\2\2\u0109\u010b\3\2\2\2\u010a"+
		"\u0108\3\2\2\2\u010b\u010c\7$\2\2\u010cN\3\2\2\2\u010d\u0111\7)\2\2\u010e"+
		"\u0110\n\36\2\2\u010f\u010e\3\2\2\2\u0110\u0113\3\2\2\2\u0111\u010f\3"+
		"\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\3\2\2\2\u0113\u0111\3\2\2\2\u0114"+
		"\u0115\7)\2\2\u0115P\3\2\2\2\30\2\3\4Xfort}\u00c6\u00d5\u00d8\u00dd\u00e9"+
		"\u00ee\u00f1\u00f4\u00fa\u00ff\u0102\u0108\u0111\6\7\3\2\6\2\2\7\4\2\2"+
		"\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}