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
import java.util.Map;
import java.util.Objects;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.ext.Box;
import org.graphper.def.DedirectedEdgeGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.Rectangle;
import org.graphper.layout.LayoutGraph;

public class FdpGraph extends LayoutGraph<FNode, FLine> {

  private Map<FNode, Map<FNode, FLine>> adjRecord;

  public FdpGraph(int capacity, Graphviz graphviz,
                  Map<Node, FNode> nodeMap) {
    super(capacity, graphviz, nodeMap);
  }

  @Override
  protected AreaGraph newGraph(int capacity) {
    return new AreaGraph(capacity, this);
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

    FLine line = lineBetweenTwoNodes(from, to);
    if (line != null) {
      line = lineBetweenTwoNodes(to, from);
    }

    if (line != null) {
      line.addParallelEdge(edge);
      return;
    }

    super.addEdge(edge);
    recordAdj(edge);
  }

  public Integer maxMinLen(FNode n, FNode w) {
    if (n.isVirtual() || w.isVirtual() || adjRecord == null) {
      return null;
    }
    FLine line = lineBetweenTwoNodes(n, w);
    if (line == null) {
      line = lineBetweenTwoNodes(w, n);
    }
    if (line == null) {
      return null;
    }
    if (!line.isParallelMerge()) {
      return line.lineAttrs().getMinlen();
    }

    Integer maxMinLen = null;
    for (int i = 0; i < line.getParallelNums(); i++) {
      FLine l = line.parallelLine(i);
      Integer minlen = l.lineAttrs().getMinlen();
      if (minlen == null) {
        continue;
      }

      if (maxMinLen == null) {
        maxMinLen = minlen;
      } else {
        maxMinLen = Math.max(maxMinLen,  minlen);
      }
    }

    return maxMinLen;
  }

  public boolean adjAlreadyExists(FNode n, FNode w) {
    return lineBetweenTwoNodes(n, w) != null;
  }

  public void recordAdj(FLine fLine) {
    adjRecord().computeIfAbsent(fLine.from(), k -> new HashMap<>()).put(fLine.to(), fLine);
    adjRecord().computeIfAbsent(fLine.to(), k -> new HashMap<>()).put(fLine.from(), fLine);
  }

  public Iterable<FLine> outAdjacent(Object n) {
    return ((EdgeDedigraph<FNode, FLine>) graph).outAdjacent(n);
  }

  private Map<FNode, Map<FNode, FLine>> adjRecord() {
    if (adjRecord == null) {
      adjRecord = new HashMap<>();
    }
    return adjRecord;
  }

  private FLine lineBetweenTwoNodes(FNode n, FNode w) {
    Map<FNode, FLine> adj = adjRecord().get(n);
    if (adj == null) {
      return null;
    }
    return adj.get(w);
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

    private FdpGraph fdpGraph;

    private final Rectangle area;

    public AreaGraph(int capacity) {
      this(capacity, null);
    }

    public AreaGraph(int capacity, FdpGraph fdpGraph) {
      super(capacity);
      this.area = new Rectangle();
      this.fdpGraph = fdpGraph;
    }

    public FdpGraph getFdpGraph() {
      return fdpGraph;
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
      double w2 = node.wd2();
      double h2 = node.ht2();
      area.updateXAxisRange(node.getX() - w2);
      area.updateXAxisRange(node.getX() + w2);
      area.updateYAxisRange(node.getY() - h2);
      area.updateYAxisRange(node.getY() + h2);
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
