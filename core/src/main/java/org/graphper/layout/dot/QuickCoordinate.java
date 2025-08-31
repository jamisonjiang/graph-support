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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.def.EdgeDedigraph;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.Asserts;

/**
 * Quick coordinate implementation for DOT layout.
 *
 * <p>This implementation uses an optimized approach combining network simplex and
 * Brandes/Köpf algorithm to achieve faster x-position calculation while maintaining good visual
 * quality. Suitable for large graphs where classic DOT performance is insufficient.
 */
class QuickCoordinate extends AbstractCoordinate {

  public QuickCoordinate(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
                         EdgeDedigraph<DNode, DLine> proxyDigraph) {
    super(nslimit, rankContent, dotAttachment, proxyDigraph);

    // Mix dot network-simplex and Brandes/Köpf algorithm to x-position
    blockNetworkSimplex();

    // Final x coordinate setting
    positive();
  }

  // ----------------------------------------------------- private method -----------------------------------------------------

  private void blockNetworkSimplex() {
    DotDigraph blockGraph = new DotDigraph(dotAttachment.getDotDigraph().vertexNum());
    addClusterBorderEdge(blockGraph, dotAttachment.getGraphviz());

    Map<Integer, Set<ConflictPair>> conflicts = new HashMap<>();
    accessNodes(null, (n, c, cb) -> cb.refreshRankRange(n));
    addClusterConflict(conflicts);

    Map<DNode, DNode> nodeBlocks = new HashMap<>();
    
    // Process virtual nodes first
    processNodesByType(nodeBlocks, conflicts, true);
    
    // Process remaining nodes
    processNodesByType(nodeBlocks, conflicts, false);

    // Connect flow blocks
    for (DNode node : proxyDigraph) {
      DNode block = nodeBlocks.get(node);
      connectFlowBlocks(nodeBlocks, blockGraph, node, block);
    }

    // Build block graph with rank constraints
    buildBlockGraphWithRankConstraints(blockGraph, nodeBlocks);

    // Run network simplex algorithm
    runNetworkSimplex(blockGraph);

    // Update node positions and run median positioning
    updateNodePositionsFromBlocks(nodeBlocks);

    medianpos(0);
    medianpos(1);
  }

  private void processNodesByType(Map<DNode, DNode> nodeBlocks, 
                                  Map<Integer, Set<ConflictPair>> conflicts, 
                                  boolean isVirtual) {
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        
        if (isVirtual) {
          if (notFirstDiscoveryVirtualNode(node) || nodeBlocks.containsKey(node)) {
            continue;
          }
        } else {
          if (nodeBlocks.containsKey(node)) {
            continue;
          }
        }

        DNode block = createNewBlock();
        dfs(node, nodeBlocks, block, isVirtual, conflicts);
      }
    }
  }

  private DNode createNewBlock() {
    DNode block = new DNode(null, 0, 0, 0);
    block.setLow(Integer.MAX_VALUE);
    block.setLim(Integer.MIN_VALUE);
    return block;
  }

  private void buildBlockGraphWithRankConstraints(DotDigraph blockGraph, 
                                                 Map<DNode, DNode> nodeBlocks) {
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        DNode block = nodeBlocks.get(node);
        
        blockGraph.add(block);
        containerBorderEdge(node, block, blockGraph);

        if (j > 0) {
          addRankSeparationConstraint(rankNode, j, block, nodeBlocks, blockGraph);
        }
      }
    }
  }

  private void addRankSeparationConstraint(RankNode rankNode, int j, DNode block,
                                          Map<DNode, DNode> nodeBlocks, DotDigraph blockGraph) {
    DNode pre = rankNode.get(j - 1);
    DNode preBlock = nodeBlocks.get(pre);
    
    if (block == preBlock) {
      return;
    }

    int limit = ((int) (block.getHeight() + preBlock.getHeight()) / 2) + (int) pre.getNodeSep();
    blockGraph.addEdge(new DLine(preBlock, block, null, 1, limit));
    adjClusterEdge(pre, rankNode.get(j), preBlock, block, blockGraph);
  }

  private void runNetworkSimplex(DotDigraph blockGraph) {
    FeasibleTree feasibleTree = new FeasibleTree(blockGraph);
    new NetworkSimplex(feasibleTree, nslimit, false, false, false, Double.MAX_VALUE, null);
  }

  private void updateNodePositionsFromBlocks(Map<DNode, DNode> nodeBlocks) {
    for (Entry<DNode, DNode> entry : nodeBlocks.entrySet()) {
      DNode node = entry.getKey();
      DNode block = entry.getValue();
      if (node.isVirtual()) {
        node.setAuxRank(block.getRank());
        continue;
      }

      double offset = (node.leftWidth() + node.rightWidth()) / 2 - node.leftWidth();
      node.setAuxRank(block.getRank() - (int) offset);
    }
  }

  private void addClusterConflict(Map<Integer, Set<ConflictPair>> conflicts) {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    addClusterConflict(dotAttachment.getGraphviz(), conflicts);
  }

  private void addClusterConflict(GraphContainer graphContainer,
                                  Map<Integer, Set<ConflictPair>> conflicts) {
    Iterable<Cluster> clusters = dotAttachment.clusters(graphContainer);
    for (Cluster cluster : clusters) {
      addClusterConflict(cluster, conflicts);
    }

    if (!graphContainer.isCluster()) {
      return;
    }

    ContainerBorder containerBorder = getContainerBorder(graphContainer);
    if (containerBorder == null) {
      return;
    }

    Map<Integer, int[]> rankIndexRange = containerBorder.rankIndexRange;
    if (rankIndexRange == null || rankIndexRange.size() == 1) {
      return;
    }

    addClusterBorderAsConflict(conflicts, containerBorder, rankIndexRange);
  }

  private static void addClusterBorderAsConflict(Map<Integer, Set<ConflictPair>> conflicts,
                                                 ContainerBorder containerBorder,
                                                 Map<Integer, int[]> rankIndexRange) {
    int i = containerBorder.min;
    int preRank = i - 1;
    int[] pre = null;
    int[] current;
    do {
      current = rankIndexRange.get(i);
      if (current == null) {
        /*
         * Insert label nodes cause the entire rank only contains virtual nodes,
         * it can cause the cluster's rank without nodes, we already fix the empty cluster rank
         * in mincross step but broke in LabelSupplement due to new label rank, so for this step
         * we choose broke any edges cross the multi cluster ranks
         */
        continue;
      }

      if (pre == null) {
        preRank = i;
        pre = current;
        continue;
      }

      Asserts.illegalArgument(pre.length != 2 || current.length != 2, StringUtils.EMPTY);

      int crossRows = i - preRank;
      Set<ConflictPair> conflictPairs = conflicts.computeIfAbsent(preRank, k -> new HashSet<>());
      conflictPairs.add(new ConflictPair(pre[0] - 0.5f, current[0] - 0.5f, crossRows));
      conflictPairs.add(new ConflictPair(pre[1] + 0.5f, current[1] + 0.5f, crossRows));
      preRank = i;
      pre = current;
    } while (i++ <= containerBorder.max);
  }

  private void containerBorderEdge(DNode node, DNode block, DotDigraph blockGraph) {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    ContainerContent containerContent = getContainerContent(node.getContainer());
    if (containerContent.container.isGraphviz()) {
      return;
    }
    blockGraph.addEdge(new DLine(containerContent.leftNode, block, 0,
                                    (int) (containerContent.leftMargin + block.topHeight()), false));
    blockGraph.addEdge(new DLine(block, containerContent.rightNode, 0,
                                 (int) (containerContent.rightMargin + block.bottomHeight()), false));
  }

  private void adjClusterEdge(DNode pre, DNode current,
                              DNode preBlock, DNode currentBlock,
                              DotDigraph blockGraph) {
    if (!dotAttachment.haveClusters()) {
      return;
    }

    GraphContainer commonParent = dotAttachment.commonParent(pre, current);
    if (commonParent == pre.getContainer() && commonParent == current.getContainer()) {
      return;
    }

    if (commonParent == pre.getContainer()) {

      ContainerContent containerContent = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, current)
      );
      blockGraph.addEdge(new DLine(preBlock, containerContent.leftNode, 0,
                                   (int) (20 + preBlock.bottomHeight()), false));
    }
    if (commonParent == current.getContainer()) {

      ContainerContent containerContent = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, pre)
      );
      blockGraph.addEdge(new DLine(containerContent.rightNode, currentBlock, 0,
                                   (int) (20 + currentBlock.topHeight()), false));
    } else {

      ContainerContent left = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, pre)
      );
      ContainerContent right = getContainerContent(
          dotAttachment.clusterDirectContainer(commonParent, current)
      );
      if (left != null && right != null) {
        blockGraph.addEdge(new DLine(left.rightNode, right.leftNode, 0, 16, false));
      }
    }
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
                   Map<Integer, Set<ConflictPair>> conflicts) {
    if (mark.containsKey(v)) {
      return;
    }

    mark.put(v, block);
    block.setLow(Math.min(block.getLow(), v.getRealRank()));
    block.setLim(Math.max(block.getLim(), v.getRealRank()));
    block.setHeight(Math.max(block.getHeight(), v.leftWidth() + v.rightWidth()));
    Set<ConflictPair> rankConflicts = conflicts.get(v.getRealRank());

    DNode successor = null;
    for (DLine edge : proxyDigraph.outAdjacent(v)) {
      DNode other = edge.other(v);
      if (hasConflict(v, other, rankConflicts, conflicts)) {
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
        rankConflicts = new HashSet<>();
        conflicts.put(v.getRealRank(), rankConflicts);
      }
      rankConflicts.add(new ConflictPair(v.getRankIndex(), successor.getRankIndex()));
      dfs(successor, mark, block, isVirtual, conflicts);
    }
  }

  private boolean hasConflict(DNode from, DNode to, Set<ConflictPair> rankConflicts,
                              Map<Integer, Set<ConflictPair>> conflicts) {
    if (from.isFlatLabelNode() || to.isFlatLabelNode()) {
      return true;
    }
    if (acrossClusterLimit(from, to)) {
      return true;
    }

    if (from == to || from.getRealRank() == to.getRealRank()) {
      return true;
    }
    if (hasConflict(rankConflicts, from, to)) {
      return true;
    }

    RankNode fr = rankContent.get(from.getRealRank());
    RankNode tr = rankContent.get(to.getRealRank());

    if (!fr.noNormalNode() && !tr.noNormalNode()) {
      return false;
    }

    if (fr.noNormalNode()) {
      Iterable<DLine> adj = proxyDigraph.inAdjacent(from);
      for (DLine line : adj) {
        from = line.other(from);
      }
    }
    if (tr.noNormalNode()) {
      Iterable<DLine> adj = proxyDigraph.outAdjacent(to);
      for (DLine line : adj) {
        to = line.other(to);
      }
    }

    if (from == null || to == null) {
      return true;
    }

    rankConflicts = conflicts.get(from.getRealRank());
    return hasConflict(rankConflicts, from, to);
  }

  private static boolean hasConflict(Set<ConflictPair> rankConflicts, DNode from, DNode to) {
    if (rankConflicts == null) {
      return false;
    }

    int crossRows = to.getRealRank() - from.getRealRank();
    for (ConflictPair conflict : rankConflicts) {
      if (conflict.crossRows != crossRows) {
        continue;
      }

      float fRankIdx = conflict.lowRankIdx;
      float tRankIdx = conflict.highRankIdx;

      if (fRankIdx < from.getRankIndex() != tRankIdx < to.getRankIndex()) {
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
          if (!allowMedian(node)) {
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
          if (!allowMedian(node)) {
            continue;
          }
          double medianPos = calculateMedianPosition(node, true);
          double constrainedPos = applySpacingConstraints(node, medianPos);
          node.setAuxRank((int) constrainedPos);
        }
      }
    }
  }

  private boolean allowMedian(DNode n) {
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

  private boolean acrossClusterLimit(DNode from, DNode to) {
    if (from.getRealRank() == to.getRealRank()) {
      return true;
    }

    GraphContainer parentContainer = DotAttachment
        .commonParent(dotAttachment.getGraphviz(), from, to);

    if (parentContainer == from.getContainer() && parentContainer == to.getContainer()) {
      return false;
    }

    if (parentContainer == from.getContainer()) {
      GraphContainer toDirC = dotAttachment.clusterDirectContainer(parentContainer, to);
      ContainerBorder containerBorder = getContainerBorder(toDirC);
      Asserts.nullArgument(containerBorder);
      return containerBorder.inRankRange(from.getRealRank());
    }

    if (parentContainer == to.getContainer()) {
      GraphContainer fromDirC = dotAttachment.clusterDirectContainer(parentContainer, from);
      ContainerBorder containerBorder = getContainerBorder(fromDirC);
      Asserts.nullArgument(containerBorder);
      return containerBorder.inRankRange(to.getRealRank());
    }

    GraphContainer fromDirC = dotAttachment.clusterDirectContainer(parentContainer, from);
    GraphContainer toDirC = dotAttachment.clusterDirectContainer(parentContainer, to);
    ContainerBorder fromClusterBorder = getContainerBorder(fromDirC);
    ContainerBorder toClusterBorder = getContainerBorder(toDirC);
    Asserts.nullArgument(fromClusterBorder);
    Asserts.nullArgument(toClusterBorder);
    return fromClusterBorder.inRankRange(toClusterBorder.min)
        || fromClusterBorder.inRankRange(toClusterBorder.max);
  }

  /**
   * Apply spacing constraints to ensure no node overlap Returns the constrained position that
   * respects minimum spacing requirements
   */
  private double applySpacingConstraints(DNode node, double desiredPos) {
    desiredPos = containerLimit(node, desiredPos);
    DNode pre = rankContent.rankPreNode(node);
    DNode next = rankContent.rankNextNode(node);

    if (pre == null && next == null) {
      return desiredPos;
    }

    if (pre != null) {
      double limit = pre.getAuxRank() + pre.rightWidth() + pre.getNodeSep() + node.leftWidth();
      if (desiredPos < limit) {
        desiredPos = limit;
      }
      desiredPos = adjContainerLimit(node, pre, true, desiredPos);
    }

    if (next != null) {
      double limit = next.getAuxRank() - (node.rightWidth() + node.getNodeSep() + next.leftWidth());
      if (desiredPos > limit) {
        desiredPos = limit;
      }
      desiredPos = adjContainerLimit(node, next, false, desiredPos);
    }

    return desiredPos;
  }

  private double containerLimit(DNode node, double desirePos) {
    if (!dotAttachment.haveClusters()) {
      return desirePos;
    }

    ContainerContent containerContent = getContainerContent(node.getContainer());
    if (containerContent.container.isGraphviz()) {
      return desirePos;
    }

    DNode leftNode = containerContent.leftNode;
    DNode rightNode = containerContent.rightNode;
    desirePos = Math.max(leftNode.getRank() + containerContent.leftMargin + node.leftWidth(), desirePos);
    desirePos = Math.min(rightNode.getRank() - containerContent.rightMargin - node.rightWidth(), desirePos);
    return desirePos;
  }

  private double adjContainerLimit(DNode node, DNode adjNode, boolean pre, double desirePos) {
    if (!dotAttachment.haveClusters()) {
      return desirePos;
    }

    GraphContainer commonParent = dotAttachment.commonParent(node, adjNode);
    if (commonParent == adjNode.getContainer() || commonParent != node.getContainer()) {
      return desirePos;
    }

    ContainerContent containerContent = getContainerContent(
        dotAttachment.clusterDirectContainer(commonParent, adjNode)
    );

    if (pre) {
      return Math.max(containerContent.rightNode.getRank() + node.leftWidth() + 20, desirePos) ;
    } else {
      return Math.min(containerContent.leftNode.getRank() - node.rightWidth() - 20, desirePos) ;
    }
  }

  private static class ConflictPair {
    private final float lowRankIdx;
    private final float highRankIdx;

    private int crossRows = 1;

    private ConflictPair(float lowRankIdx, float highRankIdx) {
      this.lowRankIdx = lowRankIdx;
      this.highRankIdx = highRankIdx;
    }

    private ConflictPair(float lowRankIdx, float highRankIdx, int crossRows) {
      this.lowRankIdx = lowRankIdx;
      this.highRankIdx = highRankIdx;
      this.crossRows = crossRows;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ConflictPair that = (ConflictPair) o;
      return lowRankIdx == that.lowRankIdx && highRankIdx == that.highRankIdx;
    }

    @Override
    public int hashCode() {
      return Objects.hash(lowRankIdx, highRankIdx);
    }
  }
}
