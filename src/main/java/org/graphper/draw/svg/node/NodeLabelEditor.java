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

import static org.graphper.draw.svg.SvgEditor.setText;
import static org.graphper.draw.svg.SvgEditor.text;

import java.util.function.Consumer;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.NodeEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor.TextAttribute;
import org.graphper.draw.svg.SvgEditor.TextLineAttribute;

public class NodeLabelEditor implements NodeEditor<SvgBrush> {

  @Override
  public boolean edit(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    NodeAttrs nodeAttrs = nodeDrawProp.nodeAttrs();
    String label = nodeAttrs.getLabel();

    if (StringUtils.isEmpty(label)
        || nodeDrawProp.getCell() != null
        || nodeDrawProp.getLabelCenter() == null
        || nodeAttrs.getShape().ignoreLabel()) {
      return true;
    }

    double fontSize = nodeAttrs.getFontSize() == null ? 0D : nodeAttrs.getFontSize();
    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      String id = SvgConstants.TEXT_ELE + SvgConstants.UNDERSCORE + textLineAttribute.getLineNo();
      Element text = brush.getOrCreateChildElementById(id, SvgConstants.TEXT_ELE);
      setText(text, fontSize, textLineAttribute);
      text.setTextContent(textLineAttribute.getLine());
    };

    text(new TextAttribute(nodeDrawProp.getLabelCenter(), fontSize, label,
                           nodeAttrs.getFontColor(), nodeAttrs.getFontName(), lineConsumer));
    return nodeDrawProp.nodeAttrs().getShape() != NodeShapeEnum.PLAIN_TEXT;
  }
}
