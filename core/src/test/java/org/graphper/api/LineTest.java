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
import org.graphper.api.FloatLabel;
import org.graphper.api.Line;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.Port;

public class LineTest {

  @Test
  public void testSerial() throws IOException, ClassNotFoundException {
    Line line = Line.tempLine()
        .id("id")
        .controlPoints(true)
        .showboxes(true)
        .radian(9)
        .label("fff")
        .color(Color.RED)
        .weight(4)
        .fontColor(Color.ofRGB("#444444"))
        .fontSize(98)
        .fontName("555")
        .headclip(false)
        .tailclip(false)
        .minlen(3)
        .style(LineStyle.DOTTED)
        .arrowHead(ArrowShape.CURVE)
        .arrowTail(ArrowShape.BOX)
        .arrowSize(3)
        .dir(Dir.BOTH)
        .lhead("fff")
        .ltail("333")
        .floatLabels(FloatLabel.builder().label("1").build(),
                     FloatLabel.builder().label("2").build())
        .tailPort(Port.EAST)
        .headPort(Port.NORTH)
        .href("ffff")
        .penWidth(2)
        .build();

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + "line", line,
        l -> {
          Assertions.assertEquals(line.lineAttrs(), l.lineAttrs());
          Assertions.assertEquals(2, l.lineAttrs().getFloatLabels().length);
        });
  }
}
