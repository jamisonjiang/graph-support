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
  public void testCopy() {
    digraph.addEdge(n1, n1);
    digraph.addEdge(n1, n2);
    digraph.addEdge(n1, n3);
    digraph.addEdge(n1, n5);
    digraph.addEdge(n4, n5);
    digraph.addEdge(n5, n6);

    DedirectedGraph<GNode> copy = this.digraph.copy();
    Assertions.assertEquals(copy.vertexNum(), digraph.vertexNum());
    Assertions.assertEquals(copy.edgeNum(), digraph.edgeNum());
    Assertions.assertEquals(copy.maxDegree(), digraph.maxDegree());
    Assertions.assertEquals(copy.numberOfLoops(), digraph.numberOfLoops());
    Assertions.assertEquals(copy.adjacent(n1), digraph.adjacent(n1));

    copy.remove(n1);
    Assertions.assertNotEquals(copy.vertexNum(), digraph.vertexNum());
    Assertions.assertNotEquals(copy.edgeNum(), digraph.edgeNum());
    Assertions.assertNotEquals(copy.maxDegree(), digraph.maxDegree());
    Assertions.assertNotEquals(copy.numberOfLoops(), digraph.numberOfLoops());
    Assertions.assertNotEquals(copy.adjacent(n1), digraph.adjacent(n1));
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

    DedirectedGraph<GNode> copy = digraph.copy();
    Assertions.assertEquals(digraph, copy);
    Assertions.assertEquals(digraph.hashCode(), copy.hashCode());

    Assertions.assertNotEquals(digraph, g);
    Assertions.assertEquals(digraph.hashCode(), g.hashCode());

    g.remove(n2);
    g.remove(n3);
    g.addEdge(n1, n2);
    g.addEdge(n1, n3);
    Assertions.assertEquals(digraph, g);
    Assertions.assertEquals(digraph.hashCode(), g.hashCode());
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
          // The n1 node can not be founded from deserializable graph
          g.remove(n1);
          if (VertexIndex.class.isAssignableFrom(n1.getClass())) {
            assertGraph(5, 4, 4, 1, g);
            g.add(n1);
            assertGraph(6, 4, 4, 1, g);
          } else {
            assertGraph(4, 1, 1, 0, g);
            g.add(n1);
            assertGraph(5, 1, 1, 0, g);
          }

        });
  }
}
