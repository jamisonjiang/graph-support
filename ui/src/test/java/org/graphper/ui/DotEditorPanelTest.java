package org.graphper.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.junit.jupiter.api.Test;

class DotEditorPanelTest {

  /**
   * Guards the headless tolerance of the find accelerator: the surefire JVM runs with
   * {@code java.awt.headless=true}, where the toolkit refuses to report a menu shortcut mask, so
   * this exercises the fallback. On a machine with a display it exercises the toolkit path instead.
   * Either way the shortcut must exist and stay behind a modifier.
   */
  @Test
  void bindsTheFindShortcutBehindAMenuModifier() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    InputMap inputMap = panel.editor().getInputMap();

    KeyStroke find = Arrays.stream(inputMap.keys())
        .filter(keyStroke -> "dot-find".equals(inputMap.get(keyStroke)))
        .findFirst()
        .orElseThrow(AssertionError::new);

    assertEquals(KeyEvent.VK_F, find.getKeyCode());
    assertTrue(find.getModifiers() != 0);
    assertNotNull(panel.editor().getActionMap().get("dot-find"));
  }

  @Test
  void createsAnEditorPreviewAndDefaultExample() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());

    assertNotNull(panel.editor());
    assertNotNull(panel.preview());
    assertNotNull(panel.renderButton());
    assertTrue(panel.editor().getText().contains("digraph"));
    assertTrue(panel.editor() instanceof RSyntaxTextArea);
    assertTrue(panel.editor().isCodeFoldingEnabled());
    assertTrue(panel.editorScrollPane().getLineNumbersEnabled());
    assertNotNull(panel.autoCompletion());
    assertTrue(!panel.editorScrollPane().isIconRowHeaderEnabled());
  }

  @Test
  void autoActivatesKeywordAndAttributeCompletionsAfterLetters() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());

    assertCompletion(panel, "sub", "subgraph");
    assertCompletion(panel, "lab", "label");
    assertCompletion(panel, "sha", "shape");
    assertCompletion(panel, "ran", "rankdir");
    assertCompletion(panel, "reg", "regular");
    assertCompletion(panel, "sameh", "samehead");
    assertCompletion(panel, "samet", "sametail");
    assertCompletion(panel, "no", "node");
    assertCompletion(panel, "ed", "edge");
  }

  @Test
  void hasOneUnambiguousTemplateCompletionForSubgraphAndDigraph() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());

    assertCompletionCount(panel, "sub", "subgraph", 1);
    assertCompletionCount(panel, "dig", "digraph", 1);
  }

  @Test
  void attributeCompletionsInsertAssignmentsAndPlaceCaretInsideQuotes() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());

    assertAttributeCompletion(panel, "lab", "label", "label = \"\"");
    assertAttributeCompletion(panel, "sha", "shape", "shape = \"\"");
    assertAttributeCompletion(panel, "ran", "rankdir", "rankdir = \"\"");
    assertAttributeCompletion(panel, "fill", "fillcolor", "fillcolor = \"\"");
    assertAttributeCompletion(panel, "min", "minlen", "minlen = \"\"");
  }

  @Test
  void attributeCompletionPreservesCurrentIndentation() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {\n    lab");
    editor.setCaretPosition(editor.getDocument().getLength());
    Completion completion = completion(panel, "label");

    panel.autoCompletion().insertCompletionForTest(completion);

    assertEquals("digraph G {\n    label = \"\"", editor.getText());
    assertEquals(editor.getDocument().getLength() - 1, editor.getCaretPosition());
  }

  @Test
  void insertsClosingBracketsAndKeepsTheCaretInside() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();

    assertPair(editor, '(', "()", 1);
    assertPair(editor, '[', "[]", 1);
    assertPair(editor, '{', "{}", 1);
  }

  @Test
  void typingAnExistingClosingBracketMovesPastIt() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("()");
    editor.setCaretPosition(1);

    type(editor, ')');

    assertEquals("()", editor.getText());
    assertEquals(2, editor.getCaretPosition());
  }

  @Test
  void alignsMultilineCompletionWithTheCurrentIndentation() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {\n    sub");
    editor.setCaretPosition(editor.getDocument().getLength());
    Completion completion = panel.autoCompletion().getCompletionProvider().getCompletions(editor)
        .stream()
        .filter(candidate -> "subgraph".equals(candidate.getInputText()))
        .filter(candidate -> candidate.getReplacementText().contains("\n"))
        .findFirst()
        .orElseThrow(AssertionError::new);
    int start = editor.getCaretPosition() - completion.getAlreadyEntered(editor).length();

    String replacement = panel.autoCompletion().replacementTextForTest(completion,
        editor.getDocument(), start, editor.getCaretPosition() - start);

    assertEquals("subgraph cluster_name {\n      \n    }", replacement);
  }

  @Test
  void multilineCompletionPlacesTheCaretOnItsIndentedBodyLine() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {\n    sub");
    editor.setCaretPosition(editor.getDocument().getLength());
    Completion completion = panel.autoCompletion().getCompletionProvider().getCompletions(editor)
        .stream()
        .filter(candidate -> "subgraph".equals(candidate.getInputText()))
        .filter(candidate -> candidate.getReplacementText().contains("\n"))
        .findFirst()
        .orElseThrow(AssertionError::new);

    panel.autoCompletion().insertCompletionForTest(completion);

    assertEquals("digraph G {\n    subgraph cluster_name {\n      \n    }", editor.getText());
    assertEquals(editor.getText().indexOf("      \n") + 6, editor.getCaretPosition());
  }

  @Test
  void enterBetweenCurlyBracesIndentsContentAndAlignsTheClosingBrace() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {\n  subgraph cluster_a {}");
    editor.setCaretPosition(editor.getDocument().getLength() - 1);

    press(editor, KeyStroke.getKeyStroke("ENTER"));

    assertEquals("digraph G {\n  subgraph cluster_a {\n    \n  }", editor.getText());
    assertEquals(editor.getText().indexOf("    \n") + 4, editor.getCaretPosition());
  }

  @Test
  void enterAfterSubgraphOpeningBraceIndentsOneLevel() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {\n  subgraph cluster_a {");
    editor.setCaretPosition(editor.getDocument().getLength());

    press(editor, KeyStroke.getKeyStroke("ENTER"));

    assertEquals("digraph G {\n  subgraph cluster_a {\n    ", editor.getText());
    assertEquals(editor.getDocument().getLength(), editor.getCaretPosition());
  }

  @Test
  void enterAfterGraphOpeningBraceIndentsOneLevel() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {");
    editor.setCaretPosition(editor.getDocument().getLength());

    press(editor, KeyStroke.getKeyStroke("ENTER"));

    assertEquals("digraph G {\n  ", editor.getText());
  }

  @Test
  void enterAfterContentKeepsCurrentIndentation() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    editor.setText("digraph G {\n  a -> b");
    editor.setCaretPosition(editor.getDocument().getLength());

    press(editor, KeyStroke.getKeyStroke("ENTER"));

    assertEquals("digraph G {\n  a -> b\n  ", editor.getText());
    assertEquals(editor.getDocument().getLength(), editor.getCaretPosition());
  }

  @Test
  void errorHighlightDoesNotDislodgeCaretBeforeSmartEnter() {
    DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
    RSyntaxTextArea editor = panel.editor();
    String source = "digraph G {\n  a -- b\n}";
    editor.setText(source);
    int afterOpeningBrace = source.indexOf('{') + 1;
    editor.setCaretPosition(afterOpeningBrace);
    DotErrorHighlighter highlighter =
        new DotErrorHighlighter(editor, new DotRenderService(), 400);

    highlighter.show(source, new DotError(2, 7, "Cannot use '--' in a directed graph."));
    assertEquals(afterOpeningBrace, editor.getCaretPosition());

    press(editor, KeyStroke.getKeyStroke("ENTER"));

    assertEquals("digraph G {\n  \n  a -- b\n}", editor.getText());
    assertEquals(source.indexOf('{') + 4, editor.getCaretPosition());
  }

  private static void assertCompletion(DotEditorPanel panel, String text, String expected) {
    RSyntaxTextArea editor = panel.editor();
    editor.setText(text);

    // AutoCompletion checks the inserted character before Swing advances the caret.
    editor.setCaretPosition(text.length() - 1);
    assertTrue(panel.autoCompletion().getCompletionProvider().isAutoActivateOkay(editor));
    editor.setCaretPosition(text.length());
    List<Completion> completions = panel.autoCompletion().getCompletionProvider()
        .getCompletions(editor);
    assertTrue(completions.stream().anyMatch(completion -> expected.equals(completion.getInputText())));
  }

  private static void assertAttributeCompletion(DotEditorPanel panel, String prefix,
                                                String attribute, String expected) {
    RSyntaxTextArea editor = panel.editor();
    editor.setText(prefix);
    editor.setCaretPosition(prefix.length());

    panel.autoCompletion().insertCompletionForTest(completion(panel, attribute));

    assertEquals(expected, editor.getText());
    assertEquals(expected.length() - 1, editor.getCaretPosition());
  }

  private static Completion completion(DotEditorPanel panel, String inputText) {
    return panel.autoCompletion().getCompletionProvider().getCompletions(panel.editor()).stream()
        .filter(candidate -> inputText.equals(candidate.getInputText()))
        .findFirst()
        .orElseThrow(AssertionError::new);
  }

  private static void assertCompletionCount(DotEditorPanel panel, String text, String expected,
                                            long count) {
    RSyntaxTextArea editor = panel.editor();
    editor.setText(text);
    editor.setCaretPosition(text.length());
    long actual = panel.autoCompletion().getCompletionProvider().getCompletions(editor).stream()
        .filter(completion -> expected.equals(completion.getInputText()))
        .count();
    assertEquals(count, actual);
  }

  private static void assertPair(RSyntaxTextArea editor, char opening, String expected, int caret) {
    editor.setText("");
    type(editor, opening);
    assertEquals(expected, editor.getText());
    assertEquals(caret, editor.getCaretPosition());
  }

  private static void type(RSyntaxTextArea editor, char character) {
    press(editor, KeyStroke.getKeyStroke(character));
  }

  private static void press(RSyntaxTextArea editor, KeyStroke keyStroke) {
    Object actionKey = editor.getInputMap().get(keyStroke);
    Action action = editor.getActionMap().get(actionKey);
    assertNotNull(action);
    action.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED,
        keyStroke.toString()));
  }
}
