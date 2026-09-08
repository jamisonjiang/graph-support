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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.def.EdgeDedigraph;

/**
 * Classic coordinate implementation for DOT layout.
 * 
 * <p>This implementation uses the traditional network simplex approach to achieve
 * global optimal x-position, providing the highest quality layout but may take
 * longer for large graphs.
 */
class ClassicCoordinate extends AbstractCoordinate {

  private DotDigraph auxDotDigraph;

  public ClassicCoordinate(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
                      EdgeDedigraph<DNode, DLine> proxyDigraph) {
    super(nslimit, rankContent, dotAttachment, proxyDigraph);

    // Auxiliary graph network simplex method to set the level
    networkSimplex(createAuxGraph(), false);

    // Final x coordinate setting
    positive();
  }

  @Override
  protected double containerLeftBorder(GraphContainer container) {
    return getContainerContent(container).leftNode.getAuxRank();
  }

  @Override
  protected double containerRightBorder(GraphContainer container) {
    return getContainerContent(container).rightNode.getAuxRank();
  }

  // ----------------------------------------------------- private method -----------------------------------------------------

  private DotDigraph createAuxGraph() {
    auxDotDigraph = new DotDigraph(proxyDigraph.vertexNum());
    addClusterBorderEdge(auxDotDigraph, dotAttachment.getGraphviz());
    accessNodes(this::nodeConsumer, (node, cluster, border) -> border.refreshRankRange(node));
    addClusterEdgesAcrossRoutingVirtuals();
    addOverlappingSiblingClusterEdges(dotAttachment.getGraphviz());
    return auxDotDigraph;
  }

  private void addOverlappingSiblingClusterEdges(GraphContainer parent) {
    List<Cluster> siblings = new ArrayList<>();
    for (Cluster cluster : dotAttachment.clusters(parent)) {
      siblings.add(cluster);
      addOverlappingSiblingClusterEdges(cluster);
    }

    for (int i = 0; i < siblings.size(); i++) {
      Cluster left = siblings.get(i);
      ContainerBorder leftBorder = getContainerBorder(left);
      if (leftBorder == null) {
        continue;
      }
      for (int j = i + 1; j < siblings.size(); j++) {
        Cluster right = siblings.get(j);
        ContainerBorder rightBorder = getContainerBorder(right);
        if (rightBorder == null || leftBorder.max < rightBorder.min
            || rightBorder.max < leftBorder.min) {
          continue;
        }

        ContainerContent leftContent = getContainerContent(left);
        ContainerContent rightContent = getContainerContent(right);
        if (reachable(leftContent.rightNode, rightContent.leftNode)) {
          addClusterLimit(leftContent, rightContent);
        } else if (reachable(rightContent.rightNode, leftContent.leftNode)) {
          addClusterLimit(rightContent, leftContent);
        } else if (comesBefore(leftBorder, rightBorder)) {
          addClusterLimit(leftContent, rightContent);
        } else {
          addClusterLimit(rightContent, leftContent);
        }
      }
    }
  }

  private void addClusterLimit(ContainerContent left, ContainerContent right) {
    if (reachable(right.leftNode, left.rightNode)) {
      return;
    }
    auxDotDigraph.addEdge(new DLine(left.rightNode, right.leftNode, 0, 16, false));
  }

  private boolean comesBefore(ContainerBorder left, ContainerBorder right) {
    int bestDistance = Integer.MAX_VALUE;
    double leftIndex = 0;
    double rightIndex = 0;
    for (Map.Entry<Integer, int[]> leftEntry : left.rankIndexRange.entrySet()) {
      for (Map.Entry<Integer, int[]> rightEntry : right.rankIndexRange.entrySet()) {
        int distance = Math.abs(leftEntry.getKey() - rightEntry.getKey());
        if (distance >= bestDistance) {
          continue;
        }
        bestDistance = distance;
        int[] leftRange = leftEntry.getValue();
        int[] rightRange = rightEntry.getValue();
        leftIndex = (leftRange[0] + leftRange[1]) / 2D;
        rightIndex = (rightRange[0] + rightRange[1]) / 2D;
      }
    }
    return leftIndex <= rightIndex;
  }

  /**
   * Whether {@code target} is reachable from {@code from} in the auxiliary graph.
   *
   * <p>{@code DNode} does not override {@code equals}, so membership of the visited collection is
   * reference identity. An identity-hashed set gives that in constant time, which keeps the
   * traversal at O(V + E); a list would have made every examined successor scan the whole visited
   * prefix.
   */
  private boolean reachable(DNode from, DNode target) {
    if (from == target) {
      return true;
    }

    Queue<DNode> queue = new ArrayDeque<>();
    Set<DNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    queue.offer(from);
    visited.add(from);
    while (!queue.isEmpty()) {
      DNode node = queue.poll();
      for (DLine line : auxDotDigraph.adjacent(node)) {
        if (line.from() != node) {
          continue;
        }
        DNode next = line.to();
        if (next == target) {
          return true;
        }
        if (visited.add(next)) {
          queue.offer(next);
        }
      }
    }
    return false;
  }

  private void addClusterEdgesAcrossRoutingVirtuals() {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    for (int r = rankContent.minRank(); r <= rankContent.maxRank(); r++) {
      RankContent.RankNode rank = rankContent.get(r);
      int i = 0;
      while (i < rank.size()) {
        if (!rank.get(i).isRoutingVirtual()) {
          i++;
          continue;
        }

        int leftIndex = i - 1;
        while (i < rank.size() && rank.get(i).isRoutingVirtual()) {
          i++;
        }
        if (leftIndex < 0 || i >= rank.size()) {
          continue;
        }

        DNode leftNode = rank.get(leftIndex);
        DNode rightNode = rank.get(i);
        GraphContainer commonParent = dotAttachment.commonParent(leftNode, rightNode);
        ContainerContent left = getContainerContent(
            dotAttachment.clusterDirectContainer(commonParent, leftNode)
        );
        ContainerContent right = getContainerContent(
            dotAttachment.clusterDirectContainer(commonParent, rightNode)
        );
        if (left != null && right != null && left.container != right.container) {
          auxDotDigraph.addEdge(new DLine(left.rightNode, right.leftNode, 0, 16, false));
        }
      }
    }
  }

  protected void nodeConsumer(DNode node) {
    /*
     * 1.Break an edge that spans between two ranks
     * (1) all real                 [weight, minlen]
     * (2) all virtual              [4 * weight, minlen]
     * (1) one real, one virtual    [weight, minlen]
     *
     * 2.Connects two adjacent vertices at the same rank
     * [0, (leftWidth|rightWidth) + nodesep + (leftWidth|rightWidth)]
     *
     * 3.An edge between two boundary nodes of a Cluster
     * [128 * weight, clusterLabel.width()]
     *
     * 4.The point inside the Cluster is connected to the edge of the two boundary nodes
     * [0, (leftWidth|rightWidth) + cluster.margin()]
     *
     * 5.The point outside the Cluster is connected to the edge of the two boundary nodes
     * [0, (leftWidth|rightWidth) + cluster.margin()]
     *
     * 6.The boundary nodes of a cluster and the boundary nodes of another cluster
     * (1) The two clusters are included in the relationship
     * [0, out_cluster.margin]
     * (2) The two clusters are non-inclusive
     * [0, cluster_min_gap]
     * */
    node.switchNormalModel();
    // Mode switch
    node.switchAuxModel();
    // Auxiliary edge addition across hierarchical edges
    crossRankAuxEdge(node);
    // Add auxiliary edges of vertices at the same level
    sameRankAuxEdge(node);
    // Keep every node inside its own parent cluster. Routing-only virtual nodes are exempt only
    // from borders of unrelated adjacent clusters in adjClusterEdge.
    containerBorderEdge(node);

    // Avoid separate nodes
    auxDotDigraph.add(node);
  }

  private void crossRankAuxEdge(DNode node) {
    for (DLine dLine : proxyDigraph.outAdjacent(node)) {
      for (int i = 0; i < dLine.getParallelNums(); i++) {
        DLine l = dLine.parallelLine(i);
        dotAttachment.addGeneratePort(l);
      }

      DNode other = dLine.other(node);
      node.switchNormalModel();
      other.switchNormalModel();

      if (dLine.from().getRank() == dLine.to().getRank()) {
        DNode from = dLine.from().getRankIndex() < dLine.to().getRankIndex()
            ? dLine.from() : dLine.to();
        DNode to = dLine.other(from);

        sameRankLine((int) (from.getNodeSep() * dLine.limit()), from, to, 1);
        continue;
      }

      double weight = dLine.weight();
      node.switchAuxModel();
      other.switchAuxModel();

      DNode auxNode = new DNode(null, 1, 1, dLine.from().getNodeSep());
      auxNode.setContainer(node.getContainer());
      auxNode.switchAuxModel();

      if (node.isLabelNode() || other.isLabelNode()) {
        weight *= 4;
      } else if (node.isVirtual()) {
        if (other.isVirtual()) {
          weight *= 4;
        } else {
          weight *= 2;
        }
      }

      DLine e1;
      DLine e2;
      if (!dLine.isVirtual()) {
        int limit = crossLineLimit(dLine);

        if (limit < 0) {
          e1 = new DLine(auxNode, node, null, weight, -limit);
          e2 = new DLine(auxNode, other, null, weight, 0);
        } else {
          e1 = new DLine(auxNode, node, null, weight, 0);
          e2 = new DLine(auxNode, other, null, weight, limit);
        }

        if (limit != 0) {
          node.markNotAdjustMid();
          other.markNotAdjustMid();
        }
      } else {
        e1 = new DLine(auxNode, node, null, weight, 0);
        e2 = new DLine(auxNode, other, null, weight, 0);
      }

      auxDotDigraph.addEdge(e1);
      auxDotDigraph.addEdge(e2);
    }
  }

  private void sameRankAuxEdge(DNode node) {
    node.switchNormalModel();
    DNode other = rankContent.rankNextNode(node);
    node.switchAuxModel();

    if (other == null) {
      return;
    }

    node.switchNormalModel();
    other.switchNormalModel();

    if (other.getRank() != node.getRank()) {
      return;
    }

    sameRankLine((int) node.getNodeSep(), node, other, 0);
    adjClusterEdge(node, other);
  }

  private void sameRankLine(int minLen, DNode node, DNode other, double weight) {
    node.switchAuxModel();
    other.switchAuxModel();

    int limit = (int) (node.rightWidth() + 1) + minLen + (int) (other.leftWidth() + 1);
    DLine line = new DLine(node, other, weight, limit, false);
    auxDotDigraph.addEdge(line);
  }

  private void adjClusterEdge(DNode node, DNode other) {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    GraphContainer commonParent = dotAttachment.commonParent(node, other);
    if (commonParent == node.getContainer() && commonParent == other.getContainer()) {
      return;
    }

    if (commonParent == node.getContainer()) {
      if (node.isRoutingVirtual()) {
        return;
      }

      ContainerContent containerContent = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, other)
      );
      auxDotDigraph.addEdge(new DLine(node, containerContent.leftNode, 0,
                                      (int) (20 + node.rightWidth()), false));
    }
    if (commonParent == other.getContainer()) {
      if (other.isRoutingVirtual()) {
        return;
      }

      ContainerContent containerContent = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, node)
      );
      auxDotDigraph.addEdge(new DLine(containerContent.rightNode, other, 0,
                                      (int) (20 + other.leftWidth()), false));
    } else {

      ContainerContent left = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, node)
      );
      ContainerContent right = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, other)
      );
      if (left != null && right != null) {
        auxDotDigraph.addEdge(new DLine(left.rightNode, right.leftNode, 0, 16, false));
      }
    }
  }

  private void containerBorderEdge(DNode node) {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    ContainerContent containerContent = getContainerContent(node.getContainer());
    if (containerContent.container.isGraphviz()) {
      return;
    }
    auxDotDigraph.addEdge(new DLine(containerContent.leftNode, node, 0,
                                    (int) (containerContent.leftMargin + node.leftWidth()), false));
    auxDotDigraph.addEdge(new DLine(node, containerContent.rightNode, 0,
                                    (int) (containerContent.rightMargin + node.rightWidth()),
                                    false));
  }

}
