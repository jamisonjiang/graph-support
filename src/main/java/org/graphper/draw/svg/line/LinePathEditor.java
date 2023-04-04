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

import org.graphper.api.attributes.Color;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.LineEditor;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.util.CollectionUtils;

public class LinePathEditor extends SvgEditor implements LineEditor<SvgBrush> {

	private static final Logger log = LoggerFactory.getLogger(LinePathEditor.class);

	@Override
	public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
		if (CollectionUtils.isEmpty(lineDrawProp)) {
			if (log.isWarnEnabled()) {
				log.warn("Find the wrong LineDrawProp attribute, "
						         + "terminate the drawing of the svg path, line={}", lineDrawProp.lineAttrs());
			}
			return true;
		}

		Element pathElement = brush.getOrCreateChildElementById(
				SvgBrush.getId(brush.lineId(lineDrawProp), SvgConstants.PATH_ELE),
				SvgConstants.PATH_ELE
		);

		Color color = lineDrawProp.lineAttrs().getColor();
		pathElement.setAttribute(SvgConstants.D, pointsToSvgLine(lineDrawProp.getStart(), lineDrawProp,
		                                                         lineDrawProp.isBesselCurve()));
		pathElement.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
		pathElement.setAttribute(SvgConstants.STROKE, color.value());

		Element title = brush.getOrCreateChildElementById(
				SvgBrush.getId(brush.lineId(lineDrawProp), SvgConstants.TITLE_ELE),
				SvgConstants.TITLE_ELE
		);

		String text;
		if (brush.drawBoard().drawGraph().getGraphviz().isDirected()) {
			text = lineDrawProp.getLine().tail().nodeAttrs().getLabel()
					+ "->"
					+ lineDrawProp.getLine().head().nodeAttrs().getLabel();
		} else {
			text = lineDrawProp.getLine().tail().nodeAttrs().getLabel()
					+ "--"
					+ lineDrawProp.getLine().head().nodeAttrs().getLabel();
		}

		title.setTextContent(text);

		Double penWidth = lineDrawProp.lineAttrs().getPenWidth();
		if (penWidth != null) {
			pathElement.setAttribute(STROKE_WIDTH, String.valueOf(penWidth));
		}

		return true;
	}
}