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

package org.graphper.draw;

import java.io.Serializable;
import org.graphper.api.Assemble;
import org.graphper.api.Node;
import org.graphper.api.NodeAttrs;
import org.graphper.api.attributes.Labelloc;
import org.graphper.api.attributes.NodeShape;
import org.graphper.def.FlatPoint;
import org.graphper.layout.Cell.RootCell;
import org.graphper.util.Asserts;

/**
 * Node's rendering description object.
 *
 * @author Jamison Jiang
 */
public class NodeDrawProp extends ContainerDrawProp implements Serializable {

  private static final long serialVersionUID = 2785583326128769032L;

  private NodeAttrs nodeAttrs;

  private int id;

  private final Node node;

  private final FlatPoint margin;

  private double labelHorOffset;

  private double labelVerOffset;

  private RootCell Cell;

  private boolean isCellProp;

  private ContainerDrawProp cellContainer;

  public NodeDrawProp(Node node, NodeAttrs nodeAttrs) {
    Asserts.nullArgument(node, "node");
    Asserts.nullArgument(node, "nodeAttrs");
    this.node = node;
    this.nodeAttrs = nodeAttrs;
    this.margin = nodeAttrs.getMargin();
    convertTable(nodeAttrs.getTable());
  }

  public Node getNode() {
    return node;
  }

  public void setNodeAttrs(NodeAttrs nodeAttrs) {
    Asserts.nullArgument(nodeAttrs, "nodeAttrs");
    this.nodeAttrs = nodeAttrs;
  }

  public NodeAttrs nodeAttrs() {
    return nodeAttrs;
  }

  public String id() {
    return String.valueOf(id);
  }

  public int nodeNo() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  protected Labelloc labelloc() {
    return nodeAttrs.getLabelloc();
  }

  @Override
  protected FlatPoint margin() {
    return margin;
  }

  @Override
  protected String containerId() {
    return String.valueOf(id);
  }

  @Override
  protected Assemble assemble() {
    return nodeAttrs.getAssemble();
  }

  @Override
  public NodeShape nodeShape() {
    return nodeAttrs.getNodeShape();
  }

  public double getLabelHorOffset() {
    return labelHorOffset;
  }

  public void setLabelHorOffset(double labelHorOffset) {
    this.labelHorOffset = labelHorOffset;
  }

  public double getLabelVerOffset() {
    return labelVerOffset;
  }

  public void setLabelVerOffset(double labelVerOffset) {
    this.labelVerOffset = labelVerOffset;
  }

  public RootCell getCell() {
    return Cell;
  }

  public void setCell(RootCell Cell) {
    this.Cell = Cell;
  }

  public void setCellContainer(NodeDrawProp cellContainer) {
    this.cellContainer = cellContainer;
  }

  public boolean haveChildrenCell() {
    return getAssemble() != null;
  }

  public boolean noChildrenCell() {
    return !haveChildrenCell();
  }

  public void markIsCellProp() {
    this.isCellProp = true;
  }

  public boolean isCellProp() {
    return isCellProp;
  }

  public boolean isNotCellProp() {
    return !isCellProp();
  }

  public void initCellPos() {
    if (!isCellProp()) {
      return;
    }

    FlatPoint ls = cellContainer.getLabelSize();
    FlatPoint lc = cellContainer.getLabelCenter();
    Assemble assemble = cellContainer.getAssemble();
    if (assemble == null) {
      return;
    }

    initCellPos(ls, lc, assemble);
  }

  public void initCellPos(FlatPoint ls, FlatPoint lc, Assemble assemble) {
    Asserts.illegalArgument(lc == null || ls == null || assemble == null,
                            "Cell container properties error");

    double width = getWidth();
    double height = getHeight();
    double horOffset = assemble.horOffset(node);
    double verOffset = assemble.verOffset(node);
    setLeftBorder(lc.getX() - ls.getWidth() / 2 + horOffset);
    setRightBorder(getLeftBorder() + width);
    setUpBorder(lc.getY() - ls.getHeight() / 2 + verOffset);
    setDownBorder(getUpBorder() + height);
  }
}
