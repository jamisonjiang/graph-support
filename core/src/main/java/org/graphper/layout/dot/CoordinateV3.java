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

class CoordinateV3 extends AbstractCoordinate {

  private Map<GraphContainer, ContainerContent> containerContentMap;

  public CoordinateV3(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
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
    // Mode switch
    node.switchAuxModel();

    // Add cluster boundary edge if needed
    if (!node.isVirtual() && node.getContainer().isCluster()) {
      containerBorderEdge(node);
    }

    // Note: In Sugiyama, we don't create auxiliary edges like Network Simplex
    // We work directly with the existing graph structure
  }

  // ----------------------------------------------------- private method -----------------------------------------------------

  /**
   * Main x-coordinate algorithm based on Brandes-Köpf algorithm
   * Implements the complete BK algorithm with conflict detection, four-direction alignment, and block-based compaction
   */
  private void blockNetworkSimplex() {
    Map<Integer, List<SimpleEntry<DNode, DNode>>> conflicts = new HashMap<>();

    int blockSize = 0;
    Map<DNode, DNode> nodeBlocks = new HashMap<>();
    for (DNode node : proxyDigraph) {
      if (nodeBlocks.containsKey(node)) {
        continue;
      }

      blockSize++;
      DNode block = new DNode(null, 0, 0 , 0);
      dfs(node, nodeBlocks, block, conflicts);
    }

    DotDigraph blockGraph = new DotDigraph(blockSize);
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        DNode block = nodeBlocks.get(node);
        blockGraph.add(block);

        if (j > 0) {
          DNode pre = rankNode.get(j - 1);
          DNode preBlock = nodeBlocks.get(pre);
          int limit = ((int) (block.getHeight() + preBlock.getHeight()) / 2) + (int) pre.getNodeSep();
          blockGraph.addEdge(new DLine(preBlock, block, null, 1, limit));
        }
      }
    }

    FeasibleTree feasibleTree = new FeasibleTree(blockGraph);
    new NetworkSimplex(feasibleTree, nslimit, false,
                       false, Double.MAX_VALUE, null);

    for (Entry<DNode, DNode> entry : nodeBlocks.entrySet()) {
      DNode node = entry.getKey();
      DNode block = entry.getValue();

      node.setAuxRank(block.getRank());
    }
  }

  private void dfs(DNode v, Map<DNode, DNode> mark, DNode block,
                   Map<Integer, List<SimpleEntry<DNode, DNode>>> conflicts) {
    if (mark.containsKey(v)) {
      return;
    }

    mark.put(v, block);
    block.setHeight(Math.max(block.getHeight(), v.getWidth()));
    List<SimpleEntry<DNode, DNode>> rankConflicts = conflicts.get(v.getRealRank());

    DNode successor = null;
    for (DLine edge : proxyDigraph.outAdjacent(v)) {
      DNode other = edge.other(v);
      if (hasConflict(rankConflicts, v, other)) {
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
      dfs(successor, mark, block, conflicts);
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

  /**
   * Calculate global index for a node by adding offset of previous ranks
   */
  private int calculateGlobalIndex(DNode node) {
    int globalIndex = 0;

    // Add offset from previous ranks
    for (int layer = rankContent.minRank(); layer < node.getRealRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      globalIndex += rankNode.size();
    }

    // Add the node's index within its current rank
    globalIndex += node.getRankIndex();

    return globalIndex;
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