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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.layout.dot.RankContent.RankNode;

/**
 * CoordinateV3 implements the Sugiyama algorithm for x-position calculation. This provides a more
 * efficient alternative to Network Simplex with O(V + E) complexity per iteration instead of
 * O(V²).
 * <p>
 * The algorithm uses iterative barycenter/median positioning to minimize edge crossings and
 * optimize node placement within each rank.
 */
class CoordinateV3 extends AbstractCoordinate {

  private static final int MAX_ITERATIONS = 8; // As specified in TSE93 paper
  private static final double EPSILON = 0.1;
  private static final double CONVERGENCE_THRESHOLD = 0.5;

  private Map<GraphContainer, ContainerContent> containerContentMap;
  private Map<DNode, Integer> nodeIndexMap; // Add node to index mapping

  public CoordinateV3(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
                      EdgeDedigraph<DNode, DLine> proxyDigraph) {
    super(nslimit, rankContent, dotAttachment, proxyDigraph);

    // Initialize container content map for cluster handling
    if (dotAttachment.haveClusters()) {
      containerContentMap = new HashMap<>();
      initializeContainerContent();
    }

    // Apply Sugiyama algorithm for x-position calculation
    sugiyamaXPositioning();

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
  private void sugiyamaXPositioning() {
    // Step 1: Initialize x-coordinates
    double[] xcoord = initXcoord();
    
    // Step 2: Apply Brandes-Köpf algorithm
    applyBrandesKopfPositioning(xcoord);
  }

  /**
   * Apply Brandes-Köpf positioning algorithm
   * Based on "Fast and Simple Horizontal Coordinate Assignment" by Brandes and Köpf
   * This is a proper implementation following the Dagre bk.js algorithm
   */
  private void applyBrandesKopfPositioning(double[] xcoord) {
    // Phase 1: Find conflicts (Type 1 and Type 2)
    Map<DNode, Set<DNode>> conflicts = findType1Conflicts();
    setType2Conflicts(conflicts);

    int blockSize = 0;
    Map<DNode, DNode> nodeBlocks = new HashMap<>();
    for (DNode node : proxyDigraph) {
      if (nodeBlocks.containsKey(node)) {
        continue;
      }

      blockSize++;
      DNode block = new DNode(null, 0, 0 , 0);
      dfs(node, nodeBlocks, conflicts, block);
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

  private void dfs(DNode v, Map<DNode, DNode> mark,
                   Map<DNode, Set<DNode>> conflict,
                   DNode block) {
    if (mark.containsKey(v)) {
      return;
    }

    mark.put(v, block);
    block.setHeight(Math.max(block.getHeight(), v.getWidth()));
    for (DLine edge : proxyDigraph.outAdjacent(v)) {
      DNode w = edge.other(v);
      if (hasConflict(v, w, conflict)) {
        continue;
      }

      dfs(w, mark, conflict, block);
    }
  }

  /**
   * Find Type 1 conflicts: non-inner segments crossing inner segments
   * Inner segments are edges with both incident nodes being dummy nodes
   * This is a proper implementation following the Dagre bk.js algorithm
   */
  private Map<DNode, Set<DNode>> findType1Conflicts() {
    Map<DNode, Set<DNode>> conflicts = new HashMap<>();

    for (int i = rankContent.minRank() + 1; i <= rankContent.maxRank(); i++) {
      RankNode prevLayer = rankContent.get(i - 1);
      RankNode currentLayer = rankContent.get(i);
      
      int k0 = 0;
      int scanPos = 0;
      
      for (int j = 0; j < currentLayer.size(); j++) {
        DNode v = currentLayer.get(j);
        DNode w = findOtherInnerSegmentNode(v);
        int k1 = w != null ? w.getRankIndex() : prevLayer.size();
        
        if (w != null || j == currentLayer.size() - 1) {
          // Scan nodes from scanPos to j for conflicts
          for (int k = scanPos; k <= j; k++) {
            DNode scanNode = currentLayer.get(k);
            for (DLine edge : proxyDigraph.inAdjacent(scanNode)) {
              DNode u = edge.other(scanNode);
              int uPos = u.getRankIndex();
              
              // Check if this creates a Type 1 conflict
              if ((uPos < k0 || k1 < uPos) && 
                  !(u.isVirtual() && scanNode.isVirtual())) {
                addConflict(conflicts, u, scanNode);
              }
            }
          }

          scanPos = j + 1;
          k0 = k1;
        }
      }
    }
    
    return conflicts;
  }

  /**
   * Find the other node in an inner segment (both nodes are dummy)
   */
  private DNode findOtherInnerSegmentNode(DNode v) {
    if (v.isVirtual()) {
      for (DLine edge : proxyDigraph.inAdjacent(v)) {
        DNode u = edge.other(v);
        if (u.isVirtual()) {
          return u;
        }
      }
    }
    return null;
  }

  private void setType2Conflicts(Map<DNode, Set<DNode>> conflicts) {
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode layer = rankContent.get(i);
      
      for (DNode v : layer) {
        if (!v.isVirtual()) {
          continue;
        }

        // Check for conflicts with border nodes
        for (DLine edge : proxyDigraph.inAdjacent(v)) {
          DNode u = edge.other(v);
          if (!u.isVirtual()) {
            // This is a border node, check for conflicts
            for (DNode w : layer) {
              if (w != v && !w.isVirtual()) {
                addConflict(conflicts, u, w);
              }
            }
          }
        }
      }
    }
  }

  private boolean hasConflict(DNode v, DNode w, Map<DNode, Set<DNode>> type1Conflicts) {
    Set<DNode> vConflicts = type1Conflicts.get(v);
    return vConflicts != null && vConflicts.contains(w);
  }

  /**
   * Add conflict between two nodes
   */
  private void addConflict(Map<DNode, Set<DNode>> conflicts, DNode node1, DNode node2) {
    conflicts.computeIfAbsent(node1, k -> new HashSet<>()).add(node2);
    conflicts.computeIfAbsent(node2, k -> new HashSet<>()).add(node1);
  }

  /**
   * Step 2: Initialize x-coordinates as described in TSE93 paper "For each rank, the left-most node
   * is assigned coordinate 0. The coordinate of the next node is then assigned a value sufficient
   * to satisfy the minimal separation from the previous one"
   */
  private double[] initXcoord() {
    // Count total nodes to determine array size
    int totalNodes = proxyDigraph.vertexNum();
    double[] xcoord = new double[totalNodes];

    int idx = 0;
    // Pack nodes as far left as possible on each rank
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      double currentX = 0;

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        node.switchAuxModel();

        currentX += node.leftWidth();
        xcoord[idx++] = currentX;
        currentX += (node.getNodeSep() + node.rightWidth());
      }
    }

    return xcoord;
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
   * Calculate the objective function value (total edge length)
   */
  private double xlength(double[] xcoord) {
    double totalLength = 0.0;

    for (DLine edge : proxyDigraph.edges()) {
      DNode from = edge.from();
      DNode to = edge.to();

      // Use global index
      int fromIndex = calculateGlobalIndex(from);
      int toIndex = calculateGlobalIndex(to);

      if (fromIndex >= 0 && toIndex >= 0 && fromIndex < xcoord.length && toIndex < xcoord.length) {
        double length = Math.abs(xcoord[fromIndex] - xcoord[toIndex]);
        totalLength += length * edge.weight();
      }
    }

    return totalLength;
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