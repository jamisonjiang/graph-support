package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.junit.jupiter.api.Test;

class DotTokenMakerTest {

  @Test
  void highlightsDotKeywordsAttributesStringsAndComments() {
    DotTokenMaker maker = new DotTokenMaker();
    String source = "digraph G { node [shape=\"box\"]; // comment";
    Token token = maker.getTokenList(new Segment(source.toCharArray(), 0, source.length()),
        TokenTypes.NULL, 0);

    assertTrue(has(token, "digraph", TokenTypes.RESERVED_WORD));
    assertTrue(has(token, "node", TokenTypes.RESERVED_WORD_2));
    assertTrue(has(token, "shape", TokenTypes.FUNCTION));
    assertTrue(has(token, "\"box\"", TokenTypes.LITERAL_STRING_DOUBLE_QUOTE));
    assertTrue(has(token, "// comment", TokenTypes.COMMENT_EOL));
  }

  @Test
  void preservesMultilineCommentState() {
    DotTokenMaker maker = new DotTokenMaker();
    Token token = maker.getTokenList(new Segment("still comment */ node".toCharArray(), 0, 21),
        TokenTypes.COMMENT_MULTILINE, 0);

    assertEquals(TokenTypes.COMMENT_MULTILINE, token.getType());
    assertTrue(has(token, "node", TokenTypes.RESERVED_WORD_2));
  }

  private static boolean has(Token token, String lexeme, int type) {
    while (token != null && token.isPaintable()) {
      if (lexeme.equals(token.getLexeme()) && type == token.getType()) {
        return true;
      }
      token = token.getNextToken();
    }
    return false;
  }
}
