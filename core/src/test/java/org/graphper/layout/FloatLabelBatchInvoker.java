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

import java.util.ArrayList;
import java.util.List;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;

/**
 * Drives the package private float label batch of {@link LineClip} exactly the way
 * {@code AbstractLayoutEngine.LineClipProcessor#clipAllLines()} does.
 *
 * <p>Tests outside {@code org.graphper.layout} cannot reach that entry point — that is the whole
 * point of it being package private — so this helper stands in for the layout engine and lets such
 * a test verify that the engine's batch really dispatches through the overridable
 * {@link LineClip#setFloatLabel(org.graphper.draw.LineDrawProp)} hook.
 */
public final class FloatLabelBatchInvoker {

  private FloatLabelBatchInvoker() {
  }

  /**
   * Collects the endpoint labels of every given line into one batch and resolves them together.
   *
   * @param clip      the clip whose float label hook is exercised
   * @param drawGraph the graph the labels belong to
   * @param lines     the lines of the batch, in order
   */
  public static void runBatch(LineClip clip, DrawGraph drawGraph, Iterable<LineDrawProp> lines) {
    List<ExternalLabelPlacer.Placement> placements = new ArrayList<>();
    for (LineDrawProp line : lines) {
      clip.setFloatLabel(line, placements);
    }
    ExternalLabelPlacer.place(drawGraph, placements);
  }
}
