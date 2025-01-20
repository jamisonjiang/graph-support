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

package visual_case;

import helper.GraphvizVisual;
import org.junit.jupiter.api.Test;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;

public class LabelTest extends GraphvizVisual {

  @Test
  public void testHtmlEncoder() {
    Node a = Node.builder().label("הרצליה").build();
    Node b = Node.builder().label("חוף בת ים").build();
    Node c = Node.builder().label("<f0> 0x10ba8| <f1>").build();
    Node d = Node.builder().label("<f0> 0xf7fc4380| <f1> | <f2> |-1").build();
    Graphviz graphviz = Graphviz.digraph()
        .label("⊄♠Œ")
        .addLine(Line.builder(a, b).label("\uD83C\uDF34").build())
        .addLine(Line.builder(c, d).label("\u0152Š").build())
        .build();
    visual(graphviz);
  }
}
