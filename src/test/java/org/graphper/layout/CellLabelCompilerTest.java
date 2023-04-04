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
import java.util.List;
import java.util.Map;
import org.graphper.layout.CellLabelCompiler;
import org.graphper.layout.LabelFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.graphper.def.FlatPoint;
import org.graphper.layout.CellLabelCompiler.LabelCell;
import org.graphper.layout.CellLabelCompiler.RootCell;

public class CellLabelCompilerTest {

  @Test
  public void testCompile() throws LabelFormatException {
    assertLabelCell(CellLabelCompiler.compile(""), 2, 1);
    assertLabelCell(CellLabelCompiler.compile(" "), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("      "), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("   a"), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("   a    "), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("|"), 2, 2);
    assertLabelCell(CellLabelCompiler.compile("||"), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("<P0>||"), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("   |    "), 2, 2);
    assertLabelCell(CellLabelCompiler.compile("   |  |   "), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("   |  |"), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("{}"), 3, 1);
    assertLabelCell(CellLabelCompiler.compile("  {}   "), 3, 1);
    assertLabelCell(CellLabelCompiler.compile("  {   } "), 3, 1);
    assertLabelCell(CellLabelCompiler.compile("  {   }"), 3, 1);
    assertLabelCell(CellLabelCompiler.compile("  {{   }}} "), 4, 1);
    assertLabelCell(CellLabelCompiler.compile("{   }"), 3, 1);
    assertLabelCell(CellLabelCompiler.compile("1||"), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("1\\||"), 2, 2);
    assertLabelCell(CellLabelCompiler.compile("1\\|\\|"), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("1\\\\|\\|"), 2, 2);
    assertLabelCell(CellLabelCompiler.compile("1\\ \\|\\|"), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("1\\ \\|\\ |"), 2, 2);
    assertLabelCell(CellLabelCompiler.compile("1\\|\\  |"), 2, 2);
    assertLabelCell(CellLabelCompiler.compile("1  \\ |\\  |"), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("1  \\ |\\  | "), 2, 3);
    assertLabelCell(CellLabelCompiler.compile("|||"), 2, 4);
    assertLabelCell(CellLabelCompiler.compile("|||\\ "), 2, 4);
    assertLabelCell(CellLabelCompiler.compile("|1||<*>2||"), 2, 6, newMap("2", "*"));
    assertLabelCell(CellLabelCompiler.compile("|1||<&&>\\<*\\>2||"), 2, 6,
                    newMap("2", null, "<*>2", "&&"));
    assertLabelCell(CellLabelCompiler.compile("1|{<P0>2}}|3"), 3, 3,
                    newMap("2", "P0"));
    assertLabelCell(CellLabelCompiler.compile("|1||2||{}"), 3, 6);
    assertLabelCell(CellLabelCompiler.compile("|1||2||{|}"), 3, 7);
    assertLabelCell(CellLabelCompiler.compile("|1||2||{|}}"), 3, 7);
    assertLabelCell(CellLabelCompiler.compile(" | 1| |2| |{ |} }  "), 3, 7);
    assertLabelCell(CellLabelCompiler.compile("123"), 2, 1);
    assertLabelCell(CellLabelCompiler.compile("1|{{2| 3    11 }|4}|5|"), 4, 6);
    assertLabelCell(CellLabelCompiler.compile("{<p1>1|{{<p2>2|<p3>3}|<p4>4}|<p5>5|}"), 5, 6,
                    newMap("1", "p1", "2", "p2", "3", "p3", "4", "p4", "5", "p5"));
    assertLabelCell(CellLabelCompiler.compile("{|1|{{2|3}|4}|5|}"), 5, 7);
    assertLabelCell(CellLabelCompiler.compile("1|{{{2|3}|4}|5|}"), 5, 6);
    assertLabelCell(CellLabelCompiler.compile("1|{{{2|3}|4}|5|}}}}}}"), 5, 6);
    assertLabelCell(CellLabelCompiler.compile("1|{{{2|3}|4}|5|}}}}} } "), 5, 6);
    assertLabelCell(CellLabelCompiler.compile("1|{{{2|33333\n33n\n3}|4}|5|}}}} } }"), 5, 6);
    assertLabelCell(CellLabelCompiler.compile("<f0> 0x10ba8| <f1>"), 2, 2);
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

  @Test
  public void testAlignSize() {
    RootCell cell = CellLabelCompiler.compile("1|{2|{3|4}}|5", null, 32,
                                              null, new FlatPoint(180, 120), false);
    List<LabelCell> children = cell.getChildren();
    Assertions.assertEquals(3, children.size());

    LabelCell first = children.get(0);
    LabelCell second = children.get(1);
    LabelCell third = children.get(2);

    Assertions.assertEquals(new FlatPoint(0, 0), first.offset);

    Assertions.assertEquals(34, first.getWidth(), 2);
    Assertions.assertEquals(180, first.getHeight(), 2);

    Assertions.assertEquals(52, second.getWidth(), 2);
    Assertions.assertEquals(180, second.getHeight(), 2);

    Assertions.assertEquals(34, third.getWidth(), 2);
    Assertions.assertEquals(180, third.getHeight(), 2);

    Assertions.assertEquals(2, second.childrenSize());
    Assertions.assertEquals(2, second.getChild(1).childrenSize());

    children = second.getChildren();
    first = children.get(0);
    second = children.get(1).getChild(0);
    third = children.get(1).getChild(1);

    Assertions.assertEquals(52, first.getWidth(), 2);
    Assertions.assertEquals(90, first.getHeight(), 2);

    Assertions.assertEquals(26, second.getWidth(), 2);
    Assertions.assertEquals(90, second.getHeight(), 2);

    Assertions.assertEquals(26, third.getWidth(), 2);
    Assertions.assertEquals(90, third.getHeight(), 2);
  }

  private void assertLabelCell(RootCell cell, int depth, int cellNum) {
    assertLabelCell(cell, depth, cellNum, null);
  }

  private void assertLabelCell(RootCell cell, int depth, int cellNum,
                               Map<String, String> labelIdMap) {
    int[] cn = {0};
    Assertions.assertEquals(depth, dfs(cell, cell, cn, labelIdMap));
    Assertions.assertEquals(cellNum, cn[0]);
  }


  private int dfs(RootCell rootCell, LabelCell labelCell, int[] cellNum,
                  Map<String, String> labelIdMap) {
    int max = 0;
    if (labelCell.isLeaf()) {
      if (labelIdMap != null) {
        String id = labelIdMap.get(labelCell.getLabel());
        if (id != null) {
          Assertions.assertEquals(labelCell, rootCell.getCellById(id));
        }
        Assertions.assertEquals(id, labelCell.getId());
      }
      cellNum[0]++;
    }
    for (LabelCell child : labelCell.getChildren()) {
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
