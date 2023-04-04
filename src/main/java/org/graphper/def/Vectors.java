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

import java.util.Objects;
import org.graphper.def.FlatPoint.UnmodifyFlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.ValueUtils;

/**
 * Calculation rules for various vectors
 *
 * @author Jamison Jiang
 */
public class Vectors {

  private Vectors() {
  }

  public static final FlatPoint ZERO = new UnmodifyFlatPoint(0, 0);

  /**
   * Returns a unit vector in the specified direction. Returns a value equal to {@link #ZERO} if x
   * is equal to y.
   *
   * @param x x-coordinate of direction vector
   * @param y y-coordinate of direction vector
   * @return unit vector of specified direction
   */
  public static FlatPoint unit(double x, double y) {
    double len = len(x, y);

    if (len != 0) {
      return new FlatPoint(x / len, y / len);
    }

    return new FlatPoint(0, 0);
  }

  /**
   * Returns a unit vector in the specified direction, the direction vector computed by two point
   * with subtraction. Returns a value equal to {@link #ZERO} if distance between two points is 0.
   *
   * @param subtrahend the subtrahend vector
   * @param minuend    the minuend vector
   * @return unit vector of specified direction
   * @throws NullPointerException subtrahend or minuend is null
   */
  public static FlatPoint unit(FlatPoint subtrahend, FlatPoint minuend) {
    Asserts.nullArgument(subtrahend, "subtrahend");
    Asserts.nullArgument(minuend, "minuend");
    double x = subtrahend.getX() - minuend.getX();
    double y = subtrahend.getY() - minuend.getY();
    return unit(x, y);
  }

  /**
   * Two-dimensional vector subtraction.
   *
   * @param subtrahend the subtrahend vector
   * @param minuend    the minuend vector
   * @return vector after subtraction
   * @throws NullPointerException subtrahend or minuend is null
   */
  public static FlatPoint sub(FlatPoint subtrahend, FlatPoint minuend) {
    Asserts.nullArgument(subtrahend, "subtrahend");
    Asserts.nullArgument(minuend, "minuend");
    return new FlatPoint(subtrahend.getX() - minuend.getX(), subtrahend.getY() - minuend.getY());
  }

  /**
   * Two-dimensional vector addition.
   *
   * @param added   the added vector
   * @param summand the summand vector
   * @return vector after addition
   * @throws NullPointerException added or summand is null
   */
  public static FlatPoint add(FlatPoint added, FlatPoint summand) {
    Asserts.nullArgument(added, "added");
    Asserts.nullArgument(summand, "summand");
    return new FlatPoint(added.getX() + summand.getX(), added.getY() + summand.getY());
  }

  /**
   * Two-dimensional vector multiplication.
   *
   * @param multiplier   multiplier
   * @param multiplicand multiplicand
   * @return vector after multiplication
   * @throws NullPointerException multiplier or multiplicand is null
   */
  public static double mul(FlatPoint multiplier, FlatPoint multiplicand) {
    Asserts.nullArgument(multiplier, "multiplier");
    Asserts.nullArgument(multiplicand, "multiplicand");
    return multiplier.getX() * multiplicand.getX() + multiplier.getY() * multiplicand.getY();
  }

  /**
   * Stretches the specified vector to a specified length.
   *
   * @param vector the vector stretch
   * @param newLen the target length of vector
   * @return vector after stretch
   * @throws NullPointerException null vector
   */
  public static FlatPoint scale(FlatPoint vector, double newLen) {
    Asserts.nullArgument(vector, "vector");
    double len = len(vector.getX(), vector.getY());

    if (len != 0) {
      return new FlatPoint(vector.getX() * newLen / len, vector.getY() * newLen / len);
    }

    return new FlatPoint(0, 0);
  }

  /**
   * Vector coordinates multiplied by a specified multiple.
   *
   * @param vector   the vector
   * @param multiple the multiple
   * @return multiplied vector
   * @throws NullPointerException null vector
   */
  public static FlatPoint multiple(FlatPoint vector, double multiple) {
    Asserts.nullArgument(vector, "vector");
    return new FlatPoint(vector.getX() * multiple, vector.getY() * multiple);
  }

  /**
   * Find the abscissa according to the linear equation and the ordinate of the solution point.
   *
   * @param p1 line point
   * @param p2 line point
   * @param y  the ordinate of the solution point
   * @return The abscissa of the solution point
   * @throws NullPointerException     the null point
   * @throws IllegalArgumentException the vertical coordinates of the two vertices are equal
   */
  public static double linerFuncGetX(FlatPoint p1, FlatPoint p2, double y) {
    Asserts.nullArgument(p1, "p1");
    Asserts.nullArgument(p2, "p2");

    return linerFuncGetX(p1.getX(), p1.getY(), p2.getX(), p2.getY(), y);
  }

  /**
   * Find the abscissa according to the linear equation and the ordinate of the solution point.
   *
   * @param startX the abscissa of the vertex of the line segment
   * @param startY the ordinate of the solution point
   * @param endX   the abscissa of the vertex of the line segment
   * @param endY   the ordinate of the solution point
   * @param y      The ordinate of the solution point
   * @return The abscissa of the solution point
   * @throws IllegalArgumentException the vertical coordinates of the two vertices are equal
   */
  public static double linerFuncGetX(double startX, double startY, double endX, double endY,
                                     double y) {
    Asserts.illegalArgument(startY == endY, "There are countless solutions to linear equations");

    if (startX == endX) {
      return startX;
    }

    double slope = (endY - startY) / (endX - startX);
    double constant = startY - startX * slope;

    return (y - constant) / slope;
  }

  /**
   * Find the vertical coordinate according to the linear equation and the horizontal coordinate of
   * the solution point.
   *
   * @param p1 line point
   * @param p2 line point*
   * @param x  The abscissa of the solution point
   * @return The ordinate of the solution point
   * @throws NullPointerException     the null point
   * @throws IllegalArgumentException the vertical coordinates of the two vertices are equal
   */
  public static double linerFuncGetY(FlatPoint p1, FlatPoint p2, double x) {
    Asserts.nullArgument(p1, "p1");
    Asserts.nullArgument(p2, "p2");

    return linerFuncGetY(p1.getX(), p1.getY(), p2.getX(), p2.getY(), x);
  }

  /**
   * Find the vertical coordinate according to the linear equation and the horizontal coordinate of
   * the solution point.
   *
   * @param startX the abscissa of the vertex of the line segment
   * @param startY the ordinate of the solution point
   * @param endX   the abscissa of the vertex of the line segment
   * @param endY   the ordinate of the solution point
   * @param x      The abscissa of the solution point
   * @return The ordinate of the solution point
   * @throws IllegalArgumentException the vertical coordinates of the two vertices are equal
   */
  public static double linerFuncGetY(double startX, double startY, double endX, double endY,
                                     double x) {
    Asserts.illegalArgument(startX == endX, "There are countless solutions to linear equations");

    if (startY == endY) {
      return startY;
    }

    double slope = (endY - startY) / (endX - startX);
    double constant = startY - startX * slope;

    return slope * x + constant;
  }

  /**
   * Solve the intersection of two line segments, that is, the common solution of the linear
   * equation.
   *
   * @param line1P1 line1 endpoint
   * @param line1P2 line1 endpoint
   * @param line2P1 line2 endpoint
   * @param line2P2 line2 endpoint
   * @return the intersection of two line segments
   * @throws IllegalArgumentException linear have empty point
   * @throws UnfeasibleException      two linear equations have no common solution
   */
  public static FlatPoint lineInters(FlatPoint line1P1, FlatPoint line1P2, FlatPoint line2P1,
                                     FlatPoint line2P2) throws UnfeasibleException {
    Asserts.illegalArgument(
        line1P1 == null || line1P2 == null || line2P1 == null || line2P2 == null,
        "The line segment description is incomplete and there are null points");
    Asserts.illegalArgument(Objects.equals(line1P1, line1P2) || Objects.equals(line2P1, line2P2),
                            "Two points must form a line segment");

    double m1 = line1P1.getX() - line1P2.getX();
    double d1 = line1P1.getY() - line1P2.getY();
    double m2 = line2P1.getX() - line2P2.getX();
    double d2 = line2P1.getY() - line2P2.getY();

    if ((m1 == 0 && m2 == 0) || (d1 == 0 && d2 == 0)) {
      throw new UnfeasibleException("No intersection between two line segments");
    }

    if (ValueUtils.approximate(m1, 0, 0.001)) {
      return new FlatPoint(line1P1.getX(), linerFuncGetY(line2P1, line2P2, line1P1.getX()));
    }

    if (ValueUtils.approximate(m2, 0, 0.001)) {
      return new FlatPoint(line2P1.getX(), linerFuncGetY(line1P1, line1P2, line2P1.getX()));
    }

    if (ValueUtils.approximate(d1, 0, 0.001)) {
      return new FlatPoint(linerFuncGetX(line2P1, line2P2, line1P1.getY()), line1P1.getY());
    }

    if (ValueUtils.approximate(d2, 0, 0.001)) {
      return new FlatPoint(linerFuncGetX(line1P1, line1P2, line2P1.getY()), line2P1.getY());
    }

    double slope1 = d1 / m1;
    double slope2 = d2 / m2;

    if (ValueUtils.approximate(slope1, slope2, 0.001)) {
      throw new UnfeasibleException("No intersection between two line segments");
    }

    double constant1 = line1P1.getY() - line1P1.getX() * slope1;
    double constant2 = line2P1.getY() - line2P1.getX() * slope2;
    double x = (constant1 - constant2) / (slope2 - slope1);

    return new FlatPoint(x, slope1 * x + constant1);
  }

  /**
   * Returns whether a point is inside a corner, which consists of a corner point and two direction
   * points.
   *
   * @param corner corner point
   * @param p1     edge vector of the corner
   * @param p2     edge vector of the corner
   * @param target detected target point
   * @return <tt>true</tt> if the point in angle
   * @throws NullPointerException have any null point
   */
  public static boolean inAngle(FlatPoint corner, FlatPoint p1, FlatPoint p2, FlatPoint target) {
    Asserts.nullArgument(corner, "corner");
    Asserts.nullArgument(p1, "edge vector point of corner");
    Asserts.nullArgument(p2, "edge vector point of corner");
    Asserts.nullArgument(target, "target");
    return inAngle(corner.getX(), corner.getY(), p1.getX(), p1.getY(),
                   p2.getX(), p2.getY(), target.getX(), target.getY());
  }

  /**
   * Returns whether a point is inside a corner, which consists of a corner point and two direction
   * points.
   *
   * @param cornerX x-coordinate of corner point
   * @param cornerY y-coordinate of corner point
   * @param p1x     x-coordinate edge vector of the corner
   * @param p1y     y-coordinate edge vector of the corner
   * @param p2x     x-coordinate edge vector of the corner
   * @param p2y     y-coordinate edge vector of the corner
   * @param testX   x-coordinate of detected point
   * @param testY   y-coordinate of detected point
   * @return <tt>true</tt> if the point in angle
   */
  public static boolean inAngle(double cornerX, double cornerY, double p1x, double p1y, double p2x,
                                double p2y, double testX, double testY) {
    return onLineDown(cornerX, cornerY, p1x, p1y, testX, testY) == onLineDown(cornerX, cornerY, p1x,
                                                                              p1y, p2x, p2y)
        && onLineDown(cornerX, cornerY, p2x, p2y, testX, testY) == onLineDown(cornerX, cornerY, p2x,
                                                                              p2y, p1x, p1y);
  }

  /**
   * Returns the squared value of a coordinate.
   *
   * @param x x-coordinate of point
   * @param y y-coordinate of point
   * @return squared value of point
   */
  public static double squaredLen(double x, double y) {
    return x * x + y * y;
  }

  /**
   * Returns the length of the vector (distance from the origin of the coordinates).
   *
   * @param x x-coordinate of vector
   * @param y y-coordinate of vector
   * @return length of vector
   */
  public static double len(double x, double y) {
    return Math.sqrt(squaredLen(x, y));
  }

  // ----------------------------------- private method -----------------------------------

  private static boolean onLineDown(double startX, double startY,
                                    double endX, double endY,
                                    double targetX, double targetY) {
    if (startX == endX && startY == endY) {
      return false;
    }

    // All node locate at the line would return false (not below the line)
    if (startX == endX) {
      return targetX > startX;
    }

    if (startY == endY) {
      return targetY < endY;
    }

    double val = linerFuncGetY(startX, startY, endX, endY, targetX);
    return val > targetY && !ValueUtils.approximate(val, targetY, 0.01);
  }
}
