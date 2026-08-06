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
import org.graphper.api.Cluster;
import org.graphper.api.Cluster.ClusterBuilder;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.Subgraph.SubgraphBuilder;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Randomised sweep over rank assignment, aimed at the {@code minlen=0} family of defects.
 *
 * <p>Hand-written cases turned out to be surprisingly weak here: the crash in
 * {@code NetworkSimplex.tbBalance} needs a conjunction — a node that is movable at all (not on a
 * boundary rank), whose in and out edge weights cancel, with a {@code minlen=0} edge to a neighbour
 * sitting on the outermost rank. Regular shapes such as chains and complete DAGs miss it; tangled
 * random graphs hit it naturally. A structured suite of ~150 deliberately-built cluster, nesting,
 * rank-constraint and self/parallel-edge combinations found nothing, while this generator finds the
 * crash in roughly one run in 250.
 *
 * <p>Measured power on the pre-fix code: 16 of the first 4000 seeds threw
 * {@code NullPointerException: Cannot read field "next" because "preMaxNode" is null}; after the fix,
 * 0 of 4000. That whole range is the default here — it costs a few seconds. Widen it with
 * {@code -Dfuzz.seeds=...} when touching rank code.
 */
public class RankAssignmentFuzzTest {

  private static final Rankdir[] DIRS = {Rankdir.TB, Rankdir.LR, Rankdir.BT, Rankdir.RL};

  /** Weighted so that zero — the value that breaks the {@code limit >= 1} assumption — is common. */
  private static final int[] MINLENS = {0, 0, 0, 1, 1, 2, 5};

  private static final Rank[] RANKS = {Rank.SAME, Rank.MIN, Rank.MAX, Rank.SOURCE, Rank.SINK};

  @Test
  public void randomGraphsWithMixedMinlenAlwaysLayOut() {
    int seeds = Integer.getInteger("fuzz.seeds", 4000);
    Map<String, String> failures = new LinkedHashMap<>();

    for (int seed = 0; seed < seeds; seed++) {
      StringBuilder desc = new StringBuilder();
      try {
        layout(new Random(seed), desc);
      } catch (Throwable t) {
        failures.put("seed=" + seed, t + " | " + desc);
      }
    }

    Assertions.assertTrue(failures.isEmpty(),
                          () -> "Layout failed for " + failures.size() + " of " + seeds
                              + " random graphs:\n" + String.join("\n", failures.values()));
  }

  private void layout(Random rnd, StringBuilder sb) throws Exception {
    int nodeCount = 3 + rnd.nextInt(10);
    List<Node> nodes = new ArrayList<>();
    for (int i = 0; i < nodeCount; i++) {
      nodes.add(Node.builder().id("n" + i).label("n" + i).build());
    }

    GraphvizBuilder gb = Graphviz.digraph().rankdir(DIRS[rnd.nextInt(DIRS.length)]);

    List<Node> outer = new ArrayList<>();
    List<Node> first = new ArrayList<>();
    List<Node> second = new ArrayList<>();
    for (Node n : nodes) {
      int bucket = rnd.nextInt(4);
      if (bucket == 0) {
        outer.add(n);
      } else if (bucket == 3) {
        second.add(n);
      } else {
        first.add(n);
      }
    }

    boolean nested = rnd.nextBoolean();
    if (!first.isEmpty()) {
      ClusterBuilder cb = Cluster.builder();
      for (Node n : first) {
        cb.addNode(n);
      }
      if (nested && !second.isEmpty()) {
        ClusterBuilder inner = Cluster.builder();
        for (Node n : second) {
          inner.addNode(n);
        }
        cb.cluster(inner.build());
      }
      gb.cluster(cb.build());
    }
    if ((!nested || first.isEmpty()) && !second.isEmpty()) {
      ClusterBuilder cb = Cluster.builder();
      for (Node n : second) {
        cb.addNode(n);
      }
      gb.cluster(cb.build());
    }
    for (Node n : outer) {
      gb.addNode(n);
    }

    int edgeCount = rnd.nextInt(nodeCount * 2);
    sb.append("nodes=").append(nodeCount).append(" nested=").append(nested).append(" edges=[");
    for (int e = 0; e < edgeCount; e++) {
      Node from = nodes.get(rnd.nextInt(nodeCount));
      Node to = nodes.get(rnd.nextInt(nodeCount));
      int minlen = MINLENS[rnd.nextInt(MINLENS.length)];
      gb.addLine(Line.builder(from, to).minlen(minlen).build());
      sb.append(from.nodeAttrs().getLabel()).append("->").append(to.nodeAttrs().getLabel())
          .append('(').append(minlen).append(") ");
    }
    sb.append(']');

    /*
     * At most one rank subgraph per node: a node in both rank=MIN and rank=MAX is contradictory
     * input that the API rejects on purpose, which would exercise the validator rather than layout.
     */
    if (rnd.nextInt(3) == 0 && nodeCount >= 3) {
      Rank rank = RANKS[rnd.nextInt(RANKS.length)];
      SubgraphBuilder sub = Subgraph.builder().rank(rank);
      sub.addNode(nodes.get(0));
      sub.addNode(nodes.get(1));
      gb.subgraph(sub.build());
      sb.append(" rank=").append(rank);
    }

    Layout.DOT.getLayoutEngine().layout(gb.build());
  }
}
