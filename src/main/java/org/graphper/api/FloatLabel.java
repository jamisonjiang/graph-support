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
import java.util.Objects;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.Tend;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * The floating label object of the Line. Uses two parameters to describe the position of the label
 * relative to the line:
 * <ul>
 *   <li>First use the parameter {@link #getLengthRatio()} to locate a position of the current line,
 *   0 indicates the position closest to the tail node, 1 indicates the position closest to the head node;
 *   <li>Secondly, if {@link #getTend()} is set, the set {@link #getLengthRatio()} attribute will be ignored,
 *   and the program will automatically select the appropriate point according to the corresponding value.
 *   <li>Finally, manually adjust an offset according to the obtained node by {@link #getOffset()}.
 *   The horizontal and vertical offset ratios corresponding to this offset are based on the size of the current label size.
 * </ul>
 *
 * @author Jamison Jiang
 */
public class FloatLabel implements Serializable {

  private static final long serialVersionUID = 8788129136334958892L;

  /**
   * The floating label
   */
  private final String label;

  private final Tend tend;

  private final Table table;

  private final Assemble assemble;

  /**
   * Font size of label
   */
  private final float fontSize;

  /**
   * The position is described based on the length of the line.
   */
  private final double lengthRatio;

  /**
   * The offset position of the point from the specified line.
   */
  private final FlatPoint offset;

  private FloatLabel(String label, float fontSize, double lengthRatio, Tend tend,
                     FlatPoint offset, Table table, Assemble assemble) {
    Asserts.illegalArgument(label == null && table == null && assemble == null,
                            "Empty Float Label");
    Asserts.illegalArgument(fontSize < 0, "Float label can not less than 0");
    this.label = label;
    this.fontSize = fontSize;
    this.lengthRatio = lengthRatio;
    this.tend = tend;
    this.offset = offset;
    this.table = table;
    this.assemble = assemble;
  }

  /**
   * Returns the floating label.
   *
   * @return the floating label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns font size of label.
   *
   * @return font size of label
   */
  public float getFontSize() {
    return fontSize;
  }

  /**
   * Returns the placement tendency of FloatLabel.
   *
   * @return the placement tendency of FloatLabel
   */
  public Tend getTend() {
    return tend;
  }

  /**
   * Returns the position is described based on the length of the line.
   *
   * @return the position is described based on the length of the line
   */
  public double getLengthRatio() {
    return lengthRatio;
  }

  /**
   * Returns the offset position of the point from the specified line.
   *
   * @return the offset position of the point from the specified line
   */
  public FlatPoint getOffset() {
    return offset;
  }

  /**
   * Returns the table of float label.
   *
   * @return float table
   */
  public Table getTable() {
    return table;
  }

  /**
   * Returns the assembler of float label.
   *
   * @return float assemble
   */
  public Assemble getAssemble() {
    return assemble;
  }

  public boolean ignoreTextLabel() {
    return table != null || assemble != null;
  }

  /**
   * Returns a float label builder.
   *
   * @return {@code FloatLabelBuilder}
   */
  public static FloatLabelBuilder builder() {
    return new FloatLabelBuilder();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FloatLabel that = (FloatLabel) o;
    return Float.compare(that.fontSize, fontSize) == 0
        && Double.compare(that.lengthRatio, lengthRatio) == 0
        && Objects.equals(label, that.label)
        && Objects.equals(table, that.table)
        && Objects.equals(assemble, that.assemble)
        && Objects.equals(offset, that.offset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, table, assemble, fontSize, lengthRatio, offset);
  }

  // ------------------------------------------ FloatLabel Builder ---------------------------------------

  /**
   * Float label builder
   */
  public static class FloatLabelBuilder {

    private String label;

    private float fontSize = 14;

    private Tend tend;

    private double lengthRatio;

    private FlatPoint offset;


    private Table table;

    private Assemble assemble;

    private FloatLabelBuilder() {
    }

    /**
     * Set the floating label
     *
     * @param label floating label
     * @return float label builder
     * @throws NullPointerException null label
     */
    public FloatLabelBuilder label(String label) {
      Asserts.nullArgument(label, "floatLabel");
      this.label = label;
      return this;
    }

    /**
     * Set the font size of label.
     *
     * @param fontSize font size of label
     * @return float label builder
     * @throws IllegalArgumentException the font size less than 0
     */
    public FloatLabelBuilder fontSize(float fontSize) {
      Asserts.illegalArgument(fontSize < 0, "fontSize (" + fontSize + ") can not less than 0");
      this.fontSize = fontSize;
      return this;
    }

    /**
     * Set the position is described based on the length of the line.
     *
     * @param lengthRatio the position is described based on the length of the line.
     * @return float label builder
     */
    public FloatLabelBuilder lengthRatio(double lengthRatio) {
      this.lengthRatio = lengthRatio;
      return this;
    }

    /**
     * Set an offset, the horizontal and vertical offset ratio corresponding to this offset is based
     * on the size of the current label size.
     *
     * @param sizeRatio height and width ratio
     * @return float label builder
     */
    public FloatLabelBuilder offset(double sizeRatio) {
      return offset(sizeRatio, sizeRatio);
    }

    /**
     * Set an offset, the horizontal and vertical offset ratio corresponding to this offset is based
     * on the size of the current label size.
     *
     * @param heightRatio height ratio
     * @param widthRatio  width ratio
     * @return float label builder
     */
    public FloatLabelBuilder offset(double heightRatio, double widthRatio) {
      this.offset = new FlatPoint(heightRatio, widthRatio);
      return this;
    }

    /**
     * Set the placement tendency of {@link FloatLabel}.
     *
     * @param tend placement tendency of label
     * @return float label builder
     */
    public FloatLabelBuilder tend(Tend tend) {
      this.tend = tend;
      return this;
    }

    /**
     * Set a Table similar to the HTML structure to replace the {@link #label(String)}, and the
     * generated {@link Table} will be in the position of the label.
     *
     * @param table table
     * @return float label builder
     */
    public FloatLabelBuilder table(Table table) {
      this.table = table;
      return this;
    }

    /**
     * Set an {@link Assemble} to replace the {@link #label(String)}. When setting a label for a
     * line, the program will calculate the size of the label, and then automatically put the label
     * in the appropriate position of the line. If {@link Assemble} is set, assemble will be placed
     * where the label was originally placed.
     *
     * <p>{@link Assemble} will be used as a common parent container, and all other cells set are
     * placed based on {@link Assemble}, so when adding a cell, an offset position based on
     * {@link Assemble} will be set, and the position of {@link Assemble} is where the label should
     * be.Therefore, {@link Assemble} does not provide automatic layout and cell size calculation
     * (by default, it does not automatically calculate the size of the cell according to the label
     * of the cell), which requires the setter to completely accurate calculation of all
     * parameters.
     *
     * @param assemble assemble
     * @return float label builder
     */
    public FloatLabelBuilder assemble(Assemble assemble) {
      this.assemble = assemble;
      return this;
    }

    /**
     * Returns a {@link FloatLabel}.
     *
     * @return {@code FloatLabel}
     * @throws NullPointerException not set the label
     */
    public FloatLabel build() {
      return new FloatLabel(label, fontSize, lengthRatio, tend, offset, table, assemble);
    }
  }
}
