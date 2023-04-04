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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VectorsTest {

  @Test
  public void testUnit() {
    FlatPoint p = Vectors.unit(2, 2);
    Assertions.assertEquals(1D, p.dist(), 0.001);

    p = Vectors.unit(56, 2324);
    Assertions.assertEquals(1D, p.dist(), 0.001);

    p = Vectors.unit(-23, -232);
    Assertions.assertEquals(1D, p.dist(), 0.001);

    p = Vectors.unit(-23, 0);
    Assertions.assertEquals(1D, p.dist(), 0.001);

    p = Vectors.unit(0.004, 0.011);
    Assertions.assertEquals(1D, p.dist(), 0.001);

    p = Vectors.unit(0, 0);
    Assertions.assertEquals(0D, p.dist(), 0.001);
  }

  @Test
  public void testSub() {
    FlatPoint p1 = new FlatPoint(1, 2);
    FlatPoint p2 = new FlatPoint(3, 5);

    FlatPoint sub = Vectors.sub(p2, p1);

    Assertions.assertEquals(2, sub.getX(), 0);
    Assertions.assertEquals(3, sub.getY(), 0);
  }

  @Test
  public void testAdd() {
    FlatPoint p1 = new FlatPoint(1, 2);
    FlatPoint p2 = new FlatPoint(3, 5);

    FlatPoint add = Vectors.add(p2, p1);

    Assertions.assertEquals(4, add.getX(), 0);
    Assertions.assertEquals(7, add.getY(), 0);
  }

  @Test
  public void testMul() {
    FlatPoint p1 = new FlatPoint(1, 2);
    FlatPoint p2 = new FlatPoint(3, 5);

    Assertions.assertEquals(13D, Vectors.mul(p2, p1), 0.1);
  }

  @Test
  public void testScale() {
    FlatPoint p1 = new FlatPoint(3, 4);

    FlatPoint scale = Vectors.scale(p1, 10);
    Assertions.assertEquals(6D, scale.getX(), 0);
    Assertions.assertEquals(8D, scale.getY(), 0);

    scale = Vectors.scale(p1, 0);
    Assertions.assertEquals(0D, scale.getX(), 0);
    Assertions.assertEquals(0D, scale.getY(), 0);

    scale = Vectors.scale(p1, 2.5);
    Assertions.assertEquals(1.5D, scale.getX(), 0);
    Assertions.assertEquals(2D, scale.getY(), 0);
  }

  @Test
  public void testMultiple() {
    FlatPoint p1 = new FlatPoint(8, 7);
    FlatPoint multiple = Vectors.multiple(p1, 2);

    Assertions.assertEquals(16, multiple.getX(), 0);
    Assertions.assertEquals(14, multiple.getY(), 0);

    multiple = Vectors.multiple(p1, 0);

    Assertions.assertEquals(0, multiple.getX(), 0);
    Assertions.assertEquals(0, multiple.getY(), 0);
  }

  @Test
  public void testLineInters1() throws UnfeasibleException {
    FlatPoint v1 = new FlatPoint(0, 0);
    FlatPoint v2 = new FlatPoint(1, 1);
    FlatPoint v3 = new FlatPoint(3, 1);
    FlatPoint v4 = new FlatPoint(4, 0);

    System.out.println(Vectors.lineInters(v1, v2, v3, v4));

    v1 = new FlatPoint(0, 0);
    v2 = new FlatPoint(1, 1);
    v3 = new FlatPoint(0, -1);
    v4 = new FlatPoint(1, 0);

    final FlatPoint vv1 = v1;
    final FlatPoint vv2 = v2;
    final FlatPoint vv3 = v3;
    final FlatPoint vv4 = v4;
    Assertions.assertThrows(UnfeasibleException.class, () -> Vectors.lineInters(vv1, vv2, vv3, vv4));
  }

  @Test
  public void testLineInters2() throws UnfeasibleException {
    FlatPoint v1 = new FlatPoint(0, 0);
    FlatPoint v2 = new FlatPoint(1, 1);
    FlatPoint v3 = new FlatPoint(0, -1);
    FlatPoint v4 = new FlatPoint(1, 0);

    final FlatPoint vv1 = v1;
    final FlatPoint vv2 = v2;
    final FlatPoint vv3 = v3;
    final FlatPoint vv4 = v4;
    Assertions.assertThrows(UnfeasibleException.class, () -> Vectors.lineInters(vv1, vv2, vv3, vv4));
  }

  @Test
  public void testLineInters3() throws UnfeasibleException {
    FlatPoint v1 = new FlatPoint(1, 1);
    FlatPoint v2 = new FlatPoint(2, 1);
    FlatPoint v3 = new FlatPoint(3, 1);
    FlatPoint v4 = new FlatPoint(4, 0);

    System.out.println(Vectors.lineInters(v1, v2, v3, v4));
    System.out.println(Vectors.lineInters(v3, v4, v1, v2));
  }

  @Test
  public void testLineInters4() throws UnfeasibleException {
    FlatPoint v1 = new FlatPoint(-3, 231);
    FlatPoint v2 = new FlatPoint(-3, 33);
    FlatPoint v3 = new FlatPoint(3, 1);
    FlatPoint v4 = new FlatPoint(4, 0);

    System.out.println(Vectors.lineInters(v1, v2, v3, v4));
    System.out.println(Vectors.lineInters(v3, v4, v1, v2));
  }

  @Test
  public void testLineInters5() throws UnfeasibleException {
    FlatPoint v1 = new FlatPoint(0, 0);
    FlatPoint v2 = new FlatPoint(4, 2);
    FlatPoint v3 = new FlatPoint(0, 3);
    FlatPoint v4 = new FlatPoint(3, 0);

    FlatPoint point = Vectors.lineInters(v1, v2, v3, v4);
    Assertions.assertEquals(2, point.getX(), 0);
    Assertions.assertEquals(1, point.getY(), 0);
  }

  @Test
  public void testInAngle() {
    Assertions.assertTrue(Vectors.inAngle(0, 0, 1, 1, 1, 0, 1, 0.98));
    Assertions.assertTrue(Vectors.inAngle(0, 0, 1, 1, 1, 0, 40, 39));
    Assertions.assertFalse(Vectors.inAngle(0, 0, 1, 1, 1, 0, 40, 41));
  }
}
