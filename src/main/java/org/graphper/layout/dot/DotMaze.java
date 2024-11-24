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

import static org.graphper.layout.dot.AbstractDotLineRouter.LABEL_NODE_SIDE_MIN_DISTANCE;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.graphper.def.FlatPoint;
import org.graphper.layout.Maze;
import org.graphper.util.Asserts;
import org.graphper.api.Line;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.draw.DrawGraph;
import org.graphper.layout.Grid.GridBuilder;
import org.graphper.layout.dot.RankContent.RankNode;

class DotMaze extends Maze {

  private final RankContent rankContent;

  protected DotMaze(RankContent rankContent, DrawGraph drawGraph) {
    super(drawGraph);
    Asserts.nullArgument(rankContent, "rankContent");
    this.rankContent = rankContent;

    init();
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
    recordGuideBox(node.getLabelLine(), label, guideBox);
  }

  private Box splitLabelNode(DNode node, boolean isGuideBox) {
    if (!node.isLabelNode()) {
      return null;
    }

    double guideWidth = Math.min(LABEL_NODE_SIDE_MIN_DISTANCE, node.getWidth() / 3);
    double splitLine = node.getLeftBorder() + guideWidth;
    if (isGuideBox) {
      // If label node not big enough
      if (LABEL_NODE_SIDE_MIN_DISTANCE >= node.getWidth()) {
        return node;
      }

      return new DefaultBox(node.getLeftBorder(), splitLine,
                            node.getUpBorder(), node.getDownBorder());
    }

    return new DefaultBox(splitLine, node.getRightBorder(),
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
      recordGuideBox(line.getLine(), label, guideBox);
    }
  }

  private void recordGuideBox(Line line, Box sign, Box guideBox) {
    if (guideBoxes == null) {
      guideBoxes = new LinkedHashMap<>();
    }

    List<GuideInfo> guideInfos = guideBoxes.computeIfAbsent(line, k -> new ArrayList<>());
    GuideInfo guideInfo = new GuideInfo();
    guideInfo.setSignPos(sign);
    guideInfo.setGuideBox(guideBox);
    guideInfo.setLabelSign(true);
    guideInfos.add(guideInfo);
  }
}
