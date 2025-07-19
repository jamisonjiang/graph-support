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

public class UndirectedGraphTest {

  GNode n1 = newNode("1");
  GNode n2 = newNode("2");
  GNode n3 = newNode("3");
  GNode n4 = newNode("4");
  GNode n5 = newNode("5");
  GNode n6 = newNode("6");
  GNode n7 = newNode("7");

  UndirectedGraph<GNode> graph = new UndirectedGraph<>();

  @Test
  public void testAdd() {
    graph.add(n1);
    graph.add(n2);
    graph.add(n3);

    assertGraph(3, 0, 0, 0, graph);

    graph.addEdge(n1, n2);
    graph.addEdge(n3, n4);
    graph.addEdge(n4, n4);

    assertGraph(4, 3, 3, 1, graph);
    Assertions.assertEquals(1, graph.selfLoops(n4));
  }

  @Test
  public void testRemove() {
    graph.addEdge(n1, n2);
    graph.addEdge(n3, n4);
    graph.addEdge(n4, n4);
    graph.addEdge(n4, n5);
    graph.addEdge(n5, n6);

    assertGraph(6, 5, 4, 1, graph);

    graph.remove(n1);
    assertGraph(5, 4, 4, 1, graph);

    graph.removeEdge(n4, n4);
    assertGraph(5, 3, 2, 0, graph);

    graph.removeEdge(n4, n5);
    assertGraph(5, 2, 1, 0, graph);

    graph.remove(n2);
    graph.remove(n3);
    graph.remove(n4);
    graph.remove(n5);
    graph.remove(n6);
    assertGraph(0, 0, 0, 0, graph);
  }

  @Test
  public void testConCurrentRemove() {
    graph.addEdge(n1, n2);
    graph.addEdge(n3, n4);
    graph.addEdge(n4, n4);
    graph.addEdge(n4, n5);
    graph.addEdge(n5, n6);

    Assertions.assertThrows(ConcurrentModificationException.class, () -> {
      Iterator<GNode> iterator = graph.iterator();
      while (iterator.hasNext()) {
        iterator.next();
        graph.remove(n4);
      }
    });

    Iterator<GNode> iterator = graph.iterator();
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    assertGraph(0, 0, 0, 0, graph);
  }

  @Test
  public void testAdjAndDegree() {
    graph.addEdge(n1, n1);
    graph.addEdge(n1, n2);
    graph.addEdge(n1, n3);
    graph.addEdge(n1, n5);
    graph.addEdge(n4, n5);
    graph.addEdge(n5, n6);

    Assertions.assertEquals(5, graph.degree(n1));
    assertAdjEquals(graph, n1, n1, n2, n3, n5);
    Assertions.assertEquals(3, graph.degree(n5));
    assertAdjEquals(graph, n4, n5);
    Assertions.assertEquals(0, graph.degree(n7));
    Assertions.assertEquals(1, graph.selfLoops(n1));
  }

  @Test
  public void testToArray() {
    graph.addEdge(n1, n1);
    graph.addEdge(n1, n2);
    graph.addEdge(n1, n3);

    GNode[] nodes = graph.toArray();
    Assertions.assertEquals(3, nodes.length);

    UndirectedGraph<GNode> g = new UndirectedGraph<>();
    nodes = g.toArray();
    Assertions.assertNull(nodes);
  }

  @Test
  public void testClear() {
    graph.addEdge(n1, n1);
    graph.addEdge(n1, n2);
    graph.addEdge(n1, n3);

    graph.clear();
    assertGraph(0, 0, 0, 0, graph);

    graph.addEdge(n2, n2);
    assertGraph(1, 1, 2, 1, graph);
  }

  @Test
  public void testSerial() throws IOException, ClassNotFoundException {
    graph.addEdge(n1, n1);
    graph.addEdge(n1, n2);
    graph.addEdge(n1, n3);

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + this.getClass().getName(), graph,
        g -> {
          Assertions.assertEquals(graph, g);
          assertGraph(3, 3, 4, 1, graph);
          assertGraph(3, 3, 4, 1, g);
          g.addEdge(n4, n5);
          assertGraph(5, 4, 4, 1, g);

          g.remove(n1);
          assertGraph(4, 1, 1, 0, g);
          g.add(n1);
          assertGraph(5, 1, 1, 0, g);
        });
  }
}
