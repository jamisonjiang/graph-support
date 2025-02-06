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

  public static FontAttrs fontAttrs() {
    return new FontAttrs();
  }

  public static LabelTag labelTag() {
    return new LabelTag();
  }

  public static LabelTag br() {
    return labelTag().br();
  }

  public static LabelTag text(String text) {
    return labelTag().text(text);
  }

  public static LabelTag text(LabelTag labelTag) {
    return labelTag().text(labelTag);
  }

  public static LabelTag font(String text) {
    return labelTag().font(text);
  }

  public static LabelTag font(LabelTag labelTag) {
    return labelTag().font(labelTag);
  }

  public static LabelTag font(String text, FontAttrs fontAttrs) {
    return labelTag().font(text, fontAttrs);
  }

  public static LabelTag font(LabelTag labelTag, FontAttrs fontAttrs) {
    return labelTag().font(labelTag, fontAttrs);
  }

  public static LabelTag bold(String text) {
    return labelTag().bold(text);
  }

  public static LabelTag bold(LabelTag labelTag) {
    return labelTag().bold(labelTag);
  }

  public static LabelTag italic(String text) {
    return labelTag().italic(text);
  }

  public static LabelTag italic(LabelTag labelTag) {
    return labelTag().italic(labelTag);
  }

  public static LabelTag overline(String text) {
    return labelTag().overline(text);
  }

  public static LabelTag overline(LabelTag labelTag) {
    return labelTag().overline(labelTag);
  }

  public static LabelTag underline(String text) {
    return labelTag().underline(text);
  }

  public static LabelTag underline(LabelTag labelTag) {
    return labelTag().underline(labelTag);
  }

  public static LabelTag subscript(String text) {
    return labelTag().subscript(text);
  }

  public static LabelTag subscript(LabelTag labelTag) {
    return labelTag().subscript(labelTag);
  }

  public static LabelTag superscript(String text) {
    return labelTag().superscript(text);
  }

  public static LabelTag superscript(LabelTag labelTag) {
    return labelTag().superscript(labelTag);
  }

  public static LabelTag strikeThrough(String text) {
    return labelTag().strikeThrough(text);
  }

  public static LabelTag strikeThrough(LabelTag labelTag) {
    return labelTag().strikeThrough(labelTag);
  }

  public static LabelTag top(String text) {
    return labelTag().top(text);
  }

  public static LabelTag top(LabelTag labelTag) {
    return labelTag().top(labelTag);
  }

  public static LabelTag bottom(String text) {
    return labelTag().bottom(text);
  }

  public static LabelTag bottom(LabelTag labelTag) {
    return labelTag().bottom(labelTag);
  }

  public static LabelTag verticalCenter(String text) {
    return labelTag().verticalCenter(text);
  }

  public static LabelTag verticalCenter(LabelTag labelTag) {
    return labelTag().verticalCenter(labelTag);
  }

  public static LabelTag left(String text) {
    return labelTag().left(text);
  }

  public static LabelTag left(LabelTag labelTag) {
    return labelTag().left(labelTag);
  }

  public static LabelTag right(String text) {
    return labelTag().right(text);
  }

  public static LabelTag right(LabelTag labelTag) {
    return labelTag().right(labelTag);
  }

  public static LabelTag horizontalCenter(String text) {
    return labelTag().horizontalCenter(text);
  }

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


  public static class LabelTag implements Serializable {

    private static final long serialVersionUID = -3452163944796622599L;

    private List<BasicLabelTag> tags;

    private LabelTag() {
    }

    public LabelTag br() {
      addTag(new BasicLabelTag(StringUtils.EMPTY, LabelTagType.BR, null));
      return this;
    }

    public LabelTag text(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.TEXT, null));
      return this;
    }

    public LabelTag text(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.TEXT, labelTag));
      return this;
    }

    public LabelTag font(String text) {
      addTag(new FontLabelTag(text, null, null));
      return this;
    }

    public LabelTag font(LabelTag labelTag) {
      addTag(new FontLabelTag(null, labelTag, null));
      return this;
    }

    public LabelTag font(String text, FontAttrs fontAttrs) {
      addTag(new FontLabelTag(text, null, fontAttrs));
      return this;
    }

    public LabelTag font(LabelTag labelTag, FontAttrs fontAttrs) {
      addTag(new FontLabelTag(null, labelTag, fontAttrs));
      return this;
    }

    public LabelTag bold(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.BOLD, null));
      return this;
    }

    public LabelTag bold(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.BOLD, labelTag));
      return this;
    }

    public LabelTag italic(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.ITALIC, null));
      return this;
    }

    public LabelTag italic(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.ITALIC, labelTag));
      return this;
    }

    public LabelTag overline(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.OVERLINE, null));
      return this;
    }

    public LabelTag overline(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.OVERLINE, labelTag));
      return this;
    }

    public LabelTag underline(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.UNDERLINE, null));
      return this;
    }

    public LabelTag underline(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.UNDERLINE, labelTag));
      return this;
    }

    public LabelTag subscript(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.SUBSCRIPT, null));
      return this;
    }

    public LabelTag subscript(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.SUBSCRIPT, labelTag));
      return this;
    }

    public LabelTag superscript(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.SUPERSCRIPT, null));
      return this;
    }

    public LabelTag superscript(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.SUPERSCRIPT, labelTag));
      return this;
    }

    public LabelTag strikeThrough(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.STRIKETHROUGH, null));
      return this;
    }

    public LabelTag strikeThrough(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.STRIKETHROUGH, labelTag));
      return this;
    }

    public LabelTag top(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.TOP, null));
      return this;
    }

    public LabelTag top(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.TOP, labelTag));
      return this;
    }

    public LabelTag bottom(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.BOTTOM, null));
      return this;
    }

    public LabelTag bottom(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.BOTTOM, labelTag));
      return this;
    }

    public LabelTag verticalCenter(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.VERTICAL_CENTER, null));
      return this;
    }

    public LabelTag verticalCenter(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.VERTICAL_CENTER, labelTag));
      return this;
    }

    public LabelTag left(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.LEFT, null));
      return this;
    }

    public LabelTag left(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.LEFT, labelTag));
      return this;
    }

    public LabelTag right(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.RIGHT, null));
      return this;
    }

    public LabelTag right(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.RIGHT, labelTag));
      return this;
    }

    public LabelTag horizontalCenter(String text) {
      addTag(new BasicLabelTag(text, LabelTagType.HORIZONTAL_CENTER, null));
      return this;
    }

    public LabelTag horizontalCenter(LabelTag labelTag) {
      addTag(new BasicLabelTag(null, LabelTagType.HORIZONTAL_CENTER, labelTag));
      return this;
    }

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

  public static class FontAttrs implements Serializable {

    private static final long serialVersionUID = -497148159693137849L;

    private Color color;

    private Integer pointSize;

    private String face;

    private FontAttrs() {
    }

    public FontAttrs color(Color color) {
      this.color = color;
      return this;
    }

    public FontAttrs pointSize(int pointSize) {
      this.pointSize = pointSize;
      return this;
    }

    public FontAttrs face(String face) {
      this.face = face;
      return this;
    }

    public Color getColor() {
      return color;
    }

    public Integer getPointSize() {
      return pointSize;
    }

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
      this.text = text;
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
      if (subLabelTag == null) {
        Asserts.nullArgument(text);
      }
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