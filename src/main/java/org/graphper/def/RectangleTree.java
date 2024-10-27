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
import java.util.Collections;
import java.util.List;
import org.graphper.api.ext.Box;
import org.graphper.api.ext.DefaultBox;
import org.graphper.draw.Rectangle;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

public class RectangleTree<B extends Box> {

  private Node root;

  private final int maxNodeCapacity;

  public RectangleTree(int maxNodeCapacity) {
    Asserts.illegalArgument(maxNodeCapacity < 2, "max node capacity cannot be less than 2");
    this.maxNodeCapacity = maxNodeCapacity;
  }

  public void insert(B box) {
    if (box == null) {
      return;
    }

    box.check();
    if (root == null) {
      root = new Node(null);
      root.add(new Node(box));
      return;
    }

    insertion(root, box);
  }

  public List<B> search(Box searchBox) {
    if (searchBox == null || this.root == null) {
      return Collections.emptyList();
    }

    searchBox.check();
    return search(root, searchBox);
  }

  private List<B> search(Node node, Box searchBox) {
    if (!node.isOverlap(searchBox)) {
      return Collections.emptyList();
    }

    if (node.isMBR()) {
      return Collections.singletonList(node.box);
    }

    List<B> result = null;
    for (Node child : node.getChildren()) {
      List<B> childResult = search(child, searchBox);
      if (CollectionUtils.isEmpty(childResult)) {
        continue;
      }

      if (result == null) {
        result = new ArrayList<>(childResult);
      } else {
        result.addAll(childResult);
      }
    }
    return result != null ? result : Collections.emptyList();
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
    nodes.forEach(newNode::alignSize);

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

    private final Node origin;

    private final Node splitting;

    public NodePair(Node origin, Node splitting) {
      this.origin = origin;
      this.splitting = splitting;
    }
  }

  private class Node extends Rectangle {

    private final B box;

    private Node parent;

    private List<Node> children;

    public Node(B box) {
      this.box = box;
      init();
      if (box != null) {
        alignSize(box);
      }
    }

    private boolean add(Node child) {
      if (children == null) {
        children = new ArrayList<>(maxNodeCapacity);
      }
      if (isFull()) {
        return false;
      }

      children.add(child);
      alignSize(child);
      child.parent = this;
      return true;
    }

    private void alignSize(Box box) {
      updateXAxisRange(box.getLeftBorder());
      updateXAxisRange(box.getRightBorder());
      updateYAxisRange(box.getUpBorder());
      updateYAxisRange(box.getDownBorder());
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

    private List<Node> split(Node insertNode) {
      if (!isFull()) {
        throw new IllegalArgumentException();
      }

      children.add(insertNode);
      int size = maxNodeCapacity / 2;
      int remainSize = maxNodeCapacity - size;
      List<Node> splitting = new ArrayList<>(size);
      List<Node> newChildren = new ArrayList<>(remainSize);

      distribute(remainSize, findSeeds(), splitting, newChildren);
      this.children = newChildren;
      return splitting;
    }

    private void distribute(int remainSize, NodePair seeds,
                            List<Node> splitting, List<Node> newChildren) {
      newChildren.add(seeds.origin);
      splitting.add(seeds.splitting);

      for (Node child : children) {
        if (child == seeds.origin || child == seeds.splitting) {
          continue;
        }

        Box combineOrigin = newCombineBox(child, seeds.origin);
        Box combineSplitting = newCombineBox(child, seeds.splitting);
        if (combineOrigin.getArea() < combineSplitting.getArea()
            && newChildren.size() != remainSize) {
          newChildren.add(child);
        } else {
          splitting.add(child);
        }
      }
    }

    private NodePair findSeeds() {
      Node minXNode = null;
      Node maxXNode = null;
      Node minYNode = null;
      Node maxYNode = null;

      for (Node child : children) {
        if (minXNode == null || minXNode.getLeftBorder() > child.getLeftBorder()) {
          minXNode = child;
        }
        if (maxXNode == null || maxXNode.getRightBorder() < child.getRightBorder()) {
          maxXNode = child;
        }
        if (minYNode == null || minYNode.getUpBorder() > child.getUpBorder()) {
          minYNode = child;
        }
        if (maxYNode == null || maxYNode.getDownBorder() < child.getDownBorder()) {
          maxYNode = child;
        }
      }

      return waste(minXNode, maxXNode, true) > waste(minYNode, maxYNode, false)
          ? new NodePair(minXNode, maxXNode)
          : new NodePair(minYNode, maxYNode);
    }

    private double waste(Node min, Node max, boolean xDim) {
      if (xDim) {
        return (max.getRightBorder() - min.getLeftBorder()) - (min.getWidth() + max.getWidth());
      }

      return (max.getDownBorder() - min.getUpBorder()) - (min.getHeight() + max.getHeight());
    }

    private List<Node> getChildren() {
      if (CollectionUtils.isEmpty(children)) {
        throw new IllegalArgumentException();
      }
      return children;
    }
  }
}
