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

import org.graphper.def.FlatPoint;

/**
 * Graph-support needs a way to detect the text size. The basic elements that affect the text length
 * are: specific text, font size, font, etc., which usually have different implementations according
 * to the specific environment of the relying on party.
 *
 * <p>By default, {@link AWTMeasureText} will be used as the measurement method. If it is found
 * that the current environment does not support {@code java.awt}, a very rough measurement method
 * {@link RoughMeasureText} will be used. If you have a better way, please implement this interface
 * and register the corresponding implementation in <a
 * href="https://docs.oracle.com/cd/F32325_01/doc.192/f32328/c_payments_spi.htm#SIMCG-TheSimphonyPaymentInterfaceSPI-DA817CDC">SPI</a>.
 *
 * @author Jamison Jiang
 * @see AWTMeasureText
 * @see RoughMeasureText
 */
public interface MeasureText {

  /**
   * When there are multiple available implementations, use this attribute to sort, and the one with
   * the smaller value will be used first, and if they are the same, one will be randomly selected
   * for use.
   *
   * @return <tt>true</tt> if the priority of the current measurement method
   */
  int order();

  /**
   * Returns whether the current environment supports the corresponding measurement method.
   *
   * @return <tt>true</tt> if current environment support
   */
  boolean envSupport();

  /**
   * Calculate the actual size of the label container based on the font and size of the label.
   *
   * @param text     text content
   * @param fontName font name
   * @param fontSize font size
   * @return label size
   */
  FlatPoint measure(String text, String fontName, double fontSize);
}
