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
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Layout;
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
    for (org.graphper.api.Node node : graph.nodes()) {
      if (Color.ofRGB(color).equals(node.nodeAttrs().getColor())) {
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
    InputStream input = BadCaseRoutingTest.class.getResourceAsStream(path);
    Assertions.assertNotNull(input);
    try (InputStream in = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int read;
      while ((read = in.read(buffer)) != -1) {
        output.write(buffer, 0, read);
      }
      return DotParser.parse(new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
  }
}
