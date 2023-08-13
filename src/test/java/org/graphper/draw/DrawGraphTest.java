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

package org.graphper.draw;

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import org.graphper.api.GraphResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.draw.svg.SvgRenderEngine;

public class DrawGraphTest {

  @Test
  public void testSerial() throws IOException, ClassNotFoundException, ExecuteException {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Graphviz graphviz = Graphviz.digraph().addLine(a, b).build();
    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graphviz);
    GraphResource graphResource = SvgRenderEngine.getInstance().render0(drawGraph);
    Assertions.assertNotNull(graphResource);

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + "drawGraph", drawGraph,
        g -> {
          try {
            GraphResource gr = SvgRenderEngine.getInstance().render0(g);
            Assertions.assertNotNull(gr);
          } catch (ExecuteException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
