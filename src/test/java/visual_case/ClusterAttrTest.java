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
import org.graphper.api.attributes.ClusterShapeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.graphper.api.Cluster;
import org.graphper.api.Cluster.ClusterBuilder;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeStyle;

public class ClusterAttrTest extends GraphvizVisual {

  static Stream<ClusterBuilder> labelCases() {
    return Stream.of(Cluster.builder())
        .peek(g -> {
          Node a = Node.builder().label("cluster_internal").build();
          Node b = Node.builder().label("start").build();
          Node c = Node.builder().label("end").build();
          g.addLine(a, b);
          g.addLine(a, c);
          g.label("label_test");
        })
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().fontSize(10).labeljust(Labeljust.CENTER),
                    g.clone().fontSize(50).labeljust(Labeljust.LEFT),
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

  static Stream<ClusterBuilder> marginCases() {
    ClusterBuilder clusterBuilder = Cluster.builder()
        .label("cluster_margin_test")
        .addNode(Node.builder().build());

    return Stream.of(clusterBuilder)
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().margin(0, 0),
                    g.clone().margin(1, 2)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<ClusterBuilder> penWidthCases() {
    return Stream.of(Cluster.builder())
        .flatMap(
            g -> {
              try {
                return Stream.of(
                    g.clone().penWidth(0),
                    g.clone().penWidth(0.1),
                    g.clone().penWidth(5),
                    g.clone().penWidth(10).color(Color.INDIGO),
                    g.clone().penWidth(20).color(Color.GREEN)
                );
              } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
              }
            }
        );
  }

  static Stream<ClusterBuilder> styleShapeCases() {
    return Stream.of(
            Cluster.builder().style(ClusterStyle.DASHED),
            Cluster.builder().style(ClusterStyle.DOTTED),
            Cluster.builder().style(ClusterStyle.INVIS),
            Cluster.builder().style(ClusterStyle.BOLD),
            Cluster.builder().style(ClusterStyle.ROUNDED)
        )
        .flatMap(
            g -> Stream.of(ClusterShapeEnum.values()).map(s -> {
                  try {
                    return g.clone().shape(s);
                  } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                  }
            })
        );
  }

  @ParameterizedTest
  @MethodSource("labelCases")
  public void testLabel(ClusterBuilder clusterBuilder) {
    Graphviz graphviz = Graphviz.digraph()
        .label("cluster_label_test")
        .cluster(clusterBuilder.build()).build();
    visual(graphviz);
  }

  @Test
  public void testColor() {
    Graphviz graphviz = Graphviz.digraph()
        .bgColor(Color.ORANGE)
        .startClus()
        .label("Cluster Color Test")
        .color(Color.PURPLE)
        .bgColor(Color.GREEN)
        .fontColor(Color.RED)
        .addNode(Node.builder().style(NodeStyle.INVIS).build())
        .endClus()
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("marginCases")
  public void testMargin(ClusterBuilder clusterBuilder) {
    Graphviz graphviz = Graphviz.digraph()
        .cluster(clusterBuilder.build())
        .build();
    visual(graphviz);
  }

  @Test
  public void testHref() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();

    Graphviz graphviz = Graphviz.digraph()
        .addNode(Node.builder().label("OutOfCluster").build())
        .startClus()
        .label("cluster_href_test")
        .href("https://github.com/")
        .addLine(a, b)
        .endClus()
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("penWidthCases")
  public void testPenWidth(ClusterBuilder clusterBuilder) {
    Graphviz graphviz = Graphviz.digraph()
        .cluster(
            clusterBuilder
                .label("cluster_penWidth_test")
                .addNode(Node.builder().build())
                .build()
        )
        .build();
    visual(graphviz);
  }

  @ParameterizedTest
  @MethodSource("styleShapeCases")
  public void testShape_Style(ClusterBuilder clusterBuilder) {
    Node a = Node.builder().label("a").build();

    Graphviz graphviz = Graphviz.digraph()
        .label("test_cluster_shape_style")
        .cluster(clusterBuilder.addNode(a).label("Cluster_Shape_Style").build())
        .build();
    visual(graphviz);
  }
}
