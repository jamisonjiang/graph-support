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

package org.graphper.draw.svg.cluster;

import org.graphper.api.ClusterAttrs;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;

public class ClusterShapeEditor extends SvgEditor implements ClusterEditor<SvgBrush> {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    cluster.check();

    ClusterShape shape = cluster.getCluster().clusterAttrs().getShape();
    CustomizeShapeRender customizeShapeRender = CustomizeShapeRender
        .getCustomizeShapeRender(shape.getName());
    if (customizeShapeRender != null) {
      customizeShapeRender.drawClusterSvg(brush, cluster);
    }
    ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
    double penWidth = clusterAttrs.getPenWidth();
    penWidth = SvgEditor.strokeWidth(penWidth,
                                     clusterAttrs.getStyles().contains(ClusterStyle.BOLD));
    for (Element clusterEle : brush.getEleGroup(SHAPE_GROUP_KEY)) {
      clusterEle.setAttribute(STROKE_WIDTH, String.valueOf(penWidth));
    }

    return true;
  }
}
