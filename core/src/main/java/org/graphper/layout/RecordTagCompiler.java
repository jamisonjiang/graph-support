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

package org.graphper.layout;

import java.util.ArrayList;
import java.util.List;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html.BasicRecordCell;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.RecordTag;
import org.graphper.def.FlatPoint;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.FontUtils;
import org.graphper.util.LabelTagUtils;

/**
 * Turns a {@link RecordTag} into the {@link Cell} tree consumed by layout and rendering.
 *
 * <p>This is the single geometry backend for record labels. Two front-ends feed it:
 *
 * <ul>
 *   <li>{@link CellLabelCompiler} parses the frozen record-label string grammar ({@code
 *       "{a|<p0>b}"}) into a {@code RecordTag};
 *   <li>the structured API ({@code Html.record(...)}) hands over a {@code RecordTag} directly,
 *       which is the only way to express rich-text cells.
 * </ul>
 *
 * <p>Keeping one backend means alignment, minimum-size compensation and offset propagation cannot
 * drift between the two paths.
 *
 * <h2>Orientation</h2>
 *
 * {@link RecordTag#isHorizontal()} describes how the tag's <em>children</em> are arranged, whereas
 * {@link Cell#isHor()} describes how a cell is arranged <em>among its siblings</em>. The two are
 * therefore inverse at each level: the cell owning a tag gets {@code isHor == !tag.isHorizontal()}
 * and each child cell gets {@code isHor == tag.isHorizontal()}.
 *
 * @author Jamison Jiang
 */
public class RecordTagCompiler {

  /**
   * Size used for a cell with no measurable content, matching the historical behaviour of the
   * string-based compiler.
   */
  public static final FlatPoint DEFAULT_SIZE = new FlatPoint(10, 10);

  private final String fontName;

  private final double fontSize;

  private final FlatPoint margin;

  private final FlatPoint minCellSize;

  private final boolean flip;

  private RootCell cell;

  private RecordTagCompiler(
      String fontName, double fontSize, FlatPoint margin, FlatPoint minCellSize, boolean flip) {
    this.fontName = fontName;
    this.fontSize = fontSize;
    this.margin = margin;
    this.minCellSize = minCellSize;
    this.flip = flip;
  }

  /**
   * Compiles a record tag into a cell tree, using default font metrics and no size constraints.
   *
   * @param recordTag record structure to compile
   * @return tree-level structure of label cell
   */
  public static RootCell compile(RecordTag recordTag) {
    return compile(recordTag, null, 14, null, null, false);
  }

  /**
   * Compiles a record tag into a cell tree.
   *
   * @param recordTag record structure to compile
   * @param fontName label font name
   * @param fontSize label font size
   * @param margin per cell margin
   * @param minCellSize minimum width and height of the whole record
   * @param flip invert the orientation of every level, used when {@code rankdir} rotates the graph
   * @return tree-level structure of label cell
   */
  public static RootCell compile(
      RecordTag recordTag,
      String fontName,
      double fontSize,
      FlatPoint margin,
      FlatPoint minCellSize,
      boolean flip) {
    if (recordTag == null) {
      throw new LabelFormatException("Record tag is null");
    }
    return new RecordTagCompiler(fontName, fontSize, margin, minCellSize, flip).init(recordTag);
  }

  private RootCell init(RecordTag recordTag) {
    boolean horizontal = horizontal(recordTag);
    this.cell = new RootCell(!horizontal);

    /*
     * The root level tracks the largest *child* extent, whereas nested levels track the running
     * extent of the parent itself (see accessCell). This asymmetry is inherited from the original
     * string compiler and is load-bearing for the resulting geometry.
     */
    double maxWidth = 0;
    double maxHeight = 0;
    for (BasicRecordCell recordCell : recordTag.getCells()) {
      Cell c = accessCell(cell, horizontal, recordCell);
      if (c != null) {
        maxWidth = Math.max(c.getWidth(), maxWidth);
        maxHeight = Math.max(c.getHeight(), maxHeight);
      }
    }

    postSizeHandle(cell, maxWidth, maxHeight);
    alignMinSize();
    return cell;
  }

  private Cell accessCell(Cell parent, boolean horizontal, BasicRecordCell recordCell) {
    if (recordCell == null) {
      return null;
    }

    Cell c = new Cell(horizontal);
    String id = recordCell.getId();
    if (StringUtils.isNotEmpty(id)) {
      c.id = id;
      cell.put(id, c);
    }

    RecordTag nested = recordCell.getNested();
    if (nested != null) {
      boolean childHorizontal = horizontal(nested);
      double maxWidth = 0;
      double maxHeight = 0;
      for (BasicRecordCell child : nested.getCells()) {
        accessCell(c, childHorizontal, child);
        maxWidth = Math.max(c.getWidth(), maxWidth);
        maxHeight = Math.max(c.getHeight(), maxHeight);
      }

      if (c.isLeaf()) {
        // An empty nested record still occupies one blank cell, mirroring "{}" in the string form.
        Cell blank = new Cell(childHorizontal);
        setCellSize(blank);
        addChild(c, blank);
        maxWidth = c.getWidth();
        maxHeight = c.getHeight();
      }

      postSizeHandle(c, maxWidth, maxHeight);
    } else {
      c.label = recordCell.getText();
      c.labelTag = recordCell.getTextTag();
      setCellSize(c);
    }

    addChild(parent, c);
    return c;
  }

  private void postSizeHandle(Cell cell, double maxWidth, double maxHeight) {
    for (Cell child : cell.getChildren()) {
      if (cell.isHor) {
        child.width = maxWidth;
      } else {
        child.height = maxHeight;
      }
    }
  }

  private void setCellSize(Cell c) {
    FlatPoint size;
    if (c.labelTag != null) {
      LabelAttributes attrs = new LabelAttributes();
      attrs.setFontName(fontName);
      attrs.setFontSize(fontSize);
      size = LabelTagUtils.measure(c.labelTag, attrs);
    } else if (StringUtils.isEmpty(c.label)) {
      size = DEFAULT_SIZE.clone();
    } else {
      size = FontUtils.measure(c.getLabel(), fontName, this.fontSize, 0);
    }

    c.width = size.getWidth();
    c.height = size.getHeight();
    if (margin == null) {
      return;
    }

    c.width += margin.getWidth();
    c.height += margin.getHeight();
  }

  private void alignMinSize() {
    double widthIncr = 0;
    double heightIncr = 0;
    if (minCellSize != null) {
      widthIncr = minCellSize.getWidth() - cell.getWidth();
      heightIncr = minCellSize.getHeight() - cell.getHeight();
    }

    alignMinSize(cell, widthIncr, heightIncr, cell.offset);
  }

  private void alignMinSize(Cell cell, double widthIncr, double heightIncr, FlatPoint offset) {
    if (widthIncr > 0) {
      cell.width += widthIncr;
    }
    if (heightIncr > 0) {
      cell.height += heightIncr;
    }

    if (cell.isLeaf()) {
      return;
    }

    double childAlignSize = 0;
    for (Cell child : cell.getChildren()) {
      if (cell.isHor()) {
        childAlignSize += child.getHeight();
      } else {
        childAlignSize += child.getWidth();
      }
    }

    if (cell.isHor()) {
      childAlignSize = cell.getHeight() - childAlignSize;
      heightIncr = childAlignSize / cell.childrenSize();
    } else {
      childAlignSize = cell.getWidth() - childAlignSize;
      widthIncr = childAlignSize / cell.childrenSize();
    }

    double axisOffset = 0;
    for (Cell child : cell.getChildren()) {
      FlatPoint childOffset;
      if (cell.isHor()) {
        childOffset = new FlatPoint(offset.getX(), offset.getY() + axisOffset);
      } else {
        childOffset = new FlatPoint(offset.getX() + axisOffset, offset.getY());
      }

      child.offset = childOffset;
      alignMinSize(child, widthIncr, heightIncr, childOffset);

      if (child.isHor) {
        axisOffset += child.getWidth();
      } else {
        axisOffset += child.getHeight();
      }
    }
  }

  private void addChild(Cell parent, Cell child) {
    if (child == null) {
      return;
    }
    if (parent.children == null) {
      parent.children = new ArrayList<>(2);
    }
    child.parent = parent;
    parent.children.add(child);

    double w = child.getWidth();
    double h = child.getHeight();
    if (child.isHor) {
      parent.width += w;
      parent.height = Math.max(h, parent.height);
    } else {
      parent.height += h;
      parent.width = Math.max(w, parent.width);
    }
  }

  private boolean horizontal(RecordTag recordTag) {
    return flip != recordTag.isHorizontal();
  }

  /**
   * Collects every {@link LabelTag} carried by the record, in the order the cells were declared.
   * Used by callers that need to know whether rich-text rendering is required at all.
   *
   * @param recordTag record to scan, may be {@code null}
   * @return the rich-text bodies found, never {@code null}
   */
  public static List<LabelTag> richTexts(RecordTag recordTag) {
    List<LabelTag> tags = new ArrayList<>();
    collectRichTexts(recordTag, tags);
    return tags;
  }

  private static void collectRichTexts(RecordTag recordTag, List<LabelTag> tags) {
    if (recordTag == null) {
      return;
    }
    for (BasicRecordCell recordCell : recordTag.getCells()) {
      if (recordCell == null) {
        continue;
      }
      if (recordCell.getTextTag() != null) {
        tags.add(recordCell.getTextTag());
      }
      collectRichTexts(recordCell.getNested(), tags);
    }
  }
}
