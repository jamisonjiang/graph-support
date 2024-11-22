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

import java.util.Collections;
import java.util.List;
import org.graphper.api.GraphAttrs;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.def.UnaryConcatIterable;
import org.graphper.def.FlatPoint;
import org.graphper.def.RectangleTree;
import org.graphper.def.UnfeasibleException;
import org.graphper.def.Vectors;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.ANode;
import org.graphper.layout.PortHelper;
import org.graphper.layout.PortHelper.PortPoint;
import org.graphper.util.CollectionUtils;
import org.graphper.util.ValueUtils;

/**
 * Abstract base class for routing graph edges around obstacles using a recursive splitting strategy.
 *
 * @author Jamison Jiang
 */
abstract class AroundLineRouter extends AbstractFdpLineRouter {

  protected RectangleTree<ANode> rtree;

  protected AroundLineRouter(DrawGraph drawGraph, FdpGraph fdpGraph) {
    super(drawGraph, fdpGraph);
    rtree = new RectangleTree<>(5);
    fdpGraph.forEach(rtree::insert);
  }

  protected abstract void drawLine(FLine line, Iterable<FlatPoint> splitPoints);

  @Override
  protected void handle(FLine line) {
    GraphAttrs graphAttrs = drawGraph.getGraphviz().graphAttrs();
    if (graphAttrs.isOverlap()) {
      drawStraightLine(line);
      return;
    }

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
    if (CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }

    FlatPoint from = new FlatPoint(line.from().getX(), line.from().getY());
    FlatPoint to = new FlatPoint(line.to().getX(), line.to().getY());

    Iterable<FlatPoint> splitPoints = findSplitPoints(from, to, line.from(), line.to(), 0,
                                                      layoutGraph.vertexNum() * 4);
    if (splitPoints == null) {
      drawStraightLine(line);
      return;
    }

    ANode tail = layoutGraph.getNode(lineDrawProp.getLine().tail());
    ANode head = layoutGraph.getNode(lineDrawProp.getLine().head());
    PortPoint tailPoint = PortHelper.getPortPoint(lineDrawProp.getLine(), tail, drawGraph);
    PortPoint headPoint = PortHelper.getPortPoint(lineDrawProp.getLine(), head, drawGraph);
    splitPoints = new UnaryConcatIterable<>(Collections.singletonList(tailPoint),
                                            splitPoints, Collections.singletonList(headPoint));

    drawLine(line, splitPoints);
  }

  private Iterable<FlatPoint> findSplitPoints(FlatPoint n, FlatPoint w, ANode from, ANode to,
                                              int times, int maxTimes) {
    if (times > maxTimes || approximateEquals(n, w)) {
      return null;
    }

    DefaultBox area = new DefaultBox(
        Math.min(n.getX(), w.getX()),
        Math.max(n.getX(), w.getX()),
        Math.min(n.getY(), w.getY()),
        Math.max(n.getY(), w.getY())
    );

    if (!area.positive()) {
      area.setLeftBorder(area.getLeftBorder() - 5);
      area.setRightBorder(area.getRightBorder() + 5);
      area.setUpBorder(area.getUpBorder() - 5);
      area.setDownBorder(area.getDownBorder() + 5);
    }

    List<ANode> nodes = rtree.search(area);
    if (CollectionUtils.isEmpty(nodes)) {
      return null;
    }

    for (ANode node : nodes) {
      if (node == from || node == to || node.in(n) || node.in(w)) {
        continue;
      }

      if (!isCross(n, w, node)) {
        continue;
      }

      FlatPoint splitPoint = findSplitPoint(n, w, node);
      if (splitPoint == null) {
        continue;
      }

      Iterable<FlatPoint> group1 = findSplitPoints(n, splitPoint, from, to, times + 1, maxTimes);
      Iterable<FlatPoint> group2 = findSplitPoints(splitPoint, w, from, to, times + 1, maxTimes);
      return new UnaryConcatIterable<>(group1, Collections.singleton(splitPoint), group2);
    }

    return null;
  }

  private FlatPoint findSplitPoint(FlatPoint n, FlatPoint w, Box checkBox) {
    FlatPoint leftUp = checkBox.getLeftUp();
    if (eitherEquals(n, w, leftUp) && isNotCross(n, w, leftUp, checkBox)) {
      return leftUp;
    }

    FlatPoint leftDown = checkBox.getLeftDown();
    if (eitherEquals(n, w, leftDown) && isNotCross(n, w, leftDown, checkBox)) {
      return leftDown;
    }

    FlatPoint rightUp = checkBox.getRightUp();
    if (eitherEquals(n, w, rightUp) && isNotCross(n, w, rightUp, checkBox)) {
      return rightUp;
    }

    FlatPoint rightDown = checkBox.getRightDown();
    if (eitherEquals(n, w, rightDown) && isNotCross(n, w, rightDown, checkBox)) {
      return rightDown;
    }

    double dist;
    double minDist = Double.MAX_VALUE;
    FlatPoint minDistCorner = null;
    if (eitherEquals(n, w, leftUp) && (dist = Vectors.disToLine(leftUp, n, w)) < minDist) {
      minDist = dist;
      minDistCorner = leftUp;
    }
    if (eitherEquals(n, w, leftDown) && (dist = Vectors.disToLine(leftDown, n, w)) < minDist) {
      minDist = dist;
      minDistCorner = leftDown;
    }
    if (eitherEquals(n, w, rightUp) && (dist = Vectors.disToLine(rightUp, n, w)) < minDist) {
      minDist = dist;
      minDistCorner = rightUp;
    }
    if (eitherEquals(n, w, rightDown) && Vectors.disToLine(rightDown, n, w) < minDist) {
      minDistCorner = rightDown;
    }

    return minDistCorner;
  }

  private boolean isNotCross(FlatPoint n, FlatPoint w, FlatPoint splitPoint, Box checkBox) {
    return !isCross(n, splitPoint, checkBox) && !isCross(w, splitPoint, checkBox);
  }

  private boolean isCross(FlatPoint fromPoint, FlatPoint toPoint, Box checkBox) {
    return linesIntersect(fromPoint, toPoint, checkBox.getLeftUp(), checkBox.getRightDown())
        || linesIntersect(fromPoint, toPoint, checkBox.getLeftDown(), checkBox.getRightUp());
  }

  private boolean linesIntersect(FlatPoint fromPoint, FlatPoint toPoint,
                                 FlatPoint source, FlatPoint target) {
    try {
      FlatPoint intersect = Vectors.lineInters(fromPoint, toPoint, source, target);
      if (approximateEquals(intersect, fromPoint) || approximateEquals(intersect, toPoint)
          || approximateEquals(intersect, source) || approximateEquals(intersect, target)) {
        return false;
      }

      return inLineSegment(fromPoint, toPoint, intersect)
          && inLineSegment(source, target, intersect);
    } catch (UnfeasibleException e) {
      return false;
    }
  }

  private boolean inLineSegment(FlatPoint n, FlatPoint w, FlatPoint intersect) {
    return intersect.getX() < n.getX() == intersect.getX() > w.getX();
  }

  private boolean eitherEquals(FlatPoint p, FlatPoint q, FlatPoint t) {
    return !t.equals(p) && !t.equals(q);
  }

  private boolean approximateEquals(FlatPoint p, FlatPoint q) {
    return ValueUtils.approximate(p.getX(), q.getX())
        && ValueUtils.approximate(p.getY(), q.getY());
  }
}
