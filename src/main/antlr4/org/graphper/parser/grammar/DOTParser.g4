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

parser grammar DOTParser;

options { tokenVocab=DOTLexer; }

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
      id_? LB stmt_list RB EOF
    ;

/*
 * List of statements within the graph.
 */
stmt_list
    : (stmt (COMMA | SEMI_COLON)?)*
    ;

/*
 * A single statement which can be a node, edge, attribute, etc.
 */
stmt
    : node_stmt
    | edge_stmt
    | attr_stmt
    | graph_a_list
    | id_ EQUAL id_
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
    : (LSB a_list? RSB)+
    ;

graph_a_list
    : a_list
    ;

/*
 * A list of key-value attribute pairs.
 */
a_list
    : (id_ EQUAL value (COMMA | SEMI_COLON)?)+
    ;

value
    : table_wrapper
    | id_
    ;

table_wrapper
    : TAG_OPEN htmlElement TAG_CLOSE
//    : LT STRING GT
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
    : {directed}? DA
        # directedEdge
    | { !directed }? UDA
        # undirectedEdge
    | DA
        {
            if (!directed) {
                notifyErrorListeners("Cannot use '->' in an undirected graph.");
            }
        }
        # invalidDirectedEdge
    | UDA
        {
            if (directed) {
                notifyErrorListeners("Cannot use '--' in a directed graph.");
            }
        }
        # invalidUndirectedEdge
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
    : COLON id_ (COLON id_)?
    ;

/*
 * Subgraph definition.
 */
subgraph
    : (SUBGRAPH id_?)? LB stmt_list RB
    ;

/*
 * Identifier which can be ID, STRING, HTML_STRING, or NUMBER.
 */
id_
    : ID
    | STRING
    | NUMBER
    ;

htmlElement
    : TAG_OPEN TAG_NAME htmlAttribute* (
        TAG_CLOSE (htmlContent TAG_OPEN TAG_SLASH TAG_NAME TAG_CLOSE)?
        | TAG_SLASH_CLOSE
    )
    ;

htmlContent
    : htmlChardata? ((htmlElement | htmlComment) htmlChardata?)*
    ;

htmlAttribute
    : TAG_NAME (TAG_EQUALS ATTVALUE_VALUE)?
    ;

htmlChardata
    : HTML_TEXT
    | WS
    ;

htmlMisc
    : htmlComment
    | WS
    ;

htmlComment
    : HTML_COMMENT
    | HTML_CONDITIONAL_COMMENT
    ;
