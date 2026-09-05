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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.def.Curves;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.dot.ClusterObstacleIndex.ClusterObstacle;

/** Restricts a box-guide corridor around cluster borders unrelated to the routed line. */
final class ClusterAwareBoxGuide {

  // Wide enough that a fitted spline overshooting its corridor box still misses the cluster.
  private static final double CLEARANCE = 8;

  private ClusterAwareBoxGuide() {
  }

  static ClusterRoute routeBoxes(Line line, DNode from, DNode to, List<RouterBox> boxes,
                                 boolean horizontal, DrawGraph drawGraph,
                                 ClusterObstacleIndex obstacleIndex, Set<Cluster> ignored) {
    if (line == null || boxes == null || boxes.isEmpty() || drawGraph.clusters().isEmpty()) {
      return new ClusterRoute(boxes, Collections.emptySet(), Collections.emptyMap(),
                              Collections.emptyMap(), Collections.emptySet());
    }

    List<RouterBox> routed = copy(boxes);
    Set<ClusterDrawProp> avoided = new LinkedHashSet<>();
    DNode tail = line.tail() == from.getNode() ? from : to;
    DNode head = tail == from ? to : from;
    Map<Cluster, EndpointRole> roles = clusterRoles(tail, head, drawGraph);
    Map<Integer, Set<Cluster>> restrictions = new HashMap<>();
    Set<ClusterObstacle> candidates = obstacleIndex.query(boxes);
    for (ClusterObstacle obstacle : candidates) {
      ClusterDrawProp cluster = obstacle.drawProp();
      EndpointRole role = roles.get(cluster.getCluster());
      role = role == null ? EndpointRole.UNRELATED : role;
      if (role == EndpointRole.COMMON || ignored.contains(cluster.getCluster())) {
        continue;
      }
      if (role == EndpointRole.SOURCE || role == EndpointRole.TARGET) {
        List<FlatPoint> reference = new ArrayList<>();
        for (RouterBox box : boxes) {
          DNode node = box.getNode();
          if (node != null) {
            reference.add(new FlatPoint(node.getX(), node.getY()));
          }
        }
        // A normal entry/exit needs no detour. Only constrain an endpoint cluster when the
        // existing routing chain leaves and re-enters it, or crosses it between exterior points.
        boolean headStart = boxes.get(0).getNode() == head;
        if (!reference.isEmpty() && validCrossing(reference, cluster, role, headStart)) {
          continue;
        }
      }
      List<Integer> affected = affectedBoxes(routed, cluster, tail, head, role);
      if (affected.isEmpty()) {
        continue;
      }
      boolean preferLow = preferLowSide(routed, affected, cluster, from, to, horizontal);
      boolean first = restrict(routed, affected, cluster, horizontal, preferLow);
      boolean second = !first && restrict(routed, affected, cluster, horizontal, !preferLow);
      if (!first && !second) {
        // A whole-gap detour can disconnect the corridor when an endpoint box is narrow. Restrict
        // only the slice the cluster really occupies so consecutive boxes keep overlapping.
        affected = splitAffected(routed, cluster, tail, head, role, horizontal);
        first = restrict(routed, affected, cluster, horizontal, preferLow);
        second = !first && restrict(routed, affected, cluster, horizontal, !preferLow);
      }
      if (first || second) {
        avoided.add(cluster);
        for (int index : affected) {
          restrictions.computeIfAbsent(index, key -> new LinkedHashSet<>()).add(cluster.getCluster());
        }
      }
    }
    return new ClusterRoute(routed, avoided, roles, restrictions, candidates);
  }

  static Set<Cluster> crossedAvoidedClusters(LineDrawProp route, ClusterRoute clusterRoute) {
    if (route == null || route.isEmpty() || clusterRoute.avoided.isEmpty()) {
      return Collections.emptySet();
    }
    List<FlatPoint> path = sample(route);
    Set<Cluster> crossed = new HashSet<>();
    // Terminal entry is an optional refinement. Do not sacrifice an unrelated obstacle to keep
    // an endpoint-cluster route outside until its final segment.
    for (ClusterObstacle obstacle : clusterRoute.candidates) {
      if (!clusterRoute.roles.containsKey(obstacle.cluster())
          && !doesNotCross(path, obstacle.drawProp())) {
        for (ClusterDrawProp avoided : clusterRoute.avoided) {
          EndpointRole role = clusterRoute.roles.get(avoided.getCluster());
          if (role == EndpointRole.SOURCE || role == EndpointRole.TARGET) {
            crossed.add(avoided.getCluster());
          }
        }
        break;
      }
    }
    for (ClusterDrawProp cluster : clusterRoute.avoided) {
      EndpointRole role = clusterRoute.roles.get(cluster.getCluster());
      if (!validCrossing(path, cluster, role, route.isHeadStart())) {
        crossed.add(cluster.getCluster());
      }
    }
    return crossed;
  }

  static boolean crossesNode(LineDrawProp route, ClusterObstacleIndex index) {
    int stride = route.isBesselCurve() ? 3 : 1;
    for (int i = 0; i + stride < route.size(); i += stride) {
      DefaultBox bounds = bounds(route.subList(i, i + stride + 1));
      for (DNode node : index.nodesIn(bounds)) {
        if (node.getNode() == route.getLine().tail() || node.getNode() == route.getLine().head()) {
          continue;
        }
        if (stride == 1) {
          if (crossesInterior(route.get(i), route.get(i + 1), node)) {
            return true;
          }
        } else if (curveCrossesNode(new ThirdOrderBezierCurve(route.get(i), route.get(i + 1),
                                                               route.get(i + 2), route.get(i + 3)),
                                    node, 0)) {
          return true;
        }
      }
    }
    return false;
  }

  private static DefaultBox bounds(List<FlatPoint> points) {
    double left = Double.POSITIVE_INFINITY;
    double right = Double.NEGATIVE_INFINITY;
    double top = Double.POSITIVE_INFINITY;
    double bottom = Double.NEGATIVE_INFINITY;
    for (FlatPoint point : points) {
      left = Math.min(left, point.getX());
      right = Math.max(right, point.getX());
      top = Math.min(top, point.getY());
      bottom = Math.max(bottom, point.getY());
    }
    return new DefaultBox(left, right, top, bottom);
  }

  private static boolean curveCrossesNode(ThirdOrderBezierCurve curve, DNode node, int depth) {
    DefaultBox hull = bounds(java.util.Arrays.asList(curve.getV1(), curve.getV2(),
                                                    curve.getV3(), curve.getV4()));
    if (hull.getRightBorder() <= node.getLeftBorder() + 1e-6
        || hull.getLeftBorder() >= node.getRightBorder() - 1e-6
        || hull.getDownBorder() <= node.getUpBorder() + 1e-6
        || hull.getUpBorder() >= node.getDownBorder() - 1e-6) {
      return false;
    }
    // Bezier curves stay inside their control hull. Subdivision rejects clear hulls; an unresolved
    // tiny hull is conservatively a collision, never a reason to sacrifice a node for a cluster.
    if (depth >= 12 || Math.max(hull.getWidth(), hull.getHeight()) < 0.1) {
      return true;
    }
    return curveCrossesNode(Curves.divideThirdBesselCurve(0.5, true, curve), node, depth + 1)
        || curveCrossesNode(Curves.divideThirdBesselCurve(0.5, false, curve), node, depth + 1);
  }

  private static List<FlatPoint> sample(LineDrawProp route) {
    List<FlatPoint> path = new ArrayList<>();
    path.add(route.get(0));
    if (!route.isBesselCurve()) {
      path.addAll(route.subList(1, route.size()));
      return path;
    }
    for (int i = 0; i + 3 < route.size(); i += 3) {
      for (int step = 1; step <= 24; step++) {
        path.add(Curves.besselEquationCalc(step / 24D, route.get(i), route.get(i + 1),
                                           route.get(i + 2), route.get(i + 3)));
      }
    }
    return path;
  }

  private static boolean validCrossing(List<FlatPoint> path, ClusterDrawProp cluster,
                                       EndpointRole role, boolean headStart) {
    if (role == null || role == EndpointRole.UNRELATED) {
      return doesNotCross(path, cluster);
    }
    if (role == EndpointRole.COMMON) {
      return true;
    }

    boolean reverse = role == EndpointRole.SOURCE ? headStart : !headStart;
    int first = reverse ? path.size() - 1 : 0;
    FlatPoint previous = path.get(first);
    boolean leftEndpointCluster = !inside(previous, cluster);
    for (int offset = 1; offset < path.size(); offset++) {
      int index = reverse ? path.size() - 1 - offset : offset;
      FlatPoint point = path.get(index);
      boolean pointInside = inside(point, cluster);
      if (leftEndpointCluster) {
        if (pointInside || crossesInterior(previous, point, cluster)) {
          return false;
        }
      } else if (!pointInside) {
        // The route may leave its source cluster, or enter its target cluster when traversed in
        // reverse, exactly once. Once outside, the cluster becomes an ordinary obstacle.
        leftEndpointCluster = true;
      }
      previous = point;
    }
    return true;
  }

  private static boolean doesNotCross(List<FlatPoint> path, ClusterDrawProp cluster) {
    for (int i = 0; i + 1 < path.size(); i++) {
      if (crossesInterior(path.get(i), path.get(i + 1), cluster)) {
        return false;
      }
    }
    return true;
  }

  private static boolean inside(FlatPoint point, ClusterDrawProp cluster) {
    return point.getX() > cluster.getLeftBorder() && point.getX() < cluster.getRightBorder()
        && point.getY() > cluster.getUpBorder() && point.getY() < cluster.getDownBorder();
  }

  private static boolean crossesInterior(FlatPoint from, FlatPoint to,
                                         Box cluster) {
    double epsilon = 1e-6;
    double left = cluster.getLeftBorder() + epsilon;
    double right = cluster.getRightBorder() - epsilon;
    double top = cluster.getUpBorder() + epsilon;
    double bottom = cluster.getDownBorder() - epsilon;
    double dx = to.getX() - from.getX();
    double dy = to.getY() - from.getY();
    double[] p = {-dx, dx, -dy, dy};
    double[] q = {from.getX() - left, right - from.getX(),
        from.getY() - top, bottom - from.getY()};
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

  private static List<RouterBox> copy(List<RouterBox> boxes) {
    List<RouterBox> copy = new ArrayList<>(boxes.size());
    for (RouterBox box : boxes) {
      copy.add(new RouterBox(box.getLeftBorder(), box.getRightBorder(),
                             box.getUpBorder(), box.getDownBorder(), box.getNode()));
    }
    return copy;
  }

  private static Map<Cluster, EndpointRole> clusterRoles(DNode from, DNode to,
                                                         DrawGraph drawGraph) {
    Map<Cluster, EndpointRole> roles = new HashMap<>();
    addAncestors(from.getContainer(), drawGraph.getGraphviz(), roles, EndpointRole.SOURCE);
    addAncestors(to.getContainer(), drawGraph.getGraphviz(), roles, EndpointRole.TARGET);
    // Valid lhead/ltail clusters are already included in the corresponding endpoint ancestor chain.
    return roles;
  }

  private static void addAncestors(GraphContainer container, Graphviz graphviz,
                                   Map<Cluster, EndpointRole> roles, EndpointRole role) {
    GraphContainer current = container;
    while (current != null && current != graphviz) {
      if (current.isCluster()) {
        roles.merge((Cluster) current, role, EndpointRole::merge);
      }
      current = graphviz.effectiveFather(current);
    }
  }

  private static List<Integer> affectedBoxes(List<RouterBox> boxes, ClusterDrawProp cluster,
                                             DNode from, DNode to, EndpointRole role) {
    List<Integer> affected = new ArrayList<>();
    for (int i = 0; i < boxes.size(); i++) {
      RouterBox box = boxes.get(i);
      if ((role == EndpointRole.SOURCE && box.getNode() == from)
          || (role == EndpointRole.TARGET && box.getNode() == to)) {
        continue;
      }
      if (overlap(box.getLeftBorder(), box.getRightBorder(),
                  cluster.getLeftBorder(), cluster.getRightBorder())
          && overlap(box.getUpBorder(), box.getDownBorder(),
                     cluster.getUpBorder(), cluster.getDownBorder())) {
        affected.add(i);
      }
    }
    return affected;
  }

  private static boolean preferLowSide(List<RouterBox> boxes, List<Integer> affected,
                                       ClusterDrawProp cluster, DNode from, DNode to,
                                       boolean horizontal) {
    double reference = 0;
    int count = 0;
    for (Integer index : affected) {
      DNode node = boxes.get(index).getNode();
      if (node == null || inside(node, cluster)) {
        continue;
      }
      reference += horizontal ? node.getY() : node.getX();
      count++;
    }
    if (count == 0) {
      reference = horizontal ? from.getY() + to.getY() : from.getX() + to.getX();
      reference /= 2;
    } else {
      reference /= count;
    }
    double center = horizontal ? cluster.getY() : cluster.getX();
    return reference <= center;
  }

  private static boolean inside(DNode node, ClusterDrawProp cluster) {
    return node.getX() > cluster.getLeftBorder() && node.getX() < cluster.getRightBorder()
        && node.getY() > cluster.getUpBorder() && node.getY() < cluster.getDownBorder();
  }

  private static boolean restrict(List<RouterBox> boxes, List<Integer> affected,
                                  ClusterDrawProp cluster, boolean horizontal, boolean lowSide) {
    if (affected.isEmpty()) {
      return false;
    }
    for (Integer index : affected) {
      RouterBox box = boxes.get(index);
      if (!canRestrict(box, cluster, horizontal, lowSide)) {
        return false;
      }
    }
    double[] snapshot = new double[affected.size()];
    for (int i = 0; i < affected.size(); i++) {
      RouterBox box = boxes.get(affected.get(i));
      if (horizontal) {
        snapshot[i] = lowSide ? box.getDownBorder() : box.getUpBorder();
        if (lowSide) {
          box.setDownBorder(Math.min(box.getDownBorder(), cluster.getUpBorder() - CLEARANCE));
        } else {
          box.setUpBorder(Math.max(box.getUpBorder(), cluster.getDownBorder() + CLEARANCE));
        }
      } else {
        snapshot[i] = lowSide ? box.getRightBorder() : box.getLeftBorder();
        if (lowSide) {
          box.setRightBorder(Math.min(box.getRightBorder(), cluster.getLeftBorder() - CLEARANCE));
        } else {
          box.setLeftBorder(Math.max(box.getLeftBorder(), cluster.getRightBorder() + CLEARANCE));
        }
      }
    }
    if (connected(boxes, horizontal)) {
      return true;
    }
    // A corridor whose consecutive boxes no longer overlap has no continuous route. Forcing it
    // makes the fitted line jump sideways across a rank and slice through a node.
    for (int i = 0; i < affected.size(); i++) {
      RouterBox box = boxes.get(affected.get(i));
      if (horizontal) {
        if (lowSide) {
          box.setDownBorder(snapshot[i]);
        } else {
          box.setUpBorder(snapshot[i]);
        }
      } else if (lowSide) {
        box.setRightBorder(snapshot[i]);
      } else {
        box.setLeftBorder(snapshot[i]);
      }
    }
    return false;
  }

  private static boolean connected(List<RouterBox> boxes, boolean horizontal) {
    for (int i = 0; i + 1 < boxes.size(); i++) {
      RouterBox current = boxes.get(i);
      RouterBox next = boxes.get(i + 1);
      boolean overlap = horizontal
          ? overlap(current.getUpBorder(), current.getDownBorder(),
                    next.getUpBorder(), next.getDownBorder())
          : overlap(current.getLeftBorder(), current.getRightBorder(),
                    next.getLeftBorder(), next.getRightBorder());
      if (!overlap) {
        return false;
      }
    }
    return true;
  }

  private static List<Integer> splitAffected(List<RouterBox> boxes, ClusterDrawProp cluster,
                                             DNode tail, DNode head, EndpointRole role,
                                             boolean horizontal) {
    if (horizontal) {
      splitAt(boxes, cluster.getLeftBorder() - CLEARANCE, true);
      splitAt(boxes, cluster.getRightBorder() + CLEARANCE, true);
    } else {
      splitAt(boxes, cluster.getUpBorder() - CLEARANCE, false);
      splitAt(boxes, cluster.getDownBorder() + CLEARANCE, false);
    }
    return affectedBoxes(boxes, cluster, tail, head, role);
  }

  private static void splitAt(List<RouterBox> boxes, double position, boolean horizontal) {
    for (int i = 0; i < boxes.size(); i++) {
      RouterBox box = boxes.get(i);
      // Endpoint boxes anchor a real node, so they must stay whole.
      if (box.getNode() != null) {
        continue;
      }
      double low = horizontal ? box.getLeftBorder() : box.getUpBorder();
      double high = horizontal ? box.getRightBorder() : box.getDownBorder();
      if (position - low < CLEARANCE || high - position < CLEARANCE) {
        continue;
      }
      RouterBox lead = horizontal
          ? new RouterBox(low, position, box.getUpBorder(), box.getDownBorder(), null)
          : new RouterBox(box.getLeftBorder(), box.getRightBorder(), low, position, null);
      RouterBox trail = horizontal
          ? new RouterBox(position, high, box.getUpBorder(), box.getDownBorder(), null)
          : new RouterBox(box.getLeftBorder(), box.getRightBorder(), position, high, null);
      boxes.set(i, lead);
      boxes.add(i + 1, trail);
      return;
    }
  }

  private static boolean canRestrict(RouterBox box, ClusterDrawProp cluster,
                                     boolean horizontal, boolean lowSide) {
    DNode node = box.getNode();
    if (horizontal) {
      double wall = lowSide ? cluster.getUpBorder() - CLEARANCE
          : cluster.getDownBorder() + CLEARANCE;
      return lowSide ? wall >= box.getUpBorder() && (node == null || node.getY() <= wall)
          : wall <= box.getDownBorder() && (node == null || node.getY() >= wall);
    }
    double wall = lowSide ? cluster.getLeftBorder() - CLEARANCE
        : cluster.getRightBorder() + CLEARANCE;
    return lowSide ? wall >= box.getLeftBorder() && (node == null || node.getX() <= wall)
        : wall <= box.getRightBorder() && (node == null || node.getX() >= wall);
  }

  private static boolean overlap(double min1, double max1, double min2, double max2) {
    return Math.min(max1, max2) - Math.max(min1, min2) > 0;
  }

  static final class ClusterRoute {

    private final List<RouterBox> boxes;

    private final Set<ClusterDrawProp> avoided;

    private final Map<Cluster, EndpointRole> roles;

    private final Map<Integer, Set<Cluster>> restrictions;

    private final Set<ClusterObstacle> candidates;

    private ClusterRoute(List<RouterBox> boxes, Set<ClusterDrawProp> avoided,
                         Map<Cluster, EndpointRole> roles,
                         Map<Integer, Set<Cluster>> restrictions,
                         Set<ClusterObstacle> candidates) {
      this.boxes = boxes;
      this.avoided = avoided;
      this.roles = roles;
      this.restrictions = restrictions;
      this.candidates = candidates;
    }

    List<RouterBox> boxes() {
      return boxes;
    }

    Set<Cluster> avoidedClusters() {
      Set<Cluster> clusters = new LinkedHashSet<>();
      for (ClusterDrawProp cluster : avoided) {
        clusters.add(cluster.getCluster());
      }
      return clusters;
    }

    Set<Cluster> restrictionsAt(int box) {
      return restrictions.getOrDefault(box, Collections.emptySet());
    }
  }

  private enum EndpointRole {
    SOURCE,
    TARGET,
    COMMON,
    UNRELATED;

    private static EndpointRole merge(EndpointRole left, EndpointRole right) {
      return left == right ? left : COMMON;
    }
  }
}
