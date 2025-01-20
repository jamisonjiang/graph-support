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

import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.TrianglePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultShapePosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TrianglePropCalcTest {

  @Test
  public void testIn() {
    TrianglePropCalc calc = new TrianglePropCalc(true);

    DefaultShapePosition shapePosition = new DefaultShapePosition(0, 1, 2, 4, NodeShapeEnum.TRIANGLE);
    Assertions.assertTrue(calc.in(shapePosition, new FlatPoint(0, 0.1)));
    Assertions.assertFalse(calc.in(shapePosition, new FlatPoint(0, -0.1)));
  }
}
