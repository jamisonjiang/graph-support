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

package org.graphper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HeadlessDefaultTest {

  @Test
  void cliDefaultsToHeadlessWithoutOverridingUiOrExplicitSettings() {
    String previous = System.getProperty("java.awt.headless");
    try {
      for (String[] args : new String[][]{null, {}, {"-h"}, {"input.dot", "-Tpng"}}) {
        System.clearProperty("java.awt.headless");
        Main.configureHeadless(args);
        Assertions.assertEquals("true", System.getProperty("java.awt.headless"));
      }
      for (String keyword : new String[]{"ui", "--ui", "UI"}) {
        System.clearProperty("java.awt.headless");
        Main.configureHeadless(new String[]{keyword});
        Assertions.assertNull(System.getProperty("java.awt.headless"));
      }
      for (String value : new String[]{"true", "false"}) {
        for (String[] args : new String[][]{{"input.dot"}, {"ui"}}) {
          System.setProperty("java.awt.headless", value);
          Main.configureHeadless(args);
          Assertions.assertEquals(value, System.getProperty("java.awt.headless"));
        }
      }
    } finally {
      if (previous == null) {
        System.clearProperty("java.awt.headless");
      } else {
        System.setProperty("java.awt.headless", previous);
      }
    }
  }
}
