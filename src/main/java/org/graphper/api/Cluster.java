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
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.ClusterShape;
import org.graphper.api.attributes.ClusterShapeEnum;
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.Layout;
import org.graphper.def.FlatPoint.UnmodifyFlatPoint;
import org.graphper.util.Asserts;

/**
 * Gather nodes and lines together and use a single border to enclose them as a sub-graph.
 *
 * @author Jamison Jiang
 */
public class Cluster extends GraphContainer implements Serializable {

  private static final long serialVersionUID = 5027532737058187995L;

  private Cluster(ClusterAttrs clusterAttrs) {
    this.clusterAttrs = clusterAttrs;
  }

  // Cluster attribute
  private final ClusterAttrs clusterAttrs;

  /**
   * Returns the attribute object of {@code Cluster}.
   *
   * @return attribute object of {@code Cluster}
   */
  public ClusterAttrs clusterAttrs() {
    return clusterAttrs;
  }

  /**
   * Returns a {@link ClusterBuilder}.
   *
   * @return {@code ClusterBuilder}
   */
  public static ClusterBuilder builder() {
    return new ClusterBuilder();
  }

  // ------------------------------------------ Cluster Builder ---------------------------------------

  /**
   * Basic {@link Cluster} builder.
   *
   * @param <B> the type of cluster builder
   */
  public abstract static class AbstractClusterBuilder<B extends GraphContainerBuilder<Cluster, B>> extends
      GraphContainerBuilder<Cluster, B> implements Cloneable {

    // Attribute of cluster
    protected ClusterAttrs clusterAttrs;

    protected AbstractClusterBuilder() {
      this.clusterAttrs = new ClusterAttrs();
    }

    /**
     * Set the label of the cluster.
     *
     * @param label label to be added to this cluster
     * @return cluster builder
     */
    public B label(String label) {
      clusterAttrs.label = label;
      return self();
    }

    /**
     * Set the {@link Labelloc} of the cluster, used to control the vertical position of the
     * {@link ClusterAttrs#getLabel()} of the cluster.
     *
     * @param labelloc labelloc to be added to this cluster
     * @return cluster builder
     * @throws NullPointerException null labelloc
     */
    public B labelloc(Labelloc labelloc) {
      Asserts.nullArgument(labelloc, "labelloc");
      clusterAttrs.labelloc = labelloc;
      return self();
    }

    /**
     * Set the {@link Labeljust} of the cluster, used to control the horizontal position of the
     * {@link ClusterAttrs#getLabel()} of the cluster.
     *
     * @param labeljust labeljust to be added to this cluster
     * @return cluster builder
     * @throws NullPointerException null labeljust
     */
    public B labeljust(Labeljust labeljust) {
      Asserts.nullArgument(labeljust, "labeljust");
      clusterAttrs.labeljust = labeljust;
      return self();
    }

    /**
     * Set the shape of the cluster, for the shapes supported by default, please check
     * {@link ClusterShapeEnum}.
     *
     * <p>Cluster shapes except {@link ClusterShapeEnum#RECT} no guarantee that cluster container
     * will surround all nodes under {@link Layout#DOT} engine but will try best estimated the
     * container size by {@link ClusterShape#minContainerSize(double, double)} method, but still
     * have the following principles as much as possible surround all nodes:
     * <ul>
     *   <li>The gap between internal box and external box is as small ass possible, it means output
     *   of {@link ClusterShape#minContainerSize(double, double)} of current shape close enough than input.
     *   e.g, {@link ClusterShapeEnum#RECT} no gap between internal and external boxes.
     *   <li>Internal nodes are kept isolated from external nodes of cluster, it means interact edges
     *   from internal nodes to external nodes as little as possible.
     *   <li>Avoid cluster nesting as much as possible if cluster shapes is not {@link ClusterShapeEnum#RECT}
     *   (or the cluster shape no gap between internal and external box like RECT shape), the error in
     *   evaluation will be magnified in this case.
     *   <li>Manual adjust {@link #margin(double)} reserve enough internal space to avoid nodes
     *   overflow cluster container.
     * </ul>
     *
     * @param shape cluster shape
     * @return cluster builder
     * @throws NullPointerException null cluster shape
     */
    public B shape(ClusterShape shape) {
      Asserts.nullArgument(shape, "shape");
      clusterAttrs.shape = shape;
      return self();
    }

    /**
     * Set the style of cluster, Please check the details {@link ClusterStyle}.
     *
     * @param styles cluster styles
     * @return cluster builder
     * @throws IllegalArgumentException empty styles or contains null style
     */
    public B style(ClusterStyle... styles) {
      Asserts.nullOrContainsNull(styles);
      clusterAttrs.styles = Arrays.asList(styles);
      return self();
    }

    /**
     * Set the background color of the cluster.
     *
     * @param bgColor background color
     * @return cluster builder
     */
    public B bgColor(Color bgColor) {
      clusterAttrs.bgColor = bgColor;
      return self();
    }

    /**
     * Set the border color of the cluster
     *
     * @param color border color
     * @return cluster builder
     */
    public B color(Color color) {
      clusterAttrs.color = color;
      return self();
    }

    /**
     * Set the font color of cluster.
     *
     * @param fontColor font color
     * @return cluster builder
     */
    public B fontColor(Color fontColor) {
      clusterAttrs.fontColor = fontColor;
      return self();
    }

    /**
     * Set the font style of cluster.
     *
     * @param fontName font style name
     * @return cluster builder
     * @throws NullPointerException null fontName
     */
    public B fontName(String fontName) {
      Asserts.nullArgument(fontName, "fontName");
      clusterAttrs.fontName = fontName;
      return self();
    }

    /**
     * Set the margin of the cluster, use the same margin horizontally and vertically.
     *
     * @param margin horizontal and vertical margins
     * @return cluster builder
     * @throws IllegalArgumentException margin less than 0
     */
    public B margin(double margin) {
      return margin(margin, margin);
    }

    /**
     * Set the horizontal and vertical margin of the cluster.
     *
     * @param horMargin horizontal margin
     * @param verMargin vertical margin
     * @return cluster builder
     * @throws IllegalArgumentException margin less than 0
     */
    public B margin(double horMargin, double verMargin) {
      Asserts.illegalArgument(horMargin < 0,
                              "Horizontal margin (" + horMargin + ") can not less than 0");
      Asserts.illegalArgument(verMargin < 0,
                              "Vertical margin (" + verMargin + ") can not less than 0");
      clusterAttrs.margin = new UnmodifyFlatPoint(verMargin * Graphviz.PIXEL,
                                                  horMargin * Graphviz.PIXEL);
      return self();
    }

    /**
     * Set the font size of cluster.
     *
     * @param fontSize font size
     * @return cluster builder
     * @throws IllegalArgumentException font size less than 0
     */
    public B fontSize(double fontSize) {
      Asserts.illegalArgument(fontSize < 0, "fontSize (" + fontSize + ") can not less than 0");
      clusterAttrs.fontSize = fontSize;
      return self();
    }

    /**
     * Set a cluster link, only valid when the output is <tt>svg</tt>.
     *
     * @param href cluster href
     * @return cluster builder
     */
    public B href(String href) {
      clusterAttrs.href = href;
      return self();
    }

    /**
     * Set the border width of the cluster.
     *
     * @param penWidth border width
     * @return cluster builder
     * @throws IllegalArgumentException pen width less than 0
     */
    public B penWidth(double penWidth) {
      Asserts.illegalArgument(penWidth < 0, "penWidth can not be less than 0");
      clusterAttrs.penWidth = penWidth;
      return self();
    }

    /**
     * Set a Table similar to the HTML structure to replace the {@link #label(String)}, and the
     * generated {@link Table} will be in the position of the label.
     *
     * @param table table
     * @return cluster builder
     */
    public B table(Table table) {
      clusterAttrs.table = table;
      return self();
    }

    /**
     * Set an {@link Assemble} to replace the {@link #label(String)}. When setting a label for a
     * cluster, the program will calculate the size of the label, and then automatically put the
     * label in the appropriate position of the cluster. If {@link Assemble} is set, assemble will
     * be placed where the label was originally placed.
     *
     * <p>{@link Assemble} will be used as a common parent container, and all other cells set are
     * placed based on {@link Assemble}, so when adding a cell, an offset position based on
     * {@link Assemble} will be set, and the position of {@link Assemble} is where the label should
     * be.Therefore, {@link Assemble} does not provide automatic layout and cell size calculation
     * (by default, it does not automatically calculate the size of the cell according to the label
     * of the cell), which requires the setter to completely accurate calculation of all
     * parameters.
     *
     * <p>This is an example of setting two cells side by assemble.
     * <pre>{@code
     *     Graphviz.digraph()
     *         .startClus()
     *         .addNode(Node.builder().label("Node in cluster").build())
     *         .margin(0.5, 0.5)
     *         .assemble(
     *             Assemble.builder()
     *                 .width(1)
     *                 .height(0.4)
     *                 .addCell(0, 0,
     *                          Node.builder()
     *                              .width(0.5)
     *                              .height(0.4)
     *                              .label("LEFT")
     *                              .build())
     *                 .addCell(0.5, 0,
     *                          Node.builder()
     *                              .width(0.5)
     *                              .height(0.4)
     *                              .label("RIGHT")
     *                              .build())
     *                 .build()
     *         )
     *         .endClus()
     *         .build();
     * }
     * </pre>
     *
     * @param assemble assemble
     * @return cluster builder
     */
    public B assemble(Assemble assemble) {
      clusterAttrs.assemble = assemble;
      return self();
    }

    @Override
    protected Cluster newContainer() {
      return new Cluster(clusterAttrs);
    }

    @Override
    protected Cluster copy() {
      return new Cluster(clusterAttrs.clone());
    }
  }

  /**
   * {@link Cluster} builder.
   */
  public static class ClusterBuilder extends AbstractClusterBuilder<ClusterBuilder> {

    private ClusterBuilder() {
      super();
    }

    @Override
    protected ClusterBuilder self() {
      return this;
    }

    @Override
    public ClusterBuilder clone() throws CloneNotSupportedException {
      ClusterBuilder repl = (ClusterBuilder) super.clone();
      repl.container = copy();
      repl.clusterAttrs = repl.container.clusterAttrs;
      supplyFields(repl.container);
      return repl;
    }
  }

  /**
   * A {@link Cluster} builder to connect directly to the parent container builder.
   *
   * @param <G> the container type of the parent container
   * @param <B> the builder type of the parent container
   */
  public static class IntegrationClusterBuilder<G extends GraphContainer, B extends GraphContainerBuilder<G, B>> extends
      AbstractClusterBuilder<IntegrationClusterBuilder<G, B>> {

    private final B parentBuilder;

    IntegrationClusterBuilder(B parentBuilder) {
      Asserts.nullArgument(parentBuilder, "parentBuilder");
      this.parentBuilder = parentBuilder;
    }

    /**
     * End the operation of the current {@link Cluster} builder, return to the builder of the parent
     * container, and continue the construction process of the parent container.
     *
     * @return parent builder
     */
    public B endClus() {
      Cluster cluster = build();
      parentBuilder.cluster(cluster);
      return parentBuilder;
    }

    @Override
    protected IntegrationClusterBuilder<G, B> self() {
      return this;
    }
  }
}
