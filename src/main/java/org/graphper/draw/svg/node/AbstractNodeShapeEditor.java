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

import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.NodeEditor;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;

public abstract class AbstractNodeShapeEditor implements NodeEditor<SvgBrush>, SvgConstants {

  protected String getShapeElement(NodeDrawProp nodeDrawProp) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getShape();

    if (nodeShape == NodeShapeEnum.CIRCLE
        || nodeShape == NodeShapeEnum.ELLIPSE
        || nodeShape == NodeShapeEnum.POINT) {
      return NodeShapeEnum.ELLIPSE.getName();
    }

    return SvgConstants.POLYGON_ELE;
  }
}
