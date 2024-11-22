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

package org.graphper.layout;

import static org.graphper.layout.StraightPathClip.straightLineClipShape;

import java.util.Objects;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.ext.ShapePosition;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.Rectangle;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.Asserts;

/**
 * Helper to handle various case of {@link Port} feature.
 *
 * @author Jamison Jiang
 */
public class PortHelper {

  private PortHelper() {
  }

  public static FlatPoint getPortPoint(ANode node, Port port) {
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

  public static PortPoint getPortPoint(Line line, ANode node,
                                       DrawGraph drawGraph) {
    return getPortPoint(line, node, drawGraph, true);
  }

  public static PortPoint getPortPointWithoutClip(Line line, ANode node,
                                                  DrawGraph drawGraph) {
    return getPortPoint(line, node, drawGraph, false);
  }

  public static PortPoint getPortPoint(ANode node, String cellId, Port port, DrawGraph drawGraph) {
    return endPoint(true, cellId, port, node.getNode(), drawGraph, node);
  }

  public static PortPoint getPortPoint(Line line, ANode node, DrawGraph drawGraph,
                                       boolean portClipNode) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(drawGraph, "drawGraph");

    if (node.isVirtual() || line == null) {
      return new PortPoint(node.getX(), node.getY(), false, null);
    }

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    if (lineDrawProp == null) {
      return new PortPoint(node.getX(), node.getY(), false, null);
    }

    String cellId = getCellId(line, node, lineDrawProp);

    Port port = getLineEndPointPort(node.getNode(), line, drawGraph, false);
    return endPoint(portClipNode, cellId, port, node.getNode(), drawGraph, node);
  }

  public static String getCellId(Line line, ANode node, LineDrawProp lineDrawProp) {
    String cellId = null;
    if (node.getNode() == line.tail()) {
      cellId = lineDrawProp.lineAttrs().getTailCell();
    }
    if (node.getNode() == line.head()) {
      cellId = lineDrawProp.lineAttrs().getHeadCell();
    }
    return cellId;
  }

  public static PortPoint endPoint(boolean portClipNode, String cellId, Port port,
                                   Node node, DrawGraph drawGraph, ShapePosition shapePosition) {
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

    if (port == null) {
      if (cell == null) {
        return new PortPoint(shapePosition.getX(), shapePosition.getY(), false, null);
      }

      FlatPoint center = cell.getCenter(shapePosition);
      return new PortPoint(center.getX(), center.getY(), true, null);
    }

    PortPoint portPoint;
    ShapePropCalc shapeProp = nodeDrawProp.shapeProp();
    Rectangle rectangle = getNodeBoxWithRankdir(drawGraph, shapePosition);

    if (cell != null) {
      // Cell center point need the original node box to calculated.
      Rectangle cellRect = cell.getCellBox(shapePosition);
      DefaultShapePosition cellShapePos = new DefaultShapePosition(
          cellRect.getX(), cellRect.getY(), cell.getHeight(), cell.getWidth(), cell.getShape()
      );
      cellRect = getNodeBoxWithRankdir(shapePosition.getRightBorder(),
                                       shapePosition.getDownBorder(),
                                       drawGraph, cellShapePos);

      portPoint = new PortPoint(
          cellRect.getX() + port.horOffset(cellRect),
          cellRect.getY() + port.verOffset(cellRect), true, port
      );
      rectangle = cellRect;
      shapeProp = cell.getShape() != null ? cell.getShape() : NodeShapeEnum.RECT;
    } else {
      // Calculate the original port point coordinate
      portPoint = new PortPoint(
          rectangle.getX() + port.horOffset(rectangle),
          rectangle.getY() + port.verOffset(rectangle), true, port
      );
    }

    if (nodeCenter(portPoint, rectangle) || !portClipNode || shapeProp.in(rectangle, portPoint)) {
      FlipShifterStrategy.movePointOpposite(drawGraph, shapePosition, portPoint);
      return portPoint;
    }

    double leftWidth;
    double topHeight;
    if (shapePosition instanceof NodeShape) {
      NodeShape shape = (NodeShape) shapeProp;
      leftWidth = shape.leftWidth(rectangle.getWidth());
      topHeight = shape.topHeight(rectangle.getHeight());
    } else {
      leftWidth = rectangle.getWidth() / 2;
      topHeight = rectangle.getHeight() / 2;
    }

    FlatPoint center = new FlatPoint(rectangle.getLeftBorder() + leftWidth,
                                     rectangle.getUpBorder() + topHeight);

    FlatPoint p = straightLineClipShape(rectangle, shapeProp, center, portPoint);
    FlipShifterStrategy.movePointOpposite(drawGraph, shapePosition, p);
    return new PortPoint(p.getX(), p.getY(), true, port);
  }

  private static boolean nodeCenter(PortPoint portPoint, Rectangle rectangle) {
    return Objects.equals(portPoint.getX(), rectangle.getX())
        && Objects.equals(portPoint.getY(), rectangle.getY());
  }

  public static Rectangle getNodeBoxWithRankdir(DrawGraph drawGraph, ShapePosition shapePosition) {
    return getNodeBoxWithRankdir(shapePosition.getRightBorder(),
                                 shapePosition.getDownBorder(),
                                 drawGraph, shapePosition);
  }

  public static Rectangle getNodeBoxWithRankdir(double maxX, double maxY, DrawGraph drawGraph,
                                                ShapePosition shapePosition) {
    Rectangle rectangle = new Rectangle();
    rectangle.setUpBorder(shapePosition.getUpBorder());
    rectangle.setDownBorder(shapePosition.getDownBorder());
    rectangle.setLeftBorder(shapePosition.getLeftBorder());
    rectangle.setRightBorder(shapePosition.getRightBorder());

    // Rotation the rectangle to original position
    FlipShifterStrategy.moveRectangle(drawGraph, maxX, maxY, rectangle);
    return rectangle;
  }

  public static FlatPoint notFlipEndPoint(String cellId, Port port, NodeDrawProp node,
                                          ShapePosition shapePosition) {
    Asserts.nullArgument(node, "nodeDrawProp");
    Asserts.nullArgument(shapePosition, "shapePosition");

    Cell cell = null;
    RootCell rootCell = node.getCell();
    if (rootCell != null) {
      cell = rootCell.getCellById(cellId);
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

    if (node.shapeProp().in(shapePosition, portPoint)) {
      return portPoint;
    }

    FlatPoint point = new FlatPoint(shapePosition.getX(), shapePosition.getY());
    return straightLineClipShape(shapePosition, point, portPoint);
  }

  public static double portCompareNo(Line line, ANode node, DrawGraph drawGraph) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(drawGraph, "drawGraph");

    if (node.isVirtual() || line == null) {
      return 0;
    }

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    if (lineDrawProp == null) {
      return 0;
    }

    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node.getNode());
    Asserts.nullArgument(nodeDrawProp, "nodeDrawProp");

    String cellId = getCellId(line, node, lineDrawProp);
    Port port = getLineEndPointPort(node.getNode(), line, drawGraph, true);

    Cell cell = null;
    RootCell rootCell = nodeDrawProp.getCell();
    if (rootCell != null) {
      cell = rootCell.getCellById(cellId);
    }

    double compareNo = 0;
    if (cell != null) {
      FlatPoint offset = cell.getOffset();
      if (offset != null) {
        compareNo += offset.getX();
      }
    }

    if (port != null) {
      compareNo += port.horOffsetRatio();
    }
    return compareNo;
  }

  public static class PortPoint extends FlatPoint {

    private static final long serialVersionUID = 1628364834247941307L;
    private final boolean havePort;

    private final Port port;

    public PortPoint(double height, double width, boolean havePort, Port port) {
      super(height, width);
      this.havePort = havePort;
      this.port = port;
    }

    public Port getPort() {
      return port;
    }

    public boolean notNodeCenter() {
      return havePort;
    }
  }
}