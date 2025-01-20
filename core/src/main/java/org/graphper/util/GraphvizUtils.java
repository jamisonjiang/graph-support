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

package org.graphper.util;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.graphper.def.CycleDependencyException;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Subgraph;

/**
 * Graphviz container tool class.
 *
 * @author Jamison Jiang
 */
public class GraphvizUtils {

  private GraphvizUtils() {
  }

  public static void dfs(int maxDepth, boolean preConsumer,
                         GraphContainer container,
                         Consumer<Subgraph> subgraphConsumer,
                         Consumer<Cluster> clusterConsumer,
                         Predicate<GraphContainer> continueDfsPredicate) {
    dfs(maxDepth, preConsumer, null, null, container,
        subgraphConsumer, clusterConsumer, continueDfsPredicate);
  }


  public static void dfs(int maxDepth, boolean preConsumer,
                         Set<GraphContainer> path,
                         Set<GraphContainer> accessStack,
                         GraphContainer container,
                         Consumer<Subgraph> subgraphConsumer,
                         Consumer<Cluster> clusterConsumer,
                         Predicate<GraphContainer> continueDfsPredicate) {
    Asserts.nullArgument(container, "container");

    dfs(preConsumer, 1, maxDepth,
        path, accessStack, container,
        (c, f) -> subgraphConsumer.accept(c),
        (c, f) -> clusterConsumer.accept(c),
        continueDfsPredicate);
  }


  public static void dfs(int maxDepth, boolean preConsumer,
                         Set<GraphContainer> path,
                         Set<GraphContainer> accessStack,
                         GraphContainer container,
                         BiConsumer<Subgraph, GraphContainer> subgraphConsumer,
                         BiConsumer<Cluster, GraphContainer> clusterConsumer,
                         Predicate<GraphContainer> continueDfsPredicate) {
    Asserts.nullArgument(container, "container");

    dfs(preConsumer, 1, maxDepth, path, accessStack, container,
        subgraphConsumer, clusterConsumer, continueDfsPredicate);
  }

  private static void dfs(boolean preConsumer,
                          int depth, int maxDepth,
                          Set<GraphContainer> path,
                          Set<GraphContainer> accessStack,
                          GraphContainer container,
                          BiConsumer<Subgraph, GraphContainer> subgraphConsumer,
                          BiConsumer<Cluster, GraphContainer> clusterConsumer,
                          Predicate<GraphContainer> continueDfsPredicate) {
    Asserts.illegalArgument(
        depth > maxDepth,
        "The depth of the subgraph exceeds the maximum depth " + maxDepth
    );
    if (accessStack != null) {
      if (accessStack.contains(container)) {
        throw new CycleDependencyException(
            "Find that the subgraphs or clusters have circular dependencies!");
      }
      accessStack.add(container);
    }

    if (path != null) {
      if (path.contains(container)) {
        return;
      }

      path.add(container);
    }

    // Subgraphs dfs
    for (Subgraph subgraph : container.subgraphs()) {
      consumerContainer(preConsumer, depth, maxDepth, path,
                        accessStack, subgraphConsumer, clusterConsumer,
                        continueDfsPredicate, subgraph, container);
    }

    // Clusters dfs
    for (Cluster cluster : container.clusters()) {
      consumerContainer(preConsumer, depth, maxDepth, path,
                        accessStack, subgraphConsumer, clusterConsumer,
                        continueDfsPredicate, cluster, container);
    }

    if (accessStack != null) {
      accessStack.remove(container);
    }
  }

  private static void consumerContainer(boolean preConsumer,
                                        int depth, int maxDepth,
                                        Set<GraphContainer> path,
                                        Set<GraphContainer> accessStack,
                                        BiConsumer<Subgraph, GraphContainer> subgraphConsumer,
                                        BiConsumer<Cluster, GraphContainer> clusterConsumer,
                                        Predicate<GraphContainer> continueDfsPredicate,
                                        GraphContainer container, GraphContainer father) {
    if (preConsumer) {
      consumerContainer(father, container, subgraphConsumer, clusterConsumer);
    }

    // continue dfs ?
    if (continueDfsPredicate == null ||
        Objects.equals(Boolean.TRUE, continueDfsPredicate.test(container))) {
      dfs(preConsumer, depth + 1, maxDepth, path, accessStack, container,
          subgraphConsumer, clusterConsumer, continueDfsPredicate);
    }

    if (!preConsumer) {
      consumerContainer(father, container, subgraphConsumer, clusterConsumer);
    }
  }

  private static void consumerContainer(GraphContainer father, GraphContainer container,
                                        BiConsumer<Subgraph, GraphContainer> subgraphConsumer,
                                        BiConsumer<Cluster, GraphContainer> clusterConsumer) {
    if (container.isSubgraph() && subgraphConsumer != null) {
      subgraphConsumer.accept((Subgraph) container, father);
    } else if (container.isCluster() && clusterConsumer != null) {
      clusterConsumer.accept((Cluster) container, father);
    }
  }
}
