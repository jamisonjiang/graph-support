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

import static org.graphper.layout.LayoutGraph.clusters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.AbstractLayoutEngine;
import org.graphper.layout.LayoutAttach;
import org.graphper.layout.ShifterStrategy;
import org.graphper.layout.fdp.FdpGraph.AreaGraph;

public class FdpLayoutEngine extends AbstractLayoutEngine implements Serializable {

  private static final long serialVersionUID = 4639188492816085348L;

  @Override
  protected List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph) {
    return null;
  }

  @Override
  protected LayoutAttach attachment(DrawGraph drawGraph) {
    Map<Node, FNode> nodeRecord = new HashMap<>(drawGraph.getGraphviz().nodeNum());
    FdpGraph fdpGraph = new FdpGraph(drawGraph.getGraphviz().nodeNum(),
                                     drawGraph.getGraphviz(), nodeRecord);

    return new FdpAttachment(fdpGraph, drawGraph, nodeRecord);
  }

  @Override
  protected void consumerNode(Node node, LayoutAttach attachment, DrawGraph drawGraph,
                              GraphContainer parentContainer) {
    FdpAttachment fdpAttachment = (FdpAttachment) attachment;
    if (parentContainer.isCluster()) {
      fdpAttachment.markHaveCluster();
    }

    FdpGraph fdpGraph = fdpAttachment.getFdpGraph();
    FNode fn = fdpGraph.getNode(node);
    if (fn == null) {
      fn = new FNode(node);
      double width = drawGraph.width(node);
      double height = drawGraph.height(node);
      fn.setWidth(width);
      fn.setHeight(height);
      fn.setNodeAttrs(drawGraph.getNodeDrawProp(node).nodeAttrs());
    }

    fdpGraph.add(fn, parentContainer);
  }

  @Override
  protected void consumerLine(Line line, LayoutAttach attachment, DrawGraph drawGraph) {
    FdpAttachment fdpAttachment = (FdpAttachment) attachment;
    FdpGraph fdpGraph = fdpAttachment.getFdpGraph();

    FNode source = fdpGraph.getNode(line.tail());
    FNode target = fdpGraph.getNode(line.head());

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    Double weight = lineDrawProp.lineAttrs().getWeight();
    FLine fLine;
    if (weight != null) {
      fLine = new FLine(source, target, weight, line);
    } else {
      fLine = new FLine(source, target, line);
    }
    fdpGraph.addEdge(fLine);
  }

  @Override
  protected void afterRenderShifter(LayoutAttach attach) {
    FdpAttachment fdpAttachment = (FdpAttachment) attach;
    fdpAttachment.getDrawGraph().syncGraphvizBorder();
  }

  @Override
  protected void layout(DrawGraph drawGraph, LayoutAttach attach) {
    FdpAttachment fdpAttachment = (FdpAttachment) attach;
    FdpGraph graph = fdpAttachment.getFdpGraph();
    GraphAttrs graphAttrs = drawGraph.getGraphviz().graphAttrs();

    if (fdpAttachment.haveClusters()) {
      Map<Cluster, ClusterNode> clusterNode = new HashMap<>();
      layout(fdpAttachment, graph.getGraphviz(),
             graphAttrs.getMargin(), clusterNode, graphAttrs);
    } else {
      layout(graph.getGraph(), graphAttrs);
    }

    applyGraphInfo(drawGraph, graph);
  }

  private AreaGraph layout(FdpAttachment attachment, GraphContainer container, FlatPoint margin,
                           Map<Cluster, ClusterNode> clusterNode, GraphAttrs graphAttrs) {
    FdpGraph graph = attachment.getFdpGraph();
    AreaGraph proxyGraph = new AreaGraph(graph.vertexNum(), margin);
    for (FNode node : graph.nodes(container)) {
      if (node.getContainer() == container) {
        proxyGraph.add(node);
      }
    }

    for (Cluster cluster : clusters(container)) {
      AreaGraph subGraph = layout(attachment, cluster, cluster.clusterAttrs().getMargin(),
                                  clusterNode, graphAttrs);
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
        fLine = new FLine(from, to, weight, line);
      } else {
        fLine = new FLine(from, to, line);
      }
      proxyGraph.addEdge(fLine);
      graph.recordAdj(from, to);
    }

    layout(proxyGraph, graphAttrs);

    for (Cluster cluster : clusters(container)) {
      ClusterNode proxyNode = clusterNode.get(cluster);
      double xoffset = proxyNode.xoffset();
      double yoffset = proxyNode.yoffset();

      // Nodes repeat access
      for (FNode node : graph.nodes(cluster)) {
        node.setX(node.getX() - xoffset);
        node.setY(node.getY() - yoffset);
      }

      alignChildClusters(attachment.getDrawGraph(), cluster, clusterNode, xoffset, yoffset);
    }

    return proxyGraph;
  }

  private void alignChildClusters(DrawGraph drawGraph, Cluster cluster,
                                  Map<Cluster, ClusterNode> clusterNode,
                                  double xoffset, double yoffset) {
    ClusterNode proxyNode = clusterNode.get(cluster);
    ClusterDrawProp clusterDrawProp = drawGraph.getClusterDrawProp(cluster);
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

    for (Cluster child : clusters(cluster)) {
      alignChildClusters(drawGraph, child, clusterNode, xoffset, yoffset);
    }
  }

  private void layout(AreaGraph graph, GraphAttrs graphAttrs) {
    int width = 200;
    int height = 200;
    int vertexCount = graph.vertexNum();
    int edgeCount = Math.max(1, graph.edgeNum());
    int iterations = graphAttrs.getMaxiter();
    double temperature = width / (double) vertexCount;
    double k = Math.sqrt(
        (width * height) * graphAttrs.getK() * edgeCount / (vertexCount * vertexCount));

    int connectNo = 0;
    Set<FNode> mark = new HashSet<>();
    for (FNode node : graph) {
      if (mark.contains(node)) {
        continue;
      }

      dfs(graph, node, mark, ++connectNo);
    }

    initPos(graph, graphAttrs, iterations, width, height);
    fdpLayout(graph, iterations, temperature, k, width, height);
    tryDecreaseDensity(graph, graphAttrs);
    refreshGraph(graph);
  }

  private void dfs(AreaGraph areaGraph, FNode v, Set<FNode> mark, int connectNo) {
    if (mark.contains(v)) {
      return;
    }

    mark.add(v);
    v.setConnectNo(connectNo);
    for (FLine line : areaGraph.adjacent(v)) {
      FNode w = line.other(v);
      if (mark.contains(w)) {
        continue;
      }

      dfs(areaGraph, w, mark, connectNo);
    }
  }

  private void initPos(AreaGraph graph, GraphAttrs graphAttrs,
                       int iterations, int width, int height) {
    switch (graphAttrs.getInitPos()) {
      case GRID:
        initializePositionsGrid(graph, width, height);
        break;
      case CIRCLE:
        initializeCircularLayout(graph, width, height);
        break;
      case SECTOR:
      default:
        initializePositions(graph, width, height);
        break;
    }

    if (iterations > 0) {
      graph.initArea();
    }
  }

  protected void fdpLayout(AreaGraph graph, int iterations, double temperature,
                           double k, double width, double height) {
    double ksqaure = k * k;
    double gravityStrength = 0.1;

    // Force-directed algorithm
    for (int i = 0; i < iterations; i++) {
      // Calculate repulsive forces
      for (FNode n : graph) {
        n.setRepulsionX(0);
        n.setRepulsionY(0);
        for (FNode t : graph) {
          if (n == t || n.getConnectNo() != t.getConnectNo()) {
            continue;
          }

          double deltaX = n.getX() - t.getX();
          double deltaY = n.getY() - t.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          if (distance > 0) {
            double repulsiveForce = ksqaure / distance;
            n.setRepulsionX(n.getRepulsionX() + (deltaX / distance) * repulsiveForce);
            n.setRepulsionY(n.getRepulsionY() + (deltaY / distance) * repulsiveForce);
          }
        }
      }

      for (FNode n : graph) {
        for (FLine edge : graph.outAdjacent(n)) {
          if (edge.isSelf()) {
            continue;
          }

          FNode from = edge.from();
          FNode to = edge.to();
          double deltaX = from.getX() - to.getX();
          double deltaY = from.getY() - to.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          double localK = k / Math.max(edge.weight(), 1);

          if (distance > 0 && localK > 0) {
            double attractiveForce = (distance * distance) / localK;
            double dispX = (deltaX / distance) * attractiveForce;
            double dispY = (deltaY / distance) * attractiveForce;
            from.setRepulsionX(from.getRepulsionX() - dispX);
            from.setRepulsionY(from.getRepulsionY() - dispY);
            to.setRepulsionX(to.getRepulsionX() + dispX);
            to.setRepulsionY(to.getRepulsionY() + dispY);
          }
        }
      }

      // Apply gravity to pull nodes towards the center
      FlatPoint center = new FlatPoint(width / 2, height / 2);
      for (FNode n : graph) {
        double deltaX = center.getX() - n.getX();
        double deltaY = center.getY() - n.getY();
        n.setRepulsionLocation(n.getRepulsionX() + gravityStrength * deltaX,
                               n.getRepulsionY() + gravityStrength * deltaY);
      }

      // Limit the displacement and update positions
      for (FNode v : graph) {
        double displacement = Math.sqrt(v.getRepulsionX() * v.getRepulsionX()
                                            + v.getRepulsionY() * v.getRepulsionY());
        if (displacement > 0) {
          double x = v.getX() + (v.getRepulsionX() / displacement)
              * Math.min(displacement, temperature);
          double y = v.getY() + (v.getRepulsionY() / displacement)
              * Math.min(displacement, temperature);
          graph.setNodeLocation(v, x, y);
        }
      }
    }
  }

  public void initializePositions(AreaGraph graph, int width, int height) {
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

  private void initializePositionsGrid(AreaGraph graph, int width, int height) {
    int gridSize = (int) Math.ceil(Math.sqrt(graph.vertexNum()));
    double cellWidth = width / (double) gridSize;
    double cellHeight = height / (double) gridSize;

    int i = 0;
    for (FNode v : graph) {
      int row = i / gridSize;
      int col = i % gridSize;
      graph.setNodeLocation(v, col * cellWidth + cellWidth / 2, row * cellHeight + cellHeight / 2);
      i++;
    }
  }

  private static void initializeCircularLayout(AreaGraph graph, int width, int height) {
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
        overlap += applyRepulsive(n, w, xOv, xNonov);
      }

      for (FLine edge : graph.outAdjacent(n)) {
        if (edge.isSelf()) {
          continue;
        }

        applyAttractive(n, edge.other(n), k);
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

  private double applyRepulsive(FNode p, FNode q, double xOv, double xNonov) {
    double deltaX = q.getX() - p.getX();
    double deltaY = q.getY() - p.getY();
    double dist2 = deltaX * deltaX + deltaY * deltaY;

    while (dist2 == 0) {
      deltaX = 5 - Math.random() * 10;
      deltaY = 5 - Math.random() * 10;
      dist2 = deltaX * deltaX + deltaY * deltaY;
    }

    double force;
    boolean overlap = p.isOverlap(q);
    if (overlap) {
      force = xOv / dist2;
    } else {
      force = xNonov / dist2;
    }

    if (p.getConnectNo() != q.getConnectNo()) {
      force /= 10;
    }

    q.setRepulsionX(q.getRepulsionX() + deltaX * force);
    q.setRepulsionY(q.getRepulsionY() + deltaY * force);
    p.setRepulsionX(p.getRepulsionX() - deltaX * force);
    p.setRepulsionY(p.getRepulsionY() - deltaY * force);

    return overlap ? 1 : 0;
  }

  private void applyAttractive(FNode p, FNode q, double k) {
    if (p.isOverlap(q)) {
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
    return Math.hypot(node.getWidth(), node.getHeight());
  }

  private void resolveOverlaps(AreaGraph graph) {
    boolean overlapResolved;
    for (int iteration = 0; iteration < 100; iteration++) {
      overlapResolved = true;
      for (FNode v : graph) {
        for (FNode u : graph) {
          if (u == v || !v.isOverlap(u)) {
            continue;
          }

          double deltaX = v.getX() - u.getX();
          double deltaY = v.getY() - u.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

          double overlap = (v.getWidth() / 2.0 + u.getWidth() / 2.0) - Math.abs(deltaX);
          double dx = (deltaX / distance) * overlap / 2.0;
          double dy = (deltaY / distance) * overlap / 2.0;

          graph.setNodeLocation(v, v.getX() + dx, v.getY() + dy);
          graph.setNodeLocation(u, u.getX() - dx, u.getY() - dy);

          if (v.isOverlap(u)) {
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
        if (n.isOverlap(w)) {
          overlap++;
        }
      }
    }

    return overlap;
  }

  private static void refreshGraph(AreaGraph graph) {
    if (!graph.isInitStatus()) {
      return;
    }

    for (FNode node : graph) {
      graph.setNodeLocation(node, node.getX(), node.getY());
    }
  }

  private void applyGraphInfo(DrawGraph drawGraph, FdpGraph graph) {
    FlatPoint margin = drawGraph.getGraphviz().graphAttrs().getMargin();
    for (FNode node : graph) {
      if (node.empty()) {
        continue;
      }

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

    for (FNode n : graph) {
      for (FLine edge : graph.outAdjacent(n)) {
        if (edge.empty()) {
          continue;
        }

        FNode from = edge.from();
        FNode to = edge.to();
        LineDrawProp line = drawGraph.getLineDrawProp(edge.getLine());
        line.markIsLineSegment();
        line.add(new FlatPoint(from.getX(), from.getY()));
        line.add(new FlatPoint(to.getX(), to.getY()));
      }
    }

    for (Cluster cluster : clusters(drawGraph.getGraphviz())) {
      refreshByClusters(cluster, drawGraph, margin);
    }

    drawGraph.syncToGraphvizBorder();
    containerLabelPos(drawGraph);
  }

  private static void refreshByClusters(Cluster cluster, DrawGraph drawGraph, FlatPoint margin) {
    ClusterDrawProp clusterDrawProp = drawGraph.getClusterDrawProp(cluster);
    drawGraph.updateXAxisRange(clusterDrawProp.getLeftBorder() - margin.getWidth());
    drawGraph.updateXAxisRange(clusterDrawProp.getRightBorder() + margin.getWidth());
    drawGraph.updateYAxisRange(clusterDrawProp.getUpBorder() - margin.getHeight());
    drawGraph.updateYAxisRange(clusterDrawProp.getDownBorder() + margin.getHeight());
  }

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
