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

package org.graphper.draw.svg;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class SvgSemanticOutputTest {

  @Test
  public void transparentBackgroundAndSemanticClasses() throws Exception {
    Node a = Node.builder().label("A").build();
    Node b = Node.builder().label("B").build();
    String svg = Graphviz.digraph()
        .bgColor(Color.TRANSPARENT)
        .addLine(a, b)
        .build()
        .toSvgStr();

    Assertions.assertTrue(svg.contains("class=\"edge\""));
    Assertions.assertTrue(svg.contains("class=\"path\""));
    Assertions.assertTrue(svg.contains("class=\"arrowhead\""));
    Assertions.assertTrue(svg.contains("class=\"node\""));
    Assertions.assertTrue(svg.contains("class=\"shape\""));
    Assertions.assertTrue(svg.contains("class=\"label\""));
    Assertions.assertTrue(svg.contains("\n  <g"));
    Assertions.assertTrue(svg.contains("\n    <g"));

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    Document document = factory.newDocumentBuilder()
        .parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
    NodeList polygons = document.getElementsByTagName("polygon");
    boolean transparentBackground = false;
    for (int i = 0; i < polygons.getLength(); i++) {
      org.w3c.dom.Node polygon = polygons.item(i);
      if ("background".equals(attribute(polygon, "class"))) {
        transparentBackground = "none".equals(attribute(polygon, "fill"));
        break;
      }
    }
    Assertions.assertTrue(transparentBackground);
  }

  private String attribute(org.w3c.dom.Node node, String name) {
    org.w3c.dom.Node attribute = node.getAttributes().getNamedItem(name);
    return attribute == null ? null : attribute.getNodeValue();
  }
}
