# SVG Conversion Security

## Raw SVG Export

`DotRenderService.pngBytes(String)` retains its public signature but now uses the shared core
`BatikImgConverter.pngBytes(String, SecurityPolicy)` entry point. It does not transcode unchecked
SVG directly. No new module dependency is needed: UI already depends on core through DOT.

The core guard checks input length before parsing and runs a hardened SAX structural preflight
before allocating a DOM. Both SAX and DOM parsers prohibit doctypes, external entities, external
DTD loading, and XInclude. Only this exact literal declaration emitted by `SvgDocument` is stripped
from the start of the prolog (after an optional XML declaration and whitespace):

```xml
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
```

Every other doctype, including a declaration with that public/system ID plus an internal subset,
is rejected before DTD processing. No regex or general DTD removal is used. Processing instructions,
scripts, event attributes, animation, foreign content, stylesheets, inline CSS, and unsupported
elements/attributes are rejected. `use` and `symbol` instancing are forbidden, as are all `href`
references outside images and stripped navigation links, including local gradient inheritance
chains. Static primitives such as `rect`, gradients with `stop` children, and local `url(#paint)`
fills remain supported. Navigation links and tooltips are removed for conversion.

Raw SVG image references follow the supplied `SecurityPolicy`, just like graph image conversion.
Data images work by default; network sources require remote opt-in and an exact host allowlist,
and relative/filesystem sources require an approved local base directory. After structural and
attribute validation, `SecureImageLoader` loads approved sources with policy byte/time limits,
public-address validation, pinned remote connections, TLS hostname verification, and no redirects.
The image reader must recognize a single-frame PNG, JPEG, GIF, or BMP, validate its dimensions,
and successfully decode its raster. Data URI MIME declarations must match the detected format;
the canonical MIME for every resulting URI comes from the reader, not a filename or HTTP header.
Remote response headers still undergo the loader's raster content-type allowlist check.

Each image reference is replaced with a canonical base64 `xlink:href` data URI before Batik/FOP
receives the document. Ambiguous simultaneous `href` and `xlink:href` image attributes are rejected.
**Batik is never given an external image reference and external-resource loading stays disabled.**
This does not change SVG preview/export itself: an external SVG viewer uses its own loading rules.

Raster output requires positive, finite root dimensions in pixels (unitless or `px`) or points
(`pt`, converted at 96/72). Dimensions are rounded up and checked against `maxOutputPixels` before
transcoding. Context-dependent dimensions such as percentages are rejected. Embedded raster
images also undergo byte and image-pixel checks.

## Fixed Budgets

These conservative conversion ceilings apply to both raw and generated SVG, including PDF, and
cannot be raised by increasing the per-image `SecurityPolicy` settings:

| Resource | Maximum |
| --- | --- |
| Input Java string | 16,777,216 UTF-16 code units, checked before parsing |
| XML element nesting | 128, counting the root as depth 1 |
| XML elements | 200,000 |
| Attributes per element | 128, including namespace declarations |
| Aggregate attributes | 1,000,000, including namespace declarations |
| Image elements | 256 |
| Aggregate loaded image payloads | 32 MiB (after base64 decoding for data sources) |
| Aggregate canonical image data URIs | 48 MiB of ASCII, including headers and base64 padding |
| Aggregate decoded raster allowance | 128 MiB, conservatively charged at 8 bytes per pixel |

Image references are charged per occurrence, including duplicates. The loader's per-image byte
limit is clamped to the remaining aggregate loaded/encoded allowance before I/O or base64 decoding.
Decoded raster allowance and policy pixel limits are checked before raster allocation. Multi-frame
images are rejected rather than permitting unaccounted animation frames. The existing per-image
policy byte/pixel limits and raster output pixel limit may impose tighter bounds.

These controls address specific XML allocation and resource/instancing expansion risks. They are
**not a promise that arbitrary untrusted SVG complexity is safe**, nor a process-memory or CPU-time
limit. Paths, text shaping, clipping, image codecs, and Batik/FOP can still consume substantial
resources within these ceilings. Isolate hostile workloads and enforce process-level memory/time
limits where needed. Corpus compatibility and integration checks must be run before release.

## Converter Availability

Batik/FOP must accept both `KEY_ALLOW_EXTERNAL_RESOURCES=false` and `KEY_EXECUTE_ONLOAD=false`.
A missing or rejected required hint aborts direct conversion and deselects the converter during
environment discovery. A diagnostic is logged once, but conversion never continues without the
protection. The availability of these hints since Batik 1.13 is a capability minimum, not a
recommendation to use that old release; use the project's supported modern Batik/FOP versions.

Graph PNG/JPEG rendering can fall back to the native AWT converter. TIFF and PDF have no native
fallback and report that a secure converter is unavailable, with dependency guidance. Raw SVG
PNG export cannot use the graph-only native renderer and fails closed if secure Batik is absent.

PDF is vector output. `maxOutputPixels` is not a limit on PDF page dimensions or overall PDF
complexity, nor a general memory guarantee for FOP. The static SVG/resource restrictions and
embedded image limits still apply to PDF conversion. This change does not introduce PDF rasterization.

## Integration Checks

Regression coverage is in `SecureSvgTest`, `BatikSecurityHintTest`, and
`DotRenderServiceSecurityPolicyTest`, alongside the existing `DotRenderServiceTest` PNG smoke test.
`SecureSvgTest` checks shallow instancing bombs without transcoding, deep XML failing with ordinary
exceptions, input/element/attribute/image budgets, DTD rejection, benign raw rectangles and stops,
approved local/data canonicalization, MIME mismatch, truncated raster rejection, aggregate image
limits, and zero requests to allowlisted private addresses. Integration coverage should assert
that arbitrary external doctypes are rejected and approved local images are embedded before
transcoding. Run Java verification with `-Djava.awt.headless=true`.
