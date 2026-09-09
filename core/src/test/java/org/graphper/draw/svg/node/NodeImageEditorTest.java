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

package org.graphper.draw.svg.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.api.SecurityPolicy;
import org.graphper.api.attributes.Layout;
import org.graphper.draw.NodeDrawProp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A reference the policy refuses is not rendered, but the layout has already reserved its box, so
 * the node becomes an empty area. These cases pin that the drop is at least reported.
 */
class NodeImageEditorTest {

  /** A 1x1 transparent PNG, small enough to pass the embedded-data budget. */
  private static final String DATA_URI = "data:image/png;base64,"
      + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

  private final CapturingAppender appender = new CapturingAppender();

  private org.apache.log4j.Logger editorLogger;
  private Level originalLevel;

  @BeforeEach
  void captureWarnings() {
    editorLogger = org.apache.log4j.Logger.getLogger(NodeImageEditor.class);
    originalLevel = editorLogger.getLevel();
    editorLogger.setLevel(Level.WARN);
    editorLogger.addAppender(appender);
  }

  @AfterEach
  void releaseWarnings() {
    editorLogger.removeAppender(appender);
    editorLogger.setLevel(originalLevel);
  }

  @Test
  void remoteImageIsDroppedAndTheWarningNamesItAndTheWayToEnableIt() throws Exception {
    String reference = "https://images.example.com/logo.png";
    String svg = render(Graphviz.digraph()
                            .addNode(Node.builder().id("logo").image(reference)
                                         .imageSize(60, 90).build())
                            .build());

    assertFalse(svg.contains("<image"), "the rejected reference must not be emitted");
    assertFalse(svg.contains(reference));

    String warning = onlyWarning();
    assertTrue(warning.contains("logo"), warning);
    assertTrue(warning.contains(reference), warning);
    assertTrue(warning.contains("space is still reserved"), warning);
    assertTrue(warning.contains("--allow-image-host"), warning);
  }

  @Test
  void aHostOutsideTheAllowListIsReportedAgainstTheAllowList() throws Exception {
    String reference = "https://other.example.com/logo.png";
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("images.example.com").build();
    String svg = render(Graphviz.digraph().securityPolicy(policy)
                            .addNode(Node.builder().id("logo").image(reference).build())
                            .build());

    assertFalse(svg.contains("<image"));
    String warning = onlyWarning();
    assertTrue(warning.contains(reference), warning);
    assertTrue(warning.contains("images.example.com"), warning);
  }

  @Test
  void localImageIsDroppedAndTheWarningPointsAtTheBaseDirectory(@TempDir Path directory)
      throws Exception {
    Path file = Files.createFile(directory.resolve("logo.png"));
    String svg = render(Graphviz.digraph()
                            .addNode(Node.builder().id("logo").image(file.toString()).build())
                            .build());

    assertFalse(svg.contains("<image"));
    String warning = onlyWarning();
    assertTrue(warning.contains(file.toString()), warning);
    assertTrue(warning.contains("--image-dir"), warning);
  }

  @Test
  void localImageInsideTheConfiguredBaseDirectoryIsRendered(@TempDir Path directory)
      throws Exception {
    Files.write(directory.resolve("logo.png"), new byte[]{1, 2, 3});
    SecurityPolicy policy = SecurityPolicy.builder()
        .localImageBaseDirectory(directory).build();
    String svg = render(Graphviz.digraph().securityPolicy(policy)
                            .addNode(Node.builder().id("logo").image("logo.png").build())
                            .build());

    assertTrue(svg.contains("<image"), svg);
    assertTrue(svg.contains("logo.png"), svg);
    assertEquals(0, appender.events.size(), appender.messages().toString());
  }

  @Test
  void anAllowedRemoteHostIsRenderedWithoutAWarning() throws Exception {
    String reference = "https://images.example.com/logo.png";
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("images.example.com").build();
    String svg = render(Graphviz.digraph().securityPolicy(policy)
                            .addNode(Node.builder().id("logo").image(reference).build())
                            .build());

    assertTrue(svg.contains(reference), svg);
    assertEquals(0, appender.events.size(), appender.messages().toString());
  }

  @Test
  void embeddedDataImagesStayAllowedByDefault() throws Exception {
    String svg = render(Graphviz.digraph()
                            .addNode(Node.builder().id("logo").image(DATA_URI).build())
                            .build());

    assertTrue(svg.contains("<image"), svg);
    assertEquals(0, appender.events.size(), appender.messages().toString());
  }

  @Test
  void anOversizedDataImageIsReportedAgainstTheByteBudget() throws Exception {
    StringBuilder oversized = new StringBuilder("data:image/png;base64,");
    for (int i = 0; i < 200; i++) {
      oversized.append("QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFB");
    }
    SecurityPolicy policy = SecurityPolicy.builder().maxImageBytes(16).build();
    render(Graphviz.digraph().securityPolicy(policy)
               .addNode(Node.builder().id("logo").image(oversized.toString()).build())
               .build());

    String warning = onlyWarning();
    assertTrue(warning.contains("maxImageBytes"), warning);
    // The base64 payload must not be dumped in full.
    assertTrue(warning.contains("chars)"), warning);
    assertTrue(warning.length() < oversized.length(), warning);
  }

  /**
   * The reserved box is what makes a silent drop confusing: the node keeps the size the image
   * would have taken.
   */
  @Test
  void theBoxOfADroppedImageIsStillReserved() throws Exception {
    Node node = Node.builder().id("logo").image("https://images.example.com/logo.png")
        .imageSize(120, 200).build();
    NodeDrawProp prop = Layout.DOT.getLayoutEngine()
        .layout(Graphviz.digraph().addNode(node).build()).getNodeDrawProp(node);

    assertTrue(prop.getWidth() >= 200, "width was " + prop.getWidth());
    assertTrue(prop.getHeight() >= 120, "height was " + prop.getHeight());
  }

  private String render(Graphviz graphviz) throws Exception {
    return graphviz.toSvgStr();
  }

  private String onlyWarning() {
    List<String> messages = appender.messages();
    assertEquals(1, messages.size(), "expected exactly one warning but got " + messages);
    assertEquals(Level.WARN, appender.events.get(0).getLevel());
    return messages.get(0);
  }

  private static final class CapturingAppender extends AppenderSkeleton {

    private final List<LoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(LoggingEvent event) {
      events.add(event);
    }

    List<String> messages() {
      List<String> messages = new ArrayList<>(events.size());
      for (LoggingEvent event : events) {
        messages.add(String.valueOf(event.getRenderedMessage()));
      }
      return messages;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
      return false;
    }
  }
}
