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

package org.graphper.layout.dot;

import java.util.Arrays;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Splines;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstraintFalseTest {

  @Test
  public void feedbackEdgeDoesNotAffectDotRanksButIsRouted() throws Exception {
    assertFeedbackEdge(Layout.DOT, Splines.SPLINE);
  }

  @Test
  public void feedbackEdgeDoesNotAffectDotqRanksButIsRouted() throws Exception {
    assertFeedbackEdge(Layout.DOTQ, Splines.POLYLINE);
  }

  @Test
  public void feedbackEdgeIsRoutedOrthogonally() throws Exception {
    assertFeedbackEdge(Layout.DOT, Splines.ORTHO);
  }

  @Test
  public void constraintFalseCrossingClusterDoesNotAffectInternalRanks() throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Line ab = Line.builder(a, b).build();
    Line bc = Line.builder(b, c).build();
    Line feedback = Line.builder(c, a).constraint(false).minlen(10).label("feedback").build();
    Graphviz graph = Graphviz.digraph()
        .cluster(Cluster.builder().addNode(a, b).build())
        .addNode(c)
        .addLine(ab).addLine(bc).addLine(feedback)
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graph);
    assertDescending(drawGraph, a, b, c);
    assertRouted(drawGraph, feedback);
  }

  @Test
  public void allEdgesCanBeNonConstraining() throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Line ab = Line.builder(a, b).constraint(false).build();
    Line bc = Line.builder(b, c).constraint(false).build();
    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(ab).addLine(bc).build());

    assertRouted(drawGraph, ab);
    assertRouted(drawGraph, bc);
  }

  @Test
  public void nonConstrainingSelfLoopIsStillRouted() throws Exception {
    Node a = node("a");
    Line self = Line.builder(a, a).constraint(false).label("retry").build();
    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(self).build());
    assertRouted(drawGraph, self);
  }

  @Test
  public void parallelConstrainedAndNonConstrainedEdgesAreBothRouted() throws Exception {
    Node a = node("a");
    Node b = node("b");
    Line constrained = Line.builder(a, b).label("rank").build();
    Line visualOnly = Line.builder(a, b).constraint(false).label("visual").build();
    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(constrained).addLine(visualOnly).build());

    assertRouted(drawGraph, constrained);
    assertRouted(drawGraph, visualOnly);
  }

  @Test
  public void ordinaryAttachmentDoesNotAllocateConstraintList() {
    DrawGraph drawGraph = new DrawGraph(Graphviz.digraph().build());
    DotAttachment attachment = new DotAttachment(new DotDigraph(2), drawGraph,
                                                  new java.util.HashMap<>());
    Assertions.assertFalse(attachment.haveNonConstraintLines());
    Assertions.assertTrue(attachment.getNonConstraintLines().isEmpty());
  }

  @Test
  public void templateConstraintCanBeOverriddenByLine() throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Line inheritedFalse = Line.builder(c, a).build();
    Line explicitTrue = Line.builder(a, b).constraint(true).build();
    Graphviz graph = Graphviz.digraph()
        .tempLine(Line.tempLine().constraint(false).build())
        .addLine(inheritedFalse)
        .addLine(explicitTrue)
        .build();

    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(graph);
    Assertions.assertEquals(Boolean.FALSE,
                            drawGraph.getLineDrawProp(inheritedFalse).lineAttrs().getConstraint());
    Assertions.assertEquals(Boolean.TRUE,
                            drawGraph.getLineDrawProp(explicitTrue).lineAttrs().getConstraint());
    Assertions.assertTrue(centerY(drawGraph.getNodeDrawProp(b))
                              > centerY(drawGraph.getNodeDrawProp(a)));
    assertRouted(drawGraph, inheritedFalse);
  }

  @Test
  public void repeatedLayoutIsDeterministic() throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Line feedback = Line.builder(c, a).constraint(false).build();
    Graphviz graph = Graphviz.digraph()
        .addLine(a, b).addLine(b, c).addLine(feedback)
        .build();

    DrawGraph first = Layout.DOT.getLayoutEngine().layout(graph);
    DrawGraph second = Layout.DOT.getLayoutEngine().layout(graph);
    Assertions.assertEquals(centerY(first.getNodeDrawProp(a)),
                            centerY(second.getNodeDrawProp(a)), 0.001);
    Assertions.assertEquals(centerY(first.getNodeDrawProp(b)),
                            centerY(second.getNodeDrawProp(b)), 0.001);
    Assertions.assertEquals(centerY(first.getNodeDrawProp(c)),
                            centerY(second.getNodeDrawProp(c)), 0.001);
    Assertions.assertEquals(first.getLineDrawProp(feedback).size(),
                            second.getLineDrawProp(feedback).size());
  }

  @Test
  public void nullConstraintKeepsDefaultRankBehavior() throws Exception {
    Node a = node("a");
    Node b = node("b");
    Line line = Line.builder(a, b).build();
    DrawGraph drawGraph = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(line).build());

    Assertions.assertNull(line.lineAttrs().getConstraint());
    Assertions.assertTrue(centerY(drawGraph.getNodeDrawProp(b))
                              > centerY(drawGraph.getNodeDrawProp(a)));
  }

  private void assertFeedbackEdge(Layout layout, Splines splines) throws Exception {
    Node a = node("a");
    Node b = node("b");
    Node c = node("c");
    Line ab = Line.builder(a, b).build();
    Line bc = Line.builder(b, c).build();
    Line feedback = Line.builder(c, a).constraint(false).label("feedback").build();
    Graphviz graph = Graphviz.digraph()
        .layout(layout)
        .splines(splines)
        .addLine(ab).addLine(bc).addLine(feedback)
        .build();

    DrawGraph drawGraph = layout.getLayoutEngine().layout(graph);
    assertDescending(drawGraph, a, b, c);
    assertRouted(drawGraph, feedback);
    LineDrawProp feedbackProp = drawGraph.getLineDrawProp(feedback);
    Assertions.assertNotNull(feedbackProp.getArrowHead());
    Assertions.assertNotNull(feedbackProp.getLabelCenter());
    assertLogicalDirection(drawGraph, feedbackProp);
    Assertions.assertSame(c, feedback.tail());
    Assertions.assertSame(a, feedback.head());
  }

  private Node node(String id) {
    return Node.builder().id(id).label(id).build();
  }

  private void assertDescending(DrawGraph drawGraph, Node... nodes) {
    double[] y = Arrays.stream(nodes)
        .map(drawGraph::getNodeDrawProp)
        .mapToDouble(this::centerY)
        .toArray();
    for (int i = 1; i < y.length; i++) {
      Assertions.assertTrue(y[i] > y[i - 1], Arrays.toString(y));
    }
  }

  private double centerY(NodeDrawProp prop) {
    return (prop.getUpBorder() + prop.getDownBorder()) / 2;
  }

  private void assertRouted(DrawGraph drawGraph, Line line) {
    LineDrawProp prop = drawGraph.getLineDrawProp(line);
    Assertions.assertNotNull(prop);
    Assertions.assertTrue(prop.size() >= 2, "line has no routed path");
    Assertions.assertNotNull(prop.getStart());
    Assertions.assertNotNull(prop.getEnd());
  }

  private void assertLogicalDirection(DrawGraph drawGraph, LineDrawProp prop) {
    NodeDrawProp head = drawGraph.getNodeDrawProp(prop.getLine().head());
    NodeDrawProp tail = drawGraph.getNodeDrawProp(prop.getLine().tail());
    double headEndpointX = prop.isHeadStart() ? prop.getStart().getX() : prop.getEnd().getX();
    double headEndpointY = prop.isHeadStart() ? prop.getStart().getY() : prop.getEnd().getY();
    double headDistance = distance(headEndpointX, headEndpointY, head.getX(), head.getY());
    double tailDistance = distance(headEndpointX, headEndpointY, tail.getX(), tail.getY());
    Assertions.assertTrue(headDistance < tailDistance,
                          "logical head endpoint is closer to the tail node");
  }

  private double distance(double x1, double y1, double x2, double y2) {
    return Math.hypot(x1 - x2, y1 - y2);
  }
}
