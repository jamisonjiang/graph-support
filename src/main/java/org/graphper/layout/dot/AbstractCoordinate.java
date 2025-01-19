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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Labelloc;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.PortHelper;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.EnvProp;

abstract class AbstractCoordinate {

  protected final int nslimit;

  protected final boolean needFlip;

  protected final RankContent rankContent;

  protected final DotAttachment dotAttachment;

  protected final EdgeDedigraph<DNode, DLine> proxyDigraph;

  protected Map<GraphContainer, ContainerBorder> containerRankRange;

  AbstractCoordinate(int nslimit, RankContent rankContent, DotAttachment dotAttachment,
                     EdgeDedigraph<DNode, DLine> proxyDigraph) {
    Objects.requireNonNull(rankContent);
    Objects.requireNonNull(proxyDigraph);
    this.nslimit = nslimit;
    this.rankContent = rankContent;
    this.proxyDigraph = proxyDigraph;
    this.dotAttachment = dotAttachment;
    this.needFlip = dotAttachment.getDrawGraph().needFlip();
  }

  protected void accessNodes() {
    containerRankRange = dotAttachment.haveClusters() ? new HashMap<>() : new HashMap<>(1);

    ContainerBorder containerBorder = new ContainerBorder();
    containerBorder.min = rankContent.minRank();
    containerBorder.max = rankContent.maxRank();
    containerRankRange.put(dotAttachment.getGraphviz(), containerBorder);

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        node.initNodeSizeExpander(dotAttachment.getDrawGraph());
        DNode next = rankContent.rankNextNode(node);
        if (next != null) {
          next.initNodeSizeExpander(dotAttachment.getDrawGraph());
        }
        nodeConsumer(node);

        if (!node.getContainer().isCluster()) {
          continue;
        }

        updateClusterRange(node);
      }
    }

    clusterVerticalMargin(dotAttachment.getGraphviz());
    updateRankSep(true);
  }

  protected void positive() {
    DrawGraph drawGraph = dotAttachment.getDrawGraph();

    // container size parameters
    double rankY = 0;
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double rankMaxHeight = -Double.MAX_VALUE;
    List<DNode> flatNodes = null;

    if (dotAttachment.haveClusters()) {
      double maxTopHeight = 0;
      for (ClusterDrawProp cluster : drawGraph.clusters()) {
        maxTopHeight = Math.max(getVerTopMargin(null, cluster.getCluster()) + 4, maxTopHeight);
      }
      rankY += maxTopHeight;
    }

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        node.switchNormalModel();
        node.setX(node.getAuxRank());

        minX = Math.min(node.getX() - node.leftWidth(), minX);
        maxX = Math.max(node.getX() + node.rightWidth(), maxX);
        rankMaxHeight = Math
            .max((int) node.topHeight() + node.bottomHeight(), rankMaxHeight);

        if (node.haveSelfLine()) {
          for (LineDrawProp selfLine : node.getSelfLines()) {
            if (!selfLine.haveLabel()) {
              continue;
            }

            FlatPoint labelSize = selfLine.getLabelSize();
            rankMaxHeight = Math.max((int) labelSize.getHeight(), rankMaxHeight);
          }
        }

        DLine flatLabelLine = node.getFlatLabelLine();
        if (flatLabelLine != null && flatLabelLine.isSameRank()) {
          if (flatNodes == null) {
            flatNodes = new ArrayList<>();
          }
          flatNodes.add(node);
        }
      }

      rankNode.setStartY(rankY);
      rankNode.setEndY(rankY + rankMaxHeight);
      rankY = rankNode.getEndY() + rankNode.getRankSep();

      rankMaxHeight = -Double.MAX_VALUE;
    }

    if (CollectionUtils.isNotEmpty(flatNodes)) {
      for (DNode flatNode : flatNodes) {
        DNode preNode = rankContent.rankPreNode(flatNode);
        DNode nextNode = rankContent.rankNextNode(flatNode);

        if (preNode != null && nextNode != null
            && preNode.getContainer() == nextNode.getContainer()
            && preNode.getContainer() == flatNode.getContainer()) {
          flatNode.setX(
              (preNode.getX() + preNode.rightWidth() + nextNode.getX() - nextNode.leftWidth()) / 2);
        }
      }
    }

    // graph container size init
    if (!containerSizeInit()) {
      return;
    }

    expandClusterIfLabelOverflow(drawGraph.getGraphviz());
  }

  private void expandClusterIfLabelOverflow(GraphContainer container) {
    for (Cluster cluster : DotAttachment.clusters(container)) {
      ContainerBorder containerBorder = getContainerBorder(cluster);
      Asserts.nullArgument(containerBorder);

      double height = containerBorder.topRankHeight() + containerBorder.bottomRankHeight();
      
      for (int i = containerBorder.min; i < containerBorder.max; i++) {
        RankNode rankNode = rankContent.get(i);

        double rankSep = rankNode.getRankSep();

      }
    }
  }

  protected ContainerDrawProp getContainerDrawProp(GraphContainer container) {
    ContainerDrawProp containerDrawProp;
    if (container.isGraphviz()) {
      containerDrawProp = dotAttachment.getDrawGraph().getGraphvizDrawProp();
    } else if (container.isCluster()) {
      containerDrawProp = dotAttachment.getDrawGraph().getClusterDrawProp((Cluster) container);
    } else {
      throw new IllegalArgumentException(
          "An illegal container type occurred when calculating the top and bottom heights of the container");
    }
    return containerDrawProp;
  }

  protected RankContent networkSimplex(DotDigraph auxGraph, boolean needRankContent) {
    FeasibleTree feasibleTree = new FeasibleTree(auxGraph);
    NetworkSimplex networkSimplex = new NetworkSimplex(feasibleTree, nslimit, false,
                                                       needRankContent, Double.MAX_VALUE, null);
    return networkSimplex.getRankContent();
  }

  protected int crossLineLimit(DLine line) {
    if (line.isVirtual() || line.isSameRank()) {
      return 0;
    }

    DNode from = line.from().getRankIgnoreModel() < line.to().getRankIgnoreModel()
        ? line.from() : line.to();
    DNode to = line.other(from);

    FlatPoint fromPoint = null;
    FlatPoint toPoint = null;
    if (!from.isVirtual()) {
      fromPoint = PortHelper.getPortPoint(line.getLine(), from,
                                          dotAttachment.getDrawGraph());
    }
    if (!to.isVirtual()) {
      toPoint = PortHelper.getPortPoint(line.getLine(), to,
                                        dotAttachment.getDrawGraph());
    }
    return (int) ((fromPoint == null ? 0 : fromPoint.getX())
        - (toPoint == null ? 0 : toPoint.getX()));
  }

  protected int flipGetMargin(GraphContainer container, boolean left, boolean isFlip) {
    if (container == null) {
      return 0;
    }

    ContainerDrawProp containerDrawProp = getContainerDrawProp(container);
    FlatPoint labelSize = containerDrawProp.getLabelSize();
    Labelloc labelloc = null;
    if (container.isGraphviz()) {
      labelloc = ((Graphviz) container).graphAttrs().getLabelloc();
    } else if (container.isCluster()) {
      labelloc = ((Cluster) container).clusterAttrs().getLabelloc();
    }
    if (labelSize == null || labelloc == Labelloc.CENTER) {
      return (int) containerDrawProp.getVerMargin();
    }

    if ((labelloc == Labelloc.BOTTOM) == left) {
      return (int) containerDrawProp.getVerMargin()
          + (isFlip ? (int) labelSize.getWidth() : (int) labelSize.getHeight());
    }

    return (int) containerDrawProp.getVerMargin();
  }

  protected void nodeConsumer(DNode node) {
  }

  protected abstract double containerLeftBorder(GraphContainer container);

  protected abstract double containerRightBorder(GraphContainer container);

  // --------------------------------------------------- private method ---------------------------------------------------

  private void updateRankSep(boolean needIgnoreFlip) {
    if (containerRankRange == null || (needIgnoreFlip && needFlip)) {
      return;
    }

    Map<RankNode, RankTopBottom> rankUpDownHeight = new HashMap<>();
    for (Entry<GraphContainer, ContainerBorder> borderEntry : containerRankRange.entrySet()) {
      ContainerBorder rankRange = borderEntry.getValue();

      RankNode firstPreRank = rankContent.get(rankRange.min - 1);
      RankNode lastRank = rankContent.get(rankRange.max);

      RankTopBottom rtb;
      if (firstPreRank != null) {
        rtb = rankUpDownHeight.computeIfAbsent(firstPreRank, r -> new RankTopBottom());
        rtb.top = Math.max(rtb.top, rankRange.verTopMargin + 10);
      }

      rtb = rankUpDownHeight.computeIfAbsent(lastRank, r -> new RankTopBottom());
      rtb.bottom = Math.max(rtb.bottom, rankRange.verBottomMargin + 10);
    }

    for (Entry<RankNode, RankTopBottom> rankTopBottomEntry : rankUpDownHeight.entrySet()) {
      RankNode rankNode = rankTopBottomEntry.getKey();
      RankTopBottom rankTopBottom = rankTopBottomEntry.getValue();

      rankNode.setRankSep(
          Math.max(
              rankNode.getRankSep(),
              rankTopBottom.top + rankTopBottom.bottom
          )
      );
    }
  }

  private double getVerTopMargin(DNode node, GraphContainer graphContainer) {
    ContainerBorder containerBorder = getContainerBorder(graphContainer);
    if (containerBorder == null) {
      return 0;
    }

    if (node != null && node.getRank() == containerBorder.min) {
      containerBorder.maxTopHeight = Math.max(node.topHeight(), containerBorder.maxTopHeight);
    }
    return containerBorder.verTopMargin;
  }

  private double getVerBottomMargin(DNode node, GraphContainer graphContainer) {
    ContainerBorder containerBorder = getContainerBorder(graphContainer);
    if (containerBorder == null) {
      return 0;
    }

    if (node != null && node.getRank() == containerBorder.max) {
      containerBorder.maxBottomHeight = Math
          .max(node.bottomHeight(), containerBorder.maxBottomHeight);
    }
    return containerBorder.verBottomMargin;
  }

  private ContainerBorder clusterVerticalMargin(GraphContainer graphContainer) {
    double maxTopHeight = 0;
    double maxBottomHeight = 0;

    ContainerDrawProp containerDrawProp = getContainerDrawProp(graphContainer);
    ContainerBorder containerBorder = containerRankRange.computeIfAbsent(
        graphContainer, c -> new ContainerBorder());

    for (Cluster cluster : DotAttachment.clusters(graphContainer)) {
      ContainerBorder childClusterBorder = clusterVerticalMargin(cluster);

      if (containerBorder.min == childClusterBorder.min) {
        maxTopHeight = Math.max(childClusterBorder.verTopMargin, maxTopHeight);
      }

      if (containerBorder.max == childClusterBorder.max) {
        maxBottomHeight = Math.max(childClusterBorder.verBottomMargin, maxBottomHeight);
      }
    }

    if (needFlip) {
      FlatPoint labelSize = containerDrawProp.getLabelSize();
      if (labelSize != null) {
        labelSize.flip();
      }
    } else {
      containerBorder.verTopMargin = maxTopHeight + containerDrawProp.topLowestHeight();
      containerBorder.verBottomMargin = maxBottomHeight + containerDrawProp.bottomLowestHeight();
    }
    return containerBorder;
  }

  private void updateClusterRange(DNode n) {
    if (!n.getContainer().isCluster()) {
      return;
    }

    int rank = n.getRankIgnoreModel();
    Graphviz graphviz = dotAttachment.getGraphviz();
    GraphContainer container = n.getContainer();

    while (container != null && container.isCluster()) {
      ContainerBorder clusterBorder = containerRankRange
          .computeIfAbsent(container, c -> new ContainerBorder());

      clusterBorder.min = Math.min(clusterBorder.min, rank);
      clusterBorder.max = Math.max(clusterBorder.max, rank);

      container = graphviz.effectiveFather(container);
    }
  }

  private void refreshRankStartEnd() {
    double rankY = 0;
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      double height = rankNode.getEndY() - rankNode.getStartY();
      rankNode.setStartY(rankY);
      rankNode.setEndY(rankY + height);
      rankY = rankNode.getEndY() + rankNode.getRankSep();
    }
  }

  private boolean containerSizeInit() {
    DrawGraph drawGraph = dotAttachment.getDrawGraph();

    boolean clusterLabelOverflow = false;
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);

        RankNode rank = rankContent.get(node.getRank());
        double preRankY = rank.getStartY();
        double nextRankY = rank.getEndY() + rankNode.getRankSep();

        // Need an offset to center
        double offset =
            ((nextRankY - preRankY - rankNode.getRankSep()) / 2) - (node.getHeight() / 2);
        node.setY(preRankY + node.realTopHeight() + offset);

        clusterLabelOverflow |= containerAdjust(node, !rankNode.noNormalNode());
        if (!node.isVirtual()) {
          updateNodeContainer(node, drawGraph.getNodeDrawProp(node.getNode()));
        }

        drawGraph.updateXAxisRange(node.getX() - node.leftWidth());
        drawGraph.updateXAxisRange(node.getX() + node.rightWidth());
        drawGraph.updateYAxisRange(node.getY() - node.topHeight());
        drawGraph.updateYAxisRange(node.getY() + node.bottomHeight());
      }
    }

    refreshGraphBorder(drawGraph);
    return clusterLabelOverflow;
  }

  private boolean containerAdjust(DNode node, boolean normalRank) {
    if (!node.getContainer().isCluster()) {
      return false;
    }

    double verTopMargin;
    double verBottomMargin;
    Graphviz graphviz = dotAttachment.getGraphviz();
    DrawGraph drawGraph = dotAttachment.getDrawGraph();
    GraphContainer container = node.getContainer();

    boolean clusterLabelOverflow = false;
    while (container != null && container.isCluster()) {
      ClusterDrawProp clusterDrawProp = drawGraph.getClusterDrawProp((Cluster) container);
      if (clusterDrawProp == null) {
        container = graphviz.effectiveFather(container);
        continue;
      }

      if (normalRank && EnvProp.qualityCheck()) {
        double leftBorder = containerLeftBorder(container);
        double rightBorder = containerRightBorder(container);
        Asserts.illegalArgument(node.getX() < leftBorder || node.getX() > rightBorder,
                                "Node " + node + " not in container " + container.id());
      }

      verTopMargin = getVerTopMargin(node, clusterDrawProp.getCluster());
      verBottomMargin = getVerBottomMargin(node, clusterDrawProp.getCluster());
      updateClusterHorBorder((Cluster) container, clusterDrawProp);
      updateClusterVerBorder(node, clusterDrawProp, verTopMargin, verBottomMargin);
      container = graphviz.effectiveFather(container);

      if (needFlip && !clusterLabelOverflow) {
        FlatPoint labelSize = clusterDrawProp.getLabelSize();
        clusterLabelOverflow = labelSize != null
            && labelSize.getHeight() > clusterDrawProp.getHeight();
      }

      drawGraph.updateXAxisRange(clusterDrawProp.getLeftBorder());
      drawGraph.updateXAxisRange(clusterDrawProp.getRightBorder());
      drawGraph.updateYAxisRange(clusterDrawProp.getUpBorder());
      drawGraph.updateYAxisRange(clusterDrawProp.getDownBorder());
    }

    return clusterLabelOverflow;
  }

  private void updateClusterVerBorder(DNode node, ClusterDrawProp clusterDrawProp,
                                      double verTopMargin, double verBottomMargin) {
    double top = node.getY() - node.topHeight();
    double bottom = node.getY() + node.bottomHeight();

    if (node.haveSelfLine()) {
      for (LineDrawProp selfLine : node.getSelfLines()) {
        if (!selfLine.haveLabel()) {
          continue;
        }
        FlatPoint labelSize = selfLine.getLabelSize();
        top = Math.min(top, node.getY() - labelSize.getHeight() / 2);
        bottom = Math.max(bottom, node.getY() + labelSize.getHeight() / 2);
      }
    }

    clusterDrawProp.setUpBorder(
        Math.min(clusterDrawProp.getUpBorder(), top - verTopMargin - 2)
    );
    clusterDrawProp.setDownBorder(
        Math.max(clusterDrawProp.getDownBorder(), bottom + verBottomMargin + 2)
    );
  }

  private void updateClusterHorBorder(Cluster cluster, ClusterDrawProp clusterDrawProp) {
    double leftBorder = containerLeftBorder(cluster);
    double rightBorder = containerRightBorder(cluster);
    // Left and right width is 2px, fix lost precision when double to int
    clusterDrawProp.setLeftBorder(leftBorder - 2);
    clusterDrawProp.setRightBorder(rightBorder + 2);
  }

  private ContainerBorder getContainerBorder(GraphContainer graphContainer) {
    if (containerRankRange == null) {
      return null;
    }

    return containerRankRange.get(graphContainer);
  }

  private void updateNodeContainer(DNode node, NodeDrawProp nodeDrawProp) {
    if (nodeDrawProp == null) {
      return;
    }

    nodeDrawProp.setLeftBorder(node.getLeftBorder());
    nodeDrawProp.setRightBorder(node.getRightBorder());
    nodeDrawProp.setUpBorder(node.getUpBorder());
    nodeDrawProp.setDownBorder(node.getDownBorder());

    DotLayoutEngine.nodeLabelSet(nodeDrawProp, dotAttachment.getDrawGraph(), false);
  }

  private void refreshGraphBorder(DrawGraph drawGraph) {
    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    double verTopMargin = getVerTopMargin(null, graphvizDrawProp.getGraphviz());
    double verBottomMargin = getVerBottomMargin(null, graphvizDrawProp.getGraphviz());
    drawGraph.updateYAxisRange(drawGraph.getMinY() - verTopMargin);
    drawGraph.updateYAxisRange(drawGraph.getMaxY() + verBottomMargin);

    FlatPoint labelSize = graphvizDrawProp.getLabelSize();
    if (labelSize != null) {
      if (needFlip) {
        Labelloc labelloc = drawGraph.getGraphviz().graphAttrs().getLabelloc();
        if (labelloc == Labelloc.TOP) {
          drawGraph.updateXAxisRange(drawGraph.getMaxX() + labelSize.getWidth());
        } else {
          drawGraph.updateXAxisRange(drawGraph.getMinX() - labelSize.getWidth());
        }
      } else {
        double width = labelSize.getWidth() - drawGraph.width();
        if (width > 0) {
          drawGraph.updateXAxisRange(drawGraph.getMinX() - width / 2);
          drawGraph.updateXAxisRange(drawGraph.getMaxX() + width / 2);
        }
      }
    }

    if (needFlip) {
      drawGraph.updateXAxisRange(drawGraph.getMinX() - graphvizDrawProp.getVerMargin());
      drawGraph.updateXAxisRange(drawGraph.getMaxX() + graphvizDrawProp.getVerMargin());
      drawGraph.updateYAxisRange(drawGraph.getMinY() - graphvizDrawProp.getHorMargin());
      drawGraph.updateYAxisRange(drawGraph.getMaxY() + graphvizDrawProp.getHorMargin());
    } else {
      drawGraph.updateXAxisRange(drawGraph.getMinX() - graphvizDrawProp.getHorMargin());
      drawGraph.updateXAxisRange(drawGraph.getMaxX() + graphvizDrawProp.getHorMargin());
      drawGraph.updateYAxisRange(drawGraph.getMinY() - graphvizDrawProp.getVerMargin());
      drawGraph.updateYAxisRange(drawGraph.getMaxY() + graphvizDrawProp.getVerMargin());
    }
    graphvizDrawProp.setLeftBorder(drawGraph.getMinX());
    graphvizDrawProp.setRightBorder(drawGraph.getMaxX());
    graphvizDrawProp.setUpBorder(drawGraph.getMinY());
    graphvizDrawProp.setDownBorder(drawGraph.getMaxY());
  }

  protected static class ContainerBorder {

    protected int min = Integer.MAX_VALUE;

    protected int max = Integer.MIN_VALUE;

    protected int newMin;

    private double verTopMargin;

    private double verBottomMargin;

    private double maxTopHeight;

    private double maxBottomHeight;

    int width() {
      return max - min;
    }

    double topRankHeight() {
      return verTopMargin + maxTopHeight;
    }

    double bottomRankHeight() {
      return verBottomMargin + maxBottomHeight;
    }
  }

  private static class RankTopBottom {

    private double top;

    private double bottom;
  }
}
