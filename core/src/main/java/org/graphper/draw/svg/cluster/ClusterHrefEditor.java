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

import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.ClusterAttrs;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class ClusterHrefEditor implements ClusterEditor<SvgBrush>, SvgConstants {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
    if (clusterAttrs.getStyles().contains(ClusterStyle.INVIS)) {
      return false;
    }
    if (clusterAttrs.getHref() == null) {
      return true;
    }

    String href = clusterAttrs.getHref();
    String id = brush.drawBoard().clusterId(cluster.getCluster());
    String tooltip = StringUtils.isNotEmpty(clusterAttrs.getTooltip())
        ? clusterAttrs.getTooltip() : clusterAttrs.getLabel();

    Element wrapEle = brush.getOrCreateChildElementById(A_ELE + UNDERSCORE + id, A_ELE);
    brush.setWrapEle(wrapEle);

    wrapEle.setAttribute(XLINK + COLON + HREF, href);
    if (StringUtils.isNotEmpty(tooltip)) {
      wrapEle.setAttribute(XLINK + COLON + TITLE_ELE, tooltip);
    }
    return true;
  }
}
