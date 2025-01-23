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

package org.graphper;

import java.io.File;
import org.graphper.api.FileType;
import org.graphper.api.attributes.Layout;

public class Command {

  private File dotFile;

  private File output;

  private FileType fileType;

  private Layout layout;

  public File getDotFile() {
    return dotFile;
  }

  public File getOutput() {
    return output;
  }

  public FileType getFileType() {
    return fileType == null ? FileType.SVG : fileType;
  }

  public void setDotFile(File dotFile) {
    this.dotFile = dotFile;
  }

  public void setOutput(File output) {
    this.output = output;
  }

  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  public Layout getLayout() {
    return layout;
  }

  public void setLayout(Layout layout) {
    this.layout = layout;
  }
}
