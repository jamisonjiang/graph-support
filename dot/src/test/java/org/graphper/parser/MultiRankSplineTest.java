/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import org.graphper.api.Assemble;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.attributes.Splines;
import org.graphper.layout.HtmlConvertor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultiRankSplineTest {

  @Test
  public void parsedFixedTablesHonorExplicitDimensions() {
    assertParsedTableSize(55, 27);
    assertParsedTableSize(52, 17);
    assertParsedTableSize(8, 15);
    assertParsedTableSize(6, 15);
  }

  @Test
  public void explicitSplineModeOverridesDotDefault() {
    Assertions.assertNull(
        DotParser.parse("digraph { a -> b }").graphAttrs().getSplines());
    Assertions.assertEquals(Splines.ROUNDED,
        DotParser.parse("digraph { splines=rounded; a -> b }").graphAttrs().getSplines());
    Assertions.assertNull(
        DotParser.parse("digraph { layout=fdp; a -> b }").graphAttrs().getSplines());
  }

  private void assertParsedTableSize(double width, double height) {
    Graphviz graph = DotParser.parse("digraph { a -> b [label=<<TABLE FIXEDSIZE=\"TRUE\" "
        + "WIDTH=\"" + width + "\" HEIGHT=\"" + height
        + "\"><TR><TD></TD></TR></TABLE>>] }");
    Line line = graph.lines().iterator().next();
    Assemble assemble = HtmlConvertor.toAssemble(line.lineAttrs().getTable());
    Assertions.assertEquals(width, assemble.getWidth(), 0.000001);
    Assertions.assertEquals(height, assemble.getHeight(), 0.000001);
  }

}
