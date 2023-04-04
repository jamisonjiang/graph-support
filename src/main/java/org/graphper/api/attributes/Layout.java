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

import org.graphper.layout.LayoutEngine;
import org.graphper.util.Asserts;
import org.graphper.layout.dot.DotLayoutEngine;

/**
 * Layout engine enumeration.
 *
 * @author Jamison Jiang
 */
public enum Layout {

  /**
   * Hierarchical or layered drawings of directed graphs. The layout algorithm aims edges in the
   * same direction (top to bottom, or left to right) and then attempts to avoid edge crossings and
   * reduce edge length.
   */
  DOT(new DotLayoutEngine());

  private final LayoutEngine layoutEngine;

  Layout(LayoutEngine layoutEngine) {
    Asserts.nullArgument(layoutEngine, "layoutEngine");
    this.layoutEngine = layoutEngine;
  }

  /**
   * Returns the layout engine used for layout.
   *
   * @return layout engine
   */
  public LayoutEngine getLayoutEngine() {
    return layoutEngine;
  }
}
