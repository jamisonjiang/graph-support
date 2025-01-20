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

package org.graphper.api.attributes;

import org.graphper.api.Graphviz;

/**
 * Line type for drawing arrowheads, indicates which ends of the edge should be decorated with an
 * arrowhead. It will only take effect under {@link Graphviz#digraph()}.
 *
 * @author Jamison Jiang
 */
public enum Dir {

  /**
   * The directed line from T to H, draw as <tt>T -&gt; H</tt>.
   */
  FORWARD,

  /**
   * The directed line from T to H, draw as <tt>T &lt;- H</tt>.
   */
  BACK,

  /**
   * The directed line from T to H, draw as <tt>T &lt;-&gt; H</tt>.
   */
  BOTH,

  /**
   * The directed line from T to H, draw as <tt>T - H</tt>.
   */
  NONE;
}
