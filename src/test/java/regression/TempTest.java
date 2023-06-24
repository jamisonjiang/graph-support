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

package regression;

import helper.GraphvizVisual;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Rank;
import org.junit.jupiter.api.Test;

public class TempTest extends GraphvizVisual {

  @Test
  public void testTempTransitivity()  {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b\nb\nb\nb\nb\nb\nb\nb\nb\nb\nb\nb").build();
    Node c = Node.builder().label("c").labelloc(Labelloc.TOP).build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node h = Node.builder().label("h").labelloc(Labelloc.BOTTOM).build();
    Node i = Node.builder().label("i").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .scale(0.7)
        .tempNode(Node.builder().fillColor(Color.GREEN).build())
        .label("This is a demo of Cluster style delivery\nThis is a demo of Cluster style delivery\nThis is a demo of Cluster style delivery\n"
                   + "This is a demo of Cluster style delivery\nThis is a demo of Cluster style delivery")
        .labelloc(Labelloc.TOP)
        .labeljust(Labeljust.LEFT)
        .addLine(a, b)
        .addLine(a, d)
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
                .addLine(e, f)
                .cluster(
                    Cluster.builder()
                        .tempNode(
                            Node.builder()
                                .style(NodeStyle.DASHED)
                                .fillColor(Color.GREEN)
                                .margin(2, 4)
                                .build()
                        )
                        .tempLine(Line.tempLine().dir(Dir.NONE).build())
                        .label("Cluster 1\nHow to pass Cluster style")
                        .labelloc(Labelloc.BOTTOM)
                        .fontSize(36)
                        .addLine(b, c)
                        .addLine(b, c)
                        .addLine(b, c)
                        .subgraph(
                            Subgraph.builder()
                                .tempLine(Line.tempLine().dir(Dir.BACK).build())
                                .rank(Rank.SOURCE)
                                .addLine(c, h)
                                .addLine(h, i)
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

//    graphviz = Graphviz.digraph()
//        .addLine(a, b)
//        .addLine(b, c)
//        .addLine(b, c)
//        .addLine(b, c)
//        .addLine(c, h)
//        .addLine(h, i)
//        .addLine(a, d)
//        .addLine(e, f)
//        .build();

    visual(graphviz);
  }
}
