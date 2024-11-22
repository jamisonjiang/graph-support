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
import org.graphper.api.ext.ShapePosition;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * Abstract class providing utilities for clipping paths in graph rendering. This class defines
 * methods for handling path start and end points, arrow clipping, and clipping paths around
 * clusters or nodes. It also includes utility methods for working with path points and determining
 * in-out transitions with respect to shapes.
 *
 * <p>Designed to be extended by specific implementations that provide concrete behavior for
 * path clipping based on graph rendering requirements.</p>
 *
 * @author Jamison Jiang
 */
public abstract class PathClip {

  /**
   * Retrieves the starting point of the given path.
   *
   * @param path the {@link LineDrawProp} representing the path
   * @return the starting {@link FlatPoint} of the path
   */
  protected abstract FlatPoint pathFrom(LineDrawProp path);

  /**
   * Retrieves the ending point of the given path.
   *
   * @param path the {@link LineDrawProp} representing the path
   * @return the ending {@link FlatPoint} of the path
   */
  protected abstract FlatPoint pathTo(LineDrawProp path);

  /**
   * Clips the start of the path to fit an arrow of the specified size.
   *
   * @param arrowSize the size of the arrow
   * @param path      the {@link LineDrawProp} representing the path
   * @return the clipped {@link LineDrawProp}
   */
  protected abstract LineDrawProp fromArrowClip(double arrowSize, LineDrawProp path);

  /**
   * Clips the end of the path to fit an arrow of the specified size.
   *
   * @param arrowSize the size of the arrow
   * @param path      the {@link LineDrawProp} representing the path
   * @return the clipped {@link LineDrawProp}
   */
  protected abstract LineDrawProp toArrowClip(double arrowSize, LineDrawProp path);

  /**
   * Clips the path around a cluster boundary.
   *
   * @param clusterDrawProp the {@link ClusterDrawProp} representing the cluster's properties
   * @param path            the {@link LineDrawProp} representing the path
   * @return the clipped {@link LineDrawProp}
   */
  protected abstract LineDrawProp clusterClip(ClusterDrawProp clusterDrawProp, LineDrawProp path);

  /**
   * Clips the path around a node's shape boundary.
   *
   * @param node       the {@link ShapePosition} representing the node's position and shape
   * @param path       the {@link LineDrawProp} representing the path
   * @param firstStart whether the clipping applies to the start of the path
   * @return the clipped {@link LineDrawProp}
   */
  protected abstract LineDrawProp nodeClip(ShapePosition node, LineDrawProp path,
                                           boolean firstStart);

  /**
   * Checks if the given path is null.
   *
   * @param path the {@link LineDrawProp} to check
   * @return {@code true} if the path is null, {@code false} otherwise
   */
  protected boolean isNull(LineDrawProp path) {
    return path == null;
  }

  /**
   * Checks if the given path is not null.
   *
   * @param path the {@link LineDrawProp} to check
   * @return {@code true} if the path is not null, {@code false} otherwise
   */
  protected boolean isNotNull(LineDrawProp path) {
    return !isNull(path);
  }

  /**
   * Retrieves the point at the specified index from the path.
   *
   * @param path the list of {@link FlatPoint} representing the path
   * @param i    the index of the point to retrieve
   * @param <E>  FlatPoint type
   * @return the point at the specified index, or {@code null} if the index is out of bounds
   */
  public static <E extends FlatPoint> E getPoint(List<E> path, int i) {
    if (i < 0 || i >= path.size()) {
      return null;
    }
    return path.get(i);
  }

  /**
   * Retrieves the first point of the path.
   *
   * @param path the list of {@link FlatPoint} representing the path
   * @param <E>  FlatPoint type
   * @return the first point, or {@code null} if the path is empty
   */
  public static <E extends FlatPoint> E getFirst(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(0);
  }

  /**
   * Retrieves the last point of the path.
   *
   * @param path the list of {@link FlatPoint} representing the path
   * @param <E>  FlatPoint type
   * @return the last point, or {@code null} if the path is empty
   */
  public static <E extends FlatPoint> E getLast(List<E> path) {
    return CollectionUtils.isEmpty(path) ? null : path.get(path.size() - 1);
  }

  /**
   * Finds the in-out transition points in the path with respect to a shape boundary.
   *
   * @param unit          the step size for traversing the path
   * @param path          the list of {@link FlatPoint} representing the path
   * @param firstStart    whether to check the start of the path first
   * @param shapePosition the {@link ShapePosition} representing the shape boundary
   * @param <E>  FlatPoint type
   * @return an {@link InOutPointPair} representing the transition points, or {@code null} if not
   * found
   */
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

  /**
   * Represents a pair of points where a path transitions in and out of a shape boundary.
   */
  public static class InOutPointPair {

    private final int idx;
    private final boolean deleteBefore;
    private final FlatPoint in;
    private final FlatPoint out;

    /**
     * Constructs an {@code InOutPointPair}.
     *
     * @param idx          the index of the transition point
     * @param deleteBefore whether to delete points before the transition
     * @param in           the point where the path enters the shape
     * @param out          the point where the path exits the shape
     */
    public InOutPointPair(int idx, boolean deleteBefore, FlatPoint in, FlatPoint out) {
      this.idx = idx;
      this.deleteBefore = deleteBefore;
      this.in = in;
      this.out = out;
    }

    /**
     * Returns the index of the transition point.
     *
     * @return the index
     */
    public int getIdx() {
      return idx;
    }

    /**
     * Returns whether points before the transition should be deleted.
     *
     * @return {@code true} if points before should be deleted, {@code false} otherwise
     */
    public boolean isDeleteBefore() {
      return deleteBefore;
    }

    /**
     * Returns the point where the path enters the shape.
     *
     * @return the entry point
     */
    public FlatPoint getIn() {
      return in;
    }

    /**
     * Returns the point where the path exits the shape.
     *
     * @return the exit point
     */
    public FlatPoint getOut() {
      return out;
    }
  }
}
