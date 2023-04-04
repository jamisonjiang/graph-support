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

package org.graphper.layout.dot;

import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

public class OrthoNodeSizeExpander extends NodeSizeExpander {

  public OrthoNodeSizeExpander(DNode node) {
    Asserts.nullArgument(node, "Self line node");
    Asserts.illegalArgument(!node.haveSelfLine(), "Node not have any self line");

    init(node);
  }

  private void init(DNode node) {
    node.sortSelfLine(this::lineComparator);

    OffsetConsumer consumer = (lineNo, line, topOffset, bottomOffset, rightOffset) -> {
      this.topHeightOffset = topOffset;
      this.bottomHeightOffset = bottomOffset;
      this.rightWidthOffset = rightOffset;
    };
    linePos(node, consumer);
  }

  public static void linePos(DNode node, OffsetConsumer consumer) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(consumer, "consumer");
    Asserts.illegalArgument( node.getSelfLoopCount()  < 1,"node do not have any self line");

    double interval = minSelfInterval(node) / 2;
    double topHeight = node.realTopHeight();
    double bottomHeight = node.realBottomHeight();
    double topHeightOffset = 0;
    double bottomHeightOffset = 0;
    double rightWidthOffset = 0;

    for (int i = 0; i < node.getSelfLoopCount(); i++) {
      DLine line = node.selfLine(i);
      FlatPoint labelSize = line.getLabelSize();
      double height = 0;
      double width = 0;

      if (labelSize != null) {
        height = labelSize.getHeight();
        width = labelSize.getWidth();
      }

      topHeightOffset = Math.max(topHeight + topHeightOffset + interval, height / 2) - topHeight;
      bottomHeightOffset = Math.max(bottomHeight + bottomHeightOffset + interval, height / 2) - bottomHeight;
      rightWidthOffset += Math.max(width, interval);
      consumer.consumeSelfLine(i, line, topHeightOffset, bottomHeightOffset, rightWidthOffset);
    }
  }

  private int lineComparator(DLine left, DLine right) {
    FlatPoint ls = left.getLabelSize();
    FlatPoint rs = right.getLabelSize();

    if (ls == null && rs == null) {
      return 0;
    }

    if (ls == null) {
      return -1;
    }
    if (rs == null) {
      return 1;
    }

    return Double.compare(ls.getHeight(), rs.getHeight());
  }

  public interface OffsetConsumer {

    void consumeSelfLine(int lineNo, DLine line, double topOffset,
                         double bottomOffset, double rightOffset);
  }
}
