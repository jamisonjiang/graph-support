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
import java.util.Objects;
import java.util.TreeSet;
import org.graphper.api.LineAttrs;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.FlipShifterStrategy;
import org.graphper.util.Asserts;
import org.graphper.api.attributes.Port;

public class PortNodeSizeExpander extends NodeSizeExpander {

  public PortNodeSizeExpander(DrawGraph drawGraph, DNode node) {
    Asserts.nullArgument(node, "node");
    Asserts.illegalArgument(node.isVirtual(), "Node is virtual node");
    Asserts.illegalArgument(!node.haveSelfLine(), "Node do not have self lines");
    this.node = node;
    init(drawGraph);
  }

  private void init(DrawGraph drawGraph) {
    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node.getNode());
    Asserts.illegalArgument(nodeDrawProp == null, "Can not find node draw property");

    // Self line sort by two endpoint distance
    TreeSet<SelfLine> lines = sortSelfLine(drawGraph);
    // Calculate all axes offset and update node volume
    selfLinePoints(drawGraph, nodeDrawProp, lines);
  }

  private void selfLinePoints(DrawGraph drawGraph, NodeDrawProp nodeDrawProp, TreeSet<SelfLine> lines) {
    double interval = minSelfInterval(node);
    Map<Port, Double> axisDistRecord = null;
    Map<Float, Double> tempAxisDistRecord = null;
    Map<FlatPoint, Double> otherDistRecord = null;

    for (SelfLine selfLine : lines) {
      LineDrawProp lineDrawProp = lineDrawProp(drawGraph, selfLine);
      if (selfLine.directionPort != null) {
        if (otherDistRecord == null) {
          otherDistRecord = new HashMap<>(2);
        }
        double len = axisAccumulator(otherDistRecord, selfLine.tailPoint, selfLine.tailPoint,
                                     interval, selfLine, isHorizontalPort(selfLine.directionPort));
        double x = selfLine.directionPort.horOffset(node);
        double y = selfLine.directionPort.verOffset(node);
        addPoint(lineDrawProp, selfLine.tailPoint);
        if (x == node.getX()) {
          addPoint(lineDrawProp,
                   new FlatPoint(selfLine.tailPoint.getX(),
                                 y < node.getY() ? y - len : y + len)
          );
        } else {
          addPoint(lineDrawProp,
                   new FlatPoint(x < node.getX() ? x - len : x + len,
                                 selfLine.tailPoint.getY())
          );
        }
        addLabel(selfLine.line, lineDrawProp, 1);
        continue;
      }

      Port tailNearestPort = selfLine.tailNearestPort;
      Port headNearestPort = selfLine.headNearestPort;
      int n = Math.min(portDist(tailNearestPort, headNearestPort, true),
                       portDist(tailNearestPort, headNearestPort, false));

      if (n <= 1) {
        if (tempAxisDistRecord == null) {
          tempAxisDistRecord = new HashMap<>(2);
        }

        float axisNo = (float) (tailNearestPort.getNo() + headNearestPort.getNo()) / 2;
        boolean isHorizontal = n == 1 || isHorizontalPort(tailNearestPort);
        double len = axisAccumulator(tempAxisDistRecord, axisNo, axisNo,
                                     interval, selfLine, isHorizontal);
        FlatPoint point = getAdjPortPoint(node, len, tailNearestPort, headNearestPort);

        addPoint(lineDrawProp, selfLine.tailPoint);
        addPoint(lineDrawProp, point);
        addPoint(lineDrawProp, selfLine.headPoint);
        addLabel(selfLine.line, lineDrawProp, 1);
        continue;
      }

      addPoint(lineDrawProp, selfLine.tailPoint);

      Port pre;
      int labelIdx = 0;
      Port port = tailNearestPort;
      FlatPoint center = null;
      boolean positive = n == Math.abs(tailNearestPort.getNo() - headNearestPort.getNo());
      boolean clockwise = positive == tailNearestPort.getNo() < headNearestPort.getNo();
      Port midPort = findMidPort(tailNearestPort, headNearestPort, clockwise);

      do {
        pre = port;
        if (clockwise) {
          port = port.next();
        } else {
          port = port.pre();
        }

        if (port == headNearestPort) {
          break;
        }

        if (tempAxisDistRecord != null) {
          float no = (float) (pre.getNo() + port.getNo()) / 2;
          if (tempAxisDistRecord.get(no) != null) {
            double len = axisAccumulator(tempAxisDistRecord, no, null, interval, selfLine, false);
            FlatPoint point = getAdjPortPoint(node, len, pre, port);
            addPoint(lineDrawProp, point);
          }
        }

        if (axisDistRecord == null) {
          axisDistRecord = new HashMap<>(4);
        }
        double len = axisAccumulator(axisDistRecord, port, midPort, interval,
                                     selfLine, isHorizontalPort(port));

        FlatPoint point = PortHelper.notFlipEndPoint(null, port, nodeDrawProp, node);

        if (center == null) {
          center = new FlatPoint(node.getX(), node.getY());
        }
        double d = FlatPoint.twoFlatPointDistance(point, center);
        FlatPoint flatPoint = Vectors.add(center, Vectors
            .multiple(Vectors.sub(point, center), (d + len) / d));
        if (port == midPort) {
          labelIdx = lineDrawProp.size();
        }
        addPoint(lineDrawProp, flatPoint);
      } while (true);

      addPoint(lineDrawProp, selfLine.headPoint);
      addLabel(selfLine.line, lineDrawProp, labelIdx);
    }
  }

  private TreeSet<SelfLine> sortSelfLine(DrawGraph drawGraph) {
    TreeSet<SelfLine> lines = new TreeSet<>(this::selfLineComparator);
    for (int i = 0; i < node.getSelfLoopCount(); i++) {
      DLine selfLine = node.selfLine(i);
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());
      if (lineDrawProp == null) {
        continue;
      }

      LineAttrs lineAttrs = lineDrawProp.lineAttrs();
      Port tailPort = FlipShifterStrategy.movePort(drawGraph, lineAttrs.getTailPort());

      FlatPoint tailPoint = PortHelper
          .endPoint(lineAttrs.getTailCell(), lineAttrs.getTailPort(), node.getNode(), drawGraph, node);
      FlatPoint headPoint = PortHelper
          .endPoint(lineAttrs.getHeadCell(), lineAttrs.getHeadPort(), node.getNode(), drawGraph, node);

      Port tailNearestPort = null;
      Port headNearestPort = null;
      Port directionPort = null;
      if (Objects.equals(tailPoint, headPoint)) {
        boolean inCenterVerAxis = Objects.equals(tailPoint.getX(), node.getX());
        boolean inCenterHorAxis = Objects.equals(tailPoint.getY(), node.getY());

        // Endpoint in node's horizontal and vertical axis
        if (inCenterHorAxis && inCenterVerAxis) {
          directionPort = Port.EAST;
        } else if (inCenterVerAxis) {
          if (tailPoint.getY() < node.getY()) {
            directionPort = Port.NORTH;
          } else {
            directionPort = Port.SOUTH;
          }
        } else if (inCenterHorAxis) {
          if (tailPoint.getX() < node.getX()) {
            directionPort = Port.WEST;
          } else {
            directionPort = Port.EAST;
          }
        } else if (tailPort.isAxis()) {
          directionPort = tailPort;
        } else {
          directionPort = tailPoint.getX() < node.getX() ? Port.WEST : Port.EAST;
        }
      } else {
        tailNearestPort = findNearestPort(tailPoint, node, null);
        headNearestPort = findNearestPort(headPoint, node, tailNearestPort);
      }

      lines.add(
          new SelfLine(i, selfLine, tailPoint,
                       headPoint, tailNearestPort,
                       headNearestPort, directionPort)
      );
    }
    return lines;
  }

  private FlatPoint getAdjPortPoint(DNode node, double len,
                                    Port tailNearestPort,
                                    Port headNearestPort) {
    double tx = tailNearestPort.horOffset(node);
    double ty = tailNearestPort.verOffset(node);
    double hx = headNearestPort.horOffset(node);
    double hy = headNearestPort.verOffset(node);

    FlatPoint point;
    // Simplify the middle position between the two port points of shape, use the middle coordinates instead
    if (tx == hx) {
      point = new FlatPoint(tx < node.getX() ? tx - len : tx + len, (ty + hy) / 2);
    } else {
      point = new FlatPoint((tx + hx) / 2, ty < node.getY() ? ty - len : ty + len);
    }
    return point;
  }

  private <T> double axisAccumulator(Map<T, Double> accumulatorMap, T key, T labelKey,
                                     double interval, SelfLine selfLine, boolean horizontal) {
    Double length = accumulatorMap.get(key);
    length = length == null ? interval : length + interval;
    double t = length;
    if (selfLine.line.getLabelSize() != null && Objects.equals(labelKey, key)) {
      if (horizontal) {
        length += selfLine.line.getLabelSize().getWidth();
      } else {
        length += selfLine.line.getLabelSize().getHeight();
      }
    }
    accumulatorMap.put(key, length);
    return t;
  }

  private int portDist(Port p1, Port p2, boolean positive) {
    if (positive) {
      return Math.abs(p2.getNo() - p1.getNo());
    }

    Port max = p1.getNo() > p2.getNo() ? p1 : p2;
    Port min = max == p1 ? p2 : p1;

    return Port.maxNo() + 1 - max.getNo() + min.getNo();
  }

  private int selfLineComparator(SelfLine left, SelfLine right) {
    if (left.tailNearestPort == left.headNearestPort) {
      return -1;
    }
    if (right.tailNearestPort == right.headNearestPort) {
      return 1;
    }

    int d1 = Math.min(portDist(left.tailNearestPort, left.headNearestPort, true),
                      portDist(left.tailNearestPort, left.headNearestPort, false));
    int d2 = Math.min(portDist(right.tailNearestPort, right.headNearestPort, true),
                      portDist(right.tailNearestPort, right.headNearestPort, false));

    int no = Double.compare(d1, d2);
    return no != 0 ? no : Integer.compare(left.selfLineNo, right.selfLineNo);
  }

  private Port findNearestPort(FlatPoint point, ShapePosition position, Port exclude) {
    if (point == null) {
      return null;
    }

    Port minNearestPort = null;
    double minDistance = Double.MAX_VALUE;
    for (Port port : Port.values()) {
      if (port == exclude) {
        continue;
      }
      double verOffset = port.verOffset(position);
      double horOffset = port.horOffset(position);

      double d = FlatPoint.twoFlatPointDistance(point, new FlatPoint(horOffset, verOffset));
      if (d < minDistance) {
        minDistance = d;
        minNearestPort = port;
      }
    }

    return minNearestPort;
  }

  private Port findMidPort(Port start, Port end, boolean clockwise) {
    int i = 0;
    Port current = start;
    int n = (Math.min(portDist(start, end, false), portDist(start, end, true)) + 1) / 2;
    while (i++ < n) {
      if (clockwise) {
        current = current.next();
      } else {
        current = current.pre();
      }
    }

    return current;
  }

  private LineDrawProp lineDrawProp(DrawGraph drawGraph, SelfLine line) {
    return drawGraph.getLineDrawProp(line.line.getLine());
  }

  private void addLabel(DLine line, LineDrawProp lineDrawProp, int labelIdx) {
    if (!line.haveLabel() || labelIdx < 0 || labelIdx >= lineDrawProp.size()) {
      return;
    }

    FlatPoint labelSize = line.getLabelSize();
    FlatPoint point = lineDrawProp.get(labelIdx);

    FlatPoint labelCenter;
    double halfWidth = labelSize.getWidth() / 2;
    double halfHeight = labelSize.getHeight() / 2;
    FlatPoint center = new FlatPoint(node.getX(), node.getY());

    if (point.getX() == node.getX()) {
      labelCenter = new FlatPoint(point.getX(), point.getY() < node.getY()
          ? point.getY() - halfHeight
          : point.getY() + halfHeight);
    } else if (point.getY() == node.getY()) {
      labelCenter = new FlatPoint(point.getX() < node.getX()
                                      ? point.getX() - halfWidth
                                      : point.getX() + halfWidth, point.getY());
    } else {
      double d = FlatPoint.twoFlatPointDistance(point, center);
      labelCenter = Vectors.add(center, Vectors
          .multiple(Vectors.sub(point, center), (d + halfHeight) / d));
    }

    lineDrawProp.setLabelCenter(labelCenter);
    refreshVolume(labelCenter.getX() - halfWidth, labelCenter.getY() - halfHeight);
    refreshVolume(labelCenter.getX() - halfWidth, labelCenter.getY() + halfHeight);
    refreshVolume(labelCenter.getX() + halfWidth, labelCenter.getY() - halfHeight);
    refreshVolume(labelCenter.getX() + halfWidth, labelCenter.getY() + halfHeight);
  }

  private boolean isHorizontalPort(Port port) {
    return port == Port.WEST || port == Port.EAST;
  }

  private static class SelfLine {

    private final int selfLineNo;

    private final DLine line;

    private final FlatPoint tailPoint;

    private final FlatPoint headPoint;

    private final Port tailNearestPort;

    private final Port headNearestPort;

    private final Port directionPort;

    public SelfLine(int selfLineNo, DLine line, FlatPoint tailPoint,
                    FlatPoint headPoint, Port tailNearestPort,
                    Port headNearestPort, Port directionPort) {
      this.selfLineNo = selfLineNo;
      this.line = line;
      this.tailPoint = tailPoint;
      this.headPoint = headPoint;
      this.tailNearestPort = tailNearestPort;
      this.headNearestPort = headNearestPort;
      this.directionPort = directionPort;
    }
  }
}
