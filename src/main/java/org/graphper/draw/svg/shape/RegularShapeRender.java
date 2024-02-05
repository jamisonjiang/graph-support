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

import java.util.List;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.RegularPolylinePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;
import org.graphper.util.CollectionUtils;

public class RegularShapeRender extends CustomizeShapeRender {

  @Override
  public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();
    RegularPolylinePropCalc shapePropCalc =
        (RegularPolylinePropCalc) nodeShape.getShapePropCalc();
    draw(nodeDrawProp, nodeBrush, shapePropCalc);
  }

  @Override
  public void drawClusterSvg(SvgBrush clusterBrush, ClusterDrawProp clusterDrawProp) {
    ClusterShape clusterShape = clusterDrawProp.shapeProp();
    RegularPolylinePropCalc shapePropCalc =
        (RegularPolylinePropCalc) clusterShape.getShapePropCalc();
    draw(clusterDrawProp, clusterBrush, shapePropCalc);
  }

  private void draw(ContainerDrawProp box, SvgBrush svgBrush, RegularPolylinePropCalc shapePropCalc) {
    List<FlatPoint> points = shapePropCalc.calcPoints(box);
    if (CollectionUtils.isEmpty(points)) {
      return;
    }

    double[] path = new double[points.size() * 2 + 2];
    for (int i = 0; i < points.size(); i++) {
      path[i * 2] = points.get(i).getX();
      path[i * 2 + 1] = points.get(i).getY();
    }
    path[path.length - 2] = path[0];
    path[path.length - 1] = path[1];
    SvgEditor.polygonShape(box,svgBrush, path);
  }

  @Override
  public String getShapeName() {
    return NodeShapeEnum.REGULAR_POLYLINE.getName();
  }

  public static class PentagonShapeRender extends RegularShapeRender {

    @Override
    public String getShapeName() {
      return NodeShapeEnum.PENTAGON.getName();
    }
  }

  public static class HexagonShapeRender extends RegularShapeRender {

    @Override
    public String getShapeName() {
      return NodeShapeEnum.HEXAGON.getName();
    }
  }

  public static class SeptagonShapeRender extends RegularShapeRender {

    @Override
    public String getShapeName() {
      return NodeShapeEnum.SEPTAGON.getName();
    }
  }

  public static class OctagonShapeRender extends RegularShapeRender {

    @Override
    public String getShapeName() {
      return NodeShapeEnum.OCTAGON.getName();
    }
  }
}
