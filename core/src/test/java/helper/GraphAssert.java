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

package helper;

import org.junit.jupiter.api.Assertions;
import org.graphper.def.BaseEdge;
import org.graphper.def.BaseGraph;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.EdgeOpGraph;
import org.graphper.def.GNode;
import org.graphper.def.VertexDedigraph;
import org.graphper.def.VertexOpGraph;

public class GraphAssert {

  public static void assertAdjEquals(VertexOpGraph<GNode> graph, GNode node, GNode... adjs) {
    int i = 0;
    for (GNode n : graph.adjacent(node)) {
      if (adjs != null) {
        Assertions.assertEquals(n, adjs[i]);
      }
      i++;
    }

    if (adjs == null) {
      Assertions.assertEquals(0, i);
    }
  }

  public static void assertInAdjEquals(VertexDedigraph<GNode> graph, GNode node, GNode... adjs) {
    int i = 0;
    for (GNode n : graph.inAdjacent(node)) {
      if (adjs != null) {
        Assertions.assertEquals(n, adjs[i]);
      }
      i++;
    }

    if (adjs == null) {
      Assertions.assertEquals(0, i);
    }
  }

  public static void assertOutAdjEquals(VertexDedigraph<GNode> graph, GNode node, GNode... adjs) {
    int i = 0;
    for (GNode n : graph.outAdjacent(node)) {
      if (adjs != null) {
        Assertions.assertEquals(n, adjs[i]);
      }
      i++;
    }

    if (adjs == null) {
      Assertions.assertEquals(0, i);
    }
  }

  public static void assertAdjEquals(EdgeOpGraph<GNode, ? extends BaseEdge> graph,
                                     GNode node, GNode... adjs) {
    int i = 0;
    for (BaseEdge e : graph.adjacent(node)) {
      if (adjs != null) {
        Assertions.assertEquals(e.other(node), adjs[i]);
      }
      i++;
    }

    if (adjs == null) {
      Assertions.assertEquals(0, i);
    }
  }

  public static void assertInAdjEquals(EdgeDedigraph<GNode, ? extends BaseEdge> graph,
                                       GNode node, GNode... adjs) {
    int i = 0;
    for (BaseEdge e : graph.inAdjacent(node)) {
      if (adjs != null) {
        Assertions.assertEquals(e.other(node), adjs[i]);
      }
      i++;
    }

    if (adjs == null) {
      Assertions.assertEquals(0, i);
    }
  }

  public static void assertOutAdjEquals(EdgeDedigraph<GNode, ? extends BaseEdge> graph,
                                        GNode node, GNode... adjs) {
    int i = 0;
    for (BaseEdge e : graph.outAdjacent(node)) {
      if (adjs != null) {
        Assertions.assertEquals(e.other(node), adjs[i]);
      }
      i++;
    }

    if (adjs == null) {
      Assertions.assertEquals(0, i);
    }
  }

  public static void assertGraph(int vertexNum, int edgeNum,
                                 int maxDegree, int numberOfLoops,
                                 BaseGraph<GNode> graph) {
    Assertions.assertEquals(vertexNum, graph.vertexNum());
    Assertions.assertEquals(edgeNum, graph.edgeNum());
    Assertions.assertEquals(maxDegree, graph.maxDegree());
    Assertions.assertEquals(numberOfLoops, graph.numberOfLoops());
  }

}
