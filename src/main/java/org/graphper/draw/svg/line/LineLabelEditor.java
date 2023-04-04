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

package org.graphper.draw.svg.line;

import java.util.function.Consumer;
import org.graphper.api.LineAttrs;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.LineEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;
import org.graphper.def.FlatPoint;
import org.apache_gs.commons.lang3.StringUtils;

public class LineLabelEditor extends SvgEditor implements LineEditor<SvgBrush> {

  @Override
  public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
    String label = lineDrawProp.lineAttrs().getLabel();

    if (StringUtils.isEmpty(label) || lineDrawProp.getLabelCenter() == null) {
      return true;
    }

    Double fz = lineDrawProp.lineAttrs().getFontSize();
    double fontSize = fz == null ? 0D : fz;

    FlatPoint labelCenter = lineDrawProp.getLabelCenter();

    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      String id = SvgBrush.getId(
          brush.lineId(lineDrawProp),
          SvgConstants.TEXT_ELE + SvgConstants.UNDERSCORE + textLineAttribute.getLineNo()
      );

      Element text = brush.getOrCreateChildElementById(id, SvgConstants.TEXT_ELE);
      setText(text, fontSize, textLineAttribute);

      text.setTextContent(textLineAttribute.getLine());
    };

    LineAttrs lineAttrs = lineDrawProp.lineAttrs();
    text(new TextAttribute(labelCenter, fontSize, label, lineAttrs.getFontColor(),
                           lineAttrs.getFontName(), lineConsumer));
    return true;
  }
}
