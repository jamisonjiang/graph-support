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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.graphper.layout.dot.RootCrossRankFlatCrossingTest.Fixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The swap delta has to be the exact derivative of the total that a recount produces, otherwise the
 * running total drifts away from the arrangement it is supposed to describe - and drifts below zero,
 * which {@code MinCross} then reads as the best arrangement it has ever seen.
 *
 * <p>The total pairs up the <em>outgoing</em> edges of two nodes that share a rank, so a term of it
 * is identified by the two distinct tails it comes from. Every case below is one way for the delta
 * to disagree with that.
 */
class RootCrossRankCrossNumConsistencyTest {

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void symmetricSpanPredicateDoesNotExpandTheGlobalPairDomain(boolean flatReversed)
      throws Exception {
    Fixture f = new Fixture();
    DNode above = f.node("above", 1);
    DNode a = f.node("a", 2);
    DNode b = f.node("b", 2);
    DNode c = f.node("c", 2);
    f.edge(flatReversed ? c : a, flatReversed ? a : c);
    f.edge(above, b);
    Method predicate = RootCrossRank.class.getDeclaredMethod("isCross", DLine.class,
        DLine.class, boolean.class);
    predicate.setAccessible(true);
    for (boolean indexed : new boolean[]{false, true}) {
      Assertions.assertEquals(true, predicate.invoke(f.root, f.edges.get(0), f.edges.get(1),
          indexed));
      Assertions.assertEquals(true, predicate.invoke(f.root, f.edges.get(1), f.edges.get(0),
          indexed));
    }
    // The interval predicate is symmetric, but these tails belong to different ranks. A future
    // two-sided objective must change enumeration, ownership and deltas together, not this alone.
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertDeltas();
    f.assertCacheConsistent("flat/incoming domain");
  }

  /**
   * A flat edge ending at one node, against an edge from the rank above ending at the other. The
   * two tails are on different ranks, so the total has no such term, but the delta used to score it
   * - and to book it against the rank above, which then went negative.
   *
   * <p>rank r-1: {@code x}; rank r: {@code a b c} with flat {@code a-c}; rank r+1: {@code y}.
   * Edges {@code x->b} and {@code c->y}.
   */
  @ParameterizedTest
  @CsvSource({"1,false", "1,true", "2,false", "2,true", "3,false", "3,true"})
  void edgeFromAboveUnderAFlatEdgeIsNotATerm(int flatRank, boolean flatReversed) throws Exception {
    Fixture f = new Fixture();
    DNode above = flatRank > 1 ? f.node("x", flatRank - 1) : null;
    DNode a = f.node("a", flatRank);
    DNode b = f.node("b", flatRank);
    DNode c = f.node("c", flatRank);
    DNode below = flatRank < 3 ? f.node("y", flatRank + 1) : null;
    f.edge(flatReversed ? c : a, flatReversed ? a : c);
    if (above != null) {
      f.edge(above, b);
    }
    if (below != null) {
      f.edge(c, below);
    }

    f.order(flatRank, a, b, c);
    Assertions.assertEquals(0, f.oracle(), "b sits under the flat edge but only carries an edge "
        + "from above, and the total never pairs edges whose tails are on different ranks");
    f.assertCacheConsistent("before");
    f.assertDeltas();

    int[] keep = new int[3];
    int[] swapped = new int[3];
    f.crossing(b, c, keep);
    f.crossing(c, b, swapped);
    Assertions.assertArrayEquals(new int[]{0, 0, 0}, keep, "nothing to score for b before c");
    Assertions.assertArrayEquals(new int[]{0, 0, 0}, swapped, "nor for c before b");

    f.root.transpose(false);
    f.assertCacheConsistent("after transpose");
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
  }

  /**
   * The opposite direction: a flat edge ending at one node, against an edge to the rank below
   * leaving the other. That <em>is</em> a term - both tails are on the flat edge's rank - and the
   * delta used to miss it, so a swap that pulls a node out from under a flat edge scored zero.
   *
   * <p>rank r: {@code a b c} with flat {@code a-c}; {@code b} carries an edge to the rank below.
   */
  @ParameterizedTest
  @CsvSource({"1,false", "1,true", "2,false", "2,true"})
  void nodeLeavingAFlatEdgeSpanIsScored(int flatRank, boolean flatReversed) throws Exception {
    Fixture f = new Fixture();
    if (flatRank > 1) {
      f.node("top", flatRank - 1);
    }
    DNode a = f.node("a", flatRank);
    DNode b = f.node("b", flatRank);
    DNode c = f.node("c", flatRank);
    DNode below = f.node("x", flatRank + 1);
    f.edge(flatReversed ? c : a, flatReversed ? a : c);
    f.edge(b, below);

    f.order(flatRank, a, b, c);
    Assertions.assertEquals(1, f.oracle(), "b is under the flat edge and leaves the rank");
    f.assertCacheConsistent("before");
    f.assertDeltas();

    // The head swap. The tail swap is covered by
    // RootCrossRankFlatCrossingTest#mixedAndNonflatSwapContributionsRemainUnchanged.
    int[] keep = new int[3];
    int[] swapped = new int[3];
    f.crossing(b, c, keep);
    f.crossing(c, b, swapped);
    Assertions.assertEquals(1, keep[2] - swapped[2],
        "swapping b past the flat edge's head removes the crossing");
    Assertions.assertEquals(0, keep[0], "the term belongs to the flat edge's own rank");

    f.root.transpose(false);
    Assertions.assertEquals(0, f.root.crossSnapshot().getCrossNum());
    f.assertCacheConsistent("after transpose");
  }

  /**
   * Two edges that leave the same node cannot be a term - the total only pairs edges from distinct
   * tails - yet as incoming edges of two nodes on the next rank they used to be scored, which took
   * the rank above negative on its own.
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void twoEdgesOutOfOneNodeAreNotATerm(int fanRank) throws Exception {
    Fixture f = new Fixture();
    DNode tail = f.node("tail", fanRank);
    DNode other = f.node("other", fanRank);
    DNode left = f.node("left", fanRank + 1);
    DNode right = f.node("right", fanRank + 1);
    f.edge(tail, right);
    f.edge(tail, left);
    f.edge(other, left);

    f.order(fanRank, tail, other);
    f.order(fanRank + 1, left, right);
    f.assertCacheConsistent("before");
    f.assertDeltas();

    int[] keep = new int[3];
    int[] swapped = new int[3];
    f.crossing(left, right, keep);
    f.crossing(right, left, swapped);
    Assertions.assertEquals(f.oracleDelta(left, right), keep[2] - swapped[2],
        "the shared tail must not contribute to either order");

    f.root.transpose(false);
    f.assertCacheConsistent("after transpose");
  }

  /**
   * Flat edges on the first, a middle and the last rank, every arrangement of the rank they live on,
   * and every adjacent swap of it in both directions. Checks that the delta matches an independent
   * recount for each swap, and that the cache still describes the arrangement afterwards.
   */
  @ParameterizedTest
  @CsvSource({
      "1,false,false", "1,false,true", "1,true,false", "1,true,true",
      "2,false,false", "2,false,true", "2,true,false", "2,true,true",
      "3,false,false", "3,false,true", "3,true,false", "3,true,true",
  })
  void everyArrangementOfAFlatRankKeepsTheCacheExact(int flatRank, boolean flatReversed,
                                                     boolean reverse) throws Exception {
    Fixture f = new Fixture();
    DNode above = flatRank > 1 ? f.node("above", flatRank - 1) : null;
    DNode p = f.node("p", flatRank);
    DNode q = f.node("q", flatRank);
    DNode r = f.node("r", flatRank);
    DNode s = f.node("s", flatRank);
    DNode below = flatRank < 3 ? f.node("below", flatRank + 1) : null;
    DNode below2 = flatRank < 3 ? f.node("below2", flatRank + 1) : null;

    // A wide flat edge, in both orientations, plus a second flat edge for flat/flat pairs.
    f.edge(flatReversed ? r : p, flatReversed ? p : r);
    f.edge(q, s);
    if (above != null) {
      // Two edges out of one node, so the shared-tail case is exercised as well.
      f.edge(above, q);
      f.edge(above, s);
    }
    if (below != null) {
      f.edge(q, below);
      f.edge(s, below2);
    }
    f.root.resetToRoot();

    DNode[] nodes = {p, q, r, s};
    for (List<DNode> order : permutations(nodes)) {
      f.order(flatRank, order.toArray(new DNode[0]));
      String where = order.toString();
      f.assertCacheConsistent("fresh " + where);
      f.assertDeltas();
      f.root.transpose(reverse);
      f.assertCacheConsistent("after transpose " + where);
      f.root.transposeFlatPairs();
      f.assertCacheConsistent("after transposeFlatPairs " + where);
      f.root.contractFlatSpans();
      f.assertCacheConsistent("after contractFlatSpans " + where);
    }
  }

  /**
   * Random small graphs, ranks and flat edges included, driven through the same passes MinCross
   * uses. Nothing here inspects a specific number - the point is that a graph nobody wrote by hand
   * cannot take the cache away from the arrangement either.
   */
  @Test
  void randomGraphsKeepTheCacheExact() throws Exception {
    Random random = new Random(20240607L);
    for (int graph = 0; graph < 120; graph++) {
      int ranks = 2 + random.nextInt(3);
      int width = 2 + random.nextInt(3);
      Fixture f = new Fixture();
      List<List<DNode>> byRank = new ArrayList<>();
      for (int rank = 1; rank <= ranks; rank++) {
        List<DNode> row = new ArrayList<>();
        for (int i = 0; i < width; i++) {
          row.add(f.node("n" + rank + "_" + i, rank));
        }
        byRank.add(row);
      }
      for (int rank = 0; rank < ranks; rank++) {
        List<DNode> row = byRank.get(rank);
        for (int i = 0; i < width; i++) {
          if (rank + 1 < ranks && random.nextInt(100) < 60) {
            f.edge(row.get(i), byRank.get(rank + 1).get(random.nextInt(width)));
          }
          // Flat edges, both orientations, sometimes wide enough to span another node.
          if (random.nextInt(100) < 35) {
            int other = random.nextInt(width);
            if (other != i) {
              f.edge(random.nextBoolean() ? row.get(i) : row.get(other),
                  random.nextBoolean() ? row.get(other) : row.get(i));
            }
          }
        }
      }
      f.root.resetToRoot();

      String where = "graph " + graph;
      f.assertCacheConsistent(where + " fresh");
      f.assertDeltas();
      for (int pass = 0; pass < 4; pass++) {
        f.root.vmedian(pass);
        f.assertCacheConsistent(where + " after vmedian " + pass);
        f.root.transpose(pass % 4 >= 2);
        f.assertCacheConsistent(where + " after transpose " + pass);
        f.root.transposeFlatPairs();
        f.assertCacheConsistent(where + " after transposeFlatPairs " + pass);
        f.root.contractFlatSpans();
        f.assertCacheConsistent(where + " after contractFlatSpans " + pass);
      }
    }
  }

  /**
   * A flat edge whose ends are driven apart, which adjacent transposition cannot repair because it
   * can only move the nodes at the border of the span. Contracting the span has to lower the count,
   * never raise it, and has to leave the cache exact.
   */
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void contractingAWideFlatEdgeLowersTheCount(boolean flatReversed) throws Exception {
    Fixture f = new Fixture();
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    DNode e = f.node("e", 1);
    DNode x = f.node("x", 2);
    DNode y = f.node("y", 2);
    // a and e are the ends of one flat edge, and both b and d leave the rank from under it.
    f.edge(flatReversed ? e : a, flatReversed ? a : e);
    f.edge(b, x);
    f.edge(d, y);
    f.root.resetToRoot();

    f.order(1, a, b, c, d, e);
    Assertions.assertEquals(2, f.oracle(), "b and d are both under the flat edge");
    f.assertCacheConsistent("before");

    f.root.transpose(false);
    int afterTranspose = f.root.crossSnapshot().getCrossNum();
    f.assertCacheConsistent("after transpose");

    f.root.transposeFlatPairs();
    f.root.contractFlatSpans();
    int afterFlatPairs = f.root.crossSnapshot().getCrossNum();
    f.assertCacheConsistent("after transposeFlatPairs");
    Assertions.assertTrue(afterFlatPairs <= afterTranspose,
        "contracting a flat span may only lower the count, was " + afterTranspose + " -> "
            + afterFlatPairs);
    Assertions.assertEquals(0, afterFlatPairs, "nothing that leaves the rank stays under the span");
    // c carries no edge at all, so leaving it under the span costs nothing and the pass, which only
    // accepts a strictly lower count, has no reason to move it. b and d do have to come out.
    int low = Math.min(f.root.getRankIndex(a), f.root.getRankIndex(e));
    int high = Math.max(f.root.getRankIndex(a), f.root.getRankIndex(e));
    for (DNode under : new DNode[]{b, d}) {
      int idx = f.root.getRankIndex(under);
      Assertions.assertFalse(low < idx && idx < high,
          under.name() + " leaves the rank and must not stay under the flat edge");
    }

    List<DNode> settled = new ArrayList<>(f.root.getNodes(1));
    f.root.transposeFlatPairs();
    f.root.contractFlatSpans();
    Assertions.assertEquals(settled, f.root.getNodes(1), "a zero gain pass must not reorder");
    f.assertCacheConsistent("after a second transposeFlatPairs");
  }

  private static List<List<DNode>> permutations(DNode[] nodes) {
    List<List<DNode>> out = new ArrayList<>();
    permute(new ArrayList<>(Arrays.asList(nodes)), 0, out);
    return out;
  }

  private static void permute(List<DNode> nodes, int from, List<List<DNode>> out) {
    if (from == nodes.size() - 1) {
      out.add(new ArrayList<>(nodes));
      return;
    }
    for (int i = from; i < nodes.size(); i++) {
      java.util.Collections.swap(nodes, from, i);
      permute(nodes, from + 1, out);
      java.util.Collections.swap(nodes, from, i);
    }
  }
}
