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

public class GFdpLayoutEngine extends FdpLayoutEngine {

  private static final long serialVersionUID = 5753649446512849866L;

  @Override
  protected void fdpLayout(FdpGraph graph, int iterations, double temperature,
                           double coolingFactor, double k) {
    int width = Math.max(graph.vertexNum() * 50, 100);
    int height = width;
    int gridSize = graph.vertexNum() * 10; //
    int gridWidth = (int) Math.ceil(width / (double) gridSize);
    int gridHeight = (int) Math.ceil(height / (double) gridSize);

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
            v.setDispLocation(0, 0);
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
                        v.setDispLocation(v.getDispX() + (deltaX / distance) * repulsiveForce,
                                          v.getDispY() + (deltaY / distance) * repulsiveForce);
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
        for (FLine e : graph.adjacent(n)) {
          FNode v = e.from();
          FNode u = e.to();
          double deltaX = v.getX() - u.getX();
          double deltaY = v.getY() - u.getY();
          double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          if (distance > 0) {
            double attractiveForce = (distance * distance) / k;
            double dispX = (deltaX / distance) * attractiveForce;
            double dispY = (deltaY / distance) * attractiveForce;
            v.setDispLocation(v.getDispX() - dispX, v.getDispY() - dispY);
            u.setDispLocation(u.getDispX() + dispX, u.getDispY() + dispY);
          }
        }
      }

      // Limit the displacement and update positions
      for (FNode v : graph) {
        double displacement = Math.sqrt(v.getDispX() * v.getDispX() + v.getDispY() * v.getDispY());
        if (displacement > 0) {
          v.setLocation(v.getX() + (v.getDispX() / displacement) * Math.min(displacement,
                                                                            temperature),
                        v.getY() + (v.getDispY() / displacement) * Math.min(displacement,
                                                                            temperature));
        }
      }

      temperature *= coolingFactor;
    }
  }

  private static class Cell {

    List<FNode> vertices = new ArrayList<>();
  }
}