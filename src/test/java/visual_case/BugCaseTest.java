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
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.NodeShapeEnum;
import org.junit.jupiter.api.Test;

public class BugCaseTest extends GraphvizVisual {

  @Test
  public void wrongFlatEdgeLabelIn_1_0_1() {
    Node sh0045 = Node.builder().label("sh0045").build();
    Node sh0020 = Node.builder().label("sh0020").build();
    Node sh0006 = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("sh0006")
        .width(1.25)
        .height(0.661241)
        .color(Color.ofRGB("#000006"))
        .build();
    Node sh0007 = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("sh0007")
        .width(1.25)
        .height(0.661241)
        .color(Color.ofRGB("#000007"))
        .build();
    Node sh0008 = Node.builder()
        .shape(NodeShapeEnum.RECT)
        .label("sh0008")
        .width(1.25)
        .height(0.661241)
        .color(Color.ofRGB("#000008"))
        .build();
    Node sh0042 = Node.builder().label("sh0042").build();
    Node sh0016 = Node.builder().label("sh0016").build();
    Node sh0013 = Node.builder().label("sh0013").build();
    Node sh0033 = Node.builder().label("sh0033").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(Line.builder(sh0045, sh0020)
                     .arrowTail(ArrowShape.NONE)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#0000CC"))
                     .build())
        .startClus()
        .startClus()
        .color(Color.ofRGB("#000004"))
        .label("sssssssssssssssssssssssssssssss")
        .startClus()
        .addNode(sh0006, sh0007, sh0008)
        .endClus()
        .endClus()
        .endClus()

        .tempLine(Line.tempLine().arrowTail(ArrowShape.NONE).minlen(1).build())
        .addLine(Line.builder(sh0042, sh0020)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#000044"))
                     .label("11111111111111111110")
                     .build())
        .addLine(Line.builder(sh0016, sh0008)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#000048"))
                     .label("11111111111111111110")
                     .build())
        .addLine(Line.builder(sh0016, sh0020)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#00004C"))
                     .label("22222222222222222222")
                     .build())
        .addLine(Line.builder(sh0016, sh0033)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#000054"))
                     .label("3333333333333333333")
                     .build())
        .addLine(Line.builder(sh0008, sh0013)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#000058"))
                     .label("44444444444444444444")
                     .build())
        .addLine(Line.builder(sh0020, sh0008)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#000060"))
                     .label("555555555555555555555555")
                     .build())
        .addLine(Line.builder(sh0007, sh0016)
                     .arrowHead(ArrowShape.NONE)
                     .color(Color.ofRGB("#000044"))
                     .label("66666666666666666666666")
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void testAlignNodePos() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(a, b)
        .addLine(b, c)
        .addLine(b, d)
        .addLine(c, e)
        .addLine(d, e)
        .build();

    visual(graphviz);
  }
}
