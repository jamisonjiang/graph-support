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

package org.graphper.layout;

import java.util.List;
import org.graphper.util.CollectionUtils;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.layout.OrthoVisGraph.Segment;

public abstract class AbstractShifterStrategy implements ShifterStrategy {

	protected void moveGrid(ContainerDrawProp containerDrawProp) {
		if (!(containerDrawProp instanceof GraphvizDrawProp)) {
			return;
		}

		List<Segment> grid = ((GraphvizDrawProp) containerDrawProp).getGrid();
		if (CollectionUtils.isEmpty(grid)) {
			return;
		}

		for (Segment segment : grid) {
			movePoint(segment.getStart());
			movePoint(segment.getEnd());
		}
	}
}
