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

package org.graphper.api.attributes;

import org.apache_gs.commons.lang3.StringUtils;

/**
 * Sets direction of graph layout, only valid for {@link Layout#DOT} and {@link Layout#DOTQ}.
 *
 * @author Jamison Jiang
 */
public enum Rankdir {

  /**
   * Horizontal layout, left to right
   */
  LR,

  /**
   * Horizontal layout, right to left
   */
  RL,

  /**
   * Vertical layout, top to bottom
   */
  TB,

  /**
   * Vertical layout, bottom to top
   */
  BT;

  public static Rankdir rankdir(String rankdir) {
    if (StringUtils.isEmpty(rankdir)) {
      return Rankdir.TB;
    }

    for (Rankdir value : values()) {
      if (value.name().equalsIgnoreCase(rankdir)) {
        return value;
      }
    }
    return Rankdir.TB;
  }
}