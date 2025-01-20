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

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgDocument;
import org.graphper.draw.svg.SvgElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SvgDocumentTest {

  @Test
  public void testEmptyDocument() {
    SvgDocument svgDocument = new SvgDocument();
    Assertions.assertNull(svgDocument.toXml());
  }

  @Test
  public void testSvgAttr() {
    SvgDocument svgDocument = new SvgDocument();
    SvgElement svg = svgDocument.createElement("svg");
    svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
    svg.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
    svg.setAttribute("width", "200");

    Assertions.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"200\"></svg>",
        svgDocument.toXml()
    );

    svg.setAttribute("width", "400");
    Assertions.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"400\"></svg>",
        svgDocument.toXml()
    );
  }

  @Test
  public void testSvgChild() {
    SvgDocument svgDocument = new SvgDocument();
    SvgElement svg = svgDocument.createElement("svg");
    Element g = svg.createChildElement("g");
    g.setAttribute("style", "transform");
    Assertions.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg><g style=\"transform\"></g></svg>",
        svgDocument.toXml());

    Element title = g.createChildElement("title");
    title.setAttribute("class", "title");
    title.setTextContent("G");
    Assertions.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg><g style=\"transform\"><title class=\"title\">G</title></g></svg>",
        svgDocument.toXml()
    );
  }

  @Test
  public void testSetId() {
    SvgDocument svgDocument = new SvgDocument();
    SvgElement svg = svgDocument.createElement("svg");
    Element g = svg.createChildElement("g");

    Element title = g.createChildElement("title");
    title.setAttribute("class", "title");
    title.setTextContent("G");

    Element polygon = g.createChildElement("start", "polygon");
    Assertions.assertEquals(polygon, svgDocument.getElementById("start"));
    Assertions.assertNull(svgDocument.getElementById("graphviz"));

    g.setId("graphviz");
    Assertions.assertEquals(g, svgDocument.getElementById("graphviz"));

    g.setAttribute(SvgConstants.ID, "newId");
    Assertions.assertNull(svgDocument.getElementById("graphviz"));
    Assertions.assertEquals(g, svgDocument.getElementById("newId"));

    Assertions.assertTrue(svgDocument.toXml().contains("polygon"));

    svgDocument.removeEle("start");
    Assertions.assertFalse(svgDocument.toXml().contains("polygon"));
  }

  @Test
  public void testSerial() throws IOException, ClassNotFoundException {
    SvgDocument svgDocument = new SvgDocument();
    SvgElement svg = svgDocument.createElement("svg");
    Element g = svg.createChildElement("g");

    Element title = g.createChildElement("title");
    title.setAttribute("class", "title");
    title.setTextContent("G");
    title.setId("t");

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + "Document", svgDocument,
        doc -> {
          Assertions.assertEquals(doc, svgDocument);
          Element t = doc.getElementById("t");
          Assertions.assertNotNull(t);
          Assertions.assertEquals("G", t.textContext());
        }
    );
  }
}
