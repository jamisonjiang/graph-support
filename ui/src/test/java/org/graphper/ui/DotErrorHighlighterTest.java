package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.junit.jupiter.api.Test;

class DotErrorHighlighterTest {

  @Test
  void highlightingDoesNotMoveCaretOrSelection() {
    String source = "digraph G { a -- b }";
    RSyntaxTextArea editor = new RSyntaxTextArea(source);
    editor.select(2, 7);
    int dot = editor.getCaret().getDot();
    int mark = editor.getCaret().getMark();
    DotErrorHighlighter highlighter =
        new DotErrorHighlighter(editor, new DotRenderService(), 400);

    highlighter.show(source, new DotError(1, 19, "Cannot use '--' in a directed graph."));

    assertEquals(dot, editor.getCaret().getDot());
    assertEquals(mark, editor.getCaret().getMark());
    assertEquals(1, editor.getHighlighter().getHighlights().length);
  }
}
