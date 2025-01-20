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

import static org.graphper.draw.svg.SvgEditor.pointsToSvgLine;

import java.util.Arrays;
import java.util.List;
import org.graphper.api.attributes.Color;
import org.graphper.def.FlatPoint;
import org.graphper.draw.GraphEditor;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.layout.OrthoVisGraph.Segment;
import org.graphper.util.CollectionUtils;

public class GraphGridEditor implements GraphEditor<SvgBrush>, SvgConstants {

  private static final String GRID_SEGMENT = "grid_segment";

  @Override
  public boolean edit(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
    List<Segment> grid = graphvizDrawProp.getGrid();
    if (CollectionUtils.isEmpty(grid)) {
      return true;
    }

    for (int i = 0; i < grid.size(); i++) {
      Segment segment = grid.get(i);
      List<FlatPoint> points = Arrays.asList(segment.getStart(), segment.getEnd());
      String path = pointsToSvgLine(null, points, false);

      Element pathElement = brush.getOrCreateChildElementById(GRID_SEGMENT + UNDERSCORE + i,
                                                              PATH_ELE);

      pathElement.setAttribute(D, path);
      pathElement.setAttribute(FILL, NONE);
      pathElement.setAttribute(STROKE, Color.BLACK.value());
      pathElement.setAttribute(STROKE_DASHARRAY, "1,5");
    }
    return true;
  }
}
