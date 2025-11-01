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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class UnaryConcatIterableTest {

  @Test
  public void testConcatIterator() {
    List<Integer> l1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> l2 = Arrays.asList(6, 7, 8, 9, 10);

    UnaryConcatIterable<Integer> concatIterable = new UnaryConcatIterable<>(l1, l2);

    int i = 1;
    for (Integer integer : concatIterable) {
      Assertions.assertEquals(Integer.valueOf(i++), integer);
    }
  }

  @Test
  public void testConcatIteratorRemove() {
    List<Integer> l1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> l2 = Arrays.asList(6, 7, 8, 9, 10);
    List<Integer> l3 = Arrays.asList(11, 12, 13, 14, 15);

    UnaryConcatIterable<Integer> concatIterable = new UnaryConcatIterable<>(l1, l2, l3);

    Iterator<Integer> iterator = concatIterable.iterator();

    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      while (iterator.hasNext()) {
        Integer next = iterator.next();
        System.out.println(next);
        iterator.remove();
      }
    });
  }

  @Test
  public void testConcatConcat() {
    List<Integer> l1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> l2 = Arrays.asList(6, 7, 8, 9, 10);
    List<Integer> l3 = Arrays.asList(11, 12, 13, 14, 15);
    List<Integer> l4 = Arrays.asList(16, 17, 18);

    UnaryConcatIterable<Integer> concatIterable = new UnaryConcatIterable<>(
        new UnaryConcatIterable<>(l1, l2),
        new UnaryConcatIterable<>(l3, l4)
    );

    List<Integer> l5 = Arrays.asList(19, 20);
    List<Integer> l6 = Arrays.asList(21, 22);
    concatIterable = new UnaryConcatIterable<>(concatIterable, l5, l6);
    concatIterable = new UnaryConcatIterable<>(concatIterable, Collections.emptyList());

    int i = 1;
    for (Integer integer : concatIterable) {
      Assertions.assertEquals(Integer.valueOf(i++), integer);
    }
  }

  @Test
  public void testFilterConcatIterator() {
    List<Integer> l1 = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> l2 = Arrays.asList(6, 7, 8, null, 9, 10);
    List<Integer> l3 = Arrays.asList(11, 12, null, 13, 14, 15);
    List<Integer> l4 = Arrays.asList(16, 17, 18);

    Predicate<Integer> filter = integer -> {
      if (integer == null) {
        return false;
      }

      return integer % 2 == 0;
    };

    UnaryConcatIterable<Integer> concatIterable = new UnaryConcatIterable<>(
        new UnaryConcatIterable<>(l1, l2),
        new UnaryConcatIterable<>(l3, l4)
    );

    List<Integer> l5 = Arrays.asList(19, 20);
    List<Integer> l6 = Arrays.asList(21, 22);
    concatIterable = new UnaryConcatIterable<>(concatIterable, l5, l6);
    concatIterable = new UnaryConcatIterable<>(filter, concatIterable, Collections.emptyList());


    int i = 2;
    Iterator<Integer> iterator = concatIterable.iterator();
    while (iterator.hasNext()) {
      Assertions.assertEquals(Integer.valueOf(i), iterator.next());
      i += 2;
    }
    Assertions.assertThrows(NoSuchElementException.class, iterator::next);
  }
}
