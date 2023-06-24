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
import helper.TableUtils;
import java.io.File;
import java.io.IOException;
import org.graphper.api.Assemble;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rankdir;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableCaseTest extends GraphvizVisual {

  @Test
  public void testAssembleBuild() {
//     System.setProperty("port_axis.node.expander", "true");

    Node a = Node.builder()
        .margin(1, 1)
        .labelloc(Labelloc.TOP)
        .fillColor(Color.GREEN)
//        .shape(NodeShapeEnum.RECORD)
//        .label("{{{|<P1>}}|cell 2}|{<c3>cell 3|cell 4}")
        .assemble(
            Assemble.builder()
                .width(1.2)
                .height(0.6)
                .addCell(0, 0,
                         Node.builder()
                             .shape(NodeShapeEnum.PLAIN)
                             .style(NodeStyle.DASHED)
                             .fixedSize(false)
                             .assemble(
                                 Assemble.builder()
                                     .width(0.6)
                                     .height(0.3)
                                     .addCell(0, 0,
                                              Node.builder()
                                                  .height(0.3)
                                                  .width(0.3)
                                                  .style(NodeStyle.DASHED)
                                                  .fillColor(Color.GREEN)
                                                  .build())
                                     .addCell(0.3, 0,
                                              Node.builder()
                                                  .id("P1")
                                                  .height(0.3)
                                                  .width(0.3)
                                                  .shape(NodeShapeEnum.STAR)
                                                  .fillColor(Color.RED)
                                                  .build())
                                     .build()
                             )
                             .build())
                .addCell(0.6, 0,
                         Node.builder()
                             .label("cell 2")
                             .style(NodeStyle.DASHED)
                             .fixedSize(true)
                             .height(0.3)
                             .width(0.6)
                             .build())
                .addCell(0, 0.3,
                         Node.builder()
                             .id("c3")
                             .label("cell 3")
                             .style(NodeStyle.DASHED)
                             .fixedSize(true)
                             .height(0.3)
                             .width(0.6)
                             .build())
                .addCell(0.6, 0.3,
                         Node.builder()
                             .label("cell 4")
                             .style(NodeStyle.DASHED)
                             .fixedSize(true)
                             .height(0.3)
                             .width(0.6)
                             .build())
                .build()
        )
        .build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.RL)
        .tempNode(Node.builder().height(2).build())
        .assemble(
            Assemble.builder()
                .width(1.2)
                .height(0.6)
                .addCell(0, 0,
                         Node.builder()
                             .width(0.5)
                             .height(0.6)
                             .label("Class")
                             .fillColor(Color.YELLOW)
                             .build())
                .addCell(0.5, 0.1,
                         Node.builder()
                             .width(0.7)
                             .height(0.4)
                             .label("Class")
                             .fillColor(Color.ofRGB("#add8e6"))
                             .build())
                .build()
        )
        .addLine(Line.builder(a, a).tailCell("P1").headCell("c3").headPort(Port.SOUTH).build())

        .startClus()
        .labeljust(Labeljust.RIGHT)
        .labelloc(Labelloc.TOP)
        .assemble(
            Assemble.builder()
                .width(1.8)
                .height(1.5)
                .addCell(0, 0,
                         Node.builder()
                             .width(1.8)
                             .height(1.5)
                             .fillColor(Color.ofRGB("#ffe4c4"))
                             .build())
                .addCell(0.05, 0.05,
                         Node.builder()
                             .width(1.2)
                             .height(0.3)
                             .label("elephant")
                             .build())
                .addCell(1.3, 0.05,
                         Node.builder()
                             .width(0.45)
                             .height(1)
                             .labelloc(Labelloc.BOTTOM)
                             .fillColor(Color.GREEN)
                             .label("two")
                             .build())
                .addCell(0.6, 0.4,
                         Node.builder()
                             .width(0.65)
                             .height(0.65)
                             .fontSize(11)
                             .label("penguin")
                             .build())
                .addCell(0.6, 1.1,
                         Node.builder()
                             .width(1.15)
                             .height(0.35)
                             .fontSize(11)
                             .label("4")
                             .penWidth(3)
                             .build())
                .addCell(0.05, 0.4,
                         Node.builder()
                             .width(0.5)
                             .height(1.05)
                             .build())
                .addCell(0.1, 0.45,
                         Node.builder()
                             .width(0.4)
                             .height(0.95)
                             .fillColor(Color.ofRGB("#c0c0c0"))
                             .build())
                .addCell(0.15, 0.5,
                         Node.builder()
                             .width(0.3)
                             .height(0.25)
                             .label("corn")
                             .fontSize(8)
                             .build())
                .addCell(0.15, 0.8,
                         Node.builder()
                             .width(0.3)
                             .height(0.25)
                             .label("c")
                             .fillColor(Color.YELLOW)
                             .build())
                .addCell(0.15, 1.1,
                         Node.builder()
                             .width(0.3)
                             .height(0.25)
                             .label("f")
                             .build())
                .build()
        )
        .tempNode(Node.builder().height(1).build())
        .addLine(Line.builder(a, b)
                     .tailCell("P1")
                     .tailPort(Port.NORTH_WEST)
                     .assemble(
                         Assemble.builder()
                             .width(1.6)
                             .height(0.6)
                             .addCell(0.05, 0.05,
                                      Node.builder()
                                          .width(0.2)
                                          .height(0.5)
                                          .fillColor(Color.RED)
                                          .build())
                             .addCell(0.3, 0.05,
                                      Node.builder()
                                          .width(1)
                                          .height(0.5)
                                          .fontSize(12)
                                          .label("Edge labels\nalso")
                                          .build())
                             .addCell(1.35, 0.05,
                                      Node.builder()
                                          .width(0.2)
                                          .height(0.5)
                                          .fillColor(Color.BLUE)
                                          .build())
                             .addCell(0, 0,
                                      Node.builder()
                                          .width(1.6)
                                          .height(0.6)
                                          .build())
                             .build()
                     )
                     .build())
        .endClus()
        .startClus()
        .addLine(Line.builder(a, Node.builder().label("c").build())
                     .assemble(
                         Assemble.builder()
                             .width(1.6)
                             .height(0.6)
                             .addCell(0, 0,
                                      Node.builder()
                                          .width(0.8)
                                          .height(0.6)
                                          .label("LabelCell")
                                          .build())
                             .addCell(0.8, 0,
                                      Node.builder()
                                          .width(0.8)
                                          .height(0.6)
                                          .shape(NodeShapeEnum.PLAIN)
                                          .assemble(
                                              Assemble.builder()
                                                  .width(0.8)
                                                  .height(0.6)
                                                  .addCell(0, 0,
                                                           Node.builder()
                                                               .width(0.5)
                                                               .height(0.6)
                                                               .penWidth(2)
                                                               .shape(NodeShapeEnum.TRAPEZIUM)
                                                               .build())
                                                  .addCell(0.5, 0,
                                                           Node.builder()
                                                               .width(0.3)
                                                               .height(0.6)
                                                               .shape(NodeShapeEnum.CYLINDER)
                                                               .build())
                                                  .build()
                                          )
                                          .build())
                             .build()
                     )
                     .build())
        .endClus()
        .build();

    visual(graphviz);
  }

  @Test
  public void testHtml1() {
    Node node = Node.builder()
        .table(
            table()
                .tr(
                    td().colSpan(50).text("1111111111111\n111111111111"),
                    td().colSpan(2).text("2")
                )
                .tr(
                    td().text("3"),
                    td().colSpan(51).text("444444444444"),
                    td().text("5"),
                    td().text("6")
                )
        )
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .addNode(node)
        .build();

    visual(graphviz);
  }

  @Test
  public void testHtml2() {
    Node node = Node.builder()
        .table(
            table()
                .tr(
                    td().rowSpan(2).colSpan(2).text("1111"),
                    td().rowSpan(2).text("22222\n2222222222\n22222222222\n222222\n2222"),
                    td().text("3333"),
                    td().text("4444444444")
                )
                .tr(
                    td().colSpan(555).text("5555"),
                    td().colSpan(3).text("6"),
                    td().rowSpan(2).text("7"),
                    td().rowSpan(2).text("8888888888")
                )
                .tr(
                    td().text("hhh"), td().text("ee"), td(), td().text("kkk")
                )
        )
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .addNode(node)
        .build();

    visual(graphviz);
  }

  @Test
  public void testHtml3() {
    Node node = Node.builder()
        .shape(NodeShapeEnum.PLAIN_TEXT)
        .table(
            table()
                .cellSpacing(30)
                .tr(
                    td().text("11111111111111111111111111111111111111111111111"),
                    td().text("1"),
                    td().text("1"),
                    td().text("11"),
                    td().text("1"),
                    td().text("1"),
                    td().text("1"),
                    td().rowSpan(4).text("1"),
                    td().text("1"),
                    td().rowSpan(4).text("1"),
                    td().text("1"),
                    td().rowSpan(3).text("1")
                )
                .tr(
                    td().colSpan(4).rowSpan(10)
                        .text("333333333333333333333333333333333333333333333333333"),
                    td().text("4"),
                    td().text("5"),
                    td().colSpan(3).rowSpan(3).text("6")
                )
                .tr(
                    td().colSpan(2).text("3"),
                    td().colSpan(5).text(
                        "4344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444"),
                    td().text("5"),
                    td().text("6")
                )
                .tr(
                    td().text("88"),
                    td().text("88"),
                    td().text("88"),
                    td().text("88"),
                    td().text("88"),
                    td().text("88"),
                    td().text("88")
                )
        )
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .scale(0.7)
        .addNode(node)
        .build();

    visual(graphviz);
  }

  @Test
  public void testHtml4() {
    Node a = Node.builder()
        .margin(1, 1)
        .labelloc(Labelloc.TOP)
        .fillColor(Color.GREEN)
        .table(
            table().border(0).cellSpacing(0)
                .tr(td().style(NodeStyle.DASHED),
                    td().id("P1").shape(NodeShapeEnum.STAR)
                        .width(0.3).height(0.3)
                        .bgColor(Color.RED)
                        .fixedSize(true),
                    td().style(NodeStyle.DASHED).text("cell 2")
                )
                .tr(td().id("c3").colSpan(2).style(NodeStyle.DASHED).text("cell 3"),
                    td().style(NodeStyle.DASHED).text("cell 4")
                )
        )
        .build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.RL)
        .tempNode(Node.builder().height(2).build())
        .table(
            table()
                .tr(
                    td().bgColor(Color.YELLOW).text("Class"),
                    td().bgColor(Color.ofRGB("#add8e6")).text("Class")
                )
        )
        .addLine(Line.builder(a, a).tailCell("P1").headCell("c3").headPort(Port.SOUTH).build())

        .startClus()
        .labeljust(Labeljust.RIGHT)
        .labelloc(Labelloc.TOP)
        .table(
            table().cellSpacing(3).bgColor(Color.ofRGB("#ffe4c4"))
                .tr(
                    td().colSpan(3).text("elephant"),
                    td().rowSpan(2).bgColor(Color.GREEN).valign(Labelloc.TOP).text("two")
                )
                .tr(
                    td().rowSpan(2).table(
                        table().bgColor(Color.ofRGB("#c0c0c0"))
                            .tr(td().text("corn"))
                            .tr(td().bgColor(Color.YELLOW).text("c"))
                            .tr(td().align(Labeljust.LEFT).text("f"))
                    ),
                    td().text("penguin")
                )
                .tr(td().border(2).align(Labeljust.RIGHT).text("4"))
        )
        .tempNode(Node.builder().height(1).build())
        .addLine(Line.builder(a, b)
                     .tailCell("P1")
                     .tailPort(Port.NORTH_WEST)
                     .table(
                         table().cellSpacing(4)
                             .tr(
                                 td().bgColor(Color.RED),
                                 td().text("Edge labels\n also"),
                                 td().bgColor(Color.BLUE)
                             )
                     )
                     .build())
        .endClus()
        .startClus()
        .addLine(Line.builder(a, Node.builder().label("c").build())
                     .table(
                         table().border(0).cellSpacing(0)
                             .tr(
                                 td().text("LabelCell"),
                                 td().border(2).width(20).height(30).shape(NodeShapeEnum.TRAPEZIUM),
                                 td().width(20).height(30).shape(NodeShapeEnum.CYLINDER)
                             )
                     )
                     .build())
        .endClus()
        .build();

    visual(graphviz);
  }

  @Test
  public void testHtml5() {
    Node a = Node.builder()
        .table(
            table().border(0).cellSpacing(0).cellPadding(4)
                .tr(
                    td().rowSpan(3).text("Hello\nWorld"),
                    td().colSpan(3).text("b"),
                    td().rowSpan(3).text("g"),
                    td().rowSpan(3).text("h")
                )
                .tr(
                    td().text("c"),
                    td().text("d"),
                    td().text("e")
                )
                .tr(td().colSpan(3).text("f"))
        )
        .build();
    Node b = Node.builder()
        .shape(NodeShapeEnum.PLAIN)
        .table(
            table().width(300).height(100).valign(Labelloc.BOTTOM).align(Labeljust.LEFT)
                .tr(
                    td().valign(Labelloc.TOP).text("line 1"),
                    td().valign(Labelloc.CENTER).bgColor(Color.BLUE).text("line 2"),
                    td().width(100).bgColor(Color.GREY).text("line 3"),
                    td().width(100).align(Labeljust.RIGHT).bgColor(Color.YELLOW)
                        .fontName("ambrosia").text("line 4"),
                    td().table(
                        table().cellPadding(0).border(0).cellSpacing(0)
                            .tr(
                                td().border(0).fontColor(Color.GREEN).text("Mixed"),
                                td().border(0).fontColor(Color.RED).text("fonts")
                            )
                    )
                )
        )
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .addNode(a, b)
        .build();

    visual(graphviz);
  }

  @Test
  public void testCycleDependency() {
    Table table = table();
    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder()
                     .table(
                         table
                             .tr(
                                 td().table(
                                     table()
                                         .tr(
                                             td().table(table),
                                             td()
                                         )
                                 ),
                                 td()
                             )
                     )
                     .build())
        .build();

    Assertions.assertThrows(ExecuteException.class, graphviz::toSvg);
  }

  @Test
  public void testTableNesting() {
    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder()
                     .shape(NodeShapeEnum.PLAIN)
                     .table(
                         table().href("https://graphviz.org/")
                             .bgColor(Color.ofRGB("#ffe4c4"))
                             .tr(
                                 td().width(100).bgColor(Color.BLUE).valign(Labelloc.BOTTOM).table(
                                     table().height(100).bgColor(Color.GREEN)
                                         .tr(
                                             td().bgColor(Color.ofRGB("#add8e6"))
                                                 .fontColor(Color.RED)
                                                 .href("https://stackoverflow.co/").text("Left"),
                                             td().fontColor(Color.WHITE).text("right")
                                         )
                                         .tr(
                                             td().color(Color.PURPLE), td()
                                         )
                                 ),
                                 td().table(
                                     table().border(0).cellSpacing(0)
                                         .tr(
                                             td().colSpan(2).rowSpan(2), td().colSpan(3)
                                         )
                                         .tr(
                                             td(), td(), td()
                                         )
                                         .tr(
                                             td(), td(), td(), td(),
                                             td().cellPadding(12).table(
                                                 table().color(Color.PURPLE).cellSpacing(20)
                                                     .cellPadding(10)
                                                     .tr(
                                                         td().text("o"), td().text("x"),
                                                         td().text("o")
                                                     )
                                                     .tr(
                                                         td().text("x"), td().text("o"),
                                                         td().text("x")
                                                     )
                                                     .tr(
                                                         td().text("o"), td().text("x"),
                                                         td().text("o")
                                                     )
                                             )
                                         )
                                 )
                             )
                     )
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void testTablePortLine() {
    Node struct1 = Node.builder()
        .table(
            table().border(0).cellBorder(1).cellSpacing(0)
                .tr(
                    td().text("left"),
                    td().id("f1").text("mid dle"),
                    td().id("f2").text("right")
                )
        ).build();

    Node struct2 = Node.builder()
        .table(
            table().border(0).cellBorder(1).cellSpacing(0)
                .tr(
                    td().id("f0").text("one"),
                    td().text("two")
                )
        ).build();

    Node struct3 = Node.builder()
        .table(
            table().border(0).cellBorder(1).cellSpacing(0).cellPadding(4)
                .tr(
                    td().rowSpan(3).text("hello\nworld"),
                    td().id("b").colSpan(3).text("b"),
                    td().id("g").rowSpan(3).text("g"),
                    td().rowSpan(3).text("h")
                )
                .tr(
                    td().id("c").text("c"),
                    td().id("here").text("d"),
                    td().text("e")
                )
                .tr(td().id("f").colSpan(3).text("f"))
        ).build();

    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .tempNode(Node.builder().shape(NodeShapeEnum.PLAIN).build())
        .addLine(Line.builder(struct1, struct2).tailCell("f1").headCell("f0").build())
        .addLine(Line.builder(struct1, struct3).tailCell("f2").headCell("f").build())
        .addLine(Line.builder(struct1, struct3).tailCell("f2").headCell("here").build());

    visual(graphvizBuilder.build());
    visual(graphvizBuilder.rankdir(Rankdir.LR).build());
    visual(graphvizBuilder.rankdir(Rankdir.RL).build());
    visual(graphvizBuilder.rankdir(Rankdir.BT).build());
  }

  @Test
  public void testHtml_dir() throws IOException {
    String tableCasesPath = DocumentUtils.getTableCasesPath();
    File tableCases = new File(tableCasesPath);
    for (File file : tableCases.listFiles()) {
      Table table = TableUtils.readFile(file);
      Assertions.assertNotNull(table);
      visual(Graphviz.digraph()
                 .addNode(Node.builder()
                              .shape(NodeShapeEnum.PLAIN)
                              .table(table)
                              .build())
                 .build());
    }
  }

  @Test
  public void testFloatHtml() {
    Node tail = Node.builder().label("tail").build();
    Node head = Node.builder().label("head").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .rankdir(Rankdir.LR)
        .rankSep(6)
        .addLine(
            Line.builder(tail, head)
                .floatLabels(
                    FloatLabel.builder()
                        .offset(0.5)
                        .lengthRatio(0.2)
                        .fontSize(20)
                        .label("tail")
                        .table(
                            table()
                                .tr(td(), td(), td())
                                .tr(td(), td().text("tail table"), td())
                                .tr(td(), td(), td())
                        )
                        .build(),
                    FloatLabel.builder()
                        .label("center")
                        .offset(0)
                        .lengthRatio(0.5)
                        .table(
                            table().cellSpacing(0)
                                .tr(td(), td(), td())
                                .tr(td(), td().fontColor(Color.GREEN).text("center table"), td())
                                .tr(td(), td(), td())
                        )
                        .build(),
                    FloatLabel.builder()
                        .label("head\nhead\nhead\nhead")
                        .offset(-0.5)
                        .lengthRatio(0.8)
                        .table(
                            table().cellSpacing(0).style(NodeStyle.BOLD)
                                .tr(td().text("x"), td().text("o"), td().text("x"))
                                .tr(td().text("o"), td().text("head table"), td().text("o"))
                                .tr(td().text("x"), td().text("o"), td().text("x"))
                        )
                        .fontSize(60)
                        .build()
                )
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testTableProp() {
    Td td = td().id("td_prop").text("x").align(Labeljust.RIGHT).valign(Labelloc.BOTTOM)
        .cellPadding(60).style(NodeStyle.DASHED).color(Color.GOLD).bgColor(Color.PINK)
        .border(5).fixedSize(false).height(10).width(150).href("https://graphviz.org/")
        .fontColor(Color.INDIGO).fontName("Impact").fontSize(50).shape(NodeShapeEnum.DIAMOND);

    Graphviz graphviz = Graphviz.digraph()
        .labelloc(Labelloc.TOP)
        .addNode(Node.builder().style(NodeStyle.INVIS).build())
        .table(
            table().id("Table").cellSpacing(0).align(Labeljust.LEFT).valign(Labelloc.TOP)
                .cellPadding(30).style(NodeStyle.DOTTED).color(Color.RED).bgColor(Color.GREY)
                .border(10).fixedSize(true).cellBorder(2).height(100).width(200)
                .fixedSize(false).href("https://github.com/")
                .tr(td, td().text("o"), td().text("x"))
                .tr(td().text("o"), td().text("x"), td().text("o"))
                .tr(td().text("x"), td().text("o"), td().text("x"))
        )
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    boolean findTable = false;
    boolean findTd = false;
    for (NodeDrawProp node : drawGraph.nodes()) {
      System.out.println();
      String nodeId = drawGraph.nodeId(node.getNode());
      if (nodeId.equalsIgnoreCase("Table")) {
        findTable = true;
      }
      if (nodeId.equalsIgnoreCase("td_prop")) {
        findTd = true;
      }
    }
    Assertions.assertTrue(findTd);
    Assertions.assertTrue(findTable);
    visual(graphviz);
  }
}
