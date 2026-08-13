/*
 * Copyright 2022 The graph-support project
 * Licensed under the Apache License, Version 2.0.
 */
package org.graphper.draw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DefaultGraphResourceTest {

  @TempDir
  Path outputDirectory;

  @Test
  public void saveRejectsTraversalAndWritesSimpleName() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    bytes.write("svg".getBytes(StandardCharsets.UTF_8));
    DefaultGraphResource resource = new DefaultGraphResource("graph", "svg", bytes);

    Assertions.assertThrows(IOException.class,
                            () -> resource.save(outputDirectory.toString(), "../escape"));
    Assertions.assertThrows(IOException.class,
                            () -> resource.save(outputDirectory.toString(), "..\\escape"));
    Assertions.assertThrows(IOException.class,
                            () -> resource.save(outputDirectory.toString(), "file.svg:stream"));
    resource.save(outputDirectory.toString(), "safe");
    Assertions.assertEquals("svg", new String(Files.readAllBytes(
        outputDirectory.resolve("safe.svg")), StandardCharsets.UTF_8));
  }
}
