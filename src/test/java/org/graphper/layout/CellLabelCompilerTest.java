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

import java.util.HashMap;
import java.util.Map;
import org.graphper.layout.Cell.RootCell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CellLabelCompilerTest {

  @Test
  public void testCompile() throws LabelFormatException {
    assertCell(CellLabelCompiler.compile(""), 2, 1);
    assertCell(CellLabelCompiler.compile(" "), 2, 1);
    assertCell(CellLabelCompiler.compile("      "), 2, 1);
    assertCell(CellLabelCompiler.compile("   a"), 2, 1);
    assertCell(CellLabelCompiler.compile("   a    "), 2, 1);
    assertCell(CellLabelCompiler.compile("|"), 2, 2);
    assertCell(CellLabelCompiler.compile("||"), 2, 3);
    assertCell(CellLabelCompiler.compile("<P0>||"), 2, 3);
    assertCell(CellLabelCompiler.compile("   |    "), 2, 2);
    assertCell(CellLabelCompiler.compile("   |  |   "), 2, 3);
    assertCell(CellLabelCompiler.compile("   |  |"), 2, 3);
    assertCell(CellLabelCompiler.compile("{}"), 3, 1);
    assertCell(CellLabelCompiler.compile("  {}   "), 3, 1);
    assertCell(CellLabelCompiler.compile("  {   } "), 3, 1);
    assertCell(CellLabelCompiler.compile("  {   }"), 3, 1);
    assertCell(CellLabelCompiler.compile("  {{   }}} "), 4, 1);
    assertCell(CellLabelCompiler.compile("{   }"), 3, 1);
    assertCell(CellLabelCompiler.compile("1||"), 2, 3);
    assertCell(CellLabelCompiler.compile("1\\||"), 2, 2);
    assertCell(CellLabelCompiler.compile("1\\|\\|"), 2, 1);
    assertCell(CellLabelCompiler.compile("1\\\\|\\|"), 2, 2);
    assertCell(CellLabelCompiler.compile("1\\ \\|\\|"), 2, 1);
    assertCell(CellLabelCompiler.compile("1\\ \\|\\ |"), 2, 2);
    assertCell(CellLabelCompiler.compile("1\\|\\  |"), 2, 2);
    assertCell(CellLabelCompiler.compile("1  \\ |\\  |"), 2, 3);
    assertCell(CellLabelCompiler.compile("1  \\ |\\  | "), 2, 3);
    assertCell(CellLabelCompiler.compile("|||"), 2, 4);
    assertCell(CellLabelCompiler.compile("|||\\ "), 2, 4);
    assertCell(CellLabelCompiler.compile("|1||<*>2||"), 2, 6, newMap("2", "*"));
    assertCell(CellLabelCompiler.compile("|1||<&&>\\<*\\>2||"), 2, 6,
                    newMap("2", null, "<*>2", "&&"));
    assertCell(CellLabelCompiler.compile("1|{<P0>2}}|3"), 3, 3,
                    newMap("2", "P0"));
    assertCell(CellLabelCompiler.compile("|1||2||{}"), 3, 6);
    assertCell(CellLabelCompiler.compile("|1||2||{|}"), 3, 7);
    assertCell(CellLabelCompiler.compile("|1||2||{|}}"), 3, 7);
    assertCell(CellLabelCompiler.compile(" | 1| |2| |{ |} }  "), 3, 7);
    assertCell(CellLabelCompiler.compile("123"), 2, 1);
    assertCell(CellLabelCompiler.compile("1|{{2| 3    11 }|4}|5|"), 4, 6);
    assertCell(CellLabelCompiler.compile("{<p1>1|{{<p2>2|<p3>3}|<p4>4}|<p5>5|}"), 5, 6,
                    newMap("1", "p1", "2", "p2", "3", "p3", "4", "p4", "5", "p5"));
    assertCell(CellLabelCompiler.compile("{|1|{{2|3}|4}|5|}"), 5, 7);
    assertCell(CellLabelCompiler.compile("1|{{{2|3}|4}|5|}"), 5, 6);
    assertCell(CellLabelCompiler.compile("1|{{{2|3}|4}|5|}}}}}}"), 5, 6);
    assertCell(CellLabelCompiler.compile("1|{{{2|3}|4}|5|}}}}} } "), 5, 6);
    assertCell(CellLabelCompiler.compile("1|{{{2|33333\n33n\n3}|4}|5|}}}} } }"), 5, 6);
    assertCell(CellLabelCompiler.compile("<f0> 0x10ba8| <f1>"), 2, 2);
    Assertions.assertThrows(LabelFormatException.class, () -> CellLabelCompiler.compile(null));
    Assertions.assertThrows(LabelFormatException.class, () -> CellLabelCompiler.compile("\\"));
    Assertions.assertThrows(LabelFormatException.class, () -> CellLabelCompiler.compile("||\\"));
    Assertions.assertThrows(LabelFormatException.class, () -> CellLabelCompiler.compile("||2\\"));
    Assertions.assertThrows(LabelFormatException.class, () -> CellLabelCompiler.compile("1|{2"));
    Assertions.assertThrows(LabelFormatException.class, () -> CellLabelCompiler.compile("1|2{2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|<oo>{2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{<P2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{P>2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{\\<P>2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{<P\\>2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{<P|>2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{<P|2>2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{<<P2>>2}}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|2{2|3}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{{{{2|3}|4}|5|}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|11|3|2|5|{6|}}888"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|11|3|2|5|{6|}7}"));
    Assertions.assertThrows(LabelFormatException.class,
                            () -> CellLabelCompiler.compile("1|{{2|3}|4\\}|5|"));
  }

  private void assertCell(RootCell cell, int depth, int cellNum) {
    assertCell(cell, depth, cellNum, null);
  }

  private void assertCell(RootCell cell, int depth, int cellNum,
                               Map<String, String> labelIdMap) {
    int[] cn = {0};
    Assertions.assertEquals(depth, dfs(cell, cell, cn, labelIdMap));
    Assertions.assertEquals(cellNum, cn[0]);
  }


  private int dfs(RootCell rootCell, Cell Cell, int[] cellNum,
                  Map<String, String> labelIdMap) {
    int max = 0;
    if (Cell.isLeaf()) {
      if (labelIdMap != null) {
        String id = labelIdMap.get(Cell.getLabel());
        if (id != null) {
          Assertions.assertEquals(Cell, rootCell.getCellById(id));
        }
        Assertions.assertEquals(id, Cell.getId());
      }
      cellNum[0]++;
    }
    for (Cell child : Cell.getChildren()) {
      max = Math.max(max, dfs(rootCell, child, cellNum, labelIdMap));
    }
    return max + 1;
  }

  private Map<String, String> newMap(String... entries) {
    if (entries == null || entries.length == 0) {
      return null;
    }

    Map<String, String> map = new HashMap<>((entries.length + 1) / 2);
    for (int i = 0; i < entries.length; i += 2) {
      map.put(entries[i], entries[i + 1]);
    }

    return map;
  }
}
