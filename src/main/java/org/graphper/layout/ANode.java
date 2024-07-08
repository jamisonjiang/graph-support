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

import org.graphper.api.GraphContainer;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.VertexIndex;

public class ANode extends VertexIndex implements Box, ShapePosition {

  private static final long serialVersionUID = 4047797184917587337L;

  protected final Node node;

  protected double x;

  protected double y;

  private double width;

  private double height;

  private NodeAttrs nodeAttrs;

  // The parent container, if it is null, it means the root container
  protected GraphContainer container;

  public ANode(Node node) {
    this.node = node;
  }

  public void setContainer(GraphContainer container) {
    this.container = container;
  }

  public GraphContainer getContainer() {
    return container;
  }

  public Node getNode() {
    return node;
  }

  public boolean empty() {
    return node == null;
  }

  @Override
  public double getWidth() {
    return width;
  }

  void setWidth(int width) {
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

  void setHeight(int height) {
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

  @Override
  public NodeShape shapeProp() {
    if (empty() || nodeAttrs == null) {
      return NodeShapeEnum.ELLIPSE;
    }

    return nodeAttrs.getNodeShape();
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public NodeAttrs getNodeAttrs() {
    return nodeAttrs;
  }

  public void setNodeAttrs(NodeAttrs nodeAttrs) {
    this.nodeAttrs = nodeAttrs;
  }
}
