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

package org.graphper.layout.dot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Random all-false graphs with routing- and geometry-affecting attributes. */
public class ConstraintFalseFuzzTest {

  @Test
  public void allFalseRandomAttributeCombinationsAlwaysRoute() {
    int seeds = Integer.getInteger("constraint.fuzz.seeds", 1000);
    Map<Integer, String> failures = new LinkedHashMap<>();
    for (int seed = 0; seed < seeds; seed++) {
      try {
        run(seed);
      } catch (Throwable throwable) {
        StringWriter stack = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stack));
        failures.put(seed, stack.toString());
      }
    }
    Assertions.assertTrue(failures.isEmpty(),
                          () -> "constraint=false failures: " + failures);
  }

  /** Tight same-rank gaps used to invert the middle RouterBox walls. */
  @Test
  public void tightSameRankRoutingRegressionSeeds() throws Exception {
    run(2964);
    run(3020);
  }

  private void run(int seed) throws Exception {
    Random random = new Random(seed);
    int nodeCount = 3 + random.nextInt(8);
    List<Node> nodes = new ArrayList<>();
    NodeShapeEnum[] shapes = {NodeShapeEnum.RECT, NodeShapeEnum.ELLIPSE,
        NodeShapeEnum.DIAMOND, NodeShapeEnum.TRIANGLE, NodeShapeEnum.TRAPEZIUM,
        NodeShapeEnum.PENTAGON, NodeShapeEnum.HEXAGON, NodeShapeEnum.SEPTAGON,
        NodeShapeEnum.OCTAGON, NodeShapeEnum.STAR};
    for (int i = 0; i < nodeCount; i++) {
      Node.NodeBuilder node = Node.builder().id("n" + i).label("n" + i)
          .shape(shapes[random.nextInt(shapes.length)])
          .width(0.5 + random.nextDouble() * 2)
          .height(0.3 + random.nextDouble());
      int regular = random.nextInt(3);
      if (regular != 0) {
        node.regular(regular == 2);
      }
      nodes.add(node.build());
    }

    Layout layout = random.nextBoolean() ? Layout.DOT : Layout.DOTQ;
    Splines[] modes = {Splines.SPLINE, Splines.ROUNDED, Splines.POLYLINE,
        Splines.LINE, Splines.ORTHO};
    Rankdir rankdir = Rankdir.values()[random.nextInt(Rankdir.values().length)];
    Splines splines = modes[random.nextInt(modes.length)];
    GraphvizBuilder builder = Graphviz.digraph()
        .layout(layout)
        .rankdir(rankdir)
        .splines(splines);

    if (random.nextBoolean()) {
      Cluster.ClusterBuilder cluster = Cluster.builder().id("cluster_" + seed);
      for (int i = 0; i < nodeCount / 2; i++) {
        cluster.addNode(nodes.get(i));
      }
      builder.cluster(cluster.build());
      for (int i = nodeCount / 2; i < nodeCount; i++) {
        builder.addNode(nodes.get(i));
      }
    } else {
      nodes.forEach(builder::addNode);
    }

    int edgeCount = 1 + random.nextInt(nodeCount * 3);
    List<Line> lines = new ArrayList<>();
    for (int i = 0; i < edgeCount; i++) {
      Node tail = nodes.get(random.nextInt(nodeCount));
      Node head = nodes.get(random.nextInt(nodeCount));
      Line.LineBuilder line = Line.builder(tail, head)
          .constraint(false)
          .minlen(random.nextInt(11))
          .weight(random.nextDouble() * 100)
          .tailPort(Port.values()[random.nextInt(Port.values().length)])
          .headPort(Port.values()[random.nextInt(Port.values().length)])
          .tailclip(random.nextBoolean())
          .headclip(random.nextBoolean())
          .dir(Dir.values()[random.nextInt(Dir.values().length)])
          .arrowHead(ArrowShape.values()[random.nextInt(ArrowShape.values().length)])
          .arrowTail(ArrowShape.values()[random.nextInt(ArrowShape.values().length)])
          .arrowSize(0.1 + random.nextDouble() * 5)
          .radian(random.nextDouble() * 40)
          .style(random.nextBoolean() ? LineStyle.DASHED : LineStyle.DOTTED);
      if (random.nextBoolean()) {
        line.label("label-" + i);
      }
      if (random.nextInt(3) == 0) {
        line.floatLabels(FloatLabel.builder().label("float-" + i)
                             .lengthRatio(random.nextDouble())
                             .offset(random.nextDouble() - 0.5, random.nextDouble() - 0.5)
                             .build());
      }
      Line built = line.build();
      lines.add(built);
      builder.addLine(built);
    }

    DrawGraph drawGraph = layout.getLayoutEngine().layout(builder.build());
    for (int i = 0; i < lines.size(); i++) {
      Line line = lines.get(i);
      String context = "seed=" + seed + " layout=" + layout + " rankdir=" + rankdir
          + " splines=" + splines + " line=" + i + " tail=" + line.tail().nodeAttrs().getId()
          + " head=" + line.head().nodeAttrs().getId() + " attrs=" + line.lineAttrs();
      LineDrawProp prop = drawGraph.getLineDrawProp(line);
      Assertions.assertNotNull(prop, context);
      if (line.tail() == line.head()) {
        Assertions.assertFalse(prop.isEmpty(), context + " prop=" + prop);
      } else {
        Assertions.assertTrue(prop.size() >= 2, context + " prop=" + prop);
      }
      Assertions.assertNotNull(prop.getStart(), context);
      Assertions.assertNotNull(prop.getEnd(), context);
    }
  }
}
