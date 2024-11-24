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

import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.fdp.FdpGraph.AreaGraph;

/**
 * Force-Directed Placement (FDP) Layout Engine.
 *
 * <p>This layout engine calculates graph layouts using a force-directed algorithm. It supports
 * various
 * edge routing strategies and provides functionalities for clustering and handling connected
 * components. The engine integrates with the Graphviz library and operates on a {@link DrawGraph}
 * to produce the final layout.</p>
 *
 * @author Jamison Jiang
 */
public class FdpLayoutEngine extends AbstractFdpLayout {

  private static final long serialVersionUID = 4639188492816085348L;

  protected void fdpLayout(AreaGraph graph, int iterations, double temperature,
                           double k, double width, double height) {
    double ksqaure = k * k;
    double gravityStrength = 0.1;

    // Force-directed algorithm
    for (int i = 0; i < iterations; i++) {
      // Calculate repulsive forces
      for (FNode n : graph) {
        n.setRepulsionX(0);
        n.setRepulsionY(0);
        for (FNode t : graph) {
          if (n == t) {
            continue;
          }

          double deltaX = n.getX() - t.getX();
          double deltaY = n.getY() - t.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          if (distance > 0) {
            double repulsiveForce = ksqaure / distance;
            n.setRepulsionX(n.getRepulsionX() + (deltaX / distance) * repulsiveForce);
            n.setRepulsionY(n.getRepulsionY() + (deltaY / distance) * repulsiveForce);
          }
        }
      }

      if (k == 0) {
        continue;
      }

      for (FNode n : graph) {
        for (FLine edge : graph.outAdjacent(n)) {
          if (edge.isSelf()) {
            continue;
          }

          FNode from = edge.from();
          FNode to = edge.to();
          double deltaX = from.getX() - to.getX();
          double deltaY = from.getY() - to.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

          if (distance > 0) {
            double attractiveForce = (distance * distance) / k;
            double dispX = (deltaX / distance) * attractiveForce;
            double dispY = (deltaY / distance) * attractiveForce;
            from.setRepulsionX(from.getRepulsionX() - dispX);
            from.setRepulsionY(from.getRepulsionY() - dispY);
            to.setRepulsionX(to.getRepulsionX() + dispX);
            to.setRepulsionY(to.getRepulsionY() + dispY);
          }
        }
      }

      // Apply gravity to pull nodes towards the center
      FlatPoint center = new FlatPoint(width / 2, height / 2);
      for (FNode n : graph) {
        double deltaX = center.getX() - n.getX();
        double deltaY = center.getY() - n.getY();
        n.setRepulsionLocation(n.getRepulsionX() + gravityStrength * deltaX,
                               n.getRepulsionY() + gravityStrength * deltaY);
      }

      // Limit the displacement and update positions
      for (FNode v : graph) {
        double displacement = Math.sqrt(v.getRepulsionX() * v.getRepulsionX()
                                            + v.getRepulsionY() * v.getRepulsionY());
        if (displacement > 0) {
          double x = v.getX() + (v.getRepulsionX() / displacement)
              * Math.min(displacement, temperature);
          double y = v.getY() + (v.getRepulsionY() / displacement)
              * Math.min(displacement, temperature);
          graph.setNodeLocation(v, x, y);
        }
      }
    }
  }
}
