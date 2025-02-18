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

package org.graphper;

/**
 * A utility class to parse and manage command-line arguments. This class helps navigate through an
 * array of arguments, providing functionality to access the current argument, the next argument,
 * and advancing through the arguments.
 *
 * @author Jamison Jiang
 */
public class Arguments {

  private int currentIdx;
  private final String[] args;

  /**
   * Constructs an Arguments object using the provided arguments array.
   *
   * @param args The array of arguments to be parsed.
   */
  public Arguments(String[] args) {
    this.args = args;
  }

  /**
   * Returns the current argument in the argument array.
   *
   * @return The current argument, or null if there is no current argument.
   */
  public String current() {
    return currentExist() ? args[currentIdx] : null;
  }

  /**
   * Returns the next argument in the argument array.
   *
   * @return The next argument, or null if there is no next argument.
   */
  public String next() {
    return nextExist() ? args[currentIdx + 1] : null;
  }

  /**
   * Advances to the next argument in the array.
   *
   * @return The argument after advancing, or null if there are no more arguments.
   */
  public String advance() {
    if (nextExist()) {
      return args[++currentIdx];
    }
    if (currentExist()) {
      currentIdx++;
    }
    return null;
  }

  /**
   * Checks if the current argument exists in the array.
   *
   * @return true if the current argument exists, false otherwise.
   */
  public boolean currentExist() {
    return args != null && currentIdx <= args.length - 1;
  }

  /**
   * Checks if the next argument exists in the array.
   *
   * @return true if the next argument exists, false otherwise.
   */
  private boolean nextExist() {
    return args != null && currentIdx < args.length - 1;
  }
}