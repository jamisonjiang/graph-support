package org.graphper.api.attributes;

import java.util.Collection;
import org.graphper.util.CollectionUtils;

/**
 * Defines node label font styling options.
 *
 * @author Jamison Jiang
 */
public enum FontStyle implements Style {

  /**
   * Makes the text appear in bold.
   */
  BOLD,

  /**
   * Makes the text appear in italics.
   */
  ITALIC,

  /**
   * Draws a line above the text.
   */
  OVERLINE,

  /**
   * Draws a line under the text.
   */
  UNDERLINE,

  /**
   * Draws a line through the middle of the text.
   */
  STRIKETHROUGH;

  /**
   * Checks if the provided {@code fontStyles} array contains the {@link FontStyle#BOLD} style.
   *
   * @param fontStyles zero or more font styles to search
   * @return {@code true} if {@code BOLD} is present in the array; {@code false} otherwise
   */
  public static boolean containsBold(FontStyle... fontStyles) {
    return contains(FontStyle.BOLD, fontStyles);
  }

  /**
   * Checks if the provided {@code fontStyles} array contains the {@link FontStyle#ITALIC} style.
   *
   * @param fontStyles zero or more font styles to search
   * @return {@code true} if {@code ITALIC} is present in the array; {@code false} otherwise
   */
  public static boolean containsItalic(FontStyle... fontStyles) {
    return contains(FontStyle.ITALIC, fontStyles);
  }

  /**
   * Determines whether a target {@link FontStyle} is present in the provided array.
   *
   * @param target     the {@code FontStyle} to look for
   * @param fontStyles zero or more font styles to search
   * @return {@code true} if {@code target} is found; {@code false} otherwise
   */
  public static boolean contains(FontStyle target, FontStyle... fontStyles) {
    if (target == null || fontStyles == null) {
      return false;
    }
    for (FontStyle fontStyle : fontStyles) {
      if (fontStyle == target) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether a target {@link FontStyle} is present in the provided collection.
   *
   * @param target     the {@code FontStyle} to look for
   * @param fontStyles a collection of font styles to search
   * @return {@code true} if {@code target} is found; {@code false} otherwise
   */
  public static boolean contains(FontStyle target, Collection<FontStyle> fontStyles) {
    if (CollectionUtils.isEmpty(fontStyles) || target == null) {
      return false;
    }
    for (FontStyle fontStyle : fontStyles) {
      if (fontStyle == target) {
        return true;
      }
    }
    return false;
  }
}