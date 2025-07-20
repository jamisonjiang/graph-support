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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.layout.dot.SubgraphMerge.MergeNode;

/**
 * This process mainly has the following steps:
 * <ul>
 *   <li>All clusters under the container recursively continue;
 *   <li>Use a certain strategy to find the proxy nodes of subgraph and cluster;
 *   <li>Using Surrogate Nodes to Construct Auxiliary Graphs for Hierarchical Assignment of Network Simplex Method;
 *   <li>Expand cluster and subgraph.
 * </ul>
 *
 * @author Jamison Jiang
 */
class ContainerCollapse {

  private final DotAttachment dotAttachment;

  private final GraphContainer graphContainer;

  private final RankContent rankContent;

  ContainerCollapse(DotAttachment dotAttachment, GraphContainer graphContainer) {
    this.dotAttachment = dotAttachment;
    this.graphContainer = graphContainer;
    this.rankContent = dotRank();
  }

  RankContent getRankContent() {
    return rankContent;
  }

  private RankContent dotRank() {
    DotDigraph digraph;
    SubRankInfo subRankInfo = null;

    if (!dotAttachment.haveClusters() && !dotAttachment.haveSubgraphs()
        && graphContainer == dotAttachment.getGraphviz()) {
      // No child container, use the original graph
      digraph = dotAttachment.getDotDigraph();
    } else {
      // Collapse cluster,return processed DotDigraph
      digraph = new DotDigraph(graphContainer.directNodes().size()
                                   + graphContainer.clusters().size()
                                   + graphContainer.subgraphs().size());
      // Compress sub-containers, create proxy graphs
      subRankInfo = collapseSet(digraph);
    }

    if (digraph.vertexNum() == 0) {
      return null;
    }

    // Make digraph acyclic
    new Acyclic(digraph, dotAttachment.getDrawGraph());

    GraphAttrs graphAttrs = dotAttachment.getDrawGraph().getGraphviz().graphAttrs();
    // Hierarchy the nodes using the network simplex method.
    FeasibleTree feasibleTree = new FeasibleTree(digraph);
    SubgraphMerge subgraphMerge = subRankInfo != null ? subRankInfo.subgraphMerge : null;
    NetworkSimplex networkSimplex = new NetworkSimplex(
        feasibleTree,
        graphAttrs.getNslimit1(),
        graphAttrs.getRankSep(),
        // Let border node feature take effect
        nodes -> borderNodeHandle(nodes, subgraphMerge)
    );

    // RankContent maybe contain sub container's merge node,need restore
    return restoreRankContent(networkSimplex.getRankContent(), subRankInfo);
  }

  private void borderNodeHandle(DNode[] nodes, SubgraphMerge subgraphMerge) {
    if (subgraphMerge == null || subgraphMerge.isEmpty() || !subgraphMerge.haveBorderNode()) {
      return;
    }

    Integer sourceIdx = null;
    Integer minIdx = null;
    Integer maxIdx = null;
    Integer sinkIdx = null;
    for (int i = 0; i < nodes.length; i++) {
      DNode n = nodes[i];
      if (n.isVirtual()) {
        continue;
      }

      MergeNode mergeNode = subgraphMerge.getMergeNode(n);
      if (mergeNode != null && mergeNode.getNode() == n && mergeNode.isBorder()) {
        switch (mergeNode.getRank()) {
          case SOURCE:
            sourceIdx = i;
            break;
          case MIN:
            minIdx = i;
            break;
          case MAX:
            maxIdx = i;
            break;
          case SINK:
            sinkIdx = i;
            break;
          default:
            break;
        }
      }
    }

    moveRank(nodes, sourceIdx, minIdx, maxIdx, sinkIdx);
  }

  private void moveRank(DNode[] nodes, Integer sourceIdx,
                        Integer minIdx, Integer maxIdx,
                        Integer sinkIdx) {
    if (sourceIdx != null) {
      int minRank = nodes[0].getRank();
      insert(nodes, sourceIdx, 0);
      DNode minNormalNode = findRankNormalNode(nodes, nodes[0], 0, minRank, 1);
      if (minNormalNode != null) {
        minRank--;
      }
      nodes[0].setRank(minRank);
    } else if (minIdx != null) {
      int minRank = nodes[0].getRank();
      insert(nodes, minIdx, 0);
      nodes[0].setRank(minRank);
    }

    if (sinkIdx != null) {
      int maxRank = nodes[nodes.length - 1].getRank();
      insert(nodes, sinkIdx, nodes.length - 1);
      DNode maxNormalNode = findRankNormalNode(nodes, nodes[nodes.length - 1], nodes.length - 1,
                                               maxRank, -1);
      if (maxNormalNode != null) {
        maxRank++;
      }
      nodes[nodes.length - 1].setRank(maxRank);
    } else if (maxIdx != null) {
      int maxRank = nodes[nodes.length - 1].getRank();
      insert(nodes, maxIdx, nodes.length - 1);
      nodes[nodes.length - 1].setRank(maxRank);
    }
  }

  private DNode findRankNormalNode(DNode[] nodes, DNode skipNode, int startIdx, int rank,
                                   int delta) {
    for (int i = startIdx; i < nodes.length && i >= 0; i += delta) {
      if (nodes[i] == skipNode) {
        continue;
      }
      if (nodes[i].getRank() != rank) {
        break;
      }
      if (!nodes[i].isVirtual()) {
        return nodes[i];
      }
    }

    return null;
  }

  private void insert(DNode[] nodes, int sourceIdx, int targetIdx) {
    if (sourceIdx == targetIdx) {
      return;
    }

    DNode t = nodes[sourceIdx];
    if (Math.abs(targetIdx - sourceIdx) == 1) {
      nodes[sourceIdx] = nodes[targetIdx];
      nodes[targetIdx] = t;
      return;
    }

    if (sourceIdx > targetIdx) {
      System.arraycopy(nodes, targetIdx, nodes, targetIdx + 1, sourceIdx - targetIdx);
      nodes[targetIdx] = t;
    } else {
      System.arraycopy(nodes, sourceIdx + 1, nodes, sourceIdx, targetIdx - sourceIdx);
      nodes[targetIdx] = t;
    }
  }

  private SubRankInfo collapseSet(DotDigraph digraph) {
    List<Cluster> clusters = new ArrayList<>(0);
    // Merge subgraphs and create proxy nodes for each group of subgraphs
    SubgraphMerge subgraphMerge = SubgraphMerge
        .newSubgraphMerge(graphContainer, dotAttachment, c -> clusters.addAll(c.clusters()));

    return new SubRankInfo(
        clusters,
        subgraphMerge,
        /*
         * Inside the sub-container, use only one node proxy the other nodes
         */
        rankContentHandle(digraph, clusters, subgraphMerge)
    );
  }

  private RankContent restoreRankContent(RankContent rankContent, SubRankInfo subRankInfo) {
    if (subRankInfo == null) {
      return rankContent;
    }

    Collection<Cluster> clusters = subRankInfo.clusters != null
        ? subRankInfo.clusters
        : Collections.emptyList();

    // Expand cluster
    for (Cluster cluster : clusters) {
      // Get the cluster proxy node
      RankTemp rankTemp = subRankInfo.clusterMerge.get(cluster);

      if (rankTemp == null) {
        continue;
      }

      // Make the rank of the node inside this cluster align with the rank of the current container
      int r = rankTemp.mergeNode.getRank() - rankTemp.minRank;
      for (DNode dNode : dotAttachment.nodes(cluster)) {
        GraphContainer c = findCurrentContainerDirectContain(dNode);

        if (c != cluster || dNode == rankTemp.mergeNode) {
          continue;
        }

        dNode.setRank(dNode.getRank() + r);
      }
    }

    // Set the rank of nodes which is not delegate node in subgraph
    for (DNode node : subRankInfo.subgraphMerge.nodes()) {
      MergeNode mergeNode = subRankInfo.subgraphMerge.getMergeNode(node);
      if (mergeNode == null) {
        continue;
      }

      node.setRank(mergeNode.getNode().getRank());
    }

    return rankContent;
  }

  private Map<GraphContainer, RankTemp> rankContentHandle(DotDigraph digraph,
                                                          List<Cluster> clusters,
                                                          SubgraphMerge subgraphMerge) {
    Map<GraphContainer, RankTemp> clusterMerge = null;
    for (Cluster cluster : clusters) {
      if (cluster.isEmpty()) {
        continue;
      }

      if (clusterMerge == null) {
        clusterMerge = new HashMap<>(graphContainer.clusters().size());
      }

      // Get cluster's min rank node
      ContainerCollapse collapse = new ContainerCollapse(dotAttachment, cluster);
      RankContent rc = collapse.getRankContent();
      if (rc == null) {
        continue;
      }

      // Record the delegate node of cluster
      clusterMerge.put(cluster, findMinRank(rc));
    }

    // Add aux digraph node, Cluster/Subgraph need merge to one node
    addAuxDigraphNode(digraph, subgraphMerge, clusterMerge);

    // Add aux digraph line, need change the line who have endpoint node in Cluster/Subgraph
    addAuxDigraphLine(digraph, subgraphMerge, clusterMerge);

    return clusterMerge;
  }

  private void addAuxDigraphNode(DotDigraph digraph, SubgraphMerge subgraphMerge,
                                 Map<GraphContainer, RankTemp> clusterMerge) {
    for (DNode dNode : dotAttachment.nodes(graphContainer)) {
      GraphContainer c = findCurrentContainerDirectContain(dNode);

      // Node not in current container
      if (c == null) {
        continue;
      }

      // Add cluster delegate node
      RankTemp rankTemp = clusterMerge != null ? clusterMerge.get(c) : null;
      DNode clusterMergeNode = rankTemp != null ? rankTemp.mergeNode : null;

      if (clusterMergeNode != null) {
        digraph.add(clusterMergeNode);
        continue;
      }
      if (dNode.getContainer() != graphContainer) {
        continue;
      }

      // Add subgraph delegate node
      MergeNode mergeNode = subgraphMerge.getMergeNode(dNode);
      if (mergeNode == null || mergeNode.getNode() == dNode) {
        digraph.add(dNode);
      }
    }
  }

  private void addAuxDigraphLine(DotDigraph digraph, SubgraphMerge subgraphMerge,
                                 Map<GraphContainer, RankTemp> clusterMerge) {
    DrawGraph drawGraph = dotAttachment.getDrawGraph();
    for (Line line : dotAttachment.lines(graphContainer)) {
      DNode from = dotAttachment.get(line.tail());
      DNode to = dotAttachment.get(line.head());

      // Ignore means this node is accommodated by other containers, but the current container does
      // not grab this node, and this node does not belong to the current container.
      if (needIgnore(from, to)) {
        continue;
      }

      LineAttrs lineAttrs = drawGraph.lineAttrs(line);
      double weight = lineAttrs.getWeight() != null ? lineAttrs.getWeight() : 1D;
      int minlen = lineAttrs.getMinlen() != null ? lineAttrs.getMinlen() : 1;

      // Find the delegate node of the Subgraph where the two endpoints of the current edge are located
      MergeNode fromSubMergeNode = subgraphMerge.getMergeNode(from);
      MergeNode toSubMergeNode = subgraphMerge.getMergeNode(to);

      // If the "from" node is set to MAX or SINK, or the "to" node is set to MIN or SOURCE, the edge needs to be reversed.
      if (needReverse(fromSubMergeNode, toSubMergeNode)) {
        Object tmp = from;
        from = to;
        to = (DNode) tmp;

        tmp = fromSubMergeNode;
        fromSubMergeNode = toSubMergeNode;
        toSubMergeNode = (MergeNode) tmp;
      }

      from = fromSubMergeNode != null ? fromSubMergeNode.getNode() : from;
      to = toSubMergeNode != null ? toSubMergeNode.getNode() : to;

      if (clusterMerge == null) {
        digraph.addEdge(new DLine(from, to, null, weight, minlen));
        continue;
      }

      // Get the cluster delegate node
      RankTemp fromRankTemp = clusterMerge.get(findCurrentContainerDirectContain(from));
      RankTemp toRankTemp = clusterMerge.get(findCurrentContainerDirectContain(to));

      DNode fm = fromRankTemp != null ? fromRankTemp.mergeNode : null;
      DNode tm = toRankTemp != null ? toRankTemp.mergeNode : null;

      // Do not have any cluster delegate node
      if (isDirect(fm, tm)) {
        digraph.addEdge(new DLine(from, to, null, weight, minlen));
        continue;
      }

      /*
       * 1.The line(aux -> from)'s weight is ten times than primitive weight
       * 2.The line(aux -> to)'s weight inherit primitive weight
       * */
      addAuxClusterLine(digraph, from, to, fm, tm, weight, minlen);
    }
  }

  private void addAuxClusterLine(DotDigraph digraph, DNode from, DNode to,
                                 DNode fm, DNode tm, double weight, int minlen) {
    DNode aux = new DNode(null, 0, 0, 0);
    if (tm == null) {
      /*
       * If primitive line's from is in child cluster
       *
       * 1.The line(aux -> from)'s minlen is zero
       * 2.The line(aux -> to)'s minlen is primitive minlen add the value that original node level minus the smallest level in the cluster
       */
      digraph.addEdge(new DLine(aux, fm, null, weight * 10, 0));
      digraph.addEdge(
          new DLine(aux, to, null, weight, minlen + from.getRank() - fm.getRank()));
      return;
    }

    int r;
    if (inDiffChildCluster(fm, tm)) {
      r = minlen + (from.getRank() - fm.getRank()) - (to.getRank() - tm.getRank());
    } else {
      // If primitive line's to is in child cluster
      r = minlen - (to.getRank() - tm.getRank());
      fm = from;
    }

    /*
     * If primitive minlen minus the value that original node level minus the smallest level in the cluster is larger than zero
     *
     * 1.The line(aux -> from)'s minlen is primitive minlen add the value that original node level minus the smallest level in the cluster
     * 2.The line(aux -> to)'s minlen is zero
     */
    if (r > 0) {
      digraph.addEdge(new DLine(aux, fm, null, weight * 10, 0));
      digraph.addEdge(new DLine(aux, tm, null, weight, r));
    } else {
      digraph.addEdge(new DLine(aux, fm, null, weight * 10, -r));
      digraph.addEdge(new DLine(aux, tm, null, weight, 0));
    }
  }

  private boolean needReverse(MergeNode fromMergeNode, MergeNode toMergeNode) {
    return (fromMergeNode != null && fromMergeNode.shouldNoOutDegree())
        || (toMergeNode != null && toMergeNode.shouldNoInDegree());
  }

  private RankTemp findMinRank(RankContent rankContent) {
    RankNode rankNode = rankContent.get(rankContent.minRank);
    while (rankNode != null) {
      for (DNode dNode : rankNode) {
        if (!dNode.isVirtual()) {
          return new RankTemp(dNode, rankNode.rankIndex());
        }
      }
      rankNode = rankNode.next;
    }

    throw new IllegalArgumentException("RankContent can not find right min rank node");
  }

  private GraphContainer findCurrentContainerDirectContain(DNode node) {
    Graphviz graphviz = dotAttachment.getDrawGraph().getGraphviz();

    GraphContainer c = node.getContainer();

    if (c == graphContainer) {
      return c;
    }

    GraphContainer father = graphviz.effectiveFather(c);

    while (father != null && father != graphContainer) {
      c = father;
      father = graphviz.effectiveFather(c);
    }

    return c;
  }

  private boolean isDirect(DNode fm, DNode tm) {
    return fm == null && tm == null;
  }

  private boolean needIgnore(DNode fm, DNode tm) {
    if (fm == null || tm == null
        || fm.getContainer() == graphContainer
        || tm.getContainer() == graphContainer) {
      return false;
    }

    GraphContainer cp = DotAttachment.commonParent(dotAttachment.getGraphviz(), fm, tm);
    return cp != graphContainer;
  }

  private boolean inDiffChildCluster(DNode fm, DNode tm) {
    return fm != null && tm != null && fm != tm;
  }

  // ---------------------------------------- Object method ----------------------------------------

  private static class SubRankInfo {

    private final List<Cluster> clusters;

    private final SubgraphMerge subgraphMerge;

    private final Map<GraphContainer, RankTemp> clusterMerge;

    public SubRankInfo(List<Cluster> clusters,
                       SubgraphMerge subgraphMerge,
                       Map<GraphContainer, RankTemp> clusterMerge) {
      this.clusters = clusters;
      this.subgraphMerge = subgraphMerge;
      this.clusterMerge = clusterMerge;
    }
  }

  private static class RankTemp {

    private final DNode mergeNode;

    private final int minRank;

    RankTemp(DNode mergeNode, int minRank) {
      this.mergeNode = mergeNode;
      this.minRank = minRank;
    }
  }
}
