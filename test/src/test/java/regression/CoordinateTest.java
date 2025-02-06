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
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Splines;
import org.junit.jupiter.api.Test;

public class CoordinateTest extends GraphvizVisual {

  @Test
  public void test1() {
    Node node_1 = Node.builder().label("1").build();
    Node node_2 = Node.builder().label("2").build();
    Node node_9 = Node.builder().label("9").build();
    Node node_5 = Node.builder().label("5").build();
    Node node_6 = Node.builder().label("6").build();
    Node node_7 = Node.builder().label("7").build();
    Node node_10 = Node.builder().label("10").build();
    Node node_11 = Node.builder().label("11").build();
    Node node_3 = Node.builder().label("3").build();
    Node node_4 = Node.builder().label("4").build();
    Node node_8 = Node.builder().label("8").build();
    Node node_12 = Node
        .builder()
        .label("12")
        .build();

    Graphviz graphviz = Graphviz
        .digraph()
//        .splines(Splines.POLYLINE)
        .label("sdfsdfsd")
        .tempNode(Node.builder().color(Color.BLUE).build())
        .tempLine(
            Line.builder(node_6, node_5)
                .arrowHead(ArrowShape.BOX)
                .style(LineStyle.DOTTED)
                .color(Color.ORANGE)
                .build()
        )
        .margin(1.0)
        .addNode(node_1)
        .addLine(Line.builder(node_6, node_5).build())
        .addLine(Line.builder(node_8, node_7).build())
        .addLine(Line.builder(node_7, node_5).build())
        .addLine(Line.builder(node_1, node_11).build())
        .addLine(Line.builder(node_11, node_9).build())
        .addLine(Line.builder(node_11, node_10).weight(10).build())
        .addLine(Line.builder(node_1, node_2).build())
        .addLine(Line.builder(node_2, node_3).build())
        .addLine(Line.builder(node_3, node_4).build())
        .addLine(Line.builder(node_4, node_5).build())
        .addLine(Line.builder(node_1, node_8).weight(10).build())
        .addLine(Line.builder(node_9, node_5).build())
        .addLine(Line.builder(node_10, node_5).build())
        .addLine(Line.builder(node_1, node_12).build())
        .addLine(Line.builder(node_12, node_5).build())
        .addLine(Line.builder(node_8, node_6).build())
        .addLine(Line.builder(node_3, node_6).color(Color.ofRGB("#BBDDEE")).build())
        .bgColor(Color.ofRGB("#ff0000"))
        .subgraph(
            Subgraph
                .builder()
                .addNode(Node.builder().label("222").build())
                .cluster(
                    Cluster
                        .builder()
                        .margin(1.0, 4.0)
                        .fontColor(Color.RED)
                        .addLine(node_3, node_7)
                        .build()
                )
                .rank(Rank.MAX)
                .build()
        )
        .build();

    System.out.println(node_1.nodeAttrs());

    visual(graphviz);
  }

  @Test
  public void test2() {
    Node a = Node.builder().label("A").build();
    Node b = Node.builder().label("B").build();
    Node c = Node.builder().label("C").build();
    Node d = Node.builder().label("D").build();
    Node e = Node.builder().label("E").build();
    Node f = Node.builder().label("F").build();
    Node g = Node.builder().label("G").build();
    Node h = Node.builder().label("H").build();
    Node i = Node.builder().label("I").build();
    Node j = Node.builder().label("J").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(d, a).build())
        .addLine(Line.builder(f, b).build())
        .addLine(Line.builder(g, c).build())
        .addLine(Line.builder(g, d).build())
        .addLine(Line.builder(h, e).build())
        .addLine(Line.builder(c, a).build())
        .addLine(Line.builder(i, e).build())
        .addLine(Line.builder(g, e).build())
        .addLine(Line.builder(e, b).build())
        .build()
        ;

    visual(graphviz);
  }

  @Test
  public void test3() {
    Node Thompson = Node.builder().label("Thompson").build();
    Node Bourne = Node.builder().label("Bourne").build();
    Node Mashey = Node.builder().label("Mashey").build();
    Node Formshell = Node.builder().label("Formshell").build();
    Node csh = Node.builder().label("csh").build();
    Node esh = Node.builder().label("esh").build();
    Node vsh = Node.builder().label("vsh").build();
    Node ksh = Node.builder().label("ksh").build();
    Node System_V = Node.builder().label("System-V").build();
    Node v9sh = Node.builder().label("v9sh").build();
    Node tcsh = Node.builder().label("tcsh").build();
    Node ks_i = Node.builder().label("ksh-i").build();
    Node rc = Node.builder().label("rc").build();
    Node KornShell = Node.builder().label("KornShell").build();
    Node Bash = Node.builder().label("Bash").build();
    Node ksh_POSIX = Node.builder().label("ksh-POSIX").build();
    Node POSIX = Node.builder().label("POSIX").build();
    Node Perl = Node.builder().label("Perl").build();
    Node tcl = Node.builder().label("tcl").build();

    Node n_1972 = Node.builder().label("1972").build();
    Node n_1976 = Node.builder().label("1976").build();
    Node n_1978 = Node.builder().label("1978").build();
    Node n_1980 = Node.builder().label("1980").build();
    Node n_1982 = Node.builder().label("1982").build();
    Node n_1984 = Node.builder().label("1984").build();
    Node n_1986 = Node.builder().label("1986").build();
    Node n_1988 = Node.builder().label("1988").build();
    Node n_1990 = Node.builder().label("1990").build();
    Node n_future = Node.builder().label("future").build();

    /*
      digraph shells {
        size="7,8";
        Thompson -> Mashey;
        Thompson -> Bourne;
        Thompson -> csh;
        Bourne -> Formshell;
        Bourne -> esh;
        Bourne -> vsh;
        Bourne -> v9sh;
        Bourne -> ksh;
        Bourne -> "System-V";
        csh -> ksh;
        csh -> tcsh;
        esh -> ksh;
        vsh -> ksh;
        Formshell -> ksh;
        v9sh -> rc;
        ksh -> "ks-i";
        "ks-i" -> KornShell;
        "ks-i" -> Bash;
        Bourne -> Bash;
        KornShell -> "ksh-POSIX"
        KornShell -> Bash;
        KornShell -> POSIX;
        "System-V" -> POSIX;
      }
     * */
    Graphviz graphviz = Graphviz
        .digraph()
        .scale(6)
//        .splines(Splines.POLYLINE)
        .addLine(Thompson, Mashey)
        .addLine(Thompson, Bourne)
        .addLine(Thompson, csh)
        .addLine(Bourne, Formshell)
        .addLine(Bourne, esh)
        .addLine(Bourne, vsh)
        .addLine(Bourne, v9sh)
        .addLine(Bourne, ksh)
        .addLine(Bourne, System_V)
        .addLine(csh, ksh)
        .addLine(csh, tcsh)
        .addLine(esh, ksh)
        .addLine(vsh, ksh)
        .addLine(Formshell, ksh)
        .addLine(Line.builder(v9sh, rc).weight(5).build())
        .addLine(ksh, ks_i)
        .addLine(ks_i, KornShell)
        .addLine(ks_i, Bash)
        .addLine(Bourne, Bash)
        .addLine(KornShell, Bash)
        .addLine(KornShell, ksh_POSIX)
        .addLine(KornShell, POSIX)
        .addLine(System_V, POSIX)

        .startSub()
        .tempNode(Node.builder().shape(NodeShapeEnum.PLAINTEXT).build())
        .addLine(n_1972, n_1976)
        .addLine(n_1976, n_1978)
        .addLine(n_1978, n_1980)
        .addLine(n_1980, n_1982)
        .addLine(n_1982, n_1984)
        .addLine(n_1984, n_1986)
        .addLine(n_1986, n_1988)
        .addLine(n_1988, n_1990)
        .addLine(n_1990, n_future)
        .endSub()

        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1972)
                .addNode(Thompson)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1976)
                .addNode(Mashey)
                .addNode(Bourne)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1978)
                .addNode(Formshell)
                .addNode(csh)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1980)
                .addNode(esh)
                .addNode(vsh)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1982)
                .addNode(ksh)
                .addNode(System_V)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1984)
                .addNode(v9sh)
                .addNode(tcsh)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1986)
                .addNode(ks_i)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1988)
                .addNode(KornShell)
                .addNode(Perl)
                .addNode(rc)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_1990)
                .addNode(tcl)
                .addNode(Bash)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(n_future)
                .addNode(POSIX)
                .addNode(ksh_POSIX)
                .build()
        )

        .startSub()
        .tempLine(Line.tempLine().style(LineStyle.INVIS).build())
        .addLine(n_1984, v9sh)
        .addLine(v9sh, tcsh)
        .addLine(n_1988, rc)
        .addLine(rc, KornShell)
        .addLine(Formshell, csh)
        .addLine(KornShell, Perl)
        .endSub()

        .build();

    visual(graphviz);
  }

  @Test
  public void test4() {
    Node node_1 = Node.builder().label("1").build();
    Node node_2 = Node.builder().label("2").build();
    Node node_3 = Node.builder().label("3").build();
    Node node_4 = Node.builder().label("4").build();
    Node node_5 = Node.builder().label("5").build();
    Node v_1 = Node.builder().label("__1").build();
    Node v_2 = Node.builder().label("__2").build();
    Node v_3 = Node.builder().label("__3").build();
    Node v_4 = Node.builder().label("__4").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(v_1, node_1).build())
        .addLine(Line.builder(v_1, node_2).build())
        .addLine(Line.builder(v_2, node_1).build())
        .addLine(Line.builder(v_2, node_3).build())
        .addLine(Line.builder(v_3, node_1).weight(10).build())
        .addLine(Line.builder(v_3, node_4).weight(10).build())
        .addLine(Line.builder(v_4, node_1).build())
        .addLine(Line.builder(v_4, node_5).build())
        .addLine(Line.builder(node_2, node_3).build())
        .addLine(Line.builder(node_3, node_4).build())
        .addLine(Line.builder(node_4, node_5).build())
        .build();

    visual(graphviz);
  }

  @Test
  public void test5() {
    Node node_1 = Node.builder().label("1").build();
    Node node_2 = Node.builder().label("2").build();
    Node node_3 = Node.builder().label("3").build();
    Node node_4 = Node.builder().label("4").build();
    Node node_5 = Node.builder().label("5").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(node_1, node_2).build())
        .addLine(Line.builder(node_1, node_3).build())
        .addLine(Line.builder(node_1, node_4).build())
        .addLine(Line.builder(node_1, node_5).weight(10).build())
        .build();

    visual(graphviz);
  }

  @Test
  public void test6() {
    /*
     digraph world_dynamics {
       size="6,6";
       S8 -> 9;
       S24 -> 27;
       S24 -> 25;
       S1 -> 10;
       S1 -> 2;
       S35 -> 36;
       S35 -> 43;
       S30 -> 31;
       S30 -> 33;
       9 -> 42;
       9 -> T1;
       25 -> T1;
       25 -> 26;
       27 -> T24;
       2 -> 3;
       2 -> 16;
       2 -> 17;
       2 -> T1;
       2 -> 18;
       10 -> 11;
       10 -> 14;
       10 -> T1;
       10 -> 13;
       10 -> 12;
       31 -> T1;
       31 -> 32;
       33 -> T30;
       33 -> 34;
       42 -> 4;
       26 -> 4;
       3 -> 4;
       16 -> 15;
       17 -> 19;
       18 -> 29;
       11 -> 4;
       14 -> 15;
       37 -> 39;
       37 -> 41;
       37 -> 38;
       37 -> 40;
       13 -> 19;
       12 -> 29;
       43 -> 38;
       43 -> 40;
       36 -> 19;
       32 -> 23;
       34 -> 29;
       39 -> 15;
       41 -> 29;
       38 -> 4;
       40 -> 19;
       4 -> 5;
       19 -> 21;
       19 -> 20;
       19 -> 28;
       5 -> 6;
       5 -> T35;
       5 -> 23;
       21 -> 22;
       20 -> 15;
       28 -> 29;
       6 -> 7;
       15 -> T1;
       22 -> 23;
       22 -> T35;
       29 -> T30;
       7 -> T8;
       23 -> T24;
       23 -> T1;
     }
     */
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


   GraphvizBuilder graphvizBuilder = Graphviz
        .digraph()
        .scale(5)
        .tempLine(Line.tempLine().label("label").minlen(3).build())
        .addLine(Line.builder(n_S8, n_9).build())
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
        .addLine(Line.builder(n_2, n_17).build())
        .addLine(Line.builder(n_2, n_T1).minlen(2).build())
        .addLine(Line.builder(n_2, n_18).build())
        .addLine(Line.builder(n_10, n_11).build())
        .addLine(Line.builder(n_10, n_14).build())
        .addLine(Line.builder(n_10, n_T1).build())
        .addLine(Line.builder(n_10, n_13).build())
        .addLine(Line.builder(n_10, n_12).build())
        .addLine(Line.builder(n_31, n_T1).build())
        .addLine(Line.builder(n_31, n_32).build())
        .addLine(Line.builder(n_33, n_T30).build())
        .addLine(Line.builder(n_33, n_34).build())
        .addLine(Line.builder(n_42, n_4).build())
        .addLine(Line.builder(n_26, n_4).build())
        .addLine(Line.builder(n_3, n_4).build())
        .addLine(Line.builder(n_16, n_15).build())
        .addLine(Line.builder(n_17, n_19).build())
        .addLine(Line.builder(n_18, n_29).build())
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
        .addLine(Line.builder(n_38, n_4).build())
        .addLine(Line.builder(n_40, n_19).build())
        .addLine(Line.builder(n_4, n_5).build())
        .addLine(Line.builder(n_19, n_21).build())
        .addLine(Line.builder(n_19, n_20).build())
        .addLine(Line.builder(n_19, n_28).build())
        .addLine(Line.builder(n_5, n_6).build())
        .addLine(Line.builder(n_5, n_T35).build())
        .addLine(Line.builder(n_5, n_23).build())
        .addLine(Line.builder(n_21, n_22).build())
        .addLine(Line.builder(n_20, n_15).build())
        .addLine(Line.builder(n_28, n_29).build())
        .addLine(Line.builder(n_6, n_7).build())
        .addLine(Line.builder(n_15, n_T1).build())
        .addLine(Line.builder(n_22, n_23).build())
        .addLine(Line.builder(n_22, n_T35).build())
        .addLine(Line.builder(n_29, n_T30).build())
        .addLine(Line.builder(n_7, n_T8).build())
        .addLine(Line.builder(n_23, n_T24).build())
        .addLine(Line.builder(n_23, n_T1).build());

    visual(graphvizBuilder.build());
    visual(graphvizBuilder.splines(Splines.ORTHO).showGrid(true).build());
  }

  @Test
  public void test7() {
    Node n_25 = Node.builder().label("25").build();
    Node n_26 = Node.builder().label("26").build();
    Node n_S8 = Node.builder().label("S8").build();
    Node n_27 = Node.builder().label("27").build();
    Node n_S24 = Node.builder().label("S24").build();
    Node n_T24 = Node.builder().label("T24").build();
    Node n_T1 = Node.builder().label("T1").build();
    Node n_31 = Node.builder().label("31").build();
    Node n_10 = Node.builder().label("10").build();
    Node n_33 = Node.builder().label("33").build();
    Node n_11 = Node.builder().label("11").build();
    Node n_36 = Node.builder().label("36").build();
    Node n_16 = Node.builder().label("16").build();
    Node n_17 = Node.builder().label("17").build();
    Node n_18 = Node.builder().label("18").build();
    Node n_S30 = Node.builder().label("S30").build();
    Node n_2 = Node.builder().label("2").build();
    Node n_3 = Node.builder().label("3").build();
    Node n_S35 = Node.builder().label("S35").build();
    Node n_9 = Node.builder().label("9").build();
    Node n_S1 = Node.builder().label("S1").build();
    Node n_42 = Node.builder().label("42").build();
    Node n_43 = Node.builder().label("43").build();


    Graphviz graphviz = Graphviz
        .digraph()
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
        .addLine(Line.builder(n_2, n_17).build())
        .addLine(Line.builder(n_2, n_T1).build())
        .addLine(Line.builder(n_2, n_18).build())
        .addLine(Line.builder(n_10, n_11).build())
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .tempLine(Line.tempLine().style(LineStyle.INVIS).build())
                .addLine(n_S35, n_S8)
                .build()
        )
        .build();

    visual(graphviz);

  }

  @Test
  public void test8() {
    Node n_T24 = Node.builder().label("T24").build();
    Node n_10 = Node.builder().label("10").build();
    Node n_11 = Node.builder().label("11").build();
    Node n_16 = Node.builder().label("16").build();
    Node n_17 = Node.builder().label("17").build();
    Node n_18 = Node.builder().label("18").build();
    Node n_2 = Node.builder().label("2").build();
    Node n_3 = Node.builder().label("3").build();
    Node n__6 = Node.builder().label("_6").build();
    Node n__7 = Node.builder().label("_7").build();
    Node n_9 = Node.builder().label("9").build();
    Node n__8 = Node.builder().label("_8").build();
    Node n__9 = Node.builder().label("_9").build();
    Node n_S1 = Node.builder().label("S1").build();
    Node n__11 = Node.builder().label("_11").build();
    Node n__10 = Node.builder().label("_10").build();
    Node n__13 = Node.builder().label("_13").build();
    Node n__12 = Node.builder().label("_12").build();
    Node n__15 = Node.builder().label("_15").build();
    Node n_25 = Node.builder().label("25").build();
    Node n__14 = Node.builder().label("_14").build();
    Node n_26 = Node.builder().label("26").build();
    Node n_S8 = Node.builder().label("S8").build();
    Node n_27 = Node.builder().label("27").build();
    Node n__16 = Node.builder().label("_16").build();
    Node n___1 = Node.builder().label("__1").build();
    Node n___3 = Node.builder().label("__3").build();
    Node n___2 = Node.builder().label("__2").build();
    Node n_S24 = Node.builder().label("S24").build();
    Node n___5 = Node.builder().label("__5").build();
    Node n___4 = Node.builder().label("__4").build();
    Node n_T1 = Node.builder().label("T1").build();
    Node n_31 = Node.builder().label("31").build();
    Node n_33 = Node.builder().label("33").build();
    Node n_36 = Node.builder().label("36").build();
    Node n_S30 = Node.builder().label("S30").build();
    Node n_S35 = Node.builder().label("S35").build();
    Node n_42 = Node.builder().label("42").build();
    Node n_43 = Node.builder().label("43").build();


    Graphviz graphviz = Graphviz
        .digraph()
        .scale(5)
        .addLine(Line.builder(n_S35, n_S30).build())
        .addLine(Line.builder(n_S30, n_S1).build())
        .addLine(Line.builder(n_S1, n_S24).build())
        .addLine(Line.builder(n_S24, n_S8).build())
        .addLine(Line.builder(n___1, n_S35).build())
        .addLine(Line.builder(n___1, n_36).build())
        .addLine(Line.builder(n___1, n_S35).build())
        .addLine(Line.builder(n___1, n_43).build())
        .addLine(Line.builder(n___2, n_S30).build())
        .addLine(Line.builder(n___2, n_31).build())
        .addLine(Line.builder(n___2, n_S30).build())
        .addLine(Line.builder(n___2, n_33).build())
        .addLine(Line.builder(n___3, n_S1).build())
        .addLine(Line.builder(n___3, n_10).build())
        .addLine(Line.builder(n___3, n_S1).build())
        .addLine(Line.builder(n___3, n_2).build())
        .addLine(Line.builder(n___4, n_S24).build())
        .addLine(Line.builder(n___4, n_27).build())
        .addLine(Line.builder(n___4, n_S24).build())
        .addLine(Line.builder(n___4, n_25).build())
        .addLine(Line.builder(n___5, n_S8).build())
        .addLine(Line.builder(n___5, n_9).build())
        .addLine(Line.builder(n_36, n_43).build())
        .addLine(Line.builder(n_43, n_31).build())
        .addLine(Line.builder(n_31, n_33).build())
        .addLine(Line.builder(n_33, n_10).build())
        .addLine(Line.builder(n_10, n_2).build())
        .addLine(Line.builder(n_2, n_27).build())
        .addLine(Line.builder(n_27, n_25).build())
        .addLine(Line.builder(n_25, n_9).build())
        .addLine(Line.builder(n__6, n_10).build())
        .addLine(Line.builder(n__6, n_11).build())
        .addLine(Line.builder(n__7, n_2).build())
        .addLine(Line.builder(n__7, n_18).build())
        .addLine(Line.builder(n__8, n_2).build())
        .addLine(Line.builder(n__8, n_16).build())
        .addLine(Line.builder(n__9, n_2).build())
        .addLine(Line.builder(n__9, n_17).build())
        .addLine(Line.builder(n__10, n_2).build())
        .addLine(Line.builder(n__10, n_3).build())
        .addLine(Line.builder(n__11, n_2).build())
        .addLine(Line.builder(n__11, n_T1).build())
        .addLine(Line.builder(n__12, n_27).build())
        .addLine(Line.builder(n__12, n_T24).build())
        .addLine(Line.builder(n__13, n_25).build())
        .addLine(Line.builder(n__13, n_T1).build())
        .addLine(Line.builder(n__14, n_25).build())
        .addLine(Line.builder(n__14, n_26).build())
        .addLine(Line.builder(n__15, n_9).weight(10).build())
        .addLine(Line.builder(n__15, n_T1).weight(10).build())
        .addLine(Line.builder(n__16, n_9).build())
        .addLine(Line.builder(n__16, n_42).build())
        .addLine(Line.builder(n_11, n_18).build())
        .addLine(Line.builder(n_18, n_16).build())
        .addLine(Line.builder(n_16, n_17).build())
        .addLine(Line.builder(n_17, n_3).build())
        .addLine(Line.builder(n_3, n_T24).build())
        .addLine(Line.builder(n_T24, n_T1).build())
        .addLine(Line.builder(n_T1, n_26).build())
        .addLine(Line.builder(n_26, n_42).build())
        .build();

    visual(graphviz);
  }
}
