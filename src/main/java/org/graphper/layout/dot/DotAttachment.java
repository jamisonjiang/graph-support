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
import java.util.List;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.def.BiConcatIterable;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

class DotAttachment {

  private final DotDigraph dotDigraph;

  private final Map<Node, DNode> nodeRecord;

  private final DrawGraph drawGraph;

  private boolean haveClusters;

  private boolean haveSubgraphs;

  private List<DLine> labelLines;

  private DotLineClip lineClip;

  private SameRankAdjacentRecord sameRankAdjacentRecord;

  public DotAttachment(DotDigraph dotDigraph, DrawGraph drawGraph, Map<Node, DNode> nodeRecord) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.dotDigraph = dotDigraph;
    this.drawGraph = drawGraph;
    this.nodeRecord = nodeRecord;
  }

  void initLineClip() {
    lineClip = new DotLineClip(drawGraph, dotDigraph);
  }

  void clipAllLines() {
    if (lineClip != null) {
      lineClip.clipAllLines();
    }
  }

  Iterable<DNode> nodes(GraphContainer graphContainer) {
    return dotDigraph.nodes(graphContainer);
  }

  Iterable<Line> lines(GraphContainer graphContainer) {
    return dotDigraph.lines(graphContainer);
  }

  DotDigraph getDotDigraph() {
    return dotDigraph;
  }

  DrawGraph getDrawGraph() {
    return drawGraph;
  }

  Graphviz getGraphviz() {
    return drawGraph.getGraphviz();
  }

  DNode get(Node node) {
    return nodeRecord.get(node);
  }

  public SameRankAdjacentRecord getSameRankAdjacentRecord() {
    return sameRankAdjacentRecord;
  }

  public void setSameRankAdjacentRecord(
      SameRankAdjacentRecord sameRankAdjacentRecord) {
    this.sameRankAdjacentRecord = sameRankAdjacentRecord;
  }

  DNode mappingToDNode(Node node) {
    return new DNode(
        node,
        drawGraph.width(node),
        drawGraph.height(node),
        drawGraph.getGraphviz().graphAttrs().getNodeSep()
    );
  }

  List<DLine> getLabelLines() {
    return CollectionUtils.isEmpty(labelLines) ? Collections.emptyList() : labelLines;
  }

  void addNode(DNode node) {
    dotDigraph.add(node);
  }

  void addEdge(DLine line) {
    dotDigraph.addEdge(line);

    if (line.haveLabel()) {
      if (labelLines == null) {
        labelLines = new ArrayList<>(2);
      }

      labelLines.add(line);
    }
  }

  void put(Node node, DNode dNode) {
    nodeRecord.put(node, dNode);
  }

  void markHaveCluster() {
    this.haveClusters = true;
  }

  void markHaveSubgraph() {
    this.haveSubgraphs = true;
  }

  GraphContainer breakAncestryContinuesContainer(GraphContainer graphContainer) {
    if (graphContainer == null) {
      return null;
    }

    GraphContainer container = graphContainer;
    while (container != null && !container.isSubgraph() && !container.isTransparent()) {
      container = getGraphviz().father(graphContainer);
    }

    return container;
  }

  boolean haveClusters() {
    return haveClusters;
  }

  boolean haveSubgraphs() {
    return haveSubgraphs;
  }

  boolean notContain(GraphContainer father, GraphContainer container) {
    return notContain(drawGraph.getGraphviz(), father, container);
  }

  GraphContainer commonParent(DNode v, DNode w) {
    return commonParent(getGraphviz(), v, w);
  }

  GraphContainer clusterDirectContainer(GraphContainer parent, DNode node) {
    if (node.getContainer() == parent) {
      return null;
    }

    Graphviz graphviz = getGraphviz();
    GraphContainer father;
    GraphContainer current = node.getContainer();
    while ((father = graphviz.effectiveFather(current)) != parent && father != null) {
      current = father;
    }

    return father == parent ? current : null;
  }

  static Iterable<Cluster> clusters(GraphContainer container) {
    List<Iterable<Cluster>> iterables = null;

    for (Subgraph subgraph : container.subgraphs()) {
      if (!subgraph.isTransparent()) {
        continue;
      }

      Iterable<Cluster> clusters = clusters(subgraph);
      if (clusters == null) {
        continue;
      }
      if (iterables == null) {
        iterables = new ArrayList<>(2);
      }
      iterables.add(clusters);
    }

    if (iterables == null) {
      return container.clusters();
    }

    iterables.add(container.clusters());
    return new BiConcatIterable<>(iterables);
  }

  static boolean notContain(Graphviz graphviz, GraphContainer father, GraphContainer container) {
    if (father == null || container == null) {
      return true;
    }

    GraphContainer p = container;
    while (p != father && p != null) {
      p = graphviz.father(p);
    }
    return p == null;
  }


  /**
   * Finds up to the first common parent container containing two nodes within the specified root
   * container.
   *
   * @param graphviz root container
   * @param n        node
   * @param w        node
   * @return The first common parent container of both nodes
   */
  static GraphContainer commonParent(Graphviz graphviz, DNode n, DNode w) {
    GraphContainer c1 = n.getContainer();
    GraphContainer c2 = w.getContainer();

    return commonParent(graphviz, c1, c2);
  }

  /**
   * Look up the first common parent container containing two containers in the specified root
   * container.
   *
   * @param graphviz root container
   * @param c1       container one
   * @param c2       container two
   * @return The first common parent container of both nodes
   */
  static GraphContainer commonParent(Graphviz graphviz,
                                     GraphContainer c1,
                                     GraphContainer c2) {
    if (c1 == c2) {
      return c1;
    }

    if (graphviz.effectiveFather(c1) == c2) {
      return c2;
    }

    if (graphviz.effectiveFather(c2) == c1) {
      return c1;
    }

    GraphContainer t;
    GraphContainer tn = c1;
    GraphContainer tw = c2;
    Map<GraphContainer, GraphContainer> path = new HashMap<>(4);
    while (c1 != null || c2 != null) {
      if (c1 != null) {
        t = path.get(c1);
        if (t != null && t == tw) {
          return c1;
        }

        path.put(c1, tn);
        c1 = graphviz.effectiveFather(c1);
      }

      if (c2 != null) {
        t = path.get(c2);
        if (t != null && t == tn) {
          return c2;
        }

        path.put(c2, tw);
        c2 = graphviz.effectiveFather(c2);
      }
    }

    return c1;
  }

  class DotLineClip extends LineClip {

    DotLineClip(DrawGraph drawGraph, DotDigraph dotDigraph) {
      this.drawGraph = drawGraph;
      this.dotDigraph = dotDigraph;
    }

    private void clipAllLines() {
      drawGraph.syncGraphvizBorder();

      for (LineDrawProp line : drawGraph.lines()) {
        PathClip<LineDrawProp> pathClip;
        if (line.isBesselCurve()) {
          pathClip = CurvePathClip.INSTANCE;
        } else {
          pathClip = LineDrawPropPathClip.INSTANCE;
        }

        if (line.isSelfLoop() && CollectionUtils.isNotEmpty(line)) {
          FlatPoint noPathDirection = line.get(line.size() / 2);
          clipProcess(line, pathClip, noPathDirection, line);
        } else {
          clipProcess(line, pathClip, null, line);
        }
        if (CollectionUtils.isEmpty(line)) {
          continue;
        }

        line.setStart(line.get(0));
        line.setEnd(line.get(line.size() - 1));
        setFloatLabel(line);
      }

      drawGraph.syncToGraphvizBorder();
    }
  }
}
