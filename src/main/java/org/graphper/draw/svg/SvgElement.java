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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache_gs.commons.text.StringEscapeUtils;
import org.graphper.util.Asserts;

public final class SvgElement implements SvgConstants, Element, Serializable {

  private static final long serialVersionUID = -4656435281889555382L;

  private String id;

  private final String tagName;

  private String textContext;

  private Map<String, String> attr;

  private SvgElement parent;

  private final SvgDocument document;

  SvgElement(String tagName, SvgDocument document) {
    Asserts.nullArgument(tagName, "tag name");
    Asserts.nullArgument(document, "document");
    this.tagName = tagName;
    this.document = document;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public void setId(String id) {
    Asserts.nullArgument(id, "id");
    if (Objects.equals(id, this.id)) {
      return;
    }
    document.setId(this.id, id, this);
    this.id = id;
  }

  @Override
  public String tagName() {
    return tagName;
  }

  @Override
  public Element parent() {
    return parent;
  }

  @Override
  public Element createChildElement(String tagName) {
    Asserts.nullArgument(tagName, "tagName");
    SvgElement childElement = document.createElement(tagName);
    childElement.parent = this;
    return childElement;
  }

  @Override
  public void setTextContent(String textContent) {
    this.textContext = StringEscapeUtils.escapeHtml3(textContent);
  }

  @Override
  public void setAttribute(String attrName, String value) {
    Asserts.nullArgument(attrName, "attrName");
    Asserts.nullArgument(value, "value");
    value = StringEscapeUtils.escapeHtml3(value);
    if (attr == null) {
      attr = new LinkedHashMap<>(2);
    }
    attr.put(attrName, value);
    if (Objects.equals(attrName, ID)) {
      setId(value);
    }
  }

  @Override
  public String getAttribute(String attrName) {
    if (attr == null) {
      return null;
    }
    return attr.get(attrName);
  }

  @Override
  public String toAttrStr() {
    if (attr == null) {
      return null;
    }
    StringBuilder attrVal = new StringBuilder();
    for (Entry<String, String> entry : attr.entrySet()) {
      attrVal.append(SPACE).append(entry.getKey())
          .append(EQUAL_SIGN).append(SEMICOLON)
          .append(entry.getValue()).append(SEMICOLON);
    }
    return attrVal.toString();
  }

  @Override
  public String textContext() {
    return textContext;
  }

  @Override
  public Document getDocument() {
    return document;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SvgElement that = (SvgElement) o;
    return Objects.equals(id, that.id)
        && Objects.equals(tagName, that.tagName)
        && Objects.equals(textContext, that.textContext)
        && Objects.equals(attr, that.attr)
        && Objects.equals(parent, that.parent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tagName, textContext, attr, parent);
  }
}
