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

package org.graphper.draw;

import java.util.List;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;
import org.graphper.def.FlatPoint;
import org.graphper.util.CollectionUtils;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.RegularPolylinePropCalc;

public class RegularShapeRender extends CustomizeShapeRender {

  @Override
  public void drawSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();
    RegularPolylinePropCalc shapePropCalc =
        (RegularPolylinePropCalc) nodeShape.getShapePropCalc();
    List<FlatPoint> points = shapePropCalc.calcPoints(nodeDrawProp);

    if (CollectionUtils.isEmpty(points)) {
      return;
    }

    Element shapeElement = nodeBrush.getShapeElement(nodeDrawProp, SvgConstants.POLYGON_ELE);
    String pointsStr = SvgEditor.generatePolylinePoints(points);
    shapeElement.setAttribute(SvgConstants.POINTS, pointsStr);
  }

  @Override
  public String getShapeName() {
    return NodeShapeEnum.REGULAR_POLYLINE.getName();
  }
}
