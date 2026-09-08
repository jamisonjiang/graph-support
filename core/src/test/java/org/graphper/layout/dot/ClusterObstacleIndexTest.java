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
import java.util.List;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

  @Test
  void restrictionsFollowTheirBoxWhenALaterObstacleSplitsTheCorridor() {
    Node tailNode = Node.builder().id("tail").build();
    Node headNode = Node.builder().id("head").build();
    // Two unrelated obstacles: the wide one is avoidable outright, the narrow one only after the
    // rank gap is split, which shifts the station that already carried the first restriction.
    Cluster wide = Cluster.builder().id("wide").addNode(Node.builder().build()).build();
    Cluster narrow = Cluster.builder().id("narrow").addNode(Node.builder().build()).build();
    // The wide obstacle sits clear of the corridor centre so a whole-gap detour works and records
    // a restriction first. The narrow one straddles the centre, so only a split can clear it.
    Line line = Line.builder(tailNode, headNode).build();
    Graphviz graph = Graphviz.digraph().addNode(tailNode, headNode)
        .cluster(wide).cluster(narrow).addLine(line).build();
    DrawGraph draw = new DrawGraph(graph);
    // Obstacles are ordered by depth, then rank, then cluster number. Both sit at the same depth
    // and rank here, so the numbers must be explicit: leaving them equal makes the order fall back
    // to HashMap iteration over the cluster map, which varies with JVM history.
    ClusterDrawProp wideBounds = bounds(wide, -200, -100, 25, 40);
    ClusterDrawProp narrowBounds = bounds(narrow, -50, 50, 60, 80);
    wideBounds.setClusterNo(1);
    narrowBounds.setClusterNo(2);
    draw.clusterPut(wide, wideBounds);
    draw.clusterPut(narrow, narrowBounds);
    DNode from = new DNode(new NodeDrawProp(tailNode, tailNode.nodeAttrs()), 10, 10, 10);
    DNode to = new DNode(new NodeDrawProp(headNode, headNode.nodeAttrs()), 10, 10, 10);
    from.setContainer(graph);
    to.setContainer(graph);
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
        Arrays.asList(new RouterBox(-20, 20, -10, 20, from),
                      new RouterBox(-200, 200, 20, 90),
                      new RouterBox(-200, 200, 90, 110, to)),
        false, draw, new ClusterObstacleIndex(ranks, draw), Collections.emptySet());

    Assertions.assertEquals(2, guide.avoidedClusters().size(), "both obstacles are avoidable");
    Assertions.assertTrue(guide.boxes().size() > 3, "the narrow obstacle must force a split");
    // Every recorded restriction must name a cluster this route actually avoided, and must sit on
    // a station whose geometry really is narrowed. Index-keyed attribution failed both checks once
    // a split inserted a station ahead of the recorded position.
    List<RouterBox> stations = guide.boxes();
    boolean anyRestriction = false;
    for (int i = 0; i < stations.size(); i++) {
      Set<Cluster> at = guide.restrictionsAt(i);
      anyRestriction |= !at.isEmpty();
      Assertions.assertTrue(guide.avoidedClusters().containsAll(at),
                            "station " + i + " names a cluster that was never avoided");
      RouterBox station = stations.get(i);
      for (Cluster cluster : at) {
        ClusterDrawProp obstacle = draw.getClusterDrawProp(cluster);
        Assertions.assertTrue(
            station.getRightBorder() <= obstacle.getLeftBorder()
                || station.getLeftBorder() >= obstacle.getRightBorder(),
            "station " + i + " is credited with avoiding " + cluster.id()
                + " but still spans it horizontally");
      }
    }
    Assertions.assertTrue(anyRestriction, "an avoided cluster must be attributed to some station");

    // The gap station restricted for the wide obstacle was later split in two. Both halves keep
    // that geometry, so both must keep the attribution; otherwise a retry can drop the wrong
    // constraint. Index-based keying credited only the piece that inherited the old position.
    int creditedWithWide = 0;
    for (int i = 0; i < stations.size(); i++) {
      if (guide.restrictionsAt(i).contains(wide)) {
        creditedWithWide++;
      }
    }
    Assertions.assertEquals(2, creditedWithWide,
                            "both halves of the split gap must still be credited with the "
                                + "restriction that was applied before the split");
    Assertions.assertTrue(guide.restrictionsAt(-1).isEmpty());
    Assertions.assertTrue(guide.restrictionsAt(stations.size()).isEmpty());
  }

  @ParameterizedTest(name = "failed split rollback, horizontal={0}")
  @ValueSource(booleans = {false, true})
  void failedSplitRestoresEarlierRestrictions(boolean horizontal) {
    Node tailNode = Node.builder().id("tail").build();
    Node headNode = Node.builder().id("head").build();
    Cluster wide = Cluster.builder().id("wide").addNode(Node.builder().build()).build();
    Cluster failed = Cluster.builder().id("failed").addNode(Node.builder().build()).build();
    Line line = Line.builder(tailNode, headNode).build();
    Graphviz graph = Graphviz.digraph().addNode(tailNode, headNode)
        .cluster(wide).cluster(failed).addLine(line).build();
    DrawGraph draw = new DrawGraph(graph);
    ClusterDrawProp wideBounds = horizontal ? bounds(wide, 25, 40, -200, -100)
        : bounds(wide, -200, -100, 25, 40);
    ClusterDrawProp failedBounds = horizontal ? bounds(failed, 60, 80, -100, 210)
        : bounds(failed, -100, 210, 60, 80);
    wideBounds.setClusterNo(1);
    failedBounds.setClusterNo(2);
    draw.clusterPut(wide, wideBounds);
    draw.clusterPut(failed, failedBounds);
    DNode from = new DNode(new NodeDrawProp(tailNode, tailNode.nodeAttrs()), 10, 10, 10);
    DNode to = new DNode(new NodeDrawProp(headNode, headNode.nodeAttrs()), 10, 10, 10);
    from.setContainer(graph);
    to.setContainer(graph);
    from.setRank(1);
    to.setRank(2);
    from.setX(0);
    from.setY(0);
    to.setX(horizontal ? 100 : 0);
    to.setY(horizontal ? 0 : 100);
    DotDigraph nodes = new DotDigraph(2);
    nodes.add(from);
    nodes.add(to);
    RankContent ranks = new RankContent(nodes, 70, true, null);
    ranks.get(1).setStartY(-10);
    ranks.get(1).setEndY(20);
    ranks.get(2).setStartY(90);
    ranks.get(2).setEndY(110);
    List<RouterBox> original = horizontal
        ? Arrays.asList(new RouterBox(-10, 20, -20, 20, from),
                        new RouterBox(20, 90, -200, 200),
                        new RouterBox(90, 110, -200, 200, to))
        : Arrays.asList(new RouterBox(-20, 20, -10, 20, from),
                        new RouterBox(-200, 200, 20, 90),
                        new RouterBox(-200, 200, 90, 110, to));

    // The wide obstacle narrows the gap to -92 first. The second obstacle forces a split at
    // 52, but its clearance walls (-108 and 218) cannot fit on either side even after splitting.
    ClusterAwareBoxGuide.ClusterRoute guide = ClusterAwareBoxGuide.routeBoxes(line, from, to,
        original, horizontal, draw, new ClusterObstacleIndex(ranks, draw), Collections.emptySet());

    Assertions.assertEquals(Collections.singleton(wide), guide.avoidedClusters());
    Assertions.assertFalse(guide.avoidedClusters().contains(failed));
    Assertions.assertEquals(original.size(), guide.boxes().size(),
                            "failed split must restore the original three-station list");
    for (int i = 0; i < original.size(); i++) {
      RouterBox before = original.get(i);
      RouterBox after = guide.boxes().get(i);
      Assertions.assertSame(before.getNode(), after.getNode(), "station " + i);
      Assertions.assertEquals(i == 1 && !horizontal ? -92 : before.getLeftBorder(),
                              after.getLeftBorder(), "left border of station " + i);
      Assertions.assertEquals(before.getRightBorder(), after.getRightBorder(),
                              "right border of station " + i);
      Assertions.assertEquals(i == 1 && horizontal ? -92 : before.getUpBorder(),
                              after.getUpBorder(), "upper border of station " + i);
      Assertions.assertEquals(before.getDownBorder(), after.getDownBorder(),
                              "lower border of station " + i);
      Assertions.assertEquals(i == 1 ? Collections.singleton(wide) : Collections.emptySet(),
                              guide.restrictionsAt(i), "restrictions at station " + i);
    }
    Assertions.assertTrue(guide.restrictionsAt(-1).isEmpty());
    Assertions.assertTrue(guide.restrictionsAt(guide.boxes().size()).isEmpty());
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
