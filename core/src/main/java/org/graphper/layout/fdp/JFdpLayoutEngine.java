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

package org.graphper.layout.fdp;

import org.graphper.layout.fdp.FdpGraph.AreaGraph;

/**
 * A specialized force-directed placement (JFDP) layout engine.
 * <ul>
 *   <li>Reduced node displacement for better stability.</li>
 *   <li>Degree-based scaling of attractive forces.</li>
 *   <li>Efficient repulsive force calculation to avoid overlaps.</li>
 *   <li>Bounding node positions within the graph area.</li>
 * </ul>
 */
public class JFdpLayoutEngine extends AbstractFdpLayout {

  private static final long serialVersionUID = -5998157771161759195L;

  @Override
  protected void fdpLayout(AreaGraph graph, int iterations, double temperature,
                           double k, double width, double height) {
    width = Math.max(800, graph.edgeNum() + graph.vertexNum());
    height = width;
    for (int i = 0; i < iterations; i++) {
      for (FNode node : graph) {
        node.setDx(node.getDx() / 4);
        node.setDy(node.getDy() / 4);
        node.setEdgedx(0);
        node.setEdgedy(0);
        node.setRepulsionX(0);
        node.setRepulsionY(0);
      }

      for (FNode n : graph) {
        int nd = graph.degree(n);

        for (FLine edge : graph.outAdjacent(n)) {
          if (edge.isSelf()) {
            continue;
          }

          FNode p1 = edge.from();
          FNode p2 = edge.to();
          double vx = p1.getX() - p2.getX();
          double vy = p1.getY() - p2.getY();
          double len = Math.sqrt(vx * vx + vy * vy);
          double desiredLen = 30;

          len = (len == 0) ? .0001 : len;
          double f =  (1.0 / 3.0) * (desiredLen - len) / len;

          f = f * Math.pow(0.7, (nd + graph.degree(p2) - 2));

          double dx = f * vx;
          double dy = f * vy;

          p1.setEdgedx(p1.getEdgedx() + dx);
          p1.setEdgedy(p1.getEdgedy() + dy);
          p2.setEdgedx(p2.getEdgedx() - dx);
          p2.setEdgedy(p2.getEdgedy() - dy);
        }
      }

      // Calculate repulsive forces
      for (FNode n : graph) {
        double dx = 0;
        double dy = 0;
        for (FNode t : graph) {
          if (n == t) {
            continue;
          }

          double vx = n.getX() - t.getX();
          double vy = n.getY() - t.getY();
          double distanceSq = vx * vx + vy * vy;
          if (distanceSq == 0) {
            dx += 0.1;
            dy += 0.1;
          } else {
            dx += vx / distanceSq;
            dy += vy / distanceSq;
          }
        }

        double dlen = dx * dx + dy * dy;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          n.setRepulsionX(n.getRepulsionX() + dx / dlen);
          n.setRepulsionY(n.getRepulsionY() + dy / dlen);
        }
      }

      for (FNode v : graph) {
        v.setDx(v.getDx() + v.getRepulsionX() + v.getEdgedx());
        v.setDy(v.getDy() + v.getRepulsionY() + v.getEdgedy());

        // keeps nodes from moving any faster than 5 per time unit
        double advance = 10;
        graph.setNodeLocation(v, v.getX() + Math.max(-advance, Math.min(advance, v.getDx())),
                              v.getY() + Math.max(-advance, Math.min(advance, v.getDy())));

        if (v.getX() < 0) {
          graph.setNodeLocation(v, 0, v.getY());
        } else if (v.getX() > width) {
          graph.setNodeLocation(v, width, v.getY());
        }
        if (v.getY() < 0) {
          graph.setNodeLocation(v, v.getX(), 0);
        } else if (v.getY() > height) {
          graph.setNodeLocation(v, v.getX(), height);
        }
      }
    }
  }
}
