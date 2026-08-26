/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
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
