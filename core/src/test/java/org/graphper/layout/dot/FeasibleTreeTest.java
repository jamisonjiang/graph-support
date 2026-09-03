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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Invariants of the initial feasible tree built by {@link FeasibleTree}.
 *
 * <p>The network simplex method in {@link NetworkSimplex} navigates the tree purely through the
 * {@code (low, lim)} post-order interval labels that {@code PropInit} derives from it. Those labels
 * only describe a real rooted tree if {@code generateTree} produced a spanning forest: exactly one
 * tree component per graph component, and every node covered. When a component is left split in two,
 * {@code publicRoot} walks looking for a parent with a larger {@code lim} that no longer exists and
 * spins forever — the observed 98%-CPU {@code publicRoot -> findNeedUpdateCutvalLines -> enterLine}
 * loop.
 *
 * <p>The second invariant is tightness: every tree edge must have {@code reduceLen() == 0}. The
 * feasible tree is the simplex basis, and cut values are only meaningful for a tight basis. A
 * non-tight tree edge also means the rank assignment does not realise the constraint the edge
 * encodes, which for {@code minlen=0} (limit 0) is what separates two nodes that should share a
 * rank.
 *
 * <p>Zero limits are the interesting case throughout: with {@code limit >= 1} the initial DFS
 * ranking already spreads endpoints far enough that the seeding order rarely matters, while
 * {@code limit == 0} makes "tight" mean "equal rank" and exposes any edge admitted with slack.
 */
public class FeasibleTreeTest {

  @Test
  public void twoNodeZeroLimitEdgeIsTight() {
    DNode tail = new DNode(null, 0, 0, 0);
    DNode head = new DNode(null, 0, 0, 0);
    DLine line = new DLine(tail, head, null, 1, 0);
    DotDigraph digraph = new DotDigraph(2);
    digraph.addEdge(line);

    assertFeasibleTree(digraph);

    Assertions.assertEquals(0, line.reduceLen(),
                            "the only edge of the graph entered the tree with slack");
    Assertions.assertEquals(tail.getRank(), head.getRank(),
                            "a tight limit-0 edge must leave both endpoints on one rank");
  }

  /**
   * The shape {@code ContainerCollapse.addAuxClusterLine} emits for a {@code minlen=0} edge whose
   * endpoints live in two different sibling clusters: one auxiliary node feeding both cluster
   * proxies with limit 0, weighted 10 against 1. Both proxies have to end up on the auxiliary
   * node's rank, otherwise the clusters are ranked apart and the user-visible edge turns vertical.
   */
  @Test
  public void zeroLimitForkIsFullyTight() {
    DNode auxiliary = new DNode(null, 0, 0, 0);
    DNode left = new DNode(null, 0, 0, 0);
    DNode right = new DNode(null, 0, 0, 0);
    DLine leftLine = new DLine(auxiliary, left, null, 10, 0);
    DLine rightLine = new DLine(auxiliary, right, null, 1, 0);
    DotDigraph digraph = new DotDigraph(3);
    digraph.addEdge(leftLine);
    digraph.addEdge(rightLine);

    assertFeasibleTree(digraph);

    Assertions.assertEquals(0, leftLine.reduceLen());
    Assertions.assertEquals(0, rightLine.reduceLen());
    Assertions.assertEquals(auxiliary.getRank(), left.getRank());
    Assertions.assertEquals(auxiliary.getRank(), right.getRank());
  }

  /**
   * Nodes carry ranks from earlier passes — cluster contents are ranked on their own before the
   * enclosing graph — so {@code generateTree} must not depend on starting from a zeroed or
   * consistent assignment. Leaf nodes in particular keep whatever rank they arrive with, because
   * the initial DFS only writes a rank for nodes that have outgoing edges.
   */
  @Test
  public void zeroLimitForkIsTightFromAnyInitialRanks() {
    int[] ranks = {-3, -1, 0, 1, 4};
    for (int auxRank : ranks) {
      for (int leftRank : ranks) {
        for (int rightRank : ranks) {
          DNode auxiliary = new DNode(null, 0, 0, 0);
          DNode left = new DNode(null, 0, 0, 0);
          DNode right = new DNode(null, 0, 0, 0);
          auxiliary.setRank(auxRank);
          left.setRank(leftRank);
          right.setRank(rightRank);

          DLine leftLine = new DLine(auxiliary, left, null, 10, 0);
          DLine rightLine = new DLine(auxiliary, right, null, 1, 0);
          DotDigraph digraph = new DotDigraph(3);
          digraph.addEdge(leftLine);
          digraph.addEdge(rightLine);

          String where = "initial ranks " + auxRank + "/" + leftRank + "/" + rightRank;
          assertFeasibleTree(digraph, where);
          Assertions.assertEquals(auxiliary.getRank(), left.getRank(), where);
          Assertions.assertEquals(auxiliary.getRank(), right.getRank(), where);
        }
      }
    }
  }

  /**
   * Edge direction decides which endpoint the seeding step anchors and which way the tightening
   * shift goes, so every orientation of the fork has to come out tight.
   */
  @Test
  public void zeroLimitForkIsTightForEveryOrientation() {
    for (boolean leftOut : new boolean[]{true, false}) {
      for (boolean rightOut : new boolean[]{true, false}) {
        DNode auxiliary = new DNode(null, 0, 0, 0);
        DNode left = new DNode(null, 0, 0, 0);
        DNode right = new DNode(null, 0, 0, 0);

        DLine leftLine = leftOut
            ? new DLine(auxiliary, left, null, 10, 0)
            : new DLine(left, auxiliary, null, 10, 0);
        DLine rightLine = rightOut
            ? new DLine(auxiliary, right, null, 1, 0)
            : new DLine(right, auxiliary, null, 1, 0);

        DotDigraph digraph = new DotDigraph(3);
        digraph.addEdge(leftLine);
        digraph.addEdge(rightLine);

        assertFeasibleTree(digraph, "leftOut=" + leftOut + " rightOut=" + rightOut);
      }
    }
  }

  /**
   * Mixing a zero limit with positive ones is the case where seeding the wrong endpoint matters
   * most: the tightening shift has to be the smallest slack leaving the tree, or a positive-limit
   * edge elsewhere in the component is pulled below its minimum length.
   */
  @Test
  public void mixedLimitForkIsTight() {
    for (int limit = 0; limit <= 3; limit++) {
      for (int otherLimit = 0; otherLimit <= 3; otherLimit++) {
        DNode auxiliary = new DNode(null, 0, 0, 0);
        DNode left = new DNode(null, 0, 0, 0);
        DNode right = new DNode(null, 0, 0, 0);
        DLine leftLine = new DLine(auxiliary, left, null, 10, limit);
        DLine rightLine = new DLine(auxiliary, right, null, 1, otherLimit);
        DotDigraph digraph = new DotDigraph(3);
        digraph.addEdge(leftLine);
        digraph.addEdge(rightLine);

        assertFeasibleTree(digraph, "limits " + limit + "/" + otherLimit);
      }
    }
  }

  /** Several independent components must each be seeded and spanned on their own. */
  @Test
  public void everyComponentIsSpannedAndTight() {
    DotDigraph digraph = new DotDigraph(7);
    DNode a = new DNode(null, 0, 0, 0);
    DNode b = new DNode(null, 0, 0, 0);
    DNode c = new DNode(null, 0, 0, 0);
    digraph.addEdge(new DLine(a, b, null, 1, 0));
    digraph.addEdge(new DLine(b, c, null, 1, 0));

    DNode d = new DNode(null, 0, 0, 0);
    DNode e = new DNode(null, 0, 0, 0);
    digraph.addEdge(new DLine(d, e, null, 1, 2));

    DNode f = new DNode(null, 0, 0, 0);
    DNode g = new DNode(null, 0, 0, 0);
    digraph.addEdge(new DLine(f, g, null, 3, 0));
    digraph.addEdge(new DLine(f, g, null, 1, 1));

    assertFeasibleTree(digraph);
    Assertions.assertEquals(a.getRank(), c.getRank(), "a chain of limit-0 edges collapses to a rank");
  }

  /**
   * Randomised sweep. Every generated graph is made acyclic first, exactly as
   * {@link ContainerCollapse} does, then the two invariants are checked on the resulting tree.
   * Deterministic shapes are weak here — the split component needs a particular interleaving of
   * zero and positive limits across a component boundary — so the sweep carries most of the weight.
   */
  @Test
  public void randomGraphsProduceTightSpanningForests() {
    for (int seed = 0; seed < 3000; seed++) {
      Random rnd = new Random(seed);
      int nodeCount = 2 + rnd.nextInt(8);
      List<DNode> nodes = new ArrayList<>(nodeCount);
      DotDigraph digraph = new DotDigraph(nodeCount);
      for (int i = 0; i < nodeCount; i++) {
        DNode node = new DNode(null, 0, 0, 0);
        // Arrive with a rank from an earlier pass, as cluster proxies do.
        node.setRank(rnd.nextInt(5) - 2);
        nodes.add(node);
        digraph.add(node);
      }

      int edgeCount = 1 + rnd.nextInt(nodeCount * 2);
      for (int e = 0; e < edgeCount; e++) {
        DNode from = nodes.get(rnd.nextInt(nodeCount));
        DNode to = nodes.get(rnd.nextInt(nodeCount));
        if (from == to) {
          continue;
        }
        // Zero limits deliberately over-represented; they are the failing family.
        int limit = rnd.nextInt(2) == 0 ? 0 : rnd.nextInt(4);
        digraph.addEdge(new DLine(from, to, null, 1 + rnd.nextInt(10), limit));
      }

      new Acyclic(digraph, null);
      assertFeasibleTree(digraph, "seed=" + seed);
    }
  }

  // ------------------------------- helpers -------------------------------

  private void assertFeasibleTree(DotDigraph digraph) {
    assertFeasibleTree(digraph, "");
  }

  /**
   * Builds the feasible tree and asserts the two properties {@link NetworkSimplex} relies on: the
   * tree spans every node with one component per graph component, and every tree edge is tight.
   */
  private void assertFeasibleTree(DotDigraph digraph, String where) {
    FeasibleTree feasibleTree = new FeasibleTree(digraph);
    DotGraph graph = feasibleTree.graph();
    DotGraph tree = feasibleTree.tree();

    /*
     * An edgeless node is a component on its own and contributes no tree edge, so it is never
     * added to the tree; every node that has an edge must be covered.
     */
    for (DNode node : graph) {
      if (graph.degree(node) > 0) {
        Assertions.assertTrue(tree.containNode(node),
                              where + ": a node with edges is missing from the tree");
      }
    }

    Set<ULine> treeEdges = new HashSet<>();
    for (DNode node : tree) {
      for (ULine uLine : tree.adjacent(node)) {
        if (treeEdges.add(uLine)) {
          Assertions.assertEquals(0, uLine.reduceLen(),
                                  where + ": tree edge " + uLine + " is not tight");
        }
      }
    }

    /*
     * Counted over the connected nodes only, so that edgeless nodes do not shift both totals.
     * These two numbers diverging is the corruption that makes publicRoot spin.
     */
    int connected = 0;
    for (DNode node : graph) {
      if (graph.degree(node) > 0) {
        connected++;
      }
    }
    int graphComponents = countComponents(graph, graph);
    int treeComponents = countComponents(graph, tree);
    Assertions.assertEquals(graphComponents, treeComponents,
                            where + ": the tree split a connected component, so the (low, lim) "
                                + "labels do not describe a tree and NetworkSimplex cannot "
                                + "terminate");
    Assertions.assertEquals(connected - graphComponents, treeEdges.size(),
                            where + ": a spanning forest must have V - components edges");
  }

  /**
   * Counts components over the connected nodes of {@code vertices}, following edges of
   * {@code edges}.
   */
  private int countComponents(DotGraph vertices, DotGraph edges) {
    Set<DNode> seen = new HashSet<>();
    int components = 0;
    for (DNode node : vertices) {
      if (vertices.degree(node) == 0 || !seen.add(node)) {
        continue;
      }
      components++;
      Deque<DNode> stack = new ArrayDeque<>();
      stack.push(node);
      while (!stack.isEmpty()) {
        DNode current = stack.pop();
        for (ULine uLine : edges.adjacent(current)) {
          DNode other = uLine.other(current);
          if (seen.add(other)) {
            stack.push(other);
          }
        }
      }
    }
    return components;
  }
}
