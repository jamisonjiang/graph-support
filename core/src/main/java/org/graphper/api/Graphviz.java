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

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.Table;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.InitPos;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint.UnmodifyFlatPoint;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.common.CommonRenderEngine;
import org.graphper.util.Asserts;
import org.graphper.util.FontUtils;
import org.graphper.util.GraphvizUtils;

/**
 * {@code Graphviz} is the largest {@link GraphContainer} and the unit of graph rendering.
 *
 * @author Jamison Jiang
 */
public class Graphviz extends GraphContainer implements Serializable {

  private static final long serialVersionUID = 7386714074818676956L;

  /**
   * Measurement unit.
   */
  public static final int PIXEL = 72;

  /**
   * The maximum depth that graphs can be nested.
   */
  public static final int MAX_DEPTH = 1000;

  // Graph attribute
  private GraphAttrs graphAttrs;

  // Record the parent of all graph container
  private Map<GraphContainer, GraphContainer> fatherRecord;

  // Whether is directed graph
  private final boolean isDirected;

  private volatile GraphResource svg;

  private Graphviz(boolean isDirected, GraphAttrs graphAttrs) {
    this.graphAttrs = graphAttrs;
    this.isDirected = isDirected;
  }

  /**
   * Returns graph attribute.
   *
   * @return graph attribute
   */
  public GraphAttrs graphAttrs() {
    return graphAttrs;
  }

  /**
   * Returns the graph whether is directed.
   *
   * @return <tt>true</tt> if graph is directed
   */
  public boolean isDirected() {
    return isDirected;
  }

  /**
   * Returns the parent container of the specified graph container in the current {@code Graphviz}.
   *
   * @param container parent container
   * @return closest parent container
   */
  public GraphContainer father(GraphContainer container) {
    if (fatherRecord == null) {
      return null;
    }
    return fatherRecord.get(container);
  }

  /**
   * Returns the closest effective parent container of the specified graph container in the current
   * {@code Graphviz}. Only {@link Subgraph} is an invalid parent container. This is because in the
   * calculation of some nodes, subgraph is not a real graph container with a visual container.
   *
   * @param container effective parent container
   * @return closest effective parent container
   */
  public GraphContainer effectiveFather(GraphContainer container) {
    if (fatherRecord == null) {
      return null;
    }

    GraphContainer p = fatherRecord.get(container);
    while (p != null && p.isSubgraph()) {
      p = fatherRecord.get(p);
    }

    return p;
  }

  /**
   * Output svg resource content.
   *
   * @return svg graph resource
   * @throws ExecuteException conversion execution error
   */
  public GraphResource toSvg() throws ExecuteException {
    if (svg != null) {
      return svg;
    }
    synchronized (this) {
      if (svg == null) {
        svg = toFile(null);
      }
    }
    return svg;
  }

  /**
   * Returns the rendered svg string.
   *
   * @return graph svg string
   * @throws ExecuteException conversion execution error
   */
  public String toSvgStr() throws ExecuteException {
    try (GraphResource resource = toSvg()) {
      synchronized (this) {
        this.svg = null;
      }
      return new String(resource.bytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ExecuteException(e);
    }
  }

  /**
   * Returns the rendered graph file resource, return svg if {@code fileType} is null.
   *
   * @param fileType image type
   * @return graph image
   * @throws ExecuteException conversion execution error
   */
  public GraphResource toFile(FileType fileType) throws ExecuteException {
    return CommonRenderEngine.getInstance().render(this, fileType);
  }

  // ------------------------------------------ static ---------------------------------------

  /**
   * Returns a {@link GraphvizBuilder} that marks the current graph as undirected.
   *
   * @return GraphvizBuilder
   */
  public static GraphvizBuilder graph() {
    return new GraphvizBuilder(false);
  }

  /**
   * Returns a {@link GraphvizBuilder} that marks the current graph as directed.
   *
   * @return GraphvizBuilder
   */
  public static GraphvizBuilder digraph() {
    return new GraphvizBuilder(true);
  }

  // ------------------------------------------ Graphviz Builder ---------------------------------------

  /**
   * {@link Graphviz} builder, used to build a {@link Graphviz}.
   */
  public static class GraphvizBuilder extends
      GraphContainerBuilder<Graphviz, GraphvizBuilder> implements Cloneable {

    private GraphAttrs graphAttrs;

    private final boolean isDirected;

    private GraphvizBuilder(boolean isDirected) {
      graphAttrs = new GraphAttrs();
      this.isDirected = isDirected;
    }

    private Graphviz newGraphviz() {
      return new Graphviz(isDirected, graphAttrs);
    }

    @Override
    protected GraphvizBuilder self() {
      return this;
    }

    @Override
    protected Graphviz newContainer() {
      Graphviz graphviz = newGraphviz();
      graphviz.graphAttrs = this.graphAttrs;
      return graphviz;
    }

    @Override
    protected Graphviz copy() {
      Graphviz graphviz = newGraphviz();
      graphviz.graphAttrs = graphAttrs.clone();
      return graphviz;
    }

    /**
     * Set the label of the graphviz.
     *
     * @param label label to be added to this graphviz
     * @return graphviz builder
     */
    public GraphvizBuilder label(String label) {
      graphAttrs.label = label;
      graphAttrs.fontName = FontUtils.selectFont(graphAttrs.label, graphAttrs.fontName);
      return self();
    }

    /**
     * Assigns an HTML-like {@link LabelTag} to the entire graph, enabling rich text formatting
     * (such as bold, italic, or multi-line text) in the graph's label.
     *
     * <p>If a plain text label was previously set via {@code label(String)}, calling this method
     * will override that label with the provided {@link LabelTag}.</p>
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * LabelTag tag = bold("Graph Analysis")
     *     .br()
     *     .italic("Understanding the flow of a directed graph")
     *     .br()
     *     .underline(font("Highlighted Concept", fontAttrs().color(Color.BLUE).pointSize(18)))
     *     .br()
     *     .top(bold(italic("Key Takeaways:")))
     *     .br()
     *     .left(font(text("H").subscript("2").text("O").superscript("2"), fontAttrs().color(Color.RED).pointSize(16)))
     *     .br()
     *     .right(strikeThrough("Outdated Formula"))
     *     .br()
     *     .bottom(italic("Bottom Text Example"))
     *     .font("Bigger", fontAttrs().pointSize(50))
     *     .verticalCenter(italic(bold("Centered Text Example")));

     * Graphviz graphviz = Graphviz.digraph()
     *     .labelTag(tag)
     *     // ...additional builder configuration...
     *     .build();
     * }</pre>
     *
     * @param labelTag the {@link LabelTag} containing advanced formatting for the graph label
     * @return this {@code GraphvizBuilder} for method chaining
     */
    public GraphvizBuilder labelTag(LabelTag labelTag) {
      graphAttrs.labelTag = labelTag;
      return self();
    }

    /**
     * Set the {@link Labelloc} of the graphviz, used to control the vertical position of the
     * {@link GraphAttrs#getLabel()} of the graphviz.
     *
     * @param labelloc labelloc to be added to this graphviz
     * @return graphviz builder
     * @throws NullPointerException null labelloc
     */
    public GraphvizBuilder labelloc(Labelloc labelloc) {
      Asserts.nullArgument(labelloc, "labelloc");
      graphAttrs.labelloc = labelloc;
      return self();
    }

    /**
     * Set the {@link Labeljust} of the graphviz, used to control the horizontal position of the
     * {@link GraphAttrs#getLabel()} of the graphviz.
     *
     * @param labeljust labeljust to be added to this graphviz
     * @return graphviz builder
     * @throws NullPointerException null labbeljust
     */
    public GraphvizBuilder labeljust(Labeljust labeljust) {
      Asserts.nullArgument(labeljust, "labeljust");
      graphAttrs.labeljust = labeljust;
      return self();
    }

    /**
     * Set the font style of graphviz.
     *
     * @param fontName font style name
     * @return graphviz builder
     * @throws NullPointerException null fontName
     */
    public GraphvizBuilder fontName(String fontName) {
      Asserts.nullArgument(fontName, "fontName");
      graphAttrs.fontName = fontName;
      graphAttrs.fontName = FontUtils.selectFont(graphAttrs.label, graphAttrs.fontName);
      return self();
    }

    /**
     * Set the line drawing mode of graphviz, please check {@link Splines} for details.
     *
     * @param splines line draw mode
     * @return graphviz builder
     * @throws NullPointerException null splines
     */
    public GraphvizBuilder splines(Splines splines) {
      Asserts.nullArgument(splines, "splines");
      graphAttrs.splines = splines;
      return self();
    }

    /**
     * Set the direction of the rank distribution, please check {@link Rankdir} for details.
     *
     * @param rankdir the direction of the rank distribution
     * @return graphviz builder
     * @throws NullPointerException null rankdir
     */
    public GraphvizBuilder rankdir(Rankdir rankdir) {
      Asserts.nullArgument(rankdir, "rankdir");
      graphAttrs.rankdir = rankdir;
      return self();
    }

    /**
     * Set the background color of the graphviz.
     *
     * @param bgColor background color
     * @return graphviz builder
     */
    public GraphvizBuilder bgColor(Color bgColor) {
      graphAttrs.bgColor = bgColor;
      return self();
    }

    /**
     * Set the font color of graphviz.
     *
     * @param fontColor font color
     * @return graphviz
     * @throws NullPointerException null fontColor
     */
    public GraphvizBuilder fontColor(Color fontColor) {
      Asserts.nullArgument(fontColor, "fontColor");
      graphAttrs.fontColor = fontColor;
      return self();
    }

    /**
     * Set the layout mode of graphviz, please check {@link Layout} for details.
     *
     * @param layout layout mode
     * @return graphviz builder
     * @throws NullPointerException null layout
     */
    public GraphvizBuilder layout(Layout layout) {
      Asserts.nullArgument(layout, "layout");
      graphAttrs.layout = layout;
      return self();
    }

    /**
     * Set the distance between nodes. In {@link Layout#DOT} layout, it means the distance between
     * two nodes of the same rank.
     *
     * @param nodeSep the distance between two node
     * @return graphviz builder
     * @throws IllegalArgumentException nodesep less than 0
     */
    public GraphvizBuilder nodeSep(double nodeSep) {
      Asserts.illegalArgument(nodeSep < 0, "nodeSep (" + nodeSep + ") can not less than 0");
      graphAttrs.nodeSep = nodeSep * Graphviz.PIXEL;
      return self();
    }

    /**
     * In the {@link Layout#DOT}, use the network simplex to calculate the x-coordinate of the node,
     * so nslimit represents the maximum number of iterations of the network simplex method, the
     * default maximum number of iterations is 100000.
     *
     * @param nslimit iterations times of network simplex of x-coordinate
     * @return graphviz builder
     * @throws IllegalArgumentException nslimit less than 0
     */
    public GraphvizBuilder nslimit(int nslimit) {
      Asserts.illegalArgument(nslimit < 0, "nslimit (" + nslimit + ") can not less than 0");
      graphAttrs.nslimit = nslimit;
      return self();
    }

    /**
     * In the {@link Layout#DOT}, calculate the hierarchical distribution of nodes using the network
     * simplex, so nslimit1 represents the maximum number of iterations of the network simplex, the
     * default maximum number of iterations is {@link Integer#MAX_VALUE}.
     *
     * @param nslimit1 iterations times of network simplex of x-coordinate
     * @return graphviz builder
     * @throws IllegalArgumentException nslimit1 less than 0
     */
    public GraphvizBuilder nslimit1(int nslimit1) {
      Asserts.illegalArgument(nslimit1 < 0, "nslimit (" + nslimit1 + ") can not less than 0");
      graphAttrs.nslimit1 = nslimit1;
      return self();
    }

    /**
     * Set the interval distance of the rank, only valid for {@link Layout#DOT}.
     *
     * @param rankSep interval distance of the rank
     * @return graphviz builder
     * @throws IllegalArgumentException rankSep less than 0.1
     */
    public GraphvizBuilder rankSep(double rankSep) {
      Asserts.illegalArgument(rankSep < 0.1, "ranksep (" + rankSep + ") can not less than 0.1");
      graphAttrs.rankSep = rankSep * Graphviz.PIXEL;
      return self();
    }

    /**
     * Set the scaling value of the final rendered result, the width and height will be stretched by
     * this value.
     *
     * @param scale scaling value
     * @return graphviz builder
     * @throws IllegalArgumentException scale less than 0.1
     */
    public GraphvizBuilder scale(double scale) {
      return scale(scale, scale);
    }

    /**
     * Set the width and height scaling value of the final rendered result.
     *
     * @param horScale scaling value of horizontal
     * @param verScale scaling value of vertical
     * @return graphviz builder
     * @throws IllegalArgumentException horizontal or vertical scale less than 0.1
     */
    public GraphvizBuilder scale(double horScale, double verScale) {
      Asserts.illegalArgument(horScale < 0.1,
                              "Horizontal scale (" + horScale + ") can not less than 0.1");
      Asserts.illegalArgument(verScale < 0.1,
                              "Vertical scale (" + verScale + ") can not less than 0.1");
      graphAttrs.scale = new UnmodifyFlatPoint(horScale, verScale);
      return self();
    }

    /**
     * Set the margin of the graphviz, use the same margin horizontally and vertically.
     *
     * @param margin horizontal and vertical margins
     * @return graphviz builder
     * @throws IllegalArgumentException margin less than 0
     */
    public GraphvizBuilder margin(double margin) {
      return margin(margin, margin);
    }

    /**
     * Set the horizontal and vertical margin of the graphviz.
     *
     * @param horMargin horizontal margin
     * @param verMargin vertical margin
     * @return graphviz builder
     * @throws IllegalArgumentException margin less than 0
     */
    public GraphvizBuilder margin(double horMargin, double verMargin) {
      Asserts.illegalArgument(horMargin < 0, "Horizontal margin (" + horMargin + ") must be > 0");
      Asserts.illegalArgument(verMargin < 0, "Vertical margin (" + verMargin + ") must be > 0");
      graphAttrs.margin = new UnmodifyFlatPoint(verMargin * PIXEL, horMargin * PIXEL);
      return self();
    }

    /**
     * In the {@link Layout#DOT}, multiple iterations will be used to minimize crossings between
     * lines, use this value to control the number of iterations.
     *
     * @param mclimit crossover lines reduces the number of iterations
     * @return graphviz builder
     * @throws IllegalArgumentException mclimit less than 0
     */
    public GraphvizBuilder mclimit(int mclimit) {
      Asserts.illegalArgument(mclimit < 0, "mclimit (" + mclimit + ") can not less than 0");
      graphAttrs.mclimit = mclimit;
      return self();
    }

    /**
     * Set the font size of graphviz.
     *
     * @param fontSize font size
     * @return graphviz builder
     * @throws IllegalArgumentException font size less than 0
     */
    public GraphvizBuilder fontSize(double fontSize) {
      Asserts.illegalArgument(fontSize < 0, "fontSize (" + fontSize + ") can not less than 0");
      graphAttrs.fontSize = fontSize;
      return self();
    }

    /**
     * In the {@link Splines#ORTHO} route, an OVG (Orthogonal Visibility Graph) is used to perform
     * the A-start algorithm to find the route, and the OVG visualization is similar to a grid. Set
     * this value to true to print the OVG of the route. So it only takes effect under
     * {@link Splines#ORTHO}.
     *
     * @param showGrid whether to display the OVG
     * @return graphviz builder
     */
    public GraphvizBuilder showGrid(boolean showGrid) {
      graphAttrs.showGrid = showGrid;
      return self();
    }

    /**
     * Set a graph link, only valid when the output is <tt>svg</tt>.
     *
     * @param href graph href
     * @return graphviz builder
     */
    public GraphvizBuilder href(String href) {
      graphAttrs.href = href;
      return self();
    }

    /**
     * Sets the tooltip text for the graph.
     *
     * <p><strong>Note:</strong> The tooltip only takes effect if the node has an {@code href} set
     * and is rendered in SVG format. It does not work in PNG, JPG, or other raster formats.</p>
     *
     * @param tooltip the text to be displayed as the tooltip
     * @return graphviz builder
     */
    public GraphvizBuilder tooltip(String tooltip) {
      graphAttrs.tooltip = tooltip;
      return self();
    }

    /**
     * Set a Table similar to the HTML structure to replace the {@link #label(String)}, and the
     * generated {@link Table} will be in the position of the label.
     *
     * @param table table
     * @return graphviz builder
     */
    public GraphvizBuilder table(Table table) {
      graphAttrs.table = table;
      return self();
    }

    /**
     * Sets the maximum number of iterations for the fdo layout algorithms.
     *
     * @param maxiter the maximum number of iterations, must be non-negative
     * @return graphviz builder
     * @throws IllegalArgumentException if {@code maxiter} is less than 0
     */
    public GraphvizBuilder maxiter(int maxiter) {
      Asserts.illegalArgument(maxiter < 0, "maxiter " + maxiter + " can not be less than 0");
      graphAttrs.maxiter = maxiter;
      return self();
    }

    /**
     * Sets the spring constant for the
     * fdp({@link Layout#FDP}|{@link Layout#JFDP}|{@link Layout#GFDP}) series layout.
     *
     * <p>The spring constant determines the "stiffness" of the virtual springs connecting the
     * nodes. Higher values can result in tighter node placement, while lower values allow for more
     * spacing.
     *
     * @param k the spring constant, must be non-negative
     * @return graphviz builder
     * @throws IllegalArgumentException if {@code k} is less than 0
     */
    public GraphvizBuilder k(double k) {
      Asserts.illegalArgument(k < 0, "spring constant can not be less than 0");
      graphAttrs.k = k;
      return self();
    }

    /**
     * Configures whether node overlaps are allowed in the fdp
     * ({@link Layout#FDP}|{@link Layout#JFDP}|{@link Layout#GFDP}) series layout.
     *
     * <p>If set to {@code true}, nodes may overlap in the final layout. If set to {@code false},
     * the algorithm will attempt to adjust the layout to prevent node overlaps.
     *
     * @param overlap {@code true} to allow overlaps, {@code false} to prevent overlaps
     * @return graphviz builder
     */
    public GraphvizBuilder overlap(boolean overlap) {
      graphAttrs.overlap = overlap;
      return self();
    }

    /**
     * Sets the initial positioning strategy for the fdp
     * ({@link Layout#FDP}|{@link Layout#JFDP}|{@link Layout#GFDP}) series layout.
     *
     * <p>The initial position can influence the final layout, particularly in iterative layout
     * algorithms. Use this to specify how the layout should initialize node positions.
     *
     * @param pos the initial position strategy, must not be {@code null}
     * @return graphviz builder
     * @throws IllegalArgumentException if {@code pos} is {@code null}
     */
    public GraphvizBuilder initPos(InitPos pos) {
      Asserts.nullArgument(pos, "null pos");
      graphAttrs.initPos = pos;
      return self();
    }

    /**
     * Set an {@link Assemble} to replace the {@link #label(String)}. When setting a label for a
     * graph, the program will calculate the size of the label, and then automatically put the label
     * in the appropriate position of the graph. If {@link Assemble} is set, assemble will be placed
     * where the label was originally placed.
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
     *         .addNode(Node.builder().label("Node").build())
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
     *         .build()
     * }
     * </pre>
     *
     * @param assemble assemble
     * @return graphviz builder
     */
    public GraphvizBuilder assemble(Assemble assemble) {
      graphAttrs.assemble = assemble;
      return self();
    }

    /**
     * Returns a graphviz.
     *
     * @return {@code Graphviz}
     * @throws IllegalArgumentException This type of error will be reported in the following
     *                                  situations:
     *                                  <ul>
     *                                    <li>The depth of the container exceeds {@link Graphviz#MAX_DEPTH};
     *                                    <li>There is an empty sub-graph ({@link Subgraph}|{@link Cluster});
     *                                    <li>A sub-graph ({@link Subgraph}|{@link Cluster}) is repeatedly added into {@code Graphviz}.
     *                                  </ul>
     */
    @Override
    public synchronized Graphviz build() {
      Graphviz graphviz = super.build();

      // Make sure acyclic,limit container depency depth
      Set<GraphContainer> path = new HashSet<>();
      Set<GraphContainer> accessStack = new HashSet<>();

      GraphvizUtils.dfs(MAX_DEPTH, Boolean.FALSE, path, accessStack, graphviz,
                        (s, f) -> setFather(graphviz, f, s), (c, f) -> setFather(graphviz, f, c),
                        null);

      return graphviz;
    }

    @Override
    public GraphvizBuilder clone() throws CloneNotSupportedException {
      GraphvizBuilder repl = (GraphvizBuilder) super.clone();
      repl.container = copy();
      repl.graphAttrs = repl.container.graphAttrs;
      supplyFields(repl.container);
      return repl;
    }

    private void setFather(Graphviz graphviz, GraphContainer father, GraphContainer container) {
      if (graphviz.fatherRecord == null) {
        graphviz.fatherRecord = new HashMap<>();
      }
      Asserts.illegalArgument(container.absoluteEmpty(), "Graphviz have empty sub graph!");
      Asserts.illegalArgument(graphviz.fatherRecord.get(container) != null,
                              "Graph Container is repeatedly set in Graphviz!");
      graphviz.fatherRecord.put(container, father);
    }
  }
}
