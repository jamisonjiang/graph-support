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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.CollectionUtils;

class ClusterOrder {

  private final Map<Cluster, Set<Cluster>> reachable = new HashMap<>();

  ClusterOrder(RankContent rankContent, Graphviz graphviz) {
    Objects.requireNonNull(rankContent);
    init(rankContent, graphviz);
  }

  int compare(GraphContainer left, GraphContainer right) {
    if (left == null || right == null || !left.isCluster() || !right.isCluster() || left == right) {
      return 0;
    }
    Cluster lc = (Cluster) left;
    Cluster rc = (Cluster) right;

    if (havePath(lc, rc)) {
      return -1;
    }
    if (havePath(rc, lc)) {
      return 1;
    }
    return 0;
  }

  private boolean havePath(Cluster source, Cluster target) {
    Set<Cluster> clusters = reachable.get(source);
    if (CollectionUtils.isEmpty(clusters)) {
      return false;
    }
    return clusters.contains(target);
  }

  private void init(RankContent rankContent, Graphviz graphviz) {
    OrderGraph graph = new OrderGraph();

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      Map<GraphContainer, Integer> lastIndex = new HashMap<>();
      RankNode rankNode = rankContent.get(i);
      Set<GraphContainer> leftContainers = lastIndex.keySet();

      for (int j = 0; j < rankNode.size(); j++) {
        DNode node = rankNode.get(j);
        GraphContainer container = node.getContainer();

        // All first discover container in current rank should detect all left containers
        while (container != null && container.isCluster()) {
          if (leftContainers.contains(container)) {
            break;
          }

          for (GraphContainer leftContainer : leftContainers) {
            if (leftContainer.containsContainer(container)) {
              continue;
            }

            // Add reach path
            graph.addEdge((Cluster) leftContainer, (Cluster) container);
          }
          container = graphviz.effectiveFather(container);
        }

        container = node.getContainer();
        while (container != null && container.isCluster()) {
          lastIndex.put(container, i);
          container = graphviz.effectiveFather(container);
        }
      }
    }

    Set<Cluster> mark = new HashSet<>();
    for (Cluster cluster : graph.bags.keySet()) {
      mark.clear();

      dfs(cluster, graph, mark);
      reachable.put(cluster, new HashSet<>(mark));
    }
  }

  private void dfs(Cluster cluster, OrderGraph graph, Set<Cluster> mark) {
    if (mark.contains(cluster)) {
      return;
    }

    mark.add(cluster);
    for (Cluster c : graph.adj(cluster)) {
      dfs(c, graph, mark);
    }
  }

  private static class OrderGraph {

    private final Map<Cluster, Set<Cluster>> bags = new HashMap<>();

    void addEdge(Cluster n, Cluster w) {
      bags.computeIfAbsent(n, k -> new HashSet<>()).add(w);
    }

    Set<Cluster> adj(Cluster c) {
      Set<Cluster> adj = bags.get(c);
      return CollectionUtils.isEmpty(adj) ? Collections.emptySet() : adj;
    }
  }
}
