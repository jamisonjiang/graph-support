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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.def.Curves.MultiBezierCurve;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CorridorSplineTest {

  @Test
  public void openCorridorProducesAStraightRoute() {
    List<RouterBox> boxes = Arrays.asList(
        new RouterBox(-200, 200, 0, 40),
        new RouterBox(-200, 200, 38, 76),
        new RouterBox(-200, 200, 74, 112),
        new RouterBox(-200, 200, 110, 150));

    CorridorSpline corridor = new CorridorSpline(boxes, true);
    FlatPoint start = new FlatPoint(0, 10);
    FlatPoint end = new FlatPoint(0, 140);
    List<FlatPoint> path = corridor.tautPath(0, boxes.size() - 1, start, end);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(2, path.size(),
                            "an unobstructed corridor must not bend the route: " + path);
    MultiBezierCurve curves = corridor.smooth(path);
    Assertions.assertEquals(0, reversals(curves), "straight route gained a reversal");
    Assertions.assertTrue(corridor.inCorridor(curves));
  }

  @Test
  public void routeIgnoresWaypointOffsetsWhenTheCorridorIsClear() {
    // Boxes are individually off-centre, mimicking a jittery virtual node chain. The route may use
    // any position inside them, so it must stay straight instead of chasing the box centres.
    List<RouterBox> boxes = Arrays.asList(
        new RouterBox(-40, 300, 0, 40),
        new RouterBox(-300, 60, 38, 76),
        new RouterBox(-40, 300, 74, 112),
        new RouterBox(-300, 60, 110, 150));

    CorridorSpline corridor = new CorridorSpline(boxes, true);
    List<FlatPoint> path = corridor.tautPath(0, boxes.size() - 1,
                                             new FlatPoint(0, 10), new FlatPoint(0, 140));

    Assertions.assertNotNull(path);
    Assertions.assertEquals(2, path.size(),
                            "route chased off-centre boxes instead of staying straight: " + path);
  }

  @Test
  public void narrowCorridorBendsOnceAndStaysInside() {
    // Only the middle band is restricted, so the route has to move left and come back. It must do
    // so with a single excursion rather than oscillating.
    List<RouterBox> boxes = Arrays.asList(
        new RouterBox(-200, 200, 0, 40),
        new RouterBox(-200, 200, 38, 76),
        new RouterBox(-200, -100, 74, 112),
        new RouterBox(-200, 200, 110, 148),
        new RouterBox(-200, 200, 146, 186));

    CorridorSpline corridor = new CorridorSpline(boxes, true);
    FlatPoint start = new FlatPoint(0, 10);
    FlatPoint end = new FlatPoint(0, 176);
    List<FlatPoint> path = corridor.tautPath(0, boxes.size() - 1, start, end);

    Assertions.assertNotNull(path);
    MultiBezierCurve curves = corridor.smooth(path);
    Assertions.assertNotNull(curves);
    Assertions.assertTrue(corridor.inCorridor(curves), "route left the corridor");
    Assertions.assertTrue(reversals(curves) <= 2,
                          "route oscillated instead of one excursion: " + reversals(curves));

    for (FlatPoint point : path) {
      Assertions.assertTrue(corridor.inCorridor(point), "path vertex outside corridor: " + point);
    }
  }

  @Test
  public void cornersKeepNonDegenerateTangents() {
    List<RouterBox> boxes = Arrays.asList(
        new RouterBox(-200, 200, 0, 40),
        new RouterBox(-200, -100, 38, 76),
        new RouterBox(-200, 200, 74, 114));

    CorridorSpline corridor = new CorridorSpline(boxes, true);
    List<FlatPoint> path = corridor.tautPath(0, boxes.size() - 1,
                                             new FlatPoint(0, 10), new FlatPoint(0, 104));
    MultiBezierCurve curves = corridor.smooth(path);
    Assertions.assertNotNull(curves);

    for (ThirdOrderBezierCurve curve : curves) {
      Assertions.assertTrue(
          FlatPoint.twoFlatPointDistance(curve.getV1(), curve.getV2()) > 1e-6
              && FlatPoint.twoFlatPointDistance(curve.getV3(), curve.getV4()) > 1e-6,
          "a segment collapsed to a zero length handle, which renders as a sharp corner");
    }

    // Adjacent segments must share their join and keep a continuous direction. A shared position
    // with a different heading is exactly the kink that rounding corners in isolation produces.
    for (int i = 1; i < curves.size(); i++) {
      ThirdOrderBezierCurve previous = curves.get(i - 1);
      ThirdOrderBezierCurve current = curves.get(i);
      Assertions.assertEquals(previous.getV4().getX(), current.getV1().getX(), 1e-6);
      Assertions.assertEquals(previous.getV4().getY(), current.getV1().getY(), 1e-6);

      double[] incoming = unit(previous.getV3(), previous.getV4());
      double[] outgoing = unit(current.getV1(), current.getV2());
      double dot = incoming[0] * outgoing[0] + incoming[1] * outgoing[1];
      Assertions.assertTrue(dot > 0.999,
                            "tangent direction jumps at join " + i + ", cos=" + dot);
    }
  }

  private double[] unit(FlatPoint from, FlatPoint to) {
    double dx = to.getX() - from.getX();
    double dy = to.getY() - from.getY();
    double len = Math.sqrt(dx * dx + dy * dy);
    Assertions.assertTrue(len > 1e-9, "zero length handle at " + from);
    return new double[]{dx / len, dy / len};
  }

  @Test
  public void horizontalCorridorIsRoutedAlongItsOwnAxis() {
    List<RouterBox> boxes = Arrays.asList(
        new RouterBox(0, 40, -200, 200),
        new RouterBox(38, 76, -200, 200),
        new RouterBox(74, 112, -200, -100),
        new RouterBox(110, 150, -200, 200));

    CorridorSpline corridor = new CorridorSpline(boxes, false);
    FlatPoint start = new FlatPoint(10, 0);
    FlatPoint end = new FlatPoint(140, 0);
    List<FlatPoint> path = corridor.tautPath(0, boxes.size() - 1, start, end);

    Assertions.assertNotNull(path);
    MultiBezierCurve curves = corridor.smooth(path);
    Assertions.assertNotNull(curves);
    Assertions.assertTrue(corridor.inCorridor(curves),
                          "horizontal route left the corridor: " + path);
  }

  @Test
  public void labelBoxDoesNotForceItsNodeCoordinateIntoThePath() {
    Line labelLine = Line.builder(Node.builder().build(), Node.builder().build())
        .label("station")
        .build();
    DNode labelNode = new DNode(null, 80, 38, 20, labelLine);
    labelNode.setX(-60);
    labelNode.setY(93);
    List<RouterBox> boxes = Arrays.asList(
        new RouterBox(-100, 100, 0, 40),
        new RouterBox(-100, 100, 38, 76),
        new RouterBox(-100, -20, 74, 112, labelNode),
        new RouterBox(-100, 100, 110, 148),
        new RouterBox(-100, 100, 146, 186));

    CorridorSpline corridor = new CorridorSpline(boxes, true);
    List<FlatPoint> path = corridor.tautPath(0, boxes.size() - 1,
                                             new FlatPoint(0, 10), new FlatPoint(0, 176));

    Assertions.assertNotNull(path);
    for (FlatPoint point : path) {
      Assertions.assertFalse(Math.abs(point.getX() - labelNode.getX()) < 1e-6
                                 && Math.abs(point.getY() - labelNode.getY()) < 1e-6,
                             "label coordinate became an unnecessary bend: " + path);
    }
    MultiBezierCurve curves = corridor.smooth(path);
    Assertions.assertNotNull(curves);
    Assertions.assertTrue(corridor.inCorridor(curves));
  }

  private int reversals(MultiBezierCurve curves) {
    List<FlatPoint> points = new ArrayList<>();
    for (ThirdOrderBezierCurve curve : curves) {
      if (points.isEmpty()) {
        points.add(curve.getV1());
      }
      points.add(curve.getV2());
      points.add(curve.getV3());
      points.add(curve.getV4());
    }

    int turns = 0;
    double previous = 0;
    for (int i = 1; i < points.size(); i++) {
      double dx = points.get(i).getX() - points.get(i - 1).getX();
      if (Math.abs(dx) < 0.5) {
        continue;
      }
      double direction = Math.signum(dx);
      if (previous != 0 && direction != previous) {
        turns++;
      }
      previous = direction;
    }
    return turns;
  }
}
