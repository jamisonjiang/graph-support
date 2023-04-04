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

import org.graphper.draw.svg.Element;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class NodeStyleEditor extends AbstractNodeShapeEditor {

  @Override
  public boolean edit(NodeDrawProp node, SvgBrush brush) {
    for (Element element : brush.getEleGroup(SHAPE_GROUP_KEY)) {
      setStyle(node, brush, element);
    }
    return true;
  }

  private void setStyle(NodeDrawProp node, SvgBrush brush, Element element) {
    NodeAttrs nodeAttrs = node.nodeAttrs();

    Double penWidth = nodeAttrs.getPenWidth();
    if (penWidth != null) {
      element.setAttribute(STROKE_WIDTH, String.valueOf(penWidth));
    }

    NodeStyle style = nodeAttrs.getStyle();
    element.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    if (style == null) {
      element.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
      pointAddFillStyle(node, brush, nodeAttrs, element);
      return;
    }

    drawStyle(brush, node, element, style);
    pointAddFillStyle(node, brush, nodeAttrs, element);
  }

  private void pointAddFillStyle(NodeDrawProp node, SvgBrush brush, NodeAttrs nodeAttrs,
                                 Element shapeElement) {
    if (nodeAttrs.getNodeShape() == NodeShapeEnum.POINT) {
      drawStyle(brush, node, shapeElement, NodeStyle.SOLID);
    }
  }

  private void drawStyle(SvgBrush brush, NodeDrawProp node, Element shape, NodeStyle nodeStyle) {
    if (nodeStyle == NodeStyle.DASHED) {
      dashed(shape);
    } else if (nodeStyle == NodeStyle.DOTTED) {
      dotted(shape);
    } else if (nodeStyle == NodeStyle.INVIS) {
      invis(node.getNode(), brush);
    } else if (nodeStyle == NodeStyle.BOLD) {
      bold(shape);
    }
  }

  private void dashed(Element shape) {
    shape.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    shape.setAttribute(SvgConstants.STROKE_DASHARRAY, "5,2");
  }

  private void dotted(Element shape) {
    shape.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    shape.setAttribute(SvgConstants.STROKE_DASHARRAY, "1,5");
  }

  private void invis(Node node, SvgBrush brush) {
    brush.drawBoard().removeNode(node);
  }

  private void bold(Element shape) {
    shape.setAttribute(SvgConstants.STROKE_WIDTH, "2");
  }
}