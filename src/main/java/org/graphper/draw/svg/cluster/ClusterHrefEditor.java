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
import org.graphper.draw.svg.Element;
import org.graphper.api.ClusterAttrs;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;

public class ClusterHrefEditor extends SvgEditor implements ClusterEditor<SvgBrush> {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
    if (clusterAttrs.getStyle() == ClusterStyle.INVIS) {
      return false;
    }
    if (clusterAttrs.getHref() == null) {
      return true;
    }

    String href = clusterAttrs.getHref();
    String id = brush.drawBoard().clusterId(cluster.getCluster());
    Element wrapEle = brush.getOrCreateShapeEleById(A_ELE + UNDERSCORE + id, A_ELE);
    brush.setWrapEle(wrapEle);

    wrapEle.setAttribute(XLINK + COLON + HREF, href);
    wrapEle.setAttribute(XLINK + COLON + TITLE_ELE, clusterAttrs.getLabel());
    return true;
  }
}
