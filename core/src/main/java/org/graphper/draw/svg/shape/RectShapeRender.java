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
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;

public class RectShapeRender extends CustomizeShapeRender {

  @Override
  public String getShapeName() {
    return NodeShapeEnum.RECT.getName();
  }

  @Override
  public void drawNodeSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp) {
    draw(nodeBrush, nodeDrawProp);
  }

  @Override
  public void drawClusterSvg(SvgBrush clusterBrush, ClusterDrawProp clusterDrawProp) {
    draw(clusterBrush, clusterDrawProp);
  }

  private Element draw(SvgBrush brush, ContainerDrawProp box) {
    return SvgEditor.polygonShape(box, brush, box.getLeftBorder(), box.getUpBorder(),
                                  box.getRightBorder(), box.getUpBorder(),
                                  box.getRightBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getUpBorder());

  }
}
