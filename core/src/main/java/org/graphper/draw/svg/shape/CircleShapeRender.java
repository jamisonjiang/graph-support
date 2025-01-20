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

package org.graphper.draw.svg.shape;

import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.Box;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgConstants;

public class CircleShapeRender extends EllipseShapeRender {

  @Override
  public String getShapeName() {
    return NodeShapeEnum.CIRCLE.getName();
  }

  @Override
  protected void draw(Box box, Element shapeEle) {
    double radius = Math.min(box.getHeight() / 2, box.getWidth() / 2);
    shapeEle.setAttribute(SvgConstants.CX, String.valueOf(box.getX()));
    shapeEle.setAttribute(SvgConstants.CY, String.valueOf(box.getY()));
    shapeEle.setAttribute(SvgConstants.RX, String.valueOf(radius));
    shapeEle.setAttribute(SvgConstants.RY, String.valueOf(radius));
  }
}
