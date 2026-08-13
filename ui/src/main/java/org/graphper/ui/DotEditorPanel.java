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

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache_gs.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.graphper.util.Asserts;

/**
 * A two-pane desktop editor: a DOT source editor on the left and a live SVG preview on the right.
 * Editing schedules a debounced render, and the rendered graph is shown with pan and zoom support.
 *
 * @author Jamison Jiang
 */
public class DotEditorPanel extends JPanel {

  private static final String DOT_STYLE = "text/dot";
  private static final int INDENT_WIDTH = 2;
  private static final int RENDER_DELAY_MS = 400;
  private static final int COMPLETION_DELAY_MS = 120;

  private static final Color APP_BACKGROUND = new Color(238, 242, 247);
  private static final Color SURFACE = Color.WHITE;
  private static final Color HEADER = new Color(25, 35, 54);
  private static final Color TEXT = new Color(31, 42, 61);
  private static final Color MUTED = new Color(105, 118, 138);
  private static final Color BORDER = new Color(217, 224, 234);
  private static final Color ACCENT = new Color(75, 102, 224);
  private static final Color SUCCESS = new Color(29, 151, 108);
  private static final Color ERROR = new Color(205, 62, 70);

  private static final String EXAMPLE = "digraph G {\n"
      + "  graph [rankdir=LR];\n"
      + "  node [shape=box];\n"
      + "  source -> parse -> layout -> svg;\n"
      + "}\n";

  private final DotRenderService renderService;
  private final RSyntaxTextArea editor = new RSyntaxTextArea(EXAMPLE);
  private final RTextScrollPane editorScrollPane = new RTextScrollPane(editor, true);
  private final DotAutoCompletion autoCompletion =
      new DotAutoCompletion(DotCompletionProvider.create());
  private final JSVGCanvas preview = new JSVGCanvas();
  private final JButton renderButton = new JButton("Render");
  private final JCheckBox autoRender = new JCheckBox("Auto render", true);
  private final JButton zoomButton = new JButton("100%");
  private final JLabel status = new JLabel("Ready");
  private final JLabel statusDot = new JLabel("●");
  private final JLabel fileName = new JLabel("Untitled.dot");
  private final JLabel caretStatus = new JLabel("Ln 1, Col 1");
  private final Timer renderTimer;
  private final Timer completionTimer;
  private final DotErrorHighlighter errorHighlighter;
  private final AtomicInteger renderGeneration = new AtomicInteger();
  private final CanvasInteraction canvasInteraction = new CanvasInteraction(preview);

  private String lastSvg;
  private File currentFile;
  private File previewFile;

  public DotEditorPanel(DotRenderService renderService) {
    super(new BorderLayout());
    Asserts.nullArgument(renderService, "renderService");
    this.renderService = renderService;
    this.renderTimer = singleShotTimer(RENDER_DELAY_MS, event -> render());
    this.completionTimer = singleShotTimer(COMPLETION_DELAY_MS,
                                           event -> autoCompletion.doCompletion());
    this.errorHighlighter = new DotErrorHighlighter(editor, renderService, RENDER_DELAY_MS);

    setBackground(APP_BACKGROUND);
    configureEditor();
    configurePreview();
    add(topArea(), BorderLayout.NORTH);
    add(splitPane(), BorderLayout.CENTER);
    add(statusBar(), BorderLayout.SOUTH);
    installListeners();
  }

  /**
   * Renders the graph currently in the editor, used to fill the preview when the window opens.
   */
  public void renderInitialGraph() {
    render();
  }

  RSyntaxTextArea editor() {
    return editor;
  }

  RTextScrollPane editorScrollPane() {
    return editorScrollPane;
  }

  DotAutoCompletion autoCompletion() {
    return autoCompletion;
  }

  JSVGCanvas preview() {
    return preview;
  }

  JButton renderButton() {
    return renderButton;
  }

  // ------------------------------------ editor setup ------------------------------------

  private void configureEditor() {
    registerDotLanguage();
    styleEditor();
    styleSyntaxScheme();
    styleGutter();
    installEditingAids();
    configureAutoCompletion();
    configureErrorHighlighting();
  }

  private void styleEditor() {
    editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    editor.setBackground(new Color(251, 252, 254));
    editor.setForeground(TEXT);
    editor.setCaretColor(new Color(42, 72, 171));
    editor.setSelectionColor(new Color(205, 218, 255));
    editor.setSelectedTextColor(TEXT);
    editor.setSyntaxEditingStyle(DOT_STYLE);
    editor.setTabSize(INDENT_WIDTH);
    editor.setTabsEmulated(true);
    editor.setAutoIndentEnabled(true);
    editor.setCloseCurlyBraces(true);
    editor.setInsertPairedCharacters(true);
    editor.setBracketMatchingEnabled(true);
    editor.setAnimateBracketMatching(true);
    editor.setPaintMatchedBracketPair(true);
    editor.setCodeFoldingEnabled(true);
    editor.setMarkOccurrences(true);
    editor.setPaintTabLines(true);
    editor.setAntiAliasingEnabled(true);
    editor.setMarginLineEnabled(false);
    editor.setCurrentLineHighlightColor(new Color(242, 246, 252));
  }

  private void styleSyntaxScheme() {
    setTokenColor(TokenTypes.RESERVED_WORD, new Color(54, 89, 190));
    setTokenColor(TokenTypes.RESERVED_WORD_2, new Color(126, 65, 160));
    setTokenColor(TokenTypes.FUNCTION, new Color(166, 85, 24));
    setTokenColor(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, new Color(22, 125, 89));
    setTokenColor(TokenTypes.COMMENT_EOL, new Color(126, 139, 154));
    setTokenColor(TokenTypes.COMMENT_MULTILINE, new Color(126, 139, 154));
  }

  private void setTokenColor(int tokenType, Color color) {
    editor.getSyntaxScheme().getStyle(tokenType).foreground = color;
  }

  private void styleGutter() {
    editorScrollPane.setLineNumbersEnabled(true);
    editorScrollPane.setFoldIndicatorEnabled(true);
    editorScrollPane.setBorder(BorderFactory.createEmptyBorder());
    editorScrollPane.getViewport().setBackground(editor.getBackground());
    editorScrollPane.putClientProperty(FlatClientProperties.SCROLL_PANE_SMOOTH_SCROLLING, true);
    editorScrollPane.getGutter().setBackground(new Color(247, 249, 252));
    editorScrollPane.getGutter().setBorderColor(BORDER);
    editorScrollPane.getGutter().setLineNumberColor(new Color(148, 159, 176));
    editorScrollPane.getGutter().setCurrentLineNumberColor(ACCENT);
    editorScrollPane.getGutter().setFoldIndicatorForeground(new Color(130, 142, 160));
  }

  private void configureAutoCompletion() {
    autoCompletion.setAutoActivationEnabled(false);
    autoCompletion.setAutoCompleteSingleChoices(false);
    autoCompletion.setShowDescWindow(true);
    autoCompletion.setParameterAssistanceEnabled(false);
    autoCompletion.install(editor);
  }

  private void configureErrorHighlighting() {
    errorHighlighter.install();
  }

  private void configurePreview() {
    preview.setDocumentState(JSVGCanvas.ALWAYS_STATIC);
    preview.setRecenterOnResize(true);
    preview.setBackground(Color.WHITE);
    canvasInteraction.install();
    canvasInteraction.setTransformListener(this::refreshZoom);
    preview.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
      @Override
      public void gvtRenderingCompleted(GVTTreeRendererEvent event) {
        SwingUtilities.invokeLater(DotEditorPanel.this::refreshZoom);
      }
    });
  }

  private void installListeners() {
    renderButton.addActionListener(event -> renderNow());
    editor.addCaretListener(event -> updateCaretStatus());
    editor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        scheduleCompletion(event);
        scheduleRender();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        scheduleRender();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        scheduleRender();
      }
    });
  }

  // ------------------------------------ editing aids ------------------------------------

  private void installEditingAids() {
    installPair('(', ')', "parenthesis");
    installPair('[', ']', "square-bracket");
    installPair('{', '}', "curly-brace");
    installSmartEnter();
    installFindShortcut();
  }

  private void installPair(char opening, char closing, String name) {
    bindKey(String.valueOf(opening), "dot-open-" + name, event -> surroundSelection(opening, closing));
    bindKey(String.valueOf(closing), "dot-close-" + name, event -> typeClosing(closing));
  }

  private void surroundSelection(char opening, char closing) {
    int selectionStart = editor.getSelectionStart();
    int selectionEnd = editor.getSelectionEnd();
    String selected = editor.getSelectedText();
    editor.beginAtomicEdit();
    try {
      editor.replaceSelection(opening + (selected == null ? "" : selected) + closing);
      editor.select(selectionStart + 1, selectionEnd + 1);
    } finally {
      editor.endAtomicEdit();
    }
  }

  private void typeClosing(char closing) {
    int caret = editor.getCaretPosition();
    if (hasNoSelection() && caret < editor.getDocument().getLength()
        && editor.getText().charAt(caret) == closing) {
      editor.setCaretPosition(caret + 1);
      return;
    }
    editor.replaceSelection(String.valueOf(closing));
  }

  private void installSmartEnter() {
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Action fallback = editor.getActionMap().get(editor.getInputMap().get(enter));
    bindKey(enter, "dot-smart-enter", event -> smartEnter(event, fallback));
  }

  private void smartEnter(ActionEvent event, Action fallback) {
    if (!hasNoSelection()) {
      runEnterFallback(event, fallback);
      return;
    }
    int caret = editor.getCaretPosition();
    char before = lastNonSpaceBefore(caret);
    char after = firstNonSpaceAfter(caret);
    if (before == '{' && after == '}') {
      openBlockBody(caret);
    } else if (before == '{') {
      openIndentedLine(caret);
    } else {
      openRegularLine(caret);
    }
  }

  private void runEnterFallback(ActionEvent event, Action fallback) {
    if (fallback != null) {
      fallback.actionPerformed(event);
    } else {
      editor.replaceSelection("\n");
    }
  }

  private void openBlockBody(int caret) {
    String indent = currentLineIndent(caret);
    String innerIndent = indent + indentUnit();
    editor.beginAtomicEdit();
    try {
      editor.replaceSelection("\n" + innerIndent + "\n" + indent);
      editor.setCaretPosition(caret + 1 + innerIndent.length());
    } finally {
      editor.endAtomicEdit();
    }
  }

  private void openIndentedLine(int caret) {
    String insertion = "\n" + currentLineIndent(caret) + indentUnit();
    editor.replaceSelection(insertion);
    editor.setCaretPosition(caret + insertion.length());
  }

  private void openRegularLine(int caret) {
    String insertion = "\n" + currentLineIndent(caret);
    editor.replaceSelection(insertion);
    editor.setCaretPosition(caret + insertion.length());
  }

  /**
   * @return the last non-space character before {@code caret} on its line, or {@code '\0'} when the
   *     text up to the caret is blank
   */
  private char lastNonSpaceBefore(int caret) {
    Document document = editor.getDocument();
    Element line = lineOf(caret);
    try {
      String head = document.getText(line.getStartOffset(), caret - line.getStartOffset());
      for (int i = head.length() - 1; i >= 0; i--) {
        if (!isIndentChar(head.charAt(i))) {
          return head.charAt(i);
        }
      }
    } catch (BadLocationException ignored) {
      // fall through
    }
    return '\0';
  }

  /**
   * @return the first non-space character after {@code caret} on its line, or {@code '\0'} when the
   *     rest of the line is blank
   */
  private char firstNonSpaceAfter(int caret) {
    Document document = editor.getDocument();
    Element line = lineOf(caret);
    int lineEnd = Math.min(document.getLength(), line.getEndOffset() - 1);
    if (caret >= lineEnd) {
      return '\0';
    }
    try {
      String tail = document.getText(caret, lineEnd - caret);
      for (int i = 0; i < tail.length(); i++) {
        if (!isIndentChar(tail.charAt(i))) {
          return tail.charAt(i);
        }
      }
    } catch (BadLocationException ignored) {
      // fall through
    }
    return '\0';
  }

  private Element lineOf(int offset) {
    Element root = editor.getDocument().getDefaultRootElement();
    return root.getElement(root.getElementIndex(offset));
  }

  private static boolean isIndentChar(char c) {
    return c == ' ' || c == '\t';
  }

  private String currentLineIndent(int caret) {
    try {
      return EditorTexts.lineIndent(editor.getDocument(), caret);
    } catch (BadLocationException e) {
      return "";
    }
  }

  @SuppressWarnings("deprecation")
  private void installFindShortcut() {
    int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    bindKey(KeyStroke.getKeyStroke('F', shortcut), "dot-find", event -> showFindDialog());
  }

  private void showFindDialog() {
    String value = JOptionPane.showInputDialog(this, "Find", "Find in DOT",
                                               JOptionPane.PLAIN_MESSAGE);
    if (StringUtils.isEmpty(value)) {
      return;
    }
    SearchContext context = new SearchContext(value);
    context.setSearchForward(true);
    context.setSearchWrap(true);
    SearchEngine.find(editor, context);
  }

  private void bindKey(String character, String actionKey, ActionHandler handler) {
    bindKey(KeyStroke.getKeyStroke(character.charAt(0)), actionKey, handler);
  }

  private void bindKey(KeyStroke keyStroke, String actionKey, ActionHandler handler) {
    editor.getInputMap().put(keyStroke, actionKey);
    editor.getActionMap().put(actionKey, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent event) {
        handler.handle(event);
      }
    });
  }

  private boolean hasNoSelection() {
    return editor.getSelectionStart() == editor.getSelectionEnd();
  }

  private static String indentUnit() {
    StringBuilder unit = new StringBuilder(INDENT_WIDTH);
    for (int i = 0; i < INDENT_WIDTH; i++) {
      unit.append(' ');
    }
    return unit.toString();
  }

  private static void registerDotLanguage() {
    TokenMakerFactory factory = TokenMakerFactory.getDefaultInstance();
    if (factory instanceof AbstractTokenMakerFactory) {
      ((AbstractTokenMakerFactory) factory).putMapping(DOT_STYLE, DotTokenMaker.class.getName());
    }
    FoldParserManager.get().addFoldParserMapping(DOT_STYLE, new CurlyFoldParser());
  }

  // ------------------------------------ top area ------------------------------------

  private JPanel topArea() {
    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
    top.add(brandBar());
    top.add(actionBar());
    return top;
  }

  private JPanel brandBar() {
    JPanel bar = new JPanel(new BorderLayout());
    bar.setBackground(HEADER);
    bar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(48, 64, 90)),
        BorderFactory.createEmptyBorder(0, 18, 0, 18)));
    bar.setPreferredSize(new Dimension(10, 46));
    bar.add(brandIdentity(), BorderLayout.WEST);
    bar.add(centeredRow(rendererStatus()), BorderLayout.EAST);
    return bar;
  }

  private JPanel brandIdentity() {
    JLabel brand = label("graph-support", Color.WHITE, Font.BOLD, 15f);
    brand.setIcon(new UiIcon(UiIcon.Kind.BRAND, 18));
    brand.setIconTextGap(9);

    JLabel subtitle = label("DOT STUDIO", new Color(183, 196, 218), Font.BOLD, 9f);
    subtitle.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 9));
    identity.setOpaque(false);
    identity.add(brand);
    identity.add(Box.createHorizontalStrut(10));
    identity.add(pill(new Color(42, 57, 82), subtitle));
    return identity;
  }

  private JPanel rendererStatus() {
    JLabel dot = label("●", new Color(83, 222, 169), Font.PLAIN, 10f);
    JLabel text = label("LOCAL RENDERER", new Color(174, 231, 209), Font.BOLD, 9f);
    return pill(new Color(31, 66, 67), dot, Box.createHorizontalStrut(2), text);
  }

  private JPanel actionBar() {
    JPanel toolbar = new JPanel();
    toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
    toolbar.setBackground(SURFACE);
    toolbar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
        BorderFactory.createEmptyBorder(9, 16, 9, 16)));
    toolbar.setPreferredSize(new Dimension(10, 58));

    addFileActions(toolbar);
    toolbar.add(sectionDivider());
    addRenderActions(toolbar);
    toolbar.add(Box.createHorizontalGlue());
    addExportActions(toolbar);
    toolbar.add(sectionDivider());
    addZoomActions(toolbar);
    return toolbar;
  }

  private void addFileActions(JPanel toolbar) {
    JButton open = actionButton("Open", UiIcon.Kind.OPEN, event -> openDot());
    JButton save = actionButton("Save", UiIcon.Kind.SAVE, event -> saveDot());
    toolbar.add(open);
    toolbar.add(Box.createHorizontalStrut(6));
    toolbar.add(save);
  }

  private void addRenderActions(JPanel toolbar) {
    configurePrimaryButton(renderButton, UiIcon.Kind.PLAY);
    autoRender.setOpaque(false);
    autoRender.setForeground(TEXT);
    autoRender.setFont(autoRender.getFont().deriveFont(Font.PLAIN, 12f));
    toolbar.add(renderButton);
    toolbar.add(Box.createHorizontalStrut(10));
    toolbar.add(autoRender);
  }

  private void addExportActions(JPanel toolbar) {
    JButton exportSvg = actionButton("SVG", UiIcon.Kind.EXPORT,
                                     event -> export("svg", currentSvgBytes()));
    JButton exportPng = actionButton("PNG", UiIcon.Kind.EXPORT,
                                     event -> export("png", currentPngBytes()));
    toolbar.add(label("Export", MUTED, Font.PLAIN, 12f));
    toolbar.add(Box.createHorizontalStrut(7));
    toolbar.add(exportSvg);
    toolbar.add(Box.createHorizontalStrut(6));
    toolbar.add(exportPng);
  }

  private void addZoomActions(JPanel toolbar) {
    JButton zoomOut = iconButton("Zoom out", UiIcon.Kind.ZOOM_OUT, event -> zoom(.8));
    JButton zoomIn = iconButton("Zoom in", UiIcon.Kind.ZOOM_IN, event -> zoom(1.25));
    JButton fit = actionButton("Fit", UiIcon.Kind.FIT, event -> fitToWindow());
    configureZoomReadout();
    toolbar.add(zoomOut);
    toolbar.add(Box.createHorizontalStrut(4));
    toolbar.add(zoomIn);
    toolbar.add(Box.createHorizontalStrut(6));
    toolbar.add(fit);
    toolbar.add(Box.createHorizontalStrut(6));
    toolbar.add(zoomButton);
  }

  private void configureZoomReadout() {
    zoomButton.setForeground(TEXT);
    zoomButton.setFont(zoomButton.getFont().deriveFont(Font.PLAIN, 12f));
    zoomButton.setFocusable(false);
    zoomButton.setToolTipText("Reset to 100%");
    zoomButton.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                                 FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    zoomButton.putClientProperty(FlatClientProperties.STYLE,
                                 "arc: 10; margin: 7,10,7,10; hoverBackground: #EEF2FF;");
    zoomButton.addActionListener(event -> resetZoom());
  }

  private void zoom(double factor) {
    canvasInteraction.zoomAt(factor, new Point(preview.getWidth() / 2, preview.getHeight() / 2));
    refreshZoom();
  }

  private void fitToWindow() {
    preview.resetRenderingTransform();
    SwingUtilities.invokeLater(this::refreshZoom);
  }

  private void resetZoom() {
    preview.setRenderingTransform(new AffineTransform());
    refreshZoom();
  }

  private void refreshZoom() {
    AffineTransform transform = preview.getRenderingTransform();
    double scale = transform == null ? 1D : transform.getScaleX();
    zoomButton.setText(Math.round(scale * 100) + "%");
  }

  private JButton actionButton(String text, UiIcon.Kind icon, ActionHandler handler) {
    JButton button = new JButton(text);
    if (icon != null) {
      button.setIcon(new UiIcon(icon));
      button.setIconTextGap(7);
    }
    button.setForeground(TEXT);
    button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
    button.setFocusable(false);
    button.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                             FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    button.putClientProperty(FlatClientProperties.STYLE,
                             "arc: 10; margin: 7,10,7,10; hoverBackground: #EEF2FF;");
    button.addActionListener(handler::handle);
    return button;
  }

  private JButton iconButton(String tooltip, UiIcon.Kind icon, ActionHandler handler) {
    JButton button = actionButton("", icon, handler);
    button.setToolTipText(tooltip);
    button.putClientProperty(FlatClientProperties.SQUARE_SIZE, true);
    button.putClientProperty(FlatClientProperties.STYLE,
                             "arc: 10; margin: 7,7,7,7; hoverBackground: #EEF2FF;");
    return button;
  }

  private void configurePrimaryButton(JButton button, UiIcon.Kind icon) {
    button.setIcon(new UiIcon(icon));
    button.setIconTextGap(7);
    button.setForeground(Color.WHITE);
    button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
    button.setFocusable(false);
    button.putClientProperty(FlatClientProperties.STYLE,
        "arc: 10; margin: 8,13,8,13; background: #4B66E0; hoverBackground: #3F59CE;"
            + " pressedBackground: #354DB8; disabledBackground: #AEB9EB; disabledText: #FFFFFF;");
  }

  private Component sectionDivider() {
    JPanel divider = new JPanel();
    divider.setBackground(BORDER);
    divider.setMaximumSize(new Dimension(1, 24));
    divider.setPreferredSize(new Dimension(1, 24));
    divider.setAlignmentY(Component.CENTER_ALIGNMENT);

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 7));
    wrapper.setOpaque(false);
    wrapper.setMaximumSize(new Dimension(29, 38));
    wrapper.add(divider);
    return wrapper;
  }

  // ------------------------------------ work area ------------------------------------

  private JSplitPane splitPane() {
    JPanel sourcePanel = workspacePanel("DOT SOURCE", "Edit with syntax help and live validation",
                                        fileName, editorScrollPane);
    JPanel previewPanel = workspacePanel("SVG PREVIEW", "Drag to pan · Scroll to zoom",
                                         new JLabel("LIVE"), previewStage());

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourcePanel, previewPanel);
    split.setBorder(BorderFactory.createEmptyBorder());
    split.setBackground(APP_BACKGROUND);
    split.setResizeWeight(.46);
    split.setContinuousLayout(true);
    split.setDividerLocation(610);
    split.putClientProperty(FlatClientProperties.SPLIT_PANE_EXPANDABLE_SIDE,
                            FlatClientProperties.SPLIT_PANE_EXPANDABLE_SIDE_LEFT);
    return split;
  }

  private JPanel workspacePanel(String title, String subtitle, JLabel trailing, Component content) {
    JPanel labels = new JPanel();
    labels.setOpaque(false);
    labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
    labels.add(label(title, TEXT, Font.BOLD, 11f));
    labels.add(Box.createVerticalStrut(2));
    labels.add(label(subtitle, MUTED, Font.PLAIN, 11f));

    trailing.setForeground(MUTED);
    trailing.setFont(trailing.getFont().deriveFont(Font.BOLD, 10f));

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(SURFACE);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
        BorderFactory.createEmptyBorder(10, 16, 9, 16)));
    header.add(labels, BorderLayout.WEST);
    header.add(trailing, BorderLayout.EAST);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE);
    panel.add(header, BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  private JPanel previewStage() {
    JPanel paper = new JPanel(new BorderLayout());
    paper.setBackground(Color.WHITE);
    paper.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(210, 217, 228)),
        BorderFactory.createEmptyBorder(6, 6, 6, 6)));
    paper.add(preview, BorderLayout.CENTER);

    JPanel stage = new JPanel(new BorderLayout());
    stage.setBackground(APP_BACKGROUND);
    stage.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    stage.add(paper, BorderLayout.CENTER);
    return stage;
  }

  private JPanel statusBar() {
    statusDot.setForeground(SUCCESS);
    statusDot.setFont(statusDot.getFont().deriveFont(Font.PLAIN, 10f));
    status.setForeground(MUTED);
    status.setFont(status.getFont().deriveFont(Font.PLAIN, 11f));

    JPanel state = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    state.setOpaque(false);
    state.add(statusDot);
    state.add(Box.createHorizontalStrut(7));
    state.add(status);

    JPanel metadata = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
    metadata.setOpaque(false);
    metadata.add(caretStatus);
    metadata.add(label("UTF-8", MUTED, Font.PLAIN, 11f));
    caretStatus.setForeground(MUTED);
    caretStatus.setFont(caretStatus.getFont().deriveFont(Font.PLAIN, 11f));

    JPanel bar = new JPanel(new BorderLayout());
    bar.setBackground(SURFACE);
    bar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
        BorderFactory.createEmptyBorder(7, 16, 7, 16)));
    bar.add(state, BorderLayout.WEST);
    bar.add(metadata, BorderLayout.EAST);
    return bar;
  }

  // ------------------------------------ rendering ------------------------------------

  private void renderNow() {
    renderTimer.stop();
    render();
  }

  private void scheduleRender() {
    if (autoRender.isSelected()) {
      renderTimer.restart();
    }
  }

  private void scheduleCompletion(DocumentEvent event) {
    if (event.getLength() != 1) {
      return;
    }
    SwingUtilities.invokeLater(() -> {
      int caret = editor.getCaretPosition();
      if (caret <= 0) {
        return;
      }
      try {
        if (startsIdentifier(editor.getDocument().getText(caret - 1, 1).charAt(0))) {
          completionTimer.restart();
        }
      } catch (BadLocationException ignored) {
        completionTimer.stop();
      }
    });
  }

  private static boolean startsIdentifier(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  private void render() {
    int request = renderGeneration.incrementAndGet();
    String dot = editor.getText();
    long start = System.nanoTime();
    renderButton.setEnabled(false);
    setStatus("Rendering...", false);

    new SwingWorker<String, Void>() {
      @Override
      protected String doInBackground() {
        return renderService.renderSvg(dot);
      }

      @Override
      protected void done() {
        if (request == renderGeneration.get()) {
          showRenderResult(this, start);
        }
      }
    }.execute();
  }

  private void showRenderResult(SwingWorker<String, Void> worker, long startNanos) {
    renderButton.setEnabled(true);
    try {
      lastSvg = worker.get();
      Files.write(previewFile().toPath(), renderService.svgBytes(lastSvg));
      preview.setURI(previewFile().toURI().toString());
      double millis = (System.nanoTime() - startNanos) / 1_000_000D;
      setStatus(String.format("Rendered in %.0f ms", millis), false);
    } catch (Exception e) {
      Throwable cause = rootCause(e);
      String message = cause.getMessage() == null ? cause.toString() : cause.getMessage();
      setStatus("Render failed: " + message, true);
    }
  }

  private Throwable rootCause(Throwable throwable) {
    Throwable cause = throwable;
    while (cause.getCause() != null && cause.getCause() != cause) {
      cause = cause.getCause();
    }
    return cause;
  }

  private File previewFile() throws IOException {
    if (previewFile == null) {
      previewFile = File.createTempFile("graph-support-preview-", ".svg");
      previewFile.deleteOnExit();
    }
    return previewFile;
  }

  // ------------------------------------ files ------------------------------------

  private void openDot() {
    JFileChooser chooser = chooser("Open DOT", "dot", "gv");
    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    try {
      currentFile = chooser.getSelectedFile();
      editor.setText(new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8));
      fileName.setText(currentFile.getName());
      setStatus("Opened " + currentFile.getName(), false);
    } catch (IOException e) {
      showError("Unable to open file", e);
    }
  }

  private void saveDot() {
    File file = currentFile;
    if (file == null) {
      JFileChooser chooser = chooser("Save DOT", "dot", "gv");
      if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      file = withExtension(chooser.getSelectedFile(), "dot");
    }
    try {
      writeFile(file, editor.getText().getBytes(StandardCharsets.UTF_8));
      currentFile = file;
      fileName.setText(file.getName());
      setStatus("Saved " + file.getName(), false);
    } catch (IOException e) {
      showError("Unable to save file", e);
    }
  }

  private void export(String extension, byte[] bytes) {
    if (bytes == null) {
      JOptionPane.showMessageDialog(this, "Render the graph before exporting.",
                                    "Nothing to export", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    JFileChooser chooser = chooser("Export " + extension.toUpperCase(), extension);
    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File file = withExtension(chooser.getSelectedFile(), extension);
    try {
      writeFile(file, bytes);
      setStatus("Exported " + file.getName(), false);
    } catch (IOException e) {
      showError("Unable to export file", e);
    }
  }

  private byte[] currentSvgBytes() {
    return lastSvg == null ? null : renderService.svgBytes(lastSvg);
  }

  private byte[] currentPngBytes() {
    return lastSvg == null ? null : renderService.pngBytes(lastSvg);
  }

  private static void writeFile(File file, byte[] bytes) throws IOException {
    Files.write(file.toPath(), bytes, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
  }

  private JFileChooser chooser(String title, String... extensions) {
    JFileChooser chooser = new JFileChooser(
        currentFile == null ? null : currentFile.getParentFile());
    chooser.setDialogTitle(title);
    chooser.setFileFilter(new FileNameExtensionFilter(title, extensions));
    return chooser;
  }

  private static File withExtension(File file, String extension) {
    if (file.getName().toLowerCase().endsWith("." + extension)) {
      return file;
    }
    return new File(file.getParentFile(), file.getName() + "." + extension);
  }

  // ------------------------------------ status ------------------------------------

  private void updateCaretStatus() {
    int caret = editor.getCaretPosition();
    Element root = editor.getDocument().getDefaultRootElement();
    int line = root.getElementIndex(caret);
    int column = caret - root.getElement(line).getStartOffset();
    caretStatus.setText("Ln " + (line + 1) + ", Col " + (column + 1));
  }

  private void setStatus(String message, boolean error) {
    statusDot.setForeground(error ? ERROR : SUCCESS);
    status.setForeground(error ? ERROR : MUTED);
    status.setText(message);
    status.setToolTipText(error ? message : null);
  }

  private void showError(String title, Exception error) {
    setStatus(error.getMessage(), true);
    JOptionPane.showMessageDialog(this, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
  }

  // ------------------------------------ small factories ------------------------------------

  private static JLabel label(String text, Color color, int style, float size) {
    JLabel label = new JLabel(text);
    label.setForeground(color);
    label.setFont(label.getFont().deriveFont(style, size));
    return label;
  }

  private static JPanel pill(Color background, Component... children) {
    JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
    pill.setOpaque(true);
    pill.setBackground(background);
    pill.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 7));
    pill.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");
    for (Component child : children) {
      pill.add(child);
    }
    return pill;
  }

  private static JPanel centeredRow(Component child) {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
    row.setOpaque(false);
    row.add(child);
    return row;
  }

  private static Timer singleShotTimer(int delayMs, ActionListener listener) {
    Timer timer = new Timer(delayMs, listener);
    timer.setRepeats(false);
    return timer;
  }

  /** Small functional bridge so key bindings and buttons can share concise lambdas. */
  private interface ActionHandler {
    void handle(ActionEvent event);
  }
}
