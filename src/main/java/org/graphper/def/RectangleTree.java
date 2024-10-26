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
      root = new Node(box);
      return;
    }

    insertion(root, box);
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

  private void insertion(Node node, B box) {
    if (!node.isLeaf()) {
      Node minIncrNode = selectAndUpdateMinIncrNode(node, box);
      insertion(minIncrNode, box);
      return;
    }

    Node insertNode = new Node(box);
    if (node.isFull()) {
      split(node, insertNode);
    } else {
      node.add(insertNode);
    }
  }

  private void split(Node node, Node insertNode) {
    List<Node> nodes = node.split(insertNode);
    Node newNode = new Node(null);
    newNode.children = nodes;

    Node parent = node.parent;
    if (parent == null) {
      parent = new Node(null);
      parent.add(node);
      this.root = parent;
    }

    if (parent.isFull()) {
      split(parent, newNode);
    } else {
      parent.add(newNode);
    }
  }

  private Node selectAndUpdateMinIncrNode(Node node, B box) {
    Node minIncrNode = null;
    Box minCombineBox = null;
    double minIncArea = Double.MAX_VALUE;
    for (Node child : node.getChildren()) {
      Box combine = newCombineBox(child, box);
      double incrArea = combine.getArea() - child.getArea();

      if (minCombineBox == null || minIncArea > incrArea) {
        minCombineBox = combine;
        minIncArea = incrArea;
        minIncrNode = child;
      }
    }

    minIncrNode.setLeftBorder(minCombineBox.getLeftBorder());
    minIncrNode.setRightBorder(minCombineBox.getRightBorder());
    minIncrNode.setUpBorder(minCombineBox.getUpBorder());
    minIncrNode.setDownBorder(minCombineBox.getDownBorder());
    return minIncrNode;
  }

  private Box newCombineBox(Box origin, Box expand) {
    return new DefaultBox(
        Math.min(origin.getLeftBorder(), expand.getLeftBorder()),
        Math.max(origin.getRightBorder(), expand.getRightBorder()),
        Math.min(origin.getUpBorder(), expand.getUpBorder()),
        Math.max(origin.getDownBorder(), expand.getDownBorder())
    );
  }

  private class NodePair {

    private Node origin;

    private Node splitting;

    public NodePair(Node origin) {
      this.origin = origin;
    }
  }

  private class Node extends DefaultBox {

    private B box;

    private Node parent;

    private List<Node> children;

    public Node(B box) {
      this.box = box;
    }

    private boolean add(Node child) {
      if (children == null) {
        children = new ArrayList<>(maxNodeCapacity);
      }
      if (isFull()) {
        return false;
      }

      children.add(child);
      setLeftBorder(Math.min(getLeftBorder(), child.getLeftBorder()));
      setRightBorder(Math.max(getRightBorder(), child.getRightBorder()));
      setUpBorder(Math.min(getUpBorder(), child.getUpBorder()));
      setDownBorder(Math.max(getDownBorder(), child.getDownBorder()));
      child.parent = this;
      return true;
    }

    private List<Node> split(Node insertNode) {
      if (!isFull()) {
        throw new IllegalArgumentException();
      }

      int size = maxNodeCapacity / 2;
      int remainSize = maxNodeCapacity - size;
      List<Node> splitting = new ArrayList<>(size);
      List<Node> newChildren = new ArrayList<>(remainSize);

      for (int i = 0; i < children.size(); i++) {
        if (i < remainSize) {
          newChildren.add(children.get(i));
        } else {
          splitting.add(children.get(i));
        }
      }

      this.children = newChildren;
      splitting.add(insertNode);
      return splitting;
    }

    private boolean isRoot() {
      return parent == null;
    }

    private boolean isFull() {
      return CollectionUtils.isNotEmpty(children) && children.size() == maxNodeCapacity;
    }

    private boolean isLeaf() {
      if (CollectionUtils.isEmpty(children)) {
        return false;
      }
      return children.get(0).box != null;
    }

    private boolean isMBR() {
      return box != null;
    }

    public boolean containment(Box box) {
      if (isRoot()) {
        return true;
      }

      return getLeftBorder() <= box.getLeftBorder()
          && getRightBorder() >= box.getRightBorder()
          && getUpBorder() <= box.getUpBorder()
          && getDownBorder() >= box.getDownBorder();
    }

    @Override
    public boolean isOverlap(Box box) {
      if (isRoot()) {
        return true;
      }

      return super.isOverlap(box);
    }

    public List<Node> getChildren() {
      if (CollectionUtils.isEmpty(children)) {
        throw new IllegalArgumentException();
      }
      return children;
    }
  }
}
