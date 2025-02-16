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
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Tend;
import org.junit.jupiter.api.Test;

/**
 * Some tests of the example programs in the documentation.
 */
public class DocCaseTest extends GraphvizVisual {

  @Test
  public void testSubgraphDoc() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();

    Graphviz graphviz = Graphviz.digraph()
        // Subgraph in Graphviz
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addLine(a, b)
                // Subgraph inside Subgraph
                .subgraph(
                    Subgraph.builder()
                        .addLine(c, d)
                        .build()
                )
                .build()
        )
        .cluster(
            Cluster.builder()
                // Subgraph in Cluster
                .subgraph(
                    Subgraph.builder()
                        .rank(Rank.MAX)
                        .addLine(e, f)
                        .build()
                )
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testClusterDoc() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();

    Graphviz graphviz = Graphviz.digraph()
        .subgraph(
            Subgraph.builder()
                .rank(Rank.SAME)
                .addLine(a, b)
                // Cluster inside Subgraph
                .cluster(
                    Cluster.builder()
                        .addLine(c, d)
                        .build()
                )
                .build()
        )
        // Cluster in Graphviz
        .cluster(
            Cluster.builder()
                // Cluster in Cluster
                .cluster(
                    Cluster.builder()
                        .addLine(e, f)
                        .build()
                )
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testStartSubDoc() {
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(c, d)

        // First level subgraph start
        .startSub()
        .addLine(f, e)

        // Second level subgraph start
        .startSub()
        .rank(Rank.MIN)
        .addNode(g)
        // Second level subgraph end
        .endSub()

        // First level subgraph end
        .endSub()

        // Graphviz end
        .build();

    visual(graphviz);
  }

  @Test
  public void testStartClusDoc() {
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();

    Graphviz graphviz = Graphviz.digraph()
        .addLine(c, d)

        // First level cluster start
        .startClus()
        .addLine(f, e)

        // Second level cluster start
        .startClus()
        .addNode(g)
        // Second level cluster end
        .endClus()

        // First level cluster end
        .endClus()

        // Graphviz end
        .build();

    visual(graphviz);
  }

  @Test
  public void testTempNodeDoc() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();

    Graphviz graphviz = Graphviz.digraph()
        .tempNode(Node.builder().shape(NodeShapeEnum.RECT).build())
        // The rect shape set in the root container takes effect
        .addNode(a)
        .cluster(
            Cluster.builder()
                .tempNode(Node.builder().shape(NodeShapeEnum.TRIANGLE).build())
                // The triangle shape set in the cluster takes effect
                .addNode(b)
                .subgraph(
                    Subgraph.builder()
                        .tempNode(Node.builder().shape(NodeShapeEnum.CIRCLE).build())
                        // The circle shape set in the subgraph takes effect
                        .addNode(c)
                        .build()
                )
                .build()
        )
        .cluster(
            Cluster.builder()
                // The rect shape set in the root container takes effect
                .addNode(d)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testTempLineDoc() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();
    Node g = Node.builder().label("g").build();
    Node h = Node.builder().label("h").build();

    Graphviz graphviz = Graphviz.digraph()
        .tempLine(Line.tempLine().arrowHead(ArrowShape.DOT).build())
        // The dot arrow shape set in the root container takes effect
        .addLine(a, b)
        .cluster(
            Cluster.builder()
                .tempLine(Line.tempLine().arrowHead(ArrowShape.VEE).build())
                // The vee arrow shape set in the cluster takes effect
                .addLine(c, d)
                .subgraph(
                    Subgraph.builder()
                        .tempLine(
                            Line.tempLine().arrowHead(ArrowShape.CURVE).color(Color.RED).build())
                        // The curve arrow shape set in the subgraph takes effect
                        .addLine(e, f)
                        .build()
                )
                .build()
        )
        .cluster(
            Cluster.builder()
                // The dot shape set in the root container takes effect
                .addLine(g, h)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testLheadDoc() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();

    Graphviz graphviz = Graphviz.digraph()
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
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testLtailDoc() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();

    Graphviz graphviz = Graphviz.digraph()
        // The line will point directly to the tail node, not the cluster
        .addLine(b, a)
        // Set the tail node position of the line to be cut by the cluster with id "cluster_0"
        .addLine(Line.builder(c, a).ltail("cluster_0").build())

        // The tail node "b" and "c" in a same cluster
        .cluster(
            Cluster.builder()
                // Set a cluster id, provide ltail with a unique cluster ID
                .id("cluster_0")
                .addNode(b, c)
                .build()
        )
        .build();

    visual(graphviz);
  }

  @Test
  public void testDocAttrCase() {
Node a = Node.builder().id("a").build();
Node b = Node.builder().id("b").build();

FloatLabel tailLabel = FloatLabel.builder()
    .tend(Tend.TAIL)
    .label("Tail Label")
    .build();

// Edge with main label and tail label
Line edgeWithTailLabel = Line.builder(a, b)
    .label("Main Label")
    .floatLabels(tailLabel)  // Label at the tail of the edge
    .build();

Graphviz graph = Graphviz.digraph()
    .addLine(edgeWithTailLabel)
    .build();

    visual(graph);
  }
}
