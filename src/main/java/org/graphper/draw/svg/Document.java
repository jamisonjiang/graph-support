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

import java.util.List;
import java.util.function.BiConsumer;

/**
 * The simplified {@link org.w3c.dom.Document} interface specially prepared for SVG settings. This
 * interface is intended for SVG-related use and is not recommended for general-purpose XML
 * manipulation. Provides basic methods for managing SVG elements and generating XML content.
 *
 * @author Jamison Jiang
 */
public interface Document {

  /**
   * Returns an iterable collection of the root children elements in this document.
   *
   * @return an iterable collection of child elements
   */
  Iterable<? extends Element> children();

  /**
   * Returns the element in this document with the specified ID.
   *
   * @param id the ID of the element to retrieve
   * @return the element with the specified ID, or {@code null} if no such element exists
   */
  Element getElementById(String id);

  /**
   * Creates a new element with the specified tag name.
   *
   * @param tagName the tag name for the new element
   * @return the newly created element
   */
  Element createElement(String tagName);

  /**
   * Removes the element with the specified ID from this document.
   *
   * @param id the ID of the element to remove
   * @return {@code true} if the element was removed successfully, {@code false} otherwise
   */
  boolean removeEle(String id);

  /**
   * Returns a string representation of this document in XML format.
   *
   * @return the XML string representation of this document
   */
  String toXml();

  /**
   * Applies a {@link BiConsumer} to each element and its children in this document.
   *
   * @param consumer the {@link BiConsumer} to be applied to each element and its children
   */
  void accessEles(BiConsumer<Element, List<Element>> consumer);
}
