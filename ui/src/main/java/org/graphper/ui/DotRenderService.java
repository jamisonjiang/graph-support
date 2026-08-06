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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.Graphviz;
import org.graphper.parser.DotParser;
import org.graphper.parser.ParseException;

/**
 * Turns DOT source into the resources the editor needs: an SVG string for the live preview and byte
 * payloads for SVG and PNG export.
 *
 * @author Jamison Jiang
 */
public class DotRenderService {

  /**
   * Parses and lays out the given DOT source and returns the rendered SVG.
   *
   * @param dot the DOT source to render
   * @return the rendered SVG document as a string
   * @throws IllegalArgumentException if the source produces an empty graph
   * @throws IllegalStateException    if the graph cannot be rendered
   */
  public String renderSvg(String dot) {
    Graphviz graphviz = DotParser.parse(dot);
    if (graphviz.isEmpty()) {
      throw new IllegalArgumentException("Graph is empty");
    }
    try {
      return graphviz.toSvgStr();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to render graph", e);
    }
  }

  /**
   * Checks the given DOT source for syntax errors without laying the graph out, so the editor can
   * flag mistakes while typing. Only genuine syntax errors are reported; blank input and other
   * non-syntax conditions (such as an empty graph) return {@code null}.
   *
   * @param dot the DOT source to check
   * @return the first syntax error, or {@code null} when the source parses cleanly
   */
  public DotError findError(String dot) {
    if (StringUtils.isBlank(dot)) {
      return null;
    }
    try {
      DotParser.parse(dot);
      return null;
    } catch (ParseException e) {
      return DotError.fromMessage(e.getMessage());
    } catch (RuntimeException e) {
      // Non-syntax problems (for example an empty graph) are surfaced by the render status only.
      return null;
    }
  }

  /**
   * Encodes an SVG document as UTF-8 bytes for saving to disk.
   *
   * @param svg the SVG document
   * @return the UTF-8 encoded bytes
   */
  public byte[] svgBytes(String svg) {
    return svg.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Transcodes an SVG document to PNG bytes.
   *
   * @param svg the SVG document
   * @return the encoded PNG bytes
   * @throws IllegalStateException if the SVG cannot be transcoded
   */
  public byte[] pngBytes(String svg) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      PNGTranscoder transcoder = new PNGTranscoder();
      transcoder.transcode(new TranscoderInput(new ByteArrayInputStream(svgBytes(svg))),
                           new TranscoderOutput(output));
      return output.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to export PNG", e);
    }
  }
}
