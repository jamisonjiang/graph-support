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
package org.graphper.parser;

import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.NodeStyle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StyleAttributeTest {

  @Test
  public void supportedStylesSurviveUnsupportedValues() throws Exception {
    Graphviz graph = DotParser.parse("digraph { node[style=\"rounded,filled\"];"
        + " a[shape=hexagon,fillcolor=white] }");
    Node node = graph.nodes().iterator().next();
    String svg = graph.toSvgStr();
    Assertions.assertTrue(node.nodeAttrs().getStyles().contains(NodeStyle.ROUNDED));
    Assertions.assertTrue(svg.contains("id=\"a_path\""));
    Assertions.assertFalse(svg.contains("id=\"a_polygon\""));
  }
}
