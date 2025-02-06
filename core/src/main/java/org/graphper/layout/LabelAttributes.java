package org.graphper.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.attributes.Color;
import org.graphper.api.attributes.FontStyle;
import org.graphper.util.CollectionUtils;

/**
 * Represents the attributes of a label, including font settings and text decorations.
 *
 * @author Jamison Jiang
 */
public class LabelAttributes extends Mark<LabelTag> implements Cloneable {

  private Color fontColor;

  private String fontName;

  private double fontSize;

  private boolean bold;

  private boolean italic;

  private boolean overline;

  private boolean underline;

  private boolean strikethrough;

  public Color getFontColor() {
    return fontColor;
  }

  public void setFontColor(Color fontColor) {
    this.fontColor = fontColor;
  }

  public String getFontName() {
    return fontName;
  }

  public void setFontName(String fontName) {
    this.fontName = fontName;
  }

  public double getFontSize() {
    return fontSize;
  }

  public void setFontSize(double fontSize) {
    this.fontSize = fontSize;
  }

  public boolean isBold() {
    return bold;
  }

  public void setBold(boolean bold) {
    this.bold = bold;
  }

  public boolean isItalic() {
    return italic;
  }

  public void setItalic(boolean italic) {
    this.italic = italic;
  }

  public boolean isOverline() {
    return overline;
  }

  public void setOverline(boolean overline) {
    this.overline = overline;
  }

  public boolean isUnderline() {
    return underline;
  }

  public void setUnderline(boolean underline) {
    this.underline = underline;
  }

  public boolean isStrikethrough() {
    return strikethrough;
  }

  public void setStrikethrough(boolean strikethrough) {
    this.strikethrough = strikethrough;
  }

  /**
   * Converts the label's attributes into an array of font styles.
   *
   * @return An array of {@link FontStyle} representing the applied styles,
   *         or {@code null} if no styles are applied.
   */
  public FontStyle[] toFontStyles() {
    List<FontStyle> fontStyles = null;
    if (bold) {
      fontStyles = new ArrayList<>();
      fontStyles.add(FontStyle.BOLD);
    }

    if (italic) {
      if (fontStyles == null) {
        fontStyles = new ArrayList<>();
      }
      fontStyles.add(FontStyle.ITALIC);
    }

    if (overline) {
      if (fontStyles == null) {
        fontStyles = new ArrayList<>();
      }
      fontStyles.add(FontStyle.OVERLINE);
    }

    if (underline) {
      if (fontStyles == null) {
        fontStyles = new ArrayList<>();
      }
      fontStyles.add(FontStyle.UNDERLINE);
    }

    if (strikethrough) {
      if (fontStyles == null) {
        fontStyles = new ArrayList<>();
      }
      fontStyles.add(FontStyle.STRIKETHROUGH);
    }

    if (CollectionUtils.isNotEmpty(fontStyles)) {
      return fontStyles.toArray(new FontStyle[0]);
    }

    return null;
  }

  /**
   * Converts the label's attributes which affect the text size into an array of font styles.
   *
   * @return An array of {@link FontStyle} representing the applied styles,
   *         or {@code null} if no styles are applied.
   */
  public FontStyle[] toMeasureFontStyles() {
    if (bold && italic) {
      return new FontStyle[]{FontStyle.BOLD, FontStyle.ITALIC};
    } else if (bold) {
      return new FontStyle[]{FontStyle.BOLD};
    } else if (italic) {
      return new FontStyle[]{FontStyle.ITALIC};
    }
    return null;
  }

  public void setByStyles(Collection<FontStyle> styles) {
    if (CollectionUtils.isEmpty(styles)) {
      return;
    }

    for (FontStyle style : styles) {
      if (style == FontStyle.BOLD) {
        bold = true;
      }
      if (style == FontStyle.ITALIC) {
        italic = true;
      }
      if (style == FontStyle.OVERLINE) {
        overline = true;
      }
      if (style == FontStyle.UNDERLINE) {
        underline = true;
      }
      if (style == FontStyle.STRIKETHROUGH) {
        strikethrough = true;
      }
    }
  }

  @Override
  public LabelAttributes clone() {
    try {
      return (LabelAttributes) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
