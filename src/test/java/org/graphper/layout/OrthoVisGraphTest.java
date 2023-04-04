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

package org.graphper.layout;

import org.graphper.layout.OrthoVisGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.def.FlatPoint;
import org.graphper.layout.OrthoVisGraph.GridVertex;

public class OrthoVisGraphTest {

  private final FlatPoint leftUp = new FlatPoint(0, 0);
  private final FlatPoint rightDown = new FlatPoint(1, 1);

  @Test
  public void testAddLeft() {
    OrthoVisGraph ovg = new OrthoVisGraph();
    GridVertex v1 = new GridVertex(leftUp, rightDown);
    GridVertex v2 = new GridVertex(leftUp, rightDown);
    ovg.addLeft(v1, v2);

    Assertions.assertEquals(v2, v1.getLeft());
    Assertions.assertEquals(v1, v2.getRight());
  }

  @Test
  public void testAddRight() {
    OrthoVisGraph ovg = new OrthoVisGraph();
    GridVertex v1 = new GridVertex(leftUp, rightDown);
    GridVertex v2 = new GridVertex(leftUp, rightDown);
    ovg.addRight(v1, v2);

    Assertions.assertEquals(v2, v1.getRight());
    Assertions.assertEquals(v1, v2.getLeft());
  }

  @Test
  public void testAddTop() {
    OrthoVisGraph ovg = new OrthoVisGraph();
    GridVertex v1 = new GridVertex(leftUp, rightDown);
    GridVertex v2 = new GridVertex(leftUp, rightDown);
    ovg.addTop(v1, v2);

    Assertions.assertEquals(v2, v1.getTop());
    Assertions.assertEquals(v1, v2.getBottom());
  }

  @Test
  public void testAddBottom() {
    OrthoVisGraph ovg = new OrthoVisGraph();
    GridVertex v1 = new GridVertex(leftUp, rightDown);
    GridVertex v2 = new GridVertex(leftUp, rightDown);
    ovg.addBottom(v1, v2);

    Assertions.assertEquals(v2, v1.getBottom());
    Assertions.assertEquals(v1, v2.getTop());
  }
}
