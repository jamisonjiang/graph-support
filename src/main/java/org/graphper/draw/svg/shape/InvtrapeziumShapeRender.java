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
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;

public class InvtrapeziumShapeRender extends CustomizeShapeRender {

  @Override
  public String getShapeName() {
    return NodeShapeEnum.INVTRAPEZIUM.getName();
  }

  @Override
  public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
    draw(nodeDrawProp, nodeBrush);
  }

  @Override
  public void drawClusterSvg(SvgBrush clusterBrush, ClusterDrawProp clusterDrawProp) {
    draw(clusterDrawProp, clusterBrush);
  }

  private void draw(ContainerDrawProp box, SvgBrush brush) {
    double leftBottomX = box.getLeftBorder() + box.getWidth() / 4;
    double rightBottomX = box.getRightBorder() - box.getWidth() / 4;
    SvgEditor.polygonShape(box, brush, box.getLeftBorder(), box.getUpBorder(),
                           leftBottomX, box.getDownBorder(),
                           rightBottomX, box.getDownBorder(),
                           box.getRightBorder(), box.getUpBorder(),
                           box.getLeftBorder(), box.getUpBorder());
  }
}
