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

package org.graphper.draw.common;

import org.graphper.api.FileType;
import org.graphper.draw.DefaultPipelineFactory;
import org.graphper.draw.DrawBoard;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.PipelineFactory;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgRenderEngine;

public class CommonRenderEngine extends SvgRenderEngine {

  private static final CommonRenderEngine instance;

  static {
    instance = new CommonRenderEngine(new DefaultPipelineFactory());
  }

  public static CommonRenderEngine getInstance() {
    return instance;
  }

  protected CommonRenderEngine(PipelineFactory pipelineFactory) {
    super(pipelineFactory);
  }

  @Override
  protected DrawBoard<SvgBrush, SvgBrush, SvgBrush, SvgBrush> drawBoard(DrawGraph drawGraph) {
    CommonDrawBoard drawBoard = new CommonDrawBoard(drawGraph);
    Object attach = drawGraph.getAttach();
    if (attach instanceof FileType) {
      drawBoard.setImageType((FileType) attach);
    }
    return drawBoard;
  }
}
