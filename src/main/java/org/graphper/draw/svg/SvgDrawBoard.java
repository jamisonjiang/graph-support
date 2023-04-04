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

import java.util.Objects;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.api.Cluster;
import org.graphper.api.GraphAttrs;
import org.graphper.api.Line;
import org.graphper.api.Node;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DrawBoard;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.GraphResource;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;

/**
 * Svg draw board.
 *
 * @author Jamison Jiang
 */
public class SvgDrawBoard implements DrawBoard<SvgBrush, SvgBrush, SvgBrush, SvgBrush> {

  public static final String GRAPH_ROOT = "graph_root";

  private static final String VIEWBOX_VAL = "0.00 0.00 %s %s";

  private static final String XMLNS_VAL = "http://www.w3.org/2000/svg";

  private static final String XMLNS_XLINK_VAL = "http://www.w3.org/1999/xlink";

  private final SvgDocument svgDocument;

  private final Element graphElement;

  private final DrawGraph drawGraph;

  public SvgDrawBoard(DrawGraph drawGraph) {
    Asserts.nullArgument(drawGraph, "DrawGraph");
    this.drawGraph = drawGraph;

    double width = drawGraph.width();
    double height = drawGraph.height();
    FlatPoint scale = drawGraph.getGraphviz().graphAttrs().getScale();
    if (scale != null) {
      width *= scale.getX();
      height *= scale.getY();
    }

    svgDocument = new SvgDocument();
    Element svg = svgDocument.createElement(SvgConstants.SVG_ELE);
    svg.setAttribute(SvgConstants.XMLNS, XMLNS_VAL);
    svg.setAttribute(SvgConstants.XMLNS_XLINK, XMLNS_XLINK_VAL);
    svg.setAttribute(SvgConstants.HEIGHT, height + SvgConstants.PT);
    svg.setAttribute(SvgConstants.WIDTH, width + SvgConstants.PT);
    svg.setAttribute(SvgConstants.VIEWBOX, String.format(VIEWBOX_VAL, width, height));

    graphElement = svg.createChildElement(SvgConstants.G_ELE);
    graphElement.setAttribute(SvgConstants.ID, GRAPH_ROOT);
    graphElement.setAttribute(SvgConstants.CLASS, "graph");
  }

  @Override
  public synchronized SvgBrush drawGraph(GraphvizDrawProp graphvizDrawProp) {
    String transform = SvgConstants.TRANSFORM_VAL;
    FlatPoint scale = graphvizDrawProp.getGraphviz().graphAttrs().getScale();
    if (scale == null) {
      transform = String.format(transform, "1 1", "%s");
    } else {
      String scaleStr = scale.getX() + SvgConstants.SPACE + scale.getY();
      transform = String.format(transform, scaleStr, "%s");
    }

    transform = String.format(transform, "0");
    graphElement.setAttribute(SvgConstants.TRANSFORM, transform);

    Element element = graphElement.createChildElement(SvgConstants.G_ELE);
    element.setAttribute(SvgConstants.ID, SvgConstants.GRAPH + SvgConstants.UNDERSCORE + "0");
    element.setAttribute(SvgConstants.CLASS, SvgConstants.GRAPH);

    return new SvgBrush(element, svgDocument, this);
  }

  @Override
  public synchronized SvgBrush drawCluster(ClusterDrawProp cluster) {
    Element element = graphElement.createChildElement(SvgConstants.G_ELE);
    element.setAttribute(SvgConstants.ID, clusterId(cluster));
    element.setAttribute(SvgConstants.CLASS, SvgConstants.CLUSTER);
    return new SvgBrush(element, svgDocument, this);
  }

  @Override
  public synchronized SvgBrush drawNode(NodeDrawProp nodeDrawProp) {
    Element element = graphElement.createChildElement(SvgConstants.G_ELE);
    element.setAttribute(SvgConstants.ID, nodeId(nodeDrawProp));
    element.setAttribute(SvgConstants.CLASS, SvgConstants.NODE);
    return new SvgBrush(element, svgDocument, this);
  }

  @Override
  public synchronized SvgBrush drawLine(LineDrawProp line) {
    Element element = graphElement.createChildElement(SvgConstants.G_ELE);
    element.setAttribute(SvgConstants.ID, lineId(line));
    return new SvgBrush(element, svgDocument, this);
  }

  @Override
  public boolean removeNode(Node node) {
    return svgDocument.removeEle(nodeId(node));
  }

  @Override
  public boolean removeLine(Line line) {
    return svgDocument.removeEle(lineId(line));
  }

  @Override
  public synchronized GraphResource graphResource() {
    String label = drawGraph.getGraphviz().graphAttrs().getLabel();
    return new SvgGraphResource(label != null ? label : "graphviz", svgDocument.toXml());
  }

  public GraphAttrs graphAttrs() {
    return drawGraph.getGraphviz().graphAttrs();
  }

  public DrawGraph drawGraph() {
    return drawGraph;
  }

  public String clusterId(ClusterDrawProp clusterDrawProp) {
    Objects.requireNonNull(clusterDrawProp);
    return clusterId(clusterDrawProp.getCluster());
  }

  public String nodeId(NodeDrawProp nodeDrawProp) {
    Objects.requireNonNull(nodeDrawProp);
    return nodeId(nodeDrawProp.getNode());
  }

  public String lineId(LineDrawProp lineDrawProp) {
    Objects.requireNonNull(lineDrawProp);
    return lineId(lineDrawProp.getLine());
  }

  public String clusterId(Cluster cluster) {
    Objects.requireNonNull(cluster);
    return drawGraph.clusterId(cluster);
  }

  public String nodeId(Node node) {
    Objects.requireNonNull(node);
    return drawGraph.nodeId(node);
  }

  public String lineId(Line line) {
    Objects.requireNonNull(line);
    return drawGraph.lineId(line);
  }
}
