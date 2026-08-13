/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import org.graphper.api.Graphviz;
import org.graphper.api.Html;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlTablePortParityTest {

  @Test
  public void parsedTableMatchesJavaTableCellRegistration() throws Exception {
    Node javaNode = Node.builder().id("java").shape(NodeShapeEnum.RECT)
        .table(Html.table().border(0).cellBorder(1).cellSpacing(0).cellPadding(6)
                   .tr(Html.td().id("input").text("input"),
                       Html.td().text("body"),
                       Html.td().id("output").text("output")))
        .build();
    Node parsedNode = DotParser.parse("digraph { parsed[shape=rect,label=<<TABLE BORDER=\"0\" "
        + "CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"6\"><TR>"
        + "<TD PORT=\"input\">input</TD><TD>body</TD>"
        + "<TD PORT=\"output\">output</TD></TR></TABLE>>] }")
        .nodes().iterator().next();

    Assertions.assertEquals("input", parsedNode.nodeAttrs().getTable().getTr(0).getTd(0).getId());
    Assertions.assertEquals("output", parsedNode.nodeAttrs().getTable().getTr(0).getTd(2).getId());

    DrawGraph javaDraw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addNode(javaNode).build());
    DrawGraph parsedDraw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addNode(parsedNode).build());
    NodeDrawProp javaProp = javaDraw.getNodeDrawProp(javaNode);
    NodeDrawProp parsedProp = parsedDraw.getNodeDrawProp(parsedNode);

    Assertions.assertNotNull(javaProp.getCell().getCellById("input"));
    Assertions.assertNotNull(javaProp.getCell().getCellById("output"));
    Assertions.assertNotNull(parsedProp.getCell().getCellById("input"));
    Assertions.assertNotNull(parsedProp.getCell().getCellById("output"));
  }
}
