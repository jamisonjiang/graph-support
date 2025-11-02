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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.graphper.api.Line;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Splines;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

public class LineHandler {

  protected DrawGraph drawGraph;

  protected LayoutGraph<?, ?> layoutGraph;

  protected boolean isSplineNone() {
    return drawGraph.getGraphviz().graphAttrs().getSplines() == Splines.NONE;
  }

  /**
   * Logic for drawing parallel edges with the same endpoints.
   *
   * @param parallelLines parallel lines
   */
  @SuppressWarnings("unchecked")
  protected void handleSameEndpointParallelLines(List<ALine> parallelLines) {
    symmetryParallelLine(parallelLines);
  }

  /**
   * If the edge is a union of multiple parallel edges, generate a simulated path of the paralle
   * edges.
   *
   * @param line line to be detected
   */
  @SuppressWarnings("unchecked")
  protected void parallelLineHandle(ALine line) {
    if (line == null || !line.isParallelMerge() || isSplineNone()) {
      return;
    }

    Map<Integer, List<ALine>> parallelLineRecordMap = groupParallelLineByEndpoint(line);

    for (Entry<Integer, List<ALine>> entry : parallelLineRecordMap.entrySet()) {
      List<ALine> parallelLines = entry.getValue();
      handleSameEndpointParallelLines(parallelLines);
    }
  }

  /**
   * Draw parallel sides with an axis of symmetry.
   *
   * @param parallelLines parallel edges
   */
  @SuppressWarnings("unchecked")
  protected void symmetryParallelLine(List<? extends ALine> parallelLines) {
    if (CollectionUtils.isEmpty(parallelLines)) {
      return;
    }

    ALine<? extends ANode, ? extends ALine> line = parallelLines.get(0);
    ANode from = line.from();
    ANode to = line.to();
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

  @SuppressWarnings("unchecked")
  protected Map<Integer, List<ALine>> groupParallelLineByEndpoint(ALine line) {
    Map<Integer, List<ALine>> parallelLineRecordMap = new HashMap<>(0);

    for (int i = 0; i < line.getParallelNums(); i++) {
      ALine<? extends ANode, ? extends ALine> edge = line.parallelLine(i);
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(edge.getLine());
      ANode from = edge.from();
      ANode to = edge.to();
      Port fromPort = PortHelper.getLineEndPointPort(from.getNodeDrawProp(), edge.getLineDrawProp(), drawGraph);
      Port toPort = PortHelper.getLineEndPointPort(to.getNodeDrawProp(), edge.getLineDrawProp(), drawGraph);
      String headCell = lineDrawProp.lineAttrs().getHeadCell();
      String tailCell = lineDrawProp.lineAttrs().getTailCell();

      String sign = signature(fromPort, tailCell) + signature(toPort, headCell);
      sign += signature(toPort, tailCell) + signature(fromPort, headCell);
      parallelLineRecordMap.computeIfAbsent(sign.hashCode(), h -> new ArrayList<>(1)).add(edge);
    }

    return parallelLineRecordMap;
  }

  private String signature(Port port, String cellId) {
    String sign = port != null ? port.name() : "";
    return sign + cellId;
  }

  @SuppressWarnings("unchecked")
  private void parallelEdges(ALine<? extends ANode, ? extends ALine> parallelLine, int size,
                             double distUnit, int no) {
    ANode from = parallelLine.from();
    ANode to = parallelLine.to();

    FlatPoint fromPoint = PortHelper.getPortPoint(parallelLine.getLineDrawProp(), from, drawGraph);
    FlatPoint toPoint = PortHelper.getPortPoint(parallelLine.getLineDrawProp(), to, drawGraph);

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

  @SuppressWarnings("unchecked")
  private FlatPoint newParallelControlPoint(ALine<? extends ANode, ? extends ALine> line, int size, int no, double hypotenuseLen,
                                            FlatPoint f, FlatPoint t, FlatPoint v3Center) {
    return new FlatPoint(
        v3Center.getX() + xDist(f.getX(), f.getY(), t.getX(),
                                t.getY(), hypotenuseLen, no, size / 2),
        v3Center.getY() + yDist(f.getX(), f.getY(), t.getX(), t.getY(),
                                hypotenuseLen, no, size / 2)
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

  private double yDist(double startX, double startY, double endX, double endY,
                       double hypotenuseLen, int segmentNum, int mid) {

    if (startX == endX) {
      return 0;
    }

    if (startY == endY) {
      return segmentNum <= mid ? -hypotenuseLen : hypotenuseLen;
    }

    double slop = (endY - startY) / (endX - startX);
    double yd = Math.sqrt(Math.pow(hypotenuseLen, 2) / (1 + Math.pow(slop, 2)));

    return segmentNum <= mid == slop < 0 ? -yd : yd;
  }
}
