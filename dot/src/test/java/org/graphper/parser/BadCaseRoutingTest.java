/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Assemble;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Tend;
import org.graphper.def.Curves;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BadCaseRoutingTest {

  @Test
  public void labeledFlatEdgeUsesLessCongestedClusterSide() throws Exception {
    Graphviz graph = parseResource("/flat-edge-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    Assertions.assertFalse(intersects(lineByColor(graph, draw, "#00000F"),
                                       lineByColor(graph, draw, "#00001F")));
  }

  @Test
  public void minlenZeroTailPrecedesHeadWithoutStrongerConflict() throws Exception {
    Graphviz graph = parseResource("/minlen-zero-directed-order.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    NodeDrawProp note = nodeById(graph, draw, "note");
    NodeDrawProp target = nodeById(graph, draw, "target");

    Assertions.assertNotNull(note);
    Assertions.assertNotNull(target);
    Assertions.assertTrue(note.getRightBorder() < target.getLeftBorder(),
                          "minlen=0 tail must remain left of its head");
  }

  @Test
  public void minlenZeroDirectionAndCrossingReductionAreBothPreserved() throws Exception {
    Graphviz graph = parseResource("/minlen-zero-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    NodeDrawProp tail = nodeById(graph, draw, "sh0013");
    NodeDrawProp head = nodeById(graph, draw, "sh0012");

    Assertions.assertNotNull(tail);
    Assertions.assertNotNull(head);
    Assertions.assertTrue(tail.getRightBorder() < head.getLeftBorder(),
                          "crossing reduction must not reverse a minlen=0 edge");
    Assertions.assertFalse(intersects(lineByColor(graph, draw, "#000022"),
                                       lineByColor(graph, draw, "#000026")),
                           "crossing reduction must reorder the unconstrained upstream path");
  }

  @Test
  public void minlenZeroFanoutPlacesTailBeforeEveryHead() throws Exception {
    Graphviz graph = parseResource("/minlen-zero-fanout.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    NodeDrawProp tail = nodeById(graph, draw, "sh0010");

    Assertions.assertNotNull(tail);
    for (String headId : new String[]{"sh0006", "sh0008", "sh0009"}) {
      NodeDrawProp head = nodeById(graph, draw, headId);
      Assertions.assertNotNull(head);
      Assertions.assertTrue(tail.getRightBorder() < head.getLeftBorder(),
                            "flat fan-out tail must precede " + headId);
    }
  }

  @Test
  public void minlenZeroCycleStillReceivesACompleteOrder() throws Exception {
    Graphviz graph = parseResource("/minlen-zero-cycle.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);

    for (String id : new String[]{"a", "b", "c"}) {
      Assertions.assertNotNull(nodeById(graph, draw, id), "cycle node missing: " + id);
    }
    Assertions.assertEquals(3, draw.lines().size());
  }

  @Test
  public void minlenZeroOrderSurvivesClusterCollapseAndFinalMincross() throws Exception {
    Graphviz graph = parseResource("/minlen-zero-cluster-order.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);

    for (Line line : graph.lines()) {
      if (line.lineAttrs().getMinlen() != 0) {
        continue;
      }
      NodeDrawProp tail = draw.getNodeDrawProp(line.tail());
      NodeDrawProp head = draw.getNodeDrawProp(line.head());
      Assertions.assertEquals(tail.getY(), head.getY(), 1e-6,
                              "minlen=0 endpoints left the same rank");
      Assertions.assertTrue(tail.getRightBorder() <= head.getLeftBorder() + 1e-6,
                            line.tail().nodeAttrs().getId() + " must precede "
                                + line.head().nodeAttrs().getId());
    }
  }

  @Test
  public void roundedLabeledParallelEdgesUseDistinctCorridors() throws Exception {
    String source = resourceText("/rounded-parallel-edge-overlap.dot");
    assertRoundedParallelEdgesUseDistinctCorridors(DotParser.parse(source));
    assertRoundedParallelEdgesUseDistinctCorridors(
        DotParser.parse(source.replaceFirst("\\{", "{ splines=rounded;")));
  }

  @Test
  public void flatEdgesStayOutsideTheirOwnLabelsWithinTheRankEnvelope() throws Exception {
    Graphviz graph = parseResource("/flat-label-route-envelope.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);

    for (String color : new String[]{"#00000E", "#000012"}) {
      LineDrawProp line = lineByColor(graph, draw, color);
      Assertions.assertNotNull(line);
      Assertions.assertFalse(intersectsLabelInterior(line),
                             color + " passes through its own label");
    }
  }

  @Test
  public void parallelHtmlHeadLabelsAvoidEachOtherAndEndpointNodes() throws Exception {
    assertParallelEndpointLabelsAvoidObstacles("/parallel-head-labels.dot");
  }

  @Test
  public void parallelHtmlTailLabelsAvoidEachOtherAndEndpointNodes() throws Exception {
    assertParallelEndpointLabelsAvoidObstacles("/parallel-tail-labels.dot");
  }

  @Test
  public void endpointLabelsStayOutsideTheirOwnerEdge() throws Exception {
    assertEndpointLabelsOutsideOwnerEdge("/endpoint-label-owner-edge.dot");
    assertEndpointLabelsOutsideOwnerEdge("/wide-tail-label-owner-edge.dot");
  }

  @Test
  public void spanningFlatEdgeParticipatesInLocalCrossingReduction() throws Exception {
    Graphviz graph = parseResource("/flat-edge-local-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp flat = lineByColor(graph, draw, "#000013");
    NodeDrawProp tail = nodeById(graph, draw, "sh0007");
    NodeDrawProp head = nodeById(graph, draw, "sh0006");

    Assertions.assertTrue(tail.getRightBorder() <= head.getLeftBorder(),
                          "flat crossing reduction reversed the edge");
    for (Node node : graph.nodes()) {
      NodeDrawProp candidate = draw.getNodeDrawProp(node);
      if (candidate == tail || candidate == head || Math.abs(candidate.getY() - tail.getY()) > 1e-6) {
        continue;
      }
      Assertions.assertFalse(candidate.getX() > tail.getX() && candidate.getX() < head.getX(),
                             node.nodeAttrs().getId() + " remains inside the flat edge span");
    }
    for (Line line : graph.lines()) {
      if (line.tail() == flat.getLine().tail() || line.tail() == flat.getLine().head()
          || line.head() == flat.getLine().tail() || line.head() == flat.getLine().head()) {
        continue;
      }
      Assertions.assertFalse(intersects(flat, draw.getLineDrawProp(line)),
                             "flat edge still crosses " + line.lineAttrs().getColor());
    }
  }

  private void assertParallelEndpointLabelsAvoidObstacles(String resource) throws Exception {
    Graphviz graph = parseResource(resource);
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp first = lineByColor(graph, draw, "#00000E");
    LineDrawProp second = lineByColor(graph, draw, "#000012");
    LabelBox firstLabel = floatAssembleBox(draw, first);
    LabelBox secondLabel = floatAssembleBox(draw, second);

    Assertions.assertNotNull(firstLabel);
    Assertions.assertNotNull(secondLabel);
    Assertions.assertFalse(firstLabel.overlaps(secondLabel), "parallel headlabels overlap");
    Tend tend = first.lineAttrs().getFloatLabels()[0].getTend();
    String endpointId = tend == Tend.HEAD ? "sh0008" : "sh0006";
    NodeDrawProp endpoint = nodeById(graph, draw, endpointId);
    LabelBox endpointBox = LabelBox.of(endpoint);
    Assertions.assertEquals(4, firstLabel.distanceTo(endpointBox), 1e-6);
    Assertions.assertEquals(4, secondLabel.distanceTo(endpointBox), 1e-6);
    for (String nodeId : new String[]{"sh0006", "sh0008"}) {
      NodeDrawProp node = nodeById(graph, draw, nodeId);
      Assertions.assertFalse(firstLabel.overlaps(LabelBox.of(node)),
                             "first headlabel overlaps " + nodeId);
      Assertions.assertFalse(secondLabel.overlaps(LabelBox.of(node)),
                             "second headlabel overlaps " + nodeId);
    }
  }

  private void assertEndpointLabelsOutsideOwnerEdge(String resource) throws Exception {
    Graphviz graph = parseResource(resource);
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    for (Line line : graph.lines()) {
      LineDrawProp lineProp = draw.getLineDrawProp(line);
      for (Assemble assemble : lineProp.getFloatAssembles()) {
        LabelBox label = floatAssembleBox(draw, lineProp, assemble);
        Assertions.assertFalse(intersectsBox(lineProp, label),
                               "endpoint label is pierced by its owner edge in " + resource);
      }
    }
  }

  private void assertRoundedParallelEdgesUseDistinctCorridors(Graphviz graph) throws Exception {
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp first = lineByColor(graph, draw, "#00001D");
    LineDrawProp second = lineByColor(graph, draw, "#000021");

    Assertions.assertNotNull(first);
    Assertions.assertNotNull(second);
    Assertions.assertFalse(samePath(first, second),
                           "rounded parallel edges collapsed onto the same path");
    Assertions.assertNotNull(first.getLabelCenter());
    Assertions.assertNotNull(second.getLabelCenter());
    Assertions.assertNotEquals(first.getLabelCenter().getY(), second.getLabelCenter().getY(), 1e-6,
                               "parallel edge labels collapsed onto the same lane");
  }

  @Test
  public void independentNonBranchingPathCanRemoveTwoCrossings() throws Exception {
    Graphviz graph = parseResource("/non-branching-path-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp diagonal = lineByColor(graph, draw, "#00003E");
    Assertions.assertFalse(intersects(diagonal, lineByColor(graph, draw, "#000036")));
    Assertions.assertFalse(intersects(diagonal, lineByColor(graph, draw, "#00003A")));
  }

  @Test
  public void sameRankLabelKeepsItsRoutingRankToAvoidMiddleNode() throws Exception {
    Graphviz graph = parseResource("/fan-in-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp login = lineByColor(graph, draw, "#00002D");
    NodeDrawProp middle = nodeByColor(graph, draw, "#000012");
    Assertions.assertNotNull(login);
    Assertions.assertNotNull(login.getLabelCenter());
    Assertions.assertNotNull(login.getLabelSize());
    Assertions.assertNotNull(middle);
    double labelLeft = login.getLabelCenter().getX() - login.getLabelSize().getWidth() / 2;
    double labelRight = login.getLabelCenter().getX() + login.getLabelSize().getWidth() / 2;
    double labelTop = login.getLabelCenter().getY() - login.getLabelSize().getHeight() / 2;
    double labelBottom = login.getLabelCenter().getY() + login.getLabelSize().getHeight() / 2;
    Assertions.assertTrue(labelRight < middle.getLeftBorder() || labelLeft > middle.getRightBorder()
                              || labelBottom < middle.getUpBorder()
                              || labelTop > middle.getDownBorder());
  }

  @Test
  public void postMincrossContainerPassDoesNotCreateCrossings() throws Exception {
    Graphviz graph = parseResource("/case19-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp parserToSoap = lineByColor(graph, draw, "#FE0000");
    LineDrawProp formToTerminal = lineByColor(graph, draw, "#000011");
    LineDrawProp terminalToForm = lineByColor(graph, draw, "#000012");
    Assertions.assertFalse(intersects(parserToSoap, formToTerminal));
    Assertions.assertFalse(intersects(parserToSoap, terminalToForm));
  }

  @Test
  public void zeroCrossInitOrderIsNotDegradedByLaterMincrossPasses() throws Exception {
    Graphviz graph = parseResource("/component-0385-crossing.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    List<LineDrawProp> lines = new ArrayList<>(draw.lines());
    Assertions.assertEquals(9, lines.size());
    for (int i = 0; i < lines.size(); i++) {
      for (int j = i + 1; j < lines.size(); j++) {
        Assertions.assertFalse(intersects(lines.get(i), lines.get(j)),
                               "crossing between " + lines.get(i).lineAttrs().getColor()
                                   + " and " + lines.get(j).lineAttrs().getColor());
      }
    }
  }

  @Test
  public void zeroCrossFlatOrderStillCompactsSequenceLifelines() throws Exception {
    Graphviz graph = parseResource("/sequence-flat-order.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    Assertions.assertTrue(draw.height() < 600,
                          "sequence lifelines expanded to " + draw.height());
  }

  @Test
  public void virtualNodeDoesNotDetourAroundItsParentCluster() throws Exception {
    Graphviz graph = parseResource("/cluster-virtual-node-detour.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp line = lineByColor(graph, draw, "#FE0000");
    ClusterDrawProp parent = clusterById(draw, "cluster_A");

    Assertions.assertNotNull(line);
    Assertions.assertNotNull(parent);
    for (FlatPoint point : sample(line)) {
      Assertions.assertTrue(point.getX() <= parent.getRightBorder() + 1,
                            "virtual waypoint pushed the line around cluster right="
                                + parent.getRightBorder() + ": " + point);
    }
  }

  @Test
  public void siblingClustersDoNotOverlap() throws Exception {
    Graphviz graph = parseResource("/sibling-cluster-overlap.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    ClusterDrawProp clusterC = clusterById(draw, "cluster_C");
    ClusterDrawProp clusterD = clusterById(draw, "cluster_D");

    Assertions.assertNotNull(clusterC);
    Assertions.assertNotNull(clusterD);
    double overlapX = Math.min(clusterC.getRightBorder(), clusterD.getRightBorder())
        - Math.max(clusterC.getLeftBorder(), clusterD.getLeftBorder());
    double overlapY = Math.min(clusterC.getDownBorder(), clusterD.getDownBorder())
        - Math.max(clusterC.getUpBorder(), clusterD.getUpBorder());
    Assertions.assertFalse(overlapX > 0 && overlapY > 0,
                           "Cluster C and D overlap by " + overlapX + " x " + overlapY);
  }

  @Test
  public void clustersWithoutCommonRanksAreNotForcedSideBySide() throws Exception {
    Graphviz graph = parseResource("/non-overlapping-rank-clusters.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    ClusterDrawProp cluster1 = clusterById(draw, "cluster_1");
    ClusterDrawProp cluster6 = clusterById(draw, "cluster_6");

    Assertions.assertNotNull(cluster1);
    Assertions.assertNotNull(cluster6);
    double overlapX = Math.min(cluster1.getRightBorder(), cluster6.getRightBorder())
        - Math.max(cluster1.getLeftBorder(), cluster6.getLeftBorder());
    Assertions.assertTrue(overlapX > 0,
                          "clusters without a common rank were incorrectly forced side-by-side");
    Assertions.assertTrue(cluster6.getDownBorder() <= cluster1.getUpBorder(),
                          "clusters without a common rank should remain vertically separated");
  }

  @Test
  public void routingVirtualStaysInsideItsOwnParentCluster() throws Exception {
    Graphviz graph = parseResource("/own-cluster-routing-virtual.dot");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(graph);
    LineDrawProp line = lineByColor(graph, draw, "#FE0000");
    ClusterDrawProp parent = clusterById(draw, "cluster_0");

    Assertions.assertNotNull(line);
    Assertions.assertNotNull(parent);
    for (FlatPoint point : sample(line)) {
      Assertions.assertTrue(point.getX() >= parent.getLeftBorder() - 1
                                && point.getX() <= parent.getRightBorder() + 1,
                            "routing virtual escaped its own parent cluster: " + point);
    }
  }

  private LineDrawProp lineByColor(Graphviz graph, DrawGraph draw, String color) {
    for (Line line : graph.lines()) {
      if (Color.ofRGB(color).equals(line.lineAttrs().getColor())) {
        return draw.getLineDrawProp(line);
      }
    }
    return null;
  }

  private NodeDrawProp nodeByColor(Graphviz graph, DrawGraph draw, String color) {
    for (Node node : graph.nodes()) {
      if (Color.ofRGB(color).equals(node.nodeAttrs().getColor())) {
        return draw.getNodeDrawProp(node);
      }
    }
    return null;
  }

  private NodeDrawProp nodeById(Graphviz graph, DrawGraph draw, String id) {
    for (Node node : graph.nodes()) {
      if (id.equals(node.nodeAttrs().getId())) {
        return draw.getNodeDrawProp(node);
      }
    }
    return null;
  }

  private ClusterDrawProp clusterById(DrawGraph draw, String id) {
    for (ClusterDrawProp cluster : draw.clusters()) {
      if (id.equals(cluster.getCluster().id())) {
        return cluster;
      }
    }
    return null;
  }

  private boolean intersects(LineDrawProp first, LineDrawProp second) {
    Assertions.assertNotNull(first);
    Assertions.assertNotNull(second);
    List<FlatPoint> firstPoints = sample(first);
    List<FlatPoint> secondPoints = sample(second);
    for (int i = 0; i + 1 < firstPoints.size(); i++) {
      for (int j = 0; j + 1 < secondPoints.size(); j++) {
        if (segmentIntersects(firstPoints.get(i), firstPoints.get(i + 1),
                              secondPoints.get(j), secondPoints.get(j + 1))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean samePath(LineDrawProp first, LineDrawProp second) {
    if (first.size() != second.size()) {
      return false;
    }
    for (int i = 0; i < first.size(); i++) {
      if (FlatPoint.twoFlatPointDistance(first.get(i), second.get(i)) > 1e-6) {
        return false;
      }
    }
    return true;
  }

  private boolean intersectsLabelInterior(LineDrawProp line) {
    FlatPoint center = line.getLabelCenter();
    FlatPoint size = line.getLabelSize();
    if (center == null || size == null) {
      return false;
    }

    double epsilon = 1e-6;
    double left = center.getX() - size.getWidth() / 2 + epsilon;
    double right = center.getX() + size.getWidth() / 2 - epsilon;
    double top = center.getY() - size.getHeight() / 2 + epsilon;
    double bottom = center.getY() + size.getHeight() / 2 - epsilon;
    List<FlatPoint> points = sample(line);
    for (int i = 0; i + 1 < points.size(); i++) {
      if (segmentIntersectsBox(points.get(i), points.get(i + 1), left, right, top, bottom)) {
        return true;
      }
    }
    return false;
  }

  private boolean segmentIntersectsBox(FlatPoint from, FlatPoint to, double left, double right,
                                       double top, double bottom) {
    double minX = Math.min(from.getX(), to.getX());
    double maxX = Math.max(from.getX(), to.getX());
    double minY = Math.min(from.getY(), to.getY());
    double maxY = Math.max(from.getY(), to.getY());
    if (maxX < left || minX > right || maxY < top || minY > bottom) {
      return false;
    }
    if (from.getX() >= left && from.getX() <= right && from.getY() >= top
        && from.getY() <= bottom) {
      return true;
    }
    if (to.getX() >= left && to.getX() <= right && to.getY() >= top
        && to.getY() <= bottom) {
      return true;
    }
    FlatPoint upperLeft = new FlatPoint(left, top);
    FlatPoint upperRight = new FlatPoint(right, top);
    FlatPoint lowerLeft = new FlatPoint(left, bottom);
    FlatPoint lowerRight = new FlatPoint(right, bottom);
    return segmentIntersects(from, to, upperLeft, upperRight)
        || segmentIntersects(from, to, upperRight, lowerRight)
        || segmentIntersects(from, to, lowerRight, lowerLeft)
        || segmentIntersects(from, to, lowerLeft, upperLeft);
  }

  private LabelBox floatAssembleBox(DrawGraph draw, LineDrawProp line) {
    for (Assemble assemble : line.getFloatAssembles()) {
      return floatAssembleBox(draw, line, assemble);
    }
    return null;
  }

  private LabelBox floatAssembleBox(DrawGraph draw, LineDrawProp line, Assemble assemble) {
    LabelBox box = null;
    for (Node cell : assemble.getCells()) {
      NodeDrawProp cellProp = draw.getNodeDrawProp(cell);
      box = box == null ? LabelBox.of(cellProp) : box.union(cellProp);
    }
    return box;
  }

  private boolean intersectsBox(LineDrawProp line, LabelBox box) {
    double epsilon = 1e-6;
    List<FlatPoint> points = sample(line);
    for (int i = 0; i + 1 < points.size(); i++) {
      if (segmentIntersectsBox(points.get(i), points.get(i + 1),
                              box.left + epsilon, box.right - epsilon,
                              box.top + epsilon, box.bottom - epsilon)) {
        return true;
      }
    }
    return false;
  }

  private static final class LabelBox {

    private final double left;
    private final double right;
    private final double top;
    private final double bottom;

    private LabelBox(double left, double right, double top, double bottom) {
      this.left = left;
      this.right = right;
      this.top = top;
      this.bottom = bottom;
    }

    private static LabelBox of(NodeDrawProp node) {
      return new LabelBox(node.getLeftBorder(), node.getRightBorder(),
                          node.getUpBorder(), node.getDownBorder());
    }

    private LabelBox union(NodeDrawProp node) {
      return new LabelBox(Math.min(left, node.getLeftBorder()),
                          Math.max(right, node.getRightBorder()),
                          Math.min(top, node.getUpBorder()),
                          Math.max(bottom, node.getDownBorder()));
    }

    private boolean overlaps(LabelBox other) {
      return Math.min(right, other.right) - Math.max(left, other.left) > 1e-6
          && Math.min(bottom, other.bottom) - Math.max(top, other.top) > 1e-6;
    }

    private double distanceTo(LabelBox other) {
      double dx = Math.max(Math.max(left - other.right, 0), other.left - right);
      double dy = Math.max(Math.max(top - other.bottom, 0), other.top - bottom);
      return Math.hypot(dx, dy);
    }
  }

  private List<FlatPoint> sample(LineDrawProp line) {
    List<FlatPoint> result = new ArrayList<>();
    result.add(line.get(0));
    if (!line.isBesselCurve()) {
      result.addAll(line.subList(1, line.size()));
      return result;
    }
    for (int i = 0; i + 3 < line.size(); i += 3) {
      for (int step = 1; step <= 20; step++) {
        result.add(Curves.besselEquationCalc(step / 20D, line.get(i), line.get(i + 1),
                                             line.get(i + 2), line.get(i + 3)));
      }
    }
    return result;
  }

  private boolean segmentIntersects(FlatPoint a, FlatPoint b, FlatPoint c, FlatPoint d) {
    return orient(a, b, c) * orient(a, b, d) < 0
        && orient(c, d, a) * orient(c, d, b) < 0;
  }

  private double orient(FlatPoint a, FlatPoint b, FlatPoint c) {
    return (b.getX() - a.getX()) * (c.getY() - a.getY())
        - (b.getY() - a.getY()) * (c.getX() - a.getX());
  }


  private Graphviz parseResource(String path) throws Exception {
    return DotParser.parse(resourceText(path));
  }

  private String resourceText(String path) throws Exception {
    InputStream input = BadCaseRoutingTest.class.getResourceAsStream(path);
    Assertions.assertNotNull(input);
    try (InputStream in = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int read;
      while ((read = in.read(buffer)) != -1) {
        output.write(buffer, 0, read);
      }
      return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
