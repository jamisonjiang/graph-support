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
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import org.graphper.layout.Mark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.layout.dot.RankContent.RankNode;

/**
 * The network simplex method iterates to minimize the sum of the rank spanning and weight of the
 * edges. The main steps are as follows:
 * <ul>
 *   <li>Pop tree edges with negative tangent;
 *   <li>Find the non-tree edge that replaces this tree edge with the least amount of slack;
 *   <li>Starting from the two endpoints of the replaced non-tree edge, search upwards to the first
 *   common node, and all the tree edges that pass through are the tree edges that need to change
 *   the cut value;
 *   <li>Recalculate low and lim and rank;
 *   <li>If the node can be translated in multiple ranks without affecting the final edge sum,
 *   the node moves to the rank with fewer nodes;
 * </ul>
 *
 * @author Jamison Jiang
 */
class NetworkSimplex {

  private static final Logger log = LoggerFactory.getLogger(NetworkSimplex.class);

  private RankContent rankContent;

  private final DotDigraph dotDigraph;

  private FeasibleTree feasibleTree;

  private Queue<ULine> negativeLine;

  // The tree edge that needs to modify the cut value
  private ArrayList<ULine> updateCutvalLines;

  // The starting node of the path to recalculate the cut value
  private DNode calcCutvalHead;

  // Whether rank can only be a positive integer.
  // In the abscissa layout, the rank can be non-integer.
  private final boolean positiveRank;

  private final double rankSep;


  public NetworkSimplex(FeasibleTree feasibleTree, int nsLimit, double rankSep,
                        Consumer<DNode[]> sortNodesConsumer) {
    this(feasibleTree, nsLimit, true, true, true, rankSep, sortNodesConsumer);
  }

  public NetworkSimplex(FeasibleTree feasibleTree, int nsLimit, boolean positiveRank,
                        boolean needRankContent, double rankSep,
                        Consumer<DNode[]> sortNodesConsumer) {
    this(feasibleTree, nsLimit, positiveRank, needRankContent, true, rankSep, sortNodesConsumer);
  }

  public NetworkSimplex(FeasibleTree feasibleTree, int nsLimit, boolean positiveRank,
                        boolean needRankContent, boolean needBalance, double rankSep,
                        Consumer<DNode[]> sortNodesConsumer) {
    Asserts.nullArgument(feasibleTree, "feasibleTree");
    Asserts.illegalArgument(
        feasibleTree.getDotDigraph() == null,
        "feasibleTree.getDotDigraph() can not be null"
    );
    Asserts.illegalArgument(rankSep < 0, "rankSpace (" + rankSep + ") must be > 0");
    this.feasibleTree = feasibleTree;
    this.dotDigraph = feasibleTree.getDotDigraph();
    this.negativeLine = feasibleTree.negativeLine();
    this.positiveRank = positiveRank;
    this.rankSep = rankSep;

    // Network Simplex Method, Optimal Hierarchy Assignment
    networkSimplex(nsLimit);

    // Hierarchy of Balanced Vertices + Disconnected Graph Alignment
    if (needBalance) {
      alignUnconnectGraph(balance(needRankContent, sortNodesConsumer));
    }

    clear();
  }

  public RankContent getRankContent() {
    return rankContent;
  }

  /*
   * Network simplex method, calculate the tangent value of the undirected tree, and continuously
   * replace the tree edges until the tangent value of all tree edges is not negative
   */
  private void networkSimplex(int nsLimit) {
    String prefix = null;
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled()) {
      prefix = "network simplex: ";
      log.debug("{} nodes={} edges={} maxiter={}",
                prefix, dotDigraph.vertexNum(),
                dotDigraph.edgeNum(), nsLimit);
    }

    ULine out;
    int count = 0;
    List<Set<DNode>> halfNodeRecord = null;

    /*
     * 1.Pop out edges with negative tangent;
     * 2.Find a replacement tree edge;
     * 3.Do this until there are no negative tangent tree edges or the iteration limit is reached.
     */
    while ((out = negativeTreeLine()) != null && count++ < nsLimit) {
      if (halfNodeRecord == null) {
        halfNodeRecord = new ArrayList<>(1);
      }
      halfNodeRecord.clear();
      ULine enter = findEnterLine(out, halfNodeRecord);

      if (enter == null) {
        continue;
      }

      enterLine(enter, out, halfNodeRecord.get(0));
      if (log.isDebugEnabled() && count % 100 == 0) {
        log.debug("{} {}", prefix, count);
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Network is done,total number of iterations is {},time is {}s", count,
                (System.currentTimeMillis() - start) / 1000);
    }
  }

  private ULine findEnterLine(ULine outLine, List<Set<DNode>> halfNodeRecord) {
    ULine[] minSlackLine = new ULine[]{null};
    int[] minSlack = new int[]{Integer.MAX_VALUE};

    // Find all edges that span the tail and head components, and get the edge with the least slack as a replacement edge.
    Consumer<ULine> consumer = uLine -> {
      // Remove edges that do not straddle tail and head.
      if (!FeasibleTree.isCross(outLine.getdLine(), uLine.getdLine())
          || FeasibleTree.inTail(uLine.getdLine().from(), outLine.getdLine())) {
        return;
      }

      int slack = uLine.reduceLen();
      // If the slack is less than the current minimum slack edge, the current edge becomes the minimum slack edge.
      if (minSlackLine[0] == null || slack < minSlack[0]) {
        minSlackLine[0] = uLine;
        minSlack[0] = slack;
      }
    };

    // Add component nodes
    halfNodeRecord.add(FeasibleTree.halfDfs(feasibleTree.graph(), outLine, consumer));
    return minSlackLine[0];
  }

  private void enterLine(ULine enterLine, ULine outLine, Set<DNode> halfNodes) {
    /*
     * 1.Find all tree edges that need to change the cut value;
     * 2.Remove old tree edges and add new ones;
     * 3.Update the value of (low, lim) of the node;
     * 4.Update cutoff.
     * */
    DotGraph tree = feasibleTree.tree();

    // Change the tree edge and set the tangent value of the replaced edge.
    enterLine.getdLine().setCutVal(-outLine.getdLine().getCutVal());

    // Find all edges that need to modify the cut value.
    DNode root = findNeedUpdateCutvalLines(tree, enterLine);
    DNode largeLimNode = outLine.getdLine().from().getLim() > outLine.getdLine().to().getLim()
        ? outLine.getdLine().from() : outLine.getdLine().to();

    if (notInLimLowRange(root, largeLimNode)) {
      root = publicRoot(tree, root, largeLimNode, null);
    }

    tree.removeLine(outLine);
    tree.addEdge(enterLine);

    // Reset rank
    if (enterLine.reduceLen() != 0) {
      int r = enterLine.reduceLen();

      DNode t = FeasibleTree.inTail(outLine.either(), outLine.getdLine())
          ? outLine.either() : outLine.other(outLine.either());

      if (halfNodes.contains(t)) {
        r = -r;
      }

      for (DNode halfNode : halfNodes) {
        halfNode.setRank(halfNode.getRank() + r);
      }
    }

    // Change the value of low and lim, and reset the rank of some nodes.
    new LowLimCalc(tree, root);

    // update edge tangent.
    updateCutval();

    updateCutvalLines.clear();
  }

  private Map<Integer, DNode> balance(boolean needRankContent,
                                      Consumer<DNode[]> sortNodesConsumer) {
    if (positiveRank) {
      return tbBalance(sortNodesConsumer);
    } else {
      lrBalance();

      if (needRankContent) {
        this.rankContent = new RankContent(feasibleTree.graph(), rankSep,
                                           positiveRank, sortNodesConsumer);
      }
      return null;
    }
  }

  /*
   * In the case of not affecting the total span, the rank setting of some nodes is within a
   * certain range. Balance this inner node so that the nodes are evenly distributed in the
   * Within each rank, this can have a better aspect ratio.
   * */
  private Map<Integer, DNode> tbBalance(Consumer<DNode[]> sortNodesConsumer) {
    DotGraph dotGraph = feasibleTree.graph();

    Map<Integer, DNode> connectLowRank = feasibleTree.isHaveUnconnectedGraph()
        ? new HashMap<>()
        : null;
    this.rankContent = new RankContent(dotGraph, rankSep, positiveRank, sortNodesConsumer);

    // Each node has a greedy way to obtain the most balanced rank allocation in the current situation.
    for (DNode node : dotGraph) {
      int connectNo = feasibleTree.getConnectNo(node);
      if (connectLowRank != null) {
        connectLowRank.compute(connectNo, (c, n) -> {
          if (n == null) {
            return node;
          }
          return n.getRank() < node.getRank() ? n : node;
        });
      }

      int currentRank = node.getRank();
      RankNode current = rankContent.get(currentRank);
      Integer preRank = current.pre != null ? current.pre.rankIndex() : null;
      Integer nextRank = current.next != null ? current.next.rankIndex() : null;

      // Hierarchy border nodes cannot participate in movement
      if (preRank == null || nextRank == null) {
        continue;
      }

      double inAndOutWeight = 0D;
      // The maximum value of the upper node of the current node adjacent to the node.
      int preMax = Integer.MIN_VALUE;
      // The minimum value of the lower nodes of the adjacent nodes of the current node.
      int nextMin = Integer.MAX_VALUE;

      boolean canNotMove = false;

      // Cycle all in- and out-degree edges of a node.
      for (ULine uLine : dotGraph.adjacent(node)) {
        DNode other = uLine.other(node);
        int otherRank = other.getRank();

        if (positiveRank) {
          if (otherRank < node.getRank()) {
            otherRank = otherRank + uLine.limit() - 1;
          } else {
            otherRank = otherRank - uLine.limit() + 1;
          }
        }

        if (otherRank < currentRank && (otherRank > preMax)) {
          preMax = otherRank;
        }

        if (otherRank > currentRank && otherRank < nextMin) {
          nextMin = otherRank;
        }

        // If there is no room to move, skip the current node directly to avoid unnecessary loops.
        if (canNotMove = (Objects.equals(preMax, preRank) && Objects.equals(nextMin, nextRank))) {
          break;
        }

        if (isInEdge(node, uLine)) {
          inAndOutWeight += uLine.getdLine().weight();
        } else {
          inAndOutWeight -= uLine.getdLine().weight();
        }
      }

      // If the node cannot be moved, or the total weight of the initial edge is inconsistent,
      // or the sparsest rank is the current rank, or the node has only in-degree or out-degree
      // edges, there is no need to move the rank
      if (canNotMove
          || inAndOutWeight != 0
          || preMax == Integer.MIN_VALUE
          || nextMin == Integer.MAX_VALUE) {
        continue;
      }

      // Sparsest rank
      RankNode sparsestRank = current;
      // Select the smallest rank in the range
      RankNode preMaxNode = rankContent.get(preMax);
      RankNode nextMinNode = rankContent.get(nextMin);
      RankNode curNode = preMaxNode.next;

      while (curNode != null && curNode != nextMinNode) {
        if (curNode.size() >= sparsestRank.size() - 1) {
          curNode = curNode.next;
          continue;
        }

        // Record the sparsest rank and the number of nodes of the rank
        sparsestRank = curNode;

        curNode = curNode.next;
      }

      if (sparsestRank == current) {
        continue;
      }

      updateRank(node, current, sparsestRank);
    }

    return connectLowRank;
  }

  private void updateRank(DNode node, RankNode sourceNode, RankNode targetRank) {
    if (sourceNode == targetRank || node.getRank() != sourceNode.rankIndex()) {
      return;
    }

    // Remove the record of the rank where the old node is located
    sourceNode.remove(node);
    node.setRank(targetRank.rankIndex());
    targetRank.add(node);
  }

  private void lrBalance() {
    List<Set<DNode>> halfNodeRecord = null;
    Set<ULine> lineMarks = new HashSet<>(feasibleTree.tree().edgeNum());
    for (DNode n : feasibleTree.tree()) {
      for (ULine e : feasibleTree.tree().adjacent(n)) {
        if (e.cutVal() != 0 || lineMarks.contains(e)) {
          continue;
        }

        lineMarks.add(e);

        if (halfNodeRecord == null) {
          halfNodeRecord = new ArrayList<>();
        } else {
          halfNodeRecord.clear();
        }

        ULine enter = findEnterLine(e, halfNodeRecord);
        if (enter == null) {
          continue;
        }

        int delta = enter.reduceLen();
        if (delta <= 1) {
          continue;
        }

        DNode from = enter.getdLine().from();
        Set<DNode> halfNodes = halfNodeRecord.get(0);
        if (halfNodes.contains(from)) {
          delta /= -2;
        } else {
          delta /= 2;
        }

        for (DNode halfNode : halfNodes) {
          halfNode.setRank(halfNode.getRank() - delta);
        }
      }
    }
  }

  /*
   * Find all the edges that need to change the cut value, start from the two nodes of the replaced
   * edge, and find the common first parent node along the spanning tree, the path from the parent node
   * to the two nodes in the spanning tree is all the edges that need to adjust the cut value.
   * */
  private DNode findNeedUpdateCutvalLines(DotGraph tree, ULine enterLine) {
    DNode from = enterLine.getdLine().from();
    DNode to = enterLine.getdLine().to();
    DNode current = calcCutvalHead = from;

    // Find the common node of the two nodes of the replacement edge, and add from to the path of this node
    current = publicRoot(tree, to, current, this::addUpdateCutvalLines);
    DNode root = current;

    // Add public nodes to the path "to"
    while (current != to) {
      for (ULine uLine : tree.adjacent(current)) {
        DNode other = uLine.other(current);
        if (other.getLim() > current.getLim() || notInLimLowRange(other, to)) {
          continue;
        }

        current = other;

        addUpdateCutvalLines(uLine);
        break;
      }
    }

    return root;
  }

  private DNode publicRoot(DotGraph tree, DNode to, DNode current, Consumer<ULine> lineConsumer) {
    while (notInLimLowRange(current, to)) {
      for (ULine uLine : tree.adjacent(current)) {
        DNode other = uLine.other(current);
        if (other.getLim() < current.getLim()) {
          continue;
        }

        current = other;

        if (lineConsumer != null) {
          lineConsumer.accept(uLine);
        }
        break;
      }
    }
    return current;
  }

  /*
   * Update the cut value of all edges that need to update the cut value. And the tangent values of
   * these tree edges can be calculated by the tangent values of adjacent tree edges.
   * */
  private void updateCutval() {
    if (CollectionUtils.isEmpty(updateCutvalLines)) {
      return;
    }

    DotGraph tree = feasibleTree.tree();

    // Calculate from the head of the node list composed of the edge paths that change the cut value.
    DNode current = calcCutvalHead;
    for (int i = 0; i < updateCutvalLines.size(); i++) {
      ULine updateCutvalLine = updateCutvalLines.get(i);
      double cutval = FeasibleTree.calcCutValByAdjTreeLine(
          feasibleTree.graph(),
          current,
          updateCutvalLine,
          tree::containEdge
      );

      updateCutvalLine.getdLine().setCutVal(cutval);
      current = updateCutvalLine.other(current);

      // If the modified tree edge cut value is less than 0, re-throw it into the queue for calculating the cut value.
      if (cutval < 0) {
        negativeLine.offer(updateCutvalLine);
      }
    }
  }

  // Determine whether the lim value of a node target is not within the interval [low, lim) of another node source
  private boolean notInLimLowRange(DNode source, DNode target) {
    return source.getLow() > target.getLim() || source.getLim() < target.getLim();
  }

  private void addUpdateCutvalLines(ULine uLine) {
    if (updateCutvalLines == null) {
      updateCutvalLines = new ArrayList<>();
    }

    updateCutvalLines.add(uLine);
  }

  /*
   * Finding negatively weighted edges in a tree.
   */
  private ULine negativeTreeLine() {
    if (CollectionUtils.isEmpty(negativeLine)) {
      return null;
    }

    ULine negative;
    do {
      if (CollectionUtils.isEmpty(negativeLine)) {
        return null;
      }
      negative = negativeLine.poll();
    } while (negative != null && negative.getdLine().getCutVal() >= 0);

    return negative;
  }

  private boolean isInEdge(DNode node, ULine uLine) {
    return uLine.getdLine().to() == node;
  }

  private void alignUnconnectGraph(Map<Integer, DNode> connectLowRank) {
    if (connectLowRank == null) {
      return;
    }

    DNode basic = null;
    for (DNode source : connectLowRank.values()) {
      if (source.getRank() == rankContent.minRank()) {
        basic = source;
      }
    }

    Set<DNode> mark = new HashSet<>();
    for (DNode source : connectLowRank.values()) {
      if (basic == null || basic.getRank() == source.getRank()) {
        basic = source;
        continue;
      }

      int rankOffset = source.getRank() - basic.getRank();
      dfs(mark, source, rankOffset);
    }
  }

  private void dfs(Set<DNode> mark, DNode node, int rankOffset) {
    mark.add(node);
    RankNode sourceRankNode = rankContent.get(node.getRank());
    RankNode targetRankNode = rankContent.get(node.getRank() - rankOffset);
    if (sourceRankNode == targetRankNode) {
      return;
    }

    updateRank(node, sourceRankNode, targetRankNode);

    for (ULine uLine : feasibleTree.tree().adjacent(node)) {
      DNode other = uLine.other(node);

      if (mark.contains(other)) {
        continue;
      }

      dfs(mark, other, rankOffset);
    }
  }

  private void clear() {
    updateCutvalLines = null;
    negativeLine = null;
    feasibleTree = null;
  }

  private static class LowLimCalc extends Mark<DNode> {

    private int reserveCount;

    private int low = Integer.MAX_VALUE;

    private final DNode root;

    private LowLimCalc(DotGraph tree, DNode node) {
      super(tree.vertexNum());

      this.root = node;

      reserveCount = node.getLow() - 1;

      dfs(tree, node);
    }

    private void dfs(DotGraph tree, DNode v) {
      mark(v);
      int tmpLow = Integer.MAX_VALUE;

      for (ULine e : tree.adjacent(v)) {
        DNode w = e.other(v);

        if (isMark(w) || !isRightNode(w)) {
          continue;
        }

        dfs(tree, w);
        tmpLow = Math.min(tmpLow, low);
        low = Integer.MAX_VALUE;
      }

      int lim = ++reserveCount;
      low = Math.min(tmpLow, lim);

      v.setLow(low);
      v.setLim(lim);
    }

    private boolean isRightNode(DNode node) {
      return node != root && node.getLim() >= root.getLow() && node.getLim() < root.getLim();
    }
  }
}
