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
package org.graphper.layout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphper.api.Assemble;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Html;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.HtmlConvertor.LabelIdSpace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlConvertorTest {

  @Test
  public void fixedTableHonorsExplicitDimensions() {
    assertTableSize(55, 27);
    assertTableSize(52, 17);
    assertTableSize(8, 15);
    assertTableSize(6, 15);
  }

  @Test
  public void nonFixedDimensionsRemainMinimums() {
    Assemble small = HtmlConvertor.toAssemble(Html.table().width(6).height(8).tr(Html.td()));
    Assertions.assertTrue(small.getWidth() >= 6);
    Assertions.assertTrue(small.getHeight() >= 8);

    Assemble large = HtmlConvertor.toAssemble(Html.table().width(55).height(27).tr(Html.td()));
    Assertions.assertEquals(55, large.getWidth(), 0.000001);
    Assertions.assertEquals(27, large.getHeight(), 0.000001);
  }

  @Test
  public void tinyFixedTableKeepsPositiveCellGeometry() {
    Assemble assemble = HtmlConvertor.toAssemble(
        Html.table().fixedSize(true).width(1).height(1).tr(Html.td()));
    Assertions.assertEquals(1, assemble.getWidth(), 0.000001);
    Assertions.assertEquals(1, assemble.getHeight(), 0.000001);
    for (org.graphper.api.Node cell : assemble.getCells()) {
      Assertions.assertTrue(cell.nodeAttrs().getWidth() > 0);
      Assertions.assertTrue(cell.nodeAttrs().getHeight() > 0);
    }
  }

  /**
   * An id space hands out the authored ids as long as nothing else has claimed them, so the
   * identity a caller can look a cell up by is the one the author wrote.
   */
  @Test
  public void idSpaceKeepsUncontestedAuthoredIds() {
    Html.Table table = Html.table().id("outer")
        .tr(Html.td().id("left").text("a"), Html.td().id("right").text("b"));

    Assemble assemble = HtmlConvertor.toAssemble(table, "owner_7", new LabelIdSpace());

    Assertions.assertEquals(setOf("outer", "left", "right"), idsOf(assemble));
    // The owner-local cell ids that edge ports resolve against are the authored ids either way.
    Assertions.assertEquals(setOf("outer", "left", "right"), localCellIds(assemble));
  }

  /**
   * A second label converted in the same id space cannot take an identity the first one already
   * holds, and falls back to a path under its own scope.
   */
  @Test
  public void idSpaceFallsBackToTheScopePathForContestedIds() {
    LabelIdSpace idSpace = new LabelIdSpace();
    Html.Table table = Html.table().id("outer")
        .tr(Html.td().id("left").text("a"), Html.td().id("right").text("b"));

    Assemble first = HtmlConvertor.toAssemble(table, "owner_7", idSpace);
    Assemble second = HtmlConvertor.toAssemble(table, "owner_8", idSpace);

    Assertions.assertEquals(setOf("outer", "left", "right"), idsOf(first));
    Assertions.assertEquals(setOf("owner_8::t", "owner_8::r0c0", "owner_8::r0c1"), idsOf(second));

    // Scoping the identity never changes the owner-local cell id an edge port resolves against.
    Assertions.assertEquals(localCellIds(first), localCellIds(second));
    Assertions.assertEquals(setOf("outer", "left", "right"), localCellIds(second));
  }

  /**
   * A reserved identity belongs to a real node of the graph and can never be handed to a label.
   */
  @Test
  public void idSpaceNeverHandsOutAReservedIdentity() {
    LabelIdSpace idSpace = new LabelIdSpace();
    idSpace.reserve("left");

    Assemble assemble = HtmlConvertor.toAssemble(
        Html.table().id("outer").tr(Html.td().id("left").text("a")), "owner_7", idSpace);

    Assertions.assertEquals(setOf("outer", "owner_7::r0c0"), idsOf(assemble));
    Assertions.assertEquals(setOf("outer", "left"), localCellIds(assemble));
  }

  /**
   * An authored id shaped exactly like a scope path must not be able to squat the fallback of
   * another cell.
   */
  @Test
  public void idSpaceProbesPastAnAuthoredIdThatLooksLikeAScopePath() {
    LabelIdSpace idSpace = new LabelIdSpace();

    Assemble first = HtmlConvertor.toAssemble(
        Html.table().id("owner_7::r0c0").tr(Html.td().text("a")), "owner_9", idSpace);
    Assemble second = HtmlConvertor.toAssemble(
        Html.table().tr(Html.td().text("b")), "owner_7", idSpace);

    Assertions.assertEquals(setOf("owner_7::r0c0", "owner_9::r0c0"), idsOf(first));
    Assertions.assertEquals(setOf("owner_7::t", "owner_7::r0c0::2"), idsOf(second));
  }

  /**
   * A nested table is a part of the same label, so it shares the id space and nests its fallback
   * paths under the cell that holds it.
   */
  @Test
  public void nestedTablesShareTheIdSpaceAndNestTheirScopePaths() {
    LabelIdSpace idSpace = new LabelIdSpace();
    Html.Table nested = Html.table().id("inner").tr(Html.td().id("deep").text("d"),
                                                    Html.td().text("e"));
    Html.Table table = Html.table().id("outer").tr(Html.td().id("holder").table(nested));

    Assemble first = HtmlConvertor.toAssemble(table, "owner_7", idSpace);
    Assemble second = HtmlConvertor.toAssemble(table, "owner_8", idSpace);

    Assertions.assertEquals(setOf("outer", "holder"), idsOf(first));
    Assertions.assertEquals(setOf("inner", "deep", "owner_7::r0c0::r0c1"),
                            idsOf(nestedAssemble(first)));

    Assertions.assertEquals(setOf("owner_8::t", "owner_8::r0c0"), idsOf(second));
    Assertions.assertEquals(
        setOf("owner_8::r0c0::t", "owner_8::r0c0::r0c0", "owner_8::r0c0::r0c1"),
        idsOf(nestedAssemble(second)));
  }

  /**
   * A cell with no authored id has no identity to preserve and takes its scope path, which is
   * stable and unique by construction.
   */
  @Test
  public void anonymousCellsTakeTheScopePath() {
    Assemble assemble = HtmlConvertor.toAssemble(Html.table().tr(Html.td().text("a")), "owner_7",
                                                 new LabelIdSpace());
    Assertions.assertEquals(setOf("owner_7::t", "owner_7::r0c0"), idsOf(assemble));
  }

  /**
   * An id space has nothing to fall back to without a scope, so it must refuse rather than hand out
   * identities it cannot keep unique. Every caller that has no scope has to pass no id space, which
   * is what a draw property built outside a layout does.
   */
  @Test
  public void anIdSpaceWithoutAScopeIsRefused() {
    Html.Table table = Html.table().id("outer").tr(Html.td().id("left").text("a"));
    Assertions.assertThrows(IllegalArgumentException.class,
                            () -> HtmlConvertor.toAssemble(table, null, new LabelIdSpace()));

    // A node draw property built without a cell scope keeps the authored ids and does not throw,
    // even once it has joined a graph and been handed an id space.
    NodeDrawProp prop = new NodeDrawProp(Node.builder().id("n").table(table).build(),
                                         Node.builder().id("n").table(table).build().nodeAttrs());
    prop.setLabelIdSpace(new LabelIdSpace());
    Assertions.assertEquals(setOf("outer", "left"), idsOf(prop.getAssemble()));
  }

  /**
   * The two id-space free overloads are the shapes that existed before an id space did, and callers
   * outside the layout engine still get them unchanged.
   */
  @Test
  public void overloadsWithoutAnIdSpaceKeepTheirLegacyShape() {
    Html.Table table = Html.table().id("outer")
        .tr(Html.td().id("left").text("a"), Html.td().id("right").text("b"));

    Assertions.assertEquals(setOf("outer", "left", "right"),
                            idsOf(HtmlConvertor.toAssemble(table)));
    Assertions.assertEquals(setOf("outer", "owner_7::r0c0", "owner_7::r0c1"),
                            idsOf(HtmlConvertor.toAssemble(table, "owner_7")));
  }

  /**
   * The regression guard for the identity scoping change: an authored html id that nothing else in
   * the graph uses stays resolvable through {@link DrawGraph#nodeId(Node)}, for every kind of label
   * owner. This is what {@code visual_case.TableCaseTest#testTableProp} asserts for a graph label.
   */
  @Test
  public void uncontestedAuthoredIdsResolveThroughNodeId() throws Exception {
    Node tail = Node.builder().id("tail")
        .table(Html.table().id("nodeTbl").tr(Html.td().id("nodeTd").text("node-label")))
        .build();
    Node head = Node.builder().id("head").build();

    FloatLabel floatLabel = FloatLabel.builder().lengthRatio(0.5)
        .table(Html.table().id("floatTbl").tr(Html.td().id("floatTd").text("float-label")))
        .build();
    Line line = Line.builder(tail, head)
        .table(Html.table().id("edgeTbl").tr(Html.td().id("edgeTd").text("edge-label")))
        .floatLabels(floatLabel)
        .build();
    Cluster cluster = Cluster.builder()
        .table(Html.table().id("clusterTbl").tr(Html.td().id("clusterTd").text("cluster-label")))
        .addNode(head)
        .build();
    Graphviz graphviz = Graphviz.digraph()
        .table(Html.table().id("graphTbl").tr(Html.td().id("graphTd").text("graph-label")))
        .addLine(line)
        .cluster(cluster)
        .build();

    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graphviz);

    Set<String> resolved = new HashSet<>();
    for (NodeDrawProp prop : draw.nodes()) {
      resolved.add(draw.nodeId(prop.getNode()));
    }

    for (String authored : new String[]{"nodeTbl", "nodeTd", "edgeTbl", "edgeTd", "floatTbl",
                                        "floatTd", "clusterTbl", "clusterTd", "graphTbl",
                                        "graphTd"}) {
      Assertions.assertTrue(resolved.contains(authored),
                            "authored id " + authored + " is not resolvable, got " + resolved);
    }
  }

  /**
   * The id of a real node belongs to that node. A label cell that authors the same id must not be
   * able to become it, which is what happens once cells stop being unconditionally scoped.
   */
  @Test
  public void labelCellCannotStealTheIdentityOfARealNode() throws Exception {
    Node real = Node.builder().id("clash").label("real node").build();
    Node other = Node.builder().id("other")
        .table(Html.table().tr(Html.td().id("clash").text("cell")))
        .build();

    DrawGraph draw = Layout.DOT.getLayoutEngine()
        .layout(Graphviz.digraph().addLine(real, other).build());

    Assertions.assertEquals("clash", draw.nodeId(real));
    Assertions.assertEquals("real node", draw.getNodeDrawProp(real).nodeAttrs().getRealLabel());

    List<Node> cells = new ArrayList<>(
        draw.getNodeDrawProp(other).getAssemble().getCells());
    for (Node cell : cells) {
      Assertions.assertNotEquals("clash", cell.nodeAttrs().getId());
      Assertions.assertNotSame(draw.getNodeDrawProp(real), draw.getNodeDrawProp(cell));
    }
  }

  /**
   * Every html label owner must convert its label under its own identity scope. Cells generated for
   * a label are ordinary {@link Node}s, and {@link Node#equals}/{@link Node#hashCode} match on a
   * non-null id, so before scoping the second label that reused a cell id silently adopted the
   * first label's draw property.
   */
  @Test
  public void repeatedCellIdInDifferentLabelsGetsItsOwnDrawProp() throws Exception {
    Node tail = Node.builder().id("tail")
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("node-label")))
        .build();
    Node head = Node.builder().id("head").build();

    FloatLabel floatLabel = FloatLabel.builder().lengthRatio(0.5)
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("float-label")))
        .build();
    Line line = Line.builder(tail, head)
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("edge-label")))
        .floatLabels(floatLabel)
        .build();
    Cluster cluster = Cluster.builder()
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("cluster-label")))
        .addNode(head)
        .build();
    Graphviz graphviz = Graphviz.digraph()
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("graph-label")))
        .addLine(line)
        .cluster(cluster)
        .build();

    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graphviz);

    Map<String, Assemble> labels = new LinkedHashMap<>();
    labels.put("node", draw.getNodeDrawProp(tail).getAssemble());
    labels.put("edge", draw.getLineDrawProp(line).getAssemble());
    labels.put("float", draw.getLineDrawProp(line).getFloatAssemble(floatLabel));
    labels.put("cluster", draw.getClusterDrawProp(cluster).getAssemble());
    labels.put("graph", draw.getGraphvizDrawProp().getAssemble());

    List<Node> cells = new ArrayList<>();
    List<Node> tables = new ArrayList<>();
    for (Map.Entry<String, Assemble> label : labels.entrySet()) {
      Assemble assemble = label.getValue();
      Assertions.assertNotNull(assemble, label.getKey() + " label was not converted");
      for (Node cell : assemble.getCells()) {
        // NodeAttrs#getLabel falls back to the id, so the table node is the one without a real
        // label of its own.
        if (cell.nodeAttrs().getRealLabel() == null) {
          tables.add(cell);
        } else {
          cells.add(cell);
        }
      }
    }
    Assertions.assertEquals(5, cells.size());
    Assertions.assertEquals(5, tables.size());

    // Distinct global identities, so no two labels share a draw property.
    assertDistinctIdentities(cells);
    assertDistinctIdentities(tables);

    /*
     * The decisive check: a colliding cell resolves to the draw property that the *first* label
     * created, so its attributes carry the other label's text.
     */
    for (Node cell : cells) {
      NodeDrawProp prop = draw.getNodeDrawProp(cell);
      Assertions.assertNotNull(prop, "no draw property for " + cell.nodeAttrs().getRealLabel());
      Assertions.assertEquals(cell.nodeAttrs().getRealLabel(), prop.nodeAttrs().getRealLabel(),
                              "cell resolved to the draw property of another label");
    }

    Set<NodeDrawProp> props = new HashSet<>();
    for (Node cell : cells) {
      Assertions.assertTrue(props.add(draw.getNodeDrawProp(cell)),
                            "two labels share one cell draw property");
    }
    for (Node table : tables) {
      Assertions.assertTrue(props.add(draw.getNodeDrawProp(table)),
                            "two labels share one table draw property");
    }
  }

  @Test
  public void repeatedCellIdInTwoEdgeLabelsGetsItsOwnDrawProp() throws Exception {
    Node a = Node.builder().id("a").build();
    Node b = Node.builder().id("b").build();
    Node c = Node.builder().id("c").build();
    Line first = Line.builder(a, b)
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("first-edge")))
        .build();
    Line second = Line.builder(b, c)
        .table(Html.table().id("tblDup").tr(Html.td().id("cellDup").text("second-edge")))
        .build();

    DrawGraph draw = Layout.DOT.getLayoutEngine()
        .layout(Graphviz.digraph().addLine(first).addLine(second).build());

    List<Node> cells = new ArrayList<>();
    List<Node> tables = new ArrayList<>();
    for (Line line : new Line[]{first, second}) {
      for (Node cell : draw.getLineDrawProp(line).getAssemble().getCells()) {
        if (cell.nodeAttrs().getRealLabel() == null) {
          tables.add(cell);
        } else {
          cells.add(cell);
        }
      }
    }

    Assertions.assertEquals(2, cells.size());
    Assertions.assertEquals(2, tables.size());
    assertDistinctIdentities(cells);
    assertDistinctIdentities(tables);
    Assertions.assertNotSame(draw.getNodeDrawProp(cells.get(0)),
                             draw.getNodeDrawProp(cells.get(1)));
    for (Node cell : cells) {
      Assertions.assertEquals(cell.nodeAttrs().getRealLabel(),
                              draw.getNodeDrawProp(cell).nodeAttrs().getRealLabel());
    }
  }

  private void assertDistinctIdentities(List<Node> nodes) {
    Set<String> ids = new HashSet<>();
    for (Node node : nodes) {
      Assertions.assertTrue(ids.add(node.nodeAttrs().getId()),
                            "duplicated identity " + node.nodeAttrs().getId() + " in " + ids);
    }
    for (int i = 0; i < nodes.size(); i++) {
      for (int j = i + 1; j < nodes.size(); j++) {
        Assertions.assertNotEquals(nodes.get(i), nodes.get(j),
                                   "nodes of two different labels compare equal");
      }
    }
  }

  private Set<String> idsOf(Assemble assemble) {
    Set<String> ids = new HashSet<>();
    for (Node cell : assemble.getCells()) {
      ids.add(cell.nodeAttrs().getId());
    }
    return ids;
  }

  private Set<String> localCellIds(Assemble assemble) {
    Set<String> ids = new HashSet<>();
    for (Node cell : assemble.getCells()) {
      ids.add(assemble.cellId(cell));
    }
    return ids;
  }

  private Set<String> setOf(String... ids) {
    return new HashSet<>(Arrays.asList(ids));
  }

  private Assemble nestedAssemble(Assemble assemble) {
    for (Node cell : assemble.getCells()) {
      Assemble nested = cell.nodeAttrs().getAssemble();
      if (nested != null) {
        return nested;
      }
    }
    throw new AssertionError("no nested assemble in " + idsOf(assemble));
  }

  /**
   * Deferring the conversion makes a container draw property hold on to the authored label and to
   * the shared id space, both of which have to survive a round trip.
   *
   * <p>Node labels are deliberately left out: {@code Cell} is not serializable and
   * {@code NodeDrawProp} holds its root cell in a plain field, so a graph with a node that has
   * children cells has never been serializable. That is untouched by the identity change.
   */
  @Test
  public void aDrawGraphWithHtmlLabelsStaysSerializable() throws Exception {
    Node tail = Node.builder().id("tail").build();
    Node head = Node.builder().id("head").build();
    Cluster cluster = Cluster.builder()
        .table(Html.table().id("clusterTbl").tr(Html.td().id("clusterTd").text("cluster-label")))
        .addNode(head)
        .build();
    Graphviz graphviz = Graphviz.digraph()
        .table(Html.table().id("graphTbl").tr(Html.td().id("graphTd").text("graph-label")))
        .addLine(Line.builder(tail, head)
                     .table(Html.table().id("edgeTbl").tr(Html.td().id("edgeTd").text("edge")))
                     .build())
        .cluster(cluster)
        .build();

    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graphviz);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(draw);
    }

    DrawGraph copy;
    try (ObjectInputStream in = new ObjectInputStream(
        new ByteArrayInputStream(bytes.toByteArray()))) {
      copy = (DrawGraph) in.readObject();
    }

    Set<String> before = new HashSet<>();
    for (NodeDrawProp prop : draw.nodes()) {
      before.add(draw.nodeId(prop.getNode()));
    }
    Set<String> after = new HashSet<>();
    for (NodeDrawProp prop : copy.nodes()) {
      after.add(copy.nodeId(prop.getNode()));
    }
    Assertions.assertEquals(before, after);
    Assertions.assertTrue(after.contains("graphTd"), after.toString());
    Assertions.assertTrue(after.contains("clusterTd"), after.toString());
    Assertions.assertTrue(after.contains("edgeTd"), after.toString());
  }

  private void assertTableSize(double width, double height) {
    Assemble assemble = HtmlConvertor.toAssemble(
        Html.table().fixedSize(true).width(width).height(height).tr(Html.td()));
    Assertions.assertEquals(width, assemble.getWidth(), 0.000001);
    Assertions.assertEquals(height, assemble.getHeight(), 0.000001);
  }
}
