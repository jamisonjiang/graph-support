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

package org.graphper.draw.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.graphper.api.FileType;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Hint keys are simulated so these tests work both with and without Batik. These stand-ins
 * mirror the only difference that matters: {@code KEY_ALLOW_EXTERNAL_RESOURCES} exists from Batik
 * 1.13 onwards and is absent before it.
 */
public class BatikSecurityHintTest {

  /** Batik 1.13 and later: both hardening hints are declared. */
  public static final class ModernTranscoderBase {

    public static final Object KEY_ALLOW_EXTERNAL_RESOURCES = "allow-external-resources";
    public static final Object KEY_EXECUTE_ONLOAD = "execute-onload";
  }

  /** Batik 1.9/1.12: no external-resource switch at all. */
  public static final class LegacyTranscoderBase {

    public static final Object KEY_EXECUTE_ONLOAD = "execute-onload";
  }

  /** A Batik so old that neither hint exists. */
  public static final class AncientTranscoderBase {
  }

  public static final class MissingScriptHintBase {

    public static final Object KEY_ALLOW_EXTERNAL_RESOURCES = "allow-external-resources";
  }

  /** Stands in for a transcoder; records what would have been handed to Batik. */
  public static final class RecordingTranscoder {

    private final Map<Object, Object> hints = new LinkedHashMap<>();

    public void addTranscodingHint(Object key, Object value) {
      hints.put(key, value);
    }
  }

  /** A transcoder whose hint setter blows up, standing in for an unexpected Batik variant. */
  public static final class FailingTranscoder {

    public void addTranscodingHint(Object key, Object value) {
      throw new IllegalStateException("hint rejected");
    }
  }

  @BeforeEach
  public void resetWarnings() {
    BatikImgConverter.DEGRADED_HINTS.clear();
  }

  @Test
  public void modernBatikGetsBothHardeningHints() {
    RecordingTranscoder transcoder = new RecordingTranscoder();
    BatikImgConverter.applySecurityHints(transcoder, ModernTranscoderBase.class, Object.class);

    Assertions.assertEquals(2, transcoder.hints.size());
    Assertions.assertEquals(
        Boolean.FALSE,
        transcoder.hints.get(ModernTranscoderBase.KEY_ALLOW_EXTERNAL_RESOURCES));
    Assertions.assertEquals(Boolean.FALSE,
                            transcoder.hints.get(ModernTranscoderBase.KEY_EXECUTE_ONLOAD));
    Assertions.assertTrue(BatikImgConverter.DEGRADED_HINTS.isEmpty(),
                          "nothing should be reported as degraded: "
                              + BatikImgConverter.DEGRADED_HINTS);
  }

  @Test
  public void missingExternalResourceHintFailsClosed() {
    RecordingTranscoder transcoder = new RecordingTranscoder();
    IllegalStateException error = Assertions.assertThrows(IllegalStateException.class,
        () -> BatikImgConverter.applySecurityHints(transcoder, LegacyTranscoderBase.class,
                                                   Object.class));
    Assertions.assertTrue(error.getMessage().contains("KEY_ALLOW_EXTERNAL_RESOURCES"));
    Assertions.assertTrue(transcoder.hints.isEmpty());
  }

  @Test
  public void rejectionIsReportedOnceButEveryAttemptFails() {
    for (int i = 0; i < 3; i++) {
      RecordingTranscoder transcoder = new RecordingTranscoder();
      Assertions.assertThrows(IllegalStateException.class,
          () -> BatikImgConverter.applySecurityHints(transcoder, LegacyTranscoderBase.class,
                                                     Object.class));
      Assertions.assertTrue(transcoder.hints.isEmpty());
    }
    Assertions.assertEquals(1, BatikImgConverter.DEGRADED_HINTS.size());
  }

  @Test
  public void batikWithoutAnyHardeningHintFailsClosed() {
    RecordingTranscoder transcoder = new RecordingTranscoder();
    Assertions.assertThrows(IllegalStateException.class,
        () -> BatikImgConverter.applySecurityHints(transcoder, AncientTranscoderBase.class,
                                                   Object.class));
    Assertions.assertTrue(transcoder.hints.isEmpty());
  }

  @Test
  public void aRejectedHintPropagates() {
    Assertions.assertThrows(IllegalStateException.class,
        () -> BatikImgConverter.applySecurityHints(new FailingTranscoder(),
                                                   ModernTranscoderBase.class, Object.class));
  }

  @Test
  public void missingScriptHintAlsoFailsClosed() {
    IllegalStateException error = Assertions.assertThrows(IllegalStateException.class,
        () -> BatikImgConverter.applySecurityHints(new RecordingTranscoder(),
                                                   MissingScriptHintBase.class, Object.class));
    Assertions.assertTrue(error.getMessage().contains("KEY_EXECUTE_ONLOAD"));
  }

  @Test
  public void converterIsNotSelectedWhenBatikIsAbsent() throws Exception {
    boolean present;
    try {
      Class.forName("org.apache.batik.transcoder.image.PNGTranscoder");
      present = true;
    } catch (ClassNotFoundException | LinkageError e) {
      present = false;
    }
    Assumptions.assumeFalse(present, "This case specifically tests a classpath without Batik");
    Assertions.assertFalse(new BatikImgConverter().envSupport());
    Assertions.assertFalse(new SvgToPdfConverter().envSupport());
    Graphviz graph = Graphviz.digraph().addNode(Node.builder().label("native").build()).build();
    try (org.graphper.api.GraphResource png = graph.toFile(FileType.PNG);
        org.graphper.api.GraphResource jpeg = graph.toFile(FileType.JPEG)) {
      Assertions.assertEquals((byte) 0x89, png.bytes()[0]);
      Assertions.assertEquals((byte) 0xff, jpeg.bytes()[0]);
    }
  }

  @Test
  public void modernPdfConverterStillProducesVectorOutputWhenInstalled() throws Exception {
    try {
      Class.forName("org.apache.fop.svg.PDFTranscoder");
      Class.forName("org.apache.batik.transcoder.SVGAbstractTranscoder")
          .getField("KEY_ALLOW_EXTERNAL_RESOURCES");
    } catch (ClassNotFoundException | NoSuchFieldException | LinkageError e) {
      Assumptions.assumeTrue(false, "Supported Batik/FOP is not on this test classpath");
    }
    Assertions.assertTrue(new SvgToPdfConverter().envSupport());
    Graphviz graph = Graphviz.digraph().addNode(Node.builder().label("vector").build())
        .securityPolicy(org.graphper.api.SecurityPolicy.builder().maxOutputPixels(1).build()).build();
    try (org.graphper.api.GraphResource pdf = graph.toFile(FileType.PDF)) {
      Assertions.assertEquals("%PDF", new String(pdf.bytes(), 0, 4,
                                               java.nio.charset.StandardCharsets.US_ASCII));
    }
  }

  @Test
  public void rejectedHintsDeselectRasterAndPdfConverters() {
    BatikImgConverter raster = new BatikImgConverter() {
      @Override
      protected void configureSecurityHints(Object transcoder) {
        throw new IllegalStateException("required hint rejected");
      }
    };
    SvgToPdfConverter pdf = new SvgToPdfConverter() {
      @Override
      protected void configureSecurityHints(Object transcoder) {
        throw new IllegalStateException("required hint rejected");
      }
    };
    Assertions.assertFalse(raster.support(FileType.PNG));
    Assertions.assertFalse(raster.support(FileType.JPEG));
    Assertions.assertFalse(raster.support(FileType.TIFF));
    Assertions.assertFalse(pdf.support(FileType.PDF));
    Assertions.assertTrue(new DefaultImgConverter().support(FileType.PNG));
    Assertions.assertTrue(new DefaultImgConverter().support(FileType.JPEG));
  }

  @Test
  public void absentSecureConverterHasActionableTiffAndPdfDiagnostics() {
    Graphviz graph = Graphviz.digraph().addNode(Node.builder().label("a").build()).build();
    for (FileType type : new FileType[]{FileType.TIFF, FileType.PDF}) {
      BatikImgConverter converter = type == FileType.PDF
          ? new SvgToPdfConverter() : new BatikImgConverter();
      if (converter.envSupport()) {
        continue;
      }
      CommonDrawBoard board = new CommonDrawBoard(new DrawGraph(graph));
      board.setImageType(type);
      FailInitResourceException error = Assertions.assertThrows(FailInitResourceException.class,
                                                               board::graphResource);
      Assertions.assertTrue(error.getMessage().contains("No secure converter"));
      Assertions.assertTrue(error.getMessage().contains(type.toString()));
    }
  }
}
