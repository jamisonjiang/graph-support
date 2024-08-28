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

import static org.graphper.util.EnvProp.CLIP_DIST_ERROR;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.api.ext.ShapePosition;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.dot.AbstractDotLineRouter;
import org.graphper.util.Asserts;

public class StraightPathClip extends PathClip {

  public static final StraightPathClip INSTANCE = new StraightPathClip();

  @Override
  protected FlatPoint pathFrom(LineDrawProp path) {
    return getFirst(path);
  }

  @Override
  protected FlatPoint pathTo(LineDrawProp path) {
    return getLast(path);
  }

  @Override
  protected LineDrawProp fromArrowClip(double arrowSize, LineDrawProp path) {
    FlatPoint first = getFirst(path);
    return arrowClip(arrowSize, path, first, true);
  }

  @Override
  protected LineDrawProp toArrowClip(double arrowSize, LineDrawProp path) {
    FlatPoint last = getLast(path);
    return arrowClip(arrowSize, path, last, false);
  }

  @Override
  protected LineDrawProp clusterClip(ClusterDrawProp clusterDrawProp, LineDrawProp path) {
    InOutPointPair inOutPair = findInOutPair(1, path, true, clusterDrawProp);

    if (inOutPair != null) {
      FlatPoint p = straightLineClipShape(clusterDrawProp, inOutPair.getIn(), inOutPair.getOut());

      return subPath(path, inOutPair, p);
    }

    return null;
  }

  @Override
  protected LineDrawProp nodeClip(ShapePosition node, LineDrawProp path, boolean firstStart) {
    InOutPointPair inOutPair = findInOutPair(1, path, firstStart, node);

    if (inOutPair != null) {
      FlatPoint p = straightLineClipShape(node, inOutPair.getIn(), inOutPair.getOut());

      return subPath(path, inOutPair, p);
    }

    return path;
  }


  /**
   * Divide the path using the tangent vector that the path intersects at the node boundary to fit
   * the path to the node shape.
   *
   * @param shapePosition shape position information
   * @param inPoint       point inside node
   * @param outPoint      point outside node
   * @return border crossing point
   */
  public static FlatPoint straightLineClipShape(ShapePosition shapePosition,
                                                FlatPoint inPoint, FlatPoint outPoint) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    return straightLineClipShape(shapePosition, shapePosition.shapeProp(), inPoint, outPoint);
  }

  /**
   * Divide the path using the tangent vector that the path intersects at the node boundary to fit
   * the path to the node shape.
   *
   * @param box           node box
   * @param shapePropCalc node shape properties function
   * @param inPoint       point inside node
   * @param outPoint      point outside node
   * @return border crossing point
   */
  public static FlatPoint straightLineClipShape(Box box, ShapePropCalc shapePropCalc,
                                                FlatPoint inPoint, FlatPoint outPoint) {
    Asserts.nullArgument(inPoint, "inPoint");
    Asserts.nullArgument(outPoint, "outPoint");
    Asserts.nullArgument(box, "shapePosition");
    Asserts.nullArgument(shapePropCalc, "shapePosition.nodeShape()");

    Asserts.illegalArgument(
        !shapePropCalc.in(box, inPoint),
        "The specified internal node is not inside the node"
    );
    Asserts.illegalArgument(
        shapePropCalc.in(box, outPoint),
        "The specified external node is inside the node"
    );

    FlatPoint midPoint;
    FlatPoint in = inPoint;
    FlatPoint out = outPoint;

    do {
      midPoint = new FlatPoint((in.getX() + out.getX()) / 2, (in.getY() + out.getY()) / 2);

      if (shapePropCalc.in(box, midPoint)) {
        in = midPoint;
      } else {
        out = midPoint;
      }

    } while (FlatPoint.twoFlatPointDistance(in, out) > CLIP_DIST_ERROR);

    return midPoint;
  }

  private LineDrawProp subPath(LineDrawProp path, InOutPointPair inOutPair,
                               FlatPoint p) {
    if (inOutPair.isDeleteBefore()) {
      List<FlatPoint> temp = subList(inOutPair.getIdx(), path.size(), path);
      path.clear();
      path.addAll(temp);
      path.set(0, p);
    } else {
      List<FlatPoint> temp = subList(0, inOutPair.getIdx() + 1, path);
      path.clear();
      path.addAll(temp);
      path.set(path.size() - 1, p);
    }
    return path;
  }

  private LineDrawProp arrowClip(double arrowSize, LineDrawProp path,
                                 FlatPoint first, boolean firstStart) {
    if (first == null) {
      return path;
    }

    ShapePosition shapePosition = AbstractDotLineRouter.newArrowShapePosition(first, arrowSize);
    InOutPointPair inOutPair = findInOutPair(1, path, firstStart, shapePosition);

    if (inOutPair != null) {
      FlatPoint p = straightLineClipShape(shapePosition, inOutPair.getIn(), inOutPair.getOut());
      return subPath(path, inOutPair, p);
    }

    return null;
  }

  private List<FlatPoint> subList(int start, int end, LineDrawProp lineDrawProp) {
    List<FlatPoint> temp = new ArrayList<>(end - start);
    for (int i = start; i < end; i++) {
      temp.add(lineDrawProp.get(i));
    }
    return temp;
  }
}
