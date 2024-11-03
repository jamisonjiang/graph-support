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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.graphper.api.LineAttrs;
import org.graphper.def.Curves;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineHelper {

  private static final Logger log = LoggerFactory.getLogger(LineHelper.class);

  private LineHelper() {
  }

  /**
   * A piecewise cubic Bessel converted to control points, adjacent curves share the same control
   * point.
   *
   * @param curves piecewise cubic Bessel
   * @return cubic Bessel control points
   */
  public static List<FlatPoint> multiBezierCurveToPoints(MultiBezierCurve curves) {
    Objects.requireNonNull(curves);

    List<FlatPoint> splines = new ArrayList<>(curves.size() * 3 + 1);
    multiBezierCurveToPoints(curves, splines::add);
    return splines;
  }

  public static void multiBezierCurveToPoints(MultiBezierCurve curves, Consumer<FlatPoint> pointConsumer) {
    Objects.requireNonNull(curves);

    for (int i = 0; i < curves.size(); i++) {
      ThirdOrderBezierCurve curve = curves.get(i);
      if (i == 0) {
        pointConsumer.accept(curve.getV1());
      }
      pointConsumer.accept(curve.getV2());
      pointConsumer.accept(curve.getV3());
      pointConsumer.accept(curve.getV4());
    }
  }

  /**
   * If the node has a self-loop edge, generate a simulated path of the self-loop edge.
   *
   * @param node node to be detected
   */
  public static void selfLoopHandle(ANode node) {
    if (node == null || CollectionUtils.isEmpty(node.getSelfLines())) {
      return;
    }

    for (LineDrawProp lineDrawProp : node.getSelfLines()) {
      if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() < 2) {
        continue;
      }

      for (FlatPoint point : lineDrawProp) {
        point.setX(node.getX() + point.getX());
        point.setY(node.getY() + point.getY());
      }
      if (lineDrawProp.getLabelCenter() != null) {
        FlatPoint labelCenter = lineDrawProp.getLabelCenter();
        labelCenter.setX(node.getX() + labelCenter.getX());
        labelCenter.setY(node.getY() + labelCenter.getY());
      }

      newSelfLineDrawMode(lineDrawProp);
      if (CollectionUtils.isNotEmpty(lineDrawProp)) {
        lineDrawProp.setStart(lineDrawProp.get(0));
        lineDrawProp.setEnd(lineDrawProp.get(lineDrawProp.size() - 1));
      }
    }
  }

  public static FlatPoint curveGetFloatLabelStart(double[] labelLength, double lengthRatio,
                                                  LineDrawProp lineDrawProp) {
    if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() < 4) {
      return null;
    }

    double len = labelLength != null ? labelLength[0] : -1;
    if (len < 0) {
      len = 0;
      for (int i = 3; i < lineDrawProp.size(); i += 3) {
        FlatPoint v1 = lineDrawProp.get(i - 3);
        FlatPoint v2 = lineDrawProp.get(i - 2);
        FlatPoint v3 = lineDrawProp.get(i - 1);
        FlatPoint v4 = lineDrawProp.get(i);

        len += FlatPoint.twoFlatPointDistance(v1, v2);
        len += FlatPoint.twoFlatPointDistance(v2, v3);
        len += FlatPoint.twoFlatPointDistance(v3, v4);
      }
      if (labelLength != null) {
        labelLength[0] = len;
      }
    }

    double beforeLen = 0;
    double floatLabelInCurveLen = 0;
    FlatPoint v1 = null;
    FlatPoint v2 = null;
    FlatPoint v3 = null;
    FlatPoint v4 = null;

    double start = len * lengthRatio - 1;
    double end = len * lengthRatio + 1;
    for (int i = 3; i < lineDrawProp.size(); i += 3) {
      v1 = lineDrawProp.get(i - 3);
      v2 = lineDrawProp.get(i - 2);
      v3 = lineDrawProp.get(i - 1);
      v4 = lineDrawProp.get(i);

      floatLabelInCurveLen = 0;
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v1, v2);
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v2, v3);
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v3, v4);
      if (beforeLen + floatLabelInCurveLen > end) {
        break;
      }
      beforeLen += floatLabelInCurveLen;
    }

    if (floatLabelInCurveLen == 0) {
      return null;
    }

    if (beforeLen < start && beforeLen + floatLabelInCurveLen > end) {
      double t = BigDecimal.valueOf(len)
          .multiply(BigDecimal.valueOf(lengthRatio))
          .subtract(BigDecimal.valueOf(beforeLen))
          .divide(BigDecimal.valueOf(floatLabelInCurveLen), 4, RoundingMode.HALF_UP)
          .doubleValue();
      return Curves.besselEquationCalc(t, v1, v2, v3, v4);
    }

    return lengthRatio == 0 ? v1 : v4;
  }

  public static FlatPoint straightGetFloatLabelStart(double[] labelLength, double lengthRatio,
                                                     LineDrawProp lineDrawProp) {
    if (CollectionUtils.isEmpty(lineDrawProp)) {
      return null;
    }

    double len = labelLength != null ? labelLength[0] : -1;
    if (len < 0) {
      len = 0;
      for (int i = 1; i < lineDrawProp.size(); i++) {
        FlatPoint v1 = lineDrawProp.get(i - 1);
        FlatPoint v2 = lineDrawProp.get(i);

        len += FlatPoint.twoFlatPointDistance(v1, v2);
      }
      if (labelLength != null) {
        labelLength[0] = len;
      }
    }

    double beforeLen = 0;
    double floatLabelInCurveLen = 0;
    FlatPoint v1 = null;
    FlatPoint v2 = null;

    double start = len * lengthRatio - 1;
    double end = len * lengthRatio + 1;
    for (int i = 1; i < lineDrawProp.size(); i++) {
      v1 = lineDrawProp.get(i - 1);
      v2 = lineDrawProp.get(i);

      floatLabelInCurveLen = 0;
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v1, v2);
      if (beforeLen + floatLabelInCurveLen > end) {
        break;
      }
      beforeLen += floatLabelInCurveLen;
    }

    if (floatLabelInCurveLen == 0) {
      return null;
    }

    if (beforeLen < start && beforeLen + floatLabelInCurveLen > end) {
      double t = BigDecimal.valueOf(len)
          .multiply(BigDecimal.valueOf(lengthRatio))
          .subtract(BigDecimal.valueOf(beforeLen))
          .divide(BigDecimal.valueOf(floatLabelInCurveLen), 4, RoundingMode.HALF_UP)
          .doubleValue();
      return Vectors.add(Vectors.multiple(Vectors.sub(v2, v1), t), v1);
    }

    return lengthRatio == 0 ? v1 : v2;
  }

  public static void connectWithRoundedCorner(LineDrawProp line,
                                              List<FlatPoint> startPoints,
                                              List<FlatPoint> endPoints,
                                              List<? extends FlatPoint> throughPoints,
                                              UnaryOperator<MultiBezierCurve> fitFunction) {
    if (Objects.isNull(line)) {
      return;
    }

    line.markIsBesselCurve();
    if (CollectionUtils.isEmpty(throughPoints) || throughPoints.size() < 2) {
      lineDrawPropConnect(line, startPoints, true);
      lineDrawPropConnect(line, endPoints, false);
      return;
    }

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

  private static MultiBezierCurve getCorner(FlatPoint lt, FlatPoint p1, FlatPoint p2,
                                            FlatPoint p3, FlatPoint rt, double radian,
                                            UnaryOperator<MultiBezierCurve> fitFunction) {
    double d1 = FlatPoint.twoFlatPointDistance(p1, p2);
    double d2 = FlatPoint.twoFlatPointDistance(p2, p3);

    MultiBezierCurve curves;
    if (d1 < radian || d2 < radian) {
      curves = Curves.fitCurves(Arrays.asList(p1, p3),
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

  private static void newSelfLineDrawMode(LineDrawProp lineDrawProp) {
    if (CollectionUtils.isEmpty(lineDrawProp)
        || (lineDrawProp.size() != 2 && lineDrawProp.size() != 3)) {
      return;
    }

    FlatPoint start = lineDrawProp.get(0);
    FlatPoint mid = lineDrawProp.get(1);
    FlatPoint end;
    if (lineDrawProp.size() == 2) {
      end = start.clone();
    } else {
      end = lineDrawProp.get(2);
    }
    double minX = Math.min(start.getX(), end.getX());
    double maxX = Math.max(start.getX(), end.getX());
    double minY = Math.min(start.getY(), end.getY());
    double maxY = Math.max(start.getY(), end.getY());

    lineDrawProp.clear();
    if (mid.getX() >= minX && mid.getX() <= maxX && mid.getY() >= minY && mid.getY() <= maxY) {
      log.warn("Can not draw self line: mid point in Endpoint box");
      return;
    }

    lineDrawProp.add(start);
    if (mid.getX() < minX) {
      // Left
      double startDist = (start.getX() - mid.getX()) / 4;
      double endDist = (end.getX() - mid.getX()) / 4;
      if (start.getY() < end.getY()) {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() - startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() + endDist));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() + endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() + startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() - endDist));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() - endDist));
      }
    } else if (mid.getX() > maxX) {
      // Right
      double startDist = (mid.getX() - start.getX()) / 4;
      double endDist = (mid.getX() - end.getX()) / 4;
      if (start.getY() < end.getY()) {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() - startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() + endDist));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() + endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(mid.getX(), start.getY() + startDist));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(mid.getX(), end.getY() - endDist));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() - endDist));
      }
    } else if (mid.getY() < maxY) {
      // Up
      double startDist = (start.getY() - mid.getY()) / 4;
      double endDist = (end.getY() - mid.getY()) / 4;
      if (start.getX() < end.getX()) {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() - endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() - startDist));
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() - endDist));
      }
    } else {
      // Down
      double startDist = (mid.getY() - start.getY()) / 4;
      double endDist = (mid.getY() - end.getY()) / 4;
      if (start.getX() < end.getX()) {
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(start.getX() - startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() + endDist, end.getY() + endDist));
      } else {
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, start.getY() + startDist));
        lineDrawProp.add(new FlatPoint(start.getX() + startDist, mid.getY()));
        lineDrawProp.add(mid);
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, mid.getY()));
        lineDrawProp.add(new FlatPoint(end.getX() - endDist, end.getY() + endDist));
      }
    }

    lineDrawProp.add(end);

    if (lineDrawProp.size() == 2) {
      lineDrawProp.clear();
      return;
    }

    lineDrawProp.markIsBesselCurve();
  }
}
