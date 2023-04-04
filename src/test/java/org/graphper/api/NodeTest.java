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

package org.graphper.api;

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import org.graphper.api.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;

public class NodeTest {

  @Test
  public void testSerial() throws IOException, ClassNotFoundException {
    Node node = Node.builder()
        .id("id")
        .height(1)
        .width(2)
        .shape(NodeShapeEnum.REGULAR_POLYLINE)
        .color(Color.RED)
        .fillColor(Color.ofRGB("#902924"))
        .fontColor(Color.GOLD)
        .fontName("fffff")
        .label("n")
        .labelloc(Labelloc.BOTTOM)
        .margin(1, 2)
        .fixedSize(true)
        .fontSize(22)
        .style(NodeStyle.BOLD)
        .sides(5)
        .href("xxxx")
        .image("fffff")
        .imageSize(1, 2)
        .build();

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + "node", node,
        n -> {
          Assertions.assertEquals(node.nodeAttrs(), n.nodeAttrs());
          Assertions.assertEquals(NodeShapeEnum.REGULAR_POLYLINE, n.nodeAttrs().getNodeShape());
          Assertions.assertEquals("#902924", n.nodeAttrs().getFillColor().value());
        });
  }
}
