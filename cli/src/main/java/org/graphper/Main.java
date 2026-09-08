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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.graphper.CommandUnits.AllowImageHost;
import org.graphper.CommandUnits.ImageBaseDirectory;
import org.graphper.api.Graphviz;
import org.graphper.api.Graphviz.GraphvizBuilder;
import org.graphper.api.SecurityPolicy;
import org.graphper.api.attributes.Layout;
import org.graphper.parser.DotParser;
import org.graphper.parser.ParseException;
import org.graphper.parser.PostGraphComponents;
import org.graphper.ui.DotRenderService;
import org.graphper.ui.UiLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the graph-support-cli.
 *
 * @author Jamison Jiang
 */
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  /** The subset of options the {@code ui} sub-command accepts after the keyword. */
  private static final List<CommandUnit> UI_COMMAND_UNITS =
      Arrays.asList(new AllowImageHost(), new ImageBaseDirectory());

  public static void main(String[] args) {
    if (isUiCommand(args)) {
      launchUi(args);
      return;
    }
    try {
      Command command = newCommand(args);
      File output = command.getOutput();
      SecurityPolicy securityPolicy = command.getSecurityPolicy();
      Graphviz graphviz = DotParser.parse(command.getDotFile(), new PostGraphComponents() {
        @Override
        public void postGraphviz(GraphvizBuilder graphvizBuilder) {
          Layout layout = command.getLayout();
          if (layout != null) {
            graphvizBuilder.layout(layout);
          }
          graphvizBuilder.securityPolicy(securityPolicy);
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

  static boolean isUiCommand(String[] args) {
    return args != null && args.length >= 1
        && ("ui".equalsIgnoreCase(args[0]) || "--ui".equalsIgnoreCase(args[0]));
  }

  /**
   * Starts the desktop editor, honouring the image options that follow the {@code ui} keyword.
   * The editor lives in another module and builds its own renderer, so the policy travels as
   * system properties that {@link DotRenderService} reads when it is constructed.
   */
  private static void launchUi(String[] args) {
    try {
      Command command = uiCommand(args);
      Set<String> hosts = command.getAllowedImageHosts();
      if (!hosts.isEmpty()) {
        System.setProperty(DotRenderService.ALLOWED_IMAGE_HOSTS_PROPERTY,
                           String.join(",", hosts));
      }
      if (command.getImageBaseDirectory() != null) {
        System.setProperty(DotRenderService.IMAGE_BASE_DIRECTORY_PROPERTY,
                           command.getImageBaseDirectory().toString());
      }
      // Fail fast on a bad host before opening a window that would silently ignore it.
      command.getSecurityPolicy();
    } catch (WrongCommandException | IllegalArgumentException e) {
      log.error("Command error: {}", e.getMessage());
      return;
    }
    UiLauncher.launch();
  }

  private static Command uiCommand(String[] args) throws WrongCommandException {
    Command command = new Command();
    Arguments arguments = new Arguments(Arrays.copyOfRange(args, 1, args.length));
    while (arguments.currentExist()) {
      boolean handled = false;
      for (CommandUnit unit : UI_COMMAND_UNITS) {
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
    return command;
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
