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

package org.graphper.layout.fdp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.ext.Box;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.Rectangle;
import org.graphper.layout.LayoutGraph;

public class FdpGraph extends LayoutGraph<FNode, FLine> {

  private Map<FNode, Set<FNode>> adjRecord;

  public FdpGraph(int capacity, Graphviz graphviz,
                  Map<Node, FNode> nodeMap) {
    super(capacity, graphviz, nodeMap);
  }

  @Override
  protected AreaGraph newGraph(int capacity) {
    return new AreaGraph(capacity);
  }

  @Override
  public AreaGraph getGraph() {
    return (AreaGraph) super.getGraph();
  }

  @Override
  public void addEdge(FLine edge) {
    if (Objects.isNull(edge)) {
      return;
    }

    FNode from = edge.from();
    FNode to = edge.to();

    if (adjAlreadyExists(from, to) || adjAlreadyExists(to, from)) {
      return;
    }

    super.addEdge(edge);
    recordAdj(from, to);
  }

  public boolean adjAlreadyExists(FNode from, FNode to) {
    Set<FNode> adj = adjRecord().get(from);
    if (adj == null) {
      return false;
    }
    return adj.contains(to);
  }

  public void recordAdj(FNode from, FNode to) {
    adjRecord().computeIfAbsent(from, k -> new HashSet<>()).add(to);
    adjRecord().computeIfAbsent(to, k -> new HashSet<>()).add(from);
  }

  private Map<FNode, Set<FNode>> adjRecord() {
    if (adjRecord == null) {
      adjRecord = new HashMap<>();
    }
    return adjRecord;
  }

  public Iterable<FLine> outAdjacent(Object n) {
    return ((EdgeDedigraph<FNode, FLine>) graph).outAdjacent(n);
  }

  @Override
  public String toString() {
    Map<String, FlatPoint> map = new HashMap<>();
    nodeMap.forEach((k, v) -> {
      map.put(k.nodeAttrs().getLabel(), new FlatPoint(v.getX(), v.getY()));
    });
    return map.toString();
  }


  public static class AreaGraph extends DedirectedEdgeGraph<FNode, FLine> implements Box {

    private static final long serialVersionUID = -8984880695666572968L;

    private boolean initStatus;

    private final Rectangle area;

    public AreaGraph(int capacity) {
      super(capacity);
      this.area = new Rectangle();
    }

    public void updateXAxisRange(double x) {
      area.updateXAxisRange(x);
    }

    public void updateYAxisRange(double y) {
      area.updateYAxisRange(y);
    }

    public void setNodeLocation(FNode node, double x, double y) {
      initStatus = false;
      node.setLocation(x, y);
      area.updateXAxisRange(node.getLeftBorder());
      area.updateXAxisRange(node.getRightBorder());
      area.updateYAxisRange(node.getUpBorder());
      area.updateYAxisRange(node.getDownBorder());
    }

    public void initArea() {
      area.init();
      initStatus = true;
    }

    public double width() {
      return area.getWidth();
    }

    public double height() {
      return area.getHeight();
    }

    @Override
    public double getLeftBorder() {
      return area.getLeftBorder();
    }

    @Override
    public double getRightBorder() {
      return area.getRightBorder();
    }

    @Override
    public double getUpBorder() {
      return area.getUpBorder();
    }

    @Override
    public double getDownBorder() {
      return area.getDownBorder();
    }

    public boolean isInitStatus() {
      return initStatus;
    }
  }
}
