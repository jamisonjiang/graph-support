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

package org.graphper.def;

import static org.graphper.def.GNode.newNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the edge-adjacency directed graph (DirectedEdgeGraph<V,E>).
 *
 * NOTE: We require a concrete DirectedEdge implementation for construction. If your project
 * already has (e.g.) `SimpleDirectedEdge<V>` or a static factory (e.g. DirectedEdge.of(u,v,weight)),
 * replace the placeholder `GEdge` below accordingly. The essential contract:
 *   - from()/to() accessors
 *   - weight() (if used) and reverse() returning a new edge with swapped direction
 */
class ComprehensiveDirectedEdgeGraphTest {

  private DirectedEdgeGraph<GNode, GEdge> graph;
  private GNode n1 = newNode("n1");
  private GNode n2 = newNode("n2");
  private GNode n3 = newNode("n3");
  private GNode n4 = newNode("n4");

  @BeforeEach
  void init() {
    graph = new DirectedEdgeGraph<>();
    graph.add(n1); graph.add(n2); graph.add(n3); graph.add(n4);
  }

  @Test
  void testAddEdgeAndAdjacency() {
    graph.addEdge(new GEdge(n1, n2));
    graph.addEdge(new GEdge(n2, n3));
    graph.addEdge(new GEdge(n1, n3));

    assertEquals(3, graph.edgeNum());
    assertEquals(2, graph.degree(n1)); // edges out of n1
    assertEquals(1, graph.degree(n2));
    assertEquals(0, graph.degree(n3));

    List<GNode> n1Out = mapToToNodes(graph.adjacent(n1));
    assertEquals(new HashSet<>(Arrays.asList(n2, n3)), new HashSet<>(n1Out));
    assertTrue(mapToToNodes(graph.adjacent(n3)).isEmpty());
  }

  @Test
  void testSelfLoopEdge() {
    graph.addEdge(new GEdge(n1, n1));
    assertEquals(1, graph.selfLoops(n1));
    assertEquals(1, graph.numberOfLoops());
    assertEquals(1, graph.degree(n1));
    assertEquals(1, mapToToNodes(graph.adjacent(n1)).size());
  }

  @Test
  void testReverseEdgeMutation() {
    GEdge e = new GEdge(n1, n2);
    graph.addEdge(e);
    assertEquals(1, graph.edgeNum());
    assertEquals(new HashSet<>(Arrays.asList(n2)), new HashSet<>(mapToToNodes(graph.adjacent(n1))));

    GEdge reversed = (GEdge) graph.reverseEdge(e); // direction swapped; original removed
    assertEquals(n2, reversed.from());
    assertEquals(n1, reversed.to());
    assertTrue(mapToToNodes(graph.adjacent(n2)).contains(n1));
    assertTrue(mapToToNodes(graph.adjacent(n1)).isEmpty());
  }

  @Test
  void testGraphReverseClone() {
    graph.addEdge(new GEdge(n1, n2));
    graph.addEdge(new GEdge(n2, n3));
    graph.addEdge(new GEdge(n3, n1));

    DirectedEdgeGraph<GNode, GEdge> rev = graph.reverse();
    // Original cycle n1->n2->n3->n1 becomes n2->n1, n3->n2, n1->n3
    assertTrue(mapToToNodes(rev.adjacent(n2)).contains(n1));
    assertTrue(mapToToNodes(rev.adjacent(n3)).contains(n2));
    assertTrue(mapToToNodes(rev.adjacent(n1)).contains(n3));
    assertEquals(graph.edgeNum(), rev.edgeNum());
  }

  @Test
  void GEdgesIterator() {
    graph.addEdge(new GEdge(n1, n2));
    graph.addEdge(new GEdge(n2, n3));
    graph.addEdge(new GEdge(n1, n3));

    int counted = 0;
    for (GEdge e : graph.edges()) {
      assertNotNull(e.from());
      assertNotNull(e.to());
      counted++;
    }
    assertEquals(graph.edgeNum(), counted);
  }

  @Test
  void testClear() {
    graph.addEdge(new GEdge(n1, n2));
    graph.clear();
    assertEquals(0, graph.vertexNum());
    assertEquals(0, graph.edgeNum());
  }

  private List<GNode> mapToToNodes(Iterable<GEdge> edges) {
    List<GNode> list = new ArrayList<>();
    for (GEdge e : edges) list.add(e.to());
    return list;
  }

  // Utility duplication (kept local for clarity)
  private static <T> boolean isEmpty(Iterable<T> it) { return it == null || !it.iterator().hasNext(); }
}


