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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.Cell;
import org.graphper.layout.Cell.RootCell;
import org.graphper.layout.FlatShifterStrategy;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.CollectionUtils;
import org.graphper.util.ValueUtils;

/**
 * Common parent class for some routing algorithms that use box as bootstrap.
 *
 * @author Jamison Jiang
 * @see Splines#SPLINE
 * @see Splines#ROUNDED
 * @see Splines#POLYLINE
 */
abstract class BoxGuideLineRouter extends AbstractDotLineRouter {

  private static final int PORT_ADAPT_LEN = 10;

  private static final int HALF_PORT_ADAPT_LEN = PORT_ADAPT_LEN / 2;

  @Override
  protected Object attach() {
    return new ArrayList<RouterBox>();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean nodeConsumer(DNode node, Object attach) {
    if (node.isVirtual()) {
      if (node.isLabelNode()) {
        lineLabelSet(node);
      } else if (node.isFlatLabelNode()) {
        flatLineLabelSet(node);
      }

      return true;
    }

    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void lineConsumer(DLine line, Object attach) {
    List<RouterBox> lineRouterBoxes = (List<RouterBox>) attach;
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());

    // Skip already drawn edges
    if (CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }

    if (line.isSameRank()) {
      for (int i = 1; i <= line.getParallelNums(); i++) {
        DLine edge = line.parallelLine(i - 1);
        addFlatEdgeBoxes(line, lineRouterBoxes, i);
        lineCompute(edge.getLine(), drawGraph.getLineDrawProp(edge.getLine()),
                    lineRouterBoxes, line.from(), line.to());
        lineRouterBoxes.clear();
      }
    } else {
      // Record the double-ended node of the original edge.
      DNode[] ports = new DNode[2];
      lineSegmentConsumer(
          line,
          l -> addBoxes(l, lineRouterBoxes, ports)
      );

      if (ports[0] != null && ports[1] != null) {
        // Specific real line segment calculation, both ends are real nodes.
        lineCompute(line.getLine(), lineDrawProp, lineRouterBoxes, ports[0], ports[1]);
        lineRouterBoxes.clear();
      }
    }
  }

  protected void lineDrawPropConnect(LineDrawProp lineDrawProp,
                                     List<FlatPoint> target, boolean before) {
    if (Objects.isNull(lineDrawProp) || CollectionUtils.isEmpty(target)) {
      return;
    }

    if (before) {
      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        target.remove(target.size() - 1);
      }
      for (int i = target.size() - 1; i >= 0; i--) {
        lineDrawProp.add(0, target.get(i));
      }
    } else {
      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        target.remove(0);
      }
      lineDrawProp.addAll(target);
    }
  }

  @Override
  protected void handleSameEndpointParallelLines(List<DLine> parallelLines) {
    if (CollectionUtils.isEmpty(parallelLines)) {
      return;
    }

    DLine line = parallelLines.get(0);
    DNode from = line.from();
    DNode to = line.to();
    Port fromPort = PortHelper.getLineEndPointPort(from.getNode(), line.getLine(), drawGraph);
    Port toPort = PortHelper.getLineEndPointPort(to.getNode(), line.getLine(), drawGraph);

    if ((fromPort == null && toPort == null) || parallelLines.size() > 1) {
      symmetryParallelLine(parallelLines);
      return;
    }

    if (!Boolean.TRUE.toString()
        .equalsIgnoreCase(System.getProperty("parallel.lines.case.distinction"))) {
      if (parallelLines.size() > 1) {
        symmetryParallelLine(parallelLines);
      }

      lineConsumer(parallelLines.get(0), new ArrayList<Box>());
      return;
    }

    FlatPoint fromPoint = PortHelper.getPortPoint(from, fromPort);
    FlatPoint toPoint = PortHelper.getPortPoint(to, toPort);

    double distUnit = (drawGraph.getGraphviz().graphAttrs().getNodeSep()
        + drawGraph.getGraphviz().graphAttrs().getRankSep()
        + FlatPoint.twoFlatPointDistance(fromPoint, toPoint)) / 20;

    List<RouterBox> routerBoxes = (List<RouterBox>) attach();
    if (line.isSameRank()) {
      RankNode rankNode = rankContent.get(from.getRank());
      RankNode pre = rankNode.pre();
      RankNode next = rankNode.pre();

      double minY = pre != null ? pre.getEndY() : 2 * rankNode.getStartY() - rankNode.getEndY();
      double maxY = next != null ? next.getEndY() : 2 * rankNode.getEndY() - rankNode.getStartY();

      DefaultShapePosition sp = new DefaultShapePosition(
          (fromPoint.getX() + toPoint.getX()) / 2,
          (fromPoint.getY() + toPoint.getY()) / 2,
          Math.max(from.getHeight(), to.getHeight()),
          Math.max(from.getWidth(), to.getWidth()),
          NodeShapeEnum.RECT
      );
      minY = Math.min(sp.getY() - sp.getHeight() * parallelLines.size(), minY);
      maxY = Math.max(sp.getY() + sp.getHeight() * parallelLines.size(), maxY);

      sameRankParallelLineDraw(sp, true, rankNode, minY, maxY, parallelLines);
    } else {
      RouterBox fromBox = newTwoNodeRangeBox(from);
      RouterBox toBox = newTwoNodeRangeBox(to);

      if (fromBox.getWidth() < distUnit * parallelLines.size()) {
        fromBox.setLeftBorder((fromBox.getLeftBorder() + fromBox.getRightBorder()) / 2
                                  - distUnit * parallelLines.size() / 2);
        fromBox.setRightBorder((fromBox.getLeftBorder() + fromBox.getRightBorder()) / 2
                                   + distUnit * parallelLines.size() / 2);
      }
      if (toBox.getWidth() < distUnit * parallelLines.size()) {
        toBox.setLeftBorder((toBox.getLeftBorder() + toBox.getRightBorder()) / 2
                                - distUnit * parallelLines.size() / 2);
        toBox.setRightBorder((toBox.getLeftBorder() + toBox.getRightBorder()) / 2
                                 + distUnit * parallelLines.size() / 2);
      }

      double verHeight = Math.abs(fromBox.getDownBorder() - toBox.getUpBorder());
      double startY = fromBox.getDownBorder() + verHeight / 8;
      double endY = fromBox.getDownBorder() + verHeight * 7 / 8;
      double wall = (fromPoint.getX() + toPoint.getX()) / 2 - (parallelLines.size() / 2) * distUnit
          - distUnit / 2;
      if (fromPort == Port.WEST) {
        wall -= HALF_PORT_ADAPT_LEN;
      } else if (fromPort == Port.EAST) {
        wall += HALF_PORT_ADAPT_LEN;
      }
      if (toPort == Port.WEST) {
        wall -= HALF_PORT_ADAPT_LEN;
      } else if (toPort == Port.EAST) {
        wall += HALF_PORT_ADAPT_LEN;
      }

      for (int i = 0; i < parallelLines.size(); i++) {
        DLine parallelLine = parallelLines.get(i);
        Line edge = parallelLine.getLine();

        routerBoxes.add(fromBox);
        routerBoxes.add(new RouterBox(wall, wall + distUnit, startY, endY));
        routerBoxes.add(toBox);

        lineCompute(edge, drawGraph.getLineDrawProp(edge), routerBoxes, from, to);
        routerBoxes.clear();
        if (parallelLines.size() % 2 == 0 && i == parallelLines.size() / 2 - 1) {
          wall += (2 * distUnit);
        } else {
          wall += distUnit;
        }
      }
    }
  }

  protected abstract boolean curveLine();

  protected abstract void throughPointHandle(ThroughParam throughParam);

  // ----------------------------------------------------- private method -----------------------------------------------------

  private void sameRankParallelLineDraw(ShapePosition shapePosition, boolean isSameRank,
                                        RankNode rank, double minY, double maxY,
                                        List<DLine> parallelLines) {
    if (CollectionUtils.isEmpty(parallelLines)) {
      return;
    }

    Double upBaseLine = null;
    Double downBaseLine = null;
    Double labelY = null;
    double itemsMinY = Double.MAX_VALUE;
    double itemsMaxY = -Double.MAX_VALUE;
    Map<Line, LineDrawProp> lineDrawPropMap = drawGraph.getLineDrawPropMap();
    List<FlatParallelLineParam> flatParallelLineParams = new ArrayList<>(parallelLines.size());

    for (int j = 0; j < parallelLines.size(); j++) {
      DLine line = parallelLines.get(j);
      DNode from = line.from();
      DNode to = line.to();

      from = from.getX() > to.getX() ? to : from;
      to = line.other(from);

      double leftMin = from.getX() - from.leftWidth();
      double leftMax = from.getX() + from.rightWidth() + from.getNodeSep() / 3;
      DNode pn = rankContent.rankPreNode(to);
      double rightMin = to.getX() - to.leftWidth() - (pn != null ? pn.getNodeSep() / 3 : 0);
      double rightMax = to.getX() + to.rightWidth();
      RouterBox left = new RouterBox(leftMin, leftMax, minY, maxY, from);
      RouterBox right = new RouterBox(rightMin, rightMax, minY, maxY, to);

      List<RouterBox> lineRouterBoxes = new ArrayList<>();
      lineRouterBoxes.add(left);
      lineRouterBoxes.add(right);

      RouterBox routerBox;
      LineDrawProp lineDrawProp = lineDrawPropMap.get(line.getLine());
      if (!lineDrawProp.isEmpty()) {
        lineDrawProp.clear();
      }
      FlatPoint labelSize = line.getLabelSize();

      boolean alternateDraw = isSameRank;
      Boolean upDirect = null;
      if (alternateDraw) {
        Port fromPort = PortHelper.getLineEndPointPort(from.getNode(), line.getLine(), drawGraph);
        Port toPort = PortHelper.getLineEndPointPort(to.getNode(), line.getLine(), drawGraph);
        FlatPoint fromPoint = PortHelper.getPortPoint(from, fromPort);
        FlatPoint toPoint = PortHelper.getPortPoint(to, toPort);
        upDirect = fromPoint.getY() - shapePosition.getY() + toPoint.getY() - to.getY() <= 0;
        alternateDraw = (fromPort == null || toPort == null)
            || fromPort == Port.EAST && toPort == Port.WEST
            || (fromPoint.getY() - from.getY() < 0) != (toPoint.getY() - to.getY() < 0);
      }

      // Divided into two cases: the flat label node is between two nodes and the
      // level adjacent to the level where the two nodes are located.
      if (alternateDraw) {
        // The label is always placed on the upper end of the line, and it goes up and down
        // according to the middle axis of symmetry.
        if (j % 2 == 0) {
          if (upBaseLine == null) {
            upBaseLine = shapePosition.getY();
          }
          routerBox = new RouterBox(leftMax, rightMin,
                                    upBaseLine - (labelSize != null ? labelSize.getX() : 10),
                                    upBaseLine);

          upBaseLine = routerBox.getUpBorder();
          if (labelSize != null) {
            labelY = routerBox.getDownBorder() - labelSize.getX() / 2;
          }
        } else {
          if (downBaseLine == null) {
            downBaseLine = shapePosition.getY();
          }
          downBaseLine = downBaseLine + (labelSize != null ? labelSize.getX() : 10);
          routerBox = new RouterBox(leftMax, rightMin, downBaseLine,
                                    downBaseLine + (labelSize != null ? labelSize.getX() : 10));

          if (labelSize != null) {
            labelY = routerBox.getUpBorder() - labelSize.getX() / 2;
          }
        }
      } else {
        // The label is always placed on the upper end of the line, and it goes
        // up and down according to the middle axis of symmetry.
        if (j % 2 == 0) {
          if (upBaseLine == null) {
            if (upDirect == null) {
              upBaseLine = shapePosition.getY();
            } else if (Objects.equals(upDirect, Boolean.TRUE)) {
              upBaseLine = shapePosition.getY() - shapePosition.getHeight() - PORT_ADAPT_LEN - 10;
            } else {
              upBaseLine = from.getY() + shapePosition.getHeight() + PORT_ADAPT_LEN + 10;
            }
          }
          routerBox = new RouterBox(leftMax, rightMin,
                                    upBaseLine - (labelSize != null ? labelSize.getX() : 10),
                                    upBaseLine);

          upBaseLine = upBaseLine - (labelSize != null ? labelSize.getX() : 10);
        } else {
          if (downBaseLine == null) {
            if (upDirect == null) {
              downBaseLine = shapePosition.getY();
            } else if (Objects.equals(upDirect, Boolean.TRUE)) {
              downBaseLine = shapePosition.getY() - shapePosition.getHeight() - PORT_ADAPT_LEN - 10;
            } else {
              downBaseLine = from.getY() + shapePosition.getHeight() + PORT_ADAPT_LEN + 10;
            }
          }
          double p = downBaseLine;
          downBaseLine = downBaseLine + (labelSize != null ? labelSize.getX() : 10);
          routerBox = new RouterBox(leftMax, rightMin, p, downBaseLine);
        }

        if (labelSize != null) {
          labelY = routerBox.getDownBorder() - labelSize.getX() / 2;
        }
      }

      if (j % 2 == 0) {
        itemsMinY = Math.min(itemsMinY, routerBox.getDownBorder());
        itemsMaxY = Math.max(itemsMaxY, routerBox.getDownBorder());
      } else {
        itemsMinY = Math.min(itemsMinY, routerBox.getUpBorder());
        itemsMaxY = Math.max(itemsMaxY, routerBox.getUpBorder());
      }

      if (labelSize != null && labelY != null) {
        itemsMinY = Math.min(itemsMinY, labelY - labelSize.getHeight() / 2);
        itemsMaxY = Math.max(itemsMaxY, labelY + labelSize.getHeight() / 2);
      }

      lineRouterBoxes.add(lineRouterBoxes.size() - 1, routerBox);
      if (labelY != null) {
        lineDrawProp.setLabelCenter(new FlatPoint(shapePosition.getX(), labelY));
        labelY = null;
      }

      lineDrawProp.clear();
      flatParallelLineParams.add(
          new FlatParallelLineParam(from, to, lineDrawProp, lineRouterBoxes));
    }

    for (FlatParallelLineParam parallelLineParam : flatParallelLineParams) {
      LineAttrs lineAttrs = parallelLineParam.line.lineAttrs();
      if (!havePort(lineAttrs) && (itemsMinY < rank.getStartY() || itemsMaxY > rank.getEndY())) {
        double offset = rank.getStartY() - itemsMinY;
        FlatShifterStrategy shifter = new FlatShifterStrategy(0, offset);

        for (RouterBox routerBox : parallelLineParam.routerBoxes) {
          shifter.moveBox(routerBox);
        }
        shifter.movePoint(parallelLineParam.line.getLabelCenter());
      }

      drawGraph.updateYAxisRange(itemsMinY);
      drawGraph.updateYAxisRange(itemsMaxY);
      lineCompute(parallelLineParam.line.getLine(), parallelLineParam.line,
                  parallelLineParam.routerBoxes, parallelLineParam.from, parallelLineParam.to);
    }
  }

  private boolean havePort(LineAttrs lineAttrs) {
    return lineAttrs.getTailPort() != null || lineAttrs.getHeadPort() != null;
  }

  private void lineLabelSet(DNode node) {
    Line line = node.getLabelLine();
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    if (lineDrawProp == null) {
      return;
    }

    lineDrawProp.setLabelCenter(new FlatPoint(node.getX() + node.getWidth() / 2, node.getY()));
  }

  private void flatLineLabelSet(DNode node) {
    DLine flatLabelLine = node.getFlatLabelLine();

    Map<Line, LineDrawProp> lineDrawPropMap = drawGraph.getLineDrawPropMap();
    if (lineDrawPropMap == null) {
      return;
    }

    RankNode rankNode = rankContent.get(node.getRank());
    RankNode pre = rankNode.pre();
    RankNode next = rankNode.next();

    // The lowest ordinate of the box.
    double minY = pre != null ? pre.getEndY() : 2 * rankNode.getStartY() - rankNode.getEndY();
    // The highest vertical coordinate of the box.
    double maxY = next != null ? next.getEndY() : 2 * rankNode.getEndY() - rankNode.getStartY();

    // The minimum and maximum need to be compatible with the size of the flat label line.
    minY = Math.min(node.getY() - node.getHeight() * flatLabelLine.getParallelNums(), minY);
    maxY = Math.max(node.getY() + node.getHeight() * flatLabelLine.getParallelNums(), maxY);

    Map<Integer, List<DLine>> parallelLineRecordMap = groupParallelLineByEndpoint(flatLabelLine);

    for (Entry<Integer, List<DLine>> entry : parallelLineRecordMap.entrySet()) {
      DNode from = flatLabelLine.from();
      sameRankParallelLineDraw(node, node.getRank() == from.getRank(), rankNode,
                               minY, maxY, entry.getValue());
    }
  }

  private void addBoxes(DLine line, List<RouterBox> lineRouterBoxes, DNode[] ports) {
    DNode from = line.from();
    DNode to = line.to();

    if (!from.isVirtual()) {
      ports[0] = from;
      // Add the box for "from" node.
      lineRouterBoxes.add(newTwoNodeRangeBox(from));
    }
    if (!to.isVirtual()) {
      ports[1] = to;
    }

    RankNode rankNode = rankContent.get(from.getRank());

    addRankBox(rankNode, lineRouterBoxes);

    // Add the box for "to" node.
    lineRouterBoxes.add(newTwoNodeRangeBox(to));
  }

  private void addFlatEdgeBoxes(DLine line, List<RouterBox> lineRouterBoxes, int no) {
    if (isAdj(line.from(), line.to())) {
      lineRouterBoxes.add(newTwoNodeRangeBox(line.from()));
      lineRouterBoxes.add(newTwoNodeRangeBox(line.to()));
      return;
    }

    RankNode rankNode = rankContent.get(line.from().getRank());
    RankNode pre = preNotOnlyLabelRankNode(rankNode);
    RankNode next = rankNode.next();

    flatLineBoxes(line, lineRouterBoxes, rankNode, pre, next, no);
  }

  private void flatLineBoxes(DLine line, List<RouterBox> lineRouterBoxes,
                             RankNode rankNode, RankNode pre, RankNode next, int lineNo) {
    double minY = pre != null ? pre.getEndY() : 2 * rankNode.getStartY() - rankNode.getEndY();
    double maxY = next != null ? next.getStartY() : 2 * rankNode.getEndY() - rankNode.getStartY();
    double maxHeight = pre != null
        ? rankNode.getStartY() - pre.getEndY()
        : rankNode.getRankSep();
    double nodeSep = drawGraph.getGraphviz().graphAttrs().getNodeSep();

    DNode start = line.from().getRankIndex() < line.to().getRankIndex() ? line.from() : line.to();
    DNode end = line.other(start);

    lineRouterBoxes.add(
        new RouterBox(
            start.getX() - start.leftWidth(),
            start.getX() + start.rightWidth() + nodeSep,
            minY,
            maxY,
            start
        )
    );

    lineRouterBoxes.add(
        new RouterBox(
            start.getX() + start.rightWidth() + nodeSep,
            end.getX() - end.leftWidth() - nodeSep,
            rankNode.getStartY() - maxHeight,
            rankNode.getStartY() - maxHeight + (maxHeight * lineNo / (line.getParallelNums() + 1))
        )
    );

    lineRouterBoxes.add(
        new RouterBox(
            end.getX() - end.leftWidth() - nodeSep,
            end.getX() + end.rightWidth(),
            minY,
            maxY,
            end
        )
    );
  }

  private RankNode preNotOnlyLabelRankNode(RankNode rankNode) {
    RankNode pre;

    do {
      pre = rankNode.pre();
      rankNode = pre;
    } while (pre != null && pre.noNormalNode());

    return pre;
  }

  private void lineCompute(Line line, LineDrawProp lineDrawProp,
                           List<RouterBox> lineRouterBoxes, DNode from, DNode to) {
    if (CollectionUtils.isEmpty(lineRouterBoxes) || CollectionUtils.isNotEmpty(lineDrawProp)) {
      return;
    }

    List<RouterBox> originRouterBoxes = splitPortBox(from.getRank() != to.getRank(),
                                                     lineDrawProp, lineRouterBoxes);

    List<ThroughPoint> throughPoints = null;
    RouterBox pre = null;
    Integer preIdx = null;
    for (int i = 0; i < lineRouterBoxes.size(); i++) {
      RouterBox routerBox = lineRouterBoxes.get(i);
      DNode node = routerBox.getNode();

      if (node == null) {
        continue;
      } else if (pre == null) {
        lineDrawProp.setIsHeadStart(node.getNode());
      }

      if (pre != null) {
        DNode preNode = pre.getNode();
        ThroughPoint start = getLineEndPoint(preNode, line, preIdx);
        ThroughPoint end = getLineEndPoint(node, line, i);
        if (throughPoints == null) {
          throughPoints = new ArrayList<>();
        }
        throughPoints.add(start);

        if (end.getY() > start.getY()) {
          if (preIdx == 0) {
            double x = Vectors.linerFuncGetX(start, end, pre.getDownBorder());
            if (x > pre.getLeftBorder() && x < start.getX()) {
              pre.setLeftBorder(x - 10);
            } else if (x < pre.getRightBorder() && x > start.getX()) {
              pre.setRightBorder(x + 10);
            }
          } else if (i == lineRouterBoxes.size() - 1) {
            double x = Vectors.linerFuncGetX(start, end, routerBox.getUpBorder());
            if (x > routerBox.getLeftBorder() && x < end.getX()) {
              routerBox.setLeftBorder(x - 10);
            } else if (x < routerBox.getRightBorder() && x > end.getX()) {
              routerBox.setRightBorder(x + 10);
            }
          }
        }

        throughPointCompute(throughPoints, lineRouterBoxes, preIdx, i, throughPoints.size(),
                            start, end, from.getRank() != to.getRank());
      }

      pre = routerBox;
      preIdx = i;
    }

    RouterBox routerBox = lineRouterBoxes.get(lineRouterBoxes.size() - 1);
    DNode node = routerBox.getNode();
    if (node != null) {
      ThroughPoint end = getLineEndPoint(node, line, lineRouterBoxes.size() - 1);
      if (throughPoints == null) {
        throughPoints = new ArrayList<>();
      }
      throughPoints.add(end);
    }

    if (Objects.equals(lineDrawProp.lineAttrs().getShowboxes(), Boolean.TRUE)) {
      lineDrawProp.setBoxes(new ArrayList<>(lineRouterBoxes));
    }

    if (CollectionUtils.isNotEmpty(throughPoints)) {
      ThroughParam throughParam = new ThroughParam();
      throughParam.line = line;
      throughParam.lineRouterBoxes = lineRouterBoxes;
      throughParam.lineDrawProp = lineDrawProp;
      throughParam.from = from;
      throughParam.to = to;
      throughParam.throughPoints = throughPoints;
      throughParam.isHorizontal = from.getRank() == to.getRank();

      if (CollectionUtils.isNotEmpty(originRouterBoxes)) {
        int startIdx = 0;
        int endIdx = throughPoints.size() - 1;

        for (RouterBox originRouterBox : originRouterBoxes) {
          if (originRouterBox.getNode() == from) {
            int i = 0;
            for (; i < throughPoints.size(); i++) {
              if (!originRouterBox.in(throughPoints.get(i))) {
                break;
              }
            }

            if (i > 1) {
              if (curveLine()) {
                FlatPoint rightTangent = null;
                if (i < throughPoints.size()) {
                  rightTangent = Vectors.sub(throughPoints.get(i - 1), throughPoints.get(i));
                }
                MultiBezierCurve curves = Curves.fitCurves(throughPoints.subList(0, i),
                                                           null, rightTangent, 0);
                throughParam.fromPortPoints = multiBezierCurveToPoints(curves);
              } else {
                throughParam.fromPortPoints = new ArrayList<>(i);
                throughParam.fromPortPoints.addAll(throughPoints.subList(0, i));
              }
              startIdx = i - 1;
            }
          } else {
            int i = throughPoints.size() - 1;
            for (; i >= 0; i--) {
              if (!originRouterBox.in(throughPoints.get(i))) {
                break;
              }
            }

            if (i < throughPoints.size() - 2) {
              if (curveLine()) {
                FlatPoint leftTangent = null;
                if (i >= 0) {
                  leftTangent = Vectors.sub(throughPoints.get(i + 1), throughPoints.get(i));
                }
                MultiBezierCurve curves = Curves
                    .fitCurves(throughPoints.subList(i + 1, throughPoints.size()),
                               leftTangent, null, 0);
                throughParam.toPortPoints = multiBezierCurveToPoints(curves);
              } else {
                i = Math.max(i, 0);
                throughParam.toPortPoints = new ArrayList<>(i);
                throughParam.toPortPoints.addAll(throughPoints.subList(i, throughPoints.size()));
              }
              endIdx = i + 1;
            }
          }
        }

        if (startIdx != 0 || endIdx != throughPoints.size() - 1) {
          throughParam.throughPoints = throughPoints.subList(startIdx, endIdx + 1);
        }
      }

      throughPointHandle(throughParam);
    }
  }

  private int throughPointCompute(List<ThroughPoint> throughPoints,
                                  List<RouterBox> lineRouterBoxes, int boxStartIndex,
                                  int boxEndIndex, int insertIndex, FlatPoint start,
                                  FlatPoint end, boolean vertical) {
    if ((vertical && start.getY() == end.getY())
        || (!vertical && start.getX() == end.getX())) {
      return 0;
    }

    Integer splitIndex = null;
    double fastDistance = -Double.MAX_VALUE;
    Double fastX = null, fastY = null;

    for (int i = boxStartIndex; i <= boxEndIndex; i++) {
      RouterBox routerBox = lineRouterBoxes.get(i);

      double p1, p2;
      boolean p1In, p2In;

      if (vertical) {
        p1 = Vectors
            .linerFuncGetX(start.getX(), start.getY(), end.getX(), end.getY(),
                           routerBox.getUpBorder());
        p2 = Vectors
            .linerFuncGetX(start.getX(), start.getY(), end.getX(), end.getY(),
                           routerBox.getDownBorder());
        // There will be a certain accuracy error in the double calculation, and the error will
        // be repaired by shifting back and forth by 1.
        p1In = routerBox.inXRange(p1 - 1) || routerBox.inXRange(p1 + 1)
            || routerBox.getUpBorder() < start.getY();
        p2In = routerBox.inXRange(p2 - 1) || routerBox.inXRange(p2 + 1)
            || routerBox.getDownBorder() > end.getY();
      } else {
        p1 = Vectors
            .linerFuncGetY(start.getX(), start.getY(), end.getX(), end.getY(),
                           routerBox.getLeftBorder());
        p2 = Vectors
            .linerFuncGetY(start.getX(), start.getY(), end.getX(), end.getY(),
                           routerBox.getRightBorder());
        // There will be a certain accuracy error in the double calculation, and the error will
        // be repaired by shifting back and forth by 1.
        p1In = routerBox.inYRange(p1 - 1) || routerBox.inYRange(p1 + 1)
            || routerBox.getLeftBorder() < start.getX();
        p2In = routerBox.inYRange(p2 - 1) || routerBox.inYRange(p2 + 1)
            || routerBox.getRightBorder() > end.getX();
      }

      if (p1In && p2In) {
        continue;
      }

      double wall;
      if (!p1In) {
        wall = vertical ? routerBox.closerVerWall(p1) : routerBox.closerHorWall(p1);
        double dis = Math.abs(wall - p1);
        if (fastDistance < dis) {
          fastDistance = dis;
          splitIndex = Math.max(i - 1, boxStartIndex);
          if (vertical) {
            fastX = wall;
            fastY = routerBox.getUpBorder();
          } else {
            fastX = routerBox.getLeftBorder();
            fastY = wall;
          }
        }
      }

      if (!p2In) {
        wall = vertical ? routerBox.closerVerWall(p2) : routerBox.closerHorWall(p2);
        double dis = Math.abs(wall - p2);
        if (fastDistance < dis) {
          fastDistance = dis;
          splitIndex = i;
          if (vertical) {
            fastX = wall;
            fastY = routerBox.getDownBorder();
          } else {
            fastX = routerBox.getRightBorder();
            fastY = wall;
          }
        }
      }
    }

    // The two real vertex line segments are both inside the box, return directly.
    if (splitIndex == null) {
      return 0;
    }

    // Recursively split the original line segment into two segments.
    ThroughPoint splitPoint = new ThroughPoint(fastX, fastY, splitIndex);
    throughPoints.add(insertIndex, splitPoint);
    int a = throughPointCompute(throughPoints, lineRouterBoxes, boxStartIndex,
                                splitIndex, insertIndex, start, splitPoint, vertical);
    int b = throughPointCompute(throughPoints, lineRouterBoxes, splitIndex + 1,
                                boxEndIndex, insertIndex + a + 1, splitPoint, end, vertical);
    return a + b + 1;
  }

  /*
   * The current node creates a box between the left and right nodes. If the current node is
   * virtual, it will skip a crossed virtual node on the left or right.
   */
  private RouterBox newTwoNodeRangeBox(DNode node) {
    DNode pre = adjIgnoreHaveCrossVirtualNode(
        node,
        rankContent::rankPreNode
    );
    DNode next = adjIgnoreHaveCrossVirtualNode(
        node,
        rankContent::rankNextNode
    );

    RankNode rankNode = rankContent.get(node.getRank());

    double leftWall =
        pre != null ? pre.getX() + pre.rightWidth() + (pre.getNodeSep() / 2) : drawGraph.getMinX();
    double rightWall;
    if (node.isLabelNode()) {
      rightWall = node.getX() - node.leftWidth() + LABEL_NODE_SIDE_MAX_DISTANCE;
    } else {
      rightWall = next != null
          ? next.getX() - next.leftWidth() - (node.getNodeSep() / 2)
          : leftWall + drawGraph.width();
      rightWall = Math.max(rightWall, node.getX() + node.rightWidth());
    }

    leftWall = Math.min(leftWall, node.getX() - node.leftWidth());
    return new RouterBox(
        leftWall - 2,
        rightWall + 2,
        rankNode.getStartY() - 2,
        rankNode.getEndY() + 2,
        node
    );
  }

  private void addRankBox(RankNode rankNode, List<RouterBox> routerBoxes) {
    RankNode next = rankNode.next();

    if (next == null) {
      return;
    }

    routerBoxes.add(
        new RouterBox(
            drawGraph.getMinX(),
            drawGraph.getMaxX(),
            rankNode.getEndY(),
            next.getStartY()
        )
    );
  }

  // Find the left or right node of the node, and choose to skip the neighbor node
  // based on the principle of crossing.
  private DNode adjIgnoreHaveCrossVirtualNode(DNode current, UnaryOperator<DNode> adjFunc) {
    DNode next = adjFunc.apply(current);

    if (next == null || !next.isVirtual() || next.isLabelNode()) {
      return next;
    }

    DLine l1 = null, l2 = null;
    for (DLine dLine : digraphProxy.inAdjacent(current)) {
      l1 = dLine;
    }
    for (DLine dLine : digraphProxy.inAdjacent(next)) {
      l2 = dLine;
    }

    // There is a crossover, skip directly.
    if (lineIsCross(l1, l2)) {
      return adjFunc.apply(next);
    }

    for (DLine dLine : digraphProxy.outAdjacent(current)) {
      l1 = dLine;
    }
    for (DLine dLine : digraphProxy.outAdjacent(next)) {
      l2 = dLine;
    }

    // There is a crossover, skip directly.
    if (lineIsCross(l1, l2)) {
      return adjFunc.apply(next);
    }

    return next;
  }

  private List<RouterBox> splitPortBox(boolean vertical, LineDrawProp lineProp,
                                       List<RouterBox> routerBoxes) {
    if (CollectionUtils.isEmpty(routerBoxes) || routerBoxes.size() < 2) {
      return Collections.emptyList();
    }

    boolean firstSplit = false;
    boolean lastSplit = false;
    RouterBox firstRouterBox = routerBoxes.get(0);
    RouterBox lastRouterBox = routerBoxes.get(routerBoxes.size() - 1);
    List<RouterBox> splitRouterBoxes = splitPortBox(firstRouterBox, lineProp, vertical, true);
    if (CollectionUtils.isNotEmpty(splitRouterBoxes)) {
      routerBoxes.remove(0);
      for (int i = splitRouterBoxes.size() - 1; i >= 0; i--) {
        routerBoxes.add(0, splitRouterBoxes.get(i));
      }
      firstSplit = true;
    }

    splitRouterBoxes = splitPortBox(lastRouterBox, lineProp, vertical, false);
    if (CollectionUtils.isNotEmpty(splitRouterBoxes)) {
      routerBoxes.remove(routerBoxes.size() - 1);
      routerBoxes.addAll(splitRouterBoxes);
      lastSplit = true;
    }

    if (CollectionUtils.isEmpty(routerBoxes)) {
      return Collections.emptyList();
    }

    // The first and last routerBox must have node
    RouterBox routerBox;
    do {
      routerBox = routerBoxes.get(0);
      if (routerBox.getNode() == null) {
        routerBoxes.remove(0);
      }
    } while (routerBox.getNode() == null);

    do {
      routerBox = routerBoxes.get(routerBoxes.size() - 1);
      if (routerBox.getNode() == null) {
        routerBoxes.remove(routerBoxes.size() - 1);
      }
    } while (routerBox.getNode() == null);

    if (firstSplit && lastSplit) {
      return Arrays.asList(firstRouterBox, lastRouterBox);
    } else if (firstSplit) {
      return Collections.singletonList(firstRouterBox);
    } else if (lastSplit) {
      return Collections.singletonList(lastRouterBox);
    }

    return Collections.emptyList();
  }

  private List<RouterBox> splitPortBox(RouterBox routerBox, LineDrawProp lineProp,
                                       boolean vertical, boolean lowPos) {
    DNode node = routerBox.getNode();
    if (node == null) {
      return Collections.emptyList();
    }

    Port port = PortHelper.getLineEndPointPort(node.getNode(), lineProp.getLine(), drawGraph);
    if (port == null) {
      return Collections.emptyList();
    }

    FlatPoint point = PortHelper.getPortPointWithoutClip(lineProp.getLine(), node, drawGraph);
    if (!routerBox.in(point)) {
      return Collections.emptyList();
    }

    if (!pointLocateAtBorder(point, node)) {
      Box cellBox = node;
      NodeDrawProp nodeProp = drawGraph.getNodeDrawProp(node.getNode());
      Cell cell = nodeProp.getCell();
      String cellId = PortHelper.getCellId(lineProp.getLine(), node, lineProp);
      if (cell != null && (cell = ((RootCell) cell).getCellById(cellId)) != null) {
        cellBox = cell.getCellBox(node);
      }
      return verticalCellBoxSplit(vertical, point, node, cellBox, routerBox);
    }

    RouterBox routerBox1 = null;
    RouterBox routerBox2 = null;
    if (vertical) {
      if (lowPos) {
        if (!ValueUtils.approximate(point.getY(), node.getDownBorder(), 1)) {
          routerBox1 = new RouterBox(routerBox.getLeftBorder(), routerBox.getRightBorder(),
                                     routerBox.getUpBorder(), point.getY(), node);

          if (point.getX() < node.getX()) {
            double left = node.getLeftBorder();
            routerBox2 = new RouterBox(routerBox.getLeftBorder(),
                                       left - Math
                                           .min(PORT_ADAPT_LEN, left - routerBox.getLeftBorder()),
                                       point.getY(), routerBox.getDownBorder());

          } else {
            double right = node.getRightBorder();
            routerBox2 = new RouterBox(
                right + Math.min(PORT_ADAPT_LEN, routerBox.getRightBorder() - right),
                routerBox.getRightBorder(), point.getY(), routerBox.getDownBorder());
          }
        }
      } else {
        if (!ValueUtils.approximate(point.getY(), node.getUpBorder(), 1)) {
          if (point.getX() < node.getX()) {
            double left = node.getX() - node.realLeftWidth();
            routerBox1 = new RouterBox(routerBox.getLeftBorder(),
                                       left - Math
                                           .min(PORT_ADAPT_LEN, left - routerBox.getLeftBorder()),
                                       routerBox.getUpBorder(), point.getY());
          } else {
            double right = node.getX() + node.realRightWidth();
            routerBox1 = new RouterBox(
                right + Math.min(PORT_ADAPT_LEN, routerBox.getRightBorder() - right),
                routerBox.getRightBorder(), routerBox.getUpBorder(),
                point.getY());
          }
          routerBox2 = new RouterBox(routerBox.getLeftBorder(), routerBox.getRightBorder(),
                                     point.getY(), routerBox.getDownBorder(), node);
        }
      }
    } else {
      if (point.getY() <= node.getY()) {
        double top = node.getUpBorder();
        if (point.getX() < node.getX()) {
          routerBox1 = new RouterBox(routerBox.getLeftBorder(), point.getX(),
                                     routerBox.getUpBorder(), routerBox.getDownBorder(), node);
          routerBox2 = new RouterBox(point.getX(), routerBox.getRightBorder(),
                                     routerBox.getUpBorder(),
                                     top - Math.min(PORT_ADAPT_LEN, top - routerBox.getUpBorder()));
        } else {
          routerBox1 = new RouterBox(routerBox.getLeftBorder(), point.getX(),
                                     routerBox.getUpBorder(),
                                     top - Math.min(PORT_ADAPT_LEN, top - routerBox.getUpBorder()));
          routerBox2 = new RouterBox(point.getX(), routerBox.getRightBorder(),
                                     routerBox.getUpBorder(), routerBox.getDownBorder(), node);
        }
      } else {
        double bottom = node.getDownBorder();
        if (point.getX() < node.getX()) {
          routerBox1 = new RouterBox(routerBox.getLeftBorder(), point.getX(),
                                     point.getY(), routerBox.getDownBorder(), node);
          routerBox2 = new RouterBox(point.getX(), routerBox.getRightBorder(),
                                     bottom + Math
                                         .min(PORT_ADAPT_LEN, routerBox.getDownBorder() - bottom),
                                     routerBox.getDownBorder());
        } else {
          routerBox1 = new RouterBox(routerBox.getLeftBorder(), point.getX(),
                                     bottom + Math
                                         .min(PORT_ADAPT_LEN, routerBox.getDownBorder() - bottom),
                                     routerBox.getDownBorder());
          routerBox2 = new RouterBox(point.getX(), routerBox.getRightBorder(),
                                     point.getY(), routerBox.getDownBorder(), node);
        }
      }
    }

    if (routerBox1 == null || routerBox2 == null) {
      return Collections.emptyList();
    }
    routerBox1.minGuarantee(5, 5);
    routerBox2.minGuarantee(5, 5);
    return Arrays.asList(routerBox1, routerBox2);
  }

  private List<RouterBox> verticalCellBoxSplit(boolean vertical, FlatPoint point, DNode node,
                                               Box cellBox, RouterBox routerBox) {
    RouterBox routerBox1 = null;
    RouterBox routerBox2 = null;
    if (point.getY() < cellBox.getY()) {
      if (vertical) {
        routerBox1 = new RouterBox(routerBox.getLeftBorder(), routerBox.getRightBorder(),
                                   routerBox.getUpBorder(), point.getY(), node);

        if (point.getX() < cellBox.getX()) {
          double left = cellBox.getLeftBorder();
          routerBox2 = new RouterBox(routerBox.getLeftBorder(),
                                     left - Math
                                         .min(PORT_ADAPT_LEN, left - routerBox.getLeftBorder()),
                                     point.getY(), routerBox.getDownBorder());

        } else {
          double right = cellBox.getRightBorder();
          routerBox2 = new RouterBox(
              right + Math.min(PORT_ADAPT_LEN, routerBox.getRightBorder() - right),
              routerBox.getRightBorder(), point.getY(), routerBox.getDownBorder());
        }
      }
    } else {
      if (vertical) {
        if (point.getX() < cellBox.getX()) {
          double left = cellBox.getX() - node.realLeftWidth();
          routerBox1 = new RouterBox(routerBox.getLeftBorder(),
                                     left - Math
                                         .min(PORT_ADAPT_LEN, left - routerBox.getLeftBorder()),
                                     routerBox.getUpBorder(), point.getY());
        } else {
          double right = cellBox.getX() + node.realRightWidth();
          routerBox1 = new RouterBox(right + Math.min(10, routerBox.getRightBorder() - right),
                                     routerBox.getRightBorder(), routerBox.getUpBorder(),
                                     point.getY());
        }
        routerBox2 = new RouterBox(routerBox.getLeftBorder(), routerBox.getRightBorder(),
                                   point.getY(), routerBox.getDownBorder(), node);
      }
    }

    if (routerBox1 == null || routerBox2 == null) {
      return Collections.emptyList();
    }
    routerBox1.minGuarantee(5, 5);
    routerBox2.minGuarantee(5, 5);
    return Arrays.asList(routerBox1, routerBox2);
  }

  private boolean pointLocateAtBorder(FlatPoint point, DNode node) {
    return ValueUtils.approximate(point.getX(), node.getLeftBorder(), 1)
        || ValueUtils.approximate(point.getX(), node.getRightBorder(), 1)
        || ValueUtils.approximate(point.getY(), node.getUpBorder(), 1)
        || ValueUtils.approximate(point.getY(), node.getDownBorder(), 1);
  }

  private ThroughPoint getLineEndPoint(DNode n, Line line, int boxIdx) {
    if (n.isVirtual()) {
      return new ThroughPoint(n.getX(), n.getY(), boxIdx);
    }

    FlatPoint point = PortHelper.getPortPoint(line, n, drawGraph);
    return new ThroughPoint(point.getX(), point.getY(), boxIdx);
  }

  private boolean lineIsCross(DLine l1, DLine l2) {
    if (l1 == null
        || l2 == null
        || l1.from().getX() == l2.from().getX()
        || l1.to().getX() == l2.to().getX()
    ) {
      return false;
    }
    return l1.from().getX() < l2.from().getX() != l1.to().getX() < l2.to().getX();
  }

  // --------------------------------------------- Helper class ---------------------------------------------

  protected static class ThroughPoint extends FlatPoint {

    private static final long serialVersionUID = -7915192924199950140L;

    private final int boxIndex;

    public ThroughPoint(FlatPoint flatPoint) {
      this(flatPoint.getX(), flatPoint.getY(), -1);
    }

    public ThroughPoint(double x, double y, int boxIndex) {
      super(x, y);
      this.boxIndex = boxIndex;
    }

    public int getBoxIndex() {
      return boxIndex;
    }
  }

  protected static class ThroughParam {

    public Line line;
    public DNode from;
    public DNode to;
    public LineDrawProp lineDrawProp;
    public List<ThroughPoint> throughPoints;
    public List<RouterBox> lineRouterBoxes;
    public boolean isHorizontal;
    public List<FlatPoint> fromPortPoints;
    public List<FlatPoint> toPortPoints;
  }

  private static class FlatParallelLineParam {

    private final DNode from;

    private final DNode to;

    private final LineDrawProp line;

    private final List<RouterBox> routerBoxes;

    public FlatParallelLineParam(DNode from, DNode to, LineDrawProp line,
                                 List<RouterBox> routerBoxes) {
      this.from = from;
      this.to = to;
      this.line = line;
      this.routerBoxes = routerBoxes;
    }
  }
}
