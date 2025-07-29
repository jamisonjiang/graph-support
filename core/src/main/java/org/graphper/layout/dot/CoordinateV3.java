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
   * Main x-coordinate algorithm based on TSE93 paper 4.1 Heuristic Approach
   */
  private void sugiyamaXPositioning() {
    // Step 2: Initial x-coordinates
    double[] xcoord = initXcoord();
    double[] xbest = xcoord.clone();
    double bestLength = xlength(xcoord);

    // Steps 4-12: Iterative improvement
//    for (int i = 0; i < MAX_ITERATIONS; i++) {
    for (int i = 0; i <= 1; i++) {
      // Step 5: Median position heuristic
      medianpos(i, xcoord);

      // Step 6: Minimize edge lengths
//      minedge(i, xcoord);

      // Step 7: Minimize node positions
//      minnode(i, xcoord);

      // Step 8: Minimize path lengths (straighten virtual chains)
//      minpath(i, xcoord);

      // Step 9: Pack and compact
//      packcut(i, xcoord);

      // Steps 10-11: Save best result
      double currentLength = xlength(xcoord);
      if (currentLength < bestLength) {
        xbest = xcoord.clone();
        bestLength = currentLength;
      }
    }

    // Step 13: Apply best coordinates
    applyCoordinates(xbest);
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
          double medianPos = calculateMedianPosition(node, layer, false, xcoord);
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
          double medianPos = calculateMedianPosition(node, layer, true, xcoord);
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
  private double calculateMedianPosition(DNode node, int layer, boolean upward, double[] xcoord) {
    List<Double> neighborPositions = new ArrayList<>();

    if (upward) {
      for (DLine edge : proxyDigraph.outAdjacent(node)) {
        DNode adjNode = edge.other(node);
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
    DotDigraph dotDigraph = dotAttachment.getDotDigraph();
    for (DLine edge : dotDigraph.edges()) {
      DNode from = edge.from();
      DNode to = edge.to();

      // Only consider edges between real nodes
      if (!from.isVirtual() && !to.isVirtual()) {
        int fromIndex = calculateGlobalIndex(from);
        int toIndex = calculateGlobalIndex(to);

        if (fromIndex >= 0 && toIndex >= 0 && fromIndex < xcoord.length
            && toIndex < xcoord.length) {
          // Place edge as close as possible to median of adjacent nodes
          double medianPos = (xcoord[fromIndex] + xcoord[toIndex]) / 2.0;

          // Adjust both nodes towards the median
          double adjustment = 0.1; // Small adjustment factor
          xcoord[fromIndex] += (medianPos - xcoord[fromIndex]) * adjustment;
          xcoord[toIndex] += (medianPos - xcoord[toIndex]) * adjustment;
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