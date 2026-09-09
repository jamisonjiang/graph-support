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

import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.NodeShapeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegularAttributeTest {

  @Test
  public void parsesUnsetFalseTrueAndInvalidSeparately() {
    Graphviz graph = DotParser.parse("digraph { unset[shape=hexagon];"
        + " no[shape=hexagon,regular=false]; yes[shape=hexagon,regular=true];"
        + " invalid[shape=hexagon,regular=true1]; }");
    Map<String, Node> nodes = nodes(graph);
    Assertions.assertNull(nodes.get("unset").nodeAttrs().getRegular());
    Assertions.assertEquals(Boolean.FALSE, nodes.get("no").nodeAttrs().getRegular());
    Assertions.assertEquals(Boolean.TRUE, nodes.get("yes").nodeAttrs().getRegular());
    Assertions.assertNull(nodes.get("invalid").nodeAttrs().getRegular());
  }

  @Test
  public void parsesMrecordAlias() {
    Node node = DotParser.parse("digraph { a[shape=Mrecord,label=\"{a|b}\"] }")
        .nodes().iterator().next();
    Assertions.assertEquals(NodeShapeEnum.M_RECORD, node.nodeAttrs().getShape());
  }

  @Test
  public void nodeDefaultCanBeOverridden() {
    Graphviz graph = DotParser.parse("digraph { node[shape=hexagon,regular=true];"
        + " inherited; overridden[regular=false]; }");
    Map<String, Node> nodes = nodes(graph);
    Assertions.assertEquals(Boolean.TRUE, nodes.get("inherited").nodeAttrs().getRegular());
    Assertions.assertEquals(Boolean.FALSE, nodes.get("overridden").nodeAttrs().getRegular());
  }

  private Map<String, Node> nodes(Graphviz graph) {
    Map<String, Node> nodes = new HashMap<>();
    graph.nodes().forEach(node -> nodes.put(node.nodeAttrs().getId(), node));
    return nodes;
  }
}
