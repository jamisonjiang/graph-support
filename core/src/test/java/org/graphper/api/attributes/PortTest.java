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

package org.graphper.api.attributes;

import org.graphper.api.attributes.Port;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PortTest {

  @Test
  public void testValueOf() {
    Assertions.assertEquals(Port.WEST, Port.valueOf(0));
    Assertions.assertEquals(Port.NORTH_WEST, Port.valueOf(1));
    Assertions.assertEquals(Port.NORTH, Port.valueOf(2));
    Assertions.assertEquals(Port.NORTH_EAST, Port.valueOf(3));
    Assertions.assertEquals(Port.EAST, Port.valueOf(4));
    Assertions.assertEquals(Port.SOUTH_EAST, Port.valueOf(5));
    Assertions.assertEquals(Port.SOUTH, Port.valueOf(6));
    Assertions.assertEquals(Port.SOUTH_WEST, Port.valueOf(7));
  }

  @Test
  public void testLink() {
    Assertions.assertEquals(Port.NORTH_WEST, Port.WEST.next());
    Assertions.assertEquals(Port.WEST, Port.NORTH_WEST.pre());

    Assertions.assertEquals(Port.NORTH, Port.NORTH_WEST.next());
    Assertions.assertEquals(Port.NORTH_WEST, Port.NORTH.pre());

    Assertions.assertEquals(Port.NORTH_EAST, Port.NORTH.next());
    Assertions.assertEquals(Port.NORTH, Port.NORTH_EAST.pre());

    Assertions.assertEquals(Port.EAST, Port.NORTH_EAST.next());
    Assertions.assertEquals(Port.NORTH_EAST, Port.EAST.pre());

    Assertions.assertEquals(Port.SOUTH_EAST, Port.EAST.next());
    Assertions.assertEquals(Port.EAST, Port.SOUTH_EAST.pre());

    Assertions.assertEquals(Port.SOUTH, Port.SOUTH_EAST.next());
    Assertions.assertEquals(Port.SOUTH_EAST, Port.SOUTH.pre());

    Assertions.assertEquals(Port.SOUTH_WEST, Port.SOUTH.next());
    Assertions.assertEquals(Port.SOUTH, Port.SOUTH_WEST.pre());

    Assertions.assertEquals(Port.WEST, Port.SOUTH_WEST.next());
    Assertions.assertEquals(Port.SOUTH_WEST, Port.WEST.pre());
  }
}
