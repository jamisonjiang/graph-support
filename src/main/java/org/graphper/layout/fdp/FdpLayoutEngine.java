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

    int vertexCount = graph.vertexNum();
    int edgeCount = Math.max(1, graph.edgeNum());
    int width = Math.max(vertexCount * 50, 100);
    int height = width;
    int iterations = graphAttrs.getMaxiter();
    double temperature = width / (double) vertexCount;
    double coolingFactor = 0.95;
    double k = Math.sqrt((width * height) * graphAttrs.getK() * edgeCount / (vertexCount * vertexCount));

    initializePositions(graph, width, height);

    fdpLayout(graph, iterations, temperature, coolingFactor, k);

    if (!graphAttrs.isOverlap()) {
      resolveOverlaps(graph);
    }

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
      for (FLine edge : graph.adjacent(n)) {
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

  protected void fdpLayout(FdpGraph graph, int iterations, double temperature,
                           double coolingFactor, double k) {
    int vertexCount = graph.vertexNum();
    double ksqaure = k * k;
    double edgeK = k / Math.sqrt(vertexCount);

    // Force-directed algorithm
    for (int i = 0; i < iterations; i++) {
      // Calculate repulsive forces
      for (FNode n : graph) {
        n.setDispX(0);
        n.setDispY(0);
        for (FNode t : graph) {
          if (n == t) {
            continue;
          }

          double deltaX = n.getX() - t.getX();
          double deltaY = n.getY() - t.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          if (distance > 0) {
            double repulsiveForce = ksqaure / distance;
            n.setDispX(n.getDispX() + (deltaX / distance) * repulsiveForce);
            n.setDispY(n.getDispY() + (deltaY / distance) * repulsiveForce);
          }
        }
      }

      for (FNode n : graph) {
        for (FLine edge : graph.adjacent(n)) {
          FNode from = edge.from();
          FNode to = edge.to();
          double deltaX = from.getX() - to.getX();
          double deltaY = from.getY() - to.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          double localK = edgeK / Math.max(edge.weight(), 1);

          if (distance > 0 && localK > 0) {
            double attractiveForce = (distance * distance) / localK;
            double dispX = (deltaX / distance) * attractiveForce;
            double dispY = (deltaY / distance) * attractiveForce;
            from.setDispX(from.getDispX() - dispX);
            from.setDispY(from.getDispY() - dispY);
            to.setDispX(to.getDispX() + dispX);
            to.setDispY(to.getDispY() + dispY);
          }
        }
      }

      // Limit the displacement and update positions
      for (FNode v : graph) {
        double displacement = Math.sqrt(v.getDispX() * v.getDispX() + v.getDispY() * v.getDispY());
        if (displacement > 0) {
          v.setX(v.getX() + (v.getDispX() / displacement) * Math.min(displacement, temperature));
          v.setY(v.getY() + (v.getDispY() / displacement) * Math.min(displacement, temperature));
        }
      }

      // Cool down
      temperature *= coolingFactor;
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

  private static void resolveOverlaps(FdpGraph graph) {
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
}
