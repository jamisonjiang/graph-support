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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.graphper.api.Line;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.EnvProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDotLineRouter extends LineClip implements DotLineRouter {

  private static final Logger log = LoggerFactory.getLogger(AbstractDotLineRouter.class);

  protected static final double LABEL_NODE_SIDE_MIN_DISTANCE = 10;

  // distance deviation tolerance
  protected static final double CLIP_DIST_ERROR = 0.1;

  protected RankContent rankContent;
  protected EdgeDedigraph<DNode, DLine> digraphProxy;

  @Override
  public void route() {
    Object attach = attach();

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (DNode node : rankNode) {
        if (nodeConsumer(node, attach)) {
          continue;
        }

        // All out edges
        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (line.isVirtual() || line.isHide()) {
            continue;
          }

          if (line.isParallelMerge() && (!line.isSameRank() || (line.isSameRank()
              && isAdj(line.from(), line.to())))
          ) {
            parallelLineHandle(line);
            continue;
          }

          lineConsumer(line, attach);
        }

        // Draw self loop
        selfLoopHandle(node);
      }
    }
  }

  /**
   * Before draw line, produce attachment for next method.
   *
   * @return draw line attachment
   */
  protected Object attach() {
    return null;
  }

  /**
   * The consumption action of the node when drawing the line.
   *
   * @param node   node
   * @param attach draw line attachment
   * @return True - continue draw line, False - consume next node
   */
  protected boolean nodeConsumer(DNode node, Object attach) {
    return false;
  }

  /**
   * The consumption action of the line.
   *
   * @param line   line
   * @param attach draw line attachment
   */
  protected void lineConsumer(DLine line, Object attach) {
  }

  protected boolean isSplineNone() {
    return drawGraph.getGraphviz().graphAttrs().getSplines() == Splines.NONE;
  }

  /**
   * A piecewise cubic Bessel converted to control points, adjacent curves share the same control
   * point.
   *
   * @param curves piecewise cubic Bessel
   * @return cubic Bessel control points
   */
  protected List<FlatPoint> multiBezierCurveToPoints(MultiBezierCurve curves) {
    List<FlatPoint> splines = new ArrayList<>(curves.size() * 3 + 1);

    for (int i = 0; i < curves.size(); i++) {
      ThirdOrderBezierCurve curve = curves.get(i);
      if (i == 0) {
        splines.add(curve.getV1());
      }
      splines.add(curve.getV2());
      splines.add(curve.getV3());
      splines.add(curve.getV4());
    }

    return splines;
  }

  /**
   * Convert the four control points of the curve into an array.
   *
   * @param curve cubic Bezier
   * @return control points
   */
  protected List<FlatPoint> thirdOrderBezierCurveToPoints(ThirdOrderBezierCurve curve) {
    List<FlatPoint> splines = new ArrayList<>(4);
    splines.add(curve.getV1());
    splines.add(curve.getV2());
    splines.add(curve.getV3());
    splines.add(curve.getV4());

    return splines;
  }

  /**
   * Divide the path using the tangent vector that the path intersects at the node boundary to fit
   * the path to the node shape.
   *
   * @param shapePosition shape position information
   * @param inPoint       point inside node
   * @param outPoint      point outside node
   * @return border crossing point
   */
  public static FlatPoint straightLineClipShape(ShapePosition shapePosition,
                                                FlatPoint inPoint, FlatPoint outPoint) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    return straightLineClipShape(shapePosition, shapePosition.shapeProp(), inPoint, outPoint);
  }

  /**
   * Divide the path using the tangent vector that the path intersects at the node boundary to fit
   * the path to the node shape.
   *
   * @param box           node box
   * @param shapePropCalc node shape properties function
   * @param inPoint       point inside node
   * @param outPoint      point outside node
   * @return border crossing point
   */
  public static FlatPoint straightLineClipShape(Box box, ShapePropCalc shapePropCalc,
                                                FlatPoint inPoint, FlatPoint outPoint) {
    Asserts.nullArgument(inPoint, "inPoint");
    Asserts.nullArgument(outPoint, "outPoint");
    Asserts.nullArgument(box, "shapePosition");
    Asserts.nullArgument(shapePropCalc, "shapePosition.nodeShape()");

    Asserts.illegalArgument(
        !shapePropCalc.in(box, inPoint),
        "The specified internal node is not inside the node"
    );
    Asserts.illegalArgument(
        shapePropCalc.in(box, outPoint),
        "The specified external node is inside the node"
    );

    FlatPoint midPoint;
    FlatPoint in = inPoint;
    FlatPoint out = outPoint;

    do {
      midPoint = new FlatPoint((in.getX() + out.getX()) / 2, (in.getY() + out.getY()) / 2);

      if (shapePropCalc.in(box, midPoint)) {
        in = midPoint;
      } else {
        out = midPoint;
      }

    } while (FlatPoint.twoFlatPointDistance(in, out) > CLIP_DIST_ERROR);

    return midPoint;
  }

  /**
   * According to the shape object of the specified coordinates and size, cut the specified bessel
   * curve to ensure that the curve fits the specified shape.
   *
   * @param shapePosition shape position information
   * @param bezierCurve   the curve to be clipped
   * @return curve after clip
   */
  public static ThirdOrderBezierCurve besselCurveClipShape(ShapePosition shapePosition,
                                                           ThirdOrderBezierCurve bezierCurve) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    Asserts.nullArgument(shapePosition.shapeProp(), "shapePosition.nodeShape()");
    Asserts.nullArgument(bezierCurve, "bezierCurve");

    if (shapePosition.getHeight() <= 0 || shapePosition.getWidth() <= 0) {
      return bezierCurve;
    }

    FlatPoint v1 = bezierCurve.getV1();
    FlatPoint v2 = bezierCurve.getV2();
    FlatPoint v3 = bezierCurve.getV3();
    FlatPoint v4 = bezierCurve.getV4();

    ShapePropCalc shapePropCalc = shapePosition.shapeProp();

    boolean v1In = shapePropCalc.in(shapePosition, v1);
    boolean v4In = shapePropCalc.in(shapePosition, v4);

    if (v1In && v4In) {
      return null;
    }

    if (!v1In && !v4In) {
      return bezierCurve;
    }

    double in = v1In ? 0 : 1;
    double out = v4In ? 0 : 1;
    FlatPoint[] points = {v1, v2, v3, v4};

    do {
      FlatPoint midPoint = Curves.besselEquationCalc((in + out) / 2, points);

      if (shapePropCalc.in(shapePosition, midPoint)) {
        in = (in + out) / 2;
      } else {
        out = (in + out) / 2;
      }

    } while (FlatPoint.twoFlatPointDistance(Curves.besselEquationCalc(in, points),
                                            Curves.besselEquationCalc(out, points))
        > CLIP_DIST_ERROR);

    return Curves.divideThirdBesselCurve(in, v4In, bezierCurve);
  }

  /**
   * If the node has a self-loop edge, generate a simulated path of the self-loop edge.
   *
   * @param node node to be detected
   */
  protected void selfLoopHandle(DNode node) {
    if (node == null
        || node.isVirtual()
        || CollectionUtils.isEmpty(node.getSelfLines())
        || isSplineNone()) {
      return;
    }

    boolean portAxisSelfLineMode = EnvProp.usePortAxisExpander();
    FlatPoint center = new FlatPoint(node.getX(), node.getY());
    for (DLine selfLine : node.getSelfLines()) {
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());
      if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() < 2) {
        continue;
      }

      for (FlatPoint point : lineDrawProp) {
        point.setX(node.getX() + point.getX());
        point.setY(node.getY() + point.getY());
      }
      if (lineDrawProp.getLabelCenter() != null) {
        FlatPoint labelCenter = lineDrawProp.getLabelCenter();
        labelCenter.setX(node.getX() + labelCenter.getX());
        labelCenter.setY(node.getY() + labelCenter.getY());
      }

      if (portAxisSelfLineMode) {
        if (lineDrawProp.size() == 2) {
          twoSelfLineDraw(selfLine);
        } else {
          largeTwoSelfLineDraw(center, selfLine);
        }
      } else {
        newSelfLineDrawMode(selfLine);
      }

      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        lineDrawProp.setStart(lineDrawProp.get(0));
        lineDrawProp.setEnd(lineDrawProp.get(lineDrawProp.size() - 1));
      }
    }
  }

  /**
   * Logic for drawing parallel edges with the same endpoints.
   *
   * @param parallelLines parallel lines
   */
  protected void handleSameEndpointParallelLines(List<DLine> parallelLines) {
    symmetryParallelLine(parallelLines);
  }

  /**
   * If the edge is a union of multiple parallel edges, generate a simulated path of the paralle
   * edges.
   *
   * @param line line to be detected
   */
  protected void parallelLineHandle(DLine line) {
    if (line == null || !line.isParallelMerge() || isSplineNone()) {
      return;
    }

    Map<Integer, List<DLine>> parallelLineRecordMap = groupParallelLineByEndpoint(line);

    for (Entry<Integer, List<DLine>> entry : parallelLineRecordMap.entrySet()) {
      List<DLine> parallelLines = entry.getValue();
      handleSameEndpointParallelLines(parallelLines);
    }
  }

  /**
   * Draw parallel sides with an axis of symmetry.
   *
   * @param parallelLines parallel edges
   */
  protected void symmetryParallelLine(List<DLine> parallelLines) {
    if (CollectionUtils.isEmpty(parallelLines)) {
      return;
    }

    DLine line = parallelLines.get(0);
    DNode from = line.from();
    DNode to = line.to();
    FlatPoint fromPoint = new FlatPoint(from.getX(), from.getY());
    FlatPoint toPoint = new FlatPoint(to.getX(), to.getY());

    double distUnit = (drawGraph.getGraphviz().graphAttrs().getNodeSep()
        + drawGraph.getGraphviz().graphAttrs().getRankSep()
        + FlatPoint.twoFlatPointDistance(fromPoint, toPoint)) / 20;

    for (int i = 0; i < parallelLines.size(); i++) {
      parallelEdges(parallelLines.get(i), parallelLines.size(), distUnit, i + 1);
    }
  }

  /**
   * If the line is cut by multiple virtual nodes, consume each virtual line segment through
   * lineSegmentConsumer.
   *
   * @param line     line
   * @param consumer line consumer
   */
  protected void lineSegmentConsumer(DLine line, Consumer<DLine> consumer) {
    DNode to = line.to();
    while (to.isVirtual()) {
      if (consumer != null) {
        consumer.accept(line);
      }

      for (DLine dLine : digraphProxy.outAdjacent(to)) {
        to = dLine.to();
        line = dLine;
        break;
      }
    }

    if (consumer != null && !to.isVirtual()) {
      consumer.accept(line);
    }
  }

  protected boolean isAdj(DNode n, DNode w) {
    if (Math.abs(w.getRankIndex() - n.getRankIndex()) <= 1) {
      return true;
    }

    // Skip virtual vertices between two vertices
    DNode largeRankIndexNode = n.getRankIndex() > w.getRankIndex() ? n : w;
    DNode current = n == largeRankIndexNode ? w : n;
    do {
      current = rankContent.rankNextNode(current);
    } while (current != null && current != largeRankIndexNode && current.isVirtual());

    return current == largeRankIndexNode;
  }

  protected Map<Integer, List<DLine>> groupParallelLineByEndpoint(DLine line) {
    Map<Integer, List<DLine>> parallelLineRecordMap = new HashMap<>(1);

    for (int i = 0; i < line.getParallelNums(); i++) {
      DLine edge = line.parallelLine(i);
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(edge.getLine());
      DNode from = edge.from();
      DNode to = edge.to();
      Port fromPort = PortHelper.getLineEndPointPort(from.getNode(), edge.getLine(), drawGraph);
      Port toPort = PortHelper.getLineEndPointPort(to.getNode(), edge.getLine(), drawGraph);
      String headCell = lineDrawProp.lineAttrs().getHeadCell();
      String tailCell = lineDrawProp.lineAttrs().getTailCell();

      String sign = signature(fromPort, tailCell) + signature(toPort, headCell);
      sign += signature(toPort, tailCell) + signature(fromPort, headCell);
      parallelLineRecordMap.computeIfAbsent(sign.hashCode(), h -> new ArrayList<>(2)).add(edge);
    }

    return parallelLineRecordMap;
  }

  private String signature(Port port, String cellId) {
    String sign = port != null ? port.name() : "";
    return sign + cellId;
  }

  // ----------------------------------------------------- static method -----------------------------------------------------

  public static ShapePosition newArrowShapePosition(FlatPoint point, double arrowSize) {
    Asserts.nullArgument(point, "point");
    return new DefaultShapePosition(point.getX(), point.getY(),
                                    arrowSize * 2, arrowSize * 2,
                                    NodeShapeEnum.CIRCLE);
  }

  public static <E extends FlatPoint> E getPoint(List<E> path, int i) {
    if (i < 0 || i >= path.size()) {
      return null;
    }

    return path.get(i);
  }

  public static <E extends FlatPoint> E getFirst(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(0);
  }

  public static <E extends FlatPoint> E getLast(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(path.size() - 1);
  }

  public static <E extends FlatPoint> InOutPointPair findInOutPair(int unit, List<E> path,
                                                                   boolean firstStart,
                                                                   ShapePosition shapePosition) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    Asserts.nullArgument(shapePosition.shapeProp(), "shapePosition.nodeShape()");

    Integer idx = null;
    Integer count = null;

    ShapePropCalc shapeProp = shapePosition.shapeProp();

    E point = getFirst(path);
    if (firstStart && point != null && shapeProp.in(shapePosition, point)) {
      idx = 0;
      count = unit;
    } else {
      point = getLast(path);
      if (point != null && shapeProp.in(shapePosition, point)) {
        idx = path.size() - 1;
        count = -unit;
      }
    }

    if (idx == null) {
      return null;
    }

    E pre = null;
    do {
      if (pre != null) {
        boolean preIn = shapeProp.in(shapePosition, pre);
        boolean pointIn = shapeProp.in(shapePosition, point);

        if (preIn != pointIn) {
          return new InOutPointPair(
              idx - count,
              count > 0,
              preIn ? pre : point,
              pointIn ? pre : point
          );
        }
      }

      idx += count;
      pre = point;
      point = getPoint(path, idx);
    } while (point != null);

    return null;
  }

  // ----------------------------------------------------- private method -----------------------------------------------------
  private void largeTwoSelfLineDraw(FlatPoint center, DLine selfLine) {
    Asserts.illegalArgument(selfLine == null || selfLine.isVirtual(), "error self loop no");

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());

    FlatPoint mid = lineDrawProp.get(lineDrawProp.size() / 2);
    FlatPoint start = lineDrawProp.get(0);
    FlatPoint end = lineDrawProp.get(lineDrawProp.size() - 1);

    MultiBezierCurve curves = Curves.fitCurves(Arrays.asList(start, mid, end),
                                               Vectors.add(
                                                   Vectors.sub(start, center),
                                                   Vectors.sub(mid, center)
                                               ),
                                               Vectors.add(
                                                   Vectors.sub(end, center),
                                                   Vectors.sub(mid, center)
                                               ), 0);

    lineDrawProp.clear();
    lineDrawProp.markIsBesselCurve();
    lineDrawProp.addAll(multiBezierCurveToPoints(curves));
  }

  private void twoSelfLineDraw(DLine selfLine) {
    Asserts.illegalArgument(selfLine == null || selfLine.isVirtual(), "error self loop no");

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());
    if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() != 2) {
      return;
    }

    FlatPoint start = lineDrawProp.get(0);
    FlatPoint end = lineDrawProp.get(lineDrawProp.size() - 1);
    FlatPoint axis = Vectors.sub(end, start);
    FlatPoint vertical = new FlatPoint(axis.getY(), -axis.getX());
    FlatPoint verticalOpposite = vertical.reserve();
    lineDrawProp.clear();

    double dist = axis.dist() / 4;
    lineDrawProp.add(start);
    lineDrawProp.add(Vectors.add(start, Vectors.scale(vertical, dist)));
    lineDrawProp.add(Vectors.add(end, Vectors.scale(vertical, dist)));
    lineDrawProp.add(end);
    lineDrawProp.add(Vectors.add(end, Vectors.scale(verticalOpposite, dist)));
    lineDrawProp.add(Vectors.add(start, Vectors.scale(verticalOpposite, dist)));
    lineDrawProp.add(start);

    lineDrawProp.markIsBesselCurve();
  }

  private void newSelfLineDrawMode(DLine selfLine) {
    Asserts.illegalArgument(selfLine == null || selfLine.isVirtual(), "error self loop no");

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(selfLine.getLine());
    if (CollectionUtils.isEmpty(lineDrawProp)
        || (lineDrawProp.size() != 2 && lineDrawProp.size() != 3)) {
      return;
    }

    FlatPoint start = lineDrawProp.get(0);
    FlatPoint mid = lineDrawProp.get(1);
    FlatPoint end;
    if (lineDrawProp.size() == 2) {
      end = start.clone();
    } else {
      end = lineDrawProp.get(2);
    }
    double minX = Math.min(start.getX(), end.getX());
    double maxX = Math.max(start.getX(), end.getX());
    double minY = Math.min(start.getY(), end.getY());
    double maxY = Math.max(start.getY(), end.getY());

    lineDrawProp.clear();
    if (mid.getX() >= minX && mid.getX() <= maxX && mid.getY() >= minY && mid.getY() <= maxY) {
      log.warn("Can not draw self line: mid point in Endpoint box");
      return;
    }

    lineDrawProp.add(start);
    if (mid.getX() < minX) {
      // Left
      double startDist = (start.getX() - mid.getX()) / 4;
      double endDist = (end.getX() - mid.getX()) / 4;
      if (start.getY() < end.getY()) {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() - startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() + endDist));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() + endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() + startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() - endDist));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() - endDist));
      }
    } else if (mid.getX() > maxX) {
      // Right
      double startDist = (mid.getX() - start.getX()) / 4;
      double endDist = (mid.getX() - end.getX()) / 4;
      if (start.getY() < end.getY()) {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() - startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() + endDist));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() + endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() + startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() - endDist));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() - endDist));
      }
    } else if (mid.getY() < maxY) {
      // Up
      double startDist = (start.getY() - mid.getY()) / 4;
      double endDist = (end.getY() - mid.getY()) / 4;
      if (start.getX() < end.getX()) {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() - endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() - endDist));
      }
    } else {
      // Down
      double startDist = (mid.getY() - start.getY()) / 4;
      double endDist = (mid.getY() - end.getY()) / 4;
      if (start.getX() < end.getX()) {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() + endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() + endDist));
      }
    }

    lineDrawProp.add(end);

    if (lineDrawProp.size() == 2) {
      lineDrawProp.clear();
      return;
    }

    lineDrawProp.markIsBesselCurve();
  }

  private void parallelEdges(DLine parallelLine, int size,
                             double distUnit, int no) {
    DNode from = parallelLine.from();
    DNode to = parallelLine.to();

    FlatPoint fromPoint = PortHelper.getPortPoint(parallelLine.getLine(), from, drawGraph);
    FlatPoint toPoint = PortHelper.getPortPoint(parallelLine.getLine(), to, drawGraph);

    double hypotenuseLen = hypotenuseLen(distUnit, no, size);

    Line iLine = parallelLine.getLine();
    Asserts.illegalArgument(iLine == null, "error parallel edge no");

    FlatPoint v2Center = Vectors.add(
        Vectors.multiple(Vectors.sub(fromPoint, toPoint), 0.75),
        toPoint
    );

    FlatPoint v3Center = Vectors.add(
        Vectors.multiple(Vectors.sub(fromPoint, toPoint), 0.25),
        toPoint
    );

    ThirdOrderBezierCurve curve = new ThirdOrderBezierCurve(
        fromPoint,
        newParallelControlPoint(parallelLine, size, no,
                                hypotenuseLen, fromPoint, toPoint, v2Center),
        newParallelControlPoint(parallelLine, size, no,
                                hypotenuseLen, fromPoint, toPoint, v3Center),
        toPoint
    );

    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(parallelLine.getLine());
    lineDrawProp.clear();
    lineDrawProp.addAll(thirdOrderBezierCurveToPoints(curve));
    lineDrawProp.markIsBesselCurve();
    lineDrawProp.setIsHeadStart(from.getNode());
    lineDrawProp.fakeInit();
  }

  private FlatPoint newParallelControlPoint(DLine line, int size, int no, double hypotenuseLen,
                                            FlatPoint f, FlatPoint t, FlatPoint v3Center) {
    return new FlatPoint(
        v3Center.getX() + xDist(f.getX(), f.getY(), t.getX(),
                                t.getY(), hypotenuseLen, no, size / 2),
        v3Center.getY() + yDist(f.getX(), f.getY(), t.getX(), t.getY(), hypotenuseLen, no,
                                size / 2, line.from().getRank() == line.to().getRank())
    );
  }

  private double hypotenuseLen(double unit, int segmentNum, int parallelEdgesNum) {
    if ((parallelEdgesNum & 1) == 1) {
      return Math.abs(unit * (parallelEdgesNum - 1) / 2 - (segmentNum - 1) * unit);
    }

    if (segmentNum <= (parallelEdgesNum >> 1)) {
      return unit * segmentNum - unit / 2;
    }

    return Math.abs(unit * segmentNum - (parallelEdgesNum >> 1) * unit - unit / 2);
  }

  private double xDist(double startX, double startY, double endX, double endY, double hypotenuseLen,
                       int segmentNum, int mid) {
    if (startY == endY) {
      return 0;
    }

    if (startX == endX) {
      return segmentNum <= mid ? -hypotenuseLen : hypotenuseLen;
    }

    double slop = (endY - startY) / (endX - startX);
    double xd = Math.sqrt(Math.pow(hypotenuseLen, 2) / (1 + 1 / Math.pow(slop, 2)));

    return segmentNum <= mid ? -xd : xd;
  }

  private double yDist(double startX, double startY, double endX, double endY, double hypotenuseLen,
                       int segmentNum, int mid, boolean isSameRank) {

    if (startX == endX) {
      return 0;
    }

    if (startY == endY) {
      return segmentNum <= mid ? -hypotenuseLen : hypotenuseLen;
    }

    double slop = (endY - startY) / (endX - startX);
    double yd = Math.sqrt(Math.pow(hypotenuseLen, 2) / (1 + Math.pow(slop, 2)));

    if (isSameRank) {
      return segmentNum <= mid ? -yd : yd;
    }
    return segmentNum <= mid == slop < 0 ? -yd : yd;
  }

  // --------------------------------------------- Abstract DotLinesHandlerFactory ---------------------------------------------

  public abstract static class AbstractDotLineRouterFactory<T extends AbstractDotLineRouter>
      implements DotLineRouterFactory<T> {

    @Override
    public T newInstance(DrawGraph drawGraph, DotDigraph dotDigraph, RankContent rankContent,
                         EdgeDedigraph<DNode, DLine> digraphProxy) {
      Asserts.nullArgument(drawGraph, "drawGraph");
      Asserts.nullArgument(dotDigraph, "dotDigraph");
      Asserts.nullArgument(rankContent, "rankContent");
      Asserts.nullArgument(digraphProxy, "digraphProxy");

      T t = newInstance();
      Asserts.nullArgument(t, "DotLineRouter");
      t.drawGraph = drawGraph;
      t.dotDigraph = dotDigraph;
      t.rankContent = rankContent;
      t.digraphProxy = digraphProxy;
      return t;
    }

    protected abstract T newInstance();
  }

  static class InOutPointPair {

    private final int idx;

    private final boolean deleteBefore;

    private final FlatPoint in;

    private final FlatPoint out;

    public InOutPointPair(int idx, boolean deleteBefore, FlatPoint in, FlatPoint out) {
      this.idx = idx;
      this.deleteBefore = deleteBefore;
      this.in = in;
      this.out = out;
    }

    public int getIdx() {
      return idx;
    }

    public boolean isDeleteBefore() {
      return deleteBefore;
    }

    public FlatPoint getIn() {
      return in;
    }

    public FlatPoint getOut() {
      return out;
    }
  }
}
