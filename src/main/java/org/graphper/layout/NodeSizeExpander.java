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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.Port;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.Asserts;
import org.graphper.util.ValueUtils;

/**
 * A utility class designed to dynamically expand the area occupied by a graph node in response to
 * specific layout requirements. This class is particularly useful in scenarios where nodes have
 * self-loops or other visual elements that require extra clearance to avoid intersection with
 * other nodes or edges.
 *
 * @author Jamison Jiang
 */
public class NodeSizeExpander {

  protected ANode node;

  /*
   * Node width/height and center of gravity adjustment
   */
  protected double leftWidthOffset;
  protected double rightWidthOffset;
  protected double topHeightOffset;
  protected double bottomHeightOffset;

  public double getLeftWidthOffset() {
    return leftWidthOffset;
  }

  public double getRightWidthOffset() {
    return rightWidthOffset;
  }

  public double getTopHeightOffset() {
    return topHeightOffset;
  }

  public double getBottomHeightOffset() {
    return bottomHeightOffset;
  }

  public static double minSelfInterval(ANode node) {
    if (node == null) {
      return 0;
    }
    return minSelfInterval(node.getSelfLoopCount(), node.getNodeSep());
  }

  public static double minSelfInterval(int lineNum, double nodeSep) {
    if (lineNum <= 1) {
      return nodeSep;
    }
    return (nodeSep + (nodeSep / lineNum)) / lineNum;
  }

  protected void addPoint(LineDrawProp lineDrawProp, FlatPoint point) {
    lineDrawProp.addAndNotRefreshDrawGraph(point);

    refreshVolume(point);
  }

  protected void refreshVolume(FlatPoint point) {
    refreshVolume(point.getX(), point.getY());
  }

  protected void refreshVolume(double x, double y) {
    if (x < node.getLeftBorder()) {
      leftWidthOffset = Math.max(
          leftWidthOffset,
          node.getLeftBorder() - x
      );
    }
    if (x > node.getRightBorder()) {
      rightWidthOffset = Math.max(
          rightWidthOffset,
          x - node.getRightBorder()
      );
    }
    if (y < node.getUpBorder()) {
      topHeightOffset = Math.max(
          topHeightOffset,
          node.getUpBorder() - y
      );
    }
    if (y > node.getDownBorder()) {
      bottomHeightOffset = Math.max(
          bottomHeightOffset,
          y - node.getDownBorder()
      );
    }
  }

  protected Map<GroupKey, List<GroupEntry>> groupSelfLine(DrawGraph drawGraph, ANode node) {
    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node.getNode());
    Asserts.illegalArgument(nodeDrawProp == null, "Not found the node draw properties!");

    Map<GroupKey, List<GroupEntry>> selfLineGroup = new LinkedHashMap<>(1);

    for (int i = 0; i < node.getSelfLoopCount(); i++) {
      LineDrawProp line = node.selfLine(i);
      LineAttrs lineAttrs = line.lineAttrs();
      GroupKey key = newGroupKey(lineAttrs.getTailPort(), lineAttrs.getHeadPort(), nodeDrawProp,
                                 drawGraph, lineAttrs.getTailCell(), lineAttrs.getHeadCell());
      addLineToGroup(selfLineGroup, line, key);
    }

    return selfLineGroup;
  }

  // ------------------------------------- private method -------------------------------------
  private GroupKey newGroupKey(Port tailPort, Port headPort, NodeDrawProp nodeDrawProp,
                               DrawGraph drawGraph, String tailCell, String headCell) {
    if (tailPort == null && headPort == null && tailCell == null && headCell == null) {
      GroupKey groupKey = new GroupKey();
      groupKey.tailPoint = new FlatPoint(node.getX(), node.getY());
      groupKey.headPoint = groupKey.tailPoint.clone();
      return groupKey;
    }

    GroupKey groupKey = new GroupKey();
    groupKey.tailPort = tailPort;
    groupKey.headPort = headPort;
    groupKey.tailCell = getCell(nodeDrawProp, tailCell);
    groupKey.headCell = getCell(nodeDrawProp, headCell);

    groupKey.tailPoint = PortHelper.getPortPoint(node, tailCell, tailPort, drawGraph);
    if (groupKey.samePoint()) {
      groupKey.headPoint = groupKey.tailPoint;
    } else {
      groupKey.headPoint = PortHelper.getPortPoint(node, headCell, headPort, drawGraph);
    }
    return groupKey;
  }

  private static void addLineToGroup(Map<GroupKey, List<GroupEntry>> selfLineGroup,
                                     LineDrawProp selfLine, GroupKey groupKey) {
    selfLineGroup.compute(groupKey, (g, v) -> {
      if (v == null) {
        v = new ArrayList<>(1);
      }
      v.add(new GroupEntry(groupKey, selfLine));
      return v;
    });
  }

  private Cell getCell(NodeDrawProp nodeDrawProp, String cellId) {
    RootCell rootCell = nodeDrawProp.getCell();
    if (rootCell == null) {
      return null;
    }
    return rootCell.getCellById(cellId);
  }

  protected static class GroupEntry {

    protected final GroupKey groupKey;

    protected final LineDrawProp line;

    public GroupEntry(GroupKey groupKey, LineDrawProp line) {
      Asserts.nullArgument(groupKey, "groupKey");
      Asserts.nullArgument(line, "line");
      this.groupKey = groupKey;
      this.line = line;
    }

    public LineDrawProp getLine() {
      return line;
    }
  }

  protected static class GroupKey {

    protected Port tailPort;

    protected Port headPort;

    protected Cell tailCell;

    protected Cell headCell;

    protected FlatPoint tailPoint;

    protected FlatPoint headPoint;

    public Port getTailPort() {
      return tailPort;
    }

    public Port getHeadPort() {
      return headPort;
    }

    public Cell getTailCell() {
      return tailCell;
    }

    public Cell getHeadCell() {
      return headCell;
    }

    public boolean samePoint() {
      return tailCell == headCell && tailPort == headPort;
    }

    public FlatPoint getTailPoint() {
      Asserts.illegalArgument(tailPoint == null, "GroupKey Not Ready");
      return tailPoint.clone();
    }

    public FlatPoint getHeadPoint() {
      Asserts.illegalArgument(headPoint == null, "GroupKey Not Ready");
      return headPoint.clone();
    }

    public boolean isOnlySameHor() {
      if (samePoint() || tailCell != headCell) {
        return false;
      }

      if (Objects.equals(tailPoint, headPoint) || tailPoint == null || headPoint == null) {
        return false;
      }

      return ValueUtils.approximate(tailPoint.getY(), headPoint.getY())
          && !ValueUtils.approximate(tailPoint.getX(), headPoint.getX());
    }

    public boolean isOnlySameVer() {
      if (samePoint() || tailCell != headCell) {
        return false;
      }

      if (Objects.equals(tailPoint, headPoint) || tailPoint == null || headPoint == null) {
        return false;
      }

      return ValueUtils.approximate(tailPoint.getX(), headPoint.getX())
          && !ValueUtils.approximate(tailPoint.getY(), headPoint.getY());
    }

    public boolean sameCell() {
      return tailCell == headCell;
    }

    public boolean notSameCell() {
      return !sameCell();
    }

    public boolean havePortOrCell() {
      return tailPort != null || headPort != null || tailCell != null || headCell != null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      GroupKey groupKey = (GroupKey) o;
      return (tailPort == groupKey.tailPort && headPort == groupKey.headPort
          && Objects.equals(tailCell, groupKey.tailCell)
          && Objects.equals(headCell, groupKey.headCell))
          || (tailPort == groupKey.headPort && headPort == groupKey.tailPort
          && Objects.equals(tailCell, groupKey.headCell)
          && Objects.equals(headCell, groupKey.tailCell));
    }

    @Override
    public int hashCode() {
      return Objects.hash(tailPort, headPort, tailCell, headCell)
          + Objects.hash(headPort, tailPort, headCell, tailCell);
    }
  }
}
