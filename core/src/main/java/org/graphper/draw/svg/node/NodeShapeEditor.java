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

import java.util.List;
import java.util.function.Consumer;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.ext.CylinderPropCalc;
import org.graphper.api.ext.NotePropCalc;
import org.graphper.api.ext.RecordPropCalc;
import org.graphper.api.ext.StarPropCalc;
import org.graphper.api.ext.TrianglePropCalc;
import org.graphper.def.FlatPoint;
import org.graphper.draw.CustomizeShapeRender;
import org.graphper.draw.NodeDrawProp;
import org.graphper.draw.svg.Element;
import org.graphper.draw.svg.SvgBrush;
import org.graphper.draw.svg.SvgConstants;
import org.graphper.draw.svg.SvgEditor;
import org.graphper.draw.svg.SvgEditor.TextAttribute;
import org.graphper.draw.svg.SvgEditor.TextLineAttribute;
import org.graphper.layout.Cell;
import org.graphper.layout.Cell.RootCell;
import org.graphper.layout.HtmlConvertor;
import org.graphper.layout.HtmlConvertor.PositionedText;
import org.graphper.layout.LabelAttributes;
import org.graphper.util.CollectionUtils;
import org.graphper.util.FontUtils;
import org.graphper.util.LabelTagUtils;

public class NodeShapeEditor extends AbstractNodeShapeEditor {

  @Override
  public boolean edit(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    NodeShape nodeShape = nodeDrawProp.nodeAttrs().getShape();

    CustomizeShapeRender customizeShapeRender = CustomizeShapeRender
        .getCustomizeShapeRender(nodeShape.getName());
    if (customizeShapeRender != null) {
      customizeShapeRender.drawNodeSvg(brush, nodeDrawProp);
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
      case BOX:
      case RECT:
        rect(nodeDrawProp, brush);
        break;
      case TRIANGLE:
      case INVTRIANGLE:
        TrianglePropCalc trianglePropCalc = (TrianglePropCalc) nodeShape.getShapePropCalc();
        triangle(nodeDrawProp, brush, trianglePropCalc.isPositive());
        break;
      case DIAMOND:
        diamond(nodeDrawProp, brush);
        break;
      case TRAPEZIUM:
        trapezium(nodeDrawProp, brush);
        break;
      case PARALLELOGRAM:
        parallelogram(nodeDrawProp, brush);
        break;
      case STAR:
        start(nodeDrawProp, brush);
        break;
      case NOTE:
        note(nodeDrawProp, brush);
        break;
      case CYLINDER:
        cylinder(nodeDrawProp, brush);
        break;
      case RECORD:
        record(nodeDrawProp, brush, nodeDrawProp.containsRounded());
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
    return brush.getOrCreateChildElement(getShapeElement(nodeDrawProp));
  }

  private void ellipse(NodeDrawProp nodeDrawProp, Element shapeElement) {
    ellipse(nodeDrawProp, shapeElement, nodeDrawProp.getHeight() / 2, nodeDrawProp.getWidth() / 2);
  }

  private void circle(NodeDrawProp nodeDrawProp, Element shapeElement) {
    double radius = nodeDrawProp.getHeight() / 2;
    ellipse(nodeDrawProp, shapeElement, radius, radius);
  }

  private void ellipse(NodeDrawProp nodeDrawProp, Element shapeElement,
                       double height, double width) {
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

  private void rect(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    SvgEditor.polygonShape(nodeDrawProp, brush, nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getUpBorder(), nodeDrawProp.getRightBorder(),
                           nodeDrawProp.getUpBorder(), nodeDrawProp.getRightBorder(),
                           nodeDrawProp.getDownBorder(), nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getDownBorder(), nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getUpBorder());
  }

  private void triangle(NodeDrawProp nodeDrawProp, SvgBrush brush, boolean positive) {
    if (positive) {
      SvgEditor.polygonShape(nodeDrawProp, brush,
                             nodeDrawProp.getX(),
                             nodeDrawProp.getUpBorder(),
                             nodeDrawProp.getLeftBorder(),
                             nodeDrawProp.getDownBorder(),
                             nodeDrawProp.getRightBorder(),
                             nodeDrawProp.getDownBorder(),
                             nodeDrawProp.getX(),
                             nodeDrawProp.getUpBorder());
    } else {
      SvgEditor.polygonShape(nodeDrawProp, brush,
                             nodeDrawProp.getX(),
                             nodeDrawProp.getDownBorder(),
                             nodeDrawProp.getLeftBorder(),
                             nodeDrawProp.getUpBorder(),
                             nodeDrawProp.getRightBorder(),
                             nodeDrawProp.getUpBorder(),
                             nodeDrawProp.getX(),
                             nodeDrawProp.getDownBorder());
    }
  }

  private void diamond(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    SvgEditor.polygonShape(nodeDrawProp, brush, nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getY(),
                           nodeDrawProp.getX(),
                           nodeDrawProp.getUpBorder(),
                           nodeDrawProp.getRightBorder(),
                           nodeDrawProp.getY(),
                           nodeDrawProp.getX(),
                           nodeDrawProp.getDownBorder(),
                           nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getY());
  }

  private void trapezium(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    double leftTopX = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 4;
    double rightTopX = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 4;
    SvgEditor.polygonShape(nodeDrawProp, brush, nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getDownBorder(),
                           leftTopX, nodeDrawProp.getUpBorder(),
                           rightTopX, nodeDrawProp.getUpBorder(),
                           nodeDrawProp.getRightBorder(),
                           nodeDrawProp.getDownBorder(),
                           nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getDownBorder());
  }

  private void parallelogram(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    double leftTopX = nodeDrawProp.getLeftBorder() + nodeDrawProp.getWidth() / 5;
    double rightDownX = nodeDrawProp.getRightBorder() - nodeDrawProp.getWidth() / 5;
    SvgEditor.polygonShape(nodeDrawProp, brush, nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getDownBorder(),
                           leftTopX, nodeDrawProp.getUpBorder(),
                           nodeDrawProp.getRightBorder(),
                           nodeDrawProp.getUpBorder(),
                           rightDownX, nodeDrawProp.getDownBorder(),
                           nodeDrawProp.getLeftBorder(),
                           nodeDrawProp.getDownBorder());
  }

  private void start(NodeDrawProp nodeDrawProp, SvgBrush brush) {
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

    SvgEditor.polygonShape(nodeDrawProp, brush, ps);
  }

  private void note(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    brush.getOrCreateChildElement(getShapeElement(nodeDrawProp));
    String shape = NodeShapeEnum.NOTE.getName();

    Element firstEle = brush.getOrCreateShapeEleById(shape + "0", POLYGON_ELE);
    String points = SvgEditor.generatePolylinePoints(nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder()
                                                         - NotePropCalc.RIGHT_UP_LEN,
                                                     nodeDrawProp.getUpBorder(),
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getUpBorder()
                                                         + NotePropCalc.RIGHT_UP_LEN,
                                                     nodeDrawProp.getRightBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getDownBorder(),
                                                     nodeDrawProp.getLeftBorder(),
                                                     nodeDrawProp.getUpBorder());
    firstEle.setAttribute(SvgConstants.POINTS, points);

    Element secondEle = brush.getOrCreateShapeEleById(shape + "1", POLYGON_ELE);
    points = SvgEditor.generatePolylinePoints(
        nodeDrawProp.getRightBorder() - NotePropCalc.RIGHT_UP_LEN,
        nodeDrawProp.getUpBorder(),
        nodeDrawProp.getRightBorder() - NotePropCalc.RIGHT_UP_LEN,
        nodeDrawProp.getUpBorder() + NotePropCalc.RIGHT_UP_LEN,
        nodeDrawProp.getRightBorder(),
        nodeDrawProp.getUpBorder() + NotePropCalc.RIGHT_UP_LEN);
    secondEle.setAttribute(SvgConstants.POINTS, points);
  }

  private void cylinder(NodeDrawProp nodeDrawProp, SvgBrush brush) {
    brush.getOrCreateChildElement(getShapeElement(nodeDrawProp));
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
    Element firstEle = brush.getOrCreateShapeEleById(shape + "0", PATH_ELE);
    firstEle.setAttribute(SvgConstants.D, points);

    points = SvgEditor.pointsToSvgPath(true, nodeDrawProp.getLeftBorder(), up,
                                       v2x, up + CylinderPropCalc.TOP_LEN,
                                       v3x, up + CylinderPropCalc.TOP_LEN,
                                       nodeDrawProp.getRightBorder(), up);
    Element secondEle = brush.getOrCreateShapeEleById(shape + "1", PATH_ELE);
    secondEle.setAttribute(SvgConstants.D, points);
  }

  private void record(NodeDrawProp nodeDrawProp, SvgBrush brush, boolean radianCorner) {
    RootCell cell = nodeDrawProp.getCell();
    if (cell == null) {
      return;
    }

    // Draw border of node
    String borderId = NodeShapeEnum.RECORD.getName() + "0";
    Element border;
    if (radianCorner) {
      border = brush.getOrCreateShapeEleById(borderId, SvgConstants.PATH_ELE);
      border.setAttribute(SvgConstants.D, mrecordBorder(nodeDrawProp));
    } else {
      border = brush.getOrCreateShapeEleById(borderId, SvgConstants.POLYGON_ELE);
      border.setAttribute(SvgConstants.POINTS, SvgEditor.generateBox(nodeDrawProp));
    }

    if (!nodeDrawProp.haveChildrenCell()) {
      // Draw cell
      record(nodeDrawProp, cell, brush, new int[]{1});
    }
  }

  private void record(NodeDrawProp nodeDrawProp, Cell cell, SvgBrush brush, int[] cellNo) {
    if (cell.isLeaf()) {
      return;
    }

    int no = ++cellNo[0];
    for (int i = 0; i < cell.childrenSize(); i++) {
      Cell child = cell.getChild(i);
      record(nodeDrawProp, child, brush, cellNo);

      // Get the leftUp position by offset
      FlatPoint offset = child.getOffset();
      double upBorder = nodeDrawProp.getUpBorder();
      double leftBorder = nodeDrawProp.getLeftBorder();
      upBorder += offset.getY();
      leftBorder += offset.getX();
      String cellId = NodeShapeEnum.RECORD.getName() + no + SvgConstants.UNDERSCORE + i;

      // Set cell label element
      if (child.isLeaf()) {
        if (child.getLabelTag() != null) {
          recordRichTextSet(nodeDrawProp, brush, child, cellId);
        } else if (StringUtils.isNotEmpty(child.getLabel())) {
          FlatPoint labelCenter = child.getCenter(nodeDrawProp);
          recordTextSet(nodeDrawProp, brush, child.getLabel(), cellId, labelCenter,
                        child.getWidth());
        }
      }

      // Select whether to ignore drawing of the split symbol based on isHor().
      if (needIgnoreDrawSplit(i, cell.childrenSize(), child)) {
        continue;
      }

      // Set split element
      setRecordSplitEle(cellId, cell, child, brush, upBorder, leftBorder);
    }
  }

  private String mrecordBorder(NodeDrawProp nodeDrawProp) {
    return SvgEditor.roundedBox(RecordPropCalc.CORNER_LEN, nodeDrawProp);
  }

  private void setRecordSplitEle(String cellId, Cell parent, Cell child, SvgBrush brush,
                                 double upBorder, double leftBorder) {
    double cellRightBorder = leftBorder + child.getWidth();
    Element cellEle = brush.getOrCreateShapeEleById(cellId, SvgConstants.PATH_ELE);

    String points;
    if (child.isHor()) {
      points = SvgEditor.pointsToSvgPath(false, cellRightBorder, upBorder,
                                         cellRightBorder, upBorder + parent.getHeight());

    } else {
      points = SvgEditor.pointsToSvgPath(false, leftBorder, upBorder, cellRightBorder, upBorder);
    }
    cellEle.setAttribute(SvgConstants.D, points);
  }

  private void recordTextSet(NodeDrawProp nodeDrawProp, SvgBrush brush, String label,
                              String cellId, FlatPoint labelCenter, double labelWidth) {
    NodeAttrs nodeAttrs = nodeDrawProp.nodeAttrs();
    double fontSize = nodeAttrs.getFontSize() == null ? 0D : nodeAttrs.getFontSize();

    Consumer<TextLineAttribute> lineConsumer = textLineAttribute -> {
      String id = cellId + TEXT_ELE + UNDERSCORE + textLineAttribute.getLineNo();
      Element text = brush.getOrCreateChildElementById(id, TEXT_ELE);
      SvgEditor.setText(text, fontSize, textLineAttribute);
      text.setTextContent(textLineAttribute.getLine());
      SvgEditor.setFontStyle(text, nodeAttrs.getFontStyles());
    };

    SvgEditor.text(new TextAttribute(labelCenter, fontSize, label,
                                      nodeAttrs.getFontColor(), nodeAttrs.getFontName(),
                                      labelWidth, lineConsumer));
  }

  /**
   * Draws a record cell whose body is rich text, one {@code <text>} element per styled run.
   *
   * <p>The runs and their offsets come from the same layout pass that html labels use
   * ({@link HtmlConvertor#toPositionedTexts}), so a bold or coloured fragment lands in exactly the
   * place the measurement stage reserved for it. Lowering the cell to an {@code Assemble} of
   * sub-nodes — the way standalone html labels render — is not an option here: the sub-nodes would
   * replace the cell tree that {@code tailCell}/{@code headCell} resolve ports against.
   */
  private void recordRichTextSet(NodeDrawProp nodeDrawProp, SvgBrush brush, Cell cell,
                                 String cellId) {
    NodeAttrs nodeAttrs = nodeDrawProp.nodeAttrs();
    LabelAttributes attrs = new LabelAttributes();
    attrs.setFontName(nodeAttrs.getFontName());
    attrs.setFontSize(nodeAttrs.getFontSize() == null ? 0D : nodeAttrs.getFontSize());
    attrs.setFontColor(nodeAttrs.getFontColor());
    attrs.setByStyles(nodeAttrs.getFontStyles());

    List<PositionedText> texts = HtmlConvertor.toPositionedTexts(cell.getLabelTag(), attrs);
    if (CollectionUtils.isEmpty(texts)) {
      return;
    }

    /*
     * Runs are positioned relative to the top-left of the rich text block, and the block is centered
     * inside the cell. The cell was sized from the same measurement, so the block fits by
     * construction; margin, if any, becomes the surrounding padding.
     */
    FlatPoint labelSize = LabelTagUtils.measure(cell.getLabelTag(), attrs);
    FlatPoint cellCenter = cell.getCenter(nodeDrawProp);
    double left = cellCenter.getX() - labelSize.getWidth() / 2;
    double top = cellCenter.getY() - labelSize.getHeight() / 2;

    int no = 0;
    for (PositionedText text : texts) {
      if (StringUtils.isEmpty(text.getText())) {
        continue;
      }

      String id = cellId + TEXT_ELE + UNDERSCORE + "rich" + UNDERSCORE + no++;
      Element textEle = brush.getOrCreateChildElementById(id, TEXT_ELE);

      double fontSize = text.getFontSize();
      double glyphCenterY;
      if (!text.isScriptShift()) {
        /*
         * The run's box is exactly its measured size, so the glyphs are centered in it. Using the font
         * size as the glyph height instead would sit about a pixel high, since the measured height of
         * a line exceeds the point size.
         */
        glyphCenterY = top + text.getY() + text.getHeight() / 2;
      } else if (text.getVerAlign() == Labelloc.BOTTOM) {
        // Subscript: half-size glyphs pushed to the bottom of a full-size box
        glyphCenterY = top + text.getY() + text.getHeight() - fontSize / 2;
      } else {
        // Superscript: half-size glyphs pulled to the top of a full-size box
        glyphCenterY = top + text.getY() + fontSize / 2;
      }

      // Same baseline convention as SvgEditor.text: one third of the font size below the center
      double baseline = glyphCenterY + fontSize / 3;
      double centerX = left + text.getX() + text.getWidth() / 2;

      textEle.setAttribute(SvgConstants.X, String.valueOf(centerX));
      textEle.setAttribute(SvgConstants.Y, String.valueOf(baseline));
      textEle.setAttribute(SvgConstants.TEXT_ANCHOR, SvgConstants.MIDDLE);
      textEle.setAttribute(SvgConstants.FONT_SIZE, String.valueOf(fontSize));

      Color fontColor = text.getFontColor() == null ? nodeAttrs.getFontColor()
          : text.getFontColor();
      if (fontColor != null) {
        textEle.setAttribute(SvgConstants.FILL, fontColor.value());
      }

      String fontName = text.getFontName() == null ? nodeAttrs.getFontName() : text.getFontName();
      if (fontName != null) {
        textEle.setAttribute(SvgConstants.FONT_FAMILY,
                             FontUtils.fontExists(fontName) ? fontName : FontUtils.DEFAULT_FONT);
      }

      SvgEditor.setFontStyle(textEle, text.getFontStyles());
      textEle.setTextContent(text.getText());
    }
  }

  private boolean needIgnoreDrawSplit(int idx, int size, Cell current) {
    return (current.isHor() && idx == size - 1) || (!current.isHor() && idx == 0);
  }
}
