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

import static org.graphper.util.BoxUtils.newCombineBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.graphper.api.ext.Box;
import org.graphper.draw.Rectangle;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * A spatial data structure that organizes rectangular regions using a tree structure. It allows
 * efficient insertion and search for overlapping rectangles.
 *
 * @param <B> the type of {@link Box} to be stored in the tree.
 */
public class RectangleTree<B extends Box> {

  private Node root;
  private final int maxNodeCapacity;

  /**
   * Constructs a new {@code RectangleTree} with a specified maximum node capacity.
   *
   * @param maxNodeCapacity the maximum number of children a node can have.
   * @throws IllegalArgumentException if {@code maxNodeCapacity} is less than 2.
   */
  public RectangleTree(int maxNodeCapacity) {
    Asserts.illegalArgument(maxNodeCapacity < 2, "max node capacity cannot be less than 2");
    this.maxNodeCapacity = maxNodeCapacity;
  }

  /**
   * Inserts a {@link Box} into the tree.
   *
   * @param box the box to be inserted.
   * @throws IllegalArgumentException the insert box is illegal status
   */
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

  /**
   * Searches for all boxes that overlap with the specified search box.
   *
   * @param searchBox the box to search for overlaps.
   * @return a list of overlapping boxes, or an empty list if none found.
   * @throws IllegalArgumentException the search box is illegal status
   */
  public List<B> search(Box searchBox) {
    if (searchBox == null || this.root == null) {
      return Collections.emptyList();
    }
    searchBox.check();
    return search(root, searchBox);
  }

  // Private search method to recursively find overlapping boxes.
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

  // Inserts a box into the tree.
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

  // Splits a node when it exceeds capacity.
  private void split(Node node, Node insertNode) {
    Node newNode = node.split(insertNode);
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

  // Selects the child node with the minimum area increase.
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

  /**
   * Represents a node in the RectangleTree. Each node can contain child nodes or a single box.
   */
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
      return CollectionUtils.isNotEmpty(children) && children.get(0).box != null;
    }

    private boolean isMBR() {
      return box != null;
    }

    @Override
    public boolean isOverlap(Box box) {
      if (isRoot()) {
        return true;
      }
      return super.isOverlap(box);
    }

    // Splits the node into two when capacity is exceeded.
    private Node split(Node insertNode) {
      if (!isFull()) {
        throw new IllegalArgumentException();
      }

      init();
      children.add(insertNode);
      Node splitting = new Node(null);
      distribute(findSeeds(), splitting);
      return splitting;
    }

    // Distributes child nodes between two nodes during a split.
    private void distribute(NodePair seeds, Node splitting) {
      List<Node> nodes = children;
      children = null;
      int size = maxNodeCapacity - (maxNodeCapacity / 2);

      add(seeds.origin);
      splitting.add(seeds.splitting);

      for (Node child : nodes) {
        if (child == seeds.origin || child == seeds.splitting) {
          continue;
        }

        Box combineOrigin = newCombineBox(child, seeds.origin);
        Box combineSplitting = newCombineBox(child, seeds.splitting);
        if (combineOrigin.getArea() > combineSplitting.getArea() && splitting.size() != size) {
          splitting.add(child);
        } else {
          add(child);
        }
      }
    }

    private NodePair findSeeds() {
      Node minXNode = null, maxXNode = null, minYNode = null, maxYNode = null;

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
      return xDim
          ? (max.getRightBorder() - min.getLeftBorder()) - (min.getWidth() + max.getWidth())
          : (max.getDownBorder() - min.getUpBorder()) - (min.getHeight() + max.getHeight());
    }

    private List<Node> getChildren() {
      if (CollectionUtils.isEmpty(children)) {
        throw new IllegalArgumentException();
      }
      return children;
    }

    private int size() {
      return CollectionUtils.isEmpty(children) ? 0 : children.size();
    }
  }

  /**
   * A pair of nodes used during splitting.
   */
  private class NodePair {

    private final Node origin;
    private final Node splitting;

    public NodePair(Node origin, Node splitting) {
      this.origin = origin;
      this.splitting = splitting;
    }
  }
}