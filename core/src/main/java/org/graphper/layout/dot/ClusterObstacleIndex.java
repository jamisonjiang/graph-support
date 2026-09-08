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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.ext.Box;
import org.graphper.def.RectangleTree;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawGraph;

/** Rank-local lookup of cluster rectangles used by box-guide line routers. */
final class ClusterObstacleIndex {

  private final Map<Integer, List<ClusterObstacle>> byRank;

  private final double[] rankEnds;

  private final RectangleTree<DNode> nodes = new RectangleTree<>(8);

  ClusterObstacleIndex(RankContent rankContent, DrawGraph drawGraph) {
    if (drawGraph.clusters().isEmpty()) {
      // The node index only serves cluster detours, so a cluster-free graph must not pay for it.
      rankEnds = new double[0];
      byRank = Collections.emptyMap();
      return;
    }

    rankEnds = new double[rankContent.maxRank() - rankContent.minRank() + 1];
    for (int i = 0; i < rankEnds.length; i++) {
      rankEnds[i] = rankContent.get(rankContent.minRank() + i).getEndY();
      for (DNode node : rankContent.get(rankContent.minRank() + i)) {
        if (!node.isVirtual()) {
          nodes.insert(node);
        }
      }
    }

    Graphviz graphviz = drawGraph.getGraphviz();
    List<ClusterObstacle> obstacles = new ArrayList<>(drawGraph.clusters().size());
    for (ClusterDrawProp drawProp : drawGraph.clusters()) {
      // Index the rectangle, not just member ranks: margins and titles can reach into a gap used
      // by a flat route on the neighbouring rank. The first/last buckets also cover exterior space.
      obstacles.add(new ClusterObstacle(drawProp, bucket(drawProp.getUpBorder()),
                                        bucket(drawProp.getDownBorder()),
                                        depth(drawProp.getCluster(), graphviz)));
    }
    obstacles.sort(Comparator.comparingInt(ClusterObstacle::depth)
                       .thenComparingInt(ClusterObstacle::minRank)
                       .thenComparingInt(obstacle -> obstacle.drawProp().getClusterNo()));

    byRank = new HashMap<>();
    for (ClusterObstacle obstacle : obstacles) {
      for (int rank = obstacle.minRank(); rank <= obstacle.maxRank(); rank++) {
        byRank.computeIfAbsent(rank, ignored -> new ArrayList<>()).add(obstacle);
      }
    }
  }

  List<DNode> nodesIn(Box bounds) {
    return nodes.search(bounds);
  }

  Set<ClusterObstacle> query(List<RouterBox> boxes) {
    if (byRank.isEmpty()) {
      return Collections.emptySet();
    }
    Set<ClusterObstacle> obstacles = new LinkedHashSet<>();
    for (RouterBox box : boxes) {
      int max = bucket(box.getDownBorder());
      for (int rank = bucket(box.getUpBorder()); rank <= max; rank++) {
        List<ClusterObstacle> atRank = byRank.get(rank);
        if (atRank != null) {
          for (ClusterObstacle obstacle : atRank) {
            ClusterDrawProp bounds = obstacle.drawProp();
            if (box.getLeftBorder() < bounds.getRightBorder()
                && box.getRightBorder() > bounds.getLeftBorder()
                && box.getUpBorder() < bounds.getDownBorder()
                && box.getDownBorder() > bounds.getUpBorder()) {
              obstacles.add(obstacle);
            }
          }
        }
      }
    }
    return obstacles;
  }

  private int bucket(double y) {
    int low = 0;
    int high = rankEnds.length;
    while (low < high) {
      int mid = (low + high) >>> 1;
      if (rankEnds[mid] < y) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    return low;
  }

  private static int depth(Cluster cluster, Graphviz graphviz) {
    int depth = 0;
    GraphContainer current = cluster;
    while ((current = graphviz.effectiveFather(current)) != null && current != graphviz) {
      if (current.isCluster()) {
        depth++;
      }
    }
    return depth;
  }

  static final class ClusterObstacle {

    private final ClusterDrawProp drawProp;

    private final int minRank;

    private final int maxRank;

    private final int depth;

    private ClusterObstacle(ClusterDrawProp drawProp, int minRank, int maxRank, int depth) {
      this.drawProp = drawProp;
      this.minRank = minRank;
      this.maxRank = maxRank;
      this.depth = depth;
    }

    Cluster cluster() {
      return drawProp.getCluster();
    }

    ClusterDrawProp drawProp() {
      return drawProp;
    }

    int minRank() {
      return minRank;
    }

    int maxRank() {
      return maxRank;
    }

    int depth() {
      return depth;
    }
  }

}
