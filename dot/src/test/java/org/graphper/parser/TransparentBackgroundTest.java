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

package org.graphper.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransparentBackgroundTest {

  @Test
  public void transparentBgcolor() throws Exception {
    String svg = DotParser.parse("digraph { bgcolor=transparent; a -> b }").toSvgStr();
    Assertions.assertTrue(svg.contains("class=\"background\""));
    Assertions.assertTrue(svg.contains("fill=\"none\""));
  }

  @Test
  public void transparentStyleAlias() throws Exception {
    String svg = DotParser.parse("digraph { style=transparent; a -> b }").toSvgStr();
    Assertions.assertTrue(svg.contains("class=\"background\""));
    Assertions.assertTrue(svg.contains("fill=\"none\""));
  }
}
