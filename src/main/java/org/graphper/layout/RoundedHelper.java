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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.graphper.api.LineAttrs;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.CollectionUtils;

public class RoundedHelper {

  public static void connectWithRoundedCorner(LineDrawProp line,
                                              List<FlatPoint> startPoints,
                                              List<FlatPoint> endPoints,
                                              List<? extends FlatPoint> throughPoints,
                                              UnaryOperator<MultiBezierCurve> fitFunction) {
    if (Objects.isNull(line) || CollectionUtils.isEmpty(throughPoints)
        || throughPoints.size() < 2) {
      return;
    }

    line.markIsBesselCurve();
    if (throughPoints.size() == 2) {
      FlatPoint first = throughPoints.get(0);
      FlatPoint last = throughPoints.get(throughPoints.size() - 1);

      if (startPoints == null && endPoints == null) {
        line.add(first);
        line.add(last);
        line.markIsLineSegment();
        return;
      }
      line.add(first);
      line.add(first);
      line.add(last);
      line.add(last);

      lineDrawPropConnect(line, startPoints, true);
      lineDrawPropConnect(line, endPoints, false);
      return;
    }

    FlatPoint pre = null;
    FlatPoint prepre = null;
    LineAttrs lineAttrs = line.lineAttrs();
    double radian = lineAttrs.getRadian() != null ? lineAttrs.getRadian() : 10;

    for (int i = 2; i < throughPoints.size(); i++) {
      FlatPoint p1 = prepre == null ? throughPoints.get(i - 2) : prepre;
      FlatPoint p2 = pre == null ? throughPoints.get(i - 1) : pre;
      FlatPoint p3 = throughPoints.get(i);

      if (prepre == null) {
        prepre = p1;
        line.add(p1);
      }

      if (isCorner(p1, p2, p3)) {
        ThirdOrderBezierCurve corner = null;
        FlatPoint lt = getLeftTangle(line);
        FlatPoint rt = i < throughPoints.size() - 1 ? throughPoints.get(i + 1) : null;

        MultiBezierCurve curves = getCorner(lt, p1, p2, p3, rt, radian, fitFunction);
        if (curves.size() == 1) {
          line.add(prepre);
        }

        for (ThirdOrderBezierCurve curve : curves) {
          if (corner == null && curves.size() == 1) {
            line.add(curve.getV1());
            line.add(curve.getV1());
          }

          line.add(curve.getV2());
          line.add(curve.getV3());
          line.add(curve.getV4());

          corner = curve;
        }

        if (corner != null) {
          prepre = corner.getV4();
          pre = p3;
        }

        if (corner != null && i == throughPoints.size() - 1) {
          line.add(corner.getV4());
          line.add(p3);
          line.add(p3);
        }
      } else {
        if (i == throughPoints.size() - 1) {
          line.add(p1);
          line.add(p3);
          line.add(p3);
        } else {
          pre = p3;
        }
      }
    }

    lineDrawPropConnect(line, startPoints, true);
    lineDrawPropConnect(line, endPoints, false);
  }

  private static MultiBezierCurve getCorner(FlatPoint lt, FlatPoint p1, FlatPoint p2,
                                            FlatPoint p3, FlatPoint rt, double radian,
                                            UnaryOperator<MultiBezierCurve> fitFunction) {
    double d1 = FlatPoint.twoFlatPointDistance(p1, p2);
    double d2 = FlatPoint.twoFlatPointDistance(p2, p3);

    MultiBezierCurve curves;
    if (d1 < radian || d2 < radian) {
      return Curves.fitCurves(Arrays.asList(p1, p3),
                              lt != null ? Vectors.sub(p2, lt) : null,
                              rt != null ? Vectors.sub(p3, rt) : null, 0);

    } else {
      FlatPoint dir = Vectors.sub(p2, p1);
      double ratio = (d1 - radian) / d1;
      FlatPoint v1 = Vectors.add(p1, Vectors.multiple(dir, ratio));

      ratio = (d1 - radian / 2) / d1;
      FlatPoint v2 = Vectors.add(p1, Vectors.multiple(dir, ratio));

      dir = Vectors.sub(p2, p3);
      ratio = (d2 - radian) / d2;
      FlatPoint v4 = Vectors.add(p3, Vectors.multiple(dir, ratio));

      ratio = (d2 - radian / 2) / d2;
      FlatPoint v3 = Vectors.add(p3, Vectors.multiple(dir, ratio));

      curves = new MultiBezierCurve(1);
      curves.add(new ThirdOrderBezierCurve(v1, v2, v3, v4));
    }

    return fitFunction == null ? curves : fitFunction.apply(curves);
//    return curves;
  }

  private static boolean isCorner(FlatPoint p1, FlatPoint p2, FlatPoint p3) {
    if (p1 == null || p2 == null || p3 == null) {
      return false;
    }

    if (Objects.equals(p1, p2) || Objects.equals(p2, p3) || Objects.equals(p1, p3)) {
      return false;
    }

    if (p1.similarX(p2.getX(), 1) && p1.similarX(p3.getX(), 1) && p2.similarX(p3.getX(), 1)) {
      return false;
    }

    if (p1.similarY(p2.getY(), 1) && p1.similarY(p3.getY(), 1) && p2.similarY(p3.getY(), 1)) {
      return false;
    }

    double slope1 = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
    double slope2 = (p3.getY() - p2.getY()) / (p3.getX() - p2.getX());

    return Math.abs(slope1 - slope2) > 0.06;
  }

  private static FlatPoint getLeftTangle(LineDrawProp lineDrawProp) {
    if (lineDrawProp.size() <= 3) {
      return null;
    }

    FlatPoint v4 = lineDrawProp.get(lineDrawProp.size() - 1);
    FlatPoint v3 = lineDrawProp.get(lineDrawProp.size() - 2);
    FlatPoint v2 = lineDrawProp.get(lineDrawProp.size() - 3);
    return v3.equals(v4) ? v2 : v3;
  }


  public static void lineDrawPropConnect(LineDrawProp lineDrawProp,
                                         List<FlatPoint> target, boolean before) {
    if (Objects.isNull(lineDrawProp) || CollectionUtils.isEmpty(target)) {
      return;
    }

    if (before) {
      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        target.remove(target.size() - 1);
      }
      for (int i = target.size() - 1; i >= 0; i--) {
        lineDrawProp.add(0, target.get(i));
      }
    } else {
      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        target.remove(0);
      }
      lineDrawProp.addAll(target);
    }
  }
}
