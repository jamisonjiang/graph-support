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

import org.graphper.api.FileType;
import org.graphper.api.Graphviz;
import org.graphper.draw.DrawGraph;
import org.graphper.draw.FailInitResourceException;
import org.graphper.draw.DefaultGraphResource;
import org.graphper.draw.svg.Document;
import org.graphper.layout.EnvStrategy;

/**
 * Svg is basic data for {@link Graphviz}, the other file type ({@link FileType}) should use this
 * interface to convert.
 *
 * @author Jamison Jiang
 */
public interface SvgConverter extends EnvStrategy {

  /**
   * Returns true if current {@code SvgConverter} supported this file type.
   *
   * @param fileType file type
   * @return true if supported
   */
  default boolean support(FileType fileType) {
    if (!envSupport()) {
      return false;
    }
    FileType[] fileTypes = supportFileTypes();
    if (fileTypes == null) {
      return false;
    }
    for (FileType type : fileTypes) {
      if (type == fileType) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns all supported {@link FileType}s.
   *
   * @return all supported file types
   */
  FileType[] supportFileTypes();

  /**
   * Convert svg to specific file type and returns {@link org.graphper.api.GraphResource}.
   *
   * @param document  svg document
   * @param drawGraph draw graph
   * @param fileType  file type
   * @return graph support
   * @throws FailInitResourceException if any error occurred
   */
  DefaultGraphResource convert(Document document, DrawGraph drawGraph, FileType fileType)
      throws FailInitResourceException;
}
