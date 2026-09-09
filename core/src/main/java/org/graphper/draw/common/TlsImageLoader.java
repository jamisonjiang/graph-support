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
import java.net.Socket;
import java.util.Collections;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** Optional TLS support, loaded reflectively only for an HTTPS image exchange. */
final class TlsImageLoader {

  private TlsImageLoader() {}

  static Socket start(Socket plain, String host, int port, int timeoutMillis) throws IOException {
    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    // Preserve the allow-listed peer name while reusing the already validated TCP connection.
    SSLSocket ssl = (SSLSocket) factory.createSocket(plain, host, port, true);
    bindTlsIdentity(ssl, host);
    ssl.setSoTimeout(timeoutMillis);
    ssl.startHandshake();
    return ssl;
  }

  /** Fail closed if the provider cannot enable HTTPS certificate hostname verification. */
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
}
