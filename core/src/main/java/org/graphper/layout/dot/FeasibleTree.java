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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.graphper.layout.Mark;
import org.graphper.util.CollectionUtils;

/**
 * Generate the initial feasible tree of the network simplex method, the main steps are as follows:
 * <ul>
 *   <li>Generate an undirected tree based on the original graph;
 *   <li>Generate (low, lim) flags for nodes according to this undirected tree;
 *   <li>Set all tree edges of the spanning tree to be "tight" and compute the rank of the nodes;
 *   <li>Generate the cut value of the tree edge.
 * </ul>
 *
 * @author Jamison Jiang
 */
class FeasibleTree {

  private final DotDigraph dotDigraph;

  // All tree edges with negative tangent
  private Queue<ULine> negativeLine;

  private final DotGraph graph;

  // Undirected tree
  private final DotGraph tree;

  private final boolean haveUnconnectedGraph;

  private final Map<DNode, Integer> nodeConnectRecord;

  FeasibleTree(DotDigraph digraph) {
    if (digraph == null || digraph.vertexNum() == 0) {
      throw new IllegalArgumentException("Graph can not be empty");
    }
    this.dotDigraph = digraph;

    // Initial rank assignment
    RankInit rankInit = new RankInit(digraph);
    this.graph = rankInit.graph;
    Collection<DNode> sources = rankInit.sources;
    this.tree = rankInit.tree;
    this.haveUnconnectedGraph = sources.size() > 1;
    this.nodeConnectRecord = rankInit.nodeConnectRecord;

    // Vertex (low, lim), initial rank assignment, initial cut value calculation of the edges of the spanning tree.
    if (digraph.edgeNum() != 0) {
      PropInit propInit = new PropInit(digraph, graph, tree, sources);

      this.negativeLine = propInit.negativeLine;
    }
  }

  public boolean isHaveUnconnectedGraph() {
    return haveUnconnectedGraph;
  }

  /**
   * Returns the connected components of a node.
   *
   * @param node node
   * @return the connected components
   */
  int getConnectNo(DNode node) {
    if (nodeConnectRecord == null) {
      return 1;
    }

    Integer connectNo = nodeConnectRecord.get(node);
    return connectNo != null ? connectNo : 1;
  }

  DotDigraph getDotDigraph() {
    return dotDigraph;
  }

  /**
   * If the spanning tree has an edge with negative tangency, return this edge, otherwise return
   * null.
   *
   * @return the smallest edge with a negative tangent in the spanning tree
   */
  Queue<ULine> negativeLine() {
    return negativeLine;
  }

  DotGraph graph() {
    return graph;
  }

  DotGraph tree() {
    return tree;
  }

  /**
   * Determine whether the node is the tail part of the two components split at the tree edge.
   *
   * @param node     node
   * @param treeLine tree line
   * @return <tt>true</tt> Node in tail component
   */
  static boolean inTail(DNode node, DLine treeLine) {
    DNode from = treeLine.from();
    DNode to = treeLine.to();

    boolean directed = from.getLim() < to.getLim();
    DNode tail = directed ? from : to;

    return directed == (tail.getLow() <= node.getLim() && tail.getLim() >= node.getLim());
  }

  /**
   * Determine whether an edge is a straddling edge relative to a tree edge.
   *
   * @param treeLine tree line
   * @param line     line
   * @return <tt>true</tt> if line cross the tail and head components
   */
  static boolean isCross(DLine treeLine, DLine line) {
    DNode from = line.from();
    DNode to = line.to();
    return inTail(from, treeLine) ^ inTail(to, treeLine);
  }

  /**
   * Determine the value of a certain edge across the head and tail relative to a certain tree edge.
   * If there is no crossing, it is 0.
   *
   * @param treeLine tree line
   * @param line     line
   * @return the incremental weight value
   */
  static double lineCrossVal(DLine treeLine, DLine line) {
    DNode from = line.from();
    DNode to = line.to();

    boolean fromInTail;
    boolean isCross = (fromInTail = inTail(from, treeLine)) ^ inTail(to, treeLine);

    if (!isCross) {
      return 0;
    }

    if (fromInTail) {
      return line.weight();
    }

    return -line.weight();
  }

  /**
   * Compute cut values by iterating only components with fewer nodes, avoiding scanning all edges.
   *
   * @param graph    undirected graph
   * @param treeLine tree line
   * @return cut value of tree line
   */
  static double halfDfsCalcCutVal(DotGraph graph, ULine treeLine) {
    double[] cutVal = new double[]{0D};
    Consumer<ULine> consumer = uLine ->
        cutVal[0] += lineCrossVal(
            treeLine.getdLine(),
            uLine.getdLine()
        );

    halfDfs(graph, treeLine, consumer);

    return cutVal[0];
  }

  /**
   * By iterating only components with fewer nodes, find all edges that straddle two components, and
   * perform some consumption behavior, then returns all nodes of the half of components with fewer
   * nodes.
   *
   * @param graph         undirected graph
   * @param treeLine      tree line
   * @param dLineConsumer cross line consumer
   * @return component nodes
   */
  static Set<DNode> halfDfs(DotGraph graph, ULine treeLine, Consumer<ULine> dLineConsumer) {
    Objects.requireNonNull(graph);
    Objects.requireNonNull(treeLine);

    if (dLineConsumer == null) {
      return Collections.emptySet();
    }

    DLine treeDLine = treeLine.getdLine();
    DNode from = treeDLine.from();
    DNode to = treeDLine.to();

    DNode tailNode = inTail(to, treeDLine) ? to : from;
    DNode headNode = to == tailNode ? from : to;

    Set<DNode> queenRecord = new HashSet<>();
    Queue<DNode> halfNodeQueen = new LinkedList<>();

    // Iterate the side components with fewer nodes
    DNode startNode = headNode.getLim() - headNode.getLow()
        < tailNode.getLim() - tailNode.getLow() ? headNode : tailNode;
    halfNodeQueen.offer(startNode);
    queenRecord.add(startNode);

    while (!halfNodeQueen.isEmpty()) {
      DNode node = halfNodeQueen.poll();

      for (ULine uLine : graph.adjacent(node)) {
        if (isCross(treeDLine, uLine.getdLine())) {
          dLineConsumer.accept(uLine);
          continue;
        }

        DNode other = uLine.other(node);

        if (!queenRecord.contains(other)) {
          halfNodeQueen.offer(other);
          queenRecord.add(other);
        }
      }
    }

    return queenRecord;
  }

  /**
   * When calculating the tangent value of a tree edge, if a node of the tree edge meets the
   * condition: the tangent value of all tree edges connected to the node (except the tree edge
   * itself to be calculated) is known, then you can use This method computes the tangent of the
   * edges of this tree. The original graph of the spanning tree needs to be provided, and an
   * assertion isTree is provided to judge whether a certain edge in the original graph belongs to
   * the tree edge in the spanning tree. This method can calculate the cut value of the tree edge.
   *
   * @param graph    spanning tree
   * @param node     for a certain node of the tree edge, the tangent values of other tree edges
   *                 connected to this node have been calculated
   * @param treeLine tree edges for which cut values need to be computed
   * @param isTree   the logic for judging whether an edge is a tree edge
   * @return edge cut value
   */
  static double calcCutValByAdjTreeLine(DotGraph graph, DNode node,
                                        ULine treeLine, Predicate<ULine> isTree) {
    if (graph == null || node == null || treeLine == null || isTree == null) {
      throw new NullPointerException();
    }

    /*
     * Key point: When the tangent values of all adjacent tree edges (except itself) of a certain
     * node v on a certain tree edge are known, then the tangent value of this tree edge can be
     * summarized as:
     *
     * 1.When an edge of node v has the same in-out degree as the edge of the tree:
     * (1)The edge is the edge of the tree, minus the tangent value of the edge of the tree,
     * plus the weight value of the edge of the tree;
     * (2)The edge is a non-tree edge plus a weight value.
     *
     * 2.When an edge of node v is inconsistent with the in-out degree of the tree edge:
     * (1)Add the cut value of the tree edge to the edge of the tree, and then subtract the
     * weight value of the tree edge;
     * (2)The edge is a non-tree edge minus the weight value.
     * */
    double cutVal = 0;
    for (ULine uLine : graph.adjacent(node)) {
      // Skip the tree edge to be computed now
      if (uLine.getdLine() == treeLine.getdLine()) {
        cutVal += treeLine.getdLine().weight();
        continue;
      }

      // Consistent in and out
      if (isSameInOut(treeLine.getdLine(), uLine.getdLine())) {
        if (isTree.test(uLine)) {
          // Is tree line
          cutVal = cutVal - uLine.getdLine().getCutVal() + uLine.getdLine().weight();
        } else {
          // Is not tree line
          cutVal += uLine.getdLine().weight();
        }
      }
      // Inconsistent in and out
      else {
        // Is tree line
        if (isTree.test(uLine)) {
          cutVal = cutVal + uLine.getdLine().getCutVal() - uLine.getdLine().weight();
        } else {
          // Is not tree line
          cutVal -= uLine.getdLine().weight();
        }
      }
    }

    return cutVal;
  }

  // Specify the tree edge and node, input another adjacent edge of this node, and judge whether the in-out degree is consistent
  private static boolean isSameInOut(DLine treeLine, DLine targetLine) {
    return treeLine.from() == targetLine.from() || treeLine.to() == targetLine.to();
  }

  /**
   * Initial Grade Assignment.
   */
  private static class RankInit extends Mark<DNode> {

    private final DotGraph graph;

    // Feasible spanning tree
    private final DotGraph tree;

    // Traverse start node
    private Collection<DNode> sources;

    private Map<DNode, Integer> nodeConnectRecord;

    public RankInit(DotDigraph dotDigraph) {
      int edgeNum = dotDigraph.edgeNum();
      this.graph = new DotGraph(dotDigraph.vertexNum(), edgeNum);

      if (edgeNum == 0) {
        this.tree = graph;
      } else {
        this.tree = new DotGraph(dotDigraph.vertexNum());
      }

      Queue<ULine> minLines = new PriorityQueue<>(Comparator.comparing(ULine::reduceLen));

      // The initial level is generated to ensure that the level of the "from" is higher than that of the "to"
      initRank(dotDigraph, minLines);

      // Generation of initial feasible tree
      generateTree(minLines);

      clear();
    }

    private void initRank(DotDigraph dotDigraph, Queue<ULine> minLines) {
      for (DNode node : dotDigraph) {
        if (isMark(node)) {
          continue;
        }

        dfs(dotDigraph, minLines, node);
      }

      connectSource();
    }

    private void connectSource() {
      clear();

      Map<Integer, DNode> sourceMap = new HashMap<>(1);
      int connectNo = 1;
      for (DNode node : graph) {
        if (isMark(node)) {
          continue;
        }

        dfs(node, connectNo++, sourceMap);
      }

      sources = sourceMap.values();
    }

    private void dfs(DotDigraph dotDigraph, Queue<ULine> minLines, DNode from) {
      mark(from);
      graph.add(from);
      int minRank = 0;

      ULine minLine = null;
      for (DLine dLine : dotDigraph.adjacent(from)) {
        DNode to = dLine.other(from);

        ULine uLine = new ULine(dLine.from(), to, dLine, dLine.weight());
        graph.addEdge(uLine);

        if (!isMark(to)) {
          dfs(dotDigraph, minLines, to);
        }

        minRank = Math.min(minRank, to.getRank() - dLine.limit());
        from.setRank(minRank);

        if (minLine == null || minLine.reduceLen() > uLine.reduceLen()) {
          minLine = uLine;
        }
      }

      if (minLine != null) {
        minLines.add(minLine);
      }
    }

    private void dfs(DNode node, int connectNo, Map<Integer, DNode> sourceMap) {
      mark(node);

      if (connectNo > 1) {
        if (nodeConnectRecord == null) {
          nodeConnectRecord = new HashMap<>();
        }
        nodeConnectRecord.put(node, connectNo);
      }

      DNode sn = sourceMap.get(connectNo);
      if (sn == null || node.getRank() < sn.getRank()) {
        sourceMap.put(connectNo, node);
      }

      for (ULine uLine : graph.adjacent(node)) {
        DNode other = uLine.other(node);

        if (isMark(other)) {
          continue;
        }

        dfs(other, connectNo, sourceMap);
      }
    }

    private void generateTree(Queue<ULine> minLines) {
      Queue<ULine> treeAdjacentEdges = new PriorityQueue<>(
          Comparator.comparing(ULine::reduceLen)
      );

      while (tree.vertexNum() < graph.vertexNum() && CollectionUtils.isNotEmpty(minLines)) {
        treeAdjacentEdges.clear();
        treeAdjacentEdges.add(minLines.poll());

        while (CollectionUtils.isNotEmpty(treeAdjacentEdges)) {
          ULine uLine = treeAdjacentEdges.poll();

          if (uLine == null || tree.containEdge(uLine)) {
            continue;
          }

          DLine dLine = uLine.getdLine();
          DNode next = tree.containNode(dLine.from()) ? dLine.to() : dLine.from();

          if (tree.containNode(next)) {
            continue;
          }

          // If the added tree edge at this time is not "tight", reset the coordinates of all nodes in the tree to make it a "tight edge"
          int reduceLen = dLine.reduceLen();
          if (reduceLen != 0) {
            int delta = next == dLine.from() ? -reduceLen : reduceLen;

            for (DNode dNode : tree) {
              dNode.setRank(dNode.getRank() + delta);
            }

            // Reset the priority queue, the previous operation may destroy the sorting
            treeAdjacentEdges = newMinQueue(treeAdjacentEdges);
          }

          // Add the adjacent non-entering tree edges to the newly entered tree vertex into the queue
          addAdjEdgesQueen(treeAdjacentEdges, uLine);

          tree.addEdge(uLine);
        }
      }
    }

    private void addAdjEdgesQueen(Queue<ULine> treeAdjacentEdges, ULine uLine) {
      DNode from = uLine.getdLine().from();
      DNode to = uLine.getdLine().to();

      if (!tree.containNode(from)) {
        addAdjEdgesQueen(treeAdjacentEdges, uLine, from);
      }

      if (!tree.containNode(to)) {
        addAdjEdgesQueen(treeAdjacentEdges, uLine, to);
      }
    }

    private void addAdjEdgesQueen(Queue<ULine> treeAdjacentEdges, ULine uLine, DNode node) {
      for (ULine line : graph.adjacent(node)) {
        if (line == uLine ||
            (tree.containNode(line.getdLine().from()) && tree.containNode(line.getdLine().to()))) {
          continue;
        }

        treeAdjacentEdges.offer(line);
      }
    }

    private Queue<ULine> newMinQueue(Queue<ULine> treeAdjacentEdges) {
      if (CollectionUtils.isEmpty(treeAdjacentEdges)) {
        return treeAdjacentEdges;
      }
      PriorityQueue<ULine> uLines = new PriorityQueue<>(
          treeAdjacentEdges.size(),
          Comparator.comparing(ULine::reduceLen)
      );

      uLines.addAll(treeAdjacentEdges);
      return uLines;
    }
  }

  /**
   * Set the low and lim and cut value properties.
   */
  private static class PropInit extends Mark<DNode> {

    // Reverse stack node count
    private int reserveCount = 0;

    // Record the minimum value among subsequent nodes
    private int low = Integer.MAX_VALUE;

    // All boundary nodes, that is, among the two components after breaking the tree edge, there
    // must be a component that contains only one vertex
    private Queue<DNode> cutQueen;

    // Calculate whether the tangent value of the edge has been calculated
    private Set<DLine> lineCache;

    // All negative tangent tree edges
    private Queue<ULine> negativeLine;

    private final DotDigraph dotDigraph;

    // Mark whether the vertex is a boundary node
    private final Set<DNode> isBorder;

    // Spanning tree
    private final DotGraph tree;

    // Record the number of vertexes that have completed cut value calculations and whether they
    // have entered the queue for calculating cut values
    private Map<DNode, CutValRecord> nodeCountValRecord;

    private PropInit(DotDigraph dotDigraph, DotGraph graph, DotGraph tree,
                     Collection<DNode> sourceNodes) {
      super(dotDigraph.vertexNum());
      this.dotDigraph = dotDigraph;
      this.tree = tree;

      this.isBorder = new HashSet<>();
      for (DNode source : sourceNodes) {
        if (isMark(source)) {
          continue;
        }
        dfs(source);
      }

      // Calculate the tangent of all tree edges
      computeCutVal(graph);
    }

    private void dfs(DNode v) {
      mark(v);
      // Record the lim of the vertex with the smallest lim among the subsequent nodes of the current vertex
      int tmpLow = Integer.MAX_VALUE;

      for (ULine e : tree.adjacent(v)) {
        DNode w = e.other(v);

        if (isMark(w)) {
          continue;
        }

        dfs(w);
        // Record the minimum value of the adjacent nodes of the current node so far
        tmpLow = Math.min(tmpLow, low);
        low = Integer.MAX_VALUE;
      }

      // The leaf node is a boundary node, or the source node has a degree of 1 in the spanning tree,
      // and the source node is a boundary node
      if (tree.degree(v) == 1) {
        isBorder.add(v);
        offerCutQueen(v);
      }

      int lim = ++reserveCount;
      low = Math.min(tmpLow, lim);
      v.setLow(low);
      v.setLim(lim);
    }

    private void computeCutVal(DotGraph graph) {
      // Starting from the boundary node of the tree edge, the cut value is calculated by entering the inner node layer by layer
      while (CollectionUtils.isNotEmpty(cutQueen)) {
        DNode node = cutQueen.poll();

        // Calculation logic of border nodes
        if (isBorder.contains(node)) {
          calcBorderCutVal(graph, node);
          continue;
        }

        // Ordinary node adjacent tree edge calculation
        calcNormalCutVal(graph, node);
      }
    }

    // Calculate the cut value of ordinary nodes
    private void calcNormalCutVal(DotGraph graph, DNode node) {
      int degreeThreshold = tree.degree(node) - 1;
      for (ULine uLine : tree.adjacent(node)) {

        if (lineCacheContain(uLine.getdLine())) {
          continue;
        }

        // Prove that the tangent values of all other tree edges at the current vertex have been calculated
        DNode nextNode;
        DNode other = uLine.other(node);
        if (getNodeHavedCalcLineNum(nextNode = node) == degreeThreshold
            || getNodeHavedCalcLineNum(nextNode = other) == tree.degree(other) - 1) {
          calcCutValByAdjNode(graph, nextNode, uLine);
        } else {
          // Directly loop over all edges straddling two components to compute the tangent
          npCalcCutVal(graph, uLine);
        }

        offerCutQueen(other);
      }

    }

    /*-------------------------------------- Cut value calculation of various scenes near the tree --------------------------------------*/

    // Calculate the tangent value of the boundary node
    private void calcBorderCutVal(DotGraph graph, DNode border) {
      ULine uTreeLine = null;
      for (ULine uLine : tree.adjacent(border)) {
        uTreeLine = uLine;
        break;
      }

      if (uTreeLine == null) {
        throw new RuntimeException("Find the wrong border node!");
      }

      DLine treeLine = uTreeLine.getdLine();

      // Skip if overcut is already calculated
      if (lineCacheContain(treeLine)) {
        return;
      }

      double cutVal = 0;
      boolean borderIsFrom = treeLine.from() == border;
      for (ULine edge : graph.adjacent(border)) {

        if (borderIsFrom == (edge.getdLine().from() == border)) {
          cutVal += edge.getdLine().weight();
        } else {
          cutVal -= edge.getdLine().weight();
        }
      }

      // Set the cut value edge to mark the tree edge has been visited
      setCutValAndMarkTreeLine(cutVal, uTreeLine);

      // Add another node on the edge of the tree to the access queue
      offerCutQueen(treeLine.other(border));
    }

    // Directly calculate the tangent value through adjacent edges
    private void calcCutValByAdjNode(DotGraph graph, DNode node, ULine treeLine) {
      // Set the cut value and mark the tree edge as visited
      setCutValAndMarkTreeLine(
          calcCutValByAdjTreeLine(graph, node, treeLine, tree::containEdge),
          treeLine
      );
    }

    // Computes the tangency of tree edges by exhaustively enumerating the edges spanning two components
    private void npCalcCutVal(DotGraph graph, ULine treeLine) {
      setCutValAndMarkTreeLine(halfDfsCalcCutVal(graph, treeLine), treeLine);
    }

    /*-------------------------------------- other computing operations --------------------------------------*/

    private int getNodeHavedCalcLineNum(DNode node) {
      CutValRecord cutValRecord = getCutValRecord(node);
      return cutValRecord.calcNum;
    }

    private void increaseNodeHavedCalcLineNum(DNode node) {
      CutValRecord cutValRecord = getCutValRecord(node);

      cutValRecord.calcNum++;
    }

    private CutValRecord getCutValRecord(DNode node) {
      if (nodeCountValRecord == null) {
        nodeCountValRecord = new HashMap<>(dotDigraph.vertexNum());
      }

      CutValRecord cutValRecord = nodeCountValRecord.get(node);

      if (cutValRecord == null) {
        cutValRecord = new CutValRecord();
        nodeCountValRecord.put(node, cutValRecord);
      }

      return cutValRecord;
    }

    // Set the cut value of the tree edge, and mark the tree edge has been calculated, and increase
    // the cut value count of the two nodes of the tree edge
    private void setCutValAndMarkTreeLine(double cutVal, ULine treeLine) {
      treeLine.getdLine().setCutVal(cutVal);

      if (lineCache == null) {
        lineCache = new HashSet<>();
      }

      lineCache.add(treeLine.getdLine());
      increaseNodeHavedCalcLineNum(treeLine.getdLine().from());
      increaseNodeHavedCalcLineNum(treeLine.getdLine().to());

      if (cutVal < 0) {
        if (negativeLine == null) {
          negativeLine = new LinkedList<>();
        }
        negativeLine.offer(treeLine);
      }
    }

    // Determine whether this edge has been calculated cut value
    private boolean lineCacheContain(DLine treeLine) {
      if (lineCache == null) {
        return false;
      }

      return lineCache.contains(treeLine);
    }

    private void offerCutQueen(DNode node) {
      if (cutQueen == null) {
        cutQueen = new LinkedList<>();
      }

      CutValRecord cutValRecord = getCutValRecord(node);
      if (!cutValRecord.isInCutQueen) {
        cutQueen.offer(node);
        cutValRecord.isInCutQueen = true;
      }
    }
  }

  private static class CutValRecord {

    private int calcNum;

    private boolean isInCutQueen;
  }
}