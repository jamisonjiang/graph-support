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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.graphper.api.SecurityPolicy;

/**
 * Loads policy-approved raster image bytes with bounded I/O.
 *
 * <p>Remote references are fetched with a minimal HTTP/1.1 {@code GET} over a socket that is
 * connected to an already validated IP address, so the destination that is inspected is the
 * destination that is reached. The original hostname is still used for the {@code Host} header, for
 * the TLS SNI extension and for TLS hostname verification, so pinning the address does not weaken
 * transport security.</p>
 *
 * <p>This is why {@link java.net.HttpURLConnection} is not used: it resolves the hostname again
 * itself and offers no way to bind the connection to the address that was checked. The trade-off is
 * that the JVM HTTP proxy settings are not honoured, which is unavoidable - a proxy resolves the
 * name on its own behalf, so the destination could not be enforced through it.</p>
 */
final class SecureImageLoader {

  private static final int HTTP_DEFAULT_PORT = 80;
  private static final int HTTPS_DEFAULT_PORT = 443;
  private static final int MAX_STATUS_LINE_CHARS = 1024;
  private static final int MAX_HEADER_LINE_CHARS = 8 * 1024;
  private static final int MAX_HEADER_COUNT = 100;
  private static final int READ_BUFFER_BYTES = 8 * 1024;

  // Native DNS can ignore interruption. Never replace a stuck worker with a new thread.
  private static final ThreadPoolExecutor DNS_EXECUTOR = new ThreadPoolExecutor(
      0, 4, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), task -> {
        Thread thread = new Thread(task, "graph-support-image-dns");
        thread.setDaemon(true);
        return thread;
      });
  private static final Semaphore FETCH_SLOTS = new Semaphore(128);
  private static final ScheduledThreadPoolExecutor WATCHDOG = new ScheduledThreadPoolExecutor(
      1, task -> {
        Thread thread = new Thread(task, "graph-support-image-deadline");
        thread.setDaemon(true);
        return thread;
      });

  static {
    // FETCH_SLOTS bounds scheduled tasks; cancellation must not retain sockets until their deadline.
    WATCHDOG.setRemoveOnCancelPolicy(true);
  }

  /**
   * Response media types that the raster converters can actually decode. This is a defence in depth
   * measure only: it stops obviously wrong payloads early, it does not make the fetch itself safe
   * and it has no effect on request forgery, which is handled by the host allow-list and by the
   * address validation.
   */
  private static final Set<String> ALLOWED_CONTENT_TYPES = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList("image/png", "image/jpeg", "image/jpg", "image/gif",
                                 "image/webp", "image/bmp", "image/x-bmp", "image/x-ms-bmp",
                                 "image/tiff", "image/x-tiff", "image/vnd.wap.wbmp",
                                 "image/x-icon", "image/vnd.microsoft.icon")));

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
    String scheme = uri.getScheme();
    if ("file".equalsIgnoreCase(scheme)) {
      Path file = Paths.get(uri);
      long size = Files.size(file);
      if (size > policy.getMaxImageBytes()) {
        throw new IOException("Image exceeds the configured byte limit");
      }
      try (InputStream input = Files.newInputStream(file)) {
        return readBounded(input, policy.getMaxImageBytes(), Deadline.unbounded());
      }
    }

    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new IOException("Unsupported image scheme");
    }
    return loadRemote(uri, policy);
  }

  /**
   * Resolves the host, refuses the reference unless every resolved address is public, and then
   * fetches the response from one of those exact addresses.
   */
  private static byte[] loadRemote(URI uri, SecurityPolicy policy) throws IOException {
    String host = validHost(uri.getHost());
    InetAddress[] pinned = resolvePublicAddresses(host, policy.getConnectTimeoutMillis());

    IOException connectFailure = null;
    for (InetAddress address : pinned) {
      try {
        return fetchPinned(uri, address, policy);
      } catch (PinnedConnectException e) {
        // Only an unusable address is worth another attempt. Every other failure is a protocol or
        // policy decision about this response and must not be retried against another address.
        connectFailure = e;
      }
    }
    throw connectFailure != null ? connectFailure
        : new IOException("Remote image host does not resolve to any address");
  }

  /**
   * Performs the whole exchange against {@code pinned}. Package-private so that tests can drive it
   * against a loopback server without going through the public-address filter.
   *
   * @param uri    already policy-approved {@code http}/{@code https} reference
   * @param pinned the exact address the socket must be connected to
   * @param policy limits applied to the exchange
   * @return the response body
   * @throws IOException when the response is rejected, truncated or over budget
   */
  static byte[] fetchPinned(URI uri, InetAddress pinned, SecurityPolicy policy) throws IOException {
    boolean https = "https".equalsIgnoreCase(uri.getScheme());
    String host = validHost(uri.getHost());
    int port = uri.getPort() < 0 ? (https ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT) : uri.getPort();
    String target = requestTarget(uri);
    if (!FETCH_SLOTS.tryAcquire()) {
      throw new IOException("Remote image fetch capacity is exhausted");
    }
    Socket plain = new Socket();
    Socket active = plain;
    ScheduledFuture<?> watchdog = null;
    try {
      plain.setSoTimeout(policy.getReadTimeoutMillis());
      plain.setTcpNoDelay(true);
      try {
        // InetSocketAddress built from an InetAddress performs no lookup of its own, so this is
        // the validated address and nothing else.
        plain.connect(new InetSocketAddress(pinned, port), policy.getConnectTimeoutMillis());
      } catch (IOException e) {
        throw new PinnedConnectException("Cannot connect to the resolved remote image address", e);
      }
      if (!pinned.equals(plain.getInetAddress())) {
        throw new IOException("Remote image socket is not connected to the validated address");
      }
      Deadline deadline = Deadline.after(policy.getReadTimeoutMillis());
      // SO_TIMEOUT cannot bound writes or a TLS handshake that keeps receiving partial records.
      // Close the underlying socket, not SSLSocket (whose close_notify can itself block).
      watchdog = WATCHDOG.schedule(() -> closeQuietly(plain),
                                   deadline.endNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
      try {
        if (https) {
          active = startTls(plain, host, port, deadline);
        }

        OutputStream output = active.getOutputStream();
        output.write(request(host, port, https, target).getBytes(StandardCharsets.US_ASCII));
        output.flush();

        InputStream input = new BufferedInputStream(active.getInputStream(), READ_BUFFER_BYTES);
        int status = readStatusCode(input, deadline);
        Map<String, String> headers = readHeaders(input, deadline);
        while (status >= 100 && status < 200) {
          status = readStatusCode(input, deadline);
          headers = readHeaders(input, deadline);
        }
        if (status != 200) {
          // Redirects are never followed, so a 3xx body is an unrelated payload and is discarded
          // instead of decoded. Non-success codes are rejected before any body byte is buffered.
          throw new IOException("Remote image request returned HTTP status " + status);
        }
        requireDecodableContentType(headers.get("content-type"));
        byte[] body = readBody(input, headers, policy.getMaxImageBytes(), deadline);
        deadline.check();
        return body;
      } catch (IOException e) {
        // Preserve a consistent timeout diagnostic when the watchdog interrupts socket I/O.
        deadline.check();
        throw e;
      }
    } finally {
      // Deterministic teardown on every path, including the rejection paths above. The plain socket
      // is closed first so that a TLS close cannot block on writing close_notify.
      closeQuietly(plain);
      if (active != plain) {
        closeQuietly(active);
      }
      if (watchdog != null) {
        watchdog.cancel(false);
      }
      FETCH_SLOTS.release();
    }
  }

  private static Socket startTls(Socket plain, String host, int port, Deadline deadline)
      throws IOException {
    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    // Handing the hostname (never the pinned literal address) to the factory is what keeps SNI and
    // certificate identity checks bound to the name the caller allow-listed.
    SSLSocket ssl = (SSLSocket) factory.createSocket(plain, host, port, true);
    bindTlsIdentity(ssl, host);
    ssl.setSoTimeout(deadline.remainingMillis());
    ssl.startHandshake();
    return ssl;
  }

  /**
   * Binds the TLS identity checks of {@code ssl} to {@code host} rather than to the pinned literal
   * address. Package-private so tests can assert the resulting parameters.
   *
   * @throws IOException when hostname verification cannot be enabled at all, in which case the
   *                     fetch is abandoned instead of continuing unverified
   */
  static void bindTlsIdentity(SSLSocket ssl, String host) throws IOException {
    SSLParameters parameters = ssl.getSSLParameters();
    parameters.setEndpointIdentificationAlgorithm("HTTPS");
    // RFC 6066 forbids literal addresses in SNI, and a DNS name can never be all digits and dots.
    if (!host.matches("[0-9.]+")) {
      try {
        parameters.setServerNames(Collections.singletonList(new SNIHostName(host)));
      } catch (RuntimeException e) {
        // The peer name handed to createSocket already covers default SNI behaviour.
      }
    }
    ssl.setSSLParameters(parameters);
    if (!"HTTPS".equals(ssl.getSSLParameters().getEndpointIdentificationAlgorithm())) {
      throw new IOException("TLS hostname verification is unavailable for remote images");
    }
  }

  private static String request(String host, int port, boolean https, String target) {
    int defaultPort = https ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT;
    String hostHeader = port == defaultPort ? host : host + ":" + port;
    return "GET " + target + " HTTP/1.1\r\n"
        + "Host: " + hostHeader + "\r\n"
        + "Accept: image/*\r\n"
        + "Accept-Encoding: identity\r\n"
        + "User-Agent: graph-support\r\n"
        + "Connection: close\r\n"
        + "\r\n";
  }

  /**
   * Resolves {@code host} within {@code timeoutMillis} and requires every answer to be public.
   *
   * <p>Rejecting the whole answer set, rather than filtering it, means the addresses returned here
   * are the only addresses the exchange may use.</p>
   */
  private static InetAddress[] resolvePublicAddresses(String host, int timeoutMillis)
      throws IOException {
    InetAddress[] resolved = resolveWithin(host, timeoutMillis);
    if (resolved == null || resolved.length == 0) {
      throw new IOException("Remote image host does not resolve to any address");
    }
    for (InetAddress address : resolved) {
      if (!isPublicAddress(address)) {
        throw new IOException("Remote image host resolves to a non-public address");
      }
    }
    return resolved;
  }

  /**
   * {@link InetAddress#getAllByName(String)} has no timeout of its own. A shared, bounded daemon
   * pool runs lookups without queueing; saturation fails closed and callers wait only their budget.
   */
  private static InetAddress[] resolveWithin(String host, int timeoutMillis) throws IOException {
    FutureTask<InetAddress[]> lookup = new FutureTask<>(() -> InetAddress.getAllByName(host));
    Deadline deadline = Deadline.after(timeoutMillis);
    try {
      DNS_EXECUTOR.execute(lookup);
      InetAddress[] resolved = lookup.get(deadline.remainingMillis(), TimeUnit.MILLISECONDS);
      deadline.check();
      return resolved;
    } catch (RejectedExecutionException e) {
      throw new IOException("Remote image DNS capacity is exhausted", e);
    } catch (TimeoutException e) {
      throw new IOException("Timed out resolving the remote image host");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while resolving the remote image host", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw new IOException("Cannot resolve the remote image host", cause);
    } finally {
      lookup.cancel(true);
    }
  }

  /** Package-private so the address filter can be asserted directly. */
  static boolean isPublicAddress(InetAddress address) {
    if (address == null) {
      return false;
    }
    byte[] bytes = address.getAddress();
    boolean uniqueLocalV6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    boolean carrierGradeNat = bytes.length == 4 && (bytes[0] & 0xff) == 100
        && ((bytes[1] & 0xff) >= 64 && (bytes[1] & 0xff) <= 127);
    boolean specialV4 = false;
    boolean specialV6 = false;
    if (bytes.length == 4) {
      int first = bytes[0] & 0xff;
      int second = bytes[1] & 0xff;
      int third = bytes[2] & 0xff;
      specialV4 = first == 0 || first >= 240
          || (first == 192 && (second == 0 && (third == 0 || third == 2)
                              || second == 88 && third == 99))
          || (first == 198 && (second == 18 || second == 19 || second == 51 && third == 100))
          || (first == 203 && second == 0 && third == 113);
    } else if (bytes.length == 16) {
      // Reject special-use space outside global unicast, except the classified embedded forms.
      specialV6 = ((bytes[0] & 0xe0) != 0x20 && embeddedIpv4(bytes) == null)
          || (bytes[0] == 0x20 && bytes[1] == 0x01
              && ((bytes[2] & 0xfe) == 0 || bytes[2] == 0x0d && (bytes[3] & 0xff) == 0xb8))
          || (bytes[0] == 0x3f && (bytes[1] & 0xff) == 0xff && (bytes[2] & 0xf0) == 0);
    }
    if (address.isAnyLocalAddress() || address.isLoopbackAddress()
        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
        || address.isMulticastAddress() || uniqueLocalV6 || carrierGradeNat
        || specialV4 || specialV6) {
      return false;
    }
    // An IPv6 answer can carry an IPv4 destination that the IPv6 predicates above know nothing
    // about, so the embedded address is classified as well.
    byte[] embedded = embeddedIpv4(bytes);
    if (embedded == null) {
      return true;
    }
    try {
      return isPublicAddress(InetAddress.getByAddress(embedded));
    } catch (UnknownHostException e) {
      return false;
    }
  }

  /**
   * Returns the IPv4 address carried by an IPv4-compatible ({@code ::a.b.c.d}), 6to4
   * ({@code 2002::/16}) or NAT64 ({@code 64:ff9b::/96}) IPv6 address, or {@code null} when there is
   * none. IPv4-mapped answers need no handling here because {@link InetAddress} already folds them
   * into an {@link java.net.Inet4Address}.
   */
  private static byte[] embeddedIpv4(byte[] bytes) {
    if (bytes.length != 16) {
      return null;
    }
    if ((bytes[0] & 0xff) == 0x20 && (bytes[1] & 0xff) == 0x02) {
      return new byte[]{bytes[2], bytes[3], bytes[4], bytes[5]};
    }
    boolean nat64 = (bytes[0] & 0xff) == 0x00 && (bytes[1] & 0xff) == 0x64
        && (bytes[2] & 0xff) == 0xff && (bytes[3] & 0xff) == 0x9b;
    boolean zeroPrefix = true;
    for (int i = nat64 ? 4 : 0; i < 12; i++) {
      if (bytes[i] != 0) {
        zeroPrefix = false;
        break;
      }
    }
    return zeroPrefix ? new byte[]{bytes[12], bytes[13], bytes[14], bytes[15]} : null;
  }

  private static String validHost(String host) throws IOException {
    if (host == null || host.isEmpty()) {
      throw new IOException("Remote image URL has no host");
    }
    if (host.length() > 253) {
      throw new IOException("Remote image URL host is too long");
    }
    for (int i = 0; i < host.length(); i++) {
      char c = host.charAt(i);
      boolean allowed = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || c == '.' || c == '-';
      if (!allowed) {
        throw new IOException("Remote image URL host is not a plain DNS name");
      }
    }
    return host;
  }

  private static String requestTarget(URI uri) throws IOException {
    String path = uri.getRawPath();
    if (path == null || path.isEmpty()) {
      path = "/";
    }
    String query = uri.getRawQuery();
    String target = query == null ? path : path + "?" + query;
    for (int i = 0; i < target.length(); i++) {
      char c = target.charAt(i);
      if (c < '!' || c > '~') {
        throw new IOException("Remote image URL path is not a valid request target");
      }
    }
    return target;
  }

  private static int readStatusCode(InputStream input, Deadline deadline) throws IOException {
    String statusLine = readLine(input, MAX_STATUS_LINE_CHARS, deadline);
    if (statusLine == null || !statusLine.startsWith("HTTP/1.")) {
      throw new IOException("Remote image response is not HTTP/1.x");
    }
    int space = statusLine.indexOf(' ');
    if (space < 0 || statusLine.length() < space + 4) {
      throw new IOException("Remote image response has no status code");
    }
    try {
      return Integer.parseInt(statusLine.substring(space + 1, space + 4));
    } catch (NumberFormatException e) {
      throw new IOException("Remote image response has an invalid status code");
    }
  }

  private static Map<String, String> readHeaders(InputStream input, Deadline deadline)
      throws IOException {
    Map<String, String> headers = new HashMap<>();
    for (int count = 0; ; count++) {
      if (count >= MAX_HEADER_COUNT) {
        throw new IOException("Remote image response has too many headers");
      }
      String line = readLine(input, MAX_HEADER_LINE_CHARS, deadline);
      if (line == null) {
        throw new IOException("Remote image response headers were truncated");
      }
      if (line.isEmpty()) {
        return headers;
      }
      int colon = line.indexOf(':');
      if (colon <= 0) {
        continue;
      }
      String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
      String value = line.substring(colon + 1).trim();
      String previous = headers.put(name, value);
      boolean framing = "content-length".equals(name) || "transfer-encoding".equals(name);
      if (previous != null && framing && !previous.equals(value)) {
        throw new IOException("Remote image response has conflicting " + name + " headers");
      }
      if (previous != null && !framing) {
        headers.put(name, previous);
      }
    }
  }

  private static void requireDecodableContentType(String contentType) throws IOException {
    if (contentType == null) {
      throw new IOException("Remote image response has no content type");
    }
    int semicolon = contentType.indexOf(';');
    String mediaType = (semicolon < 0 ? contentType : contentType.substring(0, semicolon))
        .trim().toLowerCase(Locale.ROOT);
    if (!ALLOWED_CONTENT_TYPES.contains(mediaType)) {
      throw new IOException("Remote image response content type is not a supported raster image");
    }
  }

  private static byte[] readBody(InputStream input, Map<String, String> headers, int maximum,
                                 Deadline deadline) throws IOException {
    String contentEncoding = headers.get("content-encoding");
    if (contentEncoding != null && !contentEncoding.trim().isEmpty()
        && !"identity".equalsIgnoreCase(contentEncoding.trim())) {
      throw new IOException("Remote image response uses an unsupported content encoding");
    }

    String transferEncoding = headers.get("transfer-encoding");
    if (transferEncoding != null && !transferEncoding.trim().isEmpty()
        && !"identity".equalsIgnoreCase(transferEncoding.trim())) {
      if (!"chunked".equalsIgnoreCase(transferEncoding.trim())) {
        throw new IOException("Remote image response uses an unsupported transfer encoding");
      }
      if (headers.containsKey("content-length")) {
        throw new IOException("Remote image response mixes chunked framing with a content length");
      }
      return readChunked(input, maximum, deadline);
    }

    String contentLength = headers.get("content-length");
    if (contentLength == null) {
      return readBounded(input, maximum, deadline);
    }
    long declared;
    try {
      declared = Long.parseLong(contentLength.trim());
    } catch (NumberFormatException e) {
      throw new IOException("Remote image response has an invalid content length");
    }
    if (declared < 0) {
      throw new IOException("Remote image response has an invalid content length");
    }
    if (declared > maximum) {
      throw new IOException("Image exceeds the configured byte limit");
    }
    return readExactly(input, (int) declared, deadline);
  }

  /**
   * Reads exactly {@code length} bytes. Stopping on the declared length instead of on end of
   * stream keeps a server that ignores {@code Connection: close} from holding the whole budget.
   */
  private static byte[] readExactly(InputStream input, int length, Deadline deadline)
      throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(length, 16 * 1024));
    byte[] buffer = new byte[READ_BUFFER_BYTES];
    int remaining = length;
    while (remaining > 0) {
      deadline.check();
      int read = input.read(buffer, 0, Math.min(buffer.length, remaining));
      if (read == -1) {
        throw new IOException("Remote image response was truncated");
      }
      output.write(buffer, 0, read);
      remaining -= read;
    }
    return output.toByteArray();
  }

  private static byte[] readChunked(InputStream input, int maximum, Deadline deadline)
      throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(maximum, 16 * 1024));
    byte[] buffer = new byte[READ_BUFFER_BYTES];
    long total = 0;
    while (true) {
      String line = readLine(input, MAX_HEADER_LINE_CHARS, deadline);
      if (line == null) {
        throw new IOException("Remote image response was truncated");
      }
      int semicolon = line.indexOf(';');
      String sizeToken = (semicolon < 0 ? line : line.substring(0, semicolon)).trim();
      long size;
      try {
        size = Long.parseLong(sizeToken, 16);
      } catch (NumberFormatException e) {
        throw new IOException("Remote image response has an invalid chunk size");
      }
      if (size < 0) {
        throw new IOException("Remote image response has an invalid chunk size");
      }
      if (size == 0) {
        return output.toByteArray();
      }
      if (size > maximum - total) {
        throw new IOException("Image exceeds the configured byte limit");
      }
      long remaining = size;
      while (remaining > 0) {
        deadline.check();
        int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
        if (read == -1) {
          throw new IOException("Remote image response was truncated");
        }
        if (read > maximum - total) {
          throw new IOException("Image exceeds the configured byte limit");
        }
        output.write(buffer, 0, read);
        remaining -= read;
        total += read;
      }
      String terminator = readLine(input, 2, deadline);
      if (terminator == null || !terminator.isEmpty()) {
        throw new IOException("Remote image response has an unterminated chunk");
      }
    }
  }

  private static String readLine(InputStream input, int maximum, Deadline deadline)
      throws IOException {
    StringBuilder line = new StringBuilder();
    while (true) {
      deadline.check();
      int c = input.read();
      if (c == -1) {
        return line.length() == 0 ? null : line.toString();
      }
      if (c == '\n') {
        break;
      }
      if (line.length() >= maximum) {
        throw new IOException("Remote image response has an oversized line");
      }
      line.append((char) c);
    }
    int length = line.length();
    if (length > 0 && line.charAt(length - 1) == '\r') {
      line.setLength(length - 1);
    }
    return line.toString();
  }

  private static byte[] readBounded(InputStream input, int maximum, Deadline deadline)
      throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(maximum, 16 * 1024));
    byte[] buffer = new byte[READ_BUFFER_BYTES];
    long total = 0;
    int read;
    while (true) {
      deadline.check();
      read = input.read(buffer);
      if (read == -1) {
        break;
      }
      total += read;
      if (total > maximum) {
        throw new IOException("Image exceeds the configured byte limit");
      }
      output.write(buffer, 0, read);
    }
    return output.toByteArray();
  }

  private static void closeQuietly(Socket socket) {
    if (socket == null) {
      return;
    }
    try {
      socket.close();
    } catch (IOException | RuntimeException ignore) {
      // Nothing useful can be done while releasing the socket.
    }
  }

  /** Marks the addresses that could not be used at all, so another answer may be tried. */
  private static final class PinnedConnectException extends IOException {

    private static final long serialVersionUID = 6820148459268479913L;

    PinnedConnectException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Aggregate time budget for one response, independent of any single socket read. */
  private static final class Deadline {

    private final boolean bounded;
    private final long endNanos;

    private Deadline(boolean bounded, long endNanos) {
      this.bounded = bounded;
      this.endNanos = endNanos;
    }

    /** Budget shared by the handshake, the request and the whole response. */
    static Deadline after(int budgetMillis) {
      return new Deadline(true, System.nanoTime() + budgetMillis * 1_000_000L);
    }

    /** Used for local files, which are not subject to the network read budget. */
    static Deadline unbounded() {
      return new Deadline(false, 0);
    }

    void check() throws IOException {
      if (bounded && System.nanoTime() - endNanos >= 0) {
        throw new IOException("Timed out reading the remote image");
      }
    }

    /** Remaining budget as a socket timeout; never {@code 0}, which would mean no timeout. */
    int remainingMillis() throws IOException {
      check();
      if (!bounded) {
        return Integer.MAX_VALUE;
      }
      long remaining = (endNanos - System.nanoTime()) / 1_000_000L;
      return (int) Math.max(1, Math.min(remaining, Integer.MAX_VALUE));
    }
  }
}
