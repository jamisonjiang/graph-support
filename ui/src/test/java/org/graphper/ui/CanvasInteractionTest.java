package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import org.apache.batik.swing.JSVGCanvas;
import org.junit.jupiter.api.Test;

class CanvasInteractionTest {

  @Test
  void preciseTrackpadWheelZoomsEvenWhenIntegerRotationIsZero() {
    JSVGCanvas canvas = canvas();
    CanvasInteraction interaction = new CanvasInteraction(canvas);
    AffineTransform before = canvas.getRenderingTransform();
    MouseWheelEvent event = new MouseWheelEvent(canvas, MouseEvent.MOUSE_WHEEL,
        System.currentTimeMillis(), 0, 200, 150, 200, 150, 0, false,
        MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 0, .25);

    interaction.mouseWheelMoved(event);

    assertNotEquals(before, canvas.getRenderingTransform());
  }

  @Test
  void leftDragPansTheCanvas() {
    JSVGCanvas canvas = canvas();
    CanvasInteraction interaction = new CanvasInteraction(canvas);
    interaction.mousePressed(mouse(canvas, MouseEvent.MOUSE_PRESSED, 100, 100));
    interaction.mouseDragged(mouse(canvas, MouseEvent.MOUSE_DRAGGED, 145, 130));

    assertEquals(45, canvas.getRenderingTransform().getTranslateX(), .01);
    assertEquals(30, canvas.getRenderingTransform().getTranslateY(), .01);
  }

  private static JSVGCanvas canvas() {
    JSVGCanvas canvas = new JSVGCanvas();
    canvas.setSize(800, 600);
    canvas.setRenderingTransform(new AffineTransform());
    return canvas;
  }

  private static MouseEvent mouse(JSVGCanvas canvas, int id, int x, int y) {
    return new MouseEvent(canvas, id, System.currentTimeMillis(), 0, x, y, 1, false,
        MouseEvent.BUTTON1);
  }
}
