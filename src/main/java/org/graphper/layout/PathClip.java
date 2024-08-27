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

import java.util.List;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.api.ext.ShapePosition;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

public abstract class PathClip {

  protected abstract FlatPoint pathFrom(LineDrawProp path);

  protected abstract FlatPoint pathTo(LineDrawProp path);

  protected abstract LineDrawProp fromArrowClip(double arrowSize, LineDrawProp path);

  protected abstract LineDrawProp toArrowClip(double arrowSize, LineDrawProp path);

  protected abstract LineDrawProp clusterClip(ClusterDrawProp clusterDrawProp, LineDrawProp path);

  protected abstract LineDrawProp nodeClip(ShapePosition node, LineDrawProp path,
                                           boolean firstStart);

  protected boolean isNull(LineDrawProp path) {
    return path == null;
  }

  protected boolean isNotNull(LineDrawProp path) {
    return !isNull(path);
  }

  public static <E extends FlatPoint> E getPoint(List<E> path, int i) {
    if (i < 0 || i >= path.size()) {
      return null;
    }

    return path.get(i);
  }

  public static <E extends FlatPoint> E getFirst(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(0);
  }

  public static <E extends FlatPoint> E getLast(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(path.size() - 1);
  }

  public static <E extends FlatPoint> InOutPointPair findInOutPair(int unit, List<E> path,
                                                                   boolean firstStart,
                                                                   ShapePosition shapePosition) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    Asserts.nullArgument(shapePosition.shapeProp(), "shapePosition.nodeShape()");

    Integer idx = null;
    Integer count = null;

    ShapePropCalc shapeProp = shapePosition.shapeProp();

    E point = getFirst(path);
    if (firstStart && point != null && shapeProp.in(shapePosition, point)) {
      idx = 0;
      count = unit;
    } else {
      point = getLast(path);
      if (point != null && shapeProp.in(shapePosition, point)) {
        idx = path.size() - 1;
        count = -unit;
      }
    }

    if (idx == null) {
      return null;
    }

    E pre = null;
    do {
      if (pre != null) {
        boolean preIn = shapeProp.in(shapePosition, pre);
        boolean pointIn = shapeProp.in(shapePosition, point);

        if (preIn != pointIn) {
          return new InOutPointPair(
              idx - count,
              count > 0,
              preIn ? pre : point,
              pointIn ? pre : point
          );
        }
      }

      idx += count;
      pre = point;
      point = getPoint(path, idx);
    } while (point != null);

    return null;
  }

  public static class InOutPointPair {

    private final int idx;

    private final boolean deleteBefore;

    private final FlatPoint in;

    private final FlatPoint out;

    public InOutPointPair(int idx, boolean deleteBefore, FlatPoint in, FlatPoint out) {
      this.idx = idx;
      this.deleteBefore = deleteBefore;
      this.in = in;
      this.out = out;
    }

    public int getIdx() {
      return idx;
    }

    public boolean isDeleteBefore() {
      return deleteBefore;
    }

    public FlatPoint getIn() {
      return in;
    }

    public FlatPoint getOut() {
      return out;
    }
  }
}
