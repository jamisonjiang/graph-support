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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.attributes.Tend;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.util.FontUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExternalLabelPlacerTest {

  @Test
  public void endpointLabelsAvoidEachOtherAndNodesAcrossLayouts() throws Exception {
    for (Layout layout : Layout.values()) {
      assertEndpointLabelsAvoidEachOtherAndNodes(layout, Rankdir.LR);
    }
    for (Layout layout : new Layout[]{Layout.DOT, Layout.DOTQ}) {
      for (Rankdir rankdir : Rankdir.values()) {
        assertEndpointLabelsAvoidEachOtherAndNodes(layout, rankdir);
      }
    }
  }

  private void assertEndpointLabelsAvoidEachOtherAndNodes(Layout layout, Rankdir rankdir)
      throws Exception {
      Node tail = Node.builder().id("tail_" + layout).label("tail").build();
      Node head = Node.builder().id("head_" + layout).label("head").build();
      Line first = endpointLabeledLine(tail, head, "first head label", "first tail label");
      Line second = endpointLabeledLine(tail, head, "second head label", "second tail label");
      Graphviz graph = Graphviz.digraph().layout(layout).rankdir(rankdir)
          .addLine(first).addLine(second).build();

      DrawGraph draw = layout.getLayoutEngine().layout(graph);
      List<LabelBox> labels = new ArrayList<>();
      collectLabels(draw.getLineDrawProp(first), labels);
      collectLabels(draw.getLineDrawProp(second), labels);
      assertLabelsOutsideOwnerPath(draw.getLineDrawProp(first));
      assertLabelsOutsideOwnerPath(draw.getLineDrawProp(second));

      Assertions.assertEquals(4, labels.size(), "missing endpoint label for " + layout + rankdir);
      for (int i = 0; i < labels.size(); i++) {
        Assertions.assertTrue(labels.get(i).inside(draw),
                              layout + "/" + rankdir + " endpoint label outside graph bounds");
        for (int j = i + 1; j < labels.size(); j++) {
          Assertions.assertFalse(labels.get(i).overlaps(labels.get(j)),
                                 layout + "/" + rankdir + " endpoint labels overlap: " + i + "/" + j);
        }
        for (Node node : new Node[]{tail, head}) {
          LabelBox nodeBox = LabelBox.of(draw.getNodeDrawProp(node));
          Assertions.assertFalse(labels.get(i).overlaps(nodeBox),
                                 layout + "/" + rankdir + " endpoint label overlaps "
                                     + node.nodeAttrs().getId() + ": " + labels.get(i)
                                     + " vs " + nodeBox);
        }
      }
  }

  @Test
  public void alongEdgeFloatLabelKeepsItsPreferredPosition() throws Exception {
    Node tail = Node.builder().id("tail").build();
    Node head = Node.builder().id("head").build();
    FloatLabel label = FloatLabel.builder().label("middle").lengthRatio(0.5).build();
    Line line = Line.builder(tail, head).floatLabels(label).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().rankdir(Rankdir.LR).addLine(line).build());
    LineDrawProp prop = draw.getLineDrawProp(line);

    FlatPoint expected = LineHelper.curveGetFloatLabelStart(null, 0.5, prop);
    if (!prop.isBesselCurve()) {
      expected = LineHelper.straightGetFloatLabelStart(null, 0.5, prop);
    }
    FlatPoint actual = prop.getFloatLabelFlatCenters().get(label);
    Assertions.assertNotNull(actual);
    Assertions.assertEquals(expected.getX(), actual.getX(), 1e-6);
    Assertions.assertEquals(expected.getY(), actual.getY(), 1e-6);
  }

  @Test
  public void unblockedEndpointLabelKeepsItsPreferredPosition() throws Exception {
    Node tail = Node.builder().id("single_tail").build();
    Node head = Node.builder().id("single_head").build();
    FloatLabel label = FloatLabel.builder().label("head label").tend(Tend.HEAD)
        .offset(-3, -3).build();
    Line line = Line.builder(tail, head).floatLabels(label).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().rankdir(Rankdir.LR).addLine(line).build());
    LineDrawProp prop = draw.getLineDrawProp(line);
    FlatPoint endpoint = prop.isHeadStart() ? prop.get(0) : prop.get(prop.size() - 1);
    FlatPoint size = FontUtils.measure(label.getLabel(), label.getFontName(), label.getFontSize(), 0);
    FlatPoint actual = prop.getFloatLabelFlatCenters().get(label);

    Assertions.assertEquals(endpoint.getX() - size.getWidth() * 3.5, actual.getX(), 1e-6);
    Assertions.assertEquals(endpoint.getY() - size.getHeight() * 3.5, actual.getY(), 1e-6);
  }

  @Test
  public void placementIsDeterministic() throws Exception {
    List<FlatPoint> first = endpointCenters(Layout.DOT);
    List<FlatPoint> second = endpointCenters(Layout.DOT);

    Assertions.assertEquals(first, second);
  }

  @Test
  public void reversedPathUsesSemanticHeadAndTailAnchors() throws Exception {
    Node a = Node.builder().id("cycle_a").build();
    Node b = Node.builder().id("cycle_b").build();
    Node c = Node.builder().id("cycle_c").build();
    Line ab = endpointLabeledLine(a, b, "ab head", "ab tail");
    Line bc = endpointLabeledLine(b, c, "bc head", "bc tail");
    Line ca = endpointLabeledLine(c, a, "ca head", "ca tail");
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().addLine(ab).addLine(bc).addLine(ca).build());
    boolean foundReversed = false;
    for (Line line : new Line[]{ab, bc, ca}) {
      LineDrawProp prop = draw.getLineDrawProp(line);
      if (!prop.isHeadStart()) {
        continue;
      }
      foundReversed = true;
      assertLabelsNearSemanticEndpoint(draw, line, Layout.DOT, Rankdir.TB);
    }
    Assertions.assertTrue(foundReversed, "cycle did not produce a reversed stored path");
  }

  @Test
  public void endpointLabelAvoidsFixedAlongEdgeFloatLabel() throws Exception {
    Node tail = Node.builder().id("tail_obstacle").build();
    Node head = Node.builder().id("head_obstacle").build();
    FloatLabel middle = FloatLabel.builder().label("wide middle label").lengthRatio(0.85).build();
    FloatLabel endpoint = FloatLabel.builder().label("head label").tend(Tend.HEAD).build();
    Line line = Line.builder(tail, head).floatLabels(middle, endpoint).build();
    DrawGraph draw = Layout.DOT.getLayoutEngine().layout(
        Graphviz.digraph().rankdir(Rankdir.LR).addLine(line).build());
    LineDrawProp prop = draw.getLineDrawProp(line);

    LabelBox middleBox = labelBox(prop, middle);
    LabelBox endpointBox = labelBox(prop, endpoint);
    Assertions.assertFalse(middleBox.overlaps(endpointBox));
  }

  private Line endpointLabeledLine(Node tail, Node head, String headText, String tailText) {
    return Line.builder(tail, head)
        .floatLabels(FloatLabel.builder().label(headText).tend(Tend.HEAD).build(),
                     FloatLabel.builder().label(tailText).tend(Tend.TAIL).build())
        .build();
  }

  private List<FlatPoint> endpointCenters(Layout layout) throws Exception {
    Node tail = Node.builder().id("det_tail").build();
    Node head = Node.builder().id("det_head").build();
    Line first = endpointLabeledLine(tail, head, "first head label", "first tail label");
    Line second = endpointLabeledLine(tail, head, "second head label", "second tail label");
    DrawGraph draw = layout.getLayoutEngine().layout(
        Graphviz.digraph().layout(layout).rankdir(Rankdir.LR)
            .addLine(first).addLine(second).build());
    List<FlatPoint> centers = new ArrayList<>();
    centers.addAll(draw.getLineDrawProp(first).getFloatLabelFlatCenters().values());
    centers.addAll(draw.getLineDrawProp(second).getFloatLabelFlatCenters().values());
    return centers;
  }

  private void collectLabels(LineDrawProp line, List<LabelBox> labels) {
    for (Map.Entry<FloatLabel, FlatPoint> entry : line.getFloatLabelFlatCenters().entrySet()) {
      FloatLabel label = entry.getKey();
      FlatPoint size = FontUtils.measure(label.getLabel(), label.getFontName(),
                                         label.getFontSize(), 0);
      labels.add(LabelBox.of(entry.getValue(), size));
    }
  }

  private void assertLabelsNearSemanticEndpoint(DrawGraph draw, Line line, Layout layout,
                                                Rankdir rankdir) {
    LineDrawProp prop = draw.getLineDrawProp(line);
    NodeDrawProp tail = draw.getNodeDrawProp(line.tail());
    NodeDrawProp head = draw.getNodeDrawProp(line.head());
    for (Map.Entry<FloatLabel, FlatPoint> entry : prop.getFloatLabelFlatCenters().entrySet()) {
      FloatLabel label = entry.getKey();
      if (label.getTend() == null) {
        continue;
      }
      FlatPoint center = entry.getValue();
      double tailDistance = FlatPoint.twoPointDistance(center.getX(), center.getY(),
                                                       tail.getX(), tail.getY());
      double headDistance = FlatPoint.twoPointDistance(center.getX(), center.getY(),
                                                       head.getX(), head.getY());
      if (label.getTend() == Tend.HEAD) {
        Assertions.assertTrue(headDistance < tailDistance,
                              layout + "/" + rankdir
                                  + " head label was placed near the semantic tail");
      } else {
        Assertions.assertTrue(tailDistance < headDistance,
                              layout + "/" + rankdir
                                  + " tail label was placed near the semantic head");
      }
    }
  }

  private LabelBox labelBox(LineDrawProp line, FloatLabel label) {
    FlatPoint center = line.getFloatLabelFlatCenters().get(label);
    Assertions.assertNotNull(center);
    FlatPoint size = FontUtils.measure(label.getLabel(), label.getFontName(),
                                       label.getFontSize(), 0);
    return LabelBox.of(center, size);
  }

  private void assertLabelsOutsideOwnerPath(LineDrawProp line) {
    List<FlatPoint> path = sample(line);
    for (Map.Entry<FloatLabel, FlatPoint> entry : line.getFloatLabelFlatCenters().entrySet()) {
      if (entry.getKey().getTend() == null) {
        continue;
      }
      LabelBox box = labelBox(line, entry.getKey());
      for (int i = 0; i + 1 < path.size(); i++) {
        Assertions.assertFalse(box.intersects(path.get(i), path.get(i + 1)),
                               "endpoint label is pierced by its owner path");
      }
    }
  }

  private List<FlatPoint> sample(LineDrawProp line) {
    List<FlatPoint> path = new ArrayList<>();
    path.add(line.get(0));
    if (!line.isBesselCurve()) {
      path.addAll(line.subList(1, line.size()));
      return path;
    }
    for (int i = 0; i + 3 < line.size(); i += 3) {
      for (int step = 1; step <= 12; step++) {
        path.add(org.graphper.def.Curves.besselEquationCalc(
            step / 12D, line.get(i), line.get(i + 1), line.get(i + 2), line.get(i + 3)));
      }
    }
    return path;
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

    private static LabelBox of(FlatPoint center, FlatPoint size) {
      return new LabelBox(center.getX() - size.getWidth() / 2,
                          center.getX() + size.getWidth() / 2,
                          center.getY() - size.getHeight() / 2,
                          center.getY() + size.getHeight() / 2);
    }

    private static LabelBox of(NodeDrawProp node) {
      return new LabelBox(node.getLeftBorder(), node.getRightBorder(),
                          node.getUpBorder(), node.getDownBorder());
    }

    private boolean overlaps(LabelBox other) {
      return Math.min(right, other.right) - Math.max(left, other.left) > 1e-6
          && Math.min(bottom, other.bottom) - Math.max(top, other.top) > 1e-6;
    }

    private boolean inside(DrawGraph draw) {
      return left >= draw.getLeftBorder() - 1e-6 && right <= draw.getRightBorder() + 1e-6
          && top >= draw.getUpBorder() - 1e-6 && bottom <= draw.getDownBorder() + 1e-6;
    }

    private boolean intersects(FlatPoint from, FlatPoint to) {
      double epsilon = 1e-6;
      double minX = left + epsilon;
      double maxX = right - epsilon;
      double minY = top + epsilon;
      double maxY = bottom - epsilon;
      double dx = to.getX() - from.getX();
      double dy = to.getY() - from.getY();
      double[] p = {-dx, dx, -dy, dy};
      double[] q = {from.getX() - minX, maxX - from.getX(),
          from.getY() - minY, maxY - from.getY()};
      double enter = 0;
      double leave = 1;
      for (int i = 0; i < p.length; i++) {
        if (Math.abs(p[i]) <= epsilon) {
          if (q[i] < 0) {
            return false;
          }
          continue;
        }
        double ratio = q[i] / p[i];
        if (p[i] < 0) {
          enter = Math.max(enter, ratio);
        } else {
          leave = Math.min(leave, ratio);
        }
        if (enter > leave) {
          return false;
        }
      }
      return leave >= 0 && enter <= 1;
    }

    @Override
    public String toString() {
      return "[" + left + ".." + right + "," + top + ".." + bottom + "]";
    }
  }
}
