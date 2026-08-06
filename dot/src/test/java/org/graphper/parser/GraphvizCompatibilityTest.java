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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.graphper.layout.LabelLines;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphvizCompatibilityTest {

  private static final Pattern VIEWBOX = Pattern.compile(
      "viewBox=\"0\\.00 0\\.00 ([0-9.]+) ([0-9.]+)\"");

  @Test
  public void leftAndRightRecordLines() throws Exception {
    String dot = "digraph pipeline {"
        + " margin=\"0,0\";"
        + " task [shape=record label=\"{assayimport|{{input.txt \\l}|{output.tsv \\r}}}\"];"
        + "}";
    String svg = DotParser.parse(dot).toSvgStr();

    Assertions.assertTrue(svg.contains(">input.txt&#160;</text>"));
    Assertions.assertTrue(svg.contains(">output.tsv&#160;</text>"));
    Assertions.assertTrue(svg.contains("text-anchor=\"start\""));
    Assertions.assertTrue(svg.contains("text-anchor=\"end\""));
  }

  @Test
  public void zeroMarginTightensFinalSvgCanvas() throws Exception {
    String node = "task [shape=record label=\"{assayimport|{input.txt|output.tsv}}\"]";
    double[] defaultSize = viewBox(DotParser.parse("digraph {" + node + "}").toSvgStr());
    double[] zeroSize = viewBox(
        DotParser.parse("digraph { margin=\"0,0\";" + node + "}").toSvgStr());

    Assertions.assertTrue(zeroSize[0] < defaultSize[0]);
    Assertions.assertTrue(zeroSize[1] < defaultSize[1]);
    Assertions.assertTrue(defaultSize[0] - zeroSize[0] >= 20);
    Assertions.assertTrue(defaultSize[1] - zeroSize[1] >= 20);
  }

  @Test
  public void zeroMarginKeepsArrowheadsInsideCanvas() throws Exception {
    String svg = DotParser.parse("digraph { margin=\"0,0\"; a -> b }").toSvgStr();
    double[] size = viewBox(svg);
    Matcher polygons = Pattern.compile(
        "class=\"arrowhead\"[^>]+points=\"([^\"]+)\"").matcher(svg);
    Assertions.assertTrue(polygons.find());
    for (String pair : polygons.group(1).trim().split("\\s+")) {
      String[] xy = pair.split(",");
      double x = Double.parseDouble(xy[0]);
      double y = Double.parseDouble(xy[1]);
      Assertions.assertTrue(x >= 0 && x <= size[0], pair);
      Assertions.assertTrue(y >= 0 && y <= size[1], pair);
    }
  }

  @Test
  public void alignmentMarkersDoNotLeakIntoSvgMetadata() throws Exception {
    String dot = "digraph { a[label=\"left\\lright\\r\"]; a -> a }";
    String svg = DotParser.parse(dot).toSvgStr();

    Assertions.assertFalse(svg.indexOf(LabelLines.LEFT_BREAK) >= 0);
    Assertions.assertFalse(svg.indexOf(LabelLines.RIGHT_BREAK) >= 0);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.newDocumentBuilder().parse(
        new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
  }

  private double[] viewBox(String svg) {
    Matcher matcher = VIEWBOX.matcher(svg);
    Assertions.assertTrue(matcher.find());
    return new double[]{Double.parseDouble(matcher.group(1)),
        Double.parseDouble(matcher.group(2))};
  }
}
