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
import java.util.List;
import java.util.Map;
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

    FLine fLine = new FLine(source, target, line);
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

    int vertexCount = graph.vertexNum();
    int width = Math.max(vertexCount * 20, 500);
    int height = width;
    int iterations = 100;
    double temperature = width / (double) vertexCount;
    double coolingFactor = 0.95;
    double k = Math.sqrt((width * height) / (double) vertexCount);

    initializePositionsGrid(graph, width, height);

    // Force-directed algorithm
    for (int i = 0; i < iterations; i++) {
      // Calculate repulsive forces
      for (FNode v : graph) {
        v.setDispX(0);
        v.setDispY(0);
        for (FNode u : graph) {
          if (v != u) {
            double deltaX = v.getX() - u.getX();
            double deltaY = v.getY() - u.getY();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > 0) {
              double repulsiveForce = k * k / distance;
              v.setDispX(v.getDispX() + (deltaX / distance) * repulsiveForce);
              v.setDispY(v.getDispY() + (deltaY / distance) * repulsiveForce);
            }
          }
        }
      }

      // Calculate attractive forces
      for (FNode n : graph) {
        for (FLine edge : graph.adjacent(n)) {
          FNode from = edge.from();
          FNode to = edge.to();
          double deltaX = from.getX() - to.getX();
          double deltaY = from.getY() - to.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          if (distance > 0) {
            double attractiveForce = (distance * distance) / k;
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

    for (FNode node : graph) {
      NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node.getNode());
      nodeDrawProp.setLeftBorder(node.getLeftBorder());
      nodeDrawProp.setRightBorder(node.getRightBorder());
      nodeDrawProp.setUpBorder(node.getUpBorder());
      nodeDrawProp.setDownBorder(node.getDownBorder());

      drawGraph.updateXAxisRange(nodeDrawProp.getLeftBorder());
      drawGraph.updateXAxisRange(nodeDrawProp.getRightBorder());
      drawGraph.updateYAxisRange(nodeDrawProp.getUpBorder());
      drawGraph.updateYAxisRange(nodeDrawProp.getDownBorder());
    }


    // Calculate attractive forces
    for (FNode n : graph) {
      for (FLine edge : graph.adjacent(n)) {
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

  @Override
  protected List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph) {
    return null;
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
}
