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
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.graphper.api.Graphviz;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphvizVisual {

  private static final Logger log = LoggerFactory.getLogger(GraphvizVisual.class);

  private static final String IMG_CELL = "<li><div class=\"images-container\"><img src=\"%s\" alt=\"\"><img src=\"%s\" alt=\"\"></div><div class=\"sk_rush\"><a href=\"%s\" target=\"_blank\">See Svg</a><span style=\"margin-right: 10px;\"></span><a href=\"%s\" target=\"_blank\">See PDF</a></div></li>";

  private static final FileType FILE_TYPE = FileType.PNG;

  protected void visual(Graphviz graphviz) {
    System.setProperty("ovg.check", "true");
    System.setProperty("use.local.img.converter", "true");
    try {
      visual(graphviz, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void visual(Graphviz graphviz, boolean view) throws Exception {
    long start = System.currentTimeMillis();
    GraphResource svg = graphviz.toSvg();
    GraphResource img = graphviz.toFile(FILE_TYPE);
    long end = System.currentTimeMillis();

    log.info("{} cost the time of layout is {}ms", graphviz.hashCode(), end - start);

    if (view) {
      System.out.println(new String(svg.bytes()));
      new GraphView(img);
      System.in.read();
    } else {
      String s = save(graphviz, 1, svg);
      String pngByLocal = save(graphviz, 2, img);
      String pngByBatik = save(graphviz, 3, svg);
      String pdfPath = DocumentUtils.getTestPngPath() + graphviz.hashCode() + ".pdf";
      try(GraphResource resource = graphviz.toFile(FileType.PDF)) {
        resource.save(DocumentUtils.getTestPngPath(), String.valueOf(graphviz.hashCode()));
      }
      appendToVisualHtml(pngByLocal, pngByBatik, s, pdfPath);
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
      String graphCell = String.format(IMG_CELL, png, pngByBatik, svg, pdf) + "%s";
      for (String line : Files.readAllLines(
          FileSystems.getDefault().getPath(html.getPath()))) {
        line = line.replaceAll("%s", graphCell);
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
        System.out.println(new String(graphResource.bytes()));
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
