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

/**
 * The exact source range to underline for a {@link DotError}. Parser messages often quote the
 * offending token; locating that token keeps semantic errors such as an invalid {@code --}
 * operator from being marked on the following node.
 *
 * @author Jamison Jiang
 */
final class DotErrorSpan {

  private final int offset;
  private final int length;

  private DotErrorSpan(int offset, int length) {
    this.offset = offset;
    this.length = length;
  }

  static DotErrorSpan locate(String source, DotError error) {
    int lineStart = lineStart(source, error.line());
    int lineEnd = lineEnd(source, lineStart);
    String line = source.substring(lineStart, lineEnd);
    int column = clamp(error.column(), 0, line.length());
    int[] local = localSpan(error.message(), line, column);
    int offset = clamp(lineStart + local[0], lineStart, lineEnd);
    int length = clamp(local[1], 1, Math.max(1, lineEnd - offset));
    return new DotErrorSpan(offset, length);
  }

  int offset() {
    return offset;
  }

  int length() {
    return length;
  }

  private static int[] localSpan(String message, String line, int column) {
    String token = quotedToken(message);
    if (token != null && !line.isEmpty()) {
      int at = line.lastIndexOf(token, Math.min(column, line.length() - 1));
      if (at < 0) {
        at = line.indexOf(token);
      }
      if (at >= 0) {
        return new int[]{at, token.length()};
      }
    }
    return tokenAt(line, column);
  }

  private static String quotedToken(String message) {
    if (message == null) {
      return null;
    }
    int open = message.indexOf('\'');
    int close = open < 0 ? -1 : message.indexOf('\'', open + 1);
    if (close <= open + 1) {
      return null;
    }
    String token = message.substring(open + 1, close);
    return "<EOF>".equals(token) ? null : token;
  }

  private static int[] tokenAt(String line, int column) {
    int start = Math.min(column, line.length());
    if (start >= line.length() && start > 0) {
      start--;
    }
    int end = start;
    while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
      end++;
    }
    return new int[]{start, Math.max(1, end - start)};
  }

  private static int lineStart(String source, int line) {
    int offset = 0;
    for (int current = 1; current < line && offset < source.length(); current++) {
      int next = source.indexOf('\n', offset);
      if (next < 0) {
        return source.length();
      }
      offset = next + 1;
    }
    return offset;
  }

  private static int lineEnd(String source, int start) {
    int end = source.indexOf('\n', start);
    if (end < 0) {
      end = source.length();
    }
    if (end > start && source.charAt(end - 1) == '\r') {
      end--;
    }
    return end;
  }

  private static int clamp(int value, int min, int max) {
    if (max < min) {
      return min;
    }
    return Math.max(min, Math.min(max, value));
  }
}
