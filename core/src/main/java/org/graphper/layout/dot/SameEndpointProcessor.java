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

import static org.graphper.layout.StraightPathClip.straightLineClipShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;

/** Applies Graphviz samehead/sametail shared boundary points before DOT routing. */
final class SameEndpointProcessor {

  private final DotAttachment attachment;

  private final DotDigraph digraph;

  private final DrawGraph drawGraph;

  SameEndpointProcessor(DotAttachment attachment) {
    this.attachment = attachment;
    this.digraph = attachment.getDotDigraph();
    this.drawGraph = attachment.getDrawGraph();
  }

  void process() {
    if (!attachment.haveSameEndpointLines()) {
      return;
    }
    Map<Node, Map<String, List<DLine>>> sameTail = new HashMap<>();
    Map<Node, Map<String, List<DLine>>> sameHead = new HashMap<>();
    for (DLine line : attachment.getSameEndpointLines()) {
      Line apiLine = line.getLine();
      if (apiLine.tail() == apiLine.head()) {
        continue;
      }
      String tailGroup = line.getLineDrawProp().lineAttrs().getSameTail();
      if (StringUtils.isNotEmpty(tailGroup)) {
        add(sameTail, apiLine.tail(), tailGroup, line);
      }
      String headGroup = line.getLineDrawProp().lineAttrs().getSameHead();
      if (StringUtils.isNotEmpty(headGroup)) {
        add(sameHead, apiLine.head(), headGroup, line);
      }
    }
    apply(sameTail, false);
    apply(sameHead, true);
  }

  private void add(Map<Node, Map<String, List<DLine>>> groups, Node node, String id, DLine line) {
    groups
        .computeIfAbsent(node, key -> new HashMap<>())
        .computeIfAbsent(id, key -> new ArrayList<>(2))
        .add(line);
  }

  private void apply(Map<Node, Map<String, List<DLine>>> groups, boolean head) {
    for (Map.Entry<Node, Map<String, List<DLine>>> nodeEntry : groups.entrySet()) {
      for (List<DLine> lines : nodeEntry.getValue().values()) {
        if (lines.size() < 2) {
          continue;
        }
        FlatPoint point = sharedPoint(nodeEntry.getKey(), lines);
        if (point == null) {
          continue;
        }
        for (DLine line : lines) {
          LineDrawProp prop = line.getLineDrawProp();
          if (head) {
            prop.setSameHeadPoint(point);
          } else {
            prop.setSameTailPoint(point);
          }
        }
      }
    }
  }

  private FlatPoint sharedPoint(Node owner, List<DLine> lines) {
    DNode ownerNode = digraph.getNode(owner);
    if (ownerNode == null) {
      return null;
    }
    double sumX = 0;
    double sumY = 0;
    FlatPoint fallback = null;
    for (DLine line : lines) {
      Node other = line.getLine().tail() == owner ? line.getLine().head() : line.getLine().tail();
      DNode otherNode = digraph.getNode(other);
      if (otherNode == null) {
        continue;
      }
      double x = otherNode.getX() - ownerNode.getX();
      double y = otherNode.getY() - ownerNode.getY();
      double length = Math.hypot(x, y);
      if (length == 0) {
        continue;
      }
      x /= length;
      y /= length;
      if (fallback == null) {
        fallback = new FlatPoint(x, y);
      }
      sumX += x;
      sumY += y;
    }
    double length = Math.hypot(sumX, sumY);
    if (length < 1E-9) {
      if (fallback == null) {
        return null;
      }
      sumX = fallback.getX();
      sumY = fallback.getY();
      length = 1;
    }
    sumX /= length;
    sumY /= length;

    NodeDrawProp ownerProp = drawGraph.getNodeDrawProp(owner);
    FlatPoint center = new FlatPoint(ownerNode.getX(), ownerNode.getY());
    double radius = Math.max(ownerProp.getWidth(), ownerProp.getHeight()) + 1;
    FlatPoint outside = new FlatPoint(center.getX() + sumX * radius, center.getY() + sumY * radius);
    return straightLineClipShape(ownerProp, ownerProp.shapeProp(), center, outside);
  }
}
