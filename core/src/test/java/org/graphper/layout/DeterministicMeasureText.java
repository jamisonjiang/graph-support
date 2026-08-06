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

import org.graphper.api.attributes.FontStyle;
import org.graphper.def.FlatPoint;

/**
 * Test-only {@link MeasureText} that always wins the SPI selection, so that any geometry derived
 * from text measurement is reproducible across operating systems and JDKs.
 *
 * <p>{@link AWTMeasureText} normally wins ({@code order() == 0}) but its results depend on the
 * fonts installed on the host and on the JDK's font rasterizer, which makes golden-file comparison
 * of cell geometry impossible. This implementation reuses the platform-independent arithmetic of
 * {@link RoughMeasureText} and registers with a negative order so it takes precedence in tests.
 *
 * <p>Because the measurement ignores {@code fontName}, tests also become independent of which
 * {@code FontSelector} the host picks for {@code FontUtils.DEFAULT_FONT}.
 */
public class DeterministicMeasureText implements MeasureText {

  private final RoughMeasureText delegate = new RoughMeasureText();

  @Override
  public int order() {
    return -1;
  }

  @Override
  public boolean envSupport() {
    return true;
  }

  @Override
  public FlatPoint measure(String text, String fontName, double fontSize, FontStyle... fontStyles) {
    return delegate.measure(text, fontName, fontSize, fontStyles);
  }
}
