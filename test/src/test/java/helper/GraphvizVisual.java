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

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.graphper.api.Graphviz;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphvizVisual {

  private static final String CASE_PLACEHOLDER = "<!-- graph-cases -->";

  private static final Logger log = LoggerFactory.getLogger(GraphvizVisual.class);

  private static final String IMG_CELL = "<li class=\"case-card\" data-search=\"%5$s %8$s %6$s\" data-tags=\"%6$s\">"
      + "<header class=\"case-header\"><span class=\"case-index\"></span>"
      + "<span class=\"case-meta\"><span class=\"case-method\">%8$s</span>"
      + "<span class=\"file-name\" title=\"%5$s\">%5$s</span></span>"
      + "<span class=\"case-tags\">%7$s</span></header>"
      + "<div class=\"comparison\">"
      + "<article class=\"preview local\"><h2 class=\"preview-title\">Local converter</h2>"
      + "<a class=\"image-link\" href=\"%1$s\"><img src=\"%1$s\" alt=\"Local converter output\" loading=\"lazy\"></a></article>"
      + "<article class=\"preview batik\"><h2 class=\"preview-title\">Batik converter</h2>"
      + "<a class=\"image-link\" href=\"%2$s\"><img src=\"%2$s\" alt=\"Batik converter output\" loading=\"lazy\"></a></article>"
      + "</div><footer class=\"case-actions\"><a class=\"preview-file\" data-kind=\"SVG\" href=\"%3$s\">Open SVG</a>"
      + "<a class=\"preview-file\" data-kind=\"PDF\" href=\"%4$s\">Open PDF</a></footer></li>";

  private static final FileType FILE_TYPE = FileType.PNG;

  private String caseTags;

  private String caseName;

  @BeforeEach
  public void init(TestInfo testInfo) {
    System.setProperty("graph.layout", "dot");
    Set<String> tags = new LinkedHashSet<>();
    testInfo.getTestClass().ifPresent(type -> {
      tags.add(normalizeTag(type.getSimpleName().replaceFirst("Test$", "")));
      addTags(tags, type);
    });
    testInfo.getTestMethod().ifPresent(method -> addTags(tags, method));
    caseTags = String.join(",", tags);
    caseName = testInfo.getTestMethod().map(method -> method.getName())
        .orElse(testInfo.getDisplayName());
  }

  protected void visual(Graphviz graphviz) {
    System.setProperty("graph.quality.check", "true");
    System.setProperty("use.local.img.converter", "true");
    try {
      visual(graphviz, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void visual(Graphviz graphviz, boolean view) throws Exception {
    GraphResource svg = graphviz.toSvg();
    GraphResource img = graphviz.toFile(FILE_TYPE);

    if (view) {
      new GraphView(img);
      System.in.read();
    } else {
      String s = save(graphviz, 1, svg);
      String pngByLocal = save(graphviz, 2, img);
      String pngByBatik = save(graphviz, 3, svg);
      String pdfFile = graphviz.hashCode() + ".pdf";
      try(GraphResource resource = graphviz.toFile(FileType.PDF)) {
        resource.save(DocumentUtils.getTestPngPath(), String.valueOf(graphviz.hashCode()));
      }
      appendToVisualHtml(pngByLocal, pngByBatik, s,
                         ".." + DocumentUtils.getRelativeTestPngPath() + pdfFile);
      img.close();
    }
  }

  private void appendToVisualHtml(String png, String pngByBatik, String svg, String pdf) throws IOException {
    synchronized (GraphvizVisual.class) {
      File html = new File(DocumentUtils.getVisualHtmlPath());
      if (!html.exists()) {
        html = new File(DocumentUtils.getVisualHtmlTemplatePath());
      }

      StringBuilder sb = new StringBuilder();
      String graphCell = String.format(IMG_CELL, png, pngByBatik, svg, pdf,
                                       html(new File(svg).getName()), html(caseTags), tagBadges(caseTags),
                                       html(caseName)) + CASE_PLACEHOLDER;
      for (String line : Files.readAllLines(
          FileSystems.getDefault().getPath(html.getPath()))) {
        line = line.replace(CASE_PLACEHOLDER, graphCell);
        sb.append(line);
      }

      html = new File(DocumentUtils.getVisualHtmlPath());
      if (!html.getParentFile().exists()) {
        html.getParentFile().mkdirs();
      }

      try (FileOutputStream fos = new FileOutputStream(html)) {
        fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  private void addTags(Set<String> tags, AnnotatedElement element) {
    VisualTags visualTags = element.getAnnotation(VisualTags.class);
    if (visualTags == null) {
      return;
    }
    for (String tag : visualTags.value()) {
      String normalized = normalizeTag(tag);
      if (!normalized.isEmpty()) {
        tags.add(normalized);
      }
    }
  }

  private String normalizeTag(String tag) {
    return tag == null ? "" : tag.trim()
        .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
        .replaceAll("[^A-Za-z0-9]+", "-")
        .replaceAll("(^-+|-+$)", "")
        .toLowerCase();
  }

  private String tagBadges(String tags) {
    StringBuilder badges = new StringBuilder();
    for (String tag : tags.split(",")) {
      if (!tag.isEmpty()) {
        badges.append("<span class=\"tag\">").append(html(tag)).append("</span>");
      }
    }
    return badges.toString();
  }

  private String html(String text) {
    return text == null ? "" : text.replace("&", "&amp;")
        .replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private String save(Graphviz graphviz, int type, GraphResource graphResource)
      throws Exception {
    String fileName = this.getClass().getName() + graphviz.hashCode() + "_" + type;
    String suffix = (type == 2 || type == 3) ? "." + FILE_TYPE.getType() : ".svg";
    String f = fileName;
    fileName += suffix;

    String separator = FileSystems.getDefault().getSeparator();
    String path = DocumentUtils.getTestPngPath() + separator + fileName;
    final File file = new File(path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    try (FileOutputStream fos = new FileOutputStream(file)) {
      if (type == 1) {
        graphResource.save(DocumentUtils.getTestPngPath(), f);
      } else if (type == 2) {
        fos.write(graphResource.bytes());
      } else {
        DocumentUtils.svgDocToImg(graphviz.toSvg().inputStream(), fos, FILE_TYPE);
      }
    }
    return ".." + DocumentUtils.getRelativeTestPngPath() + separator + fileName;
  }

  public static class GraphView extends JFrame {

    public GraphView(GraphResource graphResource) throws IOException{
      ImageIcon imageIcon = new ImageIcon(graphResource.bytes(), "graphviz");
      graphResource.close();

      JFrame mainframe = new JFrame("graph-support");
      mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JPanel cp = (JPanel) mainframe.getContentPane();
      cp.setLayout(new BorderLayout());
      JLabel label = new JLabel(imageIcon);
      cp.add("Center", label);
      mainframe.pack();
      mainframe.setVisible(true);
    }
  }
}
