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
import java.util.Objects;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.def.FlatPoint;

/**
 * Cluster attribute object.
 *
 * @author Jamison Jiang
 */
public class ClusterAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = -2390770742172274269L;

  String id;

  String label;

  Labelloc labelloc = Labelloc.TOP;

  Labeljust labeljust = Labeljust.CENTER;

  ClusterStyle style;

  Color bgColor;

  Color color;

  Color fontColor;

  String fontName = DEFAULT_FONT;

  FlatPoint margin = new FlatPoint(10, 10);

  double fontSize = 16;

  String href;

  double penWidth = 1;

  Table table;

  Assemble assemble;

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public Labelloc getLabelloc() {
    return labelloc;
  }

  public Labeljust getLabeljust() {
    return labeljust;
  }

  public ClusterStyle getStyle() {
    return style;
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
        && Double.compare(that.penWidth, penWidth) == 0
        && Objects.equals(id, that.id)
        && Objects.equals(label, that.label)
        && labelloc == that.labelloc
        && labeljust == that.labeljust
        && Objects.equals(style, that.style)
        && Objects.equals(bgColor, that.bgColor)
        && Objects.equals(color, that.color)
        && Objects.equals(fontColor, that.fontColor)
        && Objects.equals(fontName, that.fontName)
        && Objects.equals(margin, that.margin)
        && Objects.equals(href, that.href)
        && Objects.equals(table, that.table)
        && Objects.equals(assemble, that.assemble);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, label, labelloc, labeljust, style, bgColor, color, fontColor, fontName,
                        margin, fontSize, href, penWidth, table, assemble);
  }

  @Override
  public String toString() {
    return "ClusterAttrs{" +
        "id='" + id + '\'' +
        ", label='" + label + '\'' +
        ", labelloc=" + labelloc +
        ", labeljust=" + labeljust +
        ", style=" + style +
        ", bgColor=" + bgColor +
        ", color=" + color +
        ", fontColor=" + fontColor +
        ", fontName='" + fontName + '\'' +
        ", margin=" + margin +
        ", fontSize=" + fontSize +
        ", href='" + href + '\'' +
        ", penWidth=" + penWidth +
        ", table=" + table +
        ", assemble=" + assemble +
        '}';
  }
}
