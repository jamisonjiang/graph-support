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
    for (int i = 0; i < curves.size(); i++) {
      ThirdOrderBezierCurve curve = curves.get(i);
      if (i == 0) {
        splines.add(curve.getV1());
      }
      splines.add(curve.getV2());
      splines.add(curve.getV3());
      splines.add(curve.getV4());
    }

    return splines;
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

  private static void twoSelfLineDraw(LineDrawProp lineDrawProp) {
    if (CollectionUtils.isEmpty(lineDrawProp) || lineDrawProp.size() != 2) {
      return;
    }

    FlatPoint start = lineDrawProp.get(0);
    FlatPoint end = lineDrawProp.get(lineDrawProp.size() - 1);
    FlatPoint axis = Vectors.sub(end, start);
    FlatPoint vertical = new FlatPoint(axis.getY(), -axis.getX());
    FlatPoint verticalOpposite = vertical.reserve();
    lineDrawProp.clear();

    double dist = axis.dist() / 4;
    lineDrawProp.add(start);
    lineDrawProp.add(Vectors.add(start, Vectors.scale(vertical, dist)));
    lineDrawProp.add(Vectors.add(end, Vectors.scale(vertical, dist)));
    lineDrawProp.add(end);
    lineDrawProp.add(Vectors.add(end, Vectors.scale(verticalOpposite, dist)));
    lineDrawProp.add(Vectors.add(start, Vectors.scale(verticalOpposite, dist)));
    lineDrawProp.add(start);

    lineDrawProp.markIsBesselCurve();
  }

  private static void largeTwoSelfLineDraw(FlatPoint center, LineDrawProp lineDrawProp) {
    FlatPoint mid = lineDrawProp.get(lineDrawProp.size() / 2);
    FlatPoint start = lineDrawProp.get(0);
    FlatPoint end = lineDrawProp.get(lineDrawProp.size() - 1);

    MultiBezierCurve curves = Curves.fitCurves(Arrays.asList(start, mid, end),
                                               Vectors.add(
                                                   Vectors.sub(start, center),
                                                   Vectors.sub(mid, center)
                                               ),
                                               Vectors.add(
                                                   Vectors.sub(end, center),
                                                   Vectors.sub(mid, center)
                                               ), 0);

    lineDrawProp.clear();
    lineDrawProp.markIsBesselCurve();
    lineDrawProp.addAll(multiBezierCurveToPoints(curves));
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