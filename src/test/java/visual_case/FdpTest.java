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

package visual_case;

import helper.GraphvizVisual;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.junit.jupiter.api.Test;

public class FdpTest extends GraphvizVisual {

  @Test
  public void case1() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz.digraph()
        .layout(Layout.FDP)
//        .tempNode(Node.builder().shape(NodeShapeEnum.POINT).build())
        .addLine(a, b)
        .addLine(b, c)
        .addLine(c, d)
        .addLine(d, e)
        .addLine(e, f)

//        .addLine(g, b)
//        .addLine(g, c)
//        .addLine(g, d)
//        .addLine(g, e)
//        .addLine(g, f)

//        .addLine(a, b)
//        .addLine(a, c)
//        .addLine(a, d)
//        .addLine(a, e)
//        .addLine(b, c)
//        .addLine(b, d)
//        .addLine(b, e)
//        .addLine(c, d)
//        .addLine(c, e)
//        .addLine(d, e)
        .build();

    visual(graphviz);
  }
}