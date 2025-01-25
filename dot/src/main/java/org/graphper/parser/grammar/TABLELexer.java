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
		HTML_CONDITIONAL_COMMENT=1, WS=2, TAG_OPEN=3, HTML_TEXT=4, TABLE=5, TR=6, 
		TD=7, COMMA=8, SEMI_COLON=9, TAG_CLOSE=10, TAG_SLASH_CLOSE=11, TAG_SLASH=12, 
		TAG_EQUALS=13, TAG_NAME=14, TAG_WHITESPACE=15, ATTVALUE_VALUE=16, ATTRIBUTE=17;
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
			"HTML_CONDITIONAL_COMMENT", "WS", "TAG_OPEN", "HTML_TEXT", "TABLE", "TR", 
			"TD", "COMMA", "SEMI_COLON", "TAG_CLOSE", "TAG_SLASH_CLOSE", "TAG_SLASH", 
			"TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", "HEXDIGIT", "DIGIT", "TAG_NameChar", 
			"TAG_NameStartChar", "ATTVALUE_VALUE", "ATTRIBUTE", "ATTCHARS", "ATTCHAR", 
			"HEXCHARS", "DECCHARS", "DOUBLE_QUOTE_STRING", "SINGLE_QUOTE_STRING"
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
			"TAG_EQUALS", "TAG_NAME", "TAG_WHITESPACE", "ATTVALUE_VALUE", "ATTRIBUTE"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\23\u00cd\b\1\b\1"+
		"\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4"+
		"\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t"+
		"\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t"+
		"\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\3\2\3\2\3\2\3\2\3\2\7\2A\n"+
		"\2\f\2\16\2D\13\2\3\2\3\2\3\2\3\3\3\3\5\3K\n\3\3\3\6\3N\n\3\r\3\16\3O"+
		"\3\4\3\4\3\4\3\4\3\5\6\5W\n\5\r\5\16\5X\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3"+
		"\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f"+
		"\3\f\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\7\17|\n\17\f\17\16\17\177\13"+
		"\17\3\20\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\23\5\23\u008d"+
		"\n\23\3\24\5\24\u0090\n\24\3\25\7\25\u0093\n\25\f\25\16\25\u0096\13\25"+
		"\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\5\26\u00a1\n\26\3\27\6\27"+
		"\u00a4\n\27\r\27\16\27\u00a5\3\27\5\27\u00a9\n\27\3\30\5\30\u00ac\n\30"+
		"\3\31\3\31\6\31\u00b0\n\31\r\31\16\31\u00b1\3\32\6\32\u00b5\n\32\r\32"+
		"\16\32\u00b6\3\32\5\32\u00ba\n\32\3\33\3\33\7\33\u00be\n\33\f\33\16\33"+
		"\u00c1\13\33\3\33\3\33\3\34\3\34\7\34\u00c7\n\34\f\34\16\34\u00ca\13\34"+
		"\3\34\3\34\3B\2\35\5\3\7\4\t\5\13\6\r\7\17\b\21\t\23\n\25\13\27\f\31\r"+
		"\33\16\35\17\37\20!\21#\2%\2\'\2)\2+\22-\23/\2\61\2\63\2\65\2\67\29\2"+
		"\5\2\3\4\24\4\2\13\13\"\"\3\2>>\4\2VVvv\4\2CCcc\4\2DDdd\4\2NNnn\4\2GG"+
		"gg\4\2TTtt\4\2FFff\5\2\13\f\17\17\"\"\5\2\62;CHch\3\2\62;\4\2/\60aa\5"+
		"\2\u00b9\u00b9\u0302\u0371\u2041\u2042\n\2<<C\\c|\u2072\u2191\u2c02\u2ff1"+
		"\u3003\ud801\uf902\ufdd1\ufdf2\uffff\n\2%%--/<??AAC\\aac|\4\2$$>>\4\2"+
		"))>>\2\u00d5\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\3\r\3\2"+
		"\2\2\3\17\3\2\2\2\3\21\3\2\2\2\3\23\3\2\2\2\3\25\3\2\2\2\3\27\3\2\2\2"+
		"\3\31\3\2\2\2\3\33\3\2\2\2\3\35\3\2\2\2\3\37\3\2\2\2\3!\3\2\2\2\4+\3\2"+
		"\2\2\4-\3\2\2\2\5;\3\2\2\2\7M\3\2\2\2\tQ\3\2\2\2\13V\3\2\2\2\rZ\3\2\2"+
		"\2\17`\3\2\2\2\21c\3\2\2\2\23f\3\2\2\2\25h\3\2\2\2\27j\3\2\2\2\31n\3\2"+
		"\2\2\33s\3\2\2\2\35u\3\2\2\2\37y\3\2\2\2!\u0080\3\2\2\2#\u0084\3\2\2\2"+
		"%\u0086\3\2\2\2\'\u008c\3\2\2\2)\u008f\3\2\2\2+\u0094\3\2\2\2-\u00a0\3"+
		"\2\2\2/\u00a3\3\2\2\2\61\u00ab\3\2\2\2\63\u00ad\3\2\2\2\65\u00b4\3\2\2"+
		"\2\67\u00bb\3\2\2\29\u00c4\3\2\2\2;<\7>\2\2<=\7#\2\2=>\7]\2\2>B\3\2\2"+
		"\2?A\13\2\2\2@?\3\2\2\2AD\3\2\2\2BC\3\2\2\2B@\3\2\2\2CE\3\2\2\2DB\3\2"+
		"\2\2EF\7_\2\2FG\7@\2\2G\6\3\2\2\2HN\t\2\2\2IK\7\17\2\2JI\3\2\2\2JK\3\2"+
		"\2\2KL\3\2\2\2LN\7\f\2\2MH\3\2\2\2MJ\3\2\2\2NO\3\2\2\2OM\3\2\2\2OP\3\2"+
		"\2\2P\b\3\2\2\2QR\7>\2\2RS\3\2\2\2ST\b\4\2\2T\n\3\2\2\2UW\n\3\2\2VU\3"+
		"\2\2\2WX\3\2\2\2XV\3\2\2\2XY\3\2\2\2Y\f\3\2\2\2Z[\t\4\2\2[\\\t\5\2\2\\"+
		"]\t\6\2\2]^\t\7\2\2^_\t\b\2\2_\16\3\2\2\2`a\t\4\2\2ab\t\t\2\2b\20\3\2"+
		"\2\2cd\t\4\2\2de\t\n\2\2e\22\3\2\2\2fg\7.\2\2g\24\3\2\2\2hi\7=\2\2i\26"+
		"\3\2\2\2jk\7@\2\2kl\3\2\2\2lm\b\13\3\2m\30\3\2\2\2no\7\61\2\2op\7@\2\2"+
		"pq\3\2\2\2qr\b\f\3\2r\32\3\2\2\2st\7\61\2\2t\34\3\2\2\2uv\7?\2\2vw\3\2"+
		"\2\2wx\b\16\4\2x\36\3\2\2\2y}\5)\24\2z|\5\'\23\2{z\3\2\2\2|\177\3\2\2"+
		"\2}{\3\2\2\2}~\3\2\2\2~ \3\2\2\2\177}\3\2\2\2\u0080\u0081\t\13\2\2\u0081"+
		"\u0082\3\2\2\2\u0082\u0083\b\20\5\2\u0083\"\3\2\2\2\u0084\u0085\t\f\2"+
		"\2\u0085$\3\2\2\2\u0086\u0087\t\r\2\2\u0087&\3\2\2\2\u0088\u008d\5)\24"+
		"\2\u0089\u008d\t\16\2\2\u008a\u008d\5%\22\2\u008b\u008d\t\17\2\2\u008c"+
		"\u0088\3\2\2\2\u008c\u0089\3\2\2\2\u008c\u008a\3\2\2\2\u008c\u008b\3\2"+
		"\2\2\u008d(\3\2\2\2\u008e\u0090\t\20\2\2\u008f\u008e\3\2\2\2\u0090*\3"+
		"\2\2\2\u0091\u0093\7\"\2\2\u0092\u0091\3\2\2\2\u0093\u0096\3\2\2\2\u0094"+
		"\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0097\3\2\2\2\u0096\u0094\3\2"+
		"\2\2\u0097\u0098\5-\26\2\u0098\u0099\3\2\2\2\u0099\u009a\b\25\3\2\u009a"+
		",\3\2\2\2\u009b\u00a1\5\67\33\2\u009c\u00a1\59\34\2\u009d\u00a1\5/\27"+
		"\2\u009e\u00a1\5\63\31\2\u009f\u00a1\5\65\32\2\u00a0\u009b\3\2\2\2\u00a0"+
		"\u009c\3\2\2\2\u00a0\u009d\3\2\2\2\u00a0\u009e\3\2\2\2\u00a0\u009f\3\2"+
		"\2\2\u00a1.\3\2\2\2\u00a2\u00a4\5\61\30\2\u00a3\u00a2\3\2\2\2\u00a4\u00a5"+
		"\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a8\3\2\2\2\u00a7"+
		"\u00a9\7\"\2\2\u00a8\u00a7\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\60\3\2\2"+
		"\2\u00aa\u00ac\t\21\2\2\u00ab\u00aa\3\2\2\2\u00ac\62\3\2\2\2\u00ad\u00af"+
		"\7%\2\2\u00ae\u00b0\t\f\2\2\u00af\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1"+
		"\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\64\3\2\2\2\u00b3\u00b5\t\r\2"+
		"\2\u00b4\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7"+
		"\3\2\2\2\u00b7\u00b9\3\2\2\2\u00b8\u00ba\7\'\2\2\u00b9\u00b8\3\2\2\2\u00b9"+
		"\u00ba\3\2\2\2\u00ba\66\3\2\2\2\u00bb\u00bf\7$\2\2\u00bc\u00be\n\22\2"+
		"\2\u00bd\u00bc\3\2\2\2\u00be\u00c1\3\2\2\2\u00bf\u00bd\3\2\2\2\u00bf\u00c0"+
		"\3\2\2\2\u00c0\u00c2\3\2\2\2\u00c1\u00bf\3\2\2\2\u00c2\u00c3\7$\2\2\u00c3"+
		"8\3\2\2\2\u00c4\u00c8\7)\2\2\u00c5\u00c7\n\23\2\2\u00c6\u00c5\3\2\2\2"+
		"\u00c7\u00ca\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00cb"+
		"\3\2\2\2\u00ca\u00c8\3\2\2\2\u00cb\u00cc\7)\2\2\u00cc:\3\2\2\2\27\2\3"+
		"\4BJMOX}\u008c\u008f\u0094\u00a0\u00a5\u00a8\u00ab\u00b1\u00b6\u00b9\u00bf"+
		"\u00c8\6\7\3\2\6\2\2\7\4\2\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}