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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import org.graphper.api.GraphContainer;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.dot.MinCross.ClusterMerge;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

class RootCrossRank implements CrossRank {

  private static final int MIN_CROSS_SCALE = 256;
  private final DrawGraph drawGraph;

  private final BasicCrossRank root;

  private BasicCrossRank childCrossRank;

  private Map<Integer, Integer> rankStartIndex;

  private final EdgeDedigraph<DNode, DLine> digraphProxy;

  // Cross Number Cache
  private final Map<Integer, RankCrossCache> rankCrossCacheMap;

  private SameRankAdjacentRecord sameRankAdjacentRecord;

  private ClusterMerge clusterMerge;

  RootCrossRank(DrawGraph drawGraph, ClusterMerge clusterMerge) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.drawGraph = drawGraph;
    this.root = new BasicCrossRank(drawGraph.getGraphviz());
    this.digraphProxy = new DedirectedEdgeGraph<>();
    this.rankCrossCacheMap = new HashMap<>();
    this.clusterMerge = clusterMerge;
  }

  RootCrossRank(DrawGraph drawGraph, EdgeDedigraph<DNode, DLine> digraphProxy) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    Asserts.nullArgument(digraphProxy, "digraphProxy");
    this.drawGraph = drawGraph;
    this.root = new BasicCrossRank(drawGraph.getGraphviz());
    this.digraphProxy = digraphProxy;
    this.rankCrossCacheMap = new HashMap<>();
    for (DNode node : digraphProxy) {
      addNode(node, Boolean.FALSE);
    }
  }

  void setBasicCrossRank(BasicCrossRank basicCrossRank) {
    if (basicCrossRank == childCrossRank) {
      return;
    }
    this.childCrossRank = basicCrossRank;

    setCacheExpired();
  }

  void setCacheExpired() {
    for (int i = minRank(); i <= maxRank(); i++) {
      setCacheExpired(i);
    }
  }

  void setSameRankAdjacentRecord(
      SameRankAdjacentRecord sameRankAdjacentRecord) {
    this.sameRankAdjacentRecord = sameRankAdjacentRecord;
  }

  EdgeDedigraph<DNode, DLine> getDigraphProxy() {
    return digraphProxy;
  }

  BasicCrossRank getBasicCrossRank() {
    return childCrossRank != null ? childCrossRank : root;
  }

  SameRankAdjacentRecord getSameRankAdjacentRecord() {
    return sameRankAdjacentRecord;
  }

  @Override
  public int getRankIndex(DNode node) {
    if (childCrossRank == null) {
      return root.getRankIndex(node);
    }
    Integer idx = childCrossRank.safeGetRankIndex(node);
    if (idx == null) {
      return root.getRankIndex(node);
    }
    return getChildRankStartIndex(node.getRank()) + idx;
  }

  @Override
  public Integer safeGetRankIndex(DNode node) {
    if (childCrossRank == null) {
      return root.safeGetRankIndex(node);
    }
    Integer idx = childCrossRank.safeGetRankIndex(node);
    if (idx != null) {
      return getChildRankStartIndex(node.getRank()) + idx;
    }
    return root.safeGetRankIndex(node);
  }

  @Override
  public DNode getNode(int rank, int rankIdx) {
    if (childCrossRank == null) {
      return root.getNode(rank, rankIdx);
    }
    int rootRankIndex = getChildRankStartIndex(rank);
    if (rankIdx < rootRankIndex || rankIdx >= rootRankIndex + childCrossRank.rankSize(rank)) {
      return root.getNode(rank, rankIdx);
    }
    return childCrossRank.getNode(rank, rankIdx - rootRankIndex);
  }

  @Override
  public int rankSize(int rank) {
    return root.rankSize(rank);
  }

  @Override
  public int minRank() {
    return root.minRank();
  }

  @Override
  public int maxRank() {
    return root.maxRank();
  }

  @Override
  public void exchange(DNode v, DNode w) {
    crossRank().exchange(v, w);
  }

  @Override
  public void sort(Comparator<DNode> comparator) {
    if (childCrossRank != null) {
      childCrossRank.sort(comparator);
    } else {
      root.sort(comparator);
    }
  }

  @Override
  public void sort(int rank, Comparator<DNode> comparator) {
    if (childCrossRank != null) {
      childCrossRank.sort(rank, comparator);
    } else {
      root.sort(rank, comparator);
    }
  }

  @Override
  public GraphContainer container() {
    return root.container;
  }

  @Override
  public void addNode(DNode node) {
    addNode(node, Boolean.TRUE);
  }

  void addNode(DNode node, boolean proxyGraphAdd) {
    if (proxyGraphAdd) {
      digraphProxy.add(node);
    }
    root.addNode(node);
  }

  void addEdge(DLine line) {
    digraphProxy.addEdge(line);
  }

  BasicCrossRank expand(ExpandInfoProvider expandInfoProvider) {
    Asserts.nullArgument(expandInfoProvider, "expandInfoProvider");
    Iterable<DNode> expandNodes = expandInfoProvider.expandNodes();
    if (expandNodes == null) {
      return null;
    }

    if (rankStartIndex == null) {
      rankStartIndex = new HashMap<>();
    } else {
      rankStartIndex.clear();
    }
    BasicCrossRank basicCrossRank = new BasicCrossRank(expandInfoProvider.container());
    /*
     * 1.handle every expand node, delete it and replace by ExpandInfoProvider#replaceNodes
     * 2.add ExpandInfoProvider#expandLines
     * */
    for (DNode expandNode : expandNodes) {
      digraphProxy.remove(expandNode);

      List<DNode> nodes = root.rankNode.get(expandNode.getRank());
      Asserts.illegalArgument(
          CollectionUtils.isEmpty(nodes),
          "Illegal expand node, root not contain"
      );
      Iterable<DNode> replaceNodes = expandInfoProvider.replaceNodes(expandNode);
      if (replaceNodes == null) {
        continue;
      }

      int i = 0;
      int rankIndex = root.getRankIndex(expandNode);
      rankStartIndex.put(expandNode.getRank(), rankIndex);
      for (DNode replaceNode : replaceNodes) {
        if (i == 0) {
          nodes.set(rankIndex, replaceNode);
          root.nodeRankIndex.remove(expandNode);
        } else {
          nodes.add(rankIndex + i, replaceNode);
        }
        root.nodeRankIndex.put(replaceNode, rankIndex + i);
        basicCrossRank.addNode(replaceNode);
        i++;
      }

      // sync node id that rank index greater than insert index
      for (int j = rankIndex + i; j < nodes.size(); j++) {
        root.nodeRankIndex.put(nodes.get(j), j);
      }
    }

    setBasicCrossRank(basicCrossRank);
    return basicCrossRank;
  }

  void syncChildOrder() {
    if (childCrossRank == null) {
      return;
    }

    for (int i = childCrossRank.minRank(); i <= childCrossRank.maxRank(); i++) {
      int rankSize = childCrossRank.rankSize(i);
      int rankStartIdx = getChildRankStartIndex(i);
      for (int j = 0; j < rankSize; j++) {
        DNode node = childCrossRank.getNode(i, j);
        root.exchange(node, root.getNode(i, rankStartIdx + j));
      }
    }
  }

  /**
   * According to the parity of the current traversal times, it is decided to perform the "median"
   * calculation of the hierarchical nodes from the top or from the bottom.
   *
   * @param i times
   */
  void vmedian(int i) {
    Consumer<DNode> positiveAction = v -> {
      double v1 = medianValue(v, true);
      v.setMedian(v1);
    };
    Consumer<DNode> reverseAction = v -> {
      double v1 = medianValue(v, false);
      v.setMedian(v1);
    };

    IntConsumer rankIndexAction = this::sortRankVertex;

    accessRankNode(i, positiveAction, reverseAction, rankIndexAction);
  }

  void adjPostion(Consumer<DNode> adjAction, DNode node, boolean direction, boolean isProxy) {
    Objects.requireNonNull(adjAction);
    Objects.requireNonNull(node);

    /*
     * The action of this adjacent vertex will be carried out according to the following rules：
     * 1.If it is accessed from the upper and lower levels of the proxy graph,
     * directly obtain the adjacent level vertices (regardless of whether they are virtual vertices) and consume
     * 2.If it is the upper and lower levels of the original image, it will be handed over to a recursive adjacent level access logic - NodeAction,
     * which will get the first real node accessed
     * */
    Predicate<DNode> nodeAction = v -> {
      if (!isProxy && v.isVirtual()) {

        // The upper and lower levels of the original image are accessed. If the vertices of the adjacent level are virtual vertices,
        // access to the adjacent vertices of this virtual vertex will be skipped.
        class NodeAction implements Predicate<DNode> {

          // Record the first real vertex visited
          private DNode adjRealNode;

          @Override
          public boolean test(DNode vertex) {
            if (vertex.isVirtual()) {
              // virtual vertex recursive call
              adjNodeAccess(direction, vertex, this);
            } else {
              // direct assignment of real vertices
              adjRealNode = vertex;
            }

            // Jump out of the reachability matrix retrieval directly after finding the real vertex
            return false;
          }

          public DNode getAdjRealNode() {
            return adjRealNode;
          }
        }

        NodeAction action = new NodeAction();
        adjNodeAccess(direction, v, action);

        v = action.getAdjRealNode();
      }

      adjAction.accept(v);

      return true;
    };

    adjNodeAccess(direction, node, nodeAction);
  }

  /**
   * Visits each vertex of each level in turn, reducing intersections by swapping two adjacent
   * vertices. Repeat this action until the number of crossings cannot be reduced.
   *
   * @param reverse access direction, true is top to bottom, false is bottom to top
   */
  void transpose(boolean reverse) {
    int delta;
    do {
      delta = 0;
      for (int j = calcCrossRank().minRank(); j <= calcCrossRank().maxRank(); j++) {
        delta += transposeStep(j, reverse);
      }
    } while (delta >= 1);
  }

  int currentCrossNum() {
    setCacheExpired();
    int num = 0;
    for (int i = minRank(); i <= maxRank(); i++) {
      RankCrossCache rankCrossCache = getRankCacheIfAbsent(i);

      if (rankCrossCache.effective) {
        num += rankCrossCache.crossNum;
      } else {
        rankCrossCache.crossNum = computeCrossNum(i);
        rankCrossCache.effective = true;
        num += rankCrossCache.crossNum;
      }
    }
    return num;
  }

  // ----------------------------------------- private ---------------------------------------------

  private double medianValue(DNode v, boolean direction) {
    List<Double> positions = adjPostion(v, direction);

    if (CollectionUtils.isEmpty(positions)) {
      return -1.0;
    }

    if (positions.size() == 1) {
      return positions.get(0);
    }

    if (positions.size() == 2) {
      return (positions.get(0) + positions.get(1)) / 2;
    }

    positions.sort(Double::compareTo);
    int rightIndex = positions.size() / 2;
    if ((positions.size() % 2) == 1) {
      return positions.get(rightIndex);
    }

    Double l = positions.get(rightIndex - 1);
    Double r = positions.get(rightIndex);
    double left = l - positions.get(0);
    double right = positions.get(positions.size() - 1) - r;

    if (left == right) {
      return (l + r) / 2;
    }

    return (l * right + r * left) / (left + right);
  }

  private List<Double> adjPostion(DNode v, boolean direction) {
    List<Double> positions = new ArrayList<>();
    Consumer<DNode> adjAction = vertex ->
        positions.add((double) (getRankIndex(vertex)) * MIN_CROSS_SCALE);

    adjPostion(adjAction, v, direction, true);

    return positions;
  }

  /**
   * Visit the vertices of each level in a forward and reverse manner.
   *
   * @param i               Forward and reverse signs Even forward, odd reverse
   * @param positiveAction  Action when visiting a vertex forward
   * @param reverseAction   Action when visiting vertices in reverse
   * @param rankIndexAction Action after visiting post action
   */
  private void accessRankNode(int i, Consumer<DNode> positiveAction,
                              Consumer<DNode> reverseAction,
                              IntConsumer rankIndexAction) {
    Objects.requireNonNull(positiveAction);
    Objects.requireNonNull(reverseAction);

    // positive direction
    if (i % 2 == 0) {
      for (int j = calcCrossRank().minRank(); j <= calcCrossRank().maxRank(); j++) {
        rankNodesHandle(positiveAction, rankIndexAction, j);
      }
    }
    // reverse direction
    else {
      for (int j = calcCrossRank().maxRank(); j >= calcCrossRank().minRank(); j--) {
        rankNodesHandle(reverseAction, rankIndexAction, j);
      }
    }
  }

  private void rankNodesHandle(Consumer<DNode> positiveAction,
                               IntConsumer rankIndexAction,
                               int rank) {
    if (calcCrossRank().rankSize(rank) <= 1) {
      return;
    }

    for (int i = 0; i < calcCrossRank().rankSize(rank); i++) {
      positiveAction.accept(calcCrossRank().getNode(rank, i));
    }

    if (rankIndexAction != null) {
      rankIndexAction.accept(rank);
    }
  }

  private int transposeStep(int rank, boolean reverse) {
    int[] leftCrossRecord = new int[3];
    int[] rightCrossRecord = new int[3];

    int rv = 0;

    for (int i = 0; i < calcCrossRank().rankSize(rank) - 1; i++) {
      DNode v = calcCrossRank().getNode(rank, i);
      DNode w = calcCrossRank().getNode(rank, i + 1);

      if (!canExchange(v, w)) {
        continue;
      }

      crossing(v, w, leftCrossRecord);
      crossing(w, v, rightCrossRecord);

      if (leftCrossRecord[2] == rightCrossRecord[2] && i > 0
          && !canExchange(calcCrossRank().getNode(rank, i - 1), v)) {
        continue;
      }

      if (leftCrossRecord[2] > rightCrossRecord[2]
          || (leftCrossRecord[2] > 0 && reverse && leftCrossRecord[2] == rightCrossRecord[2])
      ) {
        rv += (leftCrossRecord[2] - rightCrossRecord[2]);
        exchange(v, w);

        setCacheExpired(rank);
      }
    }

    return rv;
  }


  private void setCacheExpired(int rank) {
    RankCrossCache rankCrossCache = rankCrossCacheMap.get(rank);
    if (rankCrossCache == null) {
      return;
    }

    rankCrossCache.effective = false;

    rankCrossCache = rankCrossCacheMap.get(rank - 1);
    if (rankCrossCache == null) {
      return;
    }

    rankCrossCache.effective = false;
  }

  private boolean canExchange(DNode left, DNode right) {
    if (possibleClusterIntersect(left, right)) {
      return false;
    }

    if (sameRankAdjacentRecord == null) {
      return true;
    }

    boolean haveSameAdj = sameRankAdjacentRecord.outContains(left, right);
    if (haveSameAdj) {
      return false;
    }

    Integer leftIdx = childCrossRank.safeGetRankIndex(left);
    if (leftIdx == null) {
      return true;
    }
    Set<DNode> inAdjs = sameRankAdjacentRecord.inAdjacent(right);
    for (DNode in : inAdjs) {
      Integer idx = childCrossRank.safeGetRankIndex(in);
      if (idx != null && idx > leftIdx) {
        return false;
      }
    }

    return true;
  }

  private boolean possibleClusterIntersect(DNode left, DNode right) {
    if (childCrossRank == null || left.getContainer() == right.getContainer()) {
      return false;
    }

    if (!isAdj(left, right) && left.getContainer() != right.getContainer()) {
      return true;
    }

    GraphContainer container = childCrossRank.container;
    GraphContainer leftDirC = DotAttachment
        .clusterDirectContainer(drawGraph.getGraphviz(), container, left);
    GraphContainer rightDirC = DotAttachment
        .clusterDirectContainer(drawGraph.getGraphviz(), container, right);

    if (leftDirC == null || rightDirC == null || leftDirC.isGraphviz() || rightDirC.isGraphviz()) {
      return false;
    }

    return !clusterMerge.isSingleRankCluster(leftDirC)
        && !clusterMerge.isSingleRankCluster(rightDirC);
  }

  private boolean isAdj(DNode left, DNode right) {
    int leftIdx = getRankIndex(left);
    int rightIdx = getRankIndex(right);
    return Math.abs(leftIdx - rightIdx) == 1;
  }

  private void adjNodeAccess(boolean direction,
                             DNode node,
                             Predicate<DNode> nodeActionAndReturnNeedContinue) {
    Iterable<DLine> dLines = direction
        ? digraphProxy.inAdjacent(node)
        : digraphProxy.outAdjacent(node);

    for (DLine line : dLines) {
      // get adjacent vertices
      DNode vertexInfo = line.other(node);

      // Consume vertices and jump out if you don't need to access the next value
      if (Objects.equals(nodeActionAndReturnNeedContinue.test(vertexInfo), Boolean.FALSE)) {
        break;
      }
    }
  }

  private void sortRankVertex(int rank) {
    int endIndex = calcCrossRank().rankSize(rank) - 1;
    int left;
    int right;

    // Number of times to traverse nodes
    for (int i = rank; i <= endIndex; i++) {
      left = 0;

      while (left < endIndex) {
        DNode leftNode = calcCrossRank().getNode(rank, left);
        DNode rightNode = null;

        // Find the first one on the left whose median value is greater than 0 in the hierarchy
        while (left < endIndex && leftNode.getMedian() < 0) {
          left++;
        }

        if (left >= endIndex) {
          break;
        }

        boolean canExchange = true;
        // The right side of left starts to find the first node that can be compared
        for (right = left + 1; right <= endIndex; right++) {
          rightNode = calcCrossRank().getNode(rank, right);

          if (!canExchange(leftNode, rightNode)) {
            canExchange = false;
            break;
          }

          if (rightNode.getMedian() >= 0) {
            break;
          }
        }

        if (right > endIndex) {
          break;
        }

        double lm = leftNode.getMedian();
        double rm = rightNode.getMedian();

        if (lm >= rm && canExchange) {
          setCacheExpired(rank);
          exchange(leftNode, rightNode);
        }

        left = right;
      }
    }
  }

  private void crossing(DNode left, DNode right, int[] result) {
    int h;
    if ((h = left.getRank()) != right.getRank()) {
      throw new IllegalArgumentException(
          "Inconsistent hierarchy of vertices," + left + "," + right);
    }

    int leftSortIndex = calcCrossRank().getRankIndex(left);
    int rightSortIndex = calcCrossRank().getRankIndex(right);

    // If left and right are in order, calculate the number of intersections at the current position,
    // otherwise you need to exchange the two vertices to calculate
    boolean needExchange = leftSortIndex > rightSortIndex;

    if (needExchange) {
      exchange(left, right);
    }

    if (h != minRank()) {
      result[0] = inCross(left, right);
    }
    if (h != maxRank()) {
      result[1] = outCross(left, right);
    }
    result[2] = result[0] + result[1];

    if (needExchange) {
      exchange(left, right);
    }
  }

  private RankCrossCache getRankCacheIfAbsent(int rank) {
    return rankCrossCacheMap.computeIfAbsent(rank, r -> new RankCrossCache());
  }

  private int computeCrossNum(int rank) {
    int crossNum = 0;
    int rankSize = rankSize(rank);
    for (int i = 0; i < rankSize; i++) {
      DNode current = getNode(rank, i);
      for (int j = i + 1; j < rankSize; j++) {
        DNode next = getNode(rank, j);
        // current node adjacent nodes
        Iterable<DLine> curIter = digraphProxy.outAdjacent(current);
        // next node adjacent nodes
        Iterable<DLine> nextIter = digraphProxy.outAdjacent(next);

        for (DLine curAdjLine : curIter) {
          for (DLine nextAdjLine : nextIter) {
            if (isCross(curAdjLine, nextAdjLine)) {
              crossNum++;
            }
          }
        }
      }
    }

    return crossNum;
  }

  private int inCross(DNode n, DNode w) {
    int count = 0;
    for (DLine l1 : digraphProxy.inAdjacent(n)) {
      for (DLine l2 : digraphProxy.inAdjacent(w)) {
        if (isCross(l1, l2)) {
          count++;
        }
      }
    }

    return count;
  }

  private int outCross(DNode n, DNode w) {
    int count = 0;
    for (DLine l1 : digraphProxy.outAdjacent(n)) {
      for (DLine l2 : digraphProxy.outAdjacent(w)) {
        if (isCross(l1, l2)) {
          count++;
        }
      }
    }

    return count;
  }

  private boolean isCross(DLine line1, DLine line2) {
    DNode u = line1.from();
    DNode x = line1.to();
    DNode v = line2.from();
    DNode y = line2.to();

    if (u == v || u == y || x == v || x == y) {
      if ((u == v) == (x == y)) {
        return false;
      }

      if (u == v) {
        double up = getCompareNo(line1, u);
        double vp = getCompareNo(line2, v);

        if (x.getRank() == u.getRank()) {
          return comparePointX(up, vp) < 0 == getRankIndex(x) < getRankIndex(y);
        }
        return locationTag(up, vp) * locationTag(y, x)
            + locationTag(vp, up) * locationTag(x, y) == 1;
      }

      double xp = getCompareNo(line1, x);
      double yp = getCompareNo(line2, y);

      if (u.getRank() == x.getRank()) {
        return comparePointX(xp, yp) < 0 == getRankIndex(u) < getRankIndex(v);
      }
      return locationTag(u, v) * locationTag(yp, xp) + locationTag(v, u) * locationTag(xp, yp) == 1;
    }

    boolean line1InSameRank = u.getRank() == x.getRank();
    boolean line2InSameRank = v.getRank() == y.getRank();

    if (line1InSameRank || line2InSameRank) {
      return false;
    }

    return locationTag(u, v) * locationTag(y, x) + locationTag(v, u) * locationTag(x, y) == 1;
  }

  private int locationTag(DNode v, DNode w) {
    return getRankIndex(v) < getRankIndex(w) ? 1 : 0;
  }

  private int locationTag(double o1, double o2) {
    return o1 < o2 ? 1 : 0;
  }

  private double getCompareNo(DLine line, DNode node) {
    return PortHelper.portCompareNo(line.getLine(), node, drawGraph);
  }

  private int comparePointX(double p1, double p2) {
    return Double.compare(p1, p2);
  }

  private CrossRank crossRank() {
    if (childCrossRank != null) {
      return childCrossRank;
    }

    return root;
  }

  private CrossRank calcCrossRank() {
    if (childCrossRank != null) {
      return childCrossRank;
    }
    return this;
  }

  private int getChildRankStartIndex(int rank) {
    Integer idx;
    if (childCrossRank == null || rankStartIndex == null
        || (idx = rankStartIndex.get(rank)) == null) {
      return 0;
    }
    return idx;
  }

  interface ExpandInfoProvider {

    Iterable<DNode> expandNodes();

    Iterable<DNode> replaceNodes(DNode node);

    GraphContainer container();
  }

  private static class RankCrossCache implements Cloneable {

    private int crossNum;

    private boolean effective;

    private RankCrossCache() {
    }

    @Override
    protected RankCrossCache clone() {
      try {
        return (RankCrossCache) super.clone();
      } catch (CloneNotSupportedException e) {
        RankCrossCache crossCache = new RankCrossCache();
        crossCache.crossNum = crossNum;
        crossCache.effective = effective;
        return crossCache;
      }
    }
  }
}
