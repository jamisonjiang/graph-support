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

package org.graphper.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.attributes.FontStyle;
import org.graphper.api.attributes.Layout;
import org.graphper.def.FlatPoint;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.common.CommonDrawBoard;
import org.graphper.draw.common.SvgConverter;
import org.graphper.draw.svg.Document;
import org.graphper.layout.FontSelector;
import org.graphper.layout.MeasureText;

/** Executed in the isolated loader; only JDK values cross the loader boundary. */
public class CoreResilienceFixture {

  public static String[] render() throws Exception {
    Node first = Node.builder().label("first").build();
    Node second = Node.builder().label("second").build();
    Layout[] layouts = Layout.values();
    String[] result = new String[layouts.length + 3];
    for (int i = 0; i < layouts.length; i++) {
      Graphviz graph = Graphviz.digraph().layout(layouts[i]).addLine(first, second).build();
      result[i] = graph.toSvgStr();
    }
    Graphviz graph = Graphviz.digraph().addLine(first, second).build();
    try (GraphResource resource = graph.toFile(FileType.SVG)) {
      result[layouts.length] = new String(resource.bytes(), StandardCharsets.UTF_8);
    }
    CommonDrawBoard board = new CommonDrawBoard(new DrawGraph(graph));
    try (GraphResource resource = board.graphResource()) {
      result[layouts.length + 1] = new String(resource.bytes(), StandardCharsets.UTF_8);
    }
    board.setImageType(FileType.SVG);
    try (GraphResource resource = board.graphResource()) {
      result[layouts.length + 2] = new String(resource.bytes(), StandardCharsets.UTF_8);
    }
    return result;
  }

  public static double[] measure() {
    FlatPoint size = FontUtils.measure("hello\nworld", null, 20, 0);
    return new double[]{size.getWidth(), size.getHeight()};
  }

  public static String[] fonts() {
    return new String[]{FontUtils.DEFAULT_FONT, FontUtils.selectFont("hello", null),
        FontUtils.selectFont("hello", "Viewer Font"),
        String.valueOf(FontUtils.fontExists("not-an-installed-font"))};
  }

  public static int providerCount() {
    return OptionalProviders.load(MeasureText.class).size();
  }

  public static String[] providerNames() {
    List<MeasureText> providers = OptionalProviders.load(MeasureText.class);
    String[] names = new String[providers.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = providers.get(i).getClass().getSimpleName();
    }
    return names;
  }

  public static String raster() throws Exception {
    Graphviz graph = Graphviz.digraph().addNode(Node.builder().label("raster")
        .image("data:image/png;base64,AQ==").build()).build();
    try (GraphResource resource = graph.toFile(FileType.PNG)) {
      return new String(resource.bytes(), StandardCharsets.UTF_8);
    }
  }

  public static String unsupportedRaster() throws Exception {
    CommonDrawBoard board = new CommonDrawBoard(new DrawGraph(Graphviz.digraph().build()));
    board.setImageType(FileType.PNG);
    try (GraphResource ignored = board.graphResource()) {
      throw new AssertionError("Raster conversion should be unavailable");
    } catch (FailInitResourceException e) {
      return e.getMessage();
    }
  }

  public static class CustomFont implements MeasureText, FontSelector {
    @Override
    public int order() {
      return -100;
    }

    @Override
    public boolean envSupport() {
      return true;
    }

    @Override
    public String defaultFont() {
      return "Custom SVG Font";
    }

    @Override
    public FlatPoint measure(String text, String font, double size, FontStyle... styles) {
      return new FlatPoint(7, 13);
    }
  }

  public static class LaterFont extends CustomFont {
    @Override
    public int order() {
      return 100;
    }
  }

  public static class TiedFont extends CustomFont {
  }

  public static class BrokenConstructor extends CustomFont {
    public BrokenConstructor() {
      throw new IllegalStateException("Unavailable native constructor");
    }
  }

  public static class BrokenLinkage extends CustomFont {
    static {
      failToLink();
    }

    private static void failToLink() {
      throw new NoClassDefFoundError("optional/platform/API");
    }
  }

  public static class BrokenEnvironment extends CustomFont {
    @Override
    public boolean envSupport() {
      throw new NoClassDefFoundError("optional/platform/API");
    }
  }

  public static class BrokenOrder extends CustomFont {
    @Override
    public int order() {
      throw new IllegalStateException("Unavailable priority");
    }
  }

  public static class UnsupportedFont extends CustomFont {
    @Override
    public boolean envSupport() {
      return false;
    }

    @Override
    public int order() {
      throw new AssertionError("Unsupported provider must not be ranked");
    }
  }

  public static class BrokenFontEnumeration extends CustomFont {
    @Override
    public int order() {
      return -200;
    }

    @Override
    public String defaultFont() {
      throw new NoClassDefFoundError("optional/font/enumeration");
    }
  }

  public static class CustomConverter implements SvgConverter {
    @Override
    public int order() {
      return -100;
    }

    @Override
    public boolean envSupport() {
      return true;
    }

    @Override
    public FileType[] supportFileTypes() {
      return new FileType[]{FileType.PNG};
    }

    @Override
    public DefaultGraphResource convert(Document document, DrawGraph graph, FileType type) {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      byte[] marker = getClass().getSimpleName().getBytes(StandardCharsets.UTF_8);
      bytes.write(marker, 0, marker.length);
      return new DefaultGraphResource("custom", type.getType(), bytes);
    }
  }

  public static class LaterConverter extends CustomConverter {
    @Override
    public int order() {
      return 100;
    }
  }

  public static class BrokenConverter extends CustomConverter {
    public BrokenConverter() {
      throw new NoClassDefFoundError("optional/raster/API");
    }
  }
}
