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
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class EllipseShapeRender extends CustomizeShapeRender {

  @Override
  public String getShapeName() {
    return NodeShapeEnum.ELLIPSE.getName();
  }

  @Override
  public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
    Element shapeEle = nodeBrush.getShapeElement(nodeDrawProp, NodeShapeEnum.ELLIPSE.getName());
    draw(nodeDrawProp, shapeEle);
  }

  @Override
  public void drawClusterSvg(SvgBrush clusterBrush, ClusterDrawProp clusterDrawProp) {
    Element shapeEle = clusterBrush.getShapeElement(clusterDrawProp,
                                                    NodeShapeEnum.ELLIPSE.getName());
    draw(clusterDrawProp, shapeEle);
  }

  protected void draw(Box box, Element shapeEle) {
    shapeEle.setAttribute(SvgConstants.CX, String.valueOf(box.getX()));
    shapeEle.setAttribute(SvgConstants.CY, String.valueOf(box.getY()));
    shapeEle.setAttribute(SvgConstants.RX, String.valueOf(box.getWidth() / 2));
    shapeEle.setAttribute(SvgConstants.RY, String.valueOf(box.getHeight() / 2));
  }
}
