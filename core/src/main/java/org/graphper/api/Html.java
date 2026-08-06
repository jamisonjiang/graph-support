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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.FontUtils;

/**
 * Provides utilities for creating HTML-based graph labels.
 *
 * <p>This class offers a fluent API to construct HTML elements such as tables, text,
 * and formatting tags. It allows users to define rich, multi-line, and styled text labels for
 * graph/cluster/node/line label.</p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Creating a Table</h3>
 * <pre>{@code
 * Table table = table()
 *     .tr(td().text("Title"), td().text("Description"))
 *     .tr(td().text("Node A"), td().text("Represents Start Point"))
 *     .tr(td().text("Node B"), td().text("Represents Decision"));
 *
 * Node.builder().table(table).build();
 * }</pre>
 *
 * <h3>Using Recursive Label Tags for Rich Formatting</h3>
 * <p>The following example shows how to create a label with multiple text styles:
 * <pre>{@code
 * LabelTag label = bold("Graph Analysis")
 *     .br()
 *     .italic("Understanding the flow of a directed graph")
 *     .br()
 *     .underline(font("Highlighted Concept", fontAttrs().color(Color.BLUE).pointSize(18)))
 *     .br()
 *     .top(bold(italic("Key Takeaways:")))
 *     .br()
 *     .left(font(text("H").subscript("2").text("O").superscript("2"), fontAttrs().color(Color.RED).pointSize(16)))
 *     .br()
 *     .right(strikeThrough("Outdated Formula"))
 *     .br()
 *     .bottom(italic("Bottom Text Example"))
 *     .font("Bigger", fontAttrs().pointSize(50))
 *     .verticalCenter(italic(bold("Centered Text Example")));
 *
 * Node.builder().labelTag(label).build();
 * }</pre>
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

  /**
   * Creates and returns a new {@link FontAttrs}, which can be used to set font properties.
   *
   * @return A fresh {@link FontAttrs} instance.
   */
  public static FontAttrs fontAttrs() {
    return new FontAttrs();
  }

  /**
   * Creates a new empty {@link LabelTag}.
   *
   * @return A new {@link LabelTag} instance.
   */
  public static LabelTag labelTag() {
    return new LabelTag();
  }

  /**
   * Creates a {@link LabelTag} representing a line break (i.e., &lt;br&gt;).
   *
   * @return A new {@link LabelTag} with a line break.
   */
  public static LabelTag br() {
    return labelTag().br();
  }

  /**
   * Creates a {@link LabelTag} containing plain text.
   *
   * @param text The text content to include.
   * @return A new {@link LabelTag} with the specified text.
   */
  public static LabelTag text(String text) {
    return labelTag().text(text);
  }

  /**
   * Wraps an existing {@link LabelTag} inside a text tag.
   *
   * @param labelTag The existing label to wrap as text.
   * @return A new {@link LabelTag} containing the given label.
   */
  public static LabelTag text(LabelTag labelTag) {
    return labelTag().text(labelTag);
  }

  /**
   * Creates a {@link LabelTag} applying default font styling to the specified text.
   *
   * @param text The text content to style.
   * @return A new {@link LabelTag} styled with a font tag.
   */
  public static LabelTag font(String text) {
    return labelTag().font(text);
  }

  /**
   * Wraps an existing {@link LabelTag} inside a font-styled tag.
   *
   * @param labelTag The existing label to style with a font tag.
   * @return A new {@link LabelTag} styled with a font tag.
   */
  public static LabelTag font(LabelTag labelTag) {
    return labelTag().font(labelTag);
  }

  /**
   * Creates a {@link LabelTag} applying the specified font attributes (e.g., color, size) to the
   * given text.
   *
   * @param text      The text content to style.
   * @param fontAttrs The font attributes to apply.
   * @return A new {@link LabelTag} with the specified font attributes.
   */
  public static LabelTag font(String text, FontAttrs fontAttrs) {
    return labelTag().font(text, fontAttrs);
  }

  /**
   * Wraps an existing {@link LabelTag} inside a font-styled tag with the specified attributes.
   *
   * @param labelTag  The existing label to style.
   * @param fontAttrs The font attributes to apply.
   * @return A new {@link LabelTag} with the specified font attributes.
   */
  public static LabelTag font(LabelTag labelTag, FontAttrs fontAttrs) {
    return labelTag().font(labelTag, fontAttrs);
  }

  /**
   * Creates a {@link LabelTag} containing bold text.
   *
   * @param text The text content to make bold.
   * @return A new {@link LabelTag} with bold formatting.
   */
  public static LabelTag bold(String text) {
    return labelTag().bold(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a bold formatting tag.
   *
   * @param labelTag The existing label to make bold.
   * @return A new {@link LabelTag} with bold formatting applied.
   */
  public static LabelTag bold(LabelTag labelTag) {
    return labelTag().bold(labelTag);
  }

  /**
   * Creates a {@link LabelTag} containing italic text.
   *
   * @param text The text content to italicize.
   * @return A new {@link LabelTag} with italic formatting.
   */
  public static LabelTag italic(String text) {
    return labelTag().italic(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in an italic formatting tag.
   *
   * @param labelTag The existing label to italicize.
   * @return A new {@link LabelTag} with italic formatting applied.
   */
  public static LabelTag italic(LabelTag labelTag) {
    return labelTag().italic(labelTag);
  }

  /**
   * Creates a {@link LabelTag} containing overlined text.
   *
   * @param text The text content to overline.
   * @return A new {@link LabelTag} with an overline.
   */
  public static LabelTag overline(String text) {
    return labelTag().overline(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in an overline formatting tag.
   *
   * @param labelTag The existing label to overline.
   * @return A new {@link LabelTag} with overline formatting applied.
   */
  public static LabelTag overline(LabelTag labelTag) {
    return labelTag().overline(labelTag);
  }

  /**
   * Creates a {@link LabelTag} containing underlined text.
   *
   * @param text The text content to underline.
   * @return A new {@link LabelTag} with an underline.
   */
  public static LabelTag underline(String text) {
    return labelTag().underline(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in an underline formatting tag.
   *
   * @param labelTag The existing label to underline.
   * @return A new {@link LabelTag} with underline formatting applied.
   */
  public static LabelTag underline(LabelTag labelTag) {
    return labelTag().underline(labelTag);
  }

  /**
   * Creates a {@link LabelTag} containing subscript text.
   *
   * @param text The text content to subscript.
   * @return A new {@link LabelTag} with subscript formatting.
   */
  public static LabelTag subscript(String text) {
    return labelTag().subscript(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a subscript formatting tag.
   *
   * @param labelTag The existing label to apply subscript formatting.
   * @return A new {@link LabelTag} with subscript formatting.
   */
  public static LabelTag subscript(LabelTag labelTag) {
    return labelTag().subscript(labelTag);
  }

  /**
   * Creates a {@link LabelTag} containing superscript text.
   *
   * @param text The text content to superscript.
   * @return A new {@link LabelTag} with superscript formatting.
   */
  public static LabelTag superscript(String text) {
    return labelTag().superscript(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a superscript formatting tag.
   *
   * @param labelTag The existing label to apply superscript formatting.
   * @return A new {@link LabelTag} with superscript formatting.
   */
  public static LabelTag superscript(LabelTag labelTag) {
    return labelTag().superscript(labelTag);
  }

  /**
   * Creates a {@link LabelTag} containing strikethrough text.
   *
   * @param text The text content to strike through.
   * @return A new {@link LabelTag} with strikethrough formatting.
   */
  public static LabelTag strikeThrough(String text) {
    return labelTag().strikeThrough(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a strikethrough formatting tag.
   *
   * @param labelTag The existing label to strike through.
   * @return A new {@link LabelTag} with strikethrough formatting.
   */
  public static LabelTag strikeThrough(LabelTag labelTag) {
    return labelTag().strikeThrough(labelTag);
  }

  /**
   * Aligns text to the top in a {@link LabelTag}.
   *
   * @param text The text content to align at the top.
   * @return A new {@link LabelTag} with top alignment.
   */
  public static LabelTag top(String text) {
    return labelTag().top(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a top alignment tag.
   *
   * @param labelTag The existing label to align at the top.
   * @return A new {@link LabelTag} with top alignment.
   */
  public static LabelTag top(LabelTag labelTag) {
    return labelTag().top(labelTag);
  }

  /**
   * Aligns text to the bottom in a {@link LabelTag}.
   *
   * @param text The text content to align at the bottom.
   * @return A new {@link LabelTag} with bottom alignment.
   */
  public static LabelTag bottom(String text) {
    return labelTag().bottom(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a bottom alignment tag.
   *
   * @param labelTag The existing label to align at the bottom.
   * @return A new {@link LabelTag} with bottom alignment.
   */
  public static LabelTag bottom(LabelTag labelTag) {
    return labelTag().bottom(labelTag);
  }

  /**
   * Centers text vertically in a {@link LabelTag}.
   *
   * @param text The text content to center vertically.
   * @return A new {@link LabelTag} with vertical center alignment.
   */
  public static LabelTag verticalCenter(String text) {
    return labelTag().verticalCenter(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a vertical center alignment tag.
   *
   * @param labelTag The existing label to center vertically.
   * @return A new {@link LabelTag} with vertical center alignment.
   */
  public static LabelTag verticalCenter(LabelTag labelTag) {
    return labelTag().verticalCenter(labelTag);
  }

  /**
   * Aligns text to the left in a {@link LabelTag}.
   *
   * @param text The text content to align left.
   * @return A new {@link LabelTag} with left alignment.
   */
  public static LabelTag left(String text) {
    return labelTag().left(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a left alignment tag.
   *
   * @param labelTag The existing label to align left.
   * @return A new {@link LabelTag} with left alignment.
   */
  public static LabelTag left(LabelTag labelTag) {
    return labelTag().left(labelTag);
  }

  /**
   * Aligns text to the right in a {@link LabelTag}.
   *
   * @param text The text content to align right.
   * @return A new {@link LabelTag} with right alignment.
   */
  public static LabelTag right(String text) {
    return labelTag().right(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a right alignment tag.
   *
   * @param labelTag The existing label to align right.
   * @return A new {@link LabelTag} with right alignment.
   */
  public static LabelTag right(LabelTag labelTag) {
    return labelTag().right(labelTag);
  }

  /**
   * Centers text horizontally in a {@link LabelTag}.
   *
   * @param text The text content to center horizontally.
   * @return A new {@link LabelTag} with horizontal center alignment.
   */
  public static LabelTag horizontalCenter(String text) {
    return labelTag().horizontalCenter(text);
  }

  /**
   * Wraps an existing {@link LabelTag} in a horizontal center alignment tag.
   *
   * @param labelTag The existing label to center horizontally.
   * @return A new {@link LabelTag} with horizontal center alignment.
   */
  public static LabelTag horizontalCenter(LabelTag labelTag) {
    return labelTag().horizontalCenter(labelTag);
  }

  public abstract static class Attrs<T> {

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

    private String tooltip;

    private Collection<NodeStyle> styles;

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
     * Sets the tooltip text for the Html.
     *
     * @param tooltip the text to be displayed as the tooltip
     * @return this element
     */
    public T tooltip(String tooltip) {
      this.tooltip = tooltip;
      return self();
    }

    /**
     * Set the style of the element.
     *
     * @param styles element styles
     * @return this element
     */
    public T style(NodeStyle... styles) {
      Asserts.nullOrContainsNull(styles);
      this.styles = Arrays.asList(styles);
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

    public String getTooltip() {
      return tooltip;
    }

    public Collection<NodeStyle> getStyles() {
      if (CollectionUtils.isEmpty(styles)) {
        return Collections.emptyList();
      }
      return styles;
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

    private LabelTag textTag;

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
      this.fontName = FontUtils.selectFont(this.text, this.fontName);
      return this;
    }

    public Td textTag(LabelTag textTag) {
      this.textTag = textTag;
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
      this.fontName = FontUtils.selectFont(this.text, this.fontName);
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

    public LabelTag getTextTag() {
      return textTag;
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

    public String getHref(Table table) {
      if (super.getHref() != null) {
        return super.getHref();
      }
      return table.getHref();
    }

    public String getTooltip(Table table) {
      if (super.getTooltip() != null) {
        return super.getTooltip();
      }
      return table.getTooltip();
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

    public Collection<NodeStyle> getStyles(Table table) {
      if (CollectionUtils.isNotEmpty(super.getStyles())) {
        return super.getStyles();
      }
      if (table != null) {
        return table.getStyles();
      }
      return Collections.emptyList();
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

  /**
   * Represents a label element that supports rich text formatting and various layout alignments.
   *
   * <p>This class provides methods to create formatted text elements, including bold, italic,
   * underline, and alignment options such as top, bottom, left, right, and center. It also supports
   * multi-line text (via {@link #br()}) and recursive nesting of labels.</p>
   *
   * <p><b>Example Usage:</b></p>
   * <pre>{@code
   * LabelTag label = new LabelTag()
   *     .bold("Header")
   *     .br()
   *     .italic("Some italic text")
   *     .br()
   *     .underline("Underlined text")
   *     .br()
   *     .font("Styled text", new FontAttrs().color(Color.BLUE).pointSize(16))
   *     .br()
   *     .top("Top aligned text")
   *     .br()
   *     .strikeThrough("Obsolete text");
   * }</pre>
   */
  public static class LabelTag implements Serializable {

    private static final long serialVersionUID = -3452163944796622599L;

    private List<BasicLabelTag> tags;

    private LabelTag() {
    }

    /**
     * Inserts a line break.
     *
     * @return this {@code LabelTag} with the line break added.
     */
    public LabelTag br() {
      addTag(new BasicLabelTag(StringUtils.EMPTY, LabelTagType.BR, null));
      return this;
    }

    /**
     * Adds plain text to this label.
     *
     * @param text the text content to add.
     * @return this {@code LabelTag} with the text added.
     */
    public LabelTag text(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.TEXT, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} as text.
     *
     * @param labelTag an existing label to be wrapped as text.
     * @return this {@code LabelTag} with the label nested as text.
     */
    public LabelTag text(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.TEXT, labelTag));
      return this;
    }

    /**
     * Applies font styling to the specified text.
     *
     * @param text the text to style.
     * @return this {@code LabelTag} with the font-styled text.
     */
    public LabelTag font(String text) {
      addTag(new FontLabelTag(text, null, null));
      return this;
    }

    /**
     * Applies font styling to an existing {@code LabelTag}.
     *
     * @param labelTag the label to style.
     * @return this {@code LabelTag} with the font-styled label.
     */
    public LabelTag font(LabelTag labelTag) {
      addTag(new FontLabelTag(null, labelTag, null));
      return this;
    }

    /**
     * Applies specific font attributes to the given text.
     *
     * @param text      the text to style.
     * @param fontAttrs the font attributes (e.g., size, color, face).
     * @return this {@code LabelTag} with the styled text.
     */
    public LabelTag font(String text, FontAttrs fontAttrs) {
      addTag(new FontLabelTag(text, null, fontAttrs));
      return this;
    }

    /**
     * Applies specific font attributes to an existing {@code LabelTag}.
     *
     * @param labelTag  the label to style.
     * @param fontAttrs the font attributes (e.g., size, color, face).
     * @return this {@code LabelTag} with the styled label.
     */
    public LabelTag font(LabelTag labelTag, FontAttrs fontAttrs) {
      addTag(new FontLabelTag(null, labelTag, fontAttrs));
      return this;
    }

    /**
     * Adds text with bold formatting.
     *
     * @param text the text to make bold.
     * @return this {@code LabelTag} with bold text.
     */
    public LabelTag bold(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.BOLD, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a bold tag.
     *
     * @param labelTag an existing label to be made bold.
     * @return this {@code LabelTag} with bold formatting applied.
     */
    public LabelTag bold(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.BOLD, labelTag));
      return this;
    }

    /**
     * Adds text with italic formatting.
     *
     * @param text the text to make italic.
     * @return this {@code LabelTag} with italic text.
     */
    public LabelTag italic(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.ITALIC, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in an italic tag.
     *
     * @param labelTag an existing label to be made italic.
     * @return this {@code LabelTag} with italic formatting applied.
     */
    public LabelTag italic(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.ITALIC, labelTag));
      return this;
    }

    /**
     * Adds text with an overline.
     *
     * @param text the text to overline.
     * @return this {@code LabelTag} with overlined text.
     */
    public LabelTag overline(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.OVERLINE, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in an overline tag.
     *
     * @param labelTag an existing label to be overlined.
     * @return this {@code LabelTag} with overline formatting applied.
     */
    public LabelTag overline(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.OVERLINE, labelTag));
      return this;
    }

    /**
     * Adds text with an underline.
     *
     * @param text the text to underline.
     * @return this {@code LabelTag} with underlined text.
     */
    public LabelTag underline(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.UNDERLINE, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in an underline tag.
     *
     * @param labelTag an existing label to be underlined.
     * @return this {@code LabelTag} with underline formatting applied.
     */
    public LabelTag underline(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.UNDERLINE, labelTag));
      return this;
    }

    /**
     * Adds subscript formatting to text.
     *
     * @param text the text to subscript.
     * @return this {@code LabelTag} with subscript formatting.
     */
    public LabelTag subscript(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.SUBSCRIPT, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a subscript tag.
     *
     * @param labelTag an existing label to subscript.
     * @return this {@code LabelTag} with subscript formatting applied.
     */
    public LabelTag subscript(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.SUBSCRIPT, labelTag));
      return this;
    }

    /**
     * Adds superscript formatting to text.
     *
     * @param text the text to superscript.
     * @return this {@code LabelTag} with superscript formatting.
     */
    public LabelTag superscript(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.SUPERSCRIPT, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a superscript tag.
     *
     * @param labelTag an existing label to superscript.
     * @return this {@code LabelTag} with superscript formatting applied.
     */
    public LabelTag superscript(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.SUPERSCRIPT, labelTag));
      return this;
    }

    /**
     * Adds text with a strikethrough.
     *
     * @param text the text to strike through.
     * @return this {@code LabelTag} with strikethrough formatting.
     */
    public LabelTag strikeThrough(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.STRIKETHROUGH, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a strikethrough tag.
     *
     * @param labelTag an existing label to strike through.
     * @return this {@code LabelTag} with strikethrough formatting applied.
     */
    public LabelTag strikeThrough(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.STRIKETHROUGH, labelTag));
      return this;
    }

    /**
     * Aligns text at the top.
     *
     * @param text the text to align at the top.
     * @return this {@code LabelTag} with top alignment.
     */
    public LabelTag top(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.TOP, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a top alignment tag.
     *
     * @param labelTag an existing label to align at the top.
     * @return this {@code LabelTag} with top alignment applied.
     */
    public LabelTag top(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.TOP, labelTag));
      return this;
    }

    /**
     * Aligns text at the bottom.
     *
     * @param text the text to align at the bottom.
     * @return this {@code LabelTag} with bottom alignment.
     */
    public LabelTag bottom(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.BOTTOM, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a bottom alignment tag.
     *
     * @param labelTag an existing label to align at the bottom.
     * @return this {@code LabelTag} with bottom alignment applied.
     */
    public LabelTag bottom(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.BOTTOM, labelTag));
      return this;
    }

    /**
     * Centers text vertically.
     *
     * @param text the text to center vertically.
     * @return this {@code LabelTag} with vertical center alignment.
     */
    public LabelTag verticalCenter(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.VERTICAL_CENTER, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a vertical center alignment.
     *
     * @param labelTag an existing label to center vertically.
     * @return this {@code LabelTag} with vertical center alignment.
     */
    public LabelTag verticalCenter(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.VERTICAL_CENTER, labelTag));
      return this;
    }

    /**
     * Aligns text to the left.
     *
     * @param text the text to align left.
     * @return this {@code LabelTag} with left alignment.
     */
    public LabelTag left(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.LEFT, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a left alignment.
     *
     * @param labelTag an existing label to align left.
     * @return this {@code LabelTag} with left alignment applied.
     */
    public LabelTag left(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.LEFT, labelTag));
      return this;
    }

    /**
     * Aligns text to the right.
     *
     * @param text the text to align right.
     * @return this {@code LabelTag} with right alignment.
     */
    public LabelTag right(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.RIGHT, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a right alignment.
     *
     * @param labelTag an existing label to align right.
     * @return this {@code LabelTag} with right alignment applied.
     */
    public LabelTag right(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.RIGHT, labelTag));
      return this;
    }

    /**
     * Centers text horizontally.
     *
     * @param text the text to center horizontally.
     * @return this {@code LabelTag} with horizontal center alignment.
     */
    public LabelTag horizontalCenter(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.HORIZONTAL_CENTER, null));
      return this;
    }

    /**
     * Wraps an existing {@code LabelTag} in a horizontal center alignment.
     *
     * @param labelTag an existing label to center horizontally.
     * @return this {@code LabelTag} with horizontal center alignment.
     */
    public LabelTag horizontalCenter(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.HORIZONTAL_CENTER, labelTag));
      return this;
    }

    /**
     * Returns all the nested {@link BasicLabelTag} elements in this label.
     *
     * @return a list of {@link BasicLabelTag} objects that compose this label.
     */
    public List<BasicLabelTag> getTags() {
      return tags;
    }

    private void addTag(BasicLabelTag tag) {
      Asserts.nullArgument(tag);
      if (tags == null) {
        tags = new ArrayList<>();
      }
      tags.add(tag);
    }
  }

  /**
   * Defines font attributes such as color, size (in points), and font face.
   *
   * <p>Instances of this class are typically used in conjunction with HTML-like
   * label rendering, allowing customization of text appearance within labels.</p>
   *
   * <p><b>Example Usage:</b></p>
   * <pre>{@code
   * FontAttrs attrs = new FontAttrs()
   *     .color(Color.RED)
   *     .pointSize(14)
   *     .face("Arial");
   * }</pre>
   */
  public static class FontAttrs implements Serializable {

    private static final long serialVersionUID = -497148159693137849L;

    private Color color;
    private Integer pointSize;
    private String face;

    private FontAttrs() {
    }

    /**
     * Sets the color of the font.
     *
     * @param color The {@link Color} to apply.
     * @return This {@code FontAttrs} instance, for method chaining.
     */
    public FontAttrs color(Color color) {
      this.color = color;
      return this;
    }

    /**
     * Sets the point size of the font.
     *
     * @param pointSize The font size in points.
     * @return This {@code FontAttrs} instance, for method chaining.
     */
    public FontAttrs pointSize(int pointSize) {
      this.pointSize = pointSize;
      return this;
    }

    /**
     * Sets the face (font family) of the font.
     *
     * @param face The name of the font face, e.g., "Arial" or "Times New Roman".
     * @return This {@code FontAttrs} instance, for method chaining.
     */
    public FontAttrs face(String face) {
      this.face = face;
      return this;
    }

    /**
     * Retrieves the current font color.
     *
     * @return The {@link Color} of this font.
     */
    public Color getColor() {
      return color;
    }

    /**
     * Retrieves the current point size of this font.
     *
     * @return The size in points.
     */
    public Integer getPointSize() {
      return pointSize;
    }

    /**
     * Retrieves the current font face.
     *
     * @return The name of the font face.
     */
    public String getFace() {
      return face;
    }
  }

  public static class BasicLabelTag implements Serializable {

    private static final long serialVersionUID = -2021156451838455226L;

    private final String text;

    private final LabelTagType type;

    private final LabelTag subLabelTag;

    private BasicLabelTag(String text, LabelTagType type, LabelTag subLabelTag) {
      Asserts.nullArgument(type);
      if (subLabelTag == null && text == null) {
        this.text = StringUtils.EMPTY;
      } else {
        this.text = text;
      }
      this.type = type;
      this.subLabelTag = subLabelTag;
      verify();
    }

    public String getText() {
      return text;
    }

    public LabelTagType getType() {
      return type;
    }

    public LabelTag getSubLabelTag() {
      return subLabelTag;
    }

    public void verify() {
      if (text == null) {
        Asserts.nullArgument(subLabelTag);
      }
    }
  }

  public static class FontLabelTag extends BasicLabelTag {

    private static final long serialVersionUID = -7175729224827523985L;

    private final FontAttrs fontAttrs;

    public FontLabelTag(String text, LabelTag subLabelTag, FontAttrs fontAttrs) {
      super(text, LabelTagType.FONT, subLabelTag);
      this.fontAttrs = fontAttrs;
    }

    public FontAttrs getFontAttrs() {
      return fontAttrs;
    }
  }

  public enum LabelTagType {
    BR,
    TEXT,
    BOLD,
    FONT,
    ITALIC,
    OVERLINE,
    UNDERLINE,
    SUBSCRIPT,
    SUPERSCRIPT,
    STRIKETHROUGH,

    // Layout type
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    VERTICAL_CENTER,
    HORIZONTAL_CENTER,
  }
}