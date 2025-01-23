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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.FileType;
import org.graphper.api.attributes.Layout;

public class CommandUnits {

  private CommandUnits() {
  }

  protected static final List<CommandUnit> COMMAND_UNITS = Arrays.asList(
      new Help(),
      new Version(),
      new DotInput(),
      new OutputFile(),
      new GraphLayout(),
      new OutputFileType()
  );

  public static class DotInput implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String arg = arguments.current();
      if (arg == null || arg.startsWith("-")) {
        return false;
      }

      File input = parseFile(arg);
      if (!input.exists()) {
        throw new WrongCommandException("Error: dot file " + input.getPath() +" not exists");
      }
      command.setDotFile(input);
      return true;
    }

    @Override
    public String helpCommend() {
      return null;
    }
  }

  public static class OutputFile implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String key = arguments.current();
      if (!"-o".equals(key) && !"--output".equals(key)) {
        return false;
      }

      String value = arguments.advance();
      File output = parseFile(value);
      File parentFile = output.getParentFile();
      if (parentFile == null) {
        throw new WrongCommandException("Error: output path not exists");
      }
      if (!parentFile.exists()) {
        throw new WrongCommandException(
            "Error: output path " + parentFile.getAbsolutePath() + " not exists");
      }
      command.setOutput(output);
      return true;
    }

    @Override
    public String helpCommend() {
      return "-ofile       - Write output to 'file'";
    }
  }

  public static class GraphLayout implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String arg = arguments.current();
      if (arg == null || !arg.startsWith("-K")) {
        return false;
      }

      if (arg.length() == 2) {
        throw new WrongCommandException("Error: Empty layout option -K");
      }

      try {
        Layout layout = Layout.valueOf(arg.substring(2).toUpperCase());
        command.setLayout(layout);
      } catch (IllegalArgumentException e) {
        throw new WrongCommandException("Error: Layout " + arg + " unrecognized");
      }
      return true;
    }

    @Override
    public String helpCommend() {
      String layouts = Stream.of(Layout.values())
          .map(Layout::name)
          .map(String::toLowerCase)
          .collect(Collectors.joining("|"));
      return "-Kv          - Set layout engine to 'v' (" + layouts + ")";
    }
  }

  public static class OutputFileType implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String arg = arguments.current();
      if (arg == null || !arg.startsWith("-T")) {
        return false;
      }

      try {
        FileType fileType = FileType.valueOf(arg.substring(2).toUpperCase());
        command.setFileType(fileType);
      } catch (Exception e) {
        throw new WrongCommandException("Error: File type " + arg + " not support yet");
      }
      return true;
    }

    @Override
    public String helpCommend() {
      String fileTypes = Stream.of(FileType.values())
          .map(FileType::name)
          .map(String::toLowerCase)
          .collect(Collectors.joining("|"));
      return "-Tv          - Set output format to 'v' (" + fileTypes + ")";
    }
  }

  public static class Version implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) {
      String arg = arguments.current();
      if (!"-v".equals(arg) && !"--version".equals(arg)) {
        return false;
      }

      System.out.println("graph-support CLI Version: " + getVersionFromPom());
      System.out.println("Dependency Versions:");
      System.out.println("- antlr: " + getAntlrVersion());
      System.out.println("- batik: " + getBatikVersion());
      System.out.println("- fop:   " + getFopVersion());
      System.exit(1);
      return true;
    }

    @Override
    public String helpCommend() {
      return null;
    }
  }

  public static class Help implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) {
      String arg = arguments.current();
      if (!"-h".equals(arg) && !"--help".equals(arg)) {
        return false;
      }

      for (CommandUnit unit : COMMAND_UNITS) {
        String helpCommend = unit.helpCommend();
        if (StringUtils.isEmpty(helpCommend)) {
          continue;
        }
        System.out.println(helpCommend);
      }

      System.exit(1);
      return true;
    }

    @Override
    public String helpCommend() {
      return null;
    }
  }

  public static File parseFile(String filePath) {
    File file = new File(filePath);
    File directory = file.getParentFile();
    if (directory == null) {
      file = new File("./" + filePath);
    }

    try {
      return file.getCanonicalFile();
    } catch (IOException e) {
      return file;
    }
  }
}
