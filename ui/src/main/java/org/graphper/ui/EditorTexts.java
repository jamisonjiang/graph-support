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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

/**
 * Small helpers shared by the editor and its auto-completion for reading text out of a
 * {@link Document}.
 *
 * @author Jamison Jiang
 */
final class EditorTexts {

  private EditorTexts() {
  }

  /**
   * Returns the leading indentation (spaces and tabs) of the line that contains {@code offset}, up
   * to that offset. Used to keep inserted lines aligned with the line the caret sits on.
   *
   * @param document the document to read from
   * @param offset   the offset whose line indentation is wanted
   * @return the leading spaces and tabs of the line, never {@code null}
   * @throws BadLocationException if {@code offset} is outside the document
   */
  static String lineIndent(Document document, int offset) throws BadLocationException {
    Element root = document.getDefaultRootElement();
    Element line = root.getElement(root.getElementIndex(offset));
    String head = document.getText(line.getStartOffset(), offset - line.getStartOffset());

    int end = 0;
    while (end < head.length() && isIndent(head.charAt(end))) {
      end++;
    }
    return head.substring(0, end);
  }

  private static boolean isIndent(char c) {
    return c == ' ' || c == '\t';
  }
}
