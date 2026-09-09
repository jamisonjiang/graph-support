## Security Warning

External image access is disabled by default. Embedded base64 raster images remain available and
are size-limited. Java callers can opt in through `GraphvizBuilder.securityPolicy(...)`.

### 1. Remote URL Risks

- Loading images from **untrusted URLs** may expose the application to **Server-Side Request Forgery (SSRF)** attacks.
- External images may cause **slow network requests** or **serve malicious payloads**.

### 2. Local File Risks

- Unvalidated local file paths can lead to **unauthorized system file access (Path Traversal vulnerability)**.
- If the application runs with **high privileges**, it may **expose sensitive files**.

### 3. Large Image Risks

- **Very large images** can cause **high memory usage**, leading to **Denial of Service (DoS) attacks**.
- Excessive image requests can slow down rendering and increase resource consumption.

------

### What the built-in loader enforces

These apply to fetches graph-support performs itself, which is the built-in raster converter. It
handles `PNG`, `JPG`, `JPEG` and `GIF`, and is preferred for those formats whenever the graph
contains an image:

- A reference is usable only with `allowRemoteImages(true)` **and** exact membership of its host in
  `allowRemoteImageHost(...)`. Matching is exact after IDN and case normalization; subdomains and
  parent domains are not implied. `userinfo` in the URL is rejected.
- The hostname is resolved once, the reference is refused unless **every** resolved address is
  public, and the socket is then connected to one of exactly those addresses. The address that was
  validated is therefore the address that is reached: there is no second, unchecked resolution
  between the check and the connection.
- Non-public includes any-local, loopback, link-local, site-local, IPv6 unique-local,
  carrier-grade-NAT (`100.64.0.0/10`) and multicast. The conservative filter also denies IPv4
  `0.0.0.0/8`, reserved `240.0.0.0/4`, benchmarking `198.18.0.0/15`, protocol-assignment
  `192.0.0.0/24`, deprecated relay `192.88.99.0/24` and the documentation ranges
  `192.0.2.0/24`, `198.51.100.0/24`, `203.0.113.0/24`. IPv6 must be global unicast or a
  recognized embedded form; special assignments `2001::/23` and documentation prefixes
  `2001:db8::/32`, `3fff::/20` are denied. An IPv6 answer that embeds an IPv4 destination
  (IPv4-mapped, IPv4-compatible, 6to4 `2002::/16`, NAT64 `64:ff9b::/96`) is classified by the
  embedded address too.
- Redirects are never followed and only an HTTP `200` response is accepted, so a `3xx` body is
  discarded rather than decoded.
- The response must declare a raster media type that the decoder actually supports. This is defence
  in depth against obviously wrong payloads; it does not affect request forgery.
- `connectTimeoutMillis` separately bounds hostname resolution and the TCP connect.
  DNS uses at most four shared daemon workers with no pending lookup queue. If native resolution
  ignores interruption after a caller times out, that worker remains occupied; further lookups
  fail closed immediately when all workers are occupied, rather than creating more threads.
  `readTimeoutMillis` is the **total** budget for the TLS handshake, the request and every read of
  the response, starting after TCP connect. A shared daemon watchdog closes the underlying socket
  at the deadline, including during blocked writes and slow TLS handshakes. At most 128 exchanges
  can hold watchdog capacity; excess concurrent fetches fail closed. Completed exchanges cancel
  and immediately remove their watchdog tasks.
- `maxImageBytes` is checked against the advertised `Content-Length` before any body byte is read
  and again while reading. Chunk sizes are compared against the remaining budget without adding
  attacker-controlled lengths, and each chunk write is guarded. The socket is released on every
  path, including every rejection path.
- HTTPS keeps the original hostname for the `Host` header, for the TLS SNI extension and for
  certificate hostname verification. Verification is required: if it cannot be enabled the fetch
  fails rather than continuing unverified.
- Deserialized `SecurityPolicy` instances must satisfy the same positive limits, normalized host
  allow-list and absolute normalized local-base invariants as builder-created policies. The host
  set is defensively copied. Valid serialized policies retain the existing field format and UID;
  invalid policy state is rejected with `InvalidObjectException`.

### What is *not* covered

- **SVG output.** The approved reference is embedded as an `xlink:href` and graph-support fetches
  nothing. Whatever renders the SVG issues its own request under its own rules.
- **Batik / FOP conversion.** Active content and disallowed image sources are rejected. Approved
  images are fetched through the policy-controlled loader, validated, and embedded as canonical
  base64 data URIs before conversion. Batik/FOP external-resource loading remains disabled,
  and required security hints must be available and successfully applied. Missing or broken hints
  disable that converter rather than continuing without protection. The external-resource switch
  (`SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES`) exists from **Batik 1.13** onwards.
  Graph PNG/JPEG output can use the native converter; TIFF/PDF require a supported converter.
  The UI PNG export uses the same validation and enforces the configured output-pixel budget.
  See [SVG Conversion Security](SVG%20Conversion%20Security.md) for accepted SVG content and
  vector PDF limitations. `graph-support-core` does not declare Batik itself, so check the version
  your application supplies.
- **DNS-level trust.** Pinning guarantees the connection reaches an address that passed the public
  address check. It cannot tell whether that public address is the host you meant; that is what the
  host allow-list and TLS certificate verification are for.
- **Egress proxies.** The loader connects directly and ignores the JVM `http.proxyHost` /
  `https.proxyHost` settings, because a validated address cannot be enforced through a proxy that
  resolves the hostname itself. If your deployment requires an egress proxy for outbound traffic,
  keep remote images disabled and embed bounded data URIs instead.

------

### **Recommended Security Practices**

- Keep remote access disabled when rendering untrusted graphs.
- If remote access is required, enable it explicitly and allow-list each trusted DNS hostname with
  `allowRemoteImageHost(...)`.
- Set `localImageBaseDirectory(...)` to a dedicated directory; paths outside it are rejected.
- Keep the default byte, decoded-pixel, output-pixel, and timeout limits, or lower them for a service.
- Prefer bounded embedded raster data URIs, or the built-in raster converter, for untrusted input.
  If you need `PDF` or Batik-backed output, run Batik 1.13 or newer.
