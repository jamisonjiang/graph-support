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

package org.graphper.api;

/**
 * File type enumeration.
 *
 * @author Jamison Jiang
 */
public enum FileType {
  SVG("svg"), PNG("png"), JPG("jpg"), JPEG("jpeg"), GIF("gif"),

  // Need external plugin: Apache Batik
  TIFF("tiff"),
  // Need external plugin: Apache FOP
  PDF("pdf")
  ;

  FileType(String type) {
    this.type = type;
  }

  private final String type;

  public String getType() {
    return type;
  }
}
