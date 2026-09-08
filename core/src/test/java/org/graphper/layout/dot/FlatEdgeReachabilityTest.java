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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rank;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.dot.RootCrossRankFlatCrossingTest.Fixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Two defects that both let a real flat edge go unnoticed.
 *
 * <ol>
 *   <li>Flatness used to be read off {@code minlen}. A same-rank constraint puts both ends of an
 *   ordinary edge on one rank while leaving the default {@code minlen} of {@code 1} in place, and
 *   every such edge scored zero crossings even though {@code MinCross} was already ordering the
 *   rank by it.</li>
 *   <li>The pass that moves a flat leaf together with its neighbour was unreachable for graphs
 *   without clusters, so those graphs never got that improvement.</li>
 * </ol>
 *
 * <p>The full-layout cases here deliberately go through the public layout API and judge the result
 * by an oracle over the final coordinates ({@link #crossings(DrawGraph)}), so nothing about the
 * internal representation is assumed.
 */
class FlatEdgeReachabilityTest {

  // -------------------------------------------------------------------------------------------
  // Defect 1: flatness is rank membership, not minlen
  // -------------------------------------------------------------------------------------------

  /**
   * Two same-rank edges laid out alternately, {@code a-c} over {@code b-d}, with the
   * <em>default</em> {@code minlen}. This is what a same-rank constraint produces, and it used to
   * be scored as zero crossings, which left transposition with nothing to improve.
   */
  @ParameterizedTest
  @ValueSource(strings = {"default", "explicit-one", "explicit-zero"})
  void sameRankEdgesAreCountedForEveryMinlen(String minlenMode) throws Exception {
    Integer minlen = "default".equals(minlenMode) ? null
        : ("explicit-one".equals(minlenMode) ? 1 : 0);
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    DLine ac = edge(f, a, c, minlen);
    DLine bd = edge(f, b, d, minlen);

    Method isCross = RootCrossRank.class.getDeclaredMethod("isCross", DLine.class, DLine.class,
        boolean.class);
    isCross.setAccessible(true);
    Assertions.assertTrue((boolean) isCross.invoke(f.root, ac, bd, false),
        "a-c and b-d interleave on one rank, whatever minlen says");
    Assertions.assertEquals(1, f.oracle());
    Assertions.assertEquals(1, f.root.crossSnapshot().getCrossNum());
    f.assertCacheConsistent("alternating same-rank edges, minlen " + minlenMode);
    f.assertDeltas();

    // Counting it is what makes the improvement visible to transposition.
    f.root.transpose(false);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCacheConsistent("after transpose, minlen " + minlenMode);
    // The two intervals end up disjoint or nested, never interleaved, which is what a crossing is.
    List<DNode> order = f.root.getNodes(1);
    int acLow = Math.min(order.indexOf(a), order.indexOf(c));
    int acHigh = Math.max(order.indexOf(a), order.indexOf(c));
    int bdLow = Math.min(order.indexOf(b), order.indexOf(d));
    int bdHigh = Math.max(order.indexOf(b), order.indexOf(d));
    Assertions.assertEquals(acLow < bdLow && bdLow < acHigh, acLow < bdHigh && bdHigh < acHigh,
        "a-c and b-d must not interleave any more: " + order);
  }

  /**
   * A replacement edge for a collapsed cluster carries no draw property and still must not count,
   * even though its ends share a rank. Widening flatness to rank membership must not widen this.
   */
  @Test
  void collapsedClusterProxyEdgesStillDoNotCount() throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    DLine proxy1 = new DLine(a, c, null, 1, 0);
    DLine proxy2 = new DLine(b, d, null, 1, 0);
    f.root.addEdge(proxy1);
    f.root.addEdge(proxy2);
    f.edges.add(proxy1);
    f.edges.add(proxy2);
    Assertions.assertTrue(proxy1.isSameRank());
    Assertions.assertEquals(0, f.oracle());
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCacheConsistent("proxy edges only");
    f.assertDeltas();
  }

  /**
   * Full layout, public API, {@code Rank.SAME} and nothing but the default {@code minlen}.
   *
   * <p>One rank of {@code width} members. The last member is a hub with a same-rank edge to two of
   * them, {@code carrier} and {@code span}, and {@code carrier} additionally carries the only edge
   * that leaves the rank. Whenever {@code carrier} ends up strictly between the hub and
   * {@code span}, that edge crosses the {@code hub-span} one.
   *
   * <p>Measured before the fix, every case with {@code carrier < span} came out exactly that way -
   * ten of the twenty index combinations for widths four to six, e.g. width four as
   * {@code n0 n3 n1 n2}, where the hub {@code n3} is at index 1, {@code carrier} at 2 and
   * {@code span} at 3. The interval was scored as zero, so no pass had a reason to move anything.
   * After the fix all twenty come out clean, e.g. {@code n0 n3 n2 n1}.
   *
   * <p>Node ids are uniform on purpose: {@link Node#hashCode()} is the id's hash, so an id set
   * feeds into hashed iteration and thereby into the arrangement the passes start from. The
   * before/after measurement above reproduced identically for seven different id prefixes.
   */
  @ParameterizedTest
  @ValueSource(ints = {4, 5, 6})
  void rankSameWithDefaultMinlenNoLongerLeavesACrossing(int width) throws Exception {
    for (int carrierIdx = 1; carrierIdx < width - 1; carrierIdx++) {
      for (int spanIdx = 1; spanIdx < width - 1; spanIdx++) {
        if (carrierIdx == spanIdx) {
          continue;
        }
        Node[] members = new Node[width];
        for (int i = 0; i < width; i++) {
          members[i] = Node.builder().id("n" + i).label("n" + i).build();
        }
        Node hub = members[width - 1];
        Node carrier = members[carrierIdx];
        Node span = members[spanIdx];
        Node below = Node.builder().id("nDown").label("nDown").build();

        Subgraph.SubgraphBuilder same = Subgraph.builder().rank(Rank.SAME);
        for (Node member : members) {
          same.addNode(member);
        }
        Graphviz graphviz = Graphviz.digraph().subgraph(same.build())
            // Every line keeps the default minlen of 1 - the constraint is what makes them flat.
            .addLine(Line.builder(hub, carrier).build())
            .addLine(Line.builder(hub, span).build())
            .addLine(Line.builder(carrier, below).build())
            .build();

        DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graphviz);
        String where = "width " + width + " carrier n" + carrierIdx + " span n" + spanIdx
            + ", order was " + order(draw);
        Map<Node, NodeDrawProp> props = props(draw);
        for (Node member : members) {
          Assertions.assertEquals(props.get(hub).getY(), props.get(member).getY(), 0.01,
              "the same-rank constraint has to keep every member on one rank, " + where);
        }
        Assertions.assertNotEquals(props.get(hub).getY(), props.get(below).getY(), 0.01, where);

        double lo = Math.min(props.get(hub).getX(), props.get(span).getX());
        double hi = Math.max(props.get(hub).getX(), props.get(span).getX());
        double carrierX = props.get(carrier).getX();
        Assertions.assertFalse(lo < carrierX && carrierX < hi,
            "carrier leaves the rank and must not sit under the hub-span flat edge, " + where);
        Assertions.assertEquals(0, crossings(draw), where);
      }
    }
  }

  // -------------------------------------------------------------------------------------------
  // Defect 2: the flat pair pass has to be reachable without clusters
  // -------------------------------------------------------------------------------------------

  /**
   * The pass used to require the flag that only cluster expansion sets, so a graph that never has a
   * cluster to expand could not run it at all. Nothing in this fixture is a cluster.
   */
  @Test
  void flatPairPassRunsWithoutAnyClusterMerge() throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    DNode e = f.node("e", 1);
    edge(f, a, b, 0);
    edge(f, a, d, 0);
    edge(f, c, e, 0);

    RootCrossRank clusterFree = new RootCrossRank(f.draw, f.root.getDigraphProxy());
    Assertions.assertEquals(1, clusterFree.crossSnapshot().getCrossNum());
    // No resetToRoot, exactly as MinCross leaves a cluster-free graph.
    clusterFree.transposeFlatPairs();
    Assertions.assertEquals(0, clusterFree.crossSnapshot().getCrossNum(),
        "with no cluster to expand the pass still has to run");
    clusterFree.setCacheExpired();
    Assertions.assertEquals(0, clusterFree.crossSnapshot().getCrossNum());
  }

  /**
   * Full layout, public API, no cluster anywhere.
   *
   * <p>{@code leaf} hangs off {@code mid} by a flat edge and is its only neighbour, and
   * {@code mid-tip} is a second flat edge out of {@code mid}. {@code root} spans the rank to
   * {@code far}, and {@code mid} carries the only edge that leaves the rank.
   *
   * <p>Measured before the fix, with the pass unreachable: order {@code idle root mid leaf far
   * other}, where {@code mid} sits inside the {@code root-far} interval, one crossing. Adjacent
   * transposition cannot repair it - moving {@code mid} alone out of the interval drags it away
   * from {@code leaf}. Moving the intact {@code mid,leaf} pair past {@code far} does, and that is
   * the move only this pass makes: order {@code idle root far mid leaf other}, zero crossings.
   */
  @Test
  void clusterFreeGraphGetsTheFlatPairImprovement() throws Exception {
    Node idle = Node.builder().id("idle").label("idle").build();
    Node other = Node.builder().id("other").label("other").build();
    Node root = Node.builder().id("root").label("root").build();
    Node far = Node.builder().id("far").label("far").build();
    Node mid = Node.builder().id("mid").label("mid").build();
    Node leaf = Node.builder().id("leaf").label("leaf").build();
    Node down = Node.builder().id("down").label("down").build();
    Graphviz graphviz = Graphviz.digraph()
        .subgraph(Subgraph.builder().rank(Rank.SAME)
                      .addNode(idle, other, root, far, mid, leaf).build())
        .addLine(Line.builder(root, mid).minlen(0).build())
        .addLine(Line.builder(root, far).minlen(0).build())
        .addLine(Line.builder(mid, leaf).minlen(0).build())
        .addLine(Line.builder(mid, down).build())
        .build();

    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graphviz);
    Assertions.assertTrue(draw.clusters().isEmpty(), "the fixture must stay cluster free");
    Map<Node, NodeDrawProp> props = props(draw);
    double lo = Math.min(props.get(root).getX(), props.get(far).getX());
    double hi = Math.max(props.get(root).getX(), props.get(far).getX());
    double midX = props.get(mid).getX();
    Assertions.assertFalse(lo < midX && midX < hi,
        "mid leaves the rank and must not stay under the root-far flat edge, order was "
            + order(draw));
    Assertions.assertEquals(1,
        Math.abs(rankIndex(draw, mid) - rankIndex(draw, leaf)),
        "the flat leaf travels with its neighbour, order was " + order(draw));
    Assertions.assertEquals(0, crossings(draw), "order was " + order(draw));
  }

  // -------------------------------------------------------------------------------------------
  // helpers
  // -------------------------------------------------------------------------------------------

  private static DLine edge(Fixture f, DNode from, DNode to, Integer minlen) {
    Line.LineBuilder builder = Line.builder(from.getNode(), to.getNode());
    if (minlen != null) {
      builder.minlen(minlen);
    }
    Line line = builder.build();
    DLine edge = new DLine(from, to, new LineDrawProp(line, line.lineAttrs(), f.draw), 1, 0);
    f.root.addEdge(edge);
    f.edges.add(edge);
    return edge;
  }

  private static Map<Node, NodeDrawProp> props(DrawGraph draw) {
    Map<Node, NodeDrawProp> out = new LinkedHashMap<>();
    draw.nodes().forEach(n -> out.put(n.getNode(), n));
    return out;
  }

  /** Index of a node inside its rank, ranks being the nodes that share a {@code y}. */
  private static int rankIndex(DrawGraph draw, Node node) {
    NodeDrawProp target = props(draw).get(node);
    int idx = 0;
    for (NodeDrawProp n : props(draw).values()) {
      if (Math.abs(n.getY() - target.getY()) < 0.01 && n.getX() < target.getX()) {
        idx++;
      }
    }
    return idx;
  }

  /** Ranks in {@code y} order, each rank in {@code x} order. */
  private static String order(DrawGraph draw) {
    List<NodeDrawProp> nodes = new ArrayList<>(props(draw).values());
    nodes.sort(Comparator.<NodeDrawProp>comparingDouble(NodeDrawProp::getY)
                   .thenComparingDouble(NodeDrawProp::getX));
    StringBuilder sb = new StringBuilder();
    double lastY = Double.NaN;
    for (NodeDrawProp n : nodes) {
      if (Math.abs(n.getY() - lastY) > 0.01) {
        sb.append("| ");
        lastY = n.getY();
      }
      sb.append(n.getNode().nodeAttrs().getId()).append(' ');
    }
    return sb.toString();
  }

  /**
   * Crossing oracle over the laid-out coordinates only, following the same contract the internal
   * count follows: a term is a pair of edges whose tails are distinct and share a rank. A pair that
   * shares an endpoint is routed, not crossed. A flat edge crosses another edge when exactly one of
   * that edge's endpoints on the flat edge's rank lies strictly inside its interval.
   */
  private static int crossings(DrawGraph draw) {
    List<LineDrawProp> lines = new ArrayList<>();
    draw.lines().forEach(lines::add);
    Map<Node, NodeDrawProp> props = props(draw);
    int count = 0;
    for (int i = 0; i < lines.size(); i++) {
      for (int j = i + 1; j < lines.size(); j++) {
        if (cross(props, lines.get(i), lines.get(j))) {
          count++;
        }
      }
    }
    return count;
  }

  private static boolean cross(Map<Node, NodeDrawProp> props, LineDrawProp l1, LineDrawProp l2) {
    Node t1 = l1.getLine().tail();
    Node h1 = l1.getLine().head();
    Node t2 = l2.getLine().tail();
    Node h2 = l2.getLine().head();
    if (t1 == t2 || t1 == h2 || h1 == t2 || h1 == h2) {
      return false;
    }
    NodeDrawProp pt1 = props.get(t1);
    NodeDrawProp ph1 = props.get(h1);
    NodeDrawProp pt2 = props.get(t2);
    NodeDrawProp ph2 = props.get(h2);
    boolean flat1 = sameRank(pt1, ph1);
    boolean flat2 = sameRank(pt2, ph2);
    if (!flat1 && !flat2) {
      if (!sameRank(pt1, pt2) || !sameRank(ph1, ph2)) {
        return false;
      }
      return pt1.getX() < pt2.getX() != ph1.getX() < ph2.getX();
    }
    NodeDrawProp flatTail = flat1 ? pt1 : pt2;
    NodeDrawProp flatHead = flat1 ? ph1 : ph2;
    NodeDrawProp otherTail = flat1 ? pt2 : pt1;
    NodeDrawProp otherHead = flat1 ? ph2 : ph1;
    if (!sameRank(flatTail, otherTail)) {
      return false;
    }
    double lo = Math.min(flatTail.getX(), flatHead.getX());
    double hi = Math.max(flatTail.getX(), flatHead.getX());
    boolean tailInside = lo < otherTail.getX() && otherTail.getX() < hi;
    boolean headInside = sameRank(flatTail, otherHead)
        && lo < otherHead.getX() && otherHead.getX() < hi;
    return tailInside != headInside;
  }

  private static boolean sameRank(NodeDrawProp left, NodeDrawProp right) {
    return Math.abs(left.getY() - right.getY()) < 0.01;
  }
}
