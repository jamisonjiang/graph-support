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

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;

public class DotLayoutEngineTest {

  @Test
  public void testPropInherit() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();
    Node h = Node.builder().label("h").build();

    Line l1 = Line.builder(a, b).build();
    Line l2 = Line.builder(c, d).build();
    Line l3 = Line.builder(e, f).build();
    Line l4 = Line.builder(g, h).build();

    Graphviz graphviz = Graphviz
        .digraph()
        .tempNode(Node.builder().fillColor(Color.GREEN).build())
        .addLine(l1)
        .subgraph(
            Subgraph.builder()
                .tempNode(
                    Node.builder()
                        .fillColor(Color.RED)
                        .build()
                )
                .tempLine(
                    Line.tempLine().dir(Dir.BOTH).build()
                )
                .addLine(l2)
                .cluster(
                    Cluster.builder()
                        .tempNode(
                            Node.builder()
                                .style(NodeStyle.DASHED)
                                .fillColor(Color.GREEN)
                                .margin(5, 8)
                                .build()
                        )
                        .tempLine(Line.tempLine().dir(Dir.NONE).build())
                        .addLine(l3)
                        .subgraph(
                            Subgraph.builder()
                                .tempLine(Line.tempLine().dir(Dir.BACK).build())
                                .addLine(l4)
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);

    NodeDrawProp aProp = drawGraph.getNodeDrawProp(a);
    NodeDrawProp bProp = drawGraph.getNodeDrawProp(b);
    NodeDrawProp cProp = drawGraph.getNodeDrawProp(c);
    NodeDrawProp dProp = drawGraph.getNodeDrawProp(d);
    NodeDrawProp eProp = drawGraph.getNodeDrawProp(e);
    NodeDrawProp fProp = drawGraph.getNodeDrawProp(f);
    NodeDrawProp gProp = drawGraph.getNodeDrawProp(g);
    NodeDrawProp hProp = drawGraph.getNodeDrawProp(h);
    LineDrawProp l1Porp = drawGraph.getLineDrawProp(l1);
    LineDrawProp l2Porp = drawGraph.getLineDrawProp(l2);
    LineDrawProp l3Porp = drawGraph.getLineDrawProp(l3);
    LineDrawProp l4Porp = drawGraph.getLineDrawProp(l4);

    Assertions.assertEquals(Color.GREEN, aProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(Color.GREEN, bProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(Color.RED, cProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(Color.RED, dProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(Arrays.asList(NodeStyle.DASHED), eProp.nodeAttrs().getStyles());
    Assertions.assertEquals(Arrays.asList(NodeStyle.DASHED), fProp.nodeAttrs().getStyles());

    Assertions.assertEquals(Color.GREEN, eProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(Color.GREEN, fProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(360, eProp.nodeAttrs().getMargin().getWidth(), 0);
    Assertions.assertEquals(576, eProp.nodeAttrs().getMargin().getHeight(), 0);
    Assertions.assertEquals(360, fProp.nodeAttrs().getMargin().getWidth(), 0);
    Assertions.assertEquals(576, fProp.nodeAttrs().getMargin().getHeight(), 0);

    Assertions.assertEquals(Color.GREEN, gProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(Color.GREEN, hProp.nodeAttrs().getFillColor());
    Assertions.assertEquals(360, gProp.nodeAttrs().getMargin().getWidth(), 0);
    Assertions.assertEquals(576, hProp.nodeAttrs().getMargin().getHeight(), 0);
    Assertions.assertEquals(360, gProp.nodeAttrs().getMargin().getWidth(), 0);
    Assertions.assertEquals(576, hProp.nodeAttrs().getMargin().getHeight(), 0);

    Assertions.assertEquals(Dir.FORWARD, l1Porp.lineAttrs().getDir());
    Assertions.assertEquals(Dir.BOTH, l2Porp.lineAttrs().getDir());
    Assertions.assertEquals(Dir.NONE, l3Porp.lineAttrs().getDir());
    Assertions.assertEquals(Dir.BACK, l4Porp.lineAttrs().getDir());
  }

  @Test
  public void testParentContainer() {
    Node a = Node.builder().build();
    Node b = Node.builder().build();
    Node c = Node.builder().build();
    Node d = Node.builder().build();

    Subgraph s1 = Subgraph.builder().addNode(a).build();
    Subgraph s2 = Subgraph.builder().addNode(d).subgraph(s1).build();
    Cluster c1 = Cluster.builder().addNode(b).build();
    Cluster c2 = Cluster.builder().addNode(c).cluster(c1).build();

    Graphviz graphviz = Graphviz.digraph()
        .subgraph(s2)
        .cluster(c2)
        .build();

    TLayout tLayout = new TLayout();
    tLayout.layout(graphviz);
    DotAttachment dotAttachment = tLayout.dotAttachment;

    DNode an = dotAttachment.get(a);
    DNode bn = dotAttachment.get(b);
    DNode cn = dotAttachment.get(c);
    DNode dn = dotAttachment.get(d);

    Assertions.assertEquals(graphviz, an.getContainer());
    Assertions.assertEquals(graphviz, dn.getContainer());
    Assertions.assertEquals(c1, bn.getContainer());
    Assertions.assertEquals(c2, cn.getContainer());
  }
}
