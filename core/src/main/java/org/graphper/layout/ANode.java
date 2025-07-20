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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.graphper.api.GraphContainer;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Splines;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.util.CollectionUtils;

public class ANode implements Box, ShapePosition, Serializable {

  private static final long serialVersionUID = 4047797184917587337L;

  protected final NodeDrawProp nodeDrawProp;

  protected double x;

  protected double y;

  protected double width;

  protected double height;

  // Distance between nodes
  protected double nodeSep;

  // The parent container, if it is null, it means the root container
  protected GraphContainer container;

  // Self lines
  protected List<LineDrawProp> selfLines;

  protected NodeSizeExpander nodeSizeExpander;

  public ANode(NodeDrawProp nodeDrawProp) {
    this.nodeDrawProp = nodeDrawProp;
  }

  public void setContainer(GraphContainer container) {
    this.container = container;
  }

  public GraphContainer getContainer() {
    return container;
  }

  public Node getNode() {
    return nodeDrawProp != null ? nodeDrawProp.getNode() : null;
  }

  public boolean isVirtual() {
    return nodeDrawProp == null;
  }

  @Override
  public double getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public double getLeftBorder() {
     return getX() - shapeProp().leftWidth(width);
  }

  @Override
  public double getRightBorder() {
    return getX() + shapeProp().rightWidth(width);
  }

  @Override
  public double getUpBorder() {
    return getY() - shapeProp().topHeight(height);
  }

  @Override
  public double getDownBorder() {
    return getY() + shapeProp().bottomHeight(height);
  }

  @Override
  public double getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  @Override
  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double leftWidth() {
    double lw = shapeProp().leftWidth(width);
    if (nodeSizeExpander != null) {
      lw += nodeSizeExpander.getLeftWidthOffset();
    }
    return lw;
  }

  public double rightWidth() {
    double rw = shapeProp().rightWidth(width);
    if (nodeSizeExpander != null) {
      rw += nodeSizeExpander.getRightWidthOffset();
    }
    return rw;
  }

  public double topHeight() {
    double th = shapeProp().topHeight(height);
    if (nodeSizeExpander != null) {
      th += nodeSizeExpander.getTopHeightOffset();
    }
    return th;
  }

  public double bottomHeight() {
    double bh = shapeProp().bottomHeight(height);
    if (nodeSizeExpander != null) {
      bh += nodeSizeExpander.getBottomHeightOffset();
    }
    return bh;
  }

  @Override
  public NodeShape shapeProp() {
    if (isVirtual() || nodeDrawProp == null) {
      return NodeShapeEnum.ELLIPSE;
    }

    return nodeDrawProp.nodeAttrs().getShape();
  }

  public double getAreaWidth() {
    if (CollectionUtils.isEmpty(selfLines)) {
      return getWidth();
    }
    return leftWidth() + rightWidth();
  }

  public double getAreaHeight() {
    if (CollectionUtils.isEmpty(selfLines)) {
      return getHeight();
    }
    return topHeight() + bottomHeight();
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public double realLeftWidth() {
    return shapeProp().leftWidth(width);
  }

  public double realRightWidth() {
    return shapeProp().rightWidth(width);
  }

  public double realTopHeight() {
    return shapeProp().topHeight(height);
  }

  public double realBottomHeight() {
    return shapeProp().bottomHeight(height);
  }

  public NodeAttrs getNodeAttrs() {
    return nodeDrawProp != null ? nodeDrawProp.nodeAttrs() : null;
  }

//  public void setNodeAttrs(NodeAttrs nodeAttrs) {
//    if (nodeDrawProp != null) {
//      nodeDrawProp.setNodeAttrs(nodeAttrs);
//    }
//  }

  public double getNodeSep() {
    return nodeSep;
  }

  public void setNodeSep(double nodeSep) {
    this.nodeSep = nodeSep;
  }

  public int getSelfLoopCount() {
    return selfLines == null ? 0 : selfLines.size();
  }

  public void addSelfLine(LineDrawProp line) {
    if (line == null) {
      return;
    }

    if (selfLines == null) {
      selfLines = new ArrayList<>(2);
    }
    selfLines.add(line);
  }

  public void sortSelfLine(Comparator<LineDrawProp> lineComparator) {
    if (lineComparator == null || CollectionUtils.isEmpty(selfLines)) {
      return;
    }

    selfLines.sort(lineComparator);
  }

  public LineDrawProp selfLine(int index) {
    return CollectionUtils.isEmpty(selfLines) ? null : selfLines.get(index);
  }

  public boolean haveSelfLine() {
    return CollectionUtils.isNotEmpty(selfLines);
  }

  public List<LineDrawProp> getSelfLines() {
    return selfLines == null ? Collections.emptyList() : selfLines;
  }

  public void initNodeSizeExpander(DrawGraph drawGraph) {
    if (isVirtual() || !haveSelfLine() || nodeSizeExpander != null) {
      return;
    }

    Splines splines = drawGraph.getGraphviz().graphAttrs().getSplines();
    if (splines == Splines.ORTHO) {
      nodeSizeExpander = new OrthoNodeSizeExpander(this);
    } else {
      nodeSizeExpander = new PortNodeSizeExpanderV2(drawGraph, this);
    }
  }

  public NodeSizeExpander getNodeSizeExpander() {
    return nodeSizeExpander;
  }

  public NodeDrawProp getNodeDrawProp() {
    return nodeDrawProp;
  }
}
