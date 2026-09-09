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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.attributes.Port;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.LayoutAttach;
import org.graphper.parser.DotParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Case9ParallelPortOrderTest {

  @Test
  void nonflatProxyRetainsBaselineRanksAndZeroCrossings() throws Exception {
    Graphviz graph = baselineOrderedGraph();
    new DotLayoutEngine() {
      @Override
      protected void layout(DrawGraph draw, LayoutAttach attach) throws ExecuteException {
        DotAttachment attachment = (DotAttachment) attach;
        DotDigraph digraph = attachment.getDotDigraph();
        new ContainerCollapse(attachment, graph);
        try {
          Method legal = DotLayoutEngine.class.getDeclaredMethod("handleLegalLine",
              DotDigraph.class, DrawGraph.class);
          legal.setAccessible(true);
          legal.invoke(this, digraph, draw);
        } catch (ReflectiveOperationException e) {
          throw new AssertionError(e);
        }
        RankContent content = new RankContent(digraph, graph.graphAttrs().getRankSep(), true, null);
        MinCross minCross = new MinCross(content, attachment, false);
        List<String> ranks = new ArrayList<>();
        BasicCrossRank order = new BasicCrossRank(graph);
        for (RankContent.RankNode rank : content) {
          for (int i = 0; i < rank.size(); i++) {
            DNode node = rank.get(i);
            order.addNode(node);
            ranks.add(node.getRank() + ":" + i + ":" + node.getNode().nodeAttrs().getId());
            Assertions.assertEquals(i, node.getRankIndex());
          }
        }
        Assertions.assertEquals(Arrays.asList("1:0:sh0006", "1:1:t", "2:0:k", "2:1:sh0007"), ranks);
        int edges = 0;
        for (DLine line : minCross.getDigraphProxy().edges()) {
          edges++;
          Assertions.assertFalse(line.isSameRank());
          Assertions.assertEquals(Integer.valueOf(1), line.lineAttrs().getMinlen());
        }
        Assertions.assertEquals(2, edges, "self loops are removed and parallel edges are merged");
        RootCrossRank root = new RootCrossRank(draw, minCross.getDigraphProxy());
        root.setBasicCrossRank(order);
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
        root.setCacheExpired();
        Assertions.assertEquals(0, root.crossSnapshot().getCrossNum());
      }

      @Override
      protected void afterLayoutShifter(LayoutAttach attach) { }

      @Override
      protected void afterRenderShifter(LayoutAttach attach) { }
    }.layout(graph);
  }

  @Test
  void identicalInputOrderRetainsBaselineGeometryAndSixCrossings() throws Exception {
    Graphviz graph = baselineOrderedGraph();
    DrawGraph draw = new DotLayoutEngine().layout(graph);
    List<LineDrawProp> paths = new ArrayList<>();
    for (Line line : graph.lines()) {
      LineDrawProp path = draw.getLineDrawProp(line);
      Assertions.assertFalse(path.isBesselCurve(), "ortho paths need no sampling approximation");
      Assertions.assertFalse(path.isEmpty());
      paths.add(path);
    }
    Assertions.assertEquals(16, paths.size());
    List<String> pairs = new ArrayList<>();
    for (int i = 0; i < paths.size(); i++) {
      for (int j = i + 1; j < paths.size(); j++) {
        List<FlatPoint> a = paths.get(i);
        List<FlatPoint> b = paths.get(j);
        for (int x = 1; x < a.size(); x++) {
          for (int y = 1; y < b.size(); y++) {
            double d1 = orient(b.get(y - 1), b.get(y), a.get(x - 1));
            double d2 = orient(b.get(y - 1), b.get(y), a.get(x));
            double d3 = orient(a.get(x - 1), a.get(x), b.get(y - 1));
            double d4 = orient(a.get(x - 1), a.get(x), b.get(y));
            if (d1 * d2 < 0 && d3 * d4 < 0) {
              pairs.add(i + "/" + j);
            }
          }
        }
      }
    }
    // Include shared-endpoint and self-loop pairs, exactly as the corpus scanner does.
    Assertions.assertEquals(Arrays.asList("0/1", "0/2", "3/6", "4/5", "4/6", "5/6"), pairs);
    draw.nodes().forEach(node -> {
      String id = node.getNode().nodeAttrs().getId();
      Assertions.assertEquals(id.equals("k") ? -396 : id.equals("t") ? 27 : -171, node.getX(), 0.01);
      Assertions.assertEquals(id.equals("sh0006") || id.equals("t") ? 113.5 : 232, node.getY(), 0.01);
    });
  }

  private Graphviz baselineOrderedGraph() throws Exception {
    Graphviz graph;
    try (InputStream in = getClass().getResourceAsStream("/case9-parallel-port-order.dot")) {
      Assertions.assertNotNull(in);
      graph = DotParser.parse(in, StandardCharsets.UTF_8);
    }
    List<Line> lines = new ArrayList<>();
    graph.lines().forEach(lines::add);
    // Replay the input order measured with core JAR SHA-256 30eb811fd424... . Line.compareTo
    // includes enum identity hashes, so reparsing alone does not give an identical layout input.
    lines.sort(Comparator.comparingInt(line -> {
      if (line.tail().nodeAttrs().getId().equals("k")) {
        return line.lineAttrs().getTailPort() == Port.WEST ? 0 : 1;
      }
      if (line.head().nodeAttrs().getId().equals("k")) {
        return 2;
      }
      if (line.tail().nodeAttrs().getId().equals("sh0006")) {
        return line.lineAttrs().getHeadPort() == Port.SOUTH_EAST ? 3
            : line.lineAttrs().getHeadPort() == null ? 4 : 5;
      }
      String label = line.lineAttrs().getLabel();
      return label == null ? 6 : label.startsWith("iiiiii") ? 7 : label.equals("hehehe") ? 8 : 9;
    }));
    Field field = GraphContainer.class.getDeclaredField("lines");
    field.setAccessible(true);
    field.set(graph, new LinkedHashSet<>(lines));
    return graph;
  }

  private static double orient(FlatPoint a, FlatPoint b, FlatPoint c) {
    return (b.getX() - a.getX()) * (c.getY() - a.getY())
        - (b.getY() - a.getY()) * (c.getX() - a.getX());
  }
}
