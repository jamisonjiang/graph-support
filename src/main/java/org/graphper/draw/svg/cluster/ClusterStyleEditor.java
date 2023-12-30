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

import java.util.Collection;
import org.graphper.api.ClusterAttrs;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;
import org.graphper.util.CollectionUtils;

public class ClusterStyleEditor extends SvgEditor implements ClusterEditor<SvgBrush> {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
    Collection<ClusterStyle> styles = clusterAttrs.getStyles();
    if (CollectionUtils.isEmpty(styles)) {
      return true;
    }

    for (Element clusterEle : brush.getEleGroup(SHAPE_GROUP_KEY)) {
      for (ClusterStyle style : styles) {
        setStyle(style, clusterEle);
      }
    }
    return true;
  }

  private void setStyle(ClusterStyle style, Element cluster) {
    if (style == ClusterStyle.DASHED) {
      dashed(cluster);
    } else if (style == ClusterStyle.DOTTED) {
      dotted(cluster);
    }
  }

  private void dashed(Element cluster) {
    if (cluster.getAttribute(SvgConstants.FILL) == null) {
      cluster.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    }
    cluster.setAttribute(SvgConstants.STROKE_DASHARRAY, "5,2");
  }

  private void dotted(Element cluster) {
    if (cluster.getAttribute(SvgConstants.FILL) == null) {
      cluster.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    }
    cluster.setAttribute(SvgConstants.STROKE_DASHARRAY, "1,5");
  }
}
