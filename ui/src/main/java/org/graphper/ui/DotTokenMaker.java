/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.ui;

import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

/** Syntax tokenizer for the Graphviz DOT language. */
public class DotTokenMaker extends AbstractTokenMaker {

  @Override
  public TokenMap getWordsToHighlight() {
    TokenMap words = new TokenMap(true);
    words.put("strict", TokenTypes.RESERVED_WORD);
    words.put("graph", TokenTypes.RESERVED_WORD);
    words.put("digraph", TokenTypes.RESERVED_WORD);
    words.put("subgraph", TokenTypes.RESERVED_WORD);
    words.put("node", TokenTypes.RESERVED_WORD_2);
    words.put("edge", TokenTypes.RESERVED_WORD_2);
    for (String attribute : ATTRIBUTES) {
      words.put(attribute, TokenTypes.FUNCTION);
    }
    for (String value : VALUES) {
      words.put(value, TokenTypes.DATA_TYPE);
    }
    words.put("true", TokenTypes.LITERAL_BOOLEAN);
    words.put("false", TokenTypes.LITERAL_BOOLEAN);
    return words;
  }

  @Override
  public Token getTokenList(Segment segment, int initialTokenType, int startOffset) {
    resetTokenList();
    char[] text = segment.array;
    int offset = segment.offset;
    int end = offset + segment.count;
    int index = offset;

    if (initialTokenType == TokenTypes.COMMENT_MULTILINE) {
      int close = commentEnd(text, index, end);
      if (close < 0) {
        addToken(text, index, end - 1, TokenTypes.COMMENT_MULTILINE, startOffset);
        return firstToken;
      }
      addToken(text, index, close + 1, TokenTypes.COMMENT_MULTILINE, startOffset);
      index = close + 2;
    }

    while (index < end) {
      int tokenOffset = startOffset + index - offset;
      char current = text[index];
      if (Character.isWhitespace(current)) {
        int start = index++;
        while (index < end && Character.isWhitespace(text[index])) {
          index++;
        }
        addToken(text, start, index - 1, TokenTypes.WHITESPACE, tokenOffset);
      } else if (current == '#'
          || current == '/' && index + 1 < end && text[index + 1] == '/') {
        addToken(text, index, end - 1, TokenTypes.COMMENT_EOL, tokenOffset);
        index = end;
      } else if (current == '/' && index + 1 < end && text[index + 1] == '*') {
        int close = commentEnd(text, index + 2, end);
        if (close < 0) {
          addToken(text, index, end - 1, TokenTypes.COMMENT_MULTILINE, tokenOffset);
          return firstToken;
        }
        addToken(text, index, close + 1, TokenTypes.COMMENT_MULTILINE, tokenOffset);
        index = close + 2;
      } else if (current == '"') {
        int start = index++;
        boolean escaped = false;
        while (index < end) {
          char c = text[index++];
          if (c == '"' && !escaped) {
            break;
          }
          escaped = c == '\\' && !escaped;
          if (c != '\\') {
            escaped = false;
          }
        }
        addToken(text, start, index - 1, TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, tokenOffset);
      } else if (Character.isDigit(current)
          || current == '.' && index + 1 < end && Character.isDigit(text[index + 1])) {
        int start = index++;
        boolean decimal = current == '.';
        while (index < end
            && (Character.isDigit(text[index]) || !decimal && text[index] == '.')) {
          if (text[index] == '.') {
            decimal = true;
          }
          index++;
        }
        addToken(text, start, index - 1,
            decimal ? TokenTypes.LITERAL_NUMBER_FLOAT : TokenTypes.LITERAL_NUMBER_DECIMAL_INT,
            tokenOffset);
      } else if (isIdentifierStart(current)) {
        int start = index++;
        while (index < end && isIdentifierPart(text[index])) {
          index++;
        }
        int type = wordsToHighlight.get(text, start, index - 1);
        addToken(text, start, index - 1,
            type < 0 ? TokenTypes.IDENTIFIER : type, tokenOffset);
      } else if (isSeparator(current)) {
        addToken(text, index, index, TokenTypes.SEPARATOR, tokenOffset);
        index++;
      } else {
        int start = index++;
        if (index < end && current == '-' && (text[index] == '>' || text[index] == '-')) {
          index++;
        }
        addToken(text, start, index - 1, TokenTypes.OPERATOR, tokenOffset);
      }
    }
    addNullToken();
    return firstToken;
  }

  @Override
  public boolean getCurlyBracesDenoteCodeBlocks(int languageIndex) {
    return true;
  }

  @Override
  public String[] getLineCommentStartAndEnd(int languageIndex) {
    return new String[]{"//", null};
  }

  private static int commentEnd(char[] text, int start, int end) {
    for (int i = start; i + 1 < end; i++) {
      if (text[i] == '*' && text[i + 1] == '/') {
        return i;
      }
    }
    return -1;
  }

  private static boolean isIdentifierStart(char c) {
    return Character.isLetter(c) || c == '_' || c >= 128;
  }

  private static boolean isIdentifierPart(char c) {
    return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c >= 128;
  }

  private static boolean isSeparator(char c) {
    return c == '{' || c == '}' || c == '[' || c == ']' || c == '(' || c == ')'
        || c == ';' || c == ',' || c == ':';
  }

  private static final String[] ATTRIBUTES = {
      "label", "xlabel", "shape", "style", "color", "fillcolor", "bgcolor", "fontcolor",
      "fontname", "fontsize", "rankdir", "rank", "layout", "splines", "nodesep", "ranksep",
      "minlen", "weight", "constraint", "dir", "arrowhead", "arrowtail", "arrowsize",
      "headlabel", "taillabel", "headport", "tailport", "lhead", "ltail", "width", "height",
      "fixedsize", "margin", "penwidth", "peripheries", "orientation", "sides", "regular",
      "group", "compound", "concentrate", "decorate", "samehead", "sametail", "overlap",
      "pack", "packmode", "labelloc", "labeljust", "tooltip", "href", "url"
  };

  private static final String[] VALUES = {
      "TB", "BT", "LR", "RL", "dot", "dotq", "fdp", "jfdp", "gfdp", "spline",
      "polyline", "ortho", "line", "none", "solid", "dashed", "dotted", "bold", "filled",
      "rounded", "box", "rect", "ellipse", "circle", "diamond", "point", "plaintext", "record",
      "cylinder", "normal", "vee", "dot", "both", "forward", "back", "same", "min", "max",
      "source", "sink"
  };
}
