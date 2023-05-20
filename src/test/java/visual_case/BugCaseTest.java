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

import static org.graphper.api.Html.table;
import static org.graphper.api.Html.td;

import helper.DocumentUtils;
import helper.GraphvizVisual;
import helper.SerialHelper;
import java.io.File;
import java.io.IOException;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.api.attributes.Tend;
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
                     .color(Color.ofRGB("#000044"))
                     .label("11111111111111111110")
                     .build())
        .addLine(Line.builder(sh0016, sh0008)
                     .color(Color.ofRGB("#000048"))
                     .label("11111111111111111110")
                     .build())
        .addLine(Line.builder(sh0016, sh0020)
                     .color(Color.ofRGB("#00004C"))
                     .label("22222222222222222222")
                     .build())
        .addLine(Line.builder(sh0016, sh0033)
                     .color(Color.ofRGB("#000054"))
                     .label("3333333333333333333")
                     .build())
        .addLine(Line.builder(sh0008, sh0013)
                     .color(Color.ofRGB("#000058"))
                     .label("44444444444444444444")
                     .build())
        .addLine(Line.builder(sh0020, sh0008)
                     .color(Color.ofRGB("#000060"))
                     .label("555555555555555555555555")
                     .build())
        .addLine(Line.builder(sh0007, sh0016)
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
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(a, b)
        .addLine(g, b)
        .addLine(b, c)
        .addLine(b, d)
        .addLine(c, e)
        .addLine(d, e, f)
        .build();

    visual(graphviz);
  }

  @Test
  public void testAlignNodePos2() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(c, d)
        .addLine(b, g)

        .startClus()
        .addLine(f, e)
        .addLine(d, e)
        .addLine(e, g)
        .startSub()
        .rank(Rank.MIN)
        .addNode(g)
        .endSub()
        .endClus()

        .startSub()
        .rank(Rank.SAME)
        .addLine(a, b)
        .addLine(a, c)
        .endSub()

        .build();

    visual(graphviz);
  }

  @Test
  public void testTailHeadLabel() {
    Node a = Node.builder().shape(NodeShapeEnum.RECORD)
        .label("|center|").build();
    Node b = Node.builder().label("left").build();
    Node c = Node.builder().label("right").build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.ORTHO)
        .nodeSep(3)
        .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
        .addLine(Line.builder(a, b)
                     .floatLabels(
                         FloatLabel.builder()
                             .tend(Tend.TAIL)
                             .lengthRatio(0)
                             .table(table().tr(td().height(20).width(40).bgColor(Color.BLACK)))
                             .build(),
                         FloatLabel.builder()
                             .tend(Tend.HEAD)
                             .lengthRatio(1)
                             .table(table().tr(td().height(20).width(20).bgColor(Color.BLACK)))
                             .build()
                     )
                     .build())
        .addLine(Line.builder(a, c)
                     .floatLabels(
                         FloatLabel.builder()
                             .tend(Tend.TAIL)
                             .lengthRatio(0)
                             .table(table().tr(td().height(20).width(20).bgColor(Color.BLACK)))
                             .build(),
                         FloatLabel.builder()
                             .tend(Tend.HEAD)
                             .lengthRatio(1)
                             .table(table().tr(td().height(20).width(20).bgColor(Color.BLACK)))
                             .build()
                     )
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void testWrongCluster() {
    Node sh0010 = Node.builder().label("sh0010")
        .width(1.380631)
        .height(0.459156)
        .build();
    Node sh0011 = Node.builder().label("sh0011")
        .width(0.849366)
        .height(0.458888)
        .build();
    Node sh0012 = Node.builder().label("sh0012")
        .shape(NodeShapeEnum.RECT)
        .width(0.805556)
        .height(1.098891)
        .build();
    Node sh0013 = Node.builder().label("sh0013")
        .shape(NodeShapeEnum.RECT)
        .width(0.416667)
        .height(1.098891)
        .build();
    Node zacl0004 = Node.builder().shape(NodeShapeEnum.POINT).width(.01).height(.01).margin(0).build();

    Graphviz graphviz = Graphviz.digraph()
        .nodeSep(0.486111)
        .rankSep(0.833333)
        .rankdir(Rankdir.LR)
        .addLine(Line.builder(zacl0004, sh0010).minlen(0)
                     .table(table().fixedSize(true).width(143).height(19).tr(td()))
                     .build())
        .addLine(
            Line.builder(sh0011, zacl0004).arrowTail(ArrowShape.NONE).arrowHead(ArrowShape.NONE)
                .table(table().fixedSize(true).width(248).height(19).tr(td())).minlen(0)
                .build())
        .addLine(
            Line.builder(sh0011, zacl0004).arrowTail(ArrowShape.NONE).arrowHead(ArrowShape.NONE)
                .table(table().fixedSize(true).width(48).height(19).tr(td())).minlen(0)
                .build())
        .addLine(
            Line.builder(sh0011, zacl0004).arrowTail(ArrowShape.NONE).arrowHead(ArrowShape.NONE)
                .table(table().fixedSize(true).width(248).height(19).tr(td())).minlen(0)
                .build())
        .addNode(sh0012, sh0013)
        .cluster(
            Cluster.builder()
                .id("6pa")
                .addNode(zacl0004)
                .cluster(
                    Cluster.builder()
                        .id("6p0")
                        .cluster(
                            Cluster.builder()
                                .id("6")
                                .table(
                                    table().fixedSize(true).width(65).height(14).tr(td())
                                )
                                .cluster(
                                    Cluster.builder()
                                        .id("6i")
                                        .cluster(
                                            Cluster.builder()
                                                .id("6p1")
                                                .addNode(sh0010, sh0011)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .addLine(Line.builder(sh0012, zacl0004).arrowTail(ArrowShape.NONE)
                     .arrowHead(ArrowShape.NONE).build())
        .addLine(Line.builder(zacl0004, sh0013).arrowTail(ArrowShape.NONE)
                     .arrowHead(ArrowShape.NONE).build())
        .build();


    visual(graphviz);
  }

  @Test
  public void testWrongClusterFromSerial() throws IOException, ClassNotFoundException {
    SerialHelper.readObj(o -> {
      visual((Graphviz) o);
    }, new File(DocumentUtils.getTestSerialPath() + "graph1"));
  }
}
