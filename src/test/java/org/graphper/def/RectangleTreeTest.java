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

import org.graphper.api.ext.DefaultBox;
import org.junit.jupiter.api.Test;

public class RectangleTreeTest {

  @Test
  public void testAdd() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    rtree.insert(new DefaultBox(1, 1, 2, 2));
    rtree.insert(new DefaultBox(1, 3, 1, 3));
    rtree.insert(new DefaultBox(2, 4, 2, 5));
    rtree.insert(new DefaultBox(5, 8, 1, 4));
    rtree.insert(new DefaultBox(6, 9, 2, 6));
    rtree.insert(new DefaultBox(7, 10, 3, 7));
  }
}
