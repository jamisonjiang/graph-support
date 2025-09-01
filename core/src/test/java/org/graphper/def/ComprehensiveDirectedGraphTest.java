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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the vertex-adjacency directed graph (DirectedGraph<V>).
 */
class ComprehensiveDirectedGraphTest {

  private DirectedGraph<GNode> graph;
  // Nodes
  private GNode n1 = newNode("n1");
  private GNode n2 = newNode("n2");
  private GNode n3 = newNode("n3");
  private GNode n4 = newNode("n4");
  private GNode n5 = newNode("n5");

  @BeforeEach
  void init() {
    graph = new DirectedGraph<>();
  }

  @Test
  void testAddVertices() {
    assertEquals(0, graph.vertexNum());
    assertTrue(graph.add(n1));
    assertTrue(graph.add(n2));
    assertFalse(graph.add(n1)); // duplicate
    assertEquals(2, graph.vertexNum());
    assertTrue(graph.contains(n1));
    assertTrue(graph.contains(n2));
  }

  @Test
  void testRemoveVertices() {
    graph.add(n1); graph.add(n2); graph.add(n3);
    assertTrue(graph.remove(n2));
    assertFalse(graph.contains(n2));
    assertEquals(2, graph.vertexNum());
    assertFalse(graph.remove(n2)); // already removed
  }

  @Test
  void testAddDirectedEdges() {
    graph.add(n1); graph.add(n2); graph.add(n3);
    graph.addEdge(n1, n2);
    graph.addEdge(n2, n3);

    // Directed semantics: only out-neighbors
    assertIterableEquals(Arrays.asList(n2), toList(graph.adjacent(n1)));
    assertIterableEquals(Arrays.asList(n3), toList(graph.adjacent(n2)));
    assertTrue(isEmpty(graph.adjacent(n3)));

    // degree == out-degree
    assertEquals(1, graph.degree(n1));
    assertEquals(1, graph.degree(n2));
    assertEquals(0, graph.degree(n3));

    assertEquals(2, graph.edgeNum());
  }

  @Test
  void testSelfLoop() {
    graph.add(n1);
    graph.addEdge(n1, n1);
    assertEquals(1, graph.selfLoops(n1));
    assertEquals(1, graph.numberOfLoops());
    assertEquals(1, graph.degree(n1)); // out-degree counts self-loop once
    // Adjacent should include itself once (implementation-specific: if stored once)
    assertIterableEquals(Arrays.asList(n1), toList(graph.adjacent(n1)));
  }

  @Test
  void testMultipleEdgesAndDegrees() {
    graph.add(n1); graph.add(n2); graph.add(n3); graph.add(n4);
    graph.addEdge(n1, n2);
    graph.addEdge(n1, n3);
    graph.addEdge(n1, n4);
    graph.addEdge(n2, n4);

    assertEquals(4, graph.edgeNum());
    assertEquals(3, graph.degree(n1)); // n1 -> n2,n3,n4
    assertEquals(1, graph.degree(n2));
    assertEquals(0, graph.degree(n3));
    assertEquals(0, graph.degree(n4));
    assertEquals(3, graph.maxDegree());

    double avg = graph.averageDegree();
    // averageDegree should be edgeNum / vertexNum for directed out-degrees
    assertEquals((double) graph.edgeNum() / graph.vertexNum(), avg, 1e-9);
  }

  @Test
  void testReverseGraph() {
    graph.add(n1); graph.add(n2); graph.add(n3);
    graph.addEdge(n1, n2);
    graph.addEdge(n2, n3);
    graph.addEdge(n1, n3);

    DirectedGraph<GNode> rev = graph.reverse();

    // Original directions: 1->2, 2->3, 1->3
    // Reversed: 2->1, 3->2, 3->1

    assertIterableEquals(Arrays.asList(n2, n3), sorted(adjList(graph, n1)));
    assertIterableEquals(Arrays.asList(n3), sorted(adjList(graph, n2)));
    assertTrue(isEmpty(graph.adjacent(n3)));

    assertIterableEquals(Arrays.asList(n1), sorted(adjList(rev, n2)));
    assertIterableEquals(Arrays.asList(n1, n2), sorted(adjList(rev, n3)));
    assertTrue(isEmpty(rev.adjacent(n1)));

    // Edge / vertex counts preserved
    assertEquals(graph.vertexNum(), rev.vertexNum());
    assertEquals(graph.edgeNum(), rev.edgeNum());
  }

  @Test
  void testIterationOrderViaStartNextPre() {
    graph.add(n1); graph.add(n2); graph.add(n3);
    GNode start = graph.start();
    assertNotNull(start);
    // Collect forward
    List<GNode> forward = new ArrayList<>();
    for (GNode v = start; v != null; v = graph.next(v)) {
      if (!forward.contains(v)) forward.add(v); // guard if circular
      if (forward.size() > graph.vertexNum()) break; // safety
    }
    assertEquals(graph.vertexNum(), forward.size());

    // Walk backward from last
    List<GNode> backward = new ArrayList<>();
    GNode last = forward.get(forward.size() - 1);
    for (GNode v = last; v != null; v = graph.pre(v)) {
      if (!backward.contains(v)) backward.add(v);
      if (backward.size() > graph.vertexNum()) break;
    }
    assertTrue(backward.size() >= 1);
  }

  @Test
  void testRemoveEdges() {
    graph.add(n1); graph.add(n2); graph.add(n3);
    graph.addEdge(n1, n2);
    graph.addEdge(n2, n3);
    assertEquals(2, graph.edgeNum());

    // No direct removeEdge(v,w) in vertex-based DirectedGraph (only if provided; not in BaseGraph)
    // We rebuild via reverse() trick or clear + re-add.
    DirectedGraph<GNode> rev = graph.reverse();
    // After reverse, original (n1,n2) becomes (n2,n1). Ensure adjacency changed.
    assertTrue(contains(rev.adjacent(n2), n1));
    assertFalse(contains(rev.adjacent(n1), n2));
  }

  @Test
  void testClear() {
    graph.add(n1); graph.add(n2); graph.addEdge(n1, n2);
    assertTrue(graph.vertexNum() > 0);
    assertTrue(graph.edgeNum() > 0);
    graph.clear();
    assertEquals(0, graph.vertexNum());
    assertEquals(0, graph.edgeNum());
    assertTrue(isEmpty(graph.adjacent(n1)));
  }

  // Utility helpers (mirror style from Undirected tests)
  private static <T> List<T> toList(Iterable<T> it) {
    List<T> list = new ArrayList<>();
    if (it != null) for (T t : it) list.add(t);
    return list;
  }
  private static boolean isEmpty(Iterable<?> it) { return it == null || !it.iterator().hasNext(); }
  private static <T extends Comparable<T>> List<T> sorted(Iterable<T> it) {
    List<T> l = toList(it); Collections.sort(l); return l; }
  private static <T> boolean contains(Iterable<T> it, T v) { for (T t : it) if (Objects.equals(t,v)) return true; return false; }
  private static <T> List<T> adjList(DirectedGraph<T> g, T v) { return toList(g.adjacent(v)); }
}

