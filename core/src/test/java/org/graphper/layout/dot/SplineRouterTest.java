/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.layout.dot;

import java.util.Arrays;
import java.util.List;
import org.graphper.def.Curves;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SplineRouterTest {

  @Test
  public void removesOnlyForwardCollinearThroughPoints() {
    List<BoxGuideLineRouter.ThroughPoint> points = Arrays.asList(
        point(-54, 30, 0), point(-126, 110, 2), point(-126, 208, 4),
        point(-126, 309, 6), point(-108, 390, 8));
    List<BoxGuideLineRouter.ThroughPoint> simplified = SplineRouter.simplifyThroughPoints(points);
    Assertions.assertEquals(4, simplified.size());
    Assertions.assertSame(points.get(0), simplified.get(0));
    Assertions.assertSame(points.get(1), simplified.get(1));
    Assertions.assertSame(points.get(3), simplified.get(2));
    Assertions.assertSame(points.get(4), simplified.get(3));
  }

  @Test
  public void retainsCornersAndDirectionReversals() {
    List<BoxGuideLineRouter.ThroughPoint> points = Arrays.asList(
        point(0, 0, 0), point(10, 0, 1), point(10, 10, 2), point(0, 10, 3),
        point(10, 10, 4));
    Assertions.assertEquals(points, SplineRouter.simplifyThroughPoints(points));
  }

  @Test
  public void retainsObliqueNearCollinearPoints() {
    List<BoxGuideLineRouter.ThroughPoint> points = Arrays.asList(
        point(0, 0, 0), point(10, 10.05, 1), point(20, 20, 2));
    Assertions.assertEquals(points, SplineRouter.simplifyThroughPoints(points));
  }

  @Test
  public void collinearCleanupReducesWavyLongEdgeSegments() {
    List<BoxGuideLineRouter.ThroughPoint> points = Arrays.asList(
        point(-108, 390, 0), point(-260, 466, 2), point(-260, 543, 4),
        point(-260, 639, 6), point(-126, 734, 8));
    int originalSegments = Curves.fitCurves(points, 0.04).size();
    List<BoxGuideLineRouter.ThroughPoint> simplified = SplineRouter.simplifyThroughPoints(points);
    int simplifiedSegments = Curves.fitCurves(simplified, 0.04).size();

    Assertions.assertEquals(4, originalSegments);
    Assertions.assertEquals(3, simplifiedSegments);
    Assertions.assertTrue(simplifiedSegments < originalSegments);
  }

  private BoxGuideLineRouter.ThroughPoint point(double x, double y, int box) {
    return new BoxGuideLineRouter.ThroughPoint(x, y, box);
  }
}
