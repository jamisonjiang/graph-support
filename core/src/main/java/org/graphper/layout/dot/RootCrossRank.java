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
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.PortHelper;
import org.graphper.layout.dot.MinCross.ClusterMerge;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

class RootCrossRank implements CrossRank {

  private static final int FLAT_SPAN_MAX_PASSES = 2;
  private static final int FLAT_SPAN_MAX_TRIALS = 32;
  private static final long FLAT_SPAN_MAX_WORK = 250_000;

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
   * Set as soon as one same-rank edge is known. Flat edges are the only reason a comparison has to
   * look at a node's incoming edges on top of its outgoing ones, and that extra sweep is pure
   * overhead - and, through the adjacency iterables, allocation - on the graphs that have none. The
   * flag is never cleared: over-reporting only costs the shortcut, under-reporting would lose
   * crossings.
   */
  private boolean anyFlatEdge;

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
    // The graph arrives fully built here, so the flat flag has to be derived once up front.
    digraphProxy.forEachEdges(this::recordFlatEdge);
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

  void setBasicCrossRank(
      BasicCrossRank basicCrossRank, boolean remainCache, boolean needRefreshNodeIdx) {
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

  void setSameRankAdjacentRecord(SameRankAdjacentRecord sameRankAdjacentRecord) {
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
    return childCrossRank == null ? root.getNodes(rank) : childCrossRank.getNodes(rank);
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
    syncNodeRankIdx(needSyncRankIdx, v, w);
  }

  /**
   * Restores the whole-graph index on the nodes a child arrangement just moved.
   *
   * <p>A child arrangement numbers its own members from zero, whereas every index this class hands
   * out - and every index the crossing predicates compare - counts from the start of the rank. A
   * child that does not begin at the start of its rank therefore leaves the moved nodes carrying a
   * number that cannot be compared against a node outside the child, which silently corrupts the
   * swap delta and, through it, the cached total.
   */
  private void syncNodeRankIdx(boolean needSyncRankIdx, DNode v, DNode w) {
    if (!needSyncRankIdx || childCrossRank == null) {
      return;
    }
    v.setRankIndex(getRankIndex(v));
    w.setRankIndex(getRankIndex(w));
  }

  @Override
  public void sort(Comparator<DNode> comparator, boolean needSyncRankIdx) {
    if (childCrossRank != null) {
      childCrossRank.sort(comparator, needSyncRankIdx);
      syncChildNodeRankIdx(needSyncRankIdx, childCrossRank.minRank(), childCrossRank.maxRank());
    } else {
      root.sort(comparator, needSyncRankIdx);
    }
  }

  @Override
  public void sort(int rank, Comparator<DNode> comparator, boolean needSyncRankIdx) {
    if (childCrossRank != null) {
      childCrossRank.sort(rank, comparator, needSyncRankIdx);
      syncChildNodeRankIdx(needSyncRankIdx, rank, rank);
    } else {
      root.sort(rank, comparator, needSyncRankIdx);
    }
  }

  private void syncChildNodeRankIdx(boolean needSyncRankIdx, int minRank, int maxRank) {
    if (!needSyncRankIdx || childCrossRank == null) {
      return;
    }
    for (int rank = minRank; rank <= maxRank; rank++) {
      int size = childCrossRank.rankSize(rank);
      int start = getChildRankStartIndex(rank);
      for (int i = 0; i < size; i++) {
        childCrossRank.getNode(rank, i).setRankIndex(start + i);
      }
    }
  }

  /**
   * Stamps the whole-graph index of every node onto the node.
   *
   * <p>The crossing predicates read that field rather than looking the index up, which is what
   * keeps a candidate swap cheap. The field can only be trusted if every reorder wrote it back, and
   * a child arrangement reached directly - {@link BasicCrossRank#sort(Comparator, boolean)} on the
   * arrangement this class handed out, say - numbers from its own start instead. One linear pass
   * before a run of comparisons is far cheaper than looking the index up per comparison, and it
   * makes the run independent of who reordered the arrangement last.
   */
  private void refreshNodeRankIdx() {
    for (int rank = minRank(); rank <= maxRank(); rank++) {
      int size = rankSize(rank);
      for (int i = 0; i < size; i++) {
        DNode node = getNode(rank, i);
        if (node != null) {
          node.setRankIndex(i);
        }
      }
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
    recordFlatEdge(line);
  }

  private void recordFlatEdge(DLine line) {
    // Ranks are final before any edge reaches this class, so one test per edge is enough.
    anyFlatEdge |= line != null && line.isSameRank();
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
          CollectionUtils.isEmpty(nodes), "Illegal expand node, root not contain");
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

    Consumer<DNode> positiveAction =
        v -> {
          double v1 = medianValue(v, true);
          v.setMedian(v1);
        };
    Consumer<DNode> reverseAction =
        v -> {
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
   * Assigns each node a stable number, in the arrangement's own order.
   *
   * <p>Identity hash codes would be the obvious key here and are the wrong choice: they differ from
   * one JVM run to the next, which would make the signature - and therefore the point at which a
   * cycle is noticed, and therefore the layout - irreproducible.
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
   * Order-sensitive fingerprint of the whole node arrangement, used to notice that {@link
   * #transpose(boolean)} has returned to an arrangement it already produced.
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
    refreshNodeRankIdx();
    CrossRank crossRank = calcCrossRank();

    /*
     * The loop below assumes delta reflects every reordering, so that it reaches 0 once no crossing
     * can be removed. Curvature swaps break that: they reorder a rank while contributing 0, which
     * lets a swap at one rank restore a crossing at another, whose removal then reports a genuine
     * delta of 1 forever. Two such ranks can take turns undoing each other and the loop never
     * exits - observed at over 94 million iterations on a nine node graph. Well behaved graphs
     * converge in a few dozen
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
       * Stopping on a repeated order is not a heuristic: the loop is deterministic, so revisiting
       * an order means every subsequent order repeats too and no further crossing can be removed.
       * It is therefore the earliest provably correct exit, and it terminates the oscillation
       * instead of letting the iteration bound truncate it after thousands of wasted rounds.
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
    if (!anyFlatEdge || !clusterContiguityFullyGuarded() || crossSnapshot().getCrossNum() == 0) {
      return;
    }
    refreshNodeRankIdx();
    transposeFlatNeighbours();
  }

  /**
   * Whether every move this pass can make is already answered for by {@link #canExchange(DNode,
   * DNode)}.
   *
   * <p>The pass moves a node past a neighbour it is not adjacent to, so unlike plain transposition
   * it can carry a node clear across a container. Two situations make that safe, and they are not
   * the same situation:
   *
   * <ul>
   *   <li>every cluster has been expanded, so a rank holds the members themselves and the guard
   *       compares containers directly;
   *   <li>the graph has no cluster at all, so there is no container to split. Tying the pass to the
   *       expansion flag alone made it unreachable for those graphs, because nothing expands and
   *       nothing sets the flag.
   * </ul>
   */
  private boolean clusterContiguityFullyGuarded() {
    return allClustersExpanded || clusterMerge == null;
  }

  private void transposeFlatNeighbours() {
    CrossRank current = calcCrossRank();
    for (int rank = current.minRank(); rank <= current.maxRank(); rank++) {
      for (int i = 0; i + 2 < current.rankSize(rank); i++) {
        for (boolean pairFirst : new boolean[] {true, false}) {
          DNode a = current.getNode(rank, i);
          DNode b = current.getNode(rank, i + 1);
          DNode c = current.getNode(rank, i + 2);
          DNode first = pairFirst ? a : b;
          DNode second = pairFirst ? b : c;
          boolean flatLeaf = false;
          for (DNode leaf : new DNode[] {first, second}) {
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

  /**
   * Pulls the two ends of a wide flat edge together.
   *
   * <p>The total has a term for every node that sits under a flat edge and carries an edge to the
   * next rank, so a flat edge stretched across a rank can be expensive. Adjacent transposition can
   * only ever move the two nodes at its border, and {@link #transposeFlatNeighbours()} only moves a
   * pair whose ends already touch, so a wide flat edge is a minimum neither of them can leave -
   * measured on {@code hal-flat-order.dot}, where the ends of one flat edge came to rest at
   * opposite ends of a 22 node rank.
   *
   * <p>The walk is one end sliding to the other, step by step, and every step is the same guarded
   * exchange used elsewhere, so flat precedence and cluster contiguity are respected exactly as
   * they are during transpose. Only a strictly lower count for the two ranks a reorder can touch is
   * kept, so this never trades a crossing away for a tidier flat edge.
   */
  void contractFlatSpans() {
    if (!isDot()
        || !anyFlatEdge
        || !clusterContiguityFullyGuarded()
        || crossSnapshot().getCrossNum() == 0) {
      return;
    }
    refreshNodeRankIdx();
    CrossRank current = calcCrossRank();
    int trials = 0;
    long remainingWork = FLAT_SPAN_MAX_WORK;
    for (int rank = current.minRank(); rank <= current.maxRank(); rank++) {
      int size = current.rankSize(rank);
      if (size < 3) {
        continue;
      }

      long trialWork = flatSpanTrialWork(rank);
      if (trialWork > remainingWork) {
        continue;
      }
      List<DLine> spans = wideFlatEdges(current, rank);
      if (CollectionUtils.isEmpty(spans)) {
        continue;
      }

      // Strict descent alone can still take many expensive recounts. Budgets are shared across
      // ranks and charged before a trial, so exhaustion never leaves an unevaluated partial move.
      boolean improved = true;
      for (int pass = 0; pass < FLAT_SPAN_MAX_PASSES && improved; pass++) {
        improved = false;
        for (DLine span : spans) {
          Integer fromIdx = current.safeGetRankIndex(span.from());
          Integer toIdx = current.safeGetRankIndex(span.to());
          if (fromIdx == null || toIdx == null || Math.abs(fromIdx - toIdx) < 2) {
            continue;
          }
          if (trials >= FLAT_SPAN_MAX_TRIALS || trialWork > remainingWork) {
            return;
          }
          trials++;
          remainingWork -= trialWork;
          improved |= contractFlatSpan(current, rank, span);
        }
      }
    }
  }

  /**
   * Conservative work units for three two-layer recounts, two walks and indexed restores. This
   * bounds estimated work, not elapsed time; adjacency/precedence lookups are not uniform.
   */
  private long flatSpanTrialWork(int rank) {
    long work = 0;
    for (int r = Math.max(minRank(), rank - 1); r <= rank; r++) {
      long size = rankSize(r) + 1L;
      if (size > FLAT_SPAN_MAX_WORK) {
        return FLAT_SPAN_MAX_WORK + 1;
      }
      for (int i = 0; i < rankSize(r); i++) {
        size += digraphProxy.outDegree(getNode(r, i));
        if (size > FLAT_SPAN_MAX_WORK || 4 * size * size > FLAT_SPAN_MAX_WORK) {
          return FLAT_SPAN_MAX_WORK + 1;
        }
      }
      work += 4 * size * size;
      if (work > FLAT_SPAN_MAX_WORK) {
        return FLAT_SPAN_MAX_WORK + 1;
      }
    }
    return work;
  }

  private List<DLine> wideFlatEdges(CrossRank current, int rank) {
    List<DLine> spans = null;
    int size = current.rankSize(rank);
    for (int i = 0; i < size; i++) {
      for (DLine line : digraphProxy.outAdjacent(current.getNode(rank, i))) {
        if (!line.isSameRank() || line.from() == line.to()) {
          continue;
        }
        Integer from = current.safeGetRankIndex(line.from());
        Integer to = current.safeGetRankIndex(line.to());
        if (from == null || to == null || Math.abs(from - to) < 2) {
          continue;
        }
        if (spans == null) {
          spans = new ArrayList<>(2);
        }
        spans.add(line);
      }
    }
    return spans;
  }

  private boolean contractFlatSpan(CrossRank current, int rank, DLine span) {
    int before = affectedRanksCrossNum(rank);
    // The order is put back from saved indexes rather than by walking back: the exchange guard is
    // not
    // symmetric, so a step that was allowed one way is not guaranteed to be allowed in reverse, and
    // a walk that stalled halfway back would leave behind an order that was never evaluated.
    Map<DNode, Integer> restore = new HashMap<>();
    List<DNode> nodes = current.getNodes(rank);
    for (int i = 0; i < nodes.size(); i++) {
      restore.put(nodes.get(i), i);
    }
    for (int moved = 0; moved < 2; moved++) {
      DNode walker = moved == 0 ? span.from() : span.to();
      DNode anchor = moved == 0 ? span.to() : span.from();
      int target = current.getRankIndex(anchor);
      target += current.getRankIndex(walker) < target ? -1 : 1;

      if (!slide(current, rank, walker, target)) {
        continue;
      }
      if (affectedRanksCrossNum(rank) < before) {
        setCacheExpired(rank);
        return true;
      }
      sort(rank, Comparator.comparingInt(restore::get), true);
    }
    return false;
  }

  /**
   * Walks {@code node} towards {@code target} one guarded exchange at a time. A step the guard
   * refuses ends the walk where it stands - a shorter walk is still a legal order, and it is judged
   * on the same count as a complete one.
   *
   * @return whether the node moved at all
   */
  private boolean slide(CrossRank current, int rank, DNode node, int target) {
    int idx = current.getRankIndex(node);
    int step = target > idx ? 1 : -1;
    boolean moved = false;
    while (idx != target) {
      DNode neighbour = current.getNode(rank, idx + step);
      DNode left = step > 0 ? node : neighbour;
      DNode right = step > 0 ? neighbour : node;
      if (neighbour == null || !canExchange(left, right)) {
        break;
      }
      exchange(left, right, true);
      idx += step;
      moved = true;
    }
    return moved;
  }

  private int affectedRanksCrossNum(int rank) {
    int num = computeCrossNum(rank, false);
    if (rank > minRank()) {
      num += computeCrossNum(rank - 1, false);
    }
    return num;
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
   * @param i Forward and reverse signs Even forward, odd reverse
   * @param positiveAction Action when visiting a vertex forward
   * @param reverseAction Action when visiting vertices in reverse
   * @param rankIndexAction Action after visiting post action
   */
  private void accessRankNode(
      int i,
      Consumer<DNode> positiveAction,
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

  private void rankNodesHandle(
      Consumer<DNode> positiveAction, IntConsumer rankIndexAction, int rank) {
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

  private int transposeStep(
      int rank, boolean reverse, int[] leftCrossRecord, int[] rightCrossRecord) {

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
          || (leftCrossRecord[2] > 0
              && reverse
              && leftCrossRecord[2] == rightCrossRecord[2]
              && canSacrificeCurvature(v, w))) {
        int delta = leftCrossRecord[2] - rightCrossRecord[2];
        rv += delta;
        exchange(v, w, true);

        /*
         * The total is the sum of the per rank caches, so it may only absorb what the ranks
         * absorbed.
         * Updating it with the full delta regardless drove it below zero on hundreds of ordinary
         * graphs, and MinCross steers on this value.
         */
        int applied = updateRankCache(v.getRank() - 1, rightCrossRecord[0] - leftCrossRecord[0]);
        applied += updateRankCache(v.getRank(), rightCrossRecord[1] - leftCrossRecord[1]);
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
   * <p>An expired rank drops the delta on purpose, because its value gets recomputed from scratch
   * on the next read. The caller has to know that, otherwise the running total would absorb
   * increments that no rank did — and the total is defined as the sum of the ranks (see {@link
   * #crossNum}).
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
    GraphContainer leftDirC =
        DotAttachment.clusterDirectContainer(drawGraph.getGraphviz(), container, left);
    GraphContainer rightDirC =
        DotAttachment.clusterDirectContainer(drawGraph.getGraphviz(), container, right);

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

  /**
   * Scores the two orders of one adjacent pair, so that the difference of {@code result[2]} is the
   * exact change of the total that {@link #computeCrossNum(int, boolean)} would report.
   *
   * <p>That total pairs up the <em>outgoing</em> edges of two nodes that share a rank, so a term of
   * it is identified by the two distinct tails it comes from. Swapping {@code left} with {@code
   * right} flips exactly one order relation, so a term can only move if one of its edges touches
   * {@code left} and the other touches {@code right}. Enumerating those:
   *
   * <ul>
   *   <li>outgoing against outgoing - a term of this rank, both for plain and for flat edges.
   *   <li>outgoing against an <em>incoming flat</em> edge - still a term of this rank, because a
   *       flat edge's tail sits on this very rank. Missing these used to hide real improvements: a
   *       swap that pulls a node out from under a flat edge scored 0.
   *   <li>incoming flat against incoming flat - a term of this rank for the same reason.
   *   <li>incoming plain against incoming plain - a term of the rank above, whose tails live there.
   *   <li>incoming plain against incoming flat - <em>not</em> a term at all: one tail is on this
   *       rank and the other on the rank above, so the total never pairs them. Counting these used
   *       to push the cached total below zero.
   *   <li>outgoing against incoming plain - not a term either, same reason.
   * </ul>
   *
   * @param left the node that ends up first
   * @param right the node that ends up second
   * @param result [0] terms owned by the rank above, [1] terms owned by this rank, [2] their sum
   */
  private void crossing(DNode left, DNode right, int[] result) {
    int h;
    if ((h = left.getRank()) != right.getRank()) {
      throw new IllegalArgumentException(
          "Inconsistent hierarchy of vertices," + left + "," + right);
    }

    int leftSortIndex = left.getRankIndex();
    int rightSortIndex = right.getRankIndex();

    // If left and right are in order, calculate the number of intersections at the current
    // position,
    // otherwise you need to exchange the two vertices to calculate
    boolean needExchange = leftSortIndex > rightSortIndex;

    if (needExchange) {
      left.setRankIndex(rightSortIndex);
      right.setRankIndex(leftSortIndex);
    }

    result[0] = h != minRank() ? crossCalc.interRankInCross(left, right) : 0;
    result[1] = crossCalc.originRankCross(left, right);
    result[2] = result[0] + result[1];

    if (needExchange) {
      left.setRankIndex(leftSortIndex);
      right.setRankIndex(rightSortIndex);
    }
  }

  /**
   * Rank-owned objective: unordered edge pairs with distinct tails on this rank, evaluated in tail
   * order. Flat/outgoing span penalties extend the inter-rank model; flat/incoming pairs have
   * different origin ranks and are excluded from both this total and its swap deltas. This is not
   * invariant under reversing all ranks/edges, nor a count of routed intersections. Graphviz
   * instead keeps flat adjacency separate from rcross's inter-rank adjacency, using it for
   * precedence in flat_reorder/left2right (lib/dotgen/mincross.c and fastgr.c).
   */
  private int computeCrossNum(int rank, boolean refreshRankIdx) {
    int rankSize = rankSize(rank);
    if (refreshRankIdx) {
      // Hoisted out of the pair loops: the count below addresses the arrangement, not this field.
      for (int i = 0; i < rankSize; i++) {
        getNode(rank, i).setRankIndex(i);
      }
    }

    int crossNum = 0;
    for (int i = 0; i < rankSize; i++) {
      crossCalc.openOutgoing(getNode(rank, i));
      for (int j = i + 1; j < rankSize; j++) {
        crossNum += crossCalc.outCross(getNode(rank, j));
      }
    }

    return crossNum;
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

  /**
   * Whether a same-rank edge is a drawn flat edge rather than a collapsed cluster's stand-in.
   *
   * <p>Flatness is rank membership, not {@code minlen}. A {@code minlen} of zero is one way to end
   * up on one rank, but not the only one: a same-rank constraint puts both ends on one rank while
   * leaving the default {@code minlen} of {@code 1} in place (see {@code DefaultVal}), and {@link
   * MinCross} already derives flat precedence from exactly those edges. Reading {@code minlen}
   * therefore ordered them without ever counting them, so no swap could be scored as an
   * improvement.
   *
   * <p>The draw-property guard stays. While a cluster is collapsed its external edges are replaced
   * by edges built with a {@code null} draw property, which stand in for whole ranks of that
   * cluster rather than for anything drawn between two nodes.
   */
  private boolean isRealFlatEdge(DLine line) {
    return line.getLineDrawProp() != null && line.isSameRank();
  }

  private boolean shareEndpoint(DLine line1, DLine line2) {
    return line1.from() == line2.from()
        || line1.from() == line2.to()
        || line1.to() == line2.from()
        || line1.to() == line2.to();
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
            + locationTag(line2Port, line1Port) * locationTag(line1Head, line2Head, useRankIdx)
        == 1;
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
            + locationTag(line2Tail, line1Tail, useRankIdx) * locationTag(line1Port, line2Port)
        == 1;
  }

  private boolean differentRankEdgesCross(DLine line1, DLine line2, boolean useRankIdx) {
    return locationTag(line1.from(), line2.from(), useRankIdx)
                * locationTag(line2.to(), line1.to(), useRankIdx)
            + locationTag(line2.from(), line1.from(), useRankIdx)
                * locationTag(line1.to(), line2.to(), useRankIdx)
        == 1;
  }

  private boolean flatEdgesCross(
      DLine line1, DLine line2, boolean line1Flat, boolean line2Flat, boolean useRankIdx) {
    DLine flat = line1Flat ? line1 : line2;
    DLine other = line1Flat ? line2 : line1;
    if (line1Flat && line2Flat) {
      if (flat.from().getRank() != other.from().getRank()) {
        return false;
      }
      return between(flat, other.from(), useRankIdx) != between(flat, other.to(), useRankIdx);
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
    return lessRankIdx(line.from(), node, useRankIdx) != lessRankIdx(line.to(), node, useRankIdx);
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
    LineDrawProp prop = line.getLineDrawProp();
    // Skip the call in the two cases it answers 0 by definition. Its argument checks build their
    // message before testing it, so on a rank full of virtual nodes - which is what long edge
    // splitting leaves behind - the string was the bulk of a port comparison.
    if (prop == null || node.isVirtual()) {
      return 0;
    }
    return PortHelper.portCompareNo(prop, node, drawGraph);
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
    if (childCrossRank == null
        || rankStartIndex == null
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

    /**
     * Reordering one rank invalidates the terms that rank owns and the terms the rank above owns,
     * because the latter are pairs of edges that end here. Both have to be dropped independently:
     * skipping the rank above whenever this rank happens to have no entry yet would leave a stale
     * value that {@link #isEffective()} still reports as usable.
     */
    void setCacheExpired(int rank) {
      expire(rank);
      expire(rank - 1);
    }

    private void expire(int rank) {
      RankCrossCache rankCrossCache = rankCrossCacheMap.get(rank);
      if (rankCrossCache != null) {
        rankCrossCache.effective = false;
      }
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

    private RankCrossCache() {}

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
   * Counts crossing edge pairs for two nodes that share a rank.
   *
   * <p>The edges of each side are read into a buffer first and paired afterwards. Two things come
   * out of that. The adjacency is read through {@code forEachOutAdjacent}/{@code forEachInAdjacent}
   * rather than the iterable form, which allocates - an incoming iterable is a fresh lambda over a
   * fresh reverse iterator over a fresh iterator. And the reading uses a single consumer, so the
   * quadratic part of the work is a plain loop over an array with one crossing test in it, instead
   * of a virtual call into one of several consumers; a call site that sees several implementations
   * stops being inlined, which in turn costs allocation inside the crossing test itself. The
   * buffers live as long as this object, so a steady state run allocates nothing at all.
   */
  private class CrossCalc {

    /** Take every edge. */
    private static final int ALL = 0;
    /** Take flat edges reached through the incoming adjacency of the node they end at. */
    private static final int INCOMING_FLAT = 1;
    /** Take only edges that are not flat. */
    private static final int NON_FLAT = 2;

    private DLine[] first = new DLine[8];
    private int firstSize;
    private DLine[] second = new DLine[8];
    private int secondSize;

    private boolean intoFirst;
    private int filter;

    /**
     * Set when a buffered edge does not start at the node it was read from, which is only possible
     * for a flat edge read through an incoming adjacency. While it is clear, the tails are the two
     * nodes being compared, in that order, and the pairing loop needs neither a tail comparison nor
     * a shared tail test.
     */
    private boolean foreignTail;

    private final Consumer<DLine> collector = this::collect;

    /**
     * Loads the outgoing edges of the node that comes first. Split out from the counting so that a
     * whole-rank recount reads each node's adjacency once rather than once per partner.
     */
    void openOutgoing(DNode node) {
      firstSize = 0;
      foreignTail = false;
      read(node, true, ALL, false);
    }

    /**
     * One term of the global total: the crossings between the outgoing edges loaded by {@link
     * #openOutgoing(DNode)} and the outgoing edges of {@code node}, which has to come later in the
     * rank.
     */
    int outCross(DNode node) {
      secondSize = 0;
      read(node, false, ALL, false);
      return countByArrangement();
    }

    /**
     * The terms this rank owns for the pair {@code (left, right)}. Alongside the outgoing edges
     * that {@link #outCross(DNode)} pairs up, a node's incoming flat edges take part: a flat edge
     * has its tail on this rank, so pairing it with the other node's outgoing edge is a term of
     * this rank, and swapping the two nodes can switch that term on or off.
     */
    int originRankCross(DNode left, DNode right) {
      firstSize = 0;
      secondSize = 0;
      foreignTail = false;
      read(left, true, ALL, false);
      read(right, false, ALL, false);
      if (anyFlatEdge) {
        read(left, true, INCOMING_FLAT, true);
        read(right, false, INCOMING_FLAT, true);
      }
      return countByNodeIdx();
    }

    /**
     * The terms the rank above owns for the pair {@code (left, right)}: incoming edges on both
     * sides, and only the ones that really descend from up there. A flat edge's tail is on
     * <em>this</em> rank, so pairing it with an edge from the rank above would score a pair that
     * the global total, which only ever pairs edges whose tails share a rank, does not have.
     */
    int interRankInCross(DNode left, DNode right) {
      firstSize = 0;
      secondSize = 0;
      // Both tails are on the rank above in no particular order, so they have to be compared.
      foreignTail = true;
      read(left, true, NON_FLAT, true);
      read(right, false, NON_FLAT, true);
      return countByNodeIdx();
    }

    private void read(DNode node, boolean into, int lineFilter, boolean incoming) {
      this.intoFirst = into;
      this.filter = lineFilter;
      if (incoming) {
        digraphProxy.forEachInAdjacent(node, collector);
      } else {
        digraphProxy.forEachOutAdjacent(node, collector);
      }
    }

    private void collect(DLine line) {
      if (filter == INCOMING_FLAT) {
        // Self loops show up here too, and belong to the outgoing adjacency that already has them.
        if (!line.isSameRank() || line.from() == line.to()) {
          return;
        }
        foreignTail = true;
      } else if (filter == NON_FLAT && line.isSameRank()) {
        return;
      }

      if (intoFirst) {
        if (firstSize == first.length) {
          first = Arrays.copyOf(first, firstSize * 2);
        }
        first[firstSize++] = line;
      } else {
        if (secondSize == second.length) {
          second = Arrays.copyOf(second, secondSize * 2);
        }
        second[secondSize++] = line;
      }
    }

    /**
     * Pairs everything read for the first node with everything read for the second, the way the
     * global total does: distinct tails only - a shared tail is never a term - and the tail that
     * comes first supplies the first argument, which decides the port comparison of flat edges.
     *
     * <p>The counterpart {@link #countByArrangement()} is a separate method on purpose. The
     * crossing test branches on where to take an index from, and it is only cheap while that choice
     * is a constant at the call site; handing it in as a flag keeps both halves of every such
     * branch reachable, which grew the test past the point where it still gets inlined and turned
     * the port comparison inside it into a per-comparison allocation.
     */
    private int countByNodeIdx() {
      int num = 0;
      for (int i = 0; i < firstSize; i++) {
        DLine a = first[i];
        for (int j = 0; j < secondSize; j++) {
          DLine b = second[j];
          if (!foreignTail) {
            if (isCross(a, b, true)) {
              num++;
            }
            continue;
          }

          if (a.from() == b.from()) {
            continue;
          }
          boolean ordered = a.from().getRankIndex() < b.from().getRankIndex();
          if (isCross(ordered ? a : b, ordered ? b : a, true)) {
            num++;
          }
        }
      }
      return num;
    }

    /**
     * The same pairing for the global total, which reads the arrangement rather than the node
     * fields. Every buffered edge starts at the node it was read from here, so the tails are the
     * two nodes being compared and their order is already known.
     */
    private int countByArrangement() {
      int num = 0;
      for (int i = 0; i < firstSize; i++) {
        DLine a = first[i];
        for (int j = 0; j < secondSize; j++) {
          if (isCross(a, second[j], false)) {
            num++;
          }
        }
      }
      return num;
    }
  }
}
