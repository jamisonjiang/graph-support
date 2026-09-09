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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.Curves;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Random dense ranked graphs that sample every final spline against unrelated node shapes. */
public class SplineObstacleFuzzTest {

  @Test
  public void randomDenseLongEdgesAvoidNodes() {
    int seeds = Integer.getInteger("spline.obstacle.fuzz.seeds", 100);
    int start = Integer.getInteger("spline.obstacle.fuzz.start", 0);
    Map<Integer, String> failures = new HashMap<>();
    for (int seed = start; seed < start + seeds; seed++) {
      try {
        run(seed);
      } catch (Throwable throwable) {
        failures.put(seed, throwable.toString());
      }
    }
    Assertions.assertTrue(failures.isEmpty(), () -> "spline obstacle failures: " + failures);
  }

  private void run(int seed) throws Exception {
    Random random = new Random(seed);
    int ranks = 4 + random.nextInt(5);
    int columns = 3 + random.nextInt(4);
    Node[][] nodes = new Node[ranks][columns];
    List<Node> allNodes = new ArrayList<>();
    Layout layout = random.nextBoolean() ? Layout.DOT : Layout.DOTQ;
    Rankdir rankdir = Rankdir.values()[random.nextInt(Rankdir.values().length)];
    Graphviz.GraphvizBuilder graph = Graphviz.digraph()
        .layout(layout)
        .rankdir(rankdir)
        .splines(Splines.SPLINE).nodeSep(0.25 + random.nextDouble() * 0.5)
        .rankSep(0.5 + random.nextDouble());
    NodeShapeEnum[] shapes = {NodeShapeEnum.RECT, NodeShapeEnum.ELLIPSE,
        NodeShapeEnum.DIAMOND, NodeShapeEnum.HEXAGON};
    for (int rank = 0; rank < ranks; rank++) {
      Subgraph.SubgraphBuilder same = Subgraph.builder().rank(Rank.SAME);
      for (int column = 0; column < columns; column++) {
        Node node = Node.builder().id("n" + rank + '_' + column)
            .shape(shapes[random.nextInt(shapes.length)])
            .label("r" + rank + " c" + column).build();
        nodes[rank][column] = node;
        allNodes.add(node);
        same.addNode(node);
      }
      graph.subgraph(same.build());
    }
    for (int rank = 0; rank < ranks - 1; rank++) {
      for (int column = 0; column < columns; column++) {
        graph.addLine(nodes[rank][column], nodes[rank + 1][column]);
      }
    }

    List<Line> longLines = new ArrayList<>();
    int longEdges = columns + random.nextInt(columns * 2);
    for (int i = 0; i < longEdges; i++) {
      int fromRank = random.nextInt(Math.max(1, ranks - 3));
      int toRank = fromRank + 3 + random.nextInt(ranks - fromRank - 3);
      Line line = Line.builder(nodes[fromRank][random.nextInt(columns)],
                               nodes[toRank][random.nextInt(columns)])
          .label("long " + seed + '-' + i).weight(random.nextDouble()).build();
      longLines.add(line);
      graph.addLine(line);
    }

    DrawGraph draw = layout.getLayoutEngine().layout(graph.build());
    for (Line line : longLines) {
      LineDrawProp prop = draw.getLineDrawProp(line);
      if (prop == null || !prop.isBesselCurve() || prop.size() < 4) {
        continue;
      }
      for (int i = 0; i + 3 < prop.size(); i += 3) {
        for (int sample = 1; sample < 20; sample++) {
          FlatPoint point = Curves.besselEquationCalc(sample / 20D, prop.get(i), prop.get(i + 1),
                                                      prop.get(i + 2), prop.get(i + 3));
          for (Node node : allNodes) {
            if (node == line.tail() || node == line.head()) {
              continue;
            }
            NodeDrawProp nodeProp = draw.getNodeDrawProp(node);
            Assertions.assertFalse(nodeProp.shapeProp().in(nodeProp, point),
                                   "seed=" + seed + " layout=" + layout + " rankdir=" + rankdir
                                       + " line=" + line.tail().nodeAttrs().getId() + "->"
                                       + line.head().nodeAttrs().getId() + " node="
                                       + node.nodeAttrs().getId() + " point=" + point
                                       + " controls=" + prop);
          }
        }
      }
    }
  }
}
