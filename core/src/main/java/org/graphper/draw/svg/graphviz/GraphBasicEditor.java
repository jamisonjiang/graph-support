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

package org.graphper.draw.svg.graphviz;

import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.GraphAttrs;
import org.graphper.api.attributes.Color;
import org.graphper.draw.GraphEditor;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class GraphBasicEditor implements GraphEditor<SvgBrush>, SvgConstants {

  @Override
  public boolean edit(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
    GraphAttrs graphAttrs = graphvizDrawProp.getGraphviz().graphAttrs();

    setHref(graphvizDrawProp, brush);

    Element background = brush.getOrCreateChildElementById(POLYGON_ELE, POLYGON_ELE);

    double leftBorder = graphvizDrawProp.getLeftBorder();
    double rightBorder = graphvizDrawProp.getRightBorder();
    double topBorder = graphvizDrawProp.getUpBorder();
    double bottomBorder = graphvizDrawProp.getDownBorder();
    String points = leftBorder + COMMA + topBorder + SPACE
        + rightBorder + COMMA + topBorder + SPACE
        + rightBorder + COMMA + bottomBorder + SPACE
        + leftBorder + COMMA + bottomBorder + SPACE
        + leftBorder + COMMA + topBorder;

    // Set back group color
    setBgColor(graphAttrs, background);
    background.setAttribute(POINTS, points);
    return true;
  }

  private void setHref(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
    GraphAttrs graphAttrs = graphvizDrawProp.getGraphviz().graphAttrs();
    if (graphAttrs.getHref() == null) {
      return;
    }

    String href = graphAttrs.getHref();
    String id = SvgConstants.GRAPH + SvgConstants.UNDERSCORE + "0";
    String tooltip = StringUtils.isNotEmpty(graphAttrs.getTooltip())
        ? graphAttrs.getTooltip() : graphAttrs.getLabel();

    Element wrapEle = brush.getOrCreateChildElementById(A_ELE + UNDERSCORE + id, A_ELE);
    brush.setWrapEle(wrapEle);
    wrapEle.setAttribute(XLINK + COLON + HREF, href);
    if (StringUtils.isNotEmpty(tooltip)) {
      wrapEle.setAttribute(XLINK + COLON + TITLE_ELE, tooltip);
    }
  }

  private void setBgColor(GraphAttrs graphAttrs, Element background) {
    Color bgColor = graphAttrs.getBgColor();
    if (bgColor == null) {
      setDefaultBgColor(background);
    } else {
      background.setAttribute(FILL, bgColor.value());
    }
  }

  private void setDefaultBgColor(Element background) {
    background.setAttribute(FILL, Color.WHITE.value());
  }
}
