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

package org.graphper.draw.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.SecurityPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecureSvgTest {

  private static final SecurityPolicy POLICY = SecurityPolicy.defaultPolicy();
  private static final String DOCTYPE = "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" "
      + "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">";

  @Test
  void stripsGeneratedDoctypeWithoutResolvingIt() throws Exception {
    for (String prefix : new String[]{"", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"}) {
      String safe = SecureSvg.prepare(prefix + DOCTYPE + svg(""), POLICY, true);
      assertFalse(safe.contains("DOCTYPE"));
      assertTrue(safe.contains("<svg"));
    }
  }

  @Test
  void rejectsEveryOtherDoctypeBeforeReadingItsSubset() {
    for (String declaration : new String[]{
        "<!DOCTYPE svg>",
        "<!DOCTYPE svg SYSTEM 'https://example.com/svg.dtd'>",
        "<!DOCTYPE svg [<!ENTITY x 'expanded'>]>",
        DOCTYPE.substring(0, DOCTYPE.length() - 1) + " [<!ENTITY x 'expanded'>]>",
        "<!DOCTYPE svg [<!ENTITY x '" + DOCTYPE + "'>]>",
        DOCTYPE + DOCTYPE,
        DOCTYPE.replace("http://", "https://")}) {
      for (boolean raster : new boolean[]{true, false}) {
        assertThrows(Exception.class,
            () -> SecureSvg.prepare(declaration + svg(""), POLICY, raster), declaration);
      }
    }
  }

  @Test
  void shallowInstancingBombAndGradientChainsAreRejectedWithoutTranscoding() {
    StringBuilder bomb = new StringBuilder("<defs><g id='n0'><rect width='1' height='1'/></g>");
    for (int i = 1; i <= 30; i++) {
      bomb.append("<g id='n").append(i).append("'><use href='#n").append(i - 1)
          .append("'/><use href='#n").append(i - 1).append("'/></g>");
    }
    bomb.append("</defs><use href='#n30'/>");
    for (String content : new String[]{bomb.toString(), "<symbol id='x'><rect/></symbol>",
        "<defs><linearGradient id='a' href='#b'/><linearGradient id='b' href='#a'/></defs>",
        "<radialGradient xlink:href='#paint'/>", "<rect href='#x'/>", "<stop href='#x'/>"}) {
      for (boolean raster : new boolean[]{true, false}) {
        assertThrows(IllegalArgumentException.class,
            () -> SecureSvg.prepare(svg(content), POLICY, raster));
      }
    }
  }

  @Test
  void structuralBudgetsRejectBeforeDomWithOrdinaryExceptions() {
    String deep = svg(repeat("<g>", 10_000) + repeat("</g>", 10_000));
    String many = svg(repeat("<rect/>", SecureSvg.MAX_ELEMENTS));
    StringBuilder attributes = new StringBuilder();
    for (int i = 0; i <= SecureSvg.MAX_ATTRIBUTES_PER_ELEMENT; i++) {
      attributes.append(" xmlns:n").append(i).append("='urn:n'");
    }
    String attributeHeavy = svg("<g" + attributes + "/>");
    StringBuilder ordinaryAttributes = new StringBuilder();
    for (int i = 0; i < 64; i++) {
      ordinaryAttributes.append(" a").append(i).append("=''");
    }
    String aggregateAttributes = svg(repeat("<g" + ordinaryAttributes + "/>",
        SecureSvg.MAX_ATTRIBUTES / 64 + 1));
    String images = svg(repeat("<image/>", SecureSvg.MAX_IMAGES + 1));
    for (boolean raster : new boolean[]{true, false}) {
      assertTrue(assertThrows(Exception.class, () -> SecureSvg.prepare(deep, POLICY, raster))
          .getMessage().contains("nesting depth limit"));
      assertTrue(assertThrows(Exception.class, () -> SecureSvg.prepare(many, POLICY, raster))
          .getMessage().contains("element limit"));
      for (String input : new String[]{attributeHeavy, aggregateAttributes}) {
        assertTrue(assertThrows(Exception.class, () -> SecureSvg.prepare(input, POLICY, raster))
            .getMessage().contains("attribute limit"));
      }
      assertTrue(assertThrows(Exception.class, () -> SecureSvg.prepare(images, POLICY, raster))
          .getMessage().contains("image count limit"));
    }
  }

  @Test
  void oversizedInputIsRejectedBeforeParsingEvenIfMalformed() {
    String oversized = repeat("x", SecureSvg.MAX_INPUT_CHARS + 1);
    for (boolean raster : new boolean[]{true, false}) {
      assertTrue(assertThrows(IllegalArgumentException.class,
          () -> SecureSvg.prepare(oversized, POLICY, raster))
          .getMessage().contains("input character limit"));
    }
  }

  @Test
  void boundedDepthAndOrdinaryWideDocumentsRemainAccepted() throws Exception {
    String nested = svg(repeat("<g>", SecureSvg.MAX_DEPTH - 1)
        + repeat("</g>", SecureSvg.MAX_DEPTH - 1));
    String wide = svg(repeat("<rect width='1' height='1'/>", 10_000));
    for (boolean raster : new boolean[]{true, false}) {
      assertTrue(SecureSvg.prepare(nested, POLICY, raster).contains("<g"));
      assertTrue(SecureSvg.prepare(wide, POLICY, raster).contains("<rect"));
    }
  }

  @Test
  void rejectsActiveContentAndEveryExternalResourceSyntax() {
    for (String content : new String[]{
        "<script>alert(1)</script>",
        "<g onload='alert(1)'/>",
        "<style>@import url(https://example.com/style.css);</style>",
        "<g style='fill: url(https://example.com/color.svg)'/>",
        "<g fill='url(https://example.com/color.svg#paint)'/>",
        "<g fill='u\\72l(https://example.com/color.svg#paint)'/>",
        "<image href='https://example.com/image.png'/>",
        "<image href='file:///tmp/image.png'/>",
        "<image href='relative.png'/>",
        "<image href='data:image/svg+xml;base64,PHN2Zy8+'/>",
        "<image href='data:image/png;base64,PHN2Zy8+'/>",
        "<use href='//example.com/image.svg#x'/>",
        "<use href='https&#58;//example.com/image.svg#x'/>",
        "<g xml:base='file:///tmp/'><use href='#x'/></g>",
        "<foreignObject/>",
        "<animate attributeName='href' to='https://example.com/image.svg'/>",
        "<?xml-stylesheet href='https://example.com/style.css'?>"}) {
      assertThrows(Exception.class, () -> SecureSvg.prepare(svg(content), POLICY, true), content);
    }
    assertThrows(Exception.class, () -> SecureSvg.prepare(
        "<!DOCTYPE svg [<!ENTITY x SYSTEM 'file:///etc/passwd'>]>"
            + svg("<text>&x;</text>"), POLICY, true));
  }

  @Test
  void staticLinksAndLocalPaintReferencesDoNotNeedExternalAccess() throws Exception {
    String safe = SecureSvg.prepare(svg("<defs><linearGradient id='paint'>"
        + "<stop offset='0' stop-color='#ffffff'/></linearGradient></defs>"
        + "<a href='https://example.com/' xlink:title='Tooltip'>"
        + "<rect width='5' height='5' fill='url(#paint)'/></a>"), POLICY, true);
    assertFalse(safe.contains("https://example.com"));
    assertFalse(safe.contains("xlink:title"));
    assertTrue(safe.contains("url(#paint)"));
    assertTrue(safe.contains("<rect"));
    assertTrue(safe.contains("<stop"));
    assertTrue(SecureSvg.prepare(svg("<rect width='5' height='5' fill='red'/>"), POLICY, false)
        .contains("<rect"));
  }

  @Test
  void generatedSvgPreservesAuthoredNodeNamesAsMetadata() throws Exception {
    String generated = Graphviz.digraph()
        .addNode(Node.builder().id("LL(k)").label("parser").build())
        .addNode(Node.builder().id("Step size (1)\\next").label("step").build())
        .addNode(Node.builder().id("quoted\"<&name").label("escaped").build())
        .build().toSvgStr();
    for (boolean raster : new boolean[]{true, false}) {
      String safe = SecureSvg.prepare(generated, POLICY, raster);
      assertTrue(safe.contains("LL(k)"));
      assertTrue(safe.contains("Step size (1)\\next"));
      assertTrue(safe.contains("quoted&quot;&lt;&amp;name"));
    }
  }

  @Test
  void metadataIsNotParsedAsCssButCannotBypassResourceValidation() throws Exception {
    for (String name : new String[]{"id", "class", "data-node-decoration"}) {
      String metadata = name + "='LL(k)\\name url(https://example.com/not-a-resource)'";
      String safe = SecureSvg.prepare(svg("<g " + metadata + "/>"), POLICY, true);
      assertTrue(safe.contains("LL(k)\\name url(https://example.com/not-a-resource)"));
      for (String attribute : new String[]{
          "fill='url(https://example.com/paint.svg#x)'",
          "stroke='u\\72l(https://example.com/paint.svg#x)'",
          "clip-path='url(file:///tmp/clip.svg#x)'",
          "style='fill: url(https://example.com/paint.svg#x)'",
          "onload='alert(1)'"}) {
        assertThrows(IllegalArgumentException.class,
            () -> SecureSvg.prepare(svg("<g " + metadata + " " + attribute + "/>"), POLICY, true));
      }
      assertThrows(Exception.class, () -> SecureSvg.prepare(
          svg("<image " + metadata + " href='https://example.com/image.png'/>"), POLICY, true));
      assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
          svg("<use " + metadata + " href='file:///tmp/image.svg#x'/>"), POLICY, true));
      assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
          svg("<g xmlns:foreign='urn:foreign' foreign:" + metadata + "/>"), POLICY, true));
    }
  }

  @Test
  void approvedFilesAndEmbeddedImagesBecomeCanonicalDataOnly(@TempDir Path directory)
      throws Exception {
    byte[] png = png(2, 2);
    Path file = directory.resolve("misleading.svg");
    Files.write(file, png);
    SecurityPolicy approved = SecurityPolicy.builder().localImageBaseDirectory(directory).build();
    String canonical = "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    for (String reference : new String[]{file.toUri().toString(), "misleading.svg",
        canonical.replace("data:image/png;base64,", "DATA:IMAGE/PNG;BASE64,")}) {
      for (String attribute : new String[]{"href", "xlink:href"}) {
        for (boolean raster : new boolean[]{true, false}) {
          String safe = SecureSvg.prepare(svg("<image " + attribute + "='" + reference
              + "' width='2' height='2'/>"), approved, raster);
          assertTrue(safe.contains("xlink:href=\"" + canonical + "\""));
          assertFalse(safe.contains("misleading.svg"));
          assertFalse(safe.contains("file:"));
        }
      }
    }
    assertThrows(Exception.class, () -> SecureSvg.prepare(
        svg("<image href='" + file.toUri() + "'/>"), POLICY, true));
    Files.write(directory.resolve("not-raster.png"), svg("<rect/>").getBytes(
        java.nio.charset.StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
        svg("<image href='not-raster.png'/>"), approved, false));
    assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
        svg("<image href='" + canonical + "' xlink:href='" + canonical + "'/>"), approved, true));
  }

  @Test
  void embeddedMimeAndRasterPayloadMustMatch() throws Exception {
    String encoded = Base64.getEncoder().encodeToString(png(2, 2));
    for (String mime : new String[]{"jpeg", "gif", "bmp", "webp"}) {
      assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
          svg("<image href='data:image/" + mime + ";base64," + encoded + "'/>"), POLICY, true));
    }
    byte[] truncated = Arrays.copyOf(png(2, 2), 33);
    assertThrows(Exception.class, () -> SecureSvg.prepare(svg("<image href='data:image/png;base64,"
        + Base64.getEncoder().encodeToString(truncated) + "'/>"), POLICY, true));
    assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
        svg("<image href='data:image/png;base64," + encoded + "'/>"),
        SecurityPolicy.builder().maxImagePixels(1).build(), true));
  }

  @Test
  void aggregateImageBudgetsApplyToRepeatedReferences(@TempDir Path directory) throws Exception {
    // Trailing bytes keep this a decodable PNG while exercising the loaded/encoded byte budgets.
    Path file = directory.resolve("padded.png");
    Files.write(file, Arrays.copyOf(png(1, 1), 8 * 1024 * 1024));
    SecurityPolicy approved = SecurityPolicy.builder().localImageBaseDirectory(directory).build();
    assertTrue(assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
        svg(repeat("<image href='padded.png'/>", 5)), approved, false))
        .getMessage().contains("aggregate image byte limit"));
    String embedded = "<image href='data:image/png;base64,"
        + Base64.getEncoder().encodeToString(png(2048, 2048)) + "'/>";
    assertTrue(assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(
        svg(repeat(embedded, 5)), POLICY, true))
        .getMessage().contains("aggregate decoded raster byte limit"));
  }

  @Test
  void allowlistedPrivateHostsStillReceiveNoRequests() throws Exception {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      requests.incrementAndGet();
      exchange.sendResponseHeaders(302, -1);
      exchange.close();
    });
    server.start();
    try {
      SecurityPolicy approved = SecurityPolicy.builder().allowRemoteImages(true)
          .allowRemoteImageHost("127.0.0.1").build();
      String reference = "http://127.0.0.1:" + server.getAddress().getPort() + "/redirect";
      for (boolean raster : new boolean[]{true, false}) {
        assertThrows(Exception.class, () -> SecureSvg.prepare(
            svg("<image href='" + reference + "'/>"), approved, raster));
      }
      assertEquals(0, requests.get());
    } finally {
      server.stop(0);
    }
  }

  private static byte[] png(int width, int height) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB),
        "png", output));
    return output.toByteArray();
  }

  private static String repeat(String value, int count) {
    StringBuilder result = new StringBuilder(value.length() * count);
    for (int i = 0; i < count; i++) {
      result.append(value);
    }
    return result.toString();
  }

  @Test
  void outputBudgetUsesCeilingAndPointToPixelConversion() throws Exception {
    SecurityPolicy policy = SecurityPolicy.builder().maxOutputPixels(100).build();
    SecureSvg.prepare(svg(""), policy, true);
    assertThrows(IllegalArgumentException.class,
        () -> SecureSvg.prepare(svg("").replace("10px", "10pt"), policy, true));
    assertThrows(IllegalArgumentException.class,
        () -> SecureSvg.prepare(svg("").replace("10px", "10.1px"), policy, true));
    for (String dimension : new String[]{"", "0", "-1", "NaN", "Infinity", "1e100", "100%"}) {
      assertThrows(IllegalArgumentException.class,
          () -> SecureSvg.prepare(svg("").replace("10px", dimension), policy, true), dimension);
    }
  }

  @Test
  void pdfPageDimensionsAreNotARasterOutputBudget() throws Exception {
    SecurityPolicy policy = SecurityPolicy.builder().maxOutputPixels(1).build();
    assertThrows(IllegalArgumentException.class, () -> SecureSvg.prepare(svg(""), policy, true));
    assertTrue(SecureSvg.prepare(svg(""), policy, false).contains("<svg"));
    assertThrows(IllegalArgumentException.class,
        () -> SecureSvg.prepare(svg("<script/>"), policy, false));
  }

  private static String svg(String content) {
    return "<svg xmlns='http://www.w3.org/2000/svg' "
        + "xmlns:xlink='http://www.w3.org/1999/xlink' width='10px' height='10px'>"
        + content + "</svg>";
  }
}
