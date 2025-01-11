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

 LT
    : '<'
    ;
 GT
    : '>'
    ;

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

 TABLE_OPEN
    : TABLE_ATTR_OPEN WS? HTML_ATTRS* WS? GT
    ;

 TABLE_ATTR_OPEN
    : LT WS? TABLE -> pushMode(ATTR_MODE)
    ;

 TABLE_CLOSE
    : LT WS? SLASH WS? TABLE WS? GT
    ;

 TD_OPEN
    : TD_ATTR_OPEN WS? HTML_ATTRS* WS? GT -> pushMode(TAG_TEXT_MODE)
    ;

 TD_CLOSE
    : LT WS? SLASH WS? TD WS? GT
    ;

 TD_ATTR_OPEN
    : LT WS? TD -> pushMode(ATTR_MODE)
    ;

mode ATTR_MODE;

 HTML_ATTRS
    : ID WS? EQUAL WS? VALUE (WS? COMMA? SEMI_COLON? WS? ID WS? EQUAL WS? VALUE)* -> popMode
    ;

 VALUE
    : ID
    | STRING
    | NUMBER
    ;

mode TAG_TEXT_MODE;

  TAG_TEXT
    : ~('<'|'>')+ -> popMode
    ;

