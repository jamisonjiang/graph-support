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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.Port;
import org.graphper.util.CollectionUtils;

/**
 * Line attribute object.
 *
 * @author Jamison Jiang
 */
public class LineAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = 1851488933798704614L;

  String id;

  Boolean controlPoints;

  Boolean showboxes;

  ArrowShape arrowHead;

  ArrowShape arrowTail;

  Double arrowSize;

  Color color;

  Dir dir;

  Color fontColor;

  Double fontSize;

  String fontName;

  Boolean headclip;

  Boolean tailclip;

  Integer minlen;

  Double weight;

  String label;

  Collection<LineStyle> styles;

  String lhead;

  String ltail;

  Double radian;

  FloatLabel[] floatLabels;

  Port tailPort;

  Port headPort;

  String tailCell;

  String headCell;

  String href;

  String tooltip;

  Double penWidth;

  Table table;

  Assemble assemble;

  LineAttrs() {
  }

  public String getId() {
    return id;
  }

  public Boolean getControlPoints() {
    return controlPoints;
  }

  public Boolean getShowboxes() {
    return showboxes;
  }

  public Double getWeight() {
    return weight;
  }

  public Color getColor() {
    return color != null ? color : Color.BLACK;
  }

  public ArrowShape getArrowHead() {
    return arrowHead != null ? arrowHead : ArrowShape.NORMAL;
  }

  public ArrowShape getArrowTail() {
    return arrowTail != null ? arrowTail : ArrowShape.NORMAL;
  }

  public Double getArrowSize() {
    return arrowSize;
  }

  public String getLabel() {
    return label;
  }

  public Dir getDir() {
    return dir;
  }

  public Color getFontColor() {
    return fontColor != null ? fontColor : Color.BLACK;
  }

  public Double getFontSize() {
    return fontSize;
  }

  public String getFontName() {
    return fontName;
  }

  public Boolean getHeadclip() {
    return headclip;
  }

  public Boolean getTailclip() {
    return tailclip;
  }

  public Integer getMinlen() {
    return minlen;
  }

  public Collection<LineStyle> getStyles() {
    if (CollectionUtils.isEmpty(styles)) {
      return Collections.emptyList();
    }
    return styles;
  }

  public String getLhead() {
    return lhead;
  }

  public String getLtail() {
    return ltail;
  }

  public Double getRadian() {
    return radian;
  }

  public FloatLabel[] getFloatLabels() {
    return floatLabels;
  }

  public Port getTailPort() {
    return tailPort;
  }

  public Port getHeadPort() {
    return headPort;
  }

  public String getHref() {
    return href;
  }

  public String getTooltip() {
    return tooltip;
  }

  public Double getPenWidth() {
    return penWidth;
  }

  public String getTailCell() {
    return tailCell;
  }

  public String getHeadCell() {
    return headCell;
  }

  public Table getTable() {
    return table;
  }

  public Assemble getAssemble() {
    return assemble;
  }

  @Override
  public LineAttrs clone() {
    try {
      return (LineAttrs) super.clone();
    } catch (CloneNotSupportedException e) {
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
    LineAttrs lineAttrs = (LineAttrs) o;
    return Objects.equals(id, lineAttrs.id)
        && Objects.equals(controlPoints, lineAttrs.controlPoints)
        && Objects.equals(showboxes, lineAttrs.showboxes)
        && arrowHead == lineAttrs.arrowHead && arrowTail == lineAttrs.arrowTail
        && Objects.equals(arrowSize, lineAttrs.arrowSize) && Objects.equals(color, lineAttrs.color)
        && dir == lineAttrs.dir && Objects.equals(fontColor, lineAttrs.fontColor)
        && Objects.equals(fontSize, lineAttrs.fontSize)
        && Objects.equals(fontName, lineAttrs.fontName)
        && Objects.equals(headclip, lineAttrs.headclip)
        && Objects.equals(tailclip, lineAttrs.tailclip)
        && Objects.equals(minlen, lineAttrs.minlen)
        && Objects.equals(weight, lineAttrs.weight) && Objects.equals(label, lineAttrs.label)
        && Objects.equals(styles, lineAttrs.styles) && Objects.equals(lhead, lineAttrs.lhead)
        && Objects.equals(ltail, lineAttrs.ltail) && Objects.equals(radian, lineAttrs.radian)
        && Arrays.equals(floatLabels, lineAttrs.floatLabels)
        && tailPort == lineAttrs.tailPort && headPort == lineAttrs.headPort
        && Objects.equals(tailCell, lineAttrs.tailCell) && Objects.equals(
        headCell, lineAttrs.headCell) && Objects.equals(href, lineAttrs.href)
        && Objects.equals(tooltip, lineAttrs.tooltip)
        && Objects.equals(penWidth, lineAttrs.penWidth)
        && Objects.equals(table, lineAttrs.table) && Objects.equals(assemble, lineAttrs.assemble);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, controlPoints, showboxes, arrowHead, arrowTail, arrowSize, color,
                              dir, fontColor, fontSize, fontName, headclip, tailclip, minlen,
                              weight, label, styles, lhead, ltail, radian, tailPort, headPort,
                              tailCell, headCell, href, tooltip, penWidth, table, assemble);
    result = 31 * result + Arrays.hashCode(floatLabels);
    return result;
  }

  @Override
  public String toString() {
    return "LineAttrs{" +
        "id='" + id + '\'' +
        ", controlPoints=" + controlPoints +
        ", showboxes=" + showboxes +
        ", arrowHead=" + arrowHead +
        ", arrowTail=" + arrowTail +
        ", arrowSize=" + arrowSize +
        ", color=" + color +
        ", dir=" + dir +
        ", fontColor=" + fontColor +
        ", fontSize=" + fontSize +
        ", fontName='" + fontName + '\'' +
        ", headclip=" + headclip +
        ", tailclip=" + tailclip +
        ", minlen=" + minlen +
        ", weight=" + weight +
        ", label='" + label + '\'' +
        ", styles=" + styles +
        ", lhead='" + lhead + '\'' +
        ", ltail='" + ltail + '\'' +
        ", radian=" + radian +
        ", floatLabels=" + Arrays.toString(floatLabels) +
        ", tailPort=" + tailPort +
        ", headPort=" + headPort +
        ", tailCell='" + tailCell + '\'' +
        ", headCell='" + headCell + '\'' +
        ", href='" + href + '\'' +
        ", tooltip='" + tooltip + '\'' +
        ", penWidth=" + penWidth +
        ", table=" + table +
        ", assemble=" + assemble +
        '}';
  }
}
