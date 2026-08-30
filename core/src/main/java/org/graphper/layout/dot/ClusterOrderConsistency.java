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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.api.Cluster;
import org.graphper.api.GraphContainer;

/**
 * Checks that sibling containers keep a stable left to right order across the ranks they share.
 *
 * <p>Two sibling clusters may occupy the same ranks, but only side by side: on every rank they both
 * span, one of them has to stay entirely on the same side of the other. If the order flips between
 * two ranks their rectangles have to overlap, which no later stage can repair. Being collapsed to a
 * single representative per rank does not prevent that flip, so the property has to be verified
 * directly rather than inferred from the rank layout.
 *
 * @author Jamison Jiang
 */
class ClusterOrderConsistency {

  private ClusterOrderConsistency() {
  }

  /**
   * Returns the sibling pairs whose left to right order flips between two shared ranks.
   *
   * @param crossRank ordering to inspect
   * @param attachment used to resolve the direct child container of a node
   * @return description of every inconsistent pair, empty when the ordering is safe
   */
  static List<String> violations(CrossRank crossRank, DotAttachment attachment) {
    GraphContainer parent = crossRank.container();
    // container -> rank -> index span occupied on that rank
    Map<GraphContainer, Map<Integer, int[]>> spans = new HashMap<>();

    for (int rank = crossRank.minRank(); rank <= crossRank.maxRank(); rank++) {
      for (int i = 0; i < crossRank.rankSize(rank); i++) {
        DNode node = crossRank.getNode(rank, i);
        GraphContainer direct = attachment.clusterDirectContainer(parent, node);
        if (direct == null || !direct.isCluster()) {
          continue;
        }
        int index = i;
        int[] span = spans.computeIfAbsent(direct, c -> new HashMap<>())
            .computeIfAbsent(rank, r -> new int[]{index, index});
        span[0] = Math.min(span[0], index);
        span[1] = Math.max(span[1], index);
      }
    }

    List<String> violations = new ArrayList<>();
    List<GraphContainer> containers = new ArrayList<>(spans.keySet());
    for (int a = 0; a < containers.size(); a++) {
      for (int b = a + 1; b < containers.size(); b++) {
        GraphContainer left = containers.get(a);
        GraphContainer right = containers.get(b);
        String flip = firstFlip(left, right, spans.get(left), spans.get(right));
        if (flip != null) {
          violations.add(flip);
        }
      }
    }
    return violations;
  }

  /**
   * Returns clusters whose members are separated by a node outside that cluster on the same rank.
   */
  static List<String> contiguityViolations(CrossRank crossRank, DotAttachment attachment) {
    List<String> violations = new ArrayList<>();
    collectContiguityViolations(crossRank.container(), crossRank, attachment, violations);
    return violations;
  }

  private static void collectContiguityViolations(GraphContainer parent, CrossRank crossRank,
                                                  DotAttachment attachment,
                                                  List<String> violations) {
    for (Cluster cluster : attachment.clusters(parent)) {
      for (int rank = crossRank.minRank(); rank <= crossRank.maxRank(); rank++) {
        int first = -1;
        int last = -1;
        for (int i = 0; i < crossRank.rankSize(rank); i++) {
          DNode node = crossRank.getNode(rank, i);
          if (!DotAttachment.notContains(attachment.getGraphviz(), cluster,
                                         node.getContainer())) {
            if (first < 0) {
              first = i;
            }
            last = i;
          }
        }

        for (int i = first + 1; i < last; i++) {
          DNode node = crossRank.getNode(rank, i);
          if (DotAttachment.notContains(attachment.getGraphviz(), cluster,
                                        node.getContainer())) {
            violations.add(cluster.id() + " split on rank " + rank + " by " + node
                               + " at index " + i + " inside [" + first + ".." + last + "]");
            break;
          }
        }
      }
      collectContiguityViolations(cluster, crossRank, attachment, violations);
    }
  }

  private static String firstFlip(GraphContainer left, GraphContainer right,
                                  Map<Integer, int[]> leftSpans, Map<Integer, int[]> rightSpans) {
    int expected = 0;
    Integer expectedRank = null;

    for (Map.Entry<Integer, int[]> entry : leftSpans.entrySet()) {
      int[] rightSpan = rightSpans.get(entry.getKey());
      if (rightSpan == null) {
        continue;
      }

      int[] leftSpan = entry.getValue();
      int sign;
      if (leftSpan[1] < rightSpan[0]) {
        sign = -1;
      } else if (rightSpan[1] < leftSpan[0]) {
        sign = 1;
      } else {
        return left.id() + " and " + right.id() + " interleave on rank " + entry.getKey()
            + ": " + left.id() + "[" + leftSpan[0] + ".." + leftSpan[1] + "] "
            + right.id() + "[" + rightSpan[0] + ".." + rightSpan[1] + "]";
      }

      if (expectedRank == null) {
        expected = sign;
        expectedRank = entry.getKey();
      } else if (sign != expected) {
        return left.id() + " and " + right.id() + " flip order between rank " + expectedRank
            + " and rank " + entry.getKey();
      }
    }
    return null;
  }
}
