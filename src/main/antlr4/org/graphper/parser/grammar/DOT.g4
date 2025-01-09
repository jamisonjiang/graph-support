/*
 [The "BSD licence"]
 Copyright (c) 2013 Terence Parr
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
/** Derived from http://www.graphviz.org/doc/info/lang.html.
    Comments pulled from spec.
 */

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging

grammar DOT;

@parser::members {
    // Variable to track if the current graph is directed
    boolean directed = false;
}

/*
 * Entry point for the grammar.
 */
graph
    : STRICT?
      (GRAPH { directed = false; }
       | DIGRAPH { directed = true; }
      )
      id_? '{' stmt_list '}' EOF
    ;

/*
 * List of statements within the graph.
 */
stmt_list
    : (stmt ';'?)*
    ;

/*
 * A single statement which can be a node, edge, attribute, etc.
 */
stmt
    : node_stmt
    | edge_stmt
    | attr_stmt
    | graph_a_list
    | id_ '=' id_
    | subgraph
    ;

/*
 * Attribute statement for graph, node, or edge.
 */
attr_stmt
    : (GRAPH | NODE | EDGE) attr_list
    ;

/*
 * List of attributes enclosed in square brackets.
 */
attr_list
    : ('[' a_list? ']')+
    ;

/*
 * A list of key-value attribute pairs.
 */
a_list
    : (id_ '=' id_ (';' | ',')?)+
    ;

graph_a_list
    : a_list
    ;

/*
 * Edge statement connecting nodes or subgraphs.
 */
edge_stmt
    : (node_id | subgraph) edgeRHS attr_list?
    ;

/*
 * Right-hand side of an edge, consisting of edge operators and targets.
 */
edgeRHS
    : (edgeop ( node_id | subgraph))+
    ;

/*
 * Edge operators: '->' for directed and '--' for undirected edges.
 */
edgeop
    : {directed}? '->'
        # directedEdge
    | { !directed }? '--'
        # undirectedEdge
    | '->'
        {
            if (!directed) {
                notifyErrorListeners("Cannot use '->' in an undirected graph.");
            }
        }
        # invalidDirectedEdge
     ;


/*
 * Node statement with optional attributes.
 */
node_stmt
    : node_id attr_list?
    ;

/*
 * Node identifier with optional port.
 */
node_id
    : id_ port?
    ;

/*
 * Port specification for a node.
 */
port
    : ':' id_ (':' id_)?
    ;

/*
 * Subgraph definition.
 */
subgraph
    : (SUBGRAPH id_?)? '{' stmt_list '}'
    ;

/*
 * Identifier which can be ID, STRING, HTML_STRING, or NUMBER.
 */
id_
    : ID
    | STRING
    | HTML_STRING
    | NUMBER
    ;

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
 * Lexer rule for HTML strings enclosed in angle brackets.
 */
HTML_STRING
    : '<' (TAG | ~ [<>])* '>'
    ;

fragment TAG
    : '<' .*? '>'
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