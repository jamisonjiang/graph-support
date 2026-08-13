/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserLimitTest {

  @Test
  public void rejectsExcessiveDotNestingBeforeRecursiveParse() {
    StringBuilder dot = new StringBuilder("digraph {");
    for (int i = 0; i <= DotParser.MAX_NESTING_DEPTH; i++) {
      dot.append("subgraph {");
    }
    for (int i = 0; i <= DotParser.MAX_NESTING_DEPTH; i++) {
      dot.append('}');
    }
    dot.append('}');
    Assertions.assertThrows(IllegalArgumentException.class, () -> DotParser.parse(dot.toString()));
  }

  @Test
  public void rejectsMalformedHtmlEnvelope() {
    Assertions.assertNull(HtmlParser.parse("<TABLE"));
    Assertions.assertNull(HtmlParser.parse("TABLE></TABLE>"));
  }
}
