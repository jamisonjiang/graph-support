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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.graphper.api.Line;
import org.graphper.api.LineAttrs;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.draw.LineDrawProp;
import org.graphper.layout.LineHelper;
import org.graphper.layout.LineRouter;
import org.graphper.layout.PortHelper;
import org.graphper.layout.dot.RankContent.RankNode;
import org.graphper.util.CollectionUtils;


/**
 * Implementation of {@link Splines#LINE}.
 *
 * @author Jamison Jiang
 */
class StraightLineRouter extends AbstractDotLineRouter implements LineRouter {

  @Override
  public void route() {
    DNode[] to = {null};
    // Record parallel lines cross multi ranks, these lines not be able added to DLine#addParallelEdge
    ParallelLineRecord parallelLineRecord = null;

    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode dNodes = rankContent.get(i);

      for (DNode node : dNodes) {
        if (node.isVirtual()) {
          if (node.isFlatLabelNode()) {
            flatLineLabelSet(node);
          }
          continue;
        }

        // All out edges
        for (DLine line : digraphProxy.outAdjacent(node)) {
          to[0] = null;
          if (line.isVirtual() || line.isHide()) {
            continue;
          }

          if (line.isParallelMerge() && (!line.isSameRank() || (line.isSameRank()
              && isAdj(line.from(), line.to())))
          ) {
            parallelLineHandle(line);
            continue;
          }

          LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
          if (CollectionUtils.isNotEmpty(lineDrawProp)) {
            continue;
          }

          lineDrawProp.add(PortHelper.getPortPoint(line.getLine(), line.from(), drawGraph));
          lineSegmentConsumer(line, l -> {
            if (!l.to().isVirtual()) {
              lineDrawProp.add(PortHelper.getPortPoint(line.getLine(), l.to(), drawGraph));
              to[0] = l.to();
            }
            if (l.to().isLabelNode()) {
              lineDrawProp.add(new FlatPoint(l.to().getX(), l.to().getY()));
              lineDrawProp.setLabelCenter(
                  new FlatPoint(l.to().getX() + l.to().getWidth() / 2, l.to().getY()));
            }
          });

          lineDrawProp.setIsHeadStart(line.from().getNode());
          if (parallelLineRecord == null) {
            parallelLineRecord = new ParallelLineRecord();
          }
          parallelLineRecord.addLine(node, to[0], line.getLine());
        }
        // Draw self loop
        LineHelper.selfLoopHandle(node);
      }
    }

    drawParallelLine(parallelLineRecord);
  }

  private void flatLineLabelSet(DNode node) {
    DLine flatLabelLine = node.getFlatLabelLine();

    Map<Line, LineDrawProp> lineDrawPropMap = drawGraph.getLineDrawPropMap();
    if (lineDrawPropMap == null) {
      return;
    }

    for (int i = 0; i < flatLabelLine.getParallelNums(); i++) {
      DLine line = flatLabelLine.parallelLine(i);
      if (line.getLabelSize() == null) {
        continue;
      }

      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line.getLine());
      lineDrawProp.setLabelCenter(new FlatPoint(node.getX(), node.getY()));

      if (line.from().getRank() != node.getRank()) {
        DNode tail = line.from().getNode() == line.getLine().tail() ? line.from() : line.to();
        lineDrawProp.add(PortHelper.getPortPoint(line.getLine(), tail, drawGraph));
        lineDrawProp.add(new FlatPoint(node.getX(), node.getY()));
        lineDrawProp.add(PortHelper.getPortPoint(line.getLine(), line.other(tail), drawGraph));
      }
    }
  }

  private void drawParallelLine(ParallelLineRecord parallelLineRecord) {
    if (Objects.isNull(parallelLineRecord)
        || CollectionUtils.isEmpty(parallelLineRecord.parallelLineGroup)) {
      return;
    }

    for (DLine line : parallelLineRecord.parallelLineGroup) {
      parallelLineHandle(line);
    }
  }

  class ParallelLineRecord {

    private final Map<DNode, Map<DNode, DLine>> lineRecord;

    private List<DLine> parallelLineGroup;

    ParallelLineRecord() {
      this.lineRecord = new LinkedHashMap<>();
    }

    void addLine(DNode n1, DNode n2, Line line) {
      if (n1 == n2) {
        return;
      }

      LineDrawProp lineDrawProp = drawGraph.getLineDrawProp(line);
      LineAttrs lineAttrs = lineDrawProp.lineAttrs();
      if (lineAttrs.getLabel() != null) {
        return;
      }

      DNode t = n1;
      if (n1.getRank() == n2.getRank()) {
        n1 = n1.getRankIndex() < n2.getRankIndex() ? n1 : n2;
      } else {
        n1 = n1.getRank() < n2.getRank() ? n1 : n2;
      }
      n2 = t == n1 ? n2 : t;

      Map<DNode, DLine> adjLine = lineRecord.computeIfAbsent(n1, n -> new LinkedHashMap<>());
      DLine dLine = adjLine.get(n2);
      if (dLine == null) {
        dLine = new DLine(n1, n2, line, lineAttrs, 0, 0);
        adjLine.put(n2, dLine);
      } else {
        dLine.addParallelEdge(new DLine(n1, n2, line, lineAttrs, 0, 0));
      }

      if (dLine.getParallelNums() == 2) {
        if (parallelLineGroup == null) {
          parallelLineGroup = new ArrayList<>(2);
        }
        parallelLineGroup.add(dLine);
      }
    }
  }

  // --------------------------------------------- RoundedHandlerFactory ---------------------------------------------

  static class LineRouterBuilder extends AbstractDotLineRouterFactory<StraightLineRouter> {

    @Override
    public boolean needDeal(Splines splines) {
      return splines == Splines.LINE && super.needDeal(splines);
    }

    @Override
    protected StraightLineRouter newInstance() {
      return new StraightLineRouter();
    }
  }
}
