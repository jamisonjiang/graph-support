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

import static org.graphper.layout.dot.AbstractDotLineRouter.besselCurveClipShape;
import static org.graphper.layout.dot.AbstractDotLineRouter.findInOutPair;
import static org.graphper.layout.dot.AbstractDotLineRouter.getFirst;
import static org.graphper.layout.dot.AbstractDotLineRouter.getLast;
import static org.graphper.layout.dot.AbstractDotLineRouter.newArrowShapePosition;

import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.util.CollectionUtils;
import org.graphper.api.ext.ShapePosition;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.dot.AbstractDotLineRouter.InOutPointPair;

public class CurvePathClip extends PathClip<LineDrawProp> {

  public static final CurvePathClip INSTANCE = new CurvePathClip();

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
    ShapePosition arrowShapePosition = newArrowShapePosition(getFirst(path), arrowSize);
    InOutPointPair inOutPair = findInOutPair(3, path, true, arrowShapePosition);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(arrowShapePosition, curve));
    } else {
      path.clear();
    }

    return path;
  }

  @Override
  protected LineDrawProp toArrowClip(double arrowSize, LineDrawProp path) {
    ShapePosition arrowShapePosition = newArrowShapePosition(getLast(path), arrowSize);
    InOutPointPair inOutPair = findInOutPair(3, path, false, arrowShapePosition);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(arrowShapePosition, curve));
    } else {
      path.clear();
    }

    return path;
  }

  @Override
  protected LineDrawProp clusterClip(ClusterDrawProp clusterDrawProp, LineDrawProp path) {
    InOutPointPair inOutPair = findInOutPair(3, path, true, clusterDrawProp);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(clusterDrawProp, curve));
    } else {
      path.clear();
    }

    return path;
  }

  @Override
  protected LineDrawProp nodeClip(ShapePosition node, LineDrawProp path, boolean firstStart) {
    InOutPointPair inOutPair = findInOutPair(3, path, firstStart, node);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(node, curve));
    }

    return path;
  }

  @Override
  protected boolean isNull(LineDrawProp path) {
    return CollectionUtils.isEmpty(path);
  }

  private void subPath(LineDrawProp path, InOutPointPair inOutPair,
                       ThirdOrderBezierCurve curve) {
    if (inOutPair.isDeleteBefore()) {
      if (inOutPair.getIdx() > -4) {
        path.subList(0, inOutPair.getIdx() + 4).clear();
      }

      path.add(0, curve.getV4());
      path.add(0, curve.getV3());
      path.add(0, curve.getV2());
      path.add(0, curve.getV1());
    } else {
      int time = path.size() - inOutPair.getIdx() + 3;
      for (int i = 0; i < time; i++) {
        path.remove(path.size() - 1);
      }

      path.add(curve.getV1());
      path.add(curve.getV2());
      path.add(curve.getV3());
      path.add(curve.getV4());
    }
  }

  private ThirdOrderBezierCurve getCurve(LineDrawProp path, InOutPointPair inOutPair) {
    ThirdOrderBezierCurve curve;
    if (inOutPair.isDeleteBefore()) {
      curve = new ThirdOrderBezierCurve(
          inOutPair.getIn(),
          path.get(inOutPair.getIdx() + 1),
          path.get(inOutPair.getIdx() + 2),
          inOutPair.getOut()
      );
    } else {
      curve = new ThirdOrderBezierCurve(
          inOutPair.getOut(),
          path.get(inOutPair.getIdx() - 2),
          path.get(inOutPair.getIdx() - 1),
          inOutPair.getIn()
      );
    }
    return curve;
  }
}
