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

import static org.graphper.Version.getAntlrVersion;
import static org.graphper.Version.getBatikVersion;
import static org.graphper.Version.getFopVersion;
import static org.graphper.Version.getVersionFromPom;

import java.io.File;
import org.graphper.api.FileType;

public class Command {

  private File dotFile;

  private File output;

  private FileType fileType;

  public Command(String[] args) throws WrongCommandException {
    int i = 0;
    while (i < args.length) {
      i = parseArgs(args, i);
    }
  }

  public File getDotFile() {
    return dotFile;
  }

  public File getOutput() {
    return output;
  }

  public FileType getFileType() {
    return fileType == null ? FileType.SVG : fileType;
  }

  private int parseArgs(String[] args, int currentIdx) throws WrongCommandException {
    String arg = args[currentIdx];
    if (arg.equals("-v")) {
      System.out.println("graph-support CLI Version: " + getVersionFromPom());
      System.out.println("Dependency Versions:");
      System.out.println("- antlr: " + getAntlrVersion());
      System.out.println("- batik: " + getBatikVersion());
      System.out.println("- fop:   " + getFopVersion());
      System.exit(1);
    } else if (arg.startsWith("-T")) {
      setFileType(arg.substring(2));
    } else if (arg.endsWith("-o")) {
      if (currentIdx == args.length - 1 || args[currentIdx + 1].startsWith("-")) {
        throw new WrongCommandException("-o option lack value");
      }
      this.output = parseFile(args[++currentIdx]);
    } else if (arg.startsWith("-")) {
      throw new WrongCommandException("Error: option " + arg + " unrecognized");
    } else {
      this.dotFile = parseFile(args[currentIdx]);
    }

    return ++currentIdx;
  }

  private File parseFile(String filePath) {
    File file = new File(filePath);
    File directory = file.getParentFile();
    if (directory == null) {
      // If the output file doesn't specify a directory, use the current directory
      file = new File("./" + filePath);
    }

    return file;
  }

  private void setFileType(String param) throws WrongCommandException {
    try {
      this.fileType = FileType.valueOf(param.toUpperCase());
    } catch (Exception e) {
      throw new WrongCommandException("Error: File type " + param + " not support yet");
    }
  }
}
