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

package org.graphper.draw.svg;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.draw.ClusterDrawProp;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Node;
import org.graphper.draw.Brush;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;

/**
 * SVG brush for graph elements, providing utility methods to manipulate SVG elements such as nodes
 * and edges within an SVG document. This class allows creating, modifying, and grouping SVG
 * elements to represent various shapes in the diagram.
 *
 * @author Jamison Jiang
 */
public class SvgBrush implements Brush {

  private Element wrapEle;

  private final Element element;

  private final SvgDocument svgDocument;

  private final SvgDrawBoard svgDrawBoard;

  private Map<String, List<Element>> eleGroups;

  /**
   * Constructs an instance of {@code SvgBrush} with the specified SVG element, document, and
   * drawing board.
   *
   * @param element      the root SVG element
   * @param svgDocument  the SVG document to which this element belongs
   * @param svgDrawBoard the drawing board used for managing graph elements
   * @throws NullPointerException if any of the arguments are {@code null}
   */
  public SvgBrush(Element element, SvgDocument svgDocument, SvgDrawBoard svgDrawBoard) {
    Asserts.nullArgument(element, "element");
    Asserts.nullArgument(svgDocument, "svgomDocument");
    Asserts.nullArgument(svgDrawBoard, "svgDrawBorad");
    this.element = element;
    this.svgDocument = svgDocument;
    this.svgDrawBoard = svgDrawBoard;
  }

  /**
   * Returns the unique identifier for the specified node.
   *
   * @param node the node whose ID is to be retrieved
   * @return the unique identifier of the node
   */
  public String nodeId(Node node) {
    return drawBoard().nodeId(node);
  }

  /**
   * Returns the unique identifier for the specified line.
   *
   * @param lineDrawProp the line draw properties object
   * @return the unique identifier of the line
   */
  public String lineId(LineDrawProp lineDrawProp) {
    return drawBoard().lineId(lineDrawProp);
  }

  /**
   * Retrieves a child element with the specified ID and tag name under the current element. If the
   * element does not exist, it will be created.
   *
   * <p>
   * The node shape can consist of multiple {@link org.graphper.draw.svg.Element} objects, forming a
   * complete SVG structure. Each element represents a distinct part of the shape, such as the
   * outline, internal details, labels.
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   * SvgBrush brush = ...;
   * Element root = brush.getOrCreateChildElementById("node_0", "g");
   * Element noteElement = brush.getOrCreateChildElementById("node_0note0", "polygon");
   * }
   * </pre>
   * <p>Before calling {@code getOrCreateChildElementById}:</p>
   * <pre>
   * {@code
   * <g id="node_0">
   * </g>
   * }
   * </pre>
   * <p>After calling {@code getOrCreateChildElementById}:</p>
   * <pre>
   * {@code
   * <g id="node_0">
   *   <polygon id="node_0note0"/>
   * </g>
   * }
   * </pre>
   *
   * @param id      the ID of the element to retrieve or create
   * @param tagName the tag name of the element to create if it does not exist
   * @return the existing or newly created child element
   */
  public Element getOrCreateChildElementById(String id, String tagName) {
    Element ele = svgDocument.getElementById(id);
    if (ele == null) {
      ele = getCommonContainer().createChildElement(id, tagName);
      ele.setAttribute(SvgConstants.ID, id);
    }

    return ele;
  }

  /**
   * Retrieves or creates a shape element under the current element, based on the given ID and tag
   * name. The created element will be added to the shape group.
   *
   * @param id      the ID of the element to retrieve or create
   * @param tagName the tag name of the element to create if it does not exist
   * @return the existing or newly created shape element
   */
  public Element getOrCreateShapeEleById(String id, String tagName) {
    Element shapeEle = getOrCreateChildElementById(id, tagName);
    addGroup(SvgConstants.SHAPE_GROUP_KEY, Collections.singletonList(shapeEle));
    return shapeEle;
  }

  /**
   * Retrieves or creates a child element under the current node, based on the given node and tag
   * name.
   *
   * @param node    the node draw properties object
   * @param tagName the tag name of the element
   * @return the shape element corresponding to the given node and tag name
   */
  public Element getOrCreateChildElement(NodeDrawProp node, String tagName) {
    String shapeId = SvgBrush.getId(nodeId(node.getNode()), tagName);
    return getOrCreateShapeEleById(shapeId, tagName);
  }

  /**
   * Retrieves or creates a child element under the current cluster, based on the given cluster and
   * tag name.
   *
   * @param cluster the cluster draw properties object
   * @param tagName the tag name of the element
   * @return the shape element corresponding to the given cluster and tag name
   */
  public Element getOrCreateChildElement(ClusterDrawProp cluster, String tagName) {
    String shapeId = SvgBrush.getId(drawBoard().clusterId(cluster), tagName);
    return getOrCreateShapeEleById(shapeId, tagName);
  }

  /**
   * Adds elements to a group list. Since a graph element often consists of multiple SVG elements,
   * this group helps maintain the same characteristics for all elements representing a single graph
   * entity.
   *
   * @param key   the key representing the group
   * @param group the array of elements to add to the group
   * @throws IllegalArgumentException if the group is {@code null} or empty
   */
  public void addGroup(String key, Element... group) {
    Asserts.illegalArgument(group == null || group.length == 0, "Group is empty");
    addGroup(key, Arrays.asList(group));
  }

  /**
   * Adds elements to a group list. Since a graph element often consists of multiple SVG elements,
   * this group helps maintain the same characteristics for all elements representing a single graph
   * entity.
   *
   * @param key   the key representing the group
   * @param group the list of elements to add to the group
   * @throws IllegalArgumentException if the group is {@code null} or empty
   */
  public void addGroup(String key, List<Element> group) {
    if (CollectionUtils.isEmpty(group)) {
      return;
    }

    if (eleGroups == null) {
      eleGroups = new HashMap<>(1);
    }
    eleGroups.put(key, group);
  }

  /**
   * Returns all elements in a group specified by the group key.
   *
   * @param groupKey the key representing the group
   * @return a list of all elements in this group, or an empty list if the group does not exist
   */
  public List<Element> getEleGroup(String groupKey) {
    if (eleGroups == null) {
      return Collections.emptyList();
    }

    List<Element> group = eleGroups.get(groupKey);
    return CollectionUtils.isEmpty(group) ? Collections.emptyList() : group;
  }

  @Override
  @SuppressWarnings("unchecked")
  public SvgDrawBoard drawBoard() {
    return svgDrawBoard;
  }

  /**
   * Sets the wrapper element for the current SVG brush.
   *
   * @param wrapEle the wrapper element to be set
   */
  public void setWrapEle(Element wrapEle) {
    this.wrapEle = wrapEle;
  }

  // -------------------------------------------- static ------------------------------------------

  /**
   * Constructs an ID for a child element by combining the parent element ID and the element name.
   *
   * @param parentElementId the ID of the parent element
   * @param elementName     the name of the child element
   * @return the constructed ID for the child element
   */
  public static String getId(String parentElementId, String elementName) {
    return parentElementId + SvgConstants.UNDERSCORE + elementName;
  }

  // -------------------------------------------- private ------------------------------------------

  /**
   * Returns the container element for the current element, either the wrapper or the root element.
   *
   * @return the container element
   */
  private Element getCommonContainer() {
    return wrapEle != null ? wrapEle : element;
  }
}
