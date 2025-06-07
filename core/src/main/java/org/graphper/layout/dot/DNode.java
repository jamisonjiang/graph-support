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
import org.graphper.api.GraphContainer;
import org.graphper.api.Line;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.FlatPoint;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.ANode;

class DNode extends ANode implements Box, ShapePosition {

  private static final long serialVersionUID = -7182604069185202045L;

  public static final int FLAT_LABEL_GAP = 5;

  private static final int RANK_MODEL_BIT = Integer.SIZE - 1;

  private static final int AUX_MODE = 1;

  private static final int NOT_ADJUST_MID = 0x400;

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

  // Node status
  private int status;

  private Line labelLine;

  private final DLine flatLabelLine;

  DNode(NodeDrawProp nodeDrawProp, double width, double height, double nodeSep) {
    this(nodeDrawProp, width, height, nodeSep, null, null);
  }

  DNode(NodeDrawProp nodeDrawProp, double width, double height, double nodeSep, Line labelLine) {
    this(nodeDrawProp, width, height, nodeSep, labelLine, null);
  }

  DNode(NodeDrawProp nodeDrawProp, double width, double height, double nodeSep, DLine labelLine) {
    this(nodeDrawProp, width, height, nodeSep, null, labelLine);
  }

  private DNode(NodeDrawProp nodeDrawProp, double width, double height, double nodeSep, Line labelLine,
                DLine flatLabelLine) {
    super(nodeDrawProp);
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

  int getRealRank() {
    return rank;
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

  @Override
  public double getY() {
    return y;
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

  DLine getFlatLabelLine() {
    return flatLabelLine;
  }

  @Override
  public double leftWidth() {
    if (isLabelNode()) {
      return 0;
    }
    return super.leftWidth();
  }

  @Override
  public double rightWidth() {
    if (isLabelNode()) {
      return width;
    }
    return super.rightWidth();
  }

  @Override
  public double realRightWidth() {
    if (isLabelNode()) {
      return width;
    }
    return super.realRightWidth();
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

  void nodeSepHalving() {
    this.nodeSep /= 2;
  }

  String name() {
    if (isVirtual() || getNodeAttrs() == null) {
      if (isLabelNode()) {
        return labelLine.lineAttrs().getLabel();
      } else {
        return String.valueOf(hashCode());
      }
    } else {
      return getNodeAttrs().getLabel() != null
          ? getNodeAttrs().getLabel() : "none";
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

  boolean isTail(DLine line) {
    if (line == null || line.isVirtual() || isVirtual()) {
      return false;
    }
    return Objects.equals(getNode(), line.getLine().tail());
  }

  @Override
  public String toString() {
    return "{name=" + name() + ",rank=" + getRank() + ",width=" + width + "}";
  }
}
