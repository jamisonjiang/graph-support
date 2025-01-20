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
import org.graphper.api.attributes.Color;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class ClusterColorEditor implements ClusterEditor<SvgBrush>, SvgConstants {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    cluster.check();

    ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
    for (Element clusterEle : brush.getEleGroup(SHAPE_GROUP_KEY)) {
      Color bgColor = clusterAttrs.getBgColor();
      if (bgColor != null) {
        clusterEle.setAttribute(SvgConstants.FILL, bgColor.value());
      } else {
        clusterEle.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
      }

      Color color = clusterAttrs.getColor();
      if (color != null) {
        clusterEle.setAttribute(STROKE, color.value());
      } else {
        clusterEle.setAttribute(SvgConstants.STROKE, Color.BLACK.value());
      }
    }
    return true;
  }
}
