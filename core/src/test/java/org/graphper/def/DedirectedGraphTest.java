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
import static helper.GraphAssert.assertInAdjEquals;
import static helper.GraphAssert.assertOutAdjEquals;
import static org.graphper.def.GNode.newNode;

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DedirectedGraphTest {

  GNode n1 = newNode("1");
  GNode n2 = newNode("2");
  GNode n3 = newNode("3");
  GNode n4 = newNode("4");
  GNode n5 = newNode("5");
  GNode n6 = newNode("6");
  GNode n7 = newNode("7");

  DedirectedGraph<GNode> digraph = new DedirectedGraph<>();

  @Test
  public void testAdd() {
    digraph.add(n1);
    digraph.add(n2);
    digraph.add(n3);

    assertGraph(3, 0, 0, 0, digraph);

    digraph.addEdge(n1, n2);
    digraph.addEdge(n3, n4);
    digraph.addEdge(n4, n4);

    assertGraph(4, 3, 3, 1, digraph);
    Assertions.assertEquals(1, digraph.selfLoops(n4));

    Assertions.assertEquals(n2, digraph.next(n1));
    Assertions.assertEquals(n3, digraph.next(n2));
    Assertions.assertEquals(n4, digraph.next(n3));
    Assertions.assertNull(digraph.next(n4));
    Assertions.assertNull(digraph.next(n5));
  }

  @Test
  public void testRemove() {
    digraph.addEdge(n1, n2);
    digraph.addEdge(n3, n4);
    digraph.addEdge(n4, n4);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n5, n6);

    assertGraph(6, 5, 4, 1, digraph);

    digraph.remove(n1);
    assertGraph(5, 4, 4, 1, digraph);

    digraph.removeEdge(n4, n4);
    assertGraph(5, 3, 2, 0, digraph);

    digraph.removeEdge(n4, n5);
    assertGraph(5, 2, 1, 0, digraph);

    digraph.remove(n2);
    digraph.remove(n3);
    digraph.remove(n4);
    digraph.remove(n5);
    digraph.remove(n6);
    assertGraph(0, 0, 0, 0, digraph);
  }

  @Test
  public void testConCurrentRemove() {
    digraph.addEdge(n1, n2);
    digraph.addEdge(n3, n4);
    digraph.addEdge(n4, n4);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n5, n6);

    Assertions.assertThrows(ConcurrentModificationException.class, () -> {
      Iterator<GNode> iterator = digraph.iterator();
      while (iterator.hasNext()) {
        iterator.next();
        digraph.remove(n4);
      }
    });

    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      Iterator<GNode> iterator = digraph.iterator();
      while (iterator.hasNext()) {
        iterator.next();
        iterator.remove();
      }
    });
  }

  @Test
  public void testAdjAndDegree() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);
    digraph.addEdge(n1, n5);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n5, n6);

    Assertions.assertEquals(5, digraph.degree(n1));
    assertAdjEquals(digraph, n1, n1, n2, n3, n5, n1);
    Assertions.assertEquals(3, digraph.degree(n5));
    assertAdjEquals(digraph, n4, n5);
    Assertions.assertEquals(0, digraph.degree(n7));
    Assertions.assertEquals(1, digraph.selfLoops(n1));

    Assertions.assertEquals(4, digraph.outDegree(n1));
    assertOutAdjEquals(digraph, n1, n1, n2, n3, n5);
    Assertions.assertEquals(1, digraph.outDegree(n5));
    assertOutAdjEquals(digraph, n4, n5);
    Assertions.assertEquals(0, digraph.outDegree(n7));

    Assertions.assertEquals(1, digraph.inDegree(n1));
    assertInAdjEquals(digraph, n1, n1);
    Assertions.assertEquals(2, digraph.inDegree(n5));
    assertInAdjEquals(digraph, n4);
    Assertions.assertEquals(0, digraph.outDegree(n7));
  }

  @Test
  public void testToArray() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);

    GNode[] nodes = digraph.toArray();
    Assertions.assertEquals(3, nodes.length);

    DedirectedGraph<GNode> g = new DedirectedGraph<>();
    nodes = g.toArray();
    Assertions.assertNull(nodes);
  }

  @Test
  public void testClear() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);

    digraph.clear();
    assertGraph(0, 0, 0, 0, digraph);

    digraph.addEdge(n2, n2);
    assertGraph(1, 1, 2, 1, digraph);
  }

  @Test
  public void testReverse() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n4, n6);
    digraph.add(n7);

    DedirectedGraph<GNode> reverse = digraph.reverse();
    assertGraph(7, 5, 4, 1, digraph);
    assertGraph(7, 5, 4, 1, reverse);
    assertAdjEquals(digraph, n1, n1, n2, n3, n1);
    assertAdjEquals(reverse, n1, n1, n1, n2, n3);
    assertAdjEquals(reverse, n4, n5, n6);
  }

  @Test
  public void testEqualsAndHashCode() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);

    DedirectedGraph<GNode> g = new DedirectedGraph<>();
    g.addEdge(n1, n3);
    g.addEdge(n1, n2);
    g.addEdge(n1, n1);

  }


  @Test
  public void testSerial() throws IOException, ClassNotFoundException {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + this.getClass().getName(), digraph,
        g -> {
          Assertions.assertEquals(digraph, g);
          assertGraph(3, 3, 4, 1, digraph);
          assertGraph(3, 3, 4, 1, g);
          g.addEdge(n4, n5);
          assertGraph(5, 4, 4, 1, g);

          g.remove(n1);
          assertGraph(4, 1, 1, 0, g);
          g.add(n1);
          assertGraph(5, 1, 1, 0, g);
        });
  }

  @Test
  public void testForEachAdjacent() {
    // Setup test graph
    digraph.addEdge(n1, n2);
    digraph.addEdge(n2, n3);
    digraph.addEdge(n3, n1);
    digraph.addEdge(n1, n4);
    
    // Test forEachAdjacent for n1 (should include both in and out adjacent)
    java.util.List<GNode> adjacentNodes = new java.util.ArrayList<>();
    digraph.forEachAdjacent(n1, adjacentNodes::add);
    
    // n1 should be adjacent to: n2 (out), n4 (out), n3 (in)
    Assertions.assertEquals(3, adjacentNodes.size());
    Assertions.assertTrue(adjacentNodes.contains(n2));
    Assertions.assertTrue(adjacentNodes.contains(n4));
    Assertions.assertTrue(adjacentNodes.contains(n3));
    
    // Test forEachAdjacent for n2 (should include both in and out adjacent)
    adjacentNodes.clear();
    digraph.forEachAdjacent(n2, adjacentNodes::add);
    
    // n2 should be adjacent to: n3 (out), n1 (in)
    Assertions.assertEquals(2, adjacentNodes.size());
    Assertions.assertTrue(adjacentNodes.contains(n3));
    Assertions.assertTrue(adjacentNodes.contains(n1));
    
    // Test forEachAdjacent for non-existent node
    adjacentNodes.clear();
    digraph.forEachAdjacent(n7, adjacentNodes::add);
    Assertions.assertEquals(0, adjacentNodes.size());
  }

  @Test
  public void testForEachInAdjacent() {
    // Setup test graph
    digraph.addEdge(n1, n2);
    digraph.addEdge(n2, n3);
    digraph.addEdge(n3, n1);
    digraph.addEdge(n4, n1);
    
    // Test forEachInAdjacent for n1 (should include only incoming edges)
    java.util.List<GNode> inAdjacentNodes = new java.util.ArrayList<>();
    digraph.forEachInAdjacent(n1, inAdjacentNodes::add);
    
    // n1 should have incoming edges from: n3, n4
    Assertions.assertEquals(2, inAdjacentNodes.size());
    Assertions.assertTrue(inAdjacentNodes.contains(n3));
    Assertions.assertTrue(inAdjacentNodes.contains(n4));
    
    // Test forEachInAdjacent for n2 (should include only incoming edges)
    inAdjacentNodes.clear();
    digraph.forEachInAdjacent(n2, inAdjacentNodes::add);
    
    // n2 should have incoming edges from: n1
    Assertions.assertEquals(1, inAdjacentNodes.size());
    Assertions.assertTrue(inAdjacentNodes.contains(n1));
    
    // Test forEachInAdjacent for node with no incoming edges
    inAdjacentNodes.clear();
    digraph.forEachInAdjacent(n4, inAdjacentNodes::add);
    Assertions.assertEquals(0, inAdjacentNodes.size());
    
    // Test forEachInAdjacent for non-existent node
    inAdjacentNodes.clear();
    digraph.forEachInAdjacent(n7, inAdjacentNodes::add);
    Assertions.assertEquals(0, inAdjacentNodes.size());
  }

  @Test
  public void testForEachOutAdjacent() {
    // Setup test graph
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);
    digraph.addEdge(n2, n4);
    digraph.addEdge(n3, n4);
    
    // Test forEachOutAdjacent for n1 (should include only outgoing edges)
    java.util.List<GNode> outAdjacentNodes = new java.util.ArrayList<>();
    digraph.forEachOutAdjacent(n1, outAdjacentNodes::add);
    
    // n1 should have outgoing edges to: n2, n3
    Assertions.assertEquals(2, outAdjacentNodes.size());
    Assertions.assertTrue(outAdjacentNodes.contains(n2));
    Assertions.assertTrue(outAdjacentNodes.contains(n3));
    
    // Test forEachOutAdjacent for n2 (should include only outgoing edges)
    outAdjacentNodes.clear();
    digraph.forEachOutAdjacent(n2, outAdjacentNodes::add);
    
    // n2 should have outgoing edges to: n4
    Assertions.assertEquals(1, outAdjacentNodes.size());
    Assertions.assertTrue(outAdjacentNodes.contains(n4));
    
    // Test forEachOutAdjacent for node with no outgoing edges
    outAdjacentNodes.clear();
    digraph.forEachOutAdjacent(n4, outAdjacentNodes::add);
    Assertions.assertEquals(0, outAdjacentNodes.size());
    
    // Test forEachOutAdjacent for non-existent node
    outAdjacentNodes.clear();
    digraph.forEachOutAdjacent(n7, outAdjacentNodes::add);
    Assertions.assertEquals(0, outAdjacentNodes.size());
  }

  @Test
  public void testForEachAdjacentNullPointerException() {
    // Test that null consumer throws NullPointerException
    Assertions.assertThrows(NullPointerException.class, () -> {
      digraph.forEachAdjacent(n1, null);
    });
    
    Assertions.assertThrows(NullPointerException.class, () -> {
      digraph.forEachInAdjacent(n1, null);
    });
    
    Assertions.assertThrows(NullPointerException.class, () -> {
      digraph.forEachOutAdjacent(n1, null);
    });
  }

  @Test
  public void testForEachAdjacentWithSelfLoop() {
    // Setup test graph with self-loop
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    
    // Test forEachAdjacent with self-loop
    java.util.List<GNode> adjacentNodes = new java.util.ArrayList<>();
    digraph.forEachAdjacent(n1, adjacentNodes::add);
    
    // n1 should be adjacent to: n1 (self-loop appears twice: once as in, once as out), n2 (out)
    Assertions.assertEquals(3, adjacentNodes.size());
    long n1Count = adjacentNodes.stream().filter(n -> n.equals(n1)).count();
    Assertions.assertEquals(2, n1Count); // Self-loop appears twice
    Assertions.assertTrue(adjacentNodes.contains(n2));
    
    // Test forEachInAdjacent with self-loop
    java.util.List<GNode> inAdjacentNodes = new java.util.ArrayList<>();
    digraph.forEachInAdjacent(n1, inAdjacentNodes::add);
    
    // n1 should have incoming edge from itself
    Assertions.assertEquals(1, inAdjacentNodes.size());
    Assertions.assertTrue(inAdjacentNodes.contains(n1));
    
    // Test forEachOutAdjacent with self-loop
    java.util.List<GNode> outAdjacentNodes = new java.util.ArrayList<>();
    digraph.forEachOutAdjacent(n1, outAdjacentNodes::add);
    
    // n1 should have outgoing edges to itself and n2
    Assertions.assertEquals(2, outAdjacentNodes.size());
    Assertions.assertTrue(outAdjacentNodes.contains(n1));
    Assertions.assertTrue(outAdjacentNodes.contains(n2));
  }

  @Test
  public void testForEachAdjacentConsistencyWithIterable() {
    // Setup test graph
    digraph.addEdge(n1, n2);
    digraph.addEdge(n2, n3);
    digraph.addEdge(n3, n1);
    digraph.addEdge(n4, n1);
    
    // Test that forEachAdjacent produces same results as iterating over adjacent()
    java.util.List<GNode> forEachResult = new java.util.ArrayList<>();
    java.util.List<GNode> iterableResult = new java.util.ArrayList<>();
    
    digraph.forEachAdjacent(n1, forEachResult::add);
    for (GNode node : digraph.adjacent(n1)) {
      iterableResult.add(node);
    }
    
    Assertions.assertEquals(iterableResult.size(), forEachResult.size());
    for (GNode node : iterableResult) {
      Assertions.assertTrue(forEachResult.contains(node));
    }
    
    // Test forEachInAdjacent consistency
    forEachResult.clear();
    iterableResult.clear();
    
    digraph.forEachInAdjacent(n1, forEachResult::add);
    for (GNode node : digraph.inAdjacent(n1)) {
      iterableResult.add(node);
    }
    
    Assertions.assertEquals(iterableResult.size(), forEachResult.size());
    for (GNode node : iterableResult) {
      Assertions.assertTrue(forEachResult.contains(node));
    }
    
    // Test forEachOutAdjacent consistency
    forEachResult.clear();
    iterableResult.clear();
    
    digraph.forEachOutAdjacent(n1, forEachResult::add);
    for (GNode node : digraph.outAdjacent(n1)) {
      iterableResult.add(node);
    }
    
    Assertions.assertEquals(iterableResult.size(), forEachResult.size());
    for (GNode node : iterableResult) {
      Assertions.assertTrue(forEachResult.contains(node));
    }
  }
}
