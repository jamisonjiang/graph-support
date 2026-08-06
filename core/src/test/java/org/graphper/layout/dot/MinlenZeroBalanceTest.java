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
