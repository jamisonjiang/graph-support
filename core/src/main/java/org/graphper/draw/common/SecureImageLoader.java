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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import org.graphper.api.SecurityPolicy;

/** Loads policy-approved raster image bytes with bounded I/O. */
final class SecureImageLoader {

  private SecureImageLoader() {
  }

  static byte[] load(String reference, SecurityPolicy policy) throws IOException {
    String safeReference = policy.sanitizeImage(reference);
    if (safeReference == null) {
      throw new IOException("Image resource is denied by the security policy");
    }
    if (safeReference.regionMatches(true, 0, "data:", 0, 5)) {
      int comma = safeReference.indexOf(',');
      try {
        byte[] data = Base64.getDecoder().decode(safeReference.substring(comma + 1));
        if (data.length > policy.getMaxImageBytes()) {
          throw new IOException("Image exceeds the configured byte limit");
        }
        return data;
      } catch (IllegalArgumentException e) {
        throw new IOException("Invalid base64 image", e);
      }
    }

    URI uri = URI.create(safeReference);
    if ("file".equalsIgnoreCase(uri.getScheme())) {
      Path file = Paths.get(uri);
      long size = Files.size(file);
      if (size > policy.getMaxImageBytes()) {
        throw new IOException("Image exceeds the configured byte limit");
      }
      try (InputStream input = Files.newInputStream(file)) {
        return readBounded(input, policy.getMaxImageBytes());
      }
    }

    validatePublicHost(uri.getHost());
    URLConnection connection = new URL(safeReference).openConnection();
    connection.setConnectTimeout(policy.getConnectTimeoutMillis());
    connection.setReadTimeout(policy.getReadTimeoutMillis());
    connection.setUseCaches(false);
    if (connection instanceof HttpURLConnection) {
      ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
    }
    long contentLength = connection.getContentLengthLong();
    if (contentLength > policy.getMaxImageBytes()) {
      throw new IOException("Image exceeds the configured byte limit");
    }
    try (InputStream input = connection.getInputStream()) {
      return readBounded(input, policy.getMaxImageBytes());
    }
  }

  private static void validatePublicHost(String host) throws IOException {
    if (host == null || host.isEmpty()) {
      throw new IOException("Remote image URL has no host");
    }
    for (InetAddress address : InetAddress.getAllByName(host)) {
      byte[] bytes = address.getAddress();
      boolean uniqueLocalV6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
      boolean carrierGradeNat = bytes.length == 4 && (bytes[0] & 0xff) == 100
          && ((bytes[1] & 0xff) >= 64 && (bytes[1] & 0xff) <= 127);
      if (address.isAnyLocalAddress() || address.isLoopbackAddress()
          || address.isLinkLocalAddress() || address.isSiteLocalAddress()
          || address.isMulticastAddress() || uniqueLocalV6 || carrierGradeNat) {
        throw new IOException("Remote image host resolves to a non-public address");
      }
    }
  }

  private static byte[] readBounded(InputStream input, int maximum) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(maximum, 16 * 1024));
    byte[] buffer = new byte[8192];
    int total = 0;
    int read;
    while ((read = input.read(buffer)) != -1) {
      total += read;
      if (total > maximum) {
        throw new IOException("Image exceeds the configured byte limit");
      }
      output.write(buffer, 0, read);
    }
    return output.toByteArray();
  }
}
