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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.graphper.api.Assemble;
import org.graphper.api.Cluster;
import org.graphper.api.ClusterAttrs;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer;
import org.graphper.api.Graphviz;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.FontStyle;
import org.graphper.api.attributes.Labeljust;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.def.FlatPoint;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.ContainerDrawProp;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.GraphvizDrawProp;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.RenderEngine;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.Asserts;
import org.graphper.util.ClassUtils;
import org.graphper.util.CollectionUtils;
import org.graphper.util.FontUtils;
import org.graphper.util.GraphvizUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout engine common template.
 *
 * @author Jamison Jiang
 */
public abstract class AbstractLayoutEngine implements LayoutEngine {

  private static final Logger log = LoggerFactory.getLogger(AbstractLayoutEngine.class);

  /**
   * Node default attribute value map.
   */
  private static final Map<String, Object> DEFAULT_NODE_ATTRS_MAP;

  /**
   * Node default attribute value map.
   */
  private static final Map<String, Object> DEFAULT_CELL_ATTRS_MAP;

  /**
   * Line default attribute value map.
   */
  private static final Map<String, Object> DEFAULT_LINE_ATTRS_MAP;

  static {
    try {
      DEFAULT_NODE_ATTRS_MAP = ClassUtils.propValMap(DefaultVal.DEFAULT_NODE_ATTRS);
      DEFAULT_CELL_ATTRS_MAP = ClassUtils.propValMap(DefaultVal.DEFAULT_CELL_ATTRS);
      DEFAULT_LINE_ATTRS_MAP = ClassUtils.propValMap(DefaultVal.DEFAULT_LINE_ATTRS);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to set default properties", e);
    }
  }

  public static void nodeLabelSet(NodeDrawProp nodeDrawProp, DrawGraph drawGraph,
                                  boolean needSetCenter) {
    if (nodeDrawProp == null || drawGraph == null) {
      return;
    }

    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getShape();
    FlatPoint labelCenter;
    if (Boolean.TRUE.equals(nodeDrawProp.nodeAttrs().getFixedSize())) {
      labelCenter = new FlatPoint(nodeDrawProp.getX(), nodeDrawProp.getY());
    } else {
      labelCenter = nodeShape.labelCenter(nodeDrawProp.getLabelSize(), nodeDrawProp);
    }

    double x = labelCenter.getX();
    double y = labelCenter.getY();

    Labelloc labelloc = nodeDrawProp.nodeAttrs().getLabelloc();
    if (labelloc != null && nodeDrawProp.getLabelSize() != null) {
      FlatPoint labelSize = nodeDrawProp.getLabelSize();
      x += nodeDrawProp.getLabelHorOffset();
      y += nodeDrawProp.getLabelVerOffset();

      if (!needSetCenter) {
        drawGraph.updateXAxisRange(x - labelSize.getWidth() / 2);
        drawGraph.updateXAxisRange(x + labelSize.getWidth() / 2);
        drawGraph.updateYAxisRange(y - labelSize.getWidth() / 2);
        drawGraph.updateYAxisRange(y + labelSize.getWidth() / 2);
      }
    }

    if (needSetCenter) {
      nodeDrawProp.setLabelCenter(new FlatPoint(x, y));
    }

    Assemble assemble = nodeDrawProp.getAssemble();
    if (assemble == null || nodeDrawProp.getLabelCenter() == null) {
      return;
    }

    setCellNodeOffset(drawGraph, labelCenter, assemble, false);
  }

  public static void setCellNodeOffset(DrawGraph drawGraph, FlatPoint labelCenter,
                                       Assemble assemble, boolean userLabelSize) {
    if (assemble == null || drawGraph == null || labelCenter == null) {
      return;
    }

    FlatPoint labelSize = assemble.size();
    for (Node cell : assemble.getCells()) {
      NodeDrawProp cellProp = drawGraph.getNodeDrawProp(cell);
      if (cellProp == null) {
        continue;
      }
      if (userLabelSize) {
        cellProp.initCellPos(labelSize, labelCenter, assemble);
      } else {
        cellProp.initCellPos();
      }
      drawGraph.updateXAxisRange(cellProp.getLeftBorder() - 5);
      drawGraph.updateXAxisRange(cellProp.getRightBorder() + 5);
      drawGraph.updateYAxisRange(cellProp.getUpBorder() - 5);
      drawGraph.updateYAxisRange(cellProp.getDownBorder() + 5);
      nodeLabelSet(cellProp, drawGraph, true);
    }
  }

  @Override
  public DrawGraph layout(Graphviz graphviz, RenderEngine renderEngine) throws ExecuteException {
    Asserts.nullArgument(graphviz, "Graphviz");
    Asserts.illegalArgument(graphviz.nodeNum() == 0, "Graphviz container is empty!");

    // Create DrawGraph and initialize some properties of GraphvizDrawProp.
    DrawGraph drawGraph = new DrawGraph(graphviz);
    LayoutAttach attachment = attachment(drawGraph);

    // Various id records
    Map<Node, Integer> nodeId = new HashMap<>(graphviz.nodeNum());
    Map<Line, Integer> lineId = new HashMap<>(graphviz.lineNum());
    Map<GraphContainer, Integer> clusterId = new HashMap<>(graphviz.clusters().size());

    /*
     * For each container it recurses into, get all nodes and lines, and initialize node and line
     * attributes, and finally initialize the attributes of the current container.
     */
    Consumer<GraphContainer> containerConsumer = c ->
        nodeLineClusterHandle(attachment, drawGraph, c, nodeId, lineId, clusterId);

    // Traverse all containers in depth.
    GraphvizUtils.dfs(
        Integer.MAX_VALUE,
        Boolean.FALSE,
        new HashSet<>(),
        null,
        graphviz,
        containerConsumer::accept,
        containerConsumer::accept,
        this::dfsNeedContinue
    );

    // Finally execute the root container (Graphviz).
    nodeLineClusterHandle(attachment, drawGraph, graphviz, nodeId, lineId, clusterId);

    // Graphviz handle
    handleGraphviz(attachment, nodeId, drawGraph);

    // The corresponding layout engine executes.
    layout(drawGraph, attachment);

    // Clusters scramble nodes can cause some clusters are actually empty in rendering
    removeEmptyCluster(drawGraph, attachment.getLayoutGraph());

    // Get all movement strategies in the layout engine and rendering engine, and perform element movement.
    moveGraph(drawGraph, renderEngine, attachment);
    return drawGraph;
  }

  /**
   * Returns the attachment carried by the layout engine.
   *
   * @param drawGraph draw graph object
   * @return attachment of layout
   */
  protected LayoutAttach attachment(DrawGraph drawGraph) {
    return null;
  }

  /**
   * Post-processing of nodes by the engine.
   *
   * @param node            node
   * @param attachment      layout attachment
   * @param drawGraph       draw graph object
   * @param parentContainer parent container of node
   */
  protected void consumerNode(Node node, LayoutAttach attachment, DrawGraph drawGraph,
                              GraphContainer parentContainer) {
  }

  /**
   * Post-processing of lines by the engine.
   *
   * @param line      line
   * @param attach    layout attachment
   * @param drawGraph draw graph object
   */
  protected void consumerLine(Line line, LayoutAttach attach, DrawGraph drawGraph) {
  }

  /**
   * Layout engine move post-processing.
   *
   * @param attach layout attachment
   */
  protected void afterLayoutShifter(LayoutAttach attach) {
  }

  /**
   * Renderer engine move post-processing.
   *
   * @param attach layout attachment
   */
  protected void afterRenderShifter(LayoutAttach attach) {
  }

  /**
   * Returns the measured label size.
   *
   * @param label      label
   * @param fontName   font name
   * @param fontSize   font size
   * @return label size
   */
  protected FlatPoint labelContainer(String label, String fontName, double fontSize) {
    return labelContainer(label, fontName, fontSize, null);
  }

  /**
   * Returns the measured label size.
   *
   * @param label      label
   * @param fontName   font name
   * @param fontSize   font size
   * @param fontStyles font styles
   * @return label size
   */
  protected FlatPoint labelContainer(String label, String fontName,
                                     double fontSize, Collection<FontStyle> fontStyles) {
    if (CollectionUtils.isNotEmpty(fontStyles)) {
      return FontUtils.measure(label, fontName, fontSize, 0,
                               fontStyles.toArray(new FontStyle[0]));
    } else {
      return FontUtils.measure(label, fontName, fontSize, 0);
    }
  }

  /**
   * Set the label positioning of {@link Graphviz} and {@link Cluster}.
   *
   * @param drawGraph draw graph object
   */
  protected void containerLabelPos(DrawGraph drawGraph) {
    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    containerLabelPos(graphvizDrawProp);

    for (ClusterDrawProp cluster : drawGraph.clusters()) {
      containerLabelPos(cluster);
    }
  }

  // -------------------------------- abstract method --------------------------------

  /**
   * Engine layout execute.
   *
   * @param drawGraph draw graph object
   * @param attach    layout attach
   */
  protected abstract void layout(DrawGraph drawGraph, LayoutAttach attach) throws ExecuteException;

  /**
   * The move strategy for the layout engine.
   *
   * @param drawGraph draw graph object
   * @return move strategy
   */
  protected abstract List<ShifterStrategy> shifterStrategies(DrawGraph drawGraph);

  // -------------------------------- private method --------------------------------

  private void handleGraphviz(LayoutAttach attach, Map<Node, Integer> nodeId, DrawGraph drawGraph) {
    GraphvizDrawProp graphvizDrawProp = drawGraph.getGraphvizDrawProp();
    Graphviz graphviz = graphvizDrawProp.getGraphviz();
    GraphAttrs graphAttrs = graphviz.graphAttrs();
    Assemble assemble = graphvizDrawProp.getAssemble();

    FlatPoint labelSize = null;
    if (assemble == null) {
      String label = graphAttrs.getLabel();
      if (label != null) {
        // Set label of graphviz
        labelSize = labelContainer(label, graphAttrs.getFontName(), graphAttrs.getFontSize());
      }
    } else {
      labelSize = assemble.size();
      assembleHandle(attach, drawGraph, null, nodeId, assemble);
    }

    graphvizDrawProp.setLabelSize(labelSize);
  }

  protected void containerLabelPos(ContainerDrawProp containerDrawProp) {
    FlatPoint labelSize = containerDrawProp.getLabelSize();
    if (labelSize == null) {
      return;
    }

    Labelloc labelloc = containerDrawProp.labelloc();
    Labeljust labeljust = containerDrawProp.labeljust();
    FlatPoint upperLeft = new FlatPoint(containerDrawProp.getLeftBorder(),
                                        containerDrawProp.getUpBorder());
    FlatPoint lowerRight = new FlatPoint(containerDrawProp.getRightBorder(),
                                         containerDrawProp.getDownBorder());

    // Adjust the position by Labelloc and Labeljust
    FlatPoint labelPoint = new FlatPoint(
        labeljust.getX(upperLeft, lowerRight, labelSize),
        labelloc.getY(upperLeft, lowerRight, labelSize)
    );
    containerDrawProp.setLabelCenter(labelPoint);
  }

  private boolean dfsNeedContinue(GraphContainer c) {
    return !c.isSubgraph() || c.isTransparent();
  }

  private void nodeLineClusterHandle(LayoutAttach attachment,
                                     DrawGraph drawGraph,
                                     GraphContainer container,
                                     Map<Node, Integer> nodeId,
                                     Map<Line, Integer> lineId,
                                     Map<GraphContainer, Integer> clusterId) {
    Iterable<Node> nodes;
    Iterable<Line> lines;
    /*
     * If dfs is not terminated in the current container, then only get the direct nodes and lines
     * of the current container, if it is terminated, get all the nodes and lines of the current
     * container and all its sub-containers.
     */
    if (dfsNeedContinue(container)) {
      nodes = container.directNodes();
      lines = container.directLines();
    } else {
      nodes = container.nodes();
      lines = container.lines();
    }

    // Handle all nodes
    for (Node node : nodes) {
      nodeHandle(attachment, drawGraph, container, nodeId, node, null, null, false, true, 0);
    }

    // Handle all lines
    for (Line line : lines) {
      // Handle the tail and head node
      nodeHandle(attachment, drawGraph, container, nodeId, line.head(), null, null, false, true, 0);
      nodeHandle(attachment, drawGraph, container, nodeId, line.tail(), null, null, false, true, 0);

      // Handle line
      lineHandle(attachment, drawGraph, container, lineId, nodeId, line);
    }

    // Handle all clusters
    if (container.isCluster()) {
      clusterHandle(attachment, drawGraph, (Cluster) container, nodeId, clusterId);
    }
  }

  private void nodeHandle(LayoutAttach attachment, DrawGraph drawGraph, GraphContainer container,
                          Map<Node, Integer> nodeId, Node node, RootCell rootCell, FlatPoint offset,
                          boolean isCell, boolean needCalcOffset, int depth) {
    Asserts.illegalArgument(depth > Graphviz.MAX_DEPTH,
                            "The nesting depth of cell exceeds the upper limit");
    NodeDrawProp nodeDrawProp = drawGraph.getNodeDrawProp(node);

    NodeAttrs nodeAttrs = nodeDrawProp != null
        ? nodeDrawProp.nodeAttrs() : node.nodeAttrs().clone();

    try {
      if (isCell) {
        copyTempProperties(nodeAttrs, null, DEFAULT_CELL_ATTRS_MAP);
      } else {
        // Set template properties
        copyTempProperties(
            nodeAttrs,
            findFirstHaveTempParent(drawGraph.getGraphviz(), true, container),
            DEFAULT_NODE_ATTRS_MAP
        );
      }
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Failed to access template property", e);
    }

    if (nodeDrawProp == null) {
      nodeDrawProp = new NodeDrawProp(node, nodeAttrs);
      drawGraph.nodePut(node, nodeDrawProp);

      // Node Id
      Integer n = nodeId.get(node);
      if (n == null) {
        int nz = nodeId.size();
        nodeDrawProp.setId(nz);
        nodeId.put(node, nz);
      }

      nodeContainerSet(nodeDrawProp, nodeAttrs, drawGraph.needFlip());

      // Set cell size and offset
      if (needCalcOffset && (nodeDrawProp.haveChildrenCell() || isCell)) {
        Cell cell = null;
        if (rootCell == null) {
          rootCell = new RootCell(false);
          nodeDrawProp.setCell(rootCell);
          cell = rootCell;
        } else {
          String id = nodeAttrs.getId();
          if (id != null) {
            cell = new Cell(false);
            rootCell.put(id, cell);
            if (rootCell.children == null) {
              rootCell.children = new ArrayList<>(2);
            }
            rootCell.children.add(cell);
          }
        }

        if (cell != null) {
          cell.setShape(nodeAttrs.getShape());
          cell.setWidth(nodeDrawProp.getWidth());
          cell.setHeight(nodeDrawProp.getHeight());
          cell.setOffset(offset);
        }
      }

      Assemble assemble = nodeDrawProp.getAssemble();
      if (assemble != null) {
        if (needCalcOffset) {
          // Get the node label position info
          nodeLabelSet(nodeDrawProp, drawGraph, true);
          FlatPoint center = nodeDrawProp.getLabelCenter();
          FlatPoint labelSize = nodeDrawProp.getLabelSize();

          if (center != null && labelSize != null) {
            // Calculate the absolute offset from root node
            double vo = center.getY() - (labelSize.getHeight() / 2) - nodeDrawProp.getUpBorder();
            double ho = center.getX() - (labelSize.getWidth() / 2) - nodeDrawProp.getLeftBorder();
            /*
             * If node is root node, offset is the label offset;
             * If node is not root node, offset increase the label offset.
             * */
            if (offset == null) {
              offset = new FlatPoint(ho, vo);
            } else {
              offset.setY(offset.getY() + vo);
              offset.setX(offset.getX() + ho);
            }

            for (Node c : assemble.getCells()) {
              // Added cell offset
              double horOffset = assemble.horOffset(c);
              double verOffset = assemble.verOffset(c);
              FlatPoint of = new FlatPoint(
                  offset.getX() + horOffset,
                  offset.getY() + verOffset
              );

              nodeHandle(attachment, drawGraph, container, nodeId, c, rootCell,
                         of, true, needCalcOffset, depth + 1);
              NodeDrawProp cellProp = drawGraph.getNodeDrawProp(c);
              cellProp.setCellContainer(nodeDrawProp);
            }
          }
        } else {
          for (Node c : assemble.getCells()) {
            nodeHandle(attachment, drawGraph, container, nodeId, c, null,
                       null, true, needCalcOffset, depth + 1);
            NodeDrawProp cellProp = drawGraph.getNodeDrawProp(c);
            cellProp.setCellContainer(nodeDrawProp);
          }
        }
      }
    } else {
      nodeDrawProp.setNodeAttrs(nodeAttrs);
    }

    // Node consume
    if (isCell) {
      nodeDrawProp.markIsCellProp();
    } else {
      consumerNode(node, attachment, drawGraph, container);
    }
  }

  private void lineHandle(LayoutAttach attachment, DrawGraph drawGraph, GraphContainer container,
                          Map<Line, Integer> lineId, Map<Node, Integer> nodeId, Line line) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);

    LineAttrs lineAttrs = lineDrawProp != null
        ? lineDrawProp.lineAttrs()
        : line.lineAttrs().clone();

    try {
      // Set template properties
      copyTempProperties(
          lineAttrs,
          findFirstHaveTempParent(drawGraph.getGraphviz(), false, container),
          DEFAULT_LINE_ATTRS_MAP
      );
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Failed to access template property", e);
    }

    if (lineDrawProp == null) {
      lineDrawProp = new LineDrawProp(line, lineAttrs, drawGraph);
      drawGraph.linePut(line, lineDrawProp);

      // Set line assemble
      Assemble assemble = lineDrawProp.getAssemble();
      assembleHandle(attachment, drawGraph, container, nodeId, assemble);
      for (Assemble floatAssemble : lineDrawProp.getFloatAssembles()) {
        assembleHandle(attachment, drawGraph, container, nodeId, floatAssemble);
      }
    }

    // Line id
    Integer n = lineId.get(line);
    if (n == null) {
      int nz = lineId.size();
      lineDrawProp.setId("line_" + nz);
      lineId.put(line, nz);
    }

    // Line consume
    consumerLine(line, attachment, drawGraph);
  }

  private void assembleHandle(LayoutAttach attach, DrawGraph drawGraph, GraphContainer container,
                              Map<Node, Integer> nodeId, Assemble assemble) {
    if (assemble == null) {
      return;
    }

    for (Node cell : assemble.getCells()) {
      nodeHandle(attach, drawGraph, container, nodeId, cell, null, null, true, false, 0);
    }
  }

  private void clusterHandle(LayoutAttach attach, DrawGraph drawGraph, Cluster cluster,
                             Map<Node, Integer> nodeId, Map<GraphContainer, Integer> clusterId) {
    if (drawGraph.haveCluster(cluster)) {
      return;
    }

    ClusterDrawProp clusterDrawProp = new ClusterDrawProp(cluster);
    drawGraph.clusterPut(cluster, clusterDrawProp);

    // Cluster id
    Integer n = clusterId.get(cluster);
    if (n == null) {
      int nz = clusterId.size();
      clusterDrawProp.setClusterNo(nz);
      clusterDrawProp.setId("cluster_" + nz);
      clusterId.put(cluster, nz);
    }

    // Set cluster assemble
    Assemble assemble = clusterDrawProp.getAssemble();
    assembleHandle(attach, drawGraph, null, nodeId, assemble);

    ClusterAttrs clusterAttrs = cluster.clusterAttrs();
    String label = clusterAttrs.getLabel();
    double fontSize = clusterAttrs.getFontSize();

    // Init cluster label size
    FlatPoint labelContainer = null;
    if (assemble == null) {
      if (label != null) {
        labelContainer = labelContainer(label, clusterAttrs.getFontName(), fontSize);
      }
    } else {
      labelContainer = assemble.size();
    }
    drawGraph.getClusterDrawProp(cluster).setLabelSize(labelContainer);
  }

  private void nodeContainerSet(NodeDrawProp nodeDrawProp, NodeAttrs nodeAttrs, boolean needFlip) {
    NodeShape nodeShape = nodeAttrs.getShape();

    // Set node box size
    double height = nodeAttrs.getHeight() == null
        ? nodeShape.getDefaultHeight() : nodeAttrs.getHeight();
    double width = nodeAttrs.getWidth() == null
        ? nodeShape.getDefaultWidth() : nodeAttrs.getWidth();

    // Inner Label Box size
    FlatPoint labelBox;
    double verMargin = 0;
    double horMargin = 0;

    if (nodeDrawProp.noChildrenCell() && isRecordShape(nodeShape)) {
      RootCell rootCell = CellLabelCompiler.compile(nodeAttrs.getLabel(), nodeAttrs.getFontName(),
                                                    getFontSize(nodeAttrs), nodeAttrs.getMargin(),
                                                    new FlatPoint(height, width), needFlip);
      labelBox = new FlatPoint(rootCell.getHeight(), rootCell.getWidth());
      nodeDrawProp.setCell(rootCell);
    } else {
      labelBox = sizeInit(nodeDrawProp.getAssemble(), nodeAttrs);
      if (nodeAttrs.getMargin() != null && nodeShape.needMargin()) {
        verMargin += nodeAttrs.getMargin().getHeight();
        horMargin += nodeAttrs.getMargin().getWidth();
      }
    }

    FlatPoint boxSize;
    boolean ignoreLabel = Boolean.TRUE.equals(nodeAttrs.getFixedSize()) || nodeShape.ignoreLabel();
    if (ignoreLabel) {
      boxSize = new FlatPoint(height, width);
    } else {
      FlatPoint labelSize = new FlatPoint(labelBox.getHeight(), labelBox.getWidth());
      if (nodeAttrs.getImageSize() != null) {
        FlatPoint imageSize = nodeAttrs.getImageSize();
        double h = Math.max(imageSize.getHeight() + verMargin, labelSize.getHeight());
        double w = Math.max(imageSize.getWidth() + horMargin, labelSize.getWidth());
        boxSize = nodeShape.minContainerSize(h, w);
      } else {
        boxSize = nodeShape.minContainerSize(labelSize.getHeight() + verMargin,
                                             labelSize.getWidth() + horMargin);
      }
      Asserts.illegalArgument(boxSize == null,
                              "Node Shape can not return null box size from minContainerSize");
      boxSize.setHeight(Math.max(boxSize.getHeight(), height));
      boxSize.setWidth(Math.max(boxSize.getWidth(), width));
      nodeShape.ratio(boxSize);
      nodeDrawProp.setLabelSize(labelSize);
    }

    nodeDrawProp.setLeftBorder(0);
    nodeDrawProp.setRightBorder(boxSize.getWidth());
    nodeDrawProp.setUpBorder(0);
    nodeDrawProp.setDownBorder(boxSize.getHeight());
    if (nodeDrawProp.getLabelSize() == null) {
      nodeDrawProp.setLabelSize(labelBox);
    }

    if (ignoreLabel) {
      labelOffset(nodeDrawProp, nodeAttrs, labelBox,
                  boxSize.getHeight() - labelBox.getHeight(),
                  boxSize.getWidth() - labelBox.getWidth());
    } else {
      labelOffset(nodeDrawProp, nodeAttrs, labelBox, verMargin, horMargin);
    }
  }

  private void labelOffset(NodeDrawProp nodeDrawProp, NodeAttrs nodeAttrs, FlatPoint labelBox,
                           double verMargin, double horMargin) {
    double halfHeight = (verMargin + labelBox.getHeight()) / 2;
    double halfWidth = (horMargin + labelBox.getWidth()) / 2;
    Labelloc labelloc = nodeAttrs.getLabelloc();
    Labeljust labeljust = nodeAttrs.getLabeljust();
    if (labelloc != null) {
      double offsetY = labelloc.getY(
          new FlatPoint(-halfWidth, -halfHeight),
          new FlatPoint(halfWidth, halfHeight),
          labelBox
      );
      nodeDrawProp.setLabelVerOffset(offsetY);
    }

    if (labeljust != null) {
      double offsetX = labeljust.getX(
          new FlatPoint(-halfWidth, -halfHeight),
          new FlatPoint(halfWidth, halfHeight),
          labelBox
      );
      nodeDrawProp.setLabelHorOffset(offsetX);
    }
  }

  private GraphContainer findFirstHaveTempParent(Graphviz graphviz, boolean nodeTemp,
                                                 GraphContainer container) {
    GraphContainer p = container;

    while (p != null) {
      if ((nodeTemp && p.haveNodeTemp()) || (!nodeTemp && p.haveLineTemp())) {
        break;
      }

      p = graphviz.father(p);
    }

    return p;
  }

  private FlatPoint sizeInit(Assemble assemble, NodeAttrs nodeAttrs) {
    if (assemble != null) {
      return assemble.size();
    }

    String label = nodeAttrs.getLabel();
    return labelContainer(label, nodeAttrs.getFontName(),
                          getFontSize(nodeAttrs), nodeAttrs.getFontStyles());
  }

  private double getFontSize(NodeAttrs nodeAttrs) {
    return nodeAttrs.getFontSize() != null ? nodeAttrs.getFontSize() : 0D;
  }

  @SuppressWarnings("all")
  private void copyTempProperties(Object attrs, GraphContainer container,
                                  Map<String, Object> defaultVal) throws IllegalAccessException {
    Objects.requireNonNull(defaultVal);
    if (attrs == null) {
      return;
    }

    NodeShape nodeShape = null;
    Field nodeShapeField = null;
    Class<?> cls = attrs.getClass();
    Field[] fields = cls.getDeclaredFields();
    for (Field field : fields) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      field.setAccessible(true);
      Object v = field.get(attrs);
      if (v == null) {
        Object propVal;
        if (container == null) {
          propVal = null;
        } else if (attrs instanceof NodeAttrs) {
          propVal = container.getNodeAttr(field.getName());
        } else {
          propVal = container.getLineAttr(field.getName());
        }
        propVal = propVal != null ? propVal : defaultVal.get(field.getName());

        if (propVal == null) {
          field.setAccessible(false);
          continue;
        } else if (Objects.equals("assemble", field.getName())
            || Objects.equals("table", field.getName())) {
          log.warn("Can not copy " + field.getName() + " attribute!");
          field.setAccessible(false);
          continue;
        }

        field.set(attrs, propVal);
        field.setAccessible(false);
        v = propVal;
      }

      if (v instanceof NodeShape) {
        nodeShape = (NodeShape) v;
        nodeShapeField = field;
      }
    }

    // Compile a new NodeShape description function
    if (nodeShape != null && attrs instanceof NodeAttrs) {
      nodeShapeField.setAccessible(true);
      // Node post shape
      nodeShapeField.set(attrs, nodeShape.post((NodeAttrs) attrs));
      nodeShapeField.setAccessible(false);
    }
  }

  private void moveGraph(DrawGraph drawGraph, RenderEngine renderEngine, LayoutAttach attach) {
    List<ShifterStrategy> layoutShifters = shifterStrategies(drawGraph);

    Shifter shifter;
    Set<FlatPoint> pointMark = null;
    if (CollectionUtils.isNotEmpty(layoutShifters)) {
      pointMark = new HashSet<>();
      shifter = new CombineShifter(pointMark, layoutShifters);
      executeShifter(drawGraph, shifter);
    }
    afterLayoutShifter(attach);

    if (pointMark != null) {
      pointMark.clear();
    }

    List<ShifterStrategy> renderShifters = renderEngine == null
        ? null : renderEngine.shifterStrategies(drawGraph);

    if (CollectionUtils.isNotEmpty(renderShifters)) {
      if (pointMark == null) {
        pointMark = new HashSet<>();
      }
      shifter = new CombineShifter(pointMark, renderShifters);
      executeShifter(drawGraph, shifter);
    }
    afterRenderShifter(attach);

    if (renderEngine != null && renderEngine.needShift(drawGraph)) {
      if (pointMark != null) {
        pointMark.clear();
      } else {
        pointMark = new HashSet<>();
      }
      renderShifters = renderEngine.shifterStrategies(drawGraph);
      if (CollectionUtils.isNotEmpty(renderShifters)) {
        shifter = new CombineShifter(pointMark, renderShifters);
        executeShifter(drawGraph, shifter);
      }
    }

  }

  private void executeShifter(DrawGraph drawGraph, Shifter shifter) {
    shifter.graph(drawGraph.getGraphvizDrawProp());
    drawGraph.clusters().forEach(shifter::cluster);
    drawGraph.nodes().forEach(shifter::node);
    drawGraph.lines().forEach(line -> {
      shifter.line(line);
      if (!drawGraph.needFlip()) {
        return;
      }
      FlatPoint labelSize = line.getLabelSize();
      if (labelSize == null || shifter.isMark(labelSize)) {
        return;
      }
      labelSize.flip();
      shifter.markFlatPoint(labelSize);
    });
  }

  private boolean isRecordShape(NodeShape nodeShape) {
    return nodeShape == NodeShapeEnum.RECORD || nodeShape == NodeShapeEnum.M_RECORD;
  }

  private void removeEmptyCluster(DrawGraph drawGraph, LayoutGraph layoutGraph) {
    if (layoutGraph == null) {
      return;
    }

    Map<Cluster, ClusterDrawProp> clusterDrawPropMap = drawGraph.getClusterDrawPropMap();
    if (clusterDrawPropMap == null || clusterDrawPropMap.isEmpty()) {
      return;
    }

    Iterator<Entry<Cluster, ClusterDrawProp>> iterator = clusterDrawPropMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<Cluster, ClusterDrawProp> cluster = iterator.next();
      if (layoutGraph.isEmptyGraphContainer(cluster.getKey())) {
        iterator.remove();
      }
    }
  }

  public static class LineClipProcessor extends LineClip {

    public LineClipProcessor(DrawGraph drawGraph, LayoutGraph<?, ?> layoutGraph) {
      Objects.requireNonNull(drawGraph);
      Objects.requireNonNull(layoutGraph);
      this.drawGraph = drawGraph;
      this.layoutGraph = layoutGraph;
    }

    public void clipAllLines() {
      for (LineDrawProp line : drawGraph.lines()) {
        PathClip pathClip;
        if (line.isBesselCurve()) {
          pathClip = CurvePathClip.INSTANCE;
        } else {
          pathClip = StraightPathClip.INSTANCE;
        }

        if (line.isSelfLoop() && CollectionUtils.isNotEmpty(line)) {
          FlatPoint noPathDirection = line.get(line.size() / 2);
          clipProcess(line, pathClip, noPathDirection, line);
        } else {
          clipProcess(line, pathClip, null, line);
        }

        if (CollectionUtils.isEmpty(line)) {
          continue;
        }

        line.setStart(line.get(0));
        line.setEnd(line.get(line.size() - 1));
        setFloatLabel(line);
      }

      drawGraph.syncToGraphvizBorder();
    }
  }
}
