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

import helper.GraphvizVisual;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.junit.jupiter.api.Test;

/**
 * Test class for improved CoordinateV3 implementation
 */
public class CoordinateV3ImprovedTest extends GraphvizVisual {

  @Test
  public void testHierarchicalLayout() {
    // Create a proper hierarchical structure
    Node root = Node.builder().label("Root").build();
    Node child1 = Node.builder().label("Child1").build();
    Node child2 = Node.builder().label("Child2").build();
    Node child3 = Node.builder().label("Child3").build();
    Node grandchild1 = Node.builder().label("Grandchild1").build();
    Node grandchild2 = Node.builder().label("Grandchild2").build();
    Node grandchild3 = Node.builder().label("Grandchild3").build();
    Node grandchild4 = Node.builder().label("Grandchild4").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(root, child1).build())
        .addLine(Line.builder(root, child2).build())
        .addLine(Line.builder(root, child3).build())
        .addLine(Line.builder(child1, grandchild1).build())
        .addLine(Line.builder(child1, grandchild2).build())
        .addLine(Line.builder(child2, grandchild3).build())
        .addLine(Line.builder(child3, grandchild4).build())
        .build();

    // Set system property to use CoordinateV3
    System.setProperty("dot.coordinate.v3", "true");
    
    try {
      visual(graphviz);
    } finally {
      // Clean up system property
      System.clearProperty("dot.coordinate.v3");
    }
  }

  @Test
  public void testBalancedTree() {
    // Create a balanced tree structure
    Node a = Node.builder().label("A").build();
    Node b = Node.builder().label("B").build();
    Node c = Node.builder().label("C").build();
    Node d = Node.builder().label("D").build();
    Node e = Node.builder().label("E").build();
    Node f = Node.builder().label("F").build();
    Node g = Node.builder().label("G").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(a, b).build())
        .addLine(Line.builder(a, c).build())
        .addLine(Line.builder(b, d).build())
        .addLine(Line.builder(b, e).build())
        .addLine(Line.builder(c, f).build())
        .addLine(Line.builder(c, g).build())
        .build();

    // Set system property to use CoordinateV3
    System.setProperty("dot.coordinate.v3", "true");
    
    try {
      visual(graphviz);
    } finally {
      // Clean up system property
      System.clearProperty("dot.coordinate.v3");
    }
  }

  @Test
  public void testComplexHierarchy() {
    // Create a more complex hierarchical structure
    Node start = Node.builder().label("Start").build();
    Node process1 = Node.builder().label("Process1").build();
    Node process2 = Node.builder().label("Process2").build();
    Node decision = Node.builder().label("Decision").build();
    Node process3 = Node.builder().label("Process3").build();
    Node process4 = Node.builder().label("Process4").build();
    Node end = Node.builder().label("End").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(start, process1).build())
        .addLine(Line.builder(process1, process2).build())
        .addLine(Line.builder(process2, decision).build())
        .addLine(Line.builder(decision, process3).build())
        .addLine(Line.builder(decision, process4).build())
        .addLine(Line.builder(process3, end).build())
        .addLine(Line.builder(process4, end).build())
        .build();

    // Set system property to use CoordinateV3
    System.setProperty("dot.coordinate.v3", "true");
    
    try {
      visual(graphviz);
    } finally {
      // Clean up system property
      System.clearProperty("dot.coordinate.v3");
    }
  }
} 