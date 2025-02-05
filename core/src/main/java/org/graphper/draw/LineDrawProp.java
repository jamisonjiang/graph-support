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

package org.graphper.draw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Assemble;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.Table;
import org.graphper.def.FlatPoint;
import org.graphper.def.Vectors;
import org.graphper.layout.HtmlConvertor;
import org.graphper.layout.LabelAttributes;
import org.graphper.layout.dot.RouterBox;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.FloatLabel;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;

/**
 * Line's rendering description object.
 *
 * @author Jamison Jiang
 */
public class LineDrawProp extends ArrayList<FlatPoint> implements Serializable {

  private static final long serialVersionUID = 5529902024948360413L;

  private boolean isHeadStart;

  private final DrawGraph drawGraph;

  private final Line line;

  private FlatPoint start;

  private FlatPoint end;

  private FlatPoint labelSize;

  // label container center
  private FlatPoint labelCenter;

  private ArrowDrawProp arrowHead;

  private ArrowDrawProp arrowTail;

  private final LineAttrs lineAttrs;

  private String id;

  private boolean isBesselCurve;

  private HashMap<FloatLabel, FlatPoint> floatLabelFlatCenters;

  private List<RouterBox> routerBoxes;

  private Assemble assemble;

  private Map<FloatLabel, Assemble> floatAssembles;

  public LineDrawProp(Line line, LineAttrs lineAttrs, DrawGraph drawGraph) {
    Asserts.nullArgument(line, "line");
    Asserts.nullArgument(lineAttrs, "lineAttrs");
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.line = line;
    this.lineAttrs = lineAttrs;
    this.drawGraph = drawGraph;
    convertTables();
  }

  @Override
  public boolean addAll(Collection<? extends FlatPoint> c) {
    if (CollectionUtils.isEmpty(c)) {
      return false;
    }

    for (FlatPoint point : c) {
      add(point);
    }
    return true;
  }

  @Override
  public boolean add(FlatPoint point) {
    if (super.add(point)) {
      refreshDrawGraphArea(point);
      return true;
    }

    return false;
  }

  @Override
  public void add(int index, FlatPoint point) {
    super.add(index, point);
    refreshDrawGraphArea(point);
  }

  @Override
  public FlatPoint set(int index, FlatPoint point) {
    FlatPoint p = super.set(index, point);
    refreshDrawGraphArea(point);
    return p;
  }

  public boolean addAndNotRefreshDrawGraph(FlatPoint point) {
    return super.add(point);
  }

  public void addFloatLabelCenter(FloatLabel floatLabel, FlatPoint center) {
    if (floatLabel == null || center == null) {
      return;
    }

    if (floatLabelFlatCenters == null) {
      floatLabelFlatCenters = new HashMap<>();
    }
    floatLabelFlatCenters.put(floatLabel, center);
  }

  private void refreshDrawGraphArea(FlatPoint point) {
    if (point != null) {
      drawGraph.updateXAxisRange(point.getX() - 10);
      drawGraph.updateXAxisRange(point.getX() + 10);
      drawGraph.updateYAxisRange(point.getY() - 10);
      drawGraph.updateYAxisRange(point.getY() + 10);
    }
  }

  /**
   * Returns the line's id. First, it will be fetched from {@link LineAttrs}, if not fetched, then
   * the automatically assigned id will be used.
   *
   * @return id
   */
  public String id() {
    return lineAttrs.getId() != null ? lineAttrs.getId() : id;
  }

  /**
   * Returns the line.
   *
   * @return line
   */
  public Line getLine() {
    return line;
  }

  /**
   * Returns the line's id.
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns to the starting point.
   *
   * @return starting point
   */
  public FlatPoint getStart() {
    return start;
  }

  public void setStart(FlatPoint start) {
    this.start = start;
  }

  /**
   * Returns to the end point.
   *
   * @return end point
   */
  public FlatPoint getEnd() {
    return end;
  }

  public void setEnd(FlatPoint end) {
    this.end = end;
  }

  /**
   * Returns the property object of head arrow.
   *
   * @return property object of head arrow
   */
  public ArrowDrawProp getArrowHead() {
    return arrowHead;
  }

  public void setArrowHead(ArrowDrawProp arrowHead) {
    this.arrowHead = arrowHead;
  }

  /**
   * Returns the property object of tail arrow.
   *
   * @return property object of tail arrow
   */
  public ArrowDrawProp getArrowTail() {
    return arrowTail;
  }

  public void setArrowTail(ArrowDrawProp arrowTail) {
    this.arrowTail = arrowTail;
  }

  public FlatPoint getLabelCenter() {
    return labelCenter;
  }

  public FlatPoint getLabelSize() {
    return labelSize;
  }

  public void setLabelSize(FlatPoint labelSize) {
    this.labelSize = labelSize;
  }

  public void setLabelCenter(FlatPoint labelCenter) {
    this.labelCenter = labelCenter;
  }

  public LineAttrs lineAttrs() {
    return lineAttrs;
  }

  public boolean isBesselCurve() {
    return isBesselCurve;
  }

  public void markIsBesselCurve() {
    isBesselCurve = true;
  }

  public void markIsLineSegment() {
    isBesselCurve = false;
  }

  public List<RouterBox> getBoxes() {
    return routerBoxes;
  }

  public void setBoxes(List<RouterBox> routerBoxes) {
    this.routerBoxes = routerBoxes;
  }

  public Map<FloatLabel, FlatPoint> getFloatLabelFlatCenters() {
    return floatLabelFlatCenters != null ? floatLabelFlatCenters : Collections.emptyMap();
  }

  public void fakeInit() {
    this.start = Vectors.ZERO;
    this.end = Vectors.ZERO;
  }

  public boolean isSelfLoop() {
    return line.tail() == line.head();
  }

  public boolean isInit() {
    return start != null && end != null;
  }

  public boolean isHeadStart() {
    return isHeadStart;
  }

  public void setIsHeadStart(Node node) {
    if (node == null) {
      return;
    }
    this.isHeadStart = node == getLine().head();
  }

  public Assemble getAssemble() {
    if (lineAttrs.getAssemble() != null) {
      return lineAttrs.getAssemble();
    }
    return assemble;
  }

  public Iterable<Assemble> getFloatAssembles() {
    if (floatAssembles == null) {
      return Collections.emptyList();
    }
    return floatAssembles.values();
  }

  public Assemble getFloatAssemble(FloatLabel floatLabel) {
    if (floatAssembles == null || floatLabel == null) {
      return null;
    }
    return floatAssembles.get(floatLabel);
  }

  public boolean haveLabel() {
    return StringUtils.isNotEmpty(lineAttrs.getLabel()) || assemble != null;
  }

  private void convertTables() {
    assemble = convertToAssemble(lineAttrs.getTable(),  lineAttrs.getLabelTag());
    FloatLabel[] floatLabels = lineAttrs.getFloatLabels();
    if (floatLabels == null) {
      return;
    }

    for (FloatLabel floatLabel : floatLabels) {
      Assemble floatLabelAssemble = floatLabel.getAssemble();
      if (floatLabelAssemble == null && floatLabel.getTable() != null) {
        floatLabelAssemble = convertToAssemble(floatLabel.getTable(), floatLabel.getLabelTag());
      }

      if (floatLabelAssemble == null) {
        continue;
      }

      if (floatAssembles == null) {
        floatAssembles = new HashMap<>();
      }
      floatAssembles.put(floatLabel, floatLabelAssemble);
    }
  }

  private Assemble convertToAssemble(Table table, LabelTag labelTag) {
    if (table != null) {
      return HtmlConvertor.toAssemble(table);
    }
    Double fontSize = lineAttrs.getFontSize();
    fontSize = fontSize != null ? fontSize : 0;
    LabelAttributes labelAttrs = new LabelAttributes();
    labelAttrs.setFontSize(fontSize);
    labelAttrs.setFontName(lineAttrs.getFontName());
    labelAttrs.setFontColor(lineAttrs.getFontColor());
    return HtmlConvertor.toAssemble(labelTag, labelAttrs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    LineDrawProp that = (LineDrawProp) o;
    return isBesselCurve == that.isBesselCurve &&
        Objects.equals(line, that.line) &&
        Objects.equals(start, that.start) &&
        Objects.equals(end, that.end) &&
        Objects.equals(labelCenter, that.labelCenter) &&
        Objects.equals(arrowHead, that.arrowHead) &&
        Objects.equals(arrowTail, that.arrowTail) &&
        Objects.equals(lineAttrs, that.lineAttrs) &&
        Objects.equals(id, that.id) &&
        Objects.equals(routerBoxes, that.routerBoxes);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(super.hashCode(), line, start, end, labelCenter,
              arrowHead, arrowTail, lineAttrs, id, isBesselCurve, routerBoxes);
  }

  @Override
  public String toString() {
    return "LineDrawProp{" +
        "line=" + line +
        ", start=" + start +
        ", end=" + end +
        ", labelCenter=" + labelCenter +
        ", arrowHead=" + arrowHead +
        ", arrowTail=" + arrowTail +
        ", lineAttrs=" + lineAttrs +
        ", id='" + id + '\'' +
        ", isBesselCurve=" + isBesselCurve +
        ", routerBoxes=" + routerBoxes +
        '}';
  }
}
