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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.graphper.layout.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.def.FlatPoint;
import org.graphper.def.Curves;
import org.graphper.def.Vectors;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.util.ValueUtils;
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
import org.graphper.api.ext.ShapePosition;
import org.graphper.draw.ArrowDrawProp;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;
import org.graphper.layout.Cell.RootCell;
import org.graphper.layout.LabelSizeHelper;

public abstract class LineClip {

  private static final Logger log = LoggerFactory.getLogger(LineClip.class);

  protected DrawGraph drawGraph;

  protected DotDigraph dotDigraph;

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

  protected <P> void clipProcess(P path, PathClip<P> pathClip,
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
        DNode dNode = dotDigraph.getDNode(from);
        Asserts.illegalArgument(dNode == null, "Can not found from node of line prop");
        clusterDrawProp = findLineEndPointCluster(
            dNode.getContainer(),
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
          DNode dNode = dotDigraph.getDNode(to);
          Asserts.illegalArgument(dNode == null, "Can not found to node of line prop");
          clusterDrawProp = findLineEndPointCluster(
              dNode.getContainer(),
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
      String label = floatLabel.getLabel();
      if (label == null) {
        continue;
      }

      FlatPoint labelSize = LabelSizeHelper.measure(label, lineAttrs.getFontName(),
                                                    floatLabel.getFontSize(), 0);
      if (Objects.equals(labelSize, Vectors.ZERO)) {
        continue;
      }

      FlatPoint startPoint;
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

      if (startPoint != null) {
        FlatPoint center = floatPointCenter(startPoint, labelSize, floatLabel.getDistRatio());
        lineDrawProp.addFloatLabelCenter(floatLabel, center);

        drawGraph.updateXAxisRange(center.getX() - labelSize.getWidth() / 2);
        drawGraph.updateXAxisRange(center.getX() + labelSize.getWidth() / 2);
        drawGraph.updateYAxisRange(center.getY() - labelSize.getHeight() / 2);
        drawGraph.updateYAxisRange(center.getY() + labelSize.getHeight() / 2);
      }
    }
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
  private FlatPoint calcArrowLinkPoint(FlatPoint clip, double arrowSize, FlatPoint point) {
    FlatPoint dirVector = Vectors.sub(clip, point);
    double dist = dirVector.dist();
    if (ValueUtils.approximate(dist, 0, 0.001)) {
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

  private FlatPoint curveGetFloatLabelStart(double[] labelLength,
                                            double lengthRatio,
                                            LineDrawProp lineDrawProp) {
    if (lineDrawProp.size() < 4) {
      return null;
    }

    double len = labelLength != null ? labelLength[0] : -1;
    if (len < 0) {
      len = 0;
      for (int i = 3; i < lineDrawProp.size(); i += 3) {
        FlatPoint v1 = lineDrawProp.get(i - 3);
        FlatPoint v2 = lineDrawProp.get(i - 2);
        FlatPoint v3 = lineDrawProp.get(i - 1);
        FlatPoint v4 = lineDrawProp.get(i);

        len += FlatPoint.twoFlatPointDistance(v1, v2);
        len += FlatPoint.twoFlatPointDistance(v2, v3);
        len += FlatPoint.twoFlatPointDistance(v3, v4);
      }
      if (labelLength != null) {
        labelLength[0] = len;
      }
    }

    double beforeLen = 0;
    double floatLabelInCurveLen = 0;
    FlatPoint v1 = null;
    FlatPoint v2 = null;
    FlatPoint v3 = null;
    FlatPoint v4 = null;

    double start = len * lengthRatio - 1;
    double end = len * lengthRatio + 1;
    for (int i = 3; i < lineDrawProp.size(); i += 3) {
      v1 = lineDrawProp.get(i - 3);
      v2 = lineDrawProp.get(i - 2);
      v3 = lineDrawProp.get(i - 1);
      v4 = lineDrawProp.get(i);

      floatLabelInCurveLen = 0;
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v1, v2);
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v2, v3);
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v3, v4);
      if (beforeLen + floatLabelInCurveLen > end) {
        break;
      }
      beforeLen += floatLabelInCurveLen;
    }

    if (floatLabelInCurveLen == 0) {
      return null;
    }

    if (beforeLen < start && beforeLen + floatLabelInCurveLen > end) {
      double t = BigDecimal.valueOf(len)
          .multiply(BigDecimal.valueOf(lengthRatio))
          .subtract(BigDecimal.valueOf(beforeLen))
          .divide(BigDecimal.valueOf(floatLabelInCurveLen), 4, RoundingMode.HALF_UP)
          .doubleValue();
      return Curves.besselEquationCalc(t, v1, v2, v3, v4);
    }

    return lengthRatio == 0 ? v1 : v4;
  }

  private FlatPoint straightGetFloatLabelStart(double[] labelLength,
                                               double lengthRatio,
                                               LineDrawProp lineDrawProp) {
    double len = labelLength != null ? labelLength[0] : -1;
    if (len < 0) {
      len = 0;
      for (int i = 1; i < lineDrawProp.size(); i++) {
        FlatPoint v1 = lineDrawProp.get(i - 1);
        FlatPoint v2 = lineDrawProp.get(i);

        len += FlatPoint.twoFlatPointDistance(v1, v2);
      }
      if (labelLength != null) {
        labelLength[0] = len;
      }
    }

    double beforeLen = 0;
    double floatLabelInCurveLen = 0;
    FlatPoint v1 = null;
    FlatPoint v2 = null;

    double start = len * lengthRatio - 1;
    double end = len * lengthRatio + 1;
    for (int i = 1; i < lineDrawProp.size(); i++) {
      v1 = lineDrawProp.get(i - 1);
      v2 = lineDrawProp.get(i);

      floatLabelInCurveLen = 0;
      floatLabelInCurveLen += FlatPoint.twoFlatPointDistance(v1, v2);
      if (beforeLen + floatLabelInCurveLen > end) {
        break;
      }
      beforeLen += floatLabelInCurveLen;
    }

    if (floatLabelInCurveLen == 0) {
      return null;
    }

    if (beforeLen < start && beforeLen + floatLabelInCurveLen > end) {
      double t = BigDecimal.valueOf(len)
          .multiply(BigDecimal.valueOf(lengthRatio))
          .subtract(BigDecimal.valueOf(beforeLen))
          .divide(BigDecimal.valueOf(floatLabelInCurveLen), 4, RoundingMode.HALF_UP)
          .doubleValue();
      return Vectors.add(Vectors.multiple(Vectors.sub(v2, v1), t), v1);
    }

    return lengthRatio == 0 ? v1 : v2;
  }

  private FlatPoint floatPointCenter(FlatPoint startPoint, FlatPoint labelSize, double distRatio) {
    if (drawGraph.needFlip()) {
      return new FlatPoint(startPoint.getX(),
                           startPoint.getY() + labelSize.getHeight() * distRatio);
    }

    return new FlatPoint(startPoint.getX() + labelSize.getWidth() * distRatio, startPoint.getY());
  }

  private ShapePosition getClipShapePosition(LineDrawProp line, NodeDrawProp node, boolean isTail) {
    if (node.getCell() == null) {
      return node;
    }

    String cellId = null;
    if (isTail) {
      cellId = line.lineAttrs().getTailCell();
    } else {
      cellId = line.lineAttrs().getHeadCell();
    }

    if (cellId == null) {
      return node;
    }

    RootCell Cell = node.getCell();
    Cell cell = Cell.getCellById(cellId);
    if (cell == null) {
      return node;
    }

    FlatPoint cellCenter = cell.getCenter(node);
    return new DefaultShapePosition(cellCenter.getX(), cellCenter.getY(),
                                    cell.getHeight(), cell.getWidth(),
                                    NodeShapeEnum.RECT);
  }
}
