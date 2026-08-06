package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DotErrorSpanTest {

  @Test
  void locatesTheExactSyntaxToken() {
    String source = "digraph G { a -> ; }";
    DotErrorSpan span = DotErrorSpan.locate(source,
                                            new DotError(1, source.indexOf(';'),
                                                         "mismatched input ';'"));

    assertEquals(source.indexOf(';'), span.offset());
    assertEquals(1, span.length());
  }

  @Test
  void movesSemanticEdgeErrorBackToTheOperator() {
    String source = "digraph G {\n  a -- b\n}";
    DotErrorSpan span = DotErrorSpan.locate(source,
                                            new DotError(2, 7,
                                                         "Cannot use '--' in a directed graph."));

    assertEquals(source.indexOf("--"), span.offset());
    assertEquals(2, span.length());
  }
}
