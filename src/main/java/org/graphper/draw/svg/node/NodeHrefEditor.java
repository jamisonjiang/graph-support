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

package org.graphper.draw.svg.node;

import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.NodeEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class NodeHrefEditor implements NodeEditor<SvgBrush>, SvgConstants {

  @Override
  public boolean edit(NodeDrawProp node, SvgBrush brush) {
    NodeAttrs nodeAttrs = node.nodeAttrs();
    if (nodeAttrs.getStyles().contains(NodeStyle.INVIS)) {
      return false;
    }
    if (nodeAttrs.getHref() == null) {
      return true;
    }

    String href = nodeAttrs.getHref();
    Element wrapEle = brush.getOrCreateShapeEleById(A_ELE + UNDERSCORE, A_ELE);
    brush.setWrapEle(wrapEle);

    wrapEle.setAttribute(XLINK + COLON + HREF, href);
    String tooltip = StringUtils.isNotEmpty(nodeAttrs.getTooltip())
        ? nodeAttrs.getTooltip() : nodeAttrs.getLabel();

    if (StringUtils.isNotEmpty(tooltip)) {
      wrapEle.setAttribute(XLINK + COLON + TITLE_ELE, tooltip);
    }
    return true;
  }
}
