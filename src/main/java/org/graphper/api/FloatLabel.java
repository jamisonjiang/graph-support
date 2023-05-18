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
import org.graphper.util.Asserts;

/**
 * The floating label object of the Line. Uses two parameters to describe the position of the label
 * relative to the line:
 * <ul>
 *   <li>First use the parameter {@link #getLengthRatio()} to locate a position of the current line,
 *   0 indicates the position closest to the tail node, 1 indicates the position closest to the head node;
 *   <li>Secondly, use the parameter {@link #getDistRatio()} to describe the distance of the current
 *   label relative to this point. This movement direction is always perpendicular to the hierarchical
 *   direction of the two nodes.
 * </ul>
 *
 * <p>The following is a diagram showing a line pointing from <tt>C</tt> to <tt>A</tt>, where
 * point <tt>B</tt> is located in the middle of line(<tt>C</tt>, <tt>A</tt>), point <tt>D</tt>
 * is a point that has the same vertical coordinate as <tt>C</tt>, but has moved a certain
 * distance laterally, using The description of the corresponding {@link FloatLabel} describes
 * the positions of these four points:
 * <pre>
 *   A(lengthRatio=1, distRatio=0)
 *   ^
 *   |
 *   |
 *   |B(lengthRatio=0.5, distRatio=0)
 *   |
 *   |
 *   C(lengthRatio=0, distRatio=0)         D(lengthRatio=0, distRatio=10)
 * </pre>
 *
 * @author Jamison Jiang
 */
public class FloatLabel implements Serializable {

  private static final long serialVersionUID = 8788129136334958892L;

  /**
   * The floating label
   */
  private final String label;

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
   * Based on the distance of the point on the line.
   */
  private final double distRatio;

  private FloatLabel(String label, float fontSize, double lengthRatio,
                     double distRatio, Table table, Assemble assemble) {
    Asserts.illegalArgument(label == null && table == null && assemble == null,
                            "Empty Float Label");
    Asserts.illegalArgument(fontSize < 0, "Float label can not less than 0");
    this.label = label;
    this.fontSize = fontSize;
    this.lengthRatio = lengthRatio;
    this.distRatio = distRatio;
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
   * Returns the position is described based on the length of the line.
   *
   * @return the position is described based on the length of the line
   */
  public double getLengthRatio() {
    return lengthRatio;
  }

  /**
   * Returns the distance of the point on the line.
   *
   * @return the distance of the point on the line
   */
  public double getDistRatio() {
    return distRatio;
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
        && Double.compare(that.distRatio, distRatio) == 0 && Objects.equals(label,
                                                                            that.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, fontSize, lengthRatio, distRatio);
  }

  // ------------------------------------------ FloatLabel Builder ---------------------------------------

  /**
   * Float label builder
   */
  public static class FloatLabelBuilder {

    private String label;

    private float fontSize = 14;

    private double lengthRatio;

    private double distRatio = 0.5F;

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
     * Set the distance of the point on the line.
     *
     * @param distRatio the distance of the point on the line
     * @return float label builder
     */
    public FloatLabelBuilder distRatio(double distRatio) {
      this.distRatio = distRatio;
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
      return new FloatLabel(label, fontSize, lengthRatio, distRatio, table, assemble);
    }
  }
}
