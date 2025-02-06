package org.graphper.util;

import java.util.List;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Html.BasicLabelTag;
import org.graphper.api.Html.FontAttrs;
import org.graphper.api.Html.FontLabelTag;
import org.graphper.api.Html.LabelTag;
import org.graphper.api.Html.LabelTagType;
import org.graphper.def.CycleDependencyException;
import org.graphper.def.FlatPoint;
import org.graphper.layout.LabelAttributes;

/**
 * Utility class for processing and measuring {@link LabelTag} elements.
 *
 * @author Jamison Jiang
 */
public class LabelTagUtils {

  private LabelTagUtils() {
  }

  /**
   * Measures the size of the given {@link LabelTag} based on its attributes.
   *
   * @param labelTag   The {@link LabelTag} whose size needs to be measured.
   * @param labelAttrs The {@link LabelAttributes} containing font and style information.
   * @return A {@link FlatPoint} representing the measured width and height of the label, or
   * {@code null} if the label contains no measurable content.
   * @throws CycleDependencyException If the label contains a cycle dependency.
   */
  public static FlatPoint measure(LabelTag labelTag, LabelAttributes labelAttrs) {
    if (labelTag == null || labelAttrs == null) {
      return null;
    }

    if (labelAttrs.isMark(labelTag)) {
      throw new CycleDependencyException(
          "Cannot measure size due to LabelTag has cycle dependency");
    }
    labelAttrs.mark(labelTag);

    List<BasicLabelTag> tags = labelTag.getTags();
    if (CollectionUtils.isEmpty(tags)) {
      labelAttrs.remove(labelTag);
      return null;
    }

    FlatPoint size = null;
    FlatPoint currentLineSize = null;

    for (BasicLabelTag tag : tags) {
      TagProcessingResult result = processTag(tag, currentLineSize, labelAttrs, size);
      size = result.getSize();
      currentLineSize = result.getCurrentLineSize();
    }

    labelAttrs.remove(labelTag);
    return finalizeSize(size, currentLineSize);
  }

  private static TagProcessingResult processTag(BasicLabelTag tag, FlatPoint currentLineSize,
                                                LabelAttributes labelAttrs, FlatPoint size) {
    if (isLineBreak(tag)) {
      size = processLineBreak(size, currentLineSize);
      return new TagProcessingResult(size, null);
    }

    LabelAttributes newAttrs = applyTagAttributes(tag, labelAttrs);
    FlatPoint currentLabelSize = measureTagSize(tag, newAttrs);
    if (currentLabelSize == null) {
      return new TagProcessingResult(size, currentLineSize);
    }

    currentLineSize = updateCurrentLineSize(currentLineSize, currentLabelSize);
    return new TagProcessingResult(size, currentLineSize);
  }

  private static boolean isLineBreak(BasicLabelTag tag) {
    return tag.getType() == LabelTagType.BR;
  }

  private static FlatPoint processLineBreak(FlatPoint size, FlatPoint currentLineSize) {
    if (currentLineSize == null) {
      if (size != null) {
        size.setHeight(size.getHeight() + 10);
      } else {
        size = new FlatPoint(10, 0);
      }
      return size;
    }

    if (size == null) {
      return currentLineSize;
    } else {
      size.setHeight(size.getHeight() + currentLineSize.getHeight());
      size.setWidth(Math.max(size.getWidth(), currentLineSize.getWidth()));
    }
    return size;
  }

  private static LabelAttributes applyTagAttributes(BasicLabelTag tag, LabelAttributes labelAttrs) {
    LabelAttributes newAttrs = labelAttrs.clone();

    if (tag instanceof FontLabelTag) {
      FontAttrs fn = ((FontLabelTag) tag).getFontAttrs();
      if (fn != null) {
        String fontName = StringUtils.isEmpty(fn.getFace()) ? newAttrs.getFontName() : fn.getFace();
        double fontSize = fn.getPointSize() != null ? fn.getPointSize() : newAttrs.getFontSize();
        newAttrs.setFontName(fontName);
        newAttrs.setFontSize(fontSize);
      }
    }

    if (tag.getType() == LabelTagType.BOLD) {
      newAttrs.setBold(true);
    }
    if (tag.getType() == LabelTagType.ITALIC) {
      newAttrs.setItalic(true);
    }

    return newAttrs;
  }

  private static FlatPoint measureTagSize(BasicLabelTag tag, LabelAttributes labelAttrs) {
    if (StringUtils.isNotEmpty(tag.getText())) {
      return FontUtils.measure(tag.getText(), labelAttrs.getFontName(),
                               labelAttrs.getFontSize(), 0,
                               labelAttrs.toFontStyles());
    }
    return measure(tag.getSubLabelTag(), labelAttrs);
  }

  private static FlatPoint updateCurrentLineSize(FlatPoint currentLineSize,
                                                 FlatPoint currentLabelSize) {
    if (currentLineSize == null) {
      return currentLabelSize;
    }

    currentLineSize.setWidth(currentLabelSize.getWidth() + currentLineSize.getWidth());
    currentLineSize.setHeight(Math.max(currentLineSize.getHeight(), currentLabelSize.getHeight()));
    return currentLineSize;
  }

  private static FlatPoint finalizeSize(FlatPoint size, FlatPoint currentLineSize) {
    if (size == null) {
      return currentLineSize;
    }
    if (currentLineSize != null) {
      size.setHeight(size.getHeight() + currentLineSize.getHeight());
      size.setWidth(Math.max(size.getWidth(), currentLineSize.getWidth()));
    }
    return size;
  }

  private static class TagProcessingResult {

    private final FlatPoint size;
    private final FlatPoint currentLineSize;

    public TagProcessingResult(FlatPoint size, FlatPoint currentLineSize) {
      this.size = size;
      this.currentLineSize = currentLineSize;
    }

    public FlatPoint getSize() {
      return size;
    }

    public FlatPoint getCurrentLineSize() {
      return currentLineSize;
    }
  }
}
