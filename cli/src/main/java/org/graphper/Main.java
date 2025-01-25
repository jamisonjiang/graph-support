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

import static org.graphper.CommandUnits.COMMAND_UNITS;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.attributes.Layout;
import org.graphper.parser.DotParser;
import org.graphper.parser.PostGraphComponents;

public class Main {

  public static void main(String[] args) {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

    try {
      Command command = newCommand(args);
      DotParser dotParser = new DotParser(command.getDotFile(), StandardCharsets.UTF_8);
      File output = command.getOutput();
      dotParser.parse(new PostGraphComponents() {
            @Override
            public void postGraphviz(GraphvizBuilder graphvizBuilder) {
              Layout layout = command.getLayout();
              if (layout != null) {
                graphvizBuilder.layout(layout);
              }
            }
          })
          .toFile(command.getFileType())
          .save(output.getParentFile().getAbsolutePath(), output.getName());
    } catch (WrongCommandException e) {
      System.err.println(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Command newCommand(String[] args) throws WrongCommandException {
    Command command = new Command();
    Arguments arguments = new Arguments(args);
    while (arguments.currentExist()) {
      boolean handled = false;
      for (CommandUnit unit : COMMAND_UNITS) {
        if (unit.handle(arguments, command)) {
          handled = true;
          break;
        }
      }

      if (!handled) {
        throw new WrongCommandException("Error: Option " + arguments.current() + " unrecognized");
      }
      arguments.advance();
    }

    if (command.getDotFile() == null) {
      throw new WrongCommandException("Error: No dot file");
    }

    if (command.getOutput() == null) {
      throw new WrongCommandException("Error: No output file");
    }
    return command;
  }
}
