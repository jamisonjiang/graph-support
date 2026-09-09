/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphper.parser;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Port;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.Cell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlTablePortTest {

  @Test
  public void tdPortIsMappedToCellIdAndCompassPort() throws Exception {
    Graphviz graph = DotParser.parse(graph());
    Node octagon = node(graph, "octagon");
    Table table = octagon.nodeAttrs().getTable();
    Assertions.assertNotNull(table);
    List<Td> cells = new ArrayList<>();
    table.getTrs().forEach(row -> cells.addAll(row.getTds()));
    Assertions.assertEquals("input", cells.get(0).getPort());
    Assertions.assertEquals("output", cells.get(2).getPort());

    Line cellSouth = line(graph, octagon, node(graph, "hexagon"));
    Assertions.assertEquals("output", cellSouth.lineAttrs().getTailCell());
    Assertions.assertEquals(Port.SOUTH, cellSouth.lineAttrs().getTailPort());
    Assertions.assertEquals("input", cellSouth.lineAttrs().getHeadCell());
    Assertions.assertEquals(Port.WEST, cellSouth.lineAttrs().getHeadPort());

    DrawGraph drawGraph = org.graphper.api.attributes.Layout.DOT.getLayoutEngine().layout(graph);
    NodeDrawProp octagonProp = drawGraph.getNodeDrawProp(octagon);
    NodeDrawProp hexagonProp = drawGraph.getNodeDrawProp(node(graph, "hexagon"));
    Assertions.assertNotNull(octagonProp.getCell());
    Assertions.assertNotNull(hexagonProp.getCell());
    Cell output = octagonProp.getCell().getCellById("output");
    Cell input = hexagonProp.getCell().getCellById("input");
    Assertions.assertNotNull(output);
    Assertions.assertNotNull(input);

    LineDrawProp prop = drawGraph.getLineDrawProp(cellSouth);
    Assertions.assertNotNull(prop);
    Assertions.assertTrue(prop.size() >= 2);
    org.graphper.def.FlatPoint tailEndpoint = prop.isHeadStart() ? prop.getEnd() : prop.getStart();
    org.graphper.def.FlatPoint headEndpoint = prop.isHeadStart() ? prop.getStart() : prop.getEnd();
    Assertions.assertEquals(output.getCenter(octagonProp).getX(), tailEndpoint.getX(), 0.1);
    Assertions.assertEquals(output.getCellBox(octagonProp).getDownBorder(), tailEndpoint.getY(),
                            0.1);
    Assertions.assertNotNull(prop.getArrowHead());
    Assertions.assertEquals(input.getCellBox(hexagonProp).getLeftBorder(),
                            prop.getArrowHead().getAxisEnd().getX(), 0.1);
  }

  @Test
  public void tdIdAndPortRemainIndependentInEitherAttributeOrder() {
    Graphviz graph = DotParser.parse("digraph { a[label=<<TABLE><TR>"
        + "<TD ID=\"visual-a\" PORT=\"route-a\">a</TD>"
        + "<TD PORT=\"route-b\" ID=\"visual-b\">b</TD>"
        + "</TR></TABLE>>] }");
    Table table = graph.nodes().iterator().next().nodeAttrs().getTable();
    Assertions.assertEquals("visual-a", table.getTr(0).getTd(0).getId());
    Assertions.assertEquals("route-a", table.getTr(0).getTd(0).getPort());
    Assertions.assertEquals("visual-b", table.getTr(0).getTd(1).getId());
    Assertions.assertEquals("route-b", table.getTr(0).getTd(1).getPort());
  }

  private Node node(Graphviz graph, String id) {
    for (Node node : graph.nodes()) {
      if (id.equals(node.nodeAttrs().getId())) {
        return node;
      }
    }
    throw new AssertionError("Missing node " + id);
  }

  private Line line(Graphviz graph, Node tail, Node head) {
    for (Line line : graph.lines()) {
      if (line.tail() == tail && line.head() == head) {
        return line;
      }
    }
    throw new AssertionError("Missing line " + tail + " -> " + head);
  }

  private String graph() {
    return "digraph G { graph[rankdir=LR]; source[shape=box]; target[shape=box];"
        + "octagon[shape=rect,label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" "
        + "CELLSPACING=\"0\" CELLPADDING=\"6\"><TR><TD PORT=\"input\">input</TD>"
        + "<TD>octagon body</TD><TD PORT=\"output\">output</TD></TR></TABLE>>];"
        + "hexagon[shape=rect,label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" "
        + "CELLSPACING=\"0\" CELLPADDING=\"6\"><TR><TD PORT=\"input\">input</TD>"
        + "<TD>hexagon body</TD><TD PORT=\"output\">output</TD></TR></TABLE>>];"
        + "source->octagon:input:w; octagon:output:s->hexagon:input:w;"
        + "hexagon:output:e->target; }";
  }
}
