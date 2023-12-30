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
import org.graphper.api.Assemble;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.ShapePosition;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.layout.HtmlConvert;
import org.graphper.util.Asserts;

/**
 * Common object for some box-type drawable elements.
 *
 * @author Jamison Jiang
 */
public abstract class ContainerDrawProp extends Rectangle implements ShapePosition, Serializable {

  private static final long serialVersionUID = 621795484015917002L;

  private String id;

  protected FlatPoint labelCenter;

  protected FlatPoint labelSize;

  protected Assemble assemble;

  public double topLowestHeight() {
    Asserts.nullArgument(margin(), "margin");
    Asserts.nullArgument(labelloc(), "labelloc");

    if (labelSize == null || labelloc() != Labelloc.TOP) {
      return getVerMargin();
    }

    return Math.max(getVerMargin(), labelSize.getHeight());
  }

  public double bottomLowestHeight() {
    Asserts.nullArgument(margin(), "margin");
    Asserts.nullArgument(labelloc(), "labelloc");

    if (labelSize == null || labelloc() != Labelloc.BOTTOM) {
      return getVerMargin();
    }

    return Math.max(getVerMargin(), labelSize.getHeight());
  }

  public double getHorMargin() {
    FlatPoint margin = margin();
    return margin.getWidth();
  }

  public double getVerMargin() {
    FlatPoint margin = margin();
    return margin.getHeight();
  }

  public void setId(String id) {
    this.id = id;
  }

  public String id() {
    return containerId() == null ? id : containerId();
  }

  public FlatPoint getLabelCenter() {
    return labelCenter;
  }

  public void setLabelCenter(FlatPoint labelCenter) {
    this.labelCenter = labelCenter;
  }

  public void setLabelSize(FlatPoint labelSize) {
    this.labelSize = labelSize;
  }

  public FlatPoint getLabelSize() {
    return labelSize;
  }

  public Assemble getAssemble() {
    if (assemble() != null) {
      return assemble();
    }
    return assemble;
  }

  @Override
  public double getX() {
    return (leftBorder + rightBorder) / 2;
  }

  @Override
  public double getY() {
    return (upBorder + downBorder) / 2;
  }

  @Override
  public double getHeight() {
    return Math.abs(downBorder - upBorder);
  }

  @Override
  public double getWidth() {
    return Math.abs(rightBorder - leftBorder);
  }

  @Override
  public ShapePropCalc shapeProp() {
    return NodeShapeEnum.RECT;
  }

  @Override
  public void flip() {
    super.flip();
    if (labelSize != null && !isNodeProp()) {
      labelSize.flip();
    }
  }

  public boolean isNodeProp() {
    return this instanceof NodeDrawProp;
  }

  public boolean isClusterProp() {
    return this instanceof ClusterDrawProp;
  }

  protected void convertTable(Table table) {
    if (table == null) {
      return;
    }
    assemble = HtmlConvert.toAssemble(table);
  }

  public boolean containsRounded() {
    return false;
  }

  protected abstract Labelloc labelloc();

  protected abstract FlatPoint margin();

  protected abstract String containerId();

  protected abstract Assemble assemble();
}
