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

import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.Asserts;

/**
 * Node coordinate settings, if container have clusters, the strategy of calculating the child
 * cluster first, and then treating the child cluster as a node in the parent cluster. This will
 * make some long line segments appear extra "curved", because if the from and to of the long line
 * segment exist in different containers, the "virtual vertex" will avoid entering the parent
 * container, which will make the line segment appear to be curved.
 */
class Coordinate extends AbstractCoordinate {

  private ClusterNode clusterNode;

  private DotDigraph auxDotDigraph;

  public Coordinate(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
                    EdgeDedigraph<DNode, DLine> proxyDigraph) {
    super(nslimit, rankContent, dotAttachment, proxyDigraph);

    // Auxiliary graph network simplex method to set the level
    auxGraphNetworkSimplex();

    // Final x coordinate setting
    positive();

    // help gc
    clear();
  }

  @Override
  protected double nodeLeftLimit(DNode node) {
    double horMargin = getHorMargin((node));
    DNode pre = node.isVirtual()
        ? rankContent.rankPreNode(node)
        : getClusterNodeIfPresent(node.getContainer(), rankContent.rankPreNode(node));

    double minX = pre != null
        ? pre.getAuxRank() + pre.rightWidth() + node.getNodeSep() + node.leftWidth() + horMargin
        : -Double.MAX_VALUE;

    if (!node.isVirtual() && clusterNode != null && node.getContainer().isCluster()) {
      DNode cn = clusterNode.getNode(node.getContainer());
      double margin = flipGetMargin(node.getContainer(), true, true);
      if (cn != null) {
        minX = Math.max(minX, cn.getAuxRank() - cn.leftWidth() + node.leftWidth() + margin);
      }
    }

    return minX;
  }

  @Override
  protected double nodeRightLimit(DNode node) {
    double horMargin = getHorMargin((node));
    DNode next = node.isVirtual()
        ? rankContent.rankNextNode(node)
        : getClusterNodeIfPresent(node.getContainer(), rankContent.rankNextNode(node));

    double maxX = next != null
        ? next.getAuxRank() - next.leftWidth() - node.getNodeSep() - node.rightWidth() - horMargin
        : Double.MAX_VALUE;

    if (!node.isVirtual() && clusterNode != null && node.getContainer().isCluster()) {
      DNode cn = clusterNode.getNode(node.getContainer());
      double margin = flipGetMargin(node.getContainer(), false, true);
      if (cn != null) {
        maxX = Math.min(maxX, cn.getAuxRank() + cn.rightWidth() - node.rightWidth() - margin);
      }
    }

    return maxX;
  }

  @Override
  protected double containerLeftBorder(GraphContainer container) {
    DNode node = clusterNode.getNode(container);
    return node.getAuxRank() - node.leftWidth();
  }

  @Override
  protected double containerRightBorder(GraphContainer container) {
    DNode node = clusterNode.getNode(container);
    return node.getAuxRank() + node.rightWidth();
  }

  @Override
  protected void nodeConsumer(DNode node) {
    // Mode switch
    node.switchAuxModel();
    // Auxiliary edge addition across hierarchical edges
    crossRankAuxEdge(auxDotDigraph, node);
    // Add auxiliary edges of vertices at the same level
    sameRankAuxEdge(auxDotDigraph, node);
    // Avoid separate nodes
    auxDotDigraph.add(node);
  }

  // ----------------------------------------------------- private method -----------------------------------------------------

  private DotDigraph createAuxGraph() {
    if (dotAttachment.haveClusters()) {
      Graphviz graphviz = dotAttachment.getGraphviz();
      auxDotDigraph = new ClusterDotDigraph(graphviz.directNodes().size());
    } else {
      auxDotDigraph = new DotDigraph(proxyDigraph.vertexNum());
    }

    return auxDotDigraph;
  }

  private void crossRankAuxEdge(DotDigraph auxDotDigraph, DNode node) {
    DNode[] nodes = {null, null};

    for (DLine dLine : proxyDigraph.outAdjacent(node)) {
      dLine.from().switchNormalModel();
      dLine.to().switchNormalModel();
      if (dLine.from().getRank() == dLine.to().getRank()) {
        DNode from = dLine.from().getRankIndex() < dLine.to().getRankIndex()
            ? dLine.from() : dLine.to();
        DNode to = dLine.other(from);

        if (from.getContainer() == to.getContainer()) {
          sameRankLine(auxDotDigraph, (int) (from.getNodeSep() * dLine.limit()),
                       false, from, to, 1);
        } else {
          clusterSameRank(auxDotDigraph, from, to, 1);
        }
        continue;
      }

      double weight = dLine.weight();
      DNode other = dLine.other(node);

      clusterNodeReplace(node, other, nodes);

      nodes[0].switchAuxModel();
      nodes[1].switchAuxModel();
      if (nodes[0] == node && nodes[1] != other) {
        weight = -1;
      }
      DNode auxNode = new DNode(null, 1, 1, dLine.from().getNodeSep());
      auxNode.setContainer(nodes[0].getContainer());
      auxNode.switchAuxModel();

      if (nodes[0].isVirtual() && nodes[1].isVirtual()) {
        weight *= 4;
      }

      DLine e1;
      DLine e2;
      if (!dLine.isVirtual()) {
        int limit = crossLineLimit(dLine);

        if (limit < 0) {
          e1 = new DLine(auxNode, node, null, null, weight, -limit);
          e2 = new DLine(auxNode, other, null, null, weight, 0);
        } else {
          e1 = new DLine(auxNode, node, null, null, weight, 0);
          e2 = new DLine(auxNode, other, null, null, weight, limit);
        }

        if (limit != 0) {
          node.markNotAdjustMid();
          other.markNotAdjustMid();
        }
      } else {
        e1 = new DLine(auxNode, node, null, null, weight, 0);
        e2 = new DLine(auxNode, other, null, null, weight, 0);
      }

      auxDotDigraph.addEdge(e1);
      auxDotDigraph.addEdge(e2);
    }
  }

  private void sameRankAuxEdge(DotDigraph auxDotDigraph, DNode node) {
    node.switchNormalModel();
    DNode other = rankContent.rankNextNode(node);
    node.switchAuxModel();

    if (other == null) {
      return;
    }

    node.switchNormalModel();
    other.switchNormalModel();

    if (other.getRank() != node.getRank()) {
      return;
    }

    if (node.getContainer() == other.getContainer()) {
      sameRankLine(auxDotDigraph, (int) node.getNodeSep(), false, node, other, 0);
      return;
    }

    clusterSameRank(auxDotDigraph, node, other, 0);
  }

  private void clusterSameRank(DotDigraph auxDotDigraph, DNode node, DNode other, double weight) {
    node.switchAuxModel();
    other.switchAuxModel();
    GraphContainer commonParent = dotAttachment.commonParent(node, other);
    int limit = (int) node.getNodeSep();

    if (other.isVirtual() && node.getContainer() != other.getContainer()) {
      limit = 0;
    }

    if (commonParent == node.getContainer()) {
      GraphContainer container = dotAttachment.clusterDirectContainer(commonParent, other);
      DNode n = clusterNode().getNodeOrCreate(node, commonParent, container);
      sameRankLine(auxDotDigraph, limit, true, node, n, weight);
    } else if (commonParent == other.getContainer()) {
      GraphContainer container = dotAttachment.clusterDirectContainer(commonParent, node);
      DNode n = clusterNode().getNodeOrCreate(other, commonParent, container);
      sameRankLine(auxDotDigraph, limit, true, n, other, weight);
    } else {
      GraphContainer c1 = dotAttachment.clusterDirectContainer(commonParent, node);
      GraphContainer c2 = dotAttachment.clusterDirectContainer(commonParent, other);
      sameRankLine(auxDotDigraph, limit, true,
                   clusterNode().getNodeOrCreate(node, commonParent, c1),
                   clusterNode().getNodeOrCreate(other, commonParent, c2), weight);
    }
  }

  private void clusterNodeReplace(DNode node, DNode other, DNode[] nodes) {
    node.switchAuxModel();
    other.switchAuxModel();
    nodes[0] = node;
    nodes[1] = other;

    if (node.getContainer() == other.getContainer()) {
      return;
    }
    GraphContainer commonParent = dotAttachment.commonParent(node, other);

    if (commonParent == node.getContainer()) {
      GraphContainer container = dotAttachment.clusterDirectContainer(commonParent, other);
      nodes[1] = clusterNode().getNodeOrCreate(node, commonParent, container);
    } else if (commonParent == other.getContainer()) {
      GraphContainer container = dotAttachment.clusterDirectContainer(commonParent, node);
      nodes[0] = clusterNode().getNodeOrCreate(other, commonParent, container);
    } else {
      GraphContainer c1 = dotAttachment.clusterDirectContainer(commonParent, node);
      GraphContainer c2 = dotAttachment.clusterDirectContainer(commonParent, other);
      nodes[0] = clusterNode().getNodeOrCreate(node, commonParent, c1);
      nodes[1] = clusterNode().getNodeOrCreate(other, commonParent, c2);
    }

    nodes[0].switchAuxModel();
    nodes[1].switchAuxModel();
  }

  private void sameRankLine(DotDigraph auxDotDigraph, int minLen, boolean realTimeLimit,
                            DNode node, DNode other, double weight) {
    node.switchAuxModel();
    other.switchAuxModel();

    int limit = minLen;
    if (!realTimeLimit) {
      limit = (int) (node.rightWidth() + 1) + minLen + (int) (other.leftWidth() + 1);
    }

    DLine line = new DLine(node, other, weight, limit, realTimeLimit);
    auxDotDigraph.addEdge(line);
  }

  private void auxGraphNetworkSimplex() {
    createAuxGraph();

    accessNodes();

    if (auxDotDigraph instanceof ClusterDotDigraph) {
      ClusterDotDigraph clusterDotDigraph = (ClusterDotDigraph) auxDotDigraph;
      Map<GraphContainer, ContainerBorder> clusterBorderMap = new HashMap<>();

      // Recursive cluster network simplex
      clusterNetworkSimplex(dotAttachment.getGraphviz(), clusterDotDigraph, clusterBorderMap);
      // Calculate the offset of the cluster relative to the root container
      calcClusterOffset(0, dotAttachment.getGraphviz(), clusterBorderMap);
      // According to the offset, merge multiple clusters into the root container one by one
      mergeCluster(clusterBorderMap);
    } else {
      networkSimplex(auxDotDigraph);
    }
  }

  private ContainerBorder clusterNetworkSimplex(GraphContainer container,
                                                ClusterDotDigraph clusterDotDigraph,
                                                Map<GraphContainer, ContainerBorder> clusterBorderMap) {
    DotDigraph dotDigraph = clusterDotDigraph.getDotDigraph(container);

    for (Cluster cluster : DotAttachment.clusters(container)) {
      ContainerBorder clusterHorRange = clusterNetworkSimplex(cluster, clusterDotDigraph,
                                                              clusterBorderMap);

      DNode cn = clusterNode.getNode(cluster);
      cn.setWidth(clusterHorRange.width());
    }

    RankContent rc = networkSimplex(dotDigraph);
    ContainerBorder clusterBorder = new ContainerBorder();
    clusterBorderMap.put(container, clusterBorder);

    DNode min = findMaxWidthNode(rc.get(rc.minRank()));
    DNode max = findMaxWidthNode(rc.get(rc.maxRank()));

    clusterBorder.min = min.getAuxRank() - (int) min.leftWidth();
    clusterBorder.max = max.getAuxRank() + (int) max.rightWidth();

    if (container.isCluster()) {
      if (needFlip) {
        clusterBorder.min -= flipGetMargin(container, true, true);
        clusterBorder.max += flipGetMargin(container, false, true);
      } else {
        double horMargin = getHorMargin((Cluster) container, clusterBorder);
        clusterBorder.min -= horMargin;
        clusterBorder.max += horMargin;
      }
    }

    return clusterBorder;
  }

  private double getHorMargin(Cluster container, ContainerBorder clusterBorder) {
    DrawGraph drawGraph = dotAttachment.getDrawGraph();

    ClusterDrawProp clusterDrawProp = drawGraph.getClusterDrawProp(container);
    FlatPoint labelSize = clusterDrawProp.getLabelSize();

    if (labelSize == null) {
      return clusterDrawProp.getHorMargin();
    }

    int width = clusterBorder.width();
    return Math.max(clusterDrawProp.getHorMargin(), (labelSize.getWidth() - width) / 2);
  }

  private DNode findMaxWidthNode(RankNode rankNode) {
    DNode max = rankNode.get(0);
    for (int i = 1; i < rankNode.size(); i++) {
      DNode node = rankNode.get(i);
      if (node.getWidth() > max.getWidth()) {
        max = node;
      }
    }

    return max;
  }

  private void calcClusterOffset(int offset, GraphContainer container,
                                 Map<GraphContainer, ContainerBorder> clusterBorderMap) {
    for (Cluster c : DotAttachment.clusters(container)) {
      DNode childNode = clusterNode.getNode(c);
      ContainerBorder childWidth = clusterBorderMap.get(c);
      int left = childNode.getAuxRank() - (int) childNode.leftWidth() + offset;
      int clusterOffset = childNode.getAuxRank() - (int) childNode.leftWidth() - childWidth.min;

      childWidth.newMin = left;
      childNode.setAuxRank(left + (int) childNode.leftWidth());
      calcClusterOffset(clusterOffset, c, clusterBorderMap);
    }
  }

  private void mergeCluster(Map<GraphContainer, ContainerBorder> clusterBorderMap) {
    if (clusterNode == null) {
      return;
    }

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        if (node.getContainer().isGraphviz()) {
          continue;
        }

        ContainerBorder clusterBorder = clusterBorderMap.get(node.getContainer());
        node.setAuxRank(node.getAuxRank() + clusterBorder.newMin - clusterBorder.min);
      }
    }
  }

  private DNode getClusterNodeIfPresent(GraphContainer container, DNode node) {
    if (node == null
        || node.isVirtual()
        || clusterNode == null
        || !node.getContainer().isCluster()
        || node.getContainer() == container
        || !dotAttachment.notContain(node.getContainer(), container)) {
      return node;
    }

    return clusterNode.getNode(node.getContainer());
  }

  private ClusterNode clusterNode() {
    if (clusterNode == null) {
      clusterNode = new ClusterNode();
    }
    return clusterNode;
  }

  private void clear() {
    clusterNode = null;
    auxDotDigraph = null;
  }

  private class ClusterNode {

    private Map<Cluster, DNode> clusterDNodeMap;

    DNode getNodeOrCreate(DNode n, GraphContainer father,
                          GraphContainer container) {
      if (!container.isCluster()) {
        return n;
      }
      return clusterLimitMap().computeIfAbsent((Cluster) container, c -> {
        DNode node = new DNode(null, 0, 1, 0);
        node.setContainer(father);
        node.switchAuxModel();
        return node;
      });
    }

    DNode getNode(GraphContainer container) {
      if (clusterDNodeMap == null || !container.isCluster()) {
        throw new IllegalArgumentException("Can not get any cluster node");
      }

      DNode node = clusterDNodeMap.get(container);
      Asserts.illegalArgument(node == null, "Find null cluster node");
      return node;
    }

    private Map<Cluster, DNode> clusterLimitMap() {
      if (clusterDNodeMap == null) {
        clusterDNodeMap = new HashMap<>();
      }
      return clusterDNodeMap;
    }
  }

  private static class ClusterDotDigraph extends DotDigraph {

    private static final long serialVersionUID = -7097358089045601860L;

    private Map<Cluster, DotDigraph> clusterGraph;

    ClusterDotDigraph(int capacity) {
      super(capacity > 0 ? capacity : 1);
    }

    @Override
    public boolean add(DNode node) {
      if (!node.getContainer().isCluster()) {
        return super.add(node);
      }

      return childGraph(node.getContainer()).add(node);
    }

    @Override
    public void addEdge(DLine edge) {
      DNode from = edge.from();
      if (!from.getContainer().isCluster()) {
        super.addEdge(edge);
      } else {
        childGraph(from.getContainer()).addEdge(edge);
      }
    }

    DotDigraph getDotDigraph(GraphContainer container) {
      if (clusterGraph == null) {
        return null;
      }

      if (container.isGraphviz()) {
        return this;
      }

      return clusterGraph.get(container);
    }

    private DotDigraph childGraph(GraphContainer container) {
      if (clusterGraph == null) {
        clusterGraph = new HashMap<>();
      }

      return clusterGraph.computeIfAbsent(
          (Cluster) container,
          c -> new DotDigraph(container.directNodes().size())
      );
    }
  }
}
