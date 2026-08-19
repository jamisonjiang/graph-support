/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.layout;

import org.graphper.api.Assemble;
import org.graphper.api.Html;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HtmlConvertorTest {

  @Test
  public void fixedTableHonorsExplicitDimensions() {
    assertTableSize(55, 27);
    assertTableSize(52, 17);
    assertTableSize(8, 15);
    assertTableSize(6, 15);
  }

  @Test
  public void nonFixedDimensionsRemainMinimums() {
    Assemble small = HtmlConvertor.toAssemble(Html.table().width(6).height(8).tr(Html.td()));
    Assertions.assertTrue(small.getWidth() >= 6);
    Assertions.assertTrue(small.getHeight() >= 8);

    Assemble large = HtmlConvertor.toAssemble(Html.table().width(55).height(27).tr(Html.td()));
    Assertions.assertEquals(55, large.getWidth(), 0.000001);
    Assertions.assertEquals(27, large.getHeight(), 0.000001);
  }

  @Test
  public void tinyFixedTableKeepsPositiveCellGeometry() {
    Assemble assemble = HtmlConvertor.toAssemble(
        Html.table().fixedSize(true).width(1).height(1).tr(Html.td()));
    Assertions.assertEquals(1, assemble.getWidth(), 0.000001);
    Assertions.assertEquals(1, assemble.getHeight(), 0.000001);
    for (org.graphper.api.Node cell : assemble.getCells()) {
      Assertions.assertTrue(cell.nodeAttrs().getWidth() > 0);
      Assertions.assertTrue(cell.nodeAttrs().getHeight() > 0);
    }
  }

  private void assertTableSize(double width, double height) {
    Assemble assemble = HtmlConvertor.toAssemble(
        Html.table().fixedSize(true).width(width).height(height).tr(Html.td()));
    Assertions.assertEquals(width, assemble.getWidth(), 0.000001);
    Assertions.assertEquals(height, assemble.getHeight(), 0.000001);
  }
}
