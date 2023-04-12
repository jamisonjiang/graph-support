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
import java.util.Objects;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.def.FlatPoint;
import org.graphper.api.attributes.NodeShapeEnum;

/**
 * Node attribute object.
 *
 * @author Jamison Jiang
 */
public class NodeAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = 4923498915561027736L;

  String id;

  Double height;

  Double width;

  Color color;

  Color fillColor;

  Color fontColor;

  String fontName;

  String label;

  Labeljust labeljust;

  Labelloc labelloc;

  FlatPoint margin;

  NodeShape shape;

  Boolean fixedSize;

  Double fontSize;

  NodeStyle style;

  Integer sides;

  String href;

  String image;

  FlatPoint imageSize;

  Double penWidth;

  Table table;

  Assemble assemble;

  public NodeAttrs() {
  }

  /*--------------------------------------------------- attributeVal ---------------------------------------------------*/

  public String getId() {
    return id;
  }

  public Color getColor() {
    return color != null ? color : Color.BLACK;
  }

  public Color getFillColor() {
    return fillColor;
  }

  public Color getFontColor() {
    return fontColor != null ? fontColor : Color.BLACK;
  }

  public String getLabel() {
    return label;
  }

  public NodeShape getNodeShape() {
    return shape == null ? NodeShapeEnum.ELLIPSE : shape;
  }

  public String getFontName() {
    return fontName;
  }

  public Boolean getFixedSize() {
    return fixedSize;
  }

  public Double getFontSize() {
    return fontSize;
  }

  public NodeStyle getStyle() {
    return style;
  }

  public Double getHeight() {
    return height;
  }

  public Double getWidth() {
    return width;
  }

  public Labeljust getLabeljust() {
    return labeljust;
  }

  public Labelloc getLabelloc() {
    return labelloc;
  }

  public FlatPoint getMargin() {
    return margin;
  }

  public Integer getSides() {
    return sides;
  }

  public String getHref() {
    return href;
  }

  public String getImage() {
    return image;
  }

  public FlatPoint getImageSize() {
    return imageSize;
  }

  public Double getPenWidth() {
    return penWidth;
  }

  public Table getTable() {
    return table;
  }

  public Assemble getAssemble() {
    return assemble;
  }

  @Override
  public NodeAttrs clone() {
    try {
      return (NodeAttrs) super.clone();
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
    NodeAttrs nodeAttrs = (NodeAttrs) o;
    return Objects.equals(id, nodeAttrs.id)
        && Objects.equals(height, nodeAttrs.height)
        && Objects.equals(width, nodeAttrs.width)
        && Objects.equals(color, nodeAttrs.color)
        && Objects.equals(fillColor, nodeAttrs.fillColor)
        && Objects.equals(fontColor, nodeAttrs.fontColor)
        && Objects.equals(fontName, nodeAttrs.fontName)
        && Objects.equals(label, nodeAttrs.label)
        && labeljust == nodeAttrs.labeljust
        && labelloc == nodeAttrs.labelloc
        && Objects.equals(margin, nodeAttrs.margin)
        && Objects.equals(shape, nodeAttrs.shape)
        && Objects.equals(fixedSize, nodeAttrs.fixedSize)
        && Objects.equals(fontSize, nodeAttrs.fontSize)
        && Objects.equals(style, nodeAttrs.style)
        && Objects.equals(sides, nodeAttrs.sides)
        && Objects.equals(href, nodeAttrs.href)
        && Objects.equals(image, nodeAttrs.image)
        && Objects.equals(imageSize, nodeAttrs.imageSize)
        && Objects.equals(penWidth, nodeAttrs.penWidth)
        && Objects.equals(table, nodeAttrs.table)
        && Objects.equals(assemble, nodeAttrs.assemble);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, height, width, color, fillColor, fontColor, fontName, label, labeljust,
                        labelloc, margin, shape, fixedSize, fontSize, style, sides, href, image,
                        imageSize, penWidth, table, assemble);
  }

  @Override
  public String toString() {
    return "NodeAttrs{" +
        "id='" + id + '\'' +
        ", height=" + height +
        ", width=" + width +
        ", color=" + color +
        ", fillColor=" + fillColor +
        ", fontColor=" + fontColor +
        ", fontName='" + fontName + '\'' +
        ", label='" + label + '\'' +
        ", labeljust=" + labeljust +
        ", labelloc=" + labelloc +
        ", margin=" + margin +
        ", shape=" + shape +
        ", fixedSize=" + fixedSize +
        ", fontSize=" + fontSize +
        ", style=" + style +
        ", sides=" + sides +
        ", href='" + href + '\'' +
        ", image='" + image + '\'' +
        ", imageSize=" + imageSize +
        ", penWidth=" + penWidth +
        ", table=" + table +
        ", assemble=" + assemble +
        '}';
  }
}
