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

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DirectedGraphTest {

  GNode n1 = newNode("1");
  GNode n2 = newNode("2");
  GNode n3 = newNode("3");
  GNode n4 = newNode("4");
  GNode n5 = newNode("5");
  GNode n6 = newNode("6");
  GNode n7 = newNode("7");

  DirectedGraph<GNode> digraph = new DirectedGraph<>();

  @Test
  public void testAdd() {
    digraph.add(n1);
    digraph.add(n2);
    digraph.add(n3);

    assertGraph(3, 0, 0, 0, digraph);

    digraph.addEdge(n1, n2);
    digraph.addEdge(n3, n4);
    digraph.addEdge(n4, n4);

    assertGraph(4, 3, 1, 1, digraph);
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

    assertGraph(6, 5, 2, 1, digraph);

    digraph.remove(n5);
    assertGraph(5, 3, 1, 1, digraph);

    digraph.removeEdge(n4, n4);
    assertGraph(5, 2, 1, 0, digraph);

    digraph.removeEdge(n4, n5);
    assertGraph(5, 2, 1, 0, digraph);

    digraph.remove(n1);
    digraph.remove(n2);
    digraph.remove(n3);
    digraph.remove(n4);
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

    Iterator<GNode> iterator = digraph.iterator();
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    assertGraph(0, 0, 0, 0, digraph);
  }

  @Test
  public void testAdjAndDegree() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);
    digraph.addEdge(n1, n5);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n5, n6);

    Assertions.assertEquals(4, digraph.degree(n1));
    assertAdjEquals(digraph, n1, n1, n2, n3, n5);
    Assertions.assertEquals(1, digraph.degree(n5));
    assertAdjEquals(digraph, n4, n5);
    Assertions.assertEquals(0, digraph.degree(n7));
    Assertions.assertEquals(1, digraph.selfLoops(n1));
  }

  @Test
  public void testToArray() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);

    GNode[] nodes = digraph.toArray();
    Assertions.assertEquals(3, nodes.length);

    DirectedGraph<GNode> g = new DirectedGraph<>();
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
    assertGraph(1, 1, 1, 1, digraph);
  }

  @Test
  public void testReverse() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n4, n6);
    digraph.add(n7);

    DirectedGraph<GNode> reverse = digraph.reverse();
    assertGraph(7, 5, 3, 1, digraph);
    assertGraph(7, 5, 1, 1, reverse);
    assertAdjEquals(digraph, n1, n1, n2, n3);
    assertAdjEquals(reverse, n1, n1);
    assertAdjEquals(reverse, n4);
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
          assertGraph(3, 3, 3, 1, digraph);
          assertGraph(3, 3, 3, 1, g);
          g.addEdge(n4, n5);
          assertGraph(5, 4, 3, 1, g);
          // The n1 node can not be founded from deserializable graph
          g.remove(n1);
          assertGraph(4, 1, 1, 0, g);
          g.add(n1);
          assertGraph(5, 1, 1, 0, g);
        });
  }
}
