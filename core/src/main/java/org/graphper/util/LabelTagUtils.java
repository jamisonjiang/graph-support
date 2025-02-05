package org.graphper.util;

import java.util.List;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html.BasicLabelTag;
import org.graphper.api.Html.FontAttrs;
import org.graphper.api.Html.FontLabelTag;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.LabelTagType;
import org.graphper.def.FlatPoint;
import org.graphper.layout.LabelAttributes;

public class LabelTagUtils {

  private LabelTagUtils() {
  }

  public static FlatPoint measure(LabelTag labelTag, LabelAttributes labelAttrs) {
    if (labelTag == null || labelAttrs == null) {
      return null;
    }

    List<BasicLabelTag> tags = labelTag.getTags();
    if (CollectionUtils.isEmpty(tags)) {
      return null;
    }

    FlatPoint size = null;
    FlatPoint currentLineSize = null;

    for (BasicLabelTag tag : tags) {
      if (tag.getType() == LabelTagType.BR) {
        if (currentLineSize == null) {
          if (size != null) {
            size.setHeight(size.getHeight() + 10);
          } else {
            size = new FlatPoint(10, 0);
          }
          continue;
        }

        if (size == null) {
          size = currentLineSize;
        } else {
          size.setHeight(size.getHeight() + currentLineSize.getHeight());
          size.setWidth(Math.max(size.getWidth(), currentLineSize.getWidth()));
        }
        currentLineSize = null;
        continue;
      }

      LabelAttributes newAttrs = labelAttrs.clone();

      if (tag instanceof FontLabelTag) {
        FontAttrs fn = ((FontLabelTag) tag).getFontAttrs();
        if (fn != null) {
          // Current font have higher priority
          String f = StringUtils.isEmpty(fn.getFace()) ? newAttrs.getFontName() : fn.getFace();
          double fs = fn.getPointSize() != null ? fn.getPointSize() : newAttrs.getFontSize();
          newAttrs.setFontName(f);
          newAttrs.setFontSize(fs);
        }
      }

      if (tag.getType() == LabelTagType.BOLD) {
        newAttrs.setBold(true);
      }

      if (tag.getType() == LabelTagType.ITALIC) {
        newAttrs.setItalic(true);
      }

      FlatPoint currentLabelSize;
      if (StringUtils.isNotEmpty(tag.getText())) {
        currentLabelSize = FontUtils.measure(tag.getText(), newAttrs.getFontName(),
                                             newAttrs.getFontSize(), 0,
                                             newAttrs.toFontStyles());
      } else {
        currentLabelSize = measure(tag.getSubLabelTag(), newAttrs);
      }

      if (currentLabelSize == null) {
        continue;
      }

      if (currentLineSize == null) {
        currentLineSize = currentLabelSize;
        continue;
      }

      currentLineSize.setWidth(currentLabelSize.getWidth() + currentLineSize.getWidth());
      currentLineSize.setHeight(
          Math.max(currentLineSize.getHeight(), currentLabelSize.getHeight()));
    }

    if (size == null) {
      size = currentLineSize;
    } else if (currentLineSize != null) {
      size.setHeight(size.getHeight() + currentLineSize.getHeight());
      size.setWidth(Math.max(size.getWidth(), currentLineSize.getWidth()));
    }

    return size;
  }
}
