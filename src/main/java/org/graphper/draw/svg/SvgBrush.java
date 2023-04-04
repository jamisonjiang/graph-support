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
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;
import org.graphper.api.Node;
import org.graphper.draw.Brush;
import org.graphper.draw.LineDrawProp;
import org.graphper.draw.NodeDrawProp;

/**
 * Svg brush for graph element.
 *
 * @author Jamison Jiang
 */
public class SvgBrush implements Brush {

  private Element wrapEle;

  private final Element element;

  private final SvgDocument svgDocument;

  private final SvgDrawBoard svgDrawBoard;

  private Map<String, List<Element>> eleGroups;

  public SvgBrush(Element element, SvgDocument svgDocument, SvgDrawBoard svgDrawBoard) {
    Asserts.nullArgument(element, "element");
    Asserts.nullArgument(svgDocument, "svgomDocument");
    Asserts.nullArgument(svgDrawBoard, "svgDrawBorad");
    this.element = element;
    this.svgDocument = svgDocument;
    this.svgDrawBoard = svgDrawBoard;
  }

  /**
   * Returns the node id.
   *
   * @param node node
   * @return node id
   */
  public String nodeId(Node node) {
    return drawBoard().nodeId(node);
  }

  /**
   * Returns the line id.
   *
   * @param lineDrawProp LineDrawProp
   * @return line id
   */
  public String lineId(LineDrawProp lineDrawProp) {
    return drawBoard().lineId(lineDrawProp);
  }

  /**
   * Get the child element under the current element according to the id and element tag, if there
   * is no one, create one, if there is, return it directly.
   *
   * @param id      element id
   * @param tagName element tag name
   * @return child element
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
   * Get the child element under the current element according to the node and element tag, if there
   * is no one, create one, if there is, return it directly.
   *
   * @param node    node
   * @param tagName element tag name
   * @return child element
   */
  public Element getShapeElement(NodeDrawProp node, String tagName) {
    String shapeId = SvgBrush.getId(nodeId(node.getNode()), tagName);
    return getOrCreateShapeEleById(shapeId, tagName);
  }

  /**
   * Get the shape child element under the current element according to the id and element tag, if
   * there is no one, create one, if there is, return it directly. Then this element will be put
   * into the {@link SvgConstants#SHAPE_GROUP_KEY} key.
   *
   * @param id      element id
   * @param tagName element tag name
   * @return child element
   */
  public Element getOrCreateShapeEleById(String id, String tagName) {
    Element shapeEle = getOrCreateChildElementById(id, tagName);
    addGroup(SvgConstants.SHAPE_GROUP_KEY, Collections.singletonList(shapeEle));
    return shapeEle;
  }

  /**
   * Add elements to a group list. Because a graph element needs a group of svg elements, this group
   * of svg elements needs to maintain the same characteristics of the graph element.
   *
   * @param key   group key
   * @param group elements group
   * @throws IllegalArgumentException group is null or empty
   */
  public void addGroup(String key, Element... group) {
    Asserts.illegalArgument(group == null || group.length == 0, "Group is empty");
    addGroup(key, Arrays.asList(group));
  }

  /**
   * Add elements to a group list. Because a graph element needs a group of svg elements, this group
   * of svg elements needs to maintain the same characteristics of the graph element.
   *
   * @param key   group key
   * @param group elements group
   * @throws IllegalArgumentException group is null or empty
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
   * Return all elements in a group according to the group key.
   *
   * @param groupKey group key
   * @return all elements in this group
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

  public void setWrapEle(Element wrapEle) {
    this.wrapEle = wrapEle;
  }

  // -------------------------------------------- static ------------------------------------------

  public static String getId(String parentElementId, String elementName) {
    return parentElementId + SvgConstants.UNDERSCORE + elementName;
  }

  // -------------------------------------------- private ------------------------------------------

  private Element getCommonContainer() {
    return wrapEle != null ? wrapEle : element;
  }
}
