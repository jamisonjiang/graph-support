/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.layout.dot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.def.EdgeDedigraph;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.dot.RankContent.RankNode;

/**
 * Locally reorders maximal non-branching paths after the regular mincross passes.
 */
class NonBranchingPathSort {

  private static final int MAX_CANDIDATES = 8;

  private static final int MAX_ROUNDS = 3;

  private final RankContent rankContent;
  private final DrawGraph drawGraph;
  private final EdgeDedigraph<DNode, DLine> digraph;

  private final boolean haveClusters;

  NonBranchingPathSort(RankContent rankContent, DrawGraph drawGraph,
                      EdgeDedigraph<DNode, DLine> digraph) {
    this.rankContent = rankContent;
    this.drawGraph = drawGraph;
    this.digraph = digraph;
    this.haveClusters = !drawGraph.clusters().isEmpty();
  }

  void sort() {
    BasicCrossRank current = currentOrder();
    RootCrossRank evaluator = new RootCrossRank(drawGraph, digraph);
    BasicCrossRank original = current;
    for (int round = 0; round < MAX_ROUNDS; round++) {
      int currentCross = evaluator.tryCacheCrossNum(current).getCrossNum();
      if (currentCross == 0) {
        break;
      }
      BasicCrossRank best = current;
      int bestCross = currentCross;
      PriorityQueue<Candidate> candidates = new PriorityQueue<>(
          Comparator.comparingDouble(candidate -> candidate.score));
      for (int rank = current.minRank(); rank <= current.maxRank(); rank++) {
        for (DNode anchor : current.getNodes(rank)) {
          if (anchor.isVirtual() || nonFlatDegree(anchor) <= 2) {
            continue;
          }
          for (boolean topDown : new boolean[]{true, false}) {
            Candidate candidate = candidate(current, anchor, topDown);
            if (candidate == null || !clusterContinuous(candidate.order)) {
              continue;
            }
            candidates.offer(candidate);
            if (candidates.size() > MAX_CANDIDATES) {
              candidates.poll();
            }
          }
        }
      }

      List<Candidate> orderedCandidates = new ArrayList<>(candidates);
      orderedCandidates.sort(Comparator.comparingDouble((Candidate candidate) -> candidate.score)
                                       .reversed());
      for (Candidate candidate : orderedCandidates) {
        int cross = evaluator.tryCacheCrossNum(candidate.order).getCrossNum();
        if (cross < bestCross) {
          best = candidate.order;
          bestCross = cross;
          if (bestCross == 0) {
            break;
          }
        }
      }
      if (best == current) {
        break;
      }
      current = best;
    }

    if (current != original) {
      apply(current);
    }
  }

  private Candidate candidate(BasicCrossRank current, DNode anchor, boolean topDown) {
    Map<Integer, List<DNode>> pathOrder = new LinkedHashMap<>();
    Set<DNode> visitedNodes = Collections.newSetFromMap(new IdentityHashMap<>());
    Set<DLine> visitedEdges = Collections.newSetFromMap(new IdentityHashMap<>());
    for (DLine line : directedLines(anchor, topDown, current)) {
      if (!line.isSameRank() && visitedEdges.add(line)) {
        trace(line.other(anchor), topDown, current, visitedNodes, visitedEdges, pathOrder);
      }
    }
    if (visitedNodes.isEmpty()) {
      return null;
    }
    for (DNode node : visitedNodes) {
      if (hasFlatEdge(node)) {
        return null;
      }
    }

    BasicCrossRank candidate = current.clone();
    for (Map.Entry<Integer, List<DNode>> entry : pathOrder.entrySet()) {
      int rank = entry.getKey();
      List<DNode> original = current.getNodes(rank);
      List<DNode> pathNodes = entry.getValue();
      if (pathNodes.isEmpty()) {
        continue;
      }
      List<DNode> ordered = reorderByType(original, pathNodes, visitedNodes);
      Map<DNode, Integer> order = new IdentityHashMap<>();
      for (int index = 0; index < ordered.size(); index++) {
        order.put(ordered.get(index), index);
      }
      candidate.sort(rank, Comparator.comparingInt(order::get), false);
    }
    double score = localCross(current, candidate, pathOrder.keySet());
    return score <= 0 ? null : new Candidate(candidate, score);
  }

  private List<DNode> reorderByType(List<DNode> original, List<DNode> pathNodes,
                                    Set<DNode> visitedNodes) {
    if (haveClusters) {
      for (DNode node : original) {
        if (!node.isVirtual() && !node.getContainer().isGraphviz()) {
          return new ArrayList<>(original);
        }
      }
    }
    List<DNode> rootReal = new ArrayList<>();
    Map<GraphContainer, List<DNode>> virtual = new IdentityHashMap<>();
    for (DNode node : pathNodes) {
      if (node.isVirtual()) {
        if (!haveClusters) {
          virtual.computeIfAbsent(node.getContainer(), key -> new ArrayList<>()).add(node);
        }
      } else if (node.getContainer().isGraphviz() && nonFlatDegree(node) <= 2) {
        rootReal.add(node);
      }
    }
    for (DNode node : original) {
      if (!visitedNodes.contains(node)) {
        if (node.isVirtual()) {
          if (!haveClusters) {
            virtual.computeIfAbsent(node.getContainer(), key -> new ArrayList<>()).add(node);
          }
        } else if (node.getContainer().isGraphviz() && nonFlatDegree(node) <= 2) {
          rootReal.add(node);
        }
      }
    }
    List<DNode> ordered = new ArrayList<>(original.size());
    int rootRealIndex = 0;
    Map<GraphContainer, Integer> virtualIndex = new IdentityHashMap<>();
    for (DNode node : original) {
      if (node.isVirtual()) {
        if (haveClusters) {
          ordered.add(node);
        } else {
          int index = virtualIndex.getOrDefault(node.getContainer(), 0);
          ordered.add(virtual.get(node.getContainer()).get(index));
          virtualIndex.put(node.getContainer(), index + 1);
        }
      } else if (node.getContainer().isGraphviz() && nonFlatDegree(node) <= 2) {
        ordered.add(rootReal.get(rootRealIndex++));
      } else {
        ordered.add(node);
      }
    }
    return ordered;
  }

  private double localCross(BasicCrossRank current, BasicCrossRank candidate,
                            Set<Integer> affectedRanks) {
    Set<DLine> edges = Collections.newSetFromMap(new IdentityHashMap<>());
    for (int rank : affectedRanks) {
      for (DNode node : current.getNodes(rank)) {
        for (DLine line : digraph.adjacent(node)) {
          if (!line.isSameRank() && Math.abs(line.from().getRank() - line.to().getRank()) == 1) {
            edges.add(line);
          }
        }
      }
    }
    List<DLine> lines = new ArrayList<>(edges);
    return countCross(lines, current) - countCross(lines, candidate);
  }

  private int countCross(List<DLine> lines, BasicCrossRank order) {
    int crossings = 0;
    for (int i = 0; i < lines.size(); i++) {
      DLine first = lines.get(i);
      DNode firstLow = first.from().getRank() < first.to().getRank() ? first.from() : first.to();
      DNode firstHigh = first.other(firstLow);
      for (int j = i + 1; j < lines.size(); j++) {
        DLine second = lines.get(j);
        DNode secondLow = second.from().getRank() < second.to().getRank()
            ? second.from() : second.to();
        DNode secondHigh = second.other(secondLow);
        if (firstLow.getRank() != secondLow.getRank()
            || firstHigh.getRank() != secondHigh.getRank()
            || firstLow == secondLow || firstHigh == secondHigh) {
          continue;
        }
        int lowDiff = order.getRankIndex(firstLow) - order.getRankIndex(secondLow);
        int highDiff = order.getRankIndex(firstHigh) - order.getRankIndex(secondHigh);
        if (lowDiff * highDiff < 0) {
          crossings++;
        }
      }
    }
    return crossings;
  }

  private void trace(DNode node, boolean topDown, BasicCrossRank order, Set<DNode> visitedNodes,
                     Set<DLine> visitedEdges, Map<Integer, List<DNode>> pathOrder) {
    ArrayDeque<DNode> queue = new ArrayDeque<>();
    queue.add(node);
    while (!queue.isEmpty()) {
      DNode current = queue.removeFirst();
      if (nonFlatDegree(current) > 2 || !visitedNodes.add(current)) {
        continue;
      }
      pathOrder.computeIfAbsent(current.getRank(), key -> new ArrayList<>()).add(current);
      for (DLine line : directedLines(current, topDown, order)) {
        if (!line.isSameRank() && visitedEdges.add(line)) {
          queue.addLast(line.other(current));
        }
      }
    }
  }

  private List<DLine> directedLines(DNode node, boolean topDown, BasicCrossRank order) {
    List<DLine> lines = new ArrayList<>();
    Iterable<DLine> adjacent = topDown ? digraph.outAdjacent(node) : digraph.inAdjacent(node);
    for (DLine line : adjacent) {
      if (!line.isSameRank()) {
        lines.add(line);
      }
    }
    lines.sort((left, right) -> {
      DNode leftNode = left.other(node);
      DNode rightNode = right.other(node);
      int rankCompare = Integer.compare(leftNode.getRank(), rightNode.getRank());
      if (!topDown) {
        rankCompare = -rankCompare;
      }
      return rankCompare != 0 ? rankCompare
          : Integer.compare(order.getRankIndex(leftNode), order.getRankIndex(rightNode));
    });
    return lines;
  }

  private int nonFlatDegree(DNode node) {
    int degree = 0;
    for (DLine line : digraph.adjacent(node)) {
      if (!line.isSameRank()) {
        degree++;
      }
    }
    return degree;
  }

  private boolean hasFlatEdge(DNode node) {
    for (DLine line : digraph.adjacent(node)) {
      if (line.isSameRank()) {
        return true;
      }
    }
    return false;
  }

  private BasicCrossRank currentOrder() {
    BasicCrossRank order = new BasicCrossRank(drawGraph.getGraphviz());
    RankNode rank = firstRank();
    while (rank != null) {
      for (DNode node : rank) {
        order.addNode(node);
      }
      rank = rank.next();
    }
    return order;
  }

  private boolean clusterContinuous(BasicCrossRank candidate) {
    Graphviz graphviz = drawGraph.getGraphviz();
    for (int rank = candidate.minRank(); rank <= candidate.maxRank(); rank++) {
      Map<Cluster, int[]> ranges = new LinkedHashMap<>();
      List<DNode> nodes = candidate.getNodes(rank);
      for (int index = 0; index < nodes.size(); index++) {
        DNode node = nodes.get(index);
        if (node.isVirtual()) {
          continue;
        }
        GraphContainer container = node.getContainer();
        while (container != null && !container.isGraphviz()) {
          if (container.isCluster()) {
            int[] range = ranges.get(container);
            if (range == null) {
              range = new int[]{index, index};
              ranges.put((Cluster) container, range);
            }
            range[1] = index;
          }
          container = graphviz.effectiveFather(container);
        }
      }
      for (Map.Entry<Cluster, int[]> entry : ranges.entrySet()) {
        int[] range = entry.getValue();
        for (int index = range[0]; index <= range[1]; index++) {
          DNode node = nodes.get(index);
          if (!node.isVirtual() && DotAttachment.notContains(
              graphviz, entry.getKey(), node.getContainer())) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private void apply(BasicCrossRank best) {
    for (int rank = best.minRank(); rank <= best.maxRank(); rank++) {
      Map<DNode, Integer> order = new IdentityHashMap<>();
      for (int index = 0; index < best.rankSize(rank); index++) {
        order.put(best.getNode(rank, index), index);
      }
      RankNode rankNode = rankContent.get(rank);
      if (rankNode != null) {
        rankNode.sort(Comparator.comparingInt(order::get));
        for (int index = 0; index < rankNode.size(); index++) {
          rankNode.get(index).setRankIndex(index);
        }
      }
    }
  }

  private RankNode firstRank() {
    RankNode rank = rankContent.get(rankContent.minRank());
    while (rank != null && rank.pre() != null) {
      rank = rank.pre();
    }
    return rank;
  }

  private static class Candidate {

    private final BasicCrossRank order;

    private final double score;

    private Candidate(BasicCrossRank order, double score) {
      this.order = order;
      this.score = score;
    }
  }
}
