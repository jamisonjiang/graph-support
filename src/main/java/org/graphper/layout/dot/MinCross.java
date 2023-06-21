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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import org.graphper.api.Line;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.dot.RootCrossRank.ExpandInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.Digraph.VertexDigraph;
import org.graphper.def.DirectedGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.layout.Mark;
import org.graphper.layout.dot.RankContent.RankNode;

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

  private MinCrossDedigraph digraphProxy;

  MinCross(RankContent rankContent, DotAttachment dotAttachment) {
    this.rankContent = rankContent;
    this.dotAttachment = dotAttachment;

    // Cut the line which span over than 2
    reduceLongEdges();

    // Init RootCrossRank
    initRootCrossRank();

    // Dot mincross
    dotMincross();

    // Sync node order
    syncRankOrder();

    this.rootCrossRank = null;
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
    GraphContainer container = DotAttachment.commonParent(graphviz, from, edge.to());

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

        to = DNode.newVirtualNode(20, container);
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
            new DLine(from, to, edge.getLine(),
                      edge.lineAttrs(), edge.weight(), edge.limit()),
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
    if (clusterExpand != null) {
      clusterExpand.cluster = dotAttachment.getGraphviz();
    }
    mincross(0, 2);

    for (Cluster cluster : DotAttachment.clusters(dotAttachment.getGraphviz())) {
      mincrossCluster(cluster);
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

    for (Cluster c : DotAttachment.clusters(cluster)) {
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

  private void initRootCrossRank() {
    if (!dotAttachment.haveClusters()) {
      rootCrossRank = new RootCrossRank(dotAttachment.getDrawGraph(), digraphProxy);
      return;
    }

    rootCrossRank = new RootCrossRank(dotAttachment.getDrawGraph());
    Graphviz graphviz = dotAttachment.getGraphviz();
    clusterExpand = new ClusterExpand(new ClusterMerge());

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
        line = new DLine(fromClusterNode, toCLusterNode, null, null, line.weight(), line.limit());
        rootCrossRank.addEdge(line);
      }
    } else {
      nodes.add(toCLusterNode);
      rootCrossRank.addEdge(line);
    }
  }

  private DNode clusterProxyNode(DNode node, GraphContainer graphContainer) {
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
    int maxThisPass;
    int trying;
    int minCrossNum = rootCrossRank.currentCrossNum();
    int currentNum = minCrossNum;
    BasicCrossRank tmp;
    BasicCrossRank optimal = rootCrossRank.getBasicCrossRank();
    int minQuit = dotAttachment.getDrawGraph().getGraphviz().graphAttrs().getMclimit();
    int maxIter = 24;

    BasicCrossRank c = optimal.clone();
    new InitSort(c, c.container(), dotAttachment.getDrawGraph(), true);
    int cn = getCrossNum(c);
    if (cn <= minCrossNum) {
      optimal = c;
      minCrossNum = currentNum = cn;
      rootCrossRank.setBasicCrossRank(optimal);
    }

    BasicCrossRank p = optimal.clone();
    new InitSort(p, p.container(), dotAttachment.getDrawGraph(), false);
    cn = getCrossNum(p);
    if (cn < minCrossNum) {
      optimal = p;
      minCrossNum = currentNum = cn;
      rootCrossRank.setBasicCrossRank(optimal);
    }

    for (int pass = startPass; pass <= endPass; pass++) {
      if (pass <= 1) {
        maxThisPass = Math.min(4, maxIter);

        if (pass == 1 && (rootCrossRank.getSameRankAdjacentRecord() != null
            || optimal.container().haveChildCluster())) {
          BasicCrossRank repl = optimal.clone();
          new InitSort(repl, repl.container(), dotAttachment.getDrawGraph(), false, false);

          tmp = rootCrossRank.getBasicCrossRank();
          rootCrossRank.setBasicCrossRank(repl);
          if (minCrossNum >= (currentNum = rootCrossRank.currentCrossNum())) {
            optimal = repl;
          } else {
            rootCrossRank.setBasicCrossRank(tmp);
          }
        }

        flatOrder(optimal);
        rootCrossRank.setBasicCrossRank(optimal);
        minCrossNum = rootCrossRank.currentCrossNum();
      } else {
        maxThisPass = maxIter;
      }

      optimal = optimal.clone();

      trying = 0;
      for (int i = 0; i < maxThisPass; i++) {
        if (log.isDebugEnabled()) {
          log.debug("pass {} iter {} trying {} cur_cross {} best_cross {}", pass, i, trying,
                    currentNum, minCrossNum);
        }

        if (trying++ >= minQuit || minCrossNum == 0) {
          break;
        }

        mincrossStep(i);

        // If the number of intersections is less than the minimum number of
        // intersections at this time, update the optimal sort.
        if (minCrossNum > (currentNum = rootCrossRank.currentCrossNum())) {
          optimal = rootCrossRank.getBasicCrossRank().clone();

          if (currentNum < CONVERGENCE * minCrossNum) {
            trying = 0;
          }

          minCrossNum = currentNum;
        }
      }

      if (minCrossNum == 0) {
        break;
      }
    }

    rootCrossRank.setBasicCrossRank(optimal);
    rootCrossRank.transpose(false);
    rootCrossRank.syncChildOrder();
  }

  private void mincrossStep(int iterNum) {
    rootCrossRank.vmedian(iterNum);
    rootCrossRank.transpose(iterNum % 4 >= 2);
  }

  private int getCrossNum(BasicCrossRank basicCrossRank) {
    BasicCrossRank tmp = rootCrossRank.getBasicCrossRank();
    rootCrossRank.setBasicCrossRank(basicCrossRank);
    int n = rootCrossRank.currentCrossNum();
    rootCrossRank.setBasicCrossRank(tmp);
    return n;
  }

  private void flatOrder(CrossRank crossRank) {
    SameRankAdjacentRecord sameRankAdjacentRecord = rootCrossRank.getSameRankAdjacentRecord();

    int[] no = {0};
    int connectNo = 0;
    Set<DNode> mark = new HashSet<>();
    Map<DNode, Map.Entry<Integer, Integer>> postOrderRecord = new HashMap<>();

    ClusterOrder clusterOrder = clusterExpand != null
        ? new ClusterOrder(clusterExpand.clusterMerge) : null;

    for (int i = crossRank.minRank(); i <= crossRank.maxRank(); i++) {

      for (int j = 0; j < crossRank.rankSize(i); j++) {
        DNode node = crossRank.getNode(i, j);

        if (mark.contains(node) || (sameRankAdjacentRecord != null
            && sameRankAdjacentRecord.haveIn(node))) {
          continue;
        }

        GraphContainer fromCluster = addClusterNode(node, clusterOrder);
        if (fromCluster != null) {
          clusterOrder.put(node, fromCluster);
          clusterOrder.addReorderNode(node);
        }

        postOrder(connectNo++, no, node, mark, clusterOrder, fromCluster, postOrderRecord);
      }
    }

    clusterPostOrder(clusterOrder);
    rootCrossRank.clusterOrder = clusterOrder;

    crossRank.sort((left, right) -> {
      Integer leftConnect = postOrderRecord.get(left).getKey();
      Integer rightConnect = postOrderRecord.get(right).getKey();

      if (!Objects.equals(leftConnect, rightConnect)) {
        return leftConnect.compareTo(rightConnect);
      }

      Integer leftPost = postOrderRecord.get(left).getValue();
      Integer rightPost = postOrderRecord.get(right).getValue();

      return rightPost.compareTo(leftPost);
    });

    if (clusterOrder != null) {
      clusterOrder.reorderNode(crossRank);
    }
  }

  private int postOrder(int connectNo, int[] no, DNode node, Set<DNode> mark,
                        ClusterOrder clusterOrder, GraphContainer fromCluster,
                        Map<DNode, Map.Entry<Integer, Integer>> orderRecord) {
    mark.add(node);

    if (rootCrossRank.getSameRankAdjacentRecord() == null) {
      orderRecord.put(node, new AbstractMap.SimpleEntry<>(connectNo, no[0]++));
      return connectNo;
    }

    Set<DNode> adjacent = rootCrossRank.getSameRankAdjacentRecord().outAdjacent(node);
    if (CollectionUtils.isNotEmpty(adjacent)) {
      for (DNode dNode : adjacent) {
        if (mark.contains(dNode)) {
          Entry<Integer, Integer> accessOrder = orderRecord.get(dNode);
          if (accessOrder != null) {
            connectNo = accessOrder.getKey() != null ? accessOrder.getKey() : connectNo;
          }
          continue;
        }

        GraphContainer toCluster = addClusterNode(dNode, clusterOrder);

        if (fromCluster != null && toCluster != null) {
          clusterOrder.addEdge(fromCluster, toCluster);
        }

        if (toCluster != null) {
          clusterOrder.put(dNode, toCluster);
          clusterOrder.addReorderNode(dNode);
        }

        connectNo = postOrder(connectNo, no, dNode, mark, clusterOrder, toCluster, orderRecord);
      }
    }

    orderRecord.put(node, new AbstractMap.SimpleEntry<>(connectNo, no[0]++));
    return connectNo;
  }

  private GraphContainer addClusterNode(DNode node, ClusterOrder clusterOrder) {
    if (clusterOrder == null) {
      return null;
    }

    DNode mergeNode = clusterOrder.getMergeNode(node);
    if (mergeNode != null) {
      GraphContainer cluster = dotAttachment
          .clusterDirectContainer(clusterExpand.container(), mergeNode);
      if (cluster == null) {
        return null;
      }

      clusterOrder.add(cluster);
      return cluster;
    }

    return null;
  }

  private void clusterPostOrder(ClusterOrder clusterOrder) {
    if (clusterOrder == null) {
      return;
    }

    Set<GraphContainer> mark = new HashSet<>();
    Deque<GraphContainer> stack = new LinkedList<>();
    for (GraphContainer container : clusterOrder.containers()) {
      if (mark.contains(container)) {
        continue;
      }

      clusterDfs(container, clusterOrder, mark, stack);
    }

    while (!stack.isEmpty()) {
      int size = stack.size();
      clusterOrder.addClusterOrder(stack.pollFirst(), size);
    }
  }

  private void clusterDfs(GraphContainer container, ClusterOrder clusterOrder,
                          Set<GraphContainer> mark, Deque<GraphContainer> stack) {
    mark.add(container);

    for (GraphContainer c : clusterOrder.adj(container)) {
      if (mark.contains(c)) {
        continue;
      }

      clusterDfs(c, clusterOrder, mark, stack);
    }

    stack.offerFirst(container);
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

      BasicCrossRank crossRank = new BasicCrossRank(cluster);
      Iterator<Entry<DNode, DNode>> iterator = clusterMerge.mergeNodeMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<DNode, DNode> entry = iterator.next();
        DNode node = entry.getKey();
        DNode mergeNode = entry.getValue();

        GraphContainer commonParent = dotAttachment.commonParent(node, mergeNode);
        if (dotAttachment.notContain(cluster, commonParent)) {
          continue;
        }

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

  private class InitSort extends Mark<DNode> {

    private SameRankAdjacentRecord sameRankAdjacentRecord;

    private final Map<Integer, Integer> rankAccessIndex;

    private final boolean isOutDirection;

    private final CrossRank crossRank;

    private final GraphContainer graphContainer;

    InitSort(CrossRank crossRank, GraphContainer graphContainer,
             DrawGraph drawGraph, boolean isOutDirection) {
      this(crossRank, graphContainer, drawGraph, true, isOutDirection);
    }

    InitSort(CrossRank crossRank, GraphContainer graphContainer, DrawGraph drawGraph,
             boolean isNormal, boolean isOutDirection) {
      this.isOutDirection = isOutDirection;
      this.rankAccessIndex = new HashMap<>();
      this.graphContainer = graphContainer;
      this.crossRank = crossRank;

      if (isNormal) {
        normalInit(crossRank, drawGraph, isOutDirection);
      } else {
        flatInit();
      }
    }

    private void normalInit(CrossRank crossRank, DrawGraph drawGraph, boolean isOutDirection) {
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

      for (int i = first; i != limit; i += addNum) {
        for (int j = 0; j < crossRank.rankSize(i); j++) {
          DNode node = crossRank.getNode(i, j);
          if (isMark(node)) {
            continue;
          }

          dfs(node, adjacentFunc);
        }
      }

      if (sameRankAdjacentRecord != null) {
        rootCrossRank.setSameRankAdjacentRecord(sameRankAdjacentRecord);
      }
    }

    private void flatInit() {
      Set<DNode> adjNodes = new TreeSet<>(Comparator.comparingInt(crossRank::getRankIndex));
      Map<DNode, Integer> rankIndexRecord = new HashMap<>();
      for (int i = crossRank.minRank(); i <= crossRank.maxRank(); i++) {
        int s = crossRank.rankSize(i);
        int rankIdx = 0;
        rankIndexRecord.clear();

        for (int j = 0; j < s; j++) {
          adjNodes.clear();
          DNode node = crossRank.getNode(i, j);
          mark(node);

          for (DLine line : rootCrossRank.getDigraphProxy().outAdjacent(node)) {
            if (dotAttachment.notContain(graphContainer, line.to().getContainer())) {
              continue;
            }

            DNode other = line.other(node);
            if (other.getRank() == node.getRank() || isMark(other)) {
              continue;
            }

            mark(other);
            adjNodes.add(other);
          }

          for (DNode n : adjNodes) {
            rankIndexRecord.put(n, rankIdx++);
          }
        }

        int ri = rankIdx;
        crossRank.sort(i + 1, Comparator.comparingInt(n -> {
          Integer idx = rankIndexRecord.get(n);
          return idx != null ? idx : ri + crossRank.getRankIndex(n);
        }));
      }
    }

    private void dfs(DNode from, Function<DNode, Iterable<DLine>> adjacentFunc) {
      mark(from);

      int idx = rankAccessIndex.getOrDefault(from.getRank(), 0);
      crossRank.exchange(from, crossRank.getNode(from.getRank(), idx));
      rankAccessIndex.put(from.getRank(), idx + 1);

      Iterable<DLine> adjacent = adjacentFunc.apply(from);
      for (DLine dLine : adjacent) {
        DNode to = dLine.other(from);

        if (dotAttachment.notContain(graphContainer, to.getContainer())) {
          continue;
        }

        if (isOutDirection && to.getRank() == from.getRank()) {
          if (sameRankAdjacentRecord == null) {
            sameRankAdjacentRecord = new SameRankAdjacentRecord();
          }

          sameRankAdjacentRecord.addOutAdjacent(from, dLine);
          continue;
        }

        if (isMark(to)) {
          continue;
        }

        dfs(to, adjacentFunc);
      }
    }

    private Iterable<DLine> sortLines(DNode node, DrawGraph drawGraph, Iterable<DLine> lines) {
      Set<DLine> sortLines = new TreeSet<>((l, r) -> lineComp(l, r, node, drawGraph));
      lines.forEach(sortLines::add);
      return sortLines;
    }

    private int lineComp(DLine left, DLine right, DNode node, DrawGraph drawGraph) {
      double leftComNo = PortHelper.portCompareNo(left.getLine(), node, drawGraph);
      double rightComNo = PortHelper.portCompareNo(right.getLine(), node, drawGraph);
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
      markNodeHavePort(dLine.getLine(), dLine.from(), drawGraph, false);
      markNodeHavePort(dLine.getLine(), dLine.to(), drawGraph, true);
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

    private void markNodeHavePort(Line line, DNode node, DrawGraph drawGraph, boolean isIn) {
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

  private static class InOrOutHavePort {

    private boolean inHavePort;

    private boolean outHavePort;
  }

  private static class ClusterMerge {

    private final Map<Cluster, Map<Integer, DNode>> clusterRankProxyNode;

    private final Map<DNode, DNode> mergeNodeMap;

    public ClusterMerge() {
      this.clusterRankProxyNode = new LinkedHashMap<>();
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
      DNode n = clusterRankProxyNode.computeIfAbsent(cluster, c -> new HashMap<>())
          .computeIfAbsent(node.getRank(), k -> node);

      mergeNodeMap.put(node, n);
      return n;
    }
  }

  static class ClusterOrder {

    private final ClusterMerge clusterMerge;

    private VertexDigraph<GraphContainer> clusterGraph;

    private Map<DNode, GraphContainer> nodeGraphContainerMap;

    private Map<GraphContainer, Integer> orderRecord;

    private Map<Integer, List<DNode>> reorderClusterNodes;

    public ClusterOrder(ClusterMerge clusterMerge) {
      Asserts.nullArgument(clusterMerge, "clusterMerge");
      this.clusterMerge = clusterMerge;
    }

    Iterable<GraphContainer> containers() {
      if (clusterGraph == null) {
        return Collections.emptyList();
      }

      return clusterGraph;
    }

    Iterable<GraphContainer> adj(GraphContainer container) {
      if (clusterGraph == null) {
        return Collections.emptyList();
      }

      return clusterGraph.adjacent(container);
    }

    void put(DNode node, GraphContainer container) {
      if (nodeGraphContainerMap == null) {
        nodeGraphContainerMap = new HashMap<>();
      }

      nodeGraphContainerMap.put(node, container);
    }

    void add(GraphContainer container) {
      clusterGraph().add(container);
    }

    void addEdge(GraphContainer n, GraphContainer w) {
      clusterGraph().addEdge(n, w);
    }

    DNode getMergeNode(DNode node) {
      return clusterMerge.getMergeNode(node);
    }

    void addClusterOrder(GraphContainer container, int order) {
      if (orderRecord == null) {
        orderRecord = new HashMap<>();
      }

      orderRecord.put(container, order);
    }

    Integer getClusterOrder(GraphContainer container) {
      if (orderRecord == null) {
        return null;
      }

      return orderRecord.get(container);
    }

    Integer getNodeOrder(DNode node) {
      if (nodeGraphContainerMap == null) {
        return null;
      }

      GraphContainer container = nodeGraphContainerMap.get(node);
      if (container == null) {
        return null;
      }
      return getClusterOrder(container);
    }

    void addReorderNode(DNode node) {
      if (reorderClusterNodes == null) {
        reorderClusterNodes = new HashMap<>();
      }

      reorderClusterNodes.computeIfAbsent(node.getRank(), n -> new ArrayList<>(2)).add(node);
    }

    void reorderNode(CrossRank crossRank) {
      if (reorderClusterNodes == null || reorderClusterNodes.size() == 0) {
        return;
      }

      for (Entry<Integer, List<DNode>> entry : reorderClusterNodes.entrySet()) {
        List<DNode> nodes = entry.getValue();

        for (int i = 0; i < nodes.size(); i++) {
          DNode n = nodes.get(i);
          int ni = crossRank.getRankIndex(n);
          Integer nci = getNodeOrder(n);
          if (nci == null) {
            continue;
          }

          for (int j = i + 1; j < nodes.size(); j++) {
            DNode w = nodes.get(j);
            int wi = crossRank.getRankIndex(w);
            Integer wci = getNodeOrder(w);

            if (wci == null) {
              continue;
            }

            if (ni - wi > 0 != nci - wci > 0) {
              crossRank.exchange(n, w);
            }
          }
        }
      }

      reorderClusterNodes.clear();
    }

    private VertexDigraph<GraphContainer> clusterGraph() {
      if (clusterGraph == null) {
        clusterGraph = new DirectedGraph<>();
      }
      return clusterGraph;
    }
  }
}
