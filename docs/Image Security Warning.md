## Security Warning

External image access is disabled by default. Embedded base64 raster images remain available and
are size-limited. Java callers can opt in through `GraphvizBuilder.securityPolicy(...)`.
External resources remain disabled in Batik/FOP-based PDF and TIFF conversion; use a bounded
embedded raster data URI for those formats.

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

### **Recommended Security Practices**

- Keep remote access disabled when rendering untrusted graphs.
- If remote access is required, enable it explicitly and allow-list each trusted DNS hostname with
  `allowRemoteImageHost(...)`.
- Set `localImageBaseDirectory(...)` to a dedicated directory; paths outside it are rejected.
- Keep the default byte, decoded-pixel, output-pixel, and timeout limits, or lower them for a service.
