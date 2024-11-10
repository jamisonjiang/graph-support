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
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.InitPos;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.util.EnvProp;

/**
 * Graphviz attribute object.
 *
 * @author Jamison Jiang
 */
public class GraphAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = -1741433881093744063L;

  Color bgColor;

  Splines splines;

  Color fontColor = Color.BLACK;

  Rankdir rankdir = EnvProp.defaultRankdir();

  Layout layout = Layout.DOT;

  double nodeSep = 0.5 * Graphviz.PIXEL;

  String label;

  String fontName = DEFAULT_FONT;

  Labelloc labelloc = Labelloc.BOTTOM;

  Labeljust labeljust = Labeljust.CENTER;

  int nslimit = 100000;

  int nslimit1 = Integer.MAX_VALUE;

  double rankSep = (double) Graphviz.PIXEL / 2;

  FlatPoint scale = new FlatPoint(1, 1);

  FlatPoint margin = new FlatPoint(20, 20);

  int mclimit = 8;

  double fontSize = (double) Graphviz.PIXEL / 2;

  boolean compound = false;

  boolean showGrid = false;

  String href;

  String tooltip;

  Table table;

  Assemble assemble;

  int maxiter = 100;

  double k = 1;

  boolean overlap = false;

  InitPos initPos = InitPos.SECTOR;

  public Splines getSplines() {
    return splines;
  }

  public Color getFontColor() {
    return fontColor;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public Rankdir getRankdir() {
    return rankdir;
  }

  public Layout getLayout() {
    return layout;
  }

  public double getNodeSep() {
    return nodeSep;
  }

  public String getFontName() {
    return fontName;
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

  public int getNslimit() {
    return nslimit;
  }

  public int getNslimit1() {
    return nslimit1;
  }

  public double getRankSep() {
    return rankSep;
  }

  public FlatPoint getScale() {
    return scale;
  }

  public int getMclimit() {
    return mclimit;
  }

  public FlatPoint getMargin() {
    return margin;
  }

  public double getFontSize() {
    return fontSize;
  }

  public boolean isCompound() {
    return compound;
  }

  public boolean isShowGrid() {
    return showGrid;
  }

  public String getHref() {
    return href;
  }

  public String getTooltip() {
    return tooltip;
  }

  public Table getTable() {
    return table;
  }

  public Assemble getAssemble() {
    return assemble;
  }

  public int getMaxiter() {
    return maxiter;
  }

  public double getK() {
    return k;
  }

  public boolean isOverlap() {
    return overlap;
  }

  public InitPos getInitPos() {
    return initPos;
  }

  @Override
  public GraphAttrs clone() {
    try {
      return (GraphAttrs) super.clone();
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
    GraphAttrs that = (GraphAttrs) o;
    return Double.compare(that.nodeSep, nodeSep) == 0 && nslimit == that.nslimit
        && nslimit1 == that.nslimit1 && Double.compare(that.rankSep, rankSep) == 0
        && mclimit == that.mclimit && Double.compare(that.fontSize, fontSize) == 0
        && compound == that.compound && showGrid == that.showGrid && maxiter == that.maxiter
        && Double.compare(that.k, k) == 0 && overlap == that.overlap
        && Objects.equals(bgColor, that.bgColor) && splines == that.splines
        && Objects.equals(fontColor, that.fontColor) && rankdir == that.rankdir
        && layout == that.layout && Objects.equals(label, that.label)
        && Objects.equals(fontName, that.fontName) && labelloc == that.labelloc
        && labeljust == that.labeljust && Objects.equals(scale, that.scale)
        && Objects.equals(margin, that.margin) && Objects.equals(href, that.href)
        && Objects.equals(tooltip, that.tooltip) && Objects.equals(table, that.table)
        && Objects.equals(assemble, that.assemble) && initPos == that.initPos;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bgColor, splines, fontColor, rankdir, layout, nodeSep, label, fontName,
                        labelloc, labeljust, nslimit, nslimit1, rankSep, scale, margin, mclimit,
                        fontSize, compound, showGrid, href, tooltip, table, assemble, maxiter, k,
                        overlap, initPos);
  }

  @Override
  public String toString() {
    return "GraphAttrs{" +
        "bgColor=" + bgColor +
        ", splines=" + splines +
        ", fontColor=" + fontColor +
        ", rankdir=" + rankdir +
        ", layout=" + layout +
        ", nodeSep=" + nodeSep +
        ", label='" + label + '\'' +
        ", fontName='" + fontName + '\'' +
        ", labelloc=" + labelloc +
        ", labeljust=" + labeljust +
        ", nslimit=" + nslimit +
        ", nslimit1=" + nslimit1 +
        ", rankSep=" + rankSep +
        ", scale=" + scale +
        ", margin=" + margin +
        ", mclimit=" + mclimit +
        ", fontSize=" + fontSize +
        ", compound=" + compound +
        ", showGrid=" + showGrid +
        ", href='" + href + '\'' +
        ", tooltips='" + tooltip + '\'' +
        ", table=" + table +
        ", assemble=" + assemble +
        ", maxiter=" + maxiter +
        ", k=" + k +
        ", overlap=" + overlap +
        ", initPos=" + initPos +
        '}';
  }
}
