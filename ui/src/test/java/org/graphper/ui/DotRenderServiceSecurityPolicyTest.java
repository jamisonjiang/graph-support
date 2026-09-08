package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.graphper.api.SecurityPolicy;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.common.BatikImgConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The editor used to parse and render with the built-in defaults and no way to change them. These
 * cases pin the configuration path and that the default is still the secure one.
 */
class DotRenderServiceSecurityPolicyTest {

  @AfterEach
  void clearProperties() {
    System.clearProperty(DotRenderService.ALLOWED_IMAGE_HOSTS_PROPERTY);
    System.clearProperty(DotRenderService.IMAGE_BASE_DIRECTORY_PROPERTY);
  }

  @Test
  void withoutConfigurationTheSecureDefaultIsKept() {
    assertSame(SecurityPolicy.defaultPolicy(), new DotRenderService().securityPolicy());
  }

  @Test
  void anExplicitPolicyIsUsedForRendering() {
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("images.example.com").build();
    DotRenderService service = new DotRenderService(policy);

    assertSame(policy, service.securityPolicy());
    String svg = service.renderSvg(
        "digraph { a [image=\"https://images.example.com/logo.png\"]; }");
    assertTrue(svg.contains("https://images.example.com/logo.png"), svg);
  }

  @Test
  void theDefaultServiceStillDropsARemoteImage() {
    String svg = new DotRenderService().renderSvg(
        "digraph { a [image=\"https://images.example.com/logo.png\"]; }");

    assertFalse(svg.contains("https://images.example.com/logo.png"), svg);
    assertFalse(svg.contains("<image"), svg);
  }

  @Test
  void aNullPolicyFallsBackToTheSecureDefault() {
    assertSame(SecurityPolicy.defaultPolicy(), new DotRenderService(null).securityPolicy());
  }

  @Test
  void hostsComeFromTheSystemProperty() {
    System.setProperty(DotRenderService.ALLOWED_IMAGE_HOSTS_PROPERTY,
                       "images.example.com, cdn.example.com");
    SecurityPolicy policy = DotRenderService.policyFromSystemProperties();

    assertTrue(policy.isAllowRemoteImages());
    assertEquals(2, policy.getAllowedRemoteImageHosts().size());
    assertTrue(policy.getAllowedRemoteImageHosts().contains("images.example.com"));
    assertTrue(policy.getAllowedRemoteImageHosts().contains("cdn.example.com"));
    assertNull(policy.getLocalImageBaseDirectory());
  }

  @Test
  void theBaseDirectoryComesFromTheSystemProperty(@TempDir Path directory) throws Exception {
    Files.write(directory.resolve("logo.png"), new byte[]{1, 2, 3});
    System.setProperty(DotRenderService.IMAGE_BASE_DIRECTORY_PROPERTY, directory.toString());
    DotRenderService service = new DotRenderService();

    assertEquals(directory.toAbsolutePath().normalize(),
                 service.securityPolicy().getLocalImageBaseDirectory());
    assertFalse(service.securityPolicy().isAllowRemoteImages());
    assertTrue(service.renderSvg("digraph { a [image=\"logo.png\"]; }").contains("<image"));
  }

  @Test
  void aMalformedHostIsIgnoredRatherThanOpeningTheEditorWithABrokenPolicy() {
    System.setProperty(DotRenderService.ALLOWED_IMAGE_HOSTS_PROPERTY, "not a host");

    assertSame(SecurityPolicy.defaultPolicy(), DotRenderService.policyFromSystemProperties());
  }

  @Test
  void blankPropertiesLeaveTheDefaultAlone() {
    System.setProperty(DotRenderService.ALLOWED_IMAGE_HOSTS_PROPERTY, "  ");
    System.setProperty(DotRenderService.IMAGE_BASE_DIRECTORY_PROPERTY, "  ");

    assertSame(SecurityPolicy.defaultPolicy(), DotRenderService.policyFromSystemProperties());
  }

  @Test
  void rawSvgNeverFetchesEvenAnAllowlistedRedirectOrExternalDtd() throws Exception {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    String base = "http://127.0.0.1:" + server.getAddress().getPort();
    server.createContext("/", exchange -> {
      requests.incrementAndGet();
      exchange.getResponseHeaders().add("Location", base + "/private-image.png");
      exchange.sendResponseHeaders(302, -1);
      exchange.close();
    });
    server.start();
    try {
      SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
          .allowRemoteImageHost("127.0.0.1").build();
      DotRenderService service = new DotRenderService(policy);
      for (String content : new String[]{
          "<image xlink:href='" + base + "/redirect' width='10' height='10'/>",
          "<use href='" + base + "/external.svg#x'/>",
          "<style>@import url(" + base + "/style.css);</style>",
          "<rect fill='url(" + base + "/paint.svg#x)'/>",
          "<script xlink:href='" + base + "/script.js'/>"}) {
        assertThrows(IllegalStateException.class, () -> service.pngBytes(rawSvg(content)), content);
      }
      assertThrows(IllegalStateException.class, () -> service.pngBytes(
          "<!DOCTYPE svg SYSTEM '" + base + "/svg.dtd'>" + rawSvg("<rect width='5' height='5'/>")));
      // Navigation is independently allowed and stripped, without a doctype masking the result.
      BufferedImage png = ImageIO.read(new ByteArrayInputStream(service.pngBytes(rawSvg(
          "<a xlink:href='" + base + "/redirect'><rect width='10' height='10' fill='red'/></a>"))));
      assertNotNull(png);
      assertEquals(0xffff0000, png.getRGB(5, 5));
      assertEquals(0, requests.get(), "No request, including the initial redirect, may reach Batik");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void rawSvgRendersApprovedLocalFilesAndRejectsDefaultAndTraversal(@TempDir Path directory)
      throws Exception {
    Path base = Files.createDirectory(directory.resolve("approved"));
    Path image = base.resolve("image.png");
    BufferedImage source = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < 10; y++) {
      for (int x = 0; x < 10; x++) {
        source.setRGB(x, y, 0xffff0000);
      }
    }
    assertTrue(ImageIO.write(source, "png", image.toFile()));
    Path outside = directory.resolve("outside.png");
    Files.copy(image, outside);
    SecurityPolicy policy = SecurityPolicy.builder().localImageBaseDirectory(base).build();
    DotRenderService service = new DotRenderService(policy);
    DotRenderService defaults = new DotRenderService(SecurityPolicy.defaultPolicy());
    for (String reference : new String[]{image.toUri().toString(), "image.png"}) {
      assertEquals(image.toRealPath().toUri().toString(), policy.sanitizeImage(reference));
      for (String attribute : new String[]{"href", "xlink:href"}) {
        String svg = rawSvg("<image width='10' height='10' " + attribute + "='" + reference + "'/>");
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(service.pngBytes(svg)));
        assertNotNull(rendered);
        assertEquals(10, rendered.getWidth());
        assertEquals(10, rendered.getHeight());
        assertEquals(source.getRGB(5, 5), rendered.getRGB(5, 5), "The image must not be dropped");
        assertThrows(IllegalStateException.class, () -> defaults.pngBytes(svg));
      }
      String generated = service.renderSvg("digraph { a [shape=box, label=\"\", image=\""
          + reference + "\"]; }");
      assertTrue(generated.contains("<image"), "The generated graph must retain the approved image");
      BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(service.pngBytes(generated)));
      assertNotNull(rendered);
      assertEquals(0xffff0000, rendered.getRGB(rendered.getWidth() / 2, rendered.getHeight() / 2));
    }
    for (String reference : new String[]{"../outside.png", outside.toUri().toString()}) {
      assertNull(policy.sanitizeImage(reference));
      assertThrows(IllegalStateException.class, () -> service.pngBytes(rawSvg(
          "<image width='10' height='10' xlink:href='" + reference + "'/>")));
    }
  }

  @Test
  void rawSvgRejectsScriptsEntitiesAndDimensionOverrides() {
    DotRenderService service = new DotRenderService();
    for (String content : new String[]{"<script>alert(1)</script>", "<g onload='alert(1)'/>",
        "<foreignObject/>", "<animate attributeName='width' to='1000000'/>",
        "<g style='width:1000000px'/>"}) {
      assertThrows(IllegalStateException.class, () -> service.pngBytes(rawSvg(content)), content);
    }
    assertThrows(IllegalStateException.class, () -> service.pngBytes(
        "<!DOCTYPE svg [<!ENTITY x SYSTEM 'file:///etc/passwd'>]>"
            + rawSvg("<text>&x;</text>")));
  }

  @Test
  void pngExportHonorsTheServiceOutputPixelBudget() throws Exception {
    DotRenderService service = new DotRenderService(SecurityPolicy.builder()
        .maxOutputPixels(100).build());
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(service.pngBytes(rawSvg(""))));
    assertEquals(10, image.getWidth());
    assertEquals(10, image.getHeight());
    IllegalStateException error = assertThrows(IllegalStateException.class,
        () -> service.pngBytes(rawSvg("").replace("10px", "10pt")));
    assertTrue(error.getCause().getMessage().contains("pixel limit"));
    assertThrows(IllegalStateException.class,
        () -> service.pngBytes(service.renderSvg("digraph { a -> b; }")));
  }

  @Test
  void modernBatikRemainsSupportedAndEmbeddedRasterImagesStillExport() throws Exception {
    assertTrue(new BatikImgConverter().envSupport(), "The supported UI Batik must remain selectable");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
    String content = "<image width='2' height='2' xlink:href='data:image/png;base64,"
        + Base64.getEncoder().encodeToString(output.toByteArray()) + "'/>";
    byte[] png = new DotRenderService().pngBytes(rawSvg(content));
    assertTrue(png.length > 8);
    DotRenderService limited = new DotRenderService(SecurityPolicy.builder().maxImagePixels(1).build());
    assertThrows(IllegalStateException.class, () -> limited.pngBytes(rawSvg(content)));
  }

  @Test
  void generatedLinksTooltipsAndDecoratedShapesStillExport() {
    DotRenderService service = new DotRenderService();
    String svg = service.renderSvg("digraph { a [shape=note, href=\"https://example.com/\", "
        + "tooltip=\"A note\"]; a -> b; }");
    assertTrue(service.pngBytes(svg).length > 8);
  }

  @Test
  void directPngConversionCannotContinueAfterHintRejection() {
    BatikImgConverter converter = new BatikImgConverter() {
      @Override
      protected void configureSecurityHints(Object transcoder) {
        throw new IllegalStateException("required security hint rejected");
      }
    };
    FailInitResourceException error = assertThrows(FailInitResourceException.class,
        () -> converter.pngBytes(rawSvg(""), SecurityPolicy.defaultPolicy()));
    assertTrue(error.getCause().getMessage().contains("required security hint rejected"));
  }

  private static String rawSvg(String content) {
    return "<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' "
        + "width='10px' height='10px'>" + content + "</svg>";
  }
}
