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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.graphper.api.Assemble;
import org.graphper.api.Assemble.AssembleBuilder;
import org.graphper.api.Graphviz;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.graphper.api.Html.Tr;
import org.graphper.api.Node;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.def.CycleDependencyException;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * Translate the data of the original html-like structure into a lower-level {@link Assemble}.
 *
 * @author Jamison Jiang
 */
public class HtmlConvert {

  private HtmlConvert() {
  }

  /**
   * Parses the data structure of the table, and automatically performs table layout and converts it
   * into a lower-level {@link Assemble} data structure for rendering.
   *
   * @param table table
   * @return assemble
   */
  public static Assemble toAssemble(Table table) {
    if (table == null) {
      return null;
    }

    Asserts.illegalArgument(table.rowNum() == 0, "Empty tr in table");
    RootTableHelper tableHelper = new RootTableHelper(table);
    tableLayout(table, tableHelper, tableHelper);
    tableHelper.releaseMark();

    return convertToAssemble(table, tableHelper);
  }

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
     * If the table is manually set with a width and height,
     * if the width or height is greater than the currently
     * calculated width or height, there need to lengthen the
     * width and height of the corresponding cell
     */
    double heightIncr =
        Math.max(table.getHeight() - tableHelper.getHeight(), 0) / tableHelper.horAxisNum();

    if (heightIncr > 0) {
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
          current.refreshPos(pre.position + tmp + heightIncr);
        }

        tableHelper.refreshHeight(current.position);
      }

      tableHelper.refreshHeight(tableHelper.getHeight() + table.getCellSpacing());
    }

    double widthIncr =
        Math.max(table.getWidth() - tableHelper.getWidth(), 0) / tableHelper.verAxisNum();
    if (widthIncr > 0) {
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
          current.getValue().refreshPos(pre.getValue().position + tmp + widthIncr);
        }

        tableHelper.refreshWidth(current.getValue().position);
        current = next;
      }
      tableHelper.refreshWidth(tableHelper.getWidth() + table.getCellSpacing());
    }

    tableHelper.releaseVerAxes();
    tableHelper.releaseHorAxes();
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

  private static Assemble convertToAssemble(Table table, TableHelper tableHelper) {
    double tabCellSpacing = (double) table.getCellSpacing() / (2 * Graphviz.PIXEL);
    double width = tableHelper.getWidth() / Graphviz.PIXEL;
    double height = tableHelper.getHeight() / Graphviz.PIXEL;
    AssembleBuilder assembleBuilder = Assemble.builder().width(width).height(height);
    assembleBuilder.addCell(0, 0,
                            Node.builder()
                                .id(table.getId())
                                .width(width)
                                .height(height)
                                .href(table.getHref())
                                .style(table.getStyle())
                                .color(table.getColor())
                                .fillColor(table.getBgColor())
                                .penWidth(table.getBorder())
                                .build());

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

        NodeBuilder cellBuilder = Node.builder()
            .id(td.getId())
            .width(width)
            .height(height)
            .href(td.getHref())
            .label(td.getText())
            .shape(td.getShape())
            .labeljust(td.getAlign(table))
            .labelloc(td.getValign(table))
            .penWidth(td.getBorder(table))
            .style(td.getStyle(table))
            .fontName(td.getFontName())
            .color(td.getColor())
            .fontColor(td.getFontColor())
            .fillColor(td.getBgColor())
            .fontSize(td.getFontSize());

        Table childTable = td.getTable();
        if (childTable != null) {
          Assemble assemble = convertToAssemble(childTable, tdBox.tableHelper);
          cellBuilder.assemble(assemble);
        }

        assembleBuilder.addCell(horOffset, verOffset, cellBuilder.build());
      }
    }

    return assembleBuilder.build();
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

      labelSize = LabelSizeHelper.measure(td.getText(), td.getFontName(),
                                          td.getFontSize(), 0);
    }

    int margin = td.getCellPadding(table) + table.getCellSpacing();
    tdBox.size = td.getShape().minContainerSize(labelSize.getHeight() + margin,
                                                labelSize.getWidth() + margin);
    tdBox.size.setWidth(Math.max(width, tdBox.size.getWidth()));
    tdBox.size.setHeight(Math.max(height, tdBox.size.getHeight()));
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
      if (rowOccupyRanges == null || rowOccupyRanges.size() == 0) {
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

    private double pixelPosition() {
      return position / Graphviz.PIXEL;
    }

    @Override
    public String toString() {
      return "TableAxis{" +
          "no=" + no +
          ", position=" + position +
          '}';
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
      return Math.abs(right.position - left.position) / Graphviz.PIXEL;
    }

    private double height() {
      return Math.abs(down.position - up.position) / Graphviz.PIXEL;
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
}
