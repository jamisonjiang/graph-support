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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Small crisp vector icons drawn with Java2D, so the editor ships without external image resources
 * and stays sharp at any scale. Each {@link Kind} paints inside a 16x16 grid that is scaled to the
 * requested size.
 *
 * @author Jamison Jiang
 */
final class UiIcon implements Icon {

  enum Kind {
    BRAND, OPEN, SAVE, PLAY, EXPORT, ZOOM_IN, ZOOM_OUT, FIT
  }

  private final Kind kind;
  private final int size;

  UiIcon(Kind kind) {
    this(kind, 16);
  }

  UiIcon(Kind kind, int size) {
    this.kind = kind;
    this.size = size;
  }

  @Override
  public void paintIcon(Component component, Graphics graphics, int x, int y) {
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.translate(x, y);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g.setColor(component == null ? Color.DARK_GRAY : component.getForeground());
      g.setStroke(new BasicStroke(Math.max(1.4f, size / 11f), BasicStroke.CAP_ROUND,
                                  BasicStroke.JOIN_ROUND));
      double scale = size / 16D;
      g.scale(scale, scale);
      paint(g);
    } finally {
      g.dispose();
    }
  }

  private void paint(Graphics2D g) {
    switch (kind) {
      case BRAND:
        g.drawLine(4, 5, 12, 3);
        g.drawLine(4, 5, 8, 13);
        g.drawLine(12, 3, 8, 13);
        g.fillOval(1, 2, 6, 6);
        g.fillOval(9, 0, 6, 6);
        g.fillOval(5, 10, 6, 6);
        break;
      case OPEN:
        Path2D folder = new Path2D.Double();
        folder.moveTo(1.5, 4);
        folder.lineTo(6.5, 4);
        folder.lineTo(8, 6);
        folder.lineTo(14.5, 6);
        folder.lineTo(13, 13);
        folder.lineTo(2.5, 13);
        folder.closePath();
        g.draw(folder);
        break;
      case SAVE:
        g.drawRoundRect(2, 1, 12, 14, 2, 2);
        g.drawRect(5, 2, 6, 4);
        g.drawRoundRect(5, 9, 6, 5, 1, 1);
        break;
      case PLAY:
        Path2D play = new Path2D.Double();
        play.moveTo(5, 3);
        play.lineTo(13, 8);
        play.lineTo(5, 13);
        play.closePath();
        g.fill(play);
        break;
      case EXPORT:
        g.drawRoundRect(2, 7, 12, 7, 2, 2);
        g.drawLine(8, 1, 8, 10);
        g.drawLine(5, 4, 8, 1);
        g.drawLine(11, 4, 8, 1);
        break;
      case ZOOM_IN:
      case ZOOM_OUT:
        g.drawOval(2, 2, 9, 9);
        g.draw(new Line2D.Double(10.5, 10.5, 14, 14));
        g.draw(new Line2D.Double(4.5, 6.5, 8.5, 6.5));
        if (kind == Kind.ZOOM_IN) {
          g.draw(new Line2D.Double(6.5, 4.5, 6.5, 8.5));
        }
        break;
      case FIT:
        g.drawLine(2, 6, 2, 2);
        g.drawLine(2, 2, 6, 2);
        g.drawLine(10, 2, 14, 2);
        g.drawLine(14, 2, 14, 6);
        g.drawLine(14, 10, 14, 14);
        g.drawLine(14, 14, 10, 14);
        g.drawLine(6, 14, 2, 14);
        g.drawLine(2, 14, 2, 10);
        break;
      default:
        break;
    }
  }

  @Override
  public int getIconWidth() {
    return size;
  }

  @Override
  public int getIconHeight() {
    return size;
  }
}
