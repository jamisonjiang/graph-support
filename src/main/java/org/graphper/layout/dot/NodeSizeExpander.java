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

import org.graphper.def.FlatPoint;
import org.graphper.draw.LineDrawProp;

public class NodeSizeExpander {

  protected DNode node;

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

  public static double minSelfInterval(DNode node) {
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
}
