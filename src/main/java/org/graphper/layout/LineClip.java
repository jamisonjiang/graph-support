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

import static org.graphper.layout.AbstractLayoutEngine.setCellNodeOffset;
import static org.graphper.layout.LineHelper.curveGetFloatLabelStart;
import static org.graphper.layout.LineHelper.straightGetFloatLabelStart;

import java.util.Objects;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Assemble;
import org.graphper.api.Cluster;
import org.graphper.api.FloatLabel;
import org.graphper.api.GraphAttrs;
import org.graphper.api.GraphContainer;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.Node;
import org.graphper.api.attributes.ArrowShape;
import org.graphper.api.attributes.Dir;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Tend;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.ShapePosition;
import org.graphper.def.FlatPoint;
import org.graphper.def.FlatPoint.UnmodifyFlatPoint;
import org.graphper.def.Vectors;
import org.graphper.draw.ArrowDrawProp;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.util.FontUtils;
import org.graphper.util.ValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LineClip extends LineHandler {

  private static final Logger log = LoggerFactory.getLogger(LineClip.class);

  private static final FlatPoint FLOAT_LABEL_LEFT_OFFSET = new UnmodifyFlatPoint(-0.5, -0.5);
  private static final FlatPoint FLOAT_LABEL_RIGHT_OFFSET = new UnmodifyFlatPoint(-0.5, 0.5);
  private static final FlatPoint FLOAT_LABEL_DOWN_OFFSET = new UnmodifyFlatPoint(0.5, 0.5);

  /**
   * The arrow setting of the endpoint of the line segment, it is necessary to specify the axis
   * length and axis direction of the arrow and the position of the axis end point.
   *
   * @param headAxisEnd  head arrow axis end point
   * @param headAxisDir  head arrow direction
   * @param tailAxisEnd  tail arrow axis end point
   * @param tailAxisDir  tail arrow direction
   * @param arrowSize    arrow size
   * @param lineDrawProp path properties
   */
  protected void lineArrowSite(FlatPoint headAxisEnd, FlatPoint headAxisDir,
                               FlatPoint tailAxisEnd, FlatPoint tailAxisDir,
                               double arrowSize, LineDrawProp lineDrawProp) {
    Asserts.nullArgument(lineDrawProp, "lineDrawProp");

    if (headAxisEnd != null && headAxisDir != null) {
      lineDrawProp.setArrowHead(
          new ArrowDrawProp(
              true,
              calcArrowLinkPoint(headAxisEnd, arrowSize, headAxisDir),
              headAxisEnd
          )
      );
    }

    if (tailAxisEnd != null && tailAxisDir != null) {
      lineDrawProp.setArrowTail(
          new ArrowDrawProp(
              false,
              calcArrowLinkPoint(tailAxisEnd, arrowSize, tailAxisDir),
              tailAxisEnd
          )
      );
    }
  }

  /**
   * Processes the clipping of a line within a graph layout, handling interactions with nodes,
   * clusters, and arrowheads. This method ensures that lines are appropriately clipped at their
   * start and end points based on node or cluster boundaries and handles scenarios like self-loops
   * and directional lines.
   *
   * <p>The clipping process involves the following steps:</p>
   * <ul>
   *   <li>Determine whether the line requires clipping at the start or end based on its attributes
   *       and the nodes it connects.</li>
   *   <li>Clip the line against the boundaries of the relevant node or cluster.</li>
   *   <li>Adjust arrow positions and sizes as needed for the clipped path.</li>
   *   <li>Handle scenarios where no valid path exists, ensuring appropriate arrow handling.</li>
   * </ul>
   *
   * @param path             the current line path to be clipped
   * @param pathClip         the clipping utility handling node and cluster boundaries
   * @param noPathDirection  the fallback direction when no valid path exists
   * @param lineDrawProp     the properties of the line being processed, including start and end nodes
   * @throws NullPointerException     if {@code pathClip} or {@code lineDrawProp} is {@code null}
   * @throws IllegalArgumentException if required nodes for the line are missing in the layout
   */
  protected void clipProcess(LineDrawProp path, PathClip pathClip,
                             FlatPoint noPathDirection, LineDrawProp lineDrawProp) {
    Asserts.nullArgument(pathClip, "pathClip");
    Asserts.nullArgument(lineDrawProp, "lineDrawProp");

    Line line = lineDrawProp.getLine();
    LineAttrs lineAttrs = lineDrawProp.lineAttrs();
    boolean isSelfLine = line.head() == line.tail();
    boolean reversal = !isSelfLine && lineDrawProp.isHeadStart();

    Node from = lineDrawProp.isHeadStart() ? line.head() : line.tail();
    Node to = lineDrawProp.isHeadStart() ? line.tail() : line.head();
    NodeDrawProp fromProp = drawGraph.getNodeDrawProp(from);
    NodeDrawProp toProp = drawGraph.getNodeDrawProp(to);
    FlatPoint f = pathClip.pathFrom(path);
    FlatPoint t = pathClip.pathTo(path);

    if ((!isSelfLine && needClip(line, lineAttrs, from))
        || (isSelfLine && needClip(line, lineAttrs, from, false))) {
      ClusterDrawProp clusterDrawProp = null;
      if (!isSelfLine) {
        ANode node = layoutGraph.getNode(from);
        Asserts.illegalArgument(node == null, "Can not found from node of line prop");
        clusterDrawProp = findLineEndPointCluster(
            node.getContainer(),
            getCompoundId(lineAttrs, !reversal)
        );
      }

      if (clusterDrawProp != null) {
        path = pathClip.clusterClip(clusterDrawProp, path);
      } else {
        path = pathClip.nodeClip(getClipShapePosition(lineDrawProp, fromProp, true), path, true);
      }
    }

    if (pathClip.isNotNull(path)) {
      if ((!isSelfLine && needClip(line, lineAttrs, to))
          || (isSelfLine && needClip(line, lineAttrs, to, true))) {
        ClusterDrawProp clusterDrawProp = null;
        if (!isSelfLine) {
          ANode node = layoutGraph.getNode(to);
          Asserts.illegalArgument(node == null, "Can not found to node of line prop");
          clusterDrawProp = findLineEndPointCluster(
              node.getContainer(),
              getCompoundId(lineAttrs, reversal)
          );
        }

        if (clusterDrawProp != null) {
          path = pathClip.clusterClip(clusterDrawProp, path);
        } else {
          path = pathClip.nodeClip(getClipShapePosition(lineDrawProp, toProp, false), path, false);
        }
      }
    }

    double arrowSize = getArrowSize(lineAttrs);
    if (pathClip.isNull(path)) {
      noPathArrowSet(line, reversal, f, t, noPathDirection, lineDrawProp);
    } else {
      f = pathClip.pathFrom(path);
      t = pathClip.pathTo(path);
      boolean fromArrowNeed =
          (haveTailArrow(line) && !reversal) || (haveHeadArrow(line) && reversal);
      boolean toArrowNeed = (haveHeadArrow(line) && !reversal) || (haveTailArrow(line) && reversal);

      if (fromArrowNeed) {
        path = pathClip
            .fromArrowClip(getClipSize(arrowSize, lineAttrs, lineDrawProp.isHeadStart()), path);
      }

      if (pathClip.isNull(path)) {
        noPathArrowSet(line, reversal, f, t, noPathDirection, lineDrawProp);
      } else if (toArrowNeed) {
        path = pathClip
            .toArrowClip(getClipSize(arrowSize, lineAttrs, !lineDrawProp.isHeadStart()), path);
      }

      if (pathClip.isNull(path)) {
        noPathArrowSet(line, reversal, f, t, noPathDirection, lineDrawProp);
      } else {
        FlatPoint nf = pathClip.pathFrom(path);
        FlatPoint nt = pathClip.pathTo(path);
        if (fromArrowNeed) {
          arrowSet(f, nf, !reversal, lineDrawProp);
        }
        if (toArrowNeed) {
          arrowSet(t, nt, reversal, lineDrawProp);
        }
      }
    }
  }

  protected void setFloatLabel(LineDrawProp lineDrawProp) {
    if (lineDrawProp == null) {
      return;
    }
    if (CollectionUtils.isEmpty(lineDrawProp)) {
      return;
    }

    LineAttrs lineAttrs = lineDrawProp.lineAttrs();
    FloatLabel[] floatLabels = lineAttrs.getFloatLabels();
    if (floatLabels == null || floatLabels.length == 0) {
      return;
    }

    double[] labelLength = floatLabels.length > 1 ? new double[]{-1} : null;
    for (FloatLabel floatLabel : floatLabels) {
      FlatPoint startPoint;
      FlatPoint offset = floatLabel.getOffset();
      if (floatLabel.getTend() == null) {
        if (lineDrawProp.isBesselCurve()) {
          startPoint = curveGetFloatLabelStart(
              labelLength,
              floatLabel.getLengthRatio(),
              lineDrawProp
          );
        } else {
          startPoint = straightGetFloatLabelStart(
              labelLength,
              floatLabel.getLengthRatio(),
              lineDrawProp
          );
        }
      } else {
        Tend tend = floatLabel.getTend();
        NodeDrawProp node;
        if (tend == Tend.TAIL) {
          startPoint = lineDrawProp.get(0);
          node = drawGraph.getNodeDrawProp(lineDrawProp.getLine().tail());
        } else {
          startPoint = lineDrawProp.get(lineDrawProp.size() - 1);
          node = drawGraph.getNodeDrawProp(lineDrawProp.getLine().head());
        }

        offset = getFloatLabelLeftOffset(node, lineDrawProp, startPoint, offset, tend == Tend.TAIL);
      }

      FlatPoint labelSize;
      Assemble assemble = lineDrawProp.getFloatAssemble(floatLabel);
      if (assemble == null) {
        String label = floatLabel.getLabel();
        if (label == null) {
          continue;
        }

        labelSize = FontUtils.measure(label, lineAttrs.getFontName(),
                                      floatLabel.getFontSize(), 0);
        if (Objects.equals(labelSize, Vectors.ZERO)) {
          continue;
        }
      } else {
        labelSize = assemble.size();
        setCellNodeOffset(drawGraph, startPoint, assemble, true);
      }

      if (startPoint != null) {
        FlatPoint center = floatPointCenter(startPoint, labelSize, offset);
        if (assemble == null) {
          lineDrawProp.addFloatLabelCenter(floatLabel, center);
        } else {
          setCellNodeOffset(drawGraph, center, assemble, true);
        }

        drawGraph.updateXAxisRange(center.getX() - labelSize.getWidth() / 2);
        drawGraph.updateXAxisRange(center.getX() + labelSize.getWidth() / 2);
        drawGraph.updateYAxisRange(center.getY() - labelSize.getHeight() / 2);
        drawGraph.updateYAxisRange(center.getY() + labelSize.getHeight() / 2);
      }
    }
  }

  private FlatPoint getFloatLabelLeftOffset(NodeDrawProp node, LineDrawProp lineDrawProp,
                                            FlatPoint start, FlatPoint offset, boolean isTail) {
    Box nodeBox = node;
    LineAttrs lineAttrs = lineDrawProp.lineAttrs();
    if (node.getCell() != null) {
      Cell cell = node.getCell().getCellById(
          isTail ? lineAttrs.getTailCell() : lineAttrs.getHeadCell());
      if (cell != null) {
        nodeBox = cell.getCellBox(node);
      }
    }

    FlatPoint pointOffset = calcFloatLabelOffset(start, nodeBox);
    if (offset == null) {
      return pointOffset;
    }

    return new FlatPoint(offset.getHeight() + pointOffset.getHeight(),
                         offset.getWidth() + pointOffset.getWidth());
  }

  protected boolean needClip(Line line, LineAttrs lineAttrs, Node node) {
    return needClip(line, lineAttrs, node, null);
  }

  protected boolean needClip(Line line, LineAttrs lineAttrs,
                             Node node, Boolean isHead) {
    if (isHead == null) {
      if (line.head() == node && !needClip(true, lineAttrs)) {
        return false;
      }

      return line.tail() != node || needClip(false, lineAttrs);
    }

    return needClip(isHead, lineAttrs);
  }

  protected boolean needClip(boolean isHead, LineAttrs lineAttrs) {
    if (lineAttrs == null) {
      return false;
    }

    if (isHead) {
      return Objects.equals(lineAttrs.getHeadclip(), Boolean.TRUE)
          && Objects.isNull(lineAttrs.getHeadPort());
    } else {
      return Objects.equals(lineAttrs.getTailclip(), Boolean.TRUE)
          && Objects.isNull(lineAttrs.getTailPort());
    }
  }


  protected ClusterDrawProp findLineEndPointCluster(GraphContainer graphContainer,
                                                    String clusterId) {
    if (StringUtils.isEmpty(clusterId)) {
      return null;
    }

    if (graphContainer == null || graphContainer.isGraphviz()) {
      return null;
    }

    while (graphContainer.isCluster()) {
      ClusterDrawProp clusterDrawProp = drawGraph.getClusterDrawProp((Cluster) graphContainer);
      if (clusterDrawProp != null && Objects.equals(clusterDrawProp.id(), clusterId)) {
        return clusterDrawProp;
      }
      graphContainer = drawGraph.getGraphviz().effectiveFather(graphContainer);
    }

    return null;
  }

  /**
   * Does the Tail Arrow need to be shown.
   *
   * @param line line
   * @return Tail Arrow need to be shown
   */
  protected boolean haveTailArrow(Line line) {
    Asserts.nullArgument(line, "line");

    if (!drawGraph.getGraphviz().isDirected()) {
      return false;
    }

    LineAttrs lineAttrs = getLineAttrs(line);
    if (lineAttrs.getDir() == null
        || lineAttrs.getDir() == Dir.NONE
        || lineAttrs.getDir() == Dir.FORWARD) {
      return false;
    }

    return lineAttrs.getArrowTail() != null && lineAttrs.getArrowTail() != ArrowShape.NONE;
  }

  /**
   * Does the Head Arrow need to be shown.
   *
   * @param line line
   * @return Head Arrow need to be shown
   */
  protected boolean haveHeadArrow(Line line) {
    Asserts.nullArgument(line, "line");

    if (!drawGraph.getGraphviz().isDirected()) {
      return false;
    }

    LineAttrs lineAttrs = getLineAttrs(line);
    if (lineAttrs.getDir() == null
        || lineAttrs.getDir() == Dir.NONE
        || lineAttrs.getDir() == Dir.BACK) {
      return false;
    }

    return lineAttrs.getArrowHead() != null && lineAttrs.getArrowHead() != ArrowShape.NONE;
  }

  /**
   * Return line attributes.
   *
   * @param line line
   * @return line attributes
   */
  protected LineAttrs getLineAttrs(Line line) {
    LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
    if (lineDrawProp == null) {
      return line.lineAttrs();
    }

    return lineDrawProp.lineAttrs();
  }

  protected String getCompoundId(LineAttrs lineAttrs, boolean isTail) {
    GraphAttrs graphAttrs = drawGraph.getGraphviz().graphAttrs();
    if (!graphAttrs.isCompound()) {
      return null;
    }

    return isTail ? lineAttrs.getLtail() : lineAttrs.getLhead();
  }

  protected double getArrowSize(LineAttrs lineAttrs) {
    Double arrowSize = lineAttrs.getArrowSize();
    return arrowSize != null ? arrowSize : 1d / 8;
  }

  protected double getClipSize(double arrowSize, LineAttrs lineAttrs, boolean isHead) {
    ArrowShape arrowShape = isHead ? lineAttrs.getArrowHead() : lineAttrs.getArrowTail();
    return arrowSize * arrowShape.getClipRatio();
  }

  // ---------------------------------------------------------------- private method ----------------------------------------------------------------
  private FlatPoint calcFloatLabelOffset(FlatPoint endPoint, Box box) {
    double leftBorder = box.getLeftBorder();
    double rightBorder = box.getRightBorder();
    double upBorder = box.getUpBorder();
    double downBorder = box.getDownBorder();

    if (Vectors.inAngle(box.getX(), box.getY(), leftBorder,
                        upBorder, leftBorder, downBorder,
                        endPoint.getX(), endPoint.getY())) {
      return FLOAT_LABEL_LEFT_OFFSET;
    }

    if (Vectors.inAngle(box.getX(), box.getY(), leftBorder,
                        downBorder, rightBorder, downBorder,
                        endPoint.getX(), endPoint.getY())) {
      return FLOAT_LABEL_DOWN_OFFSET;
    }

    return FLOAT_LABEL_RIGHT_OFFSET;
  }

  private FlatPoint calcArrowLinkPoint(FlatPoint clip, double arrowSize, FlatPoint point) {
    FlatPoint dirVector = Vectors.sub(clip, point);
    double dist = dirVector.dist();
    if (ValueUtils.approximate(dist, 0)) {
      return point;
    }
    return Vectors.add(
        point,
        Vectors.multiple(dirVector, (dist - arrowSize) / dist)
    );
  }

  private void arrowSet(FlatPoint f, FlatPoint t, boolean isTail, LineDrawProp lineDrawProp) {
    if (Objects.equals(f, t)) {
      log.debug(
          "Found that the arrow end point and arrow direction point of the arrow are equal, "
              + "and the arrow setting has been skipped, arrow end {}, arrow direction {}", f, t);
      return;
    }

    double arrowSize = getClipSize(getArrowSize(lineDrawProp.lineAttrs()),
                                   lineDrawProp.lineAttrs(), !isTail);
    if (isTail) {
      lineArrowSite(null, null, f, t, arrowSize, lineDrawProp);
    } else {
      lineArrowSite(f, t, null, null, arrowSize, lineDrawProp);
    }
  }

  private void noPathArrowSet(Line line, boolean reversal, FlatPoint f, FlatPoint t,
                              FlatPoint noPathDirection, LineDrawProp lineDrawProp) {
    if ((haveTailArrow(line) && !reversal) || (haveHeadArrow(line) && reversal)) {
      arrowSet(f, noPathDirection == null ? t : noPathDirection, !reversal, lineDrawProp);
    }

    if ((haveHeadArrow(line) && !reversal) || (haveTailArrow(line) && reversal)) {
      arrowSet(t, noPathDirection == null ? f : noPathDirection, reversal, lineDrawProp);
    }
  }

  private FlatPoint floatPointCenter(FlatPoint startPoint, FlatPoint labelSize, FlatPoint offset) {
    if (labelSize == null || offset == null) {
      return new FlatPoint(startPoint.getX(), startPoint.getY());
    }

    return new FlatPoint(startPoint.getX() + labelSize.getWidth() * offset.getWidth(),
                         startPoint.getY() + labelSize.getHeight() * offset.getHeight());
  }

  private ShapePosition getClipShapePosition(LineDrawProp line, NodeDrawProp node, boolean isTail) {
    if (node.getCell() == null) {
      return node;
    }

    String cellId;
    if (isTail) {
      cellId = line.lineAttrs().getTailCell();
    } else {
      cellId = line.lineAttrs().getHeadCell();
    }

    if (cellId == null) {
      return node;
    }

    RootCell rootCell = node.getCell();
    Cell cell = rootCell.getCellById(cellId);
    if (cell == null) {
      return node;
    }

    FlatPoint cellCenter = cell.getCenter(node);
    return new DefaultShapePosition(cellCenter.getX(), cellCenter.getY(),
                                    cell.getHeight(), cell.getWidth(),
                                    NodeShapeEnum.RECT);
  }
}
