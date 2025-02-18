/*b
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
import org.antlr.v4.runtime.CharStream;
import org.graphper.api.FileType;
import org.graphper.api.attributes.Layout;

/**
 * Represents a command for processing a DOT file and generating a graph image. This class contains
 * information about the input DOT file, output file, the file type for the output, and the layout
 * to be used for generating the graph.
 *
 * @author Jamison Jiang
 */
public class Command {

  private CharStream dotFile;
  private File output;
  private FileType fileType;
  private Layout layout;

  /**
   * Gets the DOT file as a CharStream.
   *
   * @return The CharStream representing the DOT file.
   */
  public CharStream getDotFile() {
    return dotFile;
  }

  /**
   * Sets the DOT file as a CharStream.
   *
   * @param dotFile The CharStream representing the DOT file.
   */
  public void setDotFile(CharStream dotFile) {
    this.dotFile = dotFile;
  }

  /**
   * Gets the output file where the generated graph will be saved.
   *
   * @return The output file.
   */
  public File getOutput() {
    return output;
  }

  /**
   * Gets the file type for the output. If not set, defaults to SVG.
   *
   * @return The output file type.
   */
  public FileType getFileType() {
    return fileType == null ? FileType.SVG : fileType;
  }

  /**
   * Sets the output file where the generated graph will be saved.
   *
   * @param output The output file.
   */
  public void setOutput(File output) {
    this.output = output;
  }

  /**
   * Sets the file type for the output.
   *
   * @param fileType The file type (e.g., PNG, SVG, PDF).
   */
  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  /**
   * Gets the layout for the graph.
   *
   * @return The layout.
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Sets the layout for the graph.
   *
   * @param layout The layout to use (e.g., DOT, FDP).
   */
  public void setLayout(Layout layout) {
    this.layout = layout;
  }
}