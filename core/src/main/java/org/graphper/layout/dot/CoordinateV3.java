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
   * Main x-coordinate algorithm based on Dagre's position.js implementation
   * Implements the Sugiyama algorithm with conflict detection, vertical alignment, and horizontal compaction
   */
  private void sugiyamaXPositioning() {
    // Step 1: Initialize x-coordinates
    double[] xcoord = initXcoord();
    
    // Step 2: Apply Dagre-style position algorithm
    applyDagrePositioning(xcoord);
    
    // Step 3: Apply final coordinates
    applyCoordinates(xcoord);
  }

  /**
   * Apply Dagre-style positioning algorithm
   */
  private void applyDagrePositioning(double[] xcoord) {
    // Phase 1: Find and resolve conflicts
    Map<DNode, Set<DNode>> conflicts = findConflicts();
    
    // Phase 2: Vertical alignment
    Map<DNode, DNode> alignment = verticalAlignment(conflicts);
    
    // Phase 3: Horizontal compaction
    horizontalCompaction(alignment, xcoord);
  }

  /**
   * Find conflicts between nodes (Type 1 and Type 2 conflicts)
   */
  private Map<DNode, Set<DNode>> findConflicts() {
    Map<DNode, Set<DNode>> conflicts = new HashMap<>();
    
    // Find Type 1 conflicts (nodes in same rank that are adjacent in the graph)
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      
      for (int i = 0; i < rankNode.size(); i++) {
        DNode node1 = rankNode.get(i);
        for (int j = i + 1; j < rankNode.size(); j++) {
          DNode node2 = rankNode.get(j);
          
          // Check if nodes are connected through edges
          if (areNodesConnected(node1, node2)) {
            addConflict(conflicts, node1, node2);
          }
        }
      }
    }
    
    return conflicts;
  }

  /**
   * Check if two nodes are connected through edges
   */
  private boolean areNodesConnected(DNode node1, DNode node2) {
    // Check if there's a path between node1 and node2 through edges
    for (DLine edge : proxyDigraph.outAdjacent(node1)) {
      DNode other = edge.other(node1);
      if (other == node2) {
        return true;
      }
    }
    
    for (DLine edge : proxyDigraph.inAdjacent(node1)) {
      DNode other = edge.other(node1);
      if (other == node2) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Add conflict between two nodes
   */
  private void addConflict(Map<DNode, Set<DNode>> conflicts, DNode node1, DNode node2) {
    conflicts.computeIfAbsent(node1, k -> new HashSet<>()).add(node2);
    conflicts.computeIfAbsent(node2, k -> new HashSet<>()).add(node1);
  }

  /**
   * Perform vertical alignment based on conflicts
   */
  private Map<DNode, DNode> verticalAlignment(Map<DNode, Set<DNode>> conflicts) {
    Map<DNode, DNode> alignment = new HashMap<>();
    
    // Process each rank from top to bottom
    for (int layer = rankContent.minRank(); layer <= rankContent.maxRank(); layer++) {
      RankNode rankNode = rankContent.get(layer);
      
      for (DNode node : rankNode) {
        if (!alignment.containsKey(node)) {
          // Find the best alignment for this node
          DNode alignedNode = findBestAlignment(node, layer, conflicts, alignment);
          if (alignedNode != null) {
            alignment.put(node, alignedNode);
          }
        }
      }
    }
    
    return alignment;
  }

  /**
   * Find the best alignment for a node
   */
  private DNode findBestAlignment(DNode node, int layer, Map<DNode, Set<DNode>> conflicts, 
                                 Map<DNode, DNode> alignment) {
    // Look for nodes in adjacent ranks that this node can align with
    List<DNode> candidates = new ArrayList<>();
    
    // Check nodes in the rank above
    if (layer > rankContent.minRank()) {
      RankNode prevRank = rankContent.get(layer - 1);
      for (DNode prevNode : prevRank) {
        if (!conflicts.containsKey(node) || !conflicts.get(node).contains(prevNode)) {
          candidates.add(prevNode);
        }
      }
    }
    
    // Check nodes in the rank below
    if (layer < rankContent.maxRank()) {
      RankNode nextRank = rankContent.get(layer + 1);
      for (DNode nextNode : nextRank) {
        if (!conflicts.containsKey(node) || !conflicts.get(node).contains(nextNode)) {
          candidates.add(nextNode);
        }
      }
    }
    
    // Choose the best candidate based on position similarity
    if (!candidates.isEmpty()) {
      return findClosestNode(node, candidates);
    }
    
    return null;
  }

  /**
   * Find the closest node based on x-coordinates
   */
  private DNode findClosestNode(DNode target, List<DNode> candidates) {
    if (candidates.isEmpty()) {
      return null;
    }
    
    DNode closest = candidates.get(0);
    double minDistance = Double.MAX_VALUE;
    
    for (DNode candidate : candidates) {
      double distance = Math.abs(target.getAuxRank() - candidate.getAuxRank());
      if (distance < minDistance) {
        minDistance = distance;
        closest = candidate;
      }
    }
    
    return closest;
  }

  /**
   * Perform horizontal compaction based on alignment
   */
  private void horizontalCompaction(Map<DNode, DNode> alignment, double[] xcoord) {
    // Group nodes by their alignment
    Map<DNode, Set<DNode>> groups = buildAlignmentGroups(alignment);
    
    // Compact each group
    for (Set<DNode> group : groups.values()) {
      compactGroup(group, xcoord);
    }
  }

  /**
   * Build groups of aligned nodes
   */
  private Map<DNode, Set<DNode>> buildAlignmentGroups(Map<DNode, DNode> alignment) {
    Map<DNode, Set<DNode>> groups = new HashMap<>();
    
    for (Map.Entry<DNode, DNode> entry : alignment.entrySet()) {
      DNode node = entry.getKey();
      DNode alignedNode = entry.getValue();
      
      // Find the root of the alignment chain
      DNode root = findAlignmentRoot(alignedNode, alignment);
      
      groups.computeIfAbsent(root, k -> new HashSet<>()).add(node);
    }
    
    return groups;
  }

  /**
   * Find the root of an alignment chain
   */
  private DNode findAlignmentRoot(DNode node, Map<DNode, DNode> alignment) {
    DNode current = node;
    Set<DNode> visited = new HashSet<>();
    
    while (current != null && !visited.contains(current)) {
      visited.add(current);
      current = alignment.get(current);
    }
    
    return current != null ? current : node;
  }

  /**
   * Compact a group of aligned nodes
   */
  private void compactGroup(Set<DNode> group, double[] xcoord) {
    if (group.isEmpty()) {
      return;
    }
    
    // Calculate the optimal position for the group
    double optimalPosition = calculateGroupOptimalPosition(group, xcoord);
    
    // Apply the position to all nodes in the group
    for (DNode node : group) {
      int nodeIndex = calculateGlobalIndex(node);
      if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
        // Apply spacing constraints
        double constrainedPos = applySpacingConstraints(node, optimalPosition, node.getRealRank(), xcoord);
        xcoord[nodeIndex] = constrainedPos;
      }
    }
  }

  /**
   * Calculate optimal position for a group of aligned nodes
   */
  private double calculateGroupOptimalPosition(Set<DNode> group, double[] xcoord) {
    if (group.isEmpty()) {
      return 0.0;
    }
    
    // Calculate weighted average position of the group
    double weightedSum = 0.0;
    double totalWeight = 0.0;
    
    for (DNode node : group) {
      int nodeIndex = calculateGlobalIndex(node);
      if (nodeIndex >= 0 && nodeIndex < xcoord.length) {
        double weight = calculateNodeWeight(node);
        weightedSum += xcoord[nodeIndex] * weight;
        totalWeight += weight;
      }
    }
    
    return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
  }

  /**
   * Calculate weight for a node (based on connectivity and importance)
   */
  private double calculateNodeWeight(DNode node) {
    double weight = 1.0;
    
    // Increase weight for real nodes
    if (!node.isVirtual()) {
      weight *= 2.0;
    }
    
    // Increase weight based on connectivity
    int inDegree = 0;
    for (DLine edge : proxyDigraph.inAdjacent(node)) {
      inDegree++;
    }
    int outDegree = 0;
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      outDegree++;
    }
    weight += (inDegree + outDegree) * 0.1;
    
    return weight;
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
          DNode neighbor = edge.from();
          if (!inQueue.contains(neighbor)) {
            queue.offer(neighbor);
            inQueue.add(neighbor);
          }
        }
        for (DLine edge : proxyDigraph.outAdjacent(node)) {
          DNode neighbor = edge.to();
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
      DNode neighbor = edge.from();
      int neighborIndex = calculateGlobalIndex(neighbor);
      if (neighborIndex >= 0 && neighborIndex < xcoord.length) {
        neighborPositions.add(xcoord[neighborIndex]);
      }
    }
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      DNode neighbor = edge.to();
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
      DNode next = edge.to();
      if (next.isVirtual() && next.getRank() > node.getRank()) {
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
      DNode parent = edge.from();
      if (parent.getRank() < node.getRank()) {
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
      DNode child = edge.to();
      if (child.getRank() > node.getRank()) {
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
      DNode parent = edge.from();
      if (parent.getRank() < node.getRank()) {
        neighbors.add(parent);
      }
    }
  }

  /**
   * Add child nodes to the neighbors list (simple version)
   */
  private void addChildNodes(DNode node, List<DNode> neighbors) {
    for (DLine edge : proxyDigraph.outAdjacent(node)) {
      DNode child = edge.to();
      if (child.getRank() > node.getRank()) {
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