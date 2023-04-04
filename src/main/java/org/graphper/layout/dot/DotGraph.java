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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.graphper.def.UndirectedEdgeGraph;

class DotGraph extends UndirectedEdgeGraph<DNode, ULine> {

  private static final long serialVersionUID = -288796423920317541L;

  private final Set<ULine> lineRecord;

  private final Set<DNode> nodeRecord;

  public DotGraph() {
    super();
    this.lineRecord = new HashSet<>();
    this.nodeRecord = new HashSet<>();
  }

  public DotGraph(int vertexNum) {
    super(vertexNum);
    this.lineRecord = new HashSet<>();
    this.nodeRecord = new HashSet<>(vertexNum);
  }

  public DotGraph(int vertexNum, int edgeNum) {
    super(vertexNum);
    this.nodeRecord = new HashSet<>(vertexNum);
    this.lineRecord = new HashSet<>(edgeNum);
  }

  @Override
  public boolean add(DNode node) {
    if (super.add(node)) {
      return nodeRecord.add(node);
    }

    return false;
  }

  @Override
  public void addEdge(ULine uLine) {
    Objects.requireNonNull(uLine);
    DLine dLine;
    Objects.requireNonNull(dLine = uLine.getdLine());

    super.addEdge(uLine);
    lineRecord.add(uLine);
    nodeRecord.add(dLine.from());
    nodeRecord.add(dLine.to());
  }

  @Override
  public boolean removeEdge(ULine uLine) {
    return removeLine(uLine);
  }

  @Override
  public boolean remove(Object vertex) {
    if (vertex instanceof DNode) {
     return removeNode((DNode) vertex);
    }

    return false;
  }

  public boolean removeNode(DNode vertex) {
    if (super.remove(vertex)) {
      return nodeRecord.remove(vertex);
    }

    return false;
  }

  public boolean removeLine(ULine uLine) {
    if (super.removeEdge(uLine)) {
      return lineRecord.remove(uLine);
    }

    return false;
  }

  public boolean containEdge(ULine uLine) {
    return lineRecord.contains(uLine);
  }

  public boolean containNode(DNode node) {
    return nodeRecord.contains(node);
  }
}
