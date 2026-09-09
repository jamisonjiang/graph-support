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
package org.graphper.api;

import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AttributeHashCompatibilityTest {

  @Test
  public void unsetRegularKeepsLegacyNodeHash() {
    NodeAttrs attrs = new NodeAttrs();
    int legacy = Objects.hash(attrs.id, attrs.height, attrs.width, attrs.color, attrs.fillColor,
                              attrs.fontColor, attrs.fontName, attrs.label, attrs.labelTag,
                              attrs.labeljust, attrs.labelloc, attrs.margin, attrs.shape,
                              attrs.fixedSize, attrs.fontSize, attrs.styles, attrs.fontStyles,
                              attrs.sides, attrs.href, attrs.tooltip, attrs.image, attrs.imageSize,
                              attrs.penWidth, attrs.table, attrs.recordTag, attrs.assemble);
    Assertions.assertEquals(legacy, attrs.hashCode());
  }

  @Test
  public void unsetDotEdgeAttrsKeepLegacyLineHash() {
    LineAttrs attrs = new LineAttrs();
    int legacy = Objects.hash(attrs.id, attrs.controlPoints, attrs.showboxes, attrs.arrowHead,
                              attrs.arrowTail, attrs.arrowSize, attrs.color, attrs.dir,
                              attrs.fontColor, attrs.fontSize, attrs.fontName, attrs.headclip,
                              attrs.tailclip, attrs.minlen, attrs.weight, attrs.label, attrs.labelTag,
                              attrs.styles, attrs.lhead, attrs.ltail, attrs.radian, attrs.tailPort,
                              attrs.headPort, attrs.tailCell, attrs.headCell, attrs.href,
                              attrs.tooltip, attrs.penWidth, attrs.table, attrs.assemble);
    legacy = 31 * legacy + Arrays.hashCode(attrs.floatLabels);
    Assertions.assertEquals(legacy, attrs.hashCode());
  }
}
