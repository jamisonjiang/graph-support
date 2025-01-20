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

/**
 * Interface for selecting system fonts based on specific strategies. The default font selection
 * depends on system-available fonts and custom strategies that can be implemented and registered
 * using the SPI.
 *
 * @author Jamison Jiang
 */
public interface FontSelector extends EnvStrategy {

  /**
   * Returns default font name when not set fontName attribute.
   *
   * @return default font name
   */
  String defaultFont();

  /**
   * Return true if font exists in system.
   *
   * @param fontName font name
   * @return true if font exists in system
   */
  default boolean exists(String fontName) {
    return true;
  }

  /**
   * Checks whether the specified font supports rendering a given character.
   *
   * @param fontName the name of the font to check
   * @param c        the character to check support for
   * @return {@code true} if the font supports the character, {@code false} otherwise
   */
  default boolean fontSupport(String fontName, char c) {
    return true;
  }

  /**
   * Finds the first font that supports rendering a given character. If no suitable font is found,
   * this method falls back to the default font.
   *
   * @param c the character to check for font support
   * @return the name of the first font that supports the character
   */
  default String findFirstSupportFont(char c) {
    return defaultFont();
  }
}
