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

import helper.DocumentUtils;
import helper.SerialHelper;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BagTest {

  @Test
  public void testAddAndIterable() {
    Bag<Integer, Integer> bag = new Bag<>(0);

    Assertions.assertTrue(isEmpty(bag));

    bag.add(1);
    bag.add(2);
    bag.add(3);
    bag.add(4);
    bag.add(5);

    Assertions.assertEquals(5, size(bag));

    int i = 0;
    for (Integer b : bag) {
      i++;
      Assertions.assertEquals(Integer.valueOf(i), b);
    }

    bag.add(null);
    Assertions.assertEquals(6, size(bag));
  }

  @Test
  public void testRemove() {
    Bag<Integer, Integer> bag = new Bag<>(0);
    bag.add(1);
    bag.add(2);
    bag.add(3);

    Iterator<Integer> iterator = bag.iterator();
    while (iterator.hasNext()) {
      Integer v = iterator.next();
      if (Objects.equals(v, 1)) {
        iterator.remove();
      }
    }

    bag.remove(2);
    bag.removeIf(v -> Objects.equals(v, 3));

    Assertions.assertTrue(isEmpty(bag));

    bag.add(4);
    bag.add(5);
    bag.add(6);

    Assertions.assertThrows(ConcurrentModificationException.class, () -> {
      for (Integer v : bag) {
        bag.remove(4);
      }
    });
    Assertions.assertThrows(ConcurrentModificationException.class, () -> {
      for (Integer v : bag) {
        bag.removeIf(n -> Objects.equals(n, 5));
      }
    });
    Assertions.assertEquals(1, size(bag));

    bag.add(7);
    bag.add(7);
    bag.add(8);

    Assertions.assertEquals(4, size(bag));
    bag.removeIf(v -> Objects.equals(v, 7));
    Assertions.assertEquals(3, size(bag));
    bag.removeIf(v -> Objects.equals(v, 7));
    Assertions.assertEquals(2, size(bag));
    bag.removeIf(v -> Objects.equals(v, 6));
    Assertions.assertEquals(1, size(bag));
    bag.removeIf(v -> Objects.equals(v, 8));
    Assertions.assertTrue(isEmpty(bag));
  }

  @Test
  public void testUnmodify() {
    Bag<Integer, Integer> bag = new Bag<>(true);
    Assertions.assertThrows(UnmodifiableBagException.class, () -> bag.add(1));
  }

  @Test
  public void testHashCodeAndEquals() {
    Bag<Integer, Integer> b1 = new Bag<>(0);
    b1.add(1);
    b1.add(2);
    b1.add(3);

    Bag<Integer, Integer> b2 = new Bag<>(0);
    b2.add(1);
    b2.add(2);
    b2.add(3);

    Bag<Integer, Integer> b3 = new Bag<>(0);
    b3.add(1);
    b3.add(3);
    b3.add(2);

    Bag<Integer, Integer> b4 = new Bag<>(1);
    b4.add(1);
    b4.add(3);
    b4.add(2);

    Bag<Integer, Integer> b5 = new Bag<>(0);
    b5.add(1);
    b5.add(3);

    Assertions.assertEquals(b1, b2);
    Assertions.assertEquals(b1.hashCode(), b2.hashCode());

    b2.add(null);
    Assertions.assertNotEquals(b1, b2);
    Assertions.assertNotEquals(b1.hashCode(), b2.hashCode());

    Assertions.assertNotEquals(b3, b4);
    Assertions.assertNotEquals(b3.hashCode(), b4.hashCode());

    Assertions.assertNotEquals(b3, b5);
    b5.add(2);
    Assertions.assertEquals(b3, b5);
  }

  @Test
  public void testSerializable() throws IOException, ClassNotFoundException {
    Bag<Integer, Integer> bag = new Bag<>(0);
    bag.add(1);
    bag.add(2);
    bag.add(3);

    SerialHelper.testSerial(
        DocumentUtils.getTestSerialPath() + "Bag", bag,
        b -> {
          Assertions.assertEquals(3, size(b));
          b.removeIf(v -> Objects.equals(v, 1));
          Assertions.assertEquals(2, size(b));
          b.add(4);
          Assertions.assertEquals(3, size(b));
        });
  }

  private boolean isEmpty(Bag bag) {
    return size(bag) == 0;
  }

  private int size(Bag bag) {
    int i = 0;
    for (Object o : bag) {
      i++;
    }
    return i;
  }
}
