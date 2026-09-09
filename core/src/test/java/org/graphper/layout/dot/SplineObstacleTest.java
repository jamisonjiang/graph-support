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
package org.graphper.layout.dot;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.Curves;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Dense long-edge regression that samples final splines against unrelated node shapes. */
public class SplineObstacleTest {

  @Test
  public void denseLongLabeledSplinesDoNotCrossUnrelatedNodes() throws Exception {
    for (Rankdir rankdir : Rankdir.values()) {
      Scenario scenario = scenario(rankdir);
      DrawGraph draw = Layout.DOT.getLayoutEngine().layout(scenario.graph);
      for (Line line : scenario.longLines) {
        assertAvoidsUnrelatedNodes(draw, line, scenario.nodes, rankdir);
      }
    }
  }

  private Scenario scenario(Rankdir rankdir) {
    int ranks = 6;
    int columns = 4;
    Node[][] nodes = new Node[ranks][columns];
    Graphviz.GraphvizBuilder graph = Graphviz.digraph().layout(Layout.DOT)
        .rankdir(rankdir).splines(Splines.SPLINE).nodeSep(0.45).rankSep(0.75);
    List<Node> allNodes = new ArrayList<>();
    for (int rank = 0; rank < ranks; rank++) {
      Subgraph.SubgraphBuilder same = Subgraph.builder().rank(Rank.SAME);
      for (int column = 0; column < columns; column++) {
        Node node = Node.builder().id("n" + rank + '_' + column)
            .shape(column % 2 == 0 ? NodeShapeEnum.RECT : NodeShapeEnum.ELLIPSE)
            .label("rank " + rank + " col " + column).build();
        nodes[rank][column] = node;
        allNodes.add(node);
        same.addNode(node);
      }
      graph.subgraph(same.build());
    }
    for (int rank = 0; rank < ranks - 1; rank++) {
      for (int column = 0; column < columns; column++) {
        graph.addLine(nodes[rank][column], nodes[rank + 1][column]);
      }
    }

    List<Line> longLines = new ArrayList<>();
    longLines.add(longLine(nodes[0][0], nodes[5][3], "long diagonal 0"));
    longLines.add(longLine(nodes[0][3], nodes[5][0], "long diagonal 1"));
    longLines.add(longLine(nodes[0][1], nodes[5][2], "long center 2"));
    longLines.add(longLine(nodes[1][3], nodes[5][1], "long lower 3"));
    longLines.forEach(graph::addLine);
    return new Scenario(graph.build(), allNodes, longLines);
  }

  private Line longLine(Node tail, Node head, String label) {
    return Line.builder(tail, head).label(label).weight(0.2).build();
  }

  private void assertAvoidsUnrelatedNodes(DrawGraph draw, Line line, List<Node> nodes,
                                          Rankdir rankdir) {
    LineDrawProp prop = draw.getLineDrawProp(line);
    Assertions.assertNotNull(prop, line.toString());
    Assertions.assertTrue(prop.size() >= 4, line.toString());
    for (int i = 0; i + 3 < prop.size(); i += 3) {
      for (int sample = 1; sample < 50; sample++) {
        double t = sample / 50D;
        FlatPoint point = Curves.besselEquationCalc(t, prop.get(i), prop.get(i + 1),
                                                    prop.get(i + 2), prop.get(i + 3));
        for (Node node : nodes) {
          if (node == line.tail() || node == line.head()) {
            continue;
          }
          NodeDrawProp nodeProp = draw.getNodeDrawProp(node);
          Assertions.assertFalse(nodeProp.shapeProp().in(nodeProp, point),
                                 "rankdir=" + rankdir + " line=" + line
                                     + " node=" + node.nodeAttrs().getId() + " point=" + point);
        }
      }
    }
  }

  private static class Scenario {
    private final Graphviz graph;
    private final List<Node> nodes;
    private final List<Line> longLines;

    private Scenario(Graphviz graph, List<Node> nodes, List<Line> longLines) {
      this.graph = graph;
      this.nodes = nodes;
      this.longLines = longLines;
    }
  }
}
