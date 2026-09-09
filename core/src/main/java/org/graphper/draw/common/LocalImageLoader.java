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

package org.graphper.draw.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Optional NIO file access for a reference already approved by the security policy. */
final class LocalImageLoader {

  private LocalImageLoader() {}

  static InputStream open(URI uri, int maximum) throws IOException {
    Path file = Paths.get(uri);
    if (Files.size(file) > maximum) {
      throw new IOException("Image exceeds the configured byte limit");
    }
    return Files.newInputStream(file);
  }
}
