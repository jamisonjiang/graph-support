## Security Warning

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

✅ **Use only trusted domains** or pre-validated image URLs.
 ✅ **Restrict local file paths** to a dedicated, controlled directory.
 ✅ **Limit image file sizes** to prevent excessive memory usage.
 ✅ **Sanitize file extensions** and **content types** to allow only valid image formats (`PNG`, `JPG`, `JPEG`).