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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;

public class GraphAttrTest extends GraphvizVisual {

  static Stream<GraphvizBuilder> basicBuilder() {
    Node n = Node.builder().style(NodeStyle.INVIS).build();
    return Stream.of(Graphviz.graph().addNode(n), Graphviz.digraph().addNode(n));
  }

  static Stream<GraphvizBuilder> labelCases() {
    Stream<GraphvizBuilder> stream = basicBuilder();
    return stream
        .peek(g -> {
          Node a = Node.builder().label("aaaaaaaaaaa").build();
          Node b = Node.builder().label("b").build();
          g.addLine(a, b);
          g.label("label_test");
        })
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().scale(1, 2).fontSize(10).labeljust(Labeljust.CENTER),
                    g.clone().scale(2, 1).fontSize(50).labeljust(Labeljust.LEFT),
                    g.clone().labeljust(Labeljust.RIGHT)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        )
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().labelloc(Labelloc.CENTER),
                    g.clone().labelloc(Labelloc.TOP),
                    g.clone().labelloc(Labelloc.BOTTOM)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> splinesCases() {
    return Stream.of(Graphviz.digraph().label("splines_test"))
        .peek(GraphAttrTest::splinesCase)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().splines(Splines.NONE),
                    g.clone().splines(Splines.ROUNDED),
                    g.clone().splines(Splines.SPLINE),
                    g.clone().splines(Splines.POLYLINE),
                    g.clone().splines(Splines.LINE),
                    g.clone().splines(Splines.ORTHO)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static void splinesCase(GraphvizBuilder graphvizBuilder) {
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

    graphvizBuilder.scale(0.7)
        .addLine(Line.builder(n_S8, n_9).build())
        .addLine(Line.builder(n_S24, n_27).build())
        .addLine(Line.builder(n_S24, n_25).build())
        .addLine(Line.builder(n_S1, n_10).build())
        .addLine(Line.builder(n_S1, n_2).build())
        .addLine(Line.builder(n_S35, n_36).build())
        .addLine(Line.builder(n_S35, n_43).arrowSize(1).build())
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
        .addLine(Line.builder(n_10, n_14).label("10 -> 14").build())
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
        .addLine(Line.builder(n_23, n_T1).build())
        .build();
  }

  static Stream<GraphvizBuilder> rankdirCases() {
    return Stream.of(Graphviz.digraph().label("rankdir_test"))
        .peek(GraphAttrTest::rankdirCase)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().rankdir(Rankdir.TB),
                    g.clone().rankdir(Rankdir.LR),
                    g.clone().rankdir(Rankdir.RL),
                    g.clone().rankdir(Rankdir.BT)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static void rankdirCase(GraphvizBuilder graphvizBuilder) {
    Node n_ksh = Node.builder().label("ksh").style(NodeStyle.DASHED).build();
    Node n_System_V = Node.builder().label("System_V").color(Color.RED).build();
    Node n_vsh = Node.builder().label("vsh").fontSize(36).build();
    Node n_Mashey = Node.builder().label("Mashey").fillColor(Color.GREEN).build();
    Node n_ksh_POSIX = Node.builder().label("ksh_POSIX").build();
    Node n_Thompson = Node.builder().label("Thompson").build();
    Node n_ks_i = Node.builder().label("ks_i").style(NodeStyle.BOLD).build();
    Node n_Formshell = Node.builder().label("Formshell").build();
    Node n_Bash = Node.builder().label("Bash").build();
    Node n_v9sh = Node.builder().label("v9sh").build();
    Node n_POSIX = Node.builder().label("POSIX").build();
    Node n_rc = Node.builder().label("rc").build();
    Node n_tcsh = Node.builder().label("tcsh").build();
    Node n_Bourne = Node.builder().label("Bourne").style(NodeStyle.DASHED).fillColor(Color.GOLD)
        .build();
    Node n_csh = Node.builder().label("csh").build();
    Node n_esh = Node.builder().label("esh").build();
    Node n_KornShell = Node.builder().label("KornShell").build();

    graphvizBuilder
        .scale(0.75)
        .addLine(n_Thompson, n_Mashey)
        .addLine(n_Thompson, n_Bourne)
        .addLine(n_Thompson, n_csh)
        .addLine(n_Bourne, n_Formshell)
        .addLine(n_Bourne, n_esh)
        .addLine(n_Bourne, n_vsh)
        .addLine(n_Bourne, n_v9sh)
        .addLine(Line.builder(n_Bourne, n_ksh).label("Bourne --> ksh").build())
        .addLine(n_Bourne, n_System_V)
        .addLine(n_csh, n_ksh)
        .addLine(n_csh, n_tcsh)
        .addLine(n_esh, n_ksh)
        .addLine(n_vsh, n_ksh)
        .addLine(n_Formshell, n_ksh)
        .addLine(n_v9sh, n_rc)
        .addLine(n_ksh, n_ks_i)
        .addLine(n_ks_i, n_KornShell)
        .addLine(n_ks_i, n_Bash)
        .addLine(n_Bourne, n_Bash)
        .addLine(n_KornShell, n_ksh_POSIX)
        .addLine(n_KornShell, n_Bash)
        .addLine(n_KornShell, n_POSIX)
        .addLine(n_System_V, n_POSIX);
  }

  static Stream<GraphvizBuilder> nodeSepCases() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();

    GraphvizBuilder graphvizBuilder = Graphviz
        .digraph()
        .label("nodeSep_test")
        .addLine(a, b)
        .addLine(a, c);

    return Stream.of(graphvizBuilder)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().nodeSep(0),
                    g.clone().nodeSep(0.1),
                    g.clone().nodeSep(0.5),
                    g.clone().nodeSep(1.1),
                    g.clone().nodeSep(2.1),
                    g.clone().nodeSep(5)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> nslimitCases() {
    return Stream.of(Graphviz.digraph().label("nslimit_test"))
        .peek(GraphAttrTest::splinesCase)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().nslimit(1),
                    g.clone().nslimit(10),
                    g.clone().nslimit(20),
                    g.clone().nslimit(60),
                    g.clone().nslimit(100)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> nslimit1Cases() {
    return Stream.of(Graphviz.digraph().label("nslimit1_test"))
        .peek(GraphAttrTest::splinesCase)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().nslimit1(1),
                    g.clone().nslimit1(0),
                    g.clone().nslimit1(20),
                    g.clone().nslimit1(30)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> rankSepCases() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();

    GraphvizBuilder graphvizBuilder = Graphviz
        .digraph()
        .label("rankSep_test")
        .addLine(a, b);

    return Stream.of(graphvizBuilder)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    // TODO: 2023/3/14 Check why do not have line
                    g.clone().rankSep(0.1),
                    g.clone().rankSep(0.5),
                    g.clone().rankSep(1.1),
                    g.clone().rankSep(2),
                    g.clone().rankSep(3)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> marginCases() {
    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .label("margin_test")
        .addNode(Node.builder().build());

    return Stream.of(graphvizBuilder)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().margin(0, 0),
                    g.clone().margin(1, 2),
                    g.clone().margin(2, 1),
                    g.clone().margin(3, 3)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> mclimitCases() {
    GraphvizBuilder graphvizBuilder = Graphviz.digraph().label("mclimit_test");

    return Stream.of(graphvizBuilder)
        .peek(GraphAttrTest::splinesCase)
        .flatMap(
            builder -> {
              try {
                return Stream.of(
                    builder.clone().mclimit(1),
                    builder.clone().mclimit(10)
                );
              } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
              }
            }
        );
  }

  static Stream<GraphvizBuilder> compoundCases() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();

    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .label("compound_test")
        // Make sure this compound is turned on
        .compound(true)
        // The line will point directly to the head node, not the cluster
        .addLine(a, b)
        // Set the head node position of the line to be cut by the cluster with id "cluster_0"
        .addLine(Line.builder(a, c).lhead("cluster_0").build())

        // The head node "b" and "c" in a same cluster
        .cluster(
            Cluster.builder()
                // Set a cluster id, provide lhead with a unique cluster ID
                .id("cluster_0")
                .addNode(b, c)
                .build()
        );

    return Stream.of(graphvizBuilder)
        .flatMap(
            builder -> {
              try {
                return Stream.of(
                    builder.compound(false),
                    builder.clone().compound(true)
                );
              } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
              }
            }
        );
  }

  @ParameterizedTest
  @MethodSource("labelCases")
  public void testLabel(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("splinesCases")
  public void testSplines(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("rankdirCases")
  public void testRankdir(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @Test
  public void testColor() {
    Graphviz graphviz = Graphviz.digraph()
        .bgColor(Color.GREEN)
        .fontColor(Color.RED)
        .label("Color Test")
        .addNode(Node.builder().style(NodeStyle.INVIS).build())
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("nodeSepCases")
  public void testNodeSep(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("nslimitCases")
  public void testNslimit(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("nslimit1Cases")
  public void testNslimit1(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("rankSepCases")
  public void testRankSep(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("marginCases")
  public void testMargin(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("mclimitCases")
  public void testMclimit(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @ParameterizedTest
  @MethodSource("compoundCases")
  public void testCompound(GraphvizBuilder graphvizBuilder) {
    visual(graphvizBuilder);
  }

  @Test
  public void testShowGrid() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .label("showGrid_test")
        .splines(Splines.ORTHO)
        .showGrid(true)
        .addLine(a, b)
        .addLine(a, c)
        .addLine(a, d)
        .build();
    visual(graphviz);
  }

  @Test
  public void testHref() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .label("href_test")
        .href("https://github.com/")
        .addLine(a, b)
        .build();
    visual(graphviz);
  }

  private void visual(GraphvizBuilder graphvizBuilder) {
    Graphviz graphviz = graphvizBuilder.build();
    visual(graphviz);
  }
}
