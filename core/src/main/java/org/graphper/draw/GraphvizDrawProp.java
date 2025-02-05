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
import java.util.List;
import org.graphper.api.Assemble;
import org.graphper.api.GraphAttrs;
import org.graphper.api.Graphviz;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.layout.LabelAttributes;
import org.graphper.layout.OrthoVisGraph.Segment;
import org.graphper.util.Asserts;

/**
 * Cluster's rendering description object.
 *
 * @author Jamison Jiang
 */
public class GraphvizDrawProp extends ContainerDrawProp implements Serializable {

  private static final long serialVersionUID = 4820693703994091283L;

  private Graphviz graphviz;

  private List<Segment> grid;

  public GraphvizDrawProp(Graphviz graphviz) {
    Asserts.nullArgument(graphviz, "graphviz");
    this.graphviz = graphviz;
    convertToAssemble(graphviz.graphAttrs().getTable(),
                      graphviz.graphAttrs().getLabelTag());
  }

  /**
   * Returns the graphviz
   *
   * @return graphviz
   */
  public Graphviz getGraphviz() {
    return graphviz;
  }

  public void setGraphviz(Graphviz graphviz) {
    this.graphviz = graphviz;
  }

  @Override
  public Labelloc labelloc() {
    return graphviz.graphAttrs().getLabelloc();
  }

  @Override
  public Labeljust labeljust() {
    return graphviz.graphAttrs().getLabeljust();
  }

  @Override
  public FlatPoint margin() {
    return graphviz.graphAttrs().getMargin();
  }

  @Override
  public String containerId() {
    return graphviz.id();
  }

  @Override
  public Assemble assemble() {
    return graphviz.graphAttrs().getAssemble();
  }

  @Override
  public LabelAttributes labelAttrs() {
    GraphAttrs graphAttrs = graphviz.graphAttrs();
    LabelAttributes labelAttrs = new LabelAttributes();
    labelAttrs.setFontColor(graphAttrs.getFontColor());
    labelAttrs.setFontName(graphAttrs.getFontName());
    labelAttrs.setFontSize(graphAttrs.getFontSize());
    return labelAttrs;
  }

  /**
   * Returns the line segment used to draw the grid in
   * {@link Splines#ORTHO}.
   *
   * @return the line segments of grid
   */
  public List<Segment> getGrid() {
    return grid;
  }

  public void addSegment(Segment segment) {
    if (segment == null) {
      return;
    }

    if (grid == null) {
      grid = new ArrayList<>();
    }
    grid.add(segment);
  }
}
