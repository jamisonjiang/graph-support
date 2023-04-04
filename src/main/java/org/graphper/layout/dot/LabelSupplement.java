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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.util.CollectionUtils;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.layout.dot.SameRankAdjacentRecord.SameRankAdjacentInfo;

/**
 * Handle all kinds of line labels, insert the label as a node into the {@link RankContent}, and
 * then participate in the coordinate allocation, and finally the label falls on the position of the
 * corresponding label node.
 *
 * @author Jamison Jiang
 */
class LabelSupplement {

  private final RankContent rankContent;

  private final DotAttachment dotAttachment;

  private final EdgeDedigraph<DNode, DLine> digraphProxy;

  public LabelSupplement(RankContent rankContent,
                         DotAttachment dotAttachment,
                         EdgeDedigraph<DNode, DLine> digraphProxy) {
    this.rankContent = rankContent;
    this.dotAttachment = dotAttachment;
    this.digraphProxy = digraphProxy;

    // If graph have any label line, may be need create some relay rank.
    insertLabelNodeRank();

    // Flat parallel edge handle
    flatParallelEdge();
  }

  /*
   * Insert the internal rank where the label node is located.
   */
  private void insertLabelNodeRank() {
    if (CollectionUtils.isEmpty(dotAttachment.getLabelLines())) {
      return;
    }

    Set<Integer> needInsertLabelRankIdxs = null;

    for (DLine labelLine : dotAttachment.getLabelLines()) {
      DNode from = labelLine.from();
      DNode to = labelLine.to();

      if (!labelLine.haveLabel() || to.getRank() - from.getRank() != 1) {
        continue;
      }

      if (needInsertLabelRankIdxs == null) {
        needInsertLabelRankIdxs = new HashSet<>();
      }
      needInsertLabelRankIdxs.add(from.getRank());
    }

    if (CollectionUtils.isEmpty(needInsertLabelRankIdxs)) {
      return;
    }

    List<DLine> addLines = null;
    List<DLine> removeLines = null;

    for (Integer rankIdx : needInsertLabelRankIdxs) {
      RankNode rankNode = rankContent.get(rankIdx);
      RankNode labelRankNode = new RankNode(rankNode, rankNode.next(), 0, true);

      if (rankNode.next != null) {
        rankNode.next.pre = labelRankNode;
      }

      rankNode.next = labelRankNode;
      rankNode.rankSep /= 2;
      labelRankNode.rankSep = rankNode.rankSep;

      for (DNode node : rankNode) {
        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (removeLines == null) {
            removeLines = new ArrayList<>();
            addLines = new ArrayList<>();
          }

          recordNewRemoveLines(line, labelRankNode, addLines, removeLines);
        }
      }
    }

    if (CollectionUtils.isEmpty(removeLines)) {
      return;
    }

    for (DLine removeLine : removeLines) {
      digraphProxy.removeEdge(removeLine);
    }

    for (DLine addLine : addLines) {
      digraphProxy.addEdge(addLine);
    }

    rankContent.rankIndexSync();

    RankNode current = rankContent.get(rankContent.minRank());
    while (current != null) {
      if (current.isLabelRank()) {
        current.sort(this::labelNodeComparator);
      }

      for (int j = 0; j < current.size(); j++) {
        DNode node = current.get(j);
        node.setRank(current.rankIndex());
        node.setRankIndex(j);
      }

      current = current.next();
    }
  }

  private void recordNewRemoveLines(DLine line, RankNode rankNode,
                                    List<DLine> addLines, List<DLine> removeLines) {
    removeLines.add(line);

    for (int i = 0; i < line.getParallelNums(); i++) {
      DLine edge = line.parallelLine(i);

      DNode virtual;
      FlatPoint labelSize = edge.getLabelSize();
      if (labelSize == null) {
        virtual = DNode.newVirtualNode(
            20,
            dotAttachment.commonParent(edge.from(), edge.to())
        );
      } else {
        virtual = new DNode(null, labelSize.getWidth(), labelSize.getHeight(),
                            edge.from().getNodeSep(), edge.getLine());
        virtual.setContainer(dotAttachment.commonParent(edge.from(), edge.to()));
      }

      addLines.add(
          new DLine(edge.from(), virtual,
                    edge.getLine(), edge.lineAttrs(),
                    edge.weight(), edge.limit())
      );
      addLines.add(
          new DLine(virtual, edge.to(),
                    edge.getLine(), edge.lineAttrs(),
                    edge.weight(), edge.limit())
      );

      rankNode.add(virtual);
    }
  }

  private int labelNodeComparator(DNode left, DNode right) {
    DNode leftPreNode = null;
    DNode leftNextNode = null;
    DNode rightPreNode = null;
    DNode rightNextNode = null;
    DLine leftLine = null;
    DLine rightLine = null;

    for (DLine line : digraphProxy.inAdjacent(left)) {
      leftPreNode = line.from();
    }

    for (DLine line : digraphProxy.outAdjacent(left)) {
      leftNextNode = line.to();
      leftLine = line;
    }

    for (DLine line : digraphProxy.inAdjacent(right)) {
      rightPreNode = line.from();
    }

    for (DLine line : digraphProxy.outAdjacent(right)) {
      rightNextNode = line.to();
      rightLine = line;
    }

    if (leftPreNode == null || leftNextNode == null
        || rightPreNode == null || rightNextNode == null) {
      return 0;
    }

    int r = Double.compare(leftPreNode.getRankIndex() + leftNextNode.getRankIndex(),
                           rightPreNode.getRankIndex() + rightNextNode.getRankIndex());

    if (r != 0 || left.name() == null || right.name() == null) {
      return r;
    }

    if (leftPreNode == rightPreNode && leftNextNode == rightNextNode) {
      FlatPoint leftPrePoint = getPortPoint(leftLine, leftPreNode);
      FlatPoint leftNextPoint = getPortPoint(leftLine, leftNextNode);
      FlatPoint rightPrePoint = getPortPoint(rightLine, rightPreNode);
      FlatPoint rightNextPoint = getPortPoint(rightLine, rightNextNode);
      r = Double.compare(
          leftPrePoint.getX() + leftNextPoint.getX(),
          rightPrePoint.getX() + rightNextPoint.getX());
      if (r != 0) {
        return r;
      }
    }

    return left.name().compareTo(right.name());
  }

  private void flatParallelEdge() {
    if (dotAttachment.getSameRankAdjacentRecord() == null
        || !dotAttachment.getSameRankAdjacentRecord().haveSameRank()) {
      return;
    }

    SameRankAdjacentRecord sameRankAdjacentRecord = dotAttachment.getSameRankAdjacentRecord();

    /*
     * Unique edge DLine records between vertices of the same rank
     * {
     *    node1 : {
     *        node2 : edge1, // The only edge edge1 between node1 and node2
     *        node3 : edge2, // The only edge edge2 between node1 and node3
     *    }
     * }
     * */
    Map<DNode, Map<DNode, DLine>> parallelEdgeRecord = null;
    Map<DLine, DNode> flatLabelNodeRecord = null;

    for (Entry<DNode, SameRankAdjacentInfo> rankAdjacentInfoEntry :
        sameRankAdjacentRecord.getOutSameRankAdjacent().entrySet()) {
      DNode node = rankAdjacentInfoEntry.getKey();
      // Some information about nodes and edges at the same rank as the node.
      SameRankAdjacentInfo sameRankAdjacentInfo = rankAdjacentInfoEntry.getValue();

      if (CollectionUtils.isEmpty(sameRankAdjacentInfo.lines)) {
        continue;
      }

      // All nodes with the same rank of outgoing edges.
      for (DLine line : sameRankAdjacentInfo.lines) {
        DNode other = line.other(node);

        if (node == other) {
          continue;
        }

        if (parallelEdgeRecord == null) {
          parallelEdgeRecord = new HashMap<>();
        }

        DLine mergeLine = null;
        // Get the records of all outbound nodes corresponding to the node.
        Map<DNode, DLine> linePair = parallelEdgeRecord.get(node);
        if (linePair != null) {
          // Among all outgoing vertices, whether there is already a recorded edge of the
          // current node, if so, this edge will be merged.
          mergeLine = linePair.get(line.other(node));
        }

        if (mergeLine == null) {
          // There is no merged edge, and the current edge is used as the merged edge.
          if (linePair == null) {
            linePair = new HashMap<>(1);
          }
          // Store unique edge records between two points.
          linePair.put(line.other(node), line);
          parallelEdgeRecord.put(node, linePair);
        } else {
          if (line.from() != mergeLine.from()) {
            line = line.reverse();
          }
          mergeLine.addParallelEdge(line);
        }
      }
    }

    if (parallelEdgeRecord == null) {
      return;
    }

    for (Map<DNode, DLine> value : parallelEdgeRecord.values()) {
      for (DLine line : value.values()) {
        // If the merged FlatEdge contains a labelLine, the current mergeLine will become a Label Node.
        if (line.haveLabel()) {
          if (flatLabelNodeRecord == null) {
            flatLabelNodeRecord = new HashMap<>();
          }

          DNode flatLabelNode = flatLabelNodeRecord.computeIfAbsent(
              line,
              ml -> new DNode(
                  null, 0, 0,
                  ml.isSameRankAdj()
                      ? ml.from().getNodeSep() / 2
                      : ml.from().getNodeSep(),
                  ml
              )
          );

          flatLabelNode.setContainer(dotAttachment.commonParent(line.from(), line.to()));

          if (line.isSameRankAdj()) {
            line.from().nodeSepHalving();
          }
        }
      }
    }

    insertFlatLabelNode(flatLabelNodeRecord);
  }

  private void insertFlatLabelNode(Map<DLine, DNode> flatLabelNodeRecord) {
    if (flatLabelNodeRecord == null) {
      return;
    }

    // The rank at which a new virtual node needs to be inserted at the next rank.
    List<RankNode> needInsertVirtualRank = null;
    // The rank index of the newly inserted rank, which is the median value between the original two rank.
    Map<RankNode, Double> newRankNodeIndex = null;
    // The priority queue mapping of the label node that needs to be inserted at the rank.
    Map<RankNode, Queue<DNode>> rankLabelNodeQueue = null;
    double minRankIndex = Double.MAX_VALUE;
    RankNode minRankNode = null;

    for (DNode flatLabelNode : flatLabelNodeRecord.values()) {
      DLine labelLine = flatLabelNode.getFlatLabelLine();
      if (labelLine == null) {
        continue;
      }

      flatLabelNode.setMedian(
          (double) (labelLine.from().getRankIndex() + labelLine.to().getRankIndex()) / 2
      );

      RankNode rankNode = rankContent.get(labelLine.from().getRank());

      if (!labelLine.isSameRankAdj()) {
        // The previous rank is empty, or the previous rank contains normal nodes, and a new rank needs to be inserted.
        if (rankNode.pre() == null || !rankNode.pre().noNormalNode()) {
          if (rankNode.pre() != null) {
            if (needInsertVirtualRank == null) {
              needInsertVirtualRank = new ArrayList<>(1);
            }

            needInsertVirtualRank.add(rankNode.pre());
          }

          double ri = rankNode.pre() != null
              ? (double) (rankNode.pre().rankIndex() + rankNode.rankIndex()) / 2
              : rankNode.rankIndex() - 1;
          rankNode = rankContent.insertLabelRankNode(rankNode.rankIndex());
          if (newRankNodeIndex == null) {
            newRankNodeIndex = new HashMap<>(1);
          }
          newRankNodeIndex.put(rankNode, ri);
        } else {
          rankNode = rankNode.pre();
        }
      }

      if (rankNode == null) {
        continue;
      }

      Double ri;
      if (minRankNode == null
          || (newRankNodeIndex != null && (ri = newRankNodeIndex.get(rankNode)) != null
          && ri < minRankIndex)
          || rankNode.rankIndex() < minRankIndex
      ) {
        minRankNode = rankNode;
        if (newRankNodeIndex != null) {
          ri = newRankNodeIndex.get(rankNode);
          if (ri != null) {
            minRankIndex = ri;
          } else {
            minRankIndex = rankNode.rankIndex();
          }
        } else {
          minRankIndex = rankNode.rankIndex();
        }
      }

      if (rankLabelNodeQueue == null) {
        rankLabelNodeQueue = new HashMap<>(4);
      }
      rankLabelNodeQueue.computeIfAbsent(
          rankNode,
          k -> new PriorityQueue<>(Comparator.comparing(DNode::getMedian))
      ).offer(flatLabelNode);
    }

    rankContent.rankIndexSync();

    // The previous rank of the newly inserted rank, if it exists, a new virtual node needs to be inserted.
    newRankAddVirtualNode(needInsertVirtualRank);

    // Synchronize rank and rankIndex of nodes in rank.
    syncNodeProp(minRankNode, rankLabelNodeQueue);
  }

  private void newRankAddVirtualNode(List<RankNode> needInsertVirtualRank) {
    if (CollectionUtils.isEmpty(needInsertVirtualRank)) {
      return;
    }

    List<DLine> removeLines = new ArrayList<>();
    for (RankNode rankNode : needInsertVirtualRank) {

      for (int i = 0; i < rankNode.size(); i++) {
        DNode node = rankNode.get(i);

        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (line.isSameRank()) {
            continue;
          }
          removeLines.add(line);
        }
      }

      RankNode next = rankNode.next();

      for (DLine removeLine : removeLines) {
        if (removeLine.isParallelMerge()) {
          for (int i = 0; i < removeLine.getParallelNums(); i++) {
            cutLine(next, removeLine.parallelLine(i));
          }
        } else {
          cutLine(next, removeLine);
        }
      }

      removeLines.clear();
    }
  }

  private void syncNodeProp(RankNode rankNode, Map<RankNode, Queue<DNode>> rankLabelNodeQueue) {
    if (rankNode == null || rankLabelNodeQueue == null) {
      return;
    }

    /*
     * According to the two end nodes of the line where the label is located, the order of the
     * label nodes at the current level is determined.
     */
    while (rankNode != null) {
      Queue<DNode> labelNodes = rankLabelNodeQueue.get(rankNode);
      int i = 0;
      for (; i < rankNode.size(); i++) {
        DNode node = rankNode.get(i);
        double median = outMedian(node, rankNode.noNormalNode());

        // Find the first node whose median value is smaller than the current node and insert
        // it in front of the current node
        while (labelNodes != null && !labelNodes.isEmpty()) {
          DNode peek = labelNodes.peek();
          if (peek.getMedian() > median) {
            break;
          }
          labelNodes.poll();
          peek.setRank(rankNode.rankIndex());
          peek.setRankIndex(i);
          rankNode.add(i++, peek);
          digraphProxy.add(peek);

          addArchBridgeFlatLine(peek, rankNode);
        }
        node.setRank(rankNode.rankIndex());
        node.setRankIndex(i);
      }

      while (labelNodes != null && !labelNodes.isEmpty()) {
        DNode poll = labelNodes.poll();
        poll.setRank(rankNode.rankIndex());
        poll.setRankIndex(i++);
        rankNode.add(poll);
        digraphProxy.add(poll);

        addArchBridgeFlatLine(poll, rankNode);
      }

      rankNode = rankNode.next();
    }
  }

  private void cutLine(RankNode next, DLine removeLine) {
    DNode virtual = DNode.newVirtualNode(
        20,
        dotAttachment.commonParent(removeLine.from(), removeLine.to())
    );
    digraphProxy.removeEdge(removeLine);
    digraphProxy.addEdge(
        new DLine(removeLine.from(), virtual,
                  removeLine.getLine(), removeLine.lineAttrs(),
                  removeLine.weight(), removeLine.limit())
    );
    digraphProxy.addEdge(
        new DLine(virtual, removeLine.to(),
                  removeLine.getLine(), removeLine.lineAttrs(),
                  removeLine.weight(), removeLine.limit())
    );
    next.add(virtual);
  }

  private void addArchBridgeFlatLine(DNode flatLabelNode, RankNode rankNode) {
    if (flatLabelNode.getFlatLabelLine() == null) {
      return;
    }

    DLine flatLabelLine = flatLabelNode.getFlatLabelLine();
    DNode next = rankNode.get(flatLabelNode.getRankIndex() + 1);
    if (next != null && (flatLabelLine.from() == next || flatLabelLine.to() == next)) {
      return;
    }

    digraphProxy.addEdge(
        new DLine(flatLabelNode, flatLabelLine.from(),
                  flatLabelLine.getLine(), flatLabelLine.lineAttrs(),
                  flatLabelLine.weight(), flatLabelLine.limit())
    );
    digraphProxy.addEdge(
        new DLine(flatLabelNode, flatLabelLine.to(),
                  flatLabelLine.getLine(), flatLabelLine.lineAttrs(),
                  flatLabelLine.weight(), flatLabelLine.limit())
    );
  }

  private double outMedian(DNode node, boolean isLabelRank) {
    double median = 0;
    if (isLabelRank) {
      for (DLine line : digraphProxy.outAdjacent(node)) {
        median = line.other(node).getRankIndex();
      }
    } else {
      median = node.getRankIndex();
    }

    return median;
  }

  private FlatPoint getPortPoint(DLine line, DNode node) {
    return PortHelper.getPortPoint(line.getLine(), node, dotAttachment.getDrawGraph(), false);
  }
}
