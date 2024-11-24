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
import org.graphper.layout.dot.DotLayoutEngine;
import org.graphper.layout.fdp.FdpLayoutEngine;
import org.graphper.layout.fdp.GFdpLayoutEngine;
import org.graphper.layout.fdp.JFdpLayoutEngine;
import org.graphper.util.Asserts;

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
  DOT(new DotLayoutEngine()),

  /**
   * A standard force-directed placement (FDP) layout engine.
   */
  FDP(new FdpLayoutEngine()),

  /**
   * A specialized force-directed placement (JFDP) layout engine.
   * <ul>
   *   <li>Reduced node displacement for better stability.</li>
   *   <li>Degree-based scaling of attractive forces.</li>
   *   <li>Efficient repulsive force calculation to avoid overlaps.</li>
   *   <li>Bounding node positions within the graph area.</li>
   * </ul>
   */
  JFDP(new JFdpLayoutEngine()),

  /**
   * A specialized force-directed placement (GFDP) layout engine.
   * <p>
   * This algorithm is another variant of FDP that focuses on localized node interactions,
   * considering only the forces within the cell area of a node. This optimization reduces
   * computational complexity and is particularly effective for dense graphs with limited
   * interaction ranges.
   * </p>
   */
  GFDP(new GFdpLayoutEngine());

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
