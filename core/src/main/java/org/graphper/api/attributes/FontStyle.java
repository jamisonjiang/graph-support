package org.graphper.api.attributes;

import java.util.Collection;
import org.graphper.util.CollectionUtils;

public enum FontStyle implements Style {

  BOLD,

  ITALIC,

  OVERLINE,

  UNDERLINE,

  STRIKETHROUGH;

  public static boolean containsBold(FontStyle... fontStyles) {
    return contains(FontStyle.BOLD, fontStyles);
  }

  public static boolean containsItalic(FontStyle... fontStyles) {
    return contains(FontStyle.ITALIC, fontStyles);
  }

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
