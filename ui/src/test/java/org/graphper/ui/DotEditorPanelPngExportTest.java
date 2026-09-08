package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.graphper.api.SecurityPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DotEditorPanelPngExportTest {

  private final SecurityPolicy policy = SecurityPolicy.builder().build();
  private final CountDownLatch entered = new CountDownLatch(1);
  private final CountDownLatch release = new CountDownLatch(1);
  private final AtomicInteger conversions = new AtomicInteger();
  private final byte[] png = {1, 2, 3};
  private TestPanel panel;
  private JButton button;
  private CountDownLatch completed;
  private RuntimeException failure;
  private String convertedSvg;

  @BeforeEach
  void createPanel() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      panel = new TestPanel(new DotRenderService(policy) {
        @Override
        public byte[] pngBytes(String svg) {
          assertFalse(SwingUtilities.isEventDispatchThread());
          assertSame(policy, securityPolicy());
          conversions.incrementAndGet();
          convertedSvg = svg;
          entered.countDown();
          try {
            assertTrue(release.await(5, TimeUnit.SECONDS), "Conversion was not released");
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
          }
          if (failure != null) {
            throw failure;
          }
          return png;
        }
      });
      button = (JButton) field("exportPng");
      setSvg("first SVG");
      button.addPropertyChangeListener("enabled", event ->
          assertTrue(SwingUtilities.isEventDispatchThread()));
    });
  }

  @AfterEach
  void cleanUp() throws Exception {
    release.countDown();
    if (completed != null) {
      assertTrue(completed.await(5, TimeUnit.SECONDS), "Worker did not finish");
    }
    SwingUtilities.invokeAndWait(() -> panel.removeNotify());
  }

  @Test
  void convertsOffEdtAndExportsOnEdtWithoutDuplicateRequests() throws Exception {
    startExport();
    assertTrue(entered.await(5, TimeUnit.SECONDS));
    SwingUtilities.invokeAndWait(() -> {
      assertFalse(button.isEnabled());
      button.doClick(0);
      assertEquals(0, panel.exports);
    });
    finishExport();
    SwingUtilities.invokeAndWait(() -> {
      assertEquals(1, conversions.get());
      assertEquals("first SVG", convertedSvg);
      assertEquals("png", panel.extension);
      assertArrayEquals(png, panel.bytes);
      assertEquals(1, panel.exports);
      assertNull(panel.error);
    });
  }

  @Test
  void discardsConversionWhenPreviewChanges() throws Exception {
    startExport();
    assertTrue(entered.await(5, TimeUnit.SECONDS));
    SwingUtilities.invokeAndWait(() -> setSvg("new SVG"));
    finishExport();
    SwingUtilities.invokeAndWait(() -> {
      assertEquals("first SVG", convertedSvg);
      assertEquals(0, panel.exports);
      assertNull(panel.error);
    });
    startExport();
    finishExport();
    SwingUtilities.invokeAndWait(() -> {
      assertEquals("new SVG", convertedSvg);
      assertEquals(1, panel.exports);
    });
  }

  @Test
  void reportsConversionFailureOnEdtAndAllowsRetry() throws Exception {
    IllegalArgumentException cause = new IllegalArgumentException("Invalid SVG");
    failure = new IllegalStateException("Unable to export PNG", cause);
    startExport();
    finishExport();
    SwingUtilities.invokeAndWait(() -> {
      assertSame(cause, panel.error);
      assertEquals("Unable to export PNG", panel.errorTitle);
      assertEquals(0, panel.exports);
    });
    failure = null;
    startExport();
    finishExport();
    SwingUtilities.invokeAndWait(() -> assertEquals(1, panel.exports));
  }

  @Test
  void removalCancelsWorkerWithoutOpeningExportOrErrorDialogs() throws Exception {
    startExport();
    assertTrue(entered.await(5, TimeUnit.SECONDS));
    SwingUtilities.invokeAndWait(() -> {
      SwingWorker<?, ?> worker = (SwingWorker<?, ?>) field("pngExportWorker");
      panel.removeNotify();
      assertTrue(worker.isCancelled());
    });
    finishExport();
    SwingUtilities.invokeAndWait(() -> {
      assertEquals(0, panel.exports);
      assertNull(panel.error);
    });
  }

  @Test
  void missingPreviewUsesExistingExportUiWithoutConversion() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      setSvg(null);
      button.doClick(0);
      assertEquals(1, panel.exports);
      assertEquals("png", panel.extension);
      assertNull(panel.bytes);
      assertNull(field("pngExportWorker"));
      assertTrue(button.isEnabled());
      assertEquals(0, conversions.get());
    });
  }

  @Test
  void svgExportStaysDirectWhilePngConversionIsPending() throws Exception {
    startExport();
    assertTrue(entered.await(5, TimeUnit.SECONDS));
    SwingUtilities.invokeAndWait(() -> {
      // SVG is the button immediately before PNG in the toolbar (with a spacer between).
      int index = button.getParent().getComponentZOrder(button);
      JButton svg = (JButton) button.getParent().getComponent(index - 2);
      assertEquals("SVG", svg.getText());
      svg.doClick(0);
      assertEquals("svg", panel.extension);
      assertArrayEquals("first SVG".getBytes(StandardCharsets.UTF_8), panel.bytes);
      assertEquals(1, panel.exports);
      assertFalse(button.isEnabled());
    });
    finishExport();
  }

  private void startExport() throws Exception {
    completed = new CountDownLatch(1);
    SwingUtilities.invokeAndWait(() -> {
      button.doClick(0);
      SwingWorker<?, ?> worker = (SwingWorker<?, ?>) field("pngExportWorker");
      worker.addPropertyChangeListener(event -> {
        if ("state".equals(event.getPropertyName())
            && event.getNewValue() == SwingWorker.StateValue.DONE) {
          completed.countDown();
        }
      });
      assertFalse(button.isEnabled());
    });
  }

  private void finishExport() throws Exception {
    release.countDown();
    assertTrue(completed.await(5, TimeUnit.SECONDS), "Worker did not finish");
    SwingUtilities.invokeAndWait(() -> {
      assertTrue(button.isEnabled());
      assertNull(field("pngExportWorker"));
    });
  }

  private Object field(String name) {
    try {
      Field field = DotEditorPanel.class.getDeclaredField(name);
      field.setAccessible(true);
      return field.get(panel);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private void setSvg(String svg) {
    try {
      Field field = DotEditorPanel.class.getDeclaredField("lastSvg");
      field.setAccessible(true);
      field.set(panel, svg);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static class TestPanel extends DotEditorPanel {
    private int exports;
    private String extension;
    private byte[] bytes;
    private String errorTitle;
    private Exception error;

    TestPanel(DotRenderService service) {
      super(service);
    }

    @Override
    void export(String extension, byte[] bytes) {
      assertTrue(SwingUtilities.isEventDispatchThread());
      exports++;
      this.extension = extension;
      this.bytes = bytes;
    }

    @Override
    void showError(String title, Exception error) {
      assertTrue(SwingUtilities.isEventDispatchThread());
      this.errorTitle = title;
      this.error = error;
    }
  }
}
