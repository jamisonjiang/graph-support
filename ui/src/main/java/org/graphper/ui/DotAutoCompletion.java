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
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * Auto-completion tuned for the DOT editor. Multiline templates keep the indentation of the line
 * they are inserted on, and the caret is left where the user will most likely type next: inside the
 * quotes of an attribute assignment, or on the indented body line of a block template.
 *
 * @author Jamison Jiang
 */
final class DotAutoCompletion extends AutoCompletion {

  DotAutoCompletion(CompletionProvider provider) {
    super(provider);
  }

  /**
   * Re-indents every line of a multiline template with the indentation of the insertion line, so a
   * completion accepted inside a nested block stays aligned. Single-line completions are returned
   * unchanged.
   */
  @Override
  protected String getReplacementText(Completion completion, Document document, int start,
                                      int length) {
    String replacement = super.getReplacementText(completion, document, start, length);
    if (replacement.indexOf('\n') < 0) {
      return replacement;
    }

    try {
      String indent = EditorTexts.lineIndent(document, start);
      return indent.isEmpty() ? replacement : replacement.replace("\n", "\n" + indent);
    } catch (BadLocationException e) {
      return replacement;
    }
  }

  /**
   * Inserts the completion and then moves the caret to the most useful spot for the template kind.
   */
  @Override
  protected void insertCompletion(Completion completion, boolean typedParamListStartChar) {
    JTextComponent editor = getTextComponent();
    int start = editor.getCaretPosition() - completion.getAlreadyEntered(editor).length();
    String replacement = getReplacementText(completion, editor.getDocument(), start,
                                            completion.getAlreadyEntered(editor).length());

    super.insertCompletion(completion, typedParamListStartChar);
    positionCaret(editor, completion, start, replacement);
  }

  private void positionCaret(JTextComponent editor, Completion completion, int start,
                             String replacement) {
    // Attribute assignments end with `= ""`; drop the caret between the quotes.
    if (completion instanceof DotAttributeCompletion) {
      editor.setCaretPosition(start + replacement.length() - 1);
      return;
    }

    // Block templates have an indented empty body line between two line breaks; land there.
    int firstBreak = replacement.indexOf('\n');
    int secondBreak = replacement.indexOf('\n', firstBreak + 1);
    if (firstBreak >= 0 && secondBreak > firstBreak) {
      editor.setCaretPosition(start + secondBreak);
    }
  }

  String replacementTextForTest(Completion completion, Document document, int start, int length) {
    return getReplacementText(completion, document, start, length);
  }

  void insertCompletionForTest(Completion completion) {
    insertCompletion(completion, false);
  }
}
