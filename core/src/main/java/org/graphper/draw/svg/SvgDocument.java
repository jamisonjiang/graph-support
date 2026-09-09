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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/** Stores SVG elements and serializes them as an XML document. */
public final class SvgDocument implements SvgConstants, Document, Serializable {

  private static final long serialVersionUID = -9126509188726886245L;

  private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  private static final String DOC_TYPE =
      "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">";

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
    ele.setId(tagName + UNDERSCORE + size);
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

    Map<Element, List<Element>> groups = groupElements();
    List<Element> roots = groups.get(null);
    if (CollectionUtils.isEmpty(roots)) {
      return null;
    }

    StringBuilder xml = new StringBuilder();
    xml.append(XML_VERSION).append('\n').append(DOC_TYPE).append('\n');
    for (Element root : roots) {
      appendElement(xml, root, groups, 0);
    }
    xml.setLength(xml.length() - 1);
    return xml.toString();
  }

  private void appendElement(
      StringBuilder xml, Element element, Map<Element, List<Element>> groups, int depth) {
    appendIndent(xml, depth);
    xml.append(LT).append(element.tagName());
    String attr = element.toAttrStr();
    if (attr != null) {
      xml.append(attr);
    }
    xml.append(GT);

    if (element.textContext() != null) {
      xml.append(element.textContext());
    }

    List<Element> children = groups.get(element);
    if (!CollectionUtils.isEmpty(children)) {
      xml.append('\n');
      for (Element child : children) {
        appendElement(xml, child, groups, depth + 1);
      }
      appendIndent(xml, depth);
    }
    xml.append(LT).append(SLASH).append(element.tagName()).append(GT).append('\n');
  }

  private void appendIndent(StringBuilder xml, int depth) {
    for (int i = 0; i < depth; i++) {
      xml.append("  ");
    }
  }

  @Override
  public void accessEles(BiConsumer<Element, List<Element>> consumer) {
    accessEles(consumer, null);
  }

  private void accessEles(
      BiConsumer<Element, List<Element>> preConsumer, Consumer<Element> postConsumer) {
    if (preConsumer == null || elementMap.size() == 0) {
      return;
    }

    Map<Element, List<Element>> groups = groupElements();
    List<Element> roots = groups.get(null);
    if (CollectionUtils.isEmpty(roots)) {
      return;
    }

    for (Element root : roots) {
      accessEle(root, groups, preConsumer, postConsumer);
    }
  }

  private Map<Element, List<Element>> groupElements() {
    Map<Element, List<Element>> groups = new LinkedHashMap<>();
    for (SvgElement element : elementMap.values()) {
      groups.compute(
          element.parent(),
          (k, v) -> {
            if (v == null) {
              v = new ArrayList<>();
            }
            v.add(element);
            return v;
          });
    }
    return groups;
  }

  private void accessEle(
      Element element,
      Map<Element, List<Element>> groups,
      BiConsumer<Element, List<Element>> preConsumer,
      Consumer<Element> postConsumer) {
    if (element == null) {
      return;
    }
    List<Element> children = groups.get(element);
    children = CollectionUtils.isEmpty(children) ? Collections.emptyList() : children;
    preConsumer.accept(element, children);
    for (Element child : children) {
      accessEle(child, groups, preConsumer, postConsumer);
    }
    if (postConsumer != null) {
      postConsumer.accept(element);
    }
  }

  void setId(String oldId, String id, SvgElement element) {
    Asserts.nullArgument(id, "id");
    Asserts.nullArgument(element, "element");
    Asserts.illegalArgument(Objects.equals(oldId, id), "The id equals to oldKey");

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
