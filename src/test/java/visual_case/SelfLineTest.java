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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.graphper.def.BiConcatIterable;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;

public class SelfLineTest extends GraphvizVisual {

  static Iterable<Port> ALL_PORTS = new BiConcatIterable<>(Collections.singletonList(null),
                                                           Arrays.asList(Port.values()));

  static Stream<GraphvizBuilder> portCases() {
    Node a = Node.builder().label("a").shape(NodeShapeEnum.RECT).build();

    int size = (int) Math.pow(Port.values().length + 1, 2);
    List<Line> selfLines = new ArrayList<>(size);
    for (Port p1 : ALL_PORTS) {
      for (Port p2 : ALL_PORTS) {
        Line line = Line.builder(a, a)
            .tailPort(p1)
            .headPort(p2)
            .floatLabels(
                FloatLabel.builder()
                    .label("tailPort=" + (p1 != null ? p1.name() : "center"))
                    .build(),
                FloatLabel.builder()
                    .label("headPort=" + (p2 != null ? p2.name() : "center"))
                    .lengthRatio(1)
                    .build()
            )
            .build();
        selfLines.add(line);
      }
    }

    return selfLines.stream().map(line -> Graphviz.digraph().addLine(line));
  }

  @ParameterizedTest
  @MethodSource("portCases")
  public void testPortSelfLine(GraphvizBuilder graphvizBuilder) {
    Graphviz graphviz = graphvizBuilder.label("Self_Line_Port_Exhaustive").build();
    visual(graphviz);
  }

  @Test
  public void testSelfLine() {
    /*
    digraph G {
        a:w->a:ne
        a:w->a:e[label="1111\n2222"]
        a:w->a:e[label="3333\n4444"]

        b:w->b:sw
        b:w->b:sw
        b:w->b:s
        b:w->b:se
        b:w->b:se[label="WEST -> SOUTH_WEST"]
        b:w->b:se

        c:w->c:nw[label="2222\n333333333333\n4444"]
        c:w->c:nw[label="hello world\nhello world\nhello world\nhello world"]
        c:sw->c:n
    }
     */

    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();
    Node h = Node.builder().label("h").build();
    Node i = Node.builder().label("i").build();
    Node j = Node.builder().label("j").build();
    Node k = Node.builder().label("k").build();
    Node l = Node.builder().label("l").build();
    Node m = Node.builder().label("m").build();
    Node n = Node.builder().label("n").build();

    GraphvizBuilder graphvizBuilder = Graphviz.digraph()
        .scale(0.7)
        .addLine(Line.builder(a, a).headPort(Port.WEST).tailPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(a, a).tailPort(Port.WEST).headPort(Port.NORTH_EAST).build())

        .addLine(Line.builder(a, a).tailPort(Port.WEST).build())
        .addLine(
            Line.builder(a, a).tailPort(Port.WEST).headPort(Port.EAST)
                .label("1111\n2222")
                .build()
        )
        .addLine(
            Line.builder(a, a).tailPort(Port.WEST).headPort(Port.EAST)
                .label("3333\n4444")
                .build()
        )

        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH).build())
        .addLine(Line.builder(b, b).tailPort(Port.WEST).headPort(Port.SOUTH_EAST).build())
        .addLine(Line.builder(b, b).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH_WEST)
                     .label("WEST -> SOUTH_WEST").build())
        .addLine(Line.builder(b, b).tailPort(Port.NORTH_WEST).headPort(Port.SOUTH_WEST).build())

        .addLine(Line.builder(c, c).tailPort(Port.WEST).headPort(Port.NORTH_WEST)
                     .label("2222\n333333333333\n4444").build())
        .addLine(Line.builder(c, c).tailPort(Port.WEST).headPort(Port.NORTH_WEST)
                     .label("hello world\nhello world\nhello world\nhello world").build())
        .addLine(Line.builder(c, c).tailPort(Port.SOUTH_WEST).headPort(Port.NORTH).build())

        .addLine(
            Line.builder(d, d).tailPort(Port.WEST).headPort(Port.EAST)
                .label("Graphviz self loop")
                .build()
        )
        .addLine(
            Line.builder(d, d).tailPort(Port.WEST).headPort(Port.EAST)
                .label("Port line label")
                .build()
        )
        .addLine(Line.builder(d, d).tailPort(Port.WEST).headPort(Port.EAST).build())
        .addLine(Line.builder(d, d).tailPort(Port.NORTH).headPort(Port.NORTH_EAST).build())
        .addLine(Line.builder(d, d).tailPort(Port.NORTH_WEST).headPort(Port.NORTH).build())

        .addLine(Line.builder(e, e).dir(
            Dir.BOTH).arrowSize(0.6).tailPort(Port.NORTH_WEST).headPort(Port.EAST).build())
        .addLine(
            Line.builder(e, e).dir(Dir.BOTH).arrowSize(0.3).tailPort(Port.EAST).headPort(Port.SOUTH)
                .label("EAST->SOUTH").build())

        .addLine(
            Line.builder(f, f)
                .tailPort(Port.NORTH)
                .headPort(Port.EAST)
                .floatLabels(
                    FloatLabel.builder()
                        .label("Tail")
                        .lengthRatio(0)
                        .build(),
                    FloatLabel.builder()
                        .label("Center")
                        .lengthRatio(0.5)
                        .build(),
                    FloatLabel.builder()
                        .label("Head")
                        .lengthRatio(1)
                        .build()
                )
                .build()
        )

        .addLine(Line.builder(g, g).tailPort(Port.NORTH_EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(g, g).tailPort(Port.NORTH_EAST).headPort(Port.EAST).build())
        .addLine(g, g)

        .addLine(Line.builder(c, h).style(LineStyle.INVIS).build())

        .addLine(h, h)
        .addLine(h, h)
        .addLine(h, h)
        .addLine(Line.builder(h, h).tailPort(Port.SOUTH_WEST).headPort(Port.SOUTH_WEST).build())
        .addLine(Line.builder(h, h).label("Self Line H -> Self Line H").build())

        .addLine(Line.builder(i, i).tailPort(Port.WEST).headPort(Port.WEST).build())
        .addLine(Line.builder(i, i).tailPort(Port.NORTH_WEST).headPort(Port.NORTH_WEST).build())

        .addLine(Line.builder(j, j).tailPort(Port.NORTH).headPort(Port.NORTH).controlPoints(true)
                     .tailclip(false).headclip(false).build())
        .addLine(Line.builder(j, j).tailPort(Port.NORTH_EAST).headPort(Port.NORTH_EAST).build())

        .addLine(Line.builder(k, k).tailPort(Port.EAST).headPort(Port.EAST).build())
        .addLine(Line.builder(k, k).tailPort(Port.SOUTH_EAST).headPort(Port.SOUTH_EAST).build())

        .addLine(Line.builder(l, l).tailPort(Port.SOUTH).headPort(Port.SOUTH).build())
        .addLine(Line.builder(l, l).tailPort(Port.SOUTH_WEST).headPort(Port.SOUTH_WEST).build())

        .addLine(Line.builder(m, m).tailPort(Port.SOUTH).headPort(Port.SOUTH).arrowSize(0.3)
                     .dir(Dir.BOTH).build())
        .addLine(Line.builder(m, m).tailPort(Port.NORTH).headPort(Port.NORTH).arrowSize(0.5)
                     .dir(Dir.BOTH).build())

        .addLine(Line.builder(n, n).headclip(false).build())
        .addLine(Line.builder(n, n).tailclip(false).build())
        .addLine(Line.builder(n, n).tailclip(false).headclip(false).build())
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(n, n)
        .addLine(Line.builder(n, n).label("fff").build())
        .addLine(Line.builder(n, n).label("Self line").build())
        .addLine(n, n)
        .addLine(Line.builder(n, n).label("yyyyy").build())

        .cluster(
            Cluster.builder()
                .addNode(c)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addNode(h, i, j, k, l, m, n)
                .build()
        );

    visual(graphvizBuilder.tempNode(Node.builder().shape(NodeShapeEnum.RECT).build()).build());
  }
}