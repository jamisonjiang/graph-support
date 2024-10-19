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

package org.graphper.layout.fdp;

import org.graphper.api.Node;
import org.graphper.layout.ANode;

public class FNode extends ANode {

  private static final long serialVersionUID = 6123993374954612168L;
  private double edgedx;

  private double edgedy;

  private double repulsionX;

  private double repulsionY;

  private double dx;

  private double dy;

  public FNode(Node node) {
    super(node);
  }

  public void alignCenter() {
    setX(getX() - getWidth() / 2 + leftWidth());
    setY(getY() - getHeight() / 2 + topHeight());
  }

  public double getRepulsionX() {
    return repulsionX;
  }

  public void setRepulsionX(double repulsionX) {
    this.repulsionX = repulsionX;
  }

  public double getRepulsionY() {
    return repulsionY;
  }

  public void setRepulsionY(double repulsionY) {
    this.repulsionY = repulsionY;
  }

  public double getEdgedx() {
    return edgedx;
  }

  public void setEdgedx(double edgedx) {
    this.edgedx = edgedx;
  }

  public double getEdgedy() {
    return edgedy;
  }

  public void setEdgedy(double edgedy) {
    this.edgedy = edgedy;
  }

  public double getDx() {
    return dx;
  }

  public void setDx(double dx) {
    this.dx = dx;
  }

  public double getDy() {
    return dy;
  }

  public void setDy(double dy) {
    this.dy = dy;
  }

  public void setLocation(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void setRepulsionLocation(double x, double y) {
    this.repulsionX = (int) Math.floor(x + 0.5);
    this.repulsionY = (int) Math.floor(y + 0.5);
  }

  public boolean isOverlap(FNode node) {
    if (node == null) {
      return false;
    }

    double deltaX = Math.abs(getX() - node.getX());
    double deltaY= Math.abs(getY() - node.getY());
    return deltaX <= wd2() + node.wd2() && deltaY <= ht2() + node.ht2();
  }

  private double wd2() {
    return getAreaWidth() / 2 + 4;
  }

  private double ht2() {
    return getAreaHeight() / 2 + 4;
  }

  String name() {
    if (isVirtual() || nodeAttrs == null) {
      return String.valueOf(hashCode());
    } else {
      return nodeAttrs.getLabel() != null ? nodeAttrs.getLabel() : "none";
    }
  }

  @Override
  public String toString() {
    return "{name=" + name() + "}";
  }
}
