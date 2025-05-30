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

import static org.graphper.draw.svg.SvgConstants.FONT_STYLE;
import static org.graphper.draw.svg.SvgConstants.FONT_WEIGHT;
import static org.graphper.draw.svg.SvgConstants.LINE_THROUGH;
import static org.graphper.draw.svg.SvgConstants.TEXT_DECORATION;
import static org.graphper.draw.svg.SvgEditor.setText;
import static org.graphper.draw.svg.SvgEditor.text;

import java.util.Collection;
import java.util.function.Consumer;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.FontStyle;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.NodeEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor.TextAttribute;
import org.graphper.draw.svg.SvgEditor.TextLineAttribute;
import org.graphper.util.CollectionUtils;

public class NodeLabelEditor implements NodeEditor<SvgBrush> {

  @Override
  public boolean edit(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    NodeAttrs nodeAttrs = nodeDrawProp.nodeAttrs();
    String label = nodeDrawProp.isCellProp() ? nodeAttrs.getRealLabel() : nodeAttrs.getLabel();

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
      setFontStyle(nodeAttrs, text);
    };

    text(new TextAttribute(nodeDrawProp.getLabelCenter(), fontSize, label,
                           nodeAttrs.getFontColor(), nodeAttrs.getFontName(), lineConsumer));
    return nodeDrawProp.nodeAttrs().getShape() != NodeShapeEnum.PLAINTEXT;
  }

  private void setFontStyle(NodeAttrs nodeAttrs, Element textEle) {
    Collection<FontStyle> fontStyles = nodeAttrs.getFontStyles();
    if (CollectionUtils.isEmpty(fontStyles)) {
      return;
    }

    for (FontStyle fontStyle : fontStyles) {
      switch (fontStyle) {
        case BOLD:
          textEle.setAttribute(FONT_WEIGHT, FontStyle.BOLD.name().toLowerCase());
          break;
        case ITALIC:
          textEle.setAttribute(FONT_STYLE, FontStyle.ITALIC.name().toLowerCase());
          break;
        case OVERLINE:
          textEle.setAttribute(TEXT_DECORATION, FontStyle.OVERLINE.name().toLowerCase());
          break;
        case UNDERLINE:
          textEle.setAttribute(TEXT_DECORATION, FontStyle.UNDERLINE.name().toLowerCase());
          break;
        case STRIKETHROUGH:
          textEle.setAttribute(TEXT_DECORATION, LINE_THROUGH);
          break;
        default:
          break;
      }
    }
  }
}
