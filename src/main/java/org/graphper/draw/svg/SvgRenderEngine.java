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

package org.graphper.draw.svg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.graphper.draw.svg.cluster.ClusterBorderEditor;
import org.graphper.draw.svg.cluster.ClusterColorEditor;
import org.graphper.draw.svg.cluster.ClusterHrefEditor;
import org.graphper.draw.svg.cluster.ClusterLabelEditor;
import org.graphper.draw.svg.cluster.ClusterStyleEditor;
import org.graphper.draw.svg.graphviz.GraphBasicEditor;
import org.graphper.draw.svg.graphviz.GraphGridEditor;
import org.graphper.draw.svg.graphviz.GraphLabelEditor;
import org.graphper.draw.svg.line.LineFloatLabelsEditor;
import org.graphper.draw.svg.node.NodeColorEditor;
import org.graphper.draw.svg.node.NodeHrefEditor;
import org.graphper.draw.svg.node.NodeImageEditor;
import org.graphper.draw.svg.node.NodeLabelEditor;
import org.graphper.draw.svg.node.NodeStyleEditor;
import org.graphper.layout.FlatShifterStrategy;
import org.graphper.layout.ShifterStrategy;
import org.graphper.draw.ClusterEditor;
import org.graphper.draw.DefaultPipelineFactory;
import org.graphper.draw.DrawBoard;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.GraphEditor;
import org.graphper.draw.LineEditor;
import org.graphper.draw.NodeEditor;
import org.graphper.draw.PipelineFactory;
import org.graphper.draw.PipelineRenderEngine;
import org.graphper.draw.svg.line.LineArrowEditor;
import org.graphper.draw.svg.line.LineBoxesEditor;
import org.graphper.draw.svg.line.LineControlPointsEditor;
import org.graphper.draw.svg.line.LineHrefEditor;
import org.graphper.draw.svg.line.LineLabelEditor;
import org.graphper.draw.svg.line.LinePathEditor;
import org.graphper.draw.svg.line.LineStyleEditor;
import org.graphper.draw.svg.node.NodeShapeEditor;

/**
 * Svg rendering engine.
 *
 * @author Jamison Jiang
 */
public class SvgRenderEngine extends
    PipelineRenderEngine<SvgBrush, SvgBrush, SvgBrush, SvgBrush> {

  private static final SvgRenderEngine svgRenderEngine;

  static {
    svgRenderEngine = new SvgRenderEngine(
        new DefaultPipelineFactory()
    );
  }

  private SvgRenderEngine(PipelineFactory pipelineFactory) {
    super(pipelineFactory);
  }

  public static SvgRenderEngine getInstance() {
    return svgRenderEngine;
  }

  @Override
  protected List<NodeEditor<SvgBrush>> initNodeEditors() {
    return Arrays.asList(
        new NodeHrefEditor(),
        new NodeShapeEditor(),
        new NodeLabelEditor(),
        new NodeStyleEditor(),
        new NodeColorEditor(),
        new NodeImageEditor()
    );
  }

  @Override
  protected List<LineEditor<SvgBrush>> initLineEditors() {
    return Arrays.asList(
        new LineHrefEditor(),
        new LinePathEditor(),
        new LineArrowEditor(),
        new LineStyleEditor(),
        new LineLabelEditor(),
        new LineBoxesEditor(),
        new LineFloatLabelsEditor(),
        new LineControlPointsEditor()
    );
  }

  @Override
  protected List<ClusterEditor<SvgBrush>> initClusterEditors() {
    return Arrays.asList(
        new ClusterHrefEditor(),
        new ClusterBorderEditor(),
        new ClusterLabelEditor(),
        new ClusterColorEditor(),
        new ClusterStyleEditor()
    );
  }

  @Override
  protected List<GraphEditor<SvgBrush>> initGraphEditors() {
    return Arrays.asList(
        new GraphBasicEditor(),
        new GraphLabelEditor(),
        new GraphGridEditor()
    );
  }

  @Override
  protected DrawBoard<SvgBrush, SvgBrush, SvgBrush, SvgBrush> drawBoard(DrawGraph drawGraph) {
    return new SvgDrawBoard(drawGraph);
  }

  @Override
  public List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph) {
    return Collections.singletonList(
        new FlatShifterStrategy(-drawGraph.getMinX(), -drawGraph.getMinY())
    );
  }
}
