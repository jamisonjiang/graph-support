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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Rank;
import org.graphper.def.Graph.VertexGraph;
import org.graphper.def.UndirectedGraph;
import org.graphper.draw.DrawGraph;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * When multiple {@link Subgraph}s contain some common nodes, according to different {@link Rank}
 * attributes, sometimes these {@link Subgraph}s need to be regarded as the same {@link Subgraph}.
 * But not all {@link Subgraph}s can be merged, some ranks are mutually exclusive, and
 * {@link SubgrahOppositRankException} will be thrown.
 *
 * @author Jamison Jiang
 */
class SubgraphMerge {

  private final DotAttachment dotAttachment;

  private final GraphContainer container;

  private final List<SubNode> subNodes;

  private Map<DNode, MergeNode> mergeNodeMap;

  private boolean haveBorderNode;

  private static final SubgraphMerge EMPTY_SUBGRAPH_MERGE = new SubgraphMerge(null, null, null);

  private SubgraphMerge(GraphContainer container,
                        List<SubNode> subNodes,
                        DotAttachment dotAttachment) {
    this.container = container;
    this.subNodes = subNodes;
    this.dotAttachment = dotAttachment;

    /*
     * Use undirected connect graph, make sure the subgraphs who have common node use the same SubKey,
     * and let these subgrah's nodes map to the unique merge node.
     */
    subConnect();
  }

  static SubgraphMerge newSubgraphMerge(GraphContainer container,
                                        DotAttachment dotAttachment,
                                        Consumer<GraphContainer> containerConsumer) {
    Asserts.nullArgument(container, "container");
    Asserts.nullArgument(dotAttachment, "dotAttachment");

    // Convert all Subgraphs into SubNodes, and prepare undirected graph for judging connectivity.
    List<SubNode> subNodes = new ArrayList<>(container.subgraphs().size());
    addSubNode(container, subNodes, dotAttachment, containerConsumer);

    if (CollectionUtils.isEmpty(subNodes)) {
      return EMPTY_SUBGRAPH_MERGE;
    }

    return new SubgraphMerge(container, subNodes, dotAttachment);
  }

  // ---------------------------------------- Object method ----------------------------------------

  boolean haveBorderNode() {
    return haveBorderNode;
  }

  MergeNode getMergeNode(DNode node) {
    if (mergeNodeMap == null) {
      return null;
    }

    return mergeNodeMap.get(node);
  }

  Iterable<DNode> nodes() {
    if (mergeNodeMap == null) {
      return Collections.emptyList();
    }

    return mergeNodeMap.keySet();
  }

  boolean isEmpty() {
    return mergeNodeMap == null;
  }

  private static void addSubNode(GraphContainer container,
                                 List<SubNode> subNodes,
                                 DotAttachment dotAttachment,
                                 Consumer<GraphContainer> containerConsumer) {
    if (containerConsumer != null) {
      containerConsumer.accept(container);
    }

    for (Subgraph subgraph : container.subgraphs()) {
      if (subgraph.isTransparent()) {
        addSubNode(subgraph, subNodes, dotAttachment, containerConsumer);
        continue;
      }

      SubNode subNode = new SubNode(subgraph);
      subNodes.add(subNode);

      // add Subgraph node mask
      DrawGraph drawGraph = dotAttachment.getDrawGraph();
      for (Node node : subgraph.nodes()) {
        DNode dNode = dotAttachment.get(node);
        if (dNode.getContainer() != container) {
          continue;
        }

        subNode.addNodeId(drawGraph.nodeNo(node));
      }
    }
  }

  private void subConnect() {
    if (CollectionUtils.isEmpty(subNodes)) {
      return;
    }

    VertexGraph<SubNode> connectGraph = new UndirectedGraph<>(subNodes.size());

    // Construct an undirected graph of connected judgments.
    for (int i = 0; i < subNodes.size(); i++) {
      SubNode s1 = subNodes.get(i);

      for (int j = i + 1; j < subNodes.size(); j++) {
        SubNode s2 = subNodes.get(j);

        // There is no connection between two Subgraphs, no edges
        if (!s1.haveCommonNode(s2)) {
          continue;
        }

        connectGraph.addEdge(s1, s2);
      }
    }

    // Merge subgraphs using connected components, use depth first search
    Set<SubNode> mark = new HashSet<>(subNodes.size());
    for (SubNode subNode : subNodes) {
      if (mark.contains(subNode)) {
        continue;
      }

      dfs(newSubNodeKey(subNode, dotAttachment), subNode, mark, connectGraph);
    }

    // Merge node by SubKey
    for (SubNode subNode : subNodes) {
      for (Node node : subNode.subgraph.nodes()) {
        DNode dn = dotAttachment.get(node);
        if (dn.getContainer() != container) {
          continue;
        }

        if (mergeNodeMap == null) {
          mergeNodeMap = new HashMap<>();
        }

        // All merged subgraphs use the same merge node.
        MergeNode mergeNode = subNode.subKey.key;
        mergeNodeMap.put(dn, subNode.subKey.key);
        if (mergeNode.isBorder()) {
          this.haveBorderNode = true;
        }
      }
    }
  }

  /*
   * Nodes in the same connected component will have the same key (SubKey).
   */
  private void dfs(SubKey subKey, SubNode subNode, Set<SubNode> mark,
                   VertexGraph<SubNode> connectGraph) {
    mark.add(subNode);
    subKey.key.rank = compareRankKey(subKey.key.rank, subNode.subgraph.getRank());
    subNode.subKey = subKey;

    for (SubNode node : connectGraph.adjacent(subNode)) {
      if (mark.contains(node)) {
        continue;
      }

      dfs(subKey, node, mark, connectGraph);
    }
  }

  private SubKey newSubNodeKey(SubNode s1, DotAttachment dotAttachment) {
    Graphviz graphviz = dotAttachment.getGraphviz();
    GraphContainer parent = graphviz.effectiveFather(s1.subgraph);
    Node node = findFirst(s1.subgraph.nodes(), parent, dotAttachment);
    MergeNode mergeNode = new MergeNode(dotAttachment.get(node), s1.subgraph.getRank());

    return new SubKey(mergeNode);
  }

  private Node findFirst(Iterable<Node> nodes, GraphContainer parent, DotAttachment dotAttachment) {
    for (Node node : nodes) {
      DNode dNode = dotAttachment.get(node);
      if (dNode.getContainer() != parent) {
        continue;
      }
      return node;
    }

    return null;
  }

  /*
   * When different ranks conflict, some ranks can be converted to higher priority, while others
   * conflict with each other. For example: when same and min conflict, the nodes of the two
   * subgraphs all become min. However, when min and max conflict, some policy adjustments cannot
   * be merged, and an exception will be thrown.
   */
  private Rank compareRankKey(Rank r1, Rank r2) {
    if (r1 == r2) {
      return r1;
    }

    if (r1 == Rank.SAME) {
      return r2;
    }

    if (r2 == Rank.SAME) {
      return r1;
    }

    if (r1 == Rank.MIN || r1 == Rank.SOURCE) {
      if (r2 == Rank.MAX || r2 == Rank.SINK) {
        throw new SubgrahOppositRankException();
      }

      return Rank.SOURCE;
    }

    if (r2 == Rank.MIN || r2 == Rank.SOURCE) {
      throw new SubgrahOppositRankException();
    }

    return Rank.SINK;
  }

  // ---------------------------------------- static class ----------------------------------------

  /**
   * The node of Subgraph is used to judge the connection of subgraph.
   */
  private static class SubNode {

    private static final long serialVersionUID = 6756911716157476290L;

    private SubKey subKey;

    /*
     * Record the node id, which is used to quickly determine whether two subgraphs are connected.
     */
    private long[] bitNodeIds;

    private final Subgraph subgraph;

    private SubNode(Subgraph subgraph) {
      Asserts.nullArgument(subgraph, "subgraph");
      this.subgraph = subgraph;
      this.bitNodeIds = new long[((subgraph.nodeNum() - 1) / Long.SIZE) + 1];
    }

    // Mark the node id
    private void addNodeId(int nodeId) {
      int segment = (nodeId - 1) / Long.SIZE;
      if (segment > bitNodeIds.length - 1) {
        bitNodeIds = Arrays.copyOf(bitNodeIds, segment + 1);
      }

      bitNodeIds[bitNodeIds.length - segment - 1] |= (1L << (nodeId - (Long.SIZE * segment) - 1));
    }

    // Return true if two subgraphs have the common node
    boolean haveCommonNode(SubNode subNode) {
      if (subNode.bitNodeIds == null || bitNodeIds == null) {
        return false;
      }

      for (int i = bitNodeIds.length - 1, j = subNode.bitNodeIds.length - 1; i >= 0 && j >= 0;
          i--, j--) {
        if ((bitNodeIds[i] & subNode.bitNodeIds[j]) > 0) {
          return true;
        }
      }

      return false;
    }
  }

  private static class SubKey {

    private final MergeNode key;

    public SubKey(MergeNode key) {
      this.key = key;
    }
  }

  static class MergeNode {

    private final DNode node;

    private Rank rank;

    MergeNode(DNode node, Rank rank) {
      this.node = node;
      this.rank = rank;
    }

    DNode getNode() {
      return node;
    }

    Rank getRank() {
      return rank;
    }

    boolean shouldNoInDegree() {
      return rank == Rank.MIN || rank == Rank.SOURCE;
    }

    boolean shouldNoOutDegree() {
      return rank == Rank.MAX || rank == Rank.SINK;
    }

    boolean isBorder() {
      return rank == Rank.MIN || rank == Rank.SOURCE || rank == Rank.MAX || rank == Rank.SINK;
    }
  }
}
