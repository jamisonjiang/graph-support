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

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;

/**
 * Validates DOT source in the background and paints a squiggle directly on the offending source
 * range. This deliberately does not register an RSyntaxTextArea parser or touch the caret,
 * selection, or document, keeping mouse and keyboard editing independent from validation.
 *
 * @author Jamison Jiang
 */
final class DotErrorHighlighter {

  private static final Color ERROR_COLOR = new Color(220, 53, 69);

  private final RSyntaxTextArea editor;
  private final DotRenderService renderService;
  private final Timer timer;
  private final AtomicInteger generation = new AtomicInteger();
  private final Highlighter.HighlightPainter painter =
      new SquiggleUnderlineHighlightPainter(ERROR_COLOR);

  private Object highlightTag;

  DotErrorHighlighter(RSyntaxTextArea editor, DotRenderService renderService, int delayMs) {
    this.editor = editor;
    this.renderService = renderService;
    this.timer = new Timer(delayMs, event -> validate());
    timer.setRepeats(false);
  }

  void install() {
    editor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        schedule();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        schedule();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        schedule();
      }
    });
  }

  private void schedule() {
    generation.incrementAndGet();
    timer.restart();
  }

  private void validate() {
    int request = generation.get();
    String source = editor.getText();
    new SwingWorker<DotError, Void>() {
      @Override
      protected DotError doInBackground() {
        return renderService.findError(source);
      }

      @Override
      protected void done() {
        if (request != generation.get() || !source.equals(editor.getText())) {
          return;
        }
        try {
          show(source, get());
        } catch (Exception ignored) {
          clear();
        }
      }
    }.execute();
  }

  void show(String source, DotError error) {
    clear();
    if (error == null || source.isEmpty()) {
      return;
    }
    DotErrorSpan span = DotErrorSpan.locate(source, error);
    int start = Math.min(span.offset(), editor.getDocument().getLength());
    int end = Math.min(start + span.length(), editor.getDocument().getLength());
    if (end <= start) {
      return;
    }
    try {
      highlightTag = editor.getHighlighter().addHighlight(start, end, painter);
    } catch (BadLocationException ignored) {
      clear();
    }
  }

  private void clear() {
    if (highlightTag != null) {
      editor.getHighlighter().removeHighlight(highlightTag);
      highlightTag = null;
    }
  }
}
