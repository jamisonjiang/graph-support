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

import java.io.Serializable;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.api.ext.DefaultBox;

public class RouterBox extends DefaultBox implements Serializable {

  private static final long serialVersionUID = 578785652705131390L;

  private final DNode node;

  public RouterBox(double leftBorder, double rightBorder, double upBorder, double downBorder) {
    this(leftBorder, rightBorder, upBorder, downBorder, null);
  }

  public RouterBox(double leftBorder, double rightBorder, double upBorder, double downBorder, DNode node) {
    super(leftBorder, rightBorder, upBorder, downBorder);
    this.node = node;
  }

  @Override
  public void setLeftBorder(double leftBorder) {
    Asserts.illegalArgument(leftBorder > rightBorder, HORIZONTAL_ERROR);
    super.setLeftBorder(leftBorder);
  }

  @Override
  public void setRightBorder(double rightBorder) {
    Asserts.illegalArgument(leftBorder > rightBorder, HORIZONTAL_ERROR);
    super.setRightBorder(rightBorder);
  }

  @Override
  public void setUpBorder(double upBorder) {
    Asserts.illegalArgument(upBorder > downBorder, VERTICAL_ERROR);
    super.setUpBorder(upBorder);
  }

  @Override
  public void setDownBorder(double downBorder) {
    Asserts.illegalArgument(upBorder > downBorder, VERTICAL_ERROR);
    super.setDownBorder(downBorder);
  }

  boolean inXRange(double x) {
    return inRange(leftBorder, rightBorder, x);
  }

  boolean inYRange(double y) {
    return inRange(upBorder, downBorder, y);
  }

  @Override
  public boolean in(FlatPoint point) {
    if (point == null) {
      return false;
    }

    return inXRange(point.getX()) && inYRange(point.getY());
  }

  double closerVerWall(double x) {
    if (x < leftBorder) {
      return leftBorder;
    }
    if (x > rightBorder) {
      return rightBorder;
    }
    if (x - leftBorder <= rightBorder - x) {
      return leftBorder;
    }
    return rightBorder;
  }

  double closerHorWall(double y) {
    if (y < upBorder) {
      return upBorder;
    }
    if (y > downBorder) {
      return downBorder;
    }
    if (y - upBorder <= downBorder - y) {
      return upBorder;
    }
    return downBorder;
  }

  public static boolean inRange(double left, double right, double num) {
    return num <= right && num >= left;
  }

  public void minGuarantee(double minWidth, double minHeight) {
    if (getWidth() < minWidth) {
      leftBorder = rightBorder - minWidth;
    }
    if (getHeight() < minHeight) {
      upBorder = downBorder - minHeight;
    }
  }

  public void reducePublicArea(RouterBox target, boolean isHor) {
    if (isHor) {
      if (inXRange(target.getLeftBorder())) {
        rightBorder = target.getLeftBorder() - 1;
        leftBorder = Math.min(leftBorder, rightBorder);
      }
      if (inXRange(target.getRightBorder())) {
        leftBorder = target.getRightBorder() + 1;
        rightBorder = Math.max(leftBorder, rightBorder);
      }
    } else {
      if (inYRange(target.getUpBorder())) {
        downBorder = target.getUpBorder() - 1;
        upBorder = Math.min(upBorder, downBorder);
      }
      if (inYRange(target.getDownBorder())) {
        upBorder = target.getDownBorder() + 1;
        downBorder = Math.max(upBorder, downBorder);
      }
    }
  }

  DNode getNode() {
    return node;
  }
}
