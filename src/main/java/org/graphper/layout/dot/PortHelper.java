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

import java.util.Objects;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.Rectangle;
import org.graphper.layout.Cell;
import org.graphper.layout.Cell.RootCell;
import org.graphper.layout.FlipShifterStrategy;
import org.graphper.util.Asserts;

public class PortHelper {

  private PortHelper() {
  }

  public static FlatPoint getPortPoint(DNode node, Port port) {
    if (port == null) {
      return new FlatPoint(node.getX(), node.getY());
    }
    return new FlatPoint(node.getX() + port.horOffset(node), node.getY() + port.verOffset(node));
  }

  public static Port getLineEndPointPort(Node node, Line line, DrawGraph drawGraph) {
    return getLineEndPointPort(node, line, drawGraph, true);
  }

  public static Port getLineEndPointPort(Node node, Line line, DrawGraph drawGraph,
                                         boolean needMove) {
    if (node == null || line == null || drawGraph == null) {
      return null;
    }

    LineAttrs lineAttrs = drawGraph.lineAttrs(line);
    Asserts.illegalArgument(lineAttrs == null, "can not find lineAttrs");
    if (node == line.tail()) {
      if (needMove) {
        return FlipShifterStrategy.movePort(drawGraph, lineAttrs.getTailPort());
      }

      return lineAttrs.getTailPort();
    }
    if (node == line.head()) {
      if (needMove) {
        return FlipShifterStrategy.movePort(drawGraph, lineAttrs.getHeadPort());
      }

      return lineAttrs.getHeadPort();
    }
    return null;
  }

  public static FlatPoint getPortPoint(Line line, DNode node,
                                       DrawGraph drawGraph) {
    return getPortPoint(line, node, drawGraph, true);
  }

  public static FlatPoint getPortPoint(Line line, DNode node, DrawGraph drawGraph,
                                       boolean portClipNode) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(drawGraph, "drawGraph");

    if (node.isVirtual() || line == null) {
      return new FlatPoint(node.getX(), node.getY());
    }

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    if (lineDrawProp == null) {
      return new FlatPoint(node.getX(), node.getY());
    }

    String cellId = getCellId(line, node, lineDrawProp);

    Port port = getLineEndPointPort(node.getNode(), line, drawGraph, false);
    return endPoint(portClipNode, cellId, port, node.getNode(), drawGraph, node);
  }

  public static String getCellId(Line line, DNode node, LineDrawProp lineDrawProp) {
    String cellId = null;
    if (node.getNode() == line.tail()) {
      cellId = lineDrawProp.lineAttrs().getTailCell();
    }
    if (node.getNode() == line.head()) {
      cellId = lineDrawProp.lineAttrs().getHeadCell();
    }
    return cellId;
  }

  public static FlatPoint endPoint(String cellId, Port port, Node node,
                                   DrawGraph drawGraph, ShapePosition shapePosition) {
    return endPoint(true, cellId, port, node, drawGraph, shapePosition);
  }

  public static FlatPoint endPoint(boolean portClipNode, String cellId, Port port, Node node,
                                   DrawGraph drawGraph, ShapePosition shapePosition) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(shapePosition, "shapePosition");

    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node);
    Asserts.nullArgument(nodeDrawProp, "nodeDrawProp");

    /*
     * 1.The flipped shapePosition needs to be changed back to the shape before flipping. e.g.: LR -> TB
     * 2.Use the original shapePosition to calculate the portPoint.
     * 3.Need to flip the portPoint to target Rankdir. e.g.: TB -> LR
     */
    Cell cell = null;
    RootCell rootCell = nodeDrawProp.getCell();
    if (rootCell != null) {
      cell = rootCell.getCellById(cellId);
    }

    Rectangle rectangle = getNodeBoxWithRankdir(drawGraph, shapePosition);

    if (port == null) {
      if (cell == null) {
        return new FlatPoint(shapePosition.getX(), shapePosition.getY());
      }

      FlatPoint center = cell.getCenter(rectangle);
      // Rotation by current shapePosition
      FlipShifterStrategy.movePointOpposite(drawGraph.rankdir(), shapePosition, center);
      return center;
    }

    FlatPoint portPoint;
    NodeShape nodeShape = nodeDrawProp.nodeShape();
    if (cell != null) {
      // Cell center point need the original node box to calculated.
      Rectangle cellRect = cell.getCellBox(rectangle);
      portPoint = new FlatPoint(
          cellRect.getX() + port.horOffset(cellRect),
          cellRect.getY() + port.verOffset(cellRect)
      );
      rectangle = cellRect;
      nodeShape = cell.getShape() != null ? cell.getShape() : NodeShapeEnum.RECT;
    } else {
      // Calculate the original port point coordinate
      portPoint = new FlatPoint(
          rectangle.getX() + port.horOffset(rectangle),
          rectangle.getY() + port.verOffset(rectangle)
      );
    }

    // If is the center, direct return
    if (Objects.equals(portPoint.getX(), rectangle.getX())
        && Objects.equals(portPoint.getY(), rectangle.getY())) {
      return portPoint;
    }

    if (!portClipNode || nodeShape.in(rectangle, portPoint)) {
      FlipShifterStrategy.movePointOpposite(drawGraph.rankdir(), shapePosition, portPoint);
      return portPoint;
    }

    double leftWidth = nodeShape.leftWidth(rectangle.getWidth());
    double topHeight = nodeShape.topHeight(rectangle.getHeight());
    FlatPoint center = new FlatPoint(rectangle.getLeftBorder() + leftWidth,
                                     rectangle.getUpBorder() + topHeight);

    FlatPoint p = AbstractDotLineRouter.straightLineClipShape(rectangle, nodeShape,
                                                              center, portPoint);
    FlipShifterStrategy.movePointOpposite(drawGraph.rankdir(), shapePosition, p);
    return p;
  }

  public static Rectangle getNodeBoxWithRankdir(DrawGraph drawGraph, ShapePosition shapePosition) {
    Rectangle rectangle = new Rectangle();
    rectangle.setUpBorder(shapePosition.getUpBorder());
    rectangle.setDownBorder(shapePosition.getDownBorder());
    rectangle.setLeftBorder(shapePosition.getLeftBorder());
    rectangle.setRightBorder(shapePosition.getRightBorder());

    // Rotation the rectangle to original position
    FlipShifterStrategy.moveRectangle(drawGraph.rankdir(), rectangle);
    return rectangle;
  }

  public static FlatPoint notFlipEndPoint(String cellId, Port port, NodeDrawProp node,
                                          ShapePosition shapePosition) {
    Asserts.nullArgument(node, "nodeDrawProp");
    Asserts.nullArgument(shapePosition, "shapePosition");

    Cell cell = null;
    RootCell Cell = node.getCell();
    if (Cell != null) {
      cell = Cell.getCellById(cellId);
    }

    if (port == null) {
      if (cell == null) {
        return new FlatPoint(shapePosition.getX(), shapePosition.getY());
      }

      return cell.getCenter(shapePosition);
    }

    if (cell != null) {
      FlatPoint cellCenter = cell.getCenter(shapePosition);
      shapePosition = new DefaultShapePosition(cellCenter.getX(), cellCenter.getY(),
                                               cell.getHeight(), cell.getWidth(),
                                               NodeShapeEnum.RECT);
    }

    FlatPoint portPoint = new FlatPoint(
        shapePosition.getX() + port.horOffset(shapePosition),
        shapePosition.getY() + port.verOffset(shapePosition)
    );
    if (Objects.equals(portPoint.getX(), shapePosition.getX())
        && Objects.equals(portPoint.getY(), shapePosition.getY())) {
      return portPoint;
    }

    if (node.nodeShape().in(shapePosition, portPoint)) {
      return portPoint;
    }

    FlatPoint point = new FlatPoint(shapePosition.getX(), shapePosition.getY());
    return AbstractDotLineRouter.straightLineClipShape(shapePosition, point, portPoint);
  }
}
