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

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
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
 * and filesystem image access are disabled unless explicitly enabled.
 *
 * <p><b>Scope of the remote image guarantees.</b> This class decides whether a reference may be
 * used at all. The address level restrictions apply to the shared image loader used by the native
 * raster renderer and by SVG preparation for Batik/FOP conversion: it resolves the allow-listed
 * hostname, refuses the reference unless every resolved address is public, and then connects to one
 * of exactly those addresses while keeping the hostname for the {@code Host} header, for TLS SNI
 * and for TLS hostname verification. It does not follow redirects and accepts only an HTTP {@code
 * 200} response with a decodable raster content type.
 *
 * <p>SVG output itself is deliberately <em>not</em> covered: it embeds the approved reference as an
 * {@code xlink:href}, so whatever renders that SVG performs its own fetch under its own rules.
 * Before Batik/FOP conversion, approved images are loaded under this policy, raster-validated and
 * embedded as canonical data URIs. Batik never receives an external image reference. Conversion
 * also requires support for disabling external resources and scripts (Batik 1.13+ security hints)
 * and fails closed if either protection is unavailable. SVG conversion has additional fixed
 * structural and aggregate image budgets; these are not a general complexity or memory guarantee
 * for arbitrary untrusted SVG.
 */
public final class SecurityPolicy implements Serializable {

  private static final long serialVersionUID = 1023362078276416241L;

  private static final SecurityPolicy DEFAULT = builder().build();

  private final boolean allowRemoteImages;
  private Set<String> allowedRemoteImageHosts;
  private final String localImageBaseDirectory;
  private final int connectTimeoutMillis;
  private final int readTimeoutMillis;
  private final int maxImageBytes;
  private final long maxImagePixels;
  private final long maxOutputPixels;

  private SecurityPolicy(Builder builder) {
    allowRemoteImages = builder.allowRemoteImages;
    allowedRemoteImageHosts =
        Collections.unmodifiableSet(new LinkedHashSet<>(builder.allowedRemoteImageHosts));
    localImageBaseDirectory = builder.localImageBaseDirectory;
    connectTimeoutMillis = builder.connectTimeoutMillis;
    readTimeoutMillis = builder.readTimeoutMillis;
    maxImageBytes = builder.maxImageBytes;
    maxImagePixels = builder.maxImagePixels;
    maxOutputPixels = builder.maxOutputPixels;
  }

  private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
    input.defaultReadObject();
    try {
      Builder validated =
          builder()
              .allowRemoteImages(allowRemoteImages)
              .connectTimeoutMillis(connectTimeoutMillis)
              .readTimeoutMillis(readTimeoutMillis)
              .maxImageBytes(maxImageBytes)
              .maxImagePixels(maxImagePixels)
              .maxOutputPixels(maxOutputPixels);
      for (String host : allowedRemoteImageHosts) {
        validated.allowRemoteImageHost(host);
      }
      if (!validated.allowedRemoteImageHosts.equals(allowedRemoteImageHosts)) {
        throw new IllegalArgumentException("remote image hosts must be normalized");
      }
      if (localImageBaseDirectory != null) {
        validated.localImageBaseDirectory(localImageBaseDirectory);
        if (!localImageBaseDirectory.equals(validated.localImageBaseDirectory)) {
          throw new IllegalArgumentException("local image base must be absolute and normalized");
        }
      }
      // Do not retain a mutable collection aliased elsewhere in the serialized object graph.
      allowedRemoteImageHosts =
          Collections.unmodifiableSet(new LinkedHashSet<>(validated.allowedRemoteImageHosts));
    } catch (RuntimeException e) {
      InvalidObjectException invalid = new InvalidObjectException("Invalid image security policy");
      invalid.initCause(e);
      throw invalid;
    }
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

  /**
   * Budget for both the hostname resolution and the TCP connect of a remote image fetch, each taken
   * separately.
   *
   * @return connect budget in milliseconds
   */
  public int getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public Set<String> getAllowedRemoteImageHosts() {
    return allowedRemoteImageHosts;
  }

  /**
   * Total budget for one remote image response, covering the TLS handshake, the request and every
   * read of the response. It is an aggregate deadline rather than a per-read allowance, so a slow
   * drip cannot outlast it by staying just under a single socket timeout.
   *
   * @return response budget in milliseconds
   */
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

  public File getLocalImageBaseDirectory() {
    return getLocalImageBaseDirectoryFile();
  }

  /** Returns the configured directory without requiring newer filesystem APIs. */
  public File getLocalImageBaseDirectoryFile() {
    return localImageBaseDirectory == null ? null : new File(localImageBaseDirectory);
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
          ? uriValue
          : null;
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Returns a normalized, policy-approved image reference, or {@code null} when access is denied.
   *
   * <p>Approval here is purely about the reference: an {@code http}/{@code https} reference needs
   * {@link #isAllowRemoteImages()} plus exact membership in {@link #getAllowedRemoteImageHosts()},
   * carries no user info, and a filesystem reference must resolve inside {@link
   * #getLocalImageBaseDirectory()}. Where the hostname actually points is checked later, by the
   * loader that performs the fetch.
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
      if (scheme != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
        String host = uri.getHost();
        return allowRemoteImages
                && host != null
                && uri.getUserInfo() == null
                && allowedRemoteImageHosts.contains(host.toLowerCase(Locale.ROOT))
            ? uriValue
            : null;
      }
      File base = getLocalImageBaseDirectoryFile();
      if (base == null || (scheme != null && !"file".equalsIgnoreCase(scheme))) {
        return null;
      }
      File requested = scheme == null ? new File(uriValue) : new File(uri);
      if (!requested.isAbsolute()) {
        requested = new File(base, uriValue);
      }
      File realBase = base.getCanonicalFile();
      File realRequested = requested.getCanonicalFile();
      if (!realBase.isDirectory() || !realRequested.exists()) {
        return null;
      }
      // Reference validation only: the loader must independently secure the actual file open.
      for (File parent = realRequested; parent != null; parent = parent.getParentFile()) {
        if (parent.equals(realBase)) {
          return new URI("file", "", realRequested.toURI().getPath(), null).toASCIIString();
        }
      }
      return null;
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
    return Objects.hash(
        allowRemoteImages,
        allowedRemoteImageHosts,
        localImageBaseDirectory,
        connectTimeoutMillis,
        readTimeoutMillis,
        maxImageBytes,
        maxImagePixels,
        maxOutputPixels);
  }

  @Override
  public String toString() {
    return "SecurityPolicy{"
        + "allowRemoteImages="
        + allowRemoteImages
        + ", allowedRemoteImageHosts="
        + allowedRemoteImageHosts
        + ", localImageBaseDirectory='"
        + localImageBaseDirectory
        + '\''
        + ", connectTimeoutMillis="
        + connectTimeoutMillis
        + ", readTimeoutMillis="
        + readTimeoutMillis
        + ", maxImageBytes="
        + maxImageBytes
        + ", maxImagePixels="
        + maxImagePixels
        + ", maxOutputPixels="
        + maxOutputPixels
        + '}';
  }

  /** Builds image-access restrictions and rendering resource limits. */
  public static final class Builder {

    private boolean allowRemoteImages;
    private final Set<String> allowedRemoteImageHosts = new LinkedHashSet<>();
    private String localImageBaseDirectory;
    private int connectTimeoutMillis = 5_000;
    private int readTimeoutMillis = 10_000;
    private int maxImageBytes = 10 * 1024 * 1024;
    private long maxImagePixels = 25_000_000;
    private long maxOutputPixels = 120_000_000;

    private Builder() {}

    public Builder allowRemoteImages(boolean allow) {
      allowRemoteImages = allow;
      return this;
    }

    /**
     * Adds a DNS hostname that may be used for an opt-in remote image. Membership is matched
     * exactly after IDN and case normalization; subdomains are not implied.
     */
    public Builder allowRemoteImageHost(String host) {
      Asserts.nullArgument(host, "host");
      String normalized = IDN.toASCII(host.trim()).toLowerCase(Locale.ROOT);
      Asserts.illegalArgument(
          normalized.isEmpty()
              || normalized.length() > 253
              || !normalized.matches("[a-z0-9.-]+")
              || normalized.startsWith(".")
              || normalized.endsWith(".")
              || normalized.contains(".."),
          "remote image host must be a DNS hostname without a port");
      allowedRemoteImageHosts.add(normalized);
      return this;
    }

    /** Sets the local image base directory as an absolute, normalized path. */
    public Builder localImageBaseDirectory(File directory) {
      Asserts.nullArgument(directory, "directory");
      Asserts.illegalArgument(
          directory.getPath().indexOf('\0') >= 0, "local image base must not contain NUL");
      localImageBaseDirectory = new File(directory.getAbsoluteFile().toURI().normalize()).getPath();
      return this;
    }

    public Builder localImageBaseDirectory(String directory) {
      Asserts.nullArgument(directory, "directory");
      return localImageBaseDirectory(new File(directory));
    }

    /**
     * Accepts desktop filesystem paths without linking this API to their runtime types. Prefer the
     * {@link #localImageBaseDirectory(File)} overload on older Android runtimes.
     */
    public Builder localImageBaseDirectory(Object directory) {
      Asserts.nullArgument(directory, "directory");
      if (directory instanceof File) {
        return localImageBaseDirectory((File) directory);
      }
      if (directory instanceof String) {
        return localImageBaseDirectory((String) directory);
      }
      try {
        Class<?> pathType = Class.forName("java.nio.file.Path");
        if (pathType.isInstance(directory)) {
          return localImageBaseDirectory((File) pathType.getMethod("toFile").invoke(directory));
        }
      } catch (ClassNotFoundException
          | NoSuchMethodException
          | IllegalAccessException
          | InvocationTargetException
          | LinkageError
          | SecurityException e) {
        throw new IllegalArgumentException("Cannot convert local image base to a File", e);
      }
      throw new IllegalArgumentException(
          "local image base must be a File, String or filesystem path");
    }

    /** Sets the per-step budget for resolving and for connecting to a remote image host. */
    public Builder connectTimeoutMillis(int timeout) {
      Asserts.illegalArgument(timeout <= 0, "connect timeout must be positive");
      connectTimeoutMillis = timeout;
      return this;
    }

    /** Sets the total budget for one remote image response, not the budget of a single read. */
    public Builder readTimeoutMillis(int timeout) {
      Asserts.illegalArgument(timeout <= 0, "read timeout must be positive");
      readTimeoutMillis = timeout;
      return this;
    }

    /** Sets the positive byte limit for an image. */
    public Builder maxImageBytes(int bytes) {
      Asserts.illegalArgument(bytes <= 0, "maximum image bytes must be positive");
      maxImageBytes = bytes;
      return this;
    }

    /** Sets the positive pixel limit for a decoded image. */
    public Builder maxImagePixels(long pixels) {
      Asserts.illegalArgument(pixels <= 0, "maximum image pixels must be positive");
      maxImagePixels = pixels;
      return this;
    }

    /** Sets the positive pixel limit for rendered output. */
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
