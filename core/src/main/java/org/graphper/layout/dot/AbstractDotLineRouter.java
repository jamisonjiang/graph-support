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

import static org.graphper.util.EnvProp.CLIP_DIST_ERROR;

import java.util.function.Consumer;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.ShapePosition;
import org.graphper.api.ext.ShapePropCalc;
import org.graphper.def.Curves;
import org.graphper.def.Curves.ThirdOrderBezierCurve;
import org.graphper.def.EdgeDedigraph;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultShapePosition;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.LayoutGraph;
import org.graphper.layout.LineClip;
import org.graphper.layout.LineHelper;
import org.graphper.layout.LineRouter;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.Asserts;

public abstract class AbstractDotLineRouter extends LineClip implements LineRouter {

  protected static final double LABEL_NODE_SIDE_MIN_DISTANCE = 10;

  protected RankContent rankContent;

  protected LayoutGraph<?, ?> layoutGraph;

  protected EdgeDedigraph<DNode, DLine> digraphProxy;

  @Override
  public void route() {
    Object attach = attach();

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (DNode node : rankNode) {
        if (nodeConsumer(node, attach)) {
          continue;
        }

        // All out edges
        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (line.isVirtual() || line.isHide()) {
            continue;
          }

          if (line.isParallelMerge() && (!line.isSameRank() || (line.isSameRank()
              && isAdj(line.from(), line.to())))
          ) {
            parallelLineHandle(line);
            continue;
          }

          lineConsumer(line, attach);
        }

        // Draw self loop
        LineHelper.selfLoopHandle(node);
      }
    }
  }

  /**
   * Before draw line, produce attachment for next method.
   *
   * @return draw line attachment
   */
  protected Object attach() {
    return null;
  }

  /**
   * The consumption action of the node when drawing the line.
   *
   * @param node   node
   * @param attach draw line attachment
   * @return True - continue draw line, False - consume next node
   */
  protected boolean nodeConsumer(DNode node, Object attach) {
    return false;
  }

  /**
   * The consumption action of the line.
   *
   * @param line   line
   * @param attach draw line attachment
   */
  protected void lineConsumer(DLine line, Object attach) {
  }

  /**
   * According to the shape object of the specified coordinates and size, cut the specified bessel
   * curve to ensure that the curve fits the specified shape.
   *
   * @param shapePosition shape position information
   * @param bezierCurve   the curve to be clipped
   * @return curve after clip
   */
  public static ThirdOrderBezierCurve besselCurveClipShape(ShapePosition shapePosition,
                                                           ThirdOrderBezierCurve bezierCurve) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    Asserts.nullArgument(shapePosition.shapeProp(), "shapePosition.nodeShape()");
    Asserts.nullArgument(bezierCurve, "bezierCurve");

    if (shapePosition.getHeight() <= 0 || shapePosition.getWidth() <= 0) {
      return bezierCurve;
    }

    FlatPoint v1 = bezierCurve.getV1();
    FlatPoint v2 = bezierCurve.getV2();
    FlatPoint v3 = bezierCurve.getV3();
    FlatPoint v4 = bezierCurve.getV4();

    ShapePropCalc shapePropCalc = shapePosition.shapeProp();

    boolean v1In = shapePropCalc.in(shapePosition, v1);
    boolean v4In = shapePropCalc.in(shapePosition, v4);

    if (v1In && v4In) {
      return null;
    }

    if (!v1In && !v4In) {
      return bezierCurve;
    }

    double in = v1In ? 0 : 1;
    double out = v4In ? 0 : 1;
    FlatPoint[] points = {v1, v2, v3, v4};

    do {
      FlatPoint midPoint = Curves.besselEquationCalc((in + out) / 2, points);

      if (shapePropCalc.in(shapePosition, midPoint)) {
        in = (in + out) / 2;
      } else {
        out = (in + out) / 2;
      }

    } while (FlatPoint.twoFlatPointDistance(Curves.besselEquationCalc(in, points),
                                            Curves.besselEquationCalc(out, points))
        > CLIP_DIST_ERROR);

    return Curves.divideThirdBesselCurve(in, v4In, bezierCurve);
  }



  /**
   * If the line is cut by multiple virtual nodes, consume each virtual line segment through
   * lineSegmentConsumer.
   *
   * @param line     line
   * @param consumer line consumer
   */
  protected void lineSegmentConsumer(DLine line, Consumer<DLine> consumer) {
    DNode to = line.to();
    while (to.isVirtual()) {
      if (consumer != null) {
        consumer.accept(line);
      }

      for (DLine dLine : digraphProxy.outAdjacent(to)) {
        to = dLine.to();
        line = dLine;
        break;
      }
    }

    if (consumer != null && !to.isVirtual()) {
      consumer.accept(line);
    }
  }

  protected boolean isAdj(DNode n, DNode w) {
    if (Math.abs(w.getRankIndex() - n.getRankIndex()) <= 1) {
      return true;
    }

    // Skip virtual vertices between two vertices
    DNode largeRankIndexNode = n.getRankIndex() > w.getRankIndex() ? n : w;
    DNode current = n == largeRankIndexNode ? w : n;
    do {
      current = rankContent.rankNextNode(current);
    } while (current != null && current != largeRankIndexNode && current.isVirtual());

    return current == largeRankIndexNode;
  }

  // ----------------------------------------------------- static method -----------------------------------------------------

  public static ShapePosition newArrowShapePosition(FlatPoint point, double arrowSize) {
    Asserts.nullArgument(point, "point");
    return new DefaultShapePosition(point.getX(), point.getY(),
                                    arrowSize * 2, arrowSize * 2,
                                    NodeShapeEnum.CIRCLE);
  }

  // --------------------------------------------- Abstract DotLinesHandlerFactory ---------------------------------------------

  public abstract static class AbstractDotLineRouterFactory<T extends AbstractDotLineRouter>
      extends DotLineRouterFactory<T> {

    @Override
    public T newInstance(DrawGraph drawGraph, DotDigraph dotDigraph, RankContent rankContent,
                         EdgeDedigraph<DNode, DLine> digraphProxy) {
      Asserts.nullArgument(drawGraph, "drawGraph");
      Asserts.nullArgument(dotDigraph, "dotDigraph");
      Asserts.nullArgument(rankContent, "rankContent");
      Asserts.nullArgument(digraphProxy, "digraphProxy");

      T t = newInstance();
      Asserts.nullArgument(t, "DotLineRouter");
      t.drawGraph = drawGraph;
      t.layoutGraph = dotDigraph;
      t.rankContent = rankContent;
      t.digraphProxy = digraphProxy;
      return t;
    }

    protected abstract T newInstance();
  }

}
