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

package org.graphper.def;

import java.util.ArrayList;
import java.util.List;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

public class RectangleTree<B extends Box> {

  private final int maxNodeCapacity;

  private Node root;

  public RectangleTree(int maxNodeCapacity) {
    Asserts.illegalArgument(maxNodeCapacity < 2, "max node capacity cannot be less than 2");
    this.maxNodeCapacity = maxNodeCapacity;
  }

  public void insert(B box) {
    if (box == null) {
      return;
    }

    if (root == null) {
      root = new Node();
    }
  }

  public void delete(B mbr) {

  }

  public List<B> search(Box searchBox) {
    return null;
  }

  public List<B> getAll() {
    return null;
  }

  public int size() {
    return 0;
  }

  private NodePair insertion(Node node, B box) {
    if (node.isLeaf && node.add(box)) {
      return new NodePair(node);
    }

    MBR minIncrBox = selectMinIncrNode(node, box);
    if (node.isLeaf) {

    } else {
      NodePair nodePair = insertion(minIncrBox.next, box);
      if (nodePair.right == null) {
        nodePair.left = node;
        return nodePair;
      }


    }
  }

  private NodePair split(Node node, B box) {
    return null;
  }

  private MBR selectMinIncrNode(Node node, B box) {
    return null;
  }

  private class NodePair {

    private Node left;

    private Node right;

    public NodePair(Node left) {
      this.left = left;
    }
  }

  private class Node extends DefaultBox {

    private boolean isRoot;

    private boolean isLeaf;

    private MBR parent;

    private List<MBR> boundingBoxes;

    private boolean add(B box) {
      if (boundingBoxes == null) {
        boundingBoxes = new ArrayList<>(maxNodeCapacity);
      }
      if (isFull()) {
        return false;
      }
//      boundingBoxes.add(box);
      return true;
    }

    private boolean isFull() {
      return CollectionUtils.isNotEmpty(boundingBoxes) && boundingBoxes.size() == maxNodeCapacity;
    }

    private boolean isLeaf() {
      return isLeaf;
    }

    public boolean containment(Box box) {
      if (isRoot) {
        return true;
      }

      return getLeftBorder() <= box.getLeftBorder()
          && getRightBorder() >= box.getRightBorder()
          && getUpBorder() <= box.getUpBorder()
          && getDownBorder() >= box.getDownBorder();
    }

    @Override
    public boolean isOverlap(Box box) {
      if (isRoot) {
        return true;
      }

      return super.isOverlap(box);
    }
  }

  private class MBR extends DefaultBox {

    private Box box;

    private Node next;
  }
}
