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

import org.graphper.api.ext.StarPropCalc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.def.FlatPoint;

public class StarPropCalcTest {

  @Test
  public void testCornerAdj() {
    StarPropCalc starPropCalc = new StarPropCalc();

    FlatPoint p0 = new FlatPoint(0, 0);
    FlatPoint p1 = new FlatPoint(1, 1);
    FlatPoint p2 = new FlatPoint(2, 2);
    FlatPoint p3 = new FlatPoint(3, 3);
    FlatPoint p4 = new FlatPoint(4, 4);
    FlatPoint[] points = new FlatPoint[]{p0, p1, p2, p3, p4};

    Assertions.assertEquals(p2, starPropCalc.cornerAdj(0, points, false));
    Assertions.assertEquals(p3, starPropCalc.cornerAdj(0, points, true));

    Assertions.assertEquals(p3, starPropCalc.cornerAdj(1, points, false));
    Assertions.assertEquals(p4, starPropCalc.cornerAdj(1, points, true));

    Assertions.assertEquals(p4, starPropCalc.cornerAdj(2, points, false));
    Assertions.assertEquals(p0, starPropCalc.cornerAdj(2, points, true));

    Assertions.assertEquals(p0, starPropCalc.cornerAdj(3, points, false));
    Assertions.assertEquals(p1, starPropCalc.cornerAdj(3, points, true));

    Assertions.assertEquals(p1, starPropCalc.cornerAdj(4, points, false));
    Assertions.assertEquals(p2, starPropCalc.cornerAdj(4, points, true));
  }
}
