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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.graphper.api.GraphContainer;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.PortHelper;
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
  private CrossCache crossCache;

  // Reusable consumer for cross calculations
  private final CrossCalc crossCalc;

  private SameRankAdjacentRecord sameRankAdjacentRecord;

  private ClusterMerge clusterMerge;

  RootCrossRank(DrawGraph drawGraph, ClusterMerge clusterMerge) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.drawGraph = drawGraph;
    this.root = new BasicCrossRank(drawGraph.getGraphviz());
    this.digraphProxy = new DedirectedEdgeGraph<>();
    this.crossCache = new CrossCache();
    this.crossCalc = new CrossCalc();
    this.clusterMerge = clusterMerge;
  }

  RootCrossRank(DrawGraph drawGraph, EdgeDedigraph<DNode, DLine> digraphProxy) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    Asserts.nullArgument(digraphProxy, "digraphProxy");
    this.drawGraph = drawGraph;
    this.root = new BasicCrossRank(drawGraph.getGraphviz());
    this.digraphProxy = digraphProxy;
    this.crossCache = new CrossCache();
    this.crossCalc = new CrossCalc();
    for (DNode node : digraphProxy) {
      addNode(node, Boolean.FALSE);
    }
  }

  void updateCross(CrossSnapshot crossSnapshot) {
    if (Objects.isNull(crossSnapshot)) {
      return;
    }

    setBasicCrossRank(crossSnapshot.crossRank);
    this.crossCache = crossSnapshot.crossCache;
  }

  void setBasicCrossRank(BasicCrossRank basicCrossRank) {
    setBasicCrossRank(basicCrossRank, false, true);
  }

  void setBasicCrossRank(BasicCrossRank basicCrossRank, boolean remainCache,
                         boolean needRefreshNodeIdx) {
    if (basicCrossRank == childCrossRank) {
      return;
    }
    this.childCrossRank = basicCrossRank;
    if (remainCache) {
      return;
    }

    int minRank = basicCrossRank.minRank();
    int maxRank = basicCrossRank.maxRank();
    for (int i = minRank - 1; i <= maxRank; i++) {
      setCacheExpired(i);

      if (i >= minRank && needRefreshNodeIdx) {
        int size = rankSize(i);
        for (int j = 0; j < size; j++) {
          DNode node = getNode(i, j);
          node.setRankIndex(j);
        }
      }
    }
  }

  void setCacheExpired() {
    for (int i = minRank(); i <= maxRank(); i++) {
      setCacheExpired(i);
    }
    crossCache.setCacheExpired();
  }

  void setCacheExpired(int rank) {
    crossCache.setCacheExpired(rank);
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
  public List<DNode> getNodes(int rank) {
    return childCrossRank == null ? root.getNodes(rank) :childCrossRank.getNodes(rank);
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
  public void exchange(DNode v, DNode w, boolean needSyncRankIdx) {
    crossRank().exchange(v, w, needSyncRankIdx);
  }

  @Override
  public void sort(Comparator<DNode> comparator, boolean needSyncRankIdx) {
    if (childCrossRank != null) {
      childCrossRank.sort(comparator, needSyncRankIdx);
    } else {
      root.sort(comparator, needSyncRankIdx);
    }
  }

  @Override
  public void sort(int rank, Comparator<DNode> comparator, boolean needSyncRankIdx) {
    if (childCrossRank != null) {
      childCrossRank.sort(rank, comparator, needSyncRankIdx);
    } else {
      root.sort(rank, comparator, needSyncRankIdx);
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

      List<DNode> nodes = root.getNodes(expandNode.getRank());
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
      List<DNode> rootRankNodes = root.getNodes(i);
      if (CollectionUtils.isEmpty(rootRankNodes)) {
        continue;
      }

      for (int j = 0; j < rankSize; j++) {
        DNode node = childCrossRank.getNode(i, j);
        int rankIdx = rankStartIdx + j;
        rootRankNodes.set(rankIdx, node);
        root.nodeRankIndex.put(node, rankIdx);
        node.setRankIndex(rankIdx);
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
    BasicCrossRank original = getBasicCrossRank();
    BasicCrossRank repl = original.clone();
    setBasicCrossRank(repl, true, false);

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

    setBasicCrossRank(original, true, false);
    CrossSnapshot medianTry = tryCacheCrossNum(repl);
    if (medianTry.getCrossNum() < crossCache.crossNum) {
      updateCross(medianTry);
    }
  }

  /**
   * Visits each vertex of each level in turn, reducing intersections by swapping two adjacent
   * vertices. Repeat this action until the number of crossings cannot be reduced.
   *
   * @param reverse access direction, true is top to bottom, false is bottom to top
   */
  void transpose(boolean reverse) {
    int delta;
    int[] leftCrossRecord = new int[3];
    int[] rightCrossRecord = new int[3];
    CrossRank crossRank = calcCrossRank();

    do {
      delta = 0;
      for (int j = crossRank.minRank(); j <= crossRank.maxRank(); j++) {
        leftCrossRecord[0] = 0;
        leftCrossRecord[1] = 0;
        leftCrossRecord[2] = 0;
        rightCrossRecord[0] = 0;
        rightCrossRecord[1] = 0;
        rightCrossRecord[2] = 0;
        delta += transposeStep(j, reverse, leftCrossRecord, rightCrossRecord);
      }
    } while (delta >= 1);
  }

  CrossSnapshot crossSnapshot() {
    return tryCacheCrossNum(getBasicCrossRank());
  }

  CrossSnapshot tryCacheCrossNum(BasicCrossRank basicCrossRank) {
    BasicCrossRank originalBasicRank = getBasicCrossRank();
    CrossCache originalCache = this.crossCache;

    if (basicCrossRank == originalBasicRank) {
      if (!originalCache.isEffective()) {
        crossNum(originalCache, true);
      }

      return new CrossSnapshot(originalCache, originalBasicRank);
    }

    CrossCache newCache = new CrossCache(crossCache);
    this.crossCache = newCache;
    setBasicCrossRank(basicCrossRank, false, false);
    crossNum(newCache, false);

    this.crossCache = originalCache;
    setBasicCrossRank(originalBasicRank, true, false);

    return new CrossSnapshot(newCache, basicCrossRank);
  }

  private void crossNum(CrossCache cache, boolean refreshRankIdx) {
    int num = 0;
    for (int i = minRank(); i <= maxRank() - 1; i++) {
      RankCrossCache rankCrossCache = cache.getRankCacheIfAbsent(i);

      if (rankCrossCache.effective) {
        num += rankCrossCache.crossNum;
      } else {
        rankCrossCache.crossNum = computeCrossNum(i, refreshRankIdx);
        rankCrossCache.effective = true;
        num += rankCrossCache.crossNum;
      }
    }
    cache.crossNum = num;
  }

  // ----------------------------------------- private ---------------------------------------------

  private double medianValue(DNode v, boolean direction) {
    int[] positions = adjPosition(v, direction);

    if (positions == null) {
      return -1;
    }

    if (positions.length == 1) {
      return positions[0];
    }

    if (positions.length == 2) {
      return (positions[0] + positions[1]) / 2.0;
    }

    Arrays.sort(positions);
    int rightIndex = positions.length / 2;
    if ((positions.length % 2) == 1) {
      return positions[rightIndex];
    }

    double l = positions[rightIndex - 1];
    double r = positions[rightIndex];
    double left = l - positions[0];
    double right = positions[positions.length - 1] - r;

    if (left == right) {
      return (l + r) / 2;
    }

    return (l * right + r * left) / (left + right);
  }

  private int[] adjPosition(DNode v, boolean direction) {
    int degree;
    if (direction) {
      degree = digraphProxy.inDegree(v);
    } else {
      degree = digraphProxy.outDegree(v);
    }

    if (degree == 0) {
      return null;
    }

    int i = 0;
    int[] adjPos = new int[degree];
    if (direction) {
      for (DLine line : digraphProxy.inAdjacent(v)) {
        DNode w = line.other(v);
        adjPos[i++] = getRankIndex(w);
      }
    } else {
      for (DLine line : digraphProxy.outAdjacent(v)) {
        DNode w = line.other(v);
        adjPos[i++] = getRankIndex(w);
      }
    }

    return adjPos;
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

    CrossRank crossRank = calcCrossRank();
    // positive direction
    if (i % 2 == 0) {
      for (int j = crossRank.minRank() + 1; j <= crossRank.maxRank(); j++) {
        rankNodesHandle(positiveAction, rankIndexAction, j);
      }
    }
    // reverse direction
    else {
      for (int j = crossRank.maxRank() - 1; j >= crossRank.minRank(); j--) {
        rankNodesHandle(reverseAction, rankIndexAction, j);
      }
    }
  }

  private void rankNodesHandle(Consumer<DNode> positiveAction,
                               IntConsumer rankIndexAction,
                               int rank) {
    CrossRank crossRank = calcCrossRank();
    int rankSize = crossRank.rankSize(rank);
    if (rankSize <= 1) {
      return;
    }

    for (int i = 0; i < rankSize; i++) {
      positiveAction.accept(crossRank.getNode(rank, i));
    }

    if (rankIndexAction != null) {
      rankIndexAction.accept(rank);
    }
  }

  private int transposeStep(int rank, boolean reverse,
                            int[] leftCrossRecord,
                            int[] rightCrossRecord) {

    int rv = 0;
    CrossRank crossRank = calcCrossRank();
    int rankSize = crossRank.rankSize(rank);

    for (int i = 0; i < rankSize - 1; i++) {
      DNode v = crossRank.getNode(rank, i);
      DNode w = crossRank.getNode(rank, i + 1);

      if (!canExchange(v, w)) {
        continue;
      }

      crossing(v, w, leftCrossRecord);
      crossing(w, v, rightCrossRecord);

      if (leftCrossRecord[2] > rightCrossRecord[2]
          || (leftCrossRecord[2] > 0 && reverse && leftCrossRecord[2] == rightCrossRecord[2])
      ) {
        int delta = leftCrossRecord[2] - rightCrossRecord[2];
        rv += delta;
        exchange(v, w, true);

        updateRankCache(v.getRank() - 1, rightCrossRecord[0] - leftCrossRecord[0]);
        updateRankCache(v.getRank(), rightCrossRecord[1] - leftCrossRecord[1]);
        crossCache.crossNum -= delta;
      }
    }

    return rv;
  }

  private void updateRankCache(int rank, int delta) {
    if (rank < minRank() || delta == 0) {
      return;
    }

    RankCrossCache rankCache = crossCache.getRankCacheIfAbsent(rank);
    if (rankCache.effective) {
      rankCache.crossNum += delta;
    }
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

  private void sortRankVertex(int rank) {
    CrossRank crossRank = calcCrossRank();
    List<DNode> nodes = crossRank.getNodes(rank);
    int last = nodes.size() - 1;

    for (int i = 0; i < last; i++) {
      for (int j = 0; j < last - i; j++) {
        DNode n = nodes.get(j);
        DNode w = nodes.get(j + 1);

        if (w.getMedian() < 0) {
          j++;
          continue;
        }
        if (!canExchange(n, w)) {
          continue;
        }

        if (n.getMedian() >= w.getMedian()) {
          exchange(n, w, false);
        }
      }
    }
  }

  private void crossing(DNode left, DNode right, int[] result) {
    int h;
    if ((h = left.getRank()) != right.getRank()) {
      throw new IllegalArgumentException(
          "Inconsistent hierarchy of vertices," + left + "," + right);
    }

    int leftSortIndex = left.getRankIndex();
    int rightSortIndex = right.getRankIndex();

    // If left and right are in order, calculate the number of intersections at the current position,
    // otherwise you need to exchange the two vertices to calculate
    boolean needExchange = leftSortIndex > rightSortIndex;

    if (needExchange) {
      left.setRankIndex(rightSortIndex);
      right.setRankIndex(leftSortIndex);
    }

    if (h != minRank()) {
      result[0] = inCross(left, right);
    }
    if (h != maxRank()) {
      result[1] = outCross(left, right);
    }
    result[2] = result[0] + result[1];

    if (needExchange) {
      left.setRankIndex(leftSortIndex);
      right.setRankIndex(rightSortIndex);
    }
  }

  private int computeCrossNum(int rank, boolean refreshRankIdx) {
    if (rank == maxRank()) {
      return 0;
    }

    int crossNum = 0;
    int rankSize = rankSize(rank);
    for (int i = 0; i < rankSize; i++) {
      DNode current = getNode(rank, i);
      if (refreshRankIdx) {
        current.setRankIndex(i);
      }

      for (int j = i + 1; j < rankSize; j++) {
        DNode next = getNode(rank, j);
        if (refreshRankIdx) {
          next.setRankIndex(j);
        }

        // current node adjacent nodes
        Iterable<DLine> curIter = digraphProxy.outAdjacent(current);
        // next node adjacent nodes
        Iterable<DLine> nextIter = digraphProxy.outAdjacent(next);

        for (DLine curAdjLine : curIter) {
          for (DLine nextAdjLine : nextIter) {
            if (isCross(curAdjLine, nextAdjLine, false)) {
              crossNum++;
            }
          }
        }
      }
    }

    return crossNum;
  }

  private int inCross(DNode n, DNode w) {
    return crossCalc.inCross(n, w);
  }

  private int outCross(DNode n, DNode w) {
    return crossCalc.outCross(n, w);
  }

  private boolean isCross(DLine line1, DLine line2, boolean useRankIdx) {
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
          return comparePointX(up, vp) < 0 == lessRankIdx(x, y, useRankIdx);
        }
        return locationTag(up, vp) * locationTag(y, x, useRankIdx)
            + locationTag(vp, up) * locationTag(x, y, useRankIdx) == 1;
      }

      double xp = getCompareNo(line1, x);
      double yp = getCompareNo(line2, y);

      if (u.getRank() == x.getRank()) {
        return comparePointX(xp, yp) < 0 == lessRankIdx(u, v, useRankIdx);
      }
      return locationTag(u, v, useRankIdx) * locationTag(yp, xp)
          + locationTag(v, u, useRankIdx) * locationTag(xp, yp) == 1;
    }

    boolean line1InSameRank = u.getRank() == x.getRank();
    boolean line2InSameRank = v.getRank() == y.getRank();

    if (line1InSameRank || line2InSameRank) {
      return false;
    }

    return locationTag(u, v, useRankIdx) * locationTag(y, x, useRankIdx)
        + locationTag(v, u, useRankIdx) * locationTag(x, y, useRankIdx) == 1;
  }

  private int locationTag(DNode v, DNode w, boolean useRankIdx) {
    if (useRankIdx) {
      return v.getRankIndex() < w.getRankIndex() ? 1 : 0;
    }
    return getRankIndex(v) < getRankIndex(w) ? 1 : 0;
  }

  private int locationTag(double o1, double o2) {
    return o1 < o2 ? 1 : 0;
  }

  private boolean lessRankIdx(DNode n, DNode w, boolean useRankIdx) {
    if (useRankIdx) {
      return n.getRankIndex() < w.getRankIndex();
    }
    return getRankIndex(n) < getRankIndex(w);
  }

  private double getCompareNo(DLine line, DNode node) {
    return PortHelper.portCompareNo(line.getLineDrawProp(), node, drawGraph);
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = minRank(); i <= maxRank(); i++) {
      int size = rankSize(i);
      for (int j = 0; j < size; j++) {
        DNode node = getNode(i, j);
        sb.append(node.name()).append(",");
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  interface ExpandInfoProvider {

    Iterable<DNode> expandNodes();

    Iterable<DNode> replaceNodes(DNode node);

    GraphContainer container();
  }

  class CrossCache {

    private int crossNum;

    private Map<Integer, RankCrossCache> rankCrossCacheMap;

    public CrossCache() {
      this.rankCrossCacheMap = new HashMap<>();
    }

    CrossCache(CrossCache crossCache) {
      if (crossCache == null) {
        return;
      }

      if (crossCache.rankCrossCacheMap == null) {
        return;
      }

      this.rankCrossCacheMap = new HashMap<>(crossCache.rankCrossCacheMap.size());
      for (Entry<Integer, RankCrossCache> entry : crossCache.rankCrossCacheMap.entrySet()) {
        Integer rank = entry.getKey();
        RankCrossCache rankCache = entry.getValue();
        this.rankCrossCacheMap.put(rank, rankCache.clone());
      }
    }

    void setCacheExpired() {
      for (int i = minRank(); i <= maxRank(); i++) {
        setCacheExpired(i);
      }
    }

    void setCacheExpired(int rank) {
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

    RankCrossCache getRankCacheIfAbsent(int rank) {
      return rankCrossCacheMap.computeIfAbsent(rank, r -> new RankCrossCache());
    }

    int getCrossNum() {
      return crossNum;
    }

    boolean isEffective() {
      if (rankCrossCacheMap.isEmpty()) {
        return false;
      }

      for (RankCrossCache cache : rankCrossCacheMap.values()) {
        if (!cache.effective) {
          return false;
        }
      }

      return true;
    }
  }

  static class RankCrossCache implements Cloneable {

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

  static class CrossSnapshot {
    private final CrossCache crossCache;

    private final BasicCrossRank crossRank;

    public CrossSnapshot(CrossCache crossCache, BasicCrossRank crossRank) {
      Objects.requireNonNull(crossCache);
      Objects.requireNonNull(crossRank);
      this.crossCache = crossCache;
      this.crossRank = crossRank;
    }

    int getCrossNum() {
      return crossCache.getCrossNum();
    }

    CrossCache getCrossCache() {
      return crossCache;
    }

    BasicCrossRank getCrossRank() {
      return crossRank;
    }
  }

  /**
   * Reusable consumer for cross calculations to avoid creating consumer objects
   */
  private class CrossCalc {
    private DNode w;
    private int crossNum;
    private DLine currentL1; // Current line from outer loop
    
    private final Consumer<DLine> inOuterConsumer = this::inOuterAccept;
    private final Consumer<DLine> innerConsumer = this::innerAccept;
    private final Consumer<DLine> outOuterConsumer = this::outOuterAccept;

    int inCross(DNode n, DNode w) {
      this.w = w;
      this.crossNum = 0;
      
      digraphProxy.forEachInAdjacent(n, inOuterConsumer);
      int result = crossNum;
      reset();
      return result;
    }

    int outCross(DNode n, DNode w) {
      this.w = w;
      this.crossNum = 0;
      
      digraphProxy.forEachOutAdjacent(n, outOuterConsumer);
      int result = crossNum;
      reset();
      return result;
    }

    private void inOuterAccept(DLine l1) {
      this.currentL1 = l1;
      digraphProxy.forEachInAdjacent(w, innerConsumer);
    }

    private void outOuterAccept(DLine l1) {
      this.currentL1 = l1;
      digraphProxy.forEachOutAdjacent(w, innerConsumer);
    }

    private void innerAccept(DLine l2) {
      if (isCross(currentL1, l2, true)) {
        crossNum++;
      }
    }

    void reset() {
      this.w = null;
      this.crossNum = 0;
      this.currentL1 = null;
    }
  }
}
