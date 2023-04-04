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

import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.LineStyle;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.LineEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgEditor;

public class LineHrefEditor extends SvgEditor implements LineEditor<SvgBrush> {

  @Override
  public boolean edit(LineDrawProp line, SvgBrush brush) {
    LineAttrs lineAttrs = line.lineAttrs();
    if (lineAttrs.getStyle() == LineStyle.INVIS) {
      return false;
    }
    if (lineAttrs.getHref() == null) {
      return true;
    }

    String href = lineAttrs.getHref();
    String id = brush.lineId(line);
    Element wrapEle = brush.getOrCreateShapeEleById(A_ELE + UNDERSCORE + id, A_ELE);
    brush.setWrapEle(wrapEle);

    wrapEle.setAttribute(XLINK + COLON + HREF, href);
    wrapEle.setAttribute(XLINK + COLON + TITLE_ELE, lineAttrs.getLabel());
    return true;
  }
}
