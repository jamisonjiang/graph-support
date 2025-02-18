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
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.attributes.Layout;
import org.graphper.parser.DotParser;
import org.graphper.parser.ParseException;
import org.graphper.parser.PostGraphComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the graph-support-cli.
 *
 * @author Jamison Jiang
 */
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    try {
      Command command = newCommand(args);
      File output = command.getOutput();
      Graphviz graphviz = DotParser.parse(command.getDotFile(), new PostGraphComponents() {
        @Override
        public void postGraphviz(GraphvizBuilder graphvizBuilder) {
          Layout layout = command.getLayout();
          if (layout != null) {
            graphvizBuilder.layout(layout);
          }
        }
      });

      if (graphviz.isEmpty()) {
        log.error("Graph is empty");
        return;
      }

      graphviz
          .toFile(command.getFileType())
          .save(output.getParentFile().getAbsolutePath(), output.getName());
    } catch (StackOverflowError e) {
      log.warn("Big graph, please increasing the stack size (e.g., java -Xss2024m -jar graph-support-cli.jar xxx).");
    }catch (ParseException e) {
      log.error("Parse script error: {}", e.getMessage());
    } catch (WrongCommandException e) {
      log.error("Command error: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Generate error:", e);
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
      throw new WrongCommandException("Error: No dot file/script");
    }

    if (command.getOutput() == null) {
      throw new WrongCommandException("Error: No output file");
    }
    return command;
  }
}
