grammar HtmlTable;

// Parser Rules

document
    :   (element | TEXT)* EOF
    ;

element
    :   table
    |   otherElement
    ;

table
    :   '<' 'table' attributes? '>' tableContent '</' 'table' '>'
    ;

tableContent
    :   (tr)*
    ;

tr
    :   '<' 'tr' attributes? '>' trContent '</' 'tr' '>'
    ;

trContent
    :   (th | td)*
    ;

th
    :   '<' 'th' attributes? '>' cellContent '</' 'th' '>'
    ;

td
    :   '<' 'td' attributes? '>' cellContent '</' 'td' '>'
    ;

cellContent
    :   (HTML_TEXT | element)*
    ;

// Rule to handle other HTML elements (if needed)
otherElement
    :   '<' NAME attributes? '>' element* '</' NAME '>'
    ;

// Lexer Rules

// Attributes (simplified)
attributes
    :   (WS NAME '=' '"' VALUE '"' )*
    ;

// Common tokens
NAME
    :   [a-zA-Z][a-zA-Z0-9]*
    ;

VALUE
    :   ~["<>]+
    ;

// Renamed TEXT to HTML_TEXT to avoid conflicts
HTML_TEXT
    :   ~[<]+
    ;

// Symbols
LT : '<';
GT : '>';
SLASH : '/';
EQUAL : '=';
QUOTE : '"';

// Whitespace within HTML
WS
    :   [ \t\r\n]+ -> skip
    ;

// Fragment Rules
fragment LETTER
    :   [a-zA-Z]
    ;

fragment DIGIT
    :   [0-9]
    ;

// Skipping other parts as per user's existing grammar
// Ensure that other lexer rules are defined in the main grammar file

// If integrating into an existing grammar, consider using lexer modes
// Example of switching to HTML_MODE when encountering a specific token

// mode HTML_MODE;
// HTML specific lexer rules here (optional)
