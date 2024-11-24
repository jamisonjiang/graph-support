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

import static org.graphper.draw.svg.SvgConstants.UNDERSCORE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphper.draw.Brush;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * SVG brush for graph elements, providing utility methods to manipulate SVG elements such as nodes
 * and edges within an SVG document. Each instance of {@code SvgBrush} represents a root SVG element
 * in the graph, enabling the creation, modification, and grouping of SVG elements to represent
 * various graphical shapes and structures.
 *
 * <p>For example, the following SVG structure represents a node in a graph:</p>
 * <pre>
 * {@code
 * <g id="node_1" class="node">
 *   <ellipse id="node_1_ellipse" cx="67.0" cy="160.0" rx="27.0" ry="18.0" fill="none" stroke="#000000"/>
 *   <text id="node_1_text_0" x="67.0" y="164" text-anchor="middle" font-size="14.0" fill="#000000" font-family="Arial">b</text>
 * </g>
 * }
 * </pre>
 *
 * <p>This class allows you to:</p>
 * <ul>
 *   <li>Create and manage child elements under a specific root element.</li>
 *   <li>Group related SVG elements (e.g., shapes, labels) for consistent styling or behavior.</li>
 *   <li>Retrieve or create individual elements dynamically based on their IDs and tag names.</li>
 *   <li>Integrate seamlessly with an {@link SvgDrawBoard} to manage the overall SVG document.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Already got root container element represent as SvgBrush, so brush is the root element <g id="node_1" class="node">
 * SvgBrush brush = ...;
 *
 * // Add an ellipse to the node
 * Element ellipse = brush.getOrCreateChildElementById("ellipse", "ellipse");
 * ellipse.setAttribute("cx", "67.0");
 * ellipse.setAttribute("cy", "160.0");
 * ellipse.setAttribute("rx", "27.0");
 * ellipse.setAttribute("ry", "18.0");
 * ellipse.setAttribute("fill", "none");
 * ellipse.setAttribute("stroke", "#000000");
 *
 * // Add a text label to the node
 * Element text = brush.getOrCreateChildElementById("text_0", "text");
 * text.setAttribute("x", "67.0");
 * text.setAttribute("y", "164");
 * text.setAttribute("text-anchor", "middle");
 * text.setAttribute("font-size", "14.0");
 * text.setAttribute("fill", "#000000");
 * text.setTextContent("b");
 * }
 * </pre>
 *
 * @author Jamison Jiang
 */
public class SvgBrush implements Brush {

  private Element wrapEle;

  private final String rootId;

  private final Element element;

  private final SvgDocument svgDocument;

  private final SvgDrawBoard svgDrawBoard;

  private Map<String, List<Element>> eleGroups;

  /**
   * Constructs an instance of {@code SvgBrush} with the specified SVG element, document, and
   * drawing board.
   *
   * @param rootId       brush element root id
   * @param element      the root SVG element
   * @param svgDocument  the SVG document to which this element belongs
   * @param svgDrawBoard the drawing board used for managing graph elements
   * @throws NullPointerException if any of the arguments are {@code null}
   */
  public SvgBrush(String rootId, Element element, SvgDocument svgDocument,
                  SvgDrawBoard svgDrawBoard) {
    Asserts.nullArgument(rootId);
    Asserts.nullArgument(element);
    Asserts.nullArgument(svgDocument);
    Asserts.nullArgument(svgDrawBoard);
    this.rootId = rootId;
    this.element = element;
    this.svgDocument = svgDocument;
    this.svgDrawBoard = svgDrawBoard;
  }

  /**
   * Returns the ID of the root container element associated with this {@code SvgBrush}.
   *
   * @return the root container element ID
   */
  public String getRootId() {
    return rootId;
  }

  /**
   * Retrieves a child element with the specified ID and tag name under the current root element
   * represented by this {@code SvgBrush}. If the element does not exist, it will be created and
   * associated with the current root element.
   *
   * <p>The {@code childId} does not need to be globally unique across the entire SVG document.
   * It only needs to be unique within the context of the current {@code SvgBrush}, as the method
   * appends the {@code childId} to the root element's ID to construct a fully qualified, unique
   * identifier for the child element.
   *
   * <p>The node shape can consist of multiple {@link org.graphper.draw.svg.Element} objects,
   * forming a complete SVG structure. Each element represents a distinct part of the shape, such as
   * the outline, internal details, or labels.
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   * SvgBrush brush = ...;
   * Element root = brush.getOrCreateChildElementById("ellipse", "ellipse");
   * Element text = brush.getOrCreateChildElementById("text_0", "text");
   * }
   * </pre>
   * <p>Before calling {@code getOrCreateChildElementById}:</p>
   * <pre>
   * {@code <g id="node_1"></g>}
   * </pre>
   * <p>After calling {@code getOrCreateChildElementById}:</p>
   * <pre>
   * {@code
   * <g id="node_1">
   *   <ellipse id="node_1_ellipse"/>
   *   <text id="node_1_text_0"/>
   * </g>
   * }
   * </pre>
   *
   * @param childId the unique identifier for the child element within the current {@code SvgBrush}
   * @param tagName the tag name of the element
   * @return the existing or newly created child element
   * @throws NullPointerException if {@code childId} or {@code tagName} is {@code null}
   */
  public Element getOrCreateChildElementById(String childId, String tagName) {
    Asserts.nullArgument(childId);
    Asserts.nullArgument(tagName);

    String id = rootId + UNDERSCORE + childId;
    Element ele = svgDocument.getElementById(id);
    if (ele == null) {
      ele = getCommonContainer().createChildElement(id, tagName);
      ele.setAttribute(SvgConstants.ID, id);
    }

    return ele;
  }

  /**
   * Retrieves or creates a shape element under the current root element represented by this
   * {@code SvgBrush}, based on the given child ID and tag name. The created element will be added
   * to the shape group for consistent styling or behavior.
   *
   * <p>The {@code childId} does not need to be unique across the entire SVG document but must be
   * unique within the context of the current {@code SvgBrush}. This method ensures the created or
   * retrieved element is associated with the shape group defined by
   * {@link SvgConstants#SHAPE_GROUP_KEY}.
   *
   * @param childId the ID of the element to retrieve or create
   * @param tagName the tag name of the element
   * @return the existing or newly created shape element
   */
  public Element getOrCreateShapeEleById(String childId, String tagName) {
    Element shapeEle = getOrCreateChildElementById(childId, tagName);
    addGroup(SvgConstants.SHAPE_GROUP_KEY, shapeEle);
    return shapeEle;
  }

  /**
   * Retrieves or creates a child element under the current root element represented by this
   * {@code SvgBrush}, based on the specified tag name. This method simplifies element creation by
   * using the same value for both the child ID and the tag name.
   *
   * <p>The created or retrieved element will automatically be added to the shape group for the
   * current {@code SvgBrush}.
   *
   * @param tagName the tag name of the element
   * @return the shape element corresponding to the given node and tag name
   */
  public Element getOrCreateChildElement(String tagName) {
    Asserts.nullArgument(tagName);
    return getOrCreateShapeEleById(tagName, tagName);
  }

  /**
   * Adds elements to a group identified by the specified key. Groups are used to logically organize
   * and manage SVG elements that represent different parts of a single graph entity, such as nodes
   * or edges. This allows consistent styling and behavior for all elements in the group.
   *
   * <p>The group key serves as an identifier to retrieve or manage the elements in the group
   * later. This method ensures that the specified elements are added to the appropriate group,
   * making it easier to apply transformations or styling to all elements in the group at once.
   *
   * <p>For example, consider two text elements representing different lines of text. By grouping
   * them under the key {@code "line_group"}, you can manage their text color or other shared
   * attributes collectively, rather than setting the color individually for each element.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   * SvgBrush brush = ...;
   *
   * // Create two text elements
   * Element textLine1 = brush.getOrCreateChildElementById("text_1", "text");
   * textLine1.setTextContent("First line of text");
   *
   * Element textLine2 = brush.getOrCreateChildElementById("text_2", "text");
   * textLine2.setTextContent("Second line of text");
   *
   * // Group the text elements under "line_group"
   * brush.addGroup("text_group", textLine1, textLine2);
   *
   * // Later, in a common handler, apply color styling to all elements in the group
   * List<Element> lineGroup = brush.getEleGroup("text_group");
   * for (Element line : lineGroup) {
   *     // Set the text color to blue
   *     line.setAttribute("fill", "#0000FF");
   * }
   * }
   * </pre>
   *
   * @param key   the key representing the group
   * @param group the array of elements to add to the group
   * @throws IllegalArgumentException if the {@code group} is {@code null} or empty
   */
  public void addGroup(String key, Element... group) {
    Asserts.illegalArgument(group == null || group.length == 0, "Group is empty");
    addGroup(key, Arrays.asList(group));
  }

  /**
   * Adds elements to a group identified by the specified key. Groups are used to logically organize
   * and manage SVG elements that represent different parts of a single graph entity, such as nodes
   * or edges. This allows consistent styling and behavior for all elements in the group.
   *
   * <p>The group key serves as an identifier to retrieve or manage the elements in the group
   * later. This method ensures that the specified elements are added to the appropriate group,
   * making it easier to apply transformations or styling to all elements in the group at once.
   *
   * <p>For example, consider two text elements representing different lines of text. By grouping
   * them under the key {@code "line_group"}, you can manage their text color or other shared
   * attributes collectively, rather than setting the color individually for each element.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   * {@code
   * SvgBrush brush = ...;
   *
   * // Create two text elements
   * Element textLine1 = brush.getOrCreateChildElementById("text_1", "text");
   * textLine1.setTextContent("First line of text");
   *
   * Element textLine2 = brush.getOrCreateChildElementById("text_2", "text");
   * textLine2.setTextContent("Second line of text");
   *
   * // Group the text elements under "line_group"
   * brush.addGroup("text_group", Arrays.asList(textLine1, textLine2));
   *
   * // Later, in a common handler, apply color styling to all elements in the group
   * List<Element> lineGroup = brush.getEleGroup("text_group");
   * for (Element line : lineGroup) {
   *     // Set the text color to blue
   *     line.setAttribute("fill", "#0000FF");
   * }
   * }
   * </pre>
   *
   * @param key   the key representing the group
   * @param group the array of elements to add to the group
   * @throws IllegalArgumentException if the {@code group} is {@code null} or empty
   */
  public void addGroup(String key, List<Element> group) {
    if (CollectionUtils.isEmpty(group)) {
      return;
    }

    if (eleGroups == null) {
      eleGroups = new HashMap<>(1);
    }
    List<Element> groupEles = eleGroups.get(key);
    if (CollectionUtils.isEmpty(groupEles)) {
      eleGroups.put(key, group);
    } else {
      groupEles = new ArrayList<>(groupEles);
      groupEles.addAll(group);
      eleGroups.put(key, groupEles);
    }
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
