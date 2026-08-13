/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssembleTest {

  @Test
  public void distinguishesLegacyNodeIdFallbackFromExplicitMissingCellId() {
    Node legacy = Node.builder().id("legacy").build();
    Assemble legacyAssemble = Assemble.builder().addCell(0, 0, legacy).build();
    Assertions.assertEquals("legacy", legacyAssemble.cellId(legacy));

    Node htmlCell = Node.builder().id("scoped-internal-id").build();
    Assemble htmlAssemble = Assemble.builder().addCell(0, 0, null, htmlCell).build();
    Assertions.assertNull(htmlAssemble.cellId(htmlCell));
  }
}
