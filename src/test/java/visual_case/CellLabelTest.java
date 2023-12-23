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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;

public class CellLabelTest extends GraphvizVisual {

  static Stream<Graphviz> cellLabelCases() throws CloneNotSupportedException {
    Node empty_1 = Node.builder().label("|||").build();
    Node empty_2 = Node.builder().label("{|||}").build();
    Node a = Node.builder()
        .label("111|222|{<P0>333|444}")
        .style(NodeStyle.DASHED)
        .color(Color.RED)
        .fontColor(Color.BLUE)
        .margin(0.8)
        .build();
    Node b = Node.builder().label("111|222|{333\n333\n333|444|555|666}").height(3).build();
    Node c = Node.builder().label("111|222|{333|{444|555}|666}").width(2).build();
    Node d = Node.builder().label("{{Name|Sex|Age}|{Michael|Men|15}|{Abigail|Female|18}}")
        .build();
    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .label("Cell_Label_Test")
        .tempNode(Node.builder().shape(NodeShapeEnum.RECORD).build())
        .addLine(a, b)
        .addNode(c)
        .addNode(d)
        .addNode(empty_1)
        .addNode(empty_2);

    return Stream.of(graphvizBuilder.build(),
                     graphvizBuilder.clone()
                         .rankdir(Rankdir.LR)
                         .tempNode(Node.builder().shape(NodeShapeEnum.M_RECORD).build())
                         .build());
  }

  static Stream<GraphvizBuilder> cellLabelSelfLineCases() throws CloneNotSupportedException {
    Node a = Node.builder().label("1|{<P2>2|{3|{<P4>4|{<P5>5|{6|{<P7>7|{<P8>8|{<P9>9|10}}}}}}}}")
        .build();
    Node b = Node.builder().label("<A>A|B|{C|{D|<E>E}}|F").build();
    Node c = Node.builder().label("C").build();
    GraphvizBuilder builder = Graphviz.digraph()
        .tempNode(Node.builder().shape(NodeShapeEnum.RECORD).build())
        .addLine(c, c)
        .addLine(Line.builder(c, c).tailPort(Port.NORTH_WEST).headPort(Port.WEST).build())
        .addLine(
            Line.builder(a, a)
                .label("22334111\n11111\n111111\n111111\n1111111\n11111")
                .tailCell("P5")
                .tailPort(Port.NORTH_EAST)
                .headCell("P5")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("22334")
                .tailCell("P5")
                .tailPort(Port.NORTH_EAST)
                .headCell("P5")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("LEFT\nLEFT\nLEFT")
                .tailCell("P5")
                .tailPort(Port.NORTH_WEST)
                .headCell("P5")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("LEFT")
                .tailCell("P5")
                .tailPort(Port.NORTH_WEST)
                .headCell("P5")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("5555555")
                .tailCell("P2")
                .tailPort(Port.NORTH_WEST)
                .headCell("P2")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("666666")
                .tailCell("P2")
                .tailPort(Port.NORTH_EAST)
                .headCell("P2")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("666666")
                .tailCell("P9")
                .tailPort(Port.NORTH_EAST)
                .headCell("P9")
                .headPort(Port.SOUTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("666666")
                .tailCell("P9")
                .tailPort(Port.SOUTH_EAST)
                .headCell("P9")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .label("666666")
                .tailCell("P7")
                .tailPort(Port.SOUTH_WEST)
                .headCell("P7")
                .headPort(Port.SOUTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .tailCell("P7")
                .tailPort(Port.SOUTH_EAST)
                .headCell("P7")
                .headPort(Port.SOUTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .tailCell("P8")
                .tailPort(Port.NORTH_WEST)
                .headCell("P8")
                .headPort(Port.SOUTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(a, a)
                .tailCell("P8")
                .tailPort(Port.SOUTH_WEST)
                .headCell("P8")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(b, b)
                .label("ffff\nggg\ndff")
                .style(LineStyle.DASHED)
                .tailPort(Port.NORTH_EAST)
                .tailCell("E")
                .headPort(Port.SOUTH)
                .build()
        )
        .addLine(
            Line.builder(b, b)
                .tailPort(Port.EAST)
                .headPort(Port.EAST)
                .build()
        )
        .addLine(
            Line.builder(b, b)
                .tailPort(Port.NORTH_EAST)
                .tailCell("A")
                .headPort(Port.SOUTH_WEST)
                .headCell("A")
                .build()
        )
        .addLine(
            Line.builder(b, b)
                .tailPort(Port.SOUTH_WEST)
                .tailCell("A")
                .headPort(Port.NORTH_EAST)
                .headCell("A")
                .build()
        );

    return Stream.of(builder, builder.clone().rankdir(Rankdir.LR),
                     builder.clone().rankdir(Rankdir.RL), builder.rankdir(Rankdir.BT));
  }

  static Stream<GraphvizBuilder> cellPortCases() {
    Node a = Node.builder().shape(NodeShapeEnum.RECORD)
        .label("{<A>A|{<B>B|<C>C}|{{<D>D|<E>E}|<F>F|<G>G}}").build();
    Node b = Node.builder().build();

    List<GraphvizBuilder> graphvizBuilders = new ArrayList<>();
    for (int i = 65; i < 72; i++) {
      for (Port port : Port.values()) {
        Line line = Line.builder(a, b)
            .tailCell(String.valueOf((char) i))
            .tailPort(port)
            .floatLabels(
                FloatLabel.builder()
                    .label("tailPort=" + port.name())
                    .build()
            )
            .build();
        graphvizBuilders.add(Graphviz.digraph().addLine(line));
      }
    }

    return graphvizBuilders.stream();
  }

  @ParameterizedTest
  @MethodSource("cellLabelCases")
  public void testCellLabel(Graphviz graphviz) {
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("cellLabelSelfLineCases")
  public void testCellLabelSelfLine(GraphvizBuilder graphvizBuilder) {
//    System.setProperty("port_axis.node.expander", "true");

    Graphviz graphviz = graphvizBuilder.label("Cell_Label_Self_Line_Test").build();
    visual(graphviz);
  }

  @Test
  public void testCellLabelLine() {
    Node a = Node.builder().shape(NodeShapeEnum.RECORD)
        .label("111111|22222|{33333333|<P4>4444|5555|<P5>6666}").build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.LINE)
        .startSub()
//        .rank(Rank.SAME)
        .addLine(
            Line.builder(a, b)
                .tailPort(Port.EAST)
                .tailCell("P5")
                .build()
        )
        .addLine(
            Line.builder(a, b)
                .tailPort(Port.EAST)
                .tailCell("P5")
                .build()
        )
        .addLine(
            Line.builder(a, b)
                .tailPort(Port.WEST)
                .tailCell("P5")
                .headPort(Port.EAST)
                .build()
        )
        .endSub()
        .build();
    visual(graphviz);
  }

  @Test
  public void testTableAlign() {
    Node a = Node.builder().label("{{Name|Sex|Age}|{Michael|Men|15}|{Abigail|Female|18}}")
        .margin(0.5)
        .build();
    Node b = Node.builder().label(
            "{{1|2|3}|{4|5|6}|{7|8|{{22|22|33}|{44|55|66}|{77|88|{{1010|2020}|{3030|40404040}}}}}}")
        .build();
    Node c = Node.builder().label(
            "{First Cell Row1\nFirst Cell Row2\nFirst Cell Row3|Third Cell}|{Second Cell|Fourth Cell}")
        .build();
    Node d = Node.builder().label(
            "{{First|Second|Third}|{|||}|{3Line: First|3Line: Second|3Line:Third}|{|||{{o|x|o}|{x|o|x}|{o|x|o}}}}")
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .label("Node Record Cases")
        .tempNode(
            Node.builder()
                .shape(NodeShapeEnum.RECORD)
                .color(Color.PURPLE)
                .fontColor(Color.BLUE)
                .style(NodeStyle.BOLD)
                .build()
        )
        .addNode(a, b, c, d)
        .build();

    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("cellPortCases")
  public void testCellPort(GraphvizBuilder graphvizBuilder) {
    Graphviz graphviz = graphvizBuilder.label("Cell_Port_Test").build();
    visual(graphviz);
  }
}
