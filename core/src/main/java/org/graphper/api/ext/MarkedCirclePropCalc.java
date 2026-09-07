/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.api.ext;

import org.graphper.def.FlatPoint;

/** Circle with horizontal chords outside the centered label rectangle. */
public class MarkedCirclePropCalc extends CirclePropCalc {

  private static final long serialVersionUID = 1L;

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    FlatPoint size = super.minContainerSize(innerHeight, innerWidth);
    double diameter = Math.max(size.getHeight(), innerHeight / .7);
    return new FlatPoint(diameter, diameter);
  }
}
