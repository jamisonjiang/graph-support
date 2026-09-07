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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.graphper.api.GraphContainer;
import org.graphper.api.attributes.Layout;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.PortHelper;
import org.graphper.layout.dot.MinCross.ClusterMerge;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

class RootCrossRank implements CrossRank {

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

  /**
   * Set once every cluster has been expanded. From then on a rank holds the cluster members
   * themselves instead of one merge node per cluster, so keeping clusters contiguous is no longer
   * automatic and has to be enforced when nodes trade places.
   */
  private boolean allClustersExpanded;

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

  /**
   * Drops the child arrangement so that the following passes act on every node of the graph. The
   * rank start offsets belong to the child arrangement and index into it, so they have to go as
   * well: the root arrangement is addressed by absolute rank indexes.
   */
  void resetToRoot() {
    this.childCrossRank = null;
    this.rankStartIndex = null;
    this.allClustersExpanded = true;
    setCacheExpired();
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
  /**
   * Assigns each node a stable number, in the arrangement's own order.
   *
   * <p>Identity hash codes would be the obvious key here and are the wrong choice: they differ from one
   * JVM run to the next, which would make the signature - and therefore the point at which a cycle is
   * noticed, and therefore the layout - irreproducible.
   */
  private Map<DNode, Integer> nodeIds(CrossRank crossRank) {
    Map<DNode, Integer> ids = new HashMap<>();
    for (int rank = crossRank.minRank(); rank <= crossRank.maxRank(); rank++) {
      int size = crossRank.rankSize(rank);
      for (int i = 0; i < size; i++) {
        ids.putIfAbsent(crossRank.getNode(rank, i), ids.size());
      }
    }
    return ids;
  }

  /**
   * Order-sensitive fingerprint of the whole node arrangement, used to notice that
   * {@link #transpose(boolean)} has returned to an arrangement it already produced.
   */
  private int orderSignature(CrossRank crossRank, Map<DNode, Integer> ids) {
    int hash = 1;
    for (int rank = crossRank.minRank(); rank <= crossRank.maxRank(); rank++) {
      int size = crossRank.rankSize(rank);
      for (int i = 0; i < size; i++) {
        Integer id = ids.get(crossRank.getNode(rank, i));
        hash = 31 * hash + (id == null ? 0 : id + 1);
      }
      hash = 31 * hash;
    }
    return hash;
  }

  void transpose(boolean reverse) {
    int delta;
    int[] leftCrossRecord = new int[3];
    int[] rightCrossRecord = new int[3];
    CrossRank crossRank = calcCrossRank();

    /*
     * The loop below assumes delta reflects every reordering, so that it reaches 0 once no crossing can
     * be removed. Curvature swaps break that: they reorder a rank while contributing 0, which lets a
     * swap at one rank restore a crossing at another, whose removal then reports a genuine delta of 1
     * forever. Two such ranks can take turns undoing each other and the loop never exits - observed at
     * over 94 million iterations on a nine node graph. Well behaved graphs converge in a few dozen
     * iterations, so this bound only cuts off the pathological cycle.
     */
    Map<DNode, Integer> nodeIds = nodeIds(crossRank);
    Set<Integer> visitedOrders = new HashSet<>();
    visitedOrders.add(orderSignature(crossRank, nodeIds));

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

      /*
       * Stopping on a repeated order is not a heuristic: the loop is deterministic, so revisiting an
       * order means every subsequent order repeats too and no further crossing can be removed. It is
       * therefore the earliest provably correct exit, and it terminates the oscillation instead of
       * letting the iteration bound truncate it after thousands of wasted rounds.
       */
      if (delta >= 1 && !visitedOrders.add(orderSignature(crossRank, nodeIds))) {
        break;
      }
    } while (delta >= 1);
  }

  CrossSnapshot crossSnapshot() {
    return tryCacheCrossNum(getBasicCrossRank());
  }

  /**
   * Move a flat leaf together with its neighbor past one adjacent node. Moving just the leaf can
   * increase crossings, trapping ordinary transpose even when moving the intact pair improves them.
   * This is one bounded scan, not a search over arbitrary nonadjacent permutations.
   */
  void transposeFlatPairs() {
    if (!allClustersExpanded || crossSnapshot().getCrossNum() == 0) {
      return;
    }
    CrossRank current = calcCrossRank();
    for (int rank = current.minRank(); rank <= current.maxRank(); rank++) {
      for (int i = 0; i + 2 < current.rankSize(rank); i++) {
        for (boolean pairFirst : new boolean[]{true, false}) {
          DNode a = current.getNode(rank, i);
          DNode b = current.getNode(rank, i + 1);
          DNode c = current.getNode(rank, i + 2);
          DNode first = pairFirst ? a : b;
          DNode second = pairFirst ? b : c;
          boolean flatLeaf = false;
          for (DNode leaf : new DNode[]{first, second}) {
            if (digraphProxy.degree(leaf) != 1) {
              continue;
            }
            for (DLine line : digraphProxy.outAdjacent(leaf)) {
              flatLeaf |= line.isSameRank() && line.other(leaf) == (leaf == first ? second : first);
            }
            for (DLine line : digraphProxy.inAdjacent(leaf)) {
              flatLeaf |= line.isSameRank() && line.other(leaf) == (leaf == first ? second : first);
            }
          }
          if (!flatLeaf || !canExchange(pairFirst ? b : a, pairFirst ? c : b)) {
            continue;
          }
          // Flat edges also affect pairs incident to other nodes, so the adjacent-edge delta is
          // insufficient here. Recount only the two affected layers, never the whole graph.
          int before = computeCrossNum(rank, false);
          if (rank > minRank()) {
            before += computeCrossNum(rank - 1, false);
          }
          exchange(pairFirst ? b : a, pairFirst ? c : b, true);
          if (!canExchange(a, c)) {
            exchange(pairFirst ? b : a, pairFirst ? c : b, true);
            continue;
          }
          exchange(a, c, true);
          int after = computeCrossNum(rank, false);
          if (rank > minRank()) {
            after += computeCrossNum(rank - 1, false);
          }
          if (after < before) {
            setCacheExpired(rank);
          } else {
            exchange(a, c, true);
            exchange(pairFirst ? b : a, pairFirst ? c : b, true);
          }
        }
      }
    }
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
    for (int i = minRank(); i <= maxRank(); i++) {
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
          || (leftCrossRecord[2] > 0 && reverse
          && leftCrossRecord[2] == rightCrossRecord[2]
          && canSacrificeCurvature(v, w))
      ) {
        int delta = leftCrossRecord[2] - rightCrossRecord[2];
        rv += delta;
        exchange(v, w, true);

        /*
         * The total is the sum of the per rank caches, so it may only absorb what the ranks absorbed.
         * Updating it with the full delta regardless drove it below zero on hundreds of ordinary
         * graphs, and MinCross steers on this value.
         */
        int applied = updateRankCache(v.getRank() - 1,
                                      rightCrossRecord[0] - leftCrossRecord[0]);
        applied += updateRankCache(v.getRank(),
                                   rightCrossRecord[1] - leftCrossRecord[1]);
        crossCache.crossNum += applied;
      }
    }

    return rv;
  }

  private boolean canSacrificeCurvature(DNode v, DNode w) {
    if (isDot()) {
      return true;
    }
    return !v.isVirtual() && !w.isVirtual();
  }

  /**
   * Applies a crossing delta to one rank's cache, and reports how much was actually applied.
   *
   * <p>An expired rank drops the delta on purpose, because its value gets recomputed from scratch on
   * the next read. The caller has to know that, otherwise the running total would absorb increments
   * that no rank did — and the total is defined as the sum of the ranks (see {@link #crossNum}).
   *
   * @return the delta that landed in the cache, {@code 0} when it was dropped
   */
  private int updateRankCache(int rank, int delta) {
    if (rank < minRank() || delta == 0) {
      return 0;
    }

    RankCrossCache rankCache = crossCache.getRankCacheIfAbsent(rank);
    if (rankCache.effective) {
      rankCache.crossNum += delta;
      return delta;
    }
    return 0;
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

    CrossRank current = getBasicCrossRank();
    Integer leftIdx = current.safeGetRankIndex(left);
    if (leftIdx == null) {
      return true;
    }
    Set<DNode> inAdjs = sameRankAdjacentRecord.inAdjacent(right);
    for (DNode in : inAdjs) {
      Integer idx = current.safeGetRankIndex(in);
      if (idx != null && idx > leftIdx) {
        return false;
      }
    }

    return true;
  }

  private boolean possibleClusterIntersect(DNode left, DNode right) {
    if (clusterMerge == null) {
      return false;
    }

    // The final pass sees the expanded members rather than atomic cluster representatives. Moving
    // one member across any container boundary would split that container, including nested ones.
    if (allClustersExpanded) {
      return left.getContainer() != right.getContainer();
    }

    if (left.getContainer() == right.getContainer()) {
      return false;
    }

    GraphContainer container = childCrossRank == null ? root.container : childCrossRank.container;
    GraphContainer leftDirC = DotAttachment
        .clusterDirectContainer(drawGraph.getGraphviz(), container, left);
    GraphContainer rightDirC = DotAttachment
        .clusterDirectContainer(drawGraph.getGraphviz(), container, right);

    if (leftDirC == null || rightDirC == null) {
      return false;
    }

    if (leftDirC.isGraphviz() || rightDirC.isGraphviz()) {
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

    result[0] = h != minRank() ? inCross(left, right) : 0;
    result[1] = h != maxRank() ? outCross(left, right) : 0;
    result[1] += flatCross(left, right);
    result[2] = result[0] + result[1];

    if (needExchange) {
      left.setRankIndex(leftSortIndex);
      right.setRankIndex(rightSortIndex);
    }
  }

  private int computeCrossNum(int rank, boolean refreshRankIdx) {
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

  private int flatCross(DNode left, DNode right) {
    List<DLine> leftLines = null;
    List<DLine> rightLines = null;
    for (int i = 0; i < 2; i++) {
      DNode node = i == 0 ? left : right;
      List<DLine> lines = null;
      for (DLine line : digraphProxy.outAdjacent(node)) {
        if (line.isSameRank()) {
          if (lines == null) {
            lines = new ArrayList<>();
          }
          lines.add(line);
        }
      }
      for (DLine line : digraphProxy.inAdjacent(node)) {
        if (line.isSameRank() && line.from() != node) {
          if (lines == null) {
            lines = new ArrayList<>();
          }
          lines.add(line);
        }
      }
      if (lines == null) {
        return 0;
      }
      if (i == 0) {
        leftLines = lines;
      } else {
        rightLines = lines;
      }
    }

    int count = 0;
    for (DLine a : leftLines) {
      for (DLine b : rightLines) {
        // Match the total's distinct-origin pairs and argument order, including shared heads.
        // Pairs present in both orientations connect left to right and cannot cross each other.
        if (a.from() == b.from()) {
          continue;
        }
        boolean ordered = a.from().getRankIndex() < b.from().getRankIndex();
        if (isCross(ordered ? a : b, ordered ? b : a, true)) {
          count++;
        }
      }
    }
    return count;
  }

  private int inCross(DNode n, DNode w) {
    return crossCalc.inCross(n, w);
  }

  private int outCross(DNode n, DNode w) {
    return crossCalc.outCross(n, w);
  }

  private boolean isCross(DLine line1, DLine line2, boolean useRankIdx) {
    if (shareEndpoint(line1, line2)) {
      return sharedEndpointEdgesCross(line1, line2, useRankIdx);
    }

    boolean line1Flat = line1.isSameRank();
    boolean line2Flat = line2.isSameRank();
    if (line1Flat || line2Flat) {
      if ((line1Flat && !isRealFlatEdge(line1)) || (line2Flat && !isRealFlatEdge(line2))) {
        return false;
      }
      return flatEdgesCross(line1, line2, line1Flat, line2Flat, useRankIdx);
    }

    return differentRankEdgesCross(line1, line2, useRankIdx);
  }

  private boolean isRealFlatEdge(DLine line) {
    return line.getLineDrawProp() != null && Integer.valueOf(0).equals(line.lineAttrs().getMinlen());
  }

  private boolean shareEndpoint(DLine line1, DLine line2) {
    return line1.from() == line2.from() || line1.from() == line2.to()
        || line1.to() == line2.from() || line1.to() == line2.to();
  }

  private boolean sharedEndpointEdgesCross(DLine line1, DLine line2, boolean useRankIdx) {
    DNode line1Tail = line1.from();
    DNode line1Head = line1.to();
    DNode line2Tail = line2.from();
    DNode line2Head = line2.to();

    // Parallel and opposing edges use separate routing and do not cross each other here.
    if ((line1Tail == line2Tail && line1Head == line2Head)
        || (line1Tail == line2Head && line1Head == line2Tail)) {
      return false;
    }
    if (line1Tail == line2Tail) {
      return commonTailEdgesCross(line1, line2, useRankIdx);
    }
    if (line1Head == line2Head) {
      return commonHeadEdgesCross(line1, line2, useRankIdx);
    }
    // A head-to-tail chain only touches at its shared node.
    return false;
  }

  private boolean commonTailEdgesCross(DLine line1, DLine line2, boolean useRankIdx) {
    DNode tail = line1.from();
    DNode line1Head = line1.to();
    DNode line2Head = line2.to();
    double line1Port = getCompareNo(line1, tail);
    double line2Port = getCompareNo(line2, tail);

    if (line1.isSameRank()) {
      return comparePointX(line1Port, line2Port) < 0
          == lessRankIdx(line1Head, line2Head, useRankIdx);
    }
    return locationTag(line1Port, line2Port) * locationTag(line2Head, line1Head, useRankIdx)
        + locationTag(line2Port, line1Port) * locationTag(line1Head, line2Head, useRankIdx) == 1;
  }

  private boolean commonHeadEdgesCross(DLine line1, DLine line2, boolean useRankIdx) {
    DNode line1Tail = line1.from();
    DNode line2Tail = line2.from();
    double line1Port = getCompareNo(line1, line1.to());
    double line2Port = getCompareNo(line2, line2.to());

    if (line1.isSameRank()) {
      return comparePointX(line1Port, line2Port) < 0
          == lessRankIdx(line1Tail, line2Tail, useRankIdx);
    }
    return locationTag(line1Tail, line2Tail, useRankIdx) * locationTag(line2Port, line1Port)
        + locationTag(line2Tail, line1Tail, useRankIdx) * locationTag(line1Port, line2Port) == 1;
  }

  private boolean differentRankEdgesCross(DLine line1, DLine line2, boolean useRankIdx) {
    return locationTag(line1.from(), line2.from(), useRankIdx)
        * locationTag(line2.to(), line1.to(), useRankIdx)
        + locationTag(line2.from(), line1.from(), useRankIdx)
        * locationTag(line1.to(), line2.to(), useRankIdx) == 1;
  }

  private boolean flatEdgesCross(DLine line1, DLine line2, boolean line1Flat,
                                 boolean line2Flat, boolean useRankIdx) {
    DLine flat = line1Flat ? line1 : line2;
    DLine other = line1Flat ? line2 : line1;
    if (line1Flat && line2Flat) {
      if (flat.from().getRank() != other.from().getRank()) {
        return false;
      }
      return between(flat, other.from(), useRankIdx)
          != between(flat, other.to(), useRankIdx);
    }

    DNode sameRankEndpoint = endpointAtRank(other, flat.from().getRank());
    return sameRankEndpoint != null && between(flat, sameRankEndpoint, useRankIdx);
  }

  private DNode endpointAtRank(DLine line, int rank) {
    if (line.from().getRank() == rank) {
      return line.from();
    }
    return line.to().getRank() == rank ? line.to() : null;
  }

  private boolean between(DLine line, DNode node, boolean useRankIdx) {
    return lessRankIdx(line.from(), node, useRankIdx)
        != lessRankIdx(line.to(), node, useRankIdx);
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

  private boolean isDot() {
    return drawGraph.getGraphviz().graphAttrs().getLayout() == Layout.DOT;
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
      // Flat-flat pairs belong only to their own rank and include both IN and OUT incidences.
      if (!(currentL1.isSameRank() && l2.isSameRank()) && isCross(currentL1, l2, true)) {
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
