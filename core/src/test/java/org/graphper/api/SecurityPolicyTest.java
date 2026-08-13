/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.api;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
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
  public void remoteImagesRequireBothOptInAndExplicitHost() {
    SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
        .allowRemoteImageHost("images.example.com").build();
    Assertions.assertEquals("https://images.example.com/a.png",
                            policy.sanitizeImage("https://images.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://other.example.com/a.png"));
    Assertions.assertNull(policy.sanitizeImage("https://user:secret@images.example.com/a.png"));
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
