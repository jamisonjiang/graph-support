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
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ClusterShapeEnum;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClusterTest extends GraphvizVisual {

  @BeforeEach
  public void init() {
//    System.setProperty("dot.coordinate.v1", "true");
  }

  @Test
  public void case1() {
    Node n1 = Node.builder().label("1").build();
    Node n2 = Node.builder().label("2").build();
    Node n3 = Node.builder().label("33333333333333333333333333333333333333333\n"
                                       + "333333333333333333333333333333\n3333333333333333333333333").build();
    Node n4 = Node.builder().label("4").build();
    Node n5 = Node.builder().label("5").build();
    Node n6 = Node.builder().label("6").build();
    Node n7 = Node.builder().label("7").build();
    Node n8 = Node.builder().label("8").build();
    Node n9 = Node.builder().label("9").build();
    Node n10 = Node.builder().label("10").build();
    Node n11 = Node.builder().label("11").build();
    Node n12 = Node.builder().label("12").build();
    Node n13 = Node.builder().label("13").build();

    Graphviz graphviz = Graphviz.digraph()
        .scale(6)
        .tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build())
        .rankdir(Rankdir.TB)
        .compound(true)
        .addLine(n1, n4)
        .addLine(Line.builder(n1, n2)
//                     .label("1 --> 2")
                     .build())
        .label("GRAPHVIZ DEMO")
        .addLine(n1, n2)
        .addLine(n1, n3)
        .addLine(n4, n6)
        .addLine(n2, n5)
        .addLine(n2, n8)
        .addLine(n3, n6)
        .addLine(n3, n8)
        .addLine(Line.builder(n5, n7).ltail("cluster_2").lhead("cluster_3").build())
        .addLine(n5, n7)
        .addLine(Line.builder(n6, n7).build())
        .addLine(n8, n7)
        .addLine(n2, n9)
        .addLine(n2, n10)
        .addLine(Line.builder(n12, n6).build())
        .addLine(n12, n11)
        .addLine(n7, n13)
        .addLine(n3, n9)
        .cluster(
            Cluster.builder()
                .id("cluster_1")
                .labeljust(Labeljust.LEFT)
                .labelloc(Labelloc.BOTTOM)
                .label("cluster_1 Nice to meet you!\nHow old are you?\nI'm fine thank you, and you?\nMe too!\nWhere are you from?")
                .fontSize(24)
                .addNode(n2)
                .addNode(n3)
                .addNode(n4)
                .addNode(n5)
                .addNode(n6)
                .addLine(n6, n11)
                .cluster(
                    Cluster.builder()
                        .id("cluster_2")
                        .labelloc(Labelloc.BOTTOM)
                        .label("cluster_2 1111111111111111111111111\n22222222222222222\n3333333333333\n4444444444444444\n555555\n66666\n77777\n888888\n9999999")
                        .fontSize(36)
                        .addNode(n3)
                        .addNode(n5)
                        .addNode(n6)

                        .startSub()
                        .rank(Rank.SAME)
                        .addLine(Line.builder(n3, n5).label("3 --> 5").build())
                        .endSub()

                        .build()
                )
                .build()
        )
        .cluster(
            Cluster.builder()
                .id("cluster_3")
                .labeljust(Labeljust.RIGHT)
                .label("cluster_3 Hello World|Hello World|Hello World\nHello World\nHello World\nHello World\nHello World\nHello World\nHello World")
                .addNode(n7)
                .addNode(n8)
                .addNode(n9)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void case2() {
    Node n_10 = Node.builder().label("10").build();
    Node n_11 = Node.builder().label("11").build();
    Node n_12 = Node.builder().label("12").build();
    Node n_13 = Node.builder().label("13").build();
    Node n_14 = Node.builder().label("14").build();
    Node n_15 = Node.builder().label("15").build();
    Node n_16 = Node.builder().label("16").build();
    Node n_17 = Node.builder().label("17").build();
    Node n_18 = Node.builder().label("18").build();
    Node n_19 = Node.builder().label("19").build();
    Node n_2 = Node.builder().label("2").build();
    Node n_3 = Node.builder().label("3").build();
    Node n_4 = Node.builder().label("4").build();
    Node n_5 = Node.builder().label("5").build();
    Node n_6 = Node.builder().label("6").build();
    Node n_7 = Node.builder().label("7").build();
    Node n_9 = Node.builder().label("9").build();
    Node n_S1 = Node.builder().label("S1").build();
    Node n_20 = Node.builder().label("20").build();
    Node n_21 = Node.builder().label("21").build();
    Node n_22 = Node.builder().label("22").build();
    Node n_23 = Node.builder().label("23").build();
    Node n_25 = Node.builder().label("25").build();
    Node n_26 = Node.builder().label("26").build();
    Node n_S8 = Node.builder().label("S8").build();
    Node n_27 = Node.builder().label("27").build();
    Node n_28 = Node.builder().label("28").build();
    Node n_29 = Node.builder().label("29").build();
    Node n_T1 = Node.builder().label("T1").build();
    Node n_31 = Node.builder().label("31").build();
    Node n_32 = Node.builder().label("32").build();
    Node n_33 = Node.builder().label("33").build();
    Node n_34 = Node.builder().label("34").build();
    Node n_36 = Node.builder().label("36").build();
    Node n_37 = Node.builder().label("37").build();
    Node n_T8 = Node.builder().label("T8").build();
    Node n_38 = Node.builder().label("38").build();
    Node n_39 = Node.builder().label("39").build();
    Node n_40 = Node.builder().label("40").build();
    Node n_41 = Node.builder().label("41").build();
    Node n_42 = Node.builder().label("42").build();
    Node n_43 = Node.builder().label("43").build();
    Node n_T24 = Node.builder().label("T24").build();
    Node n_T30 = Node.builder().label("T30").build();
    Node n_T35 = Node.builder().label("T35").build();
    Node n_S24 = Node.builder().label("S24").build();
    Node n_S30 = Node.builder().label("S30").build();
    Node n_S35 = Node.builder().label("S35").build();


    Graphviz graphviz = Graphviz
        .digraph()
        .scale(4)
        .addNode(
            Node.builder()
                .label("ds")
                .fontSize(36)
                .build()
        )
        .addLine(Line.builder(n_S8, n_9).build())
        .addLine(Line.builder(n_S24, n_27).build())
        .addLine(Line.builder(n_S24, n_25).build())
        .addLine(Line.builder(n_S1, n_10).build())
        .addLine(Line.builder(n_S1, n_2).build())
        .addLine(Line.builder(n_S35, n_36).build())
        .addLine(Line.builder(n_S35, n_43).build())
        .addLine(Line.builder(n_S30, n_31).build())
        .addLine(Line.builder(n_S30, n_33).build())
        .addLine(Line.builder(n_9, n_42).build())
        .addLine(Line.builder(n_9, n_T1).build())
        .addLine(Line.builder(n_25, n_T1).build())
        .addLine(Line.builder(n_25, n_26).build())
        .addLine(Line.builder(n_27, n_T24).build())
        .addLine(Line.builder(n_2, n_3).build())
        .addLine(Line.builder(n_2, n_16).build())

        .addLine(Line.builder(n_2, n_18).build())
        .addLine(Line.builder(n_10, n_11).build())
        .addLine(Line.builder(n_10, n_14).build())
        .addLine(Line.builder(n_10, n_T1).build())

        .addLine(Line.builder(n_31, n_32).build())
        .addLine(Line.builder(n_33, n_T30).build())
        .addLine(Line.builder(n_33, n_34).build())
        .addLine(Line.builder(n_42, n_4).build())
        .addLine(Line.builder(n_26, n_4).build())
        .addLine(Line.builder(n_3, n_4).build())

        .addLine(Line.builder(n_38, n_4).build())
        .addLine(Line.builder(n_40, n_19).build())
        .addLine(Line.builder(n_4, n_5).build())
        .addLine(Line.builder(n_19, n_21).build())

        .addLine(Line.builder(n_6, n_7).build())
        .addLine(Line.builder(n_15, n_T1).build())
        .addLine(Line.builder(n_22, n_23).build())
        .addLine(Line.builder(n_22, n_T35).build())

        .addLine(Line.builder(n_23, n_T1).build())

        .cluster(
            Cluster.builder()
                .label("HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                           + "HELLO WORLD HELLO WORLD HELLO WORLD HELLO WORLD\n"
                )
                .addLine(Line.builder(n_11, n_4).build())
                .addLine(Line.builder(n_14, n_15).build())
                .addLine(Line.builder(n_37, n_39).build())
                .addLine(Line.builder(n_37, n_41).build())
                .addLine(Line.builder(n_37, n_38).build())
                .addLine(Line.builder(n_37, n_40).build())
                .addLine(Line.builder(n_13, n_19).build())
                .addLine(Line.builder(n_12, n_29).build())
                .addLine(Line.builder(n_43, n_38).build())
                .addLine(Line.builder(n_43, n_40).build())
                .addLine(Line.builder(n_36, n_19).build())
                .addLine(Line.builder(n_32, n_23).build())
                .addLine(Line.builder(n_34, n_29).build())
                .addLine(Line.builder(n_39, n_15).build())
                .addLine(Line.builder(n_41, n_29).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_19, n_20).build())
                .addLine(Line.builder(n_19, n_28).build())
                .addLine(Line.builder(n_5, n_6).build())
                .addLine(Line.builder(n_5, n_T35).build())
                .addLine(Line.builder(n_5, n_23).build())
                .addLine(Line.builder(n_21, n_22).build())
                .addLine(Line.builder(n_20, n_15).build())
                .addLine(Line.builder(n_28, n_29).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_2, n_17).build())
                .addLine(Line.builder(n_2, n_T1).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_10, n_13).build())
                .addLine(Line.builder(n_10, n_12).build())
                .addLine(Line.builder(n_31, n_T1).build())
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(Line.builder(n_16, n_15).build())
                .addLine(Line.builder(n_17, n_19).build())
                .addLine(Line.builder(n_18, n_29).build())
                .addLine(Line.builder(n_29, n_T30).build())
                .addLine(Line.builder(n_7, n_T8).build())
                .addLine(Line.builder(n_23, n_T24).build())
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void case3() {
    Node a = Node.builder().label("api").build();
    Node b = Node.builder().label("internal").build();
    Node d = Node.builder().style(NodeStyle.INVIS).fixedSize(true).height(0).build();
    Node e = Node.builder().label("impl").build();

    Graphviz graphviz = Graphviz.digraph()
        .compound(true)
        .rankdir(Rankdir.LR)
        .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
        .addLine(Line.builder(a, d).dir(Dir.BACK).label("requires").ltail("A").lhead("B").build())
        .addLine(Line.builder(b, e).dir(Dir.BACK).label("opens").lhead("B").build())
        .addLine(Line.builder(b, e).label("exports").ltail("A").build())

        .startClus()
        .id("A")
        .label("ModuleA")
        .startClus()
        .label("coo.foo")
        .addNode(a, b)
        .endClus()
        .endClus()

        .startClus()
        .id("B")
        .label("ModuleB")
        .startClus()
        .label("com.bar")
        .addLine(Line.builder(d, e).minlen(0).style(LineStyle.INVIS).build())
        .endClus()
        .endClus()

        .build();

    visual(graphviz);
  }

  @Test
  public void testClusterShape_1() {
    Node a = Node.builder().shape(NodeShapeEnum.RECT).style(NodeStyle.ROUNDED).label("a").build();
    Node b = Node.builder().label("b").color(Color.ORANGE).shape(NodeShapeEnum.PARALLELOGRAM).build();
    Node c = Node.builder().label("c").fillColor(Color.BLUE).fontColor(Color.WHITE).shape(NodeShapeEnum.TRAPEZIUM).build();
    Node d = Node.builder().label("d").shape(NodeShapeEnum.TRAPEZIUM).build();
    Node e = Node.builder().label("e").shape(NodeShapeEnum.STAR).build();
    Node h = Node.builder().label("h").shape(NodeShapeEnum.DIAMOND).build();
    Node f = Node.builder().label("f").sides(5).build();
    Node g = Node.builder().label("g").sides(6).penWidth(5).build();

    Graphviz graphviz = Graphviz.digraph()
        .label("Graph label")
        .scale(8)
        .addNode(a)
        .startClus()
        .id("1")
        .style(ClusterStyle.ROUNDED, ClusterStyle.DOTTED, ClusterStyle.BOLD)
        .shape(ClusterShapeEnum.SEPTAGON)
        .penWidth(2.1)
        .bgColor(Color.PINK)
        .addNode(d)
        .startClus()
        .id("2")
        .tempNode(Node.builder().shape(NodeShapeEnum.REGULAR_POLYLINE)
                      .style(NodeStyle.ROUNDED, NodeStyle.DASHED, NodeStyle.BOLD).build())
        .shape(ClusterShapeEnum.RECT)
        .style(ClusterStyle.ROUNDED)
        .color(Color.RED)
        .bgColor(Color.GREEN)
        .addLine(Line.builder(b, c).style(LineStyle.DASHED, LineStyle.BOLD).build())
        .addLine(Line.builder(c, e).style(LineStyle.DASHED, LineStyle.BOLD).penWidth(6).build())
        .addLine(e, f, g, h)
        .endClus()
        .endClus()
        .build();

    visual(graphviz);
  }
}
