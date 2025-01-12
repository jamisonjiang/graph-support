
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

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging

parser grammar HTMLParser;

options {
    tokenVocab = HTMLLexer;
}

table
    : TAG_OPEN TABLE htmlAttributes? TAG_CLOSE WS? (tr WS?)+ TAG_OPEN TAG_SLASH TABLE TAG_CLOSE
    ;

tr
    : TAG_OPEN TR TAG_CLOSE WS? (td WS?)+ TAG_OPEN TAG_SLASH TR TAG_CLOSE
    ;

td
    : TAG_OPEN TD htmlAttributes? TAG_CLOSE htmlContent TAG_OPEN TAG_SLASH TD TAG_CLOSE
    ;

htmlContent
    : htmlChardata? ((table | htmlComment) htmlChardata?)*
    ;

htmlAttributes
    : htmlAttribute ( (COMMA | SEMI_COLON | WS) htmlAttribute )*
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
