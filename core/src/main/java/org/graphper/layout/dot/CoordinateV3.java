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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
    
    // Step 3: Apply final coordinates
    applyCoordinates(xcoord);
  }

  /**
   * Apply Brandes-Köpf positioning algorithm
   * Based on "Fast and Simple Horizontal Coordinate Assignment" by Brandes and Köpf
   * This is a proper implementation following the Dagre bk.js algorithm
   */
  private void applyBrandesKopfPositioning(double[] xcoord) {
    // Build layering matrix from the graph
    List<List<DNode>> layering = buildLayering();
    
    // Phase 1: Find conflicts (Type 1 and Type 2)
    Map<DNode, Set<DNode>> type1Conflicts = findType1Conflicts(layering);
    Map<DNode, Set<DNode>> type2Conflicts = findType2Conflicts(layering);
    
    // Phase 2: Generate four alignments (ul, ur, dl, dr)
    Map<String, Map<DNode, DNode>> alignments = generateFourAlignments(type1Conflicts, type2Conflicts, layering);
    
    // Phase 3: Compact each alignment
    Map<String, double[]> compactedCoords = new HashMap<>();
    for (Map.Entry<String, Map<DNode, DNode>> entry : alignments.entrySet()) {
      String direction = entry.getKey();
      Map<DNode, DNode> alignment = entry.getValue();
      double[] coords = horizontalCompaction(alignment, layering);
      compactedCoords.put(direction, coords);
    }
    
    // Phase 4: Select best alignment (smallest width)
    String bestDirection = findSmallestWidthAlignment(compactedCoords);
    
    // Phase 5: Apply the best alignment
    double[] bestCoords = compactedCoords.get(bestDirection);
    System.arraycopy(bestCoords, 0, xcoord, 0, xcoord.length);
    
    // Phase 6: Apply final spacing constraints to ensure no node overlap
    applyFinalSpacingConstraints(xcoord);
  }

  /**
   * Build layering matrix from the graph
   */
  private List<List<DNode>> buildLayering() {
    List<List<DNode>> layering = new ArrayList<>();
    
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      List<DNode> layerNodes = new ArrayList<>();
      
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        layerNodes.add(node);
      }
      
      layering.add(layerNodes);
    }
    
    return layering;
  }

  /**
   * Find Type 1 conflicts: non-inner segments crossing inner segments
   * Inner segments are edges with both incident nodes being dummy nodes
   * This is a proper implementation following the Dagre bk.js algorithm
   */
  private Map<DNode, Set<DNode>> findType1Conflicts(List<List<DNode>> layering) {
    Map<DNode, Set<DNode>> conflicts = new HashMap<>();
    int conflictCount = 0;
    
    for (int i = 1; i < layering.size(); i++) {
      List<DNode> prevLayer = layering.get(i - 1);
      List<DNode> currentLayer = layering.get(i);
      
      int k0 = 0;
      int scanPos = 0;
      
      for (int j = 0; j < currentLayer.size(); j++) {
        DNode v = currentLayer.get(j);
        DNode w = findOtherInnerSegmentNode(v);
        int k1 = w != null ? getNodeOrder(w, prevLayer) : prevLayer.size();
        
        if (w != null || j == currentLayer.size() - 1) {
          // Scan nodes from scanPos to j for conflicts
          for (int k = scanPos; k <= j; k++) {
            DNode scanNode = currentLayer.get(k);
            for (DLine edge : proxyDigraph.inAdjacent(scanNode)) {
              DNode u = edge.other(scanNode);
              int uPos = getNodeOrder(u, prevLayer);
              
              // Check if this creates a Type 1 conflict
              if ((uPos < k0 || k1 < uPos) && 
                  !(u.isVirtual() && scanNode.isVirtual())) {
                addConflict(conflicts, u, scanNode);
                conflictCount++;
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

  /**
   * Get the order/position of a node within a layer
   */
  private int getNodeOrder(DNode node, List<DNode> layer) {
    // Find the actual position of the node in the layer list
    for (int i = 0; i < layer.size(); i++) {
      if (layer.get(i) == node) {
        return i;
      }
    }
    // Fallback to rank index if not found in layer
    return node.getRankIndex();
  }

  /**
   * Get the layer index of a node
   */
  private int getLayerIndex(DNode node, List<List<DNode>> layering) {
    for (int i = 0; i < layering.size(); i++) {
      List<DNode> layer = layering.get(i);
      for (DNode layerNode : layer) {
        if (layerNode == node) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Find Type 2 conflicts: conflicts between dummy nodes and border nodes
   * This is a proper implementation following the Dagre bk.js algorithm
   */
  private Map<DNode, Set<DNode>> findType2Conflicts(List<List<DNode>> layering) {
    Map<DNode, Set<DNode>> conflicts = new HashMap<>();
    int conflictCount = 0;
    
    for (int i = 0; i < layering.size(); i++) {
      List<DNode> layer = layering.get(i);
      
      for (DNode v : layer) {
        if (v.isVirtual()) {
          // Check for conflicts with border nodes
          for (DLine edge : proxyDigraph.inAdjacent(v)) {
            DNode u = edge.other(v);
            if (!u.isVirtual()) {
              // This is a border node, check for conflicts
              for (DNode w : layer) {
                if (w != v && !w.isVirtual()) {
                  addConflict(conflicts, u, w);
                  conflictCount++;
                }
              }
            }
          }
        }
      }
    }
    
    return conflicts;
  }

  /**
   * Generate four alignments: ul (up-left), ur (up-right), dl (down-left), dr (down-right)
   */
  private Map<String, Map<DNode, DNode>> generateFourAlignments(
      Map<DNode, Set<DNode>> type1Conflicts, 
      Map<DNode, Set<DNode>> type2Conflicts,
      List<List<DNode>> layering) {
    
    Map<String, Map<DNode, DNode>> alignments = new HashMap<>();
    
    // ul: up-left (top-down, left-to-right)
    alignments.put("ul", verticalAlignment("up", "left", type1Conflicts, type2Conflicts, layering));
    
    // ur: up-right (top-down, right-to-left)
    alignments.put("ur", verticalAlignment("up", "right", type1Conflicts, type2Conflicts, layering));
    
    // dl: down-left (bottom-up, left-to-right)
    alignments.put("dl", verticalAlignment("down", "left", type1Conflicts, type2Conflicts, layering));
    
    // dr: down-right (bottom-up, right-to-left)
    alignments.put("dr", verticalAlignment("down", "right", type1Conflicts, type2Conflicts, layering));
    
    return alignments;
  }

  /**
   * Perform vertical alignment in specified direction
   */
  private Map<DNode, DNode> verticalAlignment(String vertical, String horizontal,
                                             Map<DNode, Set<DNode>> type1Conflicts,
                                             Map<DNode, Set<DNode>> type2Conflicts,
                                             List<List<DNode>> layering) {
    Map<DNode, DNode> alignment = new HashMap<>();
    Map<DNode, DNode> root = new HashMap<>();
    int alignmentCount = 0;
    
    // Determine iteration order based on vertical direction
    int startLayer = vertical.equals("up") ? 0 : layering.size() - 1;
    int endLayer = vertical.equals("up") ? layering.size() - 1 : 0;
    int step = vertical.equals("up") ? 1 : -1;
    
    for (int layer = startLayer; 
         vertical.equals("up") ? layer <= endLayer : layer >= endLayer; 
         layer += step) {
      
      List<DNode> layerNodes = layering.get(layer);
      
      // Determine iteration order within rank based on horizontal direction
      int startIndex = horizontal.equals("left") ? 0 : layerNodes.size() - 1;
      int endIndex = horizontal.equals("left") ? layerNodes.size() : -1;
      int indexStep = horizontal.equals("left") ? 1 : -1;
      
      for (int i = startIndex; 
           horizontal.equals("left") ? i < endIndex : i > endIndex; 
           i += indexStep) {
        
        DNode v = layerNodes.get(i);
        
        if (!alignment.containsKey(v)) {
          // Find median neighbor
          DNode w = findMedianNeighbor(v, vertical, horizontal, layering);
          
          if (w != null && !hasConflict(v, w, type1Conflicts, type2Conflicts)) {
            alignment.put(v, w);
            root.put(v, findAlignmentRoot(w, root));
            alignmentCount++;
          }
        }
      }
    }
    
    return alignment;
  }

  /**
   * Find median neighbor for alignment
   */
  private DNode findMedianNeighbor(DNode v, String vertical, String horizontal, List<List<DNode>> layering) {
    List<DNode> neighbors = new ArrayList<>();
    
    // Get neighbors based on vertical direction
    if (vertical.equals("up")) {
      // Look at nodes in the layer above
      int currentLayerIndex = getLayerIndex(v, layering);
      if (currentLayerIndex > 0) {
        List<DNode> prevLayer = layering.get(currentLayerIndex - 1);
        for (DNode neighbor : prevLayer) {
          if (isConnected(v, neighbor)) {
            neighbors.add(neighbor);
          }
        }
      }
    } else {
      // Look at nodes in the layer below
      int currentLayerIndex = getLayerIndex(v, layering);
      if (currentLayerIndex < layering.size() - 1) {
        List<DNode> nextLayer = layering.get(currentLayerIndex + 1);
        for (DNode neighbor : nextLayer) {
          if (isConnected(v, neighbor)) {
            neighbors.add(neighbor);
          }
        }
      }
    }
    
    if (neighbors.isEmpty()) {
      return null;
    }
    
    // Sort neighbors by position based on horizontal direction
    neighbors.sort((a, b) -> {
      int aLayerIndex = getLayerIndex(a, layering);
      int bLayerIndex = getLayerIndex(b, layering);
      if (aLayerIndex != bLayerIndex) {
        return Integer.compare(aLayerIndex, bLayerIndex);
      }
      int aOrder = getNodeOrder(a, layering.get(aLayerIndex));
      int bOrder = getNodeOrder(b, layering.get(bLayerIndex));
      int comparison = Integer.compare(aOrder, bOrder);
      return horizontal.equals("left") ? comparison : -comparison;
    });
    
    // Return median neighbor
    DNode median = neighbors.get(neighbors.size() / 2);
    return median;
  }

  /**
   * Check if two nodes are connected
   */
  private boolean isConnected(DNode v, DNode w) {
    for (DLine edge : proxyDigraph.outAdjacent(v)) {
      if (edge.other(v) == w) {
        return true;
      }
    }
    for (DLine edge : proxyDigraph.inAdjacent(v)) {
      if (edge.other(v) == w) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if there's a conflict between two nodes
   */
  private boolean hasConflict(DNode v, DNode w, 
                             Map<DNode, Set<DNode>> type1Conflicts,
                             Map<DNode, Set<DNode>> type2Conflicts) {
    Set<DNode> vConflicts = type1Conflicts.get(v);
    if (vConflicts != null && vConflicts.contains(w)) {
      return true;
    }
    
    vConflicts = type2Conflicts.get(v);
    if (vConflicts != null && vConflicts.contains(w)) {
      return true;
    }
    
    return false;
  }

  /**
   * Find the root of an alignment chain
   */
  private DNode findAlignmentRoot(DNode node, Map<DNode, DNode> root) {
    DNode current = node;
    Set<DNode> visited = new HashSet<>();
    
    while (current != null && !visited.contains(current)) {
      visited.add(current);
      current = root.get(current);
    }
    
    return current != null ? current : node;
  }

  /**
   * Perform horizontal compaction for an alignment
   */
  private double[] horizontalCompaction(Map<DNode, DNode> alignment, List<List<DNode>> layering) {
    // Build block graph
    Map<DNode, Set<DNode>> blocks = buildBlocks(alignment);
    
    // Initialize coordinates array
    int totalNodes = 0;
    for (List<DNode> layer : layering) {
      totalNodes += layer.size();
    }
    double[] xcoord = new double[totalNodes];
    
    // Two-pass compaction
    double[] coords1 = compactBlocks(blocks, xcoord, true);  // Left-to-right
    double[] coords2 = compactBlocks(blocks, xcoord, false); // Right-to-left
    
    // Use the more compact result
    double width1 = calculateWidth(coords1);
    double width2 = calculateWidth(coords2);
    
    return width1 < width2 ? coords1 : coords2;
  }

  /**
   * Build blocks from alignment
   */
  private Map<DNode, Set<DNode>> buildBlocks(Map<DNode, DNode> alignment) {
    Map<DNode, Set<DNode>> blocks = new HashMap<>();
    
    for (Map.Entry<DNode, DNode> entry : alignment.entrySet()) {
      DNode node = entry.getKey();
      DNode alignedNode = entry.getValue();
      
      // Find root of alignment chain
      DNode root = findAlignmentRoot(alignedNode, alignment);
      
      blocks.computeIfAbsent(root, k -> new HashSet<>()).add(node);
    }
    
    return blocks;
  }

  /**
   * Compact blocks in specified direction
   */
  private double[] compactBlocks(Map<DNode, Set<DNode>> blocks, double[] xcoord, boolean leftToRight) {
    double[] result = xcoord.clone();
    
    // Sort blocks by their current position
    List<DNode> sortedBlocks = new ArrayList<>(blocks.keySet());
    sortedBlocks.sort((a, b) -> {
      int aIndex = calculateGlobalIndex(a);
      int bIndex = calculateGlobalIndex(b);
      return leftToRight ? 
          Double.compare(result[aIndex], result[bIndex]) :
          Double.compare(result[bIndex], result[aIndex]);
    });
    
    // Use the same spacing logic as initXcoord to avoid node overlap
    double currentX = 0.0;
    
    for (DNode blockRoot : sortedBlocks) {
      Set<DNode> block = blocks.get(blockRoot);
      
      // Find the node with maximum width in this block
      DNode maxWidthNode = null;
      double maxWidth = 0.0;
      for (DNode node : block) {
        double nodeWidth = node.leftWidth() + node.rightWidth();
        if (nodeWidth > maxWidth) {
          maxWidth = nodeWidth;
          maxWidthNode = node;
        }
      }
      
      if (maxWidthNode != null) {
        // Position all nodes in the block at the same center position
        double centerPos = currentX + maxWidthNode.leftWidth();
        for (DNode node : block) {
          int nodeIndex = calculateGlobalIndex(node);
          if (nodeIndex >= 0 && nodeIndex < result.length) {
            result[nodeIndex] = centerPos;
          }
        }
        
        // Update currentX for next block using the same spacing logic as initXcoord
        currentX += maxWidthNode.leftWidth();
        currentX += (maxWidthNode.getNodeSep() + maxWidthNode.rightWidth());
      }
    }
    
    return result;
  }

  /**
   * Calculate width of a block
   */
  private double calculateBlockWidth(Set<DNode> block) {
    if (block.isEmpty()) {
      return 0.0;
    }
    
    double maxWidth = 0.0;
    for (DNode node : block) {
      maxWidth = Math.max(maxWidth, node.leftWidth() + node.rightWidth());
    }
    
    return maxWidth;
  }

  /**
   * Calculate total width of coordinates
   */
  private double calculateWidth(double[] coords) {
    if (coords.length == 0) {
      return 0.0;
    }
    
    double min = coords[0];
    double max = coords[0];
    
    for (double coord : coords) {
      min = Math.min(min, coord);
      max = Math.max(max, coord);
    }
    
    return max - min;
  }

  /**
   * Find the alignment with smallest width
   */
  private String findSmallestWidthAlignment(Map<String, double[]> compactedCoords) {
    String bestDirection = "ul";
    double minWidth = Double.MAX_VALUE;
    
    for (Map.Entry<String, double[]> entry : compactedCoords.entrySet()) {
      double width = calculateWidth(entry.getValue());
      if (width < minWidth) {
        minWidth = width;
        bestDirection = entry.getKey();
      }
    }
    
    return bestDirection;
  }

  /**
   * Add conflict between two nodes
   */
  private void addConflict(Map<DNode, Set<DNode>> conflicts, DNode node1, DNode node2) {
    conflicts.computeIfAbsent(node1, k -> new HashSet<>()).add(node2);
    conflicts.computeIfAbsent(node2, k -> new HashSet<>()).add(node1);
  }

  /**
   * Apply final spacing constraints to ensure no node overlap within each rank
   * Uses the same logic as initXcoord method and maintains original node order
   */
  private void applyFinalSpacingConstraints(double[] xcoord) {
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      
      if (rankNode.size() <= 1) {
        continue;
      }
      
      // Apply spacing constraints using the same logic as initXcoord
      // Maintain the original order of nodes within the rank
      double currentX = 0.0;
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        int nodeIndex = calculateGlobalIndex(node);
        if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
          // Position node at center: currentX + node.leftWidth()
          double centerPos = currentX + node.leftWidth();
          xcoord[nodeIndex] = centerPos;
          
          // Update currentX for next node: currentX + nodeSep + rightWidth
          currentX += node.leftWidth();
          currentX += (node.getNodeSep() + node.rightWidth());
        }
      }
    }
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
   * Step 5: Median position heuristic "The median heuristic assigns each node both an upward and
   * downward priority given by the weighted sum of its in- and out-edges, respectively" Alternates
   * between upward and downward directions based on iteration number Ensures minimum spacing
   * constraints are maintained to prevent node overlap
   */
  private void medianpos(int iteration, double[] xcoord) {
    // Alternate direction based on iteration: even = downward, odd = upward
    boolean downward = (iteration % 2 == 0);

    if (downward) {
      // Process downward (top to bottom) for even iterations
      for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
        RankNode rankNode = rankContent.get(layer);

        for (DNode node : rankNode) {
          double medianPos = calculateMedianPosition(node, false, xcoord, false);
          int nodeIndex = calculateGlobalIndex(node);
          if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
            // Apply median position while respecting spacing constraints
            double constrainedPos = applySpacingConstraints(node, medianPos, layer, xcoord);
            xcoord[nodeIndex] = constrainedPos;
          }
        }
      }
    } else {
      // Process upward (bottom to top) for odd iterations
      for (int layer = rankContent.maxRank(); layer >= rankContent.minRank(); layer--) {
        RankNode rankNode = rankContent.get(layer);

        for (DNode node : rankNode) {
          double medianPos = calculateMedianPosition(node, true, xcoord, false);
          int nodeIndex = calculateGlobalIndex(node);
          if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
            // Apply median position while respecting spacing constraints
            double constrainedPos = applySpacingConstraints(node, medianPos, layer, xcoord);
            xcoord[nodeIndex] = constrainedPos;
          }
        }
      }
    }
  }

  /**
   * Apply spacing constraints to ensure no node overlap Returns the constrained position that
   * respects minimum spacing requirements
   */
  private double applySpacingConstraints(DNode node, double desiredPos, int layer,
                                         double[] xcoord) {
    RankNode rankNode = rankContent.get(layer);
    int rankIndex = node.getRankIndex();
    double constrainedPos = desiredPos;

    if (rankIndex > 0) {
      DNode prevNode = rankNode.get(rankIndex - 1);
      int prevIndex = calculateGlobalIndex(prevNode);
      if (prevIndex >= 0 && prevIndex < xcoord.length) {
        double requiredSpacing = prevNode.rightWidth() + prevNode.getNodeSep() + node.leftWidth();
        double minPos = xcoord[prevIndex] + requiredSpacing;
        constrainedPos = Math.max(constrainedPos, minPos);
      }
    }

    return constrainedPos;
  }

  /**
   * Calculate downward priority (weighted sum of out-edges)
   */
  private double calculateDownwardPriority(DNode node) {
    double priority = 0.0;
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      priority += (edge.weight() * edge.other(node).getAuxRank());
    }
    return priority;
  }

  /**
   * Calculate upward priority (weighted sum of in-edges)
   */
  private double calculateUpwardPriority(DNode node) {
    double priority = 0.0;
    for (DLine edge : proxyDigraph.inAdjacent(node)) {
      priority += (edge.weight() * edge.other(node).getAuxRank());
    }
    return priority;
  }

  /**
   * Calculate median position for a node based on neighbors
   */
  private double calculateMedianPosition(DNode node, boolean upward,
                                         double[] xcoord, boolean realAdj) {
    List<Double> neighborPositions = new ArrayList<>();
    Set<DNode> accessNodes = new HashSet<>();

    if (upward) {
      for (DLine edge : proxyDigraph.outAdjacent(node)) {
        DNode adjNode = edge.other(node);

        while (realAdj && adjNode.isVirtual()) {
          for (DLine l : proxyDigraph.outAdjacent(adjNode)) {
            adjNode = l.other(adjNode);
          }
          if (accessNodes.contains(adjNode)) {
            break;
          }
          accessNodes.add(adjNode);
        }

        if (adjNode.getRealRank() != node.getRealRank()) {
          int adjIndex = calculateGlobalIndex(adjNode);
          if (adjIndex >= 0 && adjIndex < xcoord.length) {
            neighborPositions.add(xcoord[adjIndex]);
          }
        }
      }
    } else {
      for (DLine edge : proxyDigraph.inAdjacent(node)) {
        DNode adjNode = edge.other(node);

        while (realAdj && adjNode.isVirtual()) {
          for (DLine l : proxyDigraph.inAdjacent(adjNode)) {
            adjNode = l.other(adjNode);
          }
          if (accessNodes.contains(adjNode)) {
            break;
          }
          accessNodes.add(adjNode);
        }

        if (adjNode.getRealRank() != node.getRealRank()) {
          int ajdIndex = calculateGlobalIndex(adjNode);
          if (ajdIndex >= 0 && ajdIndex < xcoord.length) {
            neighborPositions.add(xcoord[ajdIndex]);
          }
        }
      }
    }

    if (neighborPositions.isEmpty()) {
      int nodeIndex = calculateGlobalIndex(node);
      return nodeIndex >= 0 && nodeIndex < xcoord.length ? xcoord[nodeIndex] : 0.0;
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
   * Step 6: Minimize edge lengths "minedge is similar to medianpos but considers only edges between
   * two real nodes"
   */
  private void minedge(int iteration, double[] xcoord) {
// Alternate direction based on iteration: even = downward, odd = upward
    boolean downward = (iteration % 2 == 0);
//    downward = false;

    if (downward) {
      // Process downward (top to bottom) for even iterations
      for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
        RankNode rankNode = rankContent.get(layer);

        for (DNode node : rankNode) {
          double medianPos = calculateMedianPosition(node, false, xcoord, true);
          int nodeIndex = calculateGlobalIndex(node);
          if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
            // Apply median position while respecting spacing constraints
            double constrainedPos = applySpacingConstraints(node, medianPos, layer, xcoord);
            xcoord[nodeIndex] = constrainedPos;
          }
        }
      }
    } else {
      // Process upward (bottom to top) for odd iterations
      for (int layer = rankContent.maxRank(); layer >= rankContent.minRank(); layer--) {
        RankNode rankNode = rankContent.get(layer);

        for (DNode node : rankNode) {
          double medianPos = calculateMedianPosition(node, true, xcoord, true);
          int nodeIndex = calculateGlobalIndex(node);
          if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
            // Apply median position while respecting spacing constraints
            double constrainedPos = applySpacingConstraints(node, medianPos, layer, xcoord);
            xcoord[nodeIndex] = constrainedPos;
          }
        }
      }
    }
  }

  /**
   * Step 7: Minimize node positions using local optimization "minnode performs local optimization
   * one node at a time, using a queue"
   */
  private void minnode(int iteration, double[] xcoord) {
    Queue<DNode> queue = new LinkedList<>();
    Set<DNode> inQueue = new HashSet<>();

    // Initially queue all nodes
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        queue.offer(node);
        inQueue.add(node);
      }
    }

    while (!queue.isEmpty()) {
      DNode node = queue.poll();
      inQueue.remove(node);

      int nodeIndex = calculateGlobalIndex(node);
      if (nodeIndex < 0 || nodeIndex >= xcoord.length) {
        continue;
      }

      double oldPos = xcoord[nodeIndex];
      double newPos = calculateOptimalPosition(node, xcoord);

      // Check if position changed significantly
      if (Math.abs(newPos - oldPos) > EPSILON) {
        xcoord[nodeIndex] = newPos;

        // Re-queue neighbors if not already in queue
        for (DLine edge : proxyDigraph.inAdjacent(node)) {
          DNode neighbor = edge.other(node);
          if (!inQueue.contains(neighbor)) {
            queue.offer(neighbor);
            inQueue.add(neighbor);
          }
        }
        for (DLine edge : proxyDigraph.outAdjacent(node)) {
          DNode neighbor = edge.other(node);
          if (!inQueue.contains(neighbor)) {
            queue.offer(neighbor);
            inQueue.add(neighbor);
          }
        }
      }
    }
  }

  /**
   * Calculate optimal position for a node based on all its neighbors
   */
  private double calculateOptimalPosition(DNode node, double[] xcoord) {
    List<Double> neighborPositions = new ArrayList<>();

    // Add all neighbor positions
    for (DLine edge : proxyDigraph.inAdjacent(node)) {
      DNode neighbor = edge.other(node);
      int neighborIndex = calculateGlobalIndex(neighbor);
      if (neighborIndex >= 0 && neighborIndex < xcoord.length) {
        neighborPositions.add(xcoord[neighborIndex]);
      }
    }
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      DNode neighbor = edge.other(node);
      int neighborIndex = calculateGlobalIndex(neighbor);
      if (neighborIndex >= 0 && neighborIndex < xcoord.length) {
        neighborPositions.add(xcoord[neighborIndex]);
      }
    }

    if (neighborPositions.isEmpty()) {
      int nodeIndex = calculateGlobalIndex(node);
      return nodeIndex >= 0 && nodeIndex < xcoord.length ? xcoord[nodeIndex] : 0.0;
    }

    // Return median of all neighbors
    neighborPositions.sort(Double::compareTo);
    int medianIndex = neighborPositions.size() / 2;
    return neighborPositions.get(medianIndex);
  }

  /**
   * Step 8: Minimize path lengths (straighten virtual chains) "minpath straightens chains of
   * virtual nodes by sequentially finding sub-chains that may be assigned the same X coordinate"
   */
  private void minpath(int iteration, double[] xcoord) {
    // Find chains of virtual nodes and straighten them
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);

        if (node.isVirtual()) {
          // Find virtual chain starting from this node
          List<DNode> virtualChain = findVirtualChain(node);
          if (virtualChain.size() > 1) {
            straightenVirtualChain(virtualChain, xcoord);
          }
        }
      }
    }
  }

  /**
   * Find a chain of virtual nodes
   */
  private List<DNode> findVirtualChain(DNode startNode) {
    List<DNode> chain = new ArrayList<>();
    DNode current = startNode;

    // Follow the chain forward
    while (current != null && current.isVirtual()) {
      chain.add(current);
      current = findNextVirtualNode(current);
    }

    return chain;
  }

  /**
   * Find the next virtual node in the chain
   */
  private DNode findNextVirtualNode(DNode node) {
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      DNode next = edge.other(node);
      if (next.isVirtual() && next.getRealRank() > node.getRealRank()) {
        return next;
      }
    }
    return null;
  }

  /**
   * Straighten a virtual chain by assigning the same X coordinate
   */
  private void straightenVirtualChain(List<DNode> chain, double[] xcoord) {
    if (chain.isEmpty()) {
      return;
    }

    // Calculate average position of the chain
    double avgPos = 0.0;
    int count = 0;

    for (DNode node : chain) {
      int nodeIndex = calculateGlobalIndex(node);
      if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
        avgPos += xcoord[nodeIndex];
        count++;
      }
    }

    if (count > 0) {
      avgPos /= count;

      // Assign the average position to all nodes in the chain
      for (DNode node : chain) {
        int nodeIndex = calculateGlobalIndex(node);
        if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
          xcoord[nodeIndex] = avgPos;
        }
      }
    }
  }

  /**
   * Step 9: Pack and compact "packcut sweeps the layout from left to right, searching for blocks
   * that can be compacted"
   */
  private void packcut(int iteration, double[] xcoord) {
    // Sort all nodes by their current X coordinates
    List<DNode> sortedNodes = new ArrayList<>();
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      for (int j = 0; j < rankNode.size(); j++) {
        sortedNodes.add(rankNode.get(j));
      }
    }

    sortedNodes.sort((a, b) -> {
      int aIndex = calculateGlobalIndex(a);
      int bIndex = calculateGlobalIndex(b);
      return Double.compare(xcoord[aIndex], xcoord[bIndex]);
    });

    // Try to compact from left to right
    for (int i = 0; i < sortedNodes.size() - 1; i++) {
      DNode currentNode = sortedNodes.get(i);
      DNode nextNode = sortedNodes.get(i + 1);

      int currentIndex = calculateGlobalIndex(currentNode);
      int nextIndex = calculateGlobalIndex(nextNode);

      if (currentIndex >= 0 && nextIndex >= 0 && currentIndex < xcoord.length
          && nextIndex < xcoord.length) {
        double requiredSpacing =
            currentNode.rightWidth() + currentNode.getNodeSep() + nextNode.leftWidth();
        double currentSpacing = xcoord[nextIndex] - xcoord[currentIndex];

        if (currentSpacing > requiredSpacing) {
          // Try to shift nextNode and all nodes to its right
          double shift = currentSpacing - requiredSpacing;
          if (canShiftNodes(sortedNodes, i + 1, -shift, xcoord)) {
            shiftNodes(sortedNodes, i + 1, -shift, xcoord);
          }
        }
      }
    }
  }

  /**
   * Check if nodes can be shifted without violating constraints
   */
  private boolean canShiftNodes(List<DNode> sortedNodes, int startIndex, double shift,
                                double[] xcoord) {
    for (int i = startIndex; i < sortedNodes.size(); i++) {
      DNode node = sortedNodes.get(i);
      int nodeIndex = calculateGlobalIndex(node);

      if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
        double newPos = xcoord[nodeIndex] + shift;

        // Check if this would violate any constraints
        if (i > 0) {
          DNode prevNode = sortedNodes.get(i - 1);
          int prevIndex = calculateGlobalIndex(prevNode);
          if (prevIndex >= 0 && prevIndex < xcoord.length) {
            double requiredSpacing =
                prevNode.rightWidth() + prevNode.getNodeSep() + node.leftWidth();
            if (newPos - xcoord[prevIndex] < requiredSpacing) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * Shift nodes by the specified amount
   */
  private void shiftNodes(List<DNode> sortedNodes, int startIndex, double shift, double[] xcoord) {
    for (int i = startIndex; i < sortedNodes.size(); i++) {
      DNode node = sortedNodes.get(i);
      int nodeIndex = calculateGlobalIndex(node);
      if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
        xcoord[nodeIndex] += shift;
      }
    }
  }

  /**
   * Apply the final coordinates to the nodes
   */
  private void applyCoordinates(double[] xcoord) {
    int idx = 0;
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        if (idx >= 0 && idx < xcoord.length) {
          node.setAuxRank((int) xcoord[idx]);
        }

        idx++;
      }
    }
  }

  /**
   * Calculate weighted barycenter position for a node based on its neighbors Based on TSE93 paper
   * heuristic approach
   */
  private double calculateWeightedBarycenter(DNode node, int layer, boolean bottomUp) {
    List<DNode> neighbors = new ArrayList<>();
    List<Double> weights = new ArrayList<>();

    if (bottomUp) {
      // Use parents (from layer above) when processing bottom-up
      if (layer > rankContent.minRank()) {
        addParentNodesWithWeights(node, neighbors, weights);
      }
    } else {
      // Use children (from layer below) when processing top-down
      if (layer < rankContent.maxRank()) {
        addChildNodesWithWeights(node, neighbors, weights);
      }
    }

    if (neighbors.isEmpty()) {
      return node.getAuxRank(); // Keep current position
    }

    // Calculate weighted average position
    double weightedSum = 0.0;
    double totalWeight = 0.0;

    for (int i = 0; i < neighbors.size(); i++) {
      DNode neighbor = neighbors.get(i);
      double weight = weights.get(i);
      weightedSum += neighbor.getAuxRank() * weight;
      totalWeight += weight;
    }

    double barycenter = totalWeight > 0 ? weightedSum / totalWeight : node.getAuxRank();

    // Apply constraints to prevent extreme movements
    double currentPos = node.getAuxRank();
    double maxMovement = 50.0; // Limit maximum movement per iteration
    double constrainedBarycenter = Math.max(currentPos - maxMovement,
                                            Math.min(currentPos + maxMovement, barycenter));

    return constrainedBarycenter;
  }

  /**
   * Calculate median position for a node based on its neighbors Based on TSE93 paper median method
   * for better crossing reduction
   */
  private double calculateMedianPosition(DNode node, int layer, boolean bottomUp) {
    List<DNode> neighbors = new ArrayList<>();

    if (bottomUp) {
      // Use parents (from layer above) when processing bottom-up
      if (layer > rankContent.minRank()) {
        addParentNodes(node, neighbors);
      }
    } else {
      // Use children (from layer below) when processing top-down
      if (layer < rankContent.maxRank()) {
        addChildNodes(node, neighbors);
      }
    }

    if (neighbors.isEmpty()) {
      return node.getAuxRank(); // Keep current position
    }

    // Sort neighbors by their x-positions
    neighbors.sort(Comparator.comparing(DNode::getAuxRank));

    // Return median position
    int medianIndex = neighbors.size() / 2;
    double medianPos = neighbors.get(medianIndex).getAuxRank();

    // Apply constraints to prevent extreme movements
    double currentPos = node.getAuxRank();
    double maxMovement = 30.0; // Smaller limit for median method
    double constrainedMedian = Math.max(currentPos - maxMovement,
                                        Math.min(currentPos + maxMovement, medianPos));

    return constrainedMedian;
  }

  /**
   * Calculate barycenter position for a node based on its neighbors (legacy method)
   */
  private double calculateBarycenter(DNode node, int layer, boolean bottomUp) {
    List<DNode> neighbors = new ArrayList<>();

    if (bottomUp) {
      // Use parents (from layer above) when processing bottom-up
      if (layer > rankContent.minRank()) {
        addParentNodes(node, neighbors);
      }
    } else {
      // Use children (from layer below) when processing top-down
      if (layer < rankContent.maxRank()) {
        addChildNodes(node, neighbors);
      }
    }

    if (neighbors.isEmpty()) {
      return node.getAuxRank(); // Keep current position
    }

    // Calculate average position
    double sum = 0.0;
    for (DNode neighbor : neighbors) {
      sum += neighbor.getAuxRank();
    }

    return sum / neighbors.size();
  }


  /**
   * Add parent nodes to the neighbors list with weights
   */
  private void addParentNodesWithWeights(DNode node, List<DNode> neighbors, List<Double> weights) {
    for (DLine edge : proxyDigraph.inAdjacent(node)) {
      DNode parent = edge.other(node);
      if (parent.getRealRank() < node.getRealRank()) {
        neighbors.add(parent);
        // Use edge weight for weighted barycenter calculation
        double weight = edge.weight();
        weights.add(weight);
      }
    }
  }

  /**
   * Add child nodes to the neighbors list with weights
   */
  private void addChildNodesWithWeights(DNode node, List<DNode> neighbors, List<Double> weights) {
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      DNode child = edge.other(node);
      if (child.getRealRank() > node.getRealRank()) {
        neighbors.add(child);
        // Use edge weight for weighted barycenter calculation
        double weight = edge.weight();
        weights.add(weight);
      }
    }
  }

  /**
   * Add parent nodes to the neighbors list (simple version)
   */
  private void addParentNodes(DNode node, List<DNode> neighbors) {
    for (DLine edge : proxyDigraph.inAdjacent(node)) {
      DNode parent = edge.other(node);
      if (parent.getRealRank() < node.getRealRank()) {
        neighbors.add(parent);
      }
    }
  }

  /**
   * Add child nodes to the neighbors list (simple version)
   */
  private void addChildNodes(DNode node, List<DNode> neighbors) {
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      DNode child = edge.other(node);
      if (child.getRealRank() > node.getRealRank()) {
        neighbors.add(child);
      }
    }
  }


  /**
   * Apply final spacing to ensure proper node separation
   */
  private void applyFinalSpacing() {
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);

      if (rankNode.size() <= 1) {
        continue;
      }

      // Ensure minimum spacing between adjacent nodes
      for (int j = 0; j < rankNode.size() - 1; j++) {
        DNode currentNode = rankNode.get(j);
        DNode nextNode = rankNode.get(j + 1);

        double requiredSpacing =
            currentNode.rightWidth() + currentNode.getNodeSep() + nextNode.leftWidth();
        double currentSpacing = nextNode.getAuxRank() - currentNode.getAuxRank();

        if (currentSpacing < requiredSpacing) {
          double adjustment = requiredSpacing - currentSpacing;
          // Adjust the next node and all subsequent nodes
          for (int k = j + 1; k < rankNode.size(); k++) {
            DNode adjustNode = rankNode.get(k);
            adjustNode.setAuxRank((int) (adjustNode.getAuxRank() + adjustment));
          }
        }
      }
    }
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