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

package regression;

import helper.GraphvizVisual;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Port;
import org.graphper.api.attributes.Rank;
import org.graphper.api.attributes.Splines;
import org.junit.jupiter.api.Test;

public class LineTest extends GraphvizVisual {

	@Test
	public void test1()  {
		Node a = Node.builder().label("a").build();
		Node b = Node.builder().label("b").build();
		Node c = Node.builder().label("c").build();
		Node d = Node.builder().label("d").build();
		Node e = Node.builder().label("e").build();
		Node f = Node.builder().label("f").build();
		Node g = Node.builder().label("g").build();

		Graphviz graphviz = Graphviz.digraph()
				.splines(Splines.LINE)
				.startSub()
				.rank(Rank.SAME)
				.addLine(Line.builder(a, b).tailPort(Port.NORTH_WEST).build())
				.addLine(a, b)
				.addLine(a, b)
				.addLine(a, b)
				.addLine(Line.builder(a, b).tailPort(Port.SOUTH).headPort(Port.SOUTH).build())
				.addLine(Line.builder(a, b).tailPort(Port.SOUTH).headPort(Port.SOUTH).build())
				.addLine(Line.builder(a, b).tailPort(Port.SOUTH).headPort(Port.SOUTH).label("a -> b").build())

				.addLine(a, g)
				.addLine(Line.builder(a, g).label("a -> g").fontName("Elephant").build())
				.endSub()

				.addLine(a, c)
				.addLine(Line.builder(c, d).tailPort(Port.WEST).headPort(Port.SOUTH).build())
				.addLine(a, d)
				.addLine(a, d)
				.addLine(Line.builder(a, d).headPort(Port.EAST).build())
				.addLine(Line.builder(a, d).headPort(Port.EAST).build())
				.addLine(Line.builder(a, d).headPort(Port.EAST).build())

				.addLine(d, e)
				.addLine(e, f)
				.addLine(d, f)
				.addLine(d, f)
				.addLine(Line.builder(d, f).label("d -> f").build())
				.build();

		visual(graphviz);
	}
}
