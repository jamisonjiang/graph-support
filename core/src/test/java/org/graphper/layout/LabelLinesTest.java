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

import java.util.List;
import org.graphper.layout.LabelLines.Alignment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LabelLinesTest {

  @Test
  public void alignmentAwareBreaks() {
    String label = "left" + LabelLines.LEFT_BREAK
        + "center\nright" + LabelLines.RIGHT_BREAK;
    List<LabelLines.Line> lines = LabelLines.parse(label);

    Assertions.assertEquals(3, lines.size());
    Assertions.assertEquals("left", lines.get(0).getText());
    Assertions.assertEquals(Alignment.LEFT, lines.get(0).getAlignment());
    Assertions.assertEquals(Alignment.CENTER, lines.get(1).getAlignment());
    Assertions.assertEquals(Alignment.RIGHT, lines.get(2).getAlignment());
    Assertions.assertEquals("left\ncenter\nright\n", LabelLines.plainText(label));
  }
}
