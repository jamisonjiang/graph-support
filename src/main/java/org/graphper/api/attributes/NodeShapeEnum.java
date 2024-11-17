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

package org.graphper.api.attributes;

import org.graphper.api.Graphviz;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.api.NodeAttrs;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.CirclePropCalc;
import org.graphper.api.ext.CylinderPropCalc;
import org.graphper.api.ext.DiamondPropCalc;
import org.graphper.api.ext.EllipsePropCalc;
import org.graphper.api.ext.NodeShapePost;
import org.graphper.api.ext.NotePropCalc;
import org.graphper.api.ext.ParallelogramPropCalc;
import org.graphper.api.ext.PlainPropCalc;
import org.graphper.api.ext.PointPropCalc;
import org.graphper.api.ext.RecordPropCalc;
import org.graphper.api.ext.RectanglePropCalc;
import org.graphper.api.ext.RegularPolylinePropCalc;
import org.graphper.api.ext.RegularPolylinePropCalc.RegularPolyShapePost;
import org.graphper.api.ext.ShapeCenterCalc;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.api.ext.StarPropCalc;
import org.graphper.api.ext.SymmetryShapeCenterCalc;
import org.graphper.api.ext.TrapeziumPropCalc;
import org.graphper.api.ext.TrianglePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;

/**
 * Enumeration of node shapes supported by the system by default.
 *
 * @author Jamison Jiang
 */
public enum NodeShapeEnum implements NodeShape {

  NOTE("note", new NotePropCalc()),

  PLAIN("plain", 0.1, 0.1, new PlainPropCalc()),

  PLAIN_TEXT("plaintext", new EllipsePropCalc()),

  UNDERLINE("underline", new RectanglePropCalc()),

  ELLIPSE("ellipse", new EllipsePropCalc()),

  CIRCLE("circle", 0.75, 0.75, new CirclePropCalc()),

  RECT("rect", new RectanglePropCalc()),

  POINT("point", 0.1, 0.1, new PointPropCalc()),

  TRIANGLE("triangle", new TrianglePropCalc(true)),

  INVTRIANGLE("invtriangle", new TrianglePropCalc(false)),

  DIAMOND("diamond", new DiamondPropCalc()),

  TRAPEZIUM("trapezium", new TrapeziumPropCalc(true)),

  INVTRAPEZIUM("invtrapezium", new TrapeziumPropCalc(false)),

  PARALLELOGRAM("parallelogram", new ParallelogramPropCalc()),

  STAR("star", 0.75, 0.75, new StarPropCalc()),

  CYLINDER("cylinder", new CylinderPropCalc()),

  PENTAGON("pentagon", 0.75, 0.75, new RegularPolylinePropCalc(), new RegularPolyShapePost(5)),

  HEXAGON("hexagon", 0.75, 0.75, new RegularPolylinePropCalc(), new RegularPolyShapePost(6)),

  SEPTAGON("septagon", 0.75, 0.75, new RegularPolylinePropCalc(), new RegularPolyShapePost(7)),

  OCTAGON("octagon", 0.75, 0.75, new RegularPolylinePropCalc(), new RegularPolyShapePost(8)),

  /**
   * Rules and more deformations, like: square, regular pentagon, regular hexagon. Please check
   * {@link NodeBuilder#sides(int)}.
   */
  REGULAR_POLYLINE("regular_polyline", 0.75, 0.75,
                   new RegularPolylinePropCalc(), new RegularPolyShapePost()),

  /**
   * When the node label is an <strong>Cell Expression</strong>, the node will be displayed as a
   * structure with multiple horizontal or vertical levels. Please refer to
   * {@link NodeBuilder#label(String)} for details.
   */
  RECORD("record", 0.75, 1, new RecordPropCalc(false)),

  /**
   * The Mrecord shape is identical to a {@link #RECORD}, except that the outermost box has rounded
   * corners.
   */
  M_RECORD("Mrecord", 0.75, 1, new RecordPropCalc(true));

  private final String name;

  private double defaultHeight = 0.5;

  private double defaultWidth = 0.75;

  private final ShapeCenterCalc shapeCenterCalc;

  private final ShapePropCalc shapePropCalc;

  private final NodeShapePost nodeShapePost;

  NodeShapeEnum(String name, ShapePropCalc shapePropCalc) {
    this.name = name;
    this.shapeCenterCalc = SymmetryShapeCenterCalc.SSPC;
    this.shapePropCalc = shapePropCalc;
    this.nodeShapePost = null;
  }

  NodeShapeEnum(String name, double defaultHeight, double defaultWidth,
                ShapePropCalc shapePropCalc) {
    this(name, defaultHeight, defaultWidth, SymmetryShapeCenterCalc.SSPC, shapePropCalc, null);
  }

  NodeShapeEnum(String name, double defaultHeight, double defaultWidth,
                ShapePropCalc shapePropCalc, NodeShapePost nodeShapePost) {
    this(name, defaultHeight, defaultWidth, SymmetryShapeCenterCalc.SSPC,
         shapePropCalc, nodeShapePost);
  }

  NodeShapeEnum(String name, double defaultHeight, double defaultWidth,
                ShapeCenterCalc shapeCenterCalc, ShapePropCalc shapePropCalc,
                NodeShapePost nodeShapePost) {
    this.name = name;
    this.defaultHeight = defaultHeight;
    this.defaultWidth = defaultWidth;
    this.shapeCenterCalc = shapeCenterCalc;
    this.shapePropCalc = shapePropCalc;
    this.nodeShapePost = nodeShapePost;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public double leftWidth(Double width) {
    return shapeCenterCalc.leftWidth(width);
  }

  @Override
  public double rightWidth(Double width) {
    return shapeCenterCalc.rightWidth(width);
  }

  @Override
  public double topHeight(Double height) {
    return shapeCenterCalc.topHeight(height);
  }

  @Override
  public double bottomHeight(Double height) {
    return shapeCenterCalc.bottomHeight(height);
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    if (innerHeight == 0 || innerWidth == 0) {
      return new FlatPoint(0, 0);
    }
    return shapePropCalc.minContainerSize(innerHeight, innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    Asserts.nullArgument(box, "shapePosition");
    Asserts.nullArgument(point, "point");
    return shapePropCalc.in(box, point);
  }

  @Override
  public void ratio(FlatPoint boxSize) {
    Asserts.nullArgument(boxSize, "boxSize");
    shapePropCalc.ratio(boxSize);
  }

  @Override
  public FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    Asserts.nullArgument(box, "box");
    Asserts.nullArgument(labelSize, "labelSize");
    return shapePropCalc.labelCenter(labelSize, box);
  }

  @Override
  public boolean needMargin() {
    return shapePropCalc.needMargin();
  }

  @Override
  public boolean ignoreLabel() {
    return shapePropCalc.ignoreLabel();
  }

  @Override
  public Color defaultFillColor() {
    return shapePropCalc.defaultFillColor();
  }

  @Override
  public NodeShape post(NodeAttrs nodeAttrs) {
    return nodeShapePost != null ? nodeShapePost.post(nodeAttrs) : this;
  }

  @Override
  public ShapePropCalc getShapePropCalc() {
    return shapePropCalc;
  }

  public double getDefaultHeight() {
    return defaultHeight * Graphviz.PIXEL;
  }

  public double getDefaultWidth() {
    return defaultWidth * Graphviz.PIXEL;
  }
}
