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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.dot.RootCrossRankFlatCrossingTest.Fixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RootCrossRankFlatSpanBudgetTest {

  @ParameterizedTest
  @ValueSource(ints = {34, 120})
  void trialAndWorkBudgetsStopBeforeStartingAnotherTrial(int parallelSpans) throws Exception {
    CountingRank root = new CountingRank(Layout.DOT);
    Fixture f = new Fixture(root);
    DNode a = f.node("a", 1);
    f.node("b", 1);
    DNode c = f.node("c", 1);
    for (int i = 0; i < parallelSpans; i++) {
      f.edge(a, c);
    }
    addUnrelatedCrossing(f);
    root.resetToRoot();
    List<DNode> original = new ArrayList<>(root.getNodes(1));
    // Every walk ties locally at zero and must restore. 34 spans hit the 32-trial cap; 120 spans
    // hit the 250,000 work cap after four trials (4 * (3 + 1 + 120)^2 per trial).
    int trials = parallelSpans == 34 ? 32 : 4;
    f.assertCacheConsistent("before budget");
    for (int repeat = 0; repeat < 2; repeat++) {
      root.restores = 0;
      root.contractFlatSpans();
      Assertions.assertEquals(2 * trials, root.restores);
      Assertions.assertEquals(original, root.getNodes(1));
      for (DNode node : original) {
        Assertions.assertEquals(root.getRankIndex(node), node.getRankIndex());
      }
      f.assertCacheConsistent("after budget " + repeat);
    }
  }

  @Test
  void trialBudgetIsSharedAcrossRanks() throws Exception {
    CountingRank root = new CountingRank(Layout.DOT);
    Fixture f = new Fixture(root);
    for (int rank : new int[]{1, 3}) {
      DNode a = f.node("a" + rank, rank);
      f.node("b" + rank, rank);
      DNode c = f.node("c" + rank, rank);
      for (int i = 0; i < 20; i++) {
        f.edge(a, c);
      }
    }
    addUnrelatedCrossing(f);
    root.resetToRoot();
    root.contractFlatSpans();
    Assertions.assertEquals(64, root.restores, "not 32 trials per rank");
    f.assertCacheConsistent("shared budget");
  }

  @Test
  void oversizedRankIsSkippedWithoutMovingOrRestoring() throws Exception {
    CountingRank root = new CountingRank(Layout.DOT);
    Fixture f = new Fixture(root);
    List<DNode> nodes = new ArrayList<>();
    for (int i = 0; i < 260; i++) {
      nodes.add(f.node("n" + i, 1));
    }
    DNode below = f.node("below", 2);
    f.edge(nodes.get(0), nodes.get(259));
    f.edge(nodes.get(130), below);
    root.resetToRoot();
    Assertions.assertEquals(1, root.crossSnapshot().getCrossNum());
    root.contractFlatSpans();
    Assertions.assertEquals(0, root.restores);
    Assertions.assertEquals(nodes, root.getNodes(1));
    f.assertCacheConsistent("oversized rank");
  }

  @ParameterizedTest
  @ValueSource(ints = {20, 36})
  void acceptedMovesSpendTrialBudgetOnlyOnEligibleSpans(int gadgets) throws Exception {
    String settled = null;
    for (int repeat = 0; repeat < 2; repeat++) {
      Fixture f = new Fixture();
      List<DNode> starts = new ArrayList<>();
      List<DNode> ends = new ArrayList<>();
      for (int i = 0; i < gadgets; i++) {
        int rank = 3 * i + 1;
        DNode a = f.node("a" + i, rank);
        DNode b = f.node("b" + i, rank);
        DNode c = f.node("c" + i, rank);
        DNode below = f.node("below" + i, rank + 1);
        f.edge(a, c);
        f.edge(b, below);
        starts.add(a);
        ends.add(c);
      }
      f.root.resetToRoot();
      Assertions.assertEquals(gadgets, f.root.crossSnapshot().getCrossNum());
      f.root.contractFlatSpans();
      // Pass two's now-adjacent spans are free: all 20 finish, but 36 exhaust 32 useful trials.
      Assertions.assertEquals(gadgets == 20 ? 0 : 4, f.root.crossSnapshot().getCrossNum());
      for (int i = 0; i < gadgets; i++) {
        Assertions.assertEquals(i < 32 ? 1 : 2,
            Math.abs(f.root.getRankIndex(starts.get(i)) - f.root.getRankIndex(ends.get(i))),
            "useful trial at gadget " + i);
      }
      f.assertCacheConsistent("accepted moves with eligible-span budget");
      if (settled != null) {
        Assertions.assertEquals(settled, f.root.toString(), "deterministic bounded prefix");
      }
      settled = f.root.toString();
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void quickModeKeepsCheapPairsButSkipsWideSpans(boolean quick) throws Exception {
    CountingRank root = new CountingRank(quick ? Layout.DOTQ : Layout.DOT);
    Fixture f = new Fixture(root);
    DNode a = f.node("a", 1);
    DNode b = f.node("b", 1);
    DNode c = f.node("c", 1);
    DNode d = f.node("d", 1);
    DNode e = f.node("e", 1);
    DNode below = f.node("below", 2);
    f.edge(a, e);
    f.edge(c, below);
    root.resetToRoot();
    List<DNode> original = new ArrayList<>(root.getNodes(1));
    root.transposeFlatPairs();
    Assertions.assertEquals(original, root.getNodes(1), "no adjacent flat pair exists");
    root.contractFlatSpans();
    Assertions.assertEquals(quick ? 1 : 0, root.crossSnapshot().getCrossNum());
    if (quick) {
      Assertions.assertEquals(original, root.getNodes(1));
    }
    f.assertCacheConsistent("wide span");

    // An adjacent flat leaf can still travel with its neighbour in either mode.
    f.edge(a, b);
    f.edge(c, d);
    f.order(1, a, b, c, e, d);
    int before = root.crossSnapshot().getCrossNum();
    root.transposeFlatPairs();
    Assertions.assertTrue(root.crossSnapshot().getCrossNum() < before);
    f.assertCacheConsistent("cheap pair");
  }

  @Test
  void passBudgetRemainsSmallAndExplicit() throws Exception {
    Field passes = RootCrossRank.class.getDeclaredField("FLAT_SPAN_MAX_PASSES");
    passes.setAccessible(true);
    Assertions.assertEquals(2, passes.getInt(null));
  }

  private static void addUnrelatedCrossing(Fixture f) {
    DNode a = f.node("fixed-a", 10);
    DNode b = f.node("fixed-b", 10);
    DNode c = f.node("fixed-c", 11);
    DNode d = f.node("fixed-d", 11);
    f.edge(a, d);
    f.edge(b, c);
  }

  private static class CountingRank extends RootCrossRank {
    int restores;

    CountingRank(Layout layout) {
      super(new DrawGraph(Graphviz.digraph().layout(layout).rankdir(Rankdir.LR).build()),
          new MinCross.ClusterMerge());
    }

    @Override
    public void sort(int rank, Comparator<DNode> comparator, boolean sync) {
      restores++;
      super.sort(rank, comparator, sync);
    }
  }
}
