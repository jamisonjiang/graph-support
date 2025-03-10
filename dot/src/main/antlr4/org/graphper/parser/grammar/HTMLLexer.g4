/*
 [The "BSD licence"]
 Copyright (c) 2013 Tom Everett
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// $antlr-format alignTrailingComments true, columnLimit 150, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine true, allowShortBlocksOnASingleLine true, minEmptyLines 0, alignSemicolons ownLine
// $antlr-format alignColons trailing, singleLineOverrulesHangingColon true, alignLexerCommands true, alignLabels true, alignTrailers true

lexer grammar HTMLLexer;

HTML_COMMENT: '<!--' .*? '-->';
HTML_CONDITIONAL_COMMENT: '<![' .*? ']>';

WS: (' ' | '\t' | '\r'? '\n')+;

TAG_OPEN: '<' -> pushMode(TAG);

HTML_TEXT: ~'<'+;

// Tag declarations

mode TAG;

TABLE: [Tt][Aa][Bb][Ll][Ee];
TR: [Tt][Rr];
TD: [Tt][Dd];
FONT: [Ff][Oo][Nn][Tt];
B: [Bb];
I: [Ii];
U: [Uu];
S: [Ss];
O: [Oo];
SUB: [Ss][Uu][Bb];
SUP: [Ss][Uu][Pp];
BR: [Bb][Rr];
VT: [Vv][Tt];
VB: [Vv][Bb];
VC: [Vv][Cc];
HL: [Hh][Ll];
HR: [Hh][Rr];
HC: [Hh][Cc];

COMMA: ',';

SEMI_COLON: ';';

TAG_CLOSE: '>' -> popMode;

TAG_SLASH_CLOSE: '/>' -> popMode;

TAG_SLASH: '/';

TAG_EQUALS: '=' -> pushMode(ATTVALUE);

TAG_NAME: TAG_NameStartChar TAG_NameChar*;

TAG_WHITESPACE: [ \t\r\n] -> channel(HIDDEN);

fragment HEXDIGIT: [a-fA-F0-9];

fragment DIGIT: [0-9];

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
    [:a-zA-Z]
    | '\u2070' ..'\u218F'
    | '\u2C00' ..'\u2FEF'
    | '\u3001' ..'\uD7FF'
    | '\uF900' ..'\uFDCF'
    | '\uFDF0' ..'\uFFFD'
;

// Attribute values

mode ATTVALUE;

ATTVALUE_VALUE: ' '* ATTRIBUTE -> popMode;

ATTRIBUTE: DOUBLE_QUOTE_STRING | SINGLE_QUOTE_STRING | ATTCHARS | HEXCHARS | DECCHARS;

fragment ATTCHARS: ATTCHAR+ ' '?;

fragment ATTCHAR: '-' | '_' | '.' | '/' | '+' | '?' | '=' | ':' | '#' | [0-9a-zA-Z];

fragment HEXCHARS: '#' [0-9a-fA-F]+;

fragment DECCHARS: [0-9]+ '%'?;

fragment DOUBLE_QUOTE_STRING: '"' ~[<"]* '"';

fragment SINGLE_QUOTE_STRING: '\'' ~[<']* '\'';