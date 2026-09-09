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

package org.graphper.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache_gs.commons.lang3.StringUtils;

/**
 * Parses Graphviz line terminators in plain labels. Besides a normal centered line break, Graphviz
 * supports {@code \l} and {@code \r}, which terminate a line and align it to the left or right side
 * of its label box.
 *
 * @author Jamison Jiang
 */
public final class LabelLines {

  /** Internal marker emitted by the DOT lexer for a left-aligned line break. */
  public static final char LEFT_BREAK = '\uE000';

  /** Internal marker emitted by the DOT lexer for a right-aligned line break. */
  public static final char RIGHT_BREAK = '\uE001';

  private LabelLines() {}

  /**
   * Returns the internal left-break marker. Use this method across module boundaries to avoid Java
   * compile-time constant inlining.
   */
  public static char leftBreak() {
    return LEFT_BREAK;
  }

  /**
   * Returns the internal right-break marker. Use this method across module boundaries to avoid Java
   * compile-time constant inlining.
   */
  public static char rightBreak() {
    return RIGHT_BREAK;
  }

  /**
   * Converts alignment-aware line terminators to ordinary newlines for text measurement.
   *
   * @param label label text
   * @return measurement-safe text
   */
  public static String plainText(String label) {
    if (StringUtils.isEmpty(label)) {
      return label;
    }
    return label.replace(LEFT_BREAK, '\n').replace(RIGHT_BREAK, '\n');
  }

  /**
   * Splits a label into lines while preserving how each line was terminated.
   *
   * @param label label text
   * @return parsed lines, never empty
   */
  public static List<Line> parse(String label) {
    if (label == null) {
      return Collections.singletonList(new Line(null, Alignment.CENTER));
    }

    List<Line> lines = new ArrayList<>();
    StringBuilder text = new StringBuilder();
    for (int i = 0; i < label.length(); i++) {
      char c = label.charAt(i);
      if (c == '\r' && i + 1 < label.length() && label.charAt(i + 1) == '\n') {
        i++;
        addLine(lines, text, Alignment.CENTER);
      } else if (c == '\r' || c == '\n') {
        addLine(lines, text, Alignment.CENTER);
      } else if (c == LEFT_BREAK) {
        addLine(lines, text, Alignment.LEFT);
      } else if (c == RIGHT_BREAK) {
        addLine(lines, text, Alignment.RIGHT);
      } else {
        text.append(c);
      }
    }
    if (text.length() > 0 || lines.isEmpty()) {
      addLine(lines, text, Alignment.CENTER);
    }
    return lines;
  }

  private static void addLine(List<Line> lines, StringBuilder text, Alignment alignment) {
    lines.add(new Line(text.toString(), alignment));
    text.setLength(0);
  }

  /** Horizontal alignment of one terminated label line. */
  public enum Alignment {
    LEFT,
    CENTER,
    RIGHT
  }

  /** One line and its alignment. */
  public static final class Line {

    private final String text;
    private final Alignment alignment;

    private Line(String text, Alignment alignment) {
      this.text = text;
      this.alignment = alignment;
    }

    public String getText() {
      return text;
    }

    public Alignment getAlignment() {
      return alignment;
    }
  }
}
