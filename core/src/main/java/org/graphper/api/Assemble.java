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

package org.graphper.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * An aggregator that manually sets the position of the cell draws to the specified position of the
 * graph by specifying the size of a parent container, and then manually setting the offset position
 * of the cell relative to the container. The position of the specified parent container in the
 * graph is generally each The position of the label of this element. It works as follows:
 * <ul>
 *   <li>Manually set a size for the container containing the label;
 *   <li>The algorithm will automatically set a position for this label container,
 *   which is the position of various labels in graph;
 *   <li>Then use this label container as a reference to set the coordinates of the node;
 * </ul>
 *
 * @author Jamison Jiang
 */
public class Assemble implements Serializable {

  private static final long serialVersionUID = 8476339759307801893L;

  private Assemble() {
  }

  private double width;

  private double height;

  private Map<Node, CellPos> cells;

  /**
   * Returns the width.
   *
   * @return width
   */
  public double getWidth() {
    return width;
  }

  /**
   * Returns the height.
   *
   * @return height
   */
  public double getHeight() {
    return height;
  }

  /**
   * Returns all cells in this assembler.
   *
   * @return all cells
   */
  public Set<Node> getCells() {
    return cells.keySet();
  }

  /**
   * Returns the horizontal offset of the cell relative to the current assembler.
   *
   * @param cell cell to be detected
   * @return the horizontal offset of the cell
   * @throws IllegalArgumentException cell is not in the current assembler
   */
  public double horOffset(Node cell) {
    CellPos cellPos = cells.get(cell);
    Asserts.illegalArgument(cellPos == null, "Can not found cell offset info");
    return cellPos.horOffset;
  }

  /**
   * Returns the vertical offset of the cell relative to the current assembler.
   *
   * @param cell cell to be detected
   * @return the vertical offset of the cell
   * @throws IllegalArgumentException cell is not in the current assembler
   */
  public double verOffset(Node cell) {
    CellPos cellPos = cells.get(cell);
    Asserts.illegalArgument(cellPos == null, "Can not found cell offset info");
    return cellPos.verOffset;
  }

  /**
   * Returns the size of current assembler.
   *
   * @return size of current assembler
   */
  public FlatPoint size() {
    return new FlatPoint(height, width);
  }

  /**
   * Returns a {@link AssembleBuilder}.
   *
   * @return {@code AssembleBuilder}
   */
  public static AssembleBuilder builder() {
    return new AssembleBuilder();
  }

  /**
   * {@link Assemble} builder, used to build a {@link Assemble}.
   */
  public static class AssembleBuilder {

    /**
     * Root box width
     */
    private double width;

    /**
     * Root box height
     */
    private double height;

    /**
     * All cells in root container
     */
    private volatile LinkedHashMap<Node, CellPos> cells;

    private AssembleBuilder() {
    }

    /**
     * Set the width of the Assembler, this width is used as an absolute width instead of the
     * default minimum width like other elements.
     *
     * @param width assembler width
     * @return assemble builder
     * @throws IllegalArgumentException width less than 0
     */
    public AssembleBuilder width(double width) {
      Asserts.illegalArgument(width <= 0, "Width " + width + " must greater than 0");
      this.width = width * Graphviz.PIXEL;
      return this;
    }

    /**
     * Set the height of the Assembler, this height is used as an absolute height instead of the
     * default minimum height like other elements.
     *
     * @param height assembler height
     * @return assemble builder
     * @throws IllegalArgumentException height less than 0
     */
    public AssembleBuilder height(double height) {
      Asserts.illegalArgument(height <= 0, "Height " + height + " must greater than 0");
      this.height = height * Graphviz.PIXEL;
      return this;
    }

    /**
     * Add a cell based on the position of assemble, and set an offset for this cell relative to the
     * position of the upper left corner of assemble.
     *
     * @param horOffset horizontal offset of cell
     * @param verOffset vertical offset of cell
     * @param cell      The cell that is set to enter assemble
     * @return assemble builder
     * @throws NullPointerException set null cell
     */
    public synchronized AssembleBuilder addCell(double horOffset, double verOffset, Node cell) {
      Asserts.nullArgument(cell, "Cell");
      if (cells == null) {
        cells = new LinkedHashMap<>();
      }
      cells.put(cell, new CellPos(horOffset, verOffset));
      return this;
    }

    /**
     * Returns a {@link Assemble}.
     *
     * @return {@code Assemble}
     */
    public synchronized Assemble build() {
      Assemble assemble = new Assemble();
      assemble.width = this.width;
      assemble.height = this.height;
      if (cells != null) {
        assemble.cells = new LinkedHashMap<>(cells);
      } else {
        assemble.cells = Collections.emptyMap();
      }
      return assemble;
    }
  }

  private static class CellPos implements Serializable {

    private static final long serialVersionUID = -4804439305833411858L;

    private final double horOffset;

    private final double verOffset;

    public CellPos(double horOffset, double verOffset) {
      this.horOffset = horOffset * Graphviz.PIXEL;
      this.verOffset = verOffset * Graphviz.PIXEL;
    }
  }
}
