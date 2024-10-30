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

package org.graphper.util;

/**
 * Some methods of numerical judgment and calculation.
 *
 * @author Jamison Jiang
 */
public class ValueUtils {

  private ValueUtils() {
  }

  /**
   * Compares two numbers within a specified precision within which two double numbers can be
   * considered approximately equal, the largest tolerance is 0.01.
   *
   * @param source    comparison double value
   * @param target    comparison double value
   * @return two values are approximately equal
   */
  public static boolean approximate(double source, double target) {
    return approximate(source, target, 0.01);
  }

  /**
   * Compares two numbers within a specified precision within which two double numbers can be
   * considered approximately equal.
   *
   * @param source    comparison double value
   * @param target    comparison double value
   * @param tolerance tolerance scope
   * @return two values are approximately equal
   */
  public static boolean approximate(double source, double target, double tolerance) {
    return Math.abs(target - source) <= tolerance;
  }

  /**
   * Returns the trigonometric tangent of an angle.  Special cases:
   * <ul><li>If the argument is NaN or an infinity, then the result
   * is NaN.
   * <li>If the argument is zero, then the result is a zero with the
   * same sign as the argument.</ul>
   *
   * <p>The computed result must be within 1 ulp of the exact result.
   * Results must be semi-monotonic.
   *
   * @param angle an angle, in degree.
   * @return the tangent of the argument.
   */
  public static double tan(double angle) {
    return Math.tan(toRadians(angle));
  }

  /**
   * Returns the trigonometric cosine of an angle. Special cases:
   * <ul><li>If the argument is NaN or an infinity, then the
   * result is NaN.</ul>
   *
   * <p>The computed result must be within 1 ulp of the exact result.
   * Results must be semi-monotonic.
   *
   * @param angle an angle, in degree.
   * @return the cosine of the argument.
   */
  public static double cos(double angle) {
    return Math.cos(toRadians(angle));
  }

  /**
   * Returns the trigonometric sine of an angle.  Special cases:
   * <ul><li>If the argument is NaN or an infinity, then the
   * result is NaN.
   * <li>If the argument is zero, then the result is a zero with the
   * same sign as the argument.</ul>
   *
   * <p>The computed result must be within 1 ulp of the exact result.
   * Results must be semi-monotonic.
   *
   * @param angle an angle, in degree.
   * @return the sine of the argument.
   */
  public static double sin(double angle) {
    return Math.sin(toRadians(angle));
  }

  private static double toRadians(double angle) {
    return Math.toRadians(angle);
  }
}
