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

/**
 * Enumeration representing the initialization position algorithms for
 * Force-Directed Placement (FDP) graph layout.
 * <p>
 * These algorithms determine the initial positions of nodes in the layout.
 * </p>
 *
 * @author Jamison Jiang
 */
public enum InitPos {

  /**
   * Initializes nodes in a grid pattern.
   */
  GRID,

  /**
   * Arranges nodes in a circular pattern.
   */
  CIRCLE,

  /**
   * Distributes nodes in sectors.
   */
  SECTOR;
}
