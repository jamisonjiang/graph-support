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

package org.graphper.ui;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;

/**
 * Direct-manipulation controls for the SVG preview: left-drag to pan, wheel or trackpad pinch to
 * zoom around the pointer, and double-click to reset. Batik's own interactors are disabled so these
 * gestures are the single source of truth.
 *
 * @author Jamison Jiang
 */
final class CanvasInteraction extends MouseAdapter {

  private final JSVGCanvas canvas;
  private Runnable transformListener = () -> {
  };
  private Point dragStart;
  private AffineTransform dragTransform;

  CanvasInteraction(JSVGCanvas canvas) {
    this.canvas = canvas;
  }

  /**
   * Registers a listener notified whenever a gesture changes the canvas rendering transform, so the
   * surrounding UI can refresh things such as a zoom readout.
   *
   * @param listener the listener, or {@code null} to clear it
   */
  void setTransformListener(Runnable listener) {
    this.transformListener = listener == null ? () -> {
    } : listener;
  }

  void install() {
    canvas.setEnablePanInteractor(false);
    canvas.setEnableZoomInteractor(false);
    canvas.setEnableImageZoomInteractor(false);
    canvas.addMouseListener(this);
    canvas.addMouseMotionListener(this);
    canvas.addMouseWheelListener(this);
    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    installMacMagnification();
  }

  @Override
  public void mousePressed(MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      dragStart = event.getPoint();
      dragTransform = transform();
      canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      event.consume();
    }
  }

  @Override
  public void mouseDragged(MouseEvent event) {
    if (dragStart == null || dragTransform == null) {
      return;
    }
    AffineTransform translated = AffineTransform.getTranslateInstance(
        event.getX() - dragStart.x, event.getY() - dragStart.y);
    translated.concatenate(dragTransform);
    canvas.setRenderingTransform(translated);
    transformListener.run();
    event.consume();
  }

  @Override
  public void mouseReleased(MouseEvent event) {
    dragStart = null;
    dragTransform = null;
    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  @Override
  public void mouseClicked(MouseEvent event) {
    if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)) {
      canvas.resetRenderingTransform();
      transformListener.run();
      event.consume();
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent event) {
    double rotation = event.getPreciseWheelRotation();
    if (rotation == 0) {
      return;
    }
    zoomAt(Math.pow(1.12, -rotation), event.getPoint());
    event.consume();
  }

  void zoomAt(double factor, Point point) {
    if (!Double.isFinite(factor) || factor <= 0) {
      return;
    }
    factor = Math.max(.05, Math.min(20, factor));
    AffineTransform zoom = AffineTransform.getTranslateInstance(point.x, point.y);
    zoom.scale(factor, factor);
    zoom.translate(-point.x, -point.y);
    zoom.concatenate(transform());
    canvas.setRenderingTransform(zoom);
    transformListener.run();
  }

  private AffineTransform transform() {
    AffineTransform current = canvas.getRenderingTransform();
    return current == null ? new AffineTransform() : new AffineTransform(current);
  }

  private void installMacMagnification() {
    try {
      Class<?> gestureListener = Class.forName("com.apple.eawt.event.GestureListener");
      Class<?> magnificationListener = Class.forName("com.apple.eawt.event.MagnificationListener");
      Class<?> utilities = Class.forName("com.apple.eawt.event.GestureUtilities");
      Object listener = Proxy.newProxyInstance(magnificationListener.getClassLoader(),
          new Class<?>[]{magnificationListener}, (proxy, method, args) -> {
            if ("magnify".equals(method.getName()) && args != null && args.length == 1) {
              Method amount = args[0].getClass().getMethod("getMagnification");
              double magnification = ((Number) amount.invoke(args[0])).doubleValue();
              zoomAt(1 + magnification,
                     new Point(canvas.getWidth() / 2, canvas.getHeight() / 2));
            }
            return null;
          });
      utilities.getMethod("addGestureListenerTo", JComponent.class, gestureListener)
          .invoke(null, canvas, listener);
    } catch (ReflectiveOperationException | LinkageError ignored) {
      // Non-macOS runtimes still support precise wheel zoom and direct dragging.
    }
  }
}
