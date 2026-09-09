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
package org.graphper.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SecurityPolicyTest {

  @TempDir
  Path temporaryDirectory;

  @Test
  public void defaultPolicyRejectsDangerousLinksAndExternalImages() {
    SecurityPolicy policy = SecurityPolicy.defaultPolicy();
    Assertions.assertNull(policy.sanitizeLink("javascript:alert(1)"));
    Assertions.assertNull(policy.sanitizeLink("data:text/html,boom"));
    Assertions.assertNull(policy.sanitizeLink("//example.com/network-path"));
    Assertions.assertNull(policy.sanitizeLink("https://user:secret@example.com/"));
    Assertions.assertEquals("https://example.com/a", policy.sanitizeLink("https://example.com/a"));
    Assertions.assertEquals("#local", policy.sanitizeLink("#local"));
    Assertions.assertNull(policy.sanitizeImage("http://127.0.0.1/a.png"));
    Assertions.assertNull(policy.sanitizeImage("../../secret.png"));
  }

  @Test
  public void localImagesAreRestrictedToConfiguredRealDirectory() throws Exception {
    Path image = Files.write(temporaryDirectory.resolve("image.png"), new byte[]{1});
    Path outside = Files.createTempFile("graph-support-outside", ".png");
    try {
      SecurityPolicy policy = SecurityPolicy.builder()
          .localImageBaseDirectory(temporaryDirectory).build();
      Assertions.assertEquals(image.toRealPath().toUri().toString(),
                              policy.sanitizeImage("image.png"));
      Assertions.assertNull(policy.sanitizeImage(outside.toString()));
    } finally {
      Files.deleteIfExists(outside);
    }
  }

  @Test
  public void fileStringAndDesktopPathBuildersAgree() {
    File directory = temporaryDirectory.resolve("child/../images").toFile();
    SecurityPolicy policy = SecurityPolicy.builder().localImageBaseDirectory(directory).build();
    Assertions.assertEquals(temporaryDirectory.resolve("images").toAbsolutePath().toFile(),
                            policy.getLocalImageBaseDirectoryFile());
    Assertions.assertEquals(policy.getLocalImageBaseDirectoryFile(),
                            policy.getLocalImageBaseDirectory());
    Assertions.assertEquals(policy, SecurityPolicy.builder()
        .localImageBaseDirectory(directory.getPath()).build());
    Assertions.assertEquals(policy, SecurityPolicy.builder()
        .localImageBaseDirectory(directory.toPath()).build());
    Assertions.assertEquals(policy, SecurityPolicy.builder()
        .localImageBaseDirectory((Object) directory).build());
    Assertions.assertThrows(IllegalArgumentException.class,
                            () -> SecurityPolicy.builder().localImageBaseDirectory(new Object()));
    Assertions.assertThrows(IllegalArgumentException.class,
                            () -> SecurityPolicy.builder().localImageBaseDirectory("bad\0path"));
  }

  @Test
  public void localReferencesRejectMissingFilesTraversalAndSiblingPrefixes() throws Exception {
    Path base = Files.createDirectory(temporaryDirectory.resolve("base"));
    Path sibling = Files.createDirectory(temporaryDirectory.resolve("base-other"));
    Path outside = Files.write(sibling.resolve("outside.png"), new byte[]{1});
    Path image = Files.write(base.resolve("image with space.png"), new byte[]{1});
    SecurityPolicy policy = SecurityPolicy.builder().localImageBaseDirectory(base.toFile()).build();
    Assertions.assertNull(policy.sanitizeImage("missing.png"));
    Assertions.assertNull(policy.sanitizeImage("../base-other/outside.png"));
    Assertions.assertNull(policy.sanitizeImage(outside.toString()));
    Assertions.assertNull(policy.sanitizeImage(outside.toUri().toString()));
    Assertions.assertEquals(image.toRealPath().toUri().toString(),
                            policy.sanitizeImage(image.toUri().toString()));
  }

  @Test
  public void localReferencesRejectSymlinksOutsideBase() throws Exception {
    Path base = Files.createDirectory(temporaryDirectory.resolve("base"));
    Path outside = Files.write(temporaryDirectory.resolve("outside.png"), new byte[]{1});
    try {
      Files.createSymbolicLink(base.resolve("link.png"), outside);
      Files.createSymbolicLink(base.resolve("dangling.png"),
                               temporaryDirectory.resolve("missing.png"));
    } catch (IOException | UnsupportedOperationException | SecurityException e) {
      Assumptions.assumeTrue(false, "Symbolic links unavailable: " + e);
    }
    SecurityPolicy policy = SecurityPolicy.builder().localImageBaseDirectory(base.toFile()).build();
    Assertions.assertNull(policy.sanitizeImage("link.png"));
    Assertions.assertNull(policy.sanitizeImage("dangling.png"));
  }

  @Test
  public void remoteImagesRequireBothOptInAndExplicitHost() {
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("images.example.com").build();
    Assertions.assertEquals("https://images.example.com/a.png",
                            policy.sanitizeImage("https://images.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://other.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://user:secret@images.example.com/a.png"));

    SecurityPolicy hostOnly = SecurityPolicy.builder()
        .allowRemoteImageHost("images.example.com").build();
    Assertions.assertNull(hostOnly.sanitizeImage("https://images.example.com/a.png"));
  }

  @Test
  public void allowListedHostIsMatchedExactlyAndCaseInsensitively() {
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("Images.EXAMPLE.com").build();
    Assertions.assertEquals("https://IMAGES.Example.CoM/a.png",
                            policy.sanitizeImage("https://IMAGES.Example.CoM/a.png"));
    // Neither a prefix, a suffix nor a deeper label is implied by the entry.
    Assertions.assertNull(policy.sanitizeImage("https://evil-images.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://images.example.com.attacker.test/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://cdn.images.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://images.example.com./a.png"));
  }

  @Test
  public void nonHttpRemoteSchemesAndControlCharactersAreDenied() {
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("images.example.com").build();
    Assertions.assertNull(policy.sanitizeImage("ftp://images.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("jar:https://images.example.com/a.jar!/a.png"));
    Assertions.assertNull(policy.sanitizeImage("gopher://images.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://images.example.com/a\r\nHost: x.png"));
    Assertions.assertNull(policy.sanitizeImage("  "));
    Assertions.assertNull(policy.sanitizeImage(null));
    // A file reference is still denied while no base directory is configured.
    Assertions.assertNull(policy.sanitizeImage("file:///etc/passwd"));
  }

  @Test
  public void allowListedHostMustBeAPlainDnsName() {
    SecurityPolicy.Builder builder = SecurityPolicy.builder();
    for (String host : new String[]{"images.example.com:8443", "http://images.example.com",
        "images.example.com/", ".images.example.com", "images.example.com.", "images..example.com",
        "[::1]", "user@images.example.com", ""}) {
      Assertions.assertThrows(IllegalArgumentException.class,
                              () -> builder.allowRemoteImageHost(host), host);
    }
  }

  @Test
  public void embeddedImageIsAcceptedOnlyWhileItFitsTheByteLimit() {
    SecurityPolicy policy = SecurityPolicy.builder().maxImageBytes(16).build();
    StringBuilder large = new StringBuilder("data:image/png;base64,");
    for (int i = 0; i < 64; i++) {
      large.append("AAAA");
    }
    Assertions.assertNull(policy.sanitizeImage(large.toString()));
    Assertions.assertNotNull(policy.sanitizeImage("data:image/png;base64,AAAAAAAAAAAAAAAA"));
    Assertions.assertNull(policy.sanitizeImage("data:text/html;base64,AAAA"));
    Assertions.assertNull(policy.sanitizeImage("data:image/svg+xml;base64,AAAA"));
  }

  @Test
  public void validPoliciesRetainTheirSerializedFormAndInvariants() throws Exception {
    Assertions.assertEquals(1023362078276416241L,
                            ObjectStreamClass.lookup(SecurityPolicy.class).getSerialVersionUID());
    SecurityPolicy custom = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("Images.EXAMPLE.com").localImageBaseDirectory(temporaryDirectory)
        .connectTimeoutMillis(123).readTimeoutMillis(456).maxImageBytes(789)
        .maxImagePixels(1234).maxOutputPixels(5678).build();
    for (SecurityPolicy policy : new SecurityPolicy[]{SecurityPolicy.defaultPolicy(), custom}) {
      SecurityPolicy restored = (SecurityPolicy) deserialize(serialize(policy));
      Assertions.assertEquals(policy, restored);
      Assertions.assertEquals(policy.hashCode(), restored.hashCode());
      Assertions.assertThrows(UnsupportedOperationException.class,
                              () -> restored.getAllowedRemoteImageHosts().add("other.test"));
    }
  }

  @Test
  public void deserializationRejectsNonPositiveLimits() throws Exception {
    for (String name : new String[]{"connectTimeoutMillis", "readTimeoutMillis", "maxImageBytes",
        "maxImagePixels", "maxOutputPixels"}) {
      for (int invalid : new int[]{0, -1}) {
        SecurityPolicy policy = SecurityPolicy.builder().build();
        Field field = SecurityPolicy.class.getDeclaredField(name);
        field.setAccessible(true);
        if (field.getType() == long.class) {
          field.setLong(policy, invalid);
        } else {
          field.setInt(policy, invalid);
        }
        byte[] bytes = serialize(policy);
        Assertions.assertThrows(InvalidObjectException.class, () -> deserialize(bytes), name);
      }
    }
  }

  @Test
  public void deserializationRejectsInvalidHostsAndLocalBase() throws Exception {
    for (Object invalidHosts : new Object[]{null, Collections.singleton(null),
        Collections.singleton(123), Collections.singleton("images.example.com:443"),
        Collections.singleton("Images.EXAMPLE.com"), Collections.singleton(" images.example.com"),
        Collections.singleton("images..example.com"), Collections.singleton("")}) {
      SecurityPolicy policy = SecurityPolicy.builder().build();
      Field field = SecurityPolicy.class.getDeclaredField("allowedRemoteImageHosts");
      field.setAccessible(true);
      field.set(policy, invalidHosts);
      byte[] bytes = serialize(policy);
      Assertions.assertThrows(InvalidObjectException.class, () -> deserialize(bytes));
    }
    for (String invalidBase : new String[]{"relative", "", "/tmp/../tmp", "\000"}) {
      SecurityPolicy policy = SecurityPolicy.builder().build();
      Field field = SecurityPolicy.class.getDeclaredField("localImageBaseDirectory");
      field.setAccessible(true);
      field.set(policy, invalidBase);
      byte[] bytes = serialize(policy);
      Assertions.assertThrows(InvalidObjectException.class, () -> deserialize(bytes), invalidBase);
    }
  }

  @Test
  public void deserializationCopiesAnAliasedHostSet() throws Exception {
    Set<String> hosts = new LinkedHashSet<>(Collections.singleton("images.example.com"));
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true).build();
    Field field = SecurityPolicy.class.getDeclaredField("allowedRemoteImageHosts");
    field.setAccessible(true);
    field.set(policy, hosts);
    Object[] restored = (Object[]) deserialize(serialize(new Object[]{policy, hosts}));
    ((Set<?>) restored[1]).clear();
    SecurityPolicy restoredPolicy = (SecurityPolicy) restored[0];
    Assertions.assertEquals(Collections.singleton("images.example.com"),
                            restoredPolicy.getAllowedRemoteImageHosts());
    Assertions.assertThrows(UnsupportedOperationException.class,
                            () -> restoredPolicy.getAllowedRemoteImageHosts().clear());
  }

  private static byte[] serialize(Object value) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
      output.writeObject(value);
    }
    return bytes.toByteArray();
  }

  private static Object deserialize(byte[] bytes) throws Exception {
    try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return input.readObject();
    }
  }

  @Test
  public void svgOmitsUnsafeHrefAndExternalImageByDefault() throws Exception {
    Node node = Node.builder().label("unsafe").href("javascript:alert(1)")
        .image("http://127.0.0.1/metadata.png").build();
    String svg = Graphviz.digraph().addNode(node).build().toSvgStr();
    Assertions.assertFalse(svg.contains("javascript:"));
    Assertions.assertFalse(svg.contains("127.0.0.1"));
  }

  @Test
  public void boundedDataImageRendersThroughSecureRasterLoader() throws Exception {
    String png = "data:image/png;base64,"
        + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUB"
        + "AScY42YAAAAASUVORK5CYII=";
    Node node = Node.builder().image(png).imageSize(1, 1).build();
    try (GraphResource resource = Graphviz.digraph().addNode(node).build().toFile(FileType.PNG)) {
      Assertions.assertTrue(resource.bytes().length > 0);
    }
  }
}
