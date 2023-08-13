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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import org.graphper.api.FileType;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.api.GraphResource;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.svg.SvgDrawBoard;
import org.graphper.util.CollectionUtils;

public class CommonDrawBoard extends SvgDrawBoard {

  private FileType fileType;

  private static final List<SvgConverter> converters;

  static {
    ServiceLoader<SvgConverter> converterServiceLoader = ServiceLoader.load(SvgConverter.class);
    List<SvgConverter> svgConverters = null;
    for (SvgConverter converter : converterServiceLoader) {
      if (!converter.envSupport()) {
        continue;
      }

      if (svgConverters == null) {
        svgConverters = new ArrayList<>();
      }
      svgConverters.add(converter);
    }

    if (CollectionUtils.isEmpty(svgConverters)) {
      converters = Collections.emptyList();
    } else {
      svgConverters.sort(Comparator.comparing(SvgConverter::order));
      converters = Collections.unmodifiableList(svgConverters);
    }
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

    if (CollectionUtils.isEmpty(converters)) {
      throwsUnsupportedImgConvert();
    }

    for (SvgConverter converter : converters) {
      if (converter.support(type)) {
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

  private void throwsUnsupportedImgConvert() throws FailInitResourceException {
    throw new FailInitResourceException("Do not have Converter");
  }
}
