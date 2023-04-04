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
 * The simplified {@link org.w3c.dom.Element} is specially prepared for Svg settings and is not
 * recommended for general use.
 *
 * @author Jamison Jiang
 */
public interface Element {

  String id();

  void setId(String id);

  String tagName();

  Element parent();

  Element createChildElement(String tagName);

  void setTextContent(String textContent);

  void setAttribute(String attrName, String value);

  String getAttribute(String attrName);

  String toAttrStr();

  String textContext();

  Document getDocument();

  default Element createChildElement(String id, String tagName) {
    Element childElement = createChildElement(tagName);
    childElement.setId(id);
    return childElement;
  }
}
