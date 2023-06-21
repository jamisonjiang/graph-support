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
import java.io.ByteArrayOutputStream;
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
import org.apache.batik.transcoder.TranscoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graphper.api.Graphviz;
import org.graphper.draw.ExecuteException;
import org.graphper.draw.GraphResource;

public class GraphvizVisual {

  private static final Logger log = LoggerFactory.getLogger(GraphvizVisual.class);

  private static final String IMG_CELL = "<li><img src=\"%s\" alt=\"\" width=\"295\"><div class=\"sk_rush\"><a href=\"%s\" target=\"_blank\">See svg</a></div></li>";

  protected void visual(Graphviz graphviz) {
    System.setProperty("ovg.check", "true");
    try {
      visual(graphviz, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void visual(Graphviz graphviz, boolean view)
      throws ExecuteException, IOException, TranscoderException {
    long start = System.currentTimeMillis();
    GraphResource graphResource = graphviz.toSvg();
    System.out.println(graphviz.toSvgStr());
    long end = System.currentTimeMillis();

    log.info("{} cost the time of layout is {}ms", graphviz.hashCode(), end - start);

    if (view) {
      new GraphView(graphResource);
      System.in.read();
    } else {
      String png = save(graphviz, false, graphResource);
      String svg = save(graphviz, true, graphResource);
      appendToVisualHtml(png, svg);
    }
  }

  private void appendToVisualHtml(String png, String svg) throws IOException {
    synchronized (GraphvizVisual.class) {
      File html = new File(DocumentUtils.getVisualHtmlPath());
      if (!html.exists()) {
        html = new File(DocumentUtils.getVisualHtmlTemplatePath());
      }

      StringBuilder sb = new StringBuilder();
      String graphCell = String.format(IMG_CELL, png, svg) + "%s";
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

  private String save(Graphviz graphviz, boolean isSvg, GraphResource graphResource)
      throws IOException, TranscoderException {
    String fileName = this.getClass().getName() + graphviz.hashCode() + (isSvg ? ".svg" : ".png");
    String path = DocumentUtils.getTestPngPath() + fileName;
    final File file = new File(path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    try (FileOutputStream fos = new FileOutputStream(file)) {
      if (isSvg) {
        fos.write(graphResource.bytes());
      } else {
        DocumentUtils.svgDocToImg(graphResource.inputStream(), fos);
      }
    }
    return ".." + DocumentUtils.getRelativeTestPngPath() + fileName;
  }

  public static class GraphView extends JFrame {

    public GraphView(GraphResource graphResource)
        throws IOException, TranscoderException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      DocumentUtils.svgDocToImg(graphResource.inputStream(), os);
      ImageIcon imageIcon = new ImageIcon(os.toByteArray(), "graphviz");

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
