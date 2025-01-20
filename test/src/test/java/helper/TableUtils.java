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

package helper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.graphper.api.Html;
import org.graphper.api.Html.Table;
import org.graphper.api.Html.Td;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class TableUtils {

  private static final Logger log = LoggerFactory.getLogger(TableUtils.class);

  public static Table readFile(File f) throws IOException {
    try (FileReader in = new FileReader(f)) {
      StringBuilder sb = new StringBuilder();
      for (int i = in.read(); i != -1; i = in.read()) {
        char c = (char) i;
        sb.append(c);
      }

      return parseTable(sb.toString());
    }
  }

  private static Table parseTable(String tableContent) {
    if (tableContent == null) {
      return null;
    }

    tableContent = tableContent.trim();
    try {
      ByteArrayInputStream is = new ByteArrayInputStream(tableContent.getBytes());
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse(is);
      return parseTable(document.getChildNodes());
    } catch (Exception e) {
      log.error("Parse html fail:", e);
    }
    return null;
  }

  private static Table parseTable(NodeList html) {
    for (int i = 0; i < html.getLength(); i++) {
      org.w3c.dom.Node t = html.item(i);
      if (!"table".equalsIgnoreCase(t.getNodeName())) {
        continue;
      }
      Table htmlTable = Html.table();
      NodeList childNodes = t.getChildNodes();

      NamedNodeMap attributes = t.getAttributes();
      if (attributes != null) {
        org.w3c.dom.Node cellspacing = attributes.getNamedItem("cellspacing");
        if (cellspacing != null) {
          htmlTable.cellSpacing(Integer.parseInt(cellspacing.getNodeValue()));
        }
        org.w3c.dom.Node cellpadding = attributes.getNamedItem("cellpadding");
        if (cellpadding != null) {
          htmlTable.cellPadding(Integer.parseInt(cellpadding.getNodeValue()));
        }
        org.w3c.dom.Node border = attributes.getNamedItem("border");
        if (border != null) {
          htmlTable.border(Integer.parseInt(border.getNodeValue()));
        }
      }

      for (int j = 0; j < childNodes.getLength(); j++) {
        org.w3c.dom.Node tr = childNodes.item(j);

        String nodeName = tr.getNodeName();
        if (!"tr".equalsIgnoreCase(nodeName)) {
          continue;
        }

        List<Td> htmlTds = new ArrayList<>();
        NodeList tds = tr.getChildNodes();
        for (int k = 0; k < tds.getLength(); k++) {
          org.w3c.dom.Node td = tds.item(k);
          if (!"td".equalsIgnoreCase(td.getNodeName()) && !"th".equalsIgnoreCase(
              td.getNodeName())) {
            continue;
          }

          Td htmlTd = Html.td();
          htmlTds.add(htmlTd);
          NodeList childTableNodes = td.getChildNodes();
          if (childTableNodes.getLength() > 0) {
            htmlTd.table(parseTable(childTableNodes));
          }

          if (htmlTd.getTable() == null) {
            String text = td.getTextContent();
            if (text != null) {
              htmlTd.text(text);
            }
          }

          attributes = td.getAttributes();
          if (attributes != null) {
            org.w3c.dom.Node rowspan = attributes.getNamedItem("rowspan");
            if (rowspan != null) {
              htmlTd.rowSpan(Integer.parseInt(rowspan.getNodeValue()));
            }
            org.w3c.dom.Node colspan = attributes.getNamedItem("colspan");
            if (colspan != null) {
              htmlTd.colSpan(Integer.parseInt(colspan.getNodeValue()));
            }
          }
        }

        htmlTable.tr(htmlTds.toArray(new Td[0]));
      }
      return htmlTable;
    }
    return null;
  }
}
