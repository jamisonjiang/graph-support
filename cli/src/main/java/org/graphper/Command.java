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
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.antlr.v4.runtime.CharStream;
import org.graphper.api.FileType;
import org.graphper.api.SecurityPolicy;
import org.graphper.api.attributes.Layout;

/**
 * Represents a command for processing a DOT file and generating a graph image. This class contains
 * information about the input DOT file, output file, the file type for the output, the layout to
 * be used for generating the graph, and the image security policy to render under.
 *
 * @author Jamison Jiang
 */
public class Command {

  private CharStream dotFile;
  private File output;
  private FileType fileType;
  private Layout layout;
  private final Set<String> allowedImageHosts = new LinkedHashSet<>();
  private Path imageBaseDirectory;

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

  /**
   * Adds a hostname whose {@code http}/{@code https} images may be loaded. Adding at least one
   * host is what turns remote image loading on; without it remote images stay denied.
   *
   * @param host DNS hostname without a port
   */
  public void addAllowedImageHost(String host) {
    allowedImageHosts.add(host);
  }

  /**
   * Gets the hostnames whose remote images are allowed.
   *
   * @return the allowed hostnames, in the order they were given
   */
  public Set<String> getAllowedImageHosts() {
    return allowedImageHosts;
  }

  /**
   * Sets the directory that filesystem image references must resolve inside. Without it
   * filesystem images stay denied.
   *
   * @param imageBaseDirectory the local image base directory
   */
  public void setImageBaseDirectory(Path imageBaseDirectory) {
    this.imageBaseDirectory = imageBaseDirectory;
  }

  /**
   * Gets the local image base directory.
   *
   * @return the local image base directory, or {@code null} when none was given
   */
  public Path getImageBaseDirectory() {
    return imageBaseDirectory;
  }

  /**
   * Builds the rendering security policy for this command. When no image option was given this is
   * exactly {@link SecurityPolicy#defaultPolicy()}, so the secure default is unchanged; the
   * options only ever add explicitly named hosts or one explicitly named base directory.
   *
   * @return the security policy to render under
   * @throws IllegalArgumentException if a hostname is not a bare DNS hostname
   */
  public SecurityPolicy getSecurityPolicy() {
    if (allowedImageHosts.isEmpty() && imageBaseDirectory == null) {
      return SecurityPolicy.defaultPolicy();
    }
    SecurityPolicy.Builder builder = SecurityPolicy.builder();
    if (!allowedImageHosts.isEmpty()) {
      builder.allowRemoteImages(true);
      for (String host : allowedImageHosts) {
        builder.allowRemoteImageHost(host);
      }
    }
    if (imageBaseDirectory != null) {
      builder.localImageBaseDirectory(imageBaseDirectory.toFile());
    }
    return builder.build();
  }
}
