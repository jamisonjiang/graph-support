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

import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the rank balancing step when edges carry {@code minlen=0}.
 *
 * <p>{@code NetworkSimplex.tbBalance} looks for the sparsest rank in the open range
 * {@code (preMax, nextMin)}. Both bounds are exclusive sentinels — the rank just outside the range
 * the node may occupy — so with {@code minlen=0}, where two endpoints are allowed to share a rank,
 * a bound can land one step beyond the outermost rank and has no {@code RankNode} behind it.
 * Dereferencing it threw {@link NullPointerException}.
 *
 * <p>A cluster is required to reach it, because the cluster collapse pass ranks the cluster's
 * contents on their own before the outer graph. A disconnected component is not required, even
 * though the original report included one — see
 * {@link #minlenZeroCycleInClusterWithoutDisconnectedNode()}.
 */
public class MinlenZeroBalanceTest {

  @Test
  public void minlenZeroCycleWithDisconnectedNodeDoesNotFailBalance() throws ExecuteException {
    Node security = Node.builder().id("security").label("security").build();
    Node rest = Node.builder().id("rest").label("rest").build();
    Node repository = Node.builder().id("repository").label("repository").build();
    Node service = Node.builder().id("service").label("service").build();
    Node implementation = Node.builder().id("implementation").label("implementation").build();
    Node model = Node.builder().id("model").label("model").build();
    Node disconnected = Node.builder().id("disconnected").label("disconnected").build();

    Cluster cluster = Cluster.builder()
        .addNode(security)
        .addNode(rest)
        .addNode(repository)
        .addNode(service)
        .addNode(implementation)
        .addNode(model)
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(cluster)
        .addNode(disconnected)
        .addLine(Line.builder(security, rest).minlen(0).build())
        .addLine(Line.builder(security, repository).minlen(1).build())
        .addLine(Line.builder(rest, service).minlen(0).build())
        .addLine(Line.builder(service, implementation).minlen(0).build())
        .addLine(Line.builder(implementation, repository).minlen(0).build())
        .addLine(Line.builder(repository, model).minlen(1).build())
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    Assertions.assertNotNull(drawGraph);

    // Not crashing is not enough: the ranking must still honour every minlen
    Map<Node, Double> y = centerYByNode(drawGraph);
    assertNotAbove(y, security, rest, 0);
    assertNotAbove(y, security, repository, 1);
    assertNotAbove(y, rest, service, 0);
    assertNotAbove(y, service, implementation, 0);
    assertNotAbove(y, implementation, repository, 0);
    assertNotAbove(y, repository, model, 1);
  }

  /**
   * The same graph without the disconnected node still crashed before the fix, so the reported
   * "图外还有一个断开的节点" is incidental rather than part of the trigger. Kept as the minimal
   * reproduction.
   */
  @Test
  public void minlenZeroCycleInClusterWithoutDisconnectedNode() throws ExecuteException {
    Node security = Node.builder().id("security").label("security").build();
    Node rest = Node.builder().id("rest").label("rest").build();
    Node repository = Node.builder().id("repository").label("repository").build();
    Node service = Node.builder().id("service").label("service").build();
    Node implementation = Node.builder().id("implementation").label("implementation").build();
    Node model = Node.builder().id("model").label("model").build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(Cluster.builder()
                     .addNode(security).addNode(rest).addNode(repository)
                     .addNode(service).addNode(implementation).addNode(model)
                     .build())
        .addLine(Line.builder(security, rest).minlen(0).build())
        .addLine(Line.builder(security, repository).minlen(1).build())
        .addLine(Line.builder(rest, service).minlen(0).build())
        .addLine(Line.builder(service, implementation).minlen(0).build())
        .addLine(Line.builder(implementation, repository).minlen(0).build())
        .addLine(Line.builder(repository, model).minlen(1).build())
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    Assertions.assertNotNull(drawGraph);

    Map<Node, Double> y = centerYByNode(drawGraph);
    assertNotAbove(y, security, rest, 0);
    assertNotAbove(y, rest, service, 0);
    assertNotAbove(y, service, implementation, 0);
    assertNotAbove(y, implementation, repository, 0);
    assertNotAbove(y, repository, model, 1);
  }

  /**
   * The mirror case: a {@code minlen=0} edge whose lower endpoint sits on the last rank pushes
   * {@code nextMin} to {@code maxRank + 1}, the upper sentinel.
   */
  @Test
  public void minlenZeroIntoLastRankDoesNotFailBalance() throws ExecuteException {
    Node a = Node.builder().id("a").label("a").build();
    Node b = Node.builder().id("b").label("b").build();
    Node c = Node.builder().id("c").label("c").build();
    Node d = Node.builder().id("d").label("d").build();
    Node disconnected = Node.builder().id("disconnected").label("disconnected").build();

    Cluster cluster = Cluster.builder()
        .addNode(a).addNode(b).addNode(c).addNode(d)
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(cluster)
        .addNode(disconnected)
        .addLine(Line.builder(a, b).minlen(1).build())
        .addLine(Line.builder(b, c).minlen(0).build())
        .addLine(Line.builder(c, d).minlen(0).build())
        .addLine(Line.builder(b, d).minlen(0).build())
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    Assertions.assertNotNull(drawGraph);

    Map<Node, Double> y = centerYByNode(drawGraph);
    assertNotAbove(y, a, b, 1);
    assertNotAbove(y, b, c, 0);
    assertNotAbove(y, c, d, 0);
    assertNotAbove(y, b, d, 0);
  }

  /**
   * Every node of a chain of {@code minlen=0} edges may legally share one rank; the engine must at
   * least not place a successor above its predecessor.
   */
  @Test
  public void allMinlenZeroChainStaysMonotonic() throws ExecuteException {
    Node n0 = Node.builder().id("n0").label("n0").build();
    Node n1 = Node.builder().id("n1").label("n1").build();
    Node n2 = Node.builder().id("n2").label("n2").build();
    Node disconnected = Node.builder().id("disconnected").label("disconnected").build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(Cluster.builder().addNode(n0).addNode(n1).addNode(n2).build())
        .addNode(disconnected)
        .addLine(Line.builder(n0, n1).minlen(0).build())
        .addLine(Line.builder(n1, n2).minlen(0).build())
        .addLine(Line.builder(n0, n2).minlen(0).build())
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    Assertions.assertNotNull(drawGraph);

    Map<Node, Double> y = centerYByNode(drawGraph);
    assertNotAbove(y, n0, n1, 0);
    assertNotAbove(y, n1, n2, 0);
    assertNotAbove(y, n0, n2, 0);
  }

  /**
   * The reference case: as two plain nodes, {@code left -> right} with {@code minlen=0} is
   * horizontal. Putting the same two endpoints into two sibling clusters must not change that.
   *
   * <p>The cluster collapse pass rewrites the edge into a fork from one auxiliary node to the two
   * cluster proxies, both with limit 0. That fork is the first thing entering the initial feasible
   * tree, and its first edge used to be admitted with slack, ranking the clusters one apart.
   */
  @Test
  public void minlenZeroAcrossSiblingClustersStaysOnSameRank() throws ExecuteException {
    Node left = Node.builder().id("left").label("Notifications").build();
    Node right = Node.builder().id("right").label("Event Handler").build();
    Cluster leftInner = Cluster.builder().id("left_inner").addNode(left).build();
    Cluster rightInner = Cluster.builder().id("right_inner").addNode(right).build();
    Cluster leftOuter = Cluster.builder().id("left_outer").cluster(leftInner).build();
    Cluster rightOuter = Cluster.builder().id("right_outer").cluster(rightInner).build();
    Graphviz graphviz = Graphviz.digraph()
        .cluster(leftOuter)
        .cluster(rightOuter)
        .addLine(Line.builder(left, right).minlen(0).build())
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    Map<Node, Double> y = centerYByNode(drawGraph);

    Assertions.assertEquals(y.get(left), y.get(right), 0.5,
                            "cross-cluster minlen=0 endpoints must remain on the same rank");
  }

  /** Baseline: the same edge without clusters. Establishes what the cluster cases must match. */
  @Test
  public void minlenZeroBetweenPlainNodesStaysOnSameRank() throws ExecuteException {
    Node left = Node.builder().id("left").label("left").build();
    Node right = Node.builder().id("right").label("right").build();
    Graphviz graphviz = Graphviz.digraph()
        .addLine(Line.builder(left, right).minlen(0).build())
        .build();

    Map<Node, Double> y = centerYByNode(Layout.DOT.getLayoutEngine().layout(graphviz));
    Assertions.assertEquals(y.get(left), y.get(right), 0.5,
                            "minlen=0 between plain nodes must be horizontal");
  }

  /** One cluster level only, so the failure does not depend on nesting depth. */
  @Test
  public void minlenZeroAcrossFlatSiblingClustersStaysOnSameRank() throws ExecuteException {
    Node left = Node.builder().id("left").label("left").build();
    Node right = Node.builder().id("right").label("right").build();
    Graphviz graphviz = Graphviz.digraph()
        .cluster(Cluster.builder().id("cluster_left").addNode(left).build())
        .cluster(Cluster.builder().id("cluster_right").addNode(right).build())
        .addLine(Line.builder(left, right).minlen(0).build())
        .build();

    Map<Node, Double> y = centerYByNode(Layout.DOT.getLayoutEngine().layout(graphviz));
    Assertions.assertEquals(y.get(left), y.get(right), 0.5,
                            "minlen=0 across flat sibling clusters must stay on one rank");
  }

  /**
   * The PlantUML shape from the report: a shared {@code p0} wrapper holding a {@code visible}
   * cluster on each side, each of which holds the endpoint. Three levels of nesting on both sides.
   */
  @Test
  public void minlenZeroAcrossNestedPlantUmlStyleClustersStaysOnSameRank()
      throws ExecuteException {
    Node left = Node.builder().id("left").label("Notifications").build();
    Node right = Node.builder().id("right").label("Event Handler").build();

    Cluster leftVisible = Cluster.builder().id("cluster_left_visible").addNode(left).build();
    Cluster rightVisible = Cluster.builder().id("cluster_right_visible").addNode(right).build();
    Cluster leftP1 = Cluster.builder().id("cluster_left_p1").cluster(leftVisible).build();
    Cluster rightP1 = Cluster.builder().id("cluster_right_p1").cluster(rightVisible).build();
    Cluster p0 = Cluster.builder().id("cluster_p0")
        .cluster(leftP1)
        .cluster(rightP1)
        .build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(p0)
        .addLine(Line.builder(left, right).minlen(0).build())
        .build();

    Map<Node, Double> y = centerYByNode(Layout.DOT.getLayoutEngine().layout(graphviz));
    Assertions.assertEquals(y.get(left), y.get(right), 0.5,
                            "minlen=0 across nested p0/visible/p1 clusters must stay on one rank");
  }

  /**
   * With more than one node per cluster the proxy no longer coincides with the endpoint, so the
   * rank offset between the endpoint and its cluster's minimum rank participates in the collapsed
   * edge's limit. The endpoints still have to share a rank.
   */
  @Test
  public void minlenZeroAcrossPopulatedSiblingClustersStaysOnSameRank() throws ExecuteException {
    Node leftTop = Node.builder().id("left_top").label("left_top").build();
    Node left = Node.builder().id("left").label("left").build();
    Node rightTop = Node.builder().id("right_top").label("right_top").build();
    Node right = Node.builder().id("right").label("right").build();

    Graphviz graphviz = Graphviz.digraph()
        .cluster(Cluster.builder().id("cluster_left")
                     .addLine(Line.builder(leftTop, left).build())
                     .build())
        .cluster(Cluster.builder().id("cluster_right")
                     .addLine(Line.builder(rightTop, right).build())
                     .build())
        .addLine(Line.builder(left, right).minlen(0).build())
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    Map<Node, Double> y = centerYByNode(drawGraph);

    Assertions.assertEquals(y.get(left), y.get(right), 0.5,
                            "minlen=0 endpoints below their cluster tops must share a rank");
    assertNotAbove(y, leftTop, left, 1);
    assertNotAbove(y, rightTop, right, 1);
  }

  /**
   * Reversed direction of the cross-cluster edge. Which endpoint the tree seeding anchors depends
   * on edge orientation, so both directions must hold.
   */
  @Test
  public void minlenZeroAcrossSiblingClustersIsDirectionIndependent() throws ExecuteException {
    Node left = Node.builder().id("left").label("left").build();
    Node right = Node.builder().id("right").label("right").build();
    Graphviz graphviz = Graphviz.digraph()
        .cluster(Cluster.builder().id("cluster_left").addNode(left).build())
        .cluster(Cluster.builder().id("cluster_right").addNode(right).build())
        .addLine(Line.builder(right, left).minlen(0).build())
        .build();

    Map<Node, Double> y = centerYByNode(Layout.DOT.getLayoutEngine().layout(graphviz));
    Assertions.assertEquals(y.get(left), y.get(right), 0.5,
                            "minlen=0 across clusters must be horizontal in either direction");
  }

  /**
   * A positive minlen across the same cluster nesting must still separate the endpoints — the fix
   * must not have collapsed everything onto one rank.
   */
  @Test
  public void positiveMinlenAcrossSiblingClustersStillSeparatesRanks() throws ExecuteException {
    Node left = Node.builder().id("left").label("left").build();
    Node right = Node.builder().id("right").label("right").build();
    Graphviz graphviz = Graphviz.digraph()
        .cluster(Cluster.builder().id("cluster_left").addNode(left).build())
        .cluster(Cluster.builder().id("cluster_right").addNode(right).build())
        .addLine(Line.builder(left, right).minlen(2).build())
        .build();

    Map<Node, Double> y = centerYByNode(Layout.DOT.getLayoutEngine().layout(graphviz));
    Assertions.assertTrue(y.get(right) > y.get(left),
                          "minlen=2 across clusters must still descend, got left=" + y.get(left)
                              + " right=" + y.get(right));
  }

  // ------------------------------- helpers -------------------------------

  private Map<Node, Double> centerYByNode(DrawGraph drawGraph) {
    Map<Node, Double> centers = new HashMap<>();
    for (NodeDrawProp prop : drawGraph.nodes()) {
      centers.put(prop.getNode(), (prop.getUpBorder() + prop.getDownBorder()) / 2);
    }
    return centers;
  }

  /**
   * Asserts that {@code to} is not laid out above {@code from}. With {@code minlen == 0} the two may
   * share a rank, so only a strict inversion is a failure; with a positive minlen the successor has
   * to be strictly lower.
   */
  private void assertNotAbove(Map<Node, Double> y, Node from, Node to, int minlen) {
    Double fromY = y.get(from);
    Double toY = y.get(to);
    Assertions.assertNotNull(fromY, "tail node missing from the layout");
    Assertions.assertNotNull(toY, "head node missing from the layout");

    if (minlen == 0) {
      Assertions.assertTrue(toY >= fromY - 0.5,
                            "minlen=0 edge was inverted: head at " + toY + " is above tail at "
                                + fromY);
    } else {
      Assertions.assertTrue(toY > fromY,
                            "minlen=" + minlen + " edge did not descend: head at " + toY
                                + ", tail at " + fromY);
    }
  }
}
