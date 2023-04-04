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

package org.graphper.draw.svg;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.graphper.util.Asserts;
import org.graphper.draw.GraphResource;

public class SvgGraphResource implements GraphResource, Serializable {

  private static final long serialVersionUID = -5347313517392065083L;

  private final String name;

  private final String content;

  public SvgGraphResource(String name, String content) {
    Asserts.nullArgument(name, "name");
    Asserts.nullArgument(content, "content");
    this.name = name;
    this.content = content;
  }

  @Override
  public byte[] bytes() {
    return content.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public InputStream inputStream() {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String suffix() {
    return ".svg";
  }
}
