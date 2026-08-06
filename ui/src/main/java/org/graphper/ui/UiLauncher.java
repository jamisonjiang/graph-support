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

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Launches the graph-support desktop editor: installs the look and feel, then shows the
 * {@link DotEditorPanel} in a frame and renders the initial graph.
 *
 * @author Jamison Jiang
 */
public final class UiLauncher {

  public static void launch() {
    System.setProperty("apple.awt.application.name", "graph-support DOT Studio");
    SwingUtilities.invokeLater(() -> {
      installLookAndFeel();
      DotEditorPanel panel = new DotEditorPanel(new DotRenderService());
      JFrame frame = new JFrame("graph-support DOT Studio");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setContentPane(panel);
      frame.setMinimumSize(new Dimension(980, 640));
      frame.setSize(1380, 860);
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
      panel.renderInitialGraph();
    });
  }

  private static void installLookAndFeel() {
    try {
      FlatMacLightLaf.setup();
      UIManager.put("Component.arc", 10);
      UIManager.put("Button.arc", 10);
      UIManager.put("TextComponent.arc", 8);
      UIManager.put("ScrollBar.width", 11);
      UIManager.put("ScrollBar.showButtons", false);
      UIManager.put("SplitPane.dividerSize", 5);
    } catch (Exception ignored) {
      // Swing's cross-platform look and feel remains available.
    }
  }

  private UiLauncher() {
  }
}
