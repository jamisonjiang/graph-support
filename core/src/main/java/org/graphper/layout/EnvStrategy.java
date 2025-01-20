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

package org.graphper.layout;

/**
 * When there are different dependencies in the environment, use different implementations to ensure
 * the implementation of certain functions follows a degradation strategy.
 *
 * @author Jamison Jiang
 */
public interface EnvStrategy {

  /**
   * When there are multiple available implementations, use this attribute to sort, and the one with
   * the smaller value will be used first, and if they are the same, one will be randomly selected
   * for use.
   *
   * @return priority of the current env strategy
   */
  int order();

  /**
   * Returns whether the current environment supports the corresponding strategy.
   *
   * @return <tt>true</tt> if current environment support
   */
  boolean envSupport();
}
