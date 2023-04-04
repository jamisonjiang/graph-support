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

package org.graphper.layout.dot;

import java.util.List;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.ShifterStrategy;
import org.graphper.layout.dot.DotAttachment;
import org.graphper.layout.dot.DotLayoutEngine;

public class TLayout extends DotLayoutEngine {

  DotAttachment dotAttachment;

  @Override
  public void layout(DrawGraph drawGraph, Object attachment) {
    this.dotAttachment = (DotAttachment) attachment;
  }

  @Override
  public List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph) {
    return super.shifterStrategies(drawGraph);
  }

  @Override
  protected void afterRenderShifter(Object drawGraph) {
  }
}