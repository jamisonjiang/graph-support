package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DotRenderServiceTest {

  private final DotRenderService service = new DotRenderService();

  @Test
  void rendersDotToSvg() {
    String svg = service.renderSvg("digraph G { a -> b; }");

    assertTrue(svg.contains("<svg"));
    assertTrue(svg.contains("a"));
    assertTrue(svg.contains("b"));
  }

  @Test
  void rejectsInvalidDot() {
    assertThrows(RuntimeException.class, () -> service.renderSvg("digraph G { a -> ; }"));
  }

  @Test
  void exportsSvgBytesWithoutChangingTheDocument() {
    String svg = service.renderSvg("digraph G { a -> b; }");

    assertEquals(svg, new String(service.svgBytes(svg), StandardCharsets.UTF_8));
  }

  @Test
  void exportsPngBytes() {
    byte[] png = service.pngBytes(service.renderSvg("digraph G { a -> b; }"));

    assertTrue(png.length > 8);
    assertTrue(png[0] == (byte) 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G');
  }

  @Test
  void findErrorLocatesInvalidDot() {
    DotError error = service.findError("digraph G { a -> ; }");

    assertNotNull(error);
    assertTrue(error.line() >= 1);
    assertTrue(error.column() >= 0);
    assertNotNull(error.message());
  }

  @Test
  void findErrorReturnsNullForValidDot() {
    assertNull(service.findError("digraph G { a -> b; }"));
  }

  @Test
  void findErrorIgnoresBlankInput() {
    assertNull(service.findError("   "));
  }
}
