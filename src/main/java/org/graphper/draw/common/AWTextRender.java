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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Objects;
import org.apache_gs.commons.lang3.StringUtils;

public class AWTextRender {

  private final Font font;

  private String text;

  private final double x;

  private final double y;

  private final Graphics2D graphics2D;

  public AWTextRender(Font font, String text, double x, double y, Graphics2D graphics2D) {
    Objects.requireNonNull(font);
    Objects.requireNonNull(text);
    Objects.requireNonNull(graphics2D);
    this.font = font;
    this.x = x;
    this.y = y;
    this.graphics2D = graphics2D;
    if (StringUtils.containsArabic(text)) {
      this.text = ArabicTextHandler.createSubstituteString(text);
    } else {
      this.text = text;
    }
  }

  public double draw() {
    // Prepare AttributedString to handle multi-language layout and shaping
    AttributedString attributedString = new AttributedString(text);
    attributedString.addAttribute(TextAttribute.FONT, font);
    AttributedCharacterIterator iterator = attributedString.getIterator();

    // Use AttributedCharacterIterator to create GlyphVector
    graphics2D.setFont(font);
    FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
    GlyphVector glyphVector = font.createGlyphVector(frc, iterator);

    // Your existing drawing logic starts here
    double xoffset = 0;
    GeneralPath outline = new GeneralPath();
    for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
      // Get the position of the glyph in the vector
      Point2D glyphPos = glyphVector.getGlyphPosition(i);

      // Calculate transformed position based on initial x, y, and glyph position
      double glyphX = x + glyphPos.getX();
      double glyphY = y + glyphPos.getY();

      // Get the outline of the glyph
      Shape glyphOutline = glyphVector.getGlyphOutline(i);

      // Apply a transformation to each glyph for scaling
      AffineTransform transform = AffineTransform.getTranslateInstance(glyphX, glyphY);

      // Apply the transformation to the glyph outline
      Shape transformedGlyph = transform.createTransformedShape(glyphOutline);
      AffineTransform tr = AffineTransform.getTranslateInstance(-glyphPos.getX(), -glyphPos.getY());
      transformedGlyph = tr.createTransformedShape(transformedGlyph);
      outline.append(transformedGlyph, false);

      // Calculate the width of each glyph and add it to the x-offset
      Rectangle2D glyphBounds = glyphVector.getGlyphLogicalBounds(i).getBounds2D();
      xoffset += glyphBounds.getWidth();
    }

    // Render the final outline
    graphics2D.fill(outline);
    return xoffset;
  }
}
