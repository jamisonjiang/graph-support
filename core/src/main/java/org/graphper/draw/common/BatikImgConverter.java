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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.FileType;
import org.graphper.api.SecurityPolicy;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.svg.Document;
import org.graphper.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link SvgConverter} that uses Apache Batik to convert SVG documents into
 * various image formats. This class supports formats such as PNG, JPEG, and TIFF by utilizing
 * Batik's transcoder classes. The environment must support the AWT {@link java.awt.Graphics2D} and
 * have Batik libraries available. TIFF output also requires an ImageIO TIFF writer and is
 * unsupported on a default Java 8 runtime.
 *
 * @author Jamison Jiang
 */
public class BatikImgConverter implements SvgConverter {

  private static final Logger log = LoggerFactory.getLogger(BatikImgConverter.class);

  private static final String T_IN_C = "org.apache.batik.transcoder.TranscoderInput";
  private static final String T_OUT_C = "org.apache.batik.transcoder.TranscoderOutput";
  private static final String T_C_C = "org.apache.batik.transcoder.Transcoder";
  private static final String P_T_C = "org.apache.batik.transcoder.image.PNGTranscoder";
  private static final String J_T_C = "org.apache.batik.transcoder.image.JPEGTranscoder";
  private static final String TF_T_C = "org.apache.batik.transcoder.image.TIFFTranscoder";
  private static final String SVG_A_T_C = "org.apache.batik.transcoder.SVGAbstractTranscoder";
  private static final String HINT_KEY_C = "org.apache.batik.transcoder.TranscodingHints$Key";

  /**
   * Hardening hints switched off before transcoding, most restrictive first. {@code
   * KEY_ALLOW_EXTERNAL_RESOURCES} only exists from Batik 1.13 onwards, so it is probed rather than
   * assumed.
   */
  private static final String[] SECURITY_HINT_FIELDS = {
    "KEY_ALLOW_EXTERNAL_RESOURCES", "KEY_EXECUTE_ONLOAD"
  };

  /** Sentinel stored for a hint that this Batik build does not expose. */
  private static final Object ABSENT_HINT = new Object();

  /** Probe results, so a missing field costs one reflective failure rather than one per convert. */
  private static final Map<String, Object> HINT_KEYS = new ConcurrentHashMap<>();

  /**
   * Hints already reported as unavailable. Package-private so the one-time warning can be asserted
   * by tests.
   */
  static final Set<String> DEGRADED_HINTS =
      Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

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
   * Checks if the current environment supports image conversion. Specifically, it checks for the
   * availability of required AWT and Batik classes.
   *
   * <p>Required security hints must also be applicable. Otherwise the native PNG/JPEG converter
   * remains available, but this converter is not selected.
   *
   * @return {@code true} if the environment supports image conversion, {@code false} otherwise
   */
  @Override
  public boolean envSupport() {
    try {
      Class.forName("java.awt.Graphics2D");
      Class.forName(T_IN_C);
      Class.forName(T_OUT_C);
      Class.forName(T_C_C);
      Class.forName(P_T_C);
      Class.forName(J_T_C);
      Class.forName(TF_T_C);
      Class.forName(SVG_A_T_C);
      Class.forName(HINT_KEY_C);
      configureSecurityHints(ClassUtils.newObject(Class.forName(P_T_C)));
      configureSecurityHints(ClassUtils.newObject(Class.forName(J_T_C)));
      configureSecurityHints(ClassUtils.newObject(Class.forName(TF_T_C)));
      return true;
    } catch (Exception | LinkageError e) {
      return false;
    }
  }

  /**
   * Returns the supported file types for the conversion.
   *
   * @return an array of supported {@link FileType}
   */
  @Override
  public FileType[] supportFileTypes() {
    return new FileType[] {FileType.PNG, FileType.JPG, FileType.JPEG, FileType.TIFF};
  }

  /**
   * Converts the given SVG document into an image of the specified type. Uses Apache Batik's
   * transcoders to handle the conversion.
   *
   * @param document the SVG document to convert
   * @param drawGraph the drawing context with graph-related attributes
   * @param fileType the target image type for conversion
   * @return a {@link DefaultGraphResource} representing the converted image
   * @throws FailInitResourceException if the conversion fails or if parameters are missing
   */
  @Override
  public DefaultGraphResource convert(Document document, DrawGraph drawGraph, FileType fileType)
      throws FailInitResourceException {
    if (document == null || drawGraph == null || fileType == null) {
      throw new FailInitResourceException("Lack parameters to convert image");
    }
    if (fileType == FileType.TIFF
        && !javax.imageio.ImageIO.getImageWritersByFormatName("TIFF").hasNext()) {
      throw new FailInitResourceException(
          "TIFF export requires an ImageIO TIFF writer;"
              + " default Java 8 runtimes do not support TIFF");
    }

    String svg = document.toXml();
    if (StringUtils.isEmpty(svg)) {
      throw new FailInitResourceException("Can not get svg");
    }
    try {
      svg = SecureSvg.prepare(svg, drawGraph.getGraphviz().graphAttrs().getSecurityPolicy(), true);
      Object transcoder;
      switch (fileType) {
        case PNG:
          transcoder = ClassUtils.newObject(Class.forName(P_T_C));
          break;
        case JPG:
        case JPEG:
          transcoder = ClassUtils.newObject(Class.forName(J_T_C));
          break;
        case TIFF:
          transcoder = ClassUtils.newObject(Class.forName(TF_T_C));
          break;
        default:
          transcoder = ClassUtils.newObject(Class.forName(P_T_C));
          break;
      }

      configureSecurityHints(transcoder);

      try (InputStream is = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8))) {
        return getFileGraphResource(drawGraph, fileType, is, transcoder);
      }
    } catch (Exception | LinkageError e) {
      throw new FailInitResourceException(e);
    }
  }

  /**
   * Converts static SVG to PNG without allowing Batik to fetch external resources. Policy-approved
   * image references, including opt-in remote and local sources, are loaded by the shared secure
   * image loader, raster-validated, and replaced with canonical data URIs before transcoding. Local
   * file URIs must resolve inside the policy's real base directory; relative paths resolve against
   * that base. Fixed SVG structural/aggregate image limits and the policy's output pixel budget are
   * checked before transcoding. These limits are not a general complexity guarantee for arbitrary
   * untrusted SVG.
   *
   * @param svg SVG source, including sources not generated by graph-support
   * @param policy rendering policy, or {@code null} for the secure default
   * @return PNG bytes
   * @throws FailInitResourceException if SVG is unsafe or secure Batik conversion is unavailable
   */
  public byte[] pngBytes(String svg, SecurityPolicy policy) throws FailInitResourceException {
    try {
      String safeSvg =
          SecureSvg.prepare(svg, policy == null ? SecurityPolicy.defaultPolicy() : policy, true);
      Object transcoder = ClassUtils.newObject(Class.forName(P_T_C));
      configureSecurityHints(transcoder);
      try (InputStream input = new ByteArrayInputStream(safeSvg.getBytes(StandardCharsets.UTF_8))) {
        return transcodeAndReturnOS(input, transcoder).toByteArray();
      }
    } catch (Exception | LinkageError e) {
      throw new FailInitResourceException(e);
    }
  }

  /**
   * Switches off external resource loading and on-load script execution for {@code transcoder}.
   *
   * <p>A missing or rejected hint aborts conversion, including direct calls that bypass converter
   * selection.
   *
   * @param transcoder the Batik transcoder to harden
   * @throws Exception if the Batik transcoder base classes are missing altogether
   */
  protected void configureSecurityHints(Object transcoder) throws Exception {
    applySecurityHints(transcoder, Class.forName(SVG_A_T_C), Class.forName(HINT_KEY_C));
  }

  /**
   * Applies every required hardening hint. Package-private so tests can supply a transcoder base
   * that is missing a hint field.
   *
   * @param transcoder transcoder receiving the hints
   * @param transcoderBase class declaring the hint key constants
   * @param hintKeyType declared parameter type of {@code addTranscodingHint}
   */
  static void applySecurityHints(Object transcoder, Class<?> transcoderBase, Class<?> hintKeyType) {
    for (String field : SECURITY_HINT_FIELDS) {
      Object key = hintKey(transcoderBase, field);
      if (key == ABSENT_HINT) {
        throw insecureBatik(field, null);
      }
      try {
        ClassUtils.invoke(
            transcoder,
            "addTranscodingHint",
            new Class[] {hintKeyType, Object.class},
            key,
            Boolean.FALSE);
      } catch (Exception | LinkageError e) {
        throw insecureBatik(field, e);
      }
    }
  }

  /**
   * Returns the hint key constant declared by {@code transcoderBase}, or {@link #ABSENT_HINT} when
   * this Batik build does not declare it. Probed once per class and field name.
   */
  private static Object hintKey(Class<?> transcoderBase, String field) {
    String cacheKey = transcoderBase.getName() + '#' + field;
    Object cached = HINT_KEYS.get(cacheKey);
    if (cached != null) {
      return cached;
    }
    Object key;
    try {
      key = ClassUtils.getStaticField(transcoderBase, field);
      if (key == null) {
        key = ABSENT_HINT;
      }
    } catch (Exception | LinkageError e) {
      key = ABSENT_HINT;
    }
    HINT_KEYS.put(cacheKey, key);
    return key;
  }

  private static IllegalStateException insecureBatik(String field, Throwable cause) {
    String message =
        "No secure Batik/FOP converter: required security hint "
            + field
            + " is unavailable or rejected. Use a supported modern Batik (1.13+ security hints)"
            + " and compatible FOP for TIFF/PDF; graph PNG/JPEG can use the native converter.";
    if (DEGRADED_HINTS.add(field)) {
      log.warn(message, cause);
    }
    return new IllegalStateException(message, cause);
  }

  /**
   * Generates a {@link DefaultGraphResource} for the converted image.
   *
   * @param drawGraph the drawing context with graph-related attributes
   * @param fileType the target image type for conversion
   * @param is the input stream containing the SVG data
   * @param transcoder the Batik transcoder object used for conversion
   * @return a {@link DefaultGraphResource} representing the converted image
   * @throws Exception if the conversion fails
   */
  protected DefaultGraphResource getFileGraphResource(
      DrawGraph drawGraph, FileType fileType, InputStream is, Object transcoder) throws Exception {
    ByteArrayOutputStream baos = transcodeAndReturnOS(is, transcoder);
    String label = drawGraph.getGraphviz().graphAttrs().getLabel();
    return new DefaultGraphResource(label, fileType.getType(), baos);
  }

  /**
   * Transcodes the SVG data from the input stream and writes it to a {@link ByteArrayOutputStream}.
   *
   * @param is the input stream containing the SVG data
   * @param transcoder the Batik transcoder object used for conversion
   * @return a {@link ByteArrayOutputStream} containing the transcoded image data
   * @throws Exception if the transcoding process fails
   */
  private ByteArrayOutputStream transcodeAndReturnOS(InputStream is, Object transcoder)
      throws Exception {
    Class<?> inputClazz = Class.forName(T_IN_C);
    Class<?> outputClazz = Class.forName(T_OUT_C);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Object input = ClassUtils.newObjectOne(inputClazz, InputStream.class, is);
    Object output = ClassUtils.newObjectOne(outputClazz, OutputStream.class, baos);
    ClassUtils.invoke(
        transcoder, "transcode", new Class[] {inputClazz, outputClazz}, input, output);
    return baos;
  }
}
