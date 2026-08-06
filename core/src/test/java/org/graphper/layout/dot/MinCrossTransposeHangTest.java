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

import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Rankdir;
import org.junit.jupiter.api.Test;

/**
 * Regression test for a hang in crossing minimisation, found by {@link RankAssignmentFuzzTest}'s
 * wider sibling while stress testing rank assignment.
 *
 * <p>Nine nodes are enough. Before the fix, {@code Layout.DOT} spun in
 * {@code RootCrossRank.transpose -> transposeStep -> exchange} at 100% CPU indefinitely (observed past
 * 45s of CPU, 94 million iterations, on a graph that normally lays out in under 3ms). It now completes
 * in under a second.
 *
 * <p><strong>Not caused by the {@code minlen=0} balance fix.</strong> The same input hangs with that
 * change reverted, and the stack never enters {@code NetworkSimplex}.
 *
 * <h2>Measured mechanism</h2>
 * Instrumenting the {@code do { ... } while (delta >= 1)} loop in {@code RootCrossRank.transpose}
 * gives, for this input:
 * <pre>
 * calls=5 localIter=19995     reverse=true delta=1 crossNum=-19995
 * calls=5 localIter=29999995  reverse=true delta=1 crossNum=-29999995
 * calls=5 localIter=94039995  reverse=true delta=1 crossNum=-94039995
 * </pre>
 * The fifth call to {@code transpose} never returns: past 94 million iterations of its inner loop,
 * {@code delta} is <em>exactly 1 on every single iteration</em>, so {@code while (delta >= 1)} can
 * never exit, and {@code crossCache.crossNum -= delta} walks the cached crossing count down to
 * −94,039,995. A crossing count cannot be negative, which makes this cache a cheap place to assert
 * the invariant and catch the whole class of defect early.
 *
 * <h2>Root cause</h2>
 * Logging the pair that gets swapped shows a cycle of period two — two ranks, one swap each per
 * iteration, byte-identical every second iteration:
 * <pre>
 * rank=2 v=&lt;virtual&gt; w=d82  LEFT[tot=1] RIGHT[tot=1]   equal   -> swapped, delta += 0
 * rank=1 v=d83       w=d84  LEFT[tot=1] RIGHT[tot=0]   strict  -> swapped, delta += 1
 * rank=2 v=d82 w=&lt;virtual&gt;  LEFT[tot=1] RIGHT[tot=1]   equal   -> swapped back, delta += 0
 * rank=1 v=d84       w=d83  LEFT[tot=1] RIGHT[tot=0]   strict  -> swapped back, delta += 1
 * </pre>
 *
 * The two swaps feed each other:
 * <ul>
 *   <li>At rank 2 the crossing counts are <em>equal</em>, so only the curvature branch fires —
 *       {@code leftCrossRecord[2] == rightCrossRecord[2] && reverse && canSacrificeCurvature}. It
 *       reorders the rank but contributes {@code delta == 0}.</li>
 *   <li>That reordering restores the crossing between {@code d83} and {@code d84} one rank up, so at
 *       rank 1 the <em>strict</em> branch fires again and contributes {@code delta == 1}.</li>
 * </ul>
 *
 * <p>So {@code while (delta >= 1)} never exits: rank 1 supplies a genuine {@code delta} of 1 forever,
 * because rank 2 keeps re-creating the crossing it removes. Neither swap loops on its own.
 *
 * <p>The loop's exit condition assumes every reordering is accounted for in {@code delta}. The
 * curvature swap breaks that assumption by mutating the order while contributing zero, which is what
 * makes a stable cycle possible. Note that {@code crossing()} itself is not miscounting: rank 1
 * really does have one crossing each time it is asked, because the order it depends on changed
 * underneath it.
 *
 * <h2>How it is fixed</h2>
 * {@code transpose} now stops when it revisits an arrangement it already produced. Because the loop is
 * deterministic, a repeated arrangement proves every later arrangement repeats too, so this is the
 * earliest provably correct exit rather than a heuristic, and it needs no iteration bound to back it up:
 * there are finitely many arrangements, and every iteration that continues has recorded one, so the loop
 * must eventually either run out of crossings to remove or land on an arrangement already seen. A hash
 * collision in the fingerprint can only end it early, never keep it running.
 *
 * <p>The arrangement fingerprint numbers nodes by their position in the arrangement rather than by
 * identity hash code. Identity hashes differ per JVM run, which made the detection point - and so the
 * layout - vary between runs; the first version of this fix had that flaw and produced measurements
 * that disagreed with each other.
 *
 * <p>Measured against 3000 generated graphs plus the 101 checked-in {@code .dot} files, counting
 * crossings geometrically from the rendered svg: <b>all 3101 outputs byte-identical, crossing total
 * unchanged</b>, and two independent runs agree.
 *
 * <h2>Alternatives measured and rejected</h2>
 * <ul>
 *   <li>Allowing each pair only one curvature swap per call: breaks the cycle, but discards beneficial
 *       curvature swaps with it, costing <b>+121 crossings</b> over the same corpus.</li>
 *   <li>On detecting a cycle, switching curvature swaps off and continuing with strict improvements
 *       instead of stopping: sounds strictly better, since it keeps optimising. Measured the same
 *       (crossing total unchanged), so it buys nothing for the extra state.</li>
 * </ul>
 *
 * <h2>Separate defect in the same area: the crossing total drifts negative</h2>
 * {@code crossCache.crossNum} is defined as the sum of the per rank caches — that is how
 * {@code crossNum(...)} recomputes it. Two things break the invariant:
 * <ol>
 *   <li>{@code updateRankCache} drops a delta for an expired rank, correctly, since that rank is
 *       recomputed on the next read. The caller used to apply the full delta to the total regardless,
 *       so the total absorbed increments no rank did. It now takes only what was actually applied.</li>
 *   <li><b>Still open:</b> {@code CrossCache.setCacheExpired} flips a rank's {@code effective} flag
 *       without reconciling the total, so between an expiry and the next full recompute the total is
 *       neither the old sum nor the new one. It still reaches negative values on hundreds of ordinary
 *       graphs. The read path guards itself — {@code tryCacheCrossNum} recomputes when
 *       {@code isEffective()} is false — so the bad value appears not to escape to {@code MinCross}.
 *       Closing it means reconciling on expiry, or not maintaining the total incrementally at all.</li>
 * </ol>
 *
 * <p>Fixing the accounting does <em>not</em> stop the hang: with both termination mechanisms removed
 * this input still spins, because the loop condition reads {@code delta} from a fresh
 * {@code crossing()} computation rather than from the cache. The two defects are independent.
 */
public class MinCrossTransposeHangTest {

  @Test
  public void transposeTerminatesOnOscillatingInput() throws Exception {
    Node n0 = node("n0");
    Node n1 = node("n1");
    Node n2 = node("n2");
    Node n3 = node("n3");
    Node n4 = node("n4");
    Node n5 = node("n5");
    Node n6 = node("n6");
    Node n7 = node("n7");
    Node n8 = node("n8");

    // Three nesting levels: {n0,n1,n2,n4} > {n6,n7} > {n8}; n3 and n5 sit at graph level
    Cluster innermost = Cluster.builder().addNode(n8).build();
    Cluster middle = Cluster.builder().addNode(n6).addNode(n7).cluster(innermost).build();
    Cluster outermost = Cluster.builder()
        .addNode(n0).addNode(n1).addNode(n2).addNode(n4)
        .cluster(middle)
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .rankdir(Rankdir.RL)
        .nslimit(1)
        .nslimit1(3)
        .cluster(outermost)
        .addNode(n3)
        .addNode(n5)
        .subgraph(Subgraph.builder().rank(Rank.SINK)
                      .addNode(n2).addNode(n3).addNode(n5).build())
        .addLine(line(n0, n4, 0, Port.EAST, Port.SOUTH))
        .addLine(line(n4, n5, 0, Port.NORTH, Port.EAST))
        .addLine(line(n0, n7, 0, Port.WEST, Port.NORTH))
        .addLine(line(n3, n4, 0, Port.WEST, Port.EAST))
        .addLine(line(n7, n0, 3, Port.NORTH, Port.SOUTH))
        .addLine(line(n3, n5, 3, Port.SOUTH, Port.NORTH))
        .addLine(line(n2, n3, 1, Port.EAST, Port.NORTH))
        .addLine(line(n1, n4, 3, Port.NORTH, Port.EAST))
        .addLine(line(n4, n3, 0, Port.WEST, Port.WEST))
        .addLine(line(n7, n4, 10, Port.WEST, Port.NORTH))
        // Parallel to the minlen=1 edge above, with a far larger minlen
        .addLine(line(n2, n3, 10, Port.NORTH, Port.NORTH))
        .addLine(line(n1, n8, 0, Port.SOUTH, Port.NORTH))
        .build();

    Layout.DOT.getLayoutEngine().layout(graphviz);
  }

  private Node node(String id) {
    return Node.builder().id(id).label(id).build();
  }

  private Line line(Node from, Node to, int minlen, Port tailPort, Port headPort) {
    return Line.builder(from, to)
        .minlen(minlen)
        .tailPort(tailPort)
        .headPort(headPort)
        .build();
  }
}
