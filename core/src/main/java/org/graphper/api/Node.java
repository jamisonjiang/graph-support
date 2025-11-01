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
import java.util.Objects;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Line.LineBuilder;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.FontStyle;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.NodeStyle;
import org.graphper.api.attributes.Port;
import org.graphper.def.FlatPoint.UnmodifyFlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.FontUtils;

/**
 * Graphviz nodes.
 *
 * @author Jamison Jiang
 */
public class Node implements Comparable<Node>, Serializable {

  private static final long serialVersionUID = 4616284202432237469L;

  // Node Attribute
  private final NodeAttrs nodeAttrs;

  private Node(NodeAttrs nodeAttrs) {
    Objects.requireNonNull(nodeAttrs);
    this.nodeAttrs = nodeAttrs;
  }

  /**
   * Returns the attribute object of {@code Node}.
   *
   * @return attribute object of {@code Node}
   */
  public NodeAttrs nodeAttrs() {
    return nodeAttrs;
  }

  /**
   * Returns a {@link NodeBuilder}.
   *
   * @return {@code NodeBuilder}
   */
  public static NodeBuilder builder() {
    return new NodeBuilder();
  }

  @Override
  public int compareTo(Node o) {
    if (o == this) {
      return 0;
    }

    if (nodeAttrs.id != null && o.nodeAttrs.id != null) {
      return nodeAttrs.id.compareTo(o.nodeAttrs.id);
    }

    if (o == null || o.nodeAttrs.label == null) {
      return 1;
    }

    if (this.nodeAttrs.label == null) {
      return -1;
    }

    int c = this.nodeAttrs.label.compareTo(o.nodeAttrs.label);

    return c != 0 ? c : this.hashCode() - o.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (nodeAttrs.id != null && Objects.equals(nodeAttrs.id, ((Node) o).nodeAttrs.id)) {
      return true;
    }

    Node node = (Node) o;
    return Objects.equals(nodeAttrs, node.nodeAttrs);
  }

  @Override
  public int hashCode() {
    if (nodeAttrs.id != null) {
      return nodeAttrs.id.hashCode();
    }

    return super.hashCode();
  }

  // ------------------------------------------ Node Builder ---------------------------------------

  /**
   * {@link Node} builder, used to build a {@link Node}.
   */
  public static final class NodeBuilder {

    // Node Attribute
    private final NodeAttrs nodeAttrs;

    private NodeBuilder() {
      nodeAttrs = new NodeAttrs();
    }

    /**
     * Set node id, if you don't set it manually, it will be assigned automatically when rendering,
     * once multiple nodes are manually set to be the same, only one will be rendered (But the node
     * will still take up space).
     *
     * @param id node id
     * @return node builder
     */
    public NodeBuilder id(String id) {
      nodeAttrs.id = id;
      return this;
    }

    /**
     * Set the label of the node, label can be an ordinary string, or a <strong>Cell
     * Expression</strong>.
     *
     * <p><strong>Cell Expression</strong> need specified by shape values of
     * {@link NodeShapeEnum#RECORD} and {@link NodeShapeEnum#M_RECORD}, The structure of a
     * record-based node is determined by its label, which has the following schema:
     * <ul>
     *   <li>rlabel	=	field ( '|' field )*
     *   <li>where field	=	fieldId or '{' rlabel '}
     *   <li>and fieldId	=	[ '&lt;' string '&gt;'] [ string ]
     * </ul>
     *
     * <p>Braces, vertical bars and angle brackets must be escaped with a backslash character if you
     * wish them to appear as a literal character. Spaces are interpreted as separators between tokens,
     * so they must be escaped if you want spaces in the text.
     *
     * <p> Flipping between horizontal and vertical layouts is done by nesting fields in braces
     * "{...}". The top-level orientation in a record is horizontal. Thus, a record with label
     * "A | B | C | D" will have 4 fields oriented left to right, while "{A | B | C | D}" will
     * have them from top to bottom and "A | { B | C } | D" will have "B" over "C", with "A" to the
     * left and "D" to the right of "B" and "C".
     *
     * <p>As an example of Cell Expression:
     * <pre>{@code
     * digraph structs {
     *     node [shape=record];
     *     struct1 [label="<f0> left|<f1> mid&#92; dle|<f2> right"];
     *     struct2 [label="<f0> one|<f1> two"];
     *     struct3 [label="hello&#92;nworld |{ b |{c|<here> d|e}| f}| g | h"];
     *     struct4 [label="{{Name|Sex|Age}|{Michael|Men|15}|{Abigail|Female|18}}"];
     *     struct1:f1 -> struct2:f0;
     *     struct1:f2 -> struct3:here;
     *     struct4
     * }
     * }
     * Transfer to java code:
     * {@code
     *     Node struct1 = Node.builder().label("<f0> left|<f1> mid dle|<f2> right").build();
     *     Node struct2 = Node.builder().label("<f0> one|<f1> two").label("b").build();
     *     Node struct3 = Node.builder().label("hello\nworld |{ b |{c|<here> d|e}| f}| g | h").build();
     *     Node struct4 = Node.builder().label("{{Name|Sex|Age}|{Michael|Men|15}|{Abigail|Female|18}}").build();
     *
     *     Graphviz.digraph()
     *         .tempNode(Node.builder().shape(NodeShapeEnum.RECORD).build())
     *         .addLine(
     *             Line.builder(struct1, struct2)
     *                 .tailCell("f1")
     *                 .headCell("f0")
     *                 .build()
     *         )
     *         .addLine(
     *             Line.builder(struct1, struct3)
     *                 .tailCell("f2")
     *                 .headCell("here")
     *                 .build()
     *         )
     *         .addNode(struct4)
     *         .build();
     * }
     * </pre>
     *
     * @param label label to be added to this node
     * @return node builder
     * @see LineBuilder#tailPort(Port)
     * @see LineBuilder#headPort(Port)
     * @see LineBuilder#tailCell(String)
     * @see LineBuilder#headCell(String)
     */
    public NodeBuilder label(String label) {
      nodeAttrs.label = label;
      nodeAttrs.fontName = FontUtils.selectFont(nodeAttrs.label, nodeAttrs.fontName);
      return this;
    }

    /**
     * Assigns an HTML-like {@link LabelTag} structure as this node's label, allowing for advanced
     * text styling such as bold, italics, and nested formatting.
     * <p>
     * This method overrides any plain text label set via {@link #label(String)}. If you need a
     * richly formatted label (e.g., multi-line text, nested tags), use {@code labelTag} rather than
     * a simple string label.
     * </p>
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
     *
     * Node node = Node.builder().labelTag(tag).build();
     * }</pre>
     *
     * @param labelTag the {@link LabelTag} to use as this node's label
     * @return this {@code NodeBuilder} for method chaining
     * @see #label(String)
     */
    public NodeBuilder labelTag(LabelTag labelTag) {
      nodeAttrs.labelTag = labelTag;
      return this;
    }

    /**
     * Set the height of the node, the height will take effect mainly in the following situations:
     * <ul>
     *   <li> When {@link NodeAttrs#getFixedSize()} is true, the actual height of the node is
     *   exactly equal to the set value;
     *   <li> When {@link NodeAttrs#getFixedSize()} is false, this height is an actual minimum
     *   limit, indicating that the node height should at least not be less than this set value;
     *   <li> When there is no manual setting, the height of the node will be automatically set
     *   according to different {@link NodeShape} when rendering.
     * </ul>
     *
     * @param height node height
     * @return node builder
     * @throws IllegalArgumentException height less than 0
     */
    public NodeBuilder height(double height) {
      Asserts.illegalArgument(height < 0, "Height (" + height + ") can not less than 0");
      nodeAttrs.height = height * Graphviz.PIXEL;
      return this;
    }

    /**
     * Set the width of the node, the width will take effect mainly in the following situations:
     * <ul>
     *   <li> When {@link NodeAttrs#getFixedSize()} is true, the actual width of the node is
     *   exactly equal to the set value;
     *   <li> When {@link NodeAttrs#getFixedSize()} is false, this width is an actual minimum
     *   limit, indicating that the node width should at least not be less than this set value;
     *   <li> When there is no manual setting, the width of the node will be automatically set
     *   according to different {@link NodeShape} when rendering.
     * </ul>
     *
     * @param width node width
     * @return node builder
     * @throws IllegalArgumentException width less than 0
     */
    public NodeBuilder width(double width) {
      Asserts.illegalArgument(width < 0, "width (" + width + ") must be > 0");
      nodeAttrs.width = width * Graphviz.PIXEL;
      return this;
    }

    /**
     * Set the shape of the node, for the shapes supported by default, please check
     * {@link NodeShapeEnum}.
     *
     * @param shape node shape
     * @return node builder
     */
    public NodeBuilder shape(NodeShape shape) {
      nodeAttrs.shape = shape;
      return this;
    }

    /**
     * Set the border color of the node.
     *
     * @param color border color
     * @return node builder
     */
    public NodeBuilder color(Color color) {
      nodeAttrs.color = color;
      return this;
    }

    /**
     * Set the fill color of the node.
     *
     * @param fillColor fill colorr
     * @return node builder
     */
    public NodeBuilder fillColor(Color fillColor) {
      nodeAttrs.fillColor = fillColor;
      return this;
    }

    /**
     * Set the font color of node.
     *
     * @param fontColor font color
     * @return node builder
     */
    public NodeBuilder fontColor(Color fontColor) {
      nodeAttrs.fontColor = fontColor;
      return this;
    }

    /**
     * Set the font style of node.
     *
     * @param fontName font style name
     * @return node builder
     */
    public NodeBuilder fontName(String fontName) {
      nodeAttrs.fontName = fontName;
      nodeAttrs.fontName = FontUtils.selectFont(nodeAttrs.label, nodeAttrs.fontName);
      return this;
    }

    /**
     * Set the {@link Labeljust} of the node, used to control the horizontal position of the
     * {@link NodeAttrs#getLabel()} of the node.
     *
     * @param labeljust labeljust to be added to this node
     * @return node builder
     */
    public NodeBuilder labeljust(Labeljust labeljust) {
      nodeAttrs.labeljust = labeljust;
      return this;
    }

    /**
     * Set the {@link Labelloc} of the node, used to control the vertical position of the
     * {@link NodeAttrs#getLabel()} of the node.
     *
     * @param labelloc labelloc to be added to this node
     * @return node builder
     */
    public NodeBuilder labelloc(Labelloc labelloc) {
      nodeAttrs.labelloc = labelloc;
      return this;
    }

    /**
     * Set the margin of the node, use the same margin horizontally and vertically.
     *
     * @param margin horizontal and vertical margins
     * @return node builder
     * @throws IllegalArgumentException margin less than 0
     */
    public NodeBuilder margin(double margin) {
      return margin(margin, margin);
    }

    /**
     * Set the horizontal and vertical margin of the node.
     *
     * @param horMargin horizontal margin
     * @param verMargin vertical margin
     * @return node builder
     * @throws IllegalArgumentException margin less than 0
     */
    public NodeBuilder margin(double horMargin, double verMargin) {
      Asserts.illegalArgument(horMargin < 0,
                              "Horizontal margin (" + horMargin + ") can not less than 0");
      Asserts.illegalArgument(verMargin < 0,
                              "Vertical margin (" + verMargin + ") can not less than 0");
      nodeAttrs.margin = new UnmodifyFlatPoint(verMargin * Graphviz.PIXEL,
                                               horMargin * Graphviz.PIXEL);
      return this;
    }

    /**
     * Set whether the size of the node needs to be fixed. The real height and width of the node
     * will change with the {@link NodeAttrs#getLabel()} and {@link NodeAttrs#getMargin()} of the
     * node, but once the size of the node is fixed, the size of the node will only be related to
     * the set {@link #height(double)} and {@link #width(double)}.
     *
     * @param fixedSize need fixed the node size
     * @return node builder
     */
    public NodeBuilder fixedSize(boolean fixedSize) {
      nodeAttrs.fixedSize = fixedSize;
      return this;
    }

    /**
     * Set the font size of node.
     *
     * @param fontSize font size
     * @return node builder
     * @throws IllegalArgumentException font size less than 0
     */
    public NodeBuilder fontSize(double fontSize) {
      Asserts.illegalArgument(fontSize < 0, "fontSize (" + fontSize + ") can not less than 0");
      nodeAttrs.fontSize = fontSize;
      return this;
    }

    /**
     * Set the style of node, Please check the details {@link NodeStyle}.
     *
     * @param styles node styles
     * @return node builder
     * @throws IllegalArgumentException empty styles or contains null style
     */
    public NodeBuilder style(NodeStyle... styles) {
      Asserts.nullOrContainsNull(styles);
      nodeAttrs.styles = Arrays.asList(styles);
      return this;
    }

    /**
     * Applies one or more font styles (e.g., bold, italic) to the node's label text.
     * <p>
     * Multiple font styles can be combined. For example, calling
     * {@code fontStyle(FontStyle.BOLD, FontStyle.ITALIC)} will make the text both bold and italic.
     * </p>
     *
     * @param fontStyles one or more {@link FontStyle} values to apply
     * @return this {@code NodeBuilder} for method chaining
     * @throws IllegalArgumentException if the provided array or any element within it is
     *                                  {@code null}
     */
    public NodeBuilder fontStyle(FontStyle... fontStyles) {
      Asserts.nullOrContainsNull(fontStyles);
      nodeAttrs.fontStyles = Arrays.asList(fontStyles);
      return this;
    }

    /**
     * Set the number of sides of the node's polygon. When the shape of the node is
     * {@link NodeShapeEnum#REGULAR_POLYLINE}, use this attribute to control the number of polygons,
     * at least 4 sides are required, and no more than 20 sides.
     *
     * @param sides the number of sides of the node's polygon
     * @return node builder
     * @throws IllegalArgumentException sides must be between 4 and 20
     */
    public NodeBuilder sides(int sides) {
      Asserts.illegalArgument(sides < 4, "side can not be lower than 4");
      Asserts.illegalArgument(sides > 20, "side can not be large than 20");
      nodeAttrs.sides = sides;
      return this;
    }

    /**
     * Set a node link, only valid when the output is <tt>svg</tt>.
     *
     * @param href node href
     * @return node builder
     */
    public NodeBuilder href(String href) {
      nodeAttrs.href = href;
      return this;
    }

    /**
     * Sets the tooltip text for the Node.
     *
     * <p><strong>Note:</strong> The tooltip only takes effect if the node has an {@code href} set
     * and is rendered in SVG format. It does not work in PNG, JPG, or other raster formats.</p>
     *
     * @param tooltip the text to be displayed as the tooltip (only works when {@code href} is set)
     * @return node builder
     */
    public NodeBuilder tooltip(String tooltip) {
      nodeAttrs.tooltip = tooltip;
      return this;
    }

    /**
     * Sets an image URL or local file path for the node.
     *
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li>For <strong>SVG output</strong>, the image URL is directly embedded without validation.</li>
     *   <li>For <strong>other output formats</strong>, the image must be accessible; otherwise, an error will occur.</li>
     * </ul>
     *
     * <p><strong>Security Warning:</strong></p>
     * <ul>
     *   <li><strong>Remote URL Risks:</strong> Loading images from untrusted URLs may expose the application to
     *       <a href="https://owasp.org/www-community/attacks/Server_Side_Request_Forgery">Server-Side Request Forgery (SSRF)</a> attacks,
     *       slow network requests, or malicious payloads.</li>
     *   <li><strong>Local File Risks:</strong> If an unvalidated local file path is used, it may allow unauthorized access
     *       to system files (Path Traversal vulnerability).</li>
     *   <li><strong>Large Image Risks:</strong> Maliciously large images can cause high memory usage or
     *       <a href="https://owasp.org/www-community/attacks/Denial_of_Service">Denial of Service (DoS) attacks</a>.</li>
     * </ul>
     *
     * <p><strong>Recommended Security Practices:</strong></p>
     * <ul>
     *   <li>Use only trusted domains or pre-validated image URLs.</li>
     *   <li>Restrict local file paths to a dedicated image directory.</li>
     *   <li>Limit image file size to prevent excessive memory consumption.</li>
     *   <li>Sanitize file extensions and content types to allow only valid image formats (e.g., PNG, JPG).</li>
     * </ul>
     *
     * @param image the image URL or local file path
     * @return node builder
     */
    public NodeBuilder image(String image) {
      nodeAttrs.image = image;
      return this;
    }

    /**
     * Set the width and height of the node image, which only takes effect when
     * {@link #image(String)} is set. If {@link NodeAttrs#getFixedSize()} is true, the real size of
     * the node will be enlarged by this property.
     *
     * @param height image height
     * @param width  image width
     * @return node builder
     * @throws IllegalArgumentException image height or width less than 0
     */
    public NodeBuilder imageSize(double height, double width) {
      Asserts.illegalArgument(height <= 0, "Image height can not less than 0");
      Asserts.illegalArgument(width <= 0, "Image width can not less than 0");
      nodeAttrs.imageSize = new UnmodifyFlatPoint(height * Graphviz.PIXEL, width * Graphviz.PIXEL);
      return this;
    }

    /**
     * Set the border width of the node.
     *
     * @param penWidth border width
     * @return node builder
     * @throws IllegalArgumentException pen width less than 0
     */
    public NodeBuilder penWidth(double penWidth) {
      Asserts.illegalArgument(penWidth < 0, "penWidth can not be less than 0");
      nodeAttrs.penWidth = penWidth;
      return this;
    }

    /**
     * Assigns an HTML-like {@link Table} structure as this node's label, replacing the simple
     * textual label set via {@link #label(String)}. This allows for a more complex arrangement of
     * row and column cells, effectively converting the node label into a small "table."
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * Table table = Html.table()
     *     .tr(Html.td().text("Header 1"), Html.td().text("Header 2"))
     *     .tr(Html.td().text("Row 1, Col 1"), Html.td().text("Row 1, Col 2"));
     * Node node = Node.builder()
     *     .table(table)
     *     .build();
     * }</pre>
     *
     * @param table the HTML-like {@link Table} to use as the node's label
     * @return this {@code NodeBuilder} for method chaining
     * @throws IllegalArgumentException if {@code table} is {@code null}
     * @see #label(String)
     * @see #labelTag
     */
    public NodeBuilder table(Table table) {
      Asserts.nullArgument(table, "table");
      nodeAttrs.table = table;
      return this;
    }

    /**
     * Set an {@link Assemble} to replace the {@link #label(String)}. When setting a label for a
     * node, the program will calculate the size of the label, and then automatically put the label
     * in the appropriate position of the node. If {@link Assemble} is set, assemble will be placed
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
     *    Graphviz.digraph()
     *         .addNode(
     *             Node.builder()
     *                 .assemble(
     *                     Assemble.builder()
     *                         .width(1)
     *                         .height(0.4)
     *                         .addCell(0, 0,
     *                                  Node.builder()
     *                                      .width(0.5)
     *                                      .height(0.4)
     *                                      .label("LEFT")
     *                                      .build())
     *                         .addCell(0.5, 0,
     *                                  Node.builder()
     *                                      .width(0.5)
     *                                      .height(0.4)
     *                                      .label("RIGHT")
     *                                      .build())
     *                         .build()
     *                 )
     *                 .build()
     *         )
     *         .build();
     * }
     * </pre>
     *
     * @param assemble assemble
     * @return node builder
     */
    public NodeBuilder assemble(Assemble assemble) {
      nodeAttrs.assemble = assemble;
      return this;
    }

    /**
     * Returns a {@link Node}.
     *
     * @return {@code Node}
     */
    public Node build() {
      return new Node(nodeAttrs.clone());
    }
  }
}
