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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.graphper.api.SecurityPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Exercises the remote image loader without touching the network: every fetch is pinned onto a
 * loopback stub server, which is exactly the seam the destination enforcement introduces.
 */
public class SecureImageLoaderTest {

  private static final byte[] PNG_HEADER = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

  private static final String HOST = "images.example.test";

  @TempDir
  Path temporaryDirectory;

  @Test
  public void okImageResponseIsReadAndKeepsTheOriginalHostHeader() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      byte[] bytes = SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                                   policy(4096, 5000));
      Assertions.assertArrayEquals(PNG_HEADER, bytes);
      String request = server.request();
      Assertions.assertTrue(request.startsWith("GET /logo.png HTTP/1.1"), request);
      Assertions.assertTrue(request.contains("Host: " + HOST + ":" + server.port()), request);
      Assertions.assertTrue(server.observedClientDisconnect(), "socket was not released");
    }
  }

  @Test
  public void queryStringIsForwardedInTheRequestTarget() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      SecureImageLoader.fetchPinned(server.uri("/logo.png?v=2&size=1"), server.address(),
                                    policy(4096, 5000));
      Assertions.assertTrue(server.request().startsWith("GET /logo.png?v=2&size=1 HTTP/1.1"),
                            server.request());
    }
  }

  @Test
  public void informationalResponseIsSkippedBeforeTheRealStatus() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "100 Continue");
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      Assertions.assertArrayEquals(PNG_HEADER,
                                   SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                 server.address(),
                                                                 policy(4096, 5000)));
    }
  }

  @Test
  public void redirectBodyIsRejectedInsteadOfDecoded() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "302 Found", "Location: http://169.254.169.254/latest/meta-data/",
                "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(4096, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("302"), failure.getMessage());
      Assertions.assertTrue(server.observedClientDisconnect(), "socket was not released");
    }
  }

  @Test
  public void nonSuccessStatusIsRejected() throws Exception {
    for (String status : new String[]{"201 Created", "204 No Content", "304 Not Modified",
        "404 Not Found", "500 Internal Server Error"}) {
      try (StubServer server = new StubServer(out -> {
        writeHead(out, status, "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
        out.write(PNG_HEADER);
        out.flush();
      })) {
        IOException failure = Assertions.assertThrows(
            IOException.class,
            () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                                policy(4096, 5000)));
        Assertions.assertTrue(failure.getMessage().contains(status.substring(0, 3)),
                              failure.getMessage());
      }
    }
  }

  @Test
  public void nonRasterContentTypeIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: text/html; charset=utf-8", "Content-Length: 5");
      out.write("<html".getBytes(StandardCharsets.US_ASCII));
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(4096, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("content type"), failure.getMessage());
      Assertions.assertTrue(server.observedClientDisconnect(), "socket was not released");
    }
  }

  @Test
  public void missingContentTypeIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      Assertions.assertThrows(IOException.class,
                              () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                  server.address(),
                                                                  policy(4096, 5000)));
    }
  }

  @Test
  public void parameterisedRasterContentTypeIsAccepted() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: IMAGE/PNG ; charset=binary",
                "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      Assertions.assertArrayEquals(PNG_HEADER,
                                   SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                 server.address(),
                                                                 policy(4096, 5000)));
    }
  }

  @Test
  public void oversizedAdvertisedLengthIsRejectedBeforeTheBodyAndReleasesTheSocket()
      throws Exception {
    // The stub deliberately sends no body at all: the rejection must come from the header alone.
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: 9999999");
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(64, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("byte limit"), failure.getMessage());
      Assertions.assertTrue(server.observedClientDisconnect(), "socket was not released");
    }
  }

  @Test
  public void unframedBodyWithinTheLimitIsReadUntilEndOfStream() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png");
      out.write(PNG_HEADER);
      out.flush();
    })) {
      Assertions.assertArrayEquals(PNG_HEADER,
                                   SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                 server.address(),
                                                                 policy(4096, 5000)));
    }
  }

  @Test
  public void unannouncedOversizedBodyIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png");
      out.write(new byte[4096]);
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(64, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("byte limit"), failure.getMessage());
    }
  }

  @Test
  public void truncatedBodyIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: 32");
      out.write(PNG_HEADER);
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(4096, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("truncated"), failure.getMessage());
    }
  }

  @Test
  public void chunkedBodyIsDecoded() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Transfer-Encoding: chunked");
      out.write("4\r\n".getBytes(StandardCharsets.US_ASCII));
      out.write(PNG_HEADER, 0, 4);
      out.write("\r\n4;padding=1\r\n".getBytes(StandardCharsets.US_ASCII));
      out.write(PNG_HEADER, 4, 4);
      out.write("\r\n0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
      out.flush();
    })) {
      Assertions.assertArrayEquals(PNG_HEADER,
                                   SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                 server.address(),
                                                                 policy(4096, 5000)));
    }
  }

  @Test
  public void oversizedChunkedBodyIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Transfer-Encoding: chunked");
      out.write("400\r\n".getBytes(StandardCharsets.US_ASCII));
      out.write(new byte[0x400]);
      out.write("\r\n0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(64, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("byte limit"), failure.getMessage());
    }
  }

  @Test
  public void chunkSizeCannotOverflowAfterOneByte() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Transfer-Encoding: chunked");
      // No second chunk body: rejection must occur on the size, not EOF or a timeout.
      out.write("1\r\nx\r\n7fffffffffffffff\r\n".getBytes(StandardCharsets.US_ASCII));
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(64, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("byte limit"), failure.getMessage());
      Assertions.assertTrue(server.observedClientDisconnect());
    }
  }

  @Test
  public void mixedFramingHeadersAreRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Transfer-Encoding: chunked",
                "Content-Length: 8");
      out.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
      out.flush();
    })) {
      Assertions.assertThrows(IOException.class,
                              () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                  server.address(),
                                                                  policy(4096, 5000)));
    }
  }

  @Test
  public void compressedResponseIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Encoding: gzip",
                "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(4096, 5000)));
      Assertions.assertTrue(failure.getMessage().contains("content encoding"),
                            failure.getMessage());
    }
  }

  @Test
  public void nonHttpGarbageIsRejected() throws Exception {
    try (StubServer server = new StubServer(out -> {
      out.write("* OK IMAP4rev1 ready\r\n".getBytes(StandardCharsets.US_ASCII));
      out.flush();
    })) {
      Assertions.assertThrows(IOException.class,
                              () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"),
                                                                  server.address(),
                                                                  policy(4096, 5000)));
    }
  }

  @Test
  public void slowDripCannotOutlastTheAggregateReadBudget() throws Exception {
    // Each single read completes well inside the socket timeout, so only an aggregate deadline can
    // stop this. Left unbounded the stub would keep the caller busy for roughly sixteen seconds.
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png");
      for (int i = 0; i < 400; i++) {
        out.write(1);
        out.flush();
        sleep(40);
      }
    })) {
      long start = System.nanoTime();
      IOException failure = Assertions.assertThrows(
          IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                              policy(1024 * 1024, 400)));
      long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
      Assertions.assertTrue(elapsedMillis < 4_000,
                            "aggregate budget was not enforced, took " + elapsedMillis + "ms");
      Assertions.assertTrue(failure.getMessage().toLowerCase().contains("timed out"),
                            failure.getMessage());
    }
  }

  @Test
  public void stalledTlsRecordCannotExtendHandshakeDeadline() throws Exception {
    // Initialize the provider before timing the exchange; no TLS server or trust override needed.
    SSLSocketFactory.getDefault();
    AtomicInteger firstClientByte = new AtomicInteger(-1);
    try (RawServer server = new RawServer(socket -> {
      firstClientByte.set(socket.getInputStream().read());
      OutputStream output = socket.getOutputStream();
      // A valid TLS record header with a deliberately incomplete handshake payload. Each drip is
      // below SO_TIMEOUT, so a per-read timeout alone would keep the handshake alive for seconds.
      output.write(new byte[]{22, 3, 3, 0x40, 0});
      for (int i = 0; i < 150; i++) {
        output.write(0);
        output.flush();
        sleep(40);
      }
    })) {
      long start = System.nanoTime();
      IOException failure = Assertions.assertThrows(IOException.class,
          () -> SecureImageLoader.fetchPinned(server.uri("https", "/logo.png"),
                                              server.address(), policy(4096, 700)));
      long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
      Assertions.assertEquals(22, firstClientByte.get(), "client must actually begin TLS");
      Assertions.assertTrue(failure.getMessage().toLowerCase().contains("timed out"),
                            failure.getMessage());
      Assertions.assertTrue(elapsedMillis < 3000, "handshake took " + elapsedMillis + "ms");
    }
  }

  @Test
  public void blockedRequestWriteIsClosedAtTheDeadline() throws Exception {
    CountDownLatch release = new CountDownLatch(1);
    try (RawServer server = new RawServer(socket -> {
      try {
        release.await(6, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    })) {
      char[] path = new char[8 * 1024 * 1024];
      Arrays.fill(path, 'a');
      URI uri = server.uri("http", "/" + new String(path));
      long start = System.nanoTime();
      IOException failure = Assertions.assertThrows(IOException.class,
          () -> SecureImageLoader.fetchPinned(uri, server.address(), policy(4096, 700)));
      long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
      Assertions.assertTrue(failure.getMessage().toLowerCase().contains("timed out"),
                            failure.getMessage());
      Assertions.assertTrue(elapsedMillis < 3000, "blocked write took " + elapsedMillis + "ms");
    } finally {
      release.countDown();
    }
  }

  @Test
  public void completedFetchDoesNotRetainItsWatchdogTask() throws Exception {
    Field field = SecureImageLoader.class.getDeclaredField("WATCHDOG");
    field.setAccessible(true);
    ScheduledThreadPoolExecutor watchdog = (ScheduledThreadPoolExecutor) field.get(null);
    int queued = watchdog.getQueue().size();
    try (StubServer server = new StubServer(out ->
        writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: 0"))) {
      SecureImageLoader.fetchPinned(server.uri("/logo.png"), server.address(),
                                    policy(4096, 60_000));
    }
    Assertions.assertTrue(watchdog.getRemoveOnCancelPolicy());
    Assertions.assertEquals(queued, watchdog.getQueue().size());
    Assertions.assertEquals(1, watchdog.getCorePoolSize());
  }

  @Test
  public void watchdogCapacityExhaustionFailsBeforeConnecting() throws Exception {
    Field field = SecureImageLoader.class.getDeclaredField("FETCH_SLOTS");
    field.setAccessible(true);
    Semaphore slots = (Semaphore) field.get(null);
    int permits = slots.drainPermits();
    try {
      Assertions.assertEquals(128, permits);
      IOException failure = Assertions.assertThrows(IOException.class,
          () -> SecureImageLoader.fetchPinned(URI.create("http://" + HOST + "/logo.png"),
                                              InetAddress.getLoopbackAddress(), policy(4096, 100)));
      Assertions.assertTrue(failure.getMessage().contains("capacity"), failure.getMessage());
      Assertions.assertEquals(0, slots.availablePermits());
    } finally {
      slots.release(permits);
    }
  }

  @Test
  public void saturatedDnsPoolRejectsWithoutQueueingOrSpawningMoreWorkers() throws Exception {
    Field field = SecureImageLoader.class.getDeclaredField("DNS_EXECUTOR");
    field.setAccessible(true);
    ThreadPoolExecutor executor = (ThreadPoolExecutor) field.get(null);
    int maximum = executor.getMaximumPoolSize();
    CountDownLatch started = new CountDownLatch(maximum);
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch finished = new CountDownLatch(maximum);
    AtomicInteger daemonWorkers = new AtomicInteger();
    try {
      for (int i = 0; i < maximum; i++) {
        CountDownLatch workerStarted = new CountDownLatch(1);
        FutureTask<Void> lookup = new FutureTask<>(() -> {
          if (Thread.currentThread().isDaemon()) {
            daemonWorkers.incrementAndGet();
          }
          started.countDown();
          workerStarted.countDown();
          // Model native name resolution that ignores cancellation.
          while (release.getCount() != 0) {
            try {
              release.await();
            } catch (InterruptedException ignored) {
              // Native DNS is not guaranteed to respond to interrupts.
            }
          }
          finished.countDown();
          return null;
        });
        executor.execute(lookup);
        Assertions.assertTrue(workerStarted.await(3, TimeUnit.SECONDS));
        Assertions.assertThrows(TimeoutException.class, () -> lookup.get(50, TimeUnit.MILLISECONDS));
        lookup.cancel(true);
      }
      Assertions.assertTrue(started.await(3, TimeUnit.SECONDS));
      Method resolve = SecureImageLoader.class.getDeclaredMethod("resolveWithin", String.class,
                                                                int.class);
      resolve.setAccessible(true);
      long start = System.nanoTime();
      for (int i = 0; i < 20; i++) {
        InvocationTargetException failure = Assertions.assertThrows(InvocationTargetException.class,
            () -> resolve.invoke(null, "127.0.0.1", 100));
        Assertions.assertTrue(failure.getCause() instanceof IOException);
        Assertions.assertTrue(failure.getCause().getMessage().contains("capacity"));
      }
      Assertions.assertTrue((System.nanoTime() - start) / 1_000_000L < 2000);
      Assertions.assertEquals(4, maximum);
      Assertions.assertEquals(maximum, daemonWorkers.get());
      Assertions.assertEquals(maximum, executor.getLargestPoolSize());
      Assertions.assertEquals(0, executor.getQueue().size());
      Assertions.assertEquals(0, executor.getQueue().remainingCapacity());
    } finally {
      release.countDown();
      Assertions.assertTrue(finished.await(3, TimeUnit.SECONDS));
    }
  }

  @Test
  public void allowListedButLoopbackHostIsNeverContacted() throws Exception {
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      SecurityPolicy policy = SecurityPolicy.builder().allowRemoteImages(true)
          .allowRemoteImageHost("localhost").build();
      String reference = "http://localhost:" + server.port() + "/logo.png";
      IOException failure = Assertions.assertThrows(
          IOException.class, () -> SecureImageLoader.load(reference, policy));
      Assertions.assertTrue(failure.getMessage().contains("non-public"), failure.getMessage());
      Assertions.assertNull(server.request(), "the stub server should never have been reached");
    }
  }

  @Test
  public void tlsIdentityStaysBoundToTheHostnameNotThePinnedAddress() throws Exception {
    SSLSocket ssl = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
    try {
      SecureImageLoader.bindTlsIdentity(ssl, HOST);
      SSLParameters parameters = ssl.getSSLParameters();
      Assertions.assertEquals("HTTPS", parameters.getEndpointIdentificationAlgorithm());
      List<SNIServerName> names = parameters.getServerNames();
      Assertions.assertNotNull(names);
      Assertions.assertEquals(1, names.size());
      Assertions.assertEquals(HOST, ((SNIHostName) names.get(0)).getAsciiName());
    } finally {
      ssl.close();
    }
  }

  @Test
  public void literalAddressHostKeepsVerificationAndSendsNoSni() throws Exception {
    SSLSocket ssl = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
    try {
      SecureImageLoader.bindTlsIdentity(ssl, "203.0.113.7");
      SSLParameters parameters = ssl.getSSLParameters();
      Assertions.assertEquals("HTTPS", parameters.getEndpointIdentificationAlgorithm());
      Assertions.assertTrue(parameters.getServerNames() == null
                                || parameters.getServerNames().isEmpty(),
                            String.valueOf(parameters.getServerNames()));
    } finally {
      ssl.close();
    }
  }

  @Test
  public void theCleanupAssertionHasTeeth() throws Exception {
    // Guards the other tests: a client that keeps the socket must leave the latch closed, so
    // observedClientDisconnect() cannot be vacuously true.
    try (StubServer server = new StubServer(out -> {
      writeHead(out, "200 OK", "Content-Type: image/png", "Content-Length: " + PNG_HEADER.length);
      out.write(PNG_HEADER);
      out.flush();
    })) {
      Socket leaked = new Socket(server.address(), server.port());
      try {
        leaked.getOutputStream().write(("GET /logo.png HTTP/1.1\r\nHost: " + HOST + "\r\n\r\n")
                                           .getBytes(StandardCharsets.US_ASCII));
        leaked.getOutputStream().flush();
        Assertions.assertFalse(server.observedClientDisconnect(800));
      } finally {
        leaked.close();
      }
      Assertions.assertTrue(server.observedClientDisconnect());
    }
  }

  @Test
  public void privateAndSpecialUseAddressesAreNotPublic() throws Exception {
    String[] rejected = {"127.0.0.1", "0.0.0.0", "10.1.2.3", "172.16.0.1", "192.168.1.1",
        "0.0.0.1", "0.255.255.255", "240.0.0.0", "255.255.255.255",
        "198.18.0.0", "198.19.255.255", "192.0.0.1", "192.0.0.255", "192.0.2.1",
        "192.88.99.1", "198.51.100.1", "203.0.113.1", "2001:db8::1", "2001::1",
        "2001:2::1", "64:ff9b:1::a00:1", "100::1", "3fff::1", "5f00::1",
        "2002:c612:1::1", "64:ff9b::240.0.0.1", "::0.1.2.3",
        "169.254.169.254", "100.64.0.1", "100.127.255.255", "224.0.0.1", "::1", "fc00::1",
        "fd12:3456::1", "fe80::1", "ff02::1",
        // IPv4 destinations smuggled inside an IPv6 answer.
        "::ffff:127.0.0.1", "::ffff:10.1.2.3", "::169.254.169.254", "::192.168.1.1",
        "2002:a01:203::1", "2002:7f00:1::1", "64:ff9b::169.254.169.254", "64:ff9b::10.1.2.3"};
    for (String literal : rejected) {
      Assertions.assertFalse(SecureImageLoader.isPublicAddress(InetAddress.getByName(literal)),
                             literal + " must not count as public");
    }
    String[] accepted = {"1.1.1.1", "8.8.8.8", "100.63.255.255", "100.128.0.1",
        "198.17.255.255", "198.20.0.0", "192.0.1.1", "192.88.98.1", "192.88.100.1",
        "2001:4860:4860::8888", "2002:808:808::1", "64:ff9b::8.8.8.8"};
    for (String literal : accepted) {
      Assertions.assertTrue(SecureImageLoader.isPublicAddress(InetAddress.getByName(literal)),
                            literal + " must count as public");
    }
    Assertions.assertFalse(SecureImageLoader.isPublicAddress(null));
  }

  @Test
  public void referencesOutsideThePolicyNeverReachTheNetwork() {
    SecurityPolicy policy = SecurityPolicy.defaultPolicy();
    IOException failure = Assertions.assertThrows(
        IOException.class, () -> SecureImageLoader.load("http://example.com/a.png", policy));
    Assertions.assertTrue(failure.getMessage().contains("denied"), failure.getMessage());
  }

  @Test
  public void embeddedAndLocalReferencesStillLoad() throws Exception {
    byte[] payload = {1, 2, 3, 4};
    String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(payload);
    Assertions.assertArrayEquals(payload,
                                 SecureImageLoader.load(dataUri, policy(4096, 5000)));

    Path image = Files.write(temporaryDirectory.resolve("local.png"), payload);
    SecurityPolicy filePolicy = SecurityPolicy.builder()
        .localImageBaseDirectory(temporaryDirectory).build();
    Assertions.assertArrayEquals(payload,
                                 SecureImageLoader.load(image.toString(), filePolicy));
  }

  @Test
  public void embeddedImageOverTheByteLimitIsRejected() {
    SecurityPolicy policy = SecurityPolicy.builder().maxImageBytes(4).build();
    String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[64]);
    Assertions.assertThrows(IOException.class, () -> SecureImageLoader.load(dataUri, policy));
  }

  private static SecurityPolicy policy(int maxBytes, int readTimeout) {
    return SecurityPolicy.builder().allowRemoteImages(true).allowRemoteImageHost(HOST)
        .maxImageBytes(maxBytes).connectTimeoutMillis(5000).readTimeoutMillis(readTimeout).build();
  }

  private static void writeHead(OutputStream output, String status, String... headers)
      throws IOException {
    StringBuilder head = new StringBuilder("HTTP/1.1 ").append(status).append("\r\n");
    for (String header : headers) {
      head.append(header).append("\r\n");
    }
    head.append("Connection: close\r\n\r\n");
    output.write(head.toString().getBytes(StandardCharsets.US_ASCII));
    output.flush();
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private interface Responder {

    void respond(OutputStream output) throws IOException;
  }

  private interface RawResponder {

    void respond(Socket socket) throws IOException;
  }

  /** Does not parse HTTP, so tests exercise the real TLS handshake and blocked request writes. */
  private static final class RawServer implements Closeable {

    private final ServerSocket listener;
    private final Thread worker;
    private volatile Socket accepted;

    RawServer(RawResponder responder) throws IOException {
      listener = new ServerSocket();
      listener.setReceiveBufferSize(1024);
      listener.bind(new java.net.InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
      worker = new Thread(() -> {
        try (Socket socket = listener.accept()) {
          accepted = socket;
          socket.setSoTimeout(5000);
          responder.respond(socket);
        } catch (IOException ignored) {
          // Deadline enforcement closes the client while the server is still sending or waiting.
        }
      }, "secure-image-loader-raw-stub");
      worker.setDaemon(true);
      worker.start();
    }

    InetAddress address() {
      return listener.getInetAddress();
    }

    URI uri(String scheme, String path) {
      return URI.create(scheme + "://" + HOST + ":" + listener.getLocalPort() + path);
    }

    @Override
    public void close() throws IOException {
      listener.close();
      if (accepted != null) {
        accepted.close();
      }
      worker.interrupt();
      try {
        worker.join(3000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /** Single-connection loopback server that also records whether the client released the socket. */
  private static final class StubServer implements Closeable {

    private final ServerSocket serverSocket;
    private final Thread worker;
    private final CountDownLatch disconnected = new CountDownLatch(1);
    private volatile String request;

    StubServer(Responder responder) throws IOException {
      serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
      worker = new Thread(() -> {
        try (Socket socket = serverSocket.accept()) {
          socket.setSoTimeout(5_000);
          request = readRequestHead(socket.getInputStream());
          responder.respond(socket.getOutputStream());
          // Honour the Connection: close the loader asks for, so an unframed body ends on EOF.
          socket.shutdownOutput();
          drainUntilDisconnect(socket.getInputStream());
        } catch (SocketTimeoutException e) {
          // A stalled peer is reported by leaving the latch closed.
        } catch (IOException | RuntimeException e) {
          // A reset while writing or draining also means the client let go of the socket.
          disconnected.countDown();
        }
      }, "secure-image-loader-stub");
      worker.setDaemon(true);
      worker.start();
    }

    private void drainUntilDisconnect(InputStream input) throws IOException {
      while (input.read() != -1) {
        // Ignore anything still in flight; only the end of stream matters here.
      }
      disconnected.countDown();
    }

    private static String readRequestHead(InputStream input) throws IOException {
      ByteArrayOutputStream head = new ByteArrayOutputStream();
      int read;
      while ((read = input.read()) != -1) {
        head.write(read);
        byte[] seen = head.toByteArray();
        if (seen.length >= 4 && seen[seen.length - 4] == '\r' && seen[seen.length - 3] == '\n'
            && seen[seen.length - 2] == '\r' && seen[seen.length - 1] == '\n') {
          break;
        }
      }
      return new String(head.toByteArray(), StandardCharsets.US_ASCII);
    }

    int port() {
      return serverSocket.getLocalPort();
    }

    InetAddress address() {
      return serverSocket.getInetAddress();
    }

    URI uri(String path) {
      return URI.create("http://" + HOST + ":" + port() + path);
    }

    String request() {
      return request;
    }

    boolean observedClientDisconnect() throws InterruptedException {
      return observedClientDisconnect(3_000);
    }

    boolean observedClientDisconnect(long waitMillis) throws InterruptedException {
      return disconnected.await(waitMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws IOException {
      serverSocket.close();
      worker.interrupt();
    }
  }
}
