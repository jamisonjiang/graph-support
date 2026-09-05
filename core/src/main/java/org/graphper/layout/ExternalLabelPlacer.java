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

import static org.graphper.layout.AbstractLayoutEngine.setCellNodeOffset;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.Assemble;
import org.graphper.api.FloatLabel;
import org.graphper.api.Node;
import org.graphper.def.Curves;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.util.FontUtils;

/**
 * Places endpoint labels after every line has been routed and clipped.
 *
 * <p>The preferred position still comes from {@link LineClip}; this class only moves a label when
 * that position intersects existing drawable content. Placement is deterministic and bounded. It
 * first keeps the label close to the side of its endpoint node and slides it along that side, then
 * increases the normal distance only when the nearer lane is blocked.</p>
 */
final class ExternalLabelPlacer {

  private static final int NORMAL_LANES = 3;

  private static final int TANGENT_STEPS = 8;

  private static final double MIN_GAP = 4;

  private static final double SIDE_CHANGE_PENALTY = 8;

  private static final double TANGENT_PENALTY = 0.5;

  private static final double INTERIOR_EPSILON = 1e-6;

  private ExternalLabelPlacer() {
  }

  static void place(DrawGraph drawGraph, List<Placement> placements) {
    if (placements == null || placements.isEmpty()) {
      return;
    }

    List<LabelBox> obstacles = obstacles(drawGraph);
    for (Placement placement : placements) {
      FlatPoint center = bestCenter(placement, obstacles, placements);
      if (placement.assemble == null) {
        placement.line.addFloatLabelCenter(placement.label, center);
      } else {
        setCellNodeOffset(drawGraph, center, placement.assemble, true);
      }
      LabelBox box = LabelBox.of(center, placement.size);
      obstacles.add(box);
      drawGraph.updateXAxisRange(box.left);
      drawGraph.updateXAxisRange(box.right);
      drawGraph.updateYAxisRange(box.top);
      drawGraph.updateYAxisRange(box.bottom);
    }
  }

  private static FlatPoint bestCenter(Placement placement, List<LabelBox> obstacles,
                                      List<Placement> placements) {
    List<Candidate> candidates = candidates(placement);
    FlatPoint best = placement.preferred;
    Score bestScore = score(new Candidate(best, 0), placement, obstacles, placements);
    if (bestScore.intersections == 0) {
      return best;
    }

    for (int i = 1; i < candidates.size(); i++) {
      Candidate candidate = candidates.get(i);
      Score candidateScore = score(candidate, placement, obstacles, placements);
      if (candidateScore.betterThan(bestScore)) {
        best = candidate.center;
        bestScore = candidateScore;
      }
    }
    return best;
  }

  private static List<Candidate> candidates(Placement placement) {
    List<Candidate> candidates = new ArrayList<>(
        1 + 4 * NORMAL_LANES * (TANGENT_STEPS * 2 + 1));
    candidates.add(new Candidate(placement.preferred, 0));
    double width = placement.size.getWidth();
    double height = placement.size.getHeight();
    Direction preferred = normalDirection(placement);
    for (Direction normal : directions(preferred)) {
      double normalSize = normal.horizontal ? width : height;
      double tangentSize = normal.horizontal ? height : width;
      double normalStep = Math.max(MIN_GAP, Math.min(12, normalSize / 4));
      double tangentStep = Math.max(MIN_GAP, Math.min(12, tangentSize / 4));
      for (int lane = 0; lane < NORMAL_LANES; lane++) {
        double normalGap = MIN_GAP + lane * normalStep;
        double tangentBase = normal.horizontal == preferred.horizontal ? tangentSize / 2 : 0;
        if (tangentBase == 0) {
          addCandidate(candidates, placement, normal, preferred, normalGap, 0);
        } else {
          addCandidate(candidates, placement, normal, preferred, normalGap, -tangentBase);
          addCandidate(candidates, placement, normal, preferred, normalGap, tangentBase);
        }
        for (int step = 1; step <= TANGENT_STEPS; step++) {
          double tangentOffset = step * tangentStep;
          addCandidate(candidates, placement, normal, preferred, normalGap,
                       -tangentBase - tangentOffset);
          addCandidate(candidates, placement, normal, preferred, normalGap,
                       tangentBase + tangentOffset);
        }
      }
    }
    return candidates;
  }

  private static Direction[] directions(Direction preferred) {
    Direction opposite = new Direction(preferred.horizontal, -preferred.sign);
    Direction before = new Direction(!preferred.horizontal, -1);
    Direction after = new Direction(!preferred.horizontal, 1);
    return new Direction[]{preferred, before, after, opposite};
  }

  private static Direction normalDirection(Placement placement) {
    NodeDrawProp node = placement.endpoint;
    if (node != null) {
      double horizontal = Math.abs(placement.anchor.getX() - node.getX())
          / Math.max(node.getWidth(), 1);
      double vertical = Math.abs(placement.anchor.getY() - node.getY())
          / Math.max(node.getHeight(), 1);
      if (horizontal >= vertical) {
        return new Direction(true, placement.anchor.getX() < node.getX() ? -1 : 1);
      }
      return new Direction(false, placement.anchor.getY() < node.getY() ? -1 : 1);
    }

    double dx = placement.preferred.getX() - placement.anchor.getX();
    double dy = placement.preferred.getY() - placement.anchor.getY();
    return Math.abs(dx) >= Math.abs(dy)
        ? new Direction(true, dx < 0 ? -1 : 1)
        : new Direction(false, dy < 0 ? -1 : 1);
  }

  private static void addCandidate(List<Candidate> candidates, Placement placement,
                                   Direction normal, Direction preferred, double normalGap,
                                   double tangentOffset) {
    FlatPoint anchor = placement.anchor;
    double x;
    double y;
    if (normal.horizontal) {
      x = normal.sign < 0
          ? placement.endpoint.getLeftBorder() - normalGap - placement.size.getWidth() / 2
          : placement.endpoint.getRightBorder() + normalGap + placement.size.getWidth() / 2;
      y = anchor.getY() + tangentOffset;
    } else {
      x = anchor.getX() + tangentOffset;
      y = normal.sign < 0
          ? placement.endpoint.getUpBorder() - normalGap - placement.size.getHeight() / 2
          : placement.endpoint.getDownBorder() + normalGap + placement.size.getHeight() / 2;
    }
    FlatPoint center = new FlatPoint(x, y);
    double association = Math.abs(tangentOffset) * TANGENT_PENALTY;
    if (!normal.same(preferred)) {
      association += SIDE_CHANGE_PENALTY;
    }
    for (Candidate candidate : candidates) {
      FlatPoint point = candidate.center;
      if (point.similarX(center.getX(), 1e-6) && point.similarY(center.getY(), 1e-6)) {
        return;
      }
    }
    candidates.add(new Candidate(center, association));
  }

  private static Score score(Candidate candidate, Placement placement, List<LabelBox> obstacles,
                             List<Placement> placements) {
    FlatPoint center = candidate.center;
    LabelBox candidateBox = LabelBox.of(center, placement.size);
    int intersections = 0;
    double area = 0;
    for (LabelBox obstacle : obstacles) {
      double overlap = candidateBox.overlap(obstacle);
      if (overlap > 0) {
        intersections++;
        area += overlap;
      }
    }
    for (Placement reserved : placements) {
      if (reserved != placement && candidateBox.contains(reserved.anchor)) {
        intersections++;
      }
    }
    if (candidateBox.intersects(placement.ownerPath)) {
      intersections++;
    }
    return new Score(intersections, area,
                     candidate.association + candidateBox.distanceTo(placement.anchor),
                     FlatPoint.twoFlatPointDistance(center, placement.preferred));
  }

  private static List<LabelBox> obstacles(DrawGraph drawGraph) {
    List<LabelBox> obstacles = new ArrayList<>();
    for (NodeDrawProp node : drawGraph.nodes(true)) {
      obstacles.add(LabelBox.of(node));
    }
    for (LineDrawProp line : drawGraph.lines()) {
      addLabel(obstacles, line.getLabelCenter(), line.getLabelSize());
      for (java.util.Map.Entry<FloatLabel, FlatPoint> entry : line
          .getFloatLabelFlatCenters().entrySet()) {
        // Endpoint labels have not been written yet; existing entries are the legacy along-edge
        // labels whose positions remain fixed.
        FloatLabel label = entry.getKey();
        if (label.getTend() != null) {
          continue;
        }
        Assemble assemble = line.getFloatAssemble(label);
        FlatPoint size = assemble != null ? assemble.size()
            : FontUtils.measure(label.getLabel(), line.lineAttrs().getFontName(),
                                label.getFontSize(), 0);
        addLabel(obstacles, entry.getValue(), size);
      }
      FloatLabel[] labels = line.lineAttrs().getFloatLabels();
      if (labels == null) {
        continue;
      }
      for (FloatLabel label : labels) {
        Assemble assemble = line.getFloatAssemble(label);
        if (label.getTend() == null && assemble != null) {
          addAssemble(obstacles, drawGraph, assemble);
        }
      }
    }
    for (ClusterDrawProp cluster : drawGraph.clusters()) {
      addLabel(obstacles, cluster.getLabelCenter(), cluster.getLabelSize());
    }
    GraphvizDrawProp graph = drawGraph.getGraphvizDrawProp();
    addLabel(obstacles, graph.getLabelCenter(), graph.getLabelSize());
    return obstacles;
  }

  private static void addLabel(List<LabelBox> obstacles, FlatPoint center, FlatPoint size) {
    if (center != null && size != null) {
      obstacles.add(LabelBox.of(center, size));
    }
  }

  private static void addAssemble(List<LabelBox> obstacles, DrawGraph drawGraph,
                                  Assemble assemble) {
    LabelBox box = null;
    for (Node cell : assemble.getCells()) {
      NodeDrawProp cellProp = drawGraph.getNodeDrawProp(cell);
      if (cellProp == null) {
        continue;
      }
      LabelBox cellBox = LabelBox.of(cellProp);
      box = box == null ? cellBox : box.union(cellBox);
    }
    if (box != null) {
      obstacles.add(box);
    }
  }

  static final class Placement {

    private final LineDrawProp line;

    private final FloatLabel label;

    private final FlatPoint anchor;

    private final FlatPoint preferred;

    private final FlatPoint size;

    private final Assemble assemble;

    private final NodeDrawProp endpoint;

    private final List<FlatPoint> ownerPath;

    Placement(LineDrawProp line, FloatLabel label, FlatPoint anchor, FlatPoint preferred,
              FlatPoint size, Assemble assemble, NodeDrawProp endpoint) {
      this.line = line;
      this.label = label;
      this.anchor = anchor;
      this.preferred = preferred;
      this.size = size;
      this.assemble = assemble;
      this.endpoint = endpoint;
      this.ownerPath = sample(line);
    }
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

    private static LabelBox of(ContainerDrawProp container) {
      return new LabelBox(container.getLeftBorder(), container.getRightBorder(),
                          container.getUpBorder(), container.getDownBorder());
    }

    private static LabelBox of(FlatPoint center, FlatPoint size) {
      double halfWidth = size.getWidth() / 2;
      double halfHeight = size.getHeight() / 2;
      return new LabelBox(center.getX() - halfWidth, center.getX() + halfWidth,
                          center.getY() - halfHeight, center.getY() + halfHeight);
    }

    private double overlap(LabelBox other) {
      double width = Math.min(right, other.right) - Math.max(left, other.left);
      double height = Math.min(bottom, other.bottom) - Math.max(top, other.top);
      return width > 0 && height > 0 ? width * height : 0;
    }

    private boolean contains(FlatPoint point) {
      return point.getX() > left && point.getX() < right
          && point.getY() > top && point.getY() < bottom;
    }

    private double distanceTo(FlatPoint point) {
      double dx = Math.max(Math.max(left - point.getX(), 0), point.getX() - right);
      double dy = Math.max(Math.max(top - point.getY(), 0), point.getY() - bottom);
      return Math.hypot(dx, dy);
    }

    private LabelBox union(LabelBox other) {
      return new LabelBox(Math.min(left, other.left), Math.max(right, other.right),
                          Math.min(top, other.top), Math.max(bottom, other.bottom));
    }

    private boolean intersects(List<FlatPoint> path) {
      for (int i = 0; i + 1 < path.size(); i++) {
        if (intersects(path.get(i), path.get(i + 1))) {
          return true;
        }
      }
      return false;
    }

    private boolean intersects(FlatPoint from, FlatPoint to) {
      double minX = left + INTERIOR_EPSILON;
      double maxX = right - INTERIOR_EPSILON;
      double minY = top + INTERIOR_EPSILON;
      double maxY = bottom - INTERIOR_EPSILON;
      if (minX >= maxX || minY >= maxY) {
        return false;
      }
      double dx = to.getX() - from.getX();
      double dy = to.getY() - from.getY();
      double[] p = {-dx, dx, -dy, dy};
      double[] q = {from.getX() - minX, maxX - from.getX(),
          from.getY() - minY, maxY - from.getY()};
      double enter = 0;
      double leave = 1;
      for (int i = 0; i < p.length; i++) {
        if (Math.abs(p[i]) <= INTERIOR_EPSILON) {
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
  }

  private static List<FlatPoint> sample(LineDrawProp line) {
    List<FlatPoint> path = new ArrayList<>();
    path.add(line.get(0));
    if (!line.isBesselCurve()) {
      path.addAll(line.subList(1, line.size()));
      return path;
    }
    for (int i = 0; i + 3 < line.size(); i += 3) {
      for (int step = 1; step <= 12; step++) {
        path.add(Curves.besselEquationCalc(step / 12D, line.get(i), line.get(i + 1),
                                           line.get(i + 2), line.get(i + 3)));
      }
    }
    return path;
  }

  private static final class Score {

    private final int intersections;

    private final double area;

    private final double distance;

    private final double preferredDistance;

    private Score(int intersections, double area, double distance, double preferredDistance) {
      this.intersections = intersections;
      this.area = area;
      this.distance = distance;
      this.preferredDistance = preferredDistance;
    }

    private boolean betterThan(Score other) {
      if (intersections != other.intersections) {
        return intersections < other.intersections;
      }
      int areaCompare = Double.compare(area, other.area);
      if (areaCompare != 0) {
        return areaCompare < 0;
      }
      int distanceCompare = Double.compare(distance, other.distance);
      if (distanceCompare != 0) {
        return distanceCompare < 0;
      }
      return preferredDistance < other.preferredDistance;
    }
  }

  private static final class Direction {

    private final boolean horizontal;

    private final int sign;

    private Direction(boolean horizontal, int sign) {
      this.horizontal = horizontal;
      this.sign = sign;
    }

    private boolean same(Direction other) {
      return horizontal == other.horizontal && sign == other.sign;
    }
  }

  private static final class Candidate {

    private final FlatPoint center;

    private final double association;

    private Candidate(FlatPoint center, double association) {
      this.center = center;
      this.association = association;
    }
  }
}
