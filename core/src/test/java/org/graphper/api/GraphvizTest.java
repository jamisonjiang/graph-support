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

package org.graphper.api;

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import java.util.concurrent.Phaser;
import org.graphper.api.Cluster;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer.GraphContainerBuilder;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.Subgraph.IntegrationSubgraphBuilder;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Rankdir;
import org.graphper.def.CycleDependencyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphvizTest {

  @Test
  public void testCycleContainerDependency() {
    Subgraph s1 = Subgraph.builder().addNode(Node.builder().label("a").build()).build();
    Subgraph s2 = Subgraph.builder().subgraph(s1).build();
    Subgraph s3 = Subgraph.builder().build();
    Subgraph s4 = Subgraph.builder().addNode(Node.builder().build()).build();
    s1.addSubgraph(s2);
    Cluster c1 = Cluster.builder().addNode(Node.builder().build()).build();
    Cluster c2 = Cluster.builder().cluster(c1).build();
    Cluster c3 = Cluster.builder().cluster(c2).cluster(c1).build();

    // Exist cycle dependency
    Assertions.assertThrows(CycleDependencyException.class,
                        () -> Graphviz.digraph().subgraph(s2).build());
    // Contain empty subgraph
    Assertions.assertThrows(IllegalArgumentException.class,
                        () -> Graphviz.graph().subgraph(s3).build());
    // Adding the same Subgraph multiple times
    Assertions.assertThrows(IllegalArgumentException.class,
                        () -> Graphviz.graph().subgraph(s4).subgraph(s4).build());
    Assertions.assertThrows(IllegalArgumentException.class,
                            () -> Graphviz.graph().cluster(c3).build());
  }

  @Test
  public void testMaxSubgraphDepth() {
    GraphvizBuilder graphvizBuilder = Graphviz.digraph();
    build(graphvizBuilder, 1, 999);
    // Still not exceed the maximum nesting depth
    graphvizBuilder.build();

    build(graphvizBuilder, 1, 1000);
    // Maximum container nesting exceeded.
    Assertions.assertThrows(IllegalArgumentException.class, graphvizBuilder::build);
  }

  @Test
  public void testMultipleBuild() {
    GraphvizBuilder graphvizBuilder = Graphviz.graph();
    Node a = Node.builder().build();
    Line line = Line.builder(a, a).build();

    Graphviz g1 = graphvizBuilder.addNode(a).build();
    Graphviz g2 = graphvizBuilder.addLine(line).build();

    Assertions.assertNotEquals(g1, g2);
    Assertions.assertEquals(1, g1.nodeNum());
    Assertions.assertEquals(1, g2.nodeNum());
    Assertions.assertTrue(g1.containsNode(a));
    Assertions.assertTrue(g2.containsNode(a));
    Assertions.assertEquals(0, g1.lineNum());
    Assertions.assertEquals(1, g2.lineNum());
    Assertions.assertFalse(g1.containsLine(line));
    Assertions.assertTrue(g2.containsLine(line));
  }

  @Test
  public void testMultipleThreadBuild() {
    GraphvizBuilder graphvizBuilder = Graphviz.digraph();
    Phaser phaser = new Phaser(101);
    for (int i = 0; i < 100; i++) {
      new Thread(() -> {
        graphvizBuilder.subgraph(
            Subgraph.builder()
                .addNode(Node.builder().build())
                .build()
        );
        phaser.arriveAndAwaitAdvance();
      }).start();
    }
    phaser.arriveAndAwaitAdvance();

    Graphviz graphviz = graphvizBuilder.build();
    Assertions.assertEquals(100, graphviz.subgraphs().size());
    Assertions.assertEquals(100, graphvizBuilder.build().subgraphs().size());
  }

  @Test
  public void testBuilderClone() throws CloneNotSupportedException {
    GraphvizBuilder builder = Graphviz.digraph().labeljust(Labeljust.RIGHT);

    GraphvizBuilder repl = builder.clone();
    repl.labeljust(Labeljust.LEFT);

    Assertions.assertEquals(Labeljust.RIGHT, builder.build().graphAttrs().getLabeljust());
    Assertions.assertEquals(Labeljust.LEFT, repl.build().graphAttrs().getLabeljust());

    Graphviz g1 = builder.build();
    Graphviz g2 = builder.labeljust(Labeljust.LEFT).build();

    Assertions.assertEquals(Labeljust.RIGHT, g1.graphAttrs().getLabeljust());
    Assertions.assertEquals(Labeljust.LEFT, g2.graphAttrs().getLabeljust());
  }

  @Test
  public void testFather() {
    Node n = Node.builder().build();
    Cluster c1 = Cluster.builder().addNode(n).build();
    Subgraph s1 = Subgraph.builder().addNode(n).cluster(c1).build();
    Subgraph s2 = Subgraph.builder().subgraph(s1).build();
    Subgraph s3 = Subgraph.builder().subgraph(s2).build();
    Cluster c2 = Cluster.builder().addNode(n).build();
    Cluster c3 = Cluster.builder().cluster(c2).subgraph(s3).build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(c3)
        .build();

    Assertions.assertEquals(s1, graphviz.father(c1));
    Assertions.assertEquals(c3, graphviz.father(c2));
    Assertions.assertEquals(s2, graphviz.father(s1));
    Assertions.assertEquals(s3, graphviz.father(s2));
    Assertions.assertEquals(c3, graphviz.effectiveFather(c1));
  }

  @Test
  public void testSerial() throws IOException, ClassNotFoundException {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Line line = Line.builder(a, b).build();
    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.LR)
        .bgColor(Color.BLUE)
        .addLine(line)
        .startSub()
        .addNode(a)
        .endSub()
        .startClus()
        .addNode(b)
        .endClus()
        .build();

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + "graphviz", graphviz,
        g -> {
          GraphAttrs graphAttrs = g.graphAttrs();
          Assertions.assertEquals(Rankdir.LR, graphAttrs.getRankdir());
          Assertions.assertEquals(4, g.nodeNum());
          Assertions.assertEquals(1, g.lineNum());
          Assertions.assertEquals(1, g.subgraphs().size());
          Assertions.assertEquals(1, g.clusters().size());
          Assertions.assertEquals(Color.BLUE, g.graphAttrs().getBgColor());
        });
  }

  private void build(GraphContainerBuilder c, int a, int b) {
    if (a > b) {
      return;
    }
    IntegrationSubgraphBuilder<Graphviz, GraphvizBuilder> s = c.startSub();
    s.addNode(Node.builder().build());
    build(s, a + 1, b);
    s.endSub();
  }
}
