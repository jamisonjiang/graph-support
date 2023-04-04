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
import org.graphper.util.Asserts;

/**
 * {@link RenderEngine} based on chained pipeline rendering.
 *
 * @param <NB> node brush type
 * @param <LB> line brush type
 * @param <CB> cluster brush type
 * @param <GB> graphviz brush type
 * @author Jamison Jiang
 */
@SuppressWarnings("all")
public abstract class PipelineRenderEngine<
    NB extends Brush,
    LB extends Brush,
    CB extends Brush,
    GB extends Brush>
    extends AbstractRenderEngine
    implements RenderEngine {

  protected volatile List<NodeEditor<NB>> nodeEditors;

  protected volatile List<LineEditor<LB>> lineEditors;

  protected volatile List<ClusterEditor<CB>> clusterEditors;

  protected volatile List<GraphEditor<GB>> graphEditors;

  protected final PipelineFactory pipelineFactory;

  protected PipelineRenderEngine(PipelineFactory pipelineFactory) {
    Asserts.nullArgument(pipelineFactory, "pipelineFactory");
    this.pipelineFactory = pipelineFactory;
  }

  @Override
  public GraphResource render0(DrawGraph drawGraph) throws ExecuteException {
    Asserts.nullArgument(drawGraph, "drawGraph");

    GraphPipelineTrigger<GB, GraphEditor<GB>> graphExecutePipeline = pipelineFactory
        .graphExecutePipeline(graphEditors(), drawGraph);
    ClusterPipelineTrigger<CB, ClusterEditor<CB>> clusterExecutePipeline = pipelineFactory
        .clusterExecutePipeline(clusterEditors(), drawGraph);
    NodePipelineTrigger<NB, NodeEditor<NB>> nodeExecutePipeline = pipelineFactory
        .nodeExecutePipeline(nodeEditors(), drawGraph);
    LinePipelineTrigger<LB, LineEditor<LB>> lineExecutePipeline = pipelineFactory
        .lineExecutePipeline(lineEditors(), drawGraph);

    try {
      // Triggle all editors of graph items
      DrawBoard<NB, LB, CB, GB> board = drawBoard(drawGraph);
      graphExecutePipeline.trigger(board::drawGraph);
      clusterExecutePipeline.trigger(board::drawCluster);
      nodeExecutePipeline.trigger(board::drawNode);
      lineExecutePipeline.trigger(board::drawLine);

      return board.graphResource();
    } catch (FailInitResourceException ex) {
      throw new ExecuteException(ex);
    }
  }

  private synchronized List<NodeEditor<NB>> nodeEditors() {
    if (nodeEditors == null) {
      nodeEditors = initNodeEditors();
    }

    return nodeEditors;
  }

  private synchronized List<LineEditor<LB>> lineEditors() {
    if (lineEditors == null) {
      lineEditors = initLineEditors();
    }

    return lineEditors;
  }

  private synchronized List<ClusterEditor<CB>> clusterEditors() {
    if (clusterEditors == null) {
      clusterEditors = initClusterEditors();
    }

    return clusterEditors;
  }

  private synchronized List<GraphEditor<GB>> graphEditors() {
    if (graphEditors == null) {
      graphEditors = initGraphEditors();
    }

    return graphEditors;
  }

  protected abstract List<NodeEditor<NB>> initNodeEditors();

  protected abstract List<LineEditor<LB>> initLineEditors();

  protected abstract List<ClusterEditor<CB>> initClusterEditors();

  protected abstract List<GraphEditor<GB>> initGraphEditors();

  protected abstract DrawBoard<NB, LB, CB, GB> drawBoard(DrawGraph drawGraph);
}
