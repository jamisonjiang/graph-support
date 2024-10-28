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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.graphper.api.ext.DefaultBox;
import org.junit.jupiter.api.Test;

public class RectangleTreeTest {

  @Test
  public void testOverlappingInsert() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    rtree.insert(new DefaultBox(1, 5, 1, 5));  // Box 1
    rtree.insert(new DefaultBox(3, 6, 3, 6));  // Box 2 (overlaps Box 1)
    rtree.insert(new DefaultBox(4, 7, 4, 8));  // Box 3 (overlaps Box 2)
    rtree.insert(new DefaultBox(2, 3, 2, 4));  // Box 4 (partial overlap)

    List<DefaultBox> result = rtree.search(new DefaultBox(2, 4, 2, 4));
    assertEquals(4, result.size());  // All boxes should match the search area.
  }

  @Test
  public void testNonOverlappingInsert() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    rtree.insert(new DefaultBox(1, 2, 1, 2));  // Box 1
    rtree.insert(new DefaultBox(3, 4, 3, 4));  // Box 2 (no overlap with Box 1)
    rtree.insert(new DefaultBox(5, 6, 5, 6));  // Box 3 (no overlap with others)

    List<DefaultBox> result = rtree.search(new DefaultBox(3, 4, 3, 4));
    assertEquals(1, result.size());  // Only Box 2 should match.
  }

  @Test
  public void testBoundaryCondition() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    rtree.insert(new DefaultBox(1, 2, 1, 2));  // Box 1
    rtree.insert(new DefaultBox(2, 3, 2, 3));  // Box 2 (touches Box 1 at boundary)

    List<DefaultBox> result = rtree.search(new DefaultBox(2, 2, 2, 2));
    assertEquals(2, result.size());  // Both Box 1 and Box 2 should match.
  }

  @Test
  public void testMultipleSplits() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(2);  // Lower capacity for more splits

    for (int i = 0; i < 1000; i++) {
      rtree.insert(new DefaultBox(i, i + 1, i, i + 1));
    }

    List<DefaultBox> result = rtree.search(new DefaultBox(3, 4, 3, 4));
    assertEquals(3, result.size());  // Only one box should match.
  }

  @Test
  public void testSearchEmptyTree() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    List<DefaultBox> result = rtree.search(new DefaultBox(1, 2, 1, 2));
    assertTrue(result.isEmpty());  // No boxes should match in an empty tree.
  }

  @Test
  public void testLargeBoxInsert() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    rtree.insert(new DefaultBox(0, 100, 0, 100));  // Large box
    rtree.insert(new DefaultBox(50, 60, 50, 60));  // Small box inside the large one

    List<DefaultBox> result = rtree.search(new DefaultBox(55, 56, 55, 56));
    assertEquals(2, result.size());  // Both boxes should match.
  }

  @Test
  public void testSearchNonExistingBox() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    rtree.insert(new DefaultBox(1, 2, 1, 2));
    rtree.insert(new DefaultBox(3, 4, 3, 4));

    List<DefaultBox> result = rtree.search(new DefaultBox(5, 6, 5, 6));
    assertTrue(result.isEmpty());  // No box should match.
  }

  @Test
  public void testDuplicateInsert() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(3);

    DefaultBox box = new DefaultBox(1, 2, 1, 2);
    rtree.insert(box);
    rtree.insert(box);  // Insert the same box again

    List<DefaultBox> result = rtree.search(new DefaultBox(1, 2, 1, 2));
    assertEquals(2, result.size());  // Both inserts should be found.
  }

  @Test
  public void testMassiveInsertion() {
    RectangleTree<DefaultBox> rtree = new RectangleTree<>(5);  // Higher capacity to trigger splits
    Random random = new Random();

    // Insert 10,000 random boxes with non-overlapping coordinates
    for (int i = 0; i < 10000; i++) {
      int x1 = random.nextInt(10000);
      int y1 = random.nextInt(10000);
      int width = random.nextInt(5) + 1;  // Width between 1 and 5
      int height = random.nextInt(5) + 1; // Height between 1 and 5
      rtree.insert(new DefaultBox(x1, x1 + width, y1, y1 + height));
    }

    rtree.insert(new DefaultBox(4900, 5100, 4400, 5200));

    // Perform a search for a box at a known range and assert the result size
    List<DefaultBox> result = rtree.search(new DefaultBox(5000, 5005, 5000, 5005));
    assertFalse(result.isEmpty());  // There should be some matching boxes.

    // Check that search on non-overlapping area returns an empty list
    List<DefaultBox> emptyResult = rtree.search(new DefaultBox(20000, 20010, 20000, 20010));
    assertTrue(emptyResult.isEmpty());  // No matches expected.
  }
}
