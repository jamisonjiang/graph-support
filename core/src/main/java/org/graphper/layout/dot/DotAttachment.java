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
import java.util.Objects;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.attributes.Port;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.Rectangle;
import org.graphper.layout.Cell;
import org.graphper.layout.Cell.RootCell;
import org.graphper.layout.LayoutAttach;
import org.graphper.layout.LayoutGraph;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.ValueUtils;

class DotAttachment extends LayoutAttach {

  private final DotDigraph dotDigraph;

  private final Map<Node, DNode> nodeRecord;

  private boolean haveSubgraphs;

  private List<DLine> labelLines;

  private GeneratePort generatePort;

  private SameRankAdjacentRecord sameRankAdjacentRecord;

  public DotAttachment(DotDigraph dotDigraph, DrawGraph drawGraph, Map<Node, DNode> nodeRecord) {
    super(drawGraph);
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.dotDigraph = dotDigraph;
    this.nodeRecord = nodeRecord;
  }

  @Override
  public LayoutGraph getLayoutGraph() {
    return dotDigraph;
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

  DNode get(Node node) {
    return nodeRecord.get(node);
  }

  public SameRankAdjacentRecord getSameRankAdjacentRecord() {
    return sameRankAdjacentRecord;
  }

  public void releaseSameRankAdj() {
    this.sameRankAdjacentRecord = null;
  }

  public void setSameRankAdjacentRecord(
      SameRankAdjacentRecord sameRankAdjacentRecord) {
    this.sameRankAdjacentRecord = sameRankAdjacentRecord;
  }

  DNode mappingToDNode(NodeDrawProp node) {
    return new DNode(
        node,
        drawGraph.width(node.getNode()),
        drawGraph.height(node.getNode()),
        drawGraph.getGraphviz().graphAttrs().getNodeSep()
    );
  }

  List<DLine> getLabelLines() {
    return CollectionUtils.isEmpty(labelLines) ? Collections.emptyList() : labelLines;
  }

  void releaseLabelLines() {
    this.labelLines = null;
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

  void markHaveSubgraph() {
    this.haveSubgraphs = true;
  }

  boolean haveSubgraphs() {
    return haveSubgraphs;
  }

  GeneratePort getGeneratePort() {
    return generatePort;
  }

  boolean notContains(GraphContainer father, GraphContainer container) {
    return notContains(drawGraph.getGraphviz(), father, container);
  }

  GraphContainer commonParent(DNode v, DNode w) {
    return commonParent(getGraphviz(), v, w);
  }

  void addGeneratePort(DLine line) {
    if (line == null || line.isVirtual()) {
      return;
    }
    Line edge = line.getLine();
    Node tail = edge.tail();
    Node head = edge.head();
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(edge);
    Asserts.nullArgument(lineDrawProp, "Can not found line prop when generate line port");
    LineAttrs lineAttrs = lineDrawProp.lineAttrs();

    GeneratePortLine generatePortLine = null;
    Node node = getNode(line.from(), tail, head);
    if (lineNodeNeedGeneratePort(node, tail, head, lineAttrs)) {
      generatePortLine = new GeneratePortLine(lineDrawProp);
      setCell(node, tail, head, lineAttrs, generatePortLine, true);
    }

    node = getNode(line.to(), tail, head);
    if (lineNodeNeedGeneratePort(node, tail, head, lineAttrs)) {
      if (generatePortLine == null) {
        generatePortLine = new GeneratePortLine(lineDrawProp);
      }

      generatePortLine.to = line.to();
      setCell(node, tail, head, lineAttrs, generatePortLine, false);
    }

    if (generatePortLine != null) {
      generatePortLine.from = line.from();
      generatePortLine.to = line.to();
      generatePort().addLine(generatePortLine);
    }
  }

  Iterable<Cluster> clusters(GraphContainer container) {
    return dotDigraph.clusters(container);
  }

  static boolean notContains(Graphviz graphviz, GraphContainer father, GraphContainer container) {
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

  // ----------------------------- private method -----------------------------
  private GeneratePort generatePort() {
    if (generatePort == null) {
      generatePort = new GeneratePort();
    }
    return generatePort;
  }

  private Node getNode(DNode node, Node tail, Node head) {
    if (node.getNode() == tail) {
      return tail;
    }
    if (node.getNode() == head) {
      return head;
    }
    return null;
  }

  private boolean lineNodeNeedGeneratePort(Node node, Node tail, Node head, LineAttrs lineAttrs) {
    if (node == null) {
      return false;
    }
    return !lineHaveNodePort(node, tail, head, lineAttrs)
        && lineHaveNodeCell(node, tail, head, lineAttrs);
  }

  private boolean lineHaveNodePort(Node node, Node tail, Node head, LineAttrs lineAttrs) {
    if (node == tail) {
      return lineAttrs.getTailPort() != null;
    }
    if (node == head) {
      return lineAttrs.getHeadPort() != null;
    }
    return false;
  }

  private boolean lineHaveNodeCell(Node node, Node tail, Node head, LineAttrs lineAttrs) {
    if (node == tail) {
      return lineAttrs.getTailCell() != null;
    }
    if (node == head) {
      return lineAttrs.getHeadCell() != null;
    }
    return false;
  }

  private void setCell(Node node, Node tail, Node head, LineAttrs lineAttrs,
                       GeneratePortLine generatePortLine, boolean isFrom) {
    Cell cell = null;
    NodeDrawProp nodeDrawProp;
    if (Objects.equals(tail, node)) {
      nodeDrawProp = drawGraph.getNodeDrawProp(tail);
      RootCell root = nodeDrawProp.getCell();
      if (root != null) {
        cell = root.getCellById(lineAttrs.getTailCell());
      }
    } else {
      nodeDrawProp = drawGraph.getNodeDrawProp(head);
      RootCell root = nodeDrawProp.getCell();
      if (root != null) {
        cell = root.getCellById(lineAttrs.getHeadCell());
      }
    }

    if (cell == null) {
      return;
    }

    generatePort().addCellOpenPorts(nodeDrawProp, cell);
    if (isFrom) {
      generatePortLine.fromCell = cell;
    } else {
      generatePortLine.toCell = cell;
    }
  }

  // ----------------------------- private method -----------------------------

  static class GeneratePort {

    private List<GeneratePortLine> lines;

    private Map<Cell, List<Port>> cellOpenPort;

    List<GeneratePortLine> getLines() {
      if (lines == null) {
        return Collections.emptyList();
      }
      return lines;
    }

    List<Port> getCellOpenBox(Cell cell) {
      if (cellOpenPort == null) {
        return null;
      }
      return cellOpenPort.getOrDefault(cell, Collections.emptyList());
    }

    private void addLine(GeneratePortLine line) {
      if (lines == null) {
        lines = new ArrayList<>();
      }
      lines.add(line);
    }

    private void addCellOpenPorts(NodeDrawProp node, Cell cell) {
      if (cellOpenPort == null) {
        cellOpenPort = new HashMap<>();
      }
      List<Port> ports = cellOpenPort.get(cell);
      if (ports != null) {
        return;
      }

      Rectangle cellBox = cell.getCellBox(node);
      if (ValueUtils.approximate(cellBox.getLeftBorder(), node.getLeftBorder())) {
        ports = new ArrayList<>(2);
        ports.add(Port.WEST);
      }
      if (ValueUtils.approximate(cellBox.getRightBorder(), node.getRightBorder())) {
        if (ports == null) {
          ports = new ArrayList<>(2);
        }
        ports.add(Port.EAST);
      }
      if (ValueUtils.approximate(cellBox.getUpBorder(), node.getUpBorder())) {
        if (ports == null) {
          ports = new ArrayList<>(2);
        }
        ports.add(Port.NORTH);
      }
      if (ValueUtils.approximate(cellBox.getDownBorder(), node.getDownBorder(), 0.01)) {
        if (ports == null) {
          ports = new ArrayList<>(2);
        }
        ports.add(Port.SOUTH);
      }

      if (ports == null) {
        return;
      }
      cellOpenPort.put(cell, ports);
    }
  }

  static class GeneratePortLine {
    private DNode from;
    private DNode to;
    private Cell fromCell;
    private Cell toCell;
    private final LineDrawProp line;

    public GeneratePortLine(LineDrawProp line) {
      Asserts.nullArgument(line, "Line prop");
      this.line = line;
    }

    public LineAttrs getLineAttrs() {
      return line.lineAttrs();
    }

    public LineDrawProp getLine() {
      return line;
    }

    public DNode getFrom() {
      return from;
    }

    public DNode getTo() {
      return to;
    }

    public Cell getFromCell() {
      return fromCell;
    }

    public Cell getToCell() {
      return toCell;
    }
  }
}

