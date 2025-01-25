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

package dot;

import helper.DocumentUtils;
import helper.GraphvizVisual;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.graphper.parser.DotParser;
import org.junit.jupiter.api.Test;

public class DotCases extends GraphvizVisual {

  @Test
  public void testDot() throws IOException {
    String directoryPath = DocumentUtils.getDotCasesPath();

    File directory = new File(directoryPath);
    if (directory.exists() && directory.isDirectory()) {
      listFilesRecursive(directory);
    } else {
      System.out.println("The specified path is not a directory or does not exist.");
    }
  }

  private void listFilesRecursive(File directory) throws IOException {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          dotParse(file);
        } else if (file.isDirectory()) {
          listFilesRecursive(file);
        }
      }
    } else {
      System.out.println("Could not access directory: " + directory.getAbsolutePath());
    }
  }

  private void dotParse(File file) throws IOException {
    if (!file.getName().endsWith(".dot")) {
      return;
    }
    try {
      DotParser dotParser = new DotParser(file, StandardCharsets.UTF_8);
      visual(dotParser.parse());
    } catch (Exception e) {
      System.err.println("File " + file.getName() + " error");
      throw e;
    }
  }
}
