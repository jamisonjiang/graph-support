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
import java.util.Comparator;
import java.util.List;
import org.graphper.api.GraphContainer;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.FlatPoint;
import org.graphper.def.VertexIndex;
import org.graphper.draw.DrawGraph;
import org.graphper.util.CollectionUtils;

class DNode extends VertexIndex implements Box, ShapePosition {

  private static final long serialVersionUID = -7182604069185202045L;

  public static final int FLAT_LABEL_GAP = 5;

  private static final int RANK_MODEL_BIT = Integer.SIZE - 1;

  private static final int AUX_MODE = 1;

  private static final int NOT_ADJUST_MID = 0x400;

  private final Node node;

  private int rank;

  // The rank index of the node
  private int rankIndex;

  // Auxiliary level for coordinate generation
  private int auxRank;

  // In the current traversal tree, among the traversal paths
  // passing through the current vertex, the minimum subsequent traversal value.
  private int low;

  // Subsequent traversal values for the current vertex.
  private int lim;

  // Median value, used to sort the same rank.
  private double median;

  private double x;

  private double y;

  private double width;

  private double height;

  // Distance between nodes
  private double nodeSep;

  // Node status
  private int status;

  // Self lines
  private List<DLine> selfLines;

  // The parent container, if it is null, it means the root container
  private GraphContainer container;

  private NodeAttrs nodeAttrs;

  private Line labelLine;

  private final DLine flatLabelLine;

  private NodeSizeExpander nodeSizeExpander;

  DNode(Node node, double width, double height, double nodeSep) {
    this(node, width, height, nodeSep, null, null);
  }

  DNode(Node node, double width, double height, double nodeSep, Line labelLine) {
    this(node, width, height, nodeSep, labelLine, null);
  }

  DNode(Node node, double width, double height, double nodeSep, DLine labelLine) {
    this(node, width, height, nodeSep, null, labelLine);
  }

  private DNode(Node node, double width, double height, double nodeSep, Line labelLine,
                DLine flatLabelLine) {
    this.node = node;
    this.width = width;
    this.height = height;
    this.nodeSep = nodeSep;
    this.labelLine = labelLine;
    this.flatLabelLine = flatLabelLine;

    setFlatLabelSize(flatLabelLine);
  }

  private void setFlatLabelSize(DLine flatLabelLine) {
    if (flatLabelLine == null) {
      return;
    }

    this.height = 0;
    this.width = 0;
    for (int i = 0; i < flatLabelLine.getParallelNums(); i++) {
      DLine line = flatLabelLine.parallelLine(i);
      FlatPoint labelSize = line.getLabelSize();
      if (labelSize == null) {
        this.height += FLAT_LABEL_GAP;
        continue;
      }

      this.height += labelSize.getHeight();
      this.width = Math.max(labelSize.getWidth(), this.width);
    }
  }

  static DNode newVirtualNode(double nodeSep, GraphContainer container) {
    DNode node = new DNode(null, 20, 1, nodeSep);
    node.setContainer(container);
    return node;
  }

  Node getNode() {
    return node;
  }

  void setNodeAttrs(NodeAttrs nodeAttrs) {
    this.nodeAttrs = nodeAttrs;
  }

  boolean isVirtual() {
    return node == null;
  }

  boolean isLabelNode() {
    return labelLine != null;
  }

  void setLabelLine(Line labelLine) {
    this.labelLine = labelLine;
  }

  boolean isFlatLabelNode() {
    return flatLabelLine != null;
  }

  int getRankIgnoreModel() {
    int s = status;
    switchNormalModel();
    int rank = getRank();
    this.status = s;
    return rank;
  }

  int getRank() {
    if (isNormalModel()) {
      return rank;
    } else if (isAuxModel()) {
      return auxRank;
    } else {
      throw new IllegalStateException("Node unknown status");
    }
  }

  void setRank(int rank) {
    if (isNormalModel()) {
      this.rank = rank;
    } else if (isAuxModel()) {
      this.auxRank = rank;
    } else {
      throw new IllegalStateException("Node unknown status");
    }
  }

  void setAuxRank(int auxRank) {
    this.auxRank = auxRank;
  }

  int getLow() {
    return low;
  }

  void setLow(int low) {
    this.low = low;
  }

  int getLim() {
    return lim;
  }

  void setLim(int lim) {
    this.lim = lim;
  }

  double getMedian() {
    return median;
  }

  void setMedian(double median) {
    this.median = median;
  }

  @Override
  public double getX() {
    return x;
  }

  void setX(double x) {
    this.x = x;
  }

  @Override
  public double getY() {
    return y;
  }

  void setY(double y) {
    this.y = y;
  }

  @Override
  public double getWidth() {
    return width;
  }

  void setWidth(int width) {
    this.width = width;
  }

  @Override
  public double getHeight() {
    return height;
  }

  void setHeight(int height) {
    this.height = height;
  }

  boolean isNormalModel() {
    return (status << RANK_MODEL_BIT >>> RANK_MODEL_BIT) == 0;
  }

  boolean isAuxModel() {
    return (status << RANK_MODEL_BIT >>> RANK_MODEL_BIT) == AUX_MODE;
  }

  boolean notAdjust() {
    return (status & NOT_ADJUST_MID) == NOT_ADJUST_MID;
  }

  void switchAuxModel() {
    status |= AUX_MODE;
  }

  void switchNormalModel() {
    status >>= 1;
    status <<= 1;
  }

  void markNotAdjustMid() {
    status |= NOT_ADJUST_MID;
  }

  int getSelfLoopCount() {
    return selfLines == null ? 0 : selfLines.size();
  }

  void addSelfLine(DLine line) {
    if (line == null) {
      return;
    }

    if (selfLines == null) {
      selfLines = new ArrayList<>(2);
    }
    selfLines.add(line);
  }

  void sortSelfLine(Comparator<DLine> lineComparator) {
    if (lineComparator == null || CollectionUtils.isEmpty(selfLines)) {
      return;
    }

    selfLines.sort(lineComparator);
  }

  DLine selfLine(int index) {
    return CollectionUtils.isEmpty(selfLines) ? null : selfLines.get(index);
  }

  boolean haveSelfLine() {
    return CollectionUtils.isNotEmpty(selfLines);
  }

  List<DLine> getSelfLines() {
    return selfLines == null ? Collections.emptyList() : selfLines;
  }

  void initNodeSizeExpander(DrawGraph drawGraph) {
    if (isVirtual() || !haveSelfLine() || nodeSizeExpander != null) {
      return;
    }

    Splines splines = drawGraph.getGraphviz().graphAttrs().getSplines();
    if (splines == Splines.ORTHO) {
      nodeSizeExpander = new OrthoNodeSizeExpander(this);
    } else if (drawGraph.usePortAxisExpander()) {
      nodeSizeExpander = new PortNodeSizeExpander(drawGraph, this);
    } else {
      nodeSizeExpander = new PortNodeSizeExpanderV2(drawGraph, this);
    }
  }

  NodeSizeExpander getNodeSizeExpander() {
    return nodeSizeExpander;
  }

  DLine getFlatLabelLine() {
    return flatLabelLine;
  }

  double leftWidth() {
    if (isLabelNode()) {
      return 0;
    }
    double lw = nodeShape().leftWidth(width);
    if (nodeSizeExpander != null) {
      lw += nodeSizeExpander.getLeftWidthOffset();
    }
    return lw;
  }

  double rightWidth() {
    if (isLabelNode()) {
      return width;
    }

    double rw = nodeShape().rightWidth(width);
    if (nodeSizeExpander != null) {
      rw += nodeSizeExpander.getRightWidthOffset();
    }
    return rw;
  }

  double topHeight() {
    double th = nodeShape().topHeight(height);
    if (nodeSizeExpander != null) {
      th += nodeSizeExpander.getTopHeightOffset();
    }
    return th;
  }

  double bottomHeight() {
    double bh = nodeShape().bottomHeight(height);
    if (nodeSizeExpander != null) {
      bh += nodeSizeExpander.getBottomHeightOffset();
    }
    return bh;
  }

  double realLeftWidth() {
    return nodeShape().leftWidth(width);
  }

  double realRightWidth() {
    if (isLabelNode()) {
      return width;
    } else {
      return nodeShape().rightWidth(width);
    }
  }

  double realTopHeight() {
    return nodeShape().topHeight(height);
  }

  double realBottomHeight() {
    return nodeShape().bottomHeight(height);
  }

  @Override
  public double getLeftBorder() {
    if (isLabelNode()) {
      return getX() - leftWidth();
    }
    return getX() - realLeftWidth();
  }

  @Override
  public double getRightBorder() {
    if (isLabelNode()) {
      return getX() + rightWidth();
    }
    return getX() + realRightWidth();
  }

  @Override
  public double getUpBorder() {
    return getY() - realTopHeight();
  }

  @Override
  public double getDownBorder() {
    return getY() + realBottomHeight();
  }

  double getNodeSep() {
    return nodeSep;
  }

  void nodeSepHalving() {
    this.nodeSep /= 2;
  }

  String name() {
    if (isVirtual() || nodeAttrs == null) {
      if (isLabelNode()) {
        return labelLine.lineAttrs().getLabel();
      } else {
        return String.valueOf(hashCode());
      }
    } else {
      return nodeAttrs.getLabel() != null
          ? nodeAttrs.getLabel() : "none";
    }
  }

  int getAuxRank() {
    return auxRank;
  }

  int getRankIndex() {
    return rankIndex;
  }

  void setRankIndex(int rankIndex) {
    if (status == AUX_MODE) {
      return;
    }
    this.rankIndex = rankIndex;
  }

  Line getLabelLine() {
    return labelLine;
  }

  @Override
  public NodeShape nodeShape() {
    if (isVirtual() || nodeAttrs == null) {
      return NodeShapeEnum.CIRCLE;
    }

    return nodeAttrs.getNodeShape();
  }

  GraphContainer getContainer() {
    return container;
  }

  void setContainer(GraphContainer container) {
    this.container = container;
  }

  boolean isTail(DLine line) {
    if (line == null || line.isVirtual() || isVirtual()) {
      return false;
    }
    return node == line.getLine().tail();
  }

  @Override
  public String toString() {
    return "{name=" + name() + ",rank=" + getRank() + ",width=" + width + "}";
  }
}
