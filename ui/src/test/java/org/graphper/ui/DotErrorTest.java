package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DotErrorTest {

  @Test
  void parsesLocationAndMessageFromParserOutput() {
    DotError error = DotError.fromMessage("line 3:7 mismatched input '}' expecting ';'");

    assertEquals(3, error.line());
    assertEquals(7, error.column());
    assertEquals("mismatched input '}' expecting ';'", error.message());
  }

  @Test
  void keepsOnlyTheFirstLineOfAMultilineMessage() {
    DotError error = DotError.fromMessage("line 2:4 extraneous input\n  a -> ;\n      ^");

    assertEquals(2, error.line());
    assertEquals(4, error.column());
    assertEquals("extraneous input", error.message());
  }

  @Test
  void fallsBackToStartOfDocumentWhenNoLocationIsPresent() {
    DotError error = DotError.fromMessage("something went wrong");

    assertEquals(1, error.line());
    assertEquals(0, error.column());
    assertEquals("something went wrong", error.message());
  }
}
