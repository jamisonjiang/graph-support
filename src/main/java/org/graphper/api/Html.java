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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * Html-related APIs.
 *
 * @author Jamison Jiang
 */
public class Html {

  private Html() {
  }

  /**
   * Create and return a new table.
   *
   * @return table
   */
  public static Table table() {
    return new Table();
  }

  /**
   * Create and return a new td tag.
   *
   * @return td tag
   */
  public static Td td() {
    return new Td();
  }

  protected abstract static class Attrs<T> {

    private String id;

    private Labeljust align;

    private Labelloc valign;

    private Color bgColor;

    private Integer cellPadding;

    private Color color;

    private Boolean fixedSize;

    private double width = 10;

    private double height = 10;

    private String href;

    private NodeStyle style;

    protected abstract T self();

    /**
     * Set a unique id to identify the element.
     *
     * @param id tag id
     * @return this element
     */
    public T id(String id) {
      this.id = id;
      return self();
    }

    /**
     * Sets the horizontal alignment of elements
     *
     * @param align horizontal alignment
     * @return this element
     */
    public T align(Labeljust align) {
      this.align = align;
      return self();
    }

    /**
     * Sets the vertical alignment of elements
     *
     * @param valign vertical alignment
     * @return this element
     */
    public T valign(Labelloc valign) {
      this.valign = valign;
      return self();
    }

    /**
     * Sets the background color of elements.
     *
     * @param bgColor background color
     * @return this element
     */
    public T bgColor(Color bgColor) {
      this.bgColor = bgColor;
      return self();
    }

    /**
     * Sets the cell padding of TD elements.
     *
     * @param cellPadding cell padding
     * @return this element
     */
    public T cellPadding(int cellPadding) {
      Asserts.illegalArgument(cellPadding < 0, "cellPadding can not be less than 0");
      this.cellPadding = cellPadding;
      return self();
    }

    /**
     * Sets the border color of elements/
     *
     * @param color border color
     * @return this element
     */
    public T color(Color color) {
      this.color = color;
      return self();
    }

    /**
     * Set whether to fix the size of TDs.
     *
     * @param fixedSize fixed td size
     * @return this element
     */
    public T fixedSize(boolean fixedSize) {
      this.fixedSize = fixedSize;
      return self();
    }

    /**
     * Sets the width of this element.
     *
     * @param width width
     * @return this element
     * @throws IllegalArgumentException width less or equals to zero
     */
    public T width(double width) {
      Asserts.illegalArgument(width <= 0, "width must be greater than 0");
      this.width = width;
      return self();
    }

    /**
     * Sets the height of this element.
     *
     * @param height height
     * @return this element
     * @throws IllegalArgumentException height less or equals to zero
     */
    public T height(double height) {
      Asserts.illegalArgument(height <= 0, "height must be greater than 0");
      this.height = height;
      return self();
    }

    /**
     * Set a element link, only valid when the output is <tt>svg</tt>.
     *
     * @param href element href
     * @return this element
     */
    public T href(String href) {
      this.href = href;
      return self();
    }

    /**
     * Set the style of the element.
     *
     * @param style element style
     * @return this element
     */
    public T style(NodeStyle style) {
      this.style = style;
      return self();
    }

    public String getId() {
      return id;
    }

    public Labeljust getAlign() {
      return align;
    }

    public Color getBgColor() {
      return bgColor;
    }

    public Integer getCellPadding() {
      return cellPadding;
    }

    public Color getColor() {
      return color;
    }

    public Boolean getFixedSize() {
      return fixedSize;
    }

    public double getWidth() {
      return width;
    }

    public double getHeight() {
      return height;
    }

    public String getHref() {
      return href;
    }

    public NodeStyle getStyle() {
      return style;
    }

    public Labelloc getValign() {
      return valign;
    }
  }

  /**
   * Html table tag.
   */
  public static class Table extends Attrs<Table> implements Serializable {

    private static final long serialVersionUID = 5520400546096949803L;

    private List<Tr> trs;

    private int border = 1;

    private int cellBorder = 1;

    private int cellSpacing = 2;

    /**
     * Set the {@link Td#border(int)} of all tds under the table, if td is not set, this value will
     * be used, if it is set, the value of td will be used.
     *
     * @param cellBorder cell border
     * @return table
     * @throws IllegalArgumentException cell border less than 0
     */
    public Table cellBorder(int cellBorder) {
      Asserts.illegalArgument(cellBorder < 0, "cellBorder can not be less than 0");
      this.cellBorder = cellBorder;
      return this;
    }

    /**
     * Set the spacing between each cell
     *
     * @param spacing cell spacing
     * @return table
     * @throws IllegalArgumentException cell spacing less than 0
     */
    public Table cellSpacing(int spacing) {
      Asserts.illegalArgument(spacing < 0, "spacing can not be less than 0");
      this.cellSpacing = spacing;
      return this;
    }

    /**
     * Set the size of the border of the table.
     *
     * @param border table border
     * @return table
     * @throws IllegalArgumentException table border less than 0
     */
    public Table border(int border) {
      Asserts.illegalArgument(border < 0, "border can not be less than 0");
      this.border = border;
      return self();
    }

    /**
     * Add a TR to the table, and add the corresponding TDs to the TR.
     *
     * @param tds td list
     * @return table
     * @throws IllegalArgumentException empty tds
     */
    public synchronized Table tr(Td... tds) {
      Asserts.illegalArgument(tds == null || tds.length == 0, "Tds are empty");
      if (trs == null) {
        trs = new ArrayList<>();
      }
      Tr tr = new Tr();
      tr.td(tds);
      trs.add(tr);
      return this;
    }

    /**
     * Returns all TRs.
     *
     * @return all TRs
     */
    public List<Tr> getTrs() {
      return CollectionUtils.isEmpty(trs)
          ? Collections.emptyList()
          : Collections.unmodifiableList(trs);
    }

    /**
     * Get TR by row index.
     *
     * @param rowIdx row index
     * @return TR of specify index
     */
    public Tr getTr(int rowIdx) {
      if (rowIdx < 0 || rowIdx > rowNum() - 1) {
        throw new IndexOutOfBoundsException();
      }
      return trs.get(rowIdx);
    }

    public int getCellBorder() {
      return cellBorder;
    }

    public int getCellSpacing() {
      return cellSpacing;
    }

    public int getBorder() {
      return border;
    }

    public int rowNum() {
      if (CollectionUtils.isEmpty(trs)) {
        return 0;
      }
      return trs.size();
    }

    @Override
    protected Table self() {
      return this;
    }
  }

  /**
   * Html tr tag.
   */
  public static class Tr implements Serializable {

    private static final long serialVersionUID = -1203994797919820914L;
    private List<Td> tds;

    public List<Td> getTds() {
      return CollectionUtils.isEmpty(tds)
          ? Collections.emptyList()
          : Collections.unmodifiableList(tds);
    }

    public Td getTd(int tdIdx) {
      if (tdIdx < 0 || tdIdx > colNum() - 1) {
        throw new IndexOutOfBoundsException();
      }
      return tds.get(tdIdx);
    }

    public int colNum() {
      if (CollectionUtils.isEmpty(tds)) {
        return 0;
      }
      return tds.size();
    }

    private synchronized void td(Td... tdArray) {
      if (tdArray == null || tdArray.length == 0) {
        return;
      }
      if (tds == null) {
        this.tds = new ArrayList<>();
      }
      Collections.addAll(tds, tdArray);
    }
  }

  /**
   * Html td tag.
   */
  public static class Td extends Attrs<Td> implements Serializable {

    private static final long serialVersionUID = -8637141082571738828L;

    private Table table;

    private Integer border;

    private int rowSpan = 1;

    private int colSpan = 1;

    private String text;

    private Color fontColor;

    private String fontName;

    private double fontSize = 12;

    private NodeShape shape = NodeShapeEnum.RECT;

    @Override
    protected Td self() {
      return this;
    }

    /**
     * Set the size of the border of the TD.
     *
     * @param border TD border
     * @return TD
     * @throws IllegalArgumentException TD border less than 0
     */
    public Td border(int border) {
      Asserts.illegalArgument(border < 0, "border can not be less than 0");
      this.border = border;
      return this;
    }

    /**
     * Set the number of TD horizontal frame rows.
     *
     * @param rowSpan row span number
     * @return TD
     */
    public Td rowSpan(int rowSpan) {
      this.rowSpan = rowSpan;
      return this;
    }

    /**
     * Set the number of TD vertical frame columns.
     *
     * @param colSpan column span number
     * @return TD
     */
    public Td colSpan(int colSpan) {
      this.colSpan = colSpan;
      return this;
    }

    /**
     * Sets the text content of TD.
     *
     * @param text text
     * @return TD
     */
    public Td text(String text) {
      this.text = text;
      return this;
    }

    /**
     * Set the font color of TD.
     *
     * @param fontColor font color
     * @return TD
     */
    public Td fontColor(Color fontColor) {
      this.fontColor = fontColor;
      return this;
    }

    /**
     * Set the font style of TD.
     *
     * @param fontName font style name
     * @return TD
     */
    public Td fontName(String fontName) {
      this.fontName = fontName;
      return this;
    }

    /**
     * Set the font size of TD.
     *
     * @param fontSize font size
     * @return TD
     * @throws IllegalArgumentException font size less than 0
     */
    public Td fontSize(double fontSize) {
      Asserts.illegalArgument(fontSize < 0, "");
      this.fontSize = fontSize;
      return this;
    }

    /**
     * Set the shape of the TD, for the shapes supported by default, please check
     * {@link NodeShapeEnum}.
     *
     * @param shape TD shape
     * @return TD
     */
    public Td shape(NodeShape shape) {
      Asserts.nullArgument(shape, "shape");
      this.shape = shape;
      return this;
    }

    /**
     * Set an embedded Table in TD.
     *
     * @param table embedded table
     * @return TD
     */
    public Td table(Table table) {
      this.table = table;
      return this;
    }

    public Table getTable() {
      return table;
    }

    public int getRowSpan() {
      return Math.max(rowSpan, 1);
    }

    public int getColSpan() {
      return Math.max(colSpan, 1);
    }

    public String getText() {
      return text;
    }

    public Color getFontColor() {
      return fontColor;
    }

    public String getFontName() {
      return fontName;
    }

    public double getFontSize() {
      return fontSize;
    }

    public NodeShape getShape() {
      return shape;
    }

    public Labeljust getAlign(Table table) {
      if (super.getAlign() != null) {
        return super.getAlign();
      }
      if (table != null && table.getAlign() != null) {
        return table.getAlign();
      }
      return Labeljust.CENTER;
    }

    public Labelloc getValign(Table table) {
      if (super.getValign() != null) {
        return super.getValign();
      }
      if (table != null && table.getValign() != null) {
        return table.getValign();
      }
      return Labelloc.CENTER;
    }

    public int getBorder(Table table) {
      if (border != null) {
        return border;
      }
      if (table != null) {
        return table.getCellBorder();
      }
      return 1;
    }

    public int getCellPadding(Table table) {
      if (super.getCellPadding() != null) {
        return super.getCellPadding();
      }
      if (table != null && table.getCellPadding() != null) {
        return table.getCellPadding();
      }
      return 5;
    }

    public NodeStyle getStyle(Table table) {
      if (super.getStyle() != null) {
        return super.getStyle();
      }
      if (table != null && table.getStyle() != null) {
        return table.getStyle();
      }
      return null;
    }

    public boolean isFixedSize(Table table) {
      if (super.getFixedSize() != null) {
        return super.getFixedSize();
      }
      if (table != null && table.getFixedSize() != null) {
        return table.getFixedSize();
      }
      return false;
    }
  }
}