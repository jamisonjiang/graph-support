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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.layout.dot.RankContent.RankNode;

/**
 * Quick coordinate implementation for DOT layout.
 * 
 * <p>This implementation uses an optimized approach combining network simplex and
 * Brandes/Köpf algorithm to achieve faster x-position calculation while maintaining
 * good visual quality. Suitable for large graphs where classic DOT performance is insufficient.
 */
class QuickCoordinate extends AbstractCoordinate {

  private Map<GraphContainer, ContainerContent> containerContentMap;

  public QuickCoordinate(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
                      EdgeDedigraph<DNode, DLine> proxyDigraph) {
    super(nslimit, rankContent, dotAttachment, proxyDigraph);

    // Initialize container content map for cluster handling
    if (dotAttachment.haveClusters()) {
      containerContentMap = new HashMap<>();
      initializeContainerContent();
    }

    // Mix dot network-simplex and Brandes/Köpf algorithm to x-position
    blockNetworkSimplex();

    // Final x coordinate setting
    positive();

    // help gc
    clear();
  }

  @Override
  protected double containerLeftBorder(GraphContainer container) {
    return getContainerContent(container).leftNode.getAuxRank();
  }

  @Override
  protected double containerRightBorder(GraphContainer container) {
    return getContainerContent(container).rightNode.getAuxRank();
  }

  @Override
  protected void nodeConsumer(DNode node) {
  }

  // ----------------------------------------------------- private method -----------------------------------------------------

  private void blockNetworkSimplex() {
    accessNodes();

    Map<Integer, List<SimpleEntry<DNode, DNode>>> conflicts = new HashMap<>();

    int blockSize = 0;
    Map<DNode, DNode> nodeBlocks = new HashMap<>();

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        if (notFirstDiscoveryVirtualNode(node) || nodeBlocks.containsKey(node)) {
          continue;
        }

        blockSize++;
        DNode block = new DNode(null, 0, 0, 0);
        block.setLow(Integer.MAX_VALUE);
        block.setLim(Integer.MIN_VALUE);
        dfs(node, nodeBlocks, block, true, conflicts);
      }
    }

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        if (nodeBlocks.containsKey(node)) {
          continue;
        }

        blockSize++;
        DNode block = new DNode(null, 0, 0, 0);
        block.setLow(Integer.MAX_VALUE);
        block.setLim(Integer.MIN_VALUE);
        dfs(node, nodeBlocks, block, false, conflicts);
      }
    }

    DotDigraph blockGraph = new DotDigraph(blockSize);
    for (DNode node : proxyDigraph) {
      DNode block = nodeBlocks.get(node);
      connectFlowBlocks(nodeBlocks, blockGraph, node, block);
    }

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        DNode block = nodeBlocks.get(node);
        blockGraph.add(block);

        if (j > 0) {
          DNode pre = rankNode.get(j - 1);
          DNode preBlock = nodeBlocks.get(pre);
          if (block == preBlock) {
            continue;
          }

          int limit =
              ((int) (block.getHeight() + preBlock.getHeight()) / 2) + (int) pre.getNodeSep();
          blockGraph.addEdge(new DLine(preBlock, block, null, 1, limit));
        }
      }
    }

    FeasibleTree feasibleTree = new FeasibleTree(blockGraph);
    new NetworkSimplex(feasibleTree, nslimit, false,
                       false, false,
                       Double.MAX_VALUE, null);

    for (Entry<DNode, DNode> entry : nodeBlocks.entrySet()) {
      DNode node = entry.getKey();
      DNode block = entry.getValue();

      node.setAuxRank(block.getRank());
    }

    medianpos(0);
    medianpos(1);
  }

  private void connectFlowBlocks(Map<DNode, DNode> nodeBlocks, DotDigraph blockGraph, DNode node,
                                 DNode block) {
    if (node.isVirtual()) {
      return;
    }

    boolean inNeedRefreshHeight = proxyDigraph.inDegree(node) == 1;
    boolean outNeedRefreshHeight = proxyDigraph.outDegree(node) == 1;
    for (DLine edge : proxyDigraph.inAdjacent(node)) {
      connectFlowBlocks(nodeBlocks, blockGraph, node, block, edge, inNeedRefreshHeight);
    }
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      connectFlowBlocks(nodeBlocks, blockGraph, node, block, edge, outNeedRefreshHeight);
    }
  }

  private void connectFlowBlocks(Map<DNode, DNode> nodeBlocks, DotDigraph blockGraph, DNode node,
                                 DNode block, DLine edge, boolean needRefreshHeight) {
    DNode other = edge.other(node);
    if (other == node || !other.isVirtual()) {
      return;
    }

    DNode adjBlock = nodeBlocks.get(other);
    if (adjBlock == block || blockIsInteract(block, adjBlock)) {
      return;
    }

    if (needRefreshHeight) {
      adjBlock.setHeight(Math.max(adjBlock.getHeight(), node.getWidth()));
    }
    DNode tmpBlock = new DNode(null, 0, 0, 0);
    blockGraph.addEdge(new DLine(tmpBlock, block, null, 1, 0));
    blockGraph.addEdge(new DLine(tmpBlock, adjBlock, null, 1, 0));
  }

  private boolean blockIsInteract(DNode block1, DNode block2) {
    return !(block1.getLim() < block2.getLow() || block1.getLow() > block2.getLim());
  }

  private boolean notFirstDiscoveryVirtualNode(DNode n) {
    if (!n.isVirtual()) {
      return true;
    }

    for (DLine edge : proxyDigraph.outAdjacent(n)) {
      DNode other = edge.other(n);
      if (other.isVirtual() && other.getRealRank() != n.getRealRank()) {
        return false;
      }
    }

    return true;
  }

  private void dfs(DNode v, Map<DNode, DNode> mark, DNode block, boolean isVirtual,
                   Map<Integer, List<SimpleEntry<DNode, DNode>>> conflicts) {
    if (mark.containsKey(v)) {
      return;
    }

    mark.put(v, block);
    block.setLow(Math.min(block.getLow(), v.getRealRank()));
    block.setLim(Math.max(block.getLim(), v.getRealRank()));
    block.setHeight(Math.max(block.getHeight(), v.getWidth()));
    List<SimpleEntry<DNode, DNode>> rankConflicts = conflicts.get(v.getRealRank());

    DNode successor = null;
    for (DLine edge : proxyDigraph.outAdjacent(v)) {
      DNode other = edge.other(v);
      if (hasConflict(rankConflicts, v, other)) {
        continue;
      }

      if (isVirtual && !other.isVirtual()) {
        continue;
      }

      if (successor == null || (!successor.isVirtual() && other.isVirtual())
          || successor.getRankIndex() < other.getRankIndex()) {
        successor = other;
      }
    }

    if (successor != null) {

      if (rankConflicts == null) {
        rankConflicts = new ArrayList<>();
        conflicts.put(v.getRealRank(), rankConflicts);
      }
      rankConflicts.add(new SimpleEntry<>(v, successor));
      dfs(successor, mark, block, isVirtual, conflicts);
    }
  }

  private boolean hasConflict(List<SimpleEntry<DNode, DNode>> rankConflicts, DNode from, DNode to) {
    if (rankConflicts == null) {
      return false;
    }

    if (from == to || from.getRealRank() == to.getRealRank()) {
      return true;
    }

    for (SimpleEntry<DNode, DNode> conflict : rankConflicts) {
      DNode f = conflict.getKey();
      DNode t = conflict.getValue();

      if (f.getRankIndex() < from.getRankIndex() != t.getRankIndex() < to.getRankIndex()) {
        return true;
      }
    }

    return false;
  }

  private void medianpos(int iteration) {
    // Alternate direction based on iteration: even = downward, odd = upward
    boolean downward = (iteration % 2 == 0);

    if (downward) {
      // Process downward (top to bottom) for even iterations
      for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
        RankNode rankNode = rankContent.get(layer);

        for (DNode node : rankNode) {
          if (!allowMedian(node, false)) {
            continue;
          }
          double medianPos = calculateMedianPosition(node, false);
          double constrainedPos = applySpacingConstraints(node, medianPos);
          node.setAuxRank((int) constrainedPos);
        }
      }
    } else {
      // Process upward (bottom to top) for odd iterations
      for (int layer = rankContent.maxRank(); layer >= rankContent.minRank(); layer--) {
        RankNode rankNode = rankContent.get(layer);

        for (DNode node : rankNode) {
          if (!allowMedian(node, true)) {
            continue;
          }
          double medianPos = calculateMedianPosition(node, true);
          double constrainedPos = applySpacingConstraints(node, medianPos);
          node.setAuxRank((int) constrainedPos);
        }
      }
    }
  }

  private boolean allowMedian(DNode n, boolean upward) {
//    return true;
//    return !n.isVirtual();
    if (!n.isVirtual()) {
      return true;
    }

    DNode pre = null;
    DNode next = null;
    for (DLine edge : proxyDigraph.outAdjacent(n)) {
      pre = edge.other(n);
    }
    for (DLine edge : proxyDigraph.inAdjacent(n)) {
      next = edge.other(n);
    }

    if (pre == null || next == null) {
      return true;
    }

    return !pre.isVirtual() && !next.isVirtual();
  }

  /**
   * Calculate median position for a node based on neighbors
   */
  private double calculateMedianPosition(DNode node, boolean upward) {
    List<Double> neighborPositions = new ArrayList<>();

    if (upward) {
      for (DLine edge : proxyDigraph.outAdjacent(node)) {
        DNode adjNode = edge.other(node);

        if (adjNode.getRealRank() != node.getRealRank()) {
          neighborPositions.add((double) adjNode.getAuxRank());
        }
      }
    } else {
      for (DLine edge : proxyDigraph.inAdjacent(node)) {
        DNode adjNode = edge.other(node);

        if (adjNode.getRealRank() != node.getRealRank()) {
          neighborPositions.add((double) adjNode.getAuxRank());
        }
      }
    }

    if (neighborPositions.isEmpty()) {
      return node.getAuxRank();
    }

    // Sort and return median
    neighborPositions.sort(Double::compareTo);
    int medianIndex = neighborPositions.size() / 2;

    // If there are two medians, take their mean for symmetry
    if (neighborPositions.size() % 2 == 0 && medianIndex > 0) {
      return (neighborPositions.get(medianIndex - 1) + neighborPositions.get(medianIndex)) / 2.0;
    } else {
      return neighborPositions.get(medianIndex);
    }
  }

  /**
   * Apply spacing constraints to ensure no node overlap Returns the constrained position that
   * respects minimum spacing requirements
   */
  private double applySpacingConstraints(DNode node, double desiredPos) {
    DNode pre = rankContent.rankPreNode(node);
    DNode next = rankContent.rankNextNode(node);

    if (pre == null && next == null) {
      return desiredPos;
    }

    if (pre != null) {
      double limit = pre.getAuxRank() + pre.rightWidth() + pre.getNodeSep() + node.leftWidth();
      if (desiredPos < limit) {
        return limit;
      }
    }

    if (next != null) {
      double limit = next.getAuxRank() - (node.rightWidth() + node.getNodeSep() + next.leftWidth());
      if (desiredPos > limit) {
        return limit;
      }
    }

    return desiredPos;
  }


  /**
   * Initialize container content for cluster handling
   */
  private void initializeContainerContent() {
    for (Cluster cluster : dotAttachment.getGraphviz().clusters()) {
      containerContentMap.put(cluster, new ContainerContent(cluster));
    }
  }

  /**
   * Add cluster boundary edges
   */
  private void containerBorderEdge(DNode node) {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    ContainerContent containerContent = getContainerContent(node.getContainer());
    if (containerContent.container.isGraphviz()) {
      return;
    }

    // Note: In Sugiyama, we don't create auxiliary edges like Network Simplex
    // Instead, we handle cluster boundaries through the barycenter calculation
    // by considering the cluster's left and right boundary nodes
  }

  /**
   * Get container content for a specific container
   */
  private ContainerContent getContainerContent(GraphContainer container) {
    return containerContentMap.get(container);
  }

  /**
   * Clear resources
   */
  private void clear() {
    containerContentMap = null;
  }

  /**
   * Container content for cluster handling
   */
  private class ContainerContent {

    private final int leftMargin;
    private final int rightMargin;
    private final DNode leftNode;
    private final DNode rightNode;
    private final GraphContainer container;

    private ContainerContent(GraphContainer container) {
      this.container = container;
      this.leftNode = newClusterNode();
      this.rightNode = newClusterNode();
      this.leftMargin = margin(true);
      this.rightMargin = margin(false);
    }

    private int minlen() {
      ContainerDrawProp containerDrawProp = getContainerDrawProp(container);
      FlatPoint labelSize = containerDrawProp.getLabelSize();
      if (labelSize == null) {
        return 0;
      }
      return needFlip ? (int) labelSize.getHeight() : (int) labelSize.getWidth();
    }

    private int margin(boolean left) {
      ContainerDrawProp containerDrawProp = getContainerDrawProp(container);
      if (!needFlip) {
        return (int) containerDrawProp.getHorMargin();
      }
      return flipGetMargin(container, left, false);
    }

    private DNode newClusterNode() {
      DNode node = new DNode(null, 0, 1, 0);
      node.setContainer(container);
      node.switchAuxModel();
      return node;
    }
  }
}
