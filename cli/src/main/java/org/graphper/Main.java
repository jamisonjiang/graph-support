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
import java.nio.charset.Charset;
import org.graphper.parser.DotParser;

public class Main {

  public static void main(String[] args) {
    try {
      Command command = new Command(args);

      // This code uses DotParser from the graph-support-dot module
      DotParser dotParser = new DotParser(command.getDotFile(), Charset.defaultCharset());

      File output = command.getOutput();
      // Save the graph to the specified output file
      dotParser.parse().toFile(command.getFileType())
          .save(output.getParentFile().getAbsolutePath(), output.getName());

      System.out.println("File successfully saved");
    } catch (WrongCommandException e) {
      System.err.println(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
