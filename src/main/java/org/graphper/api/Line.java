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
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.LineStyle;
import org.graphper.api.attributes.Splines;
import org.graphper.util.Asserts;
import org.graphper.api.Cluster.ClusterBuilder;
import org.graphper.api.Node.NodeBuilder;
import org.graphper.api.attributes.Port;

/**
 * Graphviz lines.
 *
 * @author Jamison Jiang
 */
public class Line implements Comparable<Line>, Serializable {

  private static final long serialVersionUID = 7867944912063456255L;

  private static final Node EMPTY = Node.builder().build();

  /**
   * Line head
   */
  private final Node head;

  /**
   * Line tail
   */
  private final Node tail;

  // Line attribute
  private final LineAttrs lineAttrs;

  private Line(Node head, Node tail, LineAttrs lineAttrs) {
    Asserts.nullArgument(head, "head");
    Asserts.nullArgument(tail, "tail");
    Asserts.nullArgument(lineAttrs, "lineAttrs");

    this.head = head;
    this.tail = tail;
    this.lineAttrs = lineAttrs;
  }

  /**
   * Returns the head of line.
   *
   * @return head of line
   */
  public Node head() {
    return head;
  }

  /**
   * Returns the tail of line.
   *
   * @return tail of line
   */
  public Node tail() {
    return tail;
  }

  /**
   * Returns another node of the line based on the node of the line, or null if the node is not part
   * of the line.
   *
   * @param node endpoint of line
   * @return line another node
   */
  public Node other(Node node) {
    if (node == head) {
      return tail;
    }
    if (node == tail) {
      return head;
    }
    return null;
  }

  /**
   * Returns the weight of line.
   *
   * @return weight of line
   */
  public double weight() {
    return lineAttrs.weight != null ? lineAttrs.weight : 1D;
  }

  /**
   * Returns the line attribute.
   *
   * @return line attribute
   */
  public LineAttrs lineAttrs() {
    return lineAttrs;
  }

  @Override
  public int compareTo(Line o) {
    if (o == this) {
      return 0;
    }
    if (o == null) {
      return 1;
    }

    int c = tail.compareTo(o.tail);

    if (c != 0) {
      return c;
    }

    c = head.compareTo(o.head);

    if (c != 0) {
      return c;
    }

    c = Integer.compare(lineAttrs.hashCode(), o.lineAttrs.hashCode());

    if (c != 0) {
      return c;
    }

    return this.hashCode() - o.hashCode();
  }

  /**
   * Returns a line builder consisting of the specified two nodes.
   *
   * @param tail the tail node of line
   * @param head the head node of line
   * @return {@code LineBuilder}
   * @throws NullPointerException left or right node is null
   */
  public static LineBuilder builder(Node tail, Node head) {
    return new LineBuilder(tail, head);
  }

  /**
   * Returns a line builder for building template lines.
   *
   * @return template line builder
   */
  public static LineBuilder tempLine() {
    return new LineBuilder(EMPTY, EMPTY);
  }

  // ------------------------------------------ Line Builder ---------------------------------------

  /**
   * {@link Line} builder, used to build a {@link Line}.
   */
  public static class LineBuilder {

    protected Node from;

    protected Node to;

    protected LineAttrs lineAttrs;

    private LineBuilder(Node from, Node to) {
      if (from == null || to == null) {
        throw new NullPointerException("node can not be null");
      }

      this.from = from;
      this.to = to;
      this.lineAttrs = new LineAttrs();
    }

    /**
     * Set line id, if you don't set it manually, it will be assigned automatically when rendering,
     * once multiple lines are manually set to be the same, only one will be rendered (But the line
     * will still take up space).
     *
     * @param id line id
     * @return line builder
     */
    public LineBuilder id(String id) {
      Asserts.nullArgument(id, "id");
      lineAttrs.id = id;
      return this;
    }

    /**
     * Set the label of the line. At present, the label of Line will occupy a certain space, and the
     * label will try to avoid overlapping with other elements in the figure (nodes, other lines,
     * labels of other lines). If you want to set a label that does not take up space, please check
     * {@link #floatLabels(FloatLabel...)}.
     *
     * @param label label to be added to this line
     * @return line builder
     */
    public LineBuilder label(String label) {
      lineAttrs.label = label;
      return this;
    }

    /**
     * Sets whether to show a series of points for the line's path control.
     *
     * @param controlPoints show the path control of the line
     * @return line builder
     */
    public LineBuilder controlPoints(boolean controlPoints) {
      lineAttrs.controlPoints = controlPoints;
      return this;
    }

    /**
     * Set whether to display the position of the box of the routing line. Set whether to display
     * the position of the box of the routing line. Only valid for routing algorithms such as:
     * <ul>
     *   <li> {@link Splines#SPLINE}
     *   <li> {@link Splines#ROUNDED}
     *   <li> {@link Splines#POLYLINE}
     * </ul>
     *
     * @param showboxes show the router box of the line
     * @return line builder
     */
    public LineBuilder showboxes(boolean showboxes) {
      lineAttrs.showboxes = showboxes;
      return this;
    }

    /**
     * Sets the arc at the line corners, Only valid for {@link Splines#ROUNDED}, The smaller the
     * arc, the straighter the line visually.
     *
     * @param radian the conner arc
     * @return line builder
     * @throws IllegalArgumentException radian less than 0
     */
    public LineBuilder radian(double radian) {
      Asserts.illegalArgument(radian < 0, "radian (" + radian + ") can not less than 0");
      lineAttrs.radian = radian;
      return this;
    }

    /**
     * Set the color of the line.
     *
     * @param color line color
     * @return line builder
     */
    public LineBuilder color(Color color) {
      lineAttrs.color = color;
      return this;
    }

    /**
     * Set the weight of line. In the {@link Layout#DOT} engine, the larger the value of weight, the
     * more likely the vertical of the line will become larger (this is not certain, this is a
     * priority weight value).
     *
     * @param weight line weight
     * @return line builder
     */
    public LineBuilder weight(double weight) {
      lineAttrs.weight = weight;
      return this;
    }

    /**
     * Set the font color of line, including {@link #label(String)} and
     * {@link #floatLabels(FloatLabel...)}.
     *
     * @param fontColor font color
     * @return line builder
     */
    public LineBuilder fontColor(Color fontColor) {
      lineAttrs.fontColor = fontColor;
      return this;
    }

    /**
     * Set the font size of line.
     *
     * @param fontSize font size
     * @return line builder
     * @throws IllegalArgumentException font size less than 0
     */
    public LineBuilder fontSize(double fontSize) {
      Asserts.illegalArgument(fontSize < 0, "fontSize (" + fontSize + ") can not less than 0");
      lineAttrs.fontSize = fontSize;
      return this;
    }

    /**
     * Set the font style of line.
     *
     * @param fontName font style name
     * @return line builder
     */
    public LineBuilder fontName(String fontName) {
      lineAttrs.fontName = fontName;
      return this;
    }

    /**
     * Sets whether lines need to be clipped to fit the shape of the head node. If this value is
     * false, the line will go directly to the center of gravity of the head (unless
     * {@link #headPort(Port)} is set).
     *
     * @param headclip head endpoint need to clip
     * @return line builder
     */
    public LineBuilder headclip(boolean headclip) {
      lineAttrs.headclip = headclip;
      return this;
    }

    /**
     * Sets whether lines need to be clipped to fit the shape of the tail node. If this value is
     * false, the line will go directly to the center of gravity of the tail (unless
     * {@link #tailPort(Port)} is set).
     *
     * @param tailclip tail endpoint need to clip
     * @return line builder
     */
    public LineBuilder tailclip(boolean tailclip) {
      lineAttrs.tailclip = tailclip;
      return this;
    }

    /**
     * Set the least spanning ranks of the tail and head nodes of the current line in
     * {@link Layout#DOT}.
     *
     * @param minlen least spanning ranks
     * @return line builder
     * @throws IllegalArgumentException minlen less than 0
     */
    public LineBuilder minlen(int minlen) {
      Asserts.illegalArgument(minlen < 0, "minlen (" + minlen + ") can not less than 0");
      lineAttrs.minlen = minlen;
      return this;
    }

    /**
     * Set the style of line, Please check the details {@link LineStyle}.
     *
     * @param styles line styles
     * @return line builder
     * @throws IllegalArgumentException empty styles or contains null style
     */
    public LineBuilder style(LineStyle... styles) {
      Asserts.nullOrContainsNull(styles);
      lineAttrs.styles = Arrays.asList(styles);
      return this;
    }

    /**
     * Set the shape of the head arrow, please refer to {@link ArrowShape} for details.
     *
     * @param arrowHead head arrow shape
     * @return line builder
     */
    public LineBuilder arrowHead(ArrowShape arrowHead) {
      lineAttrs.arrowHead = arrowHead;
      return this;
    }

    /**
     * Set the shape of the tail arrow, please refer to {@link ArrowShape} for details.
     *
     * @param arrowTail tail arrow shape
     * @return line builder
     */
    public LineBuilder arrowTail(ArrowShape arrowTail) {
      lineAttrs.arrowTail = arrowTail;
      return this;
    }

    /**
     * Set the size of the arrow.
     *
     * @param arrowSize arrow size
     * @return line builder
     * @throws IllegalArgumentException arrow size must greater than 0
     */
    public LineBuilder arrowSize(double arrowSize) {
      Asserts.illegalArgument(arrowSize < 0, "arrowSize (" + arrowSize + ") can not less than 0");
      lineAttrs.arrowSize = arrowSize * Graphviz.PIXEL / 10;
      return this;
    }

    /**
     * Sets the arrow direction for the line. Under {@link Graphviz#isDirected()}, the default is
     * the direction from tail to head.
     *
     * @param dir line arrow direction
     * @return line builder
     */
    public LineBuilder dir(Dir dir) {
      lineAttrs.dir = dir;
      return this;
    }

    /**
     * Set the head direction of the line according to which {@link Cluster} to clip. If attribute
     * {@link #headclip(boolean)} is set to false, {@code lhead} would never take effect.
     *
     * <p>To make this property take effect, first ensure that
     * {@link Graphviz.GraphvizBuilder#compound(boolean)} is enabled.Then find the specified
     * {@link Cluster} that you want the line segment to clipped, set the corresponding
     * id({@link ClusterBuilder#id(String)}) for the specified {@link Cluster} and then point this
     * attribute to the corresponding id. Here is a simple example:
     * <pre>{@code
     *     Graphviz.digraph()
     *         // Make sure this compound is turned on
     *         .compound(true)
     *         // The line will point directly to the head node, not the cluster
     *         .addLine(a, b)
     *         // Set the head node position of the line to be cut by the cluster with id "cluster_0"
     *         .addLine(Line.builder(a, c).lhead("cluster_0").build())
     *
     *         // The head node "b" and "c" in a same cluster
     *         .cluster(
     *             Cluster.builder()
     *                 // Set a cluster id, provide lhead with a unique cluster ID
     *                 .id("cluster_0")
     *                 .addNode(b, c)
     *                 .build()
     *         )
     *         .build();
     * }</pre>
     *
     * @param lhead the id of the cluster that the head of line needs to clipped
     * @return line builder
     */
    public LineBuilder lhead(String lhead) {
      lineAttrs.lhead = lhead;
      return this;
    }

    /**
     * Set the tail direction of the line according to which {@link Cluster} to clip. If attribute
     * {@link #tailclip(boolean)} is set to false, {@code ltail} would never take effect.
     *
     * <p>To make this property take effect, first ensure that
     * {@link Graphviz.GraphvizBuilder#compound(boolean)} is enabled.Then find the specified
     * {@link Cluster} that you want the line segment to clipped, set the corresponding
     * id({@link ClusterBuilder#id(String)}) for the specified {@link Cluster} and then point this
     * attribute to the corresponding id. Here is a simple example:
     * <pre>{@code
     *     Graphviz.digraph()
     *         // Make sure this compound is turned on
     *         .compound(true)
     *         // The line will point directly to the tail node, not the cluster
     *         .addLine(b, a)
     *         // Set the tail node position of the line to be cut by the cluster with id "cluster_0"
     *         .addLine(Line.builder(c, a).ltail("cluster_0").build())
     *
     *         // The tail node "b" and "c" in a same cluster
     *         .cluster(
     *             Cluster.builder()
     *                 // Set a cluster id, provide ltail with a unique cluster ID
     *                 .id("cluster_0")
     *                 .addNode(b, c)
     *                 .build()
     *         )
     *         .build();
     * }</pre>
     *
     * @param ltail the id of the cluster that the tail of line needs to clipped
     * @return line builder
     */
    public LineBuilder ltail(String ltail) {
      lineAttrs.ltail = ltail;
      return this;
    }

    /**
     * The floating label object of the Line. Uses two parameters to describe the position of the
     * label relative to the line:
     * <ul>
     *   <li>First use the parameter {@link FloatLabel#getLengthRatio()} to locate a position of the current line,
     *   0 indicates the position closest to the tail node, 1 indicates the position closest to the head node;
     *   <li>Secondly, if {@link FloatLabel#getTend()} is set, the set {@link FloatLabel#getLengthRatio()} attribute will be ignored,
     *   and the program will automatically select the appropriate point according to the corresponding value.
     *   <li>Finally, manually adjust an offset according to the obtained node by {@link FloatLabel#getOffset()}.
     *   The horizontal and vertical offset ratios corresponding to this offset are based on the size of the current label size.
     * </ul>
     *
     * @param floatLabels float label array
     * @return line builder
     * @throws IllegalArgumentException float labels is null or empty, or contains null element
     */
    public LineBuilder floatLabels(FloatLabel... floatLabels) {
      Asserts.nullOrContainsNull(floatLabels);
      lineAttrs.floatLabels = floatLabels;
      return this;
    }

    /**
     * Set the position where the line comes out from the tail node. The default is to start from
     * the center of the node. For specific position information, please refer to {@link Port}.
     *
     * <p>This attribute can be used in combination with {@link #tailCell(String)} to locate the
     * port of a cell in the node. For details and examples, please refer to {@link #tailCell}.
     *
     * @param tailPort tail node port
     * @return line builder
     */
    public LineBuilder tailPort(Port tailPort) {
      lineAttrs.tailPort = tailPort;
      return this;
    }

    /**
     * Set the position where the line comes out from the head node. The default is to start from
     * the center of the node. For specific position information, please refer to {@link Port}.
     *
     * <p>This attribute can be used in combination with {@link #headCell(String)} to locate the
     * port of a cell in the node. For details and examples, please refer to {@link #headCell}.
     *
     * @param headPort head node port
     * @return line builder
     */
    public LineBuilder headPort(Port headPort) {
      lineAttrs.headPort = headPort;
      return this;
    }

    /**
     * Set which cell of the tail {@link Node} the current line segment falls into. For the specific
     * content of the {@link Node}'s cell, please refer to {@link NodeBuilder#label(String)}.
     *
     * <p>If the current line had set to {@link #tailPort(Port)}, then the line will first fall to
     * the corresponding cell, and then fall to the corresponding position of the cell according to
     * {@link #tailPort(Port)}. Here is a dot script and code example:
     * <pre>{@code
     * digraph G {
     *     a[shape=record label="1|<P2>2"]
     *     a:P2:w->b
     * }
     * }
     * Transfer to java code:
     * {@code
     *     Node a = Node.builder()
     *         .shape(NodeShapeEnum.RECORD)
     *         .label("1|<P2>2")
     *         .build();
     *     Node b = Node.builder().label("b").build();
     *
     *     Graphviz.digraph()
     *         .addLine(
     *             Line.builder(a, b)
     *                 // Refer to the <P2> cell in node a
     *                 .tailCell("P2")
     *                 // Refer to the WEST Port of <P2> cell
     *                 .tailPort(Port.WEST)
     *                 .build()
     *         )
     *         .build();
     * }
     * </pre>
     *
     * @param tailCell tail cell id
     * @return line builder
     */
    public LineBuilder tailCell(String tailCell) {
      lineAttrs.tailCell = tailCell;
      return this;
    }

    /**
     * Set which cell of the head {@link Node} the current line segment falls into. For the specific
     * content of the {@link Node}'s cell, please refer to {@link NodeBuilder#label(String)}.
     *
     * <p>If the current line had set to {@link #headPort(Port)}, then the line will first fall to
     * the corresponding cell, and then fall to the corresponding position of the cell according to
     * {@link #headPort(Port)}. Here is a dot script and code example:
     * <pre>{@code
     * digraph G {
     *     b[shape=record label="<P1>1|2"]
     *     a->b:p1:s
     * }
     * }
     * Transfer to java code:
     * {@code
     *     Node a = Node.builder().build();
     *     Node b = Node.builder()
     *         .shape(NodeShapeEnum.RECORD)
     *         .label("<P1>1|2")
     *         .build();
     *
     *     Graphviz.digraph()
     *         .addLine(
     *             Line.builder(a, b)
     *                 // Refer to the <P1> cell in node b
     *                 .headCell("P1")
     *                 // Refer to the SOUTH Porlt of <P1> cell
     *                 .headPort(Port.SOUTH)
     *                 .build()
     *         )
     *         .build();
     * }
     * </pre>
     *
     * @param headCell head cell id
     * @return line builder
     */
    public LineBuilder headCell(String headCell) {
      lineAttrs.headCell = headCell;
      return this;
    }

    /**
     * Set a line link, only valid when the output is <tt>svg</tt>.
     *
     * @param href line href
     * @return line builder
     */
    public LineBuilder href(String href) {
      lineAttrs.href = href;
      return this;
    }

    /**
     * Set the border width of the line.
     *
     * @param penWidth border width
     * @return line builder
     * @throws IllegalArgumentException pen width less than 0 or equals to 0
     */
    public LineBuilder penWidth(double penWidth) {
      Asserts.illegalArgument(penWidth <= 0, "penWidth must be greater than 0");
      lineAttrs.penWidth = penWidth;
      return this;
    }

    /**
     * Set a Table similar to the HTML structure to replace the {@link #label(String)}, and the
     * generated {@link Table} will be in the position of the label.
     *
     * @param table table
     * @return line builder
     */
    public LineBuilder table(Table table) {
      lineAttrs.table = table;
      return this;
    }

    /**
     * Set an {@link Assemble} to replace the {@link #label(String)}. When setting a label for a
     * line, the program will calculate the size of the label, and then automatically put the label
     * in the appropriate position of the line. If {@link Assemble} is set, assemble will be placed
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
     * <p>This is an example of setting three cells side by assemble.
     * <pre>{@code
     *     Node left = Node.builder().label("left").build();
     *     Node right = Node.builder().label("right").build();
     *     Graphviz.digraph()
     *         .startSub()
     *         .rank(Rank.SAME)
     *         .addLine(
     *             Line.builder(left, right)
     *                 .assemble(
     *                     Assemble.builder()
     *                         .width(1.6)
     *                         .height(0.6)
     *                         .addCell(0.05, 0.05,
     *                                  Node.builder()
     *                                      .width(0.2)
     *                                      .height(0.5)
     *                                      .fillColor(Color.RED)
     *                                      .build())
     *                         .addCell(0.3, 0.05,
     *                                  Node.builder()
     *                                      .width(1)
     *                                      .height(0.5)
     *                                      .fontSize(12)
     *                                      .label("Edge labels\nalso")
     *                                      .build())
     *                         .addCell(1.35, 0.05,
     *                                  Node.builder()
     *                                      .width(0.2)
     *                                      .height(0.5)
     *                                      .fillColor(Color.BLUE)
     *                                      .build())
     *                         .addCell(0, 0,
     *                                  Node.builder()
     *                                      .width(1.6)
     *                                      .height(0.6)
     *                                      .build())
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .endSub()
     *         .build();
     * }
     * </pre>
     *
     * @param assemble assemble
     * @return line builder
     */
    public LineBuilder assemble(Assemble assemble) {
      lineAttrs.assemble = assemble;
      return this;
    }

    /**
     * Returns a {@link Line}.
     *
     * @return {@code Line}
     */
    public Line build() {
      return new Line(to, from, lineAttrs);
    }
  }
}
