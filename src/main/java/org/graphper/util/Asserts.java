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

public final class Asserts {

  private Asserts() {
  }

  /**
   * When the condition is not met and the {@code predicate} is <tt>false</tt>, use {@code errorMsg}
   * as the error message and throw {@link IllegalArgumentException} error.
   *
   * @param predicate condition to be checked
   * @param errorMsg  error message when parameters are abnormal
   * @throws IllegalArgumentException predicate is false
   */
  public static void illegalArgument(boolean predicate, String errorMsg) {
    if (predicate) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  /**
   * When the specified parameter is <tt>null</tt>, a {@link NullPointerException} is thrown with
   * the specified parameter name.
   *
   * @param obj       condition to be checked
   * @param paramName error message when parameters are abnormal
   * @throws IllegalArgumentException predicate is false
   */
  public static void nullArgument(Object obj, String paramName) {
    Objects.requireNonNull(obj, paramName + " can not be null");
  }
}
