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
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Line;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.Grid.GridBuilder;
import org.graphper.layout.OrthoVisGraph.GridVertex;
import org.graphper.layout.dot.RankContent.RankNode;

class DotMaze extends Maze {

  private final RankContent rankContent;

  private Map<Line, List<GuideInfo>> labelGuideBoxes;

  protected DotMaze(RankContent rankContent, DrawGraph drawGraph) {
    super(drawGraph);
    Asserts.nullArgument(rankContent, "rankContent");
    this.rankContent = rankContent;

    init();
  }

  List<GuideInfo> getGuideInfos(Line line) {
    if (line == null || labelGuideBoxes == null) {
      return null;
    }
    List<GuideInfo> guideInfos = labelGuideBoxes.get(line);
    if (CollectionUtils.isEmpty(guideInfos)) {
      return guideInfos;
    }

    for (GuideInfo guideInfo : guideInfos) {
      if (guideInfo.guideVertex == null) {
        guideInfo.guideVertex = getGuideVertex(guideInfo.guideBox);
      }
      Asserts.illegalArgument(guideInfo.signPos == null,
                              "Can not found guide sign of label line");
      Asserts.illegalArgument(guideInfo.guideBox == null,
                              "Can not found guide box of label line");
      Asserts.illegalArgument(guideInfo.guideVertex == null,
                              "Can not found guide vertex of label line");
    }
    return guideInfos;
  }

  @Override
  protected void initGrid(GridBuilder gridBuilder) {
    for (int i = rankContent.minRank(); i <= rankContent.maxRank(); i++) {
      RankNode rankNode = rankContent.get(i);

      for (DNode node : rankNode) {
        if (!node.isVirtual()) {
          addCell(node, new NodeCell(node), gridBuilder);
          continue;
        }

        // Add the cell and guide box of label line
        addLabelNode(gridBuilder, node);

        // Add the cell and guide box of flat label line
        addFlatLabelNode(gridBuilder, node);
      }
    }
  }

  private void addLabelNode(GridBuilder gridBuilder, DNode node) {
    if (!node.isLabelNode()) {
      return;
    }

    Box label = splitLabelNode(node, false);
    if (label != null) {
      // Label cell
      addCell(node, new VirtualCell(label), gridBuilder);
    }

    Box guideBox = splitLabelNode(node, true);
    if (guideBox != null) {
      // Label guide box
      addGuideBox(guideBox, gridBuilder);
    }

    // Record guide information
    recordGuideBox(node.getLabelLine(), label, guideBox, true);
  }

  private Box splitLabelNode(DNode node, boolean isGuideBox) {
    if (!node.isLabelNode()) {
      return null;
    }

    if (isGuideBox) {
      // If label node not big enough
      if (AbstractDotLineRouter.LABEL_NODE_SIDE_MAX_DISTANCE >= node.getWidth()) {
        return node;
      }

      double rightBorder = node.getLeftBorder() + AbstractDotLineRouter.LABEL_NODE_SIDE_MAX_DISTANCE;
      return new DefaultBox(node.getLeftBorder(), rightBorder,
                            node.getUpBorder(), node.getDownBorder());
    }

    if (AbstractDotLineRouter.LABEL_NODE_SIDE_MAX_DISTANCE >= node.getWidth()) {
      return null;
    }

    double leftBorder = node.getLeftBorder() + AbstractDotLineRouter.LABEL_NODE_SIDE_MAX_DISTANCE;
    return new DefaultBox(leftBorder, node.getRightBorder(),
                          node.getUpBorder(), node.getDownBorder());
  }

  private void addFlatLabelNode(GridBuilder gridBuilder, DNode node) {
    if (!node.isFlatLabelNode()) {
      return;
    }

    double start = node.getUpBorder();
    DLine flatLabelLine = node.getFlatLabelLine();
    for (int i = 0; i < flatLabelLine.getParallelNums(); i++) {
      DLine line = flatLabelLine.parallelLine(i);

      Box label = null;
      Box guideBox;
      FlatPoint labelSize = line.getLabelSize();
      if (labelSize != null) {
        label = new DefaultBox(node.getLeftBorder(), node.getRightBorder(), start,
                               start += (labelSize.getHeight() - DNode.FLAT_LABEL_GAP));
        guideBox = new DefaultBox(node.getLeftBorder(), node.getRightBorder(), start,
                                  start += DNode.FLAT_LABEL_GAP);
      } else {
        guideBox = new DefaultBox(node.getLeftBorder(), node.getRightBorder(), start,
                                  start += DNode.FLAT_LABEL_GAP);
      }

      addGuideBox(guideBox, gridBuilder);
      if (label == null) {
        label = guideBox;
      } else {
        addCell(node, new VirtualCell(label), gridBuilder);
      }

      // Record guide information
      recordGuideBox(line.getLine(), label, guideBox, true);
    }
  }

  private void recordGuideBox(Line line, Box sign, Box guideBox, boolean isLabelSign) {
    if (labelGuideBoxes == null) {
      labelGuideBoxes = new LinkedHashMap<>();
    }

    List<GuideInfo> guideInfos = labelGuideBoxes.computeIfAbsent(line, k -> new ArrayList<>());
    GuideInfo guideInfo = new GuideInfo();
    guideInfo.signPos = sign;
    guideInfo.guideBox = guideBox;
    guideInfo.isLabelSign = isLabelSign;
    guideInfos.add(guideInfo);
  }

  static class GuideInfo {

    private Box signPos;

    private Box guideBox;

    private GridVertex guideVertex;

    private boolean isLabelSign;

    public Box getSignPos() {
      return signPos;
    }

    public GridVertex getGuideVertex() {
      return guideVertex;
    }

    public boolean isLabelSign() {
      return isLabelSign;
    }
  }
}
