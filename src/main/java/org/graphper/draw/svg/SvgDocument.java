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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

public final class SvgDocument implements SvgConstants, Document, Serializable {

  private static final long serialVersionUID = -9126509188726886245L;

  private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  private final Map<String, SvgElement> elementMap;

  public SvgDocument() {
    this.elementMap = new LinkedHashMap<>();
  }

  @Override
  public Iterable<SvgElement> children() {
    return elementMap.values();
  }

  @Override
  public Element getElementById(String id) {
    return elementMap.get(id);
  }

  @Override
  public SvgElement createElement(String tagName) {
    Asserts.nullArgument(tagName, "tagName is null");
    SvgElement ele = new SvgElement(tagName, this);
    int size = elementMap.size();
    ele.setId(String.valueOf(size));
    return ele;
  }

  @Override
  public boolean removeEle(String id) {
    return elementMap.remove(id) != null;
  }

  @Override
  public String toXml() {
    if (elementMap.size() == 0) {
      return null;
    }
    Map<Element, List<SvgElement>> groups = new LinkedHashMap<>();
    for (SvgElement element : elementMap.values()) {
      groups.compute(element.parent(), (k, v) -> {
        if (v == null) {
          v = new ArrayList<>();
        }
        v.add(element);
        return v;
      });
    }
    List<SvgElement> roots = groups.get(null);
    if (CollectionUtils.isEmpty(roots)) {
      return null;
    }

    StringBuilder xml = new StringBuilder();
    xml.append(XML_VERSION);
    for (SvgElement root : roots) {
      toXml(xml, root, groups);
    }
    return xml.toString();
  }

  private void toXml(StringBuilder xml, Element element, Map<Element, List<SvgElement>> groups) {
    if (element == null) {
      return;
    }
    String attr = element.toAttrStr();
    xml.append(LT).append(element.tagName());
    if (attr != null) {
      xml.append(attr);
    }
    xml.append(GT);
    if (element.textContext() != null) {
      xml.append(element.textContext());
    }
    List<SvgElement> children = groups.get(element);
    if (CollectionUtils.isNotEmpty(children)) {
      for (SvgElement child : children) {
        toXml(xml, child, groups);
      }
    }
    xml.append(LT).append(SLASH).append(element.tagName()).append(GT);
  }

  void setId(String oldId, String id, SvgElement element) {
    Asserts.nullArgument(id, "id");
    Asserts.nullArgument(element, "element");
    Asserts.illegalArgument(Objects.equals(oldId, id), "The id equals to oldKey");
    SvgElement svgElement = elementMap.get(id);
    if (svgElement != null) {
      throw new IllegalArgumentException("Id was occupied");
    }

    elementMap.put(id, element);
    if (oldId != null) {
      elementMap.remove(oldId);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SvgDocument that = (SvgDocument) o;
    return Objects.equals(elementMap, that.elementMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementMap);
  }

  @Override
  public String toString() {
    return toXml();
  }
}
