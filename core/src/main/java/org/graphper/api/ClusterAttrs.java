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

import static org.graphper.util.FontUtils.DEFAULT_FONT;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.api.attributes.ClusterShapeEnum;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.def.FlatPoint;
import org.graphper.util.CollectionUtils;

/**
 * Cluster attribute object.
 *
 * @author Jamison Jiang
 */
public class ClusterAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = -2390770742172274269L;

  String id;

  String label;

  LabelTag labelTag;

  Labelloc labelloc = Labelloc.TOP;

  Labeljust labeljust = Labeljust.CENTER;

  ClusterShape shape = ClusterShapeEnum.RECT;

  Collection<ClusterStyle> styles;

  Color bgColor;

  Color color;

  Color fontColor;

  String fontName = DEFAULT_FONT;

  FlatPoint margin = new FlatPoint(10, 10);

  double fontSize = 16;

  String href;

  String tooltip;

  double penWidth = 1;

  Table table;

  Assemble assemble;

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public LabelTag getLabelTag() {
    return labelTag;
  }

  public Labelloc getLabelloc() {
    return labelloc;
  }

  public Labeljust getLabeljust() {
    return labeljust;
  }

  public ClusterShape getShape() {
    return shape;
  }

  public Collection<ClusterStyle> getStyles() {
    if (CollectionUtils.isEmpty(styles)) {
      return Collections.emptyList();
    }
    return styles;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public Color getColor() {
    return color;
  }

  public Color getFontColor() {
    return fontColor;
  }

  public String getFontName() {
    return fontName;
  }

  public FlatPoint getMargin() {
    return margin;
  }

  public double getFontSize() {
    return fontSize;
  }

  public String getHref() {
    return href;
  }

  public String getTooltip() {
    return tooltip;
  }

  public double getPenWidth() {
    return penWidth;
  }

  public Table getTable() {
    return table;
  }

  public Assemble getAssemble() {
    return assemble;
  }

  @Override
  public ClusterAttrs clone() {
    try {
      return (ClusterAttrs) super.clone();
    } catch (CloneNotSupportedException ignore) {
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClusterAttrs that = (ClusterAttrs) o;
    return Double.compare(that.fontSize, fontSize) == 0
        && Double.compare(that.penWidth, penWidth) == 0 && Objects.equals(id, that.id)
        && Objects.equals(label, that.label) && Objects.equals(labelTag, that.labelTag)
        && labelloc == that.labelloc
        && labeljust == that.labeljust && Objects.equals(shape, that.shape)
        && Objects.equals(styles, that.styles) && Objects.equals(bgColor, that.bgColor)
        && Objects.equals(color, that.color) && Objects.equals(fontColor, that.fontColor)
        && Objects.equals(fontName, that.fontName) && Objects.equals(margin, that.margin)
        && Objects.equals(href, that.href) && Objects.equals(tooltip, that.tooltip)
        && Objects.equals(table, that.table) && Objects.equals(assemble, that.assemble);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, label, labelTag, labelloc, labeljust, shape, styles, bgColor, color, fontColor,
                        fontName, margin, fontSize, href, tooltip, penWidth, table, assemble);
  }

  @Override
  public String toString() {
    return "ClusterAttrs{" +
        "id='" + id + '\'' +
        ", label='" + label + '\'' +
        ", labelTag='" + labelTag + '\'' +
        ", labelloc=" + labelloc +
        ", labeljust=" + labeljust +
        ", shape=" + shape +
        ", styles=" + styles +
        ", bgColor=" + bgColor +
        ", color=" + color +
        ", fontColor=" + fontColor +
        ", fontName='" + fontName + '\'' +
        ", margin=" + margin +
        ", fontSize=" + fontSize +
        ", href='" + href + '\'' +
        ", tooltip='" + tooltip + '\'' +
        ", penWidth=" + penWidth +
        ", table=" + table +
        ", assemble=" + assemble +
        '}';
  }
}
