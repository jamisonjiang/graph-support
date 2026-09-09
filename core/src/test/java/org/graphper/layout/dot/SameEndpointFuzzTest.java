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
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Random samehead/sametail groups mixed with ports, clipping, shapes and routing modes. */
public class SameEndpointFuzzTest {

  @Test
  public void randomGroupsShareEndpoints() {
    int seeds = Integer.getInteger("same.endpoint.fuzz.seeds", 1000);
    Map<Integer, String> failures = new HashMap<>();
    for (int seed = 0; seed < seeds; seed++) {
      try {
        run(seed);
      } catch (Throwable throwable) {
        failures.put(seed, throwable.toString());
      }
    }
    Assertions.assertTrue(failures.isEmpty(), () -> "same endpoint failures: " + failures);
  }

  private void run(int seed) throws Exception {
    Random random = new Random(seed);
    int nodeCount = 4 + random.nextInt(7);
    NodeShapeEnum[] shapes = {NodeShapeEnum.RECT, NodeShapeEnum.ELLIPSE,
        NodeShapeEnum.DIAMOND, NodeShapeEnum.HEXAGON, NodeShapeEnum.OCTAGON};
    List<Node> nodes = new ArrayList<>();
    for (int i = 0; i < nodeCount; i++) {
      nodes.add(Node.builder().id("n" + i).label("n" + i)
                    .shape(shapes[random.nextInt(shapes.length)]).build());
    }

    Layout layout = random.nextBoolean() ? Layout.DOT : Layout.DOTQ;
    Splines[] modes = {Splines.SPLINE, Splines.ROUNDED, Splines.POLYLINE,
        Splines.LINE, Splines.ORTHO};
    Splines splines = modes[random.nextInt(modes.length)];
    Graphviz.GraphvizBuilder graph = Graphviz.digraph().layout(layout).splines(splines)
        .rankdir(Rankdir.values()[random.nextInt(Rankdir.values().length)])
        .addNode(nodes.toArray(new Node[0]));
    List<Line> lines = new ArrayList<>();

    // Force at least one same-tail and one same-head group; add random groups around them.
    Node sharedTail = nodes.get(0);
    Node sharedHead = nodes.get(1);
    for (int i = 2; i < nodeCount; i++) {
      Line tailLine = line(sharedTail, nodes.get(i), random).sameTail("tail0").build();
      Line headLine = line(nodes.get(i), sharedHead, random).sameHead("head0").build();
      lines.add(tailLine);
      lines.add(headLine);
      graph.addLine(tailLine).addLine(headLine);
    }
    for (int i = 0; i < nodeCount; i++) {
      Node tail = nodes.get(random.nextInt(nodeCount));
      Node head = nodes.get(random.nextInt(nodeCount));
      Line.LineBuilder builder = line(tail, head, random);
      if (random.nextBoolean()) {
        builder.sameTail("tail" + random.nextInt(3));
      }
      if (random.nextBoolean()) {
        builder.sameHead("head" + random.nextInt(3));
      }
      Line line = builder.build();
      lines.add(line);
      graph.addLine(line);
    }

    DrawGraph draw = layout.getLayoutEngine().layout(graph.build());
    for (Line line : lines) {
      LineDrawProp prop = draw.getLineDrawProp(line);
      Assertions.assertNotNull(prop, "seed=" + seed);
      Assertions.assertFalse(prop.isEmpty(), "seed=" + seed + " line=" + line);
    }
    if (splines == Splines.ORTHO) {
      return;
    }
    assertGroup(draw, lines, true, seed);
    assertGroup(draw, lines, false, seed);
  }

  private Line.LineBuilder line(Node tail, Node head, Random random) {
    return Line.builder(tail, head)
        .constraint(random.nextBoolean())
        .tailPort(Port.values()[random.nextInt(Port.values().length)])
        .headPort(Port.values()[random.nextInt(Port.values().length)])
        .tailclip(random.nextBoolean())
        .headclip(random.nextBoolean());
  }

  private void assertGroup(DrawGraph draw, List<Line> lines, boolean tail, int seed) {
    Map<String, List<Line>> groups = new HashMap<>();
    for (Line line : lines) {
      if (line.tail() == line.head()) {
        continue;
      }
      String id = tail ? line.lineAttrs().getSameTail() : line.lineAttrs().getSameHead();
      Node owner = tail ? line.tail() : line.head();
      if (id != null) {
        groups.computeIfAbsent(System.identityHashCode(owner) + ":" + id,
                               key -> new ArrayList<>()).add(line);
      }
    }
    for (List<Line> group : groups.values()) {
      if (group.size() < 2) {
        continue;
      }
      FlatPoint expected = tail ? draw.getLineDrawProp(group.get(0)).getSameTailPoint()
          : draw.getLineDrawProp(group.get(0)).getSameHeadPoint();
      Assertions.assertNotNull(expected, "seed=" + seed);
      for (Line line : group) {
        FlatPoint actual = tail ? draw.getLineDrawProp(line).getSameTailPoint()
            : draw.getLineDrawProp(line).getSameHeadPoint();
        Assertions.assertEquals(expected, actual, "seed=" + seed);
      }
    }
  }
}
