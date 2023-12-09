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
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.FileType;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.svg.Document;
import org.graphper.util.ClassUtils;

public class BatikImgConverter implements SvgConverter {

  private static final String T_IN_C = "org.apache.batik.transcoder.TranscoderInput";
  private static final String T_OUT_C = "org.apache.batik.transcoder.TranscoderOutput";
  private static final String T_C_C = "org.apache.batik.transcoder.Transcoder";
  private static final String P_T_C = "org.apache.batik.transcoder.image.PNGTranscoder";
  private static final String J_T_C = "org.apache.batik.transcoder.image.JPEGTranscoder";
  private static final String TF_T_C = "org.apache.batik.transcoder.image.TIFFTranscoder";

  @Override
  public int order() {
    return 0;
  }

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
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @Override
  public FileType[] supportFileTypes() {
    return new FileType[]{FileType.PNG, FileType.JPG, FileType.JPEG, FileType.TIFF};
  }

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

      return getFileGraphResource(drawGraph, fileType, is, transcoder);
    } catch (Exception e) {
      throw new FailInitResourceException(e);
    }
  }

  protected DefaultGraphResource getFileGraphResource(DrawGraph drawGraph, FileType fileType,
                                                      InputStream is, Object transcoder)
      throws Exception {
    ByteArrayOutputStream baos = transcodeAndReturnOS(is, transcoder);
    String label = drawGraph.getGraphviz().graphAttrs().getLabel();
    return new DefaultGraphResource(label, fileType.getType(), baos);
  }

  private ByteArrayOutputStream transcodeAndReturnOS(InputStream is, Object transcoder) throws Exception {
    Class<?> inputClazz = Class.forName(T_IN_C);
    Class<?> outputClazz = Class.forName(T_OUT_C);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Object input = ClassUtils.newObjectOne(inputClazz, InputStream.class, is);
    Object output = ClassUtils.newObjectOne(outputClazz, OutputStream.class, baos);
    ClassUtils.invoke(transcoder, "transcode",
                      new Class[]{inputClazz, outputClazz}, input, output);
    return baos;
  }
}
