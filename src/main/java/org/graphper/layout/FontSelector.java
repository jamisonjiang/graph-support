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
 * Default font name dependent on system fonts, should choose by some strategies. Use <a
 * href="https://docs.oracle.com/cd/F32325_01/doc.192/f32328/c_payments_spi.htm#SIMCG-TheSimphonyPaymentInterfaceSPI-DA817CDC">SPI</a>
 * to add strategy.
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
}
