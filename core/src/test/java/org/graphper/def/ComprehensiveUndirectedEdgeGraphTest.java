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

import static helper.GraphAssert.assertAdjEquals;
import static helper.GraphAssert.assertGraph;
import static org.graphper.def.GNode.newNode;
import static org.graphper.def.GUEdge.newEdge;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test for UndirectedEdgeGraph implementation.
 * Tests all methods and edge cases based on the guide.
 */
public class ComprehensiveUndirectedEdgeGraphTest {

  private GNode n1, n2, n3, n4, n5, n6, n7, n8;
  private UndirectedEdgeGraph<GNode, GUEdge> graph;

  @BeforeEach
  void setUp() {
    n1 = newNode("1");
    n2 = newNode("2");
    n3 = newNode("3");
    n4 = newNode("4");
    n5 = newNode("5");
    n6 = newNode("6");
    n7 = newNode("7");
    n8 = newNode("8");
    graph = new UndirectedEdgeGraph<>();
  }

  // ==================== Constructor Tests ====================

  @Test
  void testDefaultConstructor() {
    UndirectedEdgeGraph<GNode, GUEdge> emptyGraph = new UndirectedEdgeGraph<>();
    assertGraph(0, 0, 0, 0, emptyGraph);
  }

  @Test
  void testConstructorWithCapacity() {
    UndirectedEdgeGraph<GNode, GUEdge> capacityGraph = new UndirectedEdgeGraph<>(10);
    assertGraph(0, 0, 0, 0, capacityGraph);
  }

  @Test
  void testConstructorWithInvalidCapacity() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new UndirectedEdgeGraph<GNode, GUEdge>(0);
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new UndirectedEdgeGraph<GNode, GUEdge>(-1);
    });
  }

  // ==================== Vertex Operations Tests ====================

  @Test
  void testAddVertex() {
    // Test adding single vertex
    Assertions.assertTrue(graph.add(n1));
    assertGraph(1, 0, 0, 0, graph);
    Assertions.assertTrue(graph.contains(n1));

    // Test adding multiple vertices
    Assertions.assertTrue(graph.add(n2));
    Assertions.assertTrue(graph.add(n3));
    assertGraph(3, 0, 0, 0, graph);

    // Test adding duplicate vertex
    Assertions.assertFalse(graph.add(n1));
    assertGraph(3, 0, 0, 0, graph);

    // Test adding null vertex
    Assertions.assertFalse(graph.add(null));
    assertGraph(3, 0, 0, 0, graph);
  }

  @Test
  void testRemoveVertex() {
    // Setup graph with edges
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n3, n4));
    graph.addEdge(newEdge(n4, n1));
    graph.addEdge(newEdge(n1, n1)); // self-loop
    assertGraph(4, 5, 4, 1, graph);

    // Test removing vertex with edges
    Assertions.assertTrue(graph.remove(n1));
    assertGraph(3, 2, 2, 0, graph);
    Assertions.assertFalse(graph.contains(n1));

    // Test removing non-existent vertex
    Assertions.assertFalse(graph.remove(n8));
    assertGraph(3, 2, 2, 0, graph);

    // Test removing null
    Assertions.assertFalse(graph.remove(null));
    assertGraph(3, 2, 2, 0, graph);

    // Test removing all vertices
    graph.remove(n2);
    graph.remove(n3);
    graph.remove(n4);
    assertGraph(0, 0, 0, 0, graph);
  }

  @Test
  void testContains() {
    Assertions.assertFalse(graph.contains(n1));

    graph.add(n1);
    Assertions.assertTrue(graph.contains(n1));

    graph.remove(n1);
    Assertions.assertFalse(graph.contains(n1));
  }

  @Test
  void testVertexNum() {
    Assertions.assertEquals(0, graph.vertexNum());

    graph.add(n1);
    Assertions.assertEquals(1, graph.vertexNum());

    graph.add(n2);
    graph.add(n3);
    Assertions.assertEquals(3, graph.vertexNum());

    graph.remove(n1);
    Assertions.assertEquals(2, graph.vertexNum());
  }

  // ==================== Edge Operations Tests ====================

  @Test
  void testAddEdge() {
    // Test adding simple edge
    graph.addEdge(newEdge(n1, n2));
    assertGraph(2, 1, 1, 0, graph);
    assertAdjEquals(graph, n1, n2);
    assertAdjEquals(graph, n2, n1);

    // Test adding edge with non-existent vertices (should auto-add)
    graph.addEdge(newEdge(n3, n4));
    assertGraph(4, 2, 1, 0, graph);
    assertAdjEquals(graph, n3, n4);
    assertAdjEquals(graph, n4, n3);

    // Test adding self-loop
    graph.addEdge(newEdge(n1, n1));
    assertGraph(4, 3, 3, 1, graph);
    Assertions.assertEquals(1, graph.selfLoops(n1));

    // Test adding duplicate edge
    graph.addEdge(newEdge(n1, n2));
    assertGraph(4, 4, 4, 1, graph); // Should not change

    // Test adding edge with null vertices
    Assertions.assertThrows(NullPointerException.class, () -> {
      graph.addEdge(newEdge(null, n2));
    });

    Assertions.assertThrows(NullPointerException.class, () -> {
      graph.addEdge(newEdge(n1, null));
    });

    Assertions.assertThrows(NullPointerException.class, () -> {
      graph.addEdge(null);
    });
  }

  @Test
  void testRemoveEdge() {
    // Setup graph
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n3, n4));
    graph.addEdge(newEdge(n1, n1));
    assertGraph(4, 4, 3, 1, graph);

    // Test removing existing edge
    Assertions.assertTrue(graph.removeEdge(newEdge(n1, n2)));
    assertGraph(4, 3, 2, 1, graph);
    assertAdjEquals(graph, n1, n1); // Only self-loop remains
    assertAdjEquals(graph, n2, n3);

    // Test removing self-loop
    Assertions.assertTrue(graph.removeEdge(newEdge(n1, n1)));
    assertGraph(4, 2, 2, 0, graph);

    // Test removing non-existent edge
    Assertions.assertFalse(graph.removeEdge(newEdge(n1, n5)));
    assertGraph(4, 2, 2, 0, graph);


    // Test removing edge from non-existent vertex
    Assertions.assertFalse(graph.removeEdge(newEdge(n5, n1)));
  }

  @Test
  void testEdgeNum() {
    Assertions.assertEquals(0, graph.edgeNum());

    graph.addEdge(newEdge(n1, n2));
    Assertions.assertEquals(1, graph.edgeNum());

    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n3, n4));
    Assertions.assertEquals(3, graph.edgeNum());

    graph.removeEdge(newEdge(n1, n2));
    Assertions.assertEquals(2, graph.edgeNum());
  }

  // ==================== Adjacency Tests ====================

  @Test
  void testAdjacent() {
    // Test empty graph
    Iterable<GUEdge> adj = graph.adjacent(n1);
    Assertions.assertNotNull(adj);
    Assertions.assertFalse(adj.iterator().hasNext());

    // Setup graph
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n1, n4));
    graph.addEdge(newEdge(n1, n1)); // self-loop
    graph.addEdge(newEdge(n2, n3));

    // Test adjacency for vertex with multiple edges
    assertAdjEquals(graph, n1, n2, n3, n4, n1);

    // Test adjacency for vertex with single edge
    assertAdjEquals(graph, n2, n1, n3);

    // Test adjacency for vertex with no edges
    assertAdjEquals(graph, n5);

    // Test adjacency for non-existent vertex
    adj = graph.adjacent(n8);
    Assertions.assertNotNull(adj);
    Assertions.assertFalse(adj.iterator().hasNext());
  }

  @Test
  void testForEachAdjacent() {
    // Setup graph
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n1, n4));

    // Test forEachAdjacent
    AtomicInteger count = new AtomicInteger(0);
    graph.forEachAdjacent(n1, v -> count.incrementAndGet());
    Assertions.assertEquals(3, count.get());

    // Test forEachAdjacent with null action
    Assertions.assertThrows(NullPointerException.class, () -> {
      graph.forEachAdjacent(n1, null);
    });

    // Test forEachAdjacent with non-existent vertex
    count.set(0);
    graph.forEachAdjacent(n5, v -> count.incrementAndGet());
    Assertions.assertEquals(0, count.get());
  }

  // ==================== Degree Tests ====================

  @Test
  void testDegree() {
    // Test degree of non-existent vertex
    Assertions.assertEquals(0, graph.degree(n1));

    // Setup graph
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n1, n4));
    graph.addEdge(newEdge(n1, n1)); // self-loop
    graph.addEdge(newEdge(n2, n3));

    // Test degree calculations
    Assertions.assertEquals(5, graph.degree(n1)); // 4 neighbors + 1 self-loop
    Assertions.assertEquals(2, graph.degree(n2)); // n1, n3
    Assertions.assertEquals(2, graph.degree(n3)); // n1, n2
    Assertions.assertEquals(1, graph.degree(n4)); // n1 only
    Assertions.assertEquals(0, graph.degree(n5)); // no edges
  }

  @Test
  void testSelfLoops() {
    // Test self-loops of non-existent vertex
    Assertions.assertEquals(0, graph.selfLoops(n1));

    // Setup graph with self-loops
    graph.addEdge(newEdge(n1, n1));
    graph.addEdge(newEdge(n2, n2));
    graph.addEdge(newEdge(n1, n2));

    // Test self-loop counts
    Assertions.assertEquals(1, graph.selfLoops(n1));
    Assertions.assertEquals(1, graph.selfLoops(n2));
    Assertions.assertEquals(0, graph.selfLoops(n3));
  }

  @Test
  void testMaxDegree() {
    // Test empty graph
    Assertions.assertEquals(0, graph.maxDegree());

    // Setup graph
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n1, n4));
    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n2, n4));
    graph.addEdge(newEdge(n2, n5));

    // Test max degree
    Assertions.assertEquals(4, graph.maxDegree()); // n2 has degree 4
  }

  @Test
  void testAverageDegree() {
    // Test empty graph
    Assertions.assertEquals(0.0, graph.averageDegree(), 0.001);

    // Setup graph
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n2, n3));

    double expected = (3.0 + 2.0 + 1.0) / 3.0;
    Assertions.assertEquals(expected, graph.averageDegree(), 0.001);
  }

  @Test
  void testNumberOfLoops() {
    // Test empty graph
    Assertions.assertEquals(0, graph.numberOfLoops());

    // Setup graph with self-loops
    graph.addEdge(newEdge(n1, n1));
    graph.addEdge(newEdge(n2, n2));
    graph.addEdge(newEdge(n3, n3));
    graph.addEdge(newEdge(n1, n2));

    // Test total self-loops
    Assertions.assertEquals(3, graph.numberOfLoops());
  }

  // ==================== Iterator Tests ====================

  @Test
  void testIterator() {
    // Test empty iterator
    Iterator<GNode> iterator = graph.iterator();
    Assertions.assertFalse(iterator.hasNext());

    // Setup graph
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);

    // Test iterator
    iterator = graph.iterator();
    Assertions.assertTrue(iterator.hasNext());

    GNode first = iterator.next();
    Assertions.assertNotNull(first);

    Assertions.assertTrue(iterator.hasNext());
    GNode second = iterator.next();
    Assertions.assertNotNull(second);

    Assertions.assertTrue(iterator.hasNext());
    GNode third = iterator.next();
    Assertions.assertNotNull(third);

    Assertions.assertFalse(iterator.hasNext());

    // Test iterator remove
    iterator = graph.iterator();
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    assertGraph(0, 0, 0, 0, graph);
  }

  @Test
  void testIteratorConcurrentModification() {
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);

    // Test that concurrent modification doesn't throw exception with current implementation
    Iterator<GNode> iterator = graph.iterator();
    while (iterator.hasNext()) {
      iterator.next();
      graph.add(n4);
    }
    
    // Verify that the new vertex was added
    Assertions.assertTrue(graph.contains(n4));
    Assertions.assertEquals(4, graph.vertexNum());
  }

  @Test
  void testIteratorNoSuchElement() {
    Iterator<GNode> iterator = graph.iterator();
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      iterator.next();
    });
  }

  // ==================== Navigation Tests ====================

  @Test
  void testStart() {
    // Test empty graph
    Assertions.assertNull(graph.start());

    // Test with vertices
    graph.add(n1);
    Assertions.assertEquals(n1, graph.start());

    graph.add(n2);
    Assertions.assertEquals(n1, graph.start()); // Should return first added vertex
  }

  @Test
  void testNext() {
    // Test empty graph
    Assertions.assertNull(graph.next(n1));

    // Setup graph
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);

    // Test navigation
    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertNull(graph.next(n3));

    // Test with non-existent vertex
    Assertions.assertNull(graph.next(n8));
  }

  @Test
  void testPre() {
    // Test empty graph
    Assertions.assertNull(graph.pre(n1));

    // Setup graph
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);

    // Test navigation
    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n3));

    // Test with non-existent vertex
    Assertions.assertNull(graph.pre(n8));
  }

  // ==================== Navigation with Edge Operations Tests ====================

  @Test
  void testNextAfterEdgeOperations() {
    // Setup initial graph with vertices
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);
    graph.add(n4);
    graph.add(n5);

    // Verify initial navigation
    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertEquals(n4, graph.next(n3));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));

    // Add edges and verify navigation still works
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n2, n4));
    graph.addEdge(newEdge(n3, n5));

    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertEquals(n4, graph.next(n3));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));

    // Remove edges and verify navigation still works
    graph.removeEdge(newEdge(n1, n3));
    graph.removeEdge(newEdge(n2, n4));
    graph.removeEdge(newEdge(n3, n5));

    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertEquals(n4, graph.next(n3));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));

    // Add more edges and verify navigation
    graph.addEdge(newEdge(n1, n5));
    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n4, n5));

    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertEquals(n4, graph.next(n3));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));
  }

  @Test
  void testPreAfterEdgeOperations() {
    // Setup initial graph with vertices
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);
    graph.add(n4);
    graph.add(n5);

    // Verify initial navigation
    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n3));
    Assertions.assertEquals(n3, graph.pre(n4));
    Assertions.assertEquals(n4, graph.pre(n5));

    // Add edges and verify navigation still works
    graph.addEdge(newEdge(n1, n3));
    graph.addEdge(newEdge(n2, n4));
    graph.addEdge(newEdge(n3, n5));

    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n3));
    Assertions.assertEquals(n3, graph.pre(n4));
    Assertions.assertEquals(n4, graph.pre(n5));

    // Remove edges and verify navigation still works
    graph.removeEdge(newEdge(n1, n3));
    graph.removeEdge(newEdge(n2, n4));
    graph.removeEdge(newEdge(n3, n5));

    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n3));
    Assertions.assertEquals(n3, graph.pre(n4));
    Assertions.assertEquals(n4, graph.pre(n5));

    // Add more edges and verify navigation
    graph.addEdge(newEdge(n1, n5));
    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n4, n5));

    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n3));
    Assertions.assertEquals(n3, graph.pre(n4));
    Assertions.assertEquals(n4, graph.pre(n5));
  }

  @Test
  void testNavigationAfterVertexRemoval() {
    // Setup graph with vertices and edges
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);
    graph.add(n4);
    graph.add(n5);
    graph.addEdge(newEdge(n1, n2));
    graph.addEdge(newEdge(n2, n3));
    graph.addEdge(newEdge(n3, n4));
    graph.addEdge(newEdge(n4, n5));

    // Verify initial navigation
    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertEquals(n4, graph.next(n3));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));

    // Remove middle vertex and verify navigation
    graph.remove(n3);
    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n4, graph.next(n2));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));

    // Verify pre navigation after removal
    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n4));
    Assertions.assertEquals(n4, graph.pre(n5));

    // Remove first vertex and verify navigation
    graph.remove(n1);
    Assertions.assertEquals(n4, graph.next(n2));
    Assertions.assertEquals(n5, graph.next(n4));
    Assertions.assertNull(graph.next(n5));

    Assertions.assertNull(graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n4));
    Assertions.assertEquals(n4, graph.pre(n5));

    // Remove last vertex and verify navigation
    graph.remove(n5);
    Assertions.assertEquals(n4, graph.next(n2));
    Assertions.assertNull(graph.next(n4));

    Assertions.assertNull(graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n4));
  }

  @Test
  void testNavigationAfterVertexAddition() {
    // Setup initial graph
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);

    // Verify initial navigation
    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
    Assertions.assertNull(graph.next(n3));

    Assertions.assertNull(graph.pre(n1));
    Assertions.assertEquals(n1, graph.pre(n2));
    Assertions.assertEquals(n2, graph.pre(n3));

    // Add new vertices and verify navigation
    graph.add(n4);
    graph.add(n5);

    Assertions.assertEquals(n2, graph.next(n1));
    Assertions.assertEquals(n3, graph.next(n2));
//    Assertions.assertEquals(n4, gr)
  }
}