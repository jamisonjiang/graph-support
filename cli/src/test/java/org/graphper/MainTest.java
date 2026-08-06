package org.graphper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

  @Test
  void recognizesExplicitUiCommands() {
    assertTrue(Main.isUiCommand(new String[]{"ui"}));
    assertTrue(Main.isUiCommand(new String[]{"--ui"}));
  }

  @Test
  void doesNotTreatNormalOrEmptyArgumentsAsUi() {
    assertFalse(Main.isUiCommand(new String[0]));
    assertFalse(Main.isUiCommand(new String[]{"input.dot", "-o", "output.svg"}));
  }
}
