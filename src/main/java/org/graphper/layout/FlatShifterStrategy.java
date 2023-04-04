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

import org.graphper.def.FlatPoint;
import org.graphper.layout.dot.RouterBox;
import org.graphper.draw.ContainerDrawProp;

/**
 * A strategy for two-dimensional translation of elements, where elements move a horizontal and
 * vertical offset on the plane to a specified position.
 *
 * @author Jamison Jiang
 */
public class FlatShifterStrategy extends AbstractShifterStrategy {

  private final double xOffset;

  private final double yOffset;

  public FlatShifterStrategy(double xOffset, double yOffset) {
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  @Override
  public void movePoint(FlatPoint point) {
    if (point == null) {
      return;
    }

    point.setX(point.getX() + xOffset);
    point.setY(point.getY() + yOffset);
  }

  @Override
  public void moveContainerDrawProp(ContainerDrawProp containerDrawProp) {
    if (containerDrawProp == null) {
      return;
    }

    moveGrid(containerDrawProp);
    movePoint(containerDrawProp.getLabelCenter());

    containerDrawProp.setLeftBorder(containerDrawProp.getLeftBorder() + xOffset);
    containerDrawProp.setRightBorder(containerDrawProp.getRightBorder() + xOffset);
    containerDrawProp.setUpBorder(containerDrawProp.getUpBorder() + yOffset);
    containerDrawProp.setDownBorder(containerDrawProp.getDownBorder() + yOffset);
  }

  @Override
  public void moveBox(RouterBox routerBox) {
    if (routerBox == null) {
      return;
    }

    double leftWall = routerBox.getLeftBorder();
    double rightWall = routerBox.getRightBorder();
    double upWall = routerBox.getUpBorder();
    double downWall = routerBox.getDownBorder();

    routerBox.setRightBorder(Double.MAX_VALUE);
    routerBox.setLeftBorder(leftWall + xOffset);
    routerBox.setRightBorder(rightWall + xOffset);
    routerBox.setDownBorder(Double.MAX_VALUE);
    routerBox.setUpBorder(upWall + yOffset);
    routerBox.setDownBorder(downWall + yOffset);
  }
}
