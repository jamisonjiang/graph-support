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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.api.attributes.ClusterShapeEnum;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.PortHelper;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.layout.dot.RootCrossRank.CrossSnapshot;
import org.graphper.layout.dot.RootCrossRank.ExpandInfoProvider;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.EnvProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimize intersections between edges by the median method.
 *
 * @author Jamison Jiang
 */
class MinCross {

  private static final Logger log = LoggerFactory.getLogger(MinCross.class);

  private static final double CONVERGENCE = 0.995D;

  private RootCrossRank rootCrossRank;

  private ClusterExpand clusterExpand;

  private final RankContent rankContent;

  private final DotAttachment dotAttachment;

  private final boolean useQuickMode;

  private MinCrossDedigraph digraphProxy;

  MinCross(RankContent rankContent, DotAttachment dotAttachment, boolean useQuickMode) {
    this.rankContent = rankContent;
    this.dotAttachment = dotAttachment;
    this.useQuickMode = useQuickMode;
    // Cut the line which span over than 2
    reduceLongEdges();

    // Init RootCrossRank
    initRootCrossRank();

    // Dot mincross
    dotMincross();

    // Sync node order
    syncRankOrder();

    // Try bounded DFS orders of independent non-branching paths.
    new NonBranchingPathSort(rankContent, dotAttachment.getDrawGraph(), digraphProxy).sort();

    this.rootCrossRank = null;
    this.clusterExpand = null;
  }

  public EdgeDedigraph<DNode, DLine> getDigraphProxy() {
    return digraphProxy;
  }

  /*
   * If an edge spans more than two levels, we call this edge a "long edge". The intersection
   * calculation of the "long side" is very complicated, and the "long side" needs to be removed
   * by adding virtual nodes.
   */
  private void reduceLongEdges() {
    DotDigraph digraph = dotAttachment.getDotDigraph();
    this.digraphProxy = new MinCrossDedigraph(digraph.vertexNum());
    Map<DNode, Map<DNode, DLine>> parallelEdgesRecord = new HashMap<>(1);

    int d = 0;
    RankNode pre = null;
    RankNode current = rankContent.get(rankContent.minRank());
    Map<Integer, RankNode> rm = new HashMap<>(rankContent.size());

    while (current != null) {
      // Skip empty rank.
      if (current.isEmpty()) {
        d++;
        if (pre != null) {
          pre.setRankSep(pre.getRankSep() + current.getRankSep());
        }
        current = current.next;
        continue;
      }

      if (d > 0) {
        if (pre != null) {
          pre.next = current;
        }
        current.pre = pre;
        current.setRankIndex(current.rankIndex() - d);
      }

      for (int j = 0; j < current.size(); j++) {
        DNode from = current.get(j);
        int fromRank = getOldRank(from);
        digraphProxy.add(from);

        // Short line parallel side recording.
        Map<DNode, DLine> lineMap = parallelEdgesRecord
            .computeIfAbsent(from, k -> new HashMap<>(dotAttachment.getDotDigraph().degree(from)));

        for (DLine e : digraph.adjacent(from)) {
          DNode to = e.to();
          DLine edge = lineMap.get(to);

          if (edge != null) {
            // Add short parallel edges, all parallel edges without intermediate nodes need to be processed separately.
            edge.addParallelEdge(e);
            continue;
          }

          // The short side is added directly to the picture.
          if (getOldRank(to) - fromRank <= 1 && from != to) {
            digraphProxy.addEdge(e, dotAttachment.getDrawGraph());

            if (getOldRank(to) - fromRank == 1) {
              lineMap.put(to, e);
            }
            continue;
          }

          // Long edge needs to be broken.
          cutLongEdge(e, lineMap, current);
        }

        parallelEdgesRecord.remove(from);
        from.setRank(current.rankIndex());
      }

      rm.put(current.rankIndex(), current);
      pre = current;
      current = current.next;
    }

    rankContent.rankNodeMap = rm;
    int rank = rankContent.maxRank;
    rankContent.maxRank -= d;
    while (rank > rankContent.maxRank) {
      RankNode rankNode = rankContent.get(rank);
      if (rankNode == null) {
        break;
      }

      rankContent.remove(rank);
      rank--;
    }
  }

  private int getOldRank(DNode node) {
    if (node.isLabelNode() || node.getRank() > rankContent.size()) {
      return node.getRank();
    }

    return rankContent.get(node.getRank()).rankIndex();
  }

  private void cutLongEdge(DLine edge, Map<DNode, DLine> lineMap, RankNode rankNode) {
    DNode from = edge.from();
    DNode to;
    int end = getOldRank(edge.to());
    Graphviz graphviz = dotAttachment.getGraphviz();
    GraphContainer commonParent = DotAttachment.commonParent(graphviz, from, edge.to());

    List<DNode> virtualNodes = null;
    if (edge.haveLabel() && edge.getLabelSize() != null) {
      virtualNodes = new ArrayList<>(Math.abs(end - from.getRank()));
    }

    rankNode = rankNode.next;
    while (rankNode != null && rankNode.rankIndex() <= end) {
      if (rankNode.rankIndex() == end) {
        to = edge.to();
      } else {
        if (rankNode.isEmpty()) {
          rankNode = rankNode.next;
          continue;
        }

        to = DNode.newRoutingVirtualNode(20, commonParent);
        to.setRank(rankNode.rankIndex());
        rankNode.add(to);

        if (virtualNodes != null) {
          virtualNodes.add(to);
        }
      }

      if (from == edge.from() && to == edge.to()) {
        digraphProxy.addEdge(edge, dotAttachment.getDrawGraph());
        lineMap.put(to, edge);
      } else {
        digraphProxy.addEdge(
            new DLine(from, to, edge.getLineDrawProp(), edge.weight(), edge.limit()),
            dotAttachment.getDrawGraph()
        );
      }

      from = to;
      rankNode = rankNode.next;
    }

    if (CollectionUtils.isNotEmpty(virtualNodes)) {
      FlatPoint labelSize = edge.getLabelSize();
      DNode labelNode = virtualNodes.get(virtualNodes.size() / 2);
      labelNode.setLabelLine(edge.getLine());
      labelNode.setWidth((int) labelSize.getWidth());
      labelNode.setHeight((int) labelSize.getHeight());
    }
  }

  private void dotMincross() {
    long start = System.currentTimeMillis();
    if (clusterExpand != null) {
      clusterExpand.cluster = dotAttachment.getGraphviz();
    }
    mincross(0, 2);

    for (Cluster cluster : dotAttachment.clusters(dotAttachment.getGraphviz())) {
      mincrossCluster(cluster);
    }

    /*
     * Every cluster is expanded by now, so the crossing count finally describes the real edges
     * instead of merge nodes standing in for whole ranks of a cluster. Only this pass can see - and
     * therefore remove - crossings that were invisible while any cluster was collapsed.
     */
    if (clusterExpand != null) {
      rootCrossRank.resetToRoot();
      rootCrossRank.setSameRankAdjacentRecord(sameRankAdjacentRecord());
      finalMincross(1, 2);
    }
    if (log.isDebugEnabled()) {
      log.debug("Mincross finished, using {}ms", System.currentTimeMillis() - start);
    }
  }

  private void mincrossCluster(Cluster cluster) {
    SameRankAdjacentRecord sameRankAdjacentRecord = rootCrossRank.getSameRankAdjacentRecord();

    if (sameRankAdjacentRecord != null) {
      sameRankAdjacentRecord.clearMarkIn();
    }
    BasicCrossRank crossRank = clusterExpand.init(cluster);
    rootCrossRank.expand(clusterExpand);
    expandLine(crossRank);
    clusterExpand.clusterMerge.clearCluster(cluster);
    mincross(1, 2);

    for (Cluster c : dotAttachment.clusters(cluster)) {
      mincrossCluster(c);
    }

    rootCrossRank.syncChildOrder();
    rootCrossRank.setSameRankAdjacentRecord(null);
  }

  private void syncRankOrder() {
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode from = rootCrossRank.getNode(i, j);
        rankNode.set(j, from);
        from.setRankIndex(j);

        for (DLine dLine : digraphProxy.outAdjacent(from)) {
          DNode to = dLine.other(from);

          if (to.getRank() == from.getRank()) {
            if (dotAttachment.getSameRankAdjacentRecord() == null) {
              dotAttachment.setSameRankAdjacentRecord(new SameRankAdjacentRecord());
            }

            dotAttachment.getSameRankAdjacentRecord().addOutAdjacent(from, dLine);
          }
        }
      }
    }
  }

  private SameRankAdjacentRecord sameRankAdjacentRecord() {
    SameRankAdjacentRecord record = null;
    for (DLine line : digraphProxy.edges()) {
      DNode from = line.from();
      if (from.getRank() != line.to().getRank()) {
        continue;
      }
      if (record == null) {
        record = new SameRankAdjacentRecord();
      }
      record.addOutAdjacent(from, line);
    }
    return record;
  }

  private void initRootCrossRank() {
    if (!dotAttachment.haveClusters()) {
      rootCrossRank = new RootCrossRank(dotAttachment.getDrawGraph(), digraphProxy);
      return;
    }

    checkClustersContinuity();
    Graphviz graphviz = dotAttachment.getGraphviz();
    ClusterMerge clusterMerge = new ClusterMerge();
    clusterExpand = new ClusterExpand(clusterMerge);
    rootCrossRank = new RootCrossRank(dotAttachment.getDrawGraph(), clusterMerge);

    Map<DNode, Set<DNode>> nodeHaveLine = new HashMap<>();
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode from = rankNode.get(j);
        DNode fromClusterNode = clusterProxyNode(from, graphviz);

        for (DLine line : digraphProxy.outAdjacent(from)) {
          DNode to = line.to();
          DNode toCLusterNode = clusterProxyNode(line.to(), graphviz);

          addLine(from, fromClusterNode, nodeHaveLine, line, to, toCLusterNode);
        }

        if (from == fromClusterNode) {
          rootCrossRank.addNode(fromClusterNode);
        }
      }
    }
  }

  private void checkClustersContinuity() {
    /*
     * We hope every rank in cluster should have one node as least to avoid cluster overlap,
     * otherwise position process lost reference point for cluster space between two clusters
     */
    Map<Cluster, TreeSet<Integer>> clusterRankContinuity = new HashMap<>();
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        updateClusterContinuity(rankNode.get(j), clusterRankContinuity);
      }
    }

    for (Entry<Cluster, TreeSet<Integer>> entry : clusterRankContinuity.entrySet()) {
      Cluster cluster = entry.getKey();
      TreeSet<Integer> ranks = entry.getValue();

      Integer pre = null;
      for (Integer rank : ranks) {
        if (pre != null && !Objects.equals(pre, rank - 1)) {
          // Find empty rank in cluster and insert one node
          for (int i = pre + 1; i < rank; i++) {
            RankNode rankNode = rankContent.get(i);
            DNode node = new DNode(null, 0 , 0,0);
            node.setContainer(cluster);
            node.setRank(i);
            rankNode.add(node);
            node.setRankIndex(rankNode.size() - 1);
            digraphProxy.add(node);
          }
        }
        pre = rank;
      }
    }
  }

  private void updateClusterContinuity(DNode n, Map<Cluster, TreeSet<Integer>> clusterRankContinuity) {
    if (!n.getContainer().isCluster()) {
      return;
    }

    Graphviz graphviz = dotAttachment.getGraphviz();
    GraphContainer container = n.getContainer();

    while (container != null && container.isCluster()) {
      Cluster cluster = (Cluster) container;
      clusterRankContinuity.computeIfAbsent(cluster, c -> new TreeSet<>()).add(n.getRank());
      container = graphviz.effectiveFather(container);
    }
  }

  private void expandLine(BasicCrossRank clusterCrossRank) {
    Map<DNode, Set<DNode>> nodeHaveLine = new HashMap<>();
    Set<DLine> linesRecord = new HashSet<>();
    GraphContainer container = clusterCrossRank.container();

    for (int i = clusterCrossRank.minRank(); i <= clusterCrossRank.maxRank(); i++) {
      int s = clusterCrossRank.rankSize(i);
      for (int j = 0; j < s; j++) {
        DNode from = clusterCrossRank.getNode(i, j);
        DNode fromClusterNode = clusterProxyNode(from, container);

        for (DLine line : digraphProxy.outAdjacent(from)) {
          linesRecord.add(line);
          DNode to = line.to();
          DNode toCLusterNode = clusterProxyNode(line.to(), container);

          addLine(from, fromClusterNode, nodeHaveLine, line, to, toCLusterNode);
        }
      }
    }

    for (int i = clusterCrossRank.maxRank(); i >= clusterCrossRank.minRank(); i--) {
      int s = clusterCrossRank.rankSize(i);
      for (int j = 0; j < s; j++) {
        DNode to = clusterCrossRank.getNode(i, j);
        DNode toClusterNode = clusterProxyNode(to, clusterCrossRank.container);

        for (DLine line : digraphProxy.inAdjacent(to)) {
          if (linesRecord.contains(line)) {
            continue;
          }

          DNode from = line.from();
          DNode fromClusterNode = clusterProxyNode(from, clusterCrossRank.container);

          addLine(from, fromClusterNode, nodeHaveLine, line, to, toClusterNode);
        }
      }
    }
  }

  private void addLine(DNode from, DNode fromClusterNode, Map<DNode, Set<DNode>> nodeHaveLine,
                       DLine line, DNode to, DNode toCLusterNode) {
    Set<DNode> nodes = nodeHaveLine.computeIfAbsent(fromClusterNode, f -> new HashSet<>(2));
    if (from != fromClusterNode || to != toCLusterNode) {
      if (!nodes.contains(toCLusterNode) && fromClusterNode != toCLusterNode) {
        nodes.add(toCLusterNode);
        line = new DLine(fromClusterNode, toCLusterNode, null, line.weight(), line.limit());
        rootCrossRank.addEdge(line);
      }
    } else {
      nodes.add(toCLusterNode);
      rootCrossRank.addEdge(line);
    }
  }

  private void updateClusterRange(DNode n) {
    if (!n.getContainer().isCluster()) {
      return;
    }

    int rank = n.getRankIgnoreModel();
    Graphviz graphviz = dotAttachment.getGraphviz();
    GraphContainer container = n.getContainer();

    while (container != null && container.isCluster()) {
      Cluster cluster = (Cluster) container;
      ClusterRankRange range = clusterExpand.clusterMerge.clusterRankRange
          .computeIfAbsent(cluster, c -> new ClusterRankRange());

      range.minRank = Math.min(range.minRank, rank);
      range.maxRank = Math.max(range.maxRank, rank);

      container = graphviz.effectiveFather(container);
    }
  }

  private DNode clusterProxyNode(DNode node, GraphContainer graphContainer) {
    updateClusterRange(node);

    Graphviz graphviz = dotAttachment.getGraphviz();
    if (node.getContainer() == graphContainer) {
      return node;
    }

    GraphContainer father;
    GraphContainer current = node.getContainer();
    while ((father = graphviz.effectiveFather(current)) != graphContainer && father != null) {
      current = father;
    }

    if (father == null) {
      DNode n = clusterExpand.clusterMerge.getMergeNode(node);
      return n != null ? n : node;
    }

    return clusterExpand.clusterMerge.getMergeNodeOrPut((Cluster) current, node);
  }

  private void mincross(int startPass, int endPass) {
    int maxIter = 24;
    int minQuit = dotAttachment.getDrawGraph().getGraphviz().graphAttrs().getMclimit();
    CrossSnapshot optimal = rootCrossRank.crossSnapshot();

    /*
     * Try both rank directions and both within-rank directions. Every candidate starts from the
     * same order, and collapsed child clusters are placed as complete rank blocks before any of
     * their external edges are followed.
     */
    BasicCrossRank initial = optimal.getCrossRank().clone();

    // The legacy two-direction initial sort, kept behaviourally identical to the original
    // implementation, including the SameRankAdjacentRecord side effect performed by InitSort.
    BasicCrossRank c = initial.clone();
    new InitSort(c, c.container(), dotAttachment.getDrawGraph(), true, true, false);
    CrossSnapshot cn = rootCrossRank.tryCacheCrossNum(c);
    if (c.container().haveChildCluster() || cn.getCrossNum() <= optimal.getCrossNum()) {
      optimal = cn;
      rootCrossRank.updateCross(cn);
    }

    c = optimal.getCrossRank().clone();
    new InitSort(c, c.container(), dotAttachment.getDrawGraph(), false, true, false);
    cn = rootCrossRank.tryCacheCrossNum(c);
    if (cn.getCrossNum() < optimal.getCrossNum()) {
      optimal = cn;
      rootCrossRank.updateCross(cn);
    }

    /*
     * Only when the legacy order still has crossings is it worth exploring the atomic cluster
     * block candidates, which place every rank of a collapsed child cluster before following any
     * of its external edges. A zero-cross legacy order is already optimal, and replacing it with
     * a tied atomic order would only change an established topology for no measurable gain.
     */
    if (optimal.getCrossNum() > 0 || crossClusterFlatViolations(optimal.getCrossRank()) > 0) {
      optimal = tryAtomicInitSort(initial, optimal);
    }

    if (useQuickMode) {
      logQuickModeStep(0);
      flatOrder(optimal.getCrossRank());
      logQuickModeStep(1);
      mincrossStep(0);
      logQuickModeStep(2);
      mincrossStep(1);
      logQuickModeStep(3);
    } else {
      runDotMincrossProcess(startPass, endPass, maxIter, minQuit, optimal);
    }

    rootCrossRank.transpose(false);
    rootCrossRank.syncChildOrder();
  }

  /**
   * Runs crossing reduction once every cluster is expanded. The existing order already preserves
   * the cluster blocks established by the preceding passes, so this pass deliberately excludes
   * both InitSort and flatOrder and only applies median sorting and adjacent transposition.
   */
  private void finalMincross(int startPass, int endPass) {
    int maxIter = 24;
    int minQuit = dotAttachment.getDrawGraph().getGraphviz().graphAttrs().getMclimit();
    CrossSnapshot optimal = rootCrossRank.crossSnapshot();

    for (int pass = startPass; pass <= endPass; pass++) {
      int trying = 0;
      for (int i = 0; i < maxIter; i++) {
        if (log.isDebugEnabled()) {
          log.debug("final pass {} iter {} trying {} best_cross {}", pass, i, trying,
                    optimal.getCrossNum());
        }

        if (trying++ >= minQuit || optimal.getCrossNum() == 0) {
          break;
        }

        int preOptimalCrossNum = optimal.getCrossNum();
        rootCrossRank.vmedian(i);
        rootCrossRank.transpose(i % 4 >= 2);
        optimal = rootCrossRank.crossSnapshot();

        if (preOptimalCrossNum * CONVERGENCE <= optimal.getCrossNum()) {
          trying++;
        }
      }

      if (optimal.getCrossNum() == 0) {
        break;
      }
    }

    rootCrossRank.transpose(false);
    rootCrossRank.syncChildOrder();
    checkClusterOrder();
  }

  private void checkClusterOrder() {
    if (!EnvProp.qualityCheck()) {
      return;
    }
    List<String> violations = ClusterOrderConsistency
        .violations(rootCrossRank.getBasicCrossRank(), dotAttachment);
    violations.addAll(ClusterOrderConsistency
                          .contiguityViolations(rootCrossRank.getBasicCrossRank(), dotAttachment));
    Asserts.illegalArgument(!violations.isEmpty(), "Cluster order changed: " + violations);
  }

  /**
   * Explores the atomic cluster block initial orders in all four access directions and adopts the
   * best one only if it beats {@code legacyOptimal}. Because {@link InitSort} installs its own
   * {@link SameRankAdjacentRecord} into {@link #rootCrossRank} as a side effect, the record that
   * belongs to the legacy order is restored whenever no atomic candidate wins, and the winning
   * candidate always keeps its own record.
   */
  private CrossSnapshot tryAtomicInitSort(BasicCrossRank initial, CrossSnapshot legacyOptimal) {
    SameRankAdjacentRecord legacyRecord = rootCrossRank.getSameRankAdjacentRecord();
    CrossSnapshot best = null;
    SameRankAdjacentRecord bestRecord = null;
    int legacyViolations = crossClusterFlatViolations(legacyOptimal.getCrossRank());
    int bestViolations = Integer.MAX_VALUE;

    for (boolean rankForward : new boolean[]{true, false}) {
      for (boolean orderForward : new boolean[]{true, false}) {
        BasicCrossRank candidate = initial.clone();
        InitSort initSort = new InitSort(candidate, candidate.container(),
                                         dotAttachment.getDrawGraph(), rankForward,
                                         orderForward, true);
        CrossSnapshot snapshot = rootCrossRank.tryCacheCrossNum(candidate);
        int violations = crossClusterFlatViolations(candidate);
        if (best == null || violations < bestViolations
            || violations == bestViolations && snapshot.getCrossNum() < best.getCrossNum()) {
          best = snapshot;
          bestViolations = violations;
          bestRecord = initSort.sameRankAdjacentRecord != null
              ? initSort.sameRankAdjacentRecord : legacyRecord;
        }
      }
    }

    if (best == null || bestViolations > legacyViolations
        || bestViolations == legacyViolations
        && best.getCrossNum() >= legacyOptimal.getCrossNum()) {
      rootCrossRank.setSameRankAdjacentRecord(legacyRecord);
      rootCrossRank.updateCross(legacyOptimal);
      return legacyOptimal;
    }

    rootCrossRank.setSameRankAdjacentRecord(bestRecord);
    rootCrossRank.updateCross(best);
    return best;
  }

  private int crossClusterFlatViolations(CrossRank crossRank) {
    int violations = 0;
    for (DLine line : rootCrossRank.getDigraphProxy().edges()) {
      DNode from = line.from();
      DNode to = line.to();
      if (from.getRank() != to.getRank()) {
        continue;
      }
      GraphContainer fromContainer = dotAttachment
          .clusterDirectContainer(crossRank.container(), from);
      GraphContainer toContainer = dotAttachment
          .clusterDirectContainer(crossRank.container(), to);
      if (fromContainer == toContainer) {
        continue;
      }
      Integer fromIndex = crossRank.safeGetRankIndex(from);
      Integer toIndex = crossRank.safeGetRankIndex(to);
      if (fromIndex != null && toIndex != null && fromIndex >= toIndex) {
        violations++;
      }
    }
    return violations;
  }

  private void logQuickModeStep(int time) {
    if (log.isDebugEnabled()) {
      CrossSnapshot crossSnapshot = rootCrossRank.crossSnapshot();
      log.debug("trying {} best_cross {}", time, crossSnapshot.getCrossNum());
    }
  }

  private CrossSnapshot runDotMincrossProcess(int startPass, int endPass, int maxIter, int minQuit,
                                             CrossSnapshot optimal) {
    for (int pass = startPass; pass <= endPass; pass++) {
      if (pass <= 1) {
        flatOrder(optimal.getCrossRank());
        optimal = rootCrossRank.crossSnapshot();
      }

      int trying = 0;
      for (int i = 0; i < maxIter; i++) {
        if (log.isDebugEnabled()) {
          log.debug("pass {} iter {} trying {} best_cross {}", pass, i, trying, optimal.getCrossNum());
        }

        if (trying++ >= minQuit || optimal.getCrossNum() == 0) {
          break;
        }

        int preOptimalCrossNum = optimal.getCrossNum();
        mincrossStep(i);
        optimal = rootCrossRank.crossSnapshot();

        if (preOptimalCrossNum * CONVERGENCE <= optimal.getCrossNum()) {
            trying++;
        }
      }

      if (optimal.getCrossNum() == 0) {
        break;
      }
    }
    return optimal;
  }

  private void mincrossStep(int iterNum) {
    rootCrossRank.vmedian(iterNum);
    rootCrossRank.transpose(iterNum % 4 >= 2);
  }

  private void flatOrder(CrossRank crossRank) {
    SameRankAdjacentRecord sameRankAdjacentRecord = rootCrossRank.getSameRankAdjacentRecord();
    if (sameRankAdjacentRecord == null) {
      return;
    }

    initialFlatOrder(crossRank, sameRankAdjacentRecord);
    repairFlatOrder(crossRank, sameRankAdjacentRecord);
  }

  private void initialFlatOrder(CrossRank crossRank,
                                SameRankAdjacentRecord sameRankAdjacentRecord) {
    int[] no = {0};
    int connectNo = 0;
    Set<DNode> mark = new HashSet<>();
    Map<DNode, Map.Entry<Integer, Integer>> postOrderRecord = new HashMap<>();
    for (int rank = crossRank.minRank(); rank <= crossRank.maxRank(); rank++) {
      for (int index = 0; index < crossRank.rankSize(rank); index++) {
        DNode node = crossRank.getNode(rank, index);
        if (mark.contains(node)) {
          continue;
        }
        rootCrossRank.setCacheExpired(rank);
        postOrder(connectNo++, no, node, mark, postOrderRecord);
      }
    }
    crossRank.sort((left, right) -> {
      Integer leftConnect = postOrderRecord.get(left).getKey();
      Integer rightConnect = postOrderRecord.get(right).getKey();
      if (!Objects.equals(leftConnect, rightConnect)) {
        return leftConnect.compareTo(rightConnect);
      }
      return postOrderRecord.get(right).getValue().compareTo(postOrderRecord.get(left).getValue());
    }, true);
  }

  private void repairFlatOrder(CrossRank crossRank,
                               SameRankAdjacentRecord sameRankAdjacentRecord) {
    for (int rank = crossRank.minRank(); rank <= crossRank.maxRank(); rank++) {
      List<DNode> original = new ArrayList<>(crossRank.getNodes(rank));
      Set<DNode> rankNodes = new HashSet<>(original);
      Map<DNode, Integer> indegree = new HashMap<>();
      for (DNode node : original) {
        indegree.put(node, 0);
      }
      for (DNode node : original) {
        for (DNode adjacent : sameRankAdjacentRecord.outAdjacent(node)) {
          if (node.getContainer() == adjacent.getContainer() && rankNodes.contains(adjacent)) {
            indegree.put(adjacent, indegree.get(adjacent) + 1);
          }
        }
      }

      Set<DNode> remaining = new LinkedHashSet<>(original);
      List<DNode> ordered = new ArrayList<>(original.size());
      while (!remaining.isEmpty()) {
        DNode next = null;
        for (DNode node : remaining) {
          if (indegree.get(node) == 0) {
            next = node;
            break;
          }
        }
        // A directed cycle cannot satisfy every flat constraint. Preserve the current order when
        // choosing where to break it, then continue honoring every constraint that remains acyclic.
        if (next == null) {
          next = remaining.iterator().next();
        }
        remaining.remove(next);
        ordered.add(next);
        for (DNode adjacent : sameRankAdjacentRecord.outAdjacent(next)) {
          if (next.getContainer() == adjacent.getContainer() && remaining.contains(adjacent)) {
            indegree.put(adjacent, indegree.get(adjacent) - 1);
          }
        }
      }

      Map<DNode, Integer> order = new HashMap<>();
      boolean changed = false;
      for (int i = 0; i < ordered.size(); i++) {
        DNode node = ordered.get(i);
        order.put(node, i);
        changed |= node != original.get(i);
      }
      if (changed) {
        rootCrossRank.setCacheExpired(rank);
        crossRank.sort(rank, Comparator.comparingInt(order::get), true);
      }
    }
  }

  private int postOrder(int connectNo, int[] no, DNode node, Set<DNode> mark,
                        Map<DNode, Map.Entry<Integer, Integer>> orderRecord) {
    mark.add(node);
    Set<DNode> adjacent = rootCrossRank.getSameRankAdjacentRecord().outAdjacent(node);
    if (CollectionUtils.isNotEmpty(adjacent)) {
      for (DNode dNode : adjacent) {
        if (node.getContainer() != dNode.getContainer()) {
          continue;
        }
        if (mark.contains(dNode)) {
          Entry<Integer, Integer> accessOrder = orderRecord.get(dNode);
          if (accessOrder != null) {
            connectNo = accessOrder.getKey() != null ? accessOrder.getKey() : connectNo;
          }
          continue;
        }
        connectNo = postOrder(connectNo, no, dNode, mark, orderRecord);
      }
    }
    orderRecord.put(node, new AbstractMap.SimpleEntry<>(connectNo, no[0]++));
    return connectNo;
  }

  private class ClusterExpand implements ExpandInfoProvider {

    private GraphContainer cluster;

    private final ClusterMerge clusterMerge;

    private Map<DNode, Set<DNode>> mergeNodes;

    public ClusterExpand(ClusterMerge clusterMerge) {
      this.clusterMerge = clusterMerge;
    }

    BasicCrossRank init(Cluster cluster) {
      this.cluster = cluster;

      if (mergeNodes == null) {
        mergeNodes = new HashMap<>();
      } else {
        mergeNodes.clear();
      }

      ClusterInnerSize clusterInnerSize = null;
      if (cluster.clusterAttrs().getShape() != ClusterShapeEnum.RECT) {
        clusterInnerSize = new ClusterInnerSize();
      }

      BasicCrossRank crossRank = new BasicCrossRank(cluster);
      Iterator<Entry<DNode, DNode>> iterator = clusterMerge.mergeNodeMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<DNode, DNode> entry = iterator.next();
        DNode node = entry.getKey();
        DNode mergeNode = entry.getValue();
        if (clusterInnerSize != null) {
          clusterInnerSize.refresh(node);
        }

        GraphContainer commonParent = dotAttachment.commonParent(node, mergeNode);
        if (dotAttachment.notContains(cluster, commonParent)) {
          continue;
        }

        // Merge node occupied cluster proxy node first
        clusterProxyNode(node, cluster);
        crossRank.addNode(node);

        // If the two vertices are consistent, expand the vertex; and if it is directly under the
        // Cluster, remove the record, otherwise keep the merged record and leave it to the sub-Cluster to expand.
        if (node == mergeNode) {
          mergeNodes.computeIfAbsent(mergeNode, n -> new LinkedHashSet<>()).add(node);
          if (node.getContainer() == cluster) {
            iterator.remove();
          }
          continue;
        }

        // If the merged vertex directly belongs to the current Cluster, add an expansion record and remove the old record.
        if (node.getContainer() == cluster) {
          mergeNodes.computeIfAbsent(mergeNode, n -> new LinkedHashSet<>()).add(node);
          iterator.remove();
        } else if (mergeNode.getContainer() == cluster) {
          /*
           * If the merged vertex directly belongs to the current Cluster, and the merged vertex does
           * not directly belong to the current Cluster, the added expanded vertex should be the merged
           * vertex of the merged vertex in the Cluster directly belonging to the current cluster.
           * */
          mergeNodes.computeIfAbsent(mergeNode, n -> new LinkedHashSet<>())
              .add(clusterProxyNode(node, cluster));
        } else {
          /*
           * The public container of the merged vertex and the merged vertex is the current Cluster,
           * and the logic is the same as above.
           */
          if (commonParent == cluster) {
            mergeNodes.computeIfAbsent(mergeNode, n -> new LinkedHashSet<>())
                .add(clusterProxyNode(node, cluster));
          } else {
            /*
             * The public container of the merged vertex and the merged vertex is a sub-container
             * of the Cluster. It does not need to be expanded, and only needs to update the merge
             * node record of the Cluster directly under the mergeNode in the current Cluster.
             */
            clusterMerge.getMergeNodeOrPut((Cluster) commonParent, mergeNode);
          }
        }
      }

      if (clusterInnerSize != null) {
        ClusterDrawProp drawProp = dotAttachment.getDrawGraph().getClusterDrawProp(cluster);
        ClusterShape shape = cluster.clusterAttrs().getShape();
        FlatPoint size = clusterInnerSize.size();
        if (size != null) {
          FlatPoint outSize = shape.minContainerSize(size.getHeight(), size.getWidth());
          Asserts.nullArgument(outSize, "Cluster shape cannot return null outer box size");
          FlatPoint margin = cluster.clusterAttrs().getMargin();
          double verMargin = (outSize.getHeight() - size.getHeight()) / 2;
          double horMargin = (outSize.getWidth() - size.getWidth()) / 2;
          drawProp.setMargin(new FlatPoint(Math.max(verMargin, margin.getHeight()),
                                           Math.max(horMargin, margin.getWidth())));
        }
      }
      return crossRank;
    }

    @Override
    public Iterable<DNode> expandNodes() {
      return clusterMerge.clusterMergeNode(cluster);
    }

    @Override
    public Iterable<DNode> replaceNodes(DNode node) {
      return mergeNodes.get(node);
    }

    @Override
    public GraphContainer container() {
      return cluster;
    }
  }

  private class InitSort {

    private SameRankAdjacentRecord sameRankAdjacentRecord;

    private final Map<DNode, ComOrder> nodeComOrderMap;

    private final Map<Integer, Integer> rankAccessIndex;

    private final boolean isOutDirection;

    private final boolean isOrderForward;

    private final boolean atomicClusterBlocks;

    private final CrossRank crossRank;

    private final GraphContainer graphContainer;

    private final List<ComOrder> components;

    private final Set<GraphContainer> placedClusterBlocks;

    InitSort(CrossRank crossRank, GraphContainer graphContainer, DrawGraph drawGraph,
             boolean isOutDirection, boolean isOrderForward, boolean atomicClusterBlocks) {
      this.isOutDirection = isOutDirection;
      this.isOrderForward = isOrderForward;
      this.atomicClusterBlocks = atomicClusterBlocks;
      this.nodeComOrderMap = new HashMap<>();
      this.rankAccessIndex = new HashMap<>();
      this.graphContainer = graphContainer;
      this.crossRank = crossRank;
      this.components = new ArrayList<>();
      this.placedClusterBlocks = new HashSet<>();

      initByNatureDsfOrder(crossRank, drawGraph, isOutDirection);
    }

    private boolean isMark(DNode node) {
      return nodeComOrderMap.get(node) != null;
    }

    private void mark(DNode node, ComOrder comOrder) {
      nodeComOrderMap.put(node, comOrder);
    }

    private void initByNatureDsfOrder(CrossRank crossRank, DrawGraph drawGraph, boolean isOutDirection) {
      int first, addNum, limit;

      if (isOutDirection) {
        first = crossRank.minRank();
        addNum = 1;
        limit = crossRank.maxRank() + 1;
      } else {
        first = crossRank.maxRank();
        addNum = -1;
        limit = crossRank.minRank() - 1;
      }

      EdgeDedigraph<DNode, DLine> digraph = rootCrossRank.getDigraphProxy();

      Function<DNode, Iterable<DLine>> adjacentFunc = n -> {
        if (digraph instanceof MinCrossDedigraph) {
          MinCrossDedigraph dedigraph = (MinCrossDedigraph) digraph;
          if (isOutDirection) {
            if (dedigraph.outHavePort(n)) {
              return sortLines(n, drawGraph, dedigraph.outAdjacent(n));
            }
            return dedigraph.outAdjacent(n);
          }
          if (dedigraph.inHavePort(n)) {
            return sortLines(n, drawGraph, dedigraph.inAdjacent(n));
          }
          return dedigraph.inAdjacent(n);
        } else {
          return isOutDirection ? digraph.outAdjacent(n) : digraph.inAdjacent(n);
        }
      };

      int componentNo = 0;
      for (int i = first; i != limit; i += addNum) {
        int rankSize = crossRank.rankSize(i);
        for (int access = 0; access < rankSize; access++) {
          int j = isOrderForward ? access : rankSize - access - 1;
          DNode node = crossRank.getNode(i, j);
          if (isMark(node)) {
            continue;
          }

          ComOrder comOrder = new ComOrder(componentNo++);
          components.add(comOrder);
          dfs(node, adjacentFunc, comOrder);
        }
      }

      orderByComponents(crossRank);
      if (sameRankAdjacentRecord != null) {
        rootCrossRank.setSameRankAdjacentRecord(sameRankAdjacentRecord);
      }
      if (EnvProp.qualityCheck()) {
        List<String> violations = ClusterOrderConsistency.violations(crossRank, dotAttachment);
        Asserts.illegalArgument(!violations.isEmpty(),
                                "Cluster order changed between ranks in " + graphContainer.id()
                                    + ": " + violations);
      }
    }

    private void orderByComponents(CrossRank crossRank) {
      // All nodes in same components update the order to average of reference components,
      // this operation can break the local optimal dilemma to found a better global order.
      for (ComOrder component : components) {
        component.refreshByRefComs();
      }

      Comparator<DNode> comparator = (l, r) -> {
        ComOrder lc = nodeComOrderMap.get(l);
        ComOrder rc = nodeComOrderMap.get(r);
        int compare = lc.compareTo(rc);
        return isOrderForward ? compare : -compare;
      };
      crossRank.sort(comparator, false);
    }

    private void dfs(DNode from, Function<DNode, Iterable<DLine>> adjacentFunc, ComOrder component) {
      if (atomicClusterBlocks && clusterExpand != null && clusterExpand.clusterMerge != null) {
        GraphContainer direct = dotAttachment.clusterDirectContainer(graphContainer, from);
        // An already expanded cluster no longer owns merge nodes, so there is no block to place and
        // the node has to be handled like any other, or it would be left without a component.
        if (direct != null && direct.isCluster() && hasMergeNode((Cluster) direct)
            && placedClusterBlocks.add(direct)) {
          atomicClusterDfs((Cluster) direct, adjacentFunc, component);
          return;
        }
      }

      mark(from, component);

      int idx = nextRankIndex(from.getRank());
      crossRank.exchange(from, crossRank.getNode(from.getRank(), idx), false);

      GraphContainer fromContainer = dotAttachment
          .clusterDirectContainer(crossRank.container(), from);
      fromContainer = fromContainer == null ? from.getContainer() : fromContainer;
      DNode clusterAdjRankNode = null;

      if (fromContainer.isCluster() && clusterExpand != null
          && clusterExpand.clusterMerge != null) {
        Cluster cluster = (Cluster) fromContainer;
        /*
         * Get merge node of adjacent of cluster
         */
        clusterAdjRankNode = clusterExpand.clusterMerge
            .clusterMergeAdjRankNode(cluster, from, isOutDirection);
      }

      Iterable<DLine> adjacent = adjacentFunc.apply(from);
      for (DLine dLine : adjacent) {
        DNode to = dLine.other(from);
        toDfs(from, adjacentFunc, fromContainer, to, dLine, component);
      }

      /*
       * If from node in cluster, the rank of from node located possible
       * no any edges with the merge node of adjacent rank, we should
       * guarantee the next rank merge node accessed first even no any edges between them.
       */
      if (clusterAdjRankNode != null) {
        toDfs(from, adjacentFunc, fromContainer, clusterAdjRankNode, null, component);
      }
    }

    private void atomicClusterDfs(Cluster cluster,
                                  Function<DNode, Iterable<DLine>> adjacentFunc,
                                  ComOrder component) {
      List<DNode> representatives = new ArrayList<>();
      clusterExpand.clusterMerge.clusterMergeNode(cluster).forEach(representatives::add);
      if (!isOutDirection) {
        Collections.reverse(representatives);
      }

      ComOrder existing = null;
      for (DNode representative : representatives) {
        ComOrder marked = nodeComOrderMap.get(representative);
        if (marked != null) {
          existing = marked;
          break;
        }
      }
      if (existing != null && existing != component) {
        component.addRefCom(existing);
        component = existing;
      }

      // Reserve every rank occupied by the collapsed cluster before following any external edge.
      for (DNode representative : representatives) {
        if (isMark(representative)) {
          continue;
        }
        mark(representative, component);
        int index = nextRankIndex(representative.getRank());
        crossRank.exchange(representative,
                           crossRank.getNode(representative.getRank(), index), false);
      }

      // With the complete block reserved, its edges can safely discover other collapsed blocks.
      for (DNode representative : representatives) {
        for (DLine line : adjacentFunc.apply(representative)) {
          DNode to = line.other(representative);
          toDfs(representative, adjacentFunc, cluster, to, line, component);
        }
      }
    }

    private boolean hasMergeNode(Cluster cluster) {
      return clusterExpand.clusterMerge.clusterMergeNode(cluster).iterator().hasNext();
    }

    private int nextRankIndex(int rank) {
      int accessed = rankAccessIndex.getOrDefault(rank, 0);
      rankAccessIndex.put(rank, accessed + 1);
      return isOrderForward ? accessed : crossRank.rankSize(rank) - accessed - 1;
    }

    private void toDfs(DNode from, Function<DNode, Iterable<DLine>> adjacentFunc,
                       GraphContainer fromContainer, DNode to, DLine dLine,
                       ComOrder component) {
      /*
       * 1. Make sure cluster of to not intersect with cluster of from;
       * 2. Make sure only access head or tail node when to node in different cluster.
       */
      if (canNotAccessDiffCluster(from, fromContainer, to)) {
        return;
      }

      if (dotAttachment.notContains(graphContainer, to.getContainer())) {
        return;
      }

      if (to.getRank() == from.getRank() && dLine != null) {
        if (isOutDirection) {
          if (sameRankAdjacentRecord == null) {
            sameRankAdjacentRecord = new SameRankAdjacentRecord();
          }

          sameRankAdjacentRecord.addOutAdjacent(from, dLine);
        }
        return;
      }

      ComOrder toCom = nodeComOrderMap.get(to);
      if (toCom != null) {
        // addComponent order
        component.addRefCom(toCom);
        return;
      }

      dfs(to, adjacentFunc, component);
    }

    private boolean canNotAccessDiffCluster(DNode from, GraphContainer fromContainer, DNode to) {
      if (clusterExpand == null || clusterExpand.clusterMerge == null) {
        return false;
      }

      GraphContainer parentContainer = DotAttachment
          .commonParent(dotAttachment.getGraphviz(), from, to);
      if (parentContainer != crossRank.container()) {
        return false;
      }

      GraphContainer toContainer = dotAttachment.clusterDirectContainer(crossRank.container(), to);
      toContainer = toContainer == null ? to.getContainer() : toContainer;

      // Direct child clusters are placed as complete atomic rank blocks by dfs(). Once both sides
      // are such blocks, following their edge cannot split either cluster across components, even
      // when the edge enters at an intermediate rank.
      if (atomicClusterBlocks && fromContainer != toContainer && fromContainer.isCluster()
          && toContainer.isCluster()) {
        return false;
      }

      if (!atomicClusterBlocks && fromContainer != toContainer && fromContainer.isCluster()
          && toContainer.isCluster() && parentContainer != fromContainer
          && parentContainer != toContainer) {
        int fromMin = clusterExpand.clusterMerge.minRank((Cluster) fromContainer);
        int fromMax = clusterExpand.clusterMerge.maxRank((Cluster) fromContainer);
        int toMin = clusterExpand.clusterMerge.minRank((Cluster) toContainer);
        int toMax = clusterExpand.clusterMerge.maxRank((Cluster) toContainer);
        if (intersect(fromMin, fromMax, toMin, toMax)) {
          return true;
        }
      }

      /*
       * Only access the node which have different cluster with from node
       * if to node located at the top/bottom rank of cluster (depends on the direction)
       */
      if (toContainer.isCluster() && fromContainer != toContainer) {
        int toMin = clusterExpand.clusterMerge.minRank((Cluster) toContainer);
        int toMax = clusterExpand.clusterMerge.maxRank((Cluster) toContainer);
        if (from.getRank() < to.getRank()) {
          return to.getRank() != toMin;
        } else {
          return to.getRank() != toMax;
        }
      }

      return false;
    }

    private boolean intersect(int fromMin, int fromMax, int toMin, int toMax) {
      return inRange(toMin, toMax, fromMin) || inRange(toMin, toMax, fromMax)
          || inRange(fromMin, fromMax, toMin) || inRange(fromMin, fromMax, toMax);
    }

    private boolean inRange(int start, int end, int target) {
      return target >= start && target <= end;
    }

    private Iterable<DLine> sortLines(DNode node, DrawGraph drawGraph, Iterable<DLine> lines) {
      Set<DLine> sortLines = new TreeSet<>((l, r) -> lineComp(l, r, node, drawGraph));
      lines.forEach(sortLines::add);
      return sortLines;
    }

    private int lineComp(DLine left, DLine right, DNode node, DrawGraph drawGraph) {
      double leftComNo = PortHelper.portCompareNo(left.getLineDrawProp(), node, drawGraph);
      double rightComNo = PortHelper.portCompareNo(right.getLineDrawProp(), node, drawGraph);
      return Double.compare(leftComNo, rightComNo);
    }
  }

  private static class MinCrossDedigraph extends DedirectedEdgeGraph<DNode, DLine> {

    private static final long serialVersionUID = -2242254412888614002L;

    private Map<DNode, InOrOutHavePort> nodeInOrOutHavePortMap;

    MinCrossDedigraph(int capacity) {
      super(capacity);
    }

    void addEdge(DLine dLine, DrawGraph drawGraph) {
      this.addEdge(dLine);
      markNodeHavePort(dLine.getLineDrawProp(), dLine.from(), drawGraph, false);
      markNodeHavePort(dLine.getLineDrawProp(), dLine.to(), drawGraph, true);
    }

    boolean inHavePort(DNode node) {
      if (node == null || nodeInOrOutHavePortMap == null) {
        return false;
      }
      InOrOutHavePort inOrOutHavePort = nodeInOrOutHavePortMap.get(node);
      return inOrOutHavePort != null && inOrOutHavePort.inHavePort;
    }

    boolean outHavePort(DNode node) {
      if (node == null || nodeInOrOutHavePortMap == null) {
        return false;
      }
      InOrOutHavePort inOrOutHavePort = nodeInOrOutHavePortMap.get(node);
      return inOrOutHavePort != null && inOrOutHavePort.outHavePort;
    }

    private void markNodeHavePort(LineDrawProp line, DNode node, DrawGraph drawGraph, boolean isIn) {
      double compareNo = PortHelper.portCompareNo(line, node, drawGraph);
      if (compareNo == 0) {
        return;
      }

      if (nodeInOrOutHavePortMap == null) {
        nodeInOrOutHavePortMap = new HashMap<>();
      }
      InOrOutHavePort havePort = nodeInOrOutHavePortMap
          .computeIfAbsent(node, n -> new InOrOutHavePort());
      if (isIn) {
        havePort.inHavePort = true;
      } else {
        havePort.outHavePort = true;
      }
    }
  }

  private static class ClusterRankRange {

    private int minRank = Integer.MAX_VALUE;
    private int maxRank = Integer.MIN_VALUE;
  }

  private static class InOrOutHavePort {

    private boolean inHavePort;

    private boolean outHavePort;
  }

  static class ClusterMerge {

    private final Map<Cluster, NavigableMap<Integer, DNode>> clusterRankProxyNode;

    private final Map<Cluster, ClusterRankRange> clusterRankRange;

    private final Map<DNode, DNode> mergeNodeMap;

    public ClusterMerge() {
      this.clusterRankProxyNode = new LinkedHashMap<>();
      this.clusterRankRange = new LinkedHashMap<>();
      this.mergeNodeMap = new LinkedHashMap<>();
    }

    void clearCluster(Cluster cluster) {
      clusterRankProxyNode.remove(cluster);
    }

    Iterable<DNode> clusterMergeNode(GraphContainer container) {
      Map<Integer, DNode> rankMap = clusterRankProxyNode.get(container);
      if (rankMap == null) {
        return Collections.emptyList();
      }
      return rankMap.values();
    }

    DNode getMergeNode(DNode node) {
      return mergeNodeMap.get(node);
    }

    DNode getMergeNodeOrPut(Cluster cluster, DNode node) {
      DNode n = clusterRankProxyNode.computeIfAbsent(cluster, c -> new TreeMap<>())
          .computeIfAbsent(node.getRank(), k -> node);

      mergeNodeMap.put(node, n);
      return n;
    }

    DNode clusterMergeAdjRankNode(Cluster cluster, DNode node, boolean isNext) {
      if (cluster == null || node == null) {
        return null;
      }

      NavigableMap<Integer, DNode> clusterRankMap = clusterRankProxyNode.get(cluster);
      if (clusterRankMap == null) {
        return null;
      }
      Entry<Integer, DNode> adjRankMergeNode;
      if (isNext) {
        adjRankMergeNode = clusterRankMap.higherEntry(node.getRank());
      } else {
        adjRankMergeNode = clusterRankMap.lowerEntry(node.getRank());
      }

      if (adjRankMergeNode == null) {
        return null;
      }
      return adjRankMergeNode.getValue();
    }

    int minRank(Cluster cluster) {
      ClusterRankRange range = clusterRankRange.get(cluster);
      Asserts.illegalArgument(range == null,
                              "Do not have cluster rank record");
      return range.minRank;
    }

    int maxRank(Cluster cluster) {
      ClusterRankRange range = clusterRankRange.get(cluster);
      Asserts.illegalArgument(range == null,
                              "Do not have cluster rank record");
      return range.maxRank;
    }

    boolean isSingleRankCluster(GraphContainer container) {
      if (container == null || !container.isCluster()) {
        return false;
      }
      Cluster cluster = (Cluster) container;
      return minRank(cluster) == maxRank(cluster);
    }
  }

  private class ClusterInnerSize {

    private Map<Integer, FlatPoint> rankSize;

    void refresh(DNode node) {
      if (rankSize == null) {
        rankSize = new HashMap<>();
      }

      rankSize.compute(node.getRank(), (r, w) -> {
        if (w == null) {
          return new FlatPoint(node.getHeight(), node.getNodeSep() + node.getWidth());
        }
        w.setHeight(Math.max(w.getHeight(), node.getHeight()));
        w.setWidth(w.getWidth() + node.getWidth() + node.getNodeSep());
        return w;
      });
    }

    FlatPoint size() {
      if (rankSize == null) {
        return null;
      }

      double nodeSep = dotAttachment.getGraphviz().graphAttrs().getNodeSep();
      double height = 0;
      double width = Double.MIN_VALUE;
      for (Entry<Integer, FlatPoint> entry : rankSize.entrySet()) {
        width = Math.max(width, entry.getValue().getWidth());
        height += nodeSep + entry.getValue().getHeight();
      }

      return new FlatPoint(height, width);
    }
  }

  private static class ComOrder implements Comparable<ComOrder> {
    private double order;
    private List<ComOrder> refComs;

    private ComOrder(double order) {
      this.order = order;
    }

    private void addRefCom(ComOrder reference) {
      if (refComs == null) {
        refComs = new ArrayList<>();
      }
      refComs.add(reference);
    }

    private void refreshByRefComs() {
      if (CollectionUtils.isEmpty(refComs)) {
        return;
      }

      double newOrder =  0;
      for (ComOrder refCom : refComs) {
        newOrder += refCom.order;
      }
      newOrder /= refComs.size();
      this.order = newOrder;
    }

    @Override
    public int compareTo(ComOrder c) {
      if (c == null) {
        return -1;
      }
      if (c == this) {
        return 0;
      }
      return Double.compare(order, c.order);
    }
  }
}
