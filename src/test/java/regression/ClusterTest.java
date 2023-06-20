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

package regression;

import helper.GraphvizVisual;
import org.graphper.api.Cluster;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.Subgraph;
import org.graphper.api.attributes.Rank;
import org.junit.jupiter.api.Test;

public class ClusterTest extends GraphvizVisual {

  @Test
  public void clusterIdTest() {
    Node a = Node.builder().label("a").build();
    Node b = Node.builder().label("b").build();
    Node c = Node.builder().label("c").build();
    Node d = Node.builder().label("d").build();
    Node e = Node.builder().label("e").build();
    Node f = Node.builder().label("f").build();

    Cluster cluster = Cluster.builder()
        .addLine(e, f)
        .build();

    Graphviz graphviz = Graphviz
        .digraph()
        .cluster(
            Cluster.builder()
                .addLine(a, b)
                .build()
        )
        .cluster(
            Cluster.builder()
                .addLine(c, d)
                .build()
        )
        .subgraph(
            Subgraph.builder()
                .subgraph(
                    Subgraph.builder()
                        .rank(Rank.SAME)
                        .cluster(cluster)
                        .build()
                )
                .subgraph(
                    Subgraph.builder()
                        .subgraph(
                            Subgraph.builder()
                                .subgraph(
                                    Subgraph.builder()
                                        .rank(Rank.MIN)
                                        .cluster(Cluster.builder().addNode(d).build())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    System.out.println("--------------- subgraphs ---------------");
    for (Subgraph subgraph : graphviz.subgraphs()) {
      System.out.println("rank=" + subgraph.getRank() + ", node number=" + subgraph.nodeNum());
    }

    System.out.println();

    visual(graphviz);
  }

}
