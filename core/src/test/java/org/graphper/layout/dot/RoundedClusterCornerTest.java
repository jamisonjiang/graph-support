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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.Curves;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.dot.BoxGuideLineRouter.ThroughParam;
import org.graphper.layout.dot.BoxGuideLineRouter.ThroughPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoundedClusterCornerTest {

  @Test
  void shallowObstacleCornerSurvivesAtTopAndBottomBorders() {
    // sh0107 -> sh0207 in the unix graph: the bottom-right waypoint was discarded because
    // its slopes differ by less than 0.06, sending the connector through cluster60p0.
    for (boolean mirror : new boolean[]{false, true}) {
      Node from = Node.builder().build();
      Node to = Node.builder().build();
      Line edge = Line.builder(from, to).radian(20).build();
      DrawGraph draw = new DrawGraph(Graphviz.digraph().addLine(edge).build());
      LineDrawProp line = new LineDrawProp(edge, edge.lineAttrs(), draw);
      List<ThroughPoint> points = new ArrayList<>(Arrays.asList(
          new ThroughPoint(-2208, 877, 0), new ThroughPoint(-2269.95, 903, 0),
          new ThroughPoint(-3274, 941, 1), new ThroughPoint(-13187.67, 1162, 3),
          new ThroughPoint(-13273, 1188, 4)));
      List<RouterBox> boxes = new ArrayList<>(Arrays.asList(
          new RouterBox(-2269.95, -2145.95, 851, 903),
          new RouterBox(-3274, -1650, 901, 941),
          new RouterBox(-13534, -1650, 941, 1123),
          new RouterBox(-13534, -6044, 1123, 1164),
          new RouterBox(-13358.3, -13187.67, 1162, 1214)));
      if (mirror) {
        points.replaceAll(p -> new ThroughPoint(p.getX(), -p.getY(), p.getBoxIndex()));
        boxes.replaceAll(b -> new RouterBox(b.getLeftBorder(), b.getRightBorder(),
                                             -b.getDownBorder(), -b.getUpBorder()));
        Collections.reverse(points);
        Collections.reverse(boxes);
      }
      ThroughParam param = new ThroughParam();
      param.throughPoints = points;
      param.lineRouterBoxes = boxes;
      param.lineDrawProp = line;
      for (boolean preserve : new boolean[]{false, true}) {
        line.clear();
        param.preserveWaypoints = preserve;
        new RoundedRouter().throughPointHandle(param);
        Assertions.assertTrue(line.isBesselCurve());
        boolean crossed = false;
        for (int i = 0; i + 3 < line.size(); i += 3) {
          for (int step = 0; step <= 1000; step++) {
            FlatPoint p = Curves.besselEquationCalc(step / 1000D, line.get(i), line.get(i + 1),
                                                   line.get(i + 2), line.get(i + 3));
            double y = mirror ? -p.getY() : p.getY();
            crossed |= p.getX() > -6328 && p.getX() < -3282 && y > 507 && y < 933;
          }
        }
        Assertions.assertEquals(!preserve, crossed,
                                "Only preserving the shallow waypoint avoids the cluster");
      }
    }
  }
}
