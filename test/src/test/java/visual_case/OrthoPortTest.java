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
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.junit.jupiter.api.Test;

public class OrthoPortTest extends GraphvizVisual {

  @Test
  public void portCase1() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.ORTHO)
        .showGrid(true)
        .addLine(Line.builder(a, b)
                     .tailPort(Port.EAST)
                     .headPort(Port.SOUTH)
                     .label("line 1").build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.EAST)
                     .headPort(Port.SOUTH)
                     .label("line 1").build())
        .addLine(Line.builder(a, c)
                     .tailPort(Port.EAST).headPort(Port.EAST)
                     .label("line 2").build())
        .addLine(Line.builder(a, d)
                     .tailPort(Port.NORTH).headPort(Port.EAST)
                     .label("line 3").build())
        .addLine(Line.builder(a, e)
                     .tailPort(Port.SOUTH).headPort(Port.EAST)
                     .label("line 4").build())
        .build();

    visual(graphviz);
  }

  @Test
  public void portCase2() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.ORTHO)
        .showGrid(true)
        .addLine(Line.builder(a, b)
                     .tailPort(Port.WEST).headPort(Port.WEST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.WEST).headPort(Port.WEST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.WEST).headPort(Port.WEST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.WEST).headPort(Port.WEST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.WEST).headPort(Port.WEST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.EAST).headPort(Port.EAST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.EAST).headPort(Port.EAST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.EAST).headPort(Port.EAST)
                     .build())
        .addLine(Line.builder(a, b)
                     .tailPort(Port.EAST).headPort(Port.EAST)
                     .build())
        .addLine(Line.builder(c, d)
                     .tailPort(Port.EAST).headPort(Port.SOUTH)
                     .build())
        .addLine(Line.builder(c, d)
                     .tailPort(Port.NORTH).headPort(Port.WEST)
                     .build())
        .addLine(Line.builder(c, d)
                     .tailPort(Port.NORTH).headPort(Port.WEST)
                     .label("666")
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void portTestCase3() {
    Node a = Node.builder().shape(NodeShapeEnum.RECORD).label("{<p1>p1||<p3>p3}").build();
    Node b = Node.builder().shape(NodeShapeEnum.RECORD).label("{<p4>p4||<p2>p2}").build();

    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .showGrid(true)
        .splines(Splines.ORTHO)
        .addLine(Line.builder(a, b)
                     .label("p1 -> p2")
                     .tailCell("p1").headCell("p2")
                     .tailPort(Port.NORTH)
                     .headPort(Port.SOUTH_EAST)
                     .build())
        .addLine(Line.builder(a, b)
                     .label("p3 -> p4")
                     .tailCell("p3").headCell("p4")
                     .tailPort(Port.WEST)
                     .headPort(Port.WEST)
                     .build())
        .startSub()
        .rank(Rank.SAME)
        .addNode(a, b)
        .endSub();

    visual(graphvizBuilder.build());
    visual(graphvizBuilder.rankdir(Rankdir.LR).build());
    visual(graphvizBuilder.rankdir(Rankdir.RL).build());
    visual(graphvizBuilder.rankdir(Rankdir.BT).build());
  }

  @Test
  public void portTestCase4() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().shape(NodeShapeEnum.RECORD).label("{<f1>|<f2>}").build();
    Node d = Node.builder().shape(NodeShapeEnum.RECORD).label("{<f3>|<f4>}").build();

    Graphviz graphviz = Graphviz.digraph()
        .showGrid(true)
        .splines(Splines.ORTHO)
        .addLine(Line.builder(a, b)
                     .tailPort(Port.NORTH)
                     .headPort(Port.NORTH_WEST)
                     .build())
        .addLine(Line.builder(c, d)
                     .tailCell("f1")
                     .headCell("f3")
                     .tailPort(Port.WEST)
                     .headPort(Port.WEST)
                     .label("foo")
                     .build())
        .addLine(Line.builder(c, d)
                     .tailCell("f2")
                     .headCell("f4")
                     .tailPort(Port.EAST)
                     .headPort(Port.EAST)
                     .label("bar")
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void portTestCase5() {
    Node a = Node.builder().label("a").shape(NodeShapeEnum.RECT).build();
    Node b = Node.builder().shape(NodeShapeEnum.RECORD).label("<A>A|B|{C|{D|<E>E}}|F").build();
    Node c = Node.builder()
        .shape(NodeShapeEnum.RECORD)
        .label("1|{<P2>2|{3|{<P4>4|{<P5>5|{6|{<P7>7|{<P8>8|{<P9>9|10}}}}}}}}")
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.ORTHO)
        .addLine(a, a)
        .addLine(a, a)
        .addLine(a, a)
        .addLine(Line.builder(a, a).label("aaaa").build())
        .addLine(a, a)
        .addLine(a, a)
        .addLine(Line.builder(a, a)
                     .tailPort(Port.NORTH)
                     .headPort(Port.NORTH_WEST)
                     .label("fffffffffffffffffffff")
                     .build())
        .addLine(Line.builder(a, a)
                     .tailPort(Port.NORTH)
                     .headPort(Port.NORTH_WEST)
                     .label("fffffffffffffffffffff")
                     .build())
        .addLine(Line.builder(a, a)
                     .tailPort(Port.NORTH)
                     .headPort(Port.NORTH_WEST)
                     .build())
        .addLine(Line.builder(b, b).controlPoints(true).build())
        .addLine(
            Line.builder(b, b)
                .tailPort(Port.NORTH_EAST)
                .tailCell("E")
                .headPort(Port.SOUTH)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("22334111\n11111\n111111\n111111\n1111111\n11111")
                .tailCell("P5")
                .tailPort(Port.NORTH_EAST)
                .headCell("P5")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("22334")
                .tailCell("P5")
                .tailPort(Port.NORTH_EAST)
                .headCell("P5")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("LEFT\nLEFT\nLEFT")
                .tailCell("P5")
                .tailPort(Port.NORTH_WEST)
                .headCell("P5")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("LEFT")
                .tailCell("P5")
                .tailPort(Port.NORTH_WEST)
                .headCell("P5")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("5555555")
                .tailCell("P2")
                .tailPort(Port.NORTH_WEST)
                .headCell("P2")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("666666")
                .tailCell("P2")
                .tailPort(Port.NORTH_EAST)
                .headCell("P2")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("666666")
                .tailCell("P9")
                .tailPort(Port.NORTH_EAST)
                .headCell("P9")
                .headPort(Port.SOUTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("666666")
                .tailCell("P9")
                .tailPort(Port.SOUTH_EAST)
                .headCell("P9")
                .headPort(Port.NORTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .label("666666")
                .tailCell("P7")
                .tailPort(Port.SOUTH_WEST)
                .headCell("P7")
                .headPort(Port.SOUTH_EAST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .tailCell("P7")
                .tailPort(Port.SOUTH_EAST)
                .headCell("P7")
                .headPort(Port.SOUTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .tailCell("P8")
                .tailPort(Port.NORTH_WEST)
                .headCell("P8")
                .headPort(Port.SOUTH_WEST)
                .build()
        )
        .addLine(
            Line.builder(c, c)
                .tailCell("P8")
                .tailPort(Port.SOUTH_WEST)
                .headCell("P8")
                .headPort(Port.NORTH_WEST)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void portTestCase6() {
    Node e = Node.builder().shape(NodeShapeEnum.RECORD)
        .label("{<f0>f0|<f1>f1111111111111|{<f2>f2|<f22>}}").build();
    Node f = Node.builder().shape(NodeShapeEnum.RECORD)
        .label("{<f3>f3|{ooooooooo|<f4>f4|ooooooooo}|f5}").build();

    Graphviz graphviz = Graphviz.digraph()
        .splines(Splines.ORTHO)
        .addLine(Line.builder(e, f)
                     .tailCell("f1")
                     .headCell("f3")
                     .style(LineStyle.DASHED)
                     .build())
        .addLine(Line.builder(e, f)
                     .tailCell("f1")
                     .headCell("f3")
                     .tailPort(Port.EAST)
                     .headPort(Port.SOUTH)
                     .color(Color.RED)
                     .build())
        .addLine(Line.builder(e, f)
                     .tailCell("f2")
                     .headCell("f4")
                     .tailPort(Port.SOUTH_WEST)
                     .headPort(Port.NORTH_EAST)
                     .build())

        .addLine(Line.builder(e, f).tailCell("f2").headCell("f4").build())
        .addLine(Line.builder(e, f).tailCell("f2").headCell("f4").build())
        .addLine(Line.builder(e, f).tailCell("f2").headCell("f4").build())

        .addLine(Line.builder(e, f).tailCell("f2").headCell("f4")
                     .tailPort(Port.CENTER).headPort(Port.CENTER)
                     .build())
        .addLine(Line.builder(e, f).tailCell("f2").headCell("f4")
                     .tailPort(Port.CENTER).headPort(Port.CENTER)
                     .build())
        .addLine(Line.builder(e, f).tailCell("f2").headCell("f4")
                     .tailPort(Port.CENTER).headPort(Port.CENTER)
                     .build())
        .build();

    visual(graphviz);
  }

  @Test
  public void portTestCase7() {
    Node c = Node.builder().shape(NodeShapeEnum.RECORD).label("{<f1>f1|<f2>f2}").build();
    Node d = Node.builder().shape(NodeShapeEnum.RECORD).label("{<f3>f3|<f4>f4}").build();

    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.BT)
        .splines(Splines.ORTHO)
        .addLine(Line.builder(c, d)
                     .tailCell("f1").headCell("f3")
                     .label("f")
                     .build())
        .addLine(Line.builder(c, d)
                     .tailCell("f2").headCell("f4")
                     .label("s")
                     .build())
        .build();

    visual(graphviz);
  }
}