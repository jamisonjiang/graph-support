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

import java.awt.geom.Path2D;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.def.FlatPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StarPropCalcTest {

  /**
   * Independent oracle: the polygon the renderer actually draws for a star of this box, built
   * straight from {@code NodeShapeEditor#start}'s formula and evaluated by AWT rather than by the
   * code under test.
   */
  private static Path2D.Double renderedOutline(Box box) {
    double outerRadiusX = box.getWidth() / 2;
    double outerRadiusY = box.getHeight() / 2;
    double innerRadiusX = outerRadiusX / StarPropCalc.IN_OUT_RATIO;
    double innerRadiusY = outerRadiusY / StarPropCalc.IN_OUT_RATIO;

    Path2D.Double path = new Path2D.Double();
    double arc = StarPropCalc.START_ARC;
    for (int i = 0; i < 10; i++) {
      double radiusX = i % 2 == 0 ? outerRadiusX : innerRadiusX;
      double radiusY = i % 2 == 0 ? outerRadiusY : innerRadiusY;
      double x = box.getX() + Math.cos(arc) * radiusX;
      double y = box.getY() - Math.sin(arc) * radiusY;
      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
      arc += StarPropCalc.UNIT_ARC;
    }
    path.closePath();
    return path;
  }

  private static DefaultBox centeredBox(FlatPoint size) {
    return new DefaultBox(-size.getWidth() / 2, size.getWidth() / 2,
                          -size.getHeight() / 2, size.getHeight() / 2);
  }

  /**
   * Asserts that every point of the inner rectangle border is inside both the shape predicate and
   * the rendered outline. The step is far finer than the 1/32 grid the sampled implementation used,
   * which is what lets it see the band a notch cuts out of a rectangle edge.
   */
  private static void assertCoversInnerRectangle(FlatPoint size, double innerHeight,
                                                 double innerWidth) {
    StarPropCalc calc = new StarPropCalc();
    DefaultBox box = centeredBox(size);
    Path2D.Double outline = renderedOutline(box);

    int steps = 4000;
    for (int i = 0; i <= steps; i++) {
      double ratio = i / (double) steps;
      double x = -innerWidth / 2 + innerWidth * ratio;
      double y = -innerHeight / 2 + innerHeight * ratio;
      double[][] border = {{x, -innerHeight / 2}, {x, innerHeight / 2},
                           {-innerWidth / 2, y}, {innerWidth / 2, y}};
      for (double[] point : border) {
        Assertions.assertTrue(calc.in(box, new FlatPoint(point[0], point[1])),
                              () -> "shape reports (" + point[0] + ", " + point[1]
                                  + ") outside star " + size);
        Assertions.assertTrue(outline.contains(point[0], point[1]),
                              () -> "rendered outline excludes (" + point[0] + ", " + point[1]
                                  + ") of star " + size);
      }
    }
  }

  @Test
  public void twoArgumentContainerCoversInnerRectangle() {
    double innerHeight = 20;
    double innerWidth = 50;
    FlatPoint size = new StarPropCalc().minContainerSize(innerHeight, innerWidth);

    // The two lower corners of the inner rectangle: outside the star before the fix.
    StarPropCalc calc = new StarPropCalc();
    DefaultBox box = centeredBox(size);
    Assertions.assertTrue(calc.in(box, new FlatPoint(25, 10)));
    Assertions.assertTrue(calc.in(box, new FlatPoint(-25, 10)));

    assertCoversInnerRectangle(size, innerHeight, innerWidth);
  }

  @Test
  public void fourArgumentContainerCoversNotchBand() {
    double innerHeight = 20;
    double innerWidth = 50;
    FlatPoint size = NodeShapeEnum.STAR.minContainerSize(innerHeight, innerWidth, 100, 60);
    Assertions.assertTrue(size.getHeight() >= 100);
    Assertions.assertTrue(size.getWidth() >= 60);

    /*
     * (25, 7.7) sits on the right edge of the inner rectangle inside the band the inward notch cut
     * out of the previously returned container. The 1/32 sampling grid stepped over that band.
     */
    StarPropCalc calc = new StarPropCalc();
    DefaultBox box = centeredBox(size);
    Assertions.assertTrue(calc.in(box, new FlatPoint(25, 7.7)));
    Assertions.assertTrue(calc.in(box, new FlatPoint(-25, 7.7)));

    assertCoversInnerRectangle(size, innerHeight, innerWidth);
  }

  @Test
  public void containerIsNotGrownBeyondWhatContainmentNeeds() {
    double innerHeight = 20;
    double innerWidth = 50;
    FlatPoint size = new StarPropCalc().minContainerSize(innerHeight, innerWidth);

    // Shrinking the returned container by a hair must break containment, otherwise the search
    // overshot.
    FlatPoint shrunk = new FlatPoint(size.getHeight() * 0.9999, size.getWidth() * 0.9999);
    DefaultBox box = centeredBox(shrunk);
    Path2D.Double outline = renderedOutline(box);
    boolean covered = true;
    int steps = 4000;
    for (int i = 0; i <= steps && covered; i++) {
      double y = -innerHeight / 2 + innerHeight * i / (double) steps;
      covered = outline.contains(innerWidth / 2, y) && outline.contains(-innerWidth / 2, y);
    }
    Assertions.assertFalse(covered, "returned container is larger than containment requires");
  }

  @Test
  public void containmentAgreesWithRenderedOutline() {
    FlatPoint size = new StarPropCalc().minContainerSize(20, 50);
    StarPropCalc calc = new StarPropCalc();
    DefaultBox box = centeredBox(size);
    Path2D.Double outline = renderedOutline(box);

    /*
     * The predicate counts the border itself as inside so that a point placed exactly on the
     * outline - a compass port, for instance - is not reported outside. AWT's contains() excludes
     * the border, so the only allowed disagreements are points on it.
     */
    double borderTolerance = 1e-9 * Math.max(box.getHeight(), box.getWidth());
    int steps = 200;
    for (int i = 0; i <= steps; i++) {
      for (int j = 0; j <= steps; j++) {
        double x = box.getLeftBorder() + box.getWidth() * i / (double) steps;
        double y = box.getUpBorder() + box.getHeight() * j / (double) steps;
        if (calc.in(box, new FlatPoint(x, y)) == outline.contains(x, y)) {
          continue;
        }
        double distance = distanceToOutline(outline, x, y);
        Assertions.assertTrue(distance <= borderTolerance,
                              "shape predicate and rendered outline disagree at (" + x + ", " + y
                                  + "), " + distance + " away from the outline");
      }
    }
  }

  private static double distanceToOutline(Path2D.Double outline, double x, double y) {
    java.awt.geom.PathIterator iterator = outline.getPathIterator(null);
    double[] coords = new double[6];
    double preX = 0;
    double preY = 0;
    double startX = 0;
    double startY = 0;
    double best = Double.MAX_VALUE;
    while (!iterator.isDone()) {
      int type = iterator.currentSegment(coords);
      if (type == java.awt.geom.PathIterator.SEG_MOVETO) {
        preX = startX = coords[0];
        preY = startY = coords[1];
      } else if (type == java.awt.geom.PathIterator.SEG_LINETO) {
        best = Math.min(best, distanceToSegment(preX, preY, coords[0], coords[1], x, y));
        preX = coords[0];
        preY = coords[1];
      } else if (type == java.awt.geom.PathIterator.SEG_CLOSE) {
        best = Math.min(best, distanceToSegment(preX, preY, startX, startY, x, y));
        preX = startX;
        preY = startY;
      }
      iterator.next();
    }
    return best;
  }

  private static double distanceToSegment(double x1, double y1, double x2, double y2,
                                          double x, double y) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double squaredLen = dx * dx + dy * dy;
    double ratio = squaredLen == 0
        ? 0
        : Math.max(0, Math.min(1, ((x - x1) * dx + (y - y1) * dy) / squaredLen));
    return Math.hypot(x - (x1 + ratio * dx), y - (y1 + ratio * dy));
  }

  @Test
  public void notchGapIsOutsideAndTipsAreInside() {
    FlatPoint size = new StarPropCalc().minContainerSize(20, 20);
    StarPropCalc calc = new StarPropCalc();
    DefaultBox box = centeredBox(size);

    Assertions.assertTrue(calc.in(box, new FlatPoint(0, 0)));

    double outerRadiusX = box.getWidth() / 2;
    double outerRadiusY = box.getHeight() / 2;
    double arc = StarPropCalc.START_ARC;
    for (int i = 0; i < 10; i++) {
      double x = Math.cos(arc);
      double y = -Math.sin(arc);
      if (i % 2 == 0) {
        // On a tip, and just past it.
        Assertions.assertTrue(calc.in(box, new FlatPoint(x * outerRadiusX, y * outerRadiusY)));
        Assertions.assertFalse(calc.in(box, new FlatPoint(x * outerRadiusX * 1.01,
                                                          y * outerRadiusY * 1.01)));
      } else {
        // Along a notch axis the outline stops at 1 / IN_OUT_RATIO of the box.
        Assertions.assertFalse(calc.in(box, new FlatPoint(x * outerRadiusX * 0.9,
                                                          y * outerRadiusY * 0.9)));
      }
      arc += StarPropCalc.UNIT_ARC;
    }
  }

  @Test
  public void degenerateInnerRectangleTerminates() {
    StarPropCalc calc = new StarPropCalc();
    FlatPoint empty = calc.minContainerSize(0, 0);
    Assertions.assertEquals(0, empty.getHeight(), 0.000001);
    Assertions.assertEquals(0, empty.getWidth(), 0.000001);

    FlatPoint flat = calc.minContainerSize(0, 50);
    Assertions.assertTrue(flat.getHeight() > 0);
    Assertions.assertTrue(flat.getWidth() > 0);
    assertCoversInnerRectangle(flat, 0, 50);

    FlatPoint thin = calc.minContainerSize(20, 0);
    assertCoversInnerRectangle(thin, 20, 0);
  }

  @Test
  public void minimumsAreHonouredAndStillCover() {
    double innerHeight = 27;
    double innerWidth = 63;
    FlatPoint size = new StarPropCalc().minContainerSize(innerHeight, innerWidth, 400, 500);
    Assertions.assertTrue(size.getHeight() >= 400);
    Assertions.assertTrue(size.getWidth() >= 500);
    assertCoversInnerRectangle(size, innerHeight, innerWidth);
  }
}
