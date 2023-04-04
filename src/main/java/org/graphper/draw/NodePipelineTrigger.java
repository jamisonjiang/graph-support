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

import java.util.List;

public class NodePipelineTrigger<B extends Brush, T extends NodeEditor<B>>
    extends AbstractPipelineTrigger<NodeDrawProp, B, T, NodePipelineTrigger<B, T>> {

  public NodePipelineTrigger(List<T> editors, DrawGraph drawGraph) {
    super(editors, drawGraph);
  }

  @Override
  public Iterable<NodeDrawProp> renderItems() {
    return drawGraph.nodes();
  }
}
