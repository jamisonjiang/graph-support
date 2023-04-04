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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.ext.Box;
import org.graphper.def.BiConcatIterable;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.layout.Grid;
import org.graphper.layout.Grid.GridAxis;
import org.graphper.layout.Grid.GridBuilder;
import org.graphper.layout.OrthoVisGraph;
import org.graphper.layout.OrthoVisGraph.GridVertex;
import org.graphper.layout.OrthoVisGraph.Segment;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

public abstract class Maze {

  private static final int LEFT = 0b0001;
  private static final int RIGHT = 0b0010;
  private static final int UP = 0b0100;
  private static final int DOWN = 0b1000;
  private static final int RIGHT_ACCESSED = 0x10;
  private static final int DOWN_ACCESSED = 0x20;
  private static final int LEFT_UP_CORNET = 0x40;
  private static final double INTERNAL_OFFSET = 0.01;

  private OrthoVisGraph ovg;
  private final DrawGraph drawGraph;
  private final Map<Box, Cell> cellMap;
  private Map<Box, GridVertex> guideVertex;

  protected Maze(DrawGraph drawGraph) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.drawGraph = drawGraph;
    this.cellMap = new LinkedHashMap<>();
  }

  protected void init() {
    GridBuilder gridBuilder = Grid.builder();

    // Add horizontal and vertical sorted grid axis
    initGrid(gridBuilder);

    // Add obstacle record for grid
    createOrthoVisGraph(gridBuilder);
  }

  Cell getCell(Box cellKey) {
    return cellMap.get(cellKey);
  }

  GridVertex getGuideVertex(Box sign) {
    if (sign == null || guideVertex == null) {
      return null;
    }
    return guideVertex.get(sign);
  }

  protected void addCell(Box cellKey, Cell cell, GridBuilder gridBuilder) {
    if (cellKey == null || cell == null || gridBuilder == null) {
      return;
    }
    cell.check();
    cellMap.put(cellKey, cell);
    addToGrid(gridBuilder, cell);
  }

  protected void addGuideBox(Box guideVertexKey, GridBuilder gridBuilder) {
    if (guideVertexKey == null || gridBuilder == null) {
      return;
    }
    guideVertexKey.check();
    addGuideBox(guideVertexKey);
    addToGrid(gridBuilder, guideVertexKey);
  }

  protected abstract void initGrid(GridBuilder gridBuilder);

  // ------------------------------------------------------ private method ------------------------------------------------------

  private Iterable<? extends Box> boxes() {
    if (guideVertex == null) {
      return cellMap.values();
    }
    return new BiConcatIterable<>(cellMap.values(), guideVertex.keySet());
  }

  private void addGuideBox(Box box) {
    if (guideVertex == null) {
      guideVertex = new LinkedHashMap<>();
    }
    guideVertex.put(box, null);
  }

  private void addToGrid(GridBuilder gridBuilder, Box box) {
    gridBuilder
        .addVerAxis(box.getLeftBorder())
        .addVerAxis(box.getRightBorder())
        .addHorAxis(box.getUpBorder())
        .addHorAxis(box.getDownBorder());
  }

  private void createOrthoVisGraph(GridBuilder builder) {
    Grid grid = builder.build();
    this.ovg = new OrthoVisGraph();
    int[][] track = new int[grid.rowNum()][grid.colNum()];

    // Mark corner track value
    for (Cell cell : cellMap.values()) {
      initAxisLaunchItems(grid, track, cell, true);
    }

    // Mark guide track value
    if (guideVertex != null) {
      for (Box box : guideVertex.keySet()) {
        initAxisLaunchItems(grid, track, box, false);
      }
    }

    // Mark track value of other node in grid
    for (Box box : boxes()) {
      GridAxis leftAxis = grid.getVerAxis(box.getLeftBorder());
      GridAxis rightAxis = grid.getVerAxis(box.getRightBorder());
      GridAxis topAxis = grid.getHorAxis(box.getUpBorder());
      GridAxis bottomAxis = grid.getHorAxis(box.getDownBorder());

      /*
       *  Nodes extend in different directions, passing nodes will mark the corresponding direction
       */
      directAccess(true, true, LEFT, track, topAxis, bottomAxis, leftAxis);
      directAccess(false, true, RIGHT, track, topAxis, bottomAxis, rightAxis);
      directAccess(true, false, UP, track, leftAxis, rightAxis, topAxis);
      directAccess(false, false, DOWN, track, leftAxis, rightAxis, bottomAxis);
    }

    // Initialize ovg
    initOvg(grid, track);

    // Generate the debug grid
    generateGrid(grid, track);
  }

  private void initAxisLaunchItems(Grid grid, int[][] track, Box box, boolean isCell) {
    GridAxis leftAxis = grid.getVerAxis(box.getLeftBorder());
    GridAxis rightAxis = grid.getVerAxis(box.getRightBorder());
    GridAxis topAxis = grid.getHorAxis(box.getUpBorder());
    GridAxis bottomAxis = grid.getHorAxis(box.getDownBorder());

    if (isCell) {
      markNodeLeftUpCorner(track, topAxis.getIdx(), leftAxis.getIdx());
    }

    GridAxis current = leftAxis.next();
    // Mark the left and right access of the points on top and bottom
    while (current != null && current.getVal() < rightAxis.getVal()) {
      if (isCell) {
        topAxis.addBlockAxis(current.getVal());
        bottomAxis.addBlockAxis(current.getVal());
      }
      markLeft(track, topAxis.getIdx(), current.getIdx());
      markRight(track, topAxis.getIdx(), current.getIdx());
      markLeft(track, bottomAxis.getIdx(), current.getIdx());
      markRight(track, bottomAxis.getIdx(), current.getIdx());
      current = current.next();
    }

    current = topAxis.next();
    // Mark the top and bottom access of the points on left and right
    while (current != null && current.getVal() < bottomAxis.getVal()) {
      if (isCell) {
        leftAxis.addBlockAxis(current.getVal());
        rightAxis.addBlockAxis(current.getVal());
      }
      markUp(track, current.getIdx(), leftAxis.getIdx());
      markDown(track, current.getIdx(), leftAxis.getIdx());
      markUp(track, current.getIdx(), rightAxis.getIdx());
      markDown(track, current.getIdx(), rightAxis.getIdx());
      current = current.next();
    }

    // Set corner node track value
    nodeTrack(track, leftAxis, rightAxis, topAxis, bottomAxis);
  }

  private void directAccess(boolean isPre, boolean isHor, int dir,
                            int[][] track, GridAxis firstAxis,
                            GridAxis secondAxis, GridAxis startAxis) {
    boolean fc = true;
    boolean sc = true;
    GridAxis pre = null;
    GridAxis current = startAxis;

    do {
      boolean isFirst = current == startAxis;
      if (fc) {
        fc = directAccess(isHor, isFirst, dir, track, firstAxis, startAxis, pre, current);
      }

      if (sc) {
        sc = directAccess(isHor, isFirst, dir, track, secondAxis, startAxis, pre, current);
      }

      pre = current;
      current = isPre ? current.pre() : current.next();
    } while (current != null && (fc || sc));
  }

  private boolean directAccess(boolean isHor, boolean isFirst, int dir,
                               int[][] track, GridAxis axis, GridAxis startAxis,
                               GridAxis pre, GridAxis current) {
    int v = isHor
        ? track[axis.getIdx()][current.getIdx()]
        : track[current.getIdx()][axis.getIdx()];

    if (!isFirst) {
      markDir(track, reverseDir(dir), isHor, axis, current);
    }
    if (pre != null) {
      markDir(track, dir, isHor, axis, pre);
    }

    return (current == startAxis || notHaveVertex(v))
        && (current.isNotBlock(axis.getVal()) || isFirst);
  }

  private void initOvg(Grid grid, int[][] track) {
    GridAxis firstHorAxis = grid.getFirstHorAxis();
    GridAxis firstVerAxis = grid.getFirstVerAxis();
    Map<Integer, GridVertex> vertexMap = new HashMap<>();

    // Connect the out node and edge to ovg
    generateOutCellVertexEdge(grid, track, firstVerAxis, firstHorAxis, vertexMap);

    // Connect the internal cell node to ovg
    generateInCellVertexEdge(grid, track, vertexMap);

    // Find the center grid vertex of guide box in the grid
    generateGuideBoxVertex(grid, vertexMap);

    // Check ovg is correctly
    checkOvg();
  }

  private void generateOutCellVertexEdge(Grid grid, int[][] track, GridAxis firstVerAxis,
                                         GridAxis firstHorAxis,
                                         Map<Integer, GridVertex> vertexMap) {
    GridVertex pre = null;
    GridAxis horAxis = firstHorAxis;
    while (horAxis != null) {
      GridAxis verAxis = firstVerAxis;
      while (verAxis != null) {
        int val = track[horAxis.getIdx()][verAxis.getIdx()];
        if (!haveRight(val) || !haveDown(val) || isNodeLeftUpCorner(val)) {
          verAxis = verAxis.next();
          if (!haveRight(val) || isNodeLeftUpCorner(val)) {
            pre = null;
          }
          continue;
        }

        FlatPoint leftUp = new FlatPoint(verAxis.getVal(), horAxis.getVal());
        FlatPoint rightDown = findRightDown(track, horAxis, verAxis);

        GridVertex vertex = new GridVertex(leftUp, rightDown);
        ovg.add(vertex);
        vertexMap.put(grid.coordToIdx(horAxis.getIdx(), verAxis.getIdx()), vertex);
        if (pre != null) {
          ovg.addLeft(vertex, pre);
        }
        pre = vertex;

        verAxis = verAxis.next();
      }

      horAxis = horAxis.next();
    }

    pre = null;
    GridAxis verAxis = firstVerAxis;
    while (verAxis != null) {
      horAxis = firstHorAxis;
      while (horAxis != null) {
        GridVertex vertex = vertexMap.get(grid.coordToIdx(horAxis.getIdx(), verAxis.getIdx()));
        if (vertex == null) {
          horAxis = horAxis.next();
          continue;
        }

        if (pre != null && isVerContinuous(pre, vertex)) {
          ovg.addTop(vertex, pre);
        }
        pre = vertex;
        horAxis = horAxis.next();
      }

      verAxis = verAxis.next();
    }
  }

  private void generateInCellVertexEdge(Grid grid, int[][] track,
                                        Map<Integer, GridVertex> vertexMap) {
    for (Cell cell : cellMap.values()) {
      if (!cell.needInternalVertex()) {
        continue;
      }

      GridAxis leftAxis = grid.getVerAxis(cell.getLeftBorder());
      GridAxis rightAxis = grid.getVerAxis(cell.getRightBorder());
      GridAxis topAxis = grid.getHorAxis(cell.getUpBorder());
      GridAxis bottomAxis = grid.getHorAxis(cell.getDownBorder());

      GridAxis current = leftAxis;
      while (current != null && current.getVal() <= rightAxis.getVal()) {
        int val = track[topAxis.getIdx()][current.getIdx()];
        // Top axis, find the point which is right down point of cell vertex
        if (current.getVal() > leftAxis.getVal() && haveUp(val) && haveLeft(val)) {
          Integer idx = findCellVertexByRightDown(track, grid, topAxis, current);
          if (idx != null) {
            GridVertex adjVertex = vertexMap.get(idx);
            Asserts.illegalArgument(adjVertex == null, "Can not found ovg node");
            connectNodeInternal(adjVertex, cell, UP);
          }
        }

        val = track[bottomAxis.getIdx()][current.getIdx()];
        // Bottom axis, find the point which is left up point of cell vertex
        if (current.getVal() < rightAxis.getVal()
            && haveDown(val) && haveRight(val) && isNotNodeLeftUpCorner(val)) {
          GridVertex vertex = vertexMap.get(grid.coordToIdx(bottomAxis.getIdx(), current.getIdx()));
          Asserts.illegalArgument(vertex == null, "Can not found ovg node");
          connectNodeInternal(vertex, cell, DOWN);
        }

        current = current.next();
      }

      current = topAxis;
      while (current != null && current.getVal() <= bottomAxis.getVal()) {
        int val = track[current.getIdx()][leftAxis.getIdx()];
        // LEFT axis, find the point which is right down point of cell vertex
        if (current.getVal() > topAxis.getVal() && haveLeft(val) && haveUp(val)) {
          Integer idx = findCellVertexByRightDown(track, grid, current, leftAxis);
          if (idx != null) {
            GridVertex adjVertex = vertexMap.get(idx);
            Asserts.illegalArgument(adjVertex == null, "Can not found ovg node");
            connectNodeInternal(adjVertex, cell, LEFT);
          }
        }

        val = track[current.getIdx()][rightAxis.getIdx()];
        // Right axis, find the point which is left up point of cell vertex
        if (current.getVal() < bottomAxis.getVal()
            && haveRight(val) && haveDown(val) && isNotNodeLeftUpCorner(val)) {
          GridVertex vertex = vertexMap.get(grid.coordToIdx(current.getIdx(), rightAxis.getIdx()));
          Asserts.illegalArgument(vertex == null, "Can not found ovg node");
          connectNodeInternal(vertex, cell, RIGHT);
        }

        current = current.next();
      }
    }
  }

  private void connectNodeInternal(GridVertex adjVertex, Cell cell, int dir) {
    FlatPoint leftUp;
    FlatPoint rightDown;
    GridVertex vertex = null;
    switch (dir) {
      case LEFT:
        leftUp = new FlatPoint(adjVertex.getRightDown().getX(),
                               adjVertex.getLeftUp().getY());
        rightDown = new FlatPoint(cell.getRightBorder() - INTERNAL_OFFSET,
                                  adjVertex.getRightDown().getY());
        vertex = new GridVertex(leftUp, rightDown);
        ovg.addLeft(vertex, adjVertex);
        break;
      case RIGHT:
        leftUp = new FlatPoint(cell.getLeftBorder() + INTERNAL_OFFSET,
                               adjVertex.getLeftUp().getY());
        rightDown = new FlatPoint(adjVertex.getLeftUp().getX(),
                                  adjVertex.getRightDown().getY());
        vertex = new GridVertex(leftUp, rightDown);
        ovg.addRight(vertex, adjVertex);
        break;
      case UP:
        leftUp = new FlatPoint(adjVertex.getLeftUp().getX(),
                               adjVertex.getRightDown().getY());
        rightDown = new FlatPoint(adjVertex.getRightDown().getX(),
                                  cell.getDownBorder() - INTERNAL_OFFSET);
        vertex = new GridVertex(leftUp, rightDown);
        ovg.addTop(vertex, adjVertex);
        break;
      case DOWN:
        leftUp = new FlatPoint(adjVertex.getLeftUp().getX(),
                               cell.getUpBorder() + INTERNAL_OFFSET);
        rightDown = new FlatPoint(adjVertex.getRightDown().getX(),
                                  adjVertex.getLeftUp().getY());
        vertex = new GridVertex(leftUp, rightDown);
        ovg.addBottom(vertex, adjVertex);
        break;
      default:
        break;
    }

    if (vertex != null) {
      cell.addAxisVertex(vertex);
      vertex.markInternalNode();
    }
  }

  private FlatPoint findRightDown(int[][] track, GridAxis hor, GridAxis ver) {
    GridAxis verAxis = ver.next();

    while (verAxis != null) {
      int val = track[hor.getIdx()][verAxis.getIdx()];
      if (haveLeft(val) && haveDown(val)) {
        break;
      }
      verAxis = verAxis.next();
    }
    Asserts.illegalArgument(verAxis == null, "Can not found right down vertex");

    GridAxis horAxis = hor.next();
    while (horAxis != null) {
      int val = track[horAxis.getIdx()][verAxis.getIdx()];
      if (haveLeft(val) && haveUp(val)) {
        break;
      }
      horAxis = horAxis.next();
    }
    Asserts.illegalArgument(horAxis == null, "Can not found right down vertex");

    return new FlatPoint(verAxis.getVal(), horAxis.getVal());
  }

  private Integer findCellVertexByRightDown(int[][] track, Grid grid, GridAxis hor, GridAxis ver) {
    GridAxis verAxis = ver.pre();

    while (verAxis != null) {
      int val = track[hor.getIdx()][verAxis.getIdx()];
      if (haveRight(val) && haveUp(val)) {
        break;
      }
      verAxis = verAxis.pre();
    }
    Asserts.illegalArgument(verAxis == null, "Can not found left up vertex");

    GridAxis horAxis = hor.pre();
    while (horAxis != null) {
      int val = track[horAxis.getIdx()][verAxis.getIdx()];
      if (haveRight(val) && haveDown(val)) {
        break;
      }
      horAxis = horAxis.pre();
    }
    Asserts.illegalArgument(horAxis == null, "Can not found left up vertex");
    if (isNotNodeLeftUpCorner(track[horAxis.getIdx()][verAxis.getIdx()])) {
      return grid.coordToIdx(horAxis.getIdx(), verAxis.getIdx());
    }
    return null;
  }

  private void generateGuideBoxVertex(Grid grid, Map<Integer, GridVertex> vertexMap) {
    if (guideVertex == null) {
      return;
    }

    for (Box box : guideVertex.keySet()) {
      GridVertex vertex = getBoxCenterVertex(box, grid, vertexMap);
      if (vertex != null) {
        guideVertex.put(box, vertex);
      }
    }
  }

  private GridVertex getBoxCenterVertex(Box box, Grid grid, Map<Integer, GridVertex> vertexMap) {
    GridAxis leftAxis = grid.getVerAxis(box.getLeftBorder());
    GridAxis rightAxis = grid.getVerAxis(box.getRightBorder());
    GridAxis topAxis = grid.getHorAxis(box.getUpBorder());
    GridAxis bottomAxis = grid.getHorAxis(box.getDownBorder());

    GridAxis currentVer = leftAxis;
    while (currentVer != null && currentVer.getVal() < rightAxis.getVal()) {
      GridAxis currentHor = topAxis;
      while (currentHor != null && currentHor.getVal() < bottomAxis.getVal()) {
        GridVertex vertex = vertexMap
            .get(grid.coordToIdx(currentHor.getIdx(), currentVer.getIdx()));
        if (vertexOverlapBoxCenter(vertex, box)) {
          return vertex;
        }

        currentHor = currentHor.next();
      }

      currentVer = currentVer.next();
    }

    return null;
  }

  private boolean vertexOverlapBoxCenter(GridVertex vertex, Box box) {
    if (vertex == null || box == null) {
      return false;
    }

    FlatPoint leftUp = vertex.getLeftUp();
    FlatPoint rightDown = vertex.getRightDown();

    return leftUp.getX() <= box.getX() && leftUp.getY() <= box.getY()
        && rightDown.getX() >= box.getX() && rightDown.getY() >= box.getY();
  }

  private void checkOvg() {
    if (!Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("ovg.check"))) {
      return;
    }

    for (GridVertex node : ovg.nodes()) {
      GridVertex left = node.getLeft();
      GridVertex right = node.getRight();
      GridVertex top = node.getTop();
      GridVertex bottom = node.getBottom();

      Asserts.illegalArgument(left != null && !isHorContinuous(left, node),
                              "Left vertex is not continuous");
      Asserts.illegalArgument(right != null && !isHorContinuous(node, right),
                              "Right vertex is not continuous");
      Asserts.illegalArgument(top != null && !isVerContinuous(top, node),
                              "Top vertex is not continuous");
      Asserts.illegalArgument(bottom != null && !isVerContinuous(node, bottom),
                              "Bottom vertex is not continuous");
    }
  }

  private boolean isHorContinuous(GridVertex pre, GridVertex vertex) {
    return pre.getRightDown().getX() == vertex.getLeftUp().getX()
        && pre.getLeftUp().getY() == vertex.getLeftUp().getY()
        && pre.getRightDown().getY() == vertex.getRightDown().getY();
  }

  private boolean isVerContinuous(GridVertex pre, GridVertex vertex) {
    return pre.getRightDown().getY() == vertex.getLeftUp().getY()
        && pre.getLeftUp().getX() == vertex.getLeftUp().getX()
        && pre.getRightDown().getX() == vertex.getRightDown().getX();
  }

  private void generateGrid(Grid grid, int[][] track) {
    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    if (!graphvizDrawProp.getGraphviz().graphAttrs().isShowGrid()) {
      return;
    }

    GridAxis firstHorAxis = grid.getFirstHorAxis();
    GridAxis firstVerAxis = grid.getFirstVerAxis();
    GridAxis horAxis = firstHorAxis;

    while (horAxis != null) {
      GridAxis verAxis = firstVerAxis;
      while (verAxis != null) {
        if (notHaveVertex(track[horAxis.getIdx()][verAxis.getIdx()])) {
          verAxis = verAxis.next();
          continue;
        }

        if (rightNotAccessed(track, horAxis.getIdx(), verAxis.getIdx())) {
          Segment segment = new Segment();
          double x = rightAccess(verAxis, horAxis.getIdx(), verAxis.getIdx(), track);
          segment.setStart(new FlatPoint(verAxis.getVal(), horAxis.getVal()));
          segment.setEnd(new FlatPoint(x, horAxis.getVal()));
          addSegment(segment);
        }

        if (bottomNotAccessed(track, horAxis.getIdx(), verAxis.getIdx())) {
          Segment segment = new Segment();
          double y = bottomAccess(horAxis, horAxis.getIdx(), verAxis.getIdx(), track);
          segment.setStart(new FlatPoint(verAxis.getVal(), horAxis.getVal()));
          segment.setEnd(new FlatPoint(verAxis.getVal(), y));
          addSegment(segment);
        }

        verAxis = verAxis.next();
      }

      horAxis = horAxis.next();
    }
  }

  private double rightAccess(GridAxis axis, int row, int col, int[][] track) {
    markAccessed(track, row, col, RIGHT_ACCESSED);
    if (!haveRight(track[row][col])) {
      return axis.getVal();
    }
    return rightAccess(axis.next(), row, col + 1, track);
  }

  private double bottomAccess(GridAxis axis, int row, int col, int[][] track) {
    markAccessed(track, row, col, DOWN_ACCESSED);
    if (!haveDown(track[row][col])) {
      return axis.getVal();
    }
    return bottomAccess(axis.next(), row + 1, col, track);
  }

  private void addSegment(Segment segment) {
    FlatPoint start = segment.getStart();
    FlatPoint end = segment.getEnd();

    drawGraph.updateXAxisRange(start.getX());
    drawGraph.updateXAxisRange(end.getX());
    drawGraph.updateYAxisRange(start.getY());
    drawGraph.updateYAxisRange(end.getY());

    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    graphvizDrawProp.addSegment(segment);
  }

  private void nodeTrack(int[][] track, GridAxis leftAxis, GridAxis rightAxis,
                         GridAxis topAxis, GridAxis bottomAxis) {
    markRight(track, topAxis.getIdx(), leftAxis.getIdx());
    markDown(track, topAxis.getIdx(), leftAxis.getIdx());

    markLeft(track, topAxis.getIdx(), rightAxis.getIdx());
    markDown(track, topAxis.getIdx(), rightAxis.getIdx());

    markRight(track, bottomAxis.getIdx(), leftAxis.getIdx());
    markUp(track, bottomAxis.getIdx(), leftAxis.getIdx());

    markLeft(track, bottomAxis.getIdx(), rightAxis.getIdx());
    markUp(track, bottomAxis.getIdx(), rightAxis.getIdx());
  }

  private int markDir(int[][] track, int dir, boolean isHor, GridAxis a1, GridAxis a2) {
    int val;
    if (isHor) {
      val = track[a1.getIdx()][a2.getIdx()] |= dir;
    } else {
      val = track[a2.getIdx()][a1.getIdx()] |= dir;
    }
    return val;
  }

  private void markNodeLeftUpCorner(int[][] track, int row, int col) {
    track[row][col] |= LEFT_UP_CORNET;
  }

  private boolean isNotNodeLeftUpCorner(int val) {
    return !isNodeLeftUpCorner(val);
  }


  private boolean isNodeLeftUpCorner(int val) {
    return (val & LEFT_UP_CORNET) == LEFT_UP_CORNET;
  }

  private boolean notHaveVertex(int val) {
    return !haveVertex(val);
  }

  private boolean haveVertex(int val) {
    return haveHor(val) && haveVer(val);
  }

  private boolean haveHor(int val) {
    return haveLeft(val) || haveRight(val);
  }

  private boolean haveVer(int val) {
    return haveUp(val) || haveDown(val);
  }

  private void markLeft(int[][] track, int row, int col) {
    track[row][col] |= LEFT;
  }

  private void markRight(int[][] track, int row, int col) {
    track[row][col] |= RIGHT;
  }

  private void markUp(int[][] track, int row, int col) {
    track[row][col] |= UP;
  }

  private void markDown(int[][] track, int row, int col) {
    track[row][col] |= DOWN;
  }

  private boolean haveLeft(int val) {
    return (val & LEFT) == LEFT;
  }

  private boolean haveRight(int val) {
    return (val & RIGHT) == RIGHT;
  }

  private boolean haveUp(int val) {
    return (val & UP) == UP;
  }

  private boolean haveDown(int val) {
    return (val & DOWN) == DOWN;
  }

  private boolean rightNotAccessed(int[][] track, int row, int col) {
    return !rightAccessed(track, row, col);
  }

  private boolean bottomNotAccessed(int[][] track, int row, int col) {
    return !bottomAccessed(track, row, col);
  }

  private boolean rightAccessed(int[][] track, int row, int col) {
    return rightAccessed(track[row][col]);
  }

  private boolean bottomAccessed(int[][] track, int row, int col) {
    return downAccessed(track[row][col]);
  }

  private boolean rightAccessed(int val) {
    return (val & RIGHT_ACCESSED) == RIGHT_ACCESSED;
  }

  private boolean downAccessed(int val) {
    return (val & DOWN_ACCESSED) == DOWN_ACCESSED;
  }

  private void markAccessed(int[][] track, int row, int col, int accessMark) {
    track[row][col] |= accessMark;
  }

  private int reverseDir(int dir) {
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
        return 0;
    }
  }

  public static abstract class Cell implements Box {

    private List<GridVertex> axisVertexes;

    private void addAxisVertex(GridVertex vertex) {
      if (axisVertexes == null) {
        axisVertexes = new ArrayList<>();
      }
      axisVertexes.add(vertex);
    }

    boolean in(double x, double y) {
      return inHor(x) && inVer(y);
    }

    boolean inHor(double val) {
      return val > getLeftBorder() && val < getRightBorder();
    }

    boolean inVer(double val) {
      return val > getUpBorder() && val < getDownBorder();
    }

    boolean needInternalVertex() {
      return true;
    }

    List<GridVertex> getAxisVertexes() {
      return CollectionUtils.isNotEmpty(axisVertexes)
          ? axisVertexes : Collections.emptyList();
    }
  }

  public static class NodeCell extends Cell {

    private final DNode node;

    public NodeCell(DNode node) {
      Asserts.nullArgument(node, "node");
      this.node = node;
    }

    @Override
    public double getLeftBorder() {
      return node.getLeftBorder();
    }

    @Override
    public double getRightBorder() {
      return node.getRightBorder();
    }

    @Override
    public double getUpBorder() {
      return node.getUpBorder();
    }

    @Override
    public double getDownBorder() {
      return node.getDownBorder();
    }

    @Override
    public double getX() {
      return node.getX();
    }

    @Override
    public double getY() {
      return node.getY();
    }

    @Override
    public String toString() {
      return "NodeCell{" +
          "node=" + node +
          '}';
    }
  }

  public static class VirtualCell extends Cell {

    private final Box box;

    public VirtualCell(Box box) {
      Asserts.nullArgument(box, "box");
      this.box = box;
    }

    @Override
    public double getX() {
      return box.getX();
    }

    @Override
    public double getY() {
      return box.getY();
    }

    @Override
    public double getLeftBorder() {
      return box.getLeftBorder();
    }

    @Override
    public double getRightBorder() {
      return box.getRightBorder();
    }

    @Override
    public double getUpBorder() {
      return box.getUpBorder();
    }

    @Override
    public double getDownBorder() {
      return box.getDownBorder();
    }

    @Override
    boolean needInternalVertex() {
      return false;
    }
  }
}
