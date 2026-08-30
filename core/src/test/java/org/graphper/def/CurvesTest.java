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

package org.graphper.def;

import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CurvesTest {

  @Test
  public void cubicBezierUsesExactlyThreeInterpolationLevels() {
    FlatPoint point = Curves.besselEquationCalc(0.5,
                                                new FlatPoint(0, 0),
                                                new FlatPoint(0, 1),
                                                new FlatPoint(1, 1),
                                                new FlatPoint(1, 0));

    Assertions.assertEquals(0.5, point.getX(), 1e-9);
    Assertions.assertEquals(0.75, point.getY(), 1e-9);
  }

  @Test
  public void dividedCurveMatchesOriginalTrajectory() {
    ThirdOrderBezierCurve original = new ThirdOrderBezierCurve(
        new FlatPoint(0, 0),
        new FlatPoint(10, 15),
        new FlatPoint(30, 20),
        new FlatPoint(40, 5));
    double split = 0.65;
    ThirdOrderBezierCurve suffix = Curves.divideThirdBesselCurve(split, false, original);

    for (int i = 0; i <= 10; i++) {
      double childT = i / 10D;
      FlatPoint expected = Curves.besselEquationCalc(split + (1 - split) * childT,
                                                     original.getV1(), original.getV2(),
                                                     original.getV3(), original.getV4());
      FlatPoint actual = Curves.besselEquationCalc(childT, suffix.getV1(), suffix.getV2(),
                                                   suffix.getV3(), suffix.getV4());
      Assertions.assertEquals(expected.getX(), actual.getX(), 1e-9);
      Assertions.assertEquals(expected.getY(), actual.getY(), 1e-9);
    }
  }
}
