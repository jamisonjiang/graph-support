/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SameEndpointAttributeTest {

  @Test
  public void parsesSameTailAndSameHead() {
    Graphviz graph = DotParser.parse("digraph { a -> b [sametail=out,samehead=in] }");
    Line line = graph.lines().iterator().next();
    Assertions.assertEquals("out", line.lineAttrs().getSameTail());
    Assertions.assertEquals("in", line.lineAttrs().getSameHead());
  }
}
