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

/**
 * The simplified {@link org.w3c.dom.Element} interface specially prepared for SVG settings. This
 * interface is intended for SVG-related use and is not recommended for general-purpose XML
 * manipulation. Provides basic methods for managing SVG element attributes, children, and content.
 *
 * @author Jamison Jiang
 */
public interface Element {

  /**
   * Returns the ID of this element.
   *
   * @return the ID of the element
   */
  String id();

  /**
   * Sets the ID for this element.
   *
   * @param id the ID to set for the element
   */
  void setId(String id);

  /**
   * Returns the tag name of this element.
   *
   * @return the tag name of the element
   */
  String tagName();

  /**
   * Returns the parent element of this element.
   *
   * @return the parent element, or {@code null} if this element has no parent
   */
  Element parent();

  /**
   * Creates a new child element with the specified tag name and appends it to this element.
   *
   * @param tagName the tag name for the new child element
   * @return the newly created child element
   */
  Element createChildElement(String tagName);

  /**
   * Sets the text content for this element.
   *
   * @param textContent the text content to set
   */
  void setTextContent(String textContent);

  /**
   * Sets an attribute on this element with the specified name and value.
   *
   * @param attrName the name of the attribute to set
   * @param value    the value of the attribute
   */
  void setAttribute(String attrName, String value);

  /**
   * Returns the value of the attribute with the specified name.
   *
   * @param attrName the name of the attribute
   * @return the value of the attribute, or {@code null} if the attribute does not exist
   */
  String getAttribute(String attrName);

  /**
   * Returns a string representation of the element's attributes in the format used by SVG.
   *
   * @return a string representation of the element's attributes
   */
  String toAttrStr();

  /**
   * Returns the text content of this element.
   *
   * @return the text content of the element
   */
  String textContext();

  /**
   * Returns the {@link Document} to which this element belongs.
   *
   * @return the document containing this element
   */
  Document getDocument();

  /**
   * Creates a new child element with the specified ID and tag name, then appends it to this
   * element.
   *
   * @param id      the ID for the new child element
   * @param tagName the tag name for the new child element
   * @return the newly created child element
   */
  default Element createChildElement(String id, String tagName) {
    Element childElement = createChildElement(tagName);
    childElement.setId(id);
    return childElement;
  }
}