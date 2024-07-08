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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.AbstractLayoutEngine;
import org.graphper.layout.LayoutAttach;
import org.graphper.layout.ShifterStrategy;

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
    FdpGraph fdpGraph = fdpAttachment.getFdpGraph();

    FNode fn = fdpGraph.getNode(node);
    if (fn == null) {
      fn = new FNode(node);
      fn.setContainer(parentContainer);
      double width = drawGraph.width(node);
      double height = drawGraph.height(node);
      fn.setWidth(width);
      fn.setHeight(height);
      fdpGraph.add(fn);
    }

    fn.setNodeAttrs(drawGraph.getNodeDrawProp(node).nodeAttrs());
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

    conComp(graph);

    int width = 200;
    int height = 200;
    int vertexCount = graph.vertexNum();
    int edgeCount = Math.max(1, graph.edgeNum());
    int iterations = graphAttrs.getMaxiter();
    double temperature = width / (double) vertexCount;
    double k = Math.sqrt((width * height) * graphAttrs.getK() * edgeCount / (vertexCount * vertexCount));

    initPos(graph, graphAttrs, width, height);
    fdpLayout(graph, iterations, temperature, k, width, height);
    tryDecreaseDensity(graph);
    applyGraphInfo(drawGraph, graph);
  }

  private void conComp(FdpGraph graph) {
    Set<FNode> visited = new HashSet<>();

    FNode preComFirstNode = null;
    for (FNode n : graph) {
      if (visited.contains(n)) {
        continue;
      }

      if (preComFirstNode != null) {
        graph.addEdge(new FLine(preComFirstNode, n, 1, null));
      }
      preComFirstNode = n;
      dfs(n, graph, visited);
    }
  }

  private void dfs(FNode n, FdpGraph graph, Set<FNode> visited) {
    visited.add(n);

    for (FLine line : graph.adjacent(n)) {
      FNode w = line.other(n);
      if (visited.contains(w)) {
        continue;
      }

      dfs(w, graph, visited);
    }
  }

  private void initPos(FdpGraph graph, GraphAttrs graphAttrs, int width, int height) {
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
  }

  protected void fdpLayout(FdpGraph graph, int iterations, double temperature,
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
          if (n == t) {
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
          v.setX(v.getX() + (v.getRepulsionX() / displacement) * Math.min(displacement, temperature));
          v.setY(v.getY() + (v.getRepulsionY() / displacement) * Math.min(displacement, temperature));
        }
      }
    }
  }

  public void initializePositions(FdpGraph graph, int width, int height) {
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
      v.setLocation((double) width / 2 + radius * Math.cos(angle),
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

  private void initializePositionsGrid(FdpGraph graph, int width, int height) {
    int gridSize = (int) Math.ceil(Math.sqrt(graph.vertexNum()));
    double cellWidth = width / (double) gridSize;
    double cellHeight = height / (double) gridSize;

    int i = 0;
    for (FNode v : graph) {
      int row = i / gridSize;
      int col = i % gridSize;
      v.setLocation(col * cellWidth + cellWidth / 2, row * cellHeight + cellHeight / 2);
      i++;
    }
  }

  // Initialize positions using a circular layout
  private static void initializeCircularLayout(FdpGraph graph, int width, int height) {
    double angleIncrement = 2 * Math.PI / graph.vertexNum();
    int centerX = width / 2;
    int centerY = height / 2;
    int radius = Math.min(width, height) / 3;

    int i = 0;
    for (FNode node : graph) {
      double angle = i * angleIncrement;
      int x = (int) (centerX + radius * Math.cos(angle));
      int y = (int) (centerY + radius * Math.sin(angle));
      node.setX(x);
      node.setY(y);
      i++;
    }
  }

   void tryDecreaseDensity(FdpGraph graph) {
    GraphAttrs graphAttrs = graph.getGraphviz().graphAttrs();
    if (graphAttrs.isOverlap()) {
      return;
    }

    int overlap = overlapNum(graph);
    if (overlap == 0) {
      return;
    }

    double k = graphAttrs.getK();
    int nodeNum = graph.vertexNum();
    double temperature = 72;
    int maxLoopNum = graphAttrs.getMaxiter() / 2;
    for (int i = 0; i < 9; i++) {
      double k2 = k * k;
      double xOv = 1.5 * k2;
      double xNonov = graph.edgeNum() * xOv * 2.0 / (nodeNum * (nodeNum - 1));

      for (int j = 0; j < maxLoopNum; j++) {
        double temp = temperature * (maxLoopNum - j) / maxLoopNum;
        if (temp <= 0) {
          break;
        }

        overlap = adjust(graph, k, temp, xOv, xNonov);
        if (overlap == 0) {
          break;
        }
      }

      k += k;
    }

    resolveOverlaps(graph);
  }

  private int adjust(FdpGraph graph, double k, double temp, double xOv, double xNonov) {
    for (FNode node : graph) {
      node.setRepulsionX(0);
      node.setRepulsionY(0);
    }

    int overlap = 0;
    for (FNode n = graph.start(); n != null; n = graph.next(n)) {
      for (FNode w = graph.next(n); w != null; w = graph.next(w)) {
        overlap += applyRepulsive(n, w, xOv, xNonov);
      }

//      for (FLine edge : graph.outAdjacent(n)) {
//        if (edge.isSelf()) {
//          continue;
//        }
//
//        applyAttractive(n, edge.other(n), k);
//      }
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
        node.setX(node.getX() + dispX);
        node.setY(node.getY() + dispY);
      } else {
        double len = Math.sqrt(len2);
        node.setX(node.getX() + dispX * temp / len);
        node.setY(node.getY() + dispY * temp / len);
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

    double deltaX = p.getX() - q.getX();
    double deltaY = p.getY() - q.getY();
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

  private void resolveOverlaps(FdpGraph graph) {
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

          v.setLocation(v.getX() + dx, v.getY() + dy);
          u.setLocation(u.getX() - dx, u.getY() - dy);

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

  private int overlapNum(FdpGraph graph) {
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

  private static void applyGraphInfo(DrawGraph drawGraph, FdpGraph graph) {
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

      drawGraph.updateXAxisRange(nodeDrawProp.getLeftBorder());
      drawGraph.updateXAxisRange(nodeDrawProp.getRightBorder());
      drawGraph.updateYAxisRange(nodeDrawProp.getUpBorder());
      drawGraph.updateYAxisRange(nodeDrawProp.getDownBorder());
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

    drawGraph.syncToGraphvizBorder();
  }
}
