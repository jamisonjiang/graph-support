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

import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.Color;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public class NodeColorEditor extends AbstractNodeShapeEditor {

  @Override
  public boolean edit(NodeDrawProp node, SvgBrush brush) {
    NodeAttrs nodeAttrs = node.nodeAttrs();

    for (Element element : brush.getEleGroup(SHAPE_GROUP_KEY)) {
      Color color = nodeAttrs.getColor();
      element.setAttribute(SvgConstants.STROKE, color.value());

      Color fillColor = nodeAttrs.getFillColor();
      if (fillColor == null) {
        fillColor = nodeAttrs.getNodeShape().defaultFillColor();
      }
      if (fillColor == null) {
        continue;
      }
      element.setAttribute(FILL, fillColor.value());
    }

    return true;
  }
}
