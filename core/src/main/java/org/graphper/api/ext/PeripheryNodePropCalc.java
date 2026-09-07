/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.api.ext;

import java.io.Serializable;
import org.graphper.def.FlatPoint;

/** Concentric node borders; the outer border clips edges and the inner border contains labels. */
public class PeripheryNodePropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 1L;
  public static final double GAP = 4;

  private final ShapePropCalc outline;
  private final int borders;

  public PeripheryNodePropCalc(boolean circle, int borders) {
    this.outline = circle ? new CirclePropCalc() : new StretchablePolygonPropCalc(8);
    this.borders = borders;
  }

  public ShapePropCalc getOutline() {
    return outline;
  }

  public int getBorders() {
    return borders;
  }

  public Box innerBox(Box box, int border) {
    double inset = Math.min(GAP, Math.min(box.getWidth(), box.getHeight())
        / (2 * borders)) * border;
    return new DefaultBox(box.getLeftBorder() + inset, box.getRightBorder() - inset,
                          box.getUpBorder() + inset, box.getDownBorder() - inset);
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return minContainerSize(innerHeight, innerWidth, 0, 0);
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth,
                                    double minHeight, double minWidth) {
    double padding = 2 * GAP * (borders - 1);
    FlatPoint size = outline.minContainerSize(innerHeight, innerWidth,
        Math.max(0, minHeight - padding), Math.max(0, minWidth - padding));
    return new FlatPoint(size.getHeight() + padding, size.getWidth() + padding);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    return outline.in(box, point);
  }

  @Override
  public void ratio(FlatPoint size) {
    outline.ratio(size);
  }
}
