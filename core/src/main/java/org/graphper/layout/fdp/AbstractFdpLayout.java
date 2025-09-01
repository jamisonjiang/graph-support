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

package org.graphper.layout.fdp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Assemble;
import org.graphper.api.Cluster;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.AbstractLayoutEngine;
import org.graphper.layout.LayoutAttach;
import org.graphper.layout.LineHelper;
import org.graphper.layout.LineRouter;
import org.graphper.layout.ShifterStrategy;
import org.graphper.layout.fdp.FdpGraph.AreaGraph;
import org.graphper.layout.fdp.OrthogonalRouter.OrthogonalRouterFactory;
import org.graphper.layout.fdp.PolylineRouter.PolylineRouterFactory;
import org.graphper.layout.fdp.RoundedRouter.RoundedRouterFactory;
import org.graphper.layout.fdp.SplineRouter.SplineRouterFactory;
import org.graphper.layout.fdp.StraightLineRouter.StraightLineRouterFactory;
import org.graphper.util.FontUtils;

/**
 * Abstract implementation of a force-directed placement (FDP) layout algorithm.
 * <p>
 * This class provides a flexible and extensible framework for laying out graphs using a combination
 * of force-directed techniques and cluster-based hierarchy handling. It handles the following
 * stages in the graph layout process:
 * </p>
 * <ol>
 *   <li>Collapsing clusters into proxy nodes for hierarchical representation.</li>
 *   <li>Initializing node positions to provide a good starting point for the layout algorithm.</li>
 *   <li>Use specific fdp layout policy to calculate coordinate.</li>
 *   <li>Decreasing node overlap to improve clarity if required.</li>
 * </ol>
 *
 * @author Jamison Jiang
 */
abstract class AbstractFdpLayout extends AbstractLayoutEngine implements Serializable {

  /**
   * Spline router factory
   */
  private static final List<LineRouterFactory<?>> SPLINES_HANDLERS;

  static {
    SPLINES_HANDLERS = Arrays.asList(new StraightLineRouterFactory(), new OrthogonalRouterFactory(),
                                     new PolylineRouterFactory(), new RoundedRouterFactory(),
                                     new SplineRouterFactory());
  }

  /**
   * Abstract method to be implemented by subclasses to define the force-directed layout algorithm.
   *
   * @param graph       the graph to layout
   * @param iterations  the maximum number of iterations for the layout process
   * @param temperature the initial temperature for the force-directed algorithm
   * @param k           the ideal edge length factor
   * @param width       the width of the layout area
   * @param height      the height of the layout area
   */
  protected abstract void fdpLayout(AreaGraph graph, int iterations, double temperature,
                                    double k, double width, double height);

  @Override
  protected List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph) {
    return null;
  }

  @Override
  protected LayoutAttach attachment(DrawGraph drawGraph) {
    Map<Node, FNode> nodeRecord = new HashMap<>(drawGraph.getGraphviz().nodeNum());
    FdpGraph fdpGraph = new FdpGraph(drawGraph.getGraphviz().nodeNum(),
                                     drawGraph.getGraphviz(), nodeRecord);

    return new FdpAttachment(fdpGraph, drawGraph);
  }

  @Override
  protected void consumerNode(Node node, LayoutAttach attachment, DrawGraph drawGraph,
                              GraphContainer parentContainer) {
    FdpAttachment fdpAttachment = (FdpAttachment) attachment;

    FdpGraph fdpGraph = fdpAttachment.getFdpGraph();
    FNode fn = fdpGraph.getNode(node);
    if (fn == null) {
      NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node);
      fn = new FNode(nodeDrawProp);
      double width = drawGraph.width(node);
      double height = drawGraph.height(node);
      fn.setWidth(width);
      fn.setHeight(height);
      fn.setNodeSep(drawGraph.getGraphviz().graphAttrs().getNodeSep());
    }

    parentContainer = fdpGraph.add(fn, parentContainer);
    if (parentContainer.isCluster()) {
      fdpAttachment.markHaveCluster();
    }
  }

  @Override
  protected void consumerLine(Line line, LayoutAttach attachment, DrawGraph drawGraph) {
    FdpAttachment fdpAttachment = (FdpAttachment) attachment;
    FdpGraph fdpGraph = fdpAttachment.getFdpGraph();

    FNode source = fdpGraph.getNode(line.tail());
    FNode target = fdpGraph.getNode(line.head());

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    Double weight = lineDrawProp.lineAttrs().getWeight();

    FlatPoint size = null;
    Assemble assemble = lineDrawProp.getAssemble();
    if (assemble != null) {
      size = assemble.size();
    } else {
      LineAttrs lineAttrs = lineDrawProp.lineAttrs();
      if (StringUtils.isNotEmpty(lineAttrs.getLabel())) {
        Double fontSize = lineAttrs.getFontSize();
        size = FontUtils.measure(lineAttrs.getLabel(), lineAttrs.getFontName(),
                                 fontSize != null ? fontSize : 0, 0);
      }
    }
    lineDrawProp.setLabelSize(size);

    FLine fLine;
    if (weight != null) {
      fLine = new FLine(source, target, weight, lineDrawProp);
    } else {
      fLine = new FLine(source, target, lineDrawProp);
    }

    if (fLine.isSelf()) {
      source.addSelfLine(lineDrawProp);
    }
    fdpGraph.addEdge(fLine);
  }

  @Override
  protected void afterRenderShifter(LayoutAttach attach) {
    FdpAttachment fdpAttachment = (FdpAttachment) attach;
    fdpAttachment.getDrawGraph().syncGraphvizBorder();
    DrawGraph drawGraph = fdpAttachment.getDrawGraph();

    for (ClusterDrawProp cluster : drawGraph.clusters()) {
      Assemble assemble = cluster.getAssemble();
      setCellNodeOffset(drawGraph, cluster.getLabelCenter(), assemble, true);
    }

    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    Assemble assemble = graphvizDrawProp.getAssemble();
    if (assemble != null) {
      setCellNodeOffset(drawGraph, graphvizDrawProp.getLabelCenter(), assemble, true);
    }

    // Lines clip
    new LineClipProcessor(drawGraph, fdpAttachment.getFdpGraph()).clipAllLines();

    for (LineDrawProp line : drawGraph.lines()) {
      FlatPoint labelCenter = line.getLabelCenter();
      if (labelCenter == null) {
        if (line.isBesselCurve()) {
          labelCenter = LineHelper.curveGetFloatLabelStart(null, 0.5, line);
        } else {
          labelCenter = LineHelper.straightGetFloatLabelStart(null, 0.5, line);
        }
        line.setLabelCenter(labelCenter);
      }

      setCellNodeOffset(drawGraph, labelCenter, line.getAssemble(), true);
      FlatPoint size = line.getLabelSize();
      if (size == null || labelCenter == null) {
        continue;
      }

      double width = size.getWidth() / 2;
      double height = size.getHeight() / 2;
      drawGraph.updateXAxisRange(labelCenter.getX() - width);
      drawGraph.updateXAxisRange(labelCenter.getX() + width);
      drawGraph.updateYAxisRange(labelCenter.getY() - height);
      drawGraph.updateYAxisRange(labelCenter.getY() + height);
    }
  }

  @Override
  protected void layout(DrawGraph drawGraph, LayoutAttach attach) {
    FdpAttachment fdpAttachment = (FdpAttachment) attach;
    FdpGraph graph = fdpAttachment.getFdpGraph();

    if (fdpAttachment.haveClusters()) {
      Map<Cluster, ClusterNode> clusterNode = new HashMap<>();
      layout(fdpAttachment, graph.getGraphviz(), clusterNode);
    } else {
      layout(fdpAttachment.getDrawGraph(), graph.getGraph(), graph.getGraphviz());
    }

    applyGraphInfo(drawGraph, fdpAttachment);
  }

  private void layout(AreaGraph graph, DrawGraph drawGraph) {
    int width = 200;
    int height = 200;
    int vertexCount = graph.vertexNum();
    int edgeCount = Math.max(1, graph.edgeNum());
    GraphAttrs graphAttrs = drawGraph.getGraphviz().graphAttrs();
    int iterations = graphAttrs.getMaxiter();
    double temperature = width / (double) vertexCount;
    double k = Math.sqrt(
        (width * height) * graphAttrs.getK() * edgeCount / (vertexCount * vertexCount));

    initPos(graph, drawGraph, iterations, width, height);
    fdpLayout(graph, iterations, temperature, k, width, height);
    tryDecreaseDensity(graph, graphAttrs);
    refreshGraph(graph);
  }

  private AreaGraph layout(FdpAttachment attachment, GraphContainer container,
                           Map<Cluster, ClusterNode> clusterNode) {
    FdpGraph graph = attachment.getFdpGraph();
    AreaGraph proxyGraph = new AreaGraph(graph.vertexNum());
    for (FNode node : graph.nodes(container)) {
      if (node.getContainer() == container) {
        proxyGraph.add(node);
      }
    }

    for (Cluster cluster : attachment.clusters(container)) {
      AreaGraph subGraph = layout(attachment, cluster, clusterNode);
      FNode fNode = new FNode(null);
      fNode.setWidth(subGraph.width());
      fNode.setHeight(subGraph.height());
      fNode.setContainer(container);
      proxyGraph.add(fNode);
      clusterNode.put(cluster, new ClusterNode(fNode, subGraph));
    }

    for (Line line : graph.lines(container)) {
      FNode from = attachment.get(line.tail());
      FNode to = attachment.get(line.head());

      GraphContainer fc = attachment.clusterDirectContainer(container, from);
      GraphContainer tc = attachment.clusterDirectContainer(container, to);

      if (fc == tc && fc != null && fc != container) {
        continue;
      }

      boolean hasNodeChanged = false;
      if (fc != container && fc != null) {
        from = clusterNode.get(fc).node;
        hasNodeChanged = true;
      }
      if (tc != container && tc != null) {
        to = clusterNode.get(tc).node;
        hasNodeChanged = true;
      }

      if (hasNodeChanged && graph.adjAlreadyExists(from, to)) {
        continue;
      }

      LineDrawProp lineDrawProp = attachment.getDrawGraph().getLineDrawProp(line);
      Double weight = lineDrawProp.lineAttrs().getWeight();
      FLine fLine;
      if (weight != null) {
        fLine = new FLine(from, to, weight, lineDrawProp);
      } else {
        fLine = new FLine(from, to, lineDrawProp);
      }
      proxyGraph.addEdge(fLine);
      graph.recordAdj(fLine);
    }

    layout(attachment.getDrawGraph(), proxyGraph, container);

    for (Cluster cluster : attachment.clusters(container)) {
      ClusterNode proxyNode = clusterNode.get(cluster);
      double xoffset = proxyNode.xoffset();
      double yoffset = proxyNode.yoffset();

      for (FNode node : graph.nodes(cluster)) {
        node.setX(node.getX() - xoffset);
        node.setY(node.getY() - yoffset);
      }

      alignChildClusters(attachment, cluster, clusterNode, xoffset, yoffset);
    }

    return proxyGraph;
  }

  private void layout(DrawGraph drawGraph, AreaGraph graph, GraphContainer container) {
    int connectNo = 0;
    Map<FNode, Integer> mark = new HashMap<>();
    GraphAttrs graphAttrs = drawGraph.getGraphviz().graphAttrs();

    for (FNode node : graph) {
      if (mark.get(node) != null) {
        continue;
      }

      dfs(graph, node, mark, ++connectNo);
    }

    if (connectNo == 1) {
      layout(graph, drawGraph);
      applyMargin(graph, drawGraph, container);
      return;
    }

    AreaGraph[] areaGraphs = new AreaGraph[connectNo];
    AreaGraph connectGraph = new AreaGraph(connectNo);

    for (FNode node : graph) {
      Integer cn = mark.get(node);
      Objects.requireNonNull(cn);

      int n = cn - 1;
      if (areaGraphs[n] == null) {
        areaGraphs[n] = new AreaGraph(2);
      }
      areaGraphs[n].add(node);
    }

    for (AreaGraph areaGraph : areaGraphs) {
      for (FNode node : areaGraph) {
        for (FLine line : graph.outAdjacent(node)) {
          areaGraph.addEdge(new FLine(line.from(), line.to(), line.getLineDrawProp()));
        }
      }

      layout(areaGraph, drawGraph);
      FNode connectComponent = new FNode(null);
      connectComponent.setWidth(areaGraph.width());
      connectComponent.setHeight(areaGraph.height());
      connectGraph.add(connectComponent);
    }

    initializePositionsGrid(drawGraph, connectGraph, graph.vertexNum(), graph.vertexNum());
    tryDecreaseDensity(connectGraph, graphAttrs);

    int i = 0;
    for (FNode node : connectGraph) {
      AreaGraph componentGraph = areaGraphs[i++];
      double xOffset = componentGraph.getX() - node.getX();
      double yOffset = componentGraph.getY() - node.getY();

      for (FNode n : componentGraph) {
        graph.setNodeLocation(n, n.getX() - xOffset, n.getY() - yOffset);
      }
    }

    applyMargin(graph, drawGraph, container);
  }

  private void alignChildClusters(FdpAttachment fdpAttachment, Cluster cluster,
                                  Map<Cluster, ClusterNode> clusterNode,
                                  double xoffset, double yoffset) {
    ClusterNode proxyNode = clusterNode.get(cluster);
    ClusterDrawProp clusterDrawProp = fdpAttachment.getDrawGraph().getClusterDrawProp(cluster);
    clusterDrawProp.init();
    clusterDrawProp.updateXAxisRange(proxyNode.areaGraph.getLeftBorder() - xoffset);
    clusterDrawProp.updateXAxisRange(proxyNode.areaGraph.getRightBorder() - xoffset);
    clusterDrawProp.updateYAxisRange(proxyNode.areaGraph.getUpBorder() - yoffset);
    clusterDrawProp.updateYAxisRange(proxyNode.areaGraph.getDownBorder() - yoffset);
    proxyNode.areaGraph.initArea();
    proxyNode.areaGraph.updateXAxisRange(clusterDrawProp.getLeftBorder());
    proxyNode.areaGraph.updateXAxisRange(clusterDrawProp.getRightBorder());
    proxyNode.areaGraph.updateYAxisRange(clusterDrawProp.getUpBorder());
    proxyNode.areaGraph.updateYAxisRange(clusterDrawProp.getDownBorder());

    for (Cluster child : fdpAttachment.clusters(cluster)) {
      alignChildClusters(fdpAttachment, child, clusterNode, xoffset, yoffset);
    }
  }

  private void dfs(AreaGraph areaGraph, FNode v, Map<FNode, Integer> mark, int connectNo) {
    if (mark.get(v) != null) {
      return;
    }

    mark.put(v, connectNo);
    for (FLine line : areaGraph.adjacent(v)) {
      FNode w = line.other(v);
      if (mark.get(w) != null) {
        continue;
      }

      dfs(areaGraph, w, mark, connectNo);
    }
  }

  private void applyMargin(AreaGraph areaGraph, DrawGraph drawGraph, GraphContainer container) {
    if (container.isGraphviz()) {
      applyMargin(areaGraph, drawGraph.getGraphvizDrawProp());
      drawGraph.setLeftBorder(areaGraph.getLeftBorder());
      drawGraph.setRightBorder(areaGraph.getRightBorder());
      drawGraph.setUpBorder(areaGraph.getUpBorder());
      drawGraph.setDownBorder(areaGraph.getDownBorder());
    } else if (container.isCluster()) {
      applyMargin(areaGraph, drawGraph.getClusterDrawProp((Cluster) container));
    }
  }

  private void applyMargin(AreaGraph areaGraph, ContainerDrawProp containerDrawProp) {
    double leftMax = containerDrawProp.getHorMargin();
    double rightMax = containerDrawProp.getHorMargin();
    double topMax = containerDrawProp.topLowestHeight();
    double bottomMax = containerDrawProp.bottomLowestHeight();

    FlatPoint labelSize = containerDrawProp.getLabelSize();
    if (labelSize != null) {
      double widthIncr = (labelSize.getWidth() - areaGraph.width()) / 2;
      leftMax = Math.max(widthIncr, leftMax);
      rightMax = Math.max(widthIncr, rightMax);
    }

    ShapePropCalc shapePropCalc = containerDrawProp.shapeProp();
    FlatPoint newSize = shapePropCalc.minContainerSize(areaGraph.height(), areaGraph.width());
    double widthIncr = (newSize.getWidth() - areaGraph.width()) / 2;
    double heightIncr = (newSize.getHeight() - areaGraph.height()) / 2;
    leftMax = Math.max(leftMax, widthIncr);
    rightMax = Math.max(rightMax, widthIncr);
    topMax = Math.max(topMax, heightIncr);
    bottomMax = Math.max(bottomMax, heightIncr);

    areaGraph.updateXAxisRange(areaGraph.getLeftBorder() - leftMax);
    areaGraph.updateXAxisRange(areaGraph.getRightBorder() + rightMax);
    areaGraph.updateYAxisRange(areaGraph.getUpBorder() - topMax);
    areaGraph.updateYAxisRange(areaGraph.getDownBorder() + bottomMax);
  }

  private void initPos(AreaGraph graph, DrawGraph drawGraph,
                       int iterations, int width, int height) {
    GraphAttrs graphAttrs = drawGraph.getGraphviz().graphAttrs();
    switch (graphAttrs.getInitPos()) {
      case GRID:
        initializePositionsGrid(drawGraph, graph, width, height);
        break;
      case CIRCLE:
        initializeCircularLayout(drawGraph, graph, width, height);
        break;
      case SECTOR:
      default:
        initializePositions(drawGraph, graph, width, height);
        break;
    }

    if (iterations > 0) {
      graph.initArea();
    }
  }

  public void initializePositions(DrawGraph drawGraph, AreaGraph graph,
                                  int width, int height) {
    FNode startVertex = null;
    for (FNode n : graph) {
      startVertex = n;
      break;
    }

    if (startVertex == null) {
      return;
    }

    Queue<FNode> queue = new LinkedList<>();
    Set<FNode> visited = new HashSet<>();
    queue.add(startVertex);
    visited.add(startVertex);

    int layer = 0;
    int layerSize = 1;
    int currentLayerSize = 0;
    double angleStep = 2 * Math.PI / graph.vertexNum();
    int radiusStep = Math.min(width, height) / 10;

    while (!queue.isEmpty()) {
      FNode v = queue.poll();
      v.initNodeSizeExpander(drawGraph);
      double angle = currentLayerSize * angleStep;
      int radius = layer * radiusStep;
      graph.setNodeLocation(v, (double) width / 2 + radius * Math.cos(angle),
                            (double) height / 2 + radius * Math.sin(angle));
      currentLayerSize++;
      if (currentLayerSize >= layerSize) {
        layer++;
        layerSize *= 2;
        currentLayerSize = 0;
      }

      for (FLine line : graph.adjacent(v)) {
        FNode u = line.other(v);
        if (visited.contains(u)) {
          continue;
        }
        queue.add(u);
        visited.add(u);
      }
    }
  }

  private void initializePositionsGrid(DrawGraph drawGraph, AreaGraph graph,
                                       int width, int height) {
    int gridSize = (int) Math.ceil(Math.sqrt(graph.vertexNum()));
    double cellWidth = width / (double) gridSize;
    double cellHeight = height / (double) gridSize;

    int i = 0;
    for (FNode v : graph) {
      int row = i / gridSize;
      int col = i % gridSize;
      graph.setNodeLocation(v, col * cellWidth + cellWidth / 2, row * cellHeight + cellHeight / 2);
      v.initNodeSizeExpander(drawGraph);
      i++;
    }
  }

  private static void initializeCircularLayout(DrawGraph drawGraph, AreaGraph graph,
                                               int width, int height) {
    double angleIncrement = 2 * Math.PI / graph.vertexNum();
    int centerX = width / 2;
    int centerY = height / 2;
    int radius = Math.min(width, height) / 3;

    int i = 0;
    for (FNode node : graph) {
      double angle = i * angleIncrement;
      int x = (int) (centerX + radius * Math.cos(angle));
      int y = (int) (centerY + radius * Math.sin(angle));
      graph.setNodeLocation(node, x, y);
      node.initNodeSizeExpander(drawGraph);
      i++;
    }
  }

  void tryDecreaseDensity(AreaGraph graph, GraphAttrs graphAttrs) {
    if (graphAttrs.isOverlap()) {
      return;
    }

    int overlap = overlapNum(graph);
    if (overlap == 0) {
      return;
    }

    double k = graphAttrs.getK();
    int nodeNum = graph.vertexNum();
    int edgeNum = graph.edgeNum();
    double temperature = Graphviz.PIXEL;
    int maxLoopNum = Math.max(graphAttrs.getMaxiter() / 2, 1);
    double expand = graphAttrs.getK() * ((double) edgeNum / nodeNum);
    for (int i = 0; i < 9; i++) {
      double k2 = k * k;
      double xOv = 6 * k2;
      double xNonov = xOv * expand / nodeNum;

      for (int j = 0; j < maxLoopNum; j++) {
        double temp = temperature * (maxLoopNum - j) / maxLoopNum;
        if (temp <= 0) {
          break;
        }

        overlap = adjust(graph, k, temp, xOv, xNonov);
        if (overlap == 0) {
          break;
        }
        xNonov *= 1.1;
      }

      k += k;
    }

    resolveOverlaps(graph);
  }

  private int adjust(AreaGraph graph, double k, double temp, double xOv, double xNonov) {
    for (FNode node : graph) {
      node.setRepulsionX(0);
      node.setRepulsionY(0);
    }

    int overlap = 0;
    for (FNode n = graph.start(); n != null; n = graph.next(n)) {
      for (FNode w = graph.next(n); w != null; w = graph.next(w)) {
        overlap += applyRepulsive(n, w, xOv, xNonov, graph);
      }

      for (FLine edge : graph.outAdjacent(n)) {
        if (edge.isSelf()) {
          continue;
        }

        applyAttractive(n, edge.other(n), k, graph);
      }
    }

    if (overlap == 0) {
      return 0;
    }

    double temp2 = temp * temp;
    for (FNode node : graph) {
      double dispX = node.getRepulsionX();
      double dispY = node.getRepulsionY();
      double len2 = dispX * dispX + dispY * dispY;

      if (len2 < temp2) {
        graph.setNodeLocation(node, node.getX() + dispX, node.getY() + dispY);
      } else {
        double len = Math.sqrt(len2);
        graph.setNodeLocation(node, node.getX() + dispX * temp / len,
                              node.getY() + dispY * temp / len);
      }
    }

    return overlap;
  }

  private double applyRepulsive(FNode p, FNode q, double xOv, double xNonov, AreaGraph areaGraph) {
    double deltaX = q.getX() - p.getX();
    double deltaY = q.getY() - p.getY();
    double dist2 = deltaX * deltaX + deltaY * deltaY;

    if (dist2 == 0) {
      dist2 = 50;
    }

    double force;
    boolean overlap = isOverlapOrNotFixMineLen(p, q, areaGraph);
    if (overlap) {
      force = xOv / dist2;
    } else {
      force = xNonov / dist2;
    }

    q.setRepulsionX(q.getRepulsionX() + deltaX * force);
    q.setRepulsionY(q.getRepulsionY() + deltaY * force);
    p.setRepulsionX(p.getRepulsionX() - deltaX * force);
    p.setRepulsionY(p.getRepulsionY() - deltaY * force);

    return overlap ? 1 : 0;
  }

  private void applyAttractive(FNode p, FNode q, double k, AreaGraph areaGraph) {
    if (isOverlapOrNotFixMineLen(p, q, areaGraph)) {
      return;
    }

    double deltaX = q.getX() - p.getX();
    double deltaY = q.getY() - p.getY();
    double dist = Math.hypot(deltaX, deltaY);
    double din = rad(p) + rad(q);
    double dout = dist - din;
    double force = dout * dout / ((k + din) * dist);

    q.setRepulsionX(q.getRepulsionX() - deltaX * force);
    q.setRepulsionY(q.getRepulsionY() - deltaY * force);
    p.setRepulsionX(p.getRepulsionX() + deltaX * force);
    p.setRepulsionY(p.getRepulsionY() + deltaY * force);
  }

  private double rad(FNode node) {
    return Math.hypot(node.getAreaWidth(), node.getAreaHeight());
  }

  private void resolveOverlaps(AreaGraph graph) {
    boolean overlapResolved;
    for (int iteration = 0; iteration < 100; iteration++) {
      overlapResolved = true;
      for (FNode v : graph) {
        for (FNode u : graph) {
          if (u == v || !isOverlapOrNotFixMineLen(v, u, graph)) {
            continue;
          }

          double deltaX = v.getX() - u.getX();
          double deltaY = v.getY() - u.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

          double overlap = (v.getAreaWidth() / 2.0 + u.getAreaWidth() / 2.0) - Math.abs(deltaX);
          double dx = (deltaX / distance) * overlap / 2.0;
          double dy = (deltaY / distance) * overlap / 2.0;

          graph.setNodeLocation(v, v.getX() + dx, v.getY() + dy);
          graph.setNodeLocation(u, u.getX() - dx, u.getY() - dy);

          if (isOverlapOrNotFixMineLen(v, u, graph)) {
            overlapResolved = false;
          }
        }
      }

      if (overlapResolved) {
        break;
      }
    }
  }

  private int overlapNum(AreaGraph graph) {
    int overlap = 0;

    for (FNode n = graph.start(); n != null; n = graph.next(n)) {
      for (FNode w = graph.next(n); w != null; w = graph.next(w)) {
        if (isOverlapOrNotFixMineLen(n, w, graph)) {
          overlap++;
        }
      }
    }

    return overlap;
  }

  private boolean isOverlapOrNotFixMineLen(FNode n, FNode w, AreaGraph areaGraph) {
    if (n.isOverlap(w)) {
      return true;
    }

    FdpGraph fdpGraph = areaGraph.getFdpGraph();
    if (fdpGraph == null) {
      return false;
    }

    Integer minlen = fdpGraph.maxMinLen(n, w);
    if (minlen == null) {
      return false;
    }
    return n.distanceTo(w) < minlen * Graphviz.PIXEL;
  }

  private static void refreshGraph(AreaGraph graph) {
    if (!graph.isInitStatus()) {
      return;
    }

    for (FNode node : graph) {
      graph.setNodeLocation(node, node.getX(), node.getY());
    }
  }

  private void applyGraphInfo(DrawGraph drawGraph, FdpAttachment attachment) {
    FlatPoint margin = drawGraph.getGraphviz().graphAttrs().getMargin();
    FdpGraph graph = attachment.getFdpGraph();
    for (FNode node : graph) {
      if (node.isVirtual()) {
        continue;
      }

      node.alignCenter();
      LineHelper.selfLoopHandle(node);
      NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node.getNode());
      nodeDrawProp.setLeftBorder(node.getLeftBorder());
      nodeDrawProp.setRightBorder(node.getRightBorder());
      nodeDrawProp.setUpBorder(node.getUpBorder());
      nodeDrawProp.setDownBorder(node.getDownBorder());
      nodeLabelSet(nodeDrawProp, drawGraph, true);

      drawGraph.updateXAxisRange(nodeDrawProp.getLeftBorder() - margin.getWidth());
      drawGraph.updateXAxisRange(nodeDrawProp.getRightBorder() + margin.getWidth());
      drawGraph.updateYAxisRange(nodeDrawProp.getUpBorder() - margin.getHeight());
      drawGraph.updateYAxisRange(nodeDrawProp.getDownBorder() + margin.getHeight());
    }

    for (Cluster cluster : attachment.clusters(drawGraph.getGraphviz())) {
      refreshByClusters(cluster, drawGraph, margin);
    }

    drawGraph.syncToGraphvizBorder();
    containerLabelPos(drawGraph);

    splines(drawGraph, attachment);
  }

  private static void refreshByClusters(Cluster cluster, DrawGraph drawGraph, FlatPoint margin) {
    ClusterDrawProp clusterDrawProp = drawGraph.getClusterDrawProp(cluster);
    drawGraph.updateXAxisRange(clusterDrawProp.getLeftBorder() - margin.getWidth());
    drawGraph.updateXAxisRange(clusterDrawProp.getRightBorder() + margin.getWidth());
    drawGraph.updateYAxisRange(clusterDrawProp.getUpBorder() - margin.getHeight());
    drawGraph.updateYAxisRange(clusterDrawProp.getDownBorder() + margin.getHeight());
  }

  private void splines(DrawGraph drawGraph, FdpAttachment attachment) {
    Splines splines = drawGraph.getGraphviz().graphAttrs().getSplines();
    Map<Line, LineDrawProp> lineDrawPropMap = drawGraph.getLineDrawPropMap();

    if (splines == Splines.NONE || lineDrawPropMap == null) {
      return;
    }

    FdpGraph fdpGraph = attachment.getFdpGraph();
    // spline handler hand out
    for (LineRouterFactory<?> linesHandlerFactory : SPLINES_HANDLERS) {
      if (!linesHandlerFactory.needDeal(drawGraph.getGraphviz())) {
        continue;
      }

      LineRouter lineRouter = linesHandlerFactory.newInstance(drawGraph, fdpGraph);
      lineRouter.route();
    }
  }

  /**
   * Cluster node represent a proxy node for all nodes in this cluster.
   */
  private static class ClusterNode {

    private final FNode node;

    private final AreaGraph areaGraph;

    ClusterNode(FNode node, AreaGraph areaGraph) {
      this.node = node;
      this.areaGraph = areaGraph;
    }

    double xoffset() {
      return areaGraph.getX() - node.getX();
    }

    double yoffset() {
      return areaGraph.getY() - node.getY();
    }
  }
}
