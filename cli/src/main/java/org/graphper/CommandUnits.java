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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.FileType;
import org.graphper.api.SecurityPolicy;
import org.graphper.api.attributes.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CommandUnit} registration center.
 *
 * @author Jamison Jiang
 */
public class CommandUnits {

  private static final Logger log = LoggerFactory.getLogger(CommandUnits.class);

  private CommandUnits() {
  }

  protected static final List<CommandUnit> COMMAND_UNITS = Arrays.asList(
      new Help(),
      new Debug(),
      new Script(),
      new Version(),
      new DotInput(),
      new OutputFile(),
      new GraphLayout(),
      new OutputFileType(),
      new NativeImageRender(),
      new AllowImageHost(),
      new ImageBaseDirectory()
  );

  /** Reads a positional DOT input file. */
  public static class DotInput implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String arg = arguments.current();
      if (arg == null || arg.startsWith("-")) {
        return false;
      }

      File input = parseFile(arg);
      if (!input.exists()) {
        throw new WrongCommandException("Error: dot file " + input.getPath() + " not exists");
      }
      command.setDotFile(getCharStream(input));
      return true;
    }

    @Override
    public String helpCommend() {
      return null;
    }
  }

  /** Selects the output file path. */
  public static class OutputFile implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String key = arguments.current();
      if (!"-o".equals(key) && !"--output".equals(key)) {
        return false;
      }

      String value = arguments.advance();
      if (value == null) {
        throw new WrongCommandException("Error: don't have output path");
      }
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
      return "-oFile       - Write output to 'file'";
    }
  }

  /** Selects the graph layout engine. */
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

  /** Selects the output format. */
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

  /** Enables the native image renderer instead of Batik. */
  public static class NativeImageRender implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) {
      String arg = arguments.current();
      if (!"-n".equals(arg)) {
        return false;
      }
      System.setProperty("use.local.img.converter", "true");
      return true;
    }

    @Override
    public String helpCommend() {
      return "-n           - Use native image render (ignore Batik)";
    }
  }

  /**
   * Opt-in for remote images. Images are denied by default and this option is the only way to
   * enable them from the CLI; each occurrence adds one exact hostname to the allow list.
   */
  public static class AllowImageHost implements CommandUnit {

    static final String OPTION = "--allow-image-host";

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String host = optionValue(arguments, OPTION, "Error: don't have image host");
      if (host == null) {
        return false;
      }
      try {
        // Reject a bad hostname here rather than after the graph has already been parsed.
        SecurityPolicy.builder().allowRemoteImageHost(host);
      } catch (RuntimeException e) {
        throw new WrongCommandException("Error: image host " + host + " invalid, "
                                            + e.getMessage());
      }
      command.addAllowedImageHost(host);
      return true;
    }

    @Override
    public String helpCommend() {
      return "--allow-image-host h"
          + "\n             - Allow images from host 'h' (repeatable; remote images are denied"
          + " by default)";
    }
  }

  /**
   * Opt-in for filesystem images. Without it no local image reference resolves, because the
   * default policy has no base directory.
   */
  public static class ImageBaseDirectory implements CommandUnit {

    static final String OPTION = "--image-dir";

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String value = optionValue(arguments, OPTION, "Error: don't have image directory");
      if (value == null) {
        return false;
      }
      File directory = parseFile(value);
      if (!directory.isDirectory()) {
        throw new WrongCommandException(
            "Error: image directory " + directory.getPath() + " not exists");
      }
      command.setImageBaseDirectory(directory.toPath());
      return true;
    }

    @Override
    public String helpCommend() {
      return "--image-dir d"
          + "\n             - Allow images under directory 'd' (filesystem images are denied"
          + " by default)";
    }
  }

  /** Prints CLI and dependency versions, then exits. */
  public static class Version implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) {
      String arg = arguments.current();
      if (!"-v".equals(arg) && !"--version".equals(arg)) {
        return false;
      }

      log.info("graph-support CLI Version: {}", getVersionFromPom());
      log.info("Dependency Versions:");
      log.info("- antlr: {}", getAntlrVersion());
      log.info("- batik: {}", getBatikVersion());
      log.info("- fop:   {}", getFopVersion());
      System.exit(1);
      return true;
    }

    @Override
    public String helpCommend() {
      return null;
    }
  }

  /** Prints the available commands, then exits. */
  public static class Help implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) {
      String arg = arguments.current();
      if (!"-h".equals(arg) && !"--help".equals(arg)) {
        return false;
      }

      for (CommandUnit unit : COMMAND_UNITS) {
        String helpCommand = unit.helpCommend();
        if (StringUtils.isEmpty(helpCommand)) {
          continue;
        }
        log.info(helpCommand);
      }
      log.info("ui           - Open the desktop DOT editor and SVG preview");

      System.exit(1);
      return true;
    }

    @Override
    public String helpCommend() {
      return null;
    }
  }

  /** Enables debug logging for graph-support. */
  public static class Debug implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) {
      String arg = arguments.current();
      if (!"-d".equals(arg) && !"--debug".equals(arg)) {
        return false;
      }

      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      loggerContext.getLogger("org.graphper").setLevel(Level.DEBUG);
      return true;
    }

    @Override
    public String helpCommend() {
      return "-d           - Print debug info";
    }
  }

  /** Reads DOT source directly from a command-line argument. */
  public static class Script implements CommandUnit {

    @Override
    public boolean handle(Arguments arguments, Command command) throws WrongCommandException {
      String arg = arguments.current();
      if (!"-s".equals(arg) && !"--script".equals(arg)) {
        return false;
      }

      String script = arguments.advance();
      if (script == null) {
        throw new WrongCommandException("Error: don't have dot script");
      }
      command.setDotFile(CharStreams.fromString(script));
      return true;
    }

    @Override
    public String helpCommend() {
      return "-sScript     - Provide a DOT script string to generate a graph (instead of a file)";
    }
  }

  /**
   * Reads the value of a long option written either as {@code --opt value} or {@code --opt=value}.
   *
   * @param arguments   argument cursor, advanced past the value for the separated form
   * @param option      the option name
   * @param missingText error text when the option is present but the value is not
   * @return the value, or {@code null} when the current argument is not this option
   * @throws WrongCommandException if the option is present without a value
   */
  private static String optionValue(Arguments arguments, String option, String missingText)
      throws WrongCommandException {
    String arg = arguments.current();
    if (arg == null) {
      return null;
    }
    String value;
    if (option.equals(arg)) {
      value = arguments.advance();
    } else if (arg.startsWith(option + "=")) {
      value = arg.substring(option.length() + 1);
    } else {
      return null;
    }
    if (StringUtils.isBlank(value)) {
      throw new WrongCommandException(missingText);
    }
    return value.trim();
  }

  private static File parseFile(String filePath) {
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

  private static CharStream getCharStream(File file) throws WrongCommandException {
    CharStream charStream;
    try (InputStream is = Files.newInputStream(file.toPath());
        Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
      charStream = CharStreams.fromReader(r, file.getName());
    } catch (IOException e) {
      throw new WrongCommandException(e);
    }
    return charStream;
  }
}
