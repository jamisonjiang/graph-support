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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.graphper.layout.dot.OrthoNodeSizeExpander.OffsetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.def.DedirectedGraph;
import org.graphper.def.FlatPoint;
import org.graphper.def.VertexIndex;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Line;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.Box;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.Mark;
import org.graphper.layout.OrthoVisGraph.GridVertex;
import org.graphper.layout.dot.DotMaze.GuideInfo;
import org.graphper.layout.dot.Maze.Cell;
import org.graphper.layout.dot.RankContent.RankNode;

/**
 * Implementation of {@link Splines#ORTHO}.
 *
 * @author Jamison Jiang
 */
class OrthogonalRouter extends AbstractDotLineRouter {

  private static final Logger log = LoggerFactory.getLogger(OrthogonalRouter.class);

  private static final int LEFT = 0;
  private static final int RIGHT = 1;
  private static final int UP = 2;
  private static final int DOWN = 3;

  /*
   * When estimating the amount of curvature between two elements, when the source
   * is in different directions in different intervals of the target node, each
   * direction reaches the table of the amount of curvature of the target element.
   * */
  private static final int[][] BEND_NUM_TABLE = {
      {2, 1, 2, 1}, {1, 1, 2, 0}, {1, 2, 2, 1},
      {2, 0, 1, 1}, {0, 0, 0, 0}, {0, 2, 1, 1},
      {2, 1, 1, 2}, {1, 1, 0, 2}, {1, 2, 1, 2},
  };

  /*
   * When estimating the amount of curvature between two elements, when the source
   * is in different directions in different intervals of the target node, each
   * direction reaches the table of the side of the target element.
   * */
  private static final int[][] TARGET_SIDE_TABLE = {
      {LEFT, UP, UP, LEFT}, {UP, UP, UP, UP}, {UP, RIGHT, UP, RIGHT},
      {LEFT, LEFT, LEFT, LEFT}, {LEFT, RIGHT, UP, DOWN}, {RIGHT, RIGHT, RIGHT, RIGHT},
      {LEFT, DOWN, LEFT, DOWN}, {DOWN, DOWN, DOWN, DOWN}, {DOWN, RIGHT, RIGHT, DOWN},
  };

  private DotMaze maze;

  private PathContent pathContent;

  @Override
  public boolean needDeal(Splines splines) {
    return splines == Splines.ORTHO && super.needDeal(splines);
  }

  @Override
  public void route() {
    // TODO: 2023/1/2
    //  1.Router access low density area
    //  2.Arrow clip direction
    this.pathContent = new PathContent();
    this.maze = new DotMaze(rankContent, drawGraph);
    generateEdge();
  }

  @Override
  protected void selfLoopHandle(DNode node) {
    if (node == null || node.getSelfLoopCount() < 1) {
      return;
    }

    double nodeInternalInterval = node.getWidth() / (node.getSelfLoopCount() + 1);
    OffsetConsumer consumer = (lineNo, line, topOffset, bottomOffset, rightOffset) -> {
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
      if (lineDrawProp == null || lineDrawProp.isInit()) {
        return;
      }

      double left = node.getRightBorder() - nodeInternalInterval * (lineNo + 1);
      double right = node.getRightBorder() + rightOffset;
      double top = node.getUpBorder() - topOffset;
      double bottom =  node.getDownBorder() + bottomOffset;

      FlatPoint center = new FlatPoint(left, node.getY());
      lineDrawProp.add(center);
      lineDrawProp.add(new FlatPoint(left, top));
      lineDrawProp.add(new FlatPoint(right, top));
      lineDrawProp.add(new FlatPoint(right, bottom));
      lineDrawProp.add(new FlatPoint(left, bottom));
      lineDrawProp.add(center);

      FlatPoint labelSize = line.getLabelSize();
      if (labelSize != null) {
        lineDrawProp.setLabelCenter(new FlatPoint(right - labelSize.getWidth() / 2, node.getY()));
      }
    };

    OrthoNodeSizeExpander.linePos(node, consumer);
  }

  private void generateEdge() {
    EdgeSegRecord edgeSegRecord = new EdgeSegRecord();
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (DNode node : rankNode) {
        if (node.isVirtual()) {
          continue;
        }

        // All out edges
        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (line.isVirtual() || line.isHide()) {
            continue;
          }

          for (int j = 0; j < line.getParallelNums(); j++) {
            ovgRouter(edgeSegRecord, line.parallelLine(j));
          }
        }

        // Draw self loop
        selfLoopHandle(node);
      }
    }

    // Split the edge segment which have common area
    splitOverlapSegment(edgeSegRecord);

    // Edge segment to LineDrawProp
    edgeSegToLine(edgeSegRecord);
  }

  private void ovgRouter(EdgeSegRecord edgeSegRecord, DLine line) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
    if (lineDrawProp == null || lineDrawProp.isInit()) {
      return;
    }

    List<GuideInfo> guideInfos = maze.getGuideInfos(line.getLine());
    if (CollectionUtils.isEmpty(guideInfos)) {
      ovgRouter(edgeSegRecord, lineDrawProp, Target::new);
      return;
    }
    
    ovgRouter(edgeSegRecord, lineDrawProp, end -> new Target(end, guideInfos));

    // Set label position
    for (int i = 0; i < guideInfos.size(); i++) {
      GuideInfo guideInfo = guideInfos.get(i);
      if (!guideInfo.isLabelSign()) {
        continue;
      }

      Box signPos = guideInfo.getSignPos();
      lineDrawProp.setLabelCenter(new FlatPoint(signPos.getX(), signPos.getY()));
    }
  }

  private void ovgRouter(EdgeSegRecord edgeSegRecord, LineDrawProp lineDrawProp,
                         TargetConstructor targetConstructor) {
    DNode tail = dotDigraph.getDNode(lineDrawProp.getLine().tail());
    DNode head = dotDigraph.getDNode(lineDrawProp.getLine().head());
    if (tail == null || head == null) {
      return;
    }

    DNode from = tail.getRank() < head.getRank() ? tail : head;
    DNode to = from == tail ? head : tail;
    Cell fromCell = maze.getCell(from);
    Cell toCell = maze.getCell(to);

    if (fromCell == null || toCell == null) {
      if (log.isWarnEnabled()) {
        log.warn("From Cell = {} or To Cell = {} is null", fromCell, toCell);
      }
      return;
    }

    Target target = targetConstructor.newTarget(toCell);
    Asserts.nullArgument(target, "target");
    EdgeDraw edgeDraw = ovgRouter(fromCell, target, edgeSegRecord);
    if (edgeDraw == null) {
      return;
    }

    pathContent.clear();
    lineDrawProp.fakeInit();
    edgeDraw.from = from;
    edgeDraw.to = to;
    edgeSegRecord.addLineEdgeSeg(lineDrawProp.getLine(), edgeDraw);
  }

  private void splitOverlapSegment(EdgeSegRecord edgeSegRecord) {
    ChannelSegRank channelSegRank = new ChannelSegRank();
    DedirectedGraph<EdgeSeg> digraph = new DedirectedGraph<>();

    for (Channel channel : edgeSegRecord.channels()) {
      if (channel.segmentSize() <= 1) {
        continue;
      }

      /*
       * 1. group segments of channel
       * 2. use top sort in per group
       */
      initDigraph(channel, digraph);
      splitEdgeSegs(digraph, channelSegRank, channel);

      digraph.clear();
    }
  }

  private void splitEdgeSegs(DedirectedGraph<EdgeSeg> digraph,
                             ChannelSegRank channelSegRank, Channel channel) {
    Iterable<TreeMap<Integer, List<EdgeSeg>>> groups = channelSegRank.group(digraph);

    // Visit each group, each group is a connected subgraph
    for (Map<Integer, List<EdgeSeg>> group : groups) {
      int idx = 0;
      // Access all nodes in the subgraph, and the nodes are sorted according to the rank
      for (Entry<Integer, List<EdgeSeg>> rank : group.entrySet()) {
        if (CollectionUtils.isEmpty(rank.getValue())) {
          idx++;
          continue;
        }

        // All nodes in the same rank use the same offset
        double moveUnit = channel.range() / (group.size() + 1);
        double offset = moveUnit * ++idx - rank.getValue().get(0).axis + channel.min;
        for (EdgeSeg edgeSeg : rank.getValue()) {
          edgeSeg.moveAxis(offset);
        }
      }
    }
  }

  private void initDigraph(Channel channel, DedirectedGraph<EdgeSeg> digraph) {
    channel.sortEdgeSegs();

    for (int i = 0; i < channel.segmentSize(); i++) {
      EdgeSeg edgeSeg = channel.get(i);

      for (int j = i + 1; j < channel.segmentSize(); j++) {
        EdgeSeg overlapSeg = channel.get(j);
        if (overlapSeg.isHor != edgeSeg.isHor || !edgeSeg.startInRange(overlapSeg)) {
          break;
        }

        if (overlapCmp(edgeSeg, overlapSeg) < 0) {
          digraph.addEdge(edgeSeg, overlapSeg);
        } else {
          digraph.addEdge(overlapSeg, edgeSeg);
        }
      }
    }
  }

  private int overlapCmp(EdgeSeg source, EdgeSeg target) {
    if (source.isHor != target.isHor || !source.startInRange(target)) {
      return 0;
    }

    return crossNum(source, target, true) < crossNum(source, target, false) ? -1 : 1;
  }

  private int crossNum(EdgeSeg source, EdgeSeg target, boolean origin) {
    EdgeSeg sourceStartPre = source.startPre();
    EdgeSeg sourceEndNext = source.endNext();
    EdgeSeg targetStartPre = target.startPre();
    EdgeSeg targetEndNext = target.endNext();

    int crossNum = 0;
    if (isCross(sourceStartPre, target, !origin)) {
      crossNum++;
    }
    if (isCross(sourceEndNext, target, !origin)) {
      crossNum++;
    }
    if (isCross(targetStartPre, source, origin)) {
      crossNum++;
    }
    if (isCross(targetEndNext, source, origin)) {
      crossNum++;
    }

    return crossNum;
  }

  private boolean isCross(EdgeSeg seg1, EdgeSeg seg2, boolean isAdd) {
    if (seg1 == null || seg2 == null) {
      return false;
    }

    double start = seg1.getStart();
    double end = seg1.getEnd();
    if (isAdd) {
      start += 1;
      end += 1;
    } else {
      start -= 1;
      end -= 1;
    }
    return seg2.axis >= start && seg2.axis <= end && seg2.inRange(seg1.axis);
  }

  private void edgeSegToLine(EdgeSegRecord edgeSegRecord) {
    for (Entry<Line, EdgeDraw> entry : edgeSegRecord.lineEdgeSegs.entrySet()) {
      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(entry.getKey());
      EdgeSeg edgeSeg = entry.getValue().edgeSeg;

      lineDrawProp.setIsHeadStart(entry.getValue().from.getNode());
      while (edgeSeg != null) {
        addPoint(lineDrawProp, edgeSeg);
        edgeSeg = edgeSeg.next;
      }
    }
  }

  private void addPoint(LineDrawProp lineDrawProp, EdgeSeg edgeSeg) {
    if (edgeSeg.isHor) {
      if (edgeSeg.pos) {
        lineDrawProp.add(new FlatPoint(edgeSeg.getStart(), edgeSeg.axis));
        lineDrawProp.add(new FlatPoint(edgeSeg.getEnd(), edgeSeg.axis));
      } else {
        lineDrawProp.add(new FlatPoint(edgeSeg.getEnd(), edgeSeg.axis));
        lineDrawProp.add(new FlatPoint(edgeSeg.getStart(), edgeSeg.axis));
      }
    } else {
      if (edgeSeg.pos) {
        lineDrawProp.add(new FlatPoint(edgeSeg.axis, edgeSeg.getStart()));
        lineDrawProp.add(new FlatPoint(edgeSeg.axis, edgeSeg.getEnd()));
      } else {
        lineDrawProp.add(new FlatPoint(edgeSeg.axis, edgeSeg.getEnd()));
        lineDrawProp.add(new FlatPoint(edgeSeg.axis, edgeSeg.getStart()));
      }
    }
  }

  private EdgeDraw ovgRouter(Cell from, Target target, EdgeSegRecord edgeSegRecord) {
    /*
     * 1.Put all start node to priority queen
     * 2.Take a node from priority queen
     * 3.The compare condition composed with: bends number,direction
     */
    addStartVertexesToQueue(from, target);

    /*
     * path: s -> t, n is on the optimal path
     * f(n) = g(n) + h(n)
     * g(n): actual cost of an optimal path from s -> n
     * h(n): actual cost of an optimal path from n -> t
     *
     * Must have an estimate ^h(n) of h(n).
     */
    while (pathContent.isNotEmpty()) {
      VertexDir vertexDir = pathContent.poll();
      if (vertexDir == null) {
        continue;
      }

      // If arrive at destination, terminate the router process
      if (arriveAtDestination(target, vertexDir)) {
        return terminateRouter(edgeSegRecord, vertexDir);
      }

      GridVertex right = vertexDir.vertex.getRight();
      GridVertex top = vertexDir.vertex.getTop();
      GridVertex left = vertexDir.vertex.getLeft();
      GridVertex bottom = vertexDir.vertex.getBottom();

      successor(LEFT, left, vertexDir);
      successor(RIGHT, right, vertexDir);
      successor(UP, top, vertexDir);
      successor(DOWN, bottom, vertexDir);
    }

    if (log.isWarnEnabled()) {
      log.warn("Fail route Cell = {} to Cell = {}", from, target.end);
    }
    return null;
  }

  private boolean arriveAtDestination(Target target, VertexDir vertexDir) {
    return target.end.in(vertexDir.vertex.getX(), vertexDir.vertex.getY())
        && vertexDir.vertex.isNodeInternal();
  }

  private EdgeDraw terminateRouter(EdgeSegRecord edgeSegRecord,
                                   VertexDir end) {
    VertexDir current = end;
    // The edge segment connect node "to" center and node border point
    EdgeSeg edgeSeg = null;

    do {
      // Refresh edge segment endpoint
      if (edgeSeg != null) {
        edgeSeg.addVertex(current);
      }
      // If vertex dir is orthogonal with current segment, generate a new segment
      if (edgeSeg != null && !current.isOrthogonal(edgeSeg.isHor)) {
        current = current.parent;
        continue;
      }

      // Create a new edge segment
      EdgeSeg newEdgeSeg = new EdgeSeg(
          current.isHor() ? current.vertex.getY() : current.vertex.getX(),
          edgeSeg != null ? !edgeSeg.isHor : current.isHor(),
          current.dir == RIGHT || current.dir == DOWN
      );

      // Set link info for these two segments
      if (edgeSeg != null) {
        edgeSeg.pre = newEdgeSeg;
      }
      newEdgeSeg.next = edgeSeg;
      edgeSeg = newEdgeSeg;
      edgeSeg.addVertex(current);

      // Add pre segment to edgeSegRecord
      setEdgeRecord(edgeSegRecord, current, edgeSeg);
      current = current.parent;
    } while (current != null);

    return new EdgeDraw(edgeSeg);
  }

  private void setEdgeRecord(EdgeSegRecord edgeSegRecord, VertexDir current, EdgeSeg edgeSeg) {
    if (edgeSeg == null) {
      return;
    }

    double min;
    double max;
    if (edgeSeg.isHor) {
      min = current.vertex.getLeftUp().getY();
      max = current.vertex.getRightDown().getY();
    } else {
      min = current.vertex.getLeftUp().getX();
      max = current.vertex.getRightDown().getX();
    }
    edgeSegRecord.addSeg(min, max, edgeSeg);
  }

  private void successor(int dir, GridVertex vertex, VertexDir parentDir) {
    if (vertex == null || isContrary(dir, parentDir.dir)) {
      return;
    }

    AdjPair adjPair = pathContent.getOrNullCreateAdjPair(vertex);
    VertexDir vertexDir = adjPair.get(dir);

    // Vertex direction pair not exist
    if (vertexDir == null) {
      vertexDir = new VertexDir(dir, parentDir, vertex, parentDir.target);
      pathContent.offer(vertexDir);
      return;
    }

    if (vertexDir.compareCost(parentDir) <= 0) {
      return;
    }

    vertexDir.setParent(parentDir);
    // Entrance priority queue again
    pathContent.offer(vertexDir);
  }

  private void addStartVertexesToQueue(Cell from, Target target) {
    for (GridVertex vertex : from.getAxisVertexes()) {
      addStartVertexesToQueue(from, target, vertex);
    }
  }

  private void addStartVertexesToQueue(Cell from, Target target, GridVertex vertex) {
    VertexDir v = new VertexDir(getCellInternalNodeDir(vertex), null, vertex, target);
    v.centering = FlatPoint.twoPointDistance(vertex.getX(), vertex.getY(),
                                             from.getX(), from.getY());
    pathContent.offer(v);
  }

  private int getCellInternalNodeDir(GridVertex vertex) {
    if (vertex.getLeft() != null) {
      return LEFT;
    }
    if (vertex.getRight() != null) {
      return RIGHT;
    }
    if (vertex.getTop() != null) {
      return UP;
    }
    if (vertex.getBottom() != null) {
      return DOWN;
    }
    throw new IllegalArgumentException("Vertex is not correct internal cell node");
  }

  private static boolean isContrary(int src, int tar) {
    switch (src) {
      case LEFT:
        return tar == RIGHT;
      case RIGHT:
        return tar == LEFT;
      case UP:
        return tar == DOWN;
      case DOWN:
        return tar == UP;
      default:
        break;
    }
    return false;
  }

  private interface TargetConstructor {

    Target newTarget(Cell end);
  }

  private static class Target {

    private final Cell end;

    private List<GuideInfo> lineSigns;

    private double[] signToEndLens;

    private int[][] signToEndBendNum;

    private Target(Cell end) {
      Asserts.nullArgument(end, "end");
      this.end = end;
    }

    private Target(Cell end, List<GuideInfo> lineSigns) {
      this(end);
      Asserts.illegalArgument(CollectionUtils.isEmpty(lineSigns), "lineSigns");
      this.lineSigns = lineSigns;
      this.signToEndLens = new double[lineSigns.size()];
      this.signToEndBendNum = new int[lineSigns.size()][];
      initSignToEndLens();
      initSignToEndBendNum();
    }

    int signSize() {
      return CollectionUtils.isEmpty(lineSigns) ? 0 : lineSigns.size();
    }

    int estimateBendNumToEnd(VertexDir vertexDir) {
      if (vertexDir == null) {
        return 0;
      }

      int bendNum = Integer.MAX_VALUE;
      bendNum = Math.min(estimateBendNumToEnd(vertexDir, LEFT), bendNum);
      bendNum = Math.min(estimateBendNumToEnd(vertexDir, RIGHT), bendNum);
      bendNum = Math.min(estimateBendNumToEnd(vertexDir, UP), bendNum);
      bendNum = Math.min(estimateBendNumToEnd(vertexDir, DOWN), bendNum);
      return bendNum;
    }

    double estimateLenToEnd(VertexDir vertexDir) {
      if (vertexDir == null) {
        return 0;
      }

      // The distance that current vertex to closest sign add the distance that closest sign to end
      return signToEndLens(vertexDir.signIdx) + estimateLenToNextSign(vertexDir);
    }

    double estimateLenToNextSign(VertexDir vertexDir) {
      Asserts.nullArgument(vertexDir, "vertexDir");
      assertIdx(vertexDir.signIdx);
      return lenBetweenTwoSign(vertexDir.vertex, getBox(vertexDir.signIdx));
    }

    void markSignIndex(VertexDir vertexDir) {
      if (vertexDir == null || lineSigns == null) {
        return;
      }
      if (vertexDir.signIdx < 0 || vertexDir.signIdx >= signSize()) {
        return;
      }

      GuideInfo guideInfo = lineSigns.get(vertexDir.signIdx);
      if (guideInfo.getGuideVertex() == vertexDir.vertex) {
        vertexDir.signIdx++;
      }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~ internal method ~~~~~~~~~~~~~~~~~~~~~~~~~

    private void assertIdx(int idx) {
      Asserts.illegalArgument(idx < 0 || idx > signSize(), "Wrong Sign index");
    }

    private void initSignToEndLens() {
      if (signToEndLens == null) {
        return;
      }

      for (int i = signToEndLens.length - 1; i >= 0; i--) {
        signToEndLens[i] = signToNextLen(i);
        if (i != signToEndLens.length - 1) {
          signToEndLens[i] += signToEndLens[i + 1];
        }
      }
    }

    private void initSignToEndBendNum() {
      if (signToEndBendNum == null) {
        return;
      }

      for (int i = signToEndBendNum.length - 1; i >= 0; i--) {
        signToEndBendNum[i] = new int[4];
        signToEndBendNum[i][LEFT] = bendNumToNext(i, LEFT);
        signToEndBendNum[i][RIGHT] = bendNumToNext(i, RIGHT);
        signToEndBendNum[i][UP] = bendNumToNext(i, UP);
        signToEndBendNum[i][DOWN] = bendNumToNext(i, DOWN);

        if (i != signToEndBendNum.length - 1) {
          signToEndBendNum[i][LEFT] += signToEndBendNum[i + 1][bendTargetOppositeSide(i, LEFT)];
          signToEndBendNum[i][RIGHT] += signToEndBendNum[i + 1][bendTargetOppositeSide(i, RIGHT)];
          signToEndBendNum[i][UP] += signToEndBendNum[i + 1][bendTargetOppositeSide(i, UP)];
          signToEndBendNum[i][DOWN] += signToEndBendNum[i + 1][bendTargetOppositeSide(i, DOWN)];
        }
      }
    }

    private int estimateBendNumToEnd(VertexDir vertexDir, int dir) {
      Box box = getBox(vertexDir.signIdx);
      // The bend number that vertex to next sign
      int bendNum = bendNumBetweenTwoSign(vertexDir.vertex, box, dir);
      // The bend number that next sign to end
      bendNum += signToEndBendNum(vertexDir.signIdx,
                                  bendTargetOppositeSide(vertexDir.vertex, box, dir));
      if (vertexDir.isOrthogonal(dir)) {
        bendNum++;
      } else if (isContrary(dir, vertexDir.dir)) {
        bendNum += 3;
      }
      return bendNum;
    }

    private int bendTargetOppositeSide(int idx, int dir) {
      assertIdx(idx);
      if (idx == signSize()) {
        return 0;
      }
      Box source = getBox(idx);
      Box target = getBox(idx + 1);
      return bendTargetOppositeSide(source, target, dir);
    }

    private int bendTargetOppositeSide(Box source, Box target, int dir) {
      if (source == target) {
        return dir;
      }
      return oppositeDir(TARGET_SIDE_TABLE[findInCellRange(source, target)][dir]);
    }

    private int oppositeDir(int dir) {
      switch (dir) {
        case LEFT:
          return RIGHT;
        case RIGHT:
          return LEFT;
        case UP:
          return DOWN;
        case DOWN:
          return UP;
        default:
          break;
      }
      return LEFT;
    }

    private int findInCellRange(Box source, Box target) {
      int row = rangeVal(source.getY(), target.getUpBorder(), target.getDownBorder());
      int col = rangeVal(source.getX(), target.getLeftBorder(), target.getRightBorder());
      return row * 3 + col;
    }

    private int rangeVal(double target, double min, double max) {
      if (target <= min) {
        return 0;
      }
      if (target >= max) {
        return 2;
      }
      return 1;
    }

    private Box getBox(int idx) {
      if (lineSigns == null) {
        return end;
      }

      assertIdx(idx);
      if (idx == signSize()) {
        return end;
      }
      return lineSigns.get(idx).getGuideVertex();
    }

    private double signToEndLens(int idx) {
      assertIdx(idx);
      if (idx == signSize()) {
        return 0;
      }
      return signToEndLens[idx];
    }

    private int signToEndBendNum(int idx, int dir) {
      assertIdx(idx);
      if (idx == signSize()) {
        return 0;
      }
      return signToEndBendNum[idx][dir];
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~ internal method use for estimate bend ~~~~~~~~~~~~~~~~~~~~~~~~~

    private int bendNumToNext(int idx, int dir) {
      assertIdx(idx);
      if (idx == signSize()) {
        return 0;
      }
      Box box = getBox(idx);
      Box next = getBox(idx + 1);
      return bendNumBetweenTwoSign(box, next, dir);
    }

    private int bendNumBetweenTwoSign(Box source, Box target, int sourceDir) {
      return BEND_NUM_TABLE[findInCellRange(source, target)][sourceDir];
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~ internal method use for estimate length ~~~~~~~~~~~~~~~~~~~~~~~~~

    private double signToNextLen(int idx) {
      assertIdx(idx);
      if (idx == signSize()) {
        return 0;
      }
      Box box = getBox(idx);
      Box next = getBox(idx + 1);
      return lenBetweenTwoSign(box, next);
    }

    private double lenBetweenTwoSign(Box source, Box target) {
      return Math.abs(source.getX() - target.getX()) + Math.abs(source.getY() - target.getY());
    }
  }

  private static class VertexDir implements Comparable<VertexDir> {

    private boolean inQueue;

    private int signIdx;

    /*
     * The current direction of the current route
     */
    private final int dir;

    /*
     * The number of bends that has passed
     */
    private int costBendNum;

    private int estimateRemainBendNum;

    private double costLen;

    private double estimateRemainLen;

    private VertexDir parent;

    private Double centering;

    private final GridVertex vertex;

    private final Target target;

    private VertexDir(int dir, VertexDir parent, GridVertex vertex, Target target) {
      Asserts.nullArgument(vertex, "vertex");
      Asserts.nullArgument(target, "target");
      Asserts.illegalArgument(dir < LEFT || dir > DOWN, "Wrong direction value");
      this.dir = dir;
      this.vertex = vertex;
      this.target = target;
      setParent(parent);
    }

    private void setParent(VertexDir p) {
      if (p != null) {
        this.centering = p.centering;
        this.signIdx = p.signIdx;
        target.markSignIndex(this);
      }

      this.parent = p;
      this.costBendNum = parentBendNum(p);
      this.estimateRemainBendNum = target.estimateBendNumToEnd(this);
      this.costLen = parentLen(p) + lengthToParent();
      this.estimateRemainLen = target.estimateLenToEnd(this);
    }

    private int parentBendNum(VertexDir p) {
      if (p == null) {
        return 0;
      }
      return p.costBendNum + (dir == p.dir ? 0 : 1);
    }

    private double parentLen(VertexDir p) {
      if (p == null) {
        return 0;
      }
      return p.costLen;
    }

    private boolean isHor() {
      return dir == LEFT || dir == RIGHT;
    }

    private boolean isOrthogonal(boolean isHor) {
      return isOrthogonal(isHor ? LEFT : UP);
    }

    private boolean isOrthogonal(int compareDir) {
      if (dir == LEFT || dir == RIGHT) {
        return compareDir == UP || compareDir == DOWN;
      }
      if (dir == UP || dir == DOWN) {
        return compareDir == LEFT || compareDir == RIGHT;
      }
      return false;
    }

    private double lengthToParent() {
      if (parent == null) {
        return 0;
      }
      return FlatPoint.twoPointDistance(parent.vertex.getX(), parent.vertex.getY(),
                                        vertex.getX(), vertex.getY());
    }

    private int compareCentering(VertexDir dir) {
      if (dir == null || Objects.equals(dir.centering, centering)
          || dir.centering == null || centering == null) {
        return 0;
      }

      return Double.compare(centering, dir.centering);
    }

    private int compareCost(VertexDir compareParent) {
      if (compareParent == null) {
        return 1;
      }
      if (compareParent == parent) {
        return 0;
      }

      int r = Integer.compare(costBendNum, parentBendNum(compareParent));
      if (r != 0) {
        return r;
      }
      return Double.compare(costLen, parentLen(compareParent));
    }

    @Override
    public int compareTo(VertexDir o) {
      if (o == null) {
        return 1;
      }
      int r = Integer.compare(costBendNum + estimateRemainBendNum,
                              o.costBendNum + o.estimateRemainBendNum);
      if (r != 0) {
        return r;
      }
      r = compareCentering(o);
      if (r != 0) {
        return r;
      }

      return Double.compare(costLen + estimateRemainLen,
                            o.costLen + o.estimateRemainLen);
    }
  }

  private static class PathContent {

    private final Queue<VertexDir> queue;

    private final Map<GridVertex, AdjPair> adjPairs;

    private PathContent() {
      this.queue = new PriorityQueue<>();
      this.adjPairs = new HashMap<>();
    }

    private void offer(VertexDir vertexDir) {
      Asserts.nullArgument(vertexDir, "vertexDir");
      vertexDir.inQueue = true;
      queue.offer(vertexDir);
      AdjPair adjPair = adjPairs.computeIfAbsent(vertexDir.vertex, k -> new AdjPair());
      adjPair.set(vertexDir);
    }

    private VertexDir poll() {
      VertexDir v = queue.poll();
      if (v == null) {
        return null;
      }
      boolean inQueue = v.inQueue;
      v.inQueue = false;
      return inQueue ? v : null;
    }

    private AdjPair getOrNullCreateAdjPair(GridVertex vertex) {
      return adjPairs.computeIfAbsent(vertex, k -> new AdjPair());
    }

    private boolean isNotEmpty() {
      return !isEmpty();
    }

    private boolean isEmpty() {
      return queue.isEmpty();
    }

    protected void clear() {
      queue.clear();
      adjPairs.clear();
    }
  }

  private static class AdjPair {

    private VertexDir left;

    private VertexDir right;

    private VertexDir top;

    private VertexDir bottom;

    private VertexDir get(int dir) {
      if (dir == LEFT) {
        return left;
      }
      if (dir == RIGHT) {
        return right;
      }
      if (dir == UP) {
        return top;
      }
      if (dir == DOWN) {
        return bottom;
      }
      return null;
    }

    private void set(VertexDir vertexDir) {
      if (vertexDir == null) {
        return;
      }
      if (vertexDir.dir == LEFT) {
        this.left = vertexDir;
      }
      if (vertexDir.dir == RIGHT) {
        this.right = vertexDir;
      }
      if (vertexDir.dir == UP) {
        this.top = vertexDir;
      }
      if (vertexDir.dir == DOWN) {
        this.bottom = vertexDir;
      }
    }
  }

  private static class EdgeSeg extends VertexIndex implements Comparable<EdgeSeg> {

    private int rank;

    private int group;

    private double axis;

    private Double start;

    private Double end;

    private EdgeSeg pre;

    private EdgeSeg next;

    private final boolean isHor;

    private final boolean pos;

    public EdgeSeg(double axis, boolean isHor, boolean pos) {
      this.axis = axis;
      this.isHor = isHor;
      this.pos = pos;
    }

    private void moveAxis(double dist) {
      double oldAxis = axis;
      axis += dist;
      if (pre != null) {
        pre.moveEndpoint(oldAxis, axis);
      }
      if (next != null) {
        next.moveEndpoint(oldAxis, axis);
      }
    }

    private void moveEndpoint(double oldEndPoint, double newEndpoint) {
      if (start == null || end == null) {
        return;
      }
      if (newEndpoint > start && newEndpoint < end) {
        if (Objects.equals(oldEndPoint, start)) {
          start = newEndpoint;
        } else {
          end = newEndpoint;
        }
      } else {
        addPoint(newEndpoint);
      }
    }

    private void addVertex(VertexDir vertexDir) {
      if (isHor) {
        addPoint(vertexDir.vertex.getX());
      } else {
        addPoint(vertexDir.vertex.getY());
      }
    }

    private void addPoint(double point) {
      if (start == null) {
        start = point;
        return;
      }

      if (Objects.equals(start, point) || Objects.equals(point, end)) {
        return;
      }

      Double tmp = start;
      start = Math.min(start, point);
      if (end == null) {
        end = Math.max(tmp, point);
      } else {
        end = Math.max(end, point);
      }
    }

    private double getStart() {
      Asserts.illegalArgument(start == null, "start not ready");
      return start;
    }

    private double getEnd() {
      Asserts.illegalArgument(end == null, "end not ready");
      return end;
    }

    private boolean startInRange(EdgeSeg edgeSeg) {
      if (Objects.isNull(edgeSeg)) {
        return false;
      }
      return inRange(edgeSeg.getStart());
    }

    private boolean inRange(double val) {
      if (start == null || end == null) {
        return false;
      }
      return start <= val && end >= val;
    }

    private EdgeSeg startPre() {
      if (start == null) {
        return null;
      }
      if (pre != null && Objects.equals(start, pre.axis)) {
        return pre;
      }
      if (next != null && Objects.equals(start, next.axis)) {
        return next;
      }
      return null;
    }

    private EdgeSeg endNext() {
      if (end == null) {
        return null;
      }
      if (pre != null && Objects.equals(end, pre.axis)) {
        return pre;
      }
      if (next != null && Objects.equals(end, next.axis)) {
        return next;
      }
      return null;
    }

    @Override
    public int compareTo(EdgeSeg o) {
      if (o == null) {
        return 1;
      }
      if (isHor != o.isHor) {
        return isHor ? 1 : -1;
      }
      if (axis != o.axis) {
        return Double.compare(axis, o.axis);
      }
      if (start == null || end == null || o.start == null || o.end == null) {
        return 0;
      }
      int r = Double.compare(start, o.start);
      if (r != 0) {
        return r;
      }
      return Double.compare(end, o.end);
    }
  }

  private static class EdgeSegRecord {

    private final Map<Line, EdgeDraw> lineEdgeSegs;

    private Map<Double, Map<Double, Channel>> channels;

    private EdgeSegRecord() {
      this.lineEdgeSegs = new HashMap<>();
    }

    private void addSeg(double min, double max, EdgeSeg edgeSeg) {
      Asserts.nullArgument(edgeSeg, "edgeSeg");
      Channel channel = getNullOrCreateChannel(min, max);
      channel.addEdgeSeg(edgeSeg);
    }

    private void addLineEdgeSeg(Line line, EdgeDraw edgeDraw) {
      lineEdgeSegs.put(line, edgeDraw);
    }

    private Channel getNullOrCreateChannel(double axis1, double axis2) {
      if (channels == null) {
        channels = new HashMap<>();
      }
      double min = Math.min(axis1, axis2);
      double max = Math.max(axis1, axis2);
      Map<Double, Channel> channelRecord = channels
          .computeIfAbsent(min, a -> new HashMap<>(1));
      return channelRecord.computeIfAbsent(max, a -> new Channel(min, max));
    }

    private Iterable<Channel> channels() {
      if (channels == null) {
        return Collections.emptyList();
      }
      return channels.values().stream()
          .flatMap(m -> m.values().stream())
          .collect(Collectors.toList());
    }
  }

  private static class EdgeDraw {

    private DNode from;

    private DNode to;

    private final EdgeSeg edgeSeg;

    public EdgeDraw(EdgeSeg edgeSeg) {
      this.edgeSeg = edgeSeg;
    }

    public DNode getFrom() {
      Asserts.illegalArgument(from == null, "EdgeDraw do not have from node");
      return from;
    }

    public DNode getTo() {
      Asserts.illegalArgument(to == null, "EdgeDraw do not have to node");
      return to;
    }
  }

  private static class Channel {

    private final double min;

    private final double max;

    private List<EdgeSeg> edgeSegs;

    public Channel(double min, double max) {
      this.min = min;
      this.max = max;
    }

    private void addEdgeSeg(EdgeSeg edgeSeg) {
      if (edgeSegs == null) {
        edgeSegs = new ArrayList<>();
      }
      edgeSegs.add(edgeSeg);
    }

    private double range() {
      return Math.abs(max - min);
    }

    private int segmentSize() {
      return CollectionUtils.isNotEmpty(edgeSegs) ? edgeSegs.size() : 0;
    }

    private EdgeSeg get(int idx) {
      Asserts.illegalArgument(CollectionUtils.isEmpty(edgeSegs), "Null Edge Segments");
      return edgeSegs.get(idx);
    }

    private void sortEdgeSegs() {
      if (CollectionUtils.isEmpty(edgeSegs)) {
        return;
      }
      edgeSegs.sort(EdgeSeg::compareTo);
    }
  }

  private static class ChannelSegRank extends Mark<EdgeSeg> {

    private Iterable<TreeMap<Integer, List<EdgeSeg>>> edgeSegGroups;

    private Iterable<TreeMap<Integer, List<EdgeSeg>>> group(DedirectedGraph<EdgeSeg> digraph) {
      clear();

      setRank(digraph);

      setGroup(digraph);

      groupEdgeSegs(digraph);

      return getEdgeSegGroups();
    }

    private Iterable<TreeMap<Integer, List<EdgeSeg>>> getEdgeSegGroups() {
      return edgeSegGroups != null ? edgeSegGroups : Collections.emptyList();
    }

    private void setRank(DedirectedGraph<EdgeSeg> digraph) {
      for (EdgeSeg node : digraph) {
        if (isMark(node)) {
          continue;
        }

        dfs(node, digraph);
      }
    }

    private void dfs(EdgeSeg from, DedirectedGraph<EdgeSeg> digraph) {
      mark(from);

      for (EdgeSeg to : digraph.outAdjacent(from)) {
        if (!isMark(to)) {
          dfs(to, digraph);
        }

        from.rank = Math.min(from.rank, to.rank - 1);
      }
    }

    private void setGroup(DedirectedGraph<EdgeSeg> digraph) {
      clear();

      int group = 0;
      for (EdgeSeg node : digraph) {
        if (isMark(node)) {
          continue;
        }

        dfs(group++, node, digraph);
      }
    }

    private void dfs(int group, EdgeSeg from, DedirectedGraph<EdgeSeg> digraph) {
      mark(from);
      from.group = group;

      for (EdgeSeg to : digraph.adjacent(from)) {
        if (isMark(to)) {
          continue;
        }

        dfs(group, to, digraph);
      }
    }

    private void groupEdgeSegs(DedirectedGraph<EdgeSeg> digraph) {
      this.edgeSegGroups = digraph.stream()
          .collect(Collectors.groupingBy(e -> e.group, Collectors
              .groupingBy(e -> e.rank, TreeMap::new, Collectors.toList())))
          .values();
    }
  }

  // --------------------------------------------- OrthogonalRouterFactory ---------------------------------------------

  public static class OrthogonalRouterFactory extends
      AbstractDotLineRouterFactory<OrthogonalRouter> {

    @Override
    protected OrthogonalRouter newInstance() {
      return new OrthogonalRouter();
    }
  }
}
