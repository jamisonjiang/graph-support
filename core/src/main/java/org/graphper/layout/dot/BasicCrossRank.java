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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.graphper.api.GraphContainer;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

class BasicCrossRank implements CrossRank, Cloneable {

  private int minRank = Integer.MAX_VALUE;

  private int maxRank = Integer.MIN_VALUE;

  protected Map<Integer, List<DNode>> rankNode;

  protected Map<DNode, Integer> nodeRankIndex;

  protected final GraphContainer container;

  BasicCrossRank(GraphContainer container) {
    this.rankNode = new HashMap<>();
    this.nodeRankIndex = new HashMap<>();
    this.container = container;
  }

  @Override
  public int getRankIndex(DNode node) {
    Integer idx = nodeRankIndex.get(node);
    if (idx == null) {
      throw new NoSuchElementException();
    }

    return idx;
  }

  @Override
  public Integer safeGetRankIndex(DNode node) {
    return nodeRankIndex.get(node);
  }

  @Override
  public DNode getNode(int rank, int rankIdx) {
    List<DNode> nodes = rankNode.get(rank);

    if (CollectionUtils.isEmpty(nodes) || rankIdx < 0 || rankIdx >= nodes.size()) {
      return null;
    }

    return nodes.get(rankIdx);
  }

  @Override
  public void addNode(DNode node) {
    Asserts.nullArgument(node, "node");
    List<DNode> nodes = rankNode.get(node.getRank());
    if (nodes == null) {
      nodes = new ArrayList<>(2);
      rankNode.put(node.getRank(), nodes);
      minRank = Math.min(minRank, node.getRank());
      maxRank = Math.max(maxRank, node.getRank());
    }

    nodeRankIndex.put(node, nodes.size());
    nodes.add(node);
  }

  @Override
  public int rankSize(int rank) {
    List<DNode> nodes = rankNode.get(rank);
    if (CollectionUtils.isEmpty(nodes)) {
      return 0;
    }
    return nodes.size();
  }

  @Override
  public int minRank() {
    return minRank;
  }

  @Override
  public int maxRank() {
    return maxRank;
  }

  @Override
  public void exchange(DNode v, DNode w) {
    Objects.requireNonNull(v);
    Objects.requireNonNull(w);
    if (v.getRank() != w.getRank()) {
      throw new IllegalArgumentException(
          "Inconsistent hierarchy of vertices,only exchange vertices of the same hierarchy");
    }

    if (v == w) {
      return;
    }

    List<DNode> nodes = rankNode.get(v.getRank());

    if (CollectionUtils.isEmpty(nodes)) {
      throw new IndexOutOfBoundsException("rank index out of bounds");
    }

    int vi = getRankIndex(v);
    int wi = getRankIndex(w);
    nodes.set(vi, w);
    nodes.set(wi, v);

    nodeRankIndex.put(v, wi);
    nodeRankIndex.put(w, vi);
    v.setRankIndex(wi);
    w.setRankIndex(vi);
  }

  @Override
  public void sort(Comparator<DNode> comparator, boolean needSyncRankIdx) {
    for (int i = minRank(); i <= maxRank(); i++) {
      sort(i, comparator, needSyncRankIdx);
    }
  }

  @Override
  public void sort(int rank, Comparator<DNode> comparator, boolean needSyncRankIdx) {
    List<DNode> nodes = rankNode.get(rank);

    if (CollectionUtils.isEmpty(nodes)) {
      return;
    }

    nodes.sort(comparator);

    for (int j = 0; j < nodes.size(); j++) {
      DNode node = nodes.get(j);
      nodeRankIndex.put(node, j);
      if (needSyncRankIdx) {
        node.setRankIndex(j);
      }
    }
  }

  @Override
  public GraphContainer container() {
    return container;
  }

  @Override
  protected BasicCrossRank clone() {
    BasicCrossRank basicCrossRank;
    try {
      basicCrossRank = (BasicCrossRank) super.clone();
      basicCrossRank.nodeRankIndex = new HashMap<>(this.nodeRankIndex);
      basicCrossRank.rankNode = new HashMap<>(this.rankNode.size());
      for (Entry<Integer, List<DNode>> entry : this.rankNode.entrySet()) {
        basicCrossRank.rankNode.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    } catch (CloneNotSupportedException e) {
      return new BasicCrossRank(this.container);
    }

    return basicCrossRank;
  }
}
