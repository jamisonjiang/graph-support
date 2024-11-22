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

import java.util.ArrayList;
import java.util.List;
import org.graphper.layout.fdp.FdpGraph.AreaGraph;

/**
 * A specialized force-directed placement (GFDP) layout engine.
 * <p>
 * This algorithm is another variant of FDP that focuses on localized node interactions,
 * considering only the forces within the cell area of a node. This optimization reduces
 * computational complexity and is particularly effective for dense graphs with limited
 * interaction ranges.
 * </p>
 *
 * @author Jamison Jiang
 */
public class GFdpLayoutEngine extends AbstractFdpLayout {

  private static final long serialVersionUID = 5753649446512849866L;

  @Override
  protected void fdpLayout(AreaGraph graph, int iterations, double temperature,
                           double k, double width, double height) {
    int gridSize = graph.vertexNum() * 10;
    int gridWidth = (int) width;
    int gridHeight = (int) height;

    for (int i = 0; i < iterations; i++) {
      // Initialize grid
      Cell[][] grid = new Cell[gridWidth][gridHeight];
      for (int x = 0; x < gridWidth; x++) {
        for (int y = 0; y < gridHeight; y++) {
          grid[x][y] = new Cell();
        }
      }

      for (FNode v : graph) {
        int gridX = Math.min((int) Math.abs(v.getX() / gridSize), gridWidth - 1);
        int gridY = Math.min((int) Math.abs(v.getY() / gridSize), gridHeight - 1);
        grid[gridX][gridY].vertices.add(v);
      }

      // Calculate repulsive forces
      for (int x = 0; x < gridWidth; x++) {
        for (int y = 0; y < gridHeight; y++) {
          Cell cell = grid[x][y];
          for (FNode v : cell.vertices) {
            v.setRepulsionLocation(0, 0);
            for (int dx = -1; dx <= 1; dx++) {
              for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < gridWidth && ny < gridHeight) {
                  for (FNode u : grid[nx][ny].vertices) {
                    if (v != u) {
                      double deltaX = v.getX() - u.getX();
                      double deltaY = v.getY() - u.getY();
                      double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                      if (distance > 0) {
                        double repulsiveForce = k * k / distance;
                        v.setRepulsionLocation(
                            v.getRepulsionX() + (deltaX / distance) * repulsiveForce,
                            v.getRepulsionY() + (deltaY / distance) * repulsiveForce);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Calculate attractive forces
      for (FNode n : graph) {
        for (FLine e : graph.outAdjacent(n)) {
          if (e.isSelf()) {
            continue;
          }

          FNode v = e.from();
          FNode u = e.to();
          double deltaX = v.getX() - u.getX();
          double deltaY = v.getY() - u.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          if (distance > 0) {
            double attractiveForce = (distance * distance) / k;
            double dispX = (deltaX / distance) * attractiveForce;
            double dispY = (deltaY / distance) * attractiveForce;
            v.setRepulsionLocation(v.getRepulsionX() - dispX, v.getRepulsionY() - dispY);
            u.setRepulsionLocation(u.getRepulsionX() + dispX, u.getRepulsionY() + dispY);
          }
        }
      }

      // Limit the displacement and update positions
      for (FNode v : graph) {
        double displacement = Math.sqrt(
            v.getRepulsionX() * v.getRepulsionX() + v.getRepulsionY() * v.getRepulsionY());
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

  private static class Cell {

    List<FNode> vertices = new ArrayList<>();
  }
}
