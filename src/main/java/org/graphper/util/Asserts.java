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

import java.util.Objects;

/**
 * A utility class for common assertion checks to validate method parameters and ensure invariants.
 *
 * @author Jamison Jiang
 */
public final class Asserts {

  private Asserts() {
    // Utility class, no instances allowed
  }

  /**
   * Validates a condition and throws an {@link IllegalArgumentException} if the condition is not
   * met.
   *
   * @param predicate the condition to validate
   * @param errorMsg  the error message to include in the exception if the condition is not met
   * @throws IllegalArgumentException if {@code predicate} is {@code false}
   */
  public static void illegalArgument(boolean predicate, String errorMsg) {
    if (predicate) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  /**
   * Validates that the specified object is not {@code null}.
   *
   * @param obj the object to check
   * @throws NullPointerException if {@code obj} is {@code null}
   */
  public static void nullArgument(Object obj) {
    Objects.requireNonNull(obj);
  }

  /**
   * Validates that the specified object is not {@code null}, throwing a
   * {@link NullPointerException} with a customized error message if the check fails.
   *
   * @param obj       the object to check
   * @param paramName the name of the parameter to include in the exception message
   * @throws NullPointerException if {@code obj} is {@code null}
   */
  public static void nullArgument(Object obj, String paramName) {
    Objects.requireNonNull(obj, paramName + " can not be null");
  }

  /**
   * Validates that the specified array is not {@code null}, not empty, and does not contain any
   * {@code null} elements.
   *
   * @param arrays the array to check
   * @param <T>    the type of the elements in the array
   * @throws IllegalArgumentException if the array is {@code null}, empty, or contains {@code null}
   *                                  elements
   */
  @SafeVarargs
  public static <T> void nullOrContainsNull(T... arrays) {
    illegalArgument(arrays == null || arrays.length == 0, "Array is empty");
    for (T ele : arrays) {
      illegalArgument(ele == null, "Element is null");
    }
  }
}
