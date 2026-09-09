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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.apache_gs.commons.lang3.StringUtils;
import org.graphper.api.GraphResource;
import org.graphper.util.Asserts;

/** Stores rendered graph bytes with a resource name and file suffix. */
public class DefaultGraphResource implements GraphResource {

  private final String name;

  private final String suffix;

  private final ByteArrayOutputStream os;

  /** Creates a graph resource backed by the supplied output stream. */
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

  /**
   * Saves with no-follow protection for the destination file. If the runtime cannot provide that
   * protection, this operation fails closed; {@link #bytes()} and {@link #inputStream()} remain
   * usable.
   */
  @Override
  public void save(String parentPath, String fileName) throws IOException {
    fileName =
        StringUtils.isNotEmpty(fileName)
            ? fileName
            : (StringUtils.isNotEmpty(name()) ? name() : "graphviz");
    if (new File(fileName).isAbsolute()
        || fileName.indexOf('/') >= 0
        || fileName.indexOf('\\') >= 0
        || fileName.indexOf(':') >= 0
        || fileName.indexOf('\0') >= 0
        || ".".equals(fileName)
        || "..".equals(fileName)) {
      throw new IOException("fileName must be a single, relative file name");
    }

    String outputName = fileName.endsWith(suffix()) ? fileName : fileName + suffix();
    if (outputName.indexOf('/') >= 0
        || outputName.indexOf('\\') >= 0
        || outputName.indexOf(':') >= 0
        || outputName.indexOf('\0') >= 0) {
      throw new IOException("Output name must be a single, relative file name");
    }
    try {
      // Do not link basic resource construction/streaming to optional filesystem APIs.
      Class<?> saver = Class.forName("org.graphper.draw.NioGraphResourceSaver");
      saver
          .getMethod("save", File.class, String.class, byte[].class)
          .invoke(null, new File(parentPath), outputName, bytes());
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw new IOException("Secure file saving is unavailable", cause);
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | LinkageError
        | SecurityException e) {
      // A canonical-path check followed by FileOutputStream would permit symlink replacement.
      throw new IOException("Secure file saving requires no-follow filesystem support", e);
    }
  }

  @Override
  public void close() throws IOException {
    os.reset();
    os.close();
  }
}
