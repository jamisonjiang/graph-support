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
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.ShapePosition;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.layout.HtmlConvertor;
import org.graphper.layout.HtmlConvertor.LabelIdSpace;
import org.graphper.layout.LabelAttributes;
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

  /**
   * Html-like label registered by the subclass, converted on first use. See {@link
   * #convertToAssemble(Table, LabelTag, String)}.
   */
  private Table htmlTable;

  private LabelTag htmlLabelTag;

  private String htmlScope;

  private boolean htmlConverted;

  private LabelIdSpace labelIdSpace;

  /** Returns the minimum top clearance required by the margin and label. */
  public double topLowestHeight() {
    Asserts.nullArgument(margin(), "margin");
    Asserts.nullArgument(labelloc(), "labelloc");

    if (labelSize == null || labelloc() != Labelloc.TOP) {
      return getVerMargin();
    }

    return Math.max(getVerMargin(), labelSize.getHeight());
  }

  /** Returns the minimum bottom clearance required by the margin and label. */
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

  /** Returns the label assembly, converting a registered HTML-like label on first use. */
  public Assemble getAssemble() {
    if (assemble() != null) {
      return assemble();
    }
    if (assemble == null && !htmlConverted) {
      htmlConverted = true;
      if (htmlTable != null) {
        // Without a scope there is nothing for the id space to fall back to, so the label keeps
        // the authored ids exactly as an unscoped conversion always has.
        String scope = labelScope();
        assemble =
            HtmlConvertor.toAssemble(htmlTable, scope, scope == null ? null : labelIdSpace());
      } else {
        assemble = HtmlConvertor.toAssemble(htmlLabelTag, labelAttrs());
      }
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

  protected void convertToAssemble(Table table, LabelTag labelTag) {
    convertToAssemble(table, labelTag, null);
  }

  /**
   * Registers the html-like label of this element. The conversion itself is deferred to the first
   * {@link #getAssemble()}, because neither of the two things it needs is available while the
   * element is being constructed: the identity scope of an owner is not always known then - a
   * cluster only learns its number afterwards - and the {@link LabelIdSpace} is only handed over
   * once the element joins its {@link DrawGraph}.
   *
   * @param table table label, takes precedence over {@code labelTag}
   * @param labelTag rich text label
   * @param scope identity scope of this label, or {@code null} when the subclass answers {@link
   *     #labelScope()} itself
   */
  protected void convertToAssemble(Table table, LabelTag labelTag, String scope) {
    this.htmlTable = table;
    this.htmlLabelTag = labelTag;
    this.htmlScope = scope;
    this.htmlConverted = false;
    this.assemble = null;
  }

  /**
   * Identity scope of this element's html-like label.
   *
   * <p>Every owner of an html label in a graph must answer something different here. The scope is
   * the fallback identity of the table and cell nodes generated for the label: it is what they are
   * named when the author gave them no id, or gave them one that something else in the graph
   * already owns. See {@link LabelIdSpace}.
   *
   * @return the identity scope, or {@code null} to keep the author's ids
   */
  protected String labelScope() {
    return htmlScope;
  }

  /**
   * Attaches the identity space that this element's html-like label draws the identity of its
   * generated nodes from. Must be called before the label is first converted, which the {@link
   * DrawGraph} does by attaching the space when the element joins the graph.
   *
   * @param labelIdSpace identity space of the graph the element belongs to
   */
  public void setLabelIdSpace(LabelIdSpace labelIdSpace) {
    this.labelIdSpace = labelIdSpace;
  }

  /**
   * Returns the identity space of the graph this element belongs to, or {@code null} when the
   * element does not belong to one - a draw property built outside a layout has no graph to share
   * identities with, and falls back to naming every generated cell after its scope.
   *
   * @return identity space of the graph, or {@code null}
   */
  protected LabelIdSpace labelIdSpace() {
    return labelIdSpace;
  }

  public boolean containsRounded() {
    return false;
  }

  public abstract Labelloc labelloc();

  public abstract Labeljust labeljust();

  public abstract FlatPoint margin();

  public abstract String containerId();

  public abstract Assemble assemble();

  public abstract LabelAttributes labelAttrs();
}
