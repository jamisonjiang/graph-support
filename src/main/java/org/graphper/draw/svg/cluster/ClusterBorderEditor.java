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

import org.graphper.api.attributes.ClusterStyle;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;

public class ClusterBorderEditor extends SvgEditor implements ClusterEditor<SvgBrush> {

  private static final int MAX_CLUSTER_ROUNDED = 60;

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    cluster.check();

    Element clusterEle;
    ClusterStyle style = cluster.getCluster().clusterAttrs().getStyle();
    String points;
    if (style == ClusterStyle.ROUNDED) {
      clusterEle = brush.getOrCreateChildElementById(SvgBrush.getId(cluster.id(), PATH_ELE),
                                                     PATH_ELE);
      points = roundedBox(MAX_CLUSTER_ROUNDED, cluster);
      clusterEle.setAttribute(D, points);
    } else {
      clusterEle = brush.getOrCreateChildElementById(SvgBrush.getId(cluster.id(), POLYGON_ELE),
                                                     POLYGON_ELE);
      points = generateBox(cluster);
      clusterEle.setAttribute(POINTS, points);
    }

    brush.addGroup(SHAPE_GROUP_KEY, clusterEle);
    double penWidth = cluster.getCluster().clusterAttrs().getPenWidth();
    clusterEle.setAttribute(STROKE_WIDTH, String.valueOf(penWidth));
    return true;
  }
}
