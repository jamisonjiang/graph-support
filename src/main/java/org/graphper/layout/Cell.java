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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.attributes.NodeShape;
import org.graphper.api.attributes.NodeShapeEnum;
import org.graphper.api.attributes.Rankdir;
import org.graphper.api.ext.Box;
import org.graphper.def.FlatPoint;
import org.graphper.draw.Rectangle;
import org.graphper.util.CollectionUtils;

public class Cell {

  protected final boolean isHor;

  protected String id;

  protected String label;

  protected double width;

  protected double height;

  protected FlatPoint offset;

  protected Cell parent;

  protected List<Cell> children;

  protected NodeShape shape = NodeShapeEnum.RECT;

  public Cell(boolean isHor) {
    this.isHor = isHor;
  }

  public boolean isHor() {
    return isHor;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public FlatPoint getOffset() {
    return offset;
  }

  public void setOffset(FlatPoint offset) {
    this.offset = offset;
  }

  public Cell getParent() {
    return parent;
  }

  public void setParent(Cell parent) {
    this.parent = parent;
  }

  public void setChildren(List<Cell> children) {
    this.children = children;
  }

  public NodeShape getShape() {
    return shape;
  }

  public void setShape(NodeShape shape) {
    this.shape = shape;
  }

  public int childrenSize() {
    if (children == null) {
      return 0;
    }
    return children.size();
  }

  public Cell getChild(int i) {
    if (children == null) {
      return null;
    }
    if (i < 0 || i >= children.size()) {
      return null;
    }
    return children.get(i);
  }

  public List<Cell> getChildren() {
    if (CollectionUtils.isEmpty(children)) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(children);
  }

  public boolean isLeaf() {
    return CollectionUtils.isEmpty(children);
  }

  public FlatPoint getCenter(Box box) {
    if (box == null || offset == null) {
      return new FlatPoint(width / 2, height / 2);
    }

    double upBorder = box.getUpBorder();
    double leftBorder = box.getLeftBorder();
    upBorder += offset.getY();
    leftBorder += offset.getX();
    return new FlatPoint(leftBorder + width / 2, upBorder + height / 2);
  }

  public Rectangle getCellBox(Box box) {
    FlatPoint cellCenter = getCenter(box);
    Rectangle cellRect = new Rectangle();
    cellRect.setUpBorder(cellCenter.getY() - getHeight() / 2);
    cellRect.setDownBorder(cellCenter.getY() + getHeight() / 2);
    cellRect.setLeftBorder(cellCenter.getX() - getWidth() / 2);
    cellRect.setRightBorder(cellCenter.getX() + getWidth() / 2);
    return cellRect;
  }

  public void flip(Rankdir rankdir, Box rootBox) {
    if (rankdir == null || rankdir == Rankdir.TB || rootBox == null) {
      return;
    }

    if (rankdir == Rankdir.BT) {
      if (offset != null) {
        offset.setY(rootBox.getHeight() - offset.getY() - height);
      }
    } else {
      double tmp;
      if (offset != null) {
        if (rankdir == Rankdir.LR) {
          tmp = offset.getY();
          offset.setY(offset.getX());
          offset.setX(rootBox.getHeight() - tmp - height);
        }
        if (rankdir == Rankdir.RL) {
          tmp = offset.getX();
          offset.setX(rootBox.getHeight() - offset.getY() - height);
          offset.setY(rootBox.getWidth() - tmp - width);
        }
      }

      tmp = height;
      height = width;
      width = tmp;
    }

    if (CollectionUtils.isEmpty(children)) {
      return;
    }

    for (Cell child : children) {
      child.flip(rankdir, rootBox);
    }
  }

  public static class RootCell extends Cell {

    private Map<String, Cell> idRecord;

    public RootCell(boolean isHor) {
      super(isHor);
      this.offset = new FlatPoint(0, 0);
    }

    void put(String id, Cell cell) {
      if (StringUtils.isEmpty(id) || cell == null) {
        return;
      }

      if (idRecord == null) {
        idRecord = new HashMap<>(2);
      }
      idRecord.put(id, cell);
    }

    public Cell getCellById(String id) {
      if (idRecord == null) {
        return null;
      }

      return idRecord.get(id);
    }
  }
}
