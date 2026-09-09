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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache_gs.commons.lang3.StringUtils;

/**
 * A single DOT syntax error located in the source. The parser reports errors as messages that start
 * with {@code "line <line>:<column> <message>"}; this value object pulls that location out so the
 * editor can underline the offending spot.
 *
 * @author Jamison Jiang
 */
final class DotError {

  /** Matches the {@code line L:C message} prefix produced by the DOT parser. */
  private static final Pattern LOCATION = Pattern.compile("line (\\d+):(\\d+)\\s*(.*)");

  private final int line;
  private final int column;
  private final String message;

  DotError(int line, int column, String message) {
    this.line = line;
    this.column = column;
    this.message = message;
  }

  /**
   * Parses a parser error message into a located error. When the message carries no location the
   * error is reported at the start of the document.
   *
   * @param raw the raw parser error message, may be {@code null}
   * @return the located error, never {@code null}
   */
  static DotError fromMessage(String raw) {
    String firstLine = raw == null ? "" : raw.split("\\R", 2)[0];
    Matcher matcher = LOCATION.matcher(firstLine);
    if (matcher.matches()) {
      String message = matcher.group(3).trim();
      return new DotError(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                          StringUtils.isEmpty(message) ? "Syntax error" : message);
    }
    return new DotError(1, 0, StringUtils.isEmpty(firstLine) ? "Syntax error" : firstLine);
  }

  /**
   * Returns the one-based line the error was reported on.
   */
  int line() {
    return line;
  }

  /**
   * Returns the zero-based column the error was reported on.
   */
  int column() {
    return column;
  }

  /**
   * Returns the human-readable error message.
   */
  String message() {
    return message;
  }
}
