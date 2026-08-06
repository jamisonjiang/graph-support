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

package org.graphper.draw;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.attributes.Layout;
import org.graphper.api.attributes.Rankdir;
import org.graphper.def.UnaryConcatIterable;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.FontUtils;

/**
 * A graph object that can be drawn and rendered, after layout, contains all the description
 * properties of {@link Graphviz}, {@link Cluster}, {@link Node}, {@link Line}.
 *
 * @author Jamison Jiang
 */
public class DrawGraph extends Rectangle implements Serializable {

  private static final long serialVersionUID = -571472079515096134L;

  private static final String NODE_ID_PREFIX = "node_";

  private Object attach;

  private final GraphvizDrawProp graphvizDrawProp;

  private final Map<Node, NodeDrawProp> nodeDrawPropMap;

  private final Map<Line, LineDrawProp> lineDrawPropMap;

  private Map<Cluster, ClusterDrawProp> clusterDrawPropMap;

  public DrawGraph(Graphviz graphviz) {
    Asserts.nullArgument(graphviz, "graphviz");
    this.graphvizDrawProp = new GraphvizDrawProp(graphviz);
    this.nodeDrawPropMap = new LinkedHashMap<>();
    this.lineDrawPropMap = new LinkedHashMap<>();
  }

  public void nodePut(Node node, NodeDrawProp nodeDrawProp) {
    if (node == null || nodeDrawProp == null) {
      return;
    }
    nodeDrawPropMap.put(node, nodeDrawProp);
  }

  public void linePut(Line line, LineDrawProp lineDrawProp) {
    if (line == null || lineDrawProp == null || lineDrawPropMap == null) {
      return;
    }

    lineDrawPropMap.put(line, lineDrawProp);
  }

  public void clusterPut(Cluster cluster, ClusterDrawProp clusterDrawProp) {
    if (cluster == null || clusterDrawProp == null) {
      return;
    }

    if (clusterDrawPropMap == null) {
      clusterDrawPropMap = new HashMap<>(graphvizDrawProp.getGraphviz().clusters().size());
    }

    clusterDrawPropMap.put(cluster, clusterDrawProp);
  }

  public boolean ignoreRankdir() {
    Layout layout = getGraphviz().graphAttrs().getLayout();
    return layout != Layout.DOT && layout != Layout.DOTQ;
  }

  public boolean notNeedFlip() {
    return !needFlip();
  }

  public boolean needFlip() {
    if (ignoreRankdir()) {
      return false;
    }
    Rankdir rankdir = getGraphviz().graphAttrs().getRankdir();
    return rankdir != Rankdir.TB && rankdir != Rankdir.BT;
  }

  public Graphviz getGraphviz() {
    return graphvizDrawProp.getGraphviz();
  }

  public Rankdir rankdir() {
    return getGraphviz().graphAttrs().getRankdir();
  }

  public Layout layout() {
    return getGraphviz().graphAttrs().getLayout();
  }

  public GraphvizDrawProp getGraphvizDrawProp() {
    return graphvizDrawProp;
  }

  public Iterable<NodeDrawProp> nodes() {
    return nodes(false);
  }

  @SuppressWarnings("unchecked")
  public Iterable<NodeDrawProp> nodes(boolean filterCell) {
    if (filterCell) {
      return new UnaryConcatIterable<>(NodeDrawProp::isNotCellProp, nodeDrawPropMap.values());
    }
    return nodeDrawPropMap.values();
  }

  public Collection<LineDrawProp> lines() {
    return lineDrawPropMap != null ? lineDrawPropMap.values() : Collections.emptyList();
  }

  public Collection<ClusterDrawProp> clusters() {
    return clusterDrawPropMap != null
        ? clusterDrawPropMap.values()
        : Collections.emptyList();
  }

  public Map<Cluster, ClusterDrawProp> getClusterDrawPropMap() {
    return clusterDrawPropMap;
  }

  public NodeDrawProp getNodeDrawProp(Node node) {
    return nodeDrawPropMap.get(node);
  }

  public LineDrawProp getLineDrawProp(Line line) {
    if (line == null) {
      return null;
    }
    return getLineDrawPropMap().get(line);
  }

  public ClusterDrawProp getClusterDrawProp(Cluster cluster) {
    if (clusterDrawPropMap == null) {
      return null;
    }

    return clusterDrawPropMap.get(cluster);
  }

  public LineAttrs lineAttrs(Line line) {
    Asserts.nullArgument(line, "line");
    LineDrawProp lineDrawProp = getLineDrawPropMap().get(line);

    return lineDrawProp != null ? lineDrawProp.lineAttrs() : line.lineAttrs();
  }

  public Map<Line, LineDrawProp> getLineDrawPropMap() {
    return lineDrawPropMap != null ? lineDrawPropMap : Collections.emptyMap();
  }

  public double width(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);

    return nodeDrawProp != null ? nodeDrawProp.getWidth() : 0;
  }

  public double height(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);

    return nodeDrawProp != null ? nodeDrawProp.getHeight() : 0;
  }

  public boolean haveCluster(Cluster cluster) {
    if (clusterDrawPropMap == null || cluster == null) {
      return false;
    }

    return clusterDrawPropMap.get(cluster) != null;
  }

  public String nodeId(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);
    if (nodeDrawProp == null) {
      return null;
    }

    return nodeDrawProp.nodeAttrs().getId() != null
        ? nodeDrawProp.nodeAttrs().getId()
        : NODE_ID_PREFIX + nodeDrawProp.id();
  }

  public int nodeNo(Node node) {
    NodeDrawProp nodeDrawProp = nodeDrawPropMap.get(node);

    return nodeDrawProp != null ? nodeDrawProp.nodeNo() : 0;
  }

  public int getNodeNum() {
    return nodeDrawPropMap == null ? 0 : nodeDrawPropMap.keySet().size();
  }

  public String lineId(Line line) {
    if (lineDrawPropMap == null) {
      return null;
    }
    LineDrawProp lineDrawProp = lineDrawPropMap.get(line);
    return lineDrawProp != null ? lineDrawProp.id() : null;
  }

  public String clusterId(Cluster cluster) {
    if (clusterDrawPropMap == null) {
      return null;
    }
    ClusterDrawProp clusterDrawProp = clusterDrawPropMap.get(cluster);
    return clusterDrawProp != null ? clusterDrawProp.id() : null;
  }

  public void syncGraphvizBorder() {
    leftBorder = graphvizDrawProp.getLeftBorder();
    rightBorder = graphvizDrawProp.getRightBorder();
    upBorder = graphvizDrawProp.getUpBorder();
    downBorder = graphvizDrawProp.getDownBorder();
  }

  public void syncToGraphvizBorder() {
    graphvizDrawProp.setLeftBorder(leftBorder);
    graphvizDrawProp.setRightBorder(rightBorder);
    graphvizDrawProp.setUpBorder(upBorder);
    graphvizDrawProp.setDownBorder(downBorder);
  }

  /**
   * Recomputes the final graph border from drawable geometry, excluding the routing safety area
   * accumulated while laying out the graph. This is used only for an explicit zero graph margin.
   *
   * @param padding final safety padding around drawable content
   */
  public void tightenGraphBorder(double padding) {
    Rectangle bounds = new Rectangle();
    for (NodeDrawProp node : nodes()) {
      updateBounds(bounds, node);
    }
    for (ClusterDrawProp cluster : clusters()) {
      updateBounds(bounds, cluster);
    }
    for (LineDrawProp line : lines()) {
      for (FlatPoint point : line) {
        updateBounds(bounds, point);
      }
      updateBounds(bounds, line.getStart());
      updateBounds(bounds, line.getEnd());
      updateArrowBounds(bounds, line.getArrowHead());
      updateArrowBounds(bounds, line.getArrowTail());
      updateLabelBounds(bounds, line.getLabelCenter(), line.getLabelSize());
      for (Map.Entry<FloatLabel, FlatPoint> entry
          : line.getFloatLabelFlatCenters().entrySet()) {
        FloatLabel floatLabel = entry.getKey();
        FlatPoint size = FontUtils.measure(floatLabel.getLabel(), floatLabel.getFontName(),
                                           floatLabel.getFontSize(), 0);
        updateLabelBounds(bounds, entry.getValue(), size);
      }
    }
    updateLabelBounds(bounds, graphvizDrawProp.getLabelCenter(), graphvizDrawProp.getLabelSize());

    if (bounds.getLeftBorder() == Double.MAX_VALUE) {
      return;
    }
    leftBorder = bounds.getLeftBorder() - padding;
    rightBorder = bounds.getRightBorder() + padding;
    upBorder = bounds.getUpBorder() - padding;
    downBorder = bounds.getDownBorder() + padding;
    syncToGraphvizBorder();
  }

  private void updateBounds(Rectangle bounds, ContainerDrawProp container) {
    bounds.updateXAxisRange(container.getLeftBorder());
    bounds.updateXAxisRange(container.getRightBorder());
    bounds.updateYAxisRange(container.getUpBorder());
    bounds.updateYAxisRange(container.getDownBorder());
  }

  private void updateBounds(Rectangle bounds, FlatPoint point) {
    if (point == null) {
      return;
    }
    bounds.updateXAxisRange(point.getX());
    bounds.updateYAxisRange(point.getY());
  }

  private void updateLabelBounds(Rectangle bounds, FlatPoint center, FlatPoint size) {
    if (center == null || size == null) {
      return;
    }
    bounds.updateXAxisRange(center.getX() - size.getWidth() / 2);
    bounds.updateXAxisRange(center.getX() + size.getWidth() / 2);
    bounds.updateYAxisRange(center.getY() - size.getHeight() / 2);
    bounds.updateYAxisRange(center.getY() + size.getHeight() / 2);
  }

  private void updateArrowBounds(Rectangle bounds, ArrowDrawProp arrow) {
    if (arrow == null) {
      return;
    }
    FlatPoint begin = arrow.getAxisBegin();
    FlatPoint end = arrow.getAxisEnd();
    double radius = FlatPoint.twoFlatPointDistance(begin, end);
    bounds.updateXAxisRange(Math.min(begin.getX(), end.getX()) - radius);
    bounds.updateXAxisRange(Math.max(begin.getX(), end.getX()) + radius);
    bounds.updateYAxisRange(Math.min(begin.getY(), end.getY()) - radius);
    bounds.updateYAxisRange(Math.max(begin.getY(), end.getY()) + radius);
  }

  public void updateRange(FlatPoint point) {
    if (point == null) {
      return;
    }

    updateXAxisRange(point.getX());
    updateYAxisRange(point.getY());
  }

  public void updateXAxisRange(double x) {
    super.updateXAxisRange(x - 10);
    super.updateXAxisRange(x + 10);
  }

  public void updateYAxisRange(double y) {
    super.updateYAxisRange(y - 10);
    super.updateYAxisRange(y + 10);
  }

  public double getMinX() {
    return leftBorder;
  }

  public double getMaxX() {
    return rightBorder;
  }

  public double getMinY() {
    return upBorder;
  }

  public double getMaxY() {
    return downBorder;
  }

  public double width() {
    return getMaxX() - getMinX();
  }

  public double height() {
    return getMaxY() - getMinY();
  }

  public Object getAttach() {
    return attach;
  }

  public void setAttach(Object attach) {
    this.attach = attach;
  }
}
