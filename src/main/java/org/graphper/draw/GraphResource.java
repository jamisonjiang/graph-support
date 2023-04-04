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

package org.graphper.draw;

import java.io.InputStream;

/**
 * After the graph is rendered, you can use the {@link #bytes()} or {@link #inputStream()} method to
 * read the content of the graph. Different rendering results are processed in different ways, and
 * different processing needs to be performed according to the corresponding type.
 *
 * @author Jamison Jiang
 */
public interface GraphResource {

  /**
   * Returns the byte array of the graph directly.
   *
   * @return byte array of graph
   */
  byte[] bytes();

  /**
   * Returns the {@link InputStream} of the graph.
   *
   * @return {@code InputStream} of graph
   */
  InputStream inputStream();

  /**
   * Returns the graph name.
   *
   * @return graph name
   */
  String name();

  /**
   * Returns the file suffix corresponding to the type.
   *
   * @return file suffix
   */
  String suffix();
}