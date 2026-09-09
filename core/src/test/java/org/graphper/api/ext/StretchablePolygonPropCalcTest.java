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
package org.graphper.api.ext;

import java.util.List;
import org.graphper.def.FlatPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StretchablePolygonPropCalcTest {

  @Test
  public void oddPolygonsContainRequestedRectangle() {
    for (int sides : new int[]{3, 5, 7}) {
      StretchablePolygonPropCalc calc = new StretchablePolygonPropCalc(sides);
      FlatPoint size = calc.minContainerSize(20, 50, 15, 30);
      DefaultBox box = new DefaultBox(-size.getWidth() / 2, size.getWidth() / 2,
                                      -size.getHeight() / 2, size.getHeight() / 2);
      Assertions.assertTrue(calc.in(box, new FlatPoint(-25, -10)), "sides=" + sides);
      Assertions.assertTrue(calc.in(box, new FlatPoint(25, -10)), "sides=" + sides);
      Assertions.assertTrue(calc.in(box, new FlatPoint(25, 10)), "sides=" + sides);
      Assertions.assertTrue(calc.in(box, new FlatPoint(-25, 10)), "sides=" + sides);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void compatibilityPointMethodsDoNotMakeInUseStaleGeometry() {
    RegularPolylinePropCalc calc = new RegularPolylinePropCalc(4);
    DefaultBox first = new DefaultBox(-10, 10, -10, 10);
    calc.initPoints(first);
    List<FlatPoint> firstPoints = calc.getPoints();
    Assertions.assertEquals(4, firstPoints.size());

    DefaultBox second = new DefaultBox(-100, 100, -100, 100);
    Assertions.assertTrue(calc.in(second, new FlatPoint(50, 0)));
    Assertions.assertSame(firstPoints, calc.getPoints());
  }

  @Test
  public void rejectsDegeneratePolygon() {
    Assertions.assertThrows(IllegalArgumentException.class,
                            () -> new StretchablePolygonPropCalc(2));
  }
}
