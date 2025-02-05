
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

htmlTag
    : table EOF
    | tagContent EOF
    ;

table
    : TAG_OPEN TABLE htmlAttribute* TAG_CLOSE WS? (tr WS?)+ TAG_OPEN TAG_SLASH TABLE TAG_CLOSE
    ;

tagContent
    : htmlChardata? ((htmlElement | htmlComment) htmlChardata?)*
    ;

tr
    : TAG_OPEN TR TAG_CLOSE WS? (td WS?)+ TAG_OPEN TAG_SLASH TR TAG_CLOSE
    ;

td
    : TAG_OPEN TD htmlAttribute* TAG_CLOSE tdContent TAG_OPEN TAG_SLASH TD TAG_CLOSE
    ;

tdContent
    : htmlChardata? ((table | htmlElement | htmlComment) htmlChardata?)*
    ;

htmlElement
    : fontTag
    | bTag
    | iTag
    | uTag
    | sTag
    | oTag
    | subTag
    | supTag
    | vtTag
    | vbTag
    | vcTag
    | hlTag
    | hrTag
    | hcTag
    | brTag
    ;

fontTag
    : TAG_OPEN FONT htmlAttribute* TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH FONT TAG_CLOSE
    ;

bTag
    : TAG_OPEN B TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH B TAG_CLOSE
    ;

iTag
    : TAG_OPEN I TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH I TAG_CLOSE
    ;

uTag
    : TAG_OPEN U TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH U TAG_CLOSE
    ;

sTag
    : TAG_OPEN S TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH S TAG_CLOSE
    ;

oTag
    : TAG_OPEN O TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH O TAG_CLOSE
    ;

subTag
    : TAG_OPEN SUB TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH SUB TAG_CLOSE
    ;

supTag
    : TAG_OPEN SUP TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH SUP TAG_CLOSE
    ;

vtTag
    : TAG_OPEN VT TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH VT TAG_CLOSE
    ;

vbTag
    : TAG_OPEN VB TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH VB TAG_CLOSE
    ;

vcTag
    : TAG_OPEN VC TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH VC TAG_CLOSE
    ;

hlTag
    : TAG_OPEN HL TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH HL TAG_CLOSE
    ;

hrTag
    : TAG_OPEN HR TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH HR TAG_CLOSE
    ;

hcTag
    : TAG_OPEN HC TAG_CLOSE tagContent? TAG_OPEN TAG_SLASH HC TAG_CLOSE
    ;

brTag
    : TAG_OPEN BR TAG_SLASH_CLOSE
    ;

htmlAttribute
    : TAG_NAME (TAG_EQUALS ATTVALUE_VALUE)?
    ;

htmlChardata
    : HTML_TEXT
    | WS
    ;

htmlComment
    : HTML_COMMENT
    | HTML_CONDITIONAL_COMMENT
    ;