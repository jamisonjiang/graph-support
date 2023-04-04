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
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;

public class NodeAttrTest extends GraphvizVisual {

  static Stream<NodeBuilder> sizeCases() {
    return Stream.of(
        Node.builder().label("a").fontSize(50).height(0.1).width(0.05),
        Node.builder().label("a").height(0.05).width(0.1),
        Node.builder().label("a").height(0.5).width(1),
        Node.builder().label("a").margin(0.7).width(1).height(1).width(0.5).labelloc(Labelloc.TOP),
        Node.builder().label("a").labelloc(Labelloc.BOTTOM).height(3).width(1),
        Node.builder().label("a").labelloc(Labelloc.TOP).height(2).width(5)
    );
  }

  static Stream<Node> shapeCases() {
    Node[] nodes = new Node[NodeShapeEnum.values().length];
    int i = 0;
    for (NodeShapeEnum shape : NodeShapeEnum.values()) {
      NodeBuilder builder = Node.builder().label(shape.getName()).shape(shape);
      if (i % 2 == 0) {
        builder.color(Color.RED).fillColor(Color.GREEN).fontColor(Color.YELLOW);
      } else {
        builder.color(Color.INDIGO).fillColor(Color.ORANGE).fontColor(Color.BLUE);
      }

      nodes[i++] = builder.build();
    }
    return Stream.of(nodes);
  }

  static Stream<Node> fixedSizeCases() {
    return Stream.of(
        Node.builder().fixedSize(true).label("First Line\nSecond Line\nThird Line").height(0.3)
            .width(0.4).build(),
        Node.builder().fixedSize(false).label("First Line\nSecond Line\nThird Line").build()
    );
  }

  static Stream<Node> styleCases() {
    return Stream.of(
        Node.builder().label(NodeStyle.DASHED.getName()).style(NodeStyle.DASHED).build(),
        Node.builder().label(NodeStyle.DOTTED.getName()).style(NodeStyle.DOTTED).build(),
        Node.builder().label(NodeStyle.SOLID.getName()).style(NodeStyle.SOLID).build(),
        Node.builder().label(NodeStyle.INVIS.getName()).style(NodeStyle.INVIS).build(),
        Node.builder().label(NodeStyle.BOLD.getName()).style(NodeStyle.BOLD).build()
    );
  }

  static Stream<Node> sideCases() {
    List<Node> nodes = new ArrayList<>(17);
    for (int i = 4; i <= 20; i++) {
      nodes.add(Node.builder()
                    .label("side=" + i)
                    .shape(NodeShapeEnum.REGULAR_POLYLINE)
                    .sides(i).build());
    }
    return nodes.stream();
  }

  static Stream<Node> penWidthCases() {
    return Stream.of(
      Node.builder().penWidth(0.1).build(),
      Node.builder().penWidth(5).build(),
      Node.builder().penWidth(10).build(),
      Node.builder().penWidth(20).build()
      );
  }

  @ParameterizedTest
  @MethodSource("sizeCases")
  public void testHeightWidth(NodeBuilder nodeBuilder) {
    Graphviz graphviz = Graphviz.digraph()
        .label("node_size_test")
        .addNode(nodeBuilder.build())
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("shapeCases")
  public void testShapeColor(Node node) {
    Graphviz graphviz = Graphviz.digraph()
        .label("shape_color_test")
        .addNode(node)
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("fixedSizeCases")
  public void testFixedSize(Node node) {
    Graphviz graphviz = Graphviz.digraph()
        .label("fixedSize_test")
        .addNode(node)
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("styleCases")
  public void testStyle(Node node) {
    Graphviz graphviz = Graphviz.digraph()
        .label("node_style_test")
        .addNode(node)
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("sideCases")
  public void testSides(Node node) {
    Graphviz graphviz = Graphviz.digraph()
        .label("sides_test")
        .addNode(node)
        .build();
    visual(graphviz);
  }

  @Test
  public void testHref() {
    Graphviz graphviz = Graphviz.digraph()
        .label("href_test")
        .addNode(Node.builder().href("https://github.com/").build())
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("penWidthCases")
  public void testPenWidth(Node node) {
    Graphviz graphviz = Graphviz.digraph()
        .label("node_penWidth_test")
        .addNode(node)
        .build();
    visual(graphviz);
  }
}
