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

import static org.graphper.api.Graphviz.PIXEL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Assemble;
import org.graphper.api.Assemble.AssembleBuilder;
import org.graphper.api.Html.BasicLabelTag;
import org.graphper.api.Html.FontAttrs;
import org.graphper.api.Html.FontLabelTag;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.LabelTagType;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.api.Html.Tr;
import org.graphper.api.Node;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.FontStyle;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.def.CycleDependencyException;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.FontUtils;
import org.graphper.util.LabelTagUtils;

/**
 * Translate the data of the original html-like structure into a lower-level {@link Assemble}.
 *
 * @author Jamison Jiang
 */
public class HtmlConvertor {

  /**
   * Separator between the steps of an identity scope path.
   */
  private static final String SCOPE_SEPARATOR = "::";

  /**
   * Step appended to a scope for the node that carries a table's own border and background. It is
   * distinct from every cell step, so a table and the cell that holds it never share an identity.
   */
  private static final String TABLE_SCOPE_STEP = "t";

  private HtmlConvertor() {
  }

  /**
   * The identity space that the table and cell nodes of every html-like label of one graph are
   * drawn from.
   *
   * <p>The nodes generated for a label are ordinary {@link Node}s and end up in the same map as the
   * graph's real nodes. {@link Node#equals} and {@link Node#hashCode} match on a non-null id, so two
   * generated nodes carrying the same id are one node as far as the layout engine is concerned: the
   * second one silently adopts the draw property of the first. An id space hands out the identity
   * of each generated node and guarantees no two of them, and no generated node and real node,
   * collide.
   *
   * <p>The author supplied id is always the preferred identity, and the overwhelmingly common case
   * - an id that nothing else in the graph uses - gets exactly that, so
   * {@link org.graphper.draw.DrawGraph#nodeId(Node)} keeps resolving it. A scoped path, unique to
   * the label that owns the cell, is only used when the preferred identity is already taken or when
   * the author supplied none.
   *
   * <p>Claiming is first come first served in the order the layout engine converts labels. That
   * order only decides <em>which</em> of several labels sharing one authored id keeps it; an id
   * used once is kept no matter when it is claimed.
   *
   * <p>Not thread safe; one instance belongs to one {@link org.graphper.draw.DrawGraph}, which is
   * built by a single layout run.
   */
  public static final class LabelIdSpace implements Serializable {

    private static final long serialVersionUID = -6295986177570331891L;

    private final Set<String> claimed = new HashSet<>();

    /**
     * Reserves an identity so that no html label can be handed it. Used for the ids of the real
     * nodes of the graph, which own their id unconditionally.
     *
     * @param id identity to reserve, ignored when {@code null}
     */
    public void reserve(String id) {
      if (id != null) {
        claimed.add(id);
      }
    }

    /**
     * Returns the identity of one generated table or cell node.
     *
     * @param authoredId the {@code id} the author gave the table or cell, may be {@code null}
     * @param scopedPath fallback path identity, unique to the owning label by construction
     * @return an identity no other generated node of this graph has been given
     */
    public String identity(String authoredId, String scopedPath) {
      if (authoredId != null && claimed.add(authoredId)) {
        return authoredId;
      }
      Asserts.nullArgument(scopedPath, "scopedPath");
      if (claimed.add(scopedPath)) {
        return scopedPath;
      }

      /*
       * Only reachable when an author writes an id that looks exactly like a scope path. Probing
       * keeps the guarantee absolute instead of merely overwhelmingly likely.
       */
      for (int i = 2; ; i++) {
        String candidate = scopedPath + SCOPE_SEPARATOR + i;
        if (claimed.add(candidate)) {
          return candidate;
        }
      }
    }
  }

  /**
   * Parses the given {@link Table} data structure, performs a table layout, and converts the result
   * into a lower-level {@link Assemble} instance for rendering.
   *
   * <p>This method inspects the rows and cells of the provided {@code table} and
   * calculates their sizes and positions. It then assembles the layout data into a container-like
   * structure represented by {@link Assemble}, which can be rendered or further processed by
   * downstream logic.</p>
   *
   * @param table the HTML-like table to convert.
   * @return an {@link Assemble} structure representing the laid-out table, or {@code null} if the
   * provided table is {@code null}.
   * @throws IllegalArgumentException if the table is empty (i.e., no rows).
   * @throws CycleDependencyException if a cycle dependency is detected in table processing
   */
  public static Assemble toAssemble(Table table) {
    return toAssemble(table, null);
  }

  /**
   * Same as {@link #toAssemble(Table)}, but names every generated cell node after {@code scope}
   * instead of after the author supplied {@code id}s.
   *
   * <p>The generated nodes live in one global {@link Node} identity space: {@link Node#equals} and
   * {@link Node#hashCode} treat two nodes with the same non-null id as the same node, so an id
   * repeated in two different labels would make the second cell reuse the draw property of the
   * first. A scope unique to the label owner turns the cell ids into {@code owner::row/column}
   * paths and removes that coupling, at the price of the authored ids no longer being visible
   * through {@link org.graphper.draw.DrawGraph#nodeId(Node)}. Prefer
   * {@link #toAssemble(Table, String, LabelIdSpace)}, which pays that price only for the cells that
   * actually conflict. The author supplied {@code id} stays the owner-local cell id that edge ports
   * resolve against either way.
   *
   * <p>{@code null} leaves ids exactly as authored, which is only safe when the caller knows the
   * label is the single html label in the graph.
   *
   * @param table the HTML-like table to convert
   * @param scope identity scope of the label owner, or {@code null} to keep authored ids
   * @return an {@link Assemble} structure representing the laid-out table, or {@code null} if the
   * provided table is {@code null}
   * @throws IllegalArgumentException if the table is empty (i.e., no rows).
   * @throws CycleDependencyException if a cycle dependency is detected in table processing
   */
  public static Assemble toAssemble(Table table, String scope) {
    return toAssemble(table, scope, null);
  }

  /**
   * Same as {@link #toAssemble(Table)}, but draws the identity of every generated table and cell
   * node from {@code idSpace}, so that no two labels of one graph can end up sharing a node.
   *
   * <p>An authored {@code id} that nothing else in the graph claims is used verbatim and stays
   * resolvable through {@link org.graphper.draw.DrawGraph#nodeId(Node)}. Only a table or cell whose
   * authored id is already taken, or which has no authored id at all, falls back to a
   * {@code scope::step} path that is unique to the owning label. See {@link LabelIdSpace}.
   *
   * @param table   the HTML-like table to convert
   * @param scope   identity scope of the label owner, the fallback identity of its tables and cells
   * @param idSpace identity space of the graph, or {@code null} to fall back to
   *                {@link #toAssemble(Table, String)}
   * @return an {@link Assemble} structure representing the laid-out table, or {@code null} if the
   * provided table is {@code null}
   * @throws IllegalArgumentException if the table is empty (i.e., no rows), or if an id space is
   *                                  given without a scope to fall back to
   * @throws CycleDependencyException if a cycle dependency is detected in table processing
   */
  public static Assemble toAssemble(Table table, String scope, LabelIdSpace idSpace) {
    if (table == null) {
      return null;
    }

    /*
     * An id space without a scope could not keep its promise: it would have nothing to fall back to
     * for a contested or absent authored id. Refusing is better than silently handing out ids that
     * may collide.
     */
    Asserts.illegalArgument(idSpace != null && scope == null,
                            "An identity space needs a scope to fall back to");
    Asserts.illegalArgument(table.rowNum() == 0, "Empty tr in table");
    RootTableHelper tableHelper = new RootTableHelper(table);
    tableLayout(table, tableHelper, tableHelper);
    tableHelper.releaseMark();

    return convertToAssemble(table, tableHelper, scope, idSpace);
  }

  /**
   * Parses the given {@link LabelTag} with the specified label attributes, performs an automatic
   * layout calculation, and converts the result into a lower-level {@link Assemble} structure for
   * rendering.
   *
   * <p>This method measures the text defined by the {@code labelTag} using
   * {@link org.graphper.util.LabelTagUtils#measure}, then calculates how to position lines
   * and sub-tags within a final {@link Assemble} instance.</p>
   *
   * @param labelTag   the {@link LabelTag} (HTML-like structure) to convert
   * @param labelAttrs the default attributes (e.g., font size, color) to apply
   * @return an {@link Assemble} instance representing the laid-out label, or {@code null} if either
   * parameter is {@code null}
   * @throws IllegalArgumentException if the label measurement is {@code null}
   * @throws CycleDependencyException if a cycle dependency is detected in label processing
   */
  public static Assemble toAssemble(LabelTag labelTag, LabelAttributes labelAttrs) {
    if (labelTag == null || labelAttrs == null) {
      return null;
    }

    AssembleBuilder builder = Assemble.builder();
    TextRows textRows = new TextRows();
    TextTagValue textTagValue = new TextTagValue(labelAttrs);
    accessLabelTag(textRows, labelTag, textTagValue, new FlatPoint(0, 0), 0);

    FlatPoint size = LabelTagUtils.measure(labelTag, labelAttrs);
    Asserts.nullArgument(size);

    textAlign(size, textRows, builder);
    return builder.width(size.getWidth() / PIXEL).height(size.getHeight() / PIXEL).build();
  }

  // ------------------------------------------ table private methods ------------------------------------------

  private static void tableLayout(Table table, TableHelper tableHelper,
                                  RootTableHelper rootTableHelper) {
    Asserts.illegalArgument(table.rowNum() == 0, "Empty tr in table");
    if (rootTableHelper.isMark(table)) {
      throw new CycleDependencyException("Cycle dependency table");
    }

    rootTableHelper.mark(table);

    // Arrange td in a grid in order
    setTdGridPosition(table, tableHelper, rootTableHelper);

    // If there is no td between two adjacent vertical axes, it needs to be divided into a group,
    // and the vertical axes of the same group need to be merged in subsequent operations
    Map<Integer, TableAxis> groupRecord = groupVerAxis(tableHelper);

    // Set the table height and merge the vertical axis of the same group
    setTableHeightAndMergeVerAxis(table, tableHelper, groupRecord);

    // Set the table width
    setTableWidth(table, tableHelper);

    // Align minimum width and height
    alignSize(table, tableHelper);

    rootTableHelper.remove(table);
  }

  private static void setTdGridPosition(Table table, TableHelper tableHelper,
                                        RootTableHelper rootTableHelper) {
    for (int r = 0; r < table.rowNum(); r++) {
      Tr tr = table.getTr(r);
      Asserts.illegalArgument(tr.colNum() == 0, "Empty td in tr");

      int preAxis = 0;
      int currentOccupyRangeIdx = 0;
      for (int c = 0; c < tr.colNum(); c++) {
        Td td = tr.getTd(c);
        int rowSpan = td.getRowSpan();
        int colSpan = td.getColSpan();
        tableHelper.haveGreaterThan2ColSpan |= (colSpan > 1);

        /*
         * Start from the specified position and find a position where td can be placed.
         */
        OccupyRange occupyRange;
        do {
          occupyRange = tableHelper.getOccupyRange(r, currentOccupyRangeIdx);
          if (occupyRange == null || occupyRange.leftRange(preAxis)) {
            break;
          }

          if (occupyRange.inRange(preAxis)) {
            preAxis = occupyRange.end;
          }

          currentOccupyRangeIdx++;
        } while (true);

        rowSpan = Math.min(r + rowSpan - 1, table.rowNum() - 1) - r + 1;
        TableAxis up = tableHelper.getRowAxis(r);
        TableAxis down = tableHelper.getRowAxis(r + rowSpan);
        TableAxis left = tableHelper.getColAxis(preAxis);
        TableAxis right = tableHelper.getColAxis(preAxis += colSpan);
        tableHelper.markHaveTdBetweenColAxis(left, right);
        TdBox tdBox = new TdBox(left, right, up, down);
        tdSize(table, tdBox, td, rootTableHelper);
        tableHelper.addPos(td, tdBox);

        if (rowSpan <= 1) {
          continue;
        }

        /*
         * If the rowspan of the current td exceeds 1, add the occupancy records of the range to all the rows below.
         */
        int maxRow = rowSpan + r - 1;
        for (int i = r + 1; i <= maxRow; i++) {
          tableHelper.addOccupyRange(i, tdBox.left.no, tdBox.right.no);
        }
      }

      tableHelper.clearOccupyRanges(r);
    }
  }

  private static Map<Integer, TableAxis> groupVerAxis(TableHelper tableHelper) {
    if (!tableHelper.haveGreaterThan2ColSpan) {
      return null;
    }

    /*
     * TD's colspan may exceed the number of columns that can actually be merged.
     * At this time, if there are two adjacent vertical axes without any td records,
     * it means that the number of columns that can be merged is less than the set
     * colspan, and such adjacent redundant axes need to be merged into one axis.
     */
    Map<Integer, TableAxis> groupRecord = null;
    Entry<Integer, TableAxis> axis = tableHelper.lastColAxis();
    while (axis != null) {
      Entry<Integer, TableAxis> pre = tableHelper.lowerAxis(axis.getValue());
      if (pre != null) {
        TableAxis currentAxis = axis.getValue();
        TableAxis preAxis = pre.getValue();

        /*
         * Adjacent axes have no TD records.
         */
        if (tableHelper.notHaveTdBetweenAxes(preAxis, currentAxis)) {
          if (groupRecord == null) {
            groupRecord = new HashMap<>();
            groupRecord.put(preAxis.no, currentAxis);
            continue;
          }

          TableAxis parentAxis = groupRecord.get(currentAxis.no);
          if (parentAxis != null) {
            groupRecord.put(preAxis.no, parentAxis);
          } else {
            groupRecord.put(preAxis.no, currentAxis);
          }
        }
      }
      axis = pre;
    }
    return groupRecord;
  }

  private static void setTableHeightAndMergeVerAxis(Table table, TableHelper tableHelper,
                                                    Map<Integer, TableAxis> groupRecord) {
    for (int r = 0; r < table.rowNum(); r++) {
      Tr tr = table.getTr(r);

      for (int c = 0; c < tr.colNum(); c++) {
        Td td = tr.getTd(c);
        TdBox tdBox = tableHelper.getBox(td);
        if (tdBox == null) {
          continue;
        }

        /*
         * If the td's left and right axes are in some group,
         * use the group's representative axis instead of the current axis.
         */
        if (groupRecord != null) {
          TableAxis leftGroup = groupRecord.get(tdBox.left.no);
          TableAxis rightGroup = groupRecord.get(tdBox.right.no);

          if (leftGroup != null) {
            tdBox.left = leftGroup;
          }
          if (rightGroup != null) {
            tdBox.right = rightGroup;
          }
        }

        // Refresh horizontal axis position
        boolean refresh = tdBox.down.refreshPos(tdBox.up.position + tdBox.size.getHeight());
        tableHelper.refreshHeight(tdBox.down.position);
        // Balance the spanned axis spacing
        balanceRowChildTds(tableHelper, tdBox, refresh);
      }
    }

    if (groupRecord == null) {
      return;
    }

    for (Entry<Integer, TableAxis> group : groupRecord.entrySet()) {
      Integer key = group.getKey();
      TableAxis axis = group.getValue();
      if (!Objects.equals(key, axis.no)) {
        tableHelper.removeColAxis(key);
      }
    }
  }

  private static void setTableWidth(Table table, TableHelper tableHelper) {
    tableHelper.releaseAxesPairs();
    int[] colAccessIdx = new int[table.rowNum()];
    Entry<Integer, TableAxis> axisEntry = tableHelper.firstColAxis();
    while (axisEntry != null) {
      TableAxis axis = axisEntry.getValue();

      for (int r = 0; r < table.rowNum(); r++) {
        int rowColIdx = colAccessIdx[r];
        Tr tr = table.getTr(r);
        if (rowColIdx == tr.colNum()) {
          continue;
        }

        Td td = tr.getTd(rowColIdx);
        TdBox tdBox = tableHelper.getBox(td);
        if (tdBox == null || tdBox.left.no > axis.no) {
          continue;
        }

        // Refresh vertical axis position
        colAccessIdx[r]++;
        boolean refresh = tdBox.right.refreshPos(tdBox.left.position + tdBox.size.getWidth());
        Entry<Integer, TableAxis> nextAxis = tableHelper.higherAxis(tdBox.right);
        if (nextAxis != null) {
          nextAxis.getValue().refreshPos(tdBox.right.position);
        }
        tableHelper.refreshWidth(tdBox.right.position);
        // Balance the spanned axis spacing
        balanceColChildTds(tableHelper, tdBox, refresh);
      }

      axisEntry = tableHelper.higherAxis(axisEntry.getValue());
    }

    tableHelper.refreshWidth(tableHelper.getWidth() + table.getCellSpacing());
    tableHelper.refreshHeight(tableHelper.getHeight() + table.getCellSpacing());
  }

  private static void alignSize(Table table, TableHelper tableHelper) {
    /*
     * Explicit dimensions are minimums for normal tables and exact outer dimensions for fixed
     * tables. Keep natural cell geometry when an exact outer size is too small to preserve positive
     * axis intervals.
     */
    Asserts.illegalArgument(tableHelper.horAxisNum() <= 1, "Only have one horizontal axis");
    boolean fixedSize = Boolean.TRUE.equals(table.getFixedSize());
    double heightDiff = table.getHeight() - tableHelper.getHeight();
    double heightIncr = (fixedSize ? heightDiff : Math.max(heightDiff, 0))
        / (tableHelper.horAxisNum() - 1);

    if (heightIncr != 0 && (!fixedSize || canAlignRows(tableHelper, heightIncr))) {
      double nextRangeLen = 0;
      for (int i = 0; i < tableHelper.horAxisNum(); i++) {
        TableAxis current = tableHelper.getRowAxis(i);
        double tmp = nextRangeLen;
        if (i < tableHelper.horAxisNum() - 1) {
          TableAxis next = tableHelper.getRowAxis(i + 1);
          nextRangeLen = next.position - current.position;
        }
        if (i > 0) {
          TableAxis pre = tableHelper.getRowAxis(i - 1);
          current.alignPos(pre.position + tmp + heightIncr, fixedSize);
        }

        if (!fixedSize) {
          tableHelper.refreshHeight(current.position);
        }
      }

      if (!fixedSize) {
        tableHelper.refreshHeight(tableHelper.getHeight() + table.getCellSpacing());
      }
    }
    if (fixedSize) {
      tableHelper.setHeight(table.getHeight());
    }

    Asserts.illegalArgument(tableHelper.verAxisNum() <= 1, "Only have one vertical axis");
    double widthDiff = table.getWidth() - tableHelper.getWidth();
    double widthIncr = (fixedSize ? widthDiff : Math.max(widthDiff, 0))
        / (tableHelper.verAxisNum() - 1);
    if (widthIncr != 0 && (!fixedSize || canAlignColumns(tableHelper, widthIncr))) {
      double nextRangeLen = 0;
      Entry<Integer, TableAxis> current = tableHelper.firstColAxis();
      while (current != null) {
        Entry<Integer, TableAxis> next = tableHelper.higherAxis(current.getValue());
        double tmp = nextRangeLen;
        if (next != null) {
          nextRangeLen = next.getValue().position - current.getValue().position;
        }
        Entry<Integer, TableAxis> pre = tableHelper.lowerAxis(current.getValue());
        if (pre != null) {
          current.getValue().alignPos(pre.getValue().position + tmp + widthIncr, fixedSize);
        }

        if (!fixedSize) {
          tableHelper.refreshWidth(current.getValue().position);
        }
        current = next;
      }
      if (!fixedSize) {
        tableHelper.refreshWidth(tableHelper.getWidth() + table.getCellSpacing());
      }
    }
    if (fixedSize) {
      tableHelper.setWidth(table.getWidth());
    }

    tableHelper.releaseVerAxes();
    tableHelper.releaseHorAxes();
  }

  private static boolean canAlignRows(TableHelper tableHelper, double increment) {
    for (int i = 0; i < tableHelper.horAxisNum() - 1; i++) {
      if (tableHelper.getRowAxis(i + 1).position
          - tableHelper.getRowAxis(i).position + increment <= 0) {
        return false;
      }
    }
    return true;
  }

  private static boolean canAlignColumns(TableHelper tableHelper, double increment) {
    Entry<Integer, TableAxis> current = tableHelper.firstColAxis();
    while (current != null) {
      Entry<Integer, TableAxis> next = tableHelper.higherAxis(current.getValue());
      if (next == null) {
        return true;
      }
      if (next.getValue().position - current.getValue().position + increment <= 0) {
        return false;
      }
      current = next;
    }
    return true;
  }

  private static void balanceRowChildTds(TableHelper tableHelper, TdBox tdBox, boolean refresh) {
    if (!tdBox.rowSpanThanOne() || !refresh) {
      return;
    }

    double incr = tdBox.heightWithoutPixel() / tdBox.rowAxisDiff();
    double position = tdBox.up.position;
    for (int i = tdBox.up.no + 1; i < tdBox.down.no; i++) {
      TableAxis childAxis = tableHelper.getRowAxis(i);
      childAxis.refreshPos(position + (i - tdBox.up.no) * incr);
    }
  }

  private static void balanceColChildTds(TableHelper tableHelper, TdBox tdBox, boolean refresh) {
    if (!tdBox.colSpanThanOne() || !refresh) {
      return;
    }

    TableAxis current = tdBox.left;
    int axisNum = 0;
    do {
      Entry<Integer, TableAxis> entry = tableHelper.higherAxis(current);
      if (entry == null || entry.getKey() >= tdBox.right.no) {
        break;
      }
      axisNum++;
      current = entry.getValue();
    } while (true);

    if (axisNum == 0) {
      return;
    }

    int i = 1;
    double incr = tdBox.widthWithoutPixel() / (axisNum + 1);
    double position = tdBox.left.position;
    current = tdBox.left;
    do {
      Entry<Integer, TableAxis> entry = tableHelper.higherAxis(current);
      if (entry == null || entry.getKey() >= tdBox.right.no) {
        break;
      }
      TableAxis childAxis = entry.getValue();
      childAxis.refreshPos(position + i++ * incr);
      current = childAxis;
    } while (true);
  }

  private static Assemble convertToAssemble(Table table, TableHelper tableHelper, String scope,
                                            LabelIdSpace idSpace) {
    double tabCellSpacing = (double) table.getCellSpacing() / (2 * PIXEL);
    double width = tableHelper.getWidth() / PIXEL;
    double height = tableHelper.getHeight() / PIXEL;
    AssembleBuilder assembleBuilder = Assemble.builder().width(width).height(height);
    /*
     * The table's own id stays the owner-local cell id so that ports keep resolving against it,
     * independently of whatever global identity the id space hands the generated node.
     */
    String tableId = table.getId();
    NodeBuilder nodeBuilder = Node.builder()
        .id(identity(tableId, scope, TABLE_SCOPE_STEP, idSpace, true))
        .width(width)
        .height(height)
        .href(table.getHref())
        .tooltip(table.getTooltip())
        .color(table.getColor())
        .fillColor(table.getBgColor())
        .penWidth(table.getBorder());
    if (CollectionUtils.isNotEmpty(table.getStyles())) {
      nodeBuilder.style(table.getStyles().toArray(new NodeStyle[0]));
    }
    assembleBuilder.addCell(0, 0, tableId, nodeBuilder.build());

    for (int r = 0; r < table.rowNum(); r++) {
      Tr tr = table.getTr(r);
      for (int c = 0; c < tr.colNum(); c++) {
        Td td = tr.getTd(c);
        TdBox tdBox = tableHelper.getBox(td);
        if (tdBox == null) {
          continue;
        }

        double horOffset = tdBox.left.pixelPosition() + 2 * tabCellSpacing;
        double verOffset = tdBox.up.pixelPosition() + 2 * tabCellSpacing;
        width = tdBox.width();
        height = tdBox.height();
        if (!td.isFixedSize(table)) {
          width -= (tabCellSpacing * 2);
          height -= (tabCellSpacing * 2);
        }

        String cellStep = "r" + r + "c" + c;
        String cellScope = scope == null ? null : scope + SCOPE_SEPARATOR + cellStep;
        NodeBuilder cellBuilder = Node.builder()
            .id(identity(td.getId(), scope, cellStep, idSpace, false))
            .width(width)
            .height(height)
            .href(td.getHref(table))
            .tooltip(td.getTooltip(table))
            .label(td.getText())
            .shape(td.getShape())
            .labeljust(td.getAlign(table))
            .labelloc(td.getValign(table))
            .penWidth(td.getBorder(table))
            .fontName(td.getFontName())
            .color(td.getColor())
            .fontColor(td.getFontColor())
            .fillColor(td.getBgColor())
            .fontSize(td.getFontSize());

        if (CollectionUtils.isNotEmpty(td.getStyles(table))) {
          cellBuilder.style(td.getStyles(table).toArray(new NodeStyle[0]));
        }

        Table childTable = td.getTable();
        if (childTable != null) {
          Assemble assemble = convertToAssemble(childTable, tdBox.tableHelper, cellScope, idSpace);
          cellBuilder.assemble(assemble);
        }

        LabelTag textTag = td.getTextTag();
        if (textTag != null) {
          LabelAttributes attrs = new LabelAttributes();
          attrs.setFontColor(td.getFontColor());
          attrs.setFontName(td.getFontName());
          attrs.setFontSize(td.getFontSize());
          Assemble assemble = toAssemble(textTag, attrs);
          cellBuilder.assemble(assemble);
        }

        String cellId = td.getPort() != null ? td.getPort() : td.getId();
        assembleBuilder.addCell(horOffset, verOffset, cellId, cellBuilder.build());
      }
    }

    return assembleBuilder.build();
  }

  /**
   * Returns the global {@link Node} identity of one generated table or cell node.
   *
   * <p>With an id space the authored id wins whenever nothing else in the graph has claimed it,
   * which is what keeps {@link org.graphper.draw.DrawGraph#nodeId(Node)} resolving the ids the
   * author wrote. Without one the two legacy shapes are reproduced unchanged: an unscoped
   * conversion keeps the authored id, and a scoped conversion renames every cell after its scope
   * but still leaves the table node named by its author.
   *
   * @param authoredId the {@code id} the author gave the table or cell, may be {@code null}
   * @param scope      identity scope of the owning label, {@code null} for an unscoped conversion
   * @param step       step identifying this table or cell inside the scope
   * @param idSpace    identity space of the graph, may be {@code null}
   * @param isTable    whether the node carries the table itself rather than a cell
   * @return the identity to build the node with, possibly {@code null}
   */
  private static String identity(String authoredId, String scope, String step,
                                 LabelIdSpace idSpace, boolean isTable) {
    if (scope == null) {
      return authoredId;
    }
    String scopedPath = scope + SCOPE_SEPARATOR + step;
    if (idSpace == null) {
      return isTable ? authoredId : scopedPath;
    }
    return idSpace.identity(authoredId, scopedPath);
  }

  private static void tdSize(Table table, TdBox tdBox, Td td, RootTableHelper rootTableHelper) {
    double height;
    double width;

    Table childTable = td.getTable();
    FlatPoint labelSize;
    if (childTable != null) {
      TableHelper tableHelper = new TableHelper(childTable);
      tableLayout(childTable, tableHelper, rootTableHelper);
      tdBox.tableHelper = tableHelper;
      width = Math.max(td.getWidth(), tableHelper.getWidth() + childTable.getCellSpacing());
      height = Math.max(td.getHeight(), tableHelper.getHeight() + childTable.getCellSpacing());
      labelSize = new FlatPoint(height, width);
    } else {
      width = td.getWidth();
      height = td.getHeight();
      if (td.isFixedSize(table)) {
        tdBox.size = new FlatPoint(height, width);
        return;
      }

      LabelTag textTag = td.getTextTag();
      if (textTag != null) {
        LabelAttributes attrs = new LabelAttributes();
        attrs.setFontName(td.getFontName());
        attrs.setFontSize(td.getFontSize());
        labelSize = LabelTagUtils.measure(textTag, attrs);
      } else {
        labelSize = FontUtils.measure(td.getText(), td.getFontName(), td.getFontSize(), 0);
      }
    }

    int margin = td.getCellPadding(table) + table.getCellSpacing();
    tdBox.size = td.getShape()
        .minContainerSize(labelSize.getHeight() + margin, labelSize.getWidth() + margin);
    Asserts.nullArgument(tdBox.size, "Node shape cannot return null outer box size");
    tdBox.size.setWidth(Math.max(width, tdBox.size.getWidth()));
    tdBox.size.setHeight(Math.max(height, tdBox.size.getHeight()));
  }

  /**
   * Lays out a {@link LabelTag} and returns each styled run with its position relative to the
   * label's top-left corner, in pixels.
   *
   * <p>Same layout pass as {@link #toAssemble(LabelTag, LabelAttributes)}; the two differ only in
   * what they do with the positioned runs. {@code toAssemble} turns each run into a sub-node for the
   * generic label pipeline, while record cells need to draw the runs directly as SVG text — an
   * {@code Assemble} cannot be nested inside a record cell without losing the cell tree that edge
   * ports resolve against. Sharing the layout pass is what keeps the two from drifting.
   *
   * @param labelTag  the rich text to lay out
   * @param labelAttrs the enclosing font context that inner tags refine
   * @return positioned runs in draw order, or an empty list when there is nothing to draw
   */
  public static List<PositionedText> toPositionedTexts(LabelTag labelTag,
                                                       LabelAttributes labelAttrs) {
    if (labelTag == null || labelAttrs == null) {
      return Collections.emptyList();
    }

    TextRows textRows = new TextRows();
    TextTagValue textTagValue = new TextTagValue(labelAttrs);
    accessLabelTag(textRows, labelTag, textTagValue, new FlatPoint(0, 0), 0);

    FlatPoint size = LabelTagUtils.measure(labelTag, labelAttrs);
    Asserts.nullArgument(size);

    List<PositionedText> texts = new ArrayList<>();
    textAlign(size, textRows, (xOffset, yOffset, cell) -> {
      NodeAttrs attrs = cell.cell.nodeAttrs();
      /*
       * NodeBuilder#width/#height scale by PIXEL on the way in, so the stored values are already in
       * the same unit as the offsets computed above. Only toAssemble has to divide, because
       * AssembleBuilder#addCell expects inches.
       */
      Double width = attrs.getWidth();
      Double height = attrs.getHeight();
      texts.add(new PositionedText(xOffset, yOffset,
                                   width == null ? 0 : width,
                                   height == null ? 0 : height,
                                   attrs.getLabel(), attrs.getFontName(),
                                   attrs.getFontSize() == null ? 0 : attrs.getFontSize(),
                                   attrs.getFontColor(), attrs.getFontStyles(),
                                   attrs.getLabelloc(), cell.scriptShift));
    });
    return texts;
  }

  // ------------------------------------------ label tag private methods ------------------------------------------
  private static void textAlign(FlatPoint size, TextRows textRows, AssembleBuilder builder) {
    textAlign(size, textRows,
              (xOffset, yOffset, cell) ->
                  builder.addCell(xOffset / PIXEL, yOffset / PIXEL, cell.cell));
  }

  private static void textAlign(FlatPoint size, TextRows textRows, TextCellConsumer consumer) {
    double width = size.getWidth();
    for (TextRow row : textRows.getRows()) {
      double horOffset = row.horOffset(width);

      for (TextCell cell : row.getCells()) {
        double xOffset = cell.xOffset + horOffset;
        double yOffset = cell.yOffset + row.verOffset(cell);

        consumer.accept(xOffset, yOffset, cell);
      }
    }
  }

  @FunctionalInterface
  private interface TextCellConsumer {

    void accept(double xOffset, double yOffset, TextCell cell);
  }

  /**
   * One styled run of rich text, positioned relative to the top-left of the label it belongs to.
   * Coordinates and sizes are in pixels.
   */
  public static class PositionedText {

    private final double x;

    private final double y;

    private final double width;

    private final double height;

    private final String text;

    private final String fontName;

    private final double fontSize;

    private final Color fontColor;

    private final Collection<FontStyle> fontStyles;

    private final Labelloc verAlign;

    private final boolean scriptShift;

    PositionedText(double x, double y, double width, double height, String text, String fontName,
                   double fontSize, Color fontColor, Collection<FontStyle> fontStyles,
                   Labelloc verAlign, boolean scriptShift) {
      this.scriptShift = scriptShift;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.text = text;
      this.fontName = fontName;
      this.fontSize = fontSize;
      this.fontColor = fontColor;
      this.fontStyles = fontStyles;
      this.verAlign = verAlign;
    }

    /**
     * Vertical placement of the glyphs inside this run's box. Subscript and superscript runs keep
     * the height of a full-size run but halve the font size, so the alignment is what puts them low
     * or high.
     *
     * @return the vertical alignment, never {@code null}
     */
    public Labelloc getVerAlign() {
      return verAlign == null ? Labelloc.TOP : verAlign;
    }

    /**
     * Whether this run is a subscript or superscript. Such a run keeps the box of a full-size run but
     * draws at half the font size, so {@link #getVerAlign()} decides where inside the box the glyphs
     * go. For every other run the box is the text's own measured size and the glyphs simply fill it.
     *
     * @return {@code true} for subscript and superscript runs
     */
    public boolean isScriptShift() {
      return scriptShift;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public double getWidth() {
      return width;
    }

    public double getHeight() {
      return height;
    }

    public String getText() {
      return text;
    }

    public String getFontName() {
      return fontName;
    }

    public double getFontSize() {
      return fontSize;
    }

    public Color getFontColor() {
      return fontColor;
    }

    public Collection<FontStyle> getFontStyles() {
      return fontStyles;
    }
  }

  private static double accessLabelTag(TextRows textRows, LabelTag labelTag,
                                       TextTagValue textTagValue, FlatPoint position,
                                       double currentLineHeight) {
    if (labelTag == null) {
      return currentLineHeight;
    }
    if (textTagValue.isMark(labelTag)) {
      throw new CycleDependencyException(
          "Cannot convert LabelTag due to LabelTag has cycle dependency");
    }
    textTagValue.mark(labelTag);

    for (BasicLabelTag tag : labelTag.getTags()) {
      TextTagValue temp = textTagValue.clone();
      currentLineHeight = accessLabelTag(textRows, tag, textTagValue, position, currentLineHeight);
      textTagValue = temp;
    }

    textTagValue.remove(labelTag);
    return currentLineHeight;
  }

  private static double accessLabelTag(TextRows textRows, BasicLabelTag labelTag,
                                       TextTagValue textTagValue, FlatPoint position,
                                       double currentLineHeight) {
    if (labelTag.getType() == LabelTagType.BR) {
      position.setX(0);
      position.setY(position.getY() + currentLineHeight);
      textRows.clearCurrentRow();
      return 10;
    }

    setTextValue(labelTag, textTagValue);
    if (StringUtils.isNotEmpty(labelTag.getText())) {
      return textTagToCell(textRows, labelTag, textTagValue, position, currentLineHeight);
    }

    return accessLabelTag(textRows, labelTag.getSubLabelTag(),
                          textTagValue, position, currentLineHeight);
  }

  private static void setTextValue(BasicLabelTag labelTag, TextTagValue textTagValue) {
    if (labelTag.getType() == LabelTagType.FONT) {
      FontLabelTag fontLabelTag = (FontLabelTag) labelTag;
      FontAttrs fontAttrs = fontLabelTag.getFontAttrs();
      if (fontAttrs != null) {
        Color color = fontAttrs.getColor();
        String face = fontAttrs.getFace();
        Integer pointSize = fontAttrs.getPointSize();

        if (color != null) {
          textTagValue.setFontColor(color);
        }
        if (StringUtils.isNotEmpty(face)) {
          textTagValue.setFontName(face);
        }
        if (pointSize != null) {
          textTagValue.setFontSize(pointSize);
        }
      }
    }

    if (labelTag.getType() == LabelTagType.BOLD) {
      textTagValue.setBold(true);
    }

    if (labelTag.getType() == LabelTagType.ITALIC) {
      textTagValue.setItalic(true);
    }

    if (labelTag.getType() == LabelTagType.OVERLINE) {
      textTagValue.setOverline(true);
    }

    if (labelTag.getType() == LabelTagType.UNDERLINE) {
      textTagValue.setUnderline(true);
    }

    if (labelTag.getType() == LabelTagType.STRIKETHROUGH) {
      textTagValue.setStrikethrough(true);
    }

    if (labelTag.getType() == LabelTagType.SUBSCRIPT) {
      textTagValue.subscript = true;
    }

    if (labelTag.getType() == LabelTagType.SUPERSCRIPT) {
      textTagValue.superscript = true;
    }

    textTagValue.setVerAlign(labelTag);
    textTagValue.setHorAlign(labelTag);
  }

  private static double textTagToCell(TextRows textRows, BasicLabelTag labelTag,
                                      TextTagValue textTagValue, FlatPoint position,
                                      double currentLineHeight) {
    if (StringUtils.isEmpty(labelTag.getText())) {
      return currentLineHeight;
    }

    String fontName = textTagValue.getFontName();
    double fontSize = textTagValue.getFontSize();
    FontStyle[] fontStyles = textTagValue.toMeasureFontStyles();
    FlatPoint size = FontUtils.measure(labelTag.getText(), fontName,
                                       fontSize, 0, fontStyles);

    if (textTagValue.subscript || textTagValue.superscript) {
      FlatPoint originalSize = size;
      fontSize /= 2;
      size = FontUtils.measure(labelTag.getText(), fontName, fontSize, 0, fontStyles);
      size.setHeight(originalSize.getHeight());
    }

    NodeBuilder cellBuilder = Node
        .builder()
        .penWidth(0)
        .fixedSize(true)
        .labelloc(Labelloc.TOP)
        .label(labelTag.getText())
        .fontSize(fontSize)
        .fontName(fontName)
        .fontColor(textTagValue.getFontColor())
        .width(size.getWidth() / PIXEL)
        .height(size.getHeight() / PIXEL);

    if (textTagValue.superscript) {
      cellBuilder.labelloc(Labelloc.TOP);
    }
    if (textTagValue.subscript) {
      cellBuilder.labelloc(Labelloc.BOTTOM);
    }

    setFontStyles(textTagValue, cellBuilder);

    TextCell textCell = new TextCell(position.getX(), position.getY(),
                                     cellBuilder.build(), textTagValue.verAlign,
                                     textTagValue.subscript || textTagValue.superscript);

    TextRow currentRow = textRows.getCurrentRow();
    currentRow.setRowHorAlign(textTagValue.horAlign);
    currentRow.addCells(textCell);
    currentRow.width = Math.max(currentRow.width, position.getX() + textCell.width());
    position.setX(position.getX() + size.getWidth());

    return Math.max(currentLineHeight, size.getHeight());
  }

  private static void setFontStyles(TextTagValue textTagValue, NodeBuilder cellBuilder) {
    FontStyle[] fontStyles = textTagValue.toFontStyles();
    if (fontStyles != null) {
      cellBuilder.fontStyle(fontStyles);
    }
  }

  private static class RootTableHelper extends TableHelper {

    private Set<Table> mark;

    private RootTableHelper(Table table) {
      super(table);
    }

    private void mark(Table table) {
      if (mark == null) {
        mark = new HashSet<>();
      }
      mark.add(table);
    }

    private boolean isMark(Table table) {
      if (mark == null) {
        return false;
      }
      return mark.contains(table);
    }

    private void remove(Table table) {
      if (mark == null) {
        return;
      }

      mark.remove(table);
    }

    private void releaseMark() {
      mark = null;
    }
  }

  private static class TableHelper {

    private boolean haveGreaterThan2ColSpan;

    private final FlatPoint tableSize;

    private Map<Td, TdBox> tdBoxMap;

    private Map<Integer, List<OccupyRange>> rowOccupyRanges;

    private TableAxis[] horAxes;

    private TreeMap<Integer, TableAxis> verAxes;

    private Map<Integer, Set<Integer>> pairMark;

    private TableHelper(Table table) {
      tableSize = new FlatPoint(0, 0);
      verAxes = new TreeMap<>();
      pairMark = new HashMap<>();
      horAxes = new TableAxis[table.rowNum() + 1];
    }

    private double getWidth() {
      return tableSize.getWidth();
    }

    private double getHeight() {
      return tableSize.getHeight();
    }

    private int verAxisNum() {
      return verAxes.size();
    }

    private int horAxisNum() {
      return horAxes.length;
    }

    private void refreshWidth(double width) {
      tableSize.setWidth(Math.max(width, tableSize.getWidth()));
    }

    private void refreshHeight(double height) {
      tableSize.setHeight(Math.max(height, tableSize.getHeight()));
    }

    private void setWidth(double width) {
      tableSize.setWidth(width);
    }

    private void setHeight(double height) {
      tableSize.setHeight(height);
    }

    private TdBox getBox(Td td) {
      if (tdBoxMap == null) {
        return null;
      }
      return tdBoxMap.get(td);
    }

    private void addPos(Td td, TdBox tdBox) {
      if (tdBoxMap == null) {
        tdBoxMap = new HashMap<>();
      }
      tdBoxMap.put(td, tdBox);
    }

    private void addOccupyRange(int row, int start, int end) {
      if (rowOccupyRanges == null) {
        rowOccupyRanges = new HashMap<>();
      }
      List<OccupyRange> occupyRanges = rowOccupyRanges.get(row);
      if (CollectionUtils.isEmpty(occupyRanges)) {
        occupyRanges = new ArrayList<>();
        rowOccupyRanges.put(row, occupyRanges);
        occupyRanges.add(new OccupyRange(start, end));
        return;
      }

      int mid;
      int left = 0;
      int right = occupyRanges.size() - 1;
      while (left < right) {
        mid = (left + right) / 2;
        OccupyRange or = occupyRanges.get(mid);
        if (or.start == start) {
          break;
        }

        if (or.start < start) {
          left = mid + 1;
        } else {
          right = mid;
        }
      }

      OccupyRange leftOr = occupyRanges.get(left);
      OccupyRange rightOr = occupyRanges.get(right);
      OccupyRange occupyRange = new OccupyRange(start, end);
      if (leftOr.start >= start) {
        occupyRanges.add(left, occupyRange);
      } else if (rightOr.start <= start) {
        occupyRanges.add(right + 1, occupyRange);
      } else {
        occupyRanges.add(left + 1, occupyRange);
      }
    }

    private OccupyRange getOccupyRange(int row, int idx) {
      if (rowOccupyRanges == null || rowOccupyRanges.isEmpty()) {
        return null;
      }

      List<OccupyRange> occupyRanges = rowOccupyRanges.get(row);
      if (CollectionUtils.isEmpty(occupyRanges)) {
        return null;
      }

      if (idx < 0 || idx >= occupyRanges.size()) {
        return null;
      }
      return occupyRanges.get(idx);
    }

    private TableAxis getColAxis(int colIdx) {
      return verAxes.computeIfAbsent(colIdx, TableAxis::new);
    }

    private TableAxis getRowAxis(int rowIdx) {
      Asserts.illegalArgument(rowIdx < 0 || rowIdx >= horAxes.length, "Wrong table axis index");
      TableAxis tableAx = horAxes[rowIdx];
      if (tableAx == null) {
        tableAx = new TableAxis(rowIdx);
        horAxes[rowIdx] = tableAx;
      }
      return tableAx;
    }

    private void markHaveTdBetweenColAxis(TableAxis leftAxis, TableAxis rightAxis) {
      pairMark.computeIfAbsent(leftAxis.no, l -> new HashSet<>(1)).add(rightAxis.no);
    }

    private Entry<Integer, TableAxis> firstColAxis() {
      return verAxes.firstEntry();
    }

    private Entry<Integer, TableAxis> lastColAxis() {
      return verAxes.lastEntry();
    }

    private Entry<Integer, TableAxis> lowerAxis(TableAxis colAxis) {
      return verAxes.lowerEntry(colAxis.no);
    }

    private Entry<Integer, TableAxis> higherAxis(TableAxis colAxis) {
      return verAxes.higherEntry(colAxis.no);
    }

    private boolean haveTdBetweenAxes(TableAxis left, TableAxis right) {
      Set<Integer> adjs = pairMark.get(left.no);
      return CollectionUtils.isNotEmpty(adjs) && adjs.contains(right.no);
    }

    private boolean notHaveTdBetweenAxes(TableAxis left, TableAxis right) {
      return !haveTdBetweenAxes(left, right);
    }

    private TableAxis removeColAxis(Integer colAxisNo) {
      return verAxes.remove(colAxisNo);
    }

    private void releaseHorAxes() {
      horAxes = null;
    }

    private void releaseVerAxes() {
      verAxes = null;
    }

    private void releaseAxesPairs() {
      pairMark = null;
    }

    private void clearOccupyRanges(int row) {
      if (rowOccupyRanges == null) {
        return;
      }
      rowOccupyRanges.remove(row);
    }
  }

  private static class TableAxis {

    private final int no;

    private double position;

    private TableAxis(int no) {
      this.no = no;
    }

    private boolean refreshPos(double pos) {
      if (pos > position) {
        this.position = pos;
        return true;
      }
      return false;
    }

    private boolean alignPos(double pos, boolean allowShrink) {
      if (!allowShrink) {
        return refreshPos(pos);
      }
      boolean changed = position != pos;
      position = pos;
      return changed;
    }

    private double pixelPosition() {
      return position / PIXEL;
    }

    @Override
    public String toString() {
      return "TableAxis{" + "no=" + no + ", position=" + position + '}';
    }
  }

  private static class TdBox {

    private TableAxis left;

    private TableAxis right;

    private final TableAxis up;

    private final TableAxis down;

    private FlatPoint size;

    private TableHelper tableHelper;

    public TdBox(TableAxis left, TableAxis right, TableAxis up, TableAxis down) {
      this.left = left;
      this.right = right;
      this.up = up;
      this.down = down;
    }

    private double widthWithoutPixel() {
      return Math.abs(right.position - left.position);
    }

    private double heightWithoutPixel() {
      return Math.abs(down.position - up.position);
    }

    private double width() {
      return Math.abs(right.position - left.position) / PIXEL;
    }

    private double height() {
      return Math.abs(down.position - up.position) / PIXEL;
    }

    private boolean rowSpanThanOne() {
      return down.no - up.no > 1;
    }

    private boolean colSpanThanOne() {
      return right.no - left.no > 1;
    }

    private int rowAxisDiff() {
      return down.no - up.no;
    }
  }

  private static class OccupyRange {

    private final int start;

    private final int end;

    private OccupyRange(int start, int end) {
      Asserts.illegalArgument(end < start, "end lower than start");
      this.start = start;
      this.end = end;
    }

    private boolean leftRange(int axisNo) {
      return axisNo < start;
    }

    private boolean inRange(int axisNo) {
      return axisNo >= start && axisNo < end;
    }
  }

  private static class TextTagValue extends LabelAttributes implements Cloneable {

    private boolean subscript;

    private boolean superscript;

    private Labelloc verAlign;

    private Labeljust horAlign;

    public TextTagValue(LabelAttributes labelAttrs) {
      setBold(labelAttrs.isBold());
      setItalic(labelAttrs.isItalic());
      setFontSize(labelAttrs.getFontSize());
      setFontName(labelAttrs.getFontName());
      setFontColor(labelAttrs.getFontColor());
      setOverline(labelAttrs.isOverline());
      setUnderline(labelAttrs.isUnderline());
      setStrikethrough(labelAttrs.isStrikethrough());
    }

    private void setHorAlign(BasicLabelTag labelTag) {
      LabelTagType type = labelTag.getType();
      switch (type) {
        case LEFT:
          this.horAlign = Labeljust.LEFT;
          break;
        case RIGHT:
          this.horAlign = Labeljust.RIGHT;
          break;
        case HORIZONTAL_CENTER:
          this.horAlign = Labeljust.CENTER;
          break;
        default:
          break;
      }
    }

    private void setVerAlign(BasicLabelTag labelTag) {
      LabelTagType type = labelTag.getType();
      switch (type) {
        case TOP:
          this.verAlign = Labelloc.TOP;
          break;
        case BOTTOM:
          this.verAlign = Labelloc.BOTTOM;
          break;
        case VERTICAL_CENTER:
          this.verAlign = Labelloc.CENTER;
          break;
        default:
          break;
      }
    }

    @Override
    public TextTagValue clone() {
      TextTagValue t = (TextTagValue) super.clone();
      t.subscript = subscript;
      t.superscript = subscript;
      t.verAlign = verAlign;
      t.horAlign = horAlign;
      return t;
    }
  }

  private static class TextRows {

    private TextRow currentRow;

    private List<TextRow> rows;

    private void addRows(TextRow row) {
      if (rows == null) {
        rows = new ArrayList<>();
      }
      rows.add(row);
    }

    private List<TextRow> getRows() {
      return rows == null ? Collections.emptyList() : rows;
    }

    private TextRow getCurrentRow() {
      if (currentRow == null) {
        currentRow = new TextRow();
        addRows(currentRow);
      }
      return currentRow;
    }

    private void clearCurrentRow() {
      this.currentRow = null;
    }
  }

  private static class TextRow {

    private double width;

    private double height;

    private Labeljust rowHorAlign;

    private List<TextCell> cells;

    private void addCells(TextCell cell) {
      if (cells == null) {
        cells = new ArrayList<>();
      }
      cells.add(cell);
      width += cell.width();
      height = Math.max(height, cell.height());
    }

    private List<TextCell> getCells() {
      return cells == null ? Collections.emptyList() : cells;
    }

    private void setRowHorAlign(Labeljust labeljust) {
      if (rowHorAlign == null || rowHorAlign == labeljust) {
        rowHorAlign = labeljust;
        return;
      }

      rowHorAlign = Labeljust.CENTER;
    }

    private double horOffset(double rowsWidth) {
      if (rowHorAlign == Labeljust.LEFT) {
        return 0;
      }

      double offset = rowsWidth - width;

      if (rowHorAlign == null || rowHorAlign == Labeljust.CENTER) {
        return offset / 2;
      }

      if (rowHorAlign == Labeljust.RIGHT) {
        return offset;
      }

      return 0;
    }

    private double verOffset(TextCell textCell) {
      Labelloc verAlign = textCell.verAlign;
      if (verAlign == Labelloc.TOP) {
        return 0;
      }

      double offset = height - textCell.height();

      if (verAlign == null || verAlign == Labelloc.CENTER) {
        return offset / 2;
      }

      if (verAlign == Labelloc.BOTTOM) {
        return offset;
      }

      return 0;
    }
  }

  private static class TextCell {

    private final Node cell;

    private final double xOffset;

    private final double yOffset;

    private final Labelloc verAlign;

    /**
     * True for subscript and superscript runs, which keep a full-size box but halve the font size.
     * For every other run the box is exactly the text's measured size, so the glyphs fill it.
     */
    private final boolean scriptShift;

    TextCell(double xOffset, double yOffset, Node cell, Labelloc verAlign, boolean scriptShift) {
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.cell = cell;
      this.verAlign = verAlign;
      this.scriptShift = scriptShift;
    }

    private double width() {
      Double width = cell.nodeAttrs().getWidth();
      return width == null ? 0 : width;
    }

    private double height() {
      Double height = cell.nodeAttrs().getHeight();
      return height == null ? 0 : height;
    }
  }
}
