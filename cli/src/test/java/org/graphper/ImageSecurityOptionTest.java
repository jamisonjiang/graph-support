package org.graphper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.graphper.CommandUnits.AllowImageHost;
import org.graphper.CommandUnits.ImageBaseDirectory;
import org.graphper.api.SecurityPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The CLI is the shipped entry point, so it needs a way to opt in to images without weakening the
 * default. These cases pin that surface.
 */
class ImageSecurityOptionTest {

  private static final List<CommandUnit> UNITS =
      Arrays.asList(new AllowImageHost(), new ImageBaseDirectory());

  @Test
  void withoutOptionsTheDefaultPolicyIsUsedUnchanged() throws Exception {
    assertSame(SecurityPolicy.defaultPolicy(), parse().getSecurityPolicy());
  }

  @Test
  void allowImageHostEnablesExactlyTheNamedHosts() throws Exception {
    SecurityPolicy policy = parse("--allow-image-host", "images.example.com",
                                  "--allow-image-host=cdn.example.com").getSecurityPolicy();

    assertTrue(policy.isAllowRemoteImages());
    assertEquals(2, policy.getAllowedRemoteImageHosts().size());
    assertTrue(policy.getAllowedRemoteImageHosts().contains("images.example.com"));
    assertTrue(policy.getAllowedRemoteImageHosts().contains("cdn.example.com"));
    assertNotNull(policy.sanitizeImage("https://images.example.com/a.png"));
    assertNull(policy.sanitizeImage("https://other.example.com/a.png"));
    assertNull(policy.getLocalImageBaseDirectory());
  }

  @Test
  void hostsAreNormalizedAndSubdomainsAreNotImplied() throws Exception {
    SecurityPolicy policy = parse("--allow-image-host", "Images.EXAMPLE.com").getSecurityPolicy();

    assertTrue(policy.getAllowedRemoteImageHosts().contains("images.example.com"));
    assertNull(policy.sanitizeImage("https://deep.images.example.com/a.png"));
  }

  @Test
  void imageDirEnablesOnlyThatDirectory(@TempDir Path directory) throws Exception {
    SecurityPolicy policy = parse("--image-dir", directory.toString()).getSecurityPolicy();

    assertEquals(canonical(directory), policy.getLocalImageBaseDirectory());
    assertFalse(policy.isAllowRemoteImages());
    assertNull(policy.sanitizeImage("https://images.example.com/a.png"));
  }

  @Test
  void bothOptionsCombine(@TempDir Path directory) throws Exception {
    SecurityPolicy policy = parse("--allow-image-host", "images.example.com",
                                  "--image-dir", directory.toString()).getSecurityPolicy();

    assertTrue(policy.isAllowRemoteImages());
    assertEquals(canonical(directory), policy.getLocalImageBaseDirectory());
  }

  /** The CLI canonicalizes paths, so {@code /var/...} becomes {@code /private/var/...} on macOS. */
  private static Path canonical(Path directory) throws Exception {
    return directory.toFile().getCanonicalFile().toPath();
  }

  @Test
  void aMissingOrInvalidValueIsReportedAsACommandError(@TempDir Path directory) {
    assertThrows(WrongCommandException.class, () -> parse("--allow-image-host"));
    assertThrows(WrongCommandException.class, () -> parse("--allow-image-host="));
    assertThrows(WrongCommandException.class,
                 () -> parse("--allow-image-host", "https://images.example.com"));
    assertThrows(WrongCommandException.class,
                 () -> parse("--allow-image-host", "images.example.com:8080"));
    assertThrows(WrongCommandException.class, () -> parse("--image-dir"));
    assertThrows(WrongCommandException.class,
                 () -> parse("--image-dir", directory.resolve("absent").toString()));
  }

  @Test
  void theOptionsAreRegisteredAndDocumentedInHelp() {
    boolean host = false;
    boolean dir = false;
    for (CommandUnit unit : CommandUnits.COMMAND_UNITS) {
      String help = unit.helpCommend();
      host |= help != null && help.contains("--allow-image-host");
      dir |= help != null && help.contains("--image-dir");
    }
    assertTrue(host, "--allow-image-host must appear in -h output");
    assertTrue(dir, "--image-dir must appear in -h output");
  }

  @Test
  void endToEndTheImageDirOptionReachesTheRender(@TempDir Path directory) throws Exception {
    Files.write(directory.resolve("logo.png"), new byte[]{1, 2, 3});
    Path output = directory.resolve("with-dir.svg");

    Main.main(new String[]{"-s", "digraph { a [image=\"logo.png\"]; }",
        "-o", output.toString(), "--image-dir", directory.toString()});

    assertTrue(read(output).contains("<image"), "the allowed image should be emitted");
  }

  @Test
  void endToEndWithoutTheOptionTheImageIsStillDropped(@TempDir Path directory) throws Exception {
    Files.write(directory.resolve("logo.png"), new byte[]{1, 2, 3});
    Path output = directory.resolve("no-dir.svg");

    Main.main(new String[]{"-s", "digraph { a [image=\"logo.png\"]; }",
        "-o", output.toString()});

    assertFalse(read(output).contains("<image"), "the default must stay closed");
  }

  private static String read(Path output) throws Exception {
    return new String(Files.readAllBytes(output), StandardCharsets.UTF_8);
  }

  @Test
  void uiKeywordStillAcceptsTheSameOptions() {
    assertTrue(Main.isUiCommand(new String[]{"ui"}));
    assertTrue(Main.isUiCommand(new String[]{"ui", "--allow-image-host", "images.example.com"}));
    assertFalse(Main.isUiCommand(new String[]{"a.dot", "-o", "a.svg"}));
  }

  private static Command parse(String... args) throws WrongCommandException {
    Command command = new Command();
    Arguments arguments = new Arguments(args);
    while (arguments.currentExist()) {
      boolean handled = false;
      for (CommandUnit unit : UNITS) {
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
}
