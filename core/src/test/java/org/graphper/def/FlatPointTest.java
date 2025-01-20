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

import org.graphper.def.FlatPoint;
import org.graphper.def.UnmodifiablePointException;
import org.graphper.def.Vectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlatPointTest {

  @Test
  public void testTwoFlatPointDistance() {
    FlatPoint p1 = new FlatPoint(1, 1);
    FlatPoint p2 = new FlatPoint(2, 2);

    Assertions.assertEquals(1.41D, FlatPoint.twoFlatPointDistance(p1, p2), 0.1);

    p1 = new FlatPoint(3, 2);
    p2 = new FlatPoint(3, 5);

    Assertions.assertEquals(3D, FlatPoint.twoFlatPointDistance(p1, p2), 0.1);

    p1 = new FlatPoint(1, 1);
    p2 = new FlatPoint(5, 1);

    Assertions.assertEquals(4D, FlatPoint.twoFlatPointDistance(p1, p2), 0.1);

    p1 = new FlatPoint(1, 1);
    p2 = new FlatPoint(1, 1);

    Assertions.assertEquals(0D, FlatPoint.twoFlatPointDistance(p1, p2), 0.1);
  }

  @Test
  public void testUnmodifyFlatPoint() {
    Assertions.assertThrows(UnmodifiablePointException.class, () -> Vectors.ZERO.setX(1));
    Assertions.assertThrows(UnmodifiablePointException.class, () -> Vectors.ZERO.setHeight(1));
    Assertions.assertThrows(UnmodifiablePointException.class, () -> Vectors.ZERO.setY(1));
    Assertions.assertThrows(UnmodifiablePointException.class, () -> Vectors.ZERO.setWidth(1));
    Assertions.assertThrows(UnmodifiablePointException.class, Vectors.ZERO::flip);
  }
}
