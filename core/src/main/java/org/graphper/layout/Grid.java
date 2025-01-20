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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.graphper.api.attributes.Splines;

/**
 * Grid object for {@link Splines#ORTHO} router.
 *
 * @author Jamison Jiang
 */
public class Grid {

  private final TreeMap<Double, GridAxis> horAxes;
  private final TreeMap<Double, GridAxis> verAxes;

  private Grid(TreeMap<Double, GridAxis> horAxes,
               TreeMap<Double, GridAxis> verAxes) {
    this.horAxes = horAxes;
    this.verAxes = verAxes;
  }

  public static GridBuilder builder() {
    return new GridBuilder();
  }

  public GridAxis getVerAxis(double v) {
    return verAxes.get(v);
  }

  public GridAxis getHorAxis(double v) {
    return horAxes.get(v);
  }

  public GridAxis getFirstHorAxis() {
    for (Entry<Double, GridAxis> entry : horAxes.entrySet()) {
      return entry.getValue();
    }
    return null;
  }

  public GridAxis getFirstVerAxis() {
    for (Entry<Double, GridAxis> entry : verAxes.entrySet()) {
      return entry.getValue();
    }
    return null;
  }

  public int rowNum() {
    return horAxes.size();
  }

  public int colNum() {
    return verAxes.size();
  }

  public int coordToIdx(int row, int col) {
    return row * colNum() + col;
  }

  public static class GridAxis {

    private int idx;

    private double val;

    private GridAxis pre;

    private GridAxis next;

    private Set<Double> blockAxes;

    private GridAxis() {
    }

    public int getIdx() {
      return idx;
    }

    public double getVal() {
      return val;
    }

    public GridAxis pre() {
      return pre;
    }

    public GridAxis next() {
      return next;
    }

    public void addBlockAxis(double blockAxis) {
      if (blockAxes == null) {
        blockAxes = new HashSet<>();
      }
      blockAxes.add(blockAxis);
    }

    public boolean isNotBlock(double blockAxis) {
      return !isBlock(blockAxis);
    }

    public boolean isBlock(double blockAxis) {
      return blockAxes != null && blockAxes.contains(blockAxis);
    }
  }

  public static class GridBuilder {

    private final TreeMap<Double, GridAxis> horAxes;

    private final TreeMap<Double, GridAxis> verAxes;

    private GridBuilder() {
      this.horAxes = new TreeMap<>();
      this.verAxes = new TreeMap<>();
    }

    public GridBuilder addHorAxis(double horAxis) {
      horAxes.computeIfAbsent(horAxis, a -> new GridAxis());
      return this;
    }

    public GridBuilder addVerAxis(double verAxis) {
      verAxes.computeIfAbsent(verAxis, a -> new GridAxis());
      return this;
    }

    public Double minHorAxis() {
      return horAxes.firstKey();
    }

    public Double maxHorAxis() {
      return horAxes.lastKey();
    }

    public Double minVerAxis() {
      return verAxes.firstKey();
    }

    public Double maxVerAxis() {
      return verAxes.lastKey();
    }

    public Grid build() {
      // Supplement the hor/ver axis info
      axisInit(horAxes);
      axisInit(verAxes);
      return new Grid(horAxes, verAxes);
    }

    private void axisInit(TreeMap<Double, GridAxis> axises) {
      int i = 0;
      GridAxis pre = null;
      for (Entry<Double, GridAxis> entry : axises.entrySet()) {
        if (pre != null) {
          pre.next = entry.getValue();
        }
        entry.getValue().pre = pre;
        entry.getValue().val = entry.getKey();
        pre = entry.getValue();
        pre.idx = i++;
      }
    }
  }
}
