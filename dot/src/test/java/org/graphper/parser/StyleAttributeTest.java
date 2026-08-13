/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.NodeStyle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StyleAttributeTest {

  @Test
  public void supportedStylesSurviveUnsupportedValues() throws Exception {
    Graphviz graph = DotParser.parse("digraph { node[style=\"rounded,filled\"];"
        + " a[shape=hexagon,fillcolor=white] }");
    Node node = graph.nodes().iterator().next();
    String svg = graph.toSvgStr();
    Assertions.assertTrue(node.nodeAttrs().getStyles().contains(NodeStyle.ROUNDED));
    Assertions.assertTrue(svg.contains("id=\"a_path\""));
    Assertions.assertFalse(svg.contains("id=\"a_polygon\""));
  }
}
