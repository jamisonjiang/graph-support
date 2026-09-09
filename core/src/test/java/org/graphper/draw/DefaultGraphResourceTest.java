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
package org.graphper.draw;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
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

  @Test
  public void saveRejectsExistingAndDanglingSymlinks() throws Exception {
    Path outside = Files.write(outputDirectory.resolve("outside.svg"), new byte[]{9});
    Path missing = outputDirectory.resolve("missing.svg");
    try {
      Files.createSymbolicLink(outputDirectory.resolve("existing.svg"), outside);
      Files.createSymbolicLink(outputDirectory.resolve("dangling.svg"), missing);
    } catch (IOException | UnsupportedOperationException | SecurityException e) {
      Assumptions.assumeTrue(false, "Symbolic links unavailable: " + e);
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    bytes.write(1);
    try (DefaultGraphResource resource = new DefaultGraphResource("graph", "svg", bytes)) {
      for (String name : new String[]{"existing", "dangling"}) {
        Assertions.assertThrows(IOException.class,
                                () -> resource.save(outputDirectory.toString(), name));
      }
    }
    Assertions.assertArrayEquals(new byte[]{9}, Files.readAllBytes(outside));
    Assertions.assertFalse(Files.exists(missing));
  }

  @Test
  public void saveOverwritesRegularFilesWithoutDuplicatingSuffix() throws Exception {
    Files.write(outputDirectory.resolve("safe.svg"), new byte[]{9, 9, 9});
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    bytes.write(1);
    try (DefaultGraphResource resource = new DefaultGraphResource("safe", "svg", bytes)) {
      resource.save(outputDirectory.toString(), "safe.svg");
      Assertions.assertArrayEquals(new byte[]{1},
                                   Files.readAllBytes(outputDirectory.resolve("safe.svg")));
      resource.save(outputDirectory.toString(), null);
      Assertions.assertFalse(Files.exists(outputDirectory.resolve("safe.svg.svg")));
    }
  }

  @Test
  public void saveRejectsTraversalInSuffix() {
    DefaultGraphResource resource = new DefaultGraphResource("graph", "svg/../../escape",
                                                              new ByteArrayOutputStream());
    Assertions.assertThrows(IOException.class,
                            () -> resource.save(outputDirectory.toString(), "safe"));
  }

  @Test
  public void basicResourcesAndPolicyWorkWithoutNioFileClasses() throws Exception {
    for (boolean hideHelper : new boolean[]{false, true}) {
      ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
          if (name.startsWith("java.nio.file.")
              || (hideHelper && name.equals("org.graphper.draw.NioGraphResourceSaver"))) {
            throw new ClassNotFoundException(name);
          }
          if (!name.startsWith("org.graphper.api.SecurityPolicy")
              && !name.equals("org.graphper.draw.DefaultGraphResource")
              && !name.equals("org.graphper.draw.NioGraphResourceSaver")) {
            return super.loadClass(name, resolve);
          }
          Class<?> loaded = findLoadedClass(name);
          if (loaded == null) {
            try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
              if (input == null) {
                throw new ClassNotFoundException(name);
              }
              ByteArrayOutputStream bytes = new ByteArrayOutputStream();
              byte[] buffer = new byte[4096];
              int count;
              while ((count = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
              }
              byte[] content = bytes.toByteArray();
              loaded = defineClass(name, content, 0, content.length);
            } catch (IOException e) {
              throw new ClassNotFoundException(name, e);
            }
          }
          if (resolve) {
            resolveClass(loaded);
          }
          return loaded;
        }
      };
      Class<?> policyType = loader.loadClass("org.graphper.api.SecurityPolicy");
      Object policy = policyType.getMethod("defaultPolicy").invoke(null);
      Assertions.assertEquals("https://example.com", policyType.getMethod("sanitizeLink", String.class)
          .invoke(policy, "https://example.com"));
      Assertions.assertEquals("data:image/png;base64,AAAA",
                              policyType.getMethod("sanitizeImage", String.class)
                                  .invoke(policy, "data:image/png;base64,AAAA"));
      Object builder = policyType.getMethod("builder").invoke(null);
      builder.getClass().getMethod("localImageBaseDirectory", File.class)
          .invoke(builder, outputDirectory.toFile());
      Object localPolicy = builder.getClass().getMethod("build").invoke(builder);
      Assertions.assertEquals(outputDirectory.toFile(),
                              policyType.getMethod("getLocalImageBaseDirectoryFile")
                                  .invoke(localPolicy));
      Path image = Files.write(outputDirectory.resolve("image.png"), new byte[]{1});
      Assertions.assertEquals(image.toRealPath().toUri().toString(),
                              policyType.getMethod("sanitizeImage", String.class)
                                  .invoke(localPolicy, "image.png"));

      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      bytes.write(7);
      Class<?> resourceType = loader.loadClass("org.graphper.draw.DefaultGraphResource");
      Object resource = resourceType.getConstructor(String.class, String.class,
                                                    ByteArrayOutputStream.class)
          .newInstance("graph", "svg", bytes);
      Method save = resourceType.getMethod("save", String.class, String.class);
      InvocationTargetException failure = Assertions.assertThrows(InvocationTargetException.class,
          () -> save.invoke(resource, outputDirectory.toString(), "unavailable"));
      Assertions.assertTrue(failure.getCause() instanceof IOException);
      Assertions.assertFalse(Files.exists(outputDirectory.resolve("unavailable.svg")));
      Assertions.assertArrayEquals(new byte[]{7},
                                   (byte[]) resourceType.getMethod("bytes").invoke(resource));
      try (InputStream input = (InputStream) resourceType.getMethod("inputStream").invoke(resource)) {
        Assertions.assertEquals(7, input.read());
        Assertions.assertEquals(-1, input.read());
      }
      resourceType.getMethod("close").invoke(resource);
    }
  }
}
