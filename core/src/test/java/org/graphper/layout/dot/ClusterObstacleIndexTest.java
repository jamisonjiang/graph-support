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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClusterObstacleIndexTest {

  @Test
  void includesBordersInRankGapsAndDeduplicatesQueries() {
    Cluster nearby = Cluster.builder().addNode(Node.builder().build()).build();
    Cluster distant = Cluster.builder().addNode(Node.builder().build()).build();
    DrawGraph draw = new DrawGraph(Graphviz.digraph().cluster(nearby).cluster(distant).build());
    ClusterDrawProp near = bounds(nearby, 10, 30, 5, 35);
    ClusterDrawProp far = bounds(distant, 100, 120, 50, 70);
    draw.clusterPut(nearby, near);
    draw.clusterPut(distant, far);
    DNode first = new DNode(null, 0, 0, 0);
    DNode second = new DNode(null, 0, 0, 0);
    first.setRank(1);
    second.setRank(2);
    DotDigraph graph = new DotDigraph(2);
    graph.add(first);
    graph.add(second);
    RankContent ranks = new RankContent(graph, 20, true, null);
    ranks.get(1).setStartY(0);
    ranks.get(1).setEndY(20);
    ranks.get(2).setStartY(40);
    ranks.get(2).setEndY(60);
    ClusterObstacleIndex index = new ClusterObstacleIndex(ranks, draw);
    RouterBox gap = new RouterBox(0, 50, 25, 39);

    Set<ClusterObstacleIndex.ClusterObstacle> found = index.query(Arrays.asList(gap, gap));
    Assertions.assertEquals(1, found.size());
    Assertions.assertSame(nearby, found.iterator().next().cluster());
    Assertions.assertTrue(index.query(Collections.singletonList(
        new RouterBox(40, 50, 25, 39))).isEmpty());
    Assertions.assertTrue(index.query(Collections.singletonList(
        new RouterBox(0, 50, 80, 90))).isEmpty());
  }

  @Test
  void directTerminalEntryNeedsNoDetourForEitherStoredOrientation() {
    for (boolean reversed : new boolean[]{false, true}) {
      Node outside = Node.builder().id("outside").build();
      Node inside = Node.builder().id("inside").build();
      Cluster cluster = Cluster.builder().addNode(inside).build();
      Line line = reversed ? Line.builder(inside, outside).build()
          : Line.builder(outside, inside).build();
      Graphviz graph = Graphviz.digraph().addNode(outside).cluster(cluster).addLine(line).build();
      DrawGraph draw = new DrawGraph(graph);
      draw.clusterPut(cluster, bounds(cluster, -20, 20, 40, 120));
      DNode from = new DNode(new NodeDrawProp(outside, outside.nodeAttrs()), 10, 10, 10);
      DNode to = new DNode(new NodeDrawProp(inside, inside.nodeAttrs()), 10, 10, 10);
      from.setContainer(graph);
      to.setContainer(cluster);
      from.setRank(1);
      to.setRank(2);
      from.setX(0);
      from.setY(0);
      to.setX(0);
      to.setY(100);
      DotDigraph nodes = new DotDigraph(2);
      nodes.add(from);
      nodes.add(to);
      RankContent ranks = new RankContent(nodes, 70, true, null);
      ranks.get(1).setStartY(-10);
      ranks.get(1).setEndY(20);
      ranks.get(2).setStartY(90);
      ranks.get(2).setEndY(110);
      ClusterAwareBoxGuide.ClusterRoute guide = ClusterAwareBoxGuide.routeBoxes(line, from, to,
          Arrays.asList(new RouterBox(-100, 100, -10, 20, from),
                        new RouterBox(-100, 100, 20, 90),
                        new RouterBox(-100, 100, 90, 110, to)),
          false, draw, new ClusterObstacleIndex(ranks, draw), Collections.emptySet());
      Assertions.assertTrue(guide.avoidedClusters().isEmpty());
      LineDrawProp route = new LineDrawProp(line, line.lineAttrs(), draw);
      route.setIsHeadStart(outside);
      route.addAll(Arrays.asList(new FlatPoint(0, 0), new FlatPoint(-30, 30),
                                new FlatPoint(-30, 100), new FlatPoint(0, 100)));
      Assertions.assertTrue(ClusterAwareBoxGuide.crossedAvoidedClusters(route, guide).isEmpty());
    }
  }

  private ClusterDrawProp bounds(Cluster cluster, double left, double right,
                                 double top, double bottom) {
    ClusterDrawProp box = new ClusterDrawProp(cluster);
    box.setLeftBorder(left);
    box.setRightBorder(right);
    box.setUpBorder(top);
    box.setDownBorder(bottom);
    return box;
  }
}
