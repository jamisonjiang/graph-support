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

import java.util.List;
import org.graphper.api.FileType;
import org.graphper.api.GraphResource;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.svg.SvgDrawBoard;
import org.graphper.util.CollectionUtils;
import org.graphper.util.OptionalProviders;

/** Converts the SVG drawing into the requested output format. */
public class CommonDrawBoard extends SvgDrawBoard {

  private FileType fileType;

  private static class ConverterHolder {
    private static final List<SvgConverter> CONVERTERS = OptionalProviders.load(SvgConverter.class);
  }

  public CommonDrawBoard(DrawGraph drawGraph) {
    super(drawGraph);
  }

  public void setImageType(FileType fileType) {
    this.fileType = fileType;
  }

  @Override
  public synchronized GraphResource graphResource() throws FailInitResourceException {
    FileType type = fileType == null ? FileType.SVG : fileType;
    if (type == FileType.SVG) {
      return super.graphResource();
    }

    List<SvgConverter> converters = ConverterHolder.CONVERTERS;
    if (CollectionUtils.isEmpty(converters)) {
      throwsUnsupportedImgConvert();
    }

    boolean image = containsImage();
    for (SvgConverter converter : converters) {
      if (converter.support(type)) {
        // Prefer the bounded native image loader over built-in Batik, not over custom providers.
        if (image
            && converter
                .getClass()
                .getName()
                .equals("org.graphper.draw.common.BatikImgConverter")) {
          for (SvgConverter nativeConverter : converters) {
            if (nativeConverter
                    .getClass()
                    .getName()
                    .equals("org.graphper.draw.common.DefaultImgConverter")
                && nativeConverter.support(type)) {
              DefaultGraphResource resource = nativeConverter.convert(svgDocument, drawGraph, type);
              if (resource != null) {
                return resource;
              }
            }
          }
        }
        DefaultGraphResource resource = converter.convert(svgDocument, drawGraph, type);
        if (resource == null) {
          continue;
        }
        return resource;
      }
    }

    throwsUnsupportedImgConvert();
    return null;
  }

  private boolean containsImage() {
    final boolean[] image = new boolean[1];
    svgDocument.accessEles(
        (element, children) -> {
          if ("image".equals(element.tagName())) {
            image[0] = true;
          }
        });
    return image[0];
  }

  private void throwsUnsupportedImgConvert() throws FailInitResourceException {
    throw new FailInitResourceException(
        "No secure converter available for "
            + fileType
            + ". TIFF/PDF require supported modern Batik with external-resource and script security"
            + " hints (PDF also requires compatible FOP)."
            + " PNG/JPEG can use the native AWT converter.");
  }
}
