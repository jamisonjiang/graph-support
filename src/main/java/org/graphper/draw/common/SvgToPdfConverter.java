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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.FileType;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.svg.Document;
import org.graphper.util.ClassUtils;

/**
 * Implementation of a converter that extends {@link BatikImgConverter} to convert SVG documents
 * into PDF format. This class uses Apache FOP's {@code PDFTranscoder} to handle the SVG to PDF
 * conversion process. The environment must support the AWT {@link java.awt.Graphics2D}, Apache
 * Batik, and Apache FOP libraries.
 *
 * @author Jamison Jiang
 */
public class SvgToPdfConverter extends BatikImgConverter {

  private static Class<?> PDF_TRANSCODER;
  private static Class<?> TRANSCODING_HINTS;
  private static Class<?> SVG_A_TRANSCODER;
  private static Class<?> XML_A_TRANSCODER;
  private static Class<?> SVG_DOM_IMPL;

  static {
    try {
      PDF_TRANSCODER = Class.forName("org.apache.fop.svg.PDFTranscoder");
      TRANSCODING_HINTS = Class.forName("org.apache.batik.transcoder.TranscodingHints");
      SVG_A_TRANSCODER = Class.forName("org.apache.batik.transcoder.SVGAbstractTranscoder");
      XML_A_TRANSCODER = Class.forName("org.apache.batik.transcoder.XMLAbstractTranscoder");
      SVG_DOM_IMPL = Class.forName("org.apache.batik.anim.dom.SVGDOMImplementation");
    } catch (Exception e) {
      // ignore
    }
  }

  /**
   * Returns the priority order of this converter. The default order is set to 0.
   *
   * @return the priority order of this converter
   */
  @Override
  public int order() {
    return 0;
  }

  /**
   * Checks if the current environment supports image conversion to PDF. Specifically, it checks for
   * the availability of required AWT, Apache Batik, and Apache FOP classes.
   *
   * @return {@code true} if the environment supports conversion to PDF, {@code false} otherwise
   */
  @Override
  public boolean envSupport() {
    if (!super.envSupport()) {
      return false;
    }
    return PDF_TRANSCODER != null && SVG_DOM_IMPL != null;
  }

  /**
   * Returns the supported file type for the conversion, which is PDF.
   *
   * @return an array containing {@link FileType#PDF}
   */
  @Override
  public FileType[] supportFileTypes() {
    return new FileType[]{FileType.PDF};
  }

  /**
   * Converts the given SVG document into a PDF. Uses Apache FOP's {@code PDFTranscoder} to handle
   * the conversion.
   *
   * @param document  the SVG document to convert
   * @param drawGraph the drawing context with graph-related attributes
   * @param fileType  the target file type for conversion, which must be PDF
   * @return a {@link DefaultGraphResource} representing the converted PDF
   * @throws FailInitResourceException if the conversion fails or if parameters are missing
   */
  @Override
  public DefaultGraphResource convert(Document document, DrawGraph drawGraph, FileType fileType)
      throws FailInitResourceException {
    if (document == null || drawGraph == null || fileType == null) {
      throw new FailInitResourceException("Lack parameters to convert image");
    }

    String svg = document.toXml();
    if (StringUtils.isEmpty(svg)) {
      throw new FailInitResourceException("Can not get svg");
    }

    try (InputStream is = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8))) {
      Object transcoder = ClassUtils.newObject(PDF_TRANSCODER);
      Object transcodingHints = ClassUtils.newObject(TRANSCODING_HINTS);
      Class<?>[] paramTypes = {Object.class, Object.class};
      ClassUtils.invoke(transcodingHints, "put", paramTypes,
                        ClassUtils.getStaticField(SVG_A_TRANSCODER, "KEY_WIDTH"), 1000f);
      ClassUtils.invoke(transcodingHints, "put", paramTypes,
                        ClassUtils.getStaticField(SVG_A_TRANSCODER, "KEY_HEIGHT"), 1000f);
      ClassUtils.invoke(transcodingHints, "put", paramTypes,
                        ClassUtils.getStaticField(XML_A_TRANSCODER, "KEY_DOM_IMPLEMENTATION"),
                        ClassUtils.invokeStatic(SVG_DOM_IMPL, "getDOMImplementation"));
      ClassUtils.invoke(transcodingHints, "put", paramTypes,
                        ClassUtils.getStaticField(XML_A_TRANSCODER,
                                                  "KEY_DOCUMENT_ELEMENT_NAMESPACE_URI"),
                        ClassUtils.getStaticField(SVG_DOM_IMPL, "SVG_NAMESPACE_URI"));
      ClassUtils.invoke(transcodingHints, "put", paramTypes,
                        ClassUtils.getStaticField(XML_A_TRANSCODER, "KEY_DOCUMENT_ELEMENT"),
                        FileType.SVG.getType());
      ClassUtils.invoke(transcoder, "setTranscodingHints", transcodingHints);
      return getFileGraphResource(drawGraph, FileType.PDF, is, transcoder);
    } catch (Exception e) {
      throw new FailInitResourceException(e);
    }
  }
}