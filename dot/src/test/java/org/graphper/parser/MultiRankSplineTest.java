/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import org.graphper.api.Assemble;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
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
    Assertions.assertEquals(Splines.ROUNDED,
        DotParser.parse("digraph { splines=rounded; a -> b }").graphAttrs().getSplines());
    Assertions.assertNull(
        DotParser.parse("digraph { layout=fdp; a -> b }").graphAttrs().getSplines());
  }

  @Test
  public void defaultDotRoutingKeepsLongSplineHandlesNonZero() throws Exception {
    Graphviz graph = DotParser.parse("digraph unix {"
        + "nodesep=0.486111; ranksep=0.833333; remincross=true; searchsize=500;"
        + "sh0006 [shape=rect,label=\"\",width=1.820109,height=1.353678];"
        + "sh0007 [shape=rect,label=\"\",width=2.998928,height=1.124674];"
        + "sh0008 [shape=rect,label=\"\",width=4.810262,height=1.124674];"
        + "sh0009 [shape=rect,label=\"\",width=3.440131,height=1.124674];"
        + "sh0006->sh0006[arrowtail=none,arrowhead=none,minlen=1];"
        + "sh0007->sh0006[arrowtail=none,arrowhead=none,minlen=1];"
        + "sh0008->sh0006[arrowtail=none,arrowhead=none,minlen=1,color=\"#000012\"];"
        + "sh0008->sh0007[arrowtail=none,arrowhead=none,minlen=1];"
        + "sh0009->sh0006[arrowtail=none,arrowhead=none,minlen=1];"
        + "sh0009->sh0007[arrowtail=none,arrowhead=none,minlen=1];"
        + "}");

    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    Assertions.assertEquals(Splines.SPLINE, graph.graphAttrs().getSplines());
    Line target = null;
    for (Line line : graph.lines()) {
      if (Color.ofRGB("#000012").equals(line.lineAttrs().getColor())) {
        target = line;
        break;
      }
    }
    Assertions.assertNotNull(target);
    LineDrawProp prop = draw.getLineDrawProp(target);
    Assertions.assertTrue(prop.isBesselCurve());
    Assertions.assertEquals(0, (prop.size() - 1) % 3);
    for (int i = 0; i + 3 < prop.size(); i += 3) {
      assertDifferent(prop.get(i), prop.get(i + 1));
      assertDifferent(prop.get(i + 2), prop.get(i + 3));
    }
  }

  private void assertDifferent(FlatPoint first, FlatPoint second) {
    Assertions.assertTrue(FlatPoint.twoFlatPointDistance(first, second) > 0.000001,
                          first + " should differ from " + second);
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
