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
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Rankdir;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RootCrossRankFlatCrossingTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2})
  void fanoutCountsTwoAlternatingPairsOnSingleFirstAndLastRank(int position) throws Exception {
    Fixture f = new Fixture();
    if (position == 2) {
      f.node("previous", 1);
    }
    int rank = position == 2 ? 2 : 1;
    DNode n15 = f.node("15", rank);
    DNode n14 = f.node("14", rank);
    DNode n13 = f.node("13", rank);
    DNode n12 = f.node("12", rank);
    DNode n11 = f.node("11", rank);
    DNode n10 = f.node("10", rank);
    if (position == 1) {
      f.node("next", 2);
    }
    f.edge(n15, n10);
    f.edge(n15, n13);
    f.edge(n14, n11);
    f.edge(n14, n12);
    Assertions.assertEquals(2, f.oracle());
    Assertions.assertEquals(2, f.root.crossSnapshot().getCrossNum());
    f.assertDeltas();
    f.root.transpose(false);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCache();
  }

  @Test
  void allRanksContributeExactlyOnce() throws Exception {
    Fixture f = new Fixture();
    for (int rank = 1; rank <= 3; rank++) {
      DNode a = f.node("a" + rank, rank);
      DNode b = f.node("b" + rank, rank);
      DNode c = f.node("c" + rank, rank);
      DNode d = f.node("d" + rank, rank);
      f.edge(a, c);
      f.edge(b, d);
    }
    Assertions.assertEquals(3, f.oracle());
    Assertions.assertEquals(3, f.root.crossSnapshot().getCrossNum());
    f.assertDeltas();
    f.root.transpose(false);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCache();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11})
  void incomingOutgoingAndMixedDirectionsHaveExactSwapDeltas(int direction) throws Exception {
    Fixture f = new Fixture();
    if (direction >= 8) {
      f.node("previous", 1);
    }
    int rank = direction >= 8 ? 2 : 1;
    DNode a = f.node("a", rank);
    DNode b = f.node("b", rank);
    DNode c = f.node("c", rank);
    DNode d = f.node("d", rank);
    if (direction >= 4 && direction < 8) {
      f.node("next", 2);
    }
    f.edge((direction & 1) == 0 ? a : c, (direction & 1) == 0 ? c : a);
    f.edge((direction & 2) == 0 ? b : d, (direction & 2) == 0 ? d : b);
    Assertions.assertEquals(1, f.oracle());
    Assertions.assertEquals(1, f.root.crossSnapshot().getCrossNum());
    f.assertDeltas();
    f.root.setCacheExpired(rank);
    f.root.transpose(false);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCache();
  }

  @Test
  void sharedEndpointsParallelOpposingAndSameOriginMatchTotalForEveryOrder() throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    f.edge(a, c);
    f.edge(a, c);
    f.edge(c, a);
    f.edge(b, d);
    f.edge(b, c);
    f.edge(c, d);
    f.edge(d, d);
    DNode[] nodes = {a, b, c, d};
    for (DNode first : nodes) {
      for (DNode second : nodes) {
        if (first == second) {
          continue;
        }
        for (DNode third : nodes) {
          if (third == first || third == second) {
            continue;
          }
          List<DNode> order = new ArrayList<>();
          order.add(first);
          order.add(second);
          order.add(third);
          for (DNode last : nodes) {
            if (!order.contains(last)) {
              order.add(last);
            }
          }
          f.root.sort(1, (x, y) -> Integer.compare(order.indexOf(x), order.indexOf(y)), true);
          f.root.setCacheExpired();
          f.assertCache();
          f.assertDeltas();
          f.root.transpose(true);
          f.assertCache();
        }
      }
    }
  }

  @Test
  void trialSnapshotUsesCandidateOrderWithoutMutatingLiveOrderOrCache() throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    f.edge(a, c);
    f.edge(b, d);
    Assertions.assertEquals(1, f.root.crossSnapshot().getCrossNum());
    BasicCrossRank trial = f.root.getBasicCrossRank().clone();
    trial.exchange(b, c, false);
    RootCrossRank.CrossSnapshot candidate = f.root.tryCacheCrossNum(trial);
    Assertions.assertEquals(0, candidate.getCrossNum());
    Assertions.assertEquals(1, f.root.crossSnapshot().getCrossNum());
    Assertions.assertEquals(1, b.getRankIndex());
    f.root.updateCross(candidate);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCache();
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void flatPairTrialsOnLastRankKeepCacheExact(boolean reverse) throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    DNode e = f.node("e", 1);
    f.edge(reverse ? b : a, reverse ? a : b);
    f.edge(a, d);
    f.edge(c, e);
    f.root.resetToRoot();
    Assertions.assertEquals(1, f.root.crossSnapshot().getCrossNum());
    f.root.transposeFlatPairs();
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCache();
    List<DNode> order = new ArrayList<>(f.root.getNodes(1));
    f.root.transposeFlatPairs();
    Assertions.assertEquals(order, f.root.getNodes(1), "zero-cost trial must leave order intact");
    f.assertCache();
  }

  @Test
  void syntheticFlatEdgesRemainExcluded() throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    f.edge(a, c);
    DLine synthetic = new DLine(b, d, null, 1, 0);
    f.root.addEdge(synthetic);
    f.edges.add(synthetic);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertDeltas();
  }

  @Test
  void mixedAndNonflatSwapContributionsRemainUnchanged() throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode x = f.node("x", 2);
    DNode y = f.node("y", 2);
    f.edge(a, c);
    f.edge(a, y);
    f.edge(b, x);
    // One mixed flat/nonflat crossing plus one ordinary inter-rank crossing.
    Assertions.assertEquals(2, f.root.crossSnapshot().getCrossNum());
    Method crossing = RootCrossRank.class.getDeclaredMethod("crossing", DNode.class,
        DNode.class, int[].class);
    crossing.setAccessible(true);
    int[] before = new int[3];
    int[] after = new int[3];
    crossing.invoke(f.root, a, b, before);
    crossing.invoke(f.root, b, a, after);
    Assertions.assertArrayEquals(new int[]{0, 2, 2}, before);
    Assertions.assertArrayEquals(new int[]{0, 0, 0}, after);
    f.root.transpose(false);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCache();
  }

  private static class Fixture {
    final DrawGraph draw = new DrawGraph(Graphviz.digraph().rankdir(Rankdir.LR).build());
    final RootCrossRank root = new RootCrossRank(draw, new MinCross.ClusterMerge());
    final List<DLine> edges = new ArrayList<>();

    DNode node(String id, int rank) {
      Node node = Node.builder().id(id).build();
      DNode result = new DNode(new NodeDrawProp(node, node.nodeAttrs()), 20, 20, 20);
      result.setRank(rank);
      root.addNode(result);
      result.setRankIndex(root.getRankIndex(result));
      return result;
    }

    void edge(DNode from, DNode to) {
      Line line = Line.builder(from.getNode(), to.getNode()).minlen(0).build();
      DLine edge = new DLine(from, to, new LineDrawProp(line, line.lineAttrs(), draw), 1, 0);
      root.addEdge(edge);
      edges.add(edge);
    }

    int oracle() throws Exception {
      Method predicate = RootCrossRank.class.getDeclaredMethod("isCross", DLine.class,
          DLine.class, boolean.class);
      predicate.setAccessible(true);
      int count = 0;
      for (int i = 0; i < edges.size(); i++) {
        for (int j = i + 1; j < edges.size(); j++) {
          DLine a = edges.get(i);
          DLine b = edges.get(j);
          if (a.from() == b.from() || a.from().getRank() != b.from().getRank()) {
            continue;
          }
          boolean ordered = root.getRankIndex(a.from()) < root.getRankIndex(b.from());
          if ((boolean) predicate.invoke(root, ordered ? a : b, ordered ? b : a, false)) {
            count++;
          }
        }
      }
      return count;
    }

    void assertDeltas() throws Exception {
      Method crossing = RootCrossRank.class.getDeclaredMethod("crossing", DNode.class,
          DNode.class, int[].class);
      crossing.setAccessible(true);
      for (int rank = root.minRank(); rank <= root.maxRank(); rank++) {
        for (int i = 0; i + 1 < root.rankSize(rank); i++) {
          DNode a = root.getNode(rank, i);
          DNode b = root.getNode(rank, i + 1);
          int[] before = {99, 99, 99};
          int[] after = {99, 99, 99};
          crossing.invoke(root, a, b, before);
          crossing.invoke(root, b, a, after);
          int original = oracle();
          root.exchange(a, b, true);
          int swapped = oracle();
          root.exchange(a, b, true);
          Assertions.assertEquals(original - swapped, before[2] - after[2],
              "adjacent swap at rank " + rank + " index " + i);
          if (edges.stream().allMatch(DLine::isSameRank)) {
            Assertions.assertEquals(0, before[0], "flat-flat counts belong to their own rank");
            Assertions.assertEquals(before[1], before[2]);
          }
        }
      }
    }

    void assertCache() throws Exception {
      int expected = oracle();
      Assertions.assertEquals(expected, root.crossSnapshot().getCrossNum(), "cached count");
      root.setCacheExpired();
      Assertions.assertEquals(expected, root.crossSnapshot().getCrossNum(), "fresh count");
    }
  }
}
