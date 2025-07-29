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
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.junit.jupiter.api.Test;

/**
 * Test class for CoordinateV3 implementation
 */
public class CoordinateV3Test extends GraphvizVisual {

  @Test
  public void testSimpleGraph() {
    Node a = Node.builder().label("A").build();
    Node b = Node.builder().label("B").build();
    Node c = Node.builder().label("C").build();
    Node d = Node.builder().label("D").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(a, b).build())
        .addLine(Line.builder(b, c).build())
        .addLine(Line.builder(c, d).build())
        .addLine(Line.builder(a, c).build())
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
  public void testComplexGraph() {
    Node a = Node.builder().label("A").build();
    Node b = Node.builder().label("B").build();
    Node c = Node.builder().label("C").build();
    Node d = Node.builder().label("D").build();
    Node e = Node.builder().label("E").build();
    Node f = Node.builder().label("F").build();
    Node g = Node.builder().label("G").build();
    Node h = Node.builder().label("H").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(a, b).build())
        .addLine(Line.builder(a, c).build())
        .addLine(Line.builder(b, d).build())
        .addLine(Line.builder(b, e).build())
        .addLine(Line.builder(c, f).build())
        .addLine(Line.builder(c, g).build())
        .addLine(Line.builder(d, h).build())
        .addLine(Line.builder(e, h).build())
        .addLine(Line.builder(f, h).build())
        .addLine(Line.builder(g, h).build())
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
  public void testCrossingReduction() {
    Node a = Node.builder().label("A").build();
    Node b = Node.builder().label("B").build();
    Node c = Node.builder().label("C").build();
    Node d = Node.builder().label("D").build();

    Graphviz graphviz = Graphviz
        .digraph()
        .addLine(Line.builder(a, c).build())
        .addLine(Line.builder(b, d).build())
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