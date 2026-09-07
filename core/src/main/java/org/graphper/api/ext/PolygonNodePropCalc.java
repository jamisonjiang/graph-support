/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.api.ext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.graphper.def.FlatPoint;

/** Node outlines expressed in unit-box coordinates, shared by clipping and SVG rendering. */
public class PolygonNodePropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 1L;

  public enum Type {
    SQUARE, HOUSE, INVHOUSE, M_DIAMOND, M_SQUARE, TAB, FOLDER, BOX3D, COMPONENT
  }

  private final Type type;

  public PolygonNodePropCalc(Type type) {
    this.type = type;
  }

  public List<FlatPoint> calcPoints(Box box) {
    double[] coordinates;
    switch (type) {
      case HOUSE:
      case INVHOUSE:
        coordinates = new double[]{0, .25, .5, 0, 1, .25, 1, 1, 0, 1};
        break;
      case M_DIAMOND:
        coordinates = new double[]{0, .5, .5, 0, 1, .5, .5, 1};
        break;
      case TAB:
        coordinates = new double[]{0, 0, .35, 0, .45, .15, 1, .15, 1, 1, 0, 1};
        break;
      case FOLDER:
        coordinates = new double[]{0, 0, .35, 0, .4, .15, 1, .15, 1, 1, 0, 1};
        break;
      case BOX3D:
        coordinates = new double[]{0, .15, .15, 0, 1, 0, 1, .85, .85, 1, 0, 1};
        break;
      case COMPONENT:
        // Simplified UML component: inset terminals, convex outer box. Projecting tabs can
        // make a center ray leave and re-enter the silhouette, invalidating binary clipping.
        coordinates = new double[]{0, 0, 1, 0, 1, 1, 0, 1};
        break;
      default:
        coordinates = new double[]{0, 0, 1, 0, 1, 1, 0, 1};
        break;
    }
    List<FlatPoint> points = new ArrayList<>(coordinates.length / 2);
    for (int i = 0; i < coordinates.length; i += 2) {
      double y = type == Type.INVHOUSE ? 1 - coordinates[i + 1] : coordinates[i + 1];
      points.add(new FlatPoint(box.getLeftBorder() + coordinates[i] * box.getWidth(),
                               box.getUpBorder() + y * box.getHeight()));
    }
    return points;
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    // Centered safe rectangles avoid roofs, tabs, corner marks and component terminals.
    double width = 1;
    double height = 1;
    switch (type) {
      case HOUSE:
      case INVHOUSE:
        width = .8;
        height = .6;
        break;
      case M_DIAMOND:
        width = height = .45;
        break;
      case M_SQUARE:
        width = height = .7;
        break;
      case TAB:
      case FOLDER:
        width = .9;
        height = .65;
        break;
      case BOX3D:
        width = height = .65;
        break;
      case COMPONENT:
        width = .35;
        height = .9;
        break;
      default:
        break;
    }
    return new FlatPoint(innerHeight / height, innerWidth / width);
  }

  @Override
  public void ratio(FlatPoint size) {
    if (type == Type.SQUARE || type == Type.M_SQUARE) {
      squareRatio(size);
    }
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    List<FlatPoint> points = calcPoints(box);
    boolean inside = false;
    for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
      FlatPoint a = points.get(j);
      FlatPoint b = points.get(i);
      double cross = (point.getX() - a.getX()) * (b.getY() - a.getY())
          - (point.getY() - a.getY()) * (b.getX() - a.getX());
      if (Math.abs(cross) <= 1e-9
          && point.getX() >= Math.min(a.getX(), b.getX()) - 1e-9
          && point.getX() <= Math.max(a.getX(), b.getX()) + 1e-9
          && point.getY() >= Math.min(a.getY(), b.getY()) - 1e-9
          && point.getY() <= Math.max(a.getY(), b.getY()) + 1e-9) {
        return true;
      }
      if ((a.getY() > point.getY()) != (b.getY() > point.getY())
          && point.getX() < (b.getX() - a.getX()) * (point.getY() - a.getY())
          / (b.getY() - a.getY()) + a.getX()) {
        inside = !inside;
      }
    }
    return inside;
  }
}
