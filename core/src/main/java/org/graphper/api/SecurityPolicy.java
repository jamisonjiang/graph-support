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

import java.io.IOException;
import java.io.Serializable;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.graphper.util.Asserts;

/**
 * Controls URI and image-resource access during rendering.
 *
 * <p>The default policy allows ordinary web/mail links and bounded embedded data images. Network
 * and filesystem image access are disabled unless explicitly enabled.</p>
 */
public final class SecurityPolicy implements Serializable {

  private static final long serialVersionUID = 1023362078276416241L;

  private static final SecurityPolicy DEFAULT = builder().build();

  private final boolean allowRemoteImages;
  private final Set<String> allowedRemoteImageHosts;
  private final String localImageBaseDirectory;
  private final int connectTimeoutMillis;
  private final int readTimeoutMillis;
  private final int maxImageBytes;
  private final long maxImagePixels;
  private final long maxOutputPixels;

  private SecurityPolicy(Builder builder) {
    allowRemoteImages = builder.allowRemoteImages;
    allowedRemoteImageHosts = Collections.unmodifiableSet(
        new LinkedHashSet<>(builder.allowedRemoteImageHosts));
    localImageBaseDirectory = builder.localImageBaseDirectory;
    connectTimeoutMillis = builder.connectTimeoutMillis;
    readTimeoutMillis = builder.readTimeoutMillis;
    maxImageBytes = builder.maxImageBytes;
    maxImagePixels = builder.maxImagePixels;
    maxOutputPixels = builder.maxOutputPixels;
  }

  public static SecurityPolicy defaultPolicy() {
    return DEFAULT;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean isAllowRemoteImages() {
    return allowRemoteImages;
  }

  public int getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public Set<String> getAllowedRemoteImageHosts() {
    return allowedRemoteImageHosts;
  }

  public int getReadTimeoutMillis() {
    return readTimeoutMillis;
  }

  public int getMaxImageBytes() {
    return maxImageBytes;
  }

  public long getMaxImagePixels() {
    return maxImagePixels;
  }

  public long getMaxOutputPixels() {
    return maxOutputPixels;
  }

  public Path getLocalImageBaseDirectory() {
    return localImageBaseDirectory == null ? null : Paths.get(localImageBaseDirectory);
  }

  /** Returns a safe link or {@code null} when the value uses a dangerous or malformed scheme. */
  public String sanitizeLink(String value) {
    String uriValue = clean(value);
    if (uriValue == null) {
      return null;
    }
    try {
      URI uri = new URI(uriValue);
      String scheme = uri.getScheme();
      if (scheme == null) {
        return uri.getRawAuthority() == null ? uriValue : null;
      }
      scheme = scheme.toLowerCase(Locale.ROOT);
      if ("mailto".equals(scheme)) {
        return uriValue;
      }
      return ("http".equals(scheme) || "https".equals(scheme)) && uri.getUserInfo() == null
          ? uriValue : null;
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Returns a normalized, policy-approved image reference, or {@code null} when access is denied.
   */
  public String sanitizeImage(String value) {
    String uriValue = clean(value);
    if (uriValue == null) {
      return null;
    }
    String lowerValue = uriValue.toLowerCase(Locale.ROOT);
    if (lowerValue.startsWith("data:image/png;base64,")
        || lowerValue.startsWith("data:image/jpeg;base64,")
        || lowerValue.startsWith("data:image/gif;base64,")
        || lowerValue.startsWith("data:image/webp;base64,")
        || lowerValue.startsWith("data:image/bmp;base64,")) {
      int comma = uriValue.indexOf(',');
      long estimatedBytes = (long) (uriValue.length() - comma - 1) * 3 / 4;
      return estimatedBytes <= maxImageBytes ? uriValue : null;
    }

    try {
      URI uri = new URI(uriValue);
      String scheme = uri.getScheme();
      if (scheme != null && ("http".equalsIgnoreCase(scheme)
          || "https".equalsIgnoreCase(scheme))) {
        String host = uri.getHost();
        return allowRemoteImages && host != null && uri.getUserInfo() == null
            && allowedRemoteImageHosts.contains(host.toLowerCase(Locale.ROOT))
            ? uriValue : null;
      }
      Path base = getLocalImageBaseDirectory();
      if (base == null || (scheme != null && !"file".equalsIgnoreCase(scheme))) {
        return null;
      }
      Path requested = scheme == null ? base.resolve(uriValue) : Paths.get(uri);
      Path realBase = base.toRealPath();
      Path realRequested = requested.toRealPath();
      return realRequested.startsWith(realBase) ? realRequested.toUri().toString() : null;
    } catch (IOException | RuntimeException | URISyntaxException e) {
      return null;
    }
  }

  private String clean(String value) {
    if (value == null) {
      return null;
    }
    String cleaned = value.trim();
    if (cleaned.isEmpty()) {
      return null;
    }
    for (int i = 0; i < cleaned.length(); i++) {
      if (Character.isISOControl(cleaned.charAt(i))) {
        return null;
      }
    }
    return cleaned;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SecurityPolicy)) {
      return false;
    }
    SecurityPolicy that = (SecurityPolicy) o;
    return allowRemoteImages == that.allowRemoteImages
        && connectTimeoutMillis == that.connectTimeoutMillis
        && readTimeoutMillis == that.readTimeoutMillis
        && maxImageBytes == that.maxImageBytes
        && maxImagePixels == that.maxImagePixels
        && maxOutputPixels == that.maxOutputPixels
        && Objects.equals(allowedRemoteImageHosts, that.allowedRemoteImageHosts)
        && Objects.equals(localImageBaseDirectory, that.localImageBaseDirectory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(allowRemoteImages, allowedRemoteImageHosts, localImageBaseDirectory,
                        connectTimeoutMillis,
                        readTimeoutMillis, maxImageBytes, maxImagePixels, maxOutputPixels);
  }

  @Override
  public String toString() {
    return "SecurityPolicy{" +
        "allowRemoteImages=" + allowRemoteImages +
        ", allowedRemoteImageHosts=" + allowedRemoteImageHosts +
        ", localImageBaseDirectory='" + localImageBaseDirectory + '\'' +
        ", connectTimeoutMillis=" + connectTimeoutMillis +
        ", readTimeoutMillis=" + readTimeoutMillis +
        ", maxImageBytes=" + maxImageBytes +
        ", maxImagePixels=" + maxImagePixels +
        ", maxOutputPixels=" + maxOutputPixels +
        '}';
  }

  public static final class Builder {

    private boolean allowRemoteImages;
    private final Set<String> allowedRemoteImageHosts = new LinkedHashSet<>();
    private String localImageBaseDirectory;
    private int connectTimeoutMillis = 5_000;
    private int readTimeoutMillis = 10_000;
    private int maxImageBytes = 10 * 1024 * 1024;
    private long maxImagePixels = 25_000_000;
    private long maxOutputPixels = 120_000_000;

    private Builder() {
    }

    public Builder allowRemoteImages(boolean allow) {
      allowRemoteImages = allow;
      return this;
    }

    /** Adds a DNS hostname that may be used for an opt-in remote image. */
    public Builder allowRemoteImageHost(String host) {
      Asserts.nullArgument(host, "host");
      String normalized = IDN.toASCII(host.trim()).toLowerCase(Locale.ROOT);
      Asserts.illegalArgument(normalized.isEmpty() || normalized.length() > 253
                                  || !normalized.matches("[a-z0-9.-]+")
                                  || normalized.startsWith(".") || normalized.endsWith(".")
                                  || normalized.contains(".."),
                              "remote image host must be a DNS hostname without a port");
      allowedRemoteImageHosts.add(normalized);
      return this;
    }

    public Builder localImageBaseDirectory(Path directory) {
      Asserts.nullArgument(directory, "directory");
      localImageBaseDirectory = directory.toAbsolutePath().normalize().toString();
      return this;
    }

    public Builder connectTimeoutMillis(int timeout) {
      Asserts.illegalArgument(timeout <= 0, "connect timeout must be positive");
      connectTimeoutMillis = timeout;
      return this;
    }

    public Builder readTimeoutMillis(int timeout) {
      Asserts.illegalArgument(timeout <= 0, "read timeout must be positive");
      readTimeoutMillis = timeout;
      return this;
    }

    public Builder maxImageBytes(int bytes) {
      Asserts.illegalArgument(bytes <= 0, "maximum image bytes must be positive");
      maxImageBytes = bytes;
      return this;
    }

    public Builder maxImagePixels(long pixels) {
      Asserts.illegalArgument(pixels <= 0, "maximum image pixels must be positive");
      maxImagePixels = pixels;
      return this;
    }

    public Builder maxOutputPixels(long pixels) {
      Asserts.illegalArgument(pixels <= 0, "maximum output pixels must be positive");
      maxOutputPixels = pixels;
      return this;
    }

    public SecurityPolicy build() {
      return new SecurityPolicy(this);
    }
  }
}
