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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.GraphResource;
import org.graphper.util.Asserts;

public class DefaultGraphResource implements GraphResource {

  private final String name;

  private final String suffix;

  private final ByteArrayOutputStream os;

  public DefaultGraphResource(String name, String suffix, ByteArrayOutputStream os) {
    Asserts.nullArgument(os, "Output stream");
    this.name = name;
    this.suffix = suffix;
    this.os = os;
  }

  @Override
  public byte[] bytes() {
    return os.toByteArray();
  }

  @Override
  public InputStream inputStream() {
    return new ByteArrayInputStream(os.toByteArray());
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String suffix() {
    return "." + suffix;
  }

  @Override
  public void save(String parentPath, String fileName) throws IOException {
    fileName = StringUtils.isNotEmpty(fileName) ? fileName
        : (StringUtils.isNotEmpty(name()) ? name() : "graphviz");
    Path suppliedName = Paths.get(fileName);
    if (suppliedName.isAbsolute() || suppliedName.getNameCount() != 1
        || fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0
        || fileName.indexOf(':') >= 0 || ".".equals(fileName) || "..".equals(fileName)) {
      throw new IOException("fileName must be a single, relative file name");
    }

    String outputName = fileName.endsWith(suffix()) ? fileName : fileName + suffix();
    Path base = Paths.get(parentPath).toRealPath();
    Path target = base.resolve(outputName).normalize();
    if (!target.getParent().equals(base)
        || (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(target))) {
      throw new IOException("Output path escapes the target directory or is a symbolic link");
    }
    try (OutputStream fos = Files.newOutputStream(target, StandardOpenOption.CREATE,
                                                  StandardOpenOption.TRUNCATE_EXISTING,
                                                  StandardOpenOption.WRITE,
                                                  LinkOption.NOFOLLOW_LINKS)) {
      fos.write(bytes());
      fos.flush();
    }
  }

  @Override
  public void close() throws IOException {
    os.reset();
    os.close();
  }
}
