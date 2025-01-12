lexer grammar DOTLexer;

/*
 * Lexer rules for keywords (case-insensitive).
 */
STRICT
    : [Ss] [Tt] [Rr] [Ii] [Cc] [Tt]
    ;

GRAPH
    : [Gg] [Rr] [Aa] [Pp] [Hh]
    ;

DIGRAPH
    : [Dd] [Ii] [Gg] [Rr] [Aa] [Pp] [Hh]
    ;

NODE
    : [Nn] [Oo] [Dd] [Ee]
    ;

EDGE
    : [Ee] [Dd] [Gg] [Ee]
    ;

SUBGRAPH
    : [Ss] [Uu] [Bb] [Gg] [Rr] [Aa] [Pp] [Hh]
    ;

TABLE
    : [Tt] [Aa] [Bb] [Ll] [Ee]
    ;

TR
    : [Tt] [Rr]
    ;

TD
    : [Tt] [Dd]
    ;

/*
 * Lexer rule for numbers, including integers and decimals.
 */
NUMBER
    : '-'? ('.' DIGIT+ | DIGIT+ ( '.' DIGIT*)?)
    ;

fragment DIGIT
    : [0-9]
    ;

/*
 * Lexer rule for double-quoted strings with escape sequences.
 */
STRING
    : '"' ( ESC_SEQ | ~["\\] )* '"'
        {
            String content = getText().substring(1, getText().length() - 1);
            content = org.apache_gs.commons.text.StringEscapeUtils.unescapeJava(content);
            setText(content);
        }
    ;

/*
 * Fragment for escape sequences within strings.
 */
fragment ESC_SEQ
    : '\\' [nrt"\\bf]
    ;

/*
 * Lexer rule for identifiers.
 */
ID
    : LETTER (LETTER | DIGIT)*
    ;

fragment LETTER
    : [a-zA-Z\u0080-\u00FF_]
    ;

/*
 * Lexer rules for comments to be skipped.
 */
COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' .*? '\r'? '\n' -> skip
    ;

/*
 * Lexer rule for preprocessor lines to be skipped.
 */
PREPROC
    : '#' ~[\r\n]* -> skip
    ;

/*
 * Lexer rule for whitespace to be skipped.
 */
WS
    : [ \t\n\r]+ -> skip
    ;

/*
 * Parentheses and other symbols
 */
LB
    : '{'
    ;

RB
    : '}'
    ;

LSB
    : '['
    ;

RSB
    : ']'
    ;

COLON
    : ':'
    ;

SEMI_COLON
    : ';'
    ;

COMMA
    : ','
    ;

EQUAL
    : '='
    ;

SLASH
    : '/'
    ;

DA
    : '->'
    ;

UDA
    : '--'
    ;

/*
 * HTML Specific Tokens
 */
HTML_COMMENT: '<!--' .*? '-->';

HTML_CONDITIONAL_COMMENT: '<![' .*? ']>';

TAG_OPEN: '<' -> pushMode(TAG);
//TAG_OPEN: '<';

mode TAG;

HTML_TEXT: (~'<')+ ;

TAG_CLOSE: '>' -> popMode;

TAG_SLASH_CLOSE: '/>' -> popMode;

TAG_SLASH: '/';

TAG_EQUALS: '=' -> pushMode(ATTVALUE);

TAG_NAME: TAG_NameStartChar TAG_NameChar*;

TAG_WHITESPACE: [ \t\r\n] -> channel(HIDDEN);

fragment HEXDIGIT: [a-fA-F0-9];

fragment TAG_NameChar:
    TAG_NameStartChar
    | '-'
    | '_'
    | '.'
    | DIGIT
    | '\u00B7'
    | '\u0300' ..'\u036F'
    | '\u203F' ..'\u2040'
;

fragment TAG_NameStartChar:
    [a-zA-Z]
    | '\u2070' ..'\u218F'
    | '\u2C00' ..'\u2FEF'
    | '\u3001' ..'\uD7FF'
    | '\uF900' ..'\uFDCF'
    | '\uFDF0' ..'\uFFFD'
;

/*
 * Attribute values mode
 */
mode ATTVALUE;

/*
 * An attribute value may have spaces between the '=' and the value
 */
ATTVALUE_VALUE: ' '* ATTRIBUTE -> popMode;

ATTRIBUTE
    : DOUBLE_QUOTE_STRING
    | SINGLE_QUOTE_STRING
    | ATTCHARS
    | HEXCHARS
    | DECCHARS
    ;

/*
 * Attribute character sequences
 */
fragment ATTCHARS: ATTCHAR+ ' '?;

fragment ATTCHAR
    : '-' | '_' | '.' | '/' | '+' | ',' | '?' | '=' | ':' | ';' | '#' | [0-9a-zA-Z]
    ;

/*
 * Hexadecimal characters in attributes
 */
fragment HEXCHARS: '#' [0-9a-fA-F]+;

/*
 * Decimal characters in attributes
 */
fragment DECCHARS: [0-9]+ '%'?;

/*
 * Double-quoted string for attributes
 */
DOUBLE_QUOTE_STRING: '"' ~[<"]* '"';

/*
 * Single-quoted string for attributes
 */
SINGLE_QUOTE_STRING: '\'' ~[<']* '\'';
