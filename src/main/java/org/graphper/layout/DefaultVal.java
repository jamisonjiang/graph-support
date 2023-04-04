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

package org.graphper.layout;

import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;

/**
 * Default settings for nodes and lines.
 *
 * @author Jamison Jiang
 */
class DefaultVal {

  private DefaultVal() {
  }

  /**
   * The default value of the node. This default value will be used when the node has not manually
   * set the corresponding attribute, and there is no corresponding value set by any node template.
   */
  static final NodeAttrs DEFAULT_NODE_ATTRS = Node
      .builder()
      .shape(NodeShapeEnum.ELLIPSE)
      .margin(0.1, 0.1)
      .labelloc(Labelloc.CENTER)
      .fontSize(14)
      .fontColor(Color.BLACK)
      .build()
      .nodeAttrs();

  /**
   * The default value of the line. This default value will be used when the line has not manually
   * set the corresponding attribute, and there is no corresponding value set by any line template.
   */
  static final LineAttrs DEFAULT_LINE_ATTRS = Line
      .tempLine()
      .controlPoints(false)
      .showboxes(false)
      .radian(20)
      .arrowHead(ArrowShape.NORMAL)
      .arrowTail(ArrowShape.NORMAL)
      .headclip(true)
      .tailclip(true)
      .arrowSize(1)
      .fontSize(14)
      .minlen(1)
      .weight(1)
      .dir(Dir.FORWARD)
      .build()
      .lineAttrs();
}
