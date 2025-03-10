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
            content = content.replaceAll("\\\\(?![nrt\\\\])", ""); // Remove \ not followed by n, r, t, \

            // Replace recognized escape sequences with actual characters
            content = content.replace("\\n", "\n")
                             .replace("\\r", "\r")
                             .replace("\\t", "\t");
            content = org.apache_gs.commons.text.StringEscapeUtils.unescapeJava(content);
            setText(content);
        }
    ;

/*
 * Fragment for escape sequences within strings.
 */
fragment ESC_SEQ
    : '\\' [\\"bfnrt.\n\r]
    | '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment HEX_DIGIT
    : [0-9a-fA-F]
    ;

/** "HTML strings, angle brackets must occur in matched pairs, and
 *  unescaped newlines are allowed."
 */
HTML_STRING
    : '<' (TAG | ~ [<>])* '>'
    ;

fragment TAG
    : '<' .*? '>'
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