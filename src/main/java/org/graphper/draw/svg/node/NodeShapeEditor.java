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

package org.graphper.draw.svg.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.ext.CylinderPropCalc;
import org.graphper.api.ext.NotePropCalc;
import org.graphper.api.ext.RecordPropCalc;
import org.graphper.api.ext.StarPropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;
import org.graphper.layout.CellLabelCompiler.LabelCell;
import org.graphper.layout.CellLabelCompiler.RootCell;
import org.graphper.api.attributes.NodeShapeEnum;

public class NodeShapeEditor extends AbstractNodeShapeEditor {

  @Override
  public boolean edit(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getNodeShape();

    CustomizeShapeRender customizeShapeRender = CustomizeShapeRender
        .getCustomizeShapeRender(nodeShape.getName());
    if (customizeShapeRender != null) {
      customizeShapeRender.drawSvg(brush, nodeDrawProp);
    } else if (nodeShape instanceof NodeShapeEnum) {
      drawNodeShapeEnum(nodeDrawProp, (NodeShapeEnum) nodeShape, brush);
    }
    return true;
  }

  private void drawNodeShapeEnum(NodeDrawProp nodeDrawProp,
                                 NodeShapeEnum nodeShape, SvgBrush brush) {
    switch (nodeShape) {
      case ELLIPSE:
        ellipse(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case CIRCLE:
      case POINT:
        circle(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case UNDERLINE:
        underline(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case RECT:
        rect(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case TRIANGLE:
        triangle(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case DIAMOND:
        diamond(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case TRAPEZIUM:
        trapezium(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case PARALLELOGRAM:
        parallelogram(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case STAR:
        start(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
      case NOTE:
        note(nodeDrawProp, brush);
        break;
      case CYLINDER:
        cylinder(nodeDrawProp, brush);
        break;
      case RECORD:
        record(nodeDrawProp, brush, false);
        break;
      case M_RECORD:
        record(nodeDrawProp, brush, true);
        break;
      default:
        circle(nodeDrawProp, singleElement(nodeDrawProp, brush));
        break;
    }
  }

  private Element singleElement(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    return brush.getShapeElement(nodeDrawProp, getShapeElement(nodeDrawProp));
  }

  private void ellipse(NodeDrawProp nodeDrawProp, Element shapeElement) {
    ellipse(nodeDrawProp, shapeElement, nodeDrawProp.getHeight() / 2, nodeDrawProp.getWidth() / 2);
  }

  private void circle(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double radius = nodeDrawProp.getHeight() / 2;
    ellipse(nodeDrawProp, shapeElement, radius, radius);
  }

  private void ellipse(NodeDrawProp nodeDrawProp,
                       Element shapeElement,
                       double height,
                       double width) {
    double x = nodeDrawProp.getX();
    double y = nodeDrawProp.getY();
    shapeElement.setAttribute(SvgConstants.CX, String.valueOf(x));
    shapeElement.setAttribute(SvgConstants.CY, String.valueOf(y));
    shapeElement.setAttribute(SvgConstants.RX, String.valueOf(width));
    shapeElement.setAttribute(SvgConstants.RY, String.valueOf(height));
  }

  private void underline(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(SvgConstants.POINTS, points);
  }

  private void rect(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = SvgEditor.generateBox(nodeDrawProp);
    shapeElement.setAttribute(SvgConstants.POINTS, points);
  }

  private void triangle(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getX(),
                                                     nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(SvgConstants.POINTS, points);
  }

  private void diamond(NodeDrawProp nodeDrawProp, Element shapeElement) {
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getY(),
                                                     nodeDrawProp.getX(),
                                                     nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getY(),
                                                     nodeDrawProp.getX(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getY());
    shapeElement.setAttribute(SvgConstants.POINTS, points);
  }

  private void trapezium(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double leftTopX = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 4;
    double rightTopX = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 4;
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     leftTopX, nodeDrawProp.getUpBorder(),
                                                     rightTopX, nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(SvgConstants.POINTS, points);
  }

  private void parallelogram(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double leftTopX = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 5;
    double rightDownX = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 5;
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     leftTopX, nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getUpBorder(),
                                                     rightDownX, nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder());
    shapeElement.setAttribute(SvgConstants.POINTS, points);
  }

  private void start(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double outerRadius = nodeDrawProp.getHeight() / 2;
    double innerRadius = outerRadius / StarPropCalc.IN_OUT_RATIO;

    double arc = StarPropCalc.START_ARC;
    double[] ps = new double[22];
    for (int i = 0; i < 10; i++) {
      if (i % 2 == 0) {
        ps[i * 2] = nodeDrawProp.getX() + Math.cos(arc) * outerRadius;
        ps[i * 2 + 1] = nodeDrawProp.getY() - Math.sin(arc) * outerRadius;
      } else {
        ps[i * 2] = nodeDrawProp.getX() + Math.cos(arc) * innerRadius;
        ps[i * 2 + 1] = nodeDrawProp.getY() - Math.sin(arc) * innerRadius;
      }
      ps[20] = ps[0];
      ps[21] = ps[1];

      arc += StarPropCalc.UNIT_ARC;
    }

    shapeElement.setAttribute(SvgConstants.POINTS, SvgEditor.generatePolylinePoints(ps));
  }

  private void note(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    String nodeId = brush.nodeId(nodeDrawProp.getNode());
    brush.getShapeElement(nodeDrawProp, getShapeElement(nodeDrawProp));
    String shape = NodeShapeEnum.NOTE.getName();

    Element firstEle = brush.getOrCreateChildElementById(nodeId + shape + "0", SvgConstants.POLYGON_ELE);
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getLeftBorder(), nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder()
                                               - NotePropCalc.RIGHT_UP_LEN,
                                                     nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getUpBorder() + NotePropCalc.RIGHT_UP_LEN,
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getUpBorder());
    firstEle.setAttribute(SvgConstants.POINTS, points);

    Element secondEle = brush.getOrCreateChildElementById(nodeId + shape + "1", SvgConstants.POLYGON_ELE);
    points = SvgEditor.generatePolylinePoints(nodeDrawProp.getRightBorder() - NotePropCalc.RIGHT_UP_LEN,
                                              nodeDrawProp.getUpBorder(),
                                              nodeDrawProp.getRightBorder() - NotePropCalc.RIGHT_UP_LEN,
                                              nodeDrawProp.getUpBorder() + NotePropCalc.RIGHT_UP_LEN,
                                              nodeDrawProp.getRightBorder(),
                                              nodeDrawProp.getUpBorder() + NotePropCalc.RIGHT_UP_LEN);
    secondEle.setAttribute(SvgConstants.POINTS, points);

    brush.addGroup(SvgConstants.SHAPE_GROUP_KEY, Arrays.asList(firstEle, secondEle));
  }

  private void cylinder(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    String nodeId = brush.nodeId(nodeDrawProp.getNode());
    brush.getShapeElement(nodeDrawProp, getShapeElement(nodeDrawProp));
    String shape = NodeShapeEnum.CYLINDER.getName();

    double up = nodeDrawProp.getUpBorder() + CylinderPropCalc.TOP_LEN;
    double down = nodeDrawProp.getDownBorder() - CylinderPropCalc.TOP_LEN;
    double v2x = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 4;
    double v3x = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 4;
    String points = SvgEditor.pointsToSvgPath(true, nodeDrawProp.getLeftBorder(), up,
                                              nodeDrawProp.getLeftBorder(), up,
                                              nodeDrawProp.getLeftBorder(), down,
                                              nodeDrawProp.getLeftBorder(), down, v2x,
                                              nodeDrawProp.getDownBorder(), v3x,
                                              nodeDrawProp.getDownBorder(),
                                              nodeDrawProp.getRightBorder(), down,
                                              nodeDrawProp.getRightBorder(), down,
                                              nodeDrawProp.getRightBorder(), up,
                                              nodeDrawProp.getRightBorder(), up, v3x,
                                              nodeDrawProp.getUpBorder(), v2x,
                                              nodeDrawProp.getUpBorder(),
                                              nodeDrawProp.getLeftBorder(), up);
    Element firstEle = brush.getOrCreateChildElementById(nodeId + shape + "0", SvgConstants.PATH_ELE);
    firstEle.setAttribute(SvgConstants.D, points);

    points = SvgEditor.pointsToSvgPath(true, nodeDrawProp.getLeftBorder(), up,
                                       v2x, up + CylinderPropCalc.TOP_LEN,
                                       v3x, up + CylinderPropCalc.TOP_LEN,
                                       nodeDrawProp.getRightBorder(), up);
    Element secondEle = brush.getOrCreateChildElementById(nodeId + shape + "1", SvgConstants.PATH_ELE);
    secondEle.setAttribute(SvgConstants.D, points);

    brush.addGroup(SvgConstants.SHAPE_GROUP_KEY, Arrays.asList(firstEle, secondEle));
  }

  private void record(NodeDrawProp nodeDrawProp, SvgBrush brush, boolean radianCorner) {
    RootCell labelCell = nodeDrawProp.getLabelCell();
    if (labelCell == null) {
      return;
    }

    // Draw border of node
    String nodeId = brush.nodeId(nodeDrawProp.getNode());
    String borderId = nodeId + NodeShapeEnum.RECORD.getName() + "0";
    Element border;
    if (radianCorner) {
      border = brush.getOrCreateChildElementById(borderId, SvgConstants.PATH_ELE);
      border.setAttribute(SvgConstants.D, mrecordBorder(nodeDrawProp));
    } else {
      border = brush.getOrCreateChildElementById(borderId, SvgConstants.POLYGON_ELE);
      border.setAttribute(SvgConstants.POINTS, SvgEditor.generateBox(nodeDrawProp));
    }

    List<Element> cellElements = new ArrayList<>(labelCell.childrenSize());
    cellElements.add(border);

    // Draw cell
    record(nodeDrawProp, labelCell, brush, nodeId, new int[]{1}, cellElements);
    brush.addGroup(SvgConstants.SHAPE_GROUP_KEY, cellElements);
  }

  private void record(NodeDrawProp nodeDrawProp, LabelCell cell, SvgBrush brush,
                      String nodeId, int[] cellNo, List<Element> cellElements) {
    if (cell.isLeaf()) {
      return;
    }

    int no = ++cellNo[0];
    for (int i = 0; i < cell.childrenSize(); i++) {
      LabelCell child = cell.getChild(i);
      record(nodeDrawProp, child, brush, nodeId, cellNo, cellElements);

      // Get the leftUp position by offset
      FlatPoint offset = child.getOffset();
      double upBorder = nodeDrawProp.getUpBorder();
      double leftBorder = nodeDrawProp.getLeftBorder();
      upBorder += offset.getY();
      leftBorder += offset.getX();
      String cellId = nodeId + NodeShapeEnum.RECORD.getName() + no + SvgConstants.UNDERSCORE + i;

      // Set cell label element
      if (child.isLeaf() && StringUtils.isNotEmpty(child.getLabel())) {
        FlatPoint labelCenter = child.getCenter(nodeDrawProp);
        recordTextSet(nodeDrawProp, brush, child.getLabel(), cellId, labelCenter);
      }

      // Select whether to ignore drawing of the split symbol based on isHor().
      if (needIgnoreDrawSplit(i, cell.childrenSize(), child)) {
        continue;
      }

      // Set split element
      setRecordSplitEle(cellId, cell, child, brush, upBorder, leftBorder, cellElements);
    }
  }

  private String mrecordBorder(NodeDrawProp nodeDrawProp) {
    return SvgEditor.roundedBox(RecordPropCalc.CORNER_LEN, nodeDrawProp);
  }

  private void setRecordSplitEle(String cellId, LabelCell parent, LabelCell child, SvgBrush brush,
                                 double upBorder, double leftBorder, List<Element> cellElements) {
    double cellRightBorder = leftBorder + child.getWidth();
    Element cellEle = brush.getOrCreateChildElementById(cellId, SvgConstants.PATH_ELE);

    String points;
    if (child.isHor()) {
      points = SvgEditor.pointsToSvgPath(false, cellRightBorder, upBorder,
                                         cellRightBorder, upBorder + parent.getHeight());

    } else {
      points = SvgEditor.pointsToSvgPath(false, leftBorder, upBorder, cellRightBorder, upBorder);
    }
    cellEle.setAttribute(SvgConstants.D, points);
    cellElements.add(cellEle);
  }

  private void recordTextSet(NodeDrawProp nodeDrawProp, SvgBrush brush, String label,
                             String cellId, FlatPoint labelCenter) {
    NodeAttrs nodeAttrs = nodeDrawProp.nodeAttrs();
    double fontSize = nodeAttrs.getFontSize() == null ? 0D : nodeAttrs.getFontSize();

    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      String id = SvgBrush.getId(
          brush.nodeId(nodeDrawProp.getNode()),
          cellId + SvgConstants.TEXT_ELE + SvgConstants.UNDERSCORE + textLineAttribute.getLineNo()
      );
      Element text = brush.getOrCreateChildElementById(id, SvgConstants.TEXT_ELE);
      SvgEditor.setText(text, fontSize, textLineAttribute);
      text.setTextContent(textLineAttribute.getLine());
    };

    SvgEditor.text(new TextAttribute(labelCenter, fontSize, label,
                                     nodeAttrs.getFontColor(), nodeAttrs.getFontName(), lineConsumer));
  }

  private boolean needIgnoreDrawSplit(int idx, int size, LabelCell current) {
    return (current.isHor() && idx == size - 1) || (!current.isHor() && idx == 0);
  }
}