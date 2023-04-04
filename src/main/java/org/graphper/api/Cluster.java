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
import org.graphper.api.attributes.ClusterStyle;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.def.FlatPoint;
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
     * @throws NullPointerException null labbeljust
     */
    public B labeljust(Labeljust labeljust) {
      Asserts.nullArgument(labeljust, "labeljust");
      clusterAttrs.labeljust = labeljust;
      return self();
    }

    /**
     * Set the font style of cluster.
     *
     * @param style cluster style
     * @return cluster builder
     */
    public B style(ClusterStyle style) {
      clusterAttrs.style = style;
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
      clusterAttrs.margin = new FlatPoint(verMargin * Graphviz.PIXEL, horMargin * Graphviz.PIXEL);
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
